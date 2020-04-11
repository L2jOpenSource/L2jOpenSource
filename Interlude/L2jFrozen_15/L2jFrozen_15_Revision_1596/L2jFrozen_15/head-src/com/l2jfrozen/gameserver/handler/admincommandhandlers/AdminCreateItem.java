package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.text.NumberFormat;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.templates.L2Item;

/**
 * @author ReynalDev
 */
public class AdminCreateItem implements IAdminCommandHandler
{
	private static Logger LOGGER = Logger.getLogger(AdminCreateItem.class);
	private static final NumberFormat NF = NumberFormat.getInstance();
	private static final int MAX_ITEMS_PER_PAGE = 15;
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_itemcreate",
		"admin_create_item",
		"admin_mass_create",
		"admin_clear_inventory",
		"admin_searchitem",
		"admin_createForm"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command == null || activeChar == null)
		{
			return false;
		}
		
		StringTokenizer st = new StringTokenizer(command);
		String actualCommand = st.nextToken();
		
		if (actualCommand.equals("admin_itemcreate"))
		{
			AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
			return true;
		}
		else if (actualCommand.equals("admin_create_item"))
		{
			if (st.hasMoreTokens())
			{
				if (st.countTokens() == 2)
				{
					int itemId = 0;
					int amount = 0;
					
					try
					{
						itemId = Integer.parseInt(st.nextToken());
						amount = Integer.parseInt(st.nextToken());
					}
					catch (final NumberFormatException e)
					{
						
						activeChar.sendMessage("Usage: //itemcreate <itemId> (number value > 0) [amount] (number value > 0)");
						return false;
					}
					
					if (itemId > 0 && amount > 0)
					{
						createItem(activeChar, itemId, amount);
						return true;
					}
					
					activeChar.sendMessage("Usage: //itemcreate <itemId> (number value > 0) [amount] (number value > 0)");
					return false;
				}
				else if (st.countTokens() == 1)
				{
					int itemId = 0;
					
					try
					{
						itemId = Integer.parseInt(st.nextToken());
					}
					catch (NumberFormatException e)
					{
						activeChar.sendMessage("Usage: //itemcreate <itemId> (number value > 0) [amount] (number value > 0)");
						return false;
					}
					
					if (itemId > 0)
					{
						createItem(activeChar, itemId, 1);
						return true;
					}
					
					activeChar.sendMessage("Usage: //itemcreate <itemId> (number value > 0) [amount] (number value > 0)");
					return false;
				}
			}
			else
			{
				AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
				return true;
			}
			
			return false;
		}
		else if (actualCommand.equals("admin_mass_create"))
		{
			if (st.hasMoreTokens())
			{
				if (st.countTokens() == 2)
				{
					int itemId = 0;
					int amount = 0;
					
					try
					{
						itemId = Integer.parseInt(st.nextToken());
						amount = Integer.parseInt(st.nextToken());
					}
					catch (NumberFormatException e)
					{
						activeChar.sendMessage("Usage: //mass_create <itemId> <amount>");
						return false;
					}
					
					if (itemId > 0 && amount > 0)
					{
						massCreateItem(activeChar, itemId, amount);
						return true;
					}
					
					activeChar.sendMessage("Usage: //mass_create <itemId> <amount>");
					return false;
				}
				else if (st.countTokens() == 1)
				{
					int itemId = 0;
					
					try
					{
						itemId = Integer.parseInt(st.nextToken());
					}
					catch (NumberFormatException e)
					{
						activeChar.sendMessage("Usage: //mass_create <itemId> <amount>");
						return false;
					}
					
					if (itemId > 0)
					{
						massCreateItem(activeChar, itemId, 1);
						return true;
					}
					
					activeChar.sendMessage("Usage: //mass_create <itemId> <amount>");
					return false;
				}
			}
			
			return false;
		}
		else if (actualCommand.equals("admin_clear_inventory"))
		{
			removeAllItems(activeChar);
			return true;
		}
		else if (command.startsWith("admin_searchitem"))
		{
			StringTokenizer stItem = new StringTokenizer(command, ";");
			stItem.nextToken(); // Actual command
			
			if (stItem.countTokens() == 2)
			{
				NpcHtmlMessage htm = new NpcHtmlMessage(5);
				htm.setFile("data/html/admin/searchitem.htm");
				String itemName = stItem.nextToken().trim();
				StringBuilder sb = new StringBuilder();
				
				if (NumberUtils.isNumber(itemName))
				{
					int itemId = 0;
					
					try
					{
						itemId = Integer.parseInt(itemName);
					}
					catch (Exception e)
					{
						LOGGER.error("Invalid itemId", e);
						return false;
					}
					
					L2Item item = ItemTable.getInstance().getTemplate(itemId);
					
					sb.append("<table width=\"280\">");
					sb.append("<tr>");
					sb.append("<td width=\"250\"><center><font color=\"LEVEL\">Name</font></center></td>");
					sb.append("<td width=\"30\"><font color=\"LEVEL\">ID</font></td>");
					sb.append("</tr>");
					sb.append("<tr><td>");
					sb.append("<a action=\"bypass -h admin_createForm ");
					sb.append(item.getItemId());
					sb.append("\">");
					sb.append(item.getName());
					sb.append("</a>");
					sb.append("</td><td>");
					sb.append(item.getItemId());
					sb.append("</td></tr>");
					sb.append("</table>");
					htm.replace("%list%", sb.toString());
					activeChar.sendPacket(htm);
					return true;
				}
				
				int page = 0;
				
				try
				{
					page = Integer.parseInt(stItem.nextToken());
				}
				catch (Exception e)
				{
					LOGGER.error("Invalid number", e);
				}
				
				List<L2Item> list = ItemTable.getInstance().getAllTemplatesByText(itemName);
				
				int maxPages = list.size() / MAX_ITEMS_PER_PAGE;
				
				if (list.size() > MAX_ITEMS_PER_PAGE * maxPages)
				{
					maxPages++;
				}
				
				if (page > maxPages)
				{
					page = maxPages;
				}
				
				int start = MAX_ITEMS_PER_PAGE * page;
				int end = list.size();
				
				if (end - start > MAX_ITEMS_PER_PAGE)
				{
					end = start + MAX_ITEMS_PER_PAGE;
				}
				
				// Paginator
				sb.append("<table width=240><tr>");
				
				int previousPage = page - 1;
				
				if (previousPage >= 0)
				{
					sb.append("<td width=\"80\"><a action=\"bypass -h admin_searchitem ;");
					sb.append(itemName);
					sb.append(";");
					sb.append(previousPage);
					sb.append("\">");
					sb.append("Previous page");
					sb.append("</a>");
					sb.append("</td>");
				}
				else
				{
					sb.append("<td width=\"80\"></td>");
				}
				
				if (page >= 1) // La pagina inicial es 0
				{
					sb.append("<td width=\"80\">");
					sb.append("Page ");
					sb.append(page);
					sb.append("</td>");
				}
				
				int nextPage = page + 1;
				
				if (nextPage < maxPages)
				{
					sb.append("<td width=\"80\"><a action=\"bypass -h admin_searchitem ;");
					sb.append(itemName);
					sb.append(";");
					sb.append(nextPage);
					sb.append("\">");
					sb.append("Next page");
					sb.append("</a>");
					sb.append("</td>");
				}
				else
				{
					sb.append("<td width=\"80\"></td>");
				}
				
				sb.append("</tr></table>");
				// End paginator
				
				sb.append("<table width=\"280\">");
				sb.append("<tr>");
				sb.append("<td width=\"250\"><center><font color=\"LEVEL\">Name</font></center></td>");
				sb.append("<td width=\"30\"><font color=\"LEVEL\">ID</font></td>");
				sb.append("</tr>");
				
				for (int i = start; i < end; i++)
				{
					L2Item item = list.get(i);
					sb.append("<tr><td>");
					sb.append("<a action=\"bypass -h admin_createForm ");
					sb.append(item.getItemId());
					sb.append("\">");
					sb.append(item.getName());
					sb.append("</a>");
					sb.append("</td><td>");
					sb.append(item.getItemId());
					sb.append("</td></tr>");
				}
				
				sb.append("</table>");
				
				htm.replace("%list%", sb.toString());
				activeChar.sendPacket(htm);
			}
			else
			{
				NpcHtmlMessage htm = new NpcHtmlMessage(5);
				htm.setFile("data/html/admin/searchitem.htm");
				htm.replace("%list%", "");
				activeChar.sendPacket(htm);
			}
			
			return true;
		}
		else if (actualCommand.startsWith("admin_createForm"))
		{
			if (st.countTokens() == 1)
			{
				try
				{
					int itemId = Integer.parseInt(st.nextToken());
					
					L2Item item = ItemTable.getInstance().getTemplate(itemId);
					
					NpcHtmlMessage htm = new NpcHtmlMessage(5);
					htm.setFile("data/html/admin/searchitem_createform.htm");
					htm.replace("%item_name%", item.getName());
					htm.replace("%item_id%", itemId);
					activeChar.sendPacket(htm);
				}
				catch (Exception e)
				{
					LOGGER.error("AdminCreateItem.useAdminCommand: Invalid number format ", e);
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void createItem(L2PcInstance activeChar, int id, int num)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		
		if (target == null)
		{
			player = activeChar;
		}
		else if (target.isPlayer())
		{
			player = (L2PcInstance) target;
		}
		else
		{
			player = activeChar;
		}
		
		L2Item item = ItemTable.getInstance().getTemplate(id);
		
		player.addItem("GM create item", id, num, activeChar, true);
		
		if (activeChar.getName().equalsIgnoreCase(player.getName()))
		{
			activeChar.sendMessage("You created " + NF.format(num) + " " + item.toString() + " in your inventory.");
		}
		else
		{
			activeChar.sendMessage("You created " + NF.format(num) + " " + item.toString() + " in " + player.getName() + " inventory.");
		}
	}
	
	public void massCreateItem(L2PcInstance activeChar, int itemId, int amount)
	{
		L2Item item = ItemTable.getInstance().getTemplate(itemId);
		
		L2World.getInstance().getAllPlayers().forEach(player -> player.addItem("GM mass create", itemId, amount, activeChar, true));
		activeChar.sendMessage("You mass created " + NF.format(amount) + " " + item.toString() + " in world players inventory.");
		LOGGER.info("GM " + activeChar.getName() + " mass created " + amount + "  " + item.toString());
	}
	
	private void removeAllItems(L2PcInstance activeChar)
	{
		for (L2ItemInstance item : activeChar.getInventory().getItems())
		{
			if (item.getLocation() == L2ItemInstance.ItemLocation.INVENTORY)
			{
				activeChar.getInventory().destroyItem("Destroy", item.getObjectId(), item.getCount(), activeChar, null);
			}
		}
		
		activeChar.sendPacket(new ItemList(activeChar, false));
		activeChar.sendMessage("Your inventory has been cleared.");
	}
}
