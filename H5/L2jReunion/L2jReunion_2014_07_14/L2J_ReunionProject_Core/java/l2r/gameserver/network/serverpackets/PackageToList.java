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

import java.util.Map;
import java.util.Map.Entry;

/**
 * @author -Wooden-
 * @author UnAfraid, mrTJO
 */
public class PackageToList extends L2GameServerPacket
{
	private final Map<Integer, String> _players;
	
	public PackageToList(Map<Integer, String> chars)
	{
		_players = chars;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xC8);
		writeD(_players.size());
		for (Entry<Integer, String> entry : _players.entrySet())
		{
			writeD(entry.getKey());
			writeS(entry.getValue());
		}
	}
}
