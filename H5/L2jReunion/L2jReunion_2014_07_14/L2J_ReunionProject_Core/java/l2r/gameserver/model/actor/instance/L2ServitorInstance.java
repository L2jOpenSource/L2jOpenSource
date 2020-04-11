/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.actor.instance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javolution.util.FastList;
import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.datatables.SummonEffectsTable;
import l2r.gameserver.datatables.SummonEffectsTable.SummonEffect;
import l2r.gameserver.datatables.sql.CharSummonTable;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.l2skills.L2SkillSummon;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.network.serverpackets.SetSummonRemainTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class L2ServitorInstance extends L2Summon
{
	protected static final Logger _log = LoggerFactory.getLogger(L2ServitorInstance.class);
	
	private static final String ADD_SKILL_SAVE = "INSERT INTO character_summon_skills_save (ownerId,ownerClassIndex,summonSkillId,skill_id,skill_level,effect_count,effect_cur_time,buff_index) VALUES (?,?,?,?,?,?,?,?)";
	private static final String RESTORE_SKILL_SAVE = "SELECT skill_id,skill_level,effect_count,effect_cur_time,buff_index FROM character_summon_skills_save WHERE ownerId=? AND ownerClassIndex=? AND summonSkillId=? ORDER BY buff_index ASC";
	private static final String DELETE_SKILL_SAVE = "DELETE FROM character_summon_skills_save WHERE ownerId=? AND ownerClassIndex=? AND summonSkillId=?";
	
	private float _expPenalty = 0; // exp decrease multiplier (i.e. 0.3 (= 30%) for shadow)
	private int _itemConsumeId;
	private int _itemConsumeCount;
	private int _itemConsumeSteps;
	private final int _totalLifeTime;
	private final int _timeLostIdle;
	private final int _timeLostActive;
	private int _timeRemaining;
	private int _nextItemConsumeTime;
	public int lastShowntimeRemaining; // Following FbiAgent's example to avoid sending useless packets
	
	protected Future<?> _summonLifeTask;
	
	private int _referenceSkill;
	
	private boolean _shareElementals = false;
	private double _sharedElementalsPercent = 1;
	
	public L2ServitorInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2Skill skill)
	{
		super(objectId, template, owner);
		setInstanceType(InstanceType.L2ServitorInstance);
		setShowSummonAnimation(true);
		
		if (skill != null)
		{
			final L2SkillSummon summonSkill = (L2SkillSummon) skill;
			_itemConsumeId = summonSkill.getItemConsumeIdOT();
			_itemConsumeCount = summonSkill.getItemConsumeOT();
			_itemConsumeSteps = summonSkill.getItemConsumeSteps();
			_totalLifeTime = summonSkill.getTotalLifeTime();
			_timeLostIdle = summonSkill.getTimeLostIdle();
			_timeLostActive = summonSkill.getTimeLostActive();
			_referenceSkill = summonSkill.getId();
		}
		else
		{
			// defaults
			_itemConsumeId = 0;
			_itemConsumeCount = 0;
			_itemConsumeSteps = 0;
			_totalLifeTime = 1200000; // 20 minutes
			_timeLostIdle = 1000;
			_timeLostActive = 1000;
		}
		_timeRemaining = _totalLifeTime;
		lastShowntimeRemaining = _totalLifeTime;
		
		if (_itemConsumeId == 0)
		{
			_nextItemConsumeTime = -1; // do not consume
		}
		else if (_itemConsumeSteps == 0)
		{
			_nextItemConsumeTime = -1; // do not consume
		}
		else
		{
			_nextItemConsumeTime = _totalLifeTime - (_totalLifeTime / (_itemConsumeSteps + 1));
		}
		
		// When no item consume is defined task only need to check when summon life time has ended.
		// Otherwise have to destroy items from owner's inventory in order to let summon live.
		int delay = 1000;
		
		if (Config.DEBUG && (_itemConsumeCount != 0))
		{
			_log.warn(getClass().getSimpleName() + ": Item Consume ID: " + _itemConsumeId + ", Count: " + _itemConsumeCount + ", Rate: " + _itemConsumeSteps + " times.");
		}
		if (Config.DEBUG)
		{
			_log.warn(getClass().getSimpleName() + ": Task Delay " + (delay / 1000) + " seconds.");
		}
		
		_summonLifeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new SummonLifetime(getOwner(), this), delay, delay);
	}
	
	@Override
	public final int getLevel()
	{
		return (getTemplate() != null ? getTemplate().getLevel() : 0);
	}
	
	@Override
	public int getSummonType()
	{
		return 1;
	}
	
	public void setExpPenalty(float expPenalty)
	{
		_expPenalty = expPenalty;
	}
	
	public float getExpPenalty()
	{
		return _expPenalty;
	}
	
	public void setSharedElementals(final boolean val)
	{
		_shareElementals = val;
	}
	
	public boolean isSharingElementals()
	{
		return _shareElementals;
	}
	
	public void setSharedElementalsValue(final double val)
	{
		_sharedElementalsPercent = val;
	}
	
	public double sharedElementalsPercent()
	{
		return _sharedElementalsPercent;
	}
	
	public int getItemConsumeCount()
	{
		return _itemConsumeCount;
	}
	
	public int getItemConsumeId()
	{
		return _itemConsumeId;
	}
	
	public int getItemConsumeSteps()
	{
		return _itemConsumeSteps;
	}
	
	public int getNextItemConsumeTime()
	{
		return _nextItemConsumeTime;
	}
	
	public int getTotalLifeTime()
	{
		return _totalLifeTime;
	}
	
	public int getTimeLostIdle()
	{
		return _timeLostIdle;
	}
	
	public int getTimeLostActive()
	{
		return _timeLostActive;
	}
	
	public int getTimeRemaining()
	{
		return _timeRemaining;
	}
	
	public void setNextItemConsumeTime(int value)
	{
		_nextItemConsumeTime = value;
	}
	
	public void decNextItemConsumeTime(int value)
	{
		_nextItemConsumeTime -= value;
	}
	
	public void decTimeRemaining(int value)
	{
		_timeRemaining -= value;
	}
	
	public void addExpAndSp(int addToExp, int addToSp)
	{
		getOwner().addExpAndSp(addToExp, addToSp);
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		if (Config.DEBUG)
		{
			_log.warn(getClass().getSimpleName() + ": " + getTemplate().getName() + " (" + getOwner().getName() + ") has been killed.");
		}
		
		if (_summonLifeTask != null)
		{
			_summonLifeTask.cancel(false);
			_summonLifeTask = null;
		}
		
		CharSummonTable.getInstance().removeServitor(getOwner());
		
		return true;
		
	}
	
	/**
	 * Servitors' skills automatically change their level based on the servitor's level.<br>
	 * Until level 70, the servitor gets 1 lv of skill per 10 levels.<br>
	 * After that, it is 1 skill level per 5 servitor levels.<br>
	 * If the resulting skill level doesn't exist use the max that does exist!
	 */
	@Override
	public void doCast(L2Skill skill)
	{
		final int petLevel = getLevel();
		int skillLevel = petLevel / 10;
		if (petLevel >= 70)
		{
			skillLevel += (petLevel - 65) / 10;
		}
		
		// Adjust the level for servitors less than level 1.
		if (skillLevel < 1)
		{
			skillLevel = 1;
		}
		
		final L2Skill skillToCast = SkillData.getInstance().getInfo(skill.getId(), skillLevel);
		
		if (skillToCast != null)
		{
			super.doCast(skillToCast);
		}
		else
		{
			super.doCast(skill);
		}
	}
	
	@Override
	public void setRestoreSummon(boolean val)
	{
		_restoreSummon = val;
	}
	
	@Override
	public final void stopSkillEffects(int skillId)
	{
		super.stopSkillEffects(skillId);
		final Map<Integer, List<SummonEffect>> servitorEffects = SummonEffectsTable.getInstance().getServitorEffects(getOwner());
		if (servitorEffects != null)
		{
			final List<SummonEffect> effects = servitorEffects.get(getReferenceSkill());
			if ((effects != null) && !effects.isEmpty())
			{
				for (SummonEffect effect : effects)
				{
					final L2Skill skill = effect.getSkill();
					if ((skill != null) && (skill.getId() == skillId))
					{
						effects.remove(effect);
					}
				}
			}
		}
	}
	
	@Override
	public void store()
	{
		if ((_referenceSkill == 0) || isDead())
		{
			return;
		}
		
		if (Config.RESTORE_SERVITOR_ON_RECONNECT)
		{
			CharSummonTable.getInstance().saveSummon(this);
		}
	}
	
	@Override
	public void storeEffect(boolean storeEffects)
	{
		if (!Config.SUMMON_STORE_SKILL_COOLTIME)
		{
			return;
		}
		
		if ((getOwner() == null) || getOwner().isInOlympiadMode())
		{
			return;
		}
		
		// Clear list for overwrite
		if (SummonEffectsTable.getInstance().getServitorEffectsOwner().containsKey(getOwner().getObjectId()) && SummonEffectsTable.getInstance().getServitorEffectsOwner().get(getOwner().getObjectId()).containsKey(getOwner().getClassIndex()) && SummonEffectsTable.getInstance().getServitorEffects(getOwner()).containsKey(getReferenceSkill()))
		{
			SummonEffectsTable.getInstance().getServitorEffects(getOwner()).get(getReferenceSkill()).clear();
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_SKILL_SAVE))
		{
			// Delete all current stored effects for summon to avoid dupe
			statement.setInt(1, getOwner().getObjectId());
			statement.setInt(2, getOwner().getClassIndex());
			statement.setInt(3, getReferenceSkill());
			statement.execute();
			
			int buff_index = 0;
			
			final List<Integer> storedSkills = new FastList<>();
			
			// Store all effect data along with calculated remaining
			if (storeEffects)
			{
				try (PreparedStatement ps2 = con.prepareStatement(ADD_SKILL_SAVE))
				{
					for (L2Effect effect : getAllEffects())
					{
						if (effect == null)
						{
							continue;
						}
						L2Skill skill = effect.getSkill();
						// Do not save heals.
						switch (effect.getEffectType())
						{
							case HEAL_OVER_TIME:
							case CPHEAL_OVER_TIME:
								// TODO: Fix me.
							case HIDE:
								continue;
						}
						
						if (skill.isToggle())
						{
							continue;
						}
						
						// Dances and songs are not kept in retail.
						if (skill.isDance() && !Config.ALT_STORE_DANCES)
						{
							continue;
						}
						
						if (storedSkills.contains(skill.getReuseHashCode()))
						{
							continue;
						}
						
						storedSkills.add(skill.getReuseHashCode());
						
						if (effect.getInUse())
						{
							ps2.setInt(1, getOwner().getObjectId());
							ps2.setInt(2, getOwner().getClassIndex());
							ps2.setInt(3, getReferenceSkill());
							ps2.setInt(4, skill.getId());
							ps2.setInt(5, skill.getLevel());
							ps2.setInt(6, effect.getCount());
							ps2.setInt(7, effect.getTime());
							ps2.setInt(8, ++buff_index);
							ps2.execute();
							
							if (!SummonEffectsTable.getInstance().getServitorEffectsOwner().containsKey(getOwner().getObjectId()))
							{
								SummonEffectsTable.getInstance().getServitorEffectsOwner().put(getOwner().getObjectId(), new HashMap<Integer, Map<Integer, List<SummonEffect>>>());
							}
							if (!SummonEffectsTable.getInstance().getServitorEffectsOwner().get(getOwner().getObjectId()).containsKey(getOwner().getClassIndex()))
							{
								SummonEffectsTable.getInstance().getServitorEffectsOwner().get(getOwner().getObjectId()).put(getOwner().getClassIndex(), new HashMap<Integer, List<SummonEffect>>());
							}
							if (!SummonEffectsTable.getInstance().getServitorEffects(getOwner()).containsKey(getReferenceSkill()))
							{
								SummonEffectsTable.getInstance().getServitorEffects(getOwner()).put(getReferenceSkill(), new FastList<SummonEffect>());
							}
							
							SummonEffectsTable.getInstance().getServitorEffects(getOwner()).get(getReferenceSkill()).add(SummonEffectsTable.getInstance().new SummonEffect(skill, effect.getCount(), effect.getTime()));
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Could not store summon effect data: ", e);
		}
	}
	
	@Override
	public void restoreEffects()
	{
		if (getOwner().isInOlympiadMode())
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			if (!SummonEffectsTable.getInstance().getServitorEffectsOwner().containsKey(getOwner().getObjectId()) || !SummonEffectsTable.getInstance().getServitorEffectsOwner().get(getOwner().getObjectId()).containsKey(getOwner().getClassIndex()) || !SummonEffectsTable.getInstance().getServitorEffects(getOwner()).containsKey(getReferenceSkill()))
			{
				try (PreparedStatement statement = con.prepareStatement(RESTORE_SKILL_SAVE))
				{
					statement.setInt(1, getOwner().getObjectId());
					statement.setInt(2, getOwner().getClassIndex());
					statement.setInt(3, getReferenceSkill());
					try (ResultSet rset = statement.executeQuery())
					{
						while (rset.next())
						{
							int effectCount = rset.getInt("effect_count");
							int effectCurTime = rset.getInt("effect_cur_time");
							
							final L2Skill skill = SkillData.getInstance().getInfo(rset.getInt("skill_id"), rset.getInt("skill_level"));
							if (skill == null)
							{
								continue;
							}
							
							if (skill.hasEffects())
							{
								if (!SummonEffectsTable.getInstance().getServitorEffectsOwner().containsKey(getOwner().getObjectId()))
								{
									SummonEffectsTable.getInstance().getServitorEffectsOwner().put(getOwner().getObjectId(), new HashMap<Integer, Map<Integer, List<SummonEffect>>>());
								}
								if (!SummonEffectsTable.getInstance().getServitorEffectsOwner().get(getOwner().getObjectId()).containsKey(getOwner().getClassIndex()))
								{
									SummonEffectsTable.getInstance().getServitorEffectsOwner().get(getOwner().getObjectId()).put(getOwner().getClassIndex(), new HashMap<Integer, List<SummonEffect>>());
								}
								if (!SummonEffectsTable.getInstance().getServitorEffects(getOwner()).containsKey(getReferenceSkill()))
								{
									SummonEffectsTable.getInstance().getServitorEffects(getOwner()).put(getReferenceSkill(), new FastList<SummonEffect>());
								}
								
								SummonEffectsTable.getInstance().getServitorEffects(getOwner()).get(getReferenceSkill()).add(SummonEffectsTable.getInstance().new SummonEffect(skill, effectCount, effectCurTime));
							}
						}
					}
				}
			}
			
			try (PreparedStatement statement = con.prepareStatement(DELETE_SKILL_SAVE))
			{
				statement.setInt(1, getOwner().getObjectId());
				statement.setInt(2, getOwner().getClassIndex());
				statement.setInt(3, getReferenceSkill());
				statement.executeUpdate();
			}
		}
		catch (Exception e)
		{
			_log.warn("Could not restore " + this + " active effect data: " + e.getMessage(), e);
		}
		finally
		{
			if (!SummonEffectsTable.getInstance().getServitorEffectsOwner().containsKey(getOwner().getObjectId()) || !SummonEffectsTable.getInstance().getServitorEffectsOwner().get(getOwner().getObjectId()).containsKey(getOwner().getClassIndex()) || !SummonEffectsTable.getInstance().getServitorEffects(getOwner()).containsKey(getReferenceSkill()))
			{
				return;
			}
			
			for (SummonEffect se : SummonEffectsTable.getInstance().getServitorEffects(getOwner()).get(getReferenceSkill()))
			{
				if ((se != null) && se.getSkill().hasEffects())
				{
					Env env = new Env();
					env.setCharacter(this);
					env.setTarget(this);
					env.setSkill(se.getSkill());
					L2Effect ef;
					for (EffectTemplate et : se.getSkill().getEffectTemplates())
					{
						ef = et.getEffect(env);
						if (ef != null)
						{
							ef.setCount(se.getEffectCount());
							ef.setFirstTime(se.getEffectCurTime());
							ef.scheduleEffect();
						}
					}
				}
			}
		}
	}
	
	static class SummonLifetime implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final L2ServitorInstance _summon;
		
		SummonLifetime(L2PcInstance activeChar, L2ServitorInstance newpet)
		{
			_activeChar = activeChar;
			_summon = newpet;
		}
		
		@Override
		public void run()
		{
			if (Config.DEBUG)
			{
				_log.warn(getClass().getSimpleName() + ": " + _summon.getTemplate().getName() + " (" + _activeChar.getName() + ") run task.");
			}
			
			try
			{
				double oldTimeRemaining = _summon.getTimeRemaining();
				int maxTime = _summon.getTotalLifeTime();
				double newTimeRemaining;
				
				// if pet is attacking
				if (_summon.isAttackingNow())
				{
					_summon.decTimeRemaining(_summon.getTimeLostActive());
				}
				else
				{
					_summon.decTimeRemaining(_summon.getTimeLostIdle());
				}
				newTimeRemaining = _summon.getTimeRemaining();
				// check if the summon's lifetime has ran out
				if (newTimeRemaining < 0)
				{
					_summon.unSummon(_activeChar);
				}
				// check if it is time to consume another item
				else if ((newTimeRemaining <= _summon.getNextItemConsumeTime()) && (oldTimeRemaining > _summon.getNextItemConsumeTime()))
				{
					_summon.decNextItemConsumeTime(maxTime / (_summon.getItemConsumeSteps() + 1));
					
					// check if owner has enough itemConsume, if requested
					if ((_summon.getItemConsumeCount() > 0) && (_summon.getItemConsumeId() != 0) && !_summon.isDead() && !_summon.destroyItemByItemId("Consume", _summon.getItemConsumeId(), _summon.getItemConsumeCount(), _activeChar, true))
					{
						_summon.unSummon(_activeChar);
					}
				}
				
				// prevent useless packet-sending when the difference isn't visible.
				if ((_summon.lastShowntimeRemaining - newTimeRemaining) > (maxTime / 352))
				{
					_summon.sendPacket(new SetSummonRemainTime(maxTime, (int) newTimeRemaining));
					_summon.lastShowntimeRemaining = (int) newTimeRemaining;
					_summon.updateEffectIcons();
				}
			}
			catch (Exception e)
			{
				_log.error("Error on player [" + _activeChar.getName() + "] summon item consume task.", e);
			}
		}
	}
	
	@Override
	public void unSummon(L2PcInstance owner)
	{
		if (Config.DEBUG)
		{
			_log.info(getClass().getSimpleName() + ": " + getTemplate().getName() + " (" + owner.getName() + ") unsummoned.");
		}
		
		if (_summonLifeTask != null)
		{
			_summonLifeTask.cancel(false);
			_summonLifeTask = null;
		}
		
		super.unSummon(owner);
		
		if (!_restoreSummon)
		{
			CharSummonTable.getInstance().removeServitor(owner);
		}
	}
	
	@Override
	public boolean destroyItem(String process, int objectId, long count, L2Object reference, boolean sendMessage)
	{
		return getOwner().destroyItem(process, objectId, count, reference, sendMessage);
	}
	
	@Override
	public boolean destroyItemByItemId(String process, int itemId, long count, L2Object reference, boolean sendMessage)
	{
		if (Config.DEBUG)
		{
			_log.warn(getClass().getSimpleName() + ": " + getTemplate().getName() + " (" + getOwner().getName() + ") consume.");
		}
		
		return getOwner().destroyItemByItemId(process, itemId, count, reference, sendMessage);
	}
	
	public void setTimeRemaining(int time)
	{
		_timeRemaining = time;
	}
	
	public int getReferenceSkill()
	{
		return _referenceSkill;
	}
	
	@Override
	public byte getAttackElement()
	{
		if (isSharingElementals() && (getOwner() != null))
		{
			return getOwner().getAttackElement();
		}
		return super.getAttackElement();
	}
	
	@Override
	public int getAttackElementValue(byte attackAttribute)
	{
		if (isSharingElementals() && (getOwner() != null))
		{
			return (int) (getOwner().getAttackElementValue(attackAttribute) * sharedElementalsPercent());
		}
		return super.getAttackElementValue(attackAttribute);
	}
	
	@Override
	public int getDefenseElementValue(byte defenseAttribute)
	{
		if (isSharingElementals() && (getOwner() != null))
		{
			return (int) (getOwner().getDefenseElementValue(defenseAttribute) * sharedElementalsPercent());
		}
		return super.getDefenseElementValue(defenseAttribute);
	}
	
	@Override
	public boolean isServitor()
	{
		return true;
	}
}
