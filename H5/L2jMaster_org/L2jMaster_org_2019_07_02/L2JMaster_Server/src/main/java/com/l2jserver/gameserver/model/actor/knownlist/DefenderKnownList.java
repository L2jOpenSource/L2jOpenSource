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
package com.l2jserver.gameserver.model.actor.knownlist;

import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.instancemanager.TerritoryWarManager;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.instance.L2DefenderInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.entity.Castle;
import com.l2jserver.gameserver.model.entity.Fort;
import com.l2jserver.gameserver.model.entity.clanhall.SiegableHall;

public class DefenderKnownList extends AttackableKnownList
{
	public DefenderKnownList(L2DefenderInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public boolean addKnownObject(L2Object object)
	{
		if (!super.addKnownObject(object))
		{
			return false;
		}
		
		Castle castle = getActiveChar().getCastle();
		Fort fortress = getActiveChar().getFort();
		SiegableHall hall = getActiveChar().getConquerableHall();
		// Check if siege is in progress
		if (((fortress != null) && fortress.getZone().isActive()) || ((castle != null) && castle.getZone().isActive()) || ((hall != null) && hall.getSiegeZone().isActive()))
		{
			L2PcInstance player = null;
			if (object.isPlayable())
			{
				player = object.getActingPlayer();
			}
			int activeSiegeId = (fortress != null ? fortress.getResidenceId() : (castle != null ? castle.getResidenceId() : hall != null ? hall.getId() : 0));
			
			// Check if player is an enemy of this defender npc
			if ((player != null) && (((player.getSiegeState() == 2) && !player.isRegisteredOnThisSiegeField(activeSiegeId)) || ((player.getSiegeState() == 1) && !TerritoryWarManager.getInstance().isAllyField(player, activeSiegeId)) || (player.getSiegeState() == 0)))
			{
				if (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
				{
					getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
				}
			}
		}
		return true;
	}
	
	@Override
	public final L2DefenderInstance getActiveChar()
	{
		return (L2DefenderInstance) super.getActiveChar();
	}
}
