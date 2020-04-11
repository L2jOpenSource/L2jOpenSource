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

import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.engines.DocumentParser;
import l2r.gameserver.model.holders.RangeChanceHolder;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.items.enchant.EnchantItemGroup;
import l2r.gameserver.model.items.enchant.EnchantRateItem;
import l2r.gameserver.model.items.enchant.EnchantScrollGroup;
import l2r.gameserver.util.Util;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author UnAfraid
 */
public final class EnchantItemGroupsData extends DocumentParser
{
	private final Map<String, EnchantItemGroup> _itemGroups = new HashMap<>();
	private final Map<Integer, EnchantScrollGroup> _scrollGroups = new HashMap<>();
	
	protected EnchantItemGroupsData()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		_itemGroups.clear();
		_scrollGroups.clear();
		parseDatapackFile("data/xml/enchantItemGroups.xml");
		_log.info(getClass().getSimpleName() + ": Loaded: " + _itemGroups.size() + " item group templates.");
		_log.info(getClass().getSimpleName() + ": Loaded: " + _scrollGroups.size() + " scroll group templates.");
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
					if ("enchantRateGroup".equalsIgnoreCase(d.getNodeName()))
					{
						String name = parseString(d.getAttributes(), "name");
						final EnchantItemGroup group = new EnchantItemGroup(name);
						for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							if ("current".equalsIgnoreCase(cd.getNodeName()))
							{
								String range = parseString(cd.getAttributes(), "enchant");
								double chance = parseDouble(cd.getAttributes(), "chance");
								int min = -1;
								int max = 0;
								if (range.contains("-"))
								{
									String[] split = range.split("-");
									if ((split.length == 2) && Util.isDigit(split[0]) && Util.isDigit(split[1]))
									{
										min = Integer.parseInt(split[0]);
										max = Integer.parseInt(split[1]);
									}
								}
								else if (Util.isDigit(range))
								{
									min = Integer.parseInt(range);
									max = min;
								}
								if ((min > -1) && (max > 0))
								{
									group.addChance(new RangeChanceHolder(min, max, chance));
								}
							}
						}
						_itemGroups.put(name, group);
					}
					else if ("enchantScrollGroup".equals(d.getNodeName()))
					{
						int id = parseInteger(d.getAttributes(), "id");
						final EnchantScrollGroup group = new EnchantScrollGroup(id);
						for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							if ("enchantRate".equalsIgnoreCase(cd.getNodeName()))
							{
								final EnchantRateItem rateGroup = new EnchantRateItem(parseString(cd.getAttributes(), "group"));
								for (Node z = cd.getFirstChild(); z != null; z = z.getNextSibling())
								{
									if ("item".equals(z.getNodeName()))
									{
										final NamedNodeMap attrs = z.getAttributes();
										if (attrs.getNamedItem("slot") != null)
										{
											rateGroup.addSlot(ItemData._slots.get(parseString(attrs, "slot")));
										}
										if (attrs.getNamedItem("magicWeapon") != null)
										{
											rateGroup.setMagicWeapon(parseBoolean(attrs, "magicWeapon"));
										}
										if (attrs.getNamedItem("id") != null)
										{
											rateGroup.setItemId(parseInteger(attrs, "id"));
										}
									}
								}
								group.addRateGroup(rateGroup);
							}
						}
						_scrollGroups.put(id, group);
					}
				}
			}
		}
	}
	
	public EnchantItemGroup getItemGroup(L2Item item, int scrollGroup)
	{
		final EnchantScrollGroup group = _scrollGroups.get(scrollGroup);
		final EnchantRateItem rateGroup = group.getRateGroup(item);
		return rateGroup != null ? _itemGroups.get(rateGroup.getName()) : null;
	}
	
	public EnchantItemGroup getItemGroup(String name)
	{
		return _itemGroups.get(name);
	}
	
	public EnchantScrollGroup getScrollGroup(int id)
	{
		return _scrollGroups.get(id);
	}
	
	public static EnchantItemGroupsData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EnchantItemGroupsData _instance = new EnchantItemGroupsData();
	}
}
