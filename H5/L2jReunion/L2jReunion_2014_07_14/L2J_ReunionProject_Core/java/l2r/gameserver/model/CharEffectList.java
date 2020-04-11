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
package l2r.gameserver.model;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2r.Config;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.EffectFlag;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.olympiad.OlympiadGameManager;
import l2r.gameserver.model.olympiad.OlympiadGameTask;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.L2SkillType;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.AbnormalStatusUpdate;
import l2r.gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import l2r.gameserver.network.serverpackets.PartySpelled;
import l2r.gameserver.network.serverpackets.SystemMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharEffectList
{
	protected static final Logger _log = LoggerFactory.getLogger(CharEffectList.class);
	private static final L2Effect[] EMPTY_EFFECTS = new L2Effect[0];
	
	private FastList<L2Effect> _buffs;
	private FastList<L2Effect> _debuffs;
	private FastList<L2Effect> _passives; // They bypass most of the actions, keep in mind that those arent included in getAllEffects()
	
	// The table containing the List of all stacked effect in progress for each Stack group Identifier
	private Map<String, List<L2Effect>> _stackedEffects;
	
	private volatile boolean _hasBuffsRemovedOnAnyAction = false;
	private volatile boolean _hasBuffsRemovedOnDamage = false;
	private volatile boolean _hasDebuffsRemovedOnDamage = false;
	
	private boolean _queuesInitialized = false;
	private LinkedBlockingQueue<L2Effect> _addQueue;
	private LinkedBlockingQueue<L2Effect> _removeQueue;
	private final AtomicBoolean queueLock = new AtomicBoolean();
	private int _effectFlags;
	
	// only party icons need to be updated
	private boolean _partyOnly = false;
	
	// Owner of this list
	private final L2Character _owner;
	
	private L2Effect[] _effectCache;
	private volatile boolean _rebuildCache = true;
	private final Object _buildEffectLock = new Object();
	
	public CharEffectList(L2Character owner)
	{
		_owner = owner;
	}
	
	/**
	 * Returns all effects affecting stored in this CharEffectList
	 * @return
	 */
	public final L2Effect[] getAllEffects()
	{
		// If no effect is active, return EMPTY_EFFECTS
		if (isEmpty())
		{
			return EMPTY_EFFECTS;
		}
		
		synchronized (_buildEffectLock)
		{
			// If we dont need to rebuild the cache, just return the current one.
			if (!_rebuildCache)
			{
				return _effectCache;
			}
			
			_rebuildCache = false;
			
			// Create a copy of the effects
			FastList<L2Effect> temp = FastList.newInstance();
			
			// Add all buffs and all debuffs
			if (hasBuffs())
			{
				temp.addAll(_buffs);
			}
			if (hasDebuffs())
			{
				temp.addAll(_debuffs);
			}
			
			// Return all effects in an array
			L2Effect[] tempArray = new L2Effect[temp.size()];
			temp.toArray(tempArray);
			return (_effectCache = tempArray);
		}
	}
	
	/**
	 * Returns the first effect matching the given EffectType
	 * @param tp
	 * @return
	 */
	public final L2Effect getFirstEffect(L2EffectType tp)
	{
		L2Effect effectNotInUse = null;
		
		if (hasBuffs())
		{
			for (L2Effect e : _buffs)
			{
				if (e == null)
				{
					continue;
				}
				
				if (e.getEffectType() == tp)
				{
					if (e.getInUse())
					{
						return e;
					}
					
					effectNotInUse = e;
				}
			}
		}
		if ((effectNotInUse == null) && hasDebuffs())
		{
			for (L2Effect e : _debuffs)
			{
				if (e == null)
				{
					continue;
				}
				
				if (e.getEffectType() == tp)
				{
					if (e.getInUse())
					{
						return e;
					}
					
					effectNotInUse = e;
				}
			}
		}
		return effectNotInUse;
	}
	
	/**
	 * Returns the first effect matching the given L2Skill
	 * @param skill
	 * @return
	 */
	public final L2Effect getFirstEffect(L2Skill skill)
	{
		L2Effect effectNotInUse = null;
		
		if (skill.isDebuff())
		{
			if (hasDebuffs())
			{
				for (L2Effect e : _debuffs)
				{
					if (e == null)
					{
						continue;
					}
					
					if (e.getSkill() == skill)
					{
						if (e.getInUse())
						{
							return e;
						}
						
						effectNotInUse = e;
					}
				}
			}
		}
		else
		{
			if (hasBuffs())
			{
				for (L2Effect e : _buffs)
				{
					if (e == null)
					{
						continue;
					}
					
					if (e.getSkill() == skill)
					{
						if (e.getInUse())
						{
							return e;
						}
						
						effectNotInUse = e;
					}
				}
			}
		}
		return effectNotInUse;
	}
	
	/**
	 * Returns the first effect matching the given skillId
	 * @param skillId
	 * @return
	 */
	public final L2Effect getFirstEffect(int skillId)
	{
		L2Effect effectNotInUse = null;
		
		if (hasBuffs())
		{
			for (L2Effect e : _buffs)
			{
				if (e == null)
				{
					continue;
				}
				
				if (e.getSkill().getId() == skillId)
				{
					if (e.getInUse())
					{
						return e;
					}
					
					effectNotInUse = e;
				}
			}
		}
		
		if ((effectNotInUse == null) && (_debuffs != null) && !_debuffs.isEmpty())
		{
			for (L2Effect e : _debuffs)
			{
				if (e == null)
				{
					continue;
				}
				if (e.getSkill().getId() == skillId)
				{
					if (e.getInUse())
					{
						return e;
					}
					
					effectNotInUse = e;
				}
			}
		}
		return effectNotInUse;
	}
	
	public final L2Effect getFirstPassiveEffect(L2EffectType type)
	{
		if (_passives != null)
		{
			for (L2Effect e : _passives)
			{
				if ((e != null) && (e.getEffectType() == type))
				{
					if (e.getInUse())
					{
						return e;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Checks if the given skill stacks with an existing one.
	 * @param checkSkill the skill to be checked
	 * @return Returns whether or not this skill will stack
	 */
	private boolean doesStack(L2Skill checkSkill)
	{
		if (((_buffs == null) || _buffs.isEmpty()) || (checkSkill._effectTemplates == null) || (checkSkill._effectTemplates.length < 1) || (checkSkill._effectTemplates[0].abnormalType == null) || "none".equals(checkSkill._effectTemplates[0].abnormalType))
		{
			return false;
		}
		
		String stackType = checkSkill._effectTemplates[0].abnormalType;
		
		for (L2Effect e : _buffs)
		{
			if ((e.getAbnormalType() != null) && e.getAbnormalType().equals(stackType))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Return the number of buffs in this CharEffectList not counting Songs/Dances
	 * @return
	 */
	public int getBuffCount()
	{
		if ((_buffs == null) || _buffs.isEmpty())
		{
			return 0;
		}
		
		int buffCount = 0;
		for (L2Effect e : _buffs)
		{
			if ((e != null) && e.getShowIcon() && !e.getSkill().isDance() && !e.getSkill().isTriggeredSkill() && !e.getSkill().is7Signs())
			{
				if (e.getSkill().getSkillType() == L2SkillType.BUFF)
				{
					buffCount++;
				}
			}
		}
		
		return buffCount;
	}
	
	/**
	 * Return the number of Songs/Dances in this CharEffectList
	 * @return
	 */
	public int getDanceCount()
	{
		if ((_buffs == null) || _buffs.isEmpty())
		{
			return 0;
		}
		
		int danceCount = 0;
		for (L2Effect e : _buffs)
		{
			if ((e != null) && e.getSkill().isDance() && e.getInUse())
			{
				danceCount++;
			}
		}
		
		return danceCount;
	}
	
	/**
	 * Return the number of Activation Buffs in this CharEffectList
	 * @return
	 */
	public int getTriggeredBuffCount()
	{
		if (_buffs == null)
		{
			return 0;
		}
		int activationBuffCount = 0;
		
		// synchronized(_buffs)
		{
			if (_buffs.isEmpty())
			{
				return 0;
			}
			
			for (L2Effect e : _buffs)
			{
				if ((e != null) && e.getSkill().isTriggeredSkill() && e.getInUse())
				{
					activationBuffCount++;
				}
			}
		}
		return activationBuffCount;
	}
	
	/**
	 * Exits all effects in this CharEffectList
	 */
	public final void stopAllEffects()
	{
		// Get all active skills effects from this list
		L2Effect[] effects = getAllEffects();
		
		// Exit them
		for (L2Effect e : effects)
		{
			if (e != null)
			{
				e.exit(true);
			}
		}
	}
	
	/**
	 * Exits all effects in this CharEffectList
	 */
	public final void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		// Get all active skills effects from this list
		L2Effect[] effects = getAllEffects();
		
		// Exit them
		for (L2Effect e : effects)
		{
			if ((e != null) && !e.getSkill().isStayAfterDeath())
			{
				e.exit(true);
			}
		}
	}
	
	/**
	 * Exit all toggle-type effects
	 */
	public void stopAllToggles()
	{
		if (hasBuffs())
		{
			for (L2Effect e : _buffs)
			{
				if ((e != null) && e.getSkill().isToggle())
				{
					e.exit();
				}
			}
		}
	}
	
	/**
	 * Exit all effects having a specified type
	 * @param type
	 */
	public final void stopEffects(L2EffectType type)
	{
		if (hasBuffs())
		{
			for (L2Effect e : _buffs)
			{
				// Get active skills effects of the selected type
				if ((e != null) && (e.getEffectType() == type))
				{
					e.exit();
				}
			}
		}
		
		if (hasDebuffs())
		{
			for (L2Effect e : _debuffs)
			{
				// Get active skills effects of the selected type
				if ((e != null) && (e.getEffectType() == type))
				{
					e.exit();
				}
			}
		}
	}
	
	/**
	 * Exits all effects created by a specific skillId
	 * @param skillId
	 */
	public final void stopSkillEffects(int skillId)
	{
		if (hasBuffs())
		{
			for (L2Effect e : _buffs)
			{
				if ((e != null) && (e.getSkill().getId() == skillId))
				{
					e.exit();
				}
			}
		}
		if (hasDebuffs())
		{
			for (L2Effect e : _debuffs)
			{
				if ((e != null) && (e.getSkill().getId() == skillId))
				{
					e.exit();
				}
			}
		}
	}
	
	/**
	 * Exits all effects created by a specific skill type
	 * @param skillType skill type
	 * @param negateLvl
	 */
	public final void stopSkillEffects(L2SkillType skillType, int negateLvl)
	{
		if (hasBuffs())
		{
			for (L2Effect e : _buffs)
			{
				if ((e != null) && ((e.getSkill().getSkillType() == skillType) || ((e.getSkill().getEffectType() != null) && (e.getSkill().getEffectType() == skillType))) && ((negateLvl == -1) || ((e.getSkill().getEffectType() != null) && (e.getSkill().getEffectAbnormalLvl() >= 0) && (e.getSkill().getEffectAbnormalLvl() <= negateLvl)) || ((e.getSkill().getAbnormalLvl() >= 0) && (e.getSkill().getAbnormalLvl() <= negateLvl))))
				{
					e.exit();
				}
			}
		}
		if (hasDebuffs())
		{
			for (L2Effect e : _debuffs)
			{
				if ((e != null) && ((e.getSkill().getSkillType() == skillType) || ((e.getSkill().getEffectType() != null) && (e.getSkill().getEffectType() == skillType))) && ((negateLvl == -1) || ((e.getSkill().getEffectType() != null) && (e.getSkill().getEffectAbnormalLvl() >= 0) && (e.getSkill().getEffectAbnormalLvl() <= negateLvl)) || ((e.getSkill().getAbnormalLvl() >= 0) && (e.getSkill().getAbnormalLvl() <= negateLvl))))
				{
					e.exit();
				}
			}
		}
	}
	
	/**
	 * Exits all buffs effects of the skills with "removedOnAnyAction" set. Called on any action except movement (attack, cast).
	 */
	public void stopEffectsOnAction()
	{
		if (_hasBuffsRemovedOnAnyAction)
		{
			if (hasBuffs())
			{
				for (L2Effect e : _buffs)
				{
					if ((e != null) && e.getSkill().isRemovedOnAnyActionExceptMove())
					{
						e.exit(true);
					}
				}
			}
		}
	}
	
	public void stopEffectsOnDamage(boolean awake)
	{
		if (_hasBuffsRemovedOnDamage)
		{
			if (hasBuffs())
			{
				for (L2Effect e : _buffs)
				{
					if ((e != null) && e.getSkill().isRemovedOnDamage() && (awake || (e.getSkill().getSkillType() != L2SkillType.SLEEP)))
					{
						e.exit(true);
					}
				}
			}
		}
		if (_hasDebuffsRemovedOnDamage)
		{
			if (hasDebuffs())
			{
				for (L2Effect e : _debuffs)
				{
					if ((e != null) && e.getSkill().isRemovedOnDamage() && (awake || (e.getSkill().getSkillType() != L2SkillType.SLEEP)))
					{
						e.exit(true);
					}
				}
			}
		}
	}
	
	public void updateEffectIcons(boolean partyOnly)
	{
		if ((_buffs == null) && (_debuffs == null))
		{
			return;
		}
		
		if (partyOnly)
		{
			_partyOnly = true;
		}
		
		queueRunner();
	}
	
	public void queueEffect(L2Effect effect, boolean remove)
	{
		if (effect == null)
		{
			return;
		}
		
		if (!_queuesInitialized)
		{
			init();
		}
		
		if (remove)
		{
			_removeQueue.offer(effect);
		}
		else
		{
			_addQueue.offer(effect);
		}
		
		queueRunner();
	}
	
	private synchronized void init()
	{
		if (_queuesInitialized)
		{
			return;
		}
		_addQueue = new LinkedBlockingQueue<>();
		_removeQueue = new LinkedBlockingQueue<>();
		_queuesInitialized = true;
	}
	
	private void queueRunner()
	{
		if (!queueLock.compareAndSet(false, true))
		{
			return;
		}
		
		try
		{
			L2Effect effect;
			do
			{
				// remove has more priority than add
				// so removing all effects from queue first
				while ((effect = _removeQueue.poll()) != null)
				{
					removeEffectFromQueue(effect);
					_partyOnly = false;
				}
				
				if ((effect = _addQueue.poll()) != null)
				{
					addEffectFromQueue(effect);
					_partyOnly = false;
				}
			}
			while (!_addQueue.isEmpty() || !_removeQueue.isEmpty());
			
			computeEffectFlags();
			updateEffectIcons();
		}
		finally
		{
			queueLock.set(false);
		}
	}
	
	protected void removeEffectFromQueue(L2Effect effect)
	{
		if (effect == null)
		{
			return;
		}
		
		if (effect.isPassiveEffect())
		{
			if (effect.setInUse(false))
			{
				// Remove Func added by this effect from the L2Character Calculator
				_owner.removeStatsOwner(effect);
				_passives.remove(effect);
			}
		}
		
		FastList<L2Effect> effectList;
		
		// array modified, then rebuild on next request
		_rebuildCache = true;
		
		if (effect.getSkill().isDebuff())
		{
			if (!hasDebuffs())
			{
				return;
			}
			effectList = _debuffs;
		}
		else
		{
			if (!hasBuffs())
			{
				return;
			}
			effectList = _buffs;
		}
		
		if ("none".equals(effect.getAbnormalType()))
		{
			// Remove Func added by this effect from the L2Character Calculator
			_owner.removeStatsOwner(effect);
		}
		else
		{
			if (_stackedEffects == null)
			{
				return;
			}
			
			// Get the list of all stacked effects corresponding to the stack type of the L2Effect to add
			List<L2Effect> stackQueue = _stackedEffects.get(effect.getAbnormalType());
			
			if ((stackQueue == null) || stackQueue.isEmpty())
			{
				return;
			}
			
			int index = stackQueue.indexOf(effect);
			
			// Remove the effect from the stack group
			if (index >= 0)
			{
				stackQueue.remove(effect);
				// Check if the first stacked effect was the effect to remove
				if (index == 0)
				{
					// Remove all its Func objects from the L2Character calculator set
					_owner.removeStatsOwner(effect);
					
					// Check if there's another effect in the Stack Group
					if (!stackQueue.isEmpty())
					{
						L2Effect newStackedEffect = listsContains(stackQueue.get(0));
						if (newStackedEffect != null)
						{
							// Set the effect to In Use
							if (newStackedEffect.setInUse(true))
							{
								// Add its list of Funcs to the Calculator set of the L2Character
								_owner.addStatFuncs(newStackedEffect.getStatFuncs());
							}
						}
					}
				}
				if (stackQueue.isEmpty())
				{
					_stackedEffects.remove(effect.getAbnormalType());
				}
				else
				{
					// Update the Stack Group table _stackedEffects of the L2Character
					_stackedEffects.put(effect.getAbnormalType(), stackQueue);
				}
			}
		}
		
		// Remove the active skill L2effect from _effects of the L2Character
		if (effectList.remove(effect) && _owner.isPlayer() && effect.getShowIcon())
		{
			SystemMessage sm;
			if (effect.getSkill().isToggle())
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ABORTED);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.EFFECT_S1_DISAPPEARED);
			}
			sm.addSkillName(effect);
			_owner.sendPacket(sm);
		}
	}
	
	protected void addEffectFromQueue(L2Effect newEffect)
	{
		if (newEffect == null)
		{
			return;
		}
		
		L2Skill newSkill = newEffect.getSkill();
		
		// Passive effects are treated specially
		if (newEffect.isPassiveEffect())
		{
			if (_passives == null)
			{
				_passives = new FastList<L2Effect>().shared();
			}
			
			// Passive effects dont need stack type
			if ("none".equals(newEffect.getAbnormalType()))
			{
				// Set this L2Effect to In Use
				if (newEffect.setInUse(true))
				{
					for (L2Effect eff : _passives)
					{
						if (eff == null)
						{
							continue;
						}
						
						// Check and remove if there is already such effect in order to prevent passive effects overstack.
						if (eff.getEffectTemplate().equals(newEffect.getEffectTemplate()))
						{
							eff.exit();
						}
						
					}
					
					// Add Funcs of this effect to the Calculator set of the L2Character
					_owner.addStatFuncs(newEffect.getStatFuncs());
					_passives.add(newEffect);
				}
			}
			
			return;
		}
		
		// array modified, then rebuild on next request
		_rebuildCache = true;
		
		if (newSkill.isDebuff())
		{
			if (_debuffs == null)
			{
				_debuffs = new FastList<L2Effect>().shared();
			}
			for (L2Effect e : _debuffs)
			{
				if ((e != null) && (e.getSkill().getId() == newEffect.getSkill().getId()) && (e.getEffectType() == newEffect.getEffectType()) && (e.getAbnormalLvl() == newEffect.getAbnormalLvl()) && e.getAbnormalType().equals(newEffect.getAbnormalType()))
				{
					// Started scheduled timer needs to be canceled.
					newEffect.stopEffectTask();
					return;
				}
			}
			_debuffs.addLast(newEffect);
		}
		else
		{
			if (_buffs == null)
			{
				_buffs = new FastList<L2Effect>().shared();
			}
			
			for (L2Effect e : _buffs)
			{
				if ((e != null) && (e.getSkill().getId() == newEffect.getSkill().getId()) && (e.getEffectType() == newEffect.getEffectType()) && (e.getAbnormalLvl() == newEffect.getAbnormalLvl()) && e.getAbnormalType().equals(newEffect.getAbnormalType()))
				{
					e.exit(); // exit this
				}
			}
			
			// Remove first buff when buff list is full
			if (!doesStack(newSkill) && !newSkill.is7Signs())
			{
				int effectsToRemove;
				if (newSkill.isDance())
				{
					effectsToRemove = getDanceCount() - Config.DANCES_MAX_AMOUNT;
					if (effectsToRemove >= 0)
					{
						for (L2Effect e : _buffs)
						{
							if ((e == null) || !e.getSkill().isDance())
							{
								continue;
							}
							
							// get first dance
							e.exit();
							effectsToRemove--;
							if (effectsToRemove < 0)
							{
								break;
							}
						}
					}
				}
				else if (newSkill.isTriggeredSkill())
				{
					effectsToRemove = getTriggeredBuffCount() - Config.TRIGGERED_BUFFS_MAX_AMOUNT;
					if (effectsToRemove >= 0)
					{
						for (L2Effect e : _buffs)
						{
							if ((e == null) || !e.getSkill().isTriggeredSkill())
							{
								continue;
							}
							
							// get first dance
							e.exit();
							effectsToRemove--;
							if (effectsToRemove < 0)
							{
								break;
							}
						}
					}
				}
				else
				{
					effectsToRemove = getBuffCount() - _owner.getMaxBuffCount();
					if (effectsToRemove >= 0)
					{
						if (newSkill.getSkillType() == L2SkillType.BUFF)
						{
							for (L2Effect e : _buffs)
							{
								if ((e == null) || e.getSkill().isDance() || e.getSkill().isTriggeredSkill())
								{
									continue;
								}
								
								if (e.getSkill().getSkillType() == L2SkillType.BUFF)
								{
									e.exit();
									effectsToRemove--;
								}
								else
								{
									continue; // continue for()
								}
								
								if (effectsToRemove < 0)
								{
									break; // break for()
								}
							}
						}
					}
				}
			}
			
			// Icons order: buffs, 7s, toggles, dances, activation buffs
			if (newSkill.isTriggeredSkill())
			{
				_buffs.addLast(newEffect);
			}
			else
			{
				int pos = 0;
				if (newSkill.isToggle())
				{
					// toggle skill - before all dances
					for (L2Effect e : _buffs)
					{
						if (e == null)
						{
							continue;
						}
						if (e.getSkill().isDance())
						{
							break;
						}
						pos++;
					}
				}
				else if (newSkill.isDance())
				{
					// dance skill - before all activation buffs
					for (L2Effect e : _buffs)
					{
						if (e == null)
						{
							continue;
						}
						if (e.getSkill().isTriggeredSkill())
						{
							break;
						}
						pos++;
					}
				}
				else
				{
					// normal buff - before toggles and 7s and dances
					for (L2Effect e : _buffs)
					{
						if (e == null)
						{
							continue;
						}
						if (e.getSkill().isToggle() || e.getSkill().is7Signs() || e.getSkill().isDance() || e.getSkill().isTriggeredSkill())
						{
							break;
						}
						pos++;
					}
				}
				_buffs.add(pos, newEffect);
			}
		}
		
		// Check if a stack group is defined for this effect
		if ("none".equals(newEffect.getAbnormalType()))
		{
			// Set this L2Effect to In Use
			if (newEffect.setInUse(true))
			{
				// Add Funcs of this effect to the Calculator set of the L2Character
				_owner.addStatFuncs(newEffect.getStatFuncs());
			}
			
			return;
		}
		
		List<L2Effect> stackQueue;
		L2Effect effectToAdd = null;
		L2Effect effectToRemove = null;
		if (_stackedEffects == null)
		{
			_stackedEffects = new FastMap<>();
		}
		
		// Get the list of all stacked effects corresponding to the stack type of the L2Effect to add
		stackQueue = _stackedEffects.get(newEffect.getAbnormalType());
		
		if (stackQueue != null)
		{
			int pos = 0;
			if (!stackQueue.isEmpty())
			{
				// Get the first stacked effect of the Stack group selected
				effectToRemove = listsContains(stackQueue.get(0));
				
				// Create an Iterator to go through the list of stacked effects in progress on the L2Character
				Iterator<L2Effect> queueIterator = stackQueue.iterator();
				
				while (queueIterator.hasNext())
				{
					if (newEffect.getAbnormalLvl() < queueIterator.next().getAbnormalLvl())
					{
						pos++;
					}
					else
					{
						break;
					}
				}
				// Add the new effect to the Stack list in function of its position in the Stack group
				stackQueue.add(pos, newEffect);
				
				// skill.exit() could be used, if the users don't wish to see "effect
				// removed" always when a timer goes off, even if the buff isn't active
				// any more (has been replaced). but then check e.g. npc hold and raid petrification.
				if (Config.EFFECT_CANCELING && !newEffect.getSkill().isStatic() && (stackQueue.size() > 1))
				{
					if (newSkill.isDebuff())
					{
						_debuffs.remove(stackQueue.remove(1));
					}
					else
					{
						_buffs.remove(stackQueue.remove(1));
					}
				}
			}
			else
			{
				stackQueue.add(0, newEffect);
			}
		}
		else
		{
			stackQueue = new FastList<>();
			stackQueue.add(0, newEffect);
		}
		
		// Update the Stack Group table _stackedEffects of the L2Character
		_stackedEffects.put(newEffect.getAbnormalType(), stackQueue);
		
		// Get the first stacked effect of the Stack group selected
		if (!stackQueue.isEmpty())
		{
			effectToAdd = listsContains(stackQueue.get(0));
		}
		
		if (effectToRemove != effectToAdd)
		{
			if (effectToRemove != null)
			{
				// Remove all Func objects corresponding to this stacked effect from the Calculator set of the L2Character
				_owner.removeStatsOwner(effectToRemove);
				
				// Set the L2Effect to Not In Use
				effectToRemove.setInUse(false);
			}
			if (effectToAdd != null)
			{
				// Set this L2Effect to In Use
				if (effectToAdd.setInUse(true))
				{
					// Add all Func objects corresponding to this stacked effect to the Calculator set of the L2Character
					_owner.addStatFuncs(effectToAdd.getStatFuncs());
				}
			}
		}
	}
	
	/**
	 * Remove all passive effects held by this <b>skillId</b>.
	 * @param skillId
	 */
	public void removePassiveEffects(int skillId)
	{
		if (_passives == null)
		{
			return;
		}
		
		for (L2Effect eff : _passives)
		{
			if (eff == null)
			{
				continue;
			}
			
			if (eff.getSkill().getId() == skillId)
			{
				eff.exit();
			}
		}
	}
	
	protected void updateEffectIcons()
	{
		if (_owner == null)
		{
			return;
		}
		
		if (!_owner.isPlayable())
		{
			updateEffectFlags();
			return;
		}
		
		AbnormalStatusUpdate mi = null;
		PartySpelled ps = null;
		PartySpelled psSummon = null;
		ExOlympiadSpelledInfo os = null;
		boolean isSummon = false;
		
		if (_owner.isPlayer())
		{
			if (_partyOnly)
			{
				_partyOnly = false;
			}
			else
			{
				mi = new AbnormalStatusUpdate();
			}
			
			if (_owner.isInParty())
			{
				ps = new PartySpelled(_owner);
			}
			
			if (_owner.getActingPlayer().isInOlympiadMode() && _owner.getActingPlayer().isOlympiadStart())
			{
				os = new ExOlympiadSpelledInfo(_owner.getActingPlayer());
			}
		}
		else if (_owner.isSummon())
		{
			isSummon = true;
			ps = new PartySpelled(_owner);
			psSummon = new PartySpelled(_owner);
		}
		
		boolean foundRemovedOnAction = false;
		boolean foundRemovedOnDamage = false;
		
		if (hasBuffs())
		{
			for (L2Effect e : _buffs)
			{
				if (e == null)
				{
					continue;
				}
				
				if (e.getSkill().isRemovedOnAnyActionExceptMove())
				{
					foundRemovedOnAction = true;
				}
				if (e.getSkill().isRemovedOnDamage())
				{
					foundRemovedOnDamage = true;
				}
				
				if (!e.getShowIcon())
				{
					continue;
				}
				
				if (e.getEffectType() == L2EffectType.SIGNET_GROUND)
				{
					continue;
				}
				
				if (e.getInUse())
				{
					if (mi != null)
					{
						e.addIcon(mi);
					}
					
					if (ps != null)
					{
						if (isSummon || (!e.getSkill().isToggle() && !(e.getSkill().isStatic() && ((e.getEffectType() == L2EffectType.HEAL_OVER_TIME) || (e.getEffectType() == L2EffectType.CPHEAL_OVER_TIME) || (e.getEffectType() == L2EffectType.MANA_HEAL_OVER_TIME)))))
						{
							e.addPartySpelledIcon(ps);
						}
					}
					
					if (psSummon != null)
					{
						if (!e.getSkill().isToggle() && !(e.getSkill().isStatic() && ((e.getEffectType() == L2EffectType.HEAL_OVER_TIME) || (e.getEffectType() == L2EffectType.CPHEAL_OVER_TIME) || (e.getEffectType() == L2EffectType.MANA_HEAL_OVER_TIME))))
						{
							e.addPartySpelledIcon(psSummon);
						}
					}
					
					if (os != null)
					{
						e.addOlympiadSpelledIcon(os);
					}
				}
			}
			
		}
		
		_hasBuffsRemovedOnAnyAction = foundRemovedOnAction;
		_hasBuffsRemovedOnDamage = foundRemovedOnDamage;
		foundRemovedOnDamage = false;
		
		if (hasDebuffs())
		{
			for (L2Effect e : _debuffs)
			{
				if (e == null)
				{
					continue;
				}
				
				if (e.getSkill().isRemovedOnAnyActionExceptMove())
				{
					foundRemovedOnAction = true;
				}
				if (e.getSkill().isRemovedOnDamage())
				{
					foundRemovedOnDamage = true;
				}
				
				if (!e.getShowIcon())
				{
					continue;
				}
				
				switch (e.getEffectType())
				{
					case SIGNET_GROUND:
						continue;
				}
				
				if (e.getInUse())
				{
					if (mi != null)
					{
						e.addIcon(mi);
					}
					
					if (ps != null)
					{
						e.addPartySpelledIcon(ps);
					}
					
					if (psSummon != null)
					{
						e.addPartySpelledIcon(psSummon);
					}
					
					if (os != null)
					{
						e.addOlympiadSpelledIcon(os);
					}
				}
			}
			
		}
		
		_hasDebuffsRemovedOnDamage = foundRemovedOnDamage;
		
		if (mi != null)
		{
			_owner.sendPacket(mi);
		}
		
		if (ps != null)
		{
			if (_owner.isSummon())
			{
				L2PcInstance summonOwner = ((L2Summon) _owner).getOwner();
				
				if (summonOwner != null)
				{
					if (summonOwner.isInParty())
					{
						summonOwner.getParty().broadcastToPartyMembers(summonOwner, psSummon); // send to all member except summonOwner
						summonOwner.sendPacket(ps); // now send to summonOwner
					}
					else
					{
						summonOwner.sendPacket(ps);
					}
				}
			}
			else if (_owner.isPlayer() && _owner.isInParty())
			{
				_owner.getParty().broadcastPacket(ps);
			}
		}
		
		if (os != null)
		{
			final OlympiadGameTask game = OlympiadGameManager.getInstance().getOlympiadTask(_owner.getActingPlayer().getOlympiadGameId());
			if ((game != null) && game.isBattleStarted())
			{
				game.getZone().broadcastPacketToObservers(os);
			}
		}
	}
	
	protected void updateEffectFlags()
	{
		boolean foundRemovedOnAction = false;
		boolean foundRemovedOnDamage = false;
		
		if (hasBuffs())
		{
			for (L2Effect e : _buffs)
			{
				if (e == null)
				{
					continue;
				}
				
				if (e.getSkill().isRemovedOnAnyActionExceptMove())
				{
					foundRemovedOnAction = true;
				}
				if (e.getSkill().isRemovedOnDamage())
				{
					foundRemovedOnDamage = true;
				}
			}
		}
		_hasBuffsRemovedOnAnyAction = foundRemovedOnAction;
		_hasBuffsRemovedOnDamage = foundRemovedOnDamage;
		foundRemovedOnDamage = false;
		
		if (hasDebuffs())
		{
			for (L2Effect e : _debuffs)
			{
				if (e == null)
				{
					continue;
				}
				
				if (e.getSkill().isRemovedOnDamage())
				{
					foundRemovedOnDamage = true;
				}
			}
		}
		_hasDebuffsRemovedOnDamage = foundRemovedOnDamage;
	}
	
	/**
	 * Returns effect if contains in _buffs or _debuffs and null if not found
	 * @param effect
	 * @return
	 */
	private L2Effect listsContains(L2Effect effect)
	{
		if (hasBuffs() && _buffs.contains(effect))
		{
			return effect;
		}
		if (hasDebuffs() && _debuffs.contains(effect))
		{
			return effect;
		}
		return null;
	}
	
	/**
	 * Recalculate effect bits flag.<br>
	 * Please no concurrency access
	 */
	private final void computeEffectFlags()
	{
		int flags = 0;
		
		if (_buffs != null)
		{
			for (L2Effect e : _buffs)
			{
				if (e == null)
				{
					continue;
				}
				flags |= e.getEffectFlags();
			}
		}
		
		if (_debuffs != null)
		{
			for (L2Effect e : _debuffs)
			{
				if (e == null)
				{
					continue;
				}
				flags |= e.getEffectFlags();
			}
		}
		
		_effectFlags = flags;
	}
	
	/**
	 * Verify if this effect list is empty.
	 * @return {@code true} if both {@link #_buffs} and {@link #_debuffs} are either {@code null} or empty
	 */
	public boolean isEmpty()
	{
		return ((_buffs == null) || _buffs.isEmpty()) && ((_debuffs == null) || _debuffs.isEmpty());
	}
	
	/**
	 * Verify if this effect list has buffs effects.
	 * @return {@code true} if {@link #_buffs} is not {@code null} and is not empty
	 */
	public boolean hasBuffs()
	{
		return (_buffs != null) && !_buffs.isEmpty();
	}
	
	/**
	 * Verify if this effect list has debuffs effects.
	 * @return {@code true} if {@link #_debuffs} is not {@code null} and is not empty
	 */
	public boolean hasDebuffs()
	{
		return (_debuffs != null) && !_debuffs.isEmpty();
	}
	
	/**
	 * Check if target is affected with special buff
	 * @param flag of special buff
	 * @return boolean true if affected
	 */
	public boolean isAffected(EffectFlag flag)
	{
		return (_effectFlags & flag.getMask()) != 0;
	}
	
	/**
	 * Clear and null all queues and lists Use only during delete character from the world.
	 */
	public void clear()
	{
		/*
		 * Removed .clear() since nodes/entries and its references (Effects) should be terminated by GC when Queue/Map/List object has no more reference. This way we will save a little more CPU [DrHouse]
		 */
		try
		{
			_addQueue = null;
			_removeQueue = null;
			_buffs = null;
			_debuffs = null;
			_stackedEffects = null;
			_queuesInitialized = false;
			_effectCache = null;
		}
		catch (Exception e)
		{
			_log.warn(String.valueOf(e));
		}
	}
}
