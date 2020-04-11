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

import com.l2jserver.Config;
import com.l2jserver.gameserver.handler.IBypassHandler;
import com.l2jserver.gameserver.instancemanager.Leaderboards.ArenaLeaderboard;
import com.l2jserver.gameserver.instancemanager.Leaderboards.CraftLeaderboard;
import com.l2jserver.gameserver.instancemanager.Leaderboards.FishermanLeaderboard;
import com.l2jserver.gameserver.instancemanager.Leaderboards.TvTLeaderboard;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author Gnacik
 */

public class Leaderboard implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"rank_arena_info",
		"rank_fisherman_info",
		"rank_craft_info",
		"rank_tvt_info"
	};
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (!(target instanceof L2Npc))
		{
			return false;
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(((L2Npc) target).getObjectId());
		
		if (command.toLowerCase().startsWith(COMMANDS[0]) && Config.RANK_ARENA_ENABLED)
		{
			html.setHtml(ArenaLeaderboard.getInstance().showHtm(activeChar.getObjectId()));
			activeChar.sendPacket(html);
		}
		else if (command.toLowerCase().startsWith(COMMANDS[1]) && Config.RANK_FISHERMAN_ENABLED)
		{
			html.setHtml(FishermanLeaderboard.getInstance().showHtm(activeChar.getObjectId()));
			activeChar.sendPacket(html);
		}
		else if (command.toLowerCase().startsWith(COMMANDS[2]) && Config.RANK_CRAFT_ENABLED)
		{
			html.setHtml(CraftLeaderboard.getInstance().showHtm(activeChar.getObjectId()));
			activeChar.sendPacket(html);
		}
		else if (command.toLowerCase().startsWith(COMMANDS[3]) && Config.RANK_TVT_ENABLED)
		{
			html.setHtml(TvTLeaderboard.getInstance().showHtm(activeChar.getObjectId()));
			activeChar.sendPacket(html);
		}
		return false;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}