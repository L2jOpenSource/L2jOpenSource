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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.StringTokenizer;

import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.data.xml.impl.AdminData;
import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.model.L2AccessLevel;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;

import gr.sr.main.NamePrefix;

/**
 * This class handles following admin commands: - changelvl = change a character's access level Can be used for character ban (as opposed to regular //ban that affects accounts) or to grant mod/GM privileges ingame
 * @version $Revision: 1.1.2.2.2.3 $ $Date: 2005/04/11 10:06:00 $ con.close() change by Zoey76 24/02/2011
 */
public class AdminChangeAccessLevel implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_changelvl",
		"admin_changemasterlvl"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_changelvl"))
		{
			handleChangeLevel(command, activeChar);
		}
		else if (command.startsWith("admin_changemasterlvl"))
		{
			if ((activeChar.getTarget() != null) && activeChar.getTarget().isPlayer())
			{
				try
				{
					String val = command.substring(21);
					StringTokenizer st = new StringTokenizer(val);
					String level = st.nextToken();
					L2PcInstance target;
					target = (L2PcInstance) activeChar.getTarget();
					int masterLevel = Integer.parseInt(level);
					if ((masterLevel >= 0) && (masterLevel <= 4))
					{
						if (target.isGM())
						{
							target.setVar("namePrefixId", String.valueOf(masterLevel));
							NamePrefix.namePrefixCategories(target, Integer.parseInt(target.getVar("namePrefixId", "0")));
							// successfully
							activeChar.sendMessage("You have successfully change master category of player: " + target.getName() + " to Level: " + masterLevel + ".");
						}
						else
						{
							activeChar.sendMessage("Your target must be a GM player.");
							return false;
						}
					}
					else
					{
						activeChar.sendMessage("Usage: //changemasterlvl <0-4>");
						return false;
					}
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Usage: //changemasterlvl <0-4>");
				}
			}
			else
			{
				activeChar.sendMessage("Invalid target.");
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	/**
	 * If no character name is specified, tries to change GM's target access level. Else if a character name is provided, will try to reach it either from L2World or from a database connection.
	 * @param command
	 * @param activeChar
	 */
	private void handleChangeLevel(String command, L2PcInstance activeChar)
	{
		String[] parts = command.split(" ");
		if (parts.length == 2)
		{
			try
			{
				int lvl = Integer.parseInt(parts[1]);
				if (activeChar.getTarget() instanceof L2PcInstance)
				{
					onlineChange(activeChar, (L2PcInstance) activeChar.getTarget(), lvl);
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //changelvl <target_new_level> | <player_name> <new_level>");
			}
		}
		else if (parts.length == 3)
		{
			String name = parts[1];
			int lvl = Integer.parseInt(parts[2]);
			L2PcInstance player = L2World.getInstance().getPlayer(name);
			if (player != null)
			{
				onlineChange(activeChar, player, lvl);
			}
			else
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement ps = con.prepareStatement("UPDATE characters SET accesslevel=? WHERE char_name=?"))
				{
					ps.setInt(1, lvl);
					ps.setString(2, name);
					ps.execute();
					
					if (ps.getUpdateCount() == 0)
					{
						activeChar.sendMessage("Character not found or access level unaltered.");
					}
					else
					{
						activeChar.sendMessage("Character's access level is now set to " + lvl);
					}
				}
				catch (SQLException se)
				{
					activeChar.sendMessage("SQLException while changing character's access level");
					if (Config.DEBUG)
					{
						se.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * @param activeChar the active GM
	 * @param player the online target
	 * @param lvl the access level
	 */
	private static void onlineChange(L2PcInstance activeChar, L2PcInstance player, int lvl)
	{
		if (lvl >= 0)
		{
			if (AdminData.getInstance().hasAccessLevel(lvl))
			{
				final L2AccessLevel acccessLevel = AdminData.getInstance().getAccessLevel(lvl);
				player.setAccessLevel(lvl);
				player.sendMessage("Your access level has been changed to " + acccessLevel.getName() + " (" + acccessLevel.getLevel() + ").");
				activeChar.sendMessage(player.getName() + "'s access level has been changed to " + acccessLevel.getName() + " (" + acccessLevel.getLevel() + ").");
			}
			else
			{
				activeChar.sendMessage("You are trying to set unexisting access level: " + lvl + " please try again with a valid one!");
			}
		}
		else
		{
			player.setAccessLevel(lvl);
			player.sendMessage("Your character has been banned. Bye.");
			player.logout();
		}
	}
}
