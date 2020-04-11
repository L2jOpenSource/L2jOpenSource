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
package l2r.gameserver.datatables.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import l2r.gameserver.engines.DocumentParser;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.items.type.CrystalType;
import l2r.gameserver.model.skills.funcs.FuncTemplate;
import l2r.gameserver.model.skills.funcs.LambdaConst;
import l2r.gameserver.model.stats.Stats;

import org.w3c.dom.Node;

/**
 * This class holds the Enchant HP Bonus Data.
 * @author MrPoke, Zoey76
 */
public class EnchantItemHPBonusData extends DocumentParser
{
	private final Map<CrystalType, List<Integer>> _armorHPBonuses = new EnumMap<>(CrystalType.class);
	
	private static final float fullArmorModifier = 1.5f; // TODO: Move it to config!
	
	/**
	 * Instantiates a new enchant hp bonus data.
	 */
	protected EnchantItemHPBonusData()
	{
		load();
	}
	
	@Override
	protected void parseDocument()
	{
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("enchantHP".equalsIgnoreCase(d.getNodeName()))
					{
						List<Integer> bonuses = new ArrayList<>();
						for (Node e = d.getFirstChild(); e != null; e = e.getNextSibling())
						{
							if ("bonus".equalsIgnoreCase(e.getNodeName()))
							{
								bonuses.add(Integer.valueOf(e.getTextContent()));
							}
						}
						_armorHPBonuses.put(parseEnum(d.getAttributes(), CrystalType.class, "grade"), bonuses);
					}
				}
			}
		}
		
		if (!_armorHPBonuses.isEmpty())
		{
			final ItemData it = ItemData.getInstance();
			L2Item item;
			// Armors
			final Collection<Integer> armorIds = it.getAllArmorsId();
			for (Integer itemId : armorIds)
			{
				item = it.getTemplate(itemId);
				if ((item != null) && (item.getCrystalType() != CrystalType.NONE))
				{
					switch (item.getBodyPart())
					{
						case L2Item.SLOT_CHEST:
						case L2Item.SLOT_FEET:
						case L2Item.SLOT_GLOVES:
						case L2Item.SLOT_HEAD:
						case L2Item.SLOT_LEGS:
						case L2Item.SLOT_BACK:
						case L2Item.SLOT_FULL_ARMOR:
						case L2Item.SLOT_UNDERWEAR:
						case L2Item.SLOT_L_HAND:
							item.attach(new FuncTemplate(null, null, "EnchantHp", Stats.MAX_HP, 0x60, new LambdaConst(0)));
							break;
						default:
							break;
					}
				}
			}
			
			// Shields
			final Collection<Integer> shieldIds = it.getAllWeaponsId();
			for (Integer itemId : shieldIds)
			{
				item = it.getTemplate(itemId);
				if ((item != null) && (item.getCrystalType() != CrystalType.NONE))
				{
					switch (item.getBodyPart())
					{
						case L2Item.SLOT_L_HAND:
							item.attach(new FuncTemplate(null, null, "EnchantHp", Stats.MAX_HP, 0x60, new LambdaConst(0)));
							break;
						default:
							break;
					}
				}
			}
		}
	}
	
	@Override
	public void load()
	{
		_armorHPBonuses.clear();
		parseDatapackFile("data/xml/stats/enchantHPBonus.xml");
		_log.info(getClass().getSimpleName() + ": Loaded " + _armorHPBonuses.size() + " Enchant HP Bonuses.");
	}
	
	/**
	 * Gets the HP bonus.
	 * @param item the item
	 * @return the HP bonus
	 */
	public final int getHPBonus(L2ItemInstance item)
	{
		final List<Integer> values = _armorHPBonuses.get(item.getItem().getItemGradeSPlus());
		if ((values == null) || values.isEmpty() || (item.getOlyEnchantLevel() <= 0))
		{
			return 0;
		}
		
		final int bonus = values.get(Math.min(item.getOlyEnchantLevel(), values.size()) - 1);
		if (item.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR)
		{
			return (int) (bonus * fullArmorModifier);
		}
		return bonus;
	}
	
	/**
	 * Gets the single instance of EnchantHPBonusData.
	 * @return single instance of EnchantHPBonusData
	 */
	public static final EnchantItemHPBonusData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EnchantItemHPBonusData _instance = new EnchantItemHPBonusData();
	}
}
