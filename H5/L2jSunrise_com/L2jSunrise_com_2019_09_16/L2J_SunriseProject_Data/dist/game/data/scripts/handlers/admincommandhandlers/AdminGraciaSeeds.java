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

import java.util.Calendar;
import java.util.StringTokenizer;

import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.instancemanager.SoDManager;
import l2r.gameserver.instancemanager.SoIManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

public class AdminGraciaSeeds implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_gracia_seeds",
		"admin_kill_tiat",
		"admin_set_sodstate",
		"admin_set_soistage"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command
		
		String val = "";
		if (st.countTokens() >= 1)
		{
			val = st.nextToken();
		}
		
		if (actualCommand.equalsIgnoreCase("admin_kill_tiat"))
		{
			SoDManager.getInstance().increaseSoDTiatKilled();
		}
		else if (actualCommand.equalsIgnoreCase("admin_set_sodstate"))
		{
			SoDManager.getInstance().setSoDState(Integer.parseInt(val), true);
		}
		else if (actualCommand.equalsIgnoreCase("admin_set_soistage"))
		{
			SoIManager.setCurrentStage(Integer.parseInt(val));
		}
		
		showMenu(activeChar);
		return true;
	}
	
	private void showMenu(L2PcInstance activeChar)
	{
		NpcHtmlMessage html = new NpcHtmlMessage();
		html.setFile(activeChar, activeChar.getHtmlPrefix(), "data/html/admin/graciaseeds.htm");
		// Seed of destruction
		html.replace("%sodstage%", String.valueOf(SoDManager.getInstance().getSoDState()));
		html.replace("%sodtiatkill%", String.valueOf(SoDManager.getInstance().getSoDTiatKilled()));
		if (SoDManager.getInstance().getSoDTimeForNextStateChange() > 0)
		{
			Calendar nextChangeDate = Calendar.getInstance();
			nextChangeDate.setTimeInMillis(System.currentTimeMillis() + SoDManager.getInstance().getSoDTimeForNextStateChange());
			html.replace("%sodtime%", nextChangeDate.getTime().toString());
		}
		else
		{
			html.replace("%sodtime%", "-1");
		}
		
		// Seed of infinity
		html.replace("%soistage%", SoIManager.getCurrentStage());
		html.replace("%twinkills%", "N/A");
		html.replace("%cohemeneskills%", SoIManager.getCohemenesCount());
		html.replace("%ekimuskills%", SoIManager.getEkimusCount());
		html.replace("%soitime%", "N/A");
		activeChar.sendPacket(html);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
