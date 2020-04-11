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
package l2r.gameserver.model;

import java.util.List;

import l2r.gameserver.model.holders.ItemHolder;

/**
 * @author Zoey76
 */
public class L2ExtractableProductItem
{
	private final List<ItemHolder> _items;
	private final double _chance;
	
	public L2ExtractableProductItem(List<ItemHolder> items, double chance)
	{
		_items = items;
		_chance = chance;
	}
	
	/**
	 * @return the the production list.
	 */
	public List<ItemHolder> getItems()
	{
		return _items;
	}
	
	/**
	 * @return the chance of the production list.
	 */
	public double getChance()
	{
		return _chance;
	}
}
