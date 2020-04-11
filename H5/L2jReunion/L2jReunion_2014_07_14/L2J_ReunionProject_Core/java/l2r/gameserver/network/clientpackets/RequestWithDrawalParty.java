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

import l2r.gameserver.enums.MessageType;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.PartyMatchRoom;
import l2r.gameserver.model.PartyMatchRoomList;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.ExClosePartyRoom;
import l2r.gameserver.network.serverpackets.ExPartyRoomMember;
import l2r.gameserver.network.serverpackets.PartyMatchDetail;

/**
 * This class ...
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestWithDrawalParty extends L2GameClientPacket
{
	private static final String _C__44_REQUESTWITHDRAWALPARTY = "[C] 44 RequestWithDrawalParty";
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		L2Party party = player.getParty();
		
		if (party != null)
		{
			if (party.isInDimensionalRift() && !party.getDimensionalRift().getRevivedAtWaitingRoom().contains(player))
			{
				player.sendMessage("You can't exit party when you are in Dimensional Rift.");
			}
			else
			{
				party.removePartyMember(player, MessageType.Left);
				
				if (player.isInPartyMatchRoom())
				{
					PartyMatchRoom _room = PartyMatchRoomList.getInstance().getPlayerRoom(player);
					if (_room != null)
					{
						player.sendPacket(new PartyMatchDetail(player, _room));
						player.sendPacket(new ExPartyRoomMember(player, _room, 0));
						player.sendPacket(new ExClosePartyRoom());
						
						_room.deleteMember(player);
					}
					player.setPartyRoom(0);
					// player.setPartyMatching(0);
					player.broadcastUserInfo();
				}
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__44_REQUESTWITHDRAWALPARTY;
	}
}
