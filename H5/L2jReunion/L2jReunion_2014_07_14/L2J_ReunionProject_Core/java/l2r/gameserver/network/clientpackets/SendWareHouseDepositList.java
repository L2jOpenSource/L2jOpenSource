/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.network.clientpackets;

import static l2r.gameserver.model.itemcontainer.Inventory.ADENA_ID;

import java.util.ArrayList;
import java.util.List;

import l2r.Config;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.ItemHolder;
import l2r.gameserver.model.itemcontainer.ItemContainer;
import l2r.gameserver.model.itemcontainer.PcWarehouse;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.ItemList;
import l2r.gameserver.network.serverpackets.StatusUpdate;
import l2r.gameserver.util.Util;

/**
 * SendWareHouseDepositList client packet class.
 */
public final class SendWareHouseDepositList extends L2GameClientPacket
{
	private static final String _C__3B_SENDWAREHOUSEDEPOSITLIST = "[C] 3B SendWareHouseDepositList";
	
	private static final int BATCH_LENGTH = 12;
	
	private List<ItemHolder> _items = null;
	
	@Override
	protected void readImpl()
	{
		final int size = readD();
		if ((size <= 0) || (size > Config.MAX_ITEM_IN_PACKET) || ((size * BATCH_LENGTH) != _buf.remaining()))
		{
			return;
		}
		
		_items = new ArrayList<>(size);
		for (int i = 0; i < size; i++)
		{
			int objId = readD();
			long count = readQ();
			if ((objId < 1) || (count < 0))
			{
				_items = null;
				return;
			}
			_items.add(new ItemHolder(objId, count));
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (_items == null)
		{
			return;
		}
		
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("deposit"))
		{
			player.sendMessage("You are depositing items too fast.");
			return;
		}
		
		final ItemContainer warehouse = player.getActiveWarehouse();
		if (warehouse == null)
		{
			return;
		}
		final boolean isPrivate = warehouse instanceof PcWarehouse;
		
		final L2Npc manager = player.getLastFolkNPC();
		if (((manager == null) || !manager.isWarehouse() || !manager.canInteract(player)) && !player.isGM() && !player.isUsingAioWh())
		{
			return;
		}
		
		if (!isPrivate && !player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disabled for your Access Level.");
			return;
		}
		
		if (player.getActiveEnchantItemId() != L2PcInstance.ID_NONE)
		{
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to use enchant Exploit!", Config.DEFAULT_PUNISH);
			return;
		}
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && (player.getKarma() > 0))
		{
			return;
		}
		
		// Freight price from config or normal price per item slot (30)
		final long fee = _items.size() * 30;
		long currentAdena = player.getAdena();
		int slots = 0;
		
		for (ItemHolder i : _items)
		{
			L2ItemInstance item = player.checkItemManipulation(i.getId(), i.getCount(), "deposit");
			if (item == null)
			{
				_log.warn("Error depositing a warehouse object for char " + player.getName() + " (validity check)");
				return;
			}
			
			// Calculate needed adena and slots
			if (item.getId() == ADENA_ID)
			{
				currentAdena -= i.getCount();
			}
			if (!item.isStackable())
			{
				slots += i.getCount();
			}
			else if (warehouse.getItemByItemId(item.getId()) == null)
			{
				slots++;
			}
		}
		
		// Item Max Limit Check
		if (!warehouse.validateCapacity(slots))
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		
		// Check if enough adena and charge the fee
		if ((currentAdena < fee) || !player.reduceAdena(warehouse.getName(), fee, manager, false))
		{
			player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			return;
		}
		
		// get current tradelist if any
		if (player.getActiveTradeList() != null)
		{
			return;
		}
		
		// Proceed to the transfer
		InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for (ItemHolder i : _items)
		{
			// Check validity of requested item
			L2ItemInstance oldItem = player.checkItemManipulation(i.getId(), i.getCount(), "deposit");
			if (oldItem == null)
			{
				_log.warn("Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
				return;
			}
			
			if (!oldItem.isDepositable(isPrivate) || !oldItem.isAvailable(player, true, isPrivate))
			{
				continue;
			}
			
			final L2ItemInstance newItem = player.getInventory().transferItem(warehouse.getName(), i.getId(), i.getCount(), warehouse, player, manager);
			if (newItem == null)
			{
				_log.warn("Error depositing a warehouse object for char " + player.getName() + " (newitem == null)");
				continue;
			}
			
			if (playerIU != null)
			{
				if ((oldItem.getCount() > 0) && (oldItem != newItem))
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
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}
	
	@Override
	public String getType()
	{
		return _C__3B_SENDWAREHOUSEDEPOSITLIST;
	}
}
