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

import java.util.Collection;

import com.l2jserver.Config;
import com.l2jserver.gameserver.cache.HtmCache;
import com.l2jserver.gameserver.handler.IVoicedCommandHandler;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Command for show online players count.
 * @author St3eT
 */
public class OnlineStatus implements IVoicedCommandHandler
{
	private static long LAST_UPDATE = 0;
	private static int OFFLINE_COUNT = 0;
	private static int ONLINE_COUNT = 0;
	
	private static final String[] _voicedCommands =
	{
		"online"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		if (command.equals("online"))
		{
			if (System.currentTimeMillis() > (LAST_UPDATE + (5 * 60 * 1000)))
			{
				LAST_UPDATE = System.currentTimeMillis();
				ONLINE_COUNT = L2World.getInstance().getAllPlayersCount();
				int offlineCount = 0;
				
				final Collection<L2PcInstance> objs = L2World.getInstance().getPlayers();
				for (L2PcInstance player : objs)
				{
					if ((player.getClient() == null) || player.getClient().isDetached())
					{
						offlineCount++;
					}
				}
				OFFLINE_COUNT = offlineCount;
			}
			
			if (Config.ONLINE_STATUS_HTML)
			{
				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
				String htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), Config.ONLINE_STATUS_SHOW_OFFLINE ? "data/html/mods/OnlineStatus-off.htm" : "data/html/mods/OnlineStatus.htm");
				npcHtmlMessage.setHtml(htmContent);
				npcHtmlMessage.replace("%OnlineCount%", String.valueOf(ONLINE_COUNT));
				npcHtmlMessage.replace("%OfflineCount%", String.valueOf(OFFLINE_COUNT));
				activeChar.sendPacket(npcHtmlMessage);
			}
			else
			{
				activeChar.sendMessage("===============");
				activeChar.sendMessage("Total player count: " + ONLINE_COUNT);
				if (Config.ONLINE_STATUS_SHOW_OFFLINE)
				{
					activeChar.sendMessage("Offline shop count: " + OFFLINE_COUNT);
				}
				activeChar.sendMessage("===============");
			}
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}