package main.util;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.PcInventory;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2Item;

import main.holders.RewardHolder;
import main.holders.objects.PlayerHolder;

/**
 * @author fissban
 */
public class UtilInventory
{
	
	/**
	 * Check for multiple items in player's inventory.
	 * @param  itemIds a list of item IDs to check for
	 * @return         {@code true} if all items exist in player's inventory, {@code false} otherwise
	 */
	public static boolean hasItems(L2PcInstance player, int... itemIds)
	{
		PcInventory inv = player.getInventory();
		for (int itemId : itemIds)
		{
			if (inv.getItemByItemId(itemId) == null)
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Check for multiple items in player's inventory.
	 * @param  itemIds a list of item IDs to check for
	 * @return         {@code true} if all items exist in player's inventory, {@code false} otherwise
	 */
	public static boolean hasItems(PlayerHolder ph, int... itemIds)
	{
		PcInventory inv = ph.getInstance().getInventory();
		for (int itemId : itemIds)
		{
			if (inv.getItemByItemId(itemId) == null)
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @param  itemId : ID of the item wanted to be count
	 * @return        the quantity of one sort of item hold by the player
	 */
	public static int getItemsCount(PlayerHolder ph, int itemId)
	{
		int count = 0;
		
		for (L2ItemInstance item : ph.getInstance().getInventory().getItems())
		{
			if ((item != null) && (item.getItemId() == itemId))
			{
				count += item.getCount();
			}
		}
		
		return count;
	}
	
	/**
	 * Give items to the player's inventory.
	 * @param itemId    : Identifier of the item.
	 * @param itemCount : Quantity of items to add.
	 */
	public static void giveItems(PlayerHolder ph, RewardHolder... rewards)
	{
		for (RewardHolder r : rewards)
		{
			giveItems(ph, r.getRewardId(), r.getRewardCount(), 0);
		}
	}
	
	/**
	 * Give items to the player's inventory.
	 * @param itemId    : Identifier of the item.
	 * @param itemCount : Quantity of items to add.
	 */
	public static void giveItems(PlayerHolder ph, int itemId, int itemCount)
	{
		giveItems(ph, itemId, itemCount, 0);
	}
	
	/**
	 * Give items to the player's inventory.
	 * @param itemId       : Identifier of the item.
	 * @param itemCount    : Quantity of items to add.
	 * @param enchantLevel : Enchant level of items to add.
	 */
	public static void giveItems(PlayerHolder ph, int itemId, int itemCount, int enchantLevel)
	{
		// Incorrect amount.
		if (itemCount <= 0)
		{
			return;
		}
		
		L2PcInstance player = ph.getInstance().getActingPlayer();
		// Add items to player's inventory.
		L2ItemInstance item = player.getInventory().addItem("Engine", itemId, itemCount, player, player);
		if (item == null)
		{
			return;
		}
		
		// Set enchant level for the item.
		if (enchantLevel > 0)
		{
			item.setEnchantLevel(enchantLevel);
		}
		
		// Send message to the client.
		if (itemId == 57)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ADENA).addNumber(itemCount));
		}
		else
		{
			if (itemCount > 1)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(itemId).addNumber(itemCount));
			}
			else
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM).addItemName(itemId));
			}
		}
		
		player.sendPacket(new ItemList(player, false));
		
		// Send status update packet.
		player.refreshOverloaded();
	}
	
	/**
	 * Remove items from the player's inventory.
	 * @param itemId    : Identifier of the item.
	 * @param itemCount : Quantity of items to destroy.
	 */
	public static boolean takeItems(PlayerHolder ph, int itemId, int itemCount)
	{
		// Find item in player's inventory.
		L2ItemInstance item = ph.getInstance().getInventory().getItemByItemId(itemId);
		if (item == null)
		{
			return false;
		}
		
		// Tests on count value and set correct value if necessary.
		if ((itemCount < 0) || (itemCount > item.getCount()))
		{
			itemCount = item.getCount();
		}
		
		// Disarm item, if equipped.
		if (item.isEquipped())
		{
			ph.getInstance().getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
		}
		
		// Destroy the quantity of items wanted.
		L2ItemInstance val = ph.getInstance().getInventory().destroyItemByItemId("Engine", itemId, itemCount, ph.getInstance(), null);
		
		// Send the ItemList Server->Client Packet to the player in order to refresh its Inventory
		ph.getInstance().sendPacket(new ItemList(ph.getInstance(), true));
		
		// Send refresh inventory
		InventoryUpdate u = new InventoryUpdate();
		u.addItem(item);
		ph.getInstance().sendPacket(u);
		
		ph.getInstance().broadcastUserInfo();
		return val != null;
	}
	
	public static void dropItem(L2PcInstance player, L2Attackable npc, int itemId, int itemCount)
	{
		if (Config.AUTO_LOOT)
		{
			final L2Item itemTemplate = ItemTable.getInstance().getTemplate(itemId);
			
			if (!player.getInventory().validateCapacity(itemTemplate))
			{
				npc.DropItem(player, itemId, itemCount);
			}
			else
			{
				player.addItem("Loot", itemId, itemCount, npc, true);
			}
		}
		else
		{
			npc.DropItem(player, itemId, itemCount);
		}
	}
	
	/**
	 * Returns a dummy (fr = factice) item.<br>
	 * <U><I>Concept :</I></U><br>
	 * Dummy item is created by setting the ID of the object in the world at null value
	 * @param  itemId : int designating the item
	 * @return        L2ItemInstance designating the dummy item created
	 */
	public static L2ItemInstance createDummyItem(int itemId)
	{
		L2Item item = ItemTable.getInstance().getTemplate(itemId);
		if (item == null)
		{
			return null;
		}
		L2ItemInstance temp = new L2ItemInstance(0, item);
		try
		{
			temp = new L2ItemInstance(0, itemId);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			// this can happen if the item templates were not initialized
		}
		
		if (temp.getItem() == null)
		{
			// LOG.warning("ItemTable: Item Template missing for Id: " + itemId);
		}
		
		return temp;
	}
}
