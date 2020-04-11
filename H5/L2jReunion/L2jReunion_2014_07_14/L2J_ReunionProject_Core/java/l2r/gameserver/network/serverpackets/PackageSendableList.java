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

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;

/**
 * @author -Wooden-
 * @author UnAfraid, mrTJO
 */
public class PackageSendableList extends L2GameServerPacket
{
	private final L2ItemInstance[] _items;
	private final int _playerObjId;
	
	public PackageSendableList(L2ItemInstance[] items, int playerObjId)
	{
		_items = items;
		_playerObjId = playerObjId;
	}
	
	@Override
	protected void writeImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		writeC(0xD2);
		writeD(_playerObjId);
		writeQ(activeChar.getAdena());
		writeD(_items.length);
		for (L2ItemInstance item : _items)
		{
			writeD(item.getObjectId()); // This is not the real object id of the item!
			writeD(item.getDisplayId());
			writeD(item.getLocationSlot());
			writeQ(item.getCount());
			writeH(item.getItem().getType2());
			writeH(item.getCustomType1());
			writeH(item.isEquipped() ? 0x01 : 0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(item.getCustomType2());
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
			writeD(item.getObjectId()); // object id THE REAL ONE
		}
	}
}
