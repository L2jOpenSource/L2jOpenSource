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

import java.text.SimpleDateFormat;

import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vGodFather
 */
public class AdminOlympiad implements IAdminCommandHandler
{
	private static final Logger _log = LoggerFactory.getLogger(AdminOlympiad.class);
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_oly",
		"admin_saveoly",
		"admin_endoly",
		"admin_finisholy",
		"admin_manualhero",
		"admin_sethero",
		"admin_checkoly",
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_oly"))
		{
			showMainPage(activeChar);
			return true;
		}
		else if (command.startsWith("admin_saveoly"))
		{
			Olympiad.getInstance().saveOlympiadStatus();
			activeChar.sendMessage("olympiad system saved.");
		}
		else if (command.startsWith("admin_endoly") || command.startsWith("admin_finisholy"))
		{
			try
			{
				Olympiad.getInstance().manualSelectHeroes();
			}
			catch (Exception e)
			{
				_log.warn("An error occured while ending olympiad: " + e);
			}
			activeChar.sendMessage("Heroes formed.");
		}
		else if (command.startsWith("admin_manualhero") || command.startsWith("admin_sethero"))
		{
			if (activeChar.getTarget() == null)
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			
			final L2PcInstance target = activeChar.getTarget().isPlayer() ? activeChar.getTarget().getActingPlayer() : activeChar;
			target.setHero(!target.isHero());
			target.broadcastUserInfo();
		}
		else if (command.startsWith("admin_checkoly"))
		{
			final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			int period = Olympiad.getInstance().getPeriod();
			activeChar.sendMessage("Olympiad System: Period ends at " + format.format(period == 0 ? Olympiad.getInstance().getMillisToOlympiadEnd() + System.currentTimeMillis() : Olympiad.getInstance().getMillisToValidationEnd() + System.currentTimeMillis()));
		}
		
		showMainPage(activeChar);
		return true;
	}
	
	private void showMainPage(L2PcInstance activeChar)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage();
		adminReply.setFile(activeChar, activeChar.getHtmlPrefix(), "data/html/admin/olympiad_menu.htm");
		
		int period = Olympiad.getInstance().getPeriod();
		
		long milliToEnd = period == 0 ? Olympiad.getInstance().getMillisToOlympiadEnd() : Olympiad.getInstance().getMillisToValidationEnd();
		
		double numSecs = (milliToEnd / 1000) % 60;
		double countDown = ((milliToEnd / 1000) - numSecs) / 60;
		int numMins = (int) Math.floor(countDown % 60);
		countDown = (countDown - numMins) / 60;
		int numHours = (int) Math.floor(countDown % 24);
		int numDays = (int) Math.floor((countDown - numHours) / 24);
		
		final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		adminReply.replace("%olyperiod%", period == 0 ? "Olympiad" : "Validation");
		adminReply.replace("%endolyperiod%", String.valueOf(format.format(milliToEnd + System.currentTimeMillis())));
		adminReply.replace("%endolytime%", numDays + " day(s) " + numHours + " hour(s) " + numMins + " min(s)");
		activeChar.sendPacket(adminReply);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
