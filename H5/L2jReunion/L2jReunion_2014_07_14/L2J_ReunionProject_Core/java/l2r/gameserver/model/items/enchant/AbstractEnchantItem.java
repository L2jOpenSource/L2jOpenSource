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

import l2r.gameserver.datatables.xml.ItemData;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.items.type.CrystalType;
import l2r.gameserver.model.items.type.EtcItemType;
import l2r.gameserver.model.items.type.ItemType;
import l2r.gameserver.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author UnAfraid
 */
public abstract class AbstractEnchantItem
{
	protected static final Logger _log = LoggerFactory.getLogger(AbstractEnchantItem.class.getName());
	
	private static final ItemType[] ENCHANT_TYPES = new ItemType[]
	{
		EtcItemType.ANCIENT_CRYSTAL_ENCHANT_AM,
		EtcItemType.ANCIENT_CRYSTAL_ENCHANT_WP,
		EtcItemType.BLESS_SCRL_ENCHANT_AM,
		EtcItemType.BLESS_SCRL_ENCHANT_WP,
		EtcItemType.SCRL_ENCHANT_AM,
		EtcItemType.SCRL_ENCHANT_WP,
		EtcItemType.SCRL_INC_ENCHANT_PROP_AM,
		EtcItemType.SCRL_INC_ENCHANT_PROP_WP,
	};
	
	private final int _id;
	private final CrystalType _grade;
	private final int _maxEnchantLevel;
	private final double _bonusRate;
	
	public AbstractEnchantItem(StatsSet set)
	{
		_id = set.getInteger("id");
		if (getItem() == null)
		{
			throw new NullPointerException();
		}
		else if (!Util.contains(ENCHANT_TYPES, getItem().getItemType()))
		{
			throw new IllegalAccessError();
		}
		_grade = set.getEnum("targetGrade", CrystalType.class, CrystalType.NONE);
		_maxEnchantLevel = set.getInteger("maxEnchant", 65535);
		_bonusRate = set.getDouble("bonusRate", 0);
	}
	
	/**
	 * @return id of current item
	 */
	public final int getId()
	{
		return _id;
	}
	
	/**
	 * @return bonus chance that would be added
	 */
	public final double getBonusRate()
	{
		return _bonusRate;
	}
	
	/**
	 * @return {@link L2Item} current item/scroll
	 */
	public final L2Item getItem()
	{
		return ItemData.getInstance().getTemplate(_id);
	}
	
	/**
	 * @return grade of the item/scroll.
	 */
	public final CrystalType getGrade()
	{
		return _grade;
	}
	
	/**
	 * @return {@code true} if scroll is for weapon, {@code false} for armor
	 */
	public abstract boolean isWeapon();
	
	/**
	 * @return the maximum enchant level that this scroll/item can be used with
	 */
	public int getMaxEnchantLevel()
	{
		return _maxEnchantLevel;
	}
	
	/**
	 * @param itemToEnchant the item to be enchanted
	 * @param supportItem
	 * @return {@code true} if this support item can be used with the item to be enchanted, {@code false} otherwise
	 */
	public boolean isValid(L2ItemInstance itemToEnchant, EnchantSupportItem supportItem)
	{
		if (itemToEnchant == null)
		{
			return false;
		}
		else if (itemToEnchant.isEnchantable() == 0)
		{
			return false;
		}
		else if (!isValidItemType(itemToEnchant.getItem().getType2()))
		{
			return false;
		}
		else if ((_maxEnchantLevel != 0) && (itemToEnchant.getEnchantLevel() >= _maxEnchantLevel))
		{
			return false;
		}
		else if (_grade != itemToEnchant.getItem().getItemGradeSPlus())
		{
			return false;
		}
		return true;
	}
	
	/**
	 * @param type2
	 * @return {@code true} if current type2 is valid to be enchanted, {@code false} otherwise
	 */
	private final boolean isValidItemType(int type2)
	{
		if (type2 == L2Item.TYPE2_WEAPON)
		{
			return isWeapon();
		}
		else if ((type2 == L2Item.TYPE2_SHIELD_ARMOR) || (type2 == L2Item.TYPE2_ACCESSORY))
		{
			return !isWeapon();
		}
		return false;
	}
}
