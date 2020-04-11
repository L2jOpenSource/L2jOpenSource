package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.managers.CursedWeaponsManager;
import com.l2jfrozen.gameserver.model.ClanWarehouse;
import com.l2jfrozen.gameserver.model.ItemContainer;
import com.l2jfrozen.gameserver.model.actor.instance.L2FolkInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.EnchantResult;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2EtcItemType;

public final class SendWareHouseDepositList extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(SendWareHouseDepositList.class);
	
	private int count;
	private int[] items;
	
	@Override
	protected void readImpl()
	{
		count = readD();
		
		// check packet list size
		if (count < 0 || count * 8 > buf.remaining() || count > Config.MAX_ITEM_IN_PACKET)
		{
			count = 0;
		}
		
		items = new int[count * 2];
		for (int i = 0; i < count; i++)
		{
			final int objectId = readD();
			items[i * 2 + 0] = objectId;
			final long cnt = readD();
			
			if (cnt > Integer.MAX_VALUE || cnt < 0)
			{
				count = 0;
				items = null;
				return;
			}
			
			items[i * 2 + 1] = (int) cnt;
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (items == null)
		{
			return;
		}
		
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final ItemContainer warehouse = player.getActiveWarehouse();
		if (warehouse == null)
		{
			return;
		}
		
		final L2FolkInstance manager = player.getLastFolkNPC();
		
		if (manager == null || !player.isInsideRadius(manager, L2NpcInstance.INTERACTION_DISTANCE, false, false))
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("deposit"))
		{
			player.sendMessage("You depositing items too fast.");
			return;
		}
		
		if (player.getPrivateStoreType() != 0)
		{
			player.sendMessage("You can't deposit items when you are trading.");
			return;
		}
		
		// Like L2OFF you can't confirm a deposit when you are in trade.
		if (player.getActiveTradeList() != null)
		{
			player.sendMessage("You can't deposit items when you are trading.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isCastingNow() || player.isCastingPotionNow())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (warehouse instanceof ClanWarehouse && !player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Unsufficient privileges.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isDead())
		{
			player.sendMessage("You can't deposit items while you are dead.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && player.getKarma() > 0)
		{
			return;
		}
		
		// Like L2OFF enchant window must close
		if (player.getActiveEnchantItem() != null)
		{
			sendPacket(new SystemMessage(SystemMessageId.ENCHANT_SCROLL_CANCELLED));
			player.sendPacket(new EnchantResult(0));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Freight price from config or normal price per item slot (30)
		final int fee = count * 30;
		int currentAdena = player.getAdena();
		int slots = 0;
		
		for (int i = 0; i < count; i++)
		{
			final int objectId = items[i * 2 + 0];
			final int count = items[i * 2 + 1];
			
			// Check validity of requested item
			final L2ItemInstance item = player.checkItemManipulation(objectId, count, "deposit");
			if (item == null)
			{
				LOGGER.warn("Error depositing a warehouse object for char " + player.getName() + " (validity check)");
				items[i * 2 + 0] = 0;
				items[i * 2 + 1] = 0;
				continue;
			}
			
			if (warehouse instanceof ClanWarehouse && !item.isTradeable() || item.getItemType() == L2EtcItemType.QUEST)
			{
				return;
			}
			
			// Calculate needed adena and slots
			if (item.getItemId() == 57)
			{
				currentAdena -= count;
				
				// Max adena limit for warehouse (Tested for normal and clan warehouse)
				if (warehouse.getItemByItemId(57) != null)
				{
					long adenaInWarehouse = warehouse.getItemByItemId(57).getCount();
					long adenaDeposit = count;
					long totalAdena = adenaInWarehouse + adenaDeposit;
					if (totalAdena >= Integer.MAX_VALUE)
					{
						player.sendMessage("The maximum limit of adena in the warehouse is " + Integer.MAX_VALUE);
						player.sendMessage("You can only deposit " + (Integer.MAX_VALUE - adenaInWarehouse));
						return;
					}
				}
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
		for (int i = 0; i < count; i++)
		{
			final int objectId = items[i * 2 + 0];
			final int count = items[i * 2 + 1];
			
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
			
			if (CursedWeaponsManager.getInstance().isCursed(itemId))
			{
				LOGGER.warn(player.getName() + " try to deposit Cursed Weapon on wherehouse.");
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
	}
	
	@Override
	public String getType()
	{
		return "[C] 31 SendWareHouseDepositList";
	}
}
