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
public class PartyMatchDetail extends L2GameServerPacket
{
	private final PartyMatchRoom _room;
	
	/**
	 * @param player
	 * @param room
	 */
	public PartyMatchDetail(L2PcInstance player, PartyMatchRoom room)
	{
		_room = room;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x9d);
		writeD(_room.getId()); // Room ID
		writeD(_room.getMaxMembers()); // Max Members
		writeD(_room.getMinLvl()); // Level Min
		writeD(_room.getMaxLvl()); // Level Max
		writeD(_room.getLootType()); // Loot Type
		writeD(_room.getLocation()); // Room Location
		writeS(_room.getTitle()); // Room title
		writeH(0x00); // Unknown
	}
}
