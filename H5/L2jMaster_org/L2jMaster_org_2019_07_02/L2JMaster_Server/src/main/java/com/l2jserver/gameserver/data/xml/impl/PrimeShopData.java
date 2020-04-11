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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jserver.gameserver.model.PrimeShop;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.util.data.xml.IXmlReader;

/**
 * Loads Prime Shop from data files.
 * @author U3Games
 */
public final class PrimeShopData implements IXmlReader
{
	private final Map<Integer, PrimeShop> _primeShop = new ConcurrentHashMap<>();
	
	/**
	 * Instantiates a new prime shop data.
	 */
	public PrimeShopData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_primeShop.clear();
		parseDatapackFile("data/primeShop.xml");
		LOG.info("{}: Loaded {} items of Prime Shop.", getClass().getSimpleName(), _primeShop.size());
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("product".equalsIgnoreCase(d.getNodeName()))
					{
						final NamedNodeMap attrs = d.getAttributes();
						final StatsSet set = new StatsSet();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							final Node att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}
						final PrimeShop values = new PrimeShop(set);
						_primeShop.put(values.getProductId(), values);
					}
				}
			}
		}
	}
	
	public Collection<PrimeShop> getProductValues()
	{
		return _primeShop.values();
	}
	
	public PrimeShop getProduct(int id)
	{
		return _primeShop.get(id);
	}
	
	public static PrimeShopData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PrimeShopData _instance = new PrimeShopData();
	}
}