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
package l2r.gameserver.scripting.scriptengine.listeners.clan;

import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.itemcontainer.ClanWarehouse;
import l2r.gameserver.scripting.scriptengine.events.ClanWarehouseAddItemEvent;
import l2r.gameserver.scripting.scriptengine.events.ClanWarehouseDeleteItemEvent;
import l2r.gameserver.scripting.scriptengine.events.ClanWarehouseTransferEvent;
import l2r.gameserver.scripting.scriptengine.impl.L2JListener;

/**
 * @author TheOne
 */
public abstract class ClanWarehouseListener extends L2JListener
{
	private final ClanWarehouse _clanWarehouse;
	
	public ClanWarehouseListener(L2Clan clan)
	{
		_clanWarehouse = (ClanWarehouse) clan.getWarehouse();
		register();
	}
	
	/**
	 * An item was just added
	 * @param event
	 * @return
	 */
	public abstract boolean onAddItem(ClanWarehouseAddItemEvent event);
	
	/**
	 * An item was just deleted
	 * @param event
	 * @return
	 */
	public abstract boolean onDeleteItem(ClanWarehouseDeleteItemEvent event);
	
	/**
	 * An item was just transfered
	 * @param event
	 * @return
	 */
	public abstract boolean onTransferItem(ClanWarehouseTransferEvent event);
	
	@Override
	public void register()
	{
		_clanWarehouse.addWarehouseListener(this);
	}
	
	@Override
	public void unregister()
	{
		_clanWarehouse.removeWarehouseListener(this);
	}
	
	/**
	 * Returns the clan warehouse attached to this listener
	 * @return
	 */
	public ClanWarehouse getWarehouse()
	{
		return _clanWarehouse;
	}
}
