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

import java.util.List;
import java.util.concurrent.Future;

import javolution.util.FastList;
import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.enums.CtrlEvent;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.handler.ISkillHandler;
import l2r.gameserver.handler.SkillHandler;
import l2r.gameserver.instancemanager.DuelManager;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.interfaces.IIdentifiable;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.L2SkillType;
import l2r.gameserver.model.skills.l2skills.L2SkillDrain;
import l2r.gameserver.model.stats.BaseStats;
import l2r.gameserver.model.stats.Formulas;
import l2r.gameserver.model.stats.Stats;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.taskmanager.AttackStanceTaskManager;
import l2r.util.Rnd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.reunion.interf.ReunionEvents;

public final class L2CubicInstance implements IIdentifiable
{
	protected static final Logger _log = LoggerFactory.getLogger(L2CubicInstance.class);
	
	// Type of Cubics
	public static final int STORM_CUBIC = 1;
	public static final int VAMPIRIC_CUBIC = 2;
	public static final int LIFE_CUBIC = 3;
	public static final int VIPER_CUBIC = 4;
	public static final int POLTERGEIST_CUBIC = 5;
	public static final int BINDING_CUBIC = 6;
	public static final int AQUA_CUBIC = 7;
	public static final int SPARK_CUBIC = 8;
	public static final int ATTRACT_CUBIC = 9;
	public static final int SMART_CUBIC_EVATEMPLAR = 10;
	public static final int SMART_CUBIC_SHILLIENTEMPLAR = 11;
	public static final int SMART_CUBIC_ARCANALORD = 12;
	public static final int SMART_CUBIC_ELEMENTALMASTER = 13;
	public static final int SMART_CUBIC_SPECTRALMASTER = 14;
	
	// Max range of cubic skills
	// TODO: Check/fix the max range
	public static final int MAX_MAGIC_RANGE = 900;
	
	// Cubic skills
	public static final int SKILL_CUBIC_HEAL = 4051;
	public static final int SKILL_CUBIC_CURE = 5579;
	
	protected L2PcInstance _owner;
	protected L2Character _target;
	
	protected int _id;
	protected int _cubicPower;
	protected int _cubicDuration;
	protected int _cubicDelay;
	protected int _cubicSkillChance;
	protected int _cubicMaxCount;
	protected int _currentcount;
	protected boolean _active;
	private final boolean _givenByOther;
	
	protected List<L2Skill> _skills = new FastList<>();
	
	private Future<?> _disappearTask;
	private Future<?> _actionTask;
	
	public L2CubicInstance(L2PcInstance owner, int id, int level, int cubicPower, int cubicDelay, int cubicSkillChance, int cubicMaxCount, int cubicDuration, boolean givenByOther)
	{
		_owner = owner;
		_id = id;
		_cubicPower = cubicPower;
		_cubicDuration = cubicDuration * 1000;
		_cubicDelay = cubicDelay * 1000;
		_cubicSkillChance = cubicSkillChance;
		_cubicMaxCount = cubicMaxCount;
		_currentcount = 0;
		_active = false;
		_givenByOther = givenByOther;
		
		switch (_id)
		{
			case STORM_CUBIC:
				_skills.add(SkillData.getInstance().getInfo(4049, level));
				break;
			case VAMPIRIC_CUBIC:
				_skills.add(SkillData.getInstance().getInfo(4050, level));
				break;
			case LIFE_CUBIC:
				_skills.add(SkillData.getInstance().getInfo(4051, level));
				doAction();
				break;
			case VIPER_CUBIC:
				_skills.add(SkillData.getInstance().getInfo(4052, level));
				break;
			case POLTERGEIST_CUBIC:
				_skills.add(SkillData.getInstance().getInfo(4053, level));
				_skills.add(SkillData.getInstance().getInfo(4054, level));
				_skills.add(SkillData.getInstance().getInfo(4055, level));
				break;
			case BINDING_CUBIC:
				_skills.add(SkillData.getInstance().getInfo(4164, level));
				break;
			case AQUA_CUBIC:
				_skills.add(SkillData.getInstance().getInfo(4165, level));
				break;
			case SPARK_CUBIC:
				_skills.add(SkillData.getInstance().getInfo(4166, level));
				break;
			case ATTRACT_CUBIC:
				_skills.add(SkillData.getInstance().getInfo(5115, level));
				_skills.add(SkillData.getInstance().getInfo(5116, level));
				break;
			case SMART_CUBIC_ARCANALORD:
				// _skills.add(SkillTable.getInstance().getInfo(4049,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				_skills.add(SkillData.getInstance().getInfo(4051, 7)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(4052,6)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4053,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				_skills.add(SkillData.getInstance().getInfo(4165, 9)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(4166,9)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5115,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the
				// cubic skills list
				break;
			case SMART_CUBIC_ELEMENTALMASTER:
				_skills.add(SkillData.getInstance().getInfo(4049, 8)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4051,7)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4052,6)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4053,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4165,9)); no animation
				_skills.add(SkillData.getInstance().getInfo(4166, 9)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(5115,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the
				// cubic skills list
				break;
			case SMART_CUBIC_SPECTRALMASTER:
				_skills.add(SkillData.getInstance().getInfo(4049, 8)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4051,7)); no animation
				_skills.add(SkillData.getInstance().getInfo(4052, 6)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(4053,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4165,9)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4166,9)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5115,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the
				// cubic skills list
				break;
			case SMART_CUBIC_EVATEMPLAR:
				// _skills.add(SkillTable.getInstance().getInfo(4049,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4051,7)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4052,6)); no animation
				_skills.add(SkillData.getInstance().getInfo(4053, 8)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				_skills.add(SkillData.getInstance().getInfo(4165, 9)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(4166,9)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5115,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the
				// cubic skills list
				break;
			case SMART_CUBIC_SHILLIENTEMPLAR:
				_skills.add(SkillData.getInstance().getInfo(4049, 8)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4051,7)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4052,6)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4053,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4165,9)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4166,9)); no animation
				_skills.add(SkillData.getInstance().getInfo(5115, 4)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the
				// cubic skills list
				break;
		}
		_disappearTask = ThreadPoolManager.getInstance().scheduleGeneral(new Disappear(), _cubicDuration); // disappear
	}
	
	public synchronized void doAction()
	{
		if (_active)
		{
			return;
		}
		_active = true;
		
		switch (_id)
		{
			case AQUA_CUBIC:
			case BINDING_CUBIC:
			case SPARK_CUBIC:
			case STORM_CUBIC:
			case POLTERGEIST_CUBIC:
			case VAMPIRIC_CUBIC:
			case VIPER_CUBIC:
			case ATTRACT_CUBIC:
			case SMART_CUBIC_ARCANALORD:
			case SMART_CUBIC_ELEMENTALMASTER:
			case SMART_CUBIC_SPECTRALMASTER:
			case SMART_CUBIC_EVATEMPLAR:
			case SMART_CUBIC_SHILLIENTEMPLAR:
				_actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Action(_cubicSkillChance), 0, _cubicDelay);
				break;
			case LIFE_CUBIC:
				_actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Heal(), 0, _cubicDelay);
				break;
		}
	}
	
	@Override
	public int getId()
	{
		return _id;
	}
	
	public L2PcInstance getOwner()
	{
		return _owner;
	}
	
	public final int getMCriticalHit(L2Character target, L2Skill skill)
	{
		// Magical Critical Rate for cubics is the base Magical Critical Rate of its owner
		return (int) (BaseStats.WIT.calcBonus(_owner) * 10);
	}
	
	public int getCubicPower()
	{
		return _cubicPower;
	}
	
	public void stopAction()
	{
		_target = null;
		if (_actionTask != null)
		{
			_actionTask.cancel(true);
			_actionTask = null;
		}
		_active = false;
	}
	
	public void cancelDisappear()
	{
		if (_disappearTask != null)
		{
			_disappearTask.cancel(true);
			_disappearTask = null;
		}
	}
	
	/** this sets the enemy target for a cubic */
	public void getCubicTarget()
	{
		try
		{
			_target = null;
			L2Object ownerTarget = _owner.getTarget();
			if (ownerTarget == null)
			{
				return;
			}
			
			if (ReunionEvents.isInEvent(_owner))
			{
				if (ownerTarget instanceof L2Character)
				{
					if (ReunionEvents.canAttack(_owner, (L2Character) ownerTarget))
					{
						_target = (L2Character) ownerTarget;
					}
					return;
				}
			}
			
			// Duel targeting
			if (_owner.isInDuel())
			{
				L2PcInstance PlayerA = DuelManager.getInstance().getDuel(_owner.getDuelId()).getPlayerA();
				L2PcInstance PlayerB = DuelManager.getInstance().getDuel(_owner.getDuelId()).getPlayerB();
				
				if (DuelManager.getInstance().getDuel(_owner.getDuelId()).isPartyDuel())
				{
					L2Party partyA = PlayerA.getParty();
					L2Party partyB = PlayerB.getParty();
					L2Party partyEnemy = null;
					
					if (partyA != null)
					{
						if (partyA.getMembers().contains(_owner))
						{
							if (partyB != null)
							{
								partyEnemy = partyB;
							}
							else
							{
								_target = PlayerB;
							}
						}
						else
						{
							partyEnemy = partyA;
						}
					}
					else
					{
						if (PlayerA == _owner)
						{
							if (partyB != null)
							{
								partyEnemy = partyB;
							}
							else
							{
								_target = PlayerB;
							}
						}
						else
						{
							_target = PlayerA;
						}
					}
					if ((_target == PlayerA) || (_target == PlayerB))
					{
						if (_target == ownerTarget)
						{
							return;
						}
					}
					if (partyEnemy != null)
					{
						if (partyEnemy.getMembers().contains(ownerTarget))
						{
							_target = (L2Character) ownerTarget;
						}
						return;
					}
				}
				if ((PlayerA != _owner) && (ownerTarget == PlayerA))
				{
					_target = PlayerA;
					return;
				}
				if ((PlayerB != _owner) && (ownerTarget == PlayerB))
				{
					_target = PlayerB;
					return;
				}
				_target = null;
				return;
			}
			// Olympiad targeting
			if (_owner.isInOlympiadMode())
			{
				if (_owner.isOlympiadStart())
				{
					if (ownerTarget instanceof L2Playable)
					{
						final L2PcInstance targetPlayer = ownerTarget.getActingPlayer();
						if ((targetPlayer != null) && (targetPlayer.getOlympiadGameId() == _owner.getOlympiadGameId()) && (targetPlayer.getOlympiadSide() != _owner.getOlympiadSide()))
						{
							_target = (L2Character) ownerTarget;
						}
					}
				}
				return;
			}
			// test owners target if it is valid then use it
			if ((ownerTarget instanceof L2Character) && (ownerTarget != _owner.getSummon()) && (ownerTarget != _owner))
			{
				// target mob which has aggro on you or your summon
				if (ownerTarget instanceof L2Attackable)
				{
					if ((((L2Attackable) ownerTarget).getAggroList().get(_owner) != null) && !((L2Attackable) ownerTarget).isDead())
					{
						_target = (L2Character) ownerTarget;
						return;
					}
					if (_owner.hasSummon())
					{
						if ((((L2Attackable) ownerTarget).getAggroList().get(_owner.getSummon()) != null) && !((L2Attackable) ownerTarget).isDead())
						{
							_target = (L2Character) ownerTarget;
							return;
						}
					}
				}
				
				// get target in pvp or in siege
				L2PcInstance enemy = null;
				
				if (((_owner.getPvpFlag() > 0) && !_owner.isInsideZone(ZoneIdType.PEACE)) || _owner.isInsideZone(ZoneIdType.PVP))
				{
					if (!((L2Character) ownerTarget).isDead())
					{
						enemy = ownerTarget.getActingPlayer();
					}
					
					if (enemy != null)
					{
						boolean targetIt = true;
						
						if (_owner.getParty() != null)
						{
							if (_owner.getParty().getMembers().contains(enemy))
							{
								targetIt = false;
							}
							else if (_owner.getParty().getCommandChannel() != null)
							{
								if (_owner.getParty().getCommandChannel().getMembers().contains(enemy))
								{
									targetIt = false;
								}
							}
						}
						if ((_owner.getClan() != null) && !_owner.isInsideZone(ZoneIdType.PVP))
						{
							if (_owner.getClan().isMember(enemy.getObjectId()))
							{
								targetIt = false;
							}
							if ((_owner.getAllyId() > 0) && (enemy.getAllyId() > 0))
							{
								if (_owner.getAllyId() == enemy.getAllyId())
								{
									targetIt = false;
								}
							}
						}
						if ((enemy.getPvpFlag() == 0) && !enemy.isInsideZone(ZoneIdType.PVP))
						{
							targetIt = false;
						}
						if (enemy.isInsideZone(ZoneIdType.PEACE))
						{
							targetIt = false;
						}
						if ((_owner.getSiegeState() > 0) && (_owner.getSiegeState() == enemy.getSiegeState()))
						{
							targetIt = false;
						}
						if (!enemy.isVisible())
						{
							targetIt = false;
						}
						
						if (targetIt)
						{
							_target = enemy;
							return;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.error("", e);
		}
	}
	
	private class Action implements Runnable
	{
		private final int _chance;
		
		protected Action(int chance)
		{
			_chance = chance;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_owner.isDead() || !_owner.isOnline())
				{
					stopAction();
					_owner.getCubics().remove(this);
					_owner.broadcastUserInfo();
					cancelDisappear();
					return;
				}
				if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(_owner))
				{
					if (_owner.hasSummon())
					{
						if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(_owner.getSummon()))
						{
							stopAction();
							return;
						}
					}
					else
					{
						stopAction();
						return;
					}
				}
				// The cubic has already reached its limit and it will stay idle until its duration ends.
				if ((_cubicMaxCount > -1) && (_currentcount >= _cubicMaxCount))
				{
					stopAction();
					return;
				}
				// Smart Cubic debuff cancel is 100%
				boolean UseCubicCure = false;
				L2Skill skill = null;
				
				if ((_id >= SMART_CUBIC_EVATEMPLAR) && (_id <= SMART_CUBIC_SPECTRALMASTER))
				{
					L2Effect[] effects = _owner.getAllEffects();
					
					for (L2Effect e : effects)
					{
						if ((e != null) && e.getSkill().isDebuff() && e.getSkill().canBeDispeled())
						{
							UseCubicCure = true;
							e.exit();
						}
					}
				}
				
				if (UseCubicCure)
				{
					// Smart Cubic debuff cancel is needed, no other skill is used in this
					// activation period
					MagicSkillUse msu = new MagicSkillUse(_owner, _owner, SKILL_CUBIC_CURE, 1, 0, 0);
					_owner.broadcastPacket(msu);
					
					// The cubic has done an action, increase the currentcount
					_currentcount++;
				}
				else if (Rnd.get(1, 100) < _chance)
				{
					skill = _skills.get(Rnd.get(_skills.size()));
					if (skill != null)
					{
						if (skill.getId() == SKILL_CUBIC_HEAL)
						{
							// friendly skill, so we look a target in owner's party
							cubicTargetForHeal();
						}
						else
						{
							// offensive skill, we look for an enemy target
							getCubicTarget();
							if (!isInCubicRange(_owner, _target))
							{
								_target = null;
							}
						}
						L2Character target = _target; // copy to avoid npe
						if ((target != null) && (!target.isDead()))
						{
							if (Config.DEBUG)
							{
								_log.info("L2CubicInstance: Action.run();");
								_log.info("Cubic Id: " + _id + " Target: " + target.getName() + " distance: " + Math.sqrt(target.getDistanceSq(_owner.getX(), _owner.getY(), _owner.getZ())));
							}
							
							_owner.broadcastPacket(new MagicSkillUse(_owner, target, skill.getId(), skill.getLevel(), 0, 0));
							
							L2SkillType type = skill.getSkillType();
							ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
							L2Character[] targets =
							{
								target
							};
							
							if ((type == L2SkillType.PARALYZE) || (type == L2SkillType.STUN) || (type == L2SkillType.ROOT) || (type == L2SkillType.AGGDAMAGE))
							{
								if (Config.DEBUG)
								{
									_log.info("L2CubicInstance: Action.run() handler " + type);
								}
								useCubicDisabler(type, L2CubicInstance.this, skill, targets);
							}
							else if (type == L2SkillType.MDAM)
							{
								if (Config.DEBUG)
								{
									_log.info("L2CubicInstance: Action.run() handler " + type);
								}
								useCubicMdam(L2CubicInstance.this, skill, targets);
							}
							else if ((type == L2SkillType.POISON) || (type == L2SkillType.DEBUFF) || (type == L2SkillType.DOT))
							{
								if (Config.DEBUG)
								{
									_log.info("L2CubicInstance: Action.run() handler " + type);
								}
								useCubicContinuous(L2CubicInstance.this, skill, targets);
							}
							else if (type == L2SkillType.DRAIN)
							{
								if (Config.DEBUG)
								{
									_log.info("L2CubicInstance: Action.run() skill " + type);
								}
								((L2SkillDrain) skill).useCubicSkill(L2CubicInstance.this, targets);
							}
							else
							{
								handler.useSkill(_owner, skill, targets);
								if (Config.DEBUG)
								{
									_log.info("L2CubicInstance: Action.run(); other handler");
								}
							}
							
							if (skill.hasEffectType(L2EffectType.DMG_OVER_TIME, L2EffectType.DMG_OVER_TIME_PERCENT))
							{
								if (Config.DEBUG)
								{
									_log.info("L2CubicInstance: Action.run() handler " + type);
								}
								useCubicContinuous(L2CubicInstance.this, skill, targets);
							}
							_currentcount++;
						}
					}
				}
			}
			catch (Exception e)
			{
				_log.error("", e);
			}
		}
	}
	
	public void useCubicContinuous(L2CubicInstance activeCubic, L2Skill skill, L2Object[] targets)
	{
		for (L2Character target : (L2Character[]) targets)
		{
			if ((target == null) || target.isDead())
			{
				continue;
			}
			
			if (skill.isOffensive())
			{
				byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, skill);
				boolean acted = Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld);
				if (!acted)
				{
					activeCubic.getOwner().sendPacket(SystemMessageId.ATTACK_FAILED);
					continue;
				}
				
			}
			
			// if this is a debuff let the duel manager know about it
			// so the debuff can be removed after the duel
			// (player & target must be in the same duel)
			if ((target.isPlayer()) && target.getActingPlayer().isInDuel() && (skill.getSkillType() == L2SkillType.DEBUFF) && (activeCubic.getOwner().getDuelId() == target.getActingPlayer().getDuelId()))
			{
				DuelManager dm = DuelManager.getInstance();
				for (L2Effect debuff : skill.getEffects(activeCubic.getOwner(), target))
				{
					if (debuff != null)
					{
						dm.onBuff(target.getActingPlayer(), debuff);
					}
				}
			}
			else
			{
				skill.getEffects(activeCubic, target, null);
			}
		}
	}
	
	public void useCubicMdam(L2CubicInstance activeCubic, L2Skill skill, L2Object[] targets)
	{
		for (L2Character target : (L2Character[]) targets)
		{
			if (target == null)
			{
				continue;
			}
			
			if (target.isAlikeDead())
			{
				if (target.isPlayer())
				{
					target.stopFakeDeath(true);
				}
				else
				{
					continue;
				}
			}
			
			boolean mcrit = Formulas.calcMCrit(activeCubic.getOwner().getMCriticalHit(target, skill));
			byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, skill);
			int damage = (int) Formulas.calcMagicDam(activeCubic, target, skill, mcrit, shld);
			
			if (Config.DEBUG)
			{
				_log.info("L2SkillMdam: useCubicSkill() -> damage = " + damage);
			}
			
			if (damage > 0)
			{
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				
				if (target.getStat().calcStat(Stats.VENGEANCE_SKILL_MAGIC_DAMAGE, 0, target, skill) > Rnd.get(100))
				{
					damage = 0;
				}
				else
				{
					activeCubic.getOwner().sendDamageMessage(target, damage, mcrit, false, false);
					target.reduceCurrentHp(damage, activeCubic.getOwner(), skill);
				}
			}
		}
	}
	
	public void useCubicDisabler(L2SkillType type, L2CubicInstance activeCubic, L2Skill skill, L2Object[] targets)
	{
		if (Config.DEBUG)
		{
			_log.info("Disablers: useCubicSkill()");
		}
		
		for (L2Character target : (L2Character[]) targets)
		{
			if ((target == null) || target.isDead())
			{
				continue;
			}
			
			byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, skill);
			
			switch (type)
			{
				case STUN:
				case PARALYZE:
				case ROOT:
				{
					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld))
					{
						// if this is a debuff let the duel manager know about it
						// so the debuff can be removed after the duel
						// (player & target must be in the same duel)
						if ((target.isPlayer()) && ((L2PcInstance) target).isInDuel() && (skill.getSkillType() == L2SkillType.DEBUFF) && (activeCubic.getOwner().getDuelId() == ((L2PcInstance) target).getDuelId()))
						{
							DuelManager dm = DuelManager.getInstance();
							for (L2Effect debuff : skill.getEffects(activeCubic.getOwner(), target))
							{
								if (debuff != null)
								{
									dm.onBuff(((L2PcInstance) target), debuff);
								}
							}
						}
						else
						{
							skill.getEffects(activeCubic, target, null);
						}
						
						if (Config.DEBUG)
						{
							_log.info("Disablers: useCubicSkill() -> success");
						}
					}
					else
					{
						if (Config.DEBUG)
						{
							_log.info("Disablers: useCubicSkill() -> failed");
						}
					}
					break;
				}
				case AGGDAMAGE:
				{
					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld))
					{
						if (target.isAttackable())
						{
							target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeCubic.getOwner(), (int) ((150 * skill.getPower()) / (target.getLevel() + 7)));
						}
						skill.getEffects(activeCubic, target, null);
						
						if (Config.DEBUG)
						{
							_log.info("Disablers: useCubicSkill() -> success");
						}
					}
					else
					{
						if (Config.DEBUG)
						{
							_log.info("Disablers: useCubicSkill() -> failed");
						}
					}
					break;
				}
			}
		}
	}
	
	/**
	 * @param owner
	 * @param target
	 * @return true if the target is inside of the owner's max Cubic range
	 */
	public boolean isInCubicRange(L2Character owner, L2Character target)
	{
		if ((owner == null) || (target == null))
		{
			return false;
		}
		
		int x, y, z;
		// temporary range check until real behavior of cubics is known/coded
		int range = MAX_MAGIC_RANGE;
		
		x = (owner.getX() - target.getX());
		y = (owner.getY() - target.getY());
		z = (owner.getZ() - target.getZ());
		
		return (((x * x) + (y * y) + (z * z)) <= (range * range));
	}
	
	/** this sets the friendly target for a cubic */
	public void cubicTargetForHeal()
	{
		L2Character target = null;
		double percentleft = 100.0;
		L2Party party = _owner.getParty();
		
		// if owner is in a duel but not in a party duel, then it is the same as he does not have a
		// party
		if (_owner.isInDuel())
		{
			if (!DuelManager.getInstance().getDuel(_owner.getDuelId()).isPartyDuel())
			{
				party = null;
			}
		}
		
		if ((party != null) && !_owner.isInOlympiadMode())
		{
			// Get all visible objects in a spheric area near the L2Character
			// Get a list of Party Members
			List<L2PcInstance> partyList = party.getMembers();
			for (L2Character partyMember : partyList)
			{
				if (!partyMember.isDead())
				{
					// if party member not dead, check if he is in castrange of heal cubic
					if (isInCubicRange(_owner, partyMember))
					{
						// member is in cubic casting range, check if he need heal and if he have
						// the lowest HP
						if (partyMember.getCurrentHp() < partyMember.getMaxHp())
						{
							if (percentleft > (partyMember.getCurrentHp() / partyMember.getMaxHp()))
							{
								percentleft = (partyMember.getCurrentHp() / partyMember.getMaxHp());
								target = partyMember;
							}
						}
					}
				}
				if (partyMember.getSummon() != null)
				{
					if (partyMember.getSummon().isDead())
					{
						continue;
					}
					
					// If party member's pet not dead, check if it is in cast range of heal cubic.
					if (!isInCubicRange(_owner, partyMember.getSummon()))
					{
						continue;
					}
					
					// member's pet is in cubic casting range, check if he need heal and if he have
					// the lowest HP
					if (partyMember.getSummon().getCurrentHp() < partyMember.getSummon().getMaxHp())
					{
						if (percentleft > (partyMember.getSummon().getCurrentHp() / partyMember.getSummon().getMaxHp()))
						{
							percentleft = (partyMember.getSummon().getCurrentHp() / partyMember.getSummon().getMaxHp());
							target = partyMember.getSummon();
						}
					}
				}
			}
		}
		else
		{
			if (_owner.getCurrentHp() < _owner.getMaxHp())
			{
				percentleft = (_owner.getCurrentHp() / _owner.getMaxHp());
				target = _owner;
			}
			if (_owner.hasSummon())
			{
				if (!_owner.getSummon().isDead() && (_owner.getSummon().getCurrentHp() < _owner.getSummon().getMaxHp()) && (percentleft > (_owner.getSummon().getCurrentHp() / _owner.getSummon().getMaxHp())) && isInCubicRange(_owner, _owner.getSummon()))
				{
					target = _owner.getSummon();
				}
			}
		}
		
		_target = target;
	}
	
	public boolean givenByOther()
	{
		return _givenByOther;
	}
	
	protected class Heal implements Runnable
	{
		@Override
		public void run()
		{
			if (_owner.isDead() || !_owner.isOnline())
			{
				stopAction();
				_owner.getCubics().remove(this);
				_owner.broadcastUserInfo();
				cancelDisappear();
				return;
			}
			try
			{
				L2Skill skill = null;
				for (L2Skill sk : _skills)
				{
					if (sk.getId() == SKILL_CUBIC_HEAL)
					{
						skill = sk;
						break;
					}
				}
				
				if (skill != null)
				{
					cubicTargetForHeal();
					L2Character target = _target;
					if ((target != null) && !target.isDead())
					{
						if ((target.getMaxHp() - target.getCurrentHp()) > skill.getPower())
						{
							L2Character[] targets =
							{
								target
							};
							ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
							if (handler != null)
							{
								handler.useSkill(_owner, skill, targets);
							}
							else
							{
								skill.useSkill(_owner, targets);
							}
							
							MagicSkillUse msu = new MagicSkillUse(_owner, target, skill.getId(), skill.getLevel(), 0, 0);
							_owner.broadcastPacket(msu);
						}
					}
				}
			}
			catch (Exception e)
			{
				_log.error("", e);
			}
		}
	}
	
	private class Disappear implements Runnable
	{
		Disappear()
		{
			// run task
		}
		
		@Override
		public void run()
		{
			stopAction();
			cancelDisappear();
			_owner.getCubics().remove(getId());
			_owner.broadcastUserInfo();
		}
	}
}