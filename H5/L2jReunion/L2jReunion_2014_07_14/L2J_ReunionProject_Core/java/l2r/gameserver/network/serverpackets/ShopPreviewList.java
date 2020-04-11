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
package l2r.gameserver.network.serverpackets;

import java.util.Collection;

import l2r.Config;
import l2r.gameserver.model.buylist.L2BuyList;
import l2r.gameserver.model.buylist.Product;
import l2r.gameserver.model.items.L2Item;

public class ShopPreviewList extends L2GameServerPacket
{
	private final int _listId;
	private final Collection<Product> _list;
	private final long _money;
	private int _expertise;
	
	public ShopPreviewList(L2BuyList list, long currentMoney, int expertiseIndex)
	{
		_listId = list.getListId();
		_list = list.getProducts();
		_money = currentMoney;
		_expertise = expertiseIndex;
	}
	
	public ShopPreviewList(Collection<Product> lst, int listId, long currentMoney)
	{
		_listId = listId;
		_list = lst;
		_money = currentMoney;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xF5);
		writeC(0xC0); // ?
		writeC(0x13); // ?
		writeC(0x00); // ?
		writeC(0x00); // ?
		writeQ(_money); // current money
		writeD(_listId);
		
		int newlength = 0;
		for (Product product : _list)
		{
			if ((product.getItem().getCrystalType().getId() <= _expertise) && product.getItem().isEquipable())
			{
				newlength++;
			}
		}
		writeH(newlength);
		
		for (Product product : _list)
		{
			if ((product.getItem().getCrystalType().getId() <= _expertise) && product.getItem().isEquipable())
			{
				writeD(product.getId());
				writeH(product.getItem().getType2()); // item type2
				
				if (product.getItem().getType1() != L2Item.TYPE1_ITEM_QUESTITEM_ADENA)
				{
					writeH(product.getItem().getBodyPart()); // rev 415 slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
				}
				else
				{
					writeH(0x00); // rev 415 slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
				}
				
				writeQ(Config.WEAR_PRICE);
			}
		}
	}
}
