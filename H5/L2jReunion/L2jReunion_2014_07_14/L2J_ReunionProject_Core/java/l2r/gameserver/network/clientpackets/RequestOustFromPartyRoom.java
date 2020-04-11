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
package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.L2World;
import l2r.gameserver.model.PartyMatchRoom;
import l2r.gameserver.model.PartyMatchRoomList;
import l2r.gameserver.model.PartyMatchWaitingList;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExClosePartyRoom;
import l2r.gameserver.network.serverpackets.ListPartyWating;

/**
 * format (ch) d
 * @author -Wooden-
 */
public final class RequestOustFromPartyRoom extends L2GameClientPacket
{
	private static final String _C__D0_09_REQUESTOUSTFROMPARTYROOM = "[C] D0:09 RequestOustFromPartyRoom";
	
	private int _charid;
	
	@Override
	protected void readImpl()
	{
		_charid = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getActiveChar();
		if (player == null)
		{
			return;
		}
		
		L2PcInstance member = L2World.getInstance().getPlayer(_charid);
		if (member == null)
		{
			return;
		}
		
		PartyMatchRoom room = PartyMatchRoomList.getInstance().getPlayerRoom(member);
		if ((room == null) || (room.getOwner() != player))
		{
			return;
		}
		
		if (player.isInParty() && member.isInParty() && (player.getParty().getLeaderObjectId() == member.getParty().getLeaderObjectId()))
		{
			player.sendPacket(SystemMessageId.CANNOT_DISMISS_PARTY_MEMBER);
		}
		else
		{
			// Remove member from party room
			room.deleteMember(member);
			member.setPartyRoom(0);
			
			// Close the PartyRoom window
			member.sendPacket(new ExClosePartyRoom());
			
			// Add player back on waiting list
			PartyMatchWaitingList.getInstance().addPlayer(member);
			
			// Send Room list
			int loc = 0; // TODO: Closes town
			member.sendPacket(new ListPartyWating(member, 0, loc, member.getLevel()));
			
			// Clean player's LFP title
			member.broadcastUserInfo();
			member.sendPacket(SystemMessageId.OUSTED_FROM_PARTY_ROOM);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_09_REQUESTOUSTFROMPARTYROOM;
	}
}
