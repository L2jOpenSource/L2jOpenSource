/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.network.serverpackets;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import com.l2jserver.gameserver.model.ItemInfo;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;

/**
 * @author UnAfraid
 */
public abstract class AbstractInventoryUpdate extends AbstractItemPacket
{
	private final Map<Integer, ItemInfo> _items = new ConcurrentSkipListMap<>();
	
	public AbstractInventoryUpdate()
	{
	}
	
	public AbstractInventoryUpdate(L2ItemInstance item)
	{
		addItem(item);
	}
	
	public AbstractInventoryUpdate(List<ItemInfo> items)
	{
		for (ItemInfo item : items)
		{
			_items.put(item.getObjectId(), item);
		}
	}
	
	public final void addItem(L2ItemInstance item)
	{
		_items.put(item.getObjectId(), new ItemInfo(item));
	}
	
	public final void addNewItem(L2ItemInstance item)
	{
		_items.put(item.getObjectId(), new ItemInfo(item, 1));
	}
	
	public final void addModifiedItem(L2ItemInstance item)
	{
		_items.put(item.getObjectId(), new ItemInfo(item, 2));
	}
	
	public final void addRemovedItem(L2ItemInstance item)
	{
		_items.put(item.getObjectId(), new ItemInfo(item, 3));
	}
	
	public final void addItems(List<L2ItemInstance> items)
	{
		for (L2ItemInstance item : items)
		{
			_items.put(item.getObjectId(), new ItemInfo(item));
		}
	}
	
	public final Collection<ItemInfo> getItems()
	{
		return _items.values();
	}
	
	protected final void writeItems()
	{
		writeH(_items.size());
		for (ItemInfo item : _items.values())
		{
			writeH(item.getChange()); // Update type : 01-add, 02-modify, 03-remove
			writeItem(item);
		}
	}
}
