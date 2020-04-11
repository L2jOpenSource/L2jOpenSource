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

import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Maktakien
 */
public class MoveToLocationInVehicle extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _boatId;
	private final Location _destination;
	private final Location _origin;
	
	/**
	 * @param player
	 * @param destination
	 * @param origin
	 */
	public MoveToLocationInVehicle(L2PcInstance player, Location destination, Location origin)
	{
		_charObjId = player.getObjectId();
		_boatId = player.getBoat().getObjectId();
		_destination = destination;
		_origin = origin;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x7e);
		writeD(_charObjId);
		writeD(_boatId);
		writeD(_destination.getX());
		writeD(_destination.getY());
		writeD(_destination.getZ());
		writeD(_origin.getX());
		writeD(_origin.getY());
		writeD(_origin.getZ());
	}
}
