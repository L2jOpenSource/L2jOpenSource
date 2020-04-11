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
 * @author ShanSoft
 */
public class ExBuySellList extends L2GameServerPacket
{
	private L2ItemInstance[] _sellList = null;
	private L2ItemInstance[] _refundList = null;
	private final boolean _done;
	
	public ExBuySellList(L2PcInstance player, double taxRate, boolean done)
	{
		_sellList = player.getInventory().getAvailableItems(false, false, false);
		if (player.hasRefund())
		{
			_refundList = player.getRefund().getItems();
		}
		_done = done;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0xB7);
		writeD(0x01);
		
		if ((_sellList != null) && (_sellList.length > 0))
		{
			writeH(_sellList.length);
			for (L2ItemInstance item : _sellList)
			{
				writeD(item.getObjectId());
				writeD(item.getDisplayId());
				writeD(item.getLocationSlot());
				writeQ(item.getCount());
				writeH(item.getItem().getType2());
				writeH(item.getCustomType1());
				writeH(0x00);
				writeD(item.getItem().getBodyPart());
				writeH(item.getEnchantLevel());
				writeH(item.getCustomType2());
				// Augment, Mana, Time - hardcode for now
				writeD(0x00);
				writeD(-1);
				writeD(-9999);
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
				
				writeQ(item.getItem().getReferencePrice() / 2);
			}
		}
		else
		{
			writeH(0x00);
		}
		
		if ((_refundList != null) && (_refundList.length > 0))
		{
			writeH(_refundList.length);
			int idx = 0;
			for (L2ItemInstance item : _refundList)
			{
				writeD(item.getObjectId());
				writeD(item.getDisplayId());
				writeD(0x00);
				writeQ(item.getCount());
				writeH(item.getItem().getType2());
				writeH(item.getCustomType1());
				writeH(0x00);
				writeD(item.getItem().getBodyPart());
				writeH(item.getEnchantLevel());
				writeH(item.getCustomType2());
				// Augment, Mana, Time - hardcode for now
				writeD(0x00);
				writeD(-1);
				writeD(-9999);
				writeH(item.getAttackElementType());
				writeH(item.getAttackElementPower());
				for (byte i = 0; i < 6; i++)
				{
					writeH(item.getElementDefAttr(i));
				}
				// Enchant Effects
				writeH(0x00);
				writeH(0x00);
				writeH(0x00);
				writeD(idx++);
				writeQ((item.getItem().getReferencePrice() / 2) * item.getCount());
			}
		}
		else
		{
			writeH(0x00);
		}
		
		writeC(_done ? 0x01 : 0x00);
	}
}