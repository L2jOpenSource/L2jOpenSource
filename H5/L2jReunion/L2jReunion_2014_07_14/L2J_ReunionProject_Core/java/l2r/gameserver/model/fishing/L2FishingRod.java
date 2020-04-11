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
 * Class for the Fishing Rod object.
 * @author nonom
 */
public class L2FishingRod
{
	private final int _fishingRodId;
	private final int _fishingRodItemId;
	private final int _fishingRodLevel;
	private final String _fishingRodName;
	private final double _fishingRodDamage;
	
	public L2FishingRod(StatsSet set)
	{
		_fishingRodId = set.getInteger("fishingRodId");
		_fishingRodItemId = set.getInteger("fishingRodItemId");
		_fishingRodLevel = set.getInteger("fishingRodLevel");
		_fishingRodName = set.getString("fishingRodName");
		_fishingRodDamage = set.getDouble("fishingRodDamage");
	}
	
	/**
	 * @return the fishing rod Id.
	 */
	public int getFishingRodId()
	{
		return _fishingRodId;
	}
	
	/**
	 * @return the fishing rod Item Id.
	 */
	public int getFishingRodItemId()
	{
		return _fishingRodItemId;
	}
	
	/**
	 * @return the fishing rod Level.
	 */
	public int getFishingRodLevel()
	{
		return _fishingRodLevel;
	}
	
	/**
	 * @return the fishing rod Item Name.
	 */
	public String getFishingRodItemName()
	{
		return _fishingRodName;
	}
	
	/**
	 * @return the fishing rod Damage.
	 */
	public double getFishingRodDamage()
	{
		return _fishingRodDamage;
	}
}