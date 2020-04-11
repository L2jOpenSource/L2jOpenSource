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
 * Mode:
 * <ul>
 * <li>0 - add</li>
 * <li>1 - modify</li>
 * <li>2 - quit</li>
 * </ul>
 * @author Gnacik
 */
public class ExManagePartyRoomMember extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final PartyMatchRoom _room;
	private final int _mode;
	
	public ExManagePartyRoomMember(L2PcInstance player, PartyMatchRoom room, int mode)
	{
		_activeChar = player;
		_room = room;
		_mode = mode;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x0A);
		writeD(_mode);
		writeD(_activeChar.getObjectId());
		writeS(_activeChar.getName());
		writeD(_activeChar.getActiveClass());
		writeD(_activeChar.getLevel());
		writeD(0x00); // TODO: Closes town
		if (_room.getOwner().equals(_activeChar))
		{
			writeD(1);
		}
		else
		{
			if ((_room.getOwner().isInParty() && _activeChar.isInParty()) && (_room.getOwner().getParty().getLeaderObjectId() == _activeChar.getParty().getLeaderObjectId()))
			{
				writeD(0x02);
			}
			else
			{
				writeD(0x00);
			}
		}
	}
}