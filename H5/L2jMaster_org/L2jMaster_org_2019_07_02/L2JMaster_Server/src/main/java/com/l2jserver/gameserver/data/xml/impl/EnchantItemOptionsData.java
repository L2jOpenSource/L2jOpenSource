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
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.options.EnchantOptions;
import com.l2jserver.gameserver.util.Util;
import com.l2jserver.util.data.xml.IXmlReader;

/**
 * @author UnAfraid
 */
public class EnchantItemOptionsData implements IXmlReader
{
	private final Map<Integer, Map<Integer, EnchantOptions>> _data = new HashMap<>();
	
	protected EnchantItemOptionsData()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		_data.clear();
		parseDatapackFile("data/enchantItemOptions.xml");
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		int counter = 0;
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("item".equalsIgnoreCase(d.getNodeName()))
					{
						int itemId = parseInteger(d.getAttributes(), "id");
						if (!_data.containsKey(itemId))
						{
							_data.put(itemId, new HashMap<Integer, EnchantOptions>());
						}
						for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							if ("options".equalsIgnoreCase(cd.getNodeName()))
							{
								final EnchantOptions op = new EnchantOptions(parseInteger(cd.getAttributes(), "level"));
								_data.get(itemId).put(op.getLevel(), op);
								
								for (byte i = 0; i < 3; i++)
								{
									final Node att = cd.getAttributes().getNamedItem("option" + (i + 1));
									if ((att != null) && Util.isDigit(att.getNodeValue()))
									{
										op.setOption(i, parseInteger(att));
									}
								}
								counter++;
							}
						}
					}
				}
			}
		}
		LOG.info("{}: Loaded: {} Items and {} Options.", getClass().getSimpleName(), _data.size(), counter);
	}
	
	/**
	 * @param itemId
	 * @param enchantLevel
	 * @return enchant effects information.
	 */
	public EnchantOptions getOptions(int itemId, int enchantLevel)
	{
		if (!_data.containsKey(itemId) || !_data.get(itemId).containsKey(enchantLevel))
		{
			return null;
		}
		return _data.get(itemId).get(enchantLevel);
	}
	
	/**
	 * @param item
	 * @return enchant effects information.
	 */
	public EnchantOptions getOptions(L2ItemInstance item)
	{
		return item != null ? getOptions(item.getId(), item.getEnchantLevel()) : null;
	}
	
	/**
	 * Gets the single instance of EnchantOptionsData.
	 * @return single instance of EnchantOptionsData
	 */
	public static final EnchantItemOptionsData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EnchantItemOptionsData _instance = new EnchantItemOptionsData();
	}
}
