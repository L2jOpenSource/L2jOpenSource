/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.model.actor.status;

import com.l2jserver.gameserver.enums.DuelState;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

public class NpcStatus extends CharStatus
{
	public NpcStatus(L2Npc activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public void reduceHp(double value, L2Character attacker)
	{
		reduceHp(value, attacker, true, false, false);
	}
	
	@Override
	public void reduceHp(double value, L2Character attacker, boolean awake, boolean isDOT, boolean isHpConsumption)
	{
		if (getActiveChar().isDead())
		{
			return;
		}
		
		if (attacker != null)
		{
			final L2PcInstance attackerPlayer = attacker.getActingPlayer();
			if ((attackerPlayer != null) && attackerPlayer.isInDuel())
			{
				attackerPlayer.setDuelState(DuelState.INTERRUPTED);
			}
			
			// Add attackers to npc's attacker list
			getActiveChar().addAttackerToAttackByList(attacker);
		}
		
		super.reduceHp(value, attacker, awake, isDOT, isHpConsumption);
	}
	
	@Override
	public L2Npc getActiveChar()
	{
		return (L2Npc) super.getActiveChar();
	}
}
