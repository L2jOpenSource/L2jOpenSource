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

import l2r.gameserver.model.actor.instance.L2BoatInstance;

/**
 * @author Maktakien
 */
public class VehicleDeparture extends L2GameServerPacket
{
	private final int _objId, _x, _y, _z, _moveSpeed, _rotationSpeed;
	
	/**
	 * @param boat
	 */
	public VehicleDeparture(L2BoatInstance boat)
	{
		_objId = boat.getObjectId();
		_x = boat.getXdestination();
		_y = boat.getYdestination();
		_z = boat.getZdestination();
		_moveSpeed = (int) boat.getStat().getMoveSpeed();
		_rotationSpeed = boat.getStat().getRotationSpeed();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x6C);
		writeD(_objId);
		writeD(_moveSpeed);
		writeD(_rotationSpeed);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		
	}
}
