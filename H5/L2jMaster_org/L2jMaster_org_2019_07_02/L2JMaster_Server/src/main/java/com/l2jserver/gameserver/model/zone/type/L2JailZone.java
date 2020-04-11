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
package com.l2jserver.gameserver.model.zone.type;

import com.l2jserver.Config;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.tasks.player.TeleportTask;
import com.l2jserver.gameserver.model.zone.L2ZoneType;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.network.SystemMessageId;

/**
 * A jail zone
 * @author durgus
 */
public class L2JailZone extends L2ZoneType
{
	private static final Location JAIL_IN_LOC = new Location(-114356, -249645, -2984);
	private static final Location JAIL_OUT_LOC = new Location(17836, 170178, -3507);
	
	public L2JailZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (character.isPlayer())
		{
			character.setInsideZone(ZoneId.JAIL, true);
			character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
			if (Config.JAIL_IS_PVP)
			{
				character.setInsideZone(ZoneId.PVP, true);
				character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
			}
			if (Config.JAIL_DISABLE_TRANSACTION)
			{
				character.setInsideZone(ZoneId.NO_STORE, true);
			}
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (character.isPlayer())
		{
			final L2PcInstance player = character.getActingPlayer();
			player.setInsideZone(ZoneId.JAIL, false);
			player.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
			
			if (Config.JAIL_IS_PVP)
			{
				character.setInsideZone(ZoneId.PVP, false);
				character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
			}
			
			if (player.isJailed())
			{
				// when a player wants to exit jail even if he is still jailed, teleport him back to jail
				ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(player, JAIL_IN_LOC), 2000);
				character.sendMessage("You cannot cheat your way out of here. You must wait until your jail time is over.");
			}
			if (Config.JAIL_DISABLE_TRANSACTION)
			{
				character.setInsideZone(ZoneId.NO_STORE, false);
			}
		}
	}
	
	public static Location getLocationIn()
	{
		return JAIL_IN_LOC;
	}
	
	public static Location getLocationOut()
	{
		return JAIL_OUT_LOC;
	}
}
