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
package l2r.gameserver.model.itemcontainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javolution.util.FastList;
import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.datatables.xml.ItemData;
import l2r.gameserver.enums.ItemLocation;
import l2r.gameserver.model.TradeItem;
import l2r.gameserver.model.TradeList;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.items.type.EtcItemType;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.ItemList;
import l2r.gameserver.network.serverpackets.StatusUpdate;
import l2r.gameserver.scripting.scriptengine.events.AddToInventoryEvent;
import l2r.gameserver.scripting.scriptengine.events.ItemDestroyEvent;
import l2r.gameserver.scripting.scriptengine.events.ItemDropEvent;
import l2r.gameserver.scripting.scriptengine.events.ItemTransferEvent;
import l2r.gameserver.scripting.scriptengine.listeners.player.ItemTracker;
import l2r.gameserver.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PcInventory extends Inventory
{
	private static final Logger _log = LoggerFactory.getLogger(PcInventory.class);
	
	private final L2PcInstance _owner;
	private L2ItemInstance _adena;
	private L2ItemInstance _fAdena;
	private L2ItemInstance _ancientAdena;
	
	private int[] _blockItems = null;
	
	private int _questSlots;
	
	private final Object _lock;
	/**
	 * Block modes:
	 * <UL>
	 * <LI>-1 - no block
	 * <LI>0 - block items from _invItems, allow usage of other items
	 * <LI>1 - allow usage of items from _invItems, block other items
	 * </UL>
	 */
	private int _blockMode = -1;
	
	private static FastList<ItemTracker> itemTrackers = new FastList<ItemTracker>().shared();
	
	public PcInventory(L2PcInstance owner)
	{
		_owner = owner;
		_lock = new Object();
	}
	
	@Override
	public L2PcInstance getOwner()
	{
		return _owner;
	}
	
	@Override
	protected ItemLocation getBaseLocation()
	{
		return ItemLocation.INVENTORY;
	}
	
	@Override
	protected ItemLocation getEquipLocation()
	{
		return ItemLocation.PAPERDOLL;
	}
	
	public L2ItemInstance getAdenaInstance()
	{
		return _adena;
	}
	
	public L2ItemInstance getFAdenaInstance()
	{
		return _fAdena;
	}
	
	@Override
	public long getAdena()
	{
		return _adena != null ? _adena.getCount() : 0;
	}
	
	@Override
	public long getFAdena()
	{
		return _fAdena != null ? _fAdena.getCount() : 0;
	}
	
	public L2ItemInstance getAncientAdenaInstance()
	{
		return _ancientAdena;
	}
	
	public long getAncientAdena()
	{
		return (_ancientAdena != null) ? _ancientAdena.getCount() : 0;
	}
	
	/**
	 * Returns the list of items in inventory available for transaction
	 * @param allowAdena
	 * @param allowAncientAdena
	 * @return L2ItemInstance : items in inventory
	 */
	public L2ItemInstance[] getUniqueItems(boolean allowAdena, boolean allowAncientAdena)
	{
		return getUniqueItems(allowAdena, allowAncientAdena, true);
	}
	
	public L2ItemInstance[] getUniqueItems(boolean allowAdena, boolean allowAncientAdena, boolean onlyAvailable)
	{
		FastList<L2ItemInstance> list = FastList.newInstance();
		for (L2ItemInstance item : _items)
		{
			if (item == null)
			{
				continue;
			}
			if ((!allowAdena && (item.getId() == ADENA_ID)))
			{
				continue;
			}
			if ((!allowAncientAdena && (item.getId() == ANCIENT_ADENA_ID)))
			{
				continue;
			}
			boolean isDuplicate = false;
			for (L2ItemInstance litem : list)
			{
				if (litem.getId() == item.getId())
				{
					isDuplicate = true;
					break;
				}
			}
			if (!isDuplicate && (!onlyAvailable || (item.isSellable() && item.isAvailable(getOwner(), false, false))))
			{
				list.add(item);
			}
		}
		
		L2ItemInstance[] result = list.toArray(new L2ItemInstance[list.size()]);
		FastList.recycle(list);
		
		return result;
	}
	
	/**
	 * Returns the list of items in inventory available for transaction Allows an item to appear twice if and only if there is a difference in enchantment level.
	 * @param allowAdena
	 * @param allowAncientAdena
	 * @return L2ItemInstance : items in inventory
	 */
	public L2ItemInstance[] getUniqueItemsByEnchantLevel(boolean allowAdena, boolean allowAncientAdena)
	{
		return getUniqueItemsByEnchantLevel(allowAdena, allowAncientAdena, true);
	}
	
	public L2ItemInstance[] getUniqueItemsByEnchantLevel(boolean allowAdena, boolean allowAncientAdena, boolean onlyAvailable)
	{
		FastList<L2ItemInstance> list = FastList.newInstance();
		for (L2ItemInstance item : _items)
		{
			if (item == null)
			{
				continue;
			}
			if ((!allowAdena && (item.getId() == ADENA_ID)))
			{
				continue;
			}
			if ((!allowAncientAdena && (item.getId() == ANCIENT_ADENA_ID)))
			{
				continue;
			}
			
			boolean isDuplicate = false;
			for (L2ItemInstance litem : list)
			{
				if ((litem.getId() == item.getId()) && (litem.getEnchantLevel() == item.getEnchantLevel()))
				{
					isDuplicate = true;
					break;
				}
			}
			
			if (!isDuplicate && (!onlyAvailable || (item.isSellable() && item.isAvailable(getOwner(), false, false))))
			{
				list.add(item);
			}
		}
		
		L2ItemInstance[] result = list.toArray(new L2ItemInstance[list.size()]);
		FastList.recycle(list);
		
		return result;
	}
	
	/**
	 * @param itemId
	 * @return
	 */
	public L2ItemInstance[] getAllItemsByItemId(int itemId)
	{
		return getAllItemsByItemId(itemId, true);
	}
	
	/**
	 * Returns the list of all items in inventory that have a given item id.
	 * @param itemId : ID of item
	 * @param includeEquipped : include equipped items
	 * @return L2ItemInstance[] : matching items from inventory
	 */
	public L2ItemInstance[] getAllItemsByItemId(int itemId, boolean includeEquipped)
	{
		FastList<L2ItemInstance> list = FastList.newInstance();
		for (L2ItemInstance item : _items)
		{
			if (item == null)
			{
				continue;
			}
			
			if ((item.getId() == itemId) && (includeEquipped || !item.isEquipped()))
			{
				list.add(item);
			}
		}
		
		L2ItemInstance[] result = list.toArray(new L2ItemInstance[list.size()]);
		FastList.recycle(list);
		
		return result;
	}
	
	/**
	 * @param itemId
	 * @param enchantment
	 * @return
	 */
	public L2ItemInstance[] getAllItemsByItemId(int itemId, int enchantment)
	{
		return getAllItemsByItemId(itemId, enchantment, true);
	}
	
	/**
	 * Returns the list of all items in inventory that have a given item id AND a given enchantment level.
	 * @param itemId : ID of item
	 * @param enchantment : enchant level of item
	 * @param includeEquipped : include equipped items
	 * @return L2ItemInstance[] : matching items from inventory
	 */
	public L2ItemInstance[] getAllItemsByItemId(int itemId, int enchantment, boolean includeEquipped)
	{
		FastList<L2ItemInstance> list = FastList.newInstance();
		for (L2ItemInstance item : _items)
		{
			if (item == null)
			{
				continue;
			}
			
			if ((item.getId() == itemId) && (item.getEnchantLevel() == enchantment) && (includeEquipped || !item.isEquipped()))
			{
				list.add(item);
			}
		}
		
		L2ItemInstance[] result = list.toArray(new L2ItemInstance[list.size()]);
		FastList.recycle(list);
		
		return result;
	}
	
	/**
	 * @param allowAdena
	 * @param allowNonTradeable
	 * @param feightable
	 * @return the list of items in inventory available for transaction
	 */
	public L2ItemInstance[] getAvailableItems(boolean allowAdena, boolean allowNonTradeable, boolean feightable)
	{
		FastList<L2ItemInstance> list = FastList.newInstance();
		for (L2ItemInstance item : _items)
		{
			if ((item == null) || !item.isAvailable(getOwner(), allowAdena, allowNonTradeable) || !canManipulateWithItemId(item.getId()))
			{
				continue;
			}
			else if (feightable)
			{
				if ((item.getItemLocation() == ItemLocation.INVENTORY) && item.isFreightable())
				{
					list.add(item);
				}
			}
			else
			{
				list.add(item);
			}
		}
		
		L2ItemInstance[] result = list.toArray(new L2ItemInstance[list.size()]);
		FastList.recycle(list);
		
		return result;
	}
	
	/**
	 * Get all augmented items
	 * @return
	 */
	public L2ItemInstance[] getAugmentedItems()
	{
		FastList<L2ItemInstance> list = FastList.newInstance();
		for (L2ItemInstance item : _items)
		{
			if ((item != null) && item.isAugmented())
			{
				list.add(item);
			}
		}
		
		L2ItemInstance[] result = list.toArray(new L2ItemInstance[list.size()]);
		FastList.recycle(list);
		
		return result;
	}
	
	/**
	 * Get all element items
	 * @return
	 */
	public L2ItemInstance[] getElementItems()
	{
		FastList<L2ItemInstance> list = FastList.newInstance();
		for (L2ItemInstance item : _items)
		{
			if ((item != null) && (item.getElementals() != null))
			{
				list.add(item);
			}
		}
		
		L2ItemInstance[] result = list.toArray(new L2ItemInstance[list.size()]);
		FastList.recycle(list);
		
		return result;
	}
	
	/**
	 * Returns the list of items in inventory available for transaction adjusted by tradeList
	 * @param tradeList
	 * @return L2ItemInstance : items in inventory
	 */
	public TradeItem[] getAvailableItems(TradeList tradeList)
	{
		FastList<TradeItem> list = FastList.newInstance();
		for (L2ItemInstance item : _items)
		{
			if ((item != null) && item.isAvailable(getOwner(), false, false))
			{
				TradeItem adjItem = tradeList.adjustAvailableItem(item);
				if (adjItem != null)
				{
					list.add(adjItem);
				}
			}
		}
		
		TradeItem[] result = list.toArray(new TradeItem[list.size()]);
		FastList.recycle(list);
		
		return result;
	}
	
	/**
	 * Adjust TradeItem according his status in inventory
	 * @param item : L2ItemInstance to be adjusted
	 */
	public void adjustAvailableItem(TradeItem item)
	{
		boolean notAllEquipped = false;
		for (L2ItemInstance adjItem : getItemsByItemId(item.getItem().getId()))
		{
			if (adjItem.isEquipable())
			{
				if (!adjItem.isEquipped())
				{
					notAllEquipped |= true;
				}
			}
			else
			{
				notAllEquipped |= true;
				break;
			}
		}
		if (notAllEquipped)
		{
			L2ItemInstance adjItem = getItemByItemId(item.getItem().getId());
			item.setObjectId(adjItem.getObjectId());
			item.setEnchant(adjItem.getEnchantLevel());
			
			if (adjItem.getCount() < item.getCount())
			{
				item.setCount(adjItem.getCount());
			}
			
			return;
		}
		
		item.setCount(0);
	}
	
	/**
	 * Adds adena to PCInventory
	 * @param process : String Identifier of process triggering this action
	 * @param count : int Quantity of adena to be added
	 * @param actor : L2PcInstance Player requesting the item add
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void addAdena(String process, long count, L2PcInstance actor, Object reference)
	{
		if (count > 0)
		{
			addItem(process, ADENA_ID, count, actor, reference);
		}
	}
	
	public void addFAdena(String process, long count, L2PcInstance actor, Object reference)
	{
		if (count > 0)
		{
			addItem(process, FADENA_ID, count, actor, reference);
		}
	}
	
	/**
	 * Removes adena to PCInventory
	 * @param process : String Identifier of process triggering this action
	 * @param count : int Quantity of adena to be removed
	 * @param actor : L2PcInstance Player requesting the item add
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return boolean : true if adena was reduced
	 */
	public boolean reduceAdena(String process, long count, L2PcInstance actor, Object reference)
	{
		if (count > 0)
		{
			return destroyItemByItemId(process, ADENA_ID, count, actor, reference) != null;
		}
		return false;
	}
	
	public boolean reduceFAdena(String process, long count, L2PcInstance actor, Object reference)
	{
		if (count > 0)
		{
			return destroyItemByItemId(process, FADENA_ID, count, actor, reference) != null;
		}
		return false;
	}
	
	/**
	 * Adds specified amount of ancient adena to player inventory.
	 * @param process : String Identifier of process triggering this action
	 * @param count : int Quantity of adena to be added
	 * @param actor : L2PcInstance Player requesting the item add
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void addAncientAdena(String process, long count, L2PcInstance actor, Object reference)
	{
		if (count > 0)
		{
			addItem(process, ANCIENT_ADENA_ID, count, actor, reference);
		}
	}
	
	/**
	 * Removes specified amount of ancient adena from player inventory.
	 * @param process : String Identifier of process triggering this action
	 * @param count : int Quantity of adena to be removed
	 * @param actor : L2PcInstance Player requesting the item add
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return boolean : true if adena was reduced
	 */
	public boolean reduceAncientAdena(String process, long count, L2PcInstance actor, Object reference)
	{
		if (count > 0)
		{
			return destroyItemByItemId(process, ANCIENT_ADENA_ID, count, actor, reference) != null;
		}
		return false;
	}
	
	/**
	 * Adds item in inventory and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be added
	 * @param actor : L2PcInstance Player requesting the item add
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the new item or the updated item in inventory
	 */
	@Override
	public L2ItemInstance addItem(String process, L2ItemInstance item, L2PcInstance actor, Object reference)
	{
		item = super.addItem(process, item, actor, reference);
		
		if ((item != null) && (item.getId() == ADENA_ID) && !item.equals(_adena))
		{
			_adena = item;
		}
		
		if ((item != null) && (item.getId() == FADENA_ID) && !item.equals(_fAdena))
		{
			_fAdena = item;
		}
		
		if ((item != null) && (item.getId() == ANCIENT_ADENA_ID) && !item.equals(_ancientAdena))
		{
			_ancientAdena = item;
		}
		
		fireTrackerEvents(TrackerEvent.ADD_TO_INVENTORY, actor, item, null);
		return item;
	}
	
	/**
	 * Adds item in inventory and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item Identifier of the item to be added
	 * @param count : int Quantity of items to be added
	 * @param actor : L2PcInstance Player requesting the item creation
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the new item or the updated item in inventory
	 */
	@Override
	public L2ItemInstance addItem(String process, int itemId, long count, L2PcInstance actor, Object reference)
	{
		L2ItemInstance item = super.addItem(process, itemId, count, actor, reference);
		
		if ((item != null) && (item.getId() == ADENA_ID) && !item.equals(_adena))
		{
			_adena = item;
		}
		
		if ((item != null) && (item.getId() == FADENA_ID) && !item.equals(_fAdena))
		{
			_fAdena = item;
		}
		
		if ((item != null) && (item.getId() == ANCIENT_ADENA_ID) && !item.equals(_ancientAdena))
		{
			_ancientAdena = item;
		}
		if ((item != null) && (actor != null))
		{
			// Send inventory update packet
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate playerIU = new InventoryUpdate();
				playerIU.addItem(item);
				actor.sendPacket(playerIU);
			}
			else
			{
				actor.sendPacket(new ItemList(actor, false));
			}
			
			// Update current load as well
			StatusUpdate su = new StatusUpdate(actor);
			su.addAttribute(StatusUpdate.CUR_LOAD, actor.getCurrentLoad());
			actor.sendPacket(su);
			
			fireTrackerEvents(TrackerEvent.ADD_TO_INVENTORY, actor, item, null);
		}
		
		return item;
	}
	
	/**
	 * Transfers item to another inventory and checks _adena and _ancientAdena
	 * @param process string Identifier of process triggering this action
	 * @param objectId Item Identifier of the item to be transfered
	 * @param count Quantity of items to be transfered
	 * @param target the item container for the item to be transfered.
	 * @param actor the player requesting the item transfer
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the new item or the updated item in inventory
	 */
	@Override
	public L2ItemInstance transferItem(String process, int objectId, long count, ItemContainer target, L2PcInstance actor, Object reference)
	{
		L2ItemInstance item = super.transferItem(process, objectId, count, target, actor, reference);
		
		if ((_adena != null) && ((_adena.getCount() <= 0) || (_adena.getOwnerId() != getOwnerId())))
		{
			_adena = null;
		}
		
		if ((_fAdena != null) && ((_fAdena.getCount() <= 0) || (_fAdena.getOwnerId() != getOwnerId())))
		{
			_fAdena = null;
		}
		
		if ((_ancientAdena != null) && ((_ancientAdena.getCount() <= 0) || (_ancientAdena.getOwnerId() != getOwnerId())))
		{
			_ancientAdena = null;
		}
		
		fireTrackerEvents(TrackerEvent.TRANSFER, actor, item, target);
		return item;
	}
	
	/**
	 * Destroy item from inventory and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be destroyed
	 * @param actor : L2PcInstance Player requesting the item destroy
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the destroyed item or the updated item in inventory
	 */
	@Override
	public L2ItemInstance destroyItem(String process, L2ItemInstance item, L2PcInstance actor, Object reference)
	{
		return this.destroyItem(process, item, item.getCount(), actor, reference);
	}
	
	/**
	 * Destroy item from inventory and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be destroyed
	 * @param actor : L2PcInstance Player requesting the item destroy
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the destroyed item or the updated item in inventory
	 */
	@Override
	public L2ItemInstance destroyItem(String process, L2ItemInstance item, long count, L2PcInstance actor, Object reference)
	{
		item = super.destroyItem(process, item, count, actor, reference);
		
		if ((_adena != null) && (_adena.getCount() <= 0))
		{
			_adena = null;
		}
		
		if ((_fAdena != null) && (_fAdena.getCount() <= 0))
		{
			_fAdena = null;
		}
		
		if ((_ancientAdena != null) && (_ancientAdena.getCount() <= 0))
		{
			_ancientAdena = null;
		}
		
		fireTrackerEvents(TrackerEvent.DESTROY, actor, item, null);
		
		return item;
	}
	
	/**
	 * Destroys item from inventory and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be destroyed
	 * @param count : int Quantity of items to be destroyed
	 * @param actor : L2PcInstance Player requesting the item destroy
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the destroyed item or the updated item in inventory
	 */
	@Override
	public L2ItemInstance destroyItem(String process, int objectId, long count, L2PcInstance actor, Object reference)
	{
		L2ItemInstance item = getItemByObjectId(objectId);
		if (item == null)
		{
			return null;
		}
		return this.destroyItem(process, item, count, actor, reference);
	}
	
	/**
	 * Destroy item from inventory by using its <B>itemId</B> and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item identifier of the item to be destroyed
	 * @param count : int Quantity of items to be destroyed
	 * @param actor : L2PcInstance Player requesting the item destroy
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the destroyed item or the updated item in inventory
	 */
	@Override
	public L2ItemInstance destroyItemByItemId(String process, int itemId, long count, L2PcInstance actor, Object reference)
	{
		L2ItemInstance item = getItemByItemId(itemId);
		if (item == null)
		{
			return null;
		}
		return this.destroyItem(process, item, count, actor, reference);
	}
	
	/**
	 * Drop item from inventory and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be dropped
	 * @param actor : L2PcInstance Player requesting the item drop
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the destroyed item or the updated item in inventory
	 */
	@Override
	public L2ItemInstance dropItem(String process, L2ItemInstance item, L2PcInstance actor, Object reference)
	{
		item = super.dropItem(process, item, actor, reference);
		
		if ((_adena != null) && ((_adena.getCount() <= 0) || (_adena.getOwnerId() != getOwnerId())))
		{
			_adena = null;
		}
		
		if ((_fAdena != null) && ((_fAdena.getCount() <= 0) || (_fAdena.getOwnerId() != getOwnerId())))
		{
			_fAdena = null;
		}
		
		if ((_ancientAdena != null) && ((_ancientAdena.getCount() <= 0) || (_ancientAdena.getOwnerId() != getOwnerId())))
		{
			_ancientAdena = null;
		}
		
		fireTrackerEvents(TrackerEvent.DROP, actor, item, null);
		return item;
	}
	
	/**
	 * Drop item from inventory by using its <B>objectID</B> and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be dropped
	 * @param count : int Quantity of items to be dropped
	 * @param actor : L2PcInstance Player requesting the item drop
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the destroyed item or the updated item in inventory
	 */
	@Override
	public L2ItemInstance dropItem(String process, int objectId, long count, L2PcInstance actor, Object reference)
	{
		L2ItemInstance item = super.dropItem(process, objectId, count, actor, reference);
		
		if ((_adena != null) && ((_adena.getCount() <= 0) || (_adena.getOwnerId() != getOwnerId())))
		{
			_adena = null;
		}
		
		if ((_fAdena != null) && ((_fAdena.getCount() <= 0) || (_fAdena.getOwnerId() != getOwnerId())))
		{
			_fAdena = null;
		}
		
		if ((_ancientAdena != null) && ((_ancientAdena.getCount() <= 0) || (_ancientAdena.getOwnerId() != getOwnerId())))
		{
			_ancientAdena = null;
		}
		
		fireTrackerEvents(TrackerEvent.DROP, actor, item, null);
		return item;
	}
	
	/**
	 * <b>Overloaded</b>, when removes item from inventory, remove also owner shortcuts.
	 * @param item : L2ItemInstance to be removed from inventory
	 */
	@Override
	protected boolean removeItem(L2ItemInstance item)
	{
		// Removes any reference to the item from Shortcut bar
		getOwner().removeItemFromShortCut(item.getObjectId());
		
		// Removes active Enchant Scroll
		if (item.getObjectId() == getOwner().getActiveEnchantItemId())
		{
			getOwner().setActiveEnchantItemId(L2PcInstance.ID_NONE);
		}
		
		if (item.getId() == ADENA_ID)
		{
			_adena = null;
		}
		if (item.getId() == FADENA_ID)
		{
			_fAdena = null;
		}
		else if (item.getId() == ANCIENT_ADENA_ID)
		{
			_ancientAdena = null;
		}
		
		if (item.isQuestItem())
		{
			synchronized (_lock)
			{
				_questSlots--;
				if (_questSlots < 0)
				{
					_questSlots = 0;
					_log.warn(this + ": QuestInventory size < 0!");
				}
			}
		}
		return super.removeItem(item);
	}
	
	/**
	 * Refresh the weight of equipment loaded
	 */
	@Override
	public void refreshWeight()
	{
		super.refreshWeight();
		getOwner().refreshOverloaded();
	}
	
	/**
	 * Get back items in inventory from database
	 */
	@Override
	public void restore()
	{
		super.restore();
		_adena = getItemByItemId(ADENA_ID);
		_fAdena = getItemByItemId(FADENA_ID);
		_ancientAdena = getItemByItemId(ANCIENT_ADENA_ID);
	}
	
	public static int[][] restoreVisibleInventory(int objectId)
	{
		int[][] paperdoll = new int[31][3];
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement2 = con.prepareStatement("SELECT object_id,item_id,loc_data,enchant_level FROM items WHERE owner_id=? AND loc='PAPERDOLL'"))
		{
			statement2.setInt(1, objectId);
			try (ResultSet invdata = statement2.executeQuery())
			{
				while (invdata.next())
				{
					int slot = invdata.getInt("loc_data");
					paperdoll[slot][0] = invdata.getInt("object_id");
					paperdoll[slot][1] = invdata.getInt("item_id");
					paperdoll[slot][2] = invdata.getInt("enchant_level");
					/*
					 * if (slot == Inventory.PAPERDOLL_RHAND) { paperdoll[Inventory.PAPERDOLL_RHAND][0] = invdata.getInt("object_id"); paperdoll[Inventory.PAPERDOLL_RHAND][1] = invdata.getInt("item_id"); paperdoll[Inventory.PAPERDOLL_RHAND][2] = invdata.getInt("enchant_level"); }
					 */
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Could not restore inventory: " + e.getMessage(), e);
		}
		return paperdoll;
	}
	
	/**
	 * @param itemList the items that needs to be validated.
	 * @param sendMessage if {@code true} will send a message of inventory full.
	 * @param sendSkillMessage if {@code true} will send a message of skill not available.
	 * @return {@code true} if the inventory isn't full after taking new items and items weight add to current load doesn't exceed max weight load.
	 */
	public boolean checkInventorySlotsAndWeight(List<L2Item> itemList, boolean sendMessage, boolean sendSkillMessage)
	{
		int lootWeight = 0;
		int requiredSlots = 0;
		if (itemList != null)
		{
			for (L2Item item : itemList)
			{
				// If the item is not stackable or is stackable and not present in inventory, will need a slot.
				if (!item.isStackable() || (getInventoryItemCount(item.getId(), -1) <= 0))
				{
					requiredSlots++;
				}
				lootWeight += item.getWeight();
			}
		}
		
		boolean inventoryStatusOK = validateCapacity(requiredSlots) && validateWeight(lootWeight);
		if (!inventoryStatusOK && sendMessage)
		{
			_owner.sendPacket(SystemMessageId.SLOTS_FULL);
			if (sendSkillMessage)
			{
				_owner.sendPacket(SystemMessageId.WEIGHT_EXCEEDED_SKILL_UNAVAILABLE);
			}
		}
		return inventoryStatusOK;
	}
	
	/**
	 * If the item is not stackable or is stackable and not present in inventory, will need a slot.
	 * @param item the item to validate.
	 * @return {@code true} if there is enough room to add the item inventory.
	 */
	public boolean validateCapacity(L2ItemInstance item)
	{
		int slots = 0;
		if (!item.isStackable() || (getInventoryItemCount(item.getId(), -1) <= 0) || (item.getItemType() != EtcItemType.HERB))
		{
			slots++;
		}
		return validateCapacity(slots, item.isQuestItem());
	}
	
	/**
	 * If the item is not stackable or is stackable and not present in inventory, will need a slot.
	 * @param itemId the item Id for the item to validate.
	 * @return {@code true} if there is enough room to add the item inventory.
	 */
	public boolean validateCapacityByItemId(int itemId)
	{
		int slots = 0;
		final L2ItemInstance invItem = getItemByItemId(itemId);
		if ((invItem == null) || !invItem.isStackable())
		{
			slots++;
		}
		return validateCapacity(slots, ItemData.getInstance().getTemplate(itemId).isQuestItem());
	}
	
	@Override
	public boolean validateCapacity(long slots)
	{
		return validateCapacity(slots, false);
	}
	
	public boolean validateCapacity(long slots, boolean questItem)
	{
		if (!questItem)
		{
			return (((_items.size() - _questSlots) + slots) <= _owner.getInventoryLimit());
		}
		return (_questSlots + slots) <= _owner.getQuestInventoryLimit();
	}
	
	@Override
	public boolean validateWeight(long weight)
	{
		// Disable weight check for GMs.
		if (_owner.isGM() && _owner.getDietMode() && _owner.getAccessLevel().allowTransaction())
		{
			return true;
		}
		return ((_totalWeight + weight) <= _owner.getMaxLoad());
	}
	
	/**
	 * Set inventory block for specified IDs<br>
	 * array reference is used for {@link PcInventory#_blockItems}
	 * @param items array of Ids to block/allow
	 * @param mode blocking mode {@link PcInventory#_blockMode}
	 */
	public void setInventoryBlock(int[] items, int mode)
	{
		_blockMode = mode;
		_blockItems = items;
		
		_owner.sendPacket(new ItemList(_owner, false));
	}
	
	/**
	 * Unblock blocked itemIds
	 */
	public void unblock()
	{
		_blockMode = -1;
		_blockItems = null;
		
		_owner.sendPacket(new ItemList(_owner, false));
	}
	
	/**
	 * Check if player inventory is in block mode.
	 * @return true if some itemIds blocked
	 */
	public boolean hasInventoryBlock()
	{
		return ((_blockMode > -1) && (_blockItems != null) && (_blockItems.length > 0));
	}
	
	/**
	 * Block all player items
	 */
	public void blockAllItems()
	{
		// temp fix, some id must be sended
		setInventoryBlock(new int[]
		{
			(ItemData.getInstance().getArraySize() + 2)
		}, 1);
	}
	
	/**
	 * Return block mode
	 * @return int {@link PcInventory#_blockMode}
	 */
	public int getBlockMode()
	{
		return _blockMode;
	}
	
	/**
	 * Return TIntArrayList with blocked item ids
	 * @return TIntArrayList
	 */
	public int[] getBlockItems()
	{
		return _blockItems;
	}
	
	/**
	 * Check if player can use item by itemid
	 * @param itemId int
	 * @return true if can use
	 */
	public boolean canManipulateWithItemId(int itemId)
	{
		if (((_blockMode == 0) && Util.contains(_blockItems, itemId)) || ((_blockMode == 1) && !Util.contains(_blockItems, itemId)))
		{
			return false;
		}
		return true;
	}
	
	@Override
	protected void addItem(L2ItemInstance item)
	{
		if (item.isQuestItem())
		{
			synchronized (_lock)
			{
				_questSlots++;
			}
		}
		super.addItem(item);
	}
	
	public int getSize(boolean quest)
	{
		if (quest)
		{
			return _questSlots;
		}
		return getSize() - _questSlots;
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + _owner + "]";
	}
	
	/**
	 * Apply skills of inventory items
	 */
	public void applyItemSkills()
	{
		for (L2ItemInstance item : _items)
		{
			item.giveSkillsToOwner();
			item.applyEnchantStats();
		}
	}
	
	// LISTENERS
	private static enum TrackerEvent
	{
		DROP,
		ADD_TO_INVENTORY,
		DESTROY,
		TRANSFER
	}
	
	/**
	 * Fires the appropriate item tracker events, if any
	 * @param tEvent
	 * @param actor
	 * @param item
	 * @param target
	 */
	private void fireTrackerEvents(TrackerEvent tEvent, L2PcInstance actor, L2ItemInstance item, ItemContainer target)
	{
		if ((item != null) && (actor != null) && !itemTrackers.isEmpty())
		{
			switch (tEvent)
			{
				case ADD_TO_INVENTORY:
				{
					AddToInventoryEvent event = new AddToInventoryEvent();
					event.setItem(item);
					event.setPlayer(actor);
					for (ItemTracker tracker : itemTrackers)
					{
						if (tracker.containsItemId(item.getId()))
						{
							tracker.onAddToInventory(event);
						}
					}
					return;
				}
				case DROP:
				{
					ItemDropEvent event = new ItemDropEvent();
					event.setItem(item);
					event.setDropper(actor);
					event.setLocation(actor.getLocation());
					for (ItemTracker tracker : itemTrackers)
					{
						if (tracker.containsItemId(item.getId()))
						{
							tracker.onDrop(event);
						}
					}
					return;
				}
				case DESTROY:
				{
					ItemDestroyEvent event = new ItemDestroyEvent();
					event.setItem(item);
					event.setPlayer(actor);
					for (ItemTracker tracker : itemTrackers)
					{
						if (tracker.containsItemId(item.getId()))
						{
							tracker.onDestroy(event);
						}
					}
					return;
				}
				case TRANSFER:
				{
					if (target != null)
					{
						ItemTransferEvent event = new ItemTransferEvent();
						event.setItem(item);
						event.setPlayer(actor);
						event.setTarget(target);
						for (ItemTracker tracker : itemTrackers)
						{
							if (tracker.containsItemId(item.getId()))
							{
								tracker.onTransfer(event);
							}
						}
					}
					return;
				}
			}
		}
	}
	
	/**
	 * Adds an item tracker
	 * @param tracker
	 */
	public static void addItemTracker(ItemTracker tracker)
	{
		if (!itemTrackers.contains(tracker))
		{
			itemTrackers.add(tracker);
		}
	}
	
	/**
	 * Removes an item tracker
	 * @param tracker
	 */
	public static void removeItemTracker(ItemTracker tracker)
	{
		itemTrackers.remove(tracker);
	}
}
