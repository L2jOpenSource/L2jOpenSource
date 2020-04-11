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
package com.l2jserver.gameserver.network.clientpackets;

import com.l2jserver.gameserver.model.L2Party;
import com.l2jserver.gameserver.model.PartyMatchRoom;
import com.l2jserver.gameserver.model.PartyMatchRoomList;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ExManagePartyRoomMember;
import com.l2jserver.gameserver.network.serverpackets.JoinParty;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

public final class RequestAnswerJoinParty extends L2GameClientPacket
{
	private static final String _C__43_REQUESTANSWERPARTY = "[C] 43 RequestAnswerJoinParty";
	
	private int _response;
	
	@Override
	protected void readImpl()
	{
		_response = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final L2PcInstance requestor = player.getActiveRequester();
		if (requestor == null)
		{
			return;
		}
		
		requestor.sendPacket(new JoinParty(_response));
		
		switch (_response)
		{
			case -1: // Party disable by player client config
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_SET_TO_REFUSE_PARTY_REQUEST);
				sm.addPcName(player);
				requestor.sendPacket(sm);
				break;
			}
			case 0: // Party cancel by player
			{
				
				// requestor.sendPacket(SystemMessageId.PLAYER_DECLINED); FIXME: Done in client?
				break;
			}
			case 1: // Party accept by player
			{
				if (requestor.isInParty())
				{
					if (requestor.getParty().getMemberCount() >= 9)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PARTY_FULL);
						player.sendPacket(sm);
						requestor.sendPacket(sm);
						return;
					}
					player.joinParty(requestor.getParty());
				}
				else
				{
					requestor.setParty(new L2Party(requestor, requestor.getPartyDistributionType()));
					player.joinParty(requestor.getParty());
				}
				
				if (requestor.isInPartyMatchRoom() && player.isInPartyMatchRoom())
				{
					final PartyMatchRoomList list = PartyMatchRoomList.getInstance();
					if ((list != null) && (list.getPlayerRoomId(requestor) == list.getPlayerRoomId(player)))
					{
						final PartyMatchRoom room = list.getPlayerRoom(requestor);
						if (room != null)
						{
							final ExManagePartyRoomMember packet = new ExManagePartyRoomMember(player, room, 1);
							for (L2PcInstance member : room.getPartyMembers())
							{
								if (member != null)
								{
									member.sendPacket(packet);
								}
							}
						}
					}
				}
				else if (requestor.isInPartyMatchRoom() && !player.isInPartyMatchRoom())
				{
					final PartyMatchRoomList list = PartyMatchRoomList.getInstance();
					if (list != null)
					{
						final PartyMatchRoom room = list.getPlayerRoom(requestor);
						if (room != null)
						{
							room.addMember(player);
							ExManagePartyRoomMember packet = new ExManagePartyRoomMember(player, room, 1);
							for (L2PcInstance member : room.getPartyMembers())
							{
								if (member != null)
								{
									member.sendPacket(packet);
								}
							}
							player.setPartyRoom(room.getId());
							// player.setPartyMatching(1);
							player.broadcastUserInfo();
						}
					}
				}
				break;
			}
		}
		
		if (requestor.isInParty())
		{
			requestor.getParty().setPendingInvitation(false); // if party is null, there is no need of decreasing
		}
		
		player.setActiveRequester(null);
		requestor.onTransactionResponse();
	}
	
	@Override
	public String getType()
	{
		return _C__43_REQUESTANSWERPARTY;
	}
}
