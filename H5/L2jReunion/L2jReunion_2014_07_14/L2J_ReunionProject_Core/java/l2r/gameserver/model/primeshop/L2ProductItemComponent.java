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
package l2r.gameserver.model.primeshop;

import l2r.gameserver.datatables.xml.ItemData;
import l2r.gameserver.model.items.L2Item;

/**
 * Created by GodFather
 */
public class L2ProductItemComponent
{
	private final int _itemId;
	private final int _count;
	
	private final int _weight;
	private final boolean _dropable;
	
	public L2ProductItemComponent(int item_id, int count)
	{
		_itemId = item_id;
		_count = count;
		
		L2Item item = ItemData.getInstance().getTemplate(item_id);
		if (item != null)
		{
			_weight = item.getWeight();
			_dropable = item.isDropable();
		}
		else
		{
			_weight = 0;
			_dropable = true;
		}
	}
	
	public int getId()
	{
		return _itemId;
	}
	
	public int getCount()
	{
		return _count;
	}
	
	public int getWeight()
	{
		return _weight;
	}
	
	public boolean isDropable()
	{
		return _dropable;
	}
}