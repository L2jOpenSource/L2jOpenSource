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

import l2r.gameserver.datatables.xml.ItemData;
import l2r.gameserver.scripting.scriptengine.events.ItemCreateEvent;
import l2r.gameserver.scripting.scriptengine.impl.L2JListener;

/**
 * Watches for specific item Ids and triggers the listener when one of these items is created
 * @author TheOne
 */
public abstract class NewItemListener extends L2JListener
{
	private final List<Integer> _itemIds;
	
	public NewItemListener(List<Integer> itemIds)
	{
		_itemIds = itemIds;
		register();
	}
	
	/**
	 * An item corresponding to the itemIds list was just created
	 * @param event
	 * @return
	 */
	public abstract boolean onCreate(ItemCreateEvent event);
	
	@Override
	public void register()
	{
		ItemData.addNewItemListener(this);
	}
	
	@Override
	public void unregister()
	{
		ItemData.removeNewItemListener(this);
	}
	
	public boolean containsItemId(int itemId)
	{
		return _itemIds.contains(itemId);
	}
}
