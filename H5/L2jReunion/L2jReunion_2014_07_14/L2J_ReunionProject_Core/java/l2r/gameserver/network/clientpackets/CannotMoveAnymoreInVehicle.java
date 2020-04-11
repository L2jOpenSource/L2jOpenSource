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

import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.StopMoveInVehicle;

/**
 * @author Maktakien
 */
public final class CannotMoveAnymoreInVehicle extends L2GameClientPacket
{
	private static final String _C__76_CANNOTMOVEANYMOREINVEHICLE = "[C] 76 CannotMoveAnymoreInVehicle";
	
	private int _x;
	private int _y;
	private int _z;
	private int _heading;
	private int _boatId;
	
	@Override
	protected void readImpl()
	{
		_boatId = readD();
		_x = readD();
		_y = readD();
		_z = readD();
		_heading = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		if (player.isInBoat())
		{
			if (player.getBoat().getObjectId() == _boatId)
			{
				player.setInVehiclePosition(new Location(_x, _y, _z));
				player.setHeading(_heading);
				StopMoveInVehicle msg = new StopMoveInVehicle(player, _boatId);
				player.broadcastPacket(msg);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__76_CANNOTMOVEANYMOREINVEHICLE;
	}
}
