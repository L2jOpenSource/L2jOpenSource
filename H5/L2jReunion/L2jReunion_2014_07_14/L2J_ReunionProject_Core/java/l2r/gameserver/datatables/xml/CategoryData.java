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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import l2r.gameserver.engines.DocumentParser;
import l2r.gameserver.enums.CategoryType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This class holds different categories containing class ids or npc ids.
 * @author Nos, xban1x
 */
public final class CategoryData extends DocumentParser
{
	private static final Logger _log = LoggerFactory.getLogger(CategoryData.class);
	
	private final Map<CategoryType, Set<Integer>> _categories = new HashMap<>();
	
	protected CategoryData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseDatapackFile("data/xml/categoryData.xml");
		_log.info(getClass().getSimpleName() + ": Loaded " + _categories.size() + " Categories.");
	}
	
	@Override
	protected void parseDocument()
	{
		for (Node node = getCurrentDocument().getFirstChild(); node != null; node = node.getNextSibling())
		{
			if ("list".equalsIgnoreCase(node.getNodeName()))
			{
				for (Node list_node = node.getFirstChild(); list_node != null; list_node = list_node.getNextSibling())
				{
					if ("category".equalsIgnoreCase(list_node.getNodeName()))
					{
						final NamedNodeMap attrs = list_node.getAttributes();
						final CategoryType categoryType = CategoryType.findByName(attrs.getNamedItem("name").getNodeValue());
						if (categoryType == null)
						{
							_log.warn(getClass().getSimpleName() + ": Can't find category by name :" + attrs.getNamedItem("name").getNodeValue());
							continue;
						}
						
						final Set<Integer> ids = new HashSet<>();
						for (Node category_node = list_node.getFirstChild(); category_node != null; category_node = category_node.getNextSibling())
						{
							if ("id".equalsIgnoreCase(category_node.getNodeName()))
							{
								ids.add(Integer.parseInt(category_node.getTextContent()));
							}
						}
						_categories.put(categoryType, ids);
					}
				}
			}
		}
	}
	
	/**
	 * Checks if id is in category.
	 * @param type The category type.
	 * @param id The id to be checked.
	 * @return {@code true} if id is in category, {@code false} if id is not in category or category was not found.
	 */
	public boolean isInCategory(CategoryType type, int id)
	{
		final Set<Integer> category = getCategoryByType(type);
		if (category == null)
		{
			_log.warn(getClass().getSimpleName() + ": Can't find category type :" + type);
			return false;
		}
		return category.contains(id);
	}
	
	/**
	 * @param type The category type
	 * @return A {@code Set} containing all the ids in category if category is found, {@code null} if category was not found.
	 */
	public Set<Integer> getCategoryByType(CategoryType type)
	{
		return _categories.get(type);
	}
	
	public static CategoryData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected final static CategoryData _instance = new CategoryData();
	}
}
