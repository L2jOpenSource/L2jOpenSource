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

/**
 * @author janiii
 */
public class ExEventMatchMessage extends L2GameServerPacket
{
	private final int _type;
	private final String _message;
	
	/**
	 * Create an event match message.
	 * @param type 0 - gm, 1 - finish, 2 - start, 3 - game over, 4 - 1, 5 - 2, 6 - 3, 7 - 4, 8 - 5
	 * @param message message to show, only when type is 0 - gm
	 */
	public ExEventMatchMessage(int type, String message)
	{
		_type = type;
		_message = message;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x0F);
		writeC(_type);
		writeS(_message);
	}
}
