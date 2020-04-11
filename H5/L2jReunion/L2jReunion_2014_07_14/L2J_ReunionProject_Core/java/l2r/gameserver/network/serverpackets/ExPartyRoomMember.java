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
package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.PartyMatchRoom;
import l2r.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Gnacik
 */
public class ExPartyRoomMember extends L2GameServerPacket
{
	private final PartyMatchRoom _room;
	private final int _mode;
	
	public ExPartyRoomMember(L2PcInstance player, PartyMatchRoom room, int mode)
	{
		_room = room;
		_mode = mode;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x08);
		writeD(_mode);
		writeD(_room.getMembers());
		for (L2PcInstance member : _room.getPartyMembers())
		{
			writeD(member.getObjectId());
			writeS(member.getName());
			writeD(member.getActiveClass());
			writeD(member.getLevel());
			writeD(0x00); // TODO: Closes town
			if (_room.getOwner().equals(member))
			{
				writeD(0x01);
			}
			else
			{
				if ((_room.getOwner().isInParty() && member.isInParty()) && (_room.getOwner().getParty().getLeaderObjectId() == member.getParty().getLeaderObjectId()))
				{
					writeD(0x02);
				}
				else
				{
					writeD(0x00);
				}
			}
			writeD(0x00); // TODO: Instance datas there is more if that is not 0!
		}
	}
}