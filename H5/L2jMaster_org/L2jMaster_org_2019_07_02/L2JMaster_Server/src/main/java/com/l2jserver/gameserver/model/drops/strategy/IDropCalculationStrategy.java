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

import java.util.Collections;
import java.util.List;

import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.drops.GeneralDropItem;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.util.Rnd;

/**
 * @author Battlecruiser
 */
public interface IDropCalculationStrategy
{
	public static final IDropCalculationStrategy DEFAULT_STRATEGY = (item, victim, killer) ->
	{
		double chance = item.getChance(victim, killer);
		if (chance > (Rnd.nextDouble() * 100))
		{
			int amountMultiply = 1;
			if (item.isPreciseCalculated() && (chance > 100))
			{
				amountMultiply = (int) chance / 100;
				if ((chance % 100) > (Rnd.nextDouble() * 100))
				{
					amountMultiply++;
				}
			}
			
			return Collections.singletonList(new ItemHolder(item.getItemId(), Rnd.get(item.getMin(victim, killer), item.getMax(victim, killer)) * amountMultiply));
		}
		
		return null;
	};
	
	public List<ItemHolder> calculateDrops(GeneralDropItem item, L2Character victim, L2Character killer);
}
