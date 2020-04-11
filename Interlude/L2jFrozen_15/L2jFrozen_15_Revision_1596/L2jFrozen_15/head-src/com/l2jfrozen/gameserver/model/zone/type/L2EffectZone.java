package com.l2jfrozen.gameserver.model.zone.type;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;
import com.l2jfrozen.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.StringUtil;
import com.l2jfrozen.util.random.Rnd;

/**
 * another type of damage zone with skills
 * @author kerberos
 */
public class L2EffectZone extends L2ZoneType
{
	public static final Logger LOGGER = Logger.getLogger(L2EffectZone.class);
	
	private int chance;
	private int initialDelay;
	private int reuse;
	private boolean enabled;
	private boolean isShowDangerIcon;
	private volatile Future<?> task;
	protected volatile Map<Integer, Integer> skills;
	
	public L2EffectZone(final int id)
	{
		super(id);
		chance = 100;
		initialDelay = 0;
		reuse = 30000;
		enabled = true;
		isShowDangerIcon = true;
	}
	
	@Override
	public void setParameter(final String name, final String value)
	{
		switch (name)
		{
			case "chance":
				chance = Integer.parseInt(value);
				break;
			case "initialDelay":
				initialDelay = Integer.parseInt(value);
				break;
			case "defaultStatus":
				enabled = Boolean.parseBoolean(value);
				break;
			case "reuse":
				reuse = Integer.parseInt(value);
				break;
			case "skillIdLvl":
				final String[] propertySplit = value.split(";");
				skills = new HashMap<>(propertySplit.length);
				for (final String skill : propertySplit)
				{
					final String[] skillSplit = skill.split("-");
					if (skillSplit.length != 2)
					{
						LOGGER.warn(StringUtil.concat(getClass().getSimpleName() + ": invalid config property -> skillsIdLvl \"", skill, "\""));
					}
					else
					{
						try
						{
							skills.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
						}
						catch (final NumberFormatException nfe)
						{
							if (!skill.isEmpty())
							{
								LOGGER.warn(StringUtil.concat(getClass().getSimpleName() + ": invalid config property -> skillsIdLvl \"", skillSplit[0], "\"", skillSplit[1]));
							}
						}
					}
				}
				break;
			case "showDangerIcon":
				isShowDangerIcon = Boolean.parseBoolean(value);
				break;
			default:
				super.setParameter(name, value);
				break;
		}
	}
	
	@Override
	protected void onEnter(final L2Character character)
	{
		if (skills != null)
		{
			if (task == null)
			{
				synchronized (this)
				{
					if (task == null)
					{
						task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ApplySkill(), initialDelay, reuse);
					}
				}
			}
		}
		
		if (character instanceof L2PcInstance && isShowDangerIcon)
		{
			character.setInsideZone(L2Character.ZONE_DANGERAREA, true);
			character.sendPacket(new EtcStatusUpdate((L2PcInstance) character));
		}
	}
	
	@Override
	protected void onExit(final L2Character character)
	{
		if (character instanceof L2PcInstance && isShowDangerIcon)
		{
			character.setInsideZone(L2Character.ZONE_DANGERAREA, false);
			if (!character.isInsideZone(L2Character.ZONE_DANGERAREA))
			{
				character.sendPacket(new EtcStatusUpdate((L2PcInstance) character));
			}
		}
		
		if (characterList.isEmpty() && task != null)
		{
			task.cancel(true);
			task = null;
		}
	}
	
	protected L2Skill getSkill(final int skillId, final int skillLvl)
	{
		return SkillTable.getInstance().getInfo(skillId, skillLvl);
	}
	
	public int getChance()
	{
		return chance;
	}
	
	public boolean isEnabled()
	{
		return enabled;
	}
	
	public void addSkill(final int skillId, final int skillLvL)
	{
		if (skillLvL < 1) // remove skill
		{
			removeSkill(skillId);
			return;
		}
		
		if (skills == null)
		{
			synchronized (this)
			{
				if (skills == null)
				{
					skills = new ConcurrentHashMap<>(3);
				}
			}
		}
		skills.put(skillId, skillLvL);
	}
	
	public void removeSkill(final int skillId)
	{
		if (skills != null)
		{
			skills.remove(skillId);
		}
	}
	
	public void clearSkills()
	{
		if (skills != null)
		{
			skills.clear();
		}
	}
	
	public int getSkillLevel(final int skillId)
	{
		if (skills == null || !skills.containsKey(skillId))
		{
			return 0;
		}
		return skills.get(skillId);
	}
	
	public void setZoneEnabled(final boolean val)
	{
		enabled = val;
	}
	
	protected Collection<L2Character> getCharacterList()
	{
		return characterList.values();
	}
	
	class ApplySkill implements Runnable
	{
		ApplySkill()
		{
			if (skills == null)
			{
				throw new IllegalStateException("No skills defined.");
			}
		}
		
		@Override
		public void run()
		{
			if (isEnabled())
			{
				for (final L2Character temp : getCharacterList())
				{
					
					if (temp != null && !temp.isDead())
					{
						if (!(temp instanceof L2PlayableInstance))
						{
							continue;
						}
						
						if (Rnd.get(100) < getChance())
						{
							for (final Entry<Integer, Integer> e : skills.entrySet())
							{
								final L2Skill skill = getSkill(e.getKey(), e.getValue());
								
								if (skill == null)
								{
									LOGGER.warn("ATTENTION: Skill " + e.getKey() + " cannot be loaded.. Verify Skill definition into data/xml/skill folder...");
									continue;
								}
								
								if (skill.checkCondition(temp, temp, false))
								{
									if (temp.getFirstEffect(e.getKey()) == null)
									{
										
										skill.getEffects(temp, temp);
										
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public void onDieInside(final L2Character character)
	{
	}
	
	@Override
	public void onReviveInside(final L2Character character)
	{
	}
}