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

import java.util.List;

import com.l2jserver.Config;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.drops.strategy.IAmountMultiplierStrategy;
import com.l2jserver.gameserver.model.drops.strategy.IChanceMultiplierStrategy;
import com.l2jserver.gameserver.model.drops.strategy.IDropCalculationStrategy;
import com.l2jserver.gameserver.model.drops.strategy.IKillerChanceModifierStrategy;
import com.l2jserver.gameserver.model.drops.strategy.INonGroupedKillerChanceModifierStrategy;
import com.l2jserver.gameserver.model.drops.strategy.IPreciseDeterminationStrategy;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.itemcontainer.Inventory;

import WDConfig.ConfigWD;

/**
 * @author NosBit
 */
public final class GeneralDropItem implements IDropItem
{
	private final int _itemId;
	private final long _min;
	private final long _max;
	private final double _chance;
	
	protected final IAmountMultiplierStrategy _amountStrategy;
	protected final IChanceMultiplierStrategy _chanceStrategy;
	protected final IPreciseDeterminationStrategy _preciseStrategy;
	protected final INonGroupedKillerChanceModifierStrategy _killerStrategy;
	protected final IDropCalculationStrategy _dropCalculationStrategy;
	
	/**
	 * @param itemId the item id
	 * @param min the min count
	 * @param max the max count
	 * @param chance the chance of this drop item
	 */
	public GeneralDropItem(int itemId, long min, long max, double chance)
	{
		this(itemId, min, max, chance, 1, 1);
	}
	
	public GeneralDropItem(int itemId, long min, long max, double chance, double defaultAmountMultiplier, double defaultChanceMultiplier)
	{
		this(itemId, min, max, defaultChanceMultiplier, IAmountMultiplierStrategy.DEFAULT_STRATEGY(defaultAmountMultiplier), IChanceMultiplierStrategy.DEFAULT_STRATEGY(defaultChanceMultiplier));
	}
	
	public GeneralDropItem(int itemId, long min, long max, double chance, IAmountMultiplierStrategy amountMultiplierStrategy, IChanceMultiplierStrategy chanceMultiplierStrategy)
	{
		this(itemId, min, max, chance, amountMultiplierStrategy, chanceMultiplierStrategy, IPreciseDeterminationStrategy.DEFAULT, IKillerChanceModifierStrategy.DEFAULT_NONGROUP_STRATEGY);
	}
	
	public GeneralDropItem(int itemId, long min, long max, double chance, IAmountMultiplierStrategy amountMultiplierStrategy, IChanceMultiplierStrategy chanceMultiplierStrategy, IPreciseDeterminationStrategy preciseStrategy, INonGroupedKillerChanceModifierStrategy killerStrategy)
	{
		this(itemId, min, max, chance, amountMultiplierStrategy, chanceMultiplierStrategy, preciseStrategy, killerStrategy, IDropCalculationStrategy.DEFAULT_STRATEGY);
	}
	
	public GeneralDropItem(int itemId, long min, long max, double chance, IAmountMultiplierStrategy amountMultiplierStrategy, IChanceMultiplierStrategy chanceMultiplierStrategy, IPreciseDeterminationStrategy preciseStrategy, INonGroupedKillerChanceModifierStrategy killerStrategy, IDropCalculationStrategy dropCalculationStrategy)
	{
		_itemId = itemId;
		_min = min;
		_max = max;
		_chance = chance;
		_amountStrategy = amountMultiplierStrategy;
		_chanceStrategy = chanceMultiplierStrategy;
		_preciseStrategy = preciseStrategy;
		_killerStrategy = killerStrategy;
		_dropCalculationStrategy = dropCalculationStrategy;
		
	}
	
	/**
	 * @return the _amountStrategy
	 */
	public final IAmountMultiplierStrategy getAmountStrategy()
	{
		return _amountStrategy;
	}
	
	/**
	 * @return the _chanceStrategy
	 */
	public final IChanceMultiplierStrategy getChanceStrategy()
	{
		return _chanceStrategy;
	}
	
	/**
	 * @return the _preciseStrategy
	 */
	public final IPreciseDeterminationStrategy getPreciseStrategy()
	{
		return _preciseStrategy;
	}
	
	/**
	 * @return the _killerStrategy
	 */
	public final INonGroupedKillerChanceModifierStrategy getKillerChanceModifierStrategy()
	{
		return _killerStrategy;
	}
	
	/**
	 * @return the _dropCalculationStrategy
	 */
	public final IDropCalculationStrategy getDropCalculationStrategy()
	{
		return _dropCalculationStrategy;
	}
	
	/**
	 * Gets the item id
	 * @return the item id
	 */
	public final int getItemId()
	{
		return _itemId;
	}
	
	/**
	 * Gets the base min drop count
	 * @return the min
	 */
	public final long getMin()
	{
		return _min;
	}
	
	/**
	 * Gets the min drop count modified by server rates
	 * @param victim the victim who drops the item
	 * @param killer
	 * @return the min modified by any rates.
	 */
	public final long getMin(L2Character victim, L2Character killer)
	{
		if (Config.PREMIUM_SYSTEM_ENABLED && killer.isPlayer() && killer.getActingPlayer().hasPremiumStatus())
		{
			if (Config.PREMIUM_RATE_DROP_AMOUNT_MULTIPLIER.get(_itemId) != null)
			{
				if (killer.getActingPlayer().hasAdenaRune() && (_itemId == Inventory.ADENA_ID) && (_amountStrategy == IAmountMultiplierStrategy.DROP))
				{
					return (long) (getMin() * getAmountMultiplier(victim) * Config.PREMIUM_RATE_DROP_AMOUNT_MULTIPLIER.get(_itemId) * ConfigWD.WD_RUNE_ADENA_AMOUNT_MULTIPLIER);
				}
				return (long) (getMin() * getAmountMultiplier(victim) * Config.PREMIUM_RATE_DROP_AMOUNT_MULTIPLIER.get(_itemId));
			}
			
			if (killer.getActingPlayer().hasAdenaRune() && (_itemId == Inventory.ADENA_ID) && (_amountStrategy == IAmountMultiplierStrategy.DROP))
			{
				return (long) (getMin() * getAmountMultiplier(victim) * Config.PREMIUM_RATE_DROP_AMOUNT * ConfigWD.WD_RUNE_ADENA_AMOUNT_MULTIPLIER);
			}
			return (long) (getMin() * getAmountMultiplier(victim) * Config.PREMIUM_RATE_DROP_AMOUNT);
		}
		
		if (killer.getActingPlayer().hasAdenaRune() && (_itemId == Inventory.ADENA_ID) && (_amountStrategy == IAmountMultiplierStrategy.DROP))
		{
			return (long) (getMin() * getAmountMultiplier(victim) * ConfigWD.WD_RUNE_ADENA_AMOUNT_MULTIPLIER);
		}
		return (long) (getMin() * getAmountMultiplier(victim));
	}
	
	/**
	 * Gets the base max drop count
	 * @return the max
	 */
	public final long getMax()
	{
		return _max;
	}
	
	/**
	 * Gets the max drop count modified by server rates
	 * @param victim the victim who drops the item
	 * @param killer
	 * @return the max modified by any rates.
	 */
	public final long getMax(L2Character victim, L2Character killer)
	{
		if (Config.PREMIUM_SYSTEM_ENABLED && killer.isPlayer() && killer.getActingPlayer().hasPremiumStatus())
		{
			if (Config.PREMIUM_RATE_DROP_AMOUNT_MULTIPLIER.get(_itemId) != null)
			{
				if (killer.getActingPlayer().hasAdenaRune() && (_itemId == Inventory.ADENA_ID) && (_amountStrategy == IAmountMultiplierStrategy.DROP))
				{
					return (long) (getMax() * getAmountMultiplier(victim) * Config.PREMIUM_RATE_DROP_AMOUNT_MULTIPLIER.get(_itemId) * ConfigWD.WD_RUNE_ADENA_AMOUNT_MULTIPLIER);
				}
				return (long) (getMax() * getAmountMultiplier(victim) * Config.PREMIUM_RATE_DROP_AMOUNT_MULTIPLIER.get(_itemId));
			}
			if (killer.getActingPlayer().hasAdenaRune() && (_itemId == Inventory.ADENA_ID) && (_amountStrategy == IAmountMultiplierStrategy.DROP))
			{
				return (long) (getMax() * getAmountMultiplier(victim) * Config.PREMIUM_RATE_DROP_AMOUNT * ConfigWD.WD_RUNE_ADENA_AMOUNT_MULTIPLIER);
			}
			return (long) (getMax() * getAmountMultiplier(victim) * Config.PREMIUM_RATE_DROP_AMOUNT);
		}
		if (killer.getActingPlayer().hasAdenaRune() && (_itemId == Inventory.ADENA_ID) && (_amountStrategy == IAmountMultiplierStrategy.DROP))
		{
			return (long) (getMax() * getAmountMultiplier(victim) * ConfigWD.WD_RUNE_ADENA_AMOUNT_MULTIPLIER);
		}
		return (long) (getMax() * getAmountMultiplier(victim));
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
	 * Gets the general chance to drop this item modified by rates. <br>
	 * This shall be used in calculating chance within drop groups.
	 * @param victim the victim who drops the item
	 * @return the chance modified by any rates.
	 */
	public final double getChance(L2Character victim)
	{
		return getChance() * getChanceMultiplier(victim);
	}
	
	/**
	 * Gets the chance of dropping this item for current killer and victim (modified by server rates and another rules based on killer) <br>
	 * This shall be used to calculate chance outside of drop groups.
	 * @param victim the victim who drops the item
	 * @param killer who kills the victim
	 * @return a chance to drop modified by deep blue drop rules
	 */
	public final double getChance(L2Character victim, L2Character killer)
	{
		// Blue Spoil not increase chance at the moment
		if (ConfigWD.WD_BLUE_SPOIL && (_chanceStrategy == IChanceMultiplierStrategy.SPOIL))
		{
			if (killer.getActingPlayer().hasSpoilRune() && (_chanceStrategy == IChanceMultiplierStrategy.SPOIL))
			{
				return getChance(victim) * ConfigWD.WD_RUNE_SPOIL_CHANCE_MULTIPLIER;
			}
			return getChance(victim);
		}
		
		if (Config.PREMIUM_SYSTEM_ENABLED && killer.isPlayer() && killer.getActingPlayer().hasPremiumStatus())
		{
			if (Config.PREMIUM_RATE_DROP_CHANCE_MULTIPLIER.get(_itemId) != null)
			{
				if (killer.getActingPlayer().hasSpoilRune() && (_chanceStrategy == IChanceMultiplierStrategy.SPOIL))
				{
					return getKillerChanceModifier(victim, killer) * getChance(victim) * Config.PREMIUM_RATE_DROP_CHANCE_MULTIPLIER.get(_itemId) * ConfigWD.WD_RUNE_SPOIL_CHANCE_MULTIPLIER;
				}
				return getKillerChanceModifier(victim, killer) * getChance(victim) * Config.PREMIUM_RATE_DROP_CHANCE_MULTIPLIER.get(_itemId);
			}
			if (killer.getActingPlayer().hasSpoilRune() && (_chanceStrategy == IChanceMultiplierStrategy.SPOIL))
			{
				return getKillerChanceModifier(victim, killer) * getChance(victim) * Config.PREMIUM_RATE_DROP_CHANCE * ConfigWD.WD_RUNE_SPOIL_CHANCE_MULTIPLIER;
			}
			return getKillerChanceModifier(victim, killer) * getChance(victim) * Config.PREMIUM_RATE_DROP_CHANCE;
		}
		if (killer.getActingPlayer().hasSpoilRune() && (_chanceStrategy == IChanceMultiplierStrategy.SPOIL))
		{
			return getKillerChanceModifier(victim, killer) * getChance(victim) * ConfigWD.WD_RUNE_SPOIL_CHANCE_MULTIPLIER;
		}
		return getKillerChanceModifier(victim, killer) * getChance(victim);
	}
	
	@Override
	public final List<ItemHolder> calculateDrops(L2Character victim, L2Character killer)
	{
		return _dropCalculationStrategy.calculateDrops(this, victim, killer);
	}
	
	/**
	 * @return <code>true</code> if chance over 100% should be handled
	 */
	public final boolean isPreciseCalculated()
	{
		return _preciseStrategy.isPreciseCalculated(this);
	}
	
	/**
	 * This handles by default deep blue drop rules. It may also be used to handle another drop chance rules based on killer
	 * @param victim the victim who drops the item
	 * @param killer who kills the victim
	 * @return a number between 0 and 1 (usually)
	 */
	protected final double getKillerChanceModifier(L2Character victim, L2Character killer)
	{
		return _killerStrategy.getKillerChanceModifier(this, victim, killer);
	}
	
	/**
	 * This gets standard server rates for this item
	 * @param victim who drops the item
	 * @return
	 */
	protected final double getAmountMultiplier(L2Character victim)
	{
		return _amountStrategy.getAmountMultiplier(this, victim);
	}
	
	/**
	 * This gets standard server rates for this item
	 * @param victim who drops the item
	 * @return
	 */
	protected final double getChanceMultiplier(L2Character victim)
	{
		return _chanceStrategy.getChanceMultiplier(this, victim);
	}
}
