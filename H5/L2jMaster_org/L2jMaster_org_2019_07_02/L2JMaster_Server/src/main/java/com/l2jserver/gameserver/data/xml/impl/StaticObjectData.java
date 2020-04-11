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
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.instance.L2StaticObjectInstance;
import com.l2jserver.gameserver.model.actor.templates.L2CharTemplate;
import com.l2jserver.util.data.xml.IXmlReader;

/**
 * This class loads and holds all static object data.
 * @author UnAfraid
 */
public final class StaticObjectData implements IXmlReader
{
	private final Map<Integer, L2StaticObjectInstance> _staticObjects = new HashMap<>();
	
	/**
	 * Instantiates a new static objects.
	 */
	protected StaticObjectData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_staticObjects.clear();
		parseDatapackFile("data/staticObjects.xml");
		LOG.info("{}: Loaded {} static object templates.", getClass().getSimpleName(), _staticObjects.size());
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
					if ("object".equalsIgnoreCase(d.getNodeName()))
					{
						final NamedNodeMap attrs = d.getAttributes();
						final StatsSet set = new StatsSet();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							final Node att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}
						addObject(set);
					}
				}
			}
		}
	}
	
	/**
	 * Initialize an static object based on the stats set and add it to the map.
	 * @param set the stats set to add.
	 */
	private void addObject(StatsSet set)
	{
		L2StaticObjectInstance obj = new L2StaticObjectInstance(new L2CharTemplate(new StatsSet()), set.getInt("id"));
		obj.setType(set.getInt("type", 0));
		obj.setName(set.getString("name"));
		obj.setMap(set.getString("texture", "none"), set.getInt("map_x", 0), set.getInt("map_y", 0));
		obj.spawnMe(set.getInt("x"), set.getInt("y"), set.getInt("z"));
		_staticObjects.put(obj.getObjectId(), obj);
	}
	
	/**
	 * Gets the static objects.
	 * @return a collection of static objects.
	 */
	public Collection<L2StaticObjectInstance> getStaticObjects()
	{
		return _staticObjects.values();
	}
	
	/**
	 * Gets the single instance of StaticObjects.
	 * @return single instance of StaticObjects
	 */
	public static StaticObjectData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final StaticObjectData _instance = new StaticObjectData();
	}
}
