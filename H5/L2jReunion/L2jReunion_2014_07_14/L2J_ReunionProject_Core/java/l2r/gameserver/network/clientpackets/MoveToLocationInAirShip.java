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
import l2r.gameserver.model.actor.instance.L2AirShipInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.type.WeaponType;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.ExMoveToLocationInAirShip;
import l2r.gameserver.network.serverpackets.StopMoveInVehicle;

/**
 * format: ddddddd X:%d Y:%d Z:%d OriginX:%d OriginY:%d OriginZ:%d
 * @author GodKratos
 */
public class MoveToLocationInAirShip extends L2GameClientPacket
{
	private static final String _C__D0_20_MOVETOLOCATIONINAIRSHIP = "[C] D0:20 MoveToLocationInAirShip";
	
	private int _shipId;
	private int _targetX;
	private int _targetY;
	private int _targetZ;
	private int _originX;
	private int _originY;
	private int _originZ;
	
	@Override
	protected void readImpl()
	{
		_shipId = readD();
		_targetX = readD();
		_targetY = readD();
		_targetZ = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if ((_targetX == _originX) && (_targetY == _originY) && (_targetZ == _originZ))
		{
			activeChar.sendPacket(new StopMoveInVehicle(activeChar, _shipId));
			return;
		}
		
		if (activeChar.isAttackingNow() && (activeChar.getActiveWeaponItem() != null) && (activeChar.getActiveWeaponItem().getItemType() == WeaponType.BOW))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.isSitting() || activeChar.isMovementDisabled())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!activeChar.isInAirShip())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2AirShipInstance airShip = activeChar.getAirShip();
		if (airShip.getObjectId() != _shipId)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		activeChar.setInVehiclePosition(new Location(_targetX, _targetY, _targetZ));
		activeChar.broadcastPacket(new ExMoveToLocationInAirShip(activeChar));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_20_MOVETOLOCATIONINAIRSHIP;
	}
}
