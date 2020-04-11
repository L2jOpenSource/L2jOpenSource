/*
 * Copyright (C) 2004-2015 L2J Server
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

import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.interfaces.ILocational;

public final class TeleportToLocation extends L2GameServerPacket
{
	private final int _targetObjId;
	private final ILocational _loc;
	
	public TeleportToLocation(L2Object cha, ILocational loc)
	{
		_targetObjId = cha.getObjectId();
		_loc = loc;
	}
	
	public TeleportToLocation(L2Object cha, int x, int y, int z)
	{
		_targetObjId = cha.getObjectId();
		_loc = new Location(x, y, z, cha.getHeading());
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x22);
		writeD(_targetObjId);
		writeD(_loc.getX());
		writeD(_loc.getY());
		writeD(_loc.getZ() + 16);
		writeD(0x00); // IsValidation
		writeD(_loc.getHeading());
	}
}
