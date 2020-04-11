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
package com.l2jserver.gameserver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jserver.Config;
import com.l2jserver.gameserver.enums.ItemLocation;
import com.l2jserver.gameserver.instancemanager.ItemsOnGroundManager;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;

public final class ItemsAutoDestroy
{
	private final Map<Integer, L2ItemInstance> _items = new ConcurrentHashMap<>();
	
	protected ItemsAutoDestroy()
	{
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this::removeItems, 5000, 5000);
	}
	
	public static ItemsAutoDestroy getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public synchronized void addItem(L2ItemInstance item)
	{
		item.setDropTime(System.currentTimeMillis());
		_items.put(item.getObjectId(), item);
	}
	
	public synchronized void removeItems()
	{
		final long curtime = System.currentTimeMillis();
		for (L2ItemInstance item : _items.values())
		{
			if (item == null)
			{
				continue;
			}
			
			if ((item.getDropTime() == 0) || (item.getItemLocation() != ItemLocation.VOID))
			{
				_items.remove(item.getObjectId());
			}
			else
			{
				if (item.getItem().getAutoDestroyTime() > 0)
				{
					if ((curtime - item.getDropTime()) > item.getItem().getAutoDestroyTime())
					{
						L2World.getInstance().removeVisibleObject(item, item.getWorldRegion());
						L2World.getInstance().removeObject(item);
						_items.remove(item.getObjectId());
						if (Config.SAVE_DROPPED_ITEM)
						{
							ItemsOnGroundManager.getInstance().removeObject(item);
						}
					}
				}
				else if (item.getItem().hasExImmediateEffect())
				{
					if ((curtime - item.getDropTime()) > Config.HERB_AUTO_DESTROY_TIME)
					{
						L2World.getInstance().removeVisibleObject(item, item.getWorldRegion());
						L2World.getInstance().removeObject(item);
						_items.remove(item.getObjectId());
						if (Config.SAVE_DROPPED_ITEM)
						{
							ItemsOnGroundManager.getInstance().removeObject(item);
						}
					}
				}
				else
				{
					final long sleep = ((Config.AUTODESTROY_ITEM_AFTER == 0) ? 3600000 : Config.AUTODESTROY_ITEM_AFTER * 1000);
					
					if ((curtime - item.getDropTime()) > sleep)
					{
						L2World.getInstance().removeVisibleObject(item, item.getWorldRegion());
						L2World.getInstance().removeObject(item);
						_items.remove(item.getObjectId());
						if (Config.SAVE_DROPPED_ITEM)
						{
							ItemsOnGroundManager.getInstance().removeObject(item);
						}
					}
				}
			}
		}
	}
	
	private static class SingletonHolder
	{
		protected static final ItemsAutoDestroy _instance = new ItemsAutoDestroy();
	}
}