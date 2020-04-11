package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.ClanWarehouse;
import com.l2jfrozen.gameserver.model.ItemContainer;
import com.l2jfrozen.gameserver.model.L2Clan;
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

public final class SendWareHouseWithDrawList extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(SendWareHouseWithDrawList.class);
	
	private int count;
	private int[] items;
	
	@Override
	protected void readImpl()
	{
		count = readD();
		
		if (count < 0 || count * 8 > buf.remaining() || count > Config.MAX_ITEM_IN_PACKET)
		{
			count = 0;
			items = null;
			return;
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
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("withdraw"))
		{
			player.sendMessage("You withdrawing items too fast.");
			return;
		}
		
		// Like L2OFF you can't confirm a withdraw when you are in trade.
		if (player.getActiveTradeList() != null)
		{
			player.sendMessage("You can't withdraw items when you are trading.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
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
		
		if (warehouse instanceof ClanWarehouse && !player.getAccessLevel().allowTransaction())
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
		
		if (Config.ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH)
		{
			if (warehouse instanceof ClanWarehouse && (player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) != L2Clan.CP_CL_VIEW_WAREHOUSE)
			{
				return;
			}
		}
		else
		{
			if (warehouse instanceof ClanWarehouse && !player.isClanLeader())
			{
				// this msg is for depositing but maybe good to send some msg?
				player.sendPacket(new SystemMessage(SystemMessageId.ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE));
				return;
			}
		}
		
		int weight = 0;
		int slots = 0;
		
		for (int i = 0; i < count; i++)
		{
			final int objectId = items[i * 2 + 0];
			final int count = items[i * 2 + 1];
			
			// Calculate needed slots
			final L2ItemInstance item = warehouse.getItemByObjectId(objectId);
			if (item == null)
			{
				continue;
			}
			weight += count * item.getItem().getWeight();
			if (!item.isStackable())
			{
				slots += count;
			}
			else if (player.getInventory().getItemByItemId(item.getItemId()) == null)
			{
				slots++;
			}
		}
		
		// Item Max Limit Check
		if (!player.getInventory().validateCapacity(slots))
		{
			sendPacket(new SystemMessage(SystemMessageId.SLOTS_FULL));
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
		
		// Weight limit Check
		if (!player.getInventory().validateWeight(weight))
		{
			sendPacket(new SystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
			return;
		}
		
		// Proceed to the transfer
		final InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for (int i = 0; i < count; i++)
		{
			final int objectId = items[i * 2 + 0];
			final int count = items[i * 2 + 1];
			
			final L2ItemInstance oldItem = warehouse.getItemByObjectId(objectId);
			if (oldItem == null || oldItem.getCount() < count)
			{
				player.sendMessage("Can't withdraw requested item" + (count > 1 ? "s" : ""));
			}
			final L2ItemInstance newItem = warehouse.transferItem("Warehouse", objectId, count, player.getInventory(), player, player.getLastFolkNPC());
			if (newItem == null)
			{
				LOGGER.warn("Error withdrawing a warehouse object for char " + player.getName());
				continue;
			}
			
			if (playerIU != null)
			{
				if (newItem.getCount() > count)
				{
					playerIU.addModifiedItem(newItem);
				}
				else
				{
					playerIU.addNewItem(newItem);
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
		return "[C] 32 SendWareHouseWithDrawList";
	}
}
