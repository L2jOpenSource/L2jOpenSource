package com.l2jfrozen.gameserver.model.actor.status;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfrozen.gameserver.model.entity.Duel;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.Stats;
import com.l2jfrozen.gameserver.util.Util;

public class PcStatus extends PlayableStatus
{
	public PcStatus(final L2PcInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public final void reduceHp(final double value, final L2Character attacker)
	{
		reduceHp(value, attacker, true);
	}
	
	@Override
	public final void reduceHp(double value, final L2Character attacker, final boolean awake)
	{
		if (getActiveChar().isInvul() && getActiveChar() != attacker)
		{
			return;
		}
		
		if (attacker instanceof L2PcInstance)
		{
			if (getActiveChar().isInDuel())
			{
				// the duel is finishing - players do not recive damage
				if (getActiveChar().getDuelState() == Duel.DUELSTATE_DEAD)
				{
					return;
				}
				else if (getActiveChar().getDuelState() == Duel.DUELSTATE_WINNER)
				{
					return;
				}
				
				// cancel duel if player got hit by another player, that is not part of the duel
				if (((L2PcInstance) attacker).getDuelId() != getActiveChar().getDuelId())
				{
					getActiveChar().setDuelState(Duel.DUELSTATE_INTERRUPTED);
				}
			}
			
			if (getActiveChar().isDead() && !getActiveChar().isFakeDeath())
			{
				return;
			}
		}
		else
		{
			// if attacked by a non L2PcInstance & non L2SummonInstance the duel gets canceled
			if (getActiveChar().isInDuel() && !(attacker instanceof L2SummonInstance))
			{
				getActiveChar().setDuelState(Duel.DUELSTATE_INTERRUPTED);
			}
			
			if (getActiveChar().isDead())
			{
				return;
			}
		}
		
		int fullValue = (int) value;
		
		if (attacker != null && attacker != getActiveChar())
		{
			// Check and calculate transfered damage
			L2Summon summon = getActiveChar().getPet();
			
			// TODO correct range
			if (summon != null && summon instanceof L2SummonInstance && Util.checkIfInRange(900, getActiveChar(), summon, true))
			{
				int tDmg = (int) value * (int) getActiveChar().getStat().calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0, null, null) / 100;
				
				// Only transfer dmg up to current HP, it should not be killed
				if (summon.getCurrentHp() < tDmg)
				{
					tDmg = (int) summon.getCurrentHp() - 1;
				}
				
				if (tDmg > 0)
				{
					summon.reduceCurrentHp(tDmg, attacker);
					value -= tDmg;
					fullValue = (int) value; // reduce the annouced value here as player will get a message about summon dammage
					
					SystemMessage sm = new SystemMessage(SystemMessageId.GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_SERVITOR);
					sm.addNumber(fullValue);
					sm.addNumber(tDmg);
					attacker.sendPacket(sm);
				}
			}
			
			if (attacker instanceof L2PlayableInstance/* || attacker instanceof L2SiegeGuardInstance */)
			{
				if (getCurrentCp() >= value)
				{
					setCurrentCp(getCurrentCp() - value); // Set Cp to diff of Cp vs value
					value = 0; // No need to subtract anything from Hp
				}
				else
				{
					value -= getCurrentCp(); // Get diff from value vs Cp; will apply diff to Hp
					setCurrentCp(0); // Set Cp to 0
				}
			}
			
			summon = null;
		}
		
		super.reduceHp(value, attacker, awake);
		
		if (!getActiveChar().isDead() && getActiveChar().isSitting())
		{
			if (getActiveChar().getPrivateStoreType() != 0)
			{
				getActiveChar().setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
				getActiveChar().broadcastUserInfo();
			}
			
			getActiveChar().standUp();
		}
		
		if (getActiveChar().isFakeDeath())
		{
			getActiveChar().stopFakeDeath(null);
		}
		
		if (attacker != null && attacker != getActiveChar() && fullValue > 0)
		{
			// Send a System Message to the L2PcInstance
			SystemMessage smsg = new SystemMessage(SystemMessageId.S1_HIT_YOU_S2_DMG);
			
			if (Config.DEBUG)
			{
				LOGGER.debug("Attacker:" + attacker.getName());
			}
			
			if (attacker instanceof L2NpcInstance)
			{
				final int mobId = ((L2NpcInstance) attacker).getTemplate().idTemplate;
				
				if (Config.DEBUG)
				{
					LOGGER.debug("mob id:" + mobId);
				}
				
				smsg.addNpcName(mobId);
			}
			else if (attacker instanceof L2Summon)
			{
				final int mobId = ((L2Summon) attacker).getTemplate().idTemplate;
				
				smsg.addNpcName(mobId);
			}
			else
			{
				smsg.addString(attacker.getName());
			}
			
			smsg.addNumber(fullValue);
			getActiveChar().sendPacket(smsg);
			smsg = null;
		}
	}
	
	@Override
	public L2PcInstance getActiveChar()
	{
		return (L2PcInstance) super.getActiveChar();
	}
}
