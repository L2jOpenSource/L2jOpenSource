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

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.model.Location;
import l2r.gameserver.model.interfaces.ILocational;

/**
 * This packet shows the mouse click particle for 30 seconds on every location.
 * @author Nos
 */
public final class ExShowTrace extends L2GameServerPacket
{
	private final List<Location> _locations = new ArrayList<>();
	
	public void addLocation(int x, int y, int z)
	{
		_locations.add(new Location(x, y, z));
	}
	
	public void addLocation(ILocational loc)
	{
		addLocation(loc.getX(), loc.getY(), loc.getZ());
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x67);
		
		writeH(0); // type broken in H5
		writeD(0); // time broken in H5
		writeH(_locations.size());
		for (Location loc : _locations)
		{
			writeD(loc.getX());
			writeD(loc.getY());
			writeD(loc.getZ());
		}
	}
}
