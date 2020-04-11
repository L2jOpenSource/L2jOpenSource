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
import l2r.gameserver.model.fishing.L2FishingRod;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This class holds the Fishing Rods information.
 * @author nonom
 */
public final class FishingRodsData extends DocumentParser
{
	private static final Map<Integer, L2FishingRod> _fishingRods = new HashMap<>();
	
	/**
	 * Instantiates a new fishing rods data.
	 */
	protected FishingRodsData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_fishingRods.clear();
		parseDatapackFile("data/xml/stats/fishing/fishingRods.xml");
		_log.info(getClass().getSimpleName() + ": Loaded " + _fishingRods.size() + " Fishing Rods.");
	}
	
	@Override
	protected void parseDocument()
	{
		NamedNodeMap attrs;
		Node att;
		L2FishingRod fishingRod;
		StatsSet set;
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("fishingRod".equalsIgnoreCase(d.getNodeName()))
					{
						
						attrs = d.getAttributes();
						
						set = new StatsSet();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}
						fishingRod = new L2FishingRod(set);
						_fishingRods.put(fishingRod.getFishingRodItemId(), fishingRod);
					}
				}
			}
		}
	}
	
	/**
	 * Gets the fishing rod.
	 * @param itemId the item id
	 * @return A fishing Rod by Item Id
	 */
	public L2FishingRod getFishingRod(int itemId)
	{
		return _fishingRods.get(itemId);
	}
	
	/**
	 * Gets the single instance of FishingRodsData.
	 * @return single instance of FishingRodsData
	 */
	public static FishingRodsData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final FishingRodsData _instance = new FishingRodsData();
	}
}
