/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.model.drops.strategy;

import com.l2jserver.Config;
import com.l2jserver.gameserver.model.drops.IDropItem;

/**
 * @author Battlecruiser
 */
public interface IPreciseDeterminationStrategy
{
	public static final IPreciseDeterminationStrategy ALWAYS = dropItem -> true;
	
	public static final IPreciseDeterminationStrategy DEFAULT = dropItem -> Config.PRECISE_DROP_CALCULATION;
	
	public static final IPreciseDeterminationStrategy NEVER = dropItem -> false;
	
	/**
	 * @param dropItem
	 * @return <code>true</code> if drop calculation strategy should use precise rules
	 */
	public boolean isPreciseCalculated(IDropItem dropItem);
}
