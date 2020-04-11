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

import javolution.util.FastList;
import l2r.Config;
import l2r.gameserver.SevenSigns;
import l2r.gameserver.model.itemcontainer.Inventory;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

/**
 * @author Fulminus
 */
public class L2DropCategory
{
	private final FastList<L2DropData> _drops;
	private int _categoryChance; // a sum of chances for calculating if an item will be dropped from this category
	private int _categoryBalancedChance; // sum for balancing drop selection inside categories in high rate servers
	private final int _categoryType;
	
	public L2DropCategory(int categoryType)
	{
		_categoryType = categoryType;
		_drops = new FastList<>(0);
		_categoryChance = 0;
		_categoryBalancedChance = 0;
	}
	
	public void addDropData(L2DropData drop, boolean raid)
	{
		boolean found = false;
		
		if (drop.isQuestDrop())
		{
			// if (_questDrops == null)
			// _questDrops = new FastList<>(0);
			// _questDrops.add(drop);
		}
		else
		{
			if (Config.CUSTOM_DROPLIST_TABLE)
			{
				// If the drop exists is replaced
				for (L2DropData d : _drops)
				{
					if (d.getId() == drop.getId())
					{
						d.setMinDrop(drop.getMinDrop());
						d.setMaxDrop(drop.getMaxDrop());
						if (Math.abs(d.getChance() - drop.getChance()) >= 1e-6)
						{
							// Re-calculate Chance
							_categoryChance -= d.getChance();
							_categoryBalancedChance -= Math.min((d.getChance() * Config.RATE_DROP_ITEMS), L2DropData.MAX_CHANCE);
							d.setChance(drop.getChance());
							_categoryChance += d.getChance();
							_categoryBalancedChance += Math.min((d.getChance() * Config.RATE_DROP_ITEMS), L2DropData.MAX_CHANCE);
						}
						found = true;
						break;
					}
				}
			}
			
			if (!found)
			{
				_drops.add(drop);
				_categoryChance += drop.getChance();
				// for drop selection inside a category: max 100 % chance for getting an item, scaling all values to that.
				_categoryBalancedChance += Math.min((drop.getChance() * (raid ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS)), L2DropData.MAX_CHANCE);
			}
		}
	}
	
	public FastList<L2DropData> getAllDrops()
	{
		return _drops;
	}
	
	public void clearAllDrops()
	{
		_drops.clear();
	}
	
	public boolean isSweep()
	{
		return (getCategoryType() == -1);
	}
	
	// this returns the chance for the category to be visited in order to check if
	// drops might come from it. Category -1 (spoil) must always be visited
	// (but may return 0 or many drops)
	public int getCategoryChance()
	{
		if (getCategoryType() >= 0)
		{
			return _categoryChance;
		}
		return L2DropData.MAX_CHANCE;
	}
	
	public int getCategoryBalancedChance()
	{
		if (getCategoryType() >= 0)
		{
			return _categoryBalancedChance;
		}
		return L2DropData.MAX_CHANCE;
	}
	
	public int getCategoryType()
	{
		return _categoryType;
	}
	
	/**
	 * Useful for seeded conditions... the category will attempt to drop only among items that are allowed to be dropped when a mob is seeded.<br>
	 * Previously, this only included Adena. According to sh1ny, seals tones are also acceptable drops.<br>
	 * If no acceptable drops are in the category, nothing will be dropped.<br>
	 * therwise, it will check for the item's chance to drop and either drop it or drop nothing.
	 * @return acceptable drop when mob is seeded, if it exists. Null otherwise.
	 */
	public synchronized L2DropData dropSeedAllowedDropsOnly()
	{
		FastList<L2DropData> drops = new FastList<>();
		int subCatChance = 0;
		for (L2DropData drop : getAllDrops())
		{
			if ((drop.getId() == Inventory.ADENA_ID) || Util.contains(SevenSigns.SEAL_STONE_IDS, drop.getId()))
			{
				drops.add(drop);
				subCatChance += drop.getChance();
			}
		}
		
		// among the results choose one.
		int randomIndex = Rnd.get(subCatChance);
		int sum = 0;
		for (L2DropData drop : drops)
		{
			sum += drop.getChance();
			
			if (sum > randomIndex) // drop this item and exit the function
			{
				drops.clear();
				drops = null;
				return drop;
			}
		}
		// since it is still within category, only drop one of the acceptable drops from the results.
		return null;
	}
	
	/**
	 * One of the drops in this category is to be dropped now.<br>
	 * to see which one will be dropped, weight all items' chances such that their sum of chances equals MAX_CHANCE.<br>
	 * Since the individual drops have their base chance, we also ought to use the base category chance for the weight.<br>
	 * So weight = MAX_CHANCE/basecategoryDropChance.<br>
	 * Then get a single random number within this range. The first item (in order of the list) whose contribution to the sum makes the sum greater than the random number, will be dropped.<br>
	 * Edited: How _categoryBalancedChance works in high rate servers:<br>
	 * Let's say item1 has a drop chance (when considered alone, without category) of 1 % * RATE_DROP_ITEMS and item2 has 20 % * RATE_DROP_ITEMS, and the server's RATE_DROP_ITEMS is for example 50x.<br>
	 * Without this balancer, the relative chance inside the category to select item1 to be dropped would be 1/26 and item2 25/26, no matter what rates are used.<br>
	 * In high rate servers people usually consider the 1 % individual drop chance should become higher than this relative chance (1/26) inside the category, since having the both items for example in their own categories would result in having a drop chance for item1 50 % and item2 1000 %.<br>
	 * _categoryBalancedChance limits the individual chances to 100 % max, making the chance for item1 to be selected from this category 50/(50+100) = 1/3 and item2 100/150 = 2/3.<br>
	 * This change doesn't affect calculation when drop_chance * RATE_DROP_ITEMS < 100%, meaning there are no big changes for low rate servers and no changes at all for 1x servers.
	 * @param raid
	 * @return selected drop from category, or null if nothing is dropped.
	 */
	public synchronized L2DropData dropOne(boolean raid)
	{
		int randomIndex = Rnd.get(getCategoryBalancedChance());
		int sum = 0;
		for (L2DropData drop : getAllDrops())
		{
			sum += Math.min((drop.getChance() * (raid ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS)), L2DropData.MAX_CHANCE);
			
			if (sum >= randomIndex)
			{
				return drop;
			}
		}
		return null;
	}
}
