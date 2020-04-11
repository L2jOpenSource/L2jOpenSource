package handlers.admincommandhandlers;

import java.util.StringTokenizer;

import l2r.gameserver.data.xml.impl.ItemData;
import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.model.Elementals;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.L2Item;

import gr.sr.configsEngine.configs.impl.SecuritySystemConfigs;

/**
 * @author -=DoctorNo=-
 */
public class AdminCustomCreateItem implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_custom_itemcreate",
		"admin_custom_create_item",
		"admin_give_custom_item_target"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_custom_itemcreate"))
		{
			AdminHtml.showAdminHtml(activeChar, "customitemcreation.htm");
		}
		else if (command.startsWith("admin_custom_create_item"))
		{
			try
			{
				String val = command.substring(24);
				StringTokenizer st = new StringTokenizer(val);
				String id = st.nextToken();
				int idval = Integer.parseInt(id);
				String enchantValue = st.nextToken();
				int enchantval = Integer.parseInt(enchantValue);
				String attType = st.nextToken();
				String attValue = st.nextToken();
				int attVal = Integer.parseInt(attValue);
				createItem(activeChar, activeChar, idval, enchantval, attType, attVal);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Specify a valid number.");
			}
			AdminHtml.showAdminHtml(activeChar, "customitemcreation.htm");
		}
		else if (command.startsWith("admin_give_custom_item_target"))
		{
			try
			{
				L2PcInstance target;
				if (activeChar.getTarget().isPlayer())
				{
					target = (L2PcInstance) activeChar.getTarget();
				}
				else
				{
					activeChar.sendMessage("Invalid target.");
					return false;
				}
				
				String val = command.substring(29);
				StringTokenizer st = new StringTokenizer(val);
				String id = st.nextToken();
				int idval = Integer.parseInt(id);
				String enchantValue = st.nextToken();
				int enchantval = Integer.parseInt(enchantValue);
				String attType = st.nextToken();
				String attValue = st.nextToken();
				int attVal = Integer.parseInt(attValue);
				createItem(activeChar, target, idval, enchantval, attType, attVal);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Specify a valid number.");
			}
			AdminHtml.showAdminHtml(activeChar, "customitemcreation.htm");
		}
		return true;
	}
	
	private void createItem(L2PcInstance activeChar, L2PcInstance target, int id, int enchantvalue, String attributeType, int attributeValue)
	{
		L2Item template = ItemData.getInstance().getTemplate(id);
		if (template == null)
		{
			activeChar.sendMessage("This item doesn't exist.");
			return;
		}
		
		if ((template.isEnchantable() == 0) || !template.isElementable())
		{
			activeChar.sendMessage("This item is not enchantable or elementable.");
			return;
		}
		
		if ((enchantvalue < 0) || (enchantvalue > SecuritySystemConfigs.MAX_ENCHANT_LEVEL))
		{
			activeChar.sendMessage("Incorrect value, max enchant value " + SecuritySystemConfigs.MAX_ENCHANT_LEVEL + ".");
			return;
		}
		
		if (target.getInventory().getItemByItemId(id) != null)
		{
			activeChar.sendMessage("This item already exists in target's inventory.");
			return;
		}
		
		target.getInventory().addItem("Admin", id, 1, activeChar, null);
		
		if ((enchantvalue > 0) && (enchantvalue <= SecuritySystemConfigs.MAX_ENCHANT_LEVEL))
		{
			target.getInventory().getItemByItemId(id).setEnchantLevel(enchantvalue);
		}
		
		if (!attributeType.equals("NoElement"))
		{
			byte element = Elementals.getElementId(attributeType);
			target.getInventory().getItemByItemId(id).setElementAttr(element, attributeValue);
		}
		
		if (activeChar != target)
		{
			target.sendMessage("Admin spawned " + 1 + " " + template.getName() + " in your inventory.");
		}
		activeChar.sendMessage("You have spawned " + 1 + " " + template.getName() + "(" + id + ") in " + target.getName() + " inventory.");
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
