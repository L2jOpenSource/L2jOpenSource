package com.l2jfrozen.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.ItemContainer;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.PcFreight;
import com.l2jfrozen.gameserver.model.actor.instance.L2FolkInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2EtcItemType;

/**
 * @author -Wooden-
 */
public final class RequestPackageSend extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestPackageSend.class);
	private final List<Item> items = new ArrayList<>();
	private int objectID;
	private int psCount;
	
	@Override
	protected void readImpl()
	{
		objectID = readD();
		psCount = readD();
		
		if (psCount < 0 || psCount > 500)
		{
			psCount = -1;
			return;
		}
		
		for (int i = 0; i < psCount; i++)
		{
			final int id = readD(); // this is some id sent in PackageSendableList
			final int count = readD();
			items.add(new Item(id, count));
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (psCount == -1 || items == null)
		{
			return;
		}
		
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
		{
			return;
		}
		
		if (player.getObjectId() == objectID)
		{
			return;
		}
		
		final L2PcInstance target = L2PcInstance.load(objectID);
		
		if (player.getAccountChars().size() < 1)
		{
			return;
		}
		else if (!player.getAccountChars().containsKey(objectID))
		{
			return;
		}
		
		if (L2World.getInstance().getPlayer(objectID) != null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("deposit"))
		{
			player.sendMessage("You depositing items too fast.");
			return;
		}
		
		final PcFreight freight = target.getFreight();
		player.setActiveWarehouse(freight);
		target.deleteMe();
		final ItemContainer warehouse = player.getActiveWarehouse();
		
		if (warehouse == null)
		{
			return;
		}
		
		final L2FolkInstance manager = player.getLastFolkNPC();
		
		if ((manager == null || !player.isInsideRadius(manager, L2NpcInstance.INTERACTION_DISTANCE, false, false)) && !player.isGM())
		{
			return;
		}
		
		if (warehouse instanceof PcFreight && !player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Unsufficient privileges.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && player.getKarma() > 0)
		{
			return;
		}
		
		// Freight price from config or normal price per item slot (30)
		final int fee = psCount * Config.ALT_GAME_FREIGHT_PRICE;
		int currentAdena = player.getAdena();
		int slots = 0;
		
		for (final Item i : items)
		{
			final int objectId = i.id;
			final int count = i.count;
			
			// Check validity of requested item
			final L2ItemInstance item = player.checkItemManipulation(objectId, count, "deposit");
			
			// Check if item is null
			if (item == null)
			{
				LOGGER.warn("Error depositing a warehouse object for char " + player.getName() + " (validity check)");
				i.id = 0;
				i.count = 0;
				continue;
			}
			
			// Fix exploit for trade Augmented weapon with freight
			if (item.isAugmented())
			{
				LOGGER.warn("Error depositing a warehouse object for char " + player.getName() + " (item is augmented)");
				return;
			}
			
			if (!item.isTradeable() || item.getItemType() == L2EtcItemType.QUEST)
			{
				return;
			}
			
			// Calculate needed adena and slots
			if (item.getItemId() == 57)
			{
				currentAdena -= count;
			}
			
			if (!item.isStackable())
			{
				slots += count;
			}
			else if (warehouse.getItemByItemId(item.getItemId()) == null)
			{
				slots++;
			}
		}
		
		// Item Max Limit Check
		if (!warehouse.validateCapacity(slots))
		{
			sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
			return;
		}
		
		// Check if enough adena and charge the fee
		if (currentAdena < fee || !player.reduceAdena("Warehouse", fee, player.getLastFolkNPC(), false))
		{
			sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			return;
		}
		
		// Proceed to the transfer
		final InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for (final Item i : items)
		{
			final int objectId = i.id;
			final int count = i.count;
			
			// check for an invalid item
			if (objectId == 0 && count == 0)
			{
				continue;
			}
			
			final L2ItemInstance oldItem = player.getInventory().getItemByObjectId(objectId);
			
			if (oldItem == null)
			{
				LOGGER.warn("Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
				continue;
			}
			
			final int itemId = oldItem.getItemId();
			
			if (itemId >= 6611 && itemId <= 6621 || itemId == 6842)
			{
				continue;
			}
			
			final L2ItemInstance newItem = player.getInventory().transferItem("Warehouse", objectId, count, warehouse, player, player.getLastFolkNPC());
			
			if (newItem == null)
			{
				LOGGER.warn("Error depositing a warehouse object for char " + player.getName() + " (newitem == null)");
				continue;
			}
			
			if (playerIU != null)
			{
				if (oldItem.getCount() > 0 && oldItem != newItem)
				{
					playerIU.addModifiedItem(oldItem);
				}
				else
				{
					playerIU.addRemovedItem(oldItem);
				}
			}
		}
		
		// Send updated item list to the player
		if (playerIU != null)
		{
			player.sendPacket(playerIU);
		}
		else
		{
			player.sendPacket(new ItemList(player, false));
		}
		
		// Update current load status on player
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		
		player.setActiveWarehouse(null);
	}
	
	private class Item
	{
		public int id;
		public int count;
		
		public Item(final int i, final int c)
		{
			id = i;
			count = c;
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 9F RequestPackageSend";
	}
}
