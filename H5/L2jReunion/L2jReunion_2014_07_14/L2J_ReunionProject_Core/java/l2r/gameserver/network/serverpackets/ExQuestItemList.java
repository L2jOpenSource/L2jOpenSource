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

import javolution.util.FastList;
import l2r.gameserver.model.itemcontainer.PcInventory;
import l2r.gameserver.model.items.instance.L2ItemInstance;

/**
 * @author JIV
 */
public class ExQuestItemList extends L2GameServerPacket
{
	private final FastList<L2ItemInstance> _items;
	private final PcInventory _inventory;
	
	public ExQuestItemList(FastList<L2ItemInstance> items, PcInventory inv)
	{
		_items = items;
		_inventory = inv;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xC6);
		writeH(_items.size());
		for (L2ItemInstance item : _items)
		{
			writeD(item.getObjectId());
			writeD(item.getDisplayId());
			writeD(item.getLocationSlot());
			writeQ(item.getCount());
			writeD(item.getItem().getType2());
			writeH(item.getCustomType1()); // item type3
			// writeH(item.isEquipped() ? 0x01 : 0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchantLevel()); // enchant level
			writeH(item.getCustomType2()); // item type3
			if (item.isAugmented())
			{
				writeD(item.getAugmentation().getAugmentationId());
			}
			else
			{
				writeD(0x00);
			}
			writeD(item.getMana());
			writeD(item.isTimeLimitedItem() ? (int) (item.getRemainingTime() / 1000) : -9999);
			writeH(item.getAttackElementType());
			writeH(item.getAttackElementPower());
			for (byte i = 0; i < 6; i++)
			{
				writeH(item.getElementDefAttr(i));
			}
			// Enchant Effects
			for (int op : item.getEnchantOptions())
			{
				writeH(op);
			}
		}
		if (_inventory.hasInventoryBlock())
		{
			writeH(_inventory.getBlockItems().length);
			writeC(_inventory.getBlockMode());
			for (int i : _inventory.getBlockItems())
			{
				writeD(i);
			}
		}
		else
		{
			writeH(0x00);
		}
		FastList.recycle(_items);
	}
}
