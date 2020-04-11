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

import java.util.List;

import javolution.util.FastList;
import l2r.gameserver.model.SeedProduction;

/**
 * @author l3x
 */

public final class BuyListSeed extends L2GameServerPacket
{
	private final int _manorId;
	private List<Seed> _list = null;
	private final long _money;
	
	public BuyListSeed(long currentMoney, int castleId, List<SeedProduction> seeds)
	{
		_money = currentMoney;
		_manorId = castleId;
		
		if ((seeds != null) && (seeds.size() > 0))
		{
			_list = new FastList<>();
			for (SeedProduction s : seeds)
			{
				if ((s.getCanProduce() > 0) && (s.getPrice() > 0))
				{
					_list.add(new Seed(s.getId(), s.getCanProduce(), s.getPrice()));
				}
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xe9);
		
		writeQ(_money); // current money
		writeD(_manorId); // manor id
		
		if ((_list != null) && (_list.size() > 0))
		{
			writeH(_list.size()); // list length
			for (Seed s : _list)
			{
				writeD(s._itemId);
				writeD(s._itemId);
				writeD(0x00);
				writeQ(s._count); // item count
				writeH(0x05); // Custom Type 2
				writeH(0x00); // Custom Type 1
				writeH(0x00); // Equipped
				writeD(0x00); // Body Part
				writeH(0x00); // Enchant
				writeH(0x00); // Custom Type
				writeD(0x00); // Augment
				writeD(-1); // Mana
				writeD(-9999); // Time
				writeH(0x00); // Element Type
				writeH(0x00); // Element Power
				for (byte i = 0; i < 6; i++)
				{
					writeH(0x00);
				}
				// Enchant Effects
				writeH(0x00);
				writeH(0x00);
				writeH(0x00);
				writeQ(s._price); // price
			}
			_list.clear();
		}
		else
		{
			writeH(0x00);
		}
		
	}
	
	private static class Seed
	{
		public final int _itemId;
		public final long _count;
		public final long _price;
		
		public Seed(int itemId, long count, long price)
		{
			_itemId = itemId;
			_count = count;
			_price = price;
		}
	}
}
