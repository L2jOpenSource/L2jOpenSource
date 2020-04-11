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
package l2r.gameserver.scripting.scriptengine.listeners.player;

import java.util.List;

import l2r.gameserver.model.itemcontainer.PcInventory;
import l2r.gameserver.scripting.scriptengine.events.AddToInventoryEvent;
import l2r.gameserver.scripting.scriptengine.events.ItemDestroyEvent;
import l2r.gameserver.scripting.scriptengine.events.ItemDropEvent;
import l2r.gameserver.scripting.scriptengine.events.ItemTransferEvent;
import l2r.gameserver.scripting.scriptengine.impl.L2JListener;

/**
 * This listener allows you to track items with certain item ids.<br>
 * This can be very useful if you want to make a script which will track all high value items and will log their movements/creation/drop/etc.<br>
 * It can be a great tool to catch hackers on your server ;)
 * @author TheOne
 */
public abstract class ItemTracker extends L2JListener
{
	private final List<Integer> _itemIds;
	
	public ItemTracker(List<Integer> itemIds)
	{
		_itemIds = itemIds;
		register();
	}
	
	/**
	 * The item has been dropped
	 * @param event
	 */
	public abstract void onDrop(ItemDropEvent event);
	
	/**
	 * The item has been added to the inventory
	 * @param event
	 */
	public abstract void onAddToInventory(AddToInventoryEvent event);
	
	/**
	 * Notifies when the item is destroyed
	 * @param event
	 */
	public abstract void onDestroy(ItemDestroyEvent event);
	
	/**
	 * Notifies when the item is transfered or traded
	 * @param event
	 */
	public abstract void onTransfer(ItemTransferEvent event);
	
	@Override
	public void register()
	{
		PcInventory.addItemTracker(this);
	}
	
	@Override
	public void unregister()
	{
		PcInventory.removeItemTracker(this);
	}
	
	/**
	 * Checks if this item is tracked
	 * @param itemId
	 * @return
	 */
	public boolean containsItemId(int itemId)
	{
		return _itemIds.contains(itemId);
	}
}
