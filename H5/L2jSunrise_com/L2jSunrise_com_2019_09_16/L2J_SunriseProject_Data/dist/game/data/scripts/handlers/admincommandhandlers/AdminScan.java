/*
 * Copyright (C) 2004-2016 L2J DataPack
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
package handlers.admincommandhandlers;

import java.util.Collection;
import java.util.List;

import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.data.SpawnTable;
import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.instancemanager.RaidBossSpawnManager;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author UnAfraid, vGodFather
 */
public class AdminScan implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_scan",
		"admin_scan_count",
		"admin_deleteNpcByObjectId"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_scan_count"))
		{
			activeChar.sendMessage("Known objects in 1000 radius: " + activeChar.getKnownList().getKnownCharactersInRadius(1000).size());
		}
		else if (command.startsWith("admin_scan"))
		{
			String htm = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/admin/scan.htm");
			StringBuilder sb = new StringBuilder();
			List<L2Character> npc = activeChar.getKnownList().getKnownCharactersInRadius(1000);
			
			final int size = npc.size();
			int page = 1;
			try
			{
				page = Integer.valueOf(command.split(" ")[1]);
			}
			catch (Exception e)
			{
				// nothing to log
			}
			
			int maxItemsPerPage = 14;
			int startIndex = (page - 1) * maxItemsPerPage;
			startIndex = ((startIndex > (size - 1)) ? (size - 1) : startIndex);
			int endIndex = startIndex + maxItemsPerPage;
			endIndex = ((endIndex > (size - 1)) ? (size - 1) : endIndex);
			int filled = 0;
			
			if (size > 0)
			{
				for (int index = startIndex; index <= endIndex; ++index)
				{
					L2Character character = npc.get(index);
					if (character.isNpc())
					{
						sb.append("<tr>");
						sb.append("<td width=\"20\">" + character.getId() + "</td>");
						sb.append("<td width=\"150\">" + getName(character.getName()) + "</td>");
						sb.append("<td width=\"20\">" + Math.round(activeChar.calculateDistance(character, false, false)) + "</td>");
						sb.append("<td width=\"25\"><a action=\"bypass -h admin_deleteNpcByObjectId " + character.getObjectId() + " " + page + "\"><font color=\"LEVEL\">Delete</font></a></td>");
						sb.append("<td width=\"20\"><a action=\"bypass -h admin_move_to " + character.getX() + " " + character.getY() + " " + character.getZ() + "\"><font color=\"LEVEL\">Go to</font></a></td>");
						sb.append("</tr>");
						filled++;
					}
				}
			}
			
			while (filled < maxItemsPerPage)
			{
				sb.append("<tr>");
				sb.append("<td width=\"20\">-----</td>");
				sb.append("<td width=\"150\">-----</td>");
				sb.append("<td width=\"20\">-----</td>");
				sb.append("<td width=\"25\">-----</td>");
				sb.append("<td width=\"20\">-----</td>");
				sb.append("</tr>");
				filled++;
			}
			
			htm = htm.replaceAll("%data%", sb.toString());
			htm = htm.replace("%prev%", startIndex > 0 ? "<button width=80 height=20 value=\"PrevPage\" action=\"bypass -h admin_scan " + (page - 1) + "\" back=\"\" fore=\"L2UI_CT1.ListCTRL_DF_Title\">" : "");
			htm = htm.replace("%next%", endIndex < (size - 1) ? "<button width=80 height=20 value=\"NextPage\" action=\"bypass -h admin_scan " + (page + 1) + "\" back=\"\" fore=\"L2UI_CT1.ListCTRL_DF_Title\">" : "");
			htm = htm.replace("%curPage%", String.valueOf(page));
			
			activeChar.sendPacket(new NpcHtmlMessage(0, htm));
		}
		else if (command.startsWith("admin_deleteNpcByObjectId"))
		{
			int objectId = Integer.parseInt(command.split(" ")[1]);
			int page = Integer.parseInt(command.split(" ")[2]);
			Collection<L2Character> npc = activeChar.getKnownList().getKnownCharacters();
			for (L2Character character : npc)
			{
				if (character.isNpc() && (character.getObjectId() == objectId))
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
					useAdminCommand("admin_scan " + page, activeChar);
					break;
				}
			}
		}
		return true;
	}
	
	private String getName(String name)
	{
		if (name.length() > 15)
		{
			return name.substring(0, 14) + "...";
		}
		return name;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
