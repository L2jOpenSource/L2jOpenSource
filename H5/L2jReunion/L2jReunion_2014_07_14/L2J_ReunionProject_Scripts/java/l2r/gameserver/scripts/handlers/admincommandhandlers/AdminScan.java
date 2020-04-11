/*
 * Copyright (C) 2004-2013 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.scripts.handlers.admincommandhandlers;

import java.util.Collection;
import java.util.StringTokenizer;

import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.datatables.SpawnTable;
import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.instancemanager.RaidBossSpawnManager;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.util.Util;

/**
 * @author UnAfraid
 */
public class AdminScan implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_scan",
		"admin_deleteNpcByObjectId"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		
		if (command.startsWith("admin_scan"))
		{
			int radius = 500;
			if (st.hasMoreTokens())
			{
				String obj = st.nextToken();
				if (Util.isDigit(obj))
				{
					radius = Integer.valueOf(obj);
				}
			}
			
			String htm = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/admin/scan.htm");
			StringBuilder sb = new StringBuilder();
			Collection<L2Character> npc = activeChar.getKnownList().getKnownCharactersInRadius(radius);
			for (L2Character character : npc)
			{
				if (character instanceof L2Npc)
				{
					sb.append("<tr>");
					sb.append("<td width=\"54\">" + ((L2Npc) character).getId() + "</td>");
					sb.append("<td width=\"54\">" + character.getName() + "</td>");
					sb.append("<td width=\"54\">" + Math.round(activeChar.calculateDistance(character, false, false)) + "</td>");
					sb.append("<td width=\"54\"><a action=\"bypass -h admin_deleteNpcByObjectId " + character.getObjectId() + "\"><font color=\"LEVEL\">Delete</font></a></td>");
					sb.append("<td width=\"54\"><a action=\"bypass -h admin_move_to " + character.getX() + " " + character.getY() + " " + character.getZ() + "\"><font color=\"LEVEL\">Go to</font></a></td>");
					sb.append("</tr>");
				}
			}
			
			htm = htm.replaceAll("%data%", sb.toString());
			activeChar.sendPacket(new NpcHtmlMessage(0, htm));
		}
		else if (command.startsWith("admin_deleteNpcByObjectId") && st.hasMoreTokens())
		{
			String objectId = st.nextToken();
			if (Util.isDigit(objectId))
			{
				Collection<L2Character> npc = activeChar.getKnownList().getKnownCharacters();
				for (L2Character character : npc)
				{
					if ((character instanceof L2Npc) && (character.getObjectId() == Integer.valueOf(objectId)))
					{
						character.deleteMe();
						L2Spawn spawn = ((L2Npc) character).getSpawn();
						if (spawn != null)
						{
							spawn.stopRespawn();
							
							if (RaidBossSpawnManager.getInstance().isDefined(spawn.getId()))
							{
								RaidBossSpawnManager.getInstance().deleteSpawn(spawn, true);
							}
							else
							{
								SpawnTable.getInstance().deleteSpawn(spawn, true);
							}
						}
						activeChar.sendMessage(character.getName() + " have been deleted.");
						this.useAdminCommand("admin_scan", activeChar);
					}
				}
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
