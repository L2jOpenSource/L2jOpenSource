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

import java.util.List;

import com.l2jserver.gameserver.model.TradeItem;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

public class PrivateStoreListBuy extends AbstractItemPacket
{
	private final int _objId;
	private final long _playerAdena;
	private final List<TradeItem> _items;
	
	public PrivateStoreListBuy(L2PcInstance player, L2PcInstance storePlayer)
	{
		_objId = storePlayer.getObjectId();
		_playerAdena = player.getAdena();
		storePlayer.getSellList().updateItems(); // Update SellList for case inventory content has changed
		_items = storePlayer.getBuyList().getAvailableItems(player.getInventory());
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xbe);
		writeD(_objId);
		writeQ(_playerAdena);
		
		writeD(_items.size());
		
		for (TradeItem item : _items)
		{
			writeItem(item);
			writeD(item.getObjectId());
			writeQ(item.getPrice());
			writeQ(item.getItem().getReferencePrice() * 2);
			writeQ(item.getStoreCount());
		}
	}
}
