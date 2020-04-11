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
package com.l2jserver.gameserver.model.drops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.drops.strategy.IAmountMultiplierStrategy;
import com.l2jserver.gameserver.model.drops.strategy.IChanceMultiplierStrategy;
import com.l2jserver.gameserver.model.drops.strategy.IGroupedItemDropCalculationStrategy;
import com.l2jserver.gameserver.model.drops.strategy.IKillerChanceModifierStrategy;
import com.l2jserver.gameserver.model.drops.strategy.IPreciseDeterminationStrategy;
import com.l2jserver.gameserver.model.holders.ItemHolder;

/**
 * @author NosBit
 */
public final class GroupedGeneralDropItem implements IDropItem
{
	
	private final double _chance;
	private List<GeneralDropItem> _items;
	protected final IGroupedItemDropCalculationStrategy _dropCalculationStrategy;
	protected final IKillerChanceModifierStrategy _killerChanceModifierStrategy;
	protected final IPreciseDeterminationStrategy _preciseStrategy;
	
	/**
	 * @param chance the chance of this drop item.
	 */
	public GroupedGeneralDropItem(double chance)
	{
		this(chance, IGroupedItemDropCalculationStrategy.DEFAULT_STRATEGY, IKillerChanceModifierStrategy.DEFAULT_STRATEGY, IPreciseDeterminationStrategy.DEFAULT);
	}
	
	/**
	 * @param chance the chance of this drop item.
	 * @param dropStrategy to calculate drops.
	 * @param killerStrategy
	 * @param preciseStrategy
	 */
	public GroupedGeneralDropItem(double chance, IGroupedItemDropCalculationStrategy dropStrategy, IKillerChanceModifierStrategy killerStrategy, IPreciseDeterminationStrategy preciseStrategy)
	{
		_chance = chance;
		_dropCalculationStrategy = dropStrategy;
		_killerChanceModifierStrategy = killerStrategy;
		_preciseStrategy = preciseStrategy;
	}
	
	/**
	 * Gets the chance of this drop item.
	 * @return the chance
	 */
	public final double getChance()
	{
		return _chance;
	}
	
	/**
	 * Gets the items.
	 * @return the items
	 */
	public final List<GeneralDropItem> getItems()
	{
		return _items;
	}
	
	/**
	 * @return the strategy
	 */
	public final IGroupedItemDropCalculationStrategy getDropCalculationStrategy()
	{
		return _dropCalculationStrategy;
	}
	
	/**
	 * @return the _killerChanceModifierStrategy
	 */
	public IKillerChanceModifierStrategy getKillerChanceModifierStrategy()
	{
		return _killerChanceModifierStrategy;
	}
	
	/**
	 * @return the _preciseStrategy
	 */
	public final IPreciseDeterminationStrategy getPreciseStrategy()
	{
		return _preciseStrategy;
	}
	
	/**
	 * Sets an item list to this drop item.
	 * @param items the item list
	 */
	public final void setItems(List<GeneralDropItem> items)
	{
		_items = Collections.unmodifiableList(items);
	}
	
	/**
	 * Returns a list of items in the group with chance multiplied by chance of the group
	 * @return the list of items with modified chances
	 */
	public final List<GeneralDropItem> extractMe()
	{
		List<GeneralDropItem> items = new ArrayList<>();
		for (final GeneralDropItem item : getItems())
		{
			// precise and killer strategies of the group
			items.add(new GeneralDropItem(item.getItemId(), item.getMin(), item.getMax(), (item.getChance() * getChance()) / 100, item.getAmountStrategy(), item.getChanceStrategy(), getPreciseStrategy(), getKillerChanceModifierStrategy(), item.getDropCalculationStrategy()));
		}
		return items;
	}
	
	/**
	 * statically normalizes a group, useful when need to convert legacy SQL data
	 * @return a new group with items, which have a sum of getChance() of 100%
	 */
	public final GroupedGeneralDropItem normalizeMe()
	{
		double sumchance = 0;
		for (GeneralDropItem item : getItems())
		{
			sumchance += (item.getChance() * getChance()) / 100;
		}
		final double sumchance1 = sumchance;
		GroupedGeneralDropItem group = new GroupedGeneralDropItem(sumchance1, getDropCalculationStrategy(), IKillerChanceModifierStrategy.NO_RULES, getPreciseStrategy());
		List<GeneralDropItem> items = new ArrayList<>();
		for (final GeneralDropItem item : getItems())
		{
			// modify only the chance, leave all other rules intact
			items.add(new GeneralDropItem(item.getItemId(), item.getMin(), item.getMax(), (item.getChance() * getChance()) / sumchance1, item.getAmountStrategy(), item.getChanceStrategy(), item.getPreciseStrategy(), item.getKillerChanceModifierStrategy(), item.getDropCalculationStrategy()));
		}
		group.setItems(items);
		return group;
	}
	
	/**
	 * Creates a normalized group taking into account all drop modifiers, needed when handling a group which has items with different chance rates
	 * @param victim
	 * @param killer
	 * @return a new normalized group with all drop modifiers applied
	 */
	public final GroupedGeneralDropItem normalizeMe(L2Character victim, L2Character killer)
	{
		return normalizeMe(victim, killer, true, 1);
	}
	
	/**
	 * Creates a normalized group taking into account all drop modifiers, needed when handling a group which has items with different chance rates
	 * @param victim
	 * @param killer
	 * @param chanceModifier an additional chance modifier
	 * @return a new normalized group with all drop modifiers applied
	 */
	public final GroupedGeneralDropItem normalizeMe(L2Character victim, L2Character killer, double chanceModifier)
	{
		return normalizeMe(victim, killer, true, chanceModifier);
	}
	
	/**
	 * Creates a normalized group taking into account all drop modifiers, needed when handling a group which has items with different chance rates
	 * @param victim
	 * @return a new normalized group with all victim modifiers applied
	 */
	public final GroupedGeneralDropItem normalizeMe(L2Character victim)
	{
		return normalizeMe(victim, null, false, 1);
	}
	
	/**
	 * Creates a normalized group taking into account all drop modifiers, needed when handling a group which has items with different chance rates
	 * @param victim
	 * @param chanceModifier an additional chance modifier
	 * @return a new normalized group with all victim modifiers applied
	 */
	public final GroupedGeneralDropItem normalizeMe(L2Character victim, double chanceModifier)
	{
		return normalizeMe(victim, null, false, chanceModifier);
	}
	
	/**
	 * Creates a normalized group taking into account all drop modifiers, needed when handling a group which has items with different chance rates
	 * @param victim
	 * @param killer
	 * @param applyKillerModifier if to modify chance by {@link GroupedGeneralDropItem#getKillerChanceModifier(L2Character, L2Character)}
	 * @param chanceModifier an additional chance modifier
	 * @return a new normalized group with all drop modifiers applied
	 */
	private final GroupedGeneralDropItem normalizeMe(L2Character victim, L2Character killer, boolean applyKillerModifier, double chanceModifier)
	{
		if (applyKillerModifier)
		{
			chanceModifier *= (getKillerChanceModifier(victim, killer));
		}
		double sumchance = 0;
		for (GeneralDropItem item : getItems())
		{
			sumchance += (item.getChance(victim, killer) * getChance() * chanceModifier) / 100;
		}
		GroupedGeneralDropItem group = new GroupedGeneralDropItem(sumchance, getDropCalculationStrategy(), IKillerChanceModifierStrategy.NO_RULES, getPreciseStrategy()); // to discard further deep blue calculations
		List<GeneralDropItem> items = new ArrayList<>();
		for (GeneralDropItem item : getItems())
		{
			// the item is made almost "static"
			items.add(new GeneralDropItem(item.getItemId(), item.getMin(victim, killer), item.getMax(victim, killer), (item.getChance(victim, killer) * getChance() * chanceModifier)
				/ sumchance, IAmountMultiplierStrategy.STATIC, IChanceMultiplierStrategy.STATIC, getPreciseStrategy(), IKillerChanceModifierStrategy.NO_RULES, item.getDropCalculationStrategy()));
		}
		group.setItems(items);
		return group;
		
	}
	
	@Override
	public final List<ItemHolder> calculateDrops(L2Character victim, L2Character killer)
	{
		return _dropCalculationStrategy.calculateDrops(this, victim, killer);
	}
	
	/**
	 * This handles by default deep blue drop rules. It may also be used to handle another drop chance rules based on killer
	 * @param victim the victim who drops the item
	 * @param killer who kills the victim
	 * @return a number between 0 and 1 (usually)
	 */
	public final double getKillerChanceModifier(L2Character victim, L2Character killer)
	{
		return _killerChanceModifierStrategy.getKillerChanceModifier(this, victim, killer);
	}
	
	public boolean isPreciseCalculated()
	{
		return _preciseStrategy.isPreciseCalculated(this);
	}
}
