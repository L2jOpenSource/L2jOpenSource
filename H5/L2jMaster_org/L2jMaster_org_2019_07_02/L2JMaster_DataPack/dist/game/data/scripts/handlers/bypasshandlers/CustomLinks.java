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
 package handlers.bypasshandlers;

import com.l2jserver.gameserver.cache.HtmCache;
import com.l2jserver.gameserver.handler.IBypassHandler;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * With this bypass you can add custom links to html files.<br>
 * <a action="bypass -h CustomLinks custom.html">Custom Link</a><br>
 * Root directory data/html/
 * @version 1.0
 * @author Zoey76
 */
public class CustomLinks implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"CustomLinks"
	};
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		try
		{
			String path = command.substring(11).trim();
			if (path.indexOf("..") != -1)
			{
				return false;
			}
			String filename = "data/html/" + path;
			
			String content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), filename);
			
			if ((content == null) || content.isEmpty())
			{
				_log.warning("[" + getClass().getSimpleName() + "] " + filename + " content is null or empty.");
				return false;
			}
			
			activeChar.sendPacket(new NpcHtmlMessage(0, content));
			return true;
		}
		catch (Exception e)
		{
			_log.warning("[" + getClass().getSimpleName() + "] Exception: " + e);
		}
		return false;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}