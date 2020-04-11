package com.l2jfrozen.gameserver.network.clientpackets;

import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.controllers.TradeController;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2TradeList;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2MercManagerInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2MerchantInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.util.Util;

public final class RequestWearItem extends L2GameClientPacket
{
	protected static final Logger LOGGER = Logger.getLogger(RequestWearItem.class);
	
	protected Future<?> removeWearItemsTask;
	
	@SuppressWarnings("unused")
	private int unknow;
	
	/** List of ItemID to Wear */
	private int listId;
	
	/** Number of Item to Wear */
	private int count;
	
	/** Table of ItemId containing all Item to Wear */
	private int[] items;
	
	/** Player that request a Try on */
	protected L2PcInstance activeChar;
	
	class RemoveWearItemsTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				activeChar.destroyWearedItems("Wear", null, true);
			}
			catch (final Throwable e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				LOGGER.error("", e);
			}
		}
	}
	
	/**
	 * Decrypt the RequestWearItem Client->Server Packet and Create items table containing all ItemID to Wear.<BR>
	 * <BR>
	 */
	@Override
	protected void readImpl()
	{
		// Read and Decrypt the RequestWearItem Client->Server Packet
		activeChar = getClient().getActiveChar();
		unknow = readD();
		listId = readD(); // List of ItemID to Wear
		count = readD(); // Number of Item to Wear
		
		if (count < 0)
		{
			count = 0;
		}
		
		if (count > 100)
		{
			count = 0; // prevent too long lists
		}
		
		// Create items table that will contain all ItemID to Wear
		items = new int[count];
		
		// Fill items table with all ItemID to Wear
		for (int i = 0; i < count; i++)
		{
			final int itemId = readD();
			items[i] = itemId;
		}
	}
	
	/**
	 * Launch Wear action.<BR>
	 * <BR>
	 */
	@Override
	protected void runImpl()
	{
		// Get the current player and return if null
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (!Config.ALLOW_WEAR)
		{
			player.sendMessage("Item wear is disabled");
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id.", Config.DEFAULT_PUNISH);
		}
		
		// If Alternate rule Karma punishment is set to true, forbid Wear to player with Karma
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0)
		{
			return;
		}
		
		// Check current target of the player and the INTERACTION_DISTANCE
		final L2Object target = player.getTarget();
		if (!player.isGM() && (target == null // No target (ie GM Shop)
			|| !(target instanceof L2MerchantInstance || target instanceof L2MercManagerInstance) // Target not a merchant and not mercmanager
			|| !player.isInsideRadius(target, L2NpcInstance.INTERACTION_DISTANCE, false, false)))
		{
			return; // Distance is too far
		}
		
		L2TradeList list = null;
		
		// Get the current merchant targeted by the player
		final L2MerchantInstance merchant = target != null && target instanceof L2MerchantInstance ? (L2MerchantInstance) target : null;
		if (merchant == null)
		{
			LOGGER.warn("Null merchant!");
			return;
		}
		
		final List<L2TradeList> lists = TradeController.getInstance().getBuyListByNpcId(merchant.getNpcId());
		if (lists == null)
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id.", Config.DEFAULT_PUNISH);
			return;
		}
		
		for (final L2TradeList tradeList : lists)
		{
			if (tradeList.getListId() == listId)
			{
				list = tradeList;
			}
		}
		
		if (list == null)
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id.", Config.DEFAULT_PUNISH);
			return;
		}
		
		listId = list.getListId();
		
		// Check if the quantity of Item to Wear
		if (count < 1 || listId >= 1000000)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Total Price of the Try On
		long totalPrice = 0;
		
		// Check for buylist validity and calculates summary values
		int slots = 0;
		int weight = 0;
		
		for (int i = 0; i < count; i++)
		{
			final int itemId = items[i];
			
			if (!list.containsItemId(itemId))
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id.", Config.DEFAULT_PUNISH);
				return;
			}
			
			final L2Item template = ItemTable.getInstance().getTemplate(itemId);
			weight += template.getWeight();
			slots++;
			
			totalPrice += Config.WEAR_PRICE;
			if (totalPrice > Integer.MAX_VALUE)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " adena worth of goods.", Config.DEFAULT_PUNISH);
				return;
			}
		}
		
		// Check the weight
		if (!player.getInventory().validateWeight(weight))
		{
			sendPacket(new SystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
			return;
		}
		
		// Check the inventory capacity
		if (!player.getInventory().validateCapacity(slots))
		{
			sendPacket(new SystemMessage(SystemMessageId.SLOTS_FULL));
			return;
		}
		
		// Charge buyer and add tax to castle treasury if not owned by npc clan because a Try On is not Free
		if (totalPrice < 0 || !player.reduceAdena("Wear", (int) totalPrice, player.getLastFolkNPC(), false))
		{
			sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			return;
		}
		
		// Proceed the wear
		final InventoryUpdate playerIU = new InventoryUpdate();
		for (int i = 0; i < count; i++)
		{
			final int itemId = items[i];
			
			if (!list.containsItemId(itemId))
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id.", Config.DEFAULT_PUNISH);
				return;
			}
			
			// If player doesn't own this item : Add this L2ItemInstance to Inventory and set properties lastchanged to ADDED and wear to True
			// If player already own this item : Return its L2ItemInstance (will not be destroy because property wear set to False)
			final L2ItemInstance item = player.getInventory().addWearItem("Wear", itemId, player, merchant);
			
			// Equip player with this item (set its location)
			player.getInventory().equipItemAndRecord(item);
			
			// Add this Item in the InventoryUpdate Server->Client Packet
			playerIU.addItem(item);
		}
		
		// Send the InventoryUpdate Server->Client Packet to the player
		// Add Items in player inventory and equip them
		player.sendPacket(playerIU);
		
		// Send the StatusUpdate Server->Client Packet to the player with new CUR_LOAD (0x0e) information
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		
		// Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its knownPlayers
		player.broadcastUserInfo();
		
		// All weared items should be removed in ALLOW_WEAR_DELAY sec.
		if (removeWearItemsTask == null)
		{
			removeWearItemsTask = ThreadPoolManager.getInstance().scheduleGeneral(new RemoveWearItemsTask(), Config.WEAR_DELAY * 1000);
		}
		
	}
	
	@Override
	public String getType()
	{
		return "[C] C6 RequestWearItem";
	}
}
