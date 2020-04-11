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

import com.l2jserver.gameserver.data.sql.impl.PrimeShopTable;
import com.l2jserver.gameserver.data.xml.impl.PrimeShopData;
import com.l2jserver.gameserver.model.PrimeShop;

public final class ExBrProductList extends L2GameServerPacket
{
	private final Collection<PrimeShop> _itemList = PrimeShopData.getInstance().getProductValues();
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xD6);
		writeD(_itemList.size());
		for (PrimeShop product : _itemList)
		{
			final int category = product.getCategory();
			writeD(product.getProductId()); // product id
			writeH(category); // category id
			writeD(product.getPrice()); // price
			switch (category)
			{
				case 6:
				{
					writeD(0x01); // event
					break;
				}
				case 7:
				{
					writeD(0x02); // best
					break;
				}
				case 8:
				{
					writeD(0x03); // event & best
					break;
				}
				default:
				{
					writeD(0x00); // normal
					break;
				}
			}
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