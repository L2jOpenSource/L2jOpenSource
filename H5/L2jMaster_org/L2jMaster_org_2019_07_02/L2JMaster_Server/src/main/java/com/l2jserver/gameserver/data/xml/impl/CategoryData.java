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
package com.l2jserver.gameserver.data.xml.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jserver.gameserver.enums.CategoryType;
import com.l2jserver.util.data.xml.IXmlReader;

/**
 * Loads the category data with Class or NPC IDs.
 * @author NosBit, xban1x
 */
public final class CategoryData implements IXmlReader
{
	private final Map<CategoryType, Set<Integer>> _categories = new HashMap<>();
	
	protected CategoryData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_categories.clear();
		parseDatapackFile("data/categoryData.xml");
		LOG.info("{}: Loaded {} Categories.", getClass().getSimpleName(), _categories.size());
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		for (Node node = doc.getFirstChild(); node != null; node = node.getNextSibling())
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
							LOG.warn("{}: Can't find category by name :{}", getClass().getSimpleName(), attrs.getNamedItem("name").getNodeValue());
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
	 * Checks if ID is in category.
	 * @param type The category type
	 * @param id The id to be checked
	 * @return {@code true} if id is in category, {@code false} if id is not in category or category was not found
	 */
	public boolean isInCategory(CategoryType type, int id)
	{
		final Set<Integer> category = getCategoryByType(type);
		if (category == null)
		{
			LOG.warn("{}: Can't find category type: {}", getClass().getSimpleName(), type);
			return false;
		}
		return category.contains(id);
	}
	
	/**
	 * Gets the category by category type.
	 * @param type The category type
	 * @return A {@code Set} containing all the IDs in category if category is found, {@code null} if category was not found
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
		protected static final CategoryData _instance = new CategoryData();
	}
}
