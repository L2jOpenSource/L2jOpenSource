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

import l2r.gameserver.model.items.instance.L2ItemInstance;

public class PetItemList extends L2GameServerPacket
{
	private final L2ItemInstance[] _items;
	
	public PetItemList(L2ItemInstance[] items)
	{
		_items = items;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xB3);
		
		int count = _items.length;
		writeH(count);
		
		for (L2ItemInstance temp : _items)
		{
			writeD(temp.getObjectId());
			writeD(temp.getDisplayId());
			writeD(temp.getLocationSlot());
			writeQ(temp.getCount());
			writeH(temp.getItem().getType2());
			writeH(temp.getCustomType1());
			writeH(temp.isEquipped() ? 0x01 : 0x00);
			writeD(temp.getItem().getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			if (temp.isAugmented())
			{
				writeD(temp.getAugmentation().getAugmentationId());
			}
			else
			{
				writeD(0x00);
			}
			writeD(temp.getMana());
			writeD(temp.isTimeLimitedItem() ? (int) (temp.getRemainingTime() / 1000) : -9999);
			writeH(temp.getAttackElementType());
			writeH(temp.getAttackElementPower());
			for (byte i = 0; i < 6; i++)
			{
				writeH(temp.getElementDefAttr(i));
			}
			// Enchant Effects
			for (int op : temp.getEnchantOptions())
			{
				writeH(op);
			}
		}
	}
}
