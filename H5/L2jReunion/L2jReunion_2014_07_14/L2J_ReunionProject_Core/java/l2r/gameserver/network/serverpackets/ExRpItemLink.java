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

/**
 * @author KenM
 */
public final class ExRpItemLink extends L2GameServerPacket
{
	private final L2ItemInstance _item;
	
	public ExRpItemLink(L2ItemInstance item)
	{
		_item = item;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x6C);
		writeD(_item.getObjectId());
		writeD(_item.getDisplayId());
		writeD(_item.getLocationSlot());
		writeQ(_item.getCount());
		writeH(_item.getItem().getType2());
		writeH(_item.getCustomType1());
		writeH(_item.isEquipped() ? 0x01 : 0x00);
		writeD(_item.getItem().getBodyPart());
		writeH(_item.getEnchantLevel());
		writeH(_item.getCustomType2());
		if (_item.isAugmented())
		{
			writeD(_item.getAugmentation().getAugmentationId());
		}
		else
		{
			writeD(0x00);
		}
		writeD(_item.getMana());
		writeD(_item.isTimeLimitedItem() ? (int) (_item.getRemainingTime() / 1000) : -9999);
		writeH(_item.getAttackElementType());
		writeH(_item.getAttackElementPower());
		for (byte i = 0; i < 6; i++)
		{
			writeH(_item.getElementDefAttr(i));
		}
		// Enchant Effects
		for (int op : _item.getEnchantOptions())
		{
			writeH(op);
		}
	}
}
