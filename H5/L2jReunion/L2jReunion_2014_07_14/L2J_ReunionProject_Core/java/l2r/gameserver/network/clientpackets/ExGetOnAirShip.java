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
package l2r.gameserver.network.clientpackets;

/**
 * Format: (c) dddd d: dx d: dy d: dz d: AirShip id ??
 * @author -Wooden-
 */
public class ExGetOnAirShip extends L2GameClientPacket
{
	private static final String _C__D0_36_EXGETONAIRSHIP = "[C] D0:36 ExGetOnAirShip";
	
	private int _x;
	private int _y;
	private int _z;
	private int _shipId;
	
	@Override
	protected void readImpl()
	{
		_x = readD();
		_y = readD();
		_z = readD();
		_shipId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		_log.info("[T1:ExGetOnAirShip] x: " + _x);
		_log.info("[T1:ExGetOnAirShip] y: " + _y);
		_log.info("[T1:ExGetOnAirShip] z: " + _z);
		_log.info("[T1:ExGetOnAirShip] ship ID: " + _shipId);
	}
	
	@Override
	public String getType()
	{
		return _C__D0_36_EXGETONAIRSHIP;
	}
}
