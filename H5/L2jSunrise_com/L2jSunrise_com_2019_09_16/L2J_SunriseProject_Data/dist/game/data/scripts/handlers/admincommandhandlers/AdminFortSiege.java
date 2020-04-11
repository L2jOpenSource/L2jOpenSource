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
package handlers.admincommandhandlers;

import java.util.List;
import java.util.StringTokenizer;

import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.instancemanager.FortManager;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Fort;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.util.StringUtil;

/**
 * This class handles all siege commands: Todo: change the class name, and neaten it up
 */
public class AdminFortSiege implements IAdminCommandHandler
{
	// private static Logger _log = LoggerFactory.getLogger(AdminFortSiege.class);
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_fortsiege",
		"admin_add_fortattacker",
		"admin_list_fortsiege_clans",
		"admin_clear_fortsiege_list",
		"admin_spawn_fortdoors",
		"admin_endfortsiege",
		"admin_startfortsiege",
		"admin_setfort",
		"admin_removefort"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		command = st.nextToken(); // Get actual command
		
		// Get fort
		Fort fort = null;
		int fortId = 0;
		if (st.hasMoreTokens())
		{
			fortId = Integer.parseInt(st.nextToken());
			fort = FortManager.getInstance().getFortById(fortId);
		}
		// Get fort
		if (((fort == null) || (fortId == 0)))
		{
			// No fort specified
			showFortSelectPage(activeChar);
		}
		else
		{
			L2Object target = activeChar.getTarget();
			L2PcInstance player = null;
			if (target instanceof L2PcInstance)
			{
				player = (L2PcInstance) target;
			}
			
			if (command.equalsIgnoreCase("admin_add_fortattacker"))
			{
				if (player == null)
				{
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				}
				else
				{
					if (fort.getSiege().addAttacker(player, false) == 4)
					{
						final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.REGISTERED_TO_S1_FORTRESS_BATTLE);
						sm.addCastleId(fort.getResidenceId());
						player.sendPacket(sm);
					}
					else
					{
						player.sendMessage("During registering error occurred!");
					}
				}
			}
			else if (command.equalsIgnoreCase("admin_clear_fortsiege_list"))
			{
				fort.getSiege().clearSiegeClan();
			}
			else if (command.equalsIgnoreCase("admin_endfortsiege"))
			{
				fort.getSiege().endSiege();
			}
			else if (command.equalsIgnoreCase("admin_list_fortsiege_clans"))
			{
				activeChar.sendMessage("Not implemented yet.");
			}
			else if (command.equalsIgnoreCase("admin_setfort"))
			{
				if ((player == null) || (player.getClan() == null))
				{
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				}
				else
				{
					fort.setOwner(player.getClan(), false);
				}
			}
			else if (command.equalsIgnoreCase("admin_removefort"))
			{
				L2Clan clan = fort.getOwnerClan();
				if (clan != null)
				{
					fort.removeOwner(true);
				}
				else
				{
					activeChar.sendMessage("Unable to remove fort");
				}
			}
			else if (command.equalsIgnoreCase("admin_spawn_fortdoors"))
			{
				fort.resetDoors();
			}
			else if (command.equalsIgnoreCase("admin_startfortsiege"))
			{
				fort.getSiege().startSiege();
			}
			
			showFortSiegePage(activeChar, fort);
		}
		return true;
	}
	
	private void showFortSelectPage(L2PcInstance activeChar)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage();
		adminReply.setFile(activeChar, activeChar.getHtmlPrefix(), "data/html/admin/forts.htm");
		
		final List<Fort> forts = FortManager.getInstance().getForts();
		final StringBuilder cList = new StringBuilder(forts.size() * 100);
		
		int i = 0;
		for (Fort fort : forts)
		{
			if (fort != null)
			{
				StringUtil.append(cList, "<td fixwidth=135><a action=\"bypass -h admin_fortsiege ", String.valueOf(fort.getResidenceId()), "\">", fort.getName(), " id: ", String.valueOf(fort.getResidenceId()), "</a></td>");
			}
			if ((++i % 2) == 0)
			{
				cList.append("</tr><tr>");
			}
		}
		
		adminReply.replace("%forts%", cList.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void showFortSiegePage(L2PcInstance activeChar, Fort fort)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage();
		adminReply.setFile(activeChar, activeChar.getHtmlPrefix(), "data/html/admin/fort.htm");
		adminReply.replace("%fortName%", fort.getName());
		adminReply.replace("%fortId%", String.valueOf(fort.getResidenceId()));
		activeChar.sendPacket(adminReply);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
}
