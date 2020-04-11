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

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Duel;
import l2r.gameserver.model.stats.Stats;
import l2r.gameserver.util.Util;

public class SummonStatus extends PlayableStatus
{
	public SummonStatus(L2Summon activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public void reduceHp(double value, L2Character attacker)
	{
		reduceHp(value, attacker, true, false, false);
	}
	
	@Override
	public void reduceHp(double value, L2Character attacker, boolean awake, boolean isDOT, boolean isHPConsumption)
	{
		if ((attacker == null) || getActiveChar().isDead())
		{
			return;
		}
		
		final L2PcInstance attackerPlayer = attacker.getActingPlayer();
		if ((attackerPlayer != null) && ((getActiveChar().getOwner() == null) || (getActiveChar().getOwner().getDuelId() != attackerPlayer.getDuelId())))
		{
			attackerPlayer.setDuelState(Duel.DUELSTATE_INTERRUPTED);
		}
		
		final L2PcInstance caster = getActiveChar().getTransferingDamageTo();
		if (getActiveChar().getOwner().getParty() != null)
		{
			if ((caster != null) && Util.checkIfInRange(1000, getActiveChar(), caster, true) && !caster.isDead() && getActiveChar().getParty().getMembers().contains(caster))
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
					}
				}
			}
		}
		else if ((caster != null) && (caster == getActiveChar().getOwner()) && Util.checkIfInRange(1000, getActiveChar(), caster, true) && !caster.isDead()) // when no party, transfer only to owner (caster)
		{
			int transferDmg = 0;
			
			transferDmg = ((int) value * (int) getActiveChar().getStat().calcStat(Stats.TRANSFER_DAMAGE_TO_PLAYER, 0, null, null)) / 100;
			transferDmg = Math.min((int) caster.getCurrentHp() - 1, transferDmg);
			if (transferDmg > 0)
			{
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
				
				caster.reduceCurrentHp(transferDmg, attacker, null);
				value -= transferDmg;
			}
		}
		super.reduceHp(value, attacker, awake, isDOT, isHPConsumption);
	}
	
	@Override
	public L2Summon getActiveChar()
	{
		return (L2Summon) super.getActiveChar();
	}
}
