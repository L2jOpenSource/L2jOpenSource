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
package handlers.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jserver.gameserver.handler.IAdminCommandHandler;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * This class handles following admin commands: - admin|admin1/admin2/admin3/admin4/admin5 = slots for the 5 starting admin menus - gmliston/gmlistoff = includes/excludes active character from /gmlist results - silence = toggles private messages acceptance mode - diet = toggles weight penalty mode -
 * tradeoff = toggles trade acceptance mode - reload = reloads specified component from multisell|skill|npc|htm|item - set/set_menu/set_mod = alters specified server setting - saveolymp = saves olympiad state manually - manualhero = cycles olympiad and calculate new heroes.
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2007/07/28 10:06:06 $
 */
public class AdminPrimeShop implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_add_prime_points",
		"admin_count_prime_points",
		"admin_primeshop",
		"admin_set_prime_points",
		"admin_subtract_prime_points"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_add_prime_points"))
		{
			try
			{
				final String val = command.substring(22);
				if (!addGamePoints(activeChar, val))
				{
					activeChar.sendMessage("Usage: //add_prime_points count");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //add_prime_points count");
			}
		}
		else if (command.equals("admin_count_prime_points"))
		{
			if ((activeChar.getTarget() != null) && activeChar.getTarget().isPlayer())
			{
				final L2PcInstance target = (L2PcInstance) activeChar.getTarget();
				activeChar.sendMessage(target.getName() + " has a total of " + target.getPrimeShopPoints() + " prime points.");
			}
			else
			{
				activeChar.sendMessage("You must select a player first.");
			}
		}
		else if (command.equals("admin_primeshop"))
		{
			openGamePointsMenu(activeChar);
		}
		else if (command.startsWith("admin_set_prime_points"))
		{
			try
			{
				final String val = command.substring(22);
				if (!setGamePoints(activeChar, val))
				{
					activeChar.sendMessage("Usage: //set_prime_points count");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //set_prime_points count");
			}
		}
		else if (command.startsWith("admin_subtract_prime_points"))
		{
			try
			{
				final String val = command.substring(27);
				if (!subtractGamePoints(activeChar, val))
				{
					activeChar.sendMessage("Usage: //subtract_prime_points count");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //subtract_prime_points count");
			}
		}
		return true;
	}
	
	private void openGamePointsMenu(L2PcInstance activeChar)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage();
		html.setFile(activeChar.getHtmlPrefix(), "data/html/admin/primeshop.htm");
		activeChar.sendPacket(html);
	}
	
	private boolean addGamePoints(L2PcInstance activeChar, String val)
	{
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return false;
		}
		
		final StringTokenizer st = new StringTokenizer(val);
		if (st.countTokens() != 1)
		{
			activeChar.sendMessage("Invalid prime point count.");
			return false;
		}
		
		final String value = st.nextToken();
		long points = 0;
		try
		{
			points = Long.parseLong(value);
		}
		catch (Exception e)
		{
			return false;
		}
		
		if (points < 1)
		{
			activeChar.sendMessage("Invalid prime point count.");
			return false;
		}
		
		final long currentPoints = player.getPrimeShopPoints();
		if (currentPoints < 1)
		{
			player.setPrimeShopPoints(points);
		}
		else
		{
			player.setPrimeShopPoints(currentPoints + points);
		}
		
		player.sendMessage("Added " + points + " prime points to " + player.getName() + ".");
		player.sendMessage(player.getName() + " has now a total of " + player.getPrimeShopPoints() + " prime points.");
		return true;
	}
	
	private boolean setGamePoints(L2PcInstance activeChar, String val)
	{
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return false;
		}
		
		final StringTokenizer st = new StringTokenizer(val);
		if (st.countTokens() != 1)
		{
			activeChar.sendMessage("Invalid prime point count.");
			return false;
		}
		
		final String value = st.nextToken();
		long points = 0;
		try
		{
			points = Long.parseLong(value);
		}
		catch (Exception e)
		{
			return false;
		}
		
		if (points < 0)
		{
			activeChar.sendMessage("Invalid prime point count.");
			return false;
		}
		
		player.setPrimeShopPoints(points);
		activeChar.sendMessage(player.getName() + " has now a total of " + points + " prime points.");
		return true;
	}
	
	private boolean subtractGamePoints(L2PcInstance activeChar, String val)
	{
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return false;
		}
		
		final StringTokenizer st = new StringTokenizer(val);
		if (st.countTokens() != 1)
		{
			activeChar.sendMessage("Invalid prime point count.");
			return false;
		}
		
		final String value = st.nextToken();
		long points = 0;
		try
		{
			points = Long.parseLong(value);
		}
		catch (Exception e)
		{
			return false;
		}
		
		if (points < 1)
		{
			activeChar.sendMessage("Invalid prime point count.");
			return false;
		}
		
		final long currentPoints = player.getPrimeShopPoints();
		if (currentPoints < 1)
		{
			player.setPrimeShopPoints(points);
		}
		else
		{
			player.setPrimeShopPoints(currentPoints - points);
		}
		
		activeChar.sendMessage(player.getName() + " has now a total of " + player.getPrimeShopPoints() + " prime points.");
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}