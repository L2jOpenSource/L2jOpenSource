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

import java.util.ArrayList;
import java.util.List;

import com.l2jserver.gameserver.data.sql.impl.PrimeShopTable;
import com.l2jserver.gameserver.model.PrimeShop;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

public final class ExBrRecentProductList extends L2GameServerPacket
{
	private List<PrimeShop> _itemList = new ArrayList<>();
	
	public ExBrRecentProductList(L2PcInstance player)
	{
		final int playerObj = player.getObjectId();
		_itemList = PrimeShopTable.getInstance().getPoducts(playerObj);
	}
	
	@Override
	protected void writeImpl()
	{
		if ((_itemList == null) || _itemList.isEmpty())
		{
			return;
		}
		
		writeC(0xFE);
		writeH(0xDC);
		writeD(_itemList.size());
		for (PrimeShop product : _itemList)
		{
			writeD(product.getProductId()); // product id
			writeH(product.getCategory()); // category id
			writeD(product.getPrice()); // price
			writeD((int) (product.sale_start_date() / 1000)); // start sale
			writeD((int) (product.sale_end_date() / 1000)); // end sale
			writeC(1); // TODO: day week - product.getDayWeek()?
			writeC(product.getStartHour()); // start hour
			writeC(product.getStartMin()); // start min
			writeC(product.getEndHour()); // end hour
			writeC(product.getEndMin()); // end min
			final int productId = product.getProductId();
			writeD(PrimeShopTable.getInstance().getActualStock(productId)); // current stock
			writeD(PrimeShopTable.getInstance().getMaxStock(productId)); // max stock
		}
	}
}