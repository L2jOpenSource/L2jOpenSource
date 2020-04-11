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
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.items.enchant.EnchantScroll;
import l2r.gameserver.model.items.enchant.EnchantSupportItem;
import l2r.gameserver.model.items.instance.L2ItemInstance;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This class holds the Enchant Item information.
 * @author UnAfraid
 */
public class EnchantItemData extends DocumentParser
{
	public static final Map<Integer, EnchantScroll> _scrolls = new HashMap<>();
	public static final Map<Integer, EnchantSupportItem> _supports = new HashMap<>();
	
	/**
	 * Instantiates a new enchant item data.
	 */
	public EnchantItemData()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		_scrolls.clear();
		_supports.clear();
		parseDatapackFile("data/xml/enchantItemData.xml");
		_log.info(getClass().getSimpleName() + ": Loaded " + _scrolls.size() + " Enchant Scrolls.");
		_log.info(getClass().getSimpleName() + ": Loaded " + _supports.size() + " Support Items.");
	}
	
	@Override
	protected void parseDocument()
	{
		StatsSet set;
		Node att;
		NamedNodeMap attrs;
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("enchant".equalsIgnoreCase(d.getNodeName()))
					{
						attrs = d.getAttributes();
						set = new StatsSet();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}
						
						try
						{
							final EnchantScroll item = new EnchantScroll(set);
							for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
							{
								if ("item".equalsIgnoreCase(cd.getNodeName()))
								{
									item.addItem(parseInteger(cd.getAttributes(), "id"));
								}
							}
							_scrolls.put(item.getId(), item);
						}
						catch (NullPointerException e)
						{
							_log.warn(getClass().getSimpleName() + ": Unexistent enchant scroll: " + set.getString("id") + " defined in enchant data!");
						}
						catch (IllegalAccessError e)
						{
							_log.warn(getClass().getSimpleName() + ": Wrong enchant scroll item type: " + set.getString("id") + " defined in enchant data!");
						}
					}
					else if ("support".equalsIgnoreCase(d.getNodeName()))
					{
						attrs = d.getAttributes();
						set = new StatsSet();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}
						
						try
						{
							final EnchantSupportItem item = new EnchantSupportItem(set);
							_supports.put(item.getId(), item);
						}
						catch (NullPointerException e)
						{
							_log.warn(getClass().getSimpleName() + ": Unexistent enchant support item: " + set.getString("id") + " defined in enchant data!");
						}
						catch (IllegalAccessError e)
						{
							_log.warn(getClass().getSimpleName() + ": Wrong enchant support item type: " + set.getString("id") + " defined in enchant data!");
						}
					}
				}
			}
		}
	}
	
	/**
	 * Gets the enchant scroll.
	 * @param scroll the scroll
	 * @return enchant template for scroll
	 */
	public final EnchantScroll getEnchantScroll(L2ItemInstance scroll)
	{
		return _scrolls.get(scroll.getId());
	}
	
	/**
	 * Gets the support item.
	 * @param item the item
	 * @return enchant template for support item
	 */
	public final EnchantSupportItem getSupportItem(L2ItemInstance item)
	{
		return _supports.get(item.getId());
	}
	
	/**
	 * Gets the single instance of EnchantItemData.
	 * @return single instance of EnchantItemData
	 */
	public static final EnchantItemData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EnchantItemData _instance = new EnchantItemData();
	}
}
