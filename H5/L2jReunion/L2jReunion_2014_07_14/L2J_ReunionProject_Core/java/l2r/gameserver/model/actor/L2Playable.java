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
package l2r.gameserver.model.actor;

import l2r.Config;
import l2r.gameserver.enums.CtrlEvent;
import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.actor.events.PlayableEvents;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.knownlist.PlayableKnownList;
import l2r.gameserver.model.actor.stat.PlayableStat;
import l2r.gameserver.model.actor.status.PlayableStatus;
import l2r.gameserver.model.actor.templates.L2CharTemplate;
import l2r.gameserver.model.effects.EffectFlag;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.entity.Instance;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.skills.L2Skill;

/**
 * This class represents all Playable characters in the world.<br>
 * L2Playable:
 * <ul>
 * <li>L2PcInstance</li>
 * <li>L2Summon</li>
 * </ul>
 */
public abstract class L2Playable extends L2Character
{
	private L2Character _lockedTarget = null;
	
	/**
	 * Constructor of L2Playable.<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Call the L2Character constructor to create an empty _skills slot and link copy basic Calculator set to this L2Playable</li>
	 * </ul>
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2CharTemplate to apply to the L2Playable
	 */
	public L2Playable(int objectId, L2CharTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2Playable);
		setIsInvul(false);
	}
	
	@Override
	public PlayableKnownList getKnownList()
	{
		return (PlayableKnownList) super.getKnownList();
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new PlayableKnownList(this));
	}
	
	@Override
	public PlayableStat getStat()
	{
		return (PlayableStat) super.getStat();
	}
	
	@Override
	public void initCharStat()
	{
		setStat(new PlayableStat(this));
	}
	
	@Override
	public PlayableStatus getStatus()
	{
		return (PlayableStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new PlayableStatus(this));
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!getEvents().onDeath(killer))
		{
			return false;
		}
		
		// killing is only possible one time
		synchronized (this)
		{
			if (isDead())
			{
				return false;
			}
			// now reset currentHp to zero
			setCurrentHp(0);
			setIsDead(true);
		}
		
		// Set target to null and cancel Attack or Cast
		setTarget(null);
		
		// Stop movement
		stopMove(null);
		
		// Stop HP/MP/CP Regeneration task
		getStatus().stopHpMpRegeneration();
		
		// Stop all active skills effects in progress on the L2Character,
		// if the Character isn't affected by Soul of The Phoenix or Salvation
		if (isPhoenixBlessed())
		{
			if (isCharmOfLuckAffected())
			{
				stopEffects(L2EffectType.CHARM_OF_LUCK);
			}
			if (isNoblesseBlessed())
			{
				stopEffects(L2EffectType.NOBLESSE_BLESSING);
			}
		}
		// Same thing if the Character isn't a Noblesse Blessed L2Playable
		else if (isNoblesseBlessed())
		{
			stopEffects(L2EffectType.NOBLESSE_BLESSING);
			
			if (isCharmOfLuckAffected())
			{
				stopEffects(L2EffectType.CHARM_OF_LUCK);
			}
		}
		else
		{
			stopAllEffectsExceptThoseThatLastThroughDeath();
		}
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		broadcastStatusUpdate();
		
		if (getWorldRegion() != null)
		{
			getWorldRegion().onDeath(this);
		}
		
		// Notify Quest of L2Playable's death
		L2PcInstance actingPlayer = getActingPlayer();
		if (!actingPlayer.isNotifyQuestOfDeathEmpty())
		{
			for (QuestState qs : actingPlayer.getNotifyQuestOfDeath())
			{
				try
				{
					qs.getQuest().notifyDeath((killer == null ? this : killer), this, qs);
				}
				catch (Exception e)
				{
					if (Config.DEBUG_SCRIPT_NOTIFIES)
					{
						if (qs != null)
						{
							_log.error("L2Playeable[notifyDeath]1 quest name is: " + qs.getQuest().getName());
						}
						else
						{
							_log.error("L2Playeable[notifyDeath]1 qs is NULL");
						}
						
						if (killer == null)
						{
							_log.error("L2Playeable[notifyDeath]1 killer is NULL");
						}
						else
						{
							_log.error("L2Playeable[notifyDeath]1 killer is: " + killer.getName());
						}
						
						_log.error("L2Playeable[notifyDeath]1 ID is: " + this.getName());
					}
				}
			}
		}
		// Notify instance
		if (getInstanceId() > 0)
		{
			final Instance instance = InstanceManager.getInstance().getInstance(getInstanceId());
			if (instance != null)
			{
				try
				{
					instance.notifyDeath(killer, this);
				}
				catch (Exception e)
				{
					if (Config.DEBUG_SCRIPT_NOTIFIES)
					{
						if (killer == null)
						{
							_log.error("L2Playeable[notifyDeath]2 killer is NULL");
						}
						else
						{
							_log.error("L2Playeable[notifyDeath]2 killer is: " + killer.getName());
						}
						
						_log.error("L2Playeable[notifyDeath]2 ID is: " + this.getName());
					}
				}
			}
		}
		
		if (killer != null)
		{
			L2PcInstance player = killer.getActingPlayer();
			
			if (player != null)
			{
				player.onKillUpdatePvPKarma(this);
			}
		}
		
		// Notify L2Character AI
		getAI().notifyEvent(CtrlEvent.EVT_DEAD);
		
		return true;
	}
	
	public boolean checkIfPvP(L2Character target)
	{
		if (target == null)
		{
			return false; // Target is null
		}
		if (target == this)
		{
			return false; // Target is self
		}
		if (!target.isPlayable())
		{
			return false; // Target is not a L2Playable
		}
		
		final L2PcInstance player = getActingPlayer();
		if (player == null)
		{
			return false; // Active player is null
		}
		
		if (player.getKarma() != 0)
		{
			return false; // Active player has karma
		}
		
		final L2PcInstance targetPlayer = target.getActingPlayer();
		if (targetPlayer == null)
		{
			return false; // Target player is null
		}
		
		if (targetPlayer == this)
		{
			return false; // Target player is self
		}
		if (targetPlayer.getKarma() != 0)
		{
			return false; // Target player has karma
		}
		if (targetPlayer.getPvpFlag() == 0)
		{
			return false;
		}
		
		return true;
		// Even at war, there should be PvP flag
		// if(
		// player.getClan() == null ||
		// targetPlayer.getClan() == null ||
		// (
		// !targetPlayer.getClan().isAtWarWith(player.getClanId()) &&
		// targetPlayer.getWantsPeace() == 0 &&
		// player.getWantsPeace() == 0
		// )
		// )
		// {
		// return true;
		// }
		// return false;
	}
	
	/**
	 * Return True.
	 */
	@Override
	public boolean canBeAttacked()
	{
		return true;
	}
	
	// Support for Noblesse Blessing skill, where buffs are retained
	// after resurrect
	public final boolean isNoblesseBlessed()
	{
		return _effects.isAffected(EffectFlag.NOBLESS_BLESSING);
	}
	
	// Support for Soul of the Phoenix and Salvation skills
	public final boolean isPhoenixBlessed()
	{
		return _effects.isAffected(EffectFlag.PHOENIX_BLESSING);
	}
	
	/**
	 * @return True if the Silent Moving mode is active.
	 */
	public boolean isSilentMoving()
	{
		return _effects.isAffected(EffectFlag.SILENT_MOVE);
	}
	
	/**
	 * For Newbie Protection Blessing skill, keeps you safe from an attack by a chaotic character >= 10 levels apart from you.
	 * @return
	 */
	public final boolean isProtectionBlessingAffected()
	{
		return _effects.isAffected(EffectFlag.PROTECTION_BLESSING);
	}
	
	/**
	 * Charm of Luck - During a Raid/Boss war, decreased chance for death penalty.
	 * @return
	 */
	public final boolean isCharmOfLuckAffected()
	{
		return _effects.isAffected(EffectFlag.CHARM_OF_LUCK);
	}
	
	@Override
	public void updateEffectIcons(boolean partyOnly)
	{
		_effects.updateEffectIcons(partyOnly);
	}
	
	public boolean isLockedTarget()
	{
		return _lockedTarget != null;
	}
	
	public L2Character getLockedTarget()
	{
		return _lockedTarget;
	}
	
	public void setLockedTarget(L2Character cha)
	{
		_lockedTarget = cha;
	}
	
	L2PcInstance transferDmgTo;
	
	public void setTransferDamageTo(L2PcInstance val)
	{
		transferDmgTo = val;
	}
	
	public L2PcInstance getTransferingDamageTo()
	{
		return transferDmgTo;
	}
	
	@Override
	public void initCharEvents()
	{
		setCharEvents(new PlayableEvents(this));
	}
	
	@Override
	public PlayableEvents getEvents()
	{
		return (PlayableEvents) super.getEvents();
	}
	
	public abstract int getKarma();
	
	public abstract byte getPvpFlag();
	
	public abstract boolean useMagic(L2Skill skill, boolean forceUse, boolean dontMove);
	
	public abstract void store();
	
	public abstract void storeEffect(boolean storeEffects);
	
	public abstract void restoreEffects();
	
	@Override
	public boolean isPlayable()
	{
		return true;
	}
}
