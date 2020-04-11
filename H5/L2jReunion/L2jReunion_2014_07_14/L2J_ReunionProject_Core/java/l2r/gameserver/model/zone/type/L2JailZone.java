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
package l2r.gameserver.model.zone.type;

import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.tasks.player.TeleportTask;
import l2r.gameserver.model.zone.L2ZoneType;
import l2r.gameserver.network.SystemMessageId;

/**
 * A jail zone
 * @author durgus
 */
public class L2JailZone extends L2ZoneType
{
	protected static final Location JAIL_IN_LOC = new Location(-114356, -249645, -2984);
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
			character.setInsideZone(ZoneIdType.JAIL, true);
			character.setInsideZone(ZoneIdType.NO_SUMMON_FRIEND, true);
			if (Config.JAIL_IS_PVP)
			{
				character.setInsideZone(ZoneIdType.PVP, true);
				character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
			}
			if (Config.JAIL_DISABLE_TRANSACTION)
			{
				character.setInsideZone(ZoneIdType.NO_STORE, true);
			}
		}
	}
	
	@Override
	protected void onExit(final L2Character character)
	{
		if (character.isPlayer())
		{
			final L2PcInstance player = character.getActingPlayer();
			player.setInsideZone(ZoneIdType.JAIL, false);
			player.setInsideZone(ZoneIdType.NO_SUMMON_FRIEND, false);
			
			if (Config.JAIL_IS_PVP)
			{
				character.setInsideZone(ZoneIdType.PVP, false);
				character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
			}
			
			if (player.isJailed())
			{
				// when a player wants to exit jail even if he is still jailed, teleport him back to jail
				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
				{
					@Override
					public void run()
					{
						if (!character.isInsideZone(ZoneIdType.JAIL))
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(player, JAIL_IN_LOC), 2000);
							character.sendMessage("You cannot cheat your way out of here. You must wait until your jail time is over.");
						}
					}
				}, 4000);
			}
			if (Config.JAIL_DISABLE_TRANSACTION)
			{
				character.setInsideZone(ZoneIdType.NO_STORE, false);
			}
		}
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
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
