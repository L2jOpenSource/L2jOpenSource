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

public final class KeyPacket extends L2GameServerPacket
{
	private final byte[] _key;
	private final int _id;
	
	public KeyPacket(byte[] key, int id)
	{
		_key = key;
		_id = id;
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0x2e);
		writeC(_id); // 0 - wrong protocol, 1 - protocol ok
		for (int i = 0; i < 8; i++)
		{
			writeC(_key[i]); // key
		}
		writeD(0x01);
		writeD(0x01); // server id
		writeC(0x01);
		writeD(0x00); // obfuscation key
	}
}
