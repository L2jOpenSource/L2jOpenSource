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

import l2r.gameserver.model.TradeItem;

/**
 * @author Yme
 */
public final class TradeOtherAdd extends L2GameServerPacket
{
	private final TradeItem _item;
	
	public TradeOtherAdd(TradeItem item)
	{
		_item = item;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x1b);
		
		writeH(1); // item count
		writeH(0);
		writeD(_item.getObjectId());
		writeD(_item.getItem().getDisplayId());
		writeQ(_item.getCount());
		writeH(_item.getItem().getType2()); // item type2
		writeH(_item.getCustomType1());
		
		writeD(_item.getItem().getBodyPart()); // rev 415 slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
		writeH(_item.getEnchant()); // enchant level
		writeH(0x00);
		writeH(_item.getCustomType2());
		
		// T1
		writeH(_item.getAttackElementType());
		writeH(_item.getAttackElementPower());
		for (byte i = 0; i < 6; i++)
		{
			writeH(_item.getElementDefAttr(i));
		}
		
		writeH(0x00); // Enchant effect 1
		writeH(0x00); // Enchant effect 2
		writeH(0x00); // Enchant effect 3
	}
}
