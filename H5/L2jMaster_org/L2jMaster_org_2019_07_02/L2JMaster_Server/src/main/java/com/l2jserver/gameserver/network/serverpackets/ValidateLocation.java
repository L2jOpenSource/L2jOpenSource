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
package com.l2jserver.gameserver.network.serverpackets;

import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.Location;

public class ValidateLocation extends L2GameServerPacket
{
	private final int _charObjId;
	private final Location _loc;
	
	public ValidateLocation(L2Object obj)
	{
		_charObjId = obj.getObjectId();
		_loc = obj.getLocation();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x79);
		writeD(_charObjId);
		writeLoc(_loc);
		writeD(_loc.getHeading());
	}
}
