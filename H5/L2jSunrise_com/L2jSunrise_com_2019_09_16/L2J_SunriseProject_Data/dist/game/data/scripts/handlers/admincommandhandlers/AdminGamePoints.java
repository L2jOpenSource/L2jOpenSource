/*
 * Copyright (C) 2004-2015 L2J DataPack
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
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Admin game point commands.
 * @author Pandragon - vGodFather
 */
public class AdminGamePoints implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_add_game_points",
		"admin_count_game_points",
		"admin_gamepoints",
		"admin_set_game_points",
		"admin_subtract_game_points"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_add_game_points"))
		{
			try
			{
				String val = command.substring(22);
				addGamePoints(activeChar, val);
			}
			catch (Exception e)
			{ // Case of missing parameter
				activeChar.sendMessage("Usage: //add_game_points count");
			}
		}
		else if (command.equals("admin_count_game_points"))
		{
			if ((activeChar.getTarget() != null) && activeChar.getTarget().isPlayer())
			{
				L2PcInstance target = (L2PcInstance) activeChar.getTarget();
				activeChar.sendMessage(target.getName() + " has a total of " + target.getGamePoints() + " game points.");
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			}
		}
		else if (command.equals("admin_gamepoints"))
		{
			openGamePointsMenu(activeChar);
		}
		else if (command.startsWith("admin_set_game_points"))
		{
			try
			{
				String val = command.substring(22);
				setGamePoints(activeChar, val);
			}
			catch (Exception e)
			{ // Case of missing parameter
				activeChar.sendMessage("Usage: //set_game_points count");
			}
		}
		else if (command.startsWith("admin_subtract_game_points"))
		{
			try
			{
				String val = command.substring(27);
				subtractGamePoints(activeChar, val);
			}
			catch (Exception e)
			{ // Case of missing parameter
				activeChar.sendMessage("Usage: //subtract_game_points count");
			}
		}
		return true;
	}
	
	private void openGamePointsMenu(L2PcInstance activeChar)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage();
		html.setFile(activeChar, activeChar.getHtmlPrefix(), "data/html/admin/game_points.htm");
		activeChar.sendPacket(html);
	}
	
	private void addGamePoints(L2PcInstance admin, String val)
	{
		if ((admin.getTarget() == null) || !admin.getTarget().isPlayer())
		{
			admin.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		L2PcInstance player = admin.getTarget().getActingPlayer();
		final Long points = Long.valueOf(val);
		if (points < 1)
		{
			admin.sendMessage("Invalid game point count.");
			return;
		}
		
		final long currentPoints = player.getGamePoints();
		if (currentPoints < 1)
		{
			player.setGamePoints(points);
		}
		else
		{
			player.setGamePoints(currentPoints + points);
		}
		
		admin.sendMessage("Added " + points + " game points to " + player.getName() + ".");
		admin.sendMessage(player.getName() + " has now a total of " + player.getGamePoints() + " game points.");
		return;
	}
	
	private void setGamePoints(L2PcInstance admin, String val)
	{
		if ((admin.getTarget() == null) || !admin.getTarget().isPlayer())
		{
			admin.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		L2PcInstance player = admin.getTarget().getActingPlayer();
		final Long points = Long.valueOf(val);
		if (points < 0)
		{
			admin.sendMessage("Invalid game point count.");
			return;
		}
		
		player.setGamePoints(points);
		admin.sendMessage(player.getName() + " has now a total of " + points + " game points.");
		return;
	}
	
	private void subtractGamePoints(L2PcInstance admin, String val)
	{
		if ((admin.getTarget() == null) || !admin.getTarget().isPlayer())
		{
			admin.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		L2PcInstance player = admin.getTarget().getActingPlayer();
		final Long points = Long.valueOf(val);
		if (points < 1)
		{
			admin.sendMessage("Invalid game points count.");
			return;
		}
		
		final long currentPoints = player.getGamePoints();
		if (currentPoints <= points)
		{
			player.setGamePoints(0);
		}
		else
		{
			player.setGamePoints(currentPoints - points);
		}
		admin.sendMessage(player.getName() + " has now a total of " + player.getGamePoints() + " game points.");
		return;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}