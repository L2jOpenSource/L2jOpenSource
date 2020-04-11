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

import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import gr.sr.logsViewer.LogsViewer;

public class AdminLogsViewer implements IAdminCommandHandler
{
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_logsviewer",
		"admin_startViewer",
		"admin_stopViewer",
		"admin_viewLog",
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_logsviewer"))
		{
			AdminHtml.showAdminHtml(activeChar, "logsViewer.htm");
			return true;
		}
		
		String file = command.split(" ")[1];
		if (command.startsWith("admin_viewLog"))
		{
			AdminHtml.showAdminHtml(activeChar, "logsViewer.htm");
			LogsViewer.sendCbWindow(activeChar, file);
			return true;
		}
		if (command.startsWith("admin_startViewer"))
		{
			LogsViewer.startLogViewer(activeChar, file);
			return true;
		}
		if (command.startsWith("admin_stopViewer"))
		{
			LogsViewer.stopLogViewer(activeChar, file);
			return true;
		}
		return false;
		
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
}
