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

import com.l2jserver.gameserver.model.TradeItem;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;

public class PrivateStoreManageListBuy extends AbstractItemPacket
{
	private final int _objId;
	private final long _playerAdena;
	private final L2ItemInstance[] _itemList;
	private final TradeItem[] _buyList;
	
	public PrivateStoreManageListBuy(L2PcInstance player)
	{
		_objId = player.getObjectId();
		_playerAdena = player.getAdena();
		_itemList = player.getInventory().getUniqueItems(false, true);
		_buyList = player.getBuyList().getItems();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xbd);
		// section 1
		writeD(_objId);
		writeQ(_playerAdena);
		
		// section2
		writeD(_itemList.length); // inventory items for potential buy
		for (L2ItemInstance item : _itemList)
		{
			writeItem(item);
			writeQ(item.getItem().getReferencePrice() * 2);
		}
		
		// section 3
		writeD(_buyList.length); // count for all items already added for buy
		for (TradeItem item : _buyList)
		{
			writeItem(item);
			writeQ(item.getPrice());
			writeQ(item.getItem().getReferencePrice() * 2);
			writeQ(item.getCount());
		}
	}
}
