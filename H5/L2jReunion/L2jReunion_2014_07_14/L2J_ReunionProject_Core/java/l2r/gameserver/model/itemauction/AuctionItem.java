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
package l2r.gameserver.model.itemauction;

import l2r.gameserver.datatables.xml.ItemData;
import l2r.gameserver.model.L2Augmentation;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.items.instance.L2ItemInstance;

/**
 * @author Forsaiken
 */
public final class AuctionItem
{
	private final int _auctionItemId;
	private final int _auctionLength;
	private final long _auctionInitBid;
	
	private final int _itemId;
	private final long _itemCount;
	private final StatsSet _itemExtra;
	
	public AuctionItem(final int auctionItemId, final int auctionLength, final long auctionInitBid, final int itemId, final long itemCount, final StatsSet itemExtra)
	{
		_auctionItemId = auctionItemId;
		_auctionLength = auctionLength;
		_auctionInitBid = auctionInitBid;
		
		_itemId = itemId;
		_itemCount = itemCount;
		_itemExtra = itemExtra;
	}
	
	public final boolean checkItemExists()
	{
		final L2Item item = ItemData.getInstance().getTemplate(_itemId);
		if (item == null)
		{
			return false;
		}
		return true;
	}
	
	public final int getAuctionItemId()
	{
		return _auctionItemId;
	}
	
	public final int getAuctionLength()
	{
		return _auctionLength;
	}
	
	public final long getAuctionInitBid()
	{
		return _auctionInitBid;
	}
	
	public final int getId()
	{
		return _itemId;
	}
	
	public final long getItemCount()
	{
		return _itemCount;
	}
	
	public final L2ItemInstance createNewItemInstance()
	{
		final L2ItemInstance item = ItemData.getInstance().createItem("ItemAuction", _itemId, _itemCount, null, null);
		
		item.setEnchantLevel(item.getDefaultEnchantLevel());
		
		final int augmentationId = _itemExtra.getInteger("augmentation_id", 0);
		if (augmentationId > 0)
		{
			item.setAugmentation(new L2Augmentation(augmentationId));
		}
		
		return item;
	}
}