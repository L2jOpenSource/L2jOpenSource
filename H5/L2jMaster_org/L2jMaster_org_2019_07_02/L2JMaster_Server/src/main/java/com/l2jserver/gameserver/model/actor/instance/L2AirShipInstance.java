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
package com.l2jserver.gameserver.model.actor.instance;

import com.l2jserver.gameserver.ai.L2AirShipAI;
import com.l2jserver.gameserver.enums.InstanceType;
import com.l2jserver.gameserver.instancemanager.AirShipManager;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Vehicle;
import com.l2jserver.gameserver.model.actor.templates.L2CharTemplate;
import com.l2jserver.gameserver.network.serverpackets.ExAirShipInfo;
import com.l2jserver.gameserver.network.serverpackets.ExGetOffAirShip;
import com.l2jserver.gameserver.network.serverpackets.ExGetOnAirShip;
import com.l2jserver.gameserver.network.serverpackets.ExMoveToLocationAirShip;
import com.l2jserver.gameserver.network.serverpackets.ExStopMoveAirShip;

/**
 * Flying airships. Very similar to Maktakien boats (see L2BoatInstance) but these do fly :P
 * @author DrHouse, DS
 */
public class L2AirShipInstance extends L2Vehicle
{
	public L2AirShipInstance(L2CharTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.L2AirShipInstance);
		setAI(new L2AirShipAI(this));
	}
	
	@Override
	public boolean isAirShip()
	{
		return true;
	}
	
	public boolean isOwner(L2PcInstance player)
	{
		return false;
	}
	
	public int getOwnerId()
	{
		return 0;
	}
	
	public boolean isCaptain(L2PcInstance player)
	{
		return false;
	}
	
	public int getCaptainId()
	{
		return 0;
	}
	
	public int getHelmObjectId()
	{
		return 0;
	}
	
	public int getHelmItemId()
	{
		return 0;
	}
	
	public boolean setCaptain(L2PcInstance player)
	{
		return false;
	}
	
	public int getFuel()
	{
		return 0;
	}
	
	public void setFuel(int f)
	{
		
	}
	
	public int getMaxFuel()
	{
		return 0;
	}
	
	public void setMaxFuel(int mf)
	{
		
	}
	
	@Override
	public int getId()
	{
		return 0;
	}
	
	@Override
	public boolean moveToNextRoutePoint()
	{
		final boolean result = super.moveToNextRoutePoint();
		if (result)
		{
			broadcastPacket(new ExMoveToLocationAirShip(this));
		}
		
		return result;
	}
	
	@Override
	public boolean addPassenger(L2PcInstance player)
	{
		if (!super.addPassenger(player))
		{
			return false;
		}
		
		player.setVehicle(this);
		player.setInVehiclePosition(new Location(0, 0, 0));
		player.broadcastPacket(new ExGetOnAirShip(player, this));
		player.getKnownList().removeAllKnownObjects();
		player.setXYZ(getX(), getY(), getZ());
		player.revalidateZone(true);
		return true;
	}
	
	@Override
	public void oustPlayer(L2PcInstance player)
	{
		super.oustPlayer(player);
		final Location loc = getOustLoc();
		if (player.isOnline())
		{
			player.broadcastPacket(new ExGetOffAirShip(player, this, loc.getX(), loc.getY(), loc.getZ()));
			player.getKnownList().removeAllKnownObjects();
			player.setXYZ(loc.getX(), loc.getY(), loc.getZ());
			player.revalidateZone(true);
		}
		else
		{
			player.setXYZInvisible(loc.getX(), loc.getY(), loc.getZ());
		}
	}
	
	@Override
	public boolean deleteMe()
	{
		if (!super.deleteMe())
		{
			return false;
		}
		
		AirShipManager.getInstance().removeAirShip(this);
		return true;
	}
	
	@Override
	public void stopMove(Location loc, boolean updateKnownObjects)
	{
		super.stopMove(loc, updateKnownObjects);
		
		broadcastPacket(new ExStopMoveAirShip(this));
	}
	
	@Override
	public void updateAbnormalEffect()
	{
		broadcastPacket(new ExAirShipInfo(this));
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		if (isVisibleFor(activeChar))
		{
			activeChar.sendPacket(new ExAirShipInfo(this));
		}
	}
}