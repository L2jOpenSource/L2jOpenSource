/*
 * Copyright (C) 2004-2014 L2J DataPack
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
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.util.Util;

/**
 * This class handles following admin commands:
 * <ul>
 * <li>show_ivetory</li>
 * <li>delete_item</li>
 * </ul>
 * @author Zealar
 */
public class AdminInventory implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_show_inventory",
		"admin_delete_item"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if ((activeChar.getTarget() == null))
		{
			activeChar.sendMessage("Select a target");
			return false;
		}
		
		if (!activeChar.getTarget().isPlayer())
		{
			activeChar.sendMessage("Target need to be player");
			return false;
		}
		
		L2PcInstance player = activeChar.getTarget().getActingPlayer();
		
		if (command.startsWith(ADMIN_COMMANDS[0]))
		{
			if (command.length() > ADMIN_COMMANDS[0].length())
			{
				String com = command.substring(ADMIN_COMMANDS[0].length() + 1);
				if (Util.isDigit(com))
				{
					showItemsPage(activeChar, Integer.parseInt(com));
				}
			}
			
			else
			{
				showItemsPage(activeChar, 0);
			}
		}
		else if (command.contains(ADMIN_COMMANDS[1]))
		{
			String val = command.substring(ADMIN_COMMANDS[1].length() + 1);
			
			player.destroyItem("GM Destroy", Integer.parseInt(val), player.getInventory().getItemByObjectId(Integer.parseInt(val)).getCount(), null, true);
			showItemsPage(activeChar, 0);
		}
		
		return true;
	}
	
	private void showItemsPage(L2PcInstance activeChar, int page)
	{
		final L2PcInstance target = activeChar.getTarget().getActingPlayer();
		
		final L2ItemInstance[] items = target.getInventory().getItems();
		
		int maxItemsPerPage = 10;
		int maxPages = items.length / maxItemsPerPage;
		if (items.length > (maxItemsPerPage * maxPages))
		{
			maxPages++;
		}
		
		if (page > maxPages)
		{
			page = maxPages;
		}
		
		int itemsStart = maxItemsPerPage * page;
		int itemsEnd = items.length;
		if ((itemsEnd - itemsStart) > maxItemsPerPage)
		{
			itemsEnd = itemsStart + maxItemsPerPage;
		}
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage();
		adminReply.setFile(activeChar, activeChar.getHtmlPrefix(), "data/html/admin/inventory.htm");
		adminReply.replace("%PLAYER_NAME%", target.getName());
		
		StringBuilder sbPages = new StringBuilder();
		for (int x = 0; x < maxPages; x++)
		{
			int pagenr = x + 1;
			sbPages.append("<td><button value=\"" + String.valueOf(pagenr) + "\" action=\"bypass -h admin_show_inventory " + String.valueOf(x) + "\" width=20 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		
		adminReply.replace("%PAGES%", sbPages.toString());
		
		StringBuilder sbItems = new StringBuilder();
		
		for (int i = itemsStart; i < itemsEnd; i++)
		{
			sbItems.append("<tr><td><img src=\"" + items[i].getItem().getIcon() + "\" width=32 height=32></td>");
			sbItems.append("<td width=60>" + items[i].getName() + "</td>");
			sbItems.append("<td><button action=\"bypass -h admin_delete_item " + String.valueOf(items[i].getObjectId()) + "\" width=16 height=16 back=\"L2UI_ct1.Button_DF_Delete\" fore=\"L2UI_ct1.Button_DF_Delete\">" + "</td></tr>");
		}
		
		adminReply.replace("%ITEMS%", sbItems.toString());
		
		activeChar.sendPacket(adminReply);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}