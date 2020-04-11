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
package l2r.gameserver.model.actor.status;

import l2r.Config;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.PrivateStoreType;
import l2r.gameserver.instancemanager.DuelManager;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.stat.PcStat;
import l2r.gameserver.model.entity.Duel;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.stats.Formulas;
import l2r.gameserver.model.stats.Stats;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;
import gr.reunion.interf.ReunionEvents;

public class PcStatus extends PlayableStatus
{
	private double _currentCp = 0; // Current CP of the L2PcInstance
	
	public PcStatus(L2PcInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public final void reduceCp(int value)
	{
		if (getCurrentCp() > value)
		{
			setCurrentCp(getCurrentCp() - value);
		}
		else
		{
			setCurrentCp(0);
		}
	}
	
	@Override
	public final void reduceHp(double value, L2Character attacker)
	{
		reduceHp(value, attacker, true, false, false, false);
	}
	
	@Override
	public final void reduceHp(double value, L2Character attacker, boolean awake, boolean isDOT, boolean isHPConsumption)
	{
		reduceHp(value, attacker, awake, isDOT, isHPConsumption, false);
	}
	
	public final void reduceHp(double value, L2Character attacker, boolean awake, boolean isDOT, boolean isHPConsumption, boolean ignoreCP)
	{
		if (getActiveChar().isDead())
		{
			return;
		}
		
		// If OFFLINE_MODE_NO_DAMAGE is enabled and player is offline and he is in store/craft mode, no damage is taken.
		if (Config.OFFLINE_MODE_NO_DAMAGE && (getActiveChar().getClient() != null) && getActiveChar().getClient().isDetached() && ((Config.OFFLINE_TRADE_ENABLE && ((getActiveChar().getPrivateStoreType() == PrivateStoreType.SELL) || (getActiveChar().getPrivateStoreType() == PrivateStoreType.BUY))) || (Config.OFFLINE_CRAFT_ENABLE && (getActiveChar().isInCraftMode() || (getActiveChar().getPrivateStoreType() == PrivateStoreType.MANUFACTURE)))))
		{
			return;
		}
		
		if (getActiveChar().isInvul() && !(isDOT || isHPConsumption))
		{
			return;
		}
		
		if (!isHPConsumption)
		{
			getActiveChar().stopEffectsOnDamage(awake);
			// Attacked players in craft/shops stand up.
			if (getActiveChar().isInCraftMode() || getActiveChar().isInStoreMode())
			{
				getActiveChar().setPrivateStoreType(PrivateStoreType.NONE);
				getActiveChar().standUp();
				getActiveChar().broadcastUserInfo();
			}
			else if (getActiveChar().isSitting())
			{
				getActiveChar().standUp();
			}
			
			if (!isDOT)
			{
				if (getActiveChar().isStunned() && (Rnd.get(10) == 0))
				{
					getActiveChar().stopStunning(true);
				}
			}
		}
		
		int fullValue = (int) value;
		int tDmg = 0;
		int mpDam = 0;
		
		if ((attacker != null) && (attacker != getActiveChar()))
		{
			final L2PcInstance attackerPlayer = attacker.getActingPlayer();
			
			if (attackerPlayer != null)
			{
				if (attackerPlayer.isGM() && !attackerPlayer.getAccessLevel().canGiveDamage())
				{
					return;
				}
				
				if (getActiveChar().isInDuel())
				{
					if (getActiveChar().getDuelState() == Duel.DUELSTATE_DEAD)
					{
						return;
					}
					else if (getActiveChar().getDuelState() == Duel.DUELSTATE_WINNER)
					{
						return;
					}
					
					// cancel duel if player got hit by another player, that is not part of the duel
					if (attackerPlayer.getDuelId() != getActiveChar().getDuelId())
					{
						getActiveChar().setDuelState(Duel.DUELSTATE_INTERRUPTED);
					}
				}
			}
			
			if (attackerPlayer != null)
			{
				if (ReunionEvents.isInEvent(attackerPlayer))
				{
					ReunionEvents.onHit(attackerPlayer, getActiveChar(), fullValue, isDOT);
				}
			}
			
			// Check and calculate transfered damage
			final L2Summon summon = getActiveChar().getSummon();
			if (getActiveChar().hasServitor() && Util.checkIfInRange(1000, getActiveChar(), summon, true))
			{
				tDmg = ((int) value * (int) getActiveChar().getStat().calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0, null, null)) / 100;
				
				// Only transfer dmg up to current HP, it should not be killed
				tDmg = Math.min((int) summon.getCurrentHp() - 1, tDmg);
				if (tDmg > 0)
				{
					summon.reduceCurrentHp(tDmg, attacker, null);
					value -= tDmg;
					fullValue = (int) value; // reduce the announced value here as player will get a message about summon damage
				}
			}
			
			mpDam = ((int) value * (int) getActiveChar().getStat().calcStat(Stats.MANA_SHIELD_PERCENT, 0, null, null)) / 100;
			
			if (mpDam > 0)
			{
				mpDam = (int) (value - mpDam);
				if (mpDam > getActiveChar().getCurrentMp())
				{
					getActiveChar().sendPacket(SystemMessageId.MP_BECAME_0_ARCANE_SHIELD_DISAPPEARING);
					getActiveChar().stopSkillEffects(1556);
					value = mpDam - getActiveChar().getCurrentMp();
					getActiveChar().setCurrentMp(0);
				}
				else
				{
					getActiveChar().reduceCurrentMp(mpDam);
					SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.ARCANE_SHIELD_DECREASED_YOUR_MP_BY_S1_INSTEAD_OF_HP);
					smsg.addInt(mpDam);
					getActiveChar().sendPacket(smsg);
					return;
				}
			}
			
			final L2PcInstance caster = getActiveChar().getTransferingDamageTo();
			if ((caster != null) && (getActiveChar().getParty() != null) && Util.checkIfInRange(1000, getActiveChar(), caster, true) && !caster.isDead() && (getActiveChar() != caster) && getActiveChar().getParty().getMembers().contains(caster))
			{
				int transferDmg = 0;
				
				transferDmg = ((int) value * (int) getActiveChar().getStat().calcStat(Stats.TRANSFER_DAMAGE_TO_PLAYER, 0, null, null)) / 100;
				transferDmg = Math.min((int) caster.getCurrentHp() - 1, transferDmg);
				if (transferDmg > 0)
				{
					int membersInRange = 0;
					for (L2PcInstance member : caster.getParty().getMembers())
					{
						if (Util.checkIfInRange(1000, member, caster, false) && (member != caster))
						{
							membersInRange++;
						}
					}
					
					if ((attacker instanceof L2Playable) && (caster.getCurrentCp() > 0))
					{
						if (caster.getCurrentCp() > transferDmg)
						{
							caster.getStatus().reduceCp(transferDmg);
						}
						else
						{
							transferDmg = (int) (transferDmg - caster.getCurrentCp());
							caster.getStatus().reduceCp((int) caster.getCurrentCp());
						}
					}
					
					if (membersInRange > 0)
					{
						caster.reduceCurrentHp(transferDmg / membersInRange, attacker, null);
						value -= transferDmg;
						fullValue = (int) value;
					}
				}
			}
			
			if (!ignoreCP && (attacker instanceof L2Playable))
			{
				if (getCurrentCp() >= value)
				{
					setCurrentCp(getCurrentCp() - value); // Set Cp to diff of Cp vs value
					value = 0; // No need to subtract anything from Hp
				}
				else
				{
					value -= getCurrentCp(); // Get diff from value vs Cp; will apply diff to Hp
					setCurrentCp(0, false); // Set Cp to 0
				}
			}
			
			if ((fullValue > 0) && !isDOT)
			{
				SystemMessage smsg;
				// Send a System Message to the L2PcInstance
				smsg = SystemMessage.getSystemMessage(SystemMessageId.C1_RECEIVED_DAMAGE_OF_S3_FROM_C2);
				smsg.addString(getActiveChar().getName());
				smsg.addCharName(attacker);
				smsg.addInt(fullValue);
				getActiveChar().sendPacket(smsg);
				
				if (tDmg > 0)
				{
					smsg = SystemMessage.getSystemMessage(SystemMessageId.C1_RECEIVED_DAMAGE_OF_S3_FROM_C2);
					smsg.addString(getActiveChar().getSummon().getName());
					smsg.addCharName(attacker);
					smsg.addInt(tDmg);
					getActiveChar().sendPacket(smsg);
					
					if (attackerPlayer != null)
					{
						smsg = SystemMessage.getSystemMessage(SystemMessageId.GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_SERVITOR);
						smsg.addInt(fullValue);
						smsg.addInt(tDmg);
						attackerPlayer.sendPacket(smsg);
					}
				}
			}
		}
		
		if (value > 0)
		{
			value = getCurrentHp() - value;
			if (value <= 0)
			{
				if (getActiveChar().isInDuel())
				{
					getActiveChar().disableAllSkills();
					stopHpMpRegeneration();
					if (attacker != null)
					{
						attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
						attacker.sendPacket(ActionFailed.STATIC_PACKET);
					}
					// let the DuelManager know of his defeat
					DuelManager.getInstance().onPlayerDefeat(getActiveChar());
					value = 1;
				}
				else
				{
					value = 0;
				}
			}
			setCurrentHp(value);
		}
		
		if (getActiveChar().getCurrentHp() < 0.5)
		{
			getActiveChar().abortAttack();
			getActiveChar().abortCast();
			
			if (getActiveChar().isInOlympiadMode())
			{
				stopHpMpRegeneration();
				getActiveChar().setIsDead(true);
				getActiveChar().setIsPendingRevive(true);
				if (getActiveChar().hasSummon())
				{
					getActiveChar().getSummon().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
				}
				return;
			}
			
			getActiveChar().doDie(attacker);
			if (!Config.DISABLE_TUTORIAL)
			{
				QuestState qs = getActiveChar().getQuestState("255_Tutorial");
				if (qs != null)
				{
					qs.getQuest().notifyEvent("CE30", null, getActiveChar());
				}
			}
		}
	}
	
	@Override
	public final boolean setCurrentHp(double newHp, boolean broadcastPacket)
	{
		boolean result = super.setCurrentHp(newHp, broadcastPacket);
		
		if (!Config.DISABLE_TUTORIAL && (getCurrentHp() <= (getActiveChar().getStat().getMaxHp() * .3)))
		{
			QuestState qs = getActiveChar().getQuestState("255_Tutorial");
			if (qs != null)
			{
				qs.getQuest().notifyEvent("CE45", null, getActiveChar());
			}
		}
		
		return result;
	}
	
	@Override
	public final double getCurrentCp()
	{
		return _currentCp;
	}
	
	@Override
	public final void setCurrentCp(double newCp)
	{
		setCurrentCp(newCp, true);
	}
	
	public final void setCurrentCp(double newCp, boolean broadcastPacket)
	{
		// Get the Max CP of the L2Character
		int currentCp = (int) getCurrentCp();
		int maxCp = getActiveChar().getStat().getMaxCp();
		
		synchronized (this)
		{
			if (getActiveChar().isDead())
			{
				return;
			}
			
			if (newCp < 0)
			{
				newCp = 0;
			}
			
			if (newCp >= maxCp)
			{
				// Set the RegenActive flag to false
				_currentCp = maxCp;
				_flagsRegenActive &= ~REGEN_FLAG_CP;
				
				// Stop the HP/MP/CP Regeneration task
				if (_flagsRegenActive == 0)
				{
					stopHpMpRegeneration();
				}
			}
			else
			{
				// Set the RegenActive flag to true
				_currentCp = newCp;
				_flagsRegenActive |= REGEN_FLAG_CP;
				
				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		if ((currentCp != _currentCp) && broadcastPacket)
		{
			getActiveChar().broadcastStatusUpdate();
		}
	}
	
	@Override
	protected void doRegeneration()
	{
		final PcStat charstat = getActiveChar().getStat();
		
		// Modify the current CP of the L2Character and broadcast Server->Client packet StatusUpdate
		if (getCurrentCp() < charstat.getMaxRecoverableCp())
		{
			setCurrentCp(getCurrentCp() + Formulas.calcCpRegen(getActiveChar()), false);
		}
		
		// Modify the current HP of the L2Character and broadcast Server->Client packet StatusUpdate
		if (getCurrentHp() < charstat.getMaxRecoverableHp())
		{
			setCurrentHp(getCurrentHp() + Formulas.calcHpRegen(getActiveChar()), false);
		}
		
		// Modify the current MP of the L2Character and broadcast Server->Client packet StatusUpdate
		if (getCurrentMp() < charstat.getMaxRecoverableMp())
		{
			setCurrentMp(getCurrentMp() + Formulas.calcMpRegen(getActiveChar()), false);
		}
		
		getActiveChar().broadcastStatusUpdate(); // send the StatusUpdate packet
	}
	
	@Override
	public L2PcInstance getActiveChar()
	{
		return (L2PcInstance) super.getActiveChar();
	}
}
