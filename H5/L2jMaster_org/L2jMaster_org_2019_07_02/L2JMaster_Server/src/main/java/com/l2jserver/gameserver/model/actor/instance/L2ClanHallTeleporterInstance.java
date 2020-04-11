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

import com.l2jserver.gameserver.data.sql.impl.ClanTable;
import com.l2jserver.gameserver.enums.InstanceType;
import com.l2jserver.gameserver.instancemanager.ClanHallManager;
import com.l2jserver.gameserver.instancemanager.MapRegionManager;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.model.entity.ClanHall;
import com.l2jserver.gameserver.network.serverpackets.ActionFailed;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

public class L2ClanHallTeleporterInstance extends L2DoormenInstance
{
	private boolean _init = false;
	private ClanHall _clanHall = null;
	
	public L2ClanHallTeleporterInstance(int objectID, L2NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.L2ClanHallDoormenInstance);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		
		if (getClanHall() != null)
		{
			L2Clan owner = ClanTable.getInstance().getClan(getClanHall().getOwnerId());
			if (isOwnerClan(player))
			{
				html.setFile(player.getHtmlPrefix(), "data/html/clanHallDoormen/doormen-tele.htm");
				html.replace("%clanname%", owner.getName());
			}
			else
			{
				if ((owner != null) && (owner.getLeader() != null))
				{
					html.setFile(player.getHtmlPrefix(), "data/html/clanHallDoormen/doormen-no.htm");
					html.replace("%leadername%", owner.getLeaderName());
					html.replace("%clanname%", owner.getName());
				}
				else
				{
					html.setFile(player.getHtmlPrefix(), "data/html/clanHallDoormen/emptyowner.htm");
					html.replace("%hallname%", getClanHall().getName());
				}
			}
		}
		else
		{
			return;
		}
		
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	@Override
	protected final void openDoors(L2PcInstance player, String command)
	{
		Location _loc = getClanHall().getZone().getSpawnLoc();
		
		if (_loc != null)
		{
			player.teleToLocation(_loc, false);
			if (player.getSummon() != null)
			{
				player.getSummon().teleToLocation(_loc, false);
			}
		}
	}
	
	@Override
	protected final void closeDoors(L2PcInstance player, String command)
	{
		Location _loc = getClanHall().getZone().getChaoticSpawnLoc();
		if (_loc != null)
		{
			player.teleToLocation(_loc, false);
			if (player.getSummon() != null)
			{
				player.getSummon().teleToLocation(_loc, false);
			}
		}
		else
		{
			player.teleToLocation(MapRegionManager.TOWN);
			if (player.getSummon() != null)
			{
				player.getSummon().teleToLocation(MapRegionManager.TOWN);
			}
		}
	}
	
	private final ClanHall getClanHall()
	{
		if (!_init)
		{
			synchronized (this)
			{
				if (!_init)
				{
					_clanHall = ClanHallManager.getInstance().getNearbyClanHall(getX(), getY(), 500);
					_init = true;
				}
			}
		}
		return _clanHall;
	}
	
	@Override
	protected final boolean isOwnerClan(L2PcInstance player)
	{
		if ((player.getClan() != null) && (getClanHall() != null))
		{
			if (player.getClanId() == getClanHall().getOwnerId())
			{
				return true;
			}
		}
		return false;
	}
}