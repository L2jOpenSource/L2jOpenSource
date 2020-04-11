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

import l2r.gameserver.model.actor.instance.L2PcInstance;

/**
 * update 27.8.10
 * @author kerberos JIV
 */
public class ExValidateLocationInAirShip extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final int shipId, x, y, z, h;
	
	public ExValidateLocationInAirShip(L2PcInstance player)
	{
		_activeChar = player;
		shipId = _activeChar.getAirShip().getObjectId();
		x = player.getInVehiclePosition().getX();
		y = player.getInVehiclePosition().getY();
		z = player.getInVehiclePosition().getZ();
		h = player.getHeading();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0x6F);
		writeD(_activeChar.getObjectId());
		writeD(shipId);
		writeD(x);
		writeD(y);
		writeD(z);
		writeD(h);
	}
}
