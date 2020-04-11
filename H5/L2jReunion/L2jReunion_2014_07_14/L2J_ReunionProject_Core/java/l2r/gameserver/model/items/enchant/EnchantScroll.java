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
package l2r.gameserver.model.items.enchant;

import java.util.HashSet;
import java.util.Set;

import l2r.Config;
import l2r.gameserver.datatables.xml.EnchantItemGroupsData;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.items.type.EtcItemType;
import l2r.gameserver.model.items.type.ItemType;
import l2r.gameserver.network.Debug;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

/**
 * @author UnAfraid
 */
public final class EnchantScroll extends AbstractEnchantItem
{
	private final boolean _isWeapon;
	private final boolean _isBlessed;
	private final boolean _isSafe;
	private final int _scrollGroupId;
	private Set<Integer> _items;
	
	public EnchantScroll(StatsSet set)
	{
		super(set);
		_scrollGroupId = set.getInteger("scrollGroupId", 0);
		
		final ItemType type = getItem().getItemType();
		_isWeapon = (type == EtcItemType.ANCIENT_CRYSTAL_ENCHANT_WP) || (type == EtcItemType.BLESS_SCRL_ENCHANT_WP) || (type == EtcItemType.SCRL_ENCHANT_WP);
		_isBlessed = (type == EtcItemType.BLESS_SCRL_ENCHANT_AM) || (type == EtcItemType.BLESS_SCRL_ENCHANT_WP);
		_isSafe = (type == EtcItemType.ANCIENT_CRYSTAL_ENCHANT_AM) || (type == EtcItemType.ANCIENT_CRYSTAL_ENCHANT_WP);
	}
	
	@Override
	public boolean isWeapon()
	{
		return _isWeapon;
	}
	
	/**
	 * @return {@code true} for blessed scrolls (enchanted item will remain on failure), {@code false} otherwise
	 */
	public boolean isBlessed()
	{
		return _isBlessed;
	}
	
	/**
	 * @return {@code true} for safe-enchant scrolls (enchant level will remain on failure), {@code false} otherwise
	 */
	public boolean isSafe()
	{
		return _isSafe;
	}
	
	/**
	 * @return id of scroll group that should be used
	 */
	public int getScrollGroupId()
	{
		return _scrollGroupId;
	}
	
	/**
	 * Enforces current scroll to use only those items as possible items to enchant
	 * @param itemId
	 */
	public void addItem(int itemId)
	{
		if (_items == null)
		{
			_items = new HashSet<>();
		}
		_items.add(itemId);
	}
	
	/**
	 * @param itemToEnchant the item to be enchanted
	 * @param supportItem the support item used when enchanting (can be null)
	 * @return {@code true} if this scroll can be used with the specified support item and the item to be enchanted, {@code false} otherwise
	 */
	@Override
	public boolean isValid(L2ItemInstance itemToEnchant, EnchantSupportItem supportItem)
	{
		if ((_items != null) && !_items.contains(itemToEnchant.getId()))
		{
			return false;
		}
		else if ((supportItem != null))
		{
			if (isBlessed())
			{
				return false;
			}
			else if (!supportItem.isValid(itemToEnchant, supportItem))
			{
				return false;
			}
			else if (supportItem.isWeapon() != isWeapon())
			{
				return false;
			}
		}
		return super.isValid(itemToEnchant, supportItem);
	}
	
	/**
	 * @param player
	 * @param enchantItem
	 * @return the chance of current scroll's group.
	 */
	public double getChance(L2PcInstance player, L2ItemInstance enchantItem)
	{
		if (EnchantItemGroupsData.getInstance().getScrollGroup(_scrollGroupId) == null)
		{
			_log.warn(getClass().getSimpleName() + ": Unexistent enchant scroll group specified for enchant scroll: " + getId());
			return -1;
		}
		
		final EnchantItemGroup group = EnchantItemGroupsData.getInstance().getItemGroup(enchantItem.getItem(), _scrollGroupId);
		if (group == null)
		{
			_log.warn(getClass().getSimpleName() + ": Couldn't find enchant item group for scroll: " + getId() + " requested by: " + player);
			return -1;
		}
		return group.getChance(enchantItem.getEnchantLevel());
	}
	
	/**
	 * @param player
	 * @param enchantItem
	 * @param supportItem
	 * @return the total chance for success rate of this scroll
	 */
	public EnchantResultType calculateSuccess(L2PcInstance player, L2ItemInstance enchantItem, EnchantSupportItem supportItem)
	{
		if (!isValid(enchantItem, supportItem))
		{
			return EnchantResultType.ERROR;
		}
		
		boolean fullBody = enchantItem.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR;
		if ((enchantItem.getEnchantLevel() < Config.ENCHANT_SAFE_MAX) || (fullBody && (enchantItem.getEnchantLevel() < Config.ENCHANT_SAFE_MAX_FULL)))
		{
			return EnchantResultType.SUCCESS;
		}
		
		final double chance = getChance(player, enchantItem);
		if (chance == -1)
		{
			return EnchantResultType.ERROR;
		}
		
		final double bonusRate = getBonusRate();
		final double supportBonusRate = (supportItem != null) ? supportItem.getBonusRate() : 0;
		final double finalChance = Math.min(chance + bonusRate + supportBonusRate, 100);
		
		final double random = 100 * Rnd.nextDouble();
		final boolean success = (random < finalChance);
		
		if (player.isDebug())
		{
			final EnchantItemGroup group = EnchantItemGroupsData.getInstance().getItemGroup(enchantItem.getItem(), _scrollGroupId);
			final StatsSet set = new StatsSet();
			if (isBlessed())
			{
				set.set("isBlessed", isBlessed());
			}
			if (isSafe())
			{
				set.set("isSafe", isSafe());
			}
			set.set("chance", Util.formatDouble(chance, "#.##"));
			if (bonusRate > 0)
			{
				set.set("bonusRate", Util.formatDouble(bonusRate, "#.##"));
			}
			if (supportBonusRate > 0)
			{
				set.set("supportBonusRate", Util.formatDouble(supportBonusRate, "#.##"));
			}
			set.set("finalChance", Util.formatDouble(finalChance, "#.##"));
			set.set("random", Util.formatDouble(random, "#.##"));
			set.set("success", success);
			set.set("item group", group.getName());
			set.set("scroll group", _scrollGroupId);
			Debug.sendItemDebug(player, enchantItem, set);
		}
		return success ? EnchantResultType.SUCCESS : EnchantResultType.FAILURE;
	}
}
