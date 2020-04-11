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
package l2r.gameserver.model.fishing;

import l2r.gameserver.model.StatsSet;

/**
 * Class for the Fishing Monsters object.
 * @author nonom
 */
public class L2FishingMonster
{
	private final int _userMinLevel;
	private final int _userMaxLevel;
	private final int _fishingMonsterId;
	private final int _probability;
	
	public L2FishingMonster(StatsSet set)
	{
		_userMinLevel = set.getInteger("userMinLevel");
		_userMaxLevel = set.getInteger("userMaxLevel");
		_fishingMonsterId = set.getInteger("fishingMonsterId");
		_probability = set.getInteger("probability");
	}
	
	/**
	 * @return the minimum user level.
	 */
	public int getUserMinLevel()
	{
		return _userMinLevel;
	}
	
	/**
	 * @return the maximum user level.
	 */
	public int getUserMaxLevel()
	{
		return _userMaxLevel;
	}
	
	/**
	 * @return the fishing monster Id.
	 */
	public int getFishingMonsterId()
	{
		return _fishingMonsterId;
	}
	
	/**
	 * @return the probability.
	 */
	public int getProbability()
	{
		return _probability;
	}
}