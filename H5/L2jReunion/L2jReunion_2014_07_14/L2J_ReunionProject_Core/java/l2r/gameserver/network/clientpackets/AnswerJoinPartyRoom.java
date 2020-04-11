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
import l2r.gameserver.network.serverpackets.ExManagePartyRoomMember;
import l2r.gameserver.network.serverpackets.ExPartyRoomMember;
import l2r.gameserver.network.serverpackets.PartyMatchDetail;
import l2r.gameserver.network.serverpackets.SystemMessage;

/**
 * Format: (ch) d
 * @author -Wooden-, Tryskell
 */
public final class AnswerJoinPartyRoom extends L2GameClientPacket
{
	private static final String _C__D0_30_ANSWERJOINPARTYROOM = "[C] D0:30 AnswerJoinPartyRoom";
	private int _answer; // 1 or 0
	
	@Override
	protected void readImpl()
	{
		_answer = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getActiveChar();
		if (player == null)
		{
			return;
		}
		
		L2PcInstance partner = player.getActiveRequester();
		if (partner == null)
		{
			// Partner hasn't been found, cancel the invitation
			player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			player.setActiveRequester(null);
			return;
		}
		else if (L2World.getInstance().getPlayer(partner.getObjectId()) == null)
		{
			// Partner hasn't been found, cancel the invitation
			player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			player.setActiveRequester(null);
			return;
		}
		
		// If answer is positive, join the requester's PartyRoom.
		if ((_answer == 1) && !partner.isRequestExpired())
		{
			PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(partner.getPartyRoom());
			if (room == null)
			{
				return;
			}
			
			if ((player.getLevel() >= room.getMinLvl()) && (player.getLevel() <= room.getMaxLvl()))
			{
				// Remove from waiting list
				PartyMatchWaitingList.getInstance().removePlayer(player);
				
				player.setPartyRoom(partner.getPartyRoom());
				
				player.sendPacket(new PartyMatchDetail(player, room));
				player.sendPacket(new ExPartyRoomMember(player, room, 0));
				
				for (L2PcInstance member : room.getPartyMembers())
				{
					if (member == null)
					{
						continue;
					}
					
					member.sendPacket(new ExManagePartyRoomMember(player, room, 0));
					member.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_ENTERED_PARTY_ROOM).addPcName(player));
				}
				room.addMember(player);
				
				// Info Broadcast
				player.broadcastUserInfo();
			}
			else
			{
				player.sendPacket(SystemMessageId.CANT_ENTER_PARTY_ROOM);
			}
		}
		else
		{
			partner.sendPacket(SystemMessageId.PARTY_MATCHING_REQUEST_NO_RESPONSE);
		}
		
		// reset transaction timers
		player.setActiveRequester(null);
		partner.onTransactionResponse();
	}
	
	@Override
	public String getType()
	{
		return _C__D0_30_ANSWERJOINPARTYROOM;
	}
}