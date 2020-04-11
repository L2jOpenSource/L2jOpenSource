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

import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.items.instance.L2ItemInstance;

public final class SpawnItem extends L2GameServerPacket
{
	private final int _objectId;
	private int _itemId;
	private final int _x, _y, _z;
	private int _stackable;
	private long _count;
	
	public SpawnItem(L2Object obj)
	{
		_objectId = obj.getObjectId();
		_x = obj.getX();
		_y = obj.getY();
		_z = obj.getZ();
		
		if (obj instanceof L2ItemInstance)
		{
			L2ItemInstance item = (L2ItemInstance) obj;
			_itemId = item.getDisplayId();
			_stackable = item.isStackable() ? 0x01 : 0x00;
			_count = item.getCount();
		}
		else
		{
			_itemId = obj.getPoly().getPolyId();
			_stackable = 0;
			_count = 1;
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x05);
		writeD(_objectId);
		writeD(_itemId);
		
		writeD(_x);
		writeD(_y);
		writeD(_z);
		// only show item count if it is a stackable item
		writeD(_stackable);
		writeQ(_count);
		writeD(0x00); // c2
		writeD(0x00); // freya unk
	}
}
