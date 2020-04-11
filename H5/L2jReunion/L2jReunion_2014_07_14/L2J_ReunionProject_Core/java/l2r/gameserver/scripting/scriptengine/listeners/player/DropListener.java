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

import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.scripting.scriptengine.events.ItemDropEvent;
import l2r.gameserver.scripting.scriptengine.events.ItemPickupEvent;
import l2r.gameserver.scripting.scriptengine.impl.L2JListener;

/**
 * Global listener for items dropped by players
 * @author TheOne
 */
public abstract class DropListener extends L2JListener
{
	public DropListener()
	{
		register();
	}
	
	/**
	 * The item was dropped
	 * @param event
	 * @return
	 */
	public abstract boolean onDrop(ItemDropEvent event);
	
	/**
	 * The item was picked up
	 * @param event
	 * @return
	 */
	public abstract boolean onPickup(ItemPickupEvent event);
	
	@Override
	public void register()
	{
		L2ItemInstance.addDropListener(this);
	}
	
	@Override
	public void unregister()
	{
		L2ItemInstance.removeDropListener(this);
	}
}
