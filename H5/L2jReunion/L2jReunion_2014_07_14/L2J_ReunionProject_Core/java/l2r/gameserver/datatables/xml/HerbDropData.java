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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2r.gameserver.engines.DocumentParser;
import l2r.gameserver.model.L2DropCategory;
import l2r.gameserver.model.L2DropData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class HerbDropData extends DocumentParser
{
	private static final Logger _log = LoggerFactory.getLogger(HerbDropData.class);
	
	private final Map<Integer, List<L2DropCategory>> _herbGroups = new HashMap<>();
	
	public static HerbDropData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected HerbDropData()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		_herbGroups.clear();
		parseDatapackFile("data/xml/herbsDroplist.xml");
		_log.info(getClass().getSimpleName() + ": Loaded " + _herbGroups.size() + " herb groups.");
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
					if ("group".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						int groupId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
						
						List<L2DropCategory> category;
						if (_herbGroups.containsKey(groupId))
						{
							category = _herbGroups.get(groupId);
						}
						else
						{
							category = new ArrayList<>();
							_herbGroups.put(groupId, category);
						}
						
						for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							L2DropData dropDat = new L2DropData();
							if ("item".equalsIgnoreCase(cd.getNodeName()))
							{
								attrs = cd.getAttributes();
								int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
								int min = Integer.parseInt(attrs.getNamedItem("min").getNodeValue());
								int max = Integer.parseInt(attrs.getNamedItem("max").getNodeValue());
								int categoryType = Integer.parseInt(attrs.getNamedItem("category").getNodeValue());
								int chance = Integer.parseInt(attrs.getNamedItem("chance").getNodeValue());
								
								dropDat.setItemId(id);
								dropDat.setMinDrop(min);
								dropDat.setMaxDrop(max);
								dropDat.setChance(chance);
								
								if (ItemData.getInstance().getTemplate(dropDat.getId()) == null)
								{
									_log.warn(getClass().getSimpleName() + ": Data for undefined item template! GroupId: " + groupId + " itemId: " + dropDat.getId());
									continue;
								}
								
								boolean catExists = false;
								for (L2DropCategory cat : category)
								{
									if (cat.getCategoryType() == categoryType)
									{
										cat.addDropData(dropDat, false);
										catExists = true;
										break;
									}
								}
								
								if (!catExists)
								{
									L2DropCategory cat = new L2DropCategory(categoryType);
									cat.addDropData(dropDat, false);
									category.add(cat);
								}
							}
						}
					}
				}
			}
		}
	}
	
	public List<L2DropCategory> getHerbDroplist(int groupId)
	{
		return _herbGroups.get(groupId);
	}
	
	private static class SingletonHolder
	{
		protected static final HerbDropData _instance = new HerbDropData();
	}
}