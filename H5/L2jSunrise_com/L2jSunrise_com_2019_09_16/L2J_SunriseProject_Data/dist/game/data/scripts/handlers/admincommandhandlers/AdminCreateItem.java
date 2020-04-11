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

import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import l2r.gameserver.data.xml.impl.ItemData;
import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.L2Item;

/**
 * This class handles following admin commands: - itemcreate = show menu - create_item <id> [num] = creates num items with respective id, if num is not specified, assumes 1.
 * @version $Revision: 1.2.2.2.2.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminCreateItem implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_itemcreate",
		"admin_create_item",
		"admin_create_coin",
		"admin_give_item_target",
		"admin_give_item_to_all",
		"admin_give_item_to_online"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_itemcreate"))
		{
			AdminHtml.showAdminHtml(activeChar, "itemcreation.htm");
		}
		else if (command.startsWith("admin_create_item"))
		{
			try
			{
				String val = command.substring(17);
				StringTokenizer st = new StringTokenizer(val);
				if (st.countTokens() == 2)
				{
					String id = st.nextToken();
					int idval = Integer.parseInt(id);
					String num = st.nextToken();
					long numval = Long.parseLong(num);
					createItem(activeChar, activeChar, idval, numval);
				}
				else if (st.countTokens() == 1)
				{
					String id = st.nextToken();
					int idval = Integer.parseInt(id);
					createItem(activeChar, activeChar, idval, 1);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //create_item <itemId> [amount]");
			}
			catch (NumberFormatException nfe)
			{
				activeChar.sendMessage("Specify a valid number.");
			}
			AdminHtml.showAdminHtml(activeChar, "itemcreation.htm");
		}
		else if (command.startsWith("admin_give_item_to_online"))
		{
			String val = command.substring(25);
			StringTokenizer st = new StringTokenizer(val);
			int idval = 0;
			long numval = 0;
			if (st.countTokens() == 2)
			{
				String name = st.nextToken();
				idval = getCustomItemId(name);
				String num = st.nextToken();
				numval = Long.parseLong(num);
			}
			else if (st.countTokens() == 1)
			{
				String name = st.nextToken();
				idval = getCustomItemId(name);
				numval = 1;
			}
			int counter = 0;
			L2Item template = ItemData.getInstance().getTemplate(idval);
			if (template == null)
			{
				activeChar.sendMessage("This item doesn't exist.");
				return false;
			}
			if ((numval > 10) && !template.isStackable())
			{
				activeChar.sendMessage("This item does not stack - Creation aborted.");
				return false;
			}
			for (L2PcInstance onlinePlayer : L2World.getInstance().getPlayers())
			{
				boolean mustBeRewarded = true;
				if ((activeChar != onlinePlayer) && onlinePlayer.isOnline() && ((onlinePlayer.getClient() != null) && !onlinePlayer.getClient().isDetached()))
				{
					String[] player_Ip = new String[2];
					player_Ip[0] = onlinePlayer.getClient().getConnection().getInetAddress().getHostAddress();
					int[][] trace1 = onlinePlayer.getClient().getTrace();
					for (int o = 0; o < trace1[0].length; o++)
					{
						player_Ip[1] = player_Ip[1] + trace1[0][o];
						if (o != (trace1[0].length - 1))
						{
							player_Ip[1] = player_Ip[1] + ".";
						}
					}
					
					if (getAddedIps() != null)
					{
						for (String[] listIps : getAddedIps())
						{
							if (player_Ip[0].equals(listIps[0]) && player_Ip[1].equals(listIps[1]))
							{
								mustBeRewarded = false;
							}
						}
					}
					
					if (mustBeRewarded)
					{
						addToList(onlinePlayer);
						onlinePlayer.getInventory().addItem("Admin", idval, numval, onlinePlayer, activeChar);
						onlinePlayer.sendMessage("Vote system spawned " + numval + " " + template.getName() + " in your inventory.");
						counter++;
					}
				}
			}
			
			if (getAddedIps() != null)
			{
				_dualboxCheck.clear();
			}
			
			activeChar.sendMessage(counter + " players rewarded with " + template.getName());
		}
		else if (command.startsWith("admin_create_coin"))
		{
			try
			{
				String val = command.substring(17);
				StringTokenizer st = new StringTokenizer(val);
				if (st.countTokens() == 2)
				{
					String name = st.nextToken();
					int idval = getCoinId(name);
					if (idval > 0)
					{
						String num = st.nextToken();
						long numval = Long.parseLong(num);
						createItem(activeChar, activeChar, idval, numval);
					}
				}
				else if (st.countTokens() == 1)
				{
					String name = st.nextToken();
					int idval = getCoinId(name);
					createItem(activeChar, activeChar, idval, 1);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //create_coin <name> [amount]");
			}
			catch (NumberFormatException nfe)
			{
				activeChar.sendMessage("Specify a valid number.");
			}
			AdminHtml.showAdminHtml(activeChar, "itemcreation.htm");
		}
		else if (command.startsWith("admin_give_item_target"))
		{
			try
			{
				L2PcInstance target;
				if (activeChar.getTarget() instanceof L2PcInstance)
				{
					target = (L2PcInstance) activeChar.getTarget();
				}
				else
				{
					activeChar.sendMessage("Invalid target.");
					return false;
				}
				
				String val = command.substring(22);
				StringTokenizer st = new StringTokenizer(val);
				if (st.countTokens() == 2)
				{
					String id = st.nextToken();
					int idval = Integer.parseInt(id);
					String num = st.nextToken();
					long numval = Long.parseLong(num);
					createItem(activeChar, target, idval, numval);
				}
				else if (st.countTokens() == 1)
				{
					String id = st.nextToken();
					int idval = Integer.parseInt(id);
					createItem(activeChar, target, idval, 1);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //give_item_target <itemId> [amount]");
			}
			catch (NumberFormatException nfe)
			{
				activeChar.sendMessage("Specify a valid number.");
			}
			AdminHtml.showAdminHtml(activeChar, "itemcreation.htm");
		}
		else if (command.startsWith("admin_give_item_to_all"))
		{
			String val = command.substring(22);
			StringTokenizer st = new StringTokenizer(val);
			int idval = 0;
			long numval = 0;
			if (st.countTokens() == 2)
			{
				String id = st.nextToken();
				idval = Integer.parseInt(id);
				String num = st.nextToken();
				numval = Long.parseLong(num);
			}
			else if (st.countTokens() == 1)
			{
				String id = st.nextToken();
				idval = Integer.parseInt(id);
				numval = 1;
			}
			int counter = 0;
			L2Item template = ItemData.getInstance().getTemplate(idval);
			if (template == null)
			{
				activeChar.sendMessage("This item doesn't exist.");
				return false;
			}
			if ((numval > 10) && !template.isStackable())
			{
				activeChar.sendMessage("This item does not stack - Creation aborted.");
				return false;
			}
			for (L2PcInstance onlinePlayer : L2World.getInstance().getPlayers())
			{
				if ((activeChar != onlinePlayer) && onlinePlayer.isOnline() && ((onlinePlayer.getClient() != null) && !onlinePlayer.getClient().isDetached()))
				{
					onlinePlayer.getInventory().addItem("Admin", idval, numval, onlinePlayer, activeChar);
					onlinePlayer.sendMessage("Admin spawned " + numval + " " + template.getName() + " in your inventory.");
					counter++;
				}
			}
			activeChar.sendMessage(counter + " players rewarded with " + template.getName());
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void createItem(L2PcInstance activeChar, L2PcInstance target, int id, long num)
	{
		L2Item template = ItemData.getInstance().getTemplate(id);
		if (template == null)
		{
			activeChar.sendMessage("This item doesn't exist.");
			return;
		}
		if ((num > 10) && !template.isStackable())
		{
			activeChar.sendMessage("This item does not stack - Creation aborted.");
			return;
		}
		
		target.getInventory().addItem("Admin", id, num, activeChar, null);
		
		if (activeChar != target)
		{
			target.sendMessage("Admin spawned " + num + " " + template.getName() + " in your inventory.");
		}
		activeChar.sendMessage("You have spawned " + num + " " + template.getName() + "(" + id + ") in " + target.getName() + " inventory.");
	}
	
	private int getCoinId(String name)
	{
		int id;
		if (name.equalsIgnoreCase("adena"))
		{
			id = 57;
		}
		else if (name.equalsIgnoreCase("ancientadena"))
		{
			id = 5575;
		}
		else if (name.equalsIgnoreCase("festivaladena"))
		{
			id = 6673;
		}
		else if (name.equalsIgnoreCase("blueeva"))
		{
			id = 4355;
		}
		else if (name.equalsIgnoreCase("goldeinhasad"))
		{
			id = 4356;
		}
		else if (name.equalsIgnoreCase("silvershilen"))
		{
			id = 4357;
		}
		else if (name.equalsIgnoreCase("bloodypaagrio"))
		{
			id = 4358;
		}
		else if (name.equalsIgnoreCase("fantasyislecoin"))
		{
			id = 13067;
		}
		else
		{
			id = 0;
		}
		
		return id;
	}
	
	private int getCustomItemId(String name)
	{
		int id;
		if (name.equalsIgnoreCase("GiantCodex"))
		{
			id = 6622;
		}
		else if (name.equalsIgnoreCase("Mastery"))
		{
			id = 9627;
		}
		else if (name.equalsIgnoreCase("FestivalAdena"))
		{
			id = 40002;
		}
		else if (name.equalsIgnoreCase("TopLs80"))
		{
			id = 9576;
		}
		else if (name.equalsIgnoreCase("BlessWeapon"))
		{
			id = 6577;
		}
		else if (name.equalsIgnoreCase("BlessArmor"))
		{
			id = 6578;
		}
		else
		{
			id = 0;
		}
		
		return id;
	}
	
	protected static final Set<String[]> _dualboxCheck = ConcurrentHashMap.newKeySet();
	
	// Dual Box Check pcIp based
	protected static boolean addToList(L2PcInstance player)
	{
		String[] player_Ip = new String[2];
		player_Ip[0] = player.getClient().getConnection().getInetAddress().getHostAddress();
		int[][] trace1 = player.getClient().getTrace();
		for (int o = 0; o < trace1[0].length; o++)
		{
			player_Ip[1] = player_Ip[1] + trace1[0][o];
			if (o != (trace1[0].length - 1))
			{
				player_Ip[1] = player_Ip[1] + ".";
			}
		}
		
		_dualboxCheck.add(player_Ip);
		return true;
	}
	
	public Set<String[]> getAddedIps()
	{
		return _dualboxCheck;
	}
}
