/*
 * Copyright (C) 2004-2019 L2J DataPack
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
package handlers.voicedcommandhandlers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import com.l2jserver.Config;
import com.l2jserver.gameserver.data.xml.impl.NpcData;
import com.l2jserver.gameserver.handler.IVoicedCommandHandler;
import com.l2jserver.gameserver.instancemanager.GrandBossManager;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author JMD.
 */

public class Epic implements IVoicedCommandHandler
{
	static final Logger _log = Logger.getLogger(Epic.class.getName());
	private static final String[] VOICED_COMMANDS =
	{
		"epic"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		if (!Config.ALLOW_EPIC_COMMAND)
		{
			activeChar.sendMessage("This command is disabled!");
			return false;
		}
		if (command.startsWith("epic"))
		{
			return Status(activeChar);
		}
		return false;
	}
	
	public boolean Status(L2PcInstance activeChar)
	{
		
		int[] BOSSES =
		{
			29001,
			29006,
			29014,
			29020,
			29028,
			29068,
			29118
		
		};
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		final StringBuilder replyMSG = new StringBuilder("<html><body><center>");
		// replyMSG.append("<font color=\"LEVEL\">* Grand Boss Status & Respawn *</font><br>");
		for (int boss : BOSSES)
		{
			String name = NpcData.getInstance().getTemplate(boss).getName();
			StatsSet stats = GrandBossManager.getInstance().getStatsSet(boss);
			if (stats == null)
			{
				replyMSG.append("Stats for GrandBoss " + boss + " not found!<br>");
				continue;
			}
			
			long delay = stats.getLong("respawn_time");
			long currentTime = System.currentTimeMillis();
			if (delay <= currentTime)
			{
				replyMSG.append("(" + name + ") is <font color=\"00FF00\">Alive</font><br>");
				
			}
			else
			{
				replyMSG.append("(" + name + ") is <font color=\"FF0000\">Dead</font> <font color=\"FF9900\">( " + sdf.format(new Date(delay)) + " )</font><br>");
			}
		}
		replyMSG.append("</center></body></html>");
		final NpcHtmlMessage adminReply = new NpcHtmlMessage();
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}