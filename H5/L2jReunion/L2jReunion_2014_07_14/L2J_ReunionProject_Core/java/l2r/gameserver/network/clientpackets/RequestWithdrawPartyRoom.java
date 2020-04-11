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

import l2r.gameserver.model.PartyMatchRoom;
import l2r.gameserver.model.PartyMatchRoomList;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExClosePartyRoom;

/**
 * @author Gnacik
 */
public final class RequestWithdrawPartyRoom extends L2GameClientPacket
{
	private static final String _C__D0_0B_REQUESTWITHDRAWPARTYROOM = "[C] D0:0B RequestWithdrawPartyRoom";
	
	private int _roomid;
	@SuppressWarnings("unused")
	private int _unk1;
	
	@Override
	protected void readImpl()
	{
		_roomid = readD();
		_unk1 = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance _activeChar = getClient().getActiveChar();
		
		if (_activeChar == null)
		{
			return;
		}
		
		PartyMatchRoom _room = PartyMatchRoomList.getInstance().getRoom(_roomid);
		if (_room == null)
		{
			return;
		}
		
		if ((_activeChar.isInParty() && _room.getOwner().isInParty()) && (_activeChar.getParty().getLeaderObjectId() == _room.getOwner().getParty().getLeaderObjectId()))
		{
			// If user is in party with Room Owner
			// is not removed from Room
			
			// _activeChar.setPartyMatching(0);
			_activeChar.broadcastUserInfo();
		}
		else
		{
			_room.deleteMember(_activeChar);
			
			_activeChar.setPartyRoom(0);
			// _activeChar.setPartyMatching(0);
			
			_activeChar.sendPacket(new ExClosePartyRoom());
			_activeChar.sendPacket(SystemMessageId.PARTY_ROOM_EXITED);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_0B_REQUESTWITHDRAWPARTYROOM;
	}
}
