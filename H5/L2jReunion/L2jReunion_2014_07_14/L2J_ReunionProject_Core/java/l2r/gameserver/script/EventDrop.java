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
package l2r.gameserver.script;

/**
 * @author Zoey76
 */
public class EventDrop
{
	private final int[] _itemIdList;
	private final int _minCount;
	private final int _maxCount;
	private final int _dropChance;
	
	public EventDrop(int[] itemIdList, int minCount, int maxCount, int dropChance)
	{
		_itemIdList = itemIdList;
		_minCount = minCount;
		_maxCount = maxCount;
		_dropChance = dropChance;
	}
	
	public EventDrop(int itemId, int minCount, int maxCount, int dropChance)
	{
		_itemIdList = new int[]
		{
			itemId
		};
		_minCount = minCount;
		_maxCount = maxCount;
		_dropChance = dropChance;
	}
	
	/**
	 * @return the _itemId
	 */
	public int[] getIdList()
	{
		return _itemIdList;
	}
	
	/**
	 * @return the _minCount
	 */
	public int getMinCount()
	{
		return _minCount;
	}
	
	/**
	 * @return the _maxCount
	 */
	public int getMaxCount()
	{
		return _maxCount;
	}
	
	/**
	 * @return the _dropChance
	 */
	public int getDropChance()
	{
		return _dropChance;
	}
}
