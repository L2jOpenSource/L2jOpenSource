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

import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.GetOffVehicle;
import l2r.gameserver.network.serverpackets.StopMoveInVehicle;

/**
 * @author Maktakien
 */
public final class RequestGetOffVehicle extends L2GameClientPacket
{
	private static final String _C__54_GETOFFVEHICLE = "[S] 54 GetOffVehicle";
	
	private int _boatId, _x, _y, _z;
	
	@Override
	protected void readImpl()
	{
		_boatId = readD();
		_x = readD();
		_y = readD();
		_z = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		if (!activeChar.isInBoat() || (activeChar.getBoat().getObjectId() != _boatId) || activeChar.getBoat().isMoving() || !activeChar.isInsideRadius(_x, _y, _z, 1000, true, false))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		activeChar.broadcastPacket(new StopMoveInVehicle(activeChar, _boatId));
		activeChar.setVehicle(null);
		activeChar.setInVehiclePosition(null);
		sendPacket(ActionFailed.STATIC_PACKET);
		activeChar.broadcastPacket(new GetOffVehicle(activeChar.getObjectId(), _boatId, _x, _y, _z));
		activeChar.setXYZ(_x, _y, _z);
		activeChar.setInsideZone(ZoneIdType.PEACE, false);
		activeChar.revalidateZone(true);
	}
	
	@Override
	public String getType()
	{
		return _C__54_GETOFFVEHICLE;
	}
}
