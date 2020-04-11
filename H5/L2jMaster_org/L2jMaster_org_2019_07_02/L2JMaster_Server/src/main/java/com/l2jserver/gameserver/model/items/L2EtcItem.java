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
package com.l2jserver.gameserver.model.items;

import java.util.ArrayList;
import java.util.List;

import com.l2jserver.gameserver.model.L2ExtractableProduct;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.itemcontainer.Inventory;
import com.l2jserver.gameserver.model.items.type.EtcItemType;
import com.l2jserver.gameserver.model.items.type.ItemType1;
import com.l2jserver.gameserver.model.items.type.ItemType2;
import com.l2jserver.util.StringUtil;

/**
 * This class is dedicated to the management of EtcItem.
 */
public final class L2EtcItem extends L2Item
{
	private String _handler;
	private EtcItemType _type;
	private final boolean _isBlessed;
	private final List<L2ExtractableProduct> _extractableItems;
	
	/**
	 * Constructor for EtcItem.
	 * @param set StatsSet designating the set of couples (key,value) for description of the Etc
	 */
	public L2EtcItem(StatsSet set)
	{
		super(set);
		_type = set.getEnum("etcitem_type", EtcItemType.class, EtcItemType.NONE);
		
		// l2j custom - L2EtcItemType.SHOT
		switch (getDefaultAction())
		{
			case SOULSHOT:
			case SUMMON_SOULSHOT:
			case SUMMON_SPIRITSHOT:
			case SPIRITSHOT:
			{
				_type = EtcItemType.SHOT;
				break;
			}
		}
		
		_type1 = ItemType1.ITEM_QUESTITEM_ADENA;
		_type2 = ItemType2.OTHER; // default is other
		
		if (isQuestItem())
		{
			_type2 = ItemType2.QUEST;
		}
		else if ((getId() == Inventory.ADENA_ID) || (getId() == Inventory.ANCIENT_ADENA_ID))
		{
			_type2 = ItemType2.MONEY;
		}
		
		_handler = set.getString("handler", null); // ! null !
		_isBlessed = set.getBoolean("blessed", false);
		
		// Extractable
		String capsuled_items = set.getString("capsuled_items", null);
		if (capsuled_items != null)
		{
			String[] split = capsuled_items.split(";");
			_extractableItems = new ArrayList<>(split.length);
			for (String part : split)
			{
				if (part.trim().isEmpty())
				{
					continue;
				}
				String[] data = part.split(",");
				if (data.length != 4)
				{
					_log.info(StringUtil.concat("> Couldnt parse ", part, " in capsuled_items! item ", toString()));
					continue;
				}
				int itemId = Integer.parseInt(data[0]);
				int min = Integer.parseInt(data[1]);
				int max = Integer.parseInt(data[2]);
				double chance = Double.parseDouble(data[3]);
				if (max < min)
				{
					_log.info(StringUtil.concat("> Max amount < Min amount in ", part, ", item ", toString()));
					continue;
				}
				L2ExtractableProduct product = new L2ExtractableProduct(itemId, min, max, chance);
				_extractableItems.add(product);
			}
			((ArrayList<?>) _extractableItems).trimToSize();
			
			// check for handler
			if (_handler == null)
			{
				_log.warning("Item " + this + " define capsuled_items but missing handler.");
				_handler = "ExtractableItems";
			}
		}
		else
		{
			_extractableItems = null;
		}
	}
	
	/**
	 * @return the type of Etc Item.
	 */
	@Override
	public EtcItemType getItemType()
	{
		return _type;
	}
	
	/**
	 * @return the ID of the Etc item after applying the mask.
	 */
	@Override
	public int getItemMask()
	{
		return getItemType().mask();
	}
	
	/**
	 * @return the handler name, null if no handler for item.
	 */
	public String getHandlerName()
	{
		return _handler;
	}
	
	/**
	 * @return {@code true} if the item is blessed, {@code false} otherwise.
	 */
	public final boolean isBlessed()
	{
		return _isBlessed;
	}
	
	/**
	 * @return the extractable items list.
	 */
	public List<L2ExtractableProduct> getExtractableItems()
	{
		return _extractableItems;
	}
}
