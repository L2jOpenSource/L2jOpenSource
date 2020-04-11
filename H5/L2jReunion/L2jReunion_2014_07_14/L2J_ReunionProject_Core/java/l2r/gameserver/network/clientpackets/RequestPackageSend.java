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

import l2r.Config;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.ItemHolder;
import l2r.gameserver.model.itemcontainer.Inventory;
import l2r.gameserver.model.itemcontainer.ItemContainer;
import l2r.gameserver.model.itemcontainer.PcFreight;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.ItemList;
import l2r.gameserver.network.serverpackets.StatusUpdate;
import l2r.gameserver.util.Util;

/**
 * @author -Wooden-
 * @author UnAfraid Thanks mrTJO
 */
public class RequestPackageSend extends L2GameClientPacket
{
	private final String _C_A8_REQUESTPACKAGESEND = "[C] A8 RequestPackageSend";
	private static final int BATCH_LENGTH = 12; // length of the one item
	
	private ItemHolder _items[] = null;
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		
		int count = readD();
		if ((count <= 0) || (count > Config.MAX_ITEM_IN_PACKET) || ((count * BATCH_LENGTH) != _buf.remaining()))
		{
			return;
		}
		
		_items = new ItemHolder[count];
		for (int i = 0; i < count; i++)
		{
			int objId = readD();
			long cnt = readQ();
			if ((objId < 1) || (cnt < 0))
			{
				_items = null;
				return;
			}
			
			_items[i] = new ItemHolder(objId, cnt);
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getActiveChar();
		if ((_items == null) || (player == null) || !player.getAccountChars().containsKey(_objectId))
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("deposit"))
		{
			player.sendMessage("You depositing items too fast.");
			return;
		}
		
		final L2Npc manager = player.getLastFolkNPC();
		if (((manager == null) || !player.isInsideRadius(manager, L2Npc.INTERACTION_DISTANCE, false, false)))
		{
			return;
		}
		
		if (player.getActiveEnchantItemId() != L2PcInstance.ID_NONE)
		{
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to use enchant Exploit!", Config.DEFAULT_PUNISH);
			return;
		}
		
		// get current tradelist if any
		if (player.getActiveTradeList() != null)
		{
			return;
		}
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && (player.getKarma() > 0))
		{
			return;
		}
		
		// Freight price from config per item slot.
		final int fee = _items.length * Config.ALT_FREIGHT_PRICE;
		long currentAdena = player.getAdena();
		int slots = 0;
		
		final ItemContainer warehouse = new PcFreight(_objectId);
		for (ItemHolder i : _items)
		{
			// Check validity of requested item
			final L2ItemInstance item = player.checkItemManipulation(i.getId(), i.getCount(), "freight");
			if (item == null)
			{
				_log.warn("Error depositing a warehouse object for char " + player.getName() + " (validity check)");
				warehouse.deleteMe();
				return;
			}
			else if (!item.isFreightable())
			{
				warehouse.deleteMe();
				return;
			}
			
			// Calculate needed adena and slots
			if (item.getId() == Inventory.ADENA_ID)
			{
				currentAdena -= i.getCount();
			}
			else if (!item.isStackable())
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
			warehouse.deleteMe();
			return;
		}
		
		// Check if enough adena and charge the fee
		if ((currentAdena < fee) || !player.reduceAdena(warehouse.getName(), fee, manager, false))
		{
			player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			warehouse.deleteMe();
			return;
		}
		
		// Proceed to the transfer
		final InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for (ItemHolder i : _items)
		{
			// Check validity of requested item
			final L2ItemInstance oldItem = player.checkItemManipulation(i.getId(), i.getCount(), "deposit");
			if (oldItem == null)
			{
				_log.warn("Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
				warehouse.deleteMe();
				return;
			}
			
			final L2ItemInstance newItem = player.getInventory().transferItem("Trade", i.getId(), i.getCount(), warehouse, player, null);
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
		
		warehouse.deleteMe();
		
		// Send updated item list to the player
		sendPacket(playerIU != null ? playerIU : new ItemList(player, false));
		
		// Update current load status on player
		final StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		sendPacket(su);
	}
	
	@Override
	public String getType()
	{
		return _C_A8_REQUESTPACKAGESEND;
	}
}
