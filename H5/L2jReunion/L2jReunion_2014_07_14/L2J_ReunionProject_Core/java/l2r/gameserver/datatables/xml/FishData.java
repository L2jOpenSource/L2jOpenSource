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
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.fishing.L2Fish;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This class holds the Fish information.
 * @author nonom
 */
public final class FishData extends DocumentParser
{
	private static final Map<Integer, L2Fish> _fishNormal = new HashMap<>();
	private static final Map<Integer, L2Fish> _fishEasy = new HashMap<>();
	private static final Map<Integer, L2Fish> _fishHard = new HashMap<>();
	
	/**
	 * Instantiates a new fish data.
	 */
	protected FishData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_fishEasy.clear();
		_fishNormal.clear();
		_fishHard.clear();
		parseDatapackFile("data/xml/stats/fishing/fishes.xml");
		_log.info(getClass().getSimpleName() + ": Loaded " + (_fishEasy.size() + _fishNormal.size() + _fishHard.size()) + " Fishes.");
	}
	
	@Override
	protected void parseDocument()
	{
		NamedNodeMap attrs;
		Node att;
		L2Fish fish;
		StatsSet set;
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("fish".equalsIgnoreCase(d.getNodeName()))
					{
						attrs = d.getAttributes();
						
						set = new StatsSet();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}
						fish = new L2Fish(set);
						switch (fish.getFishGrade())
						{
							case 0:
							{
								_fishEasy.put(fish.getFishId(), fish);
								break;
							}
							case 1:
							{
								_fishNormal.put(fish.getFishId(), fish);
								break;
							}
							case 2:
							{
								_fishHard.put(fish.getFishId(), fish);
								break;
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Gets the fish.
	 * @param level the fish Level
	 * @param group the fish Group
	 * @param grade the fish Grade
	 * @return List of Fish that can be fished
	 */
	public List<L2Fish> getFish(int level, int group, int grade)
	{
		final ArrayList<L2Fish> result = new ArrayList<>();
		Map<Integer, L2Fish> fish = null;
		switch (grade)
		{
			case 0:
			{
				fish = _fishEasy;
				break;
			}
			case 1:
			{
				fish = _fishNormal;
				break;
			}
			case 2:
			{
				fish = _fishHard;
				break;
			}
			default:
			{
				_log.warn(getClass().getSimpleName() + ": Unmanaged fish grade!");
				return result;
			}
		}
		
		for (L2Fish f : fish.values())
		{
			if ((f.getFishLevel() != level) || (f.getFishGroup() != group))
			{
				continue;
			}
			result.add(f);
		}
		
		if (result.isEmpty())
		{
			_log.warn(getClass().getSimpleName() + ": Cannot find any fish for level: " + level + " group: " + group + " and grade: " + grade + "!");
		}
		return result;
	}
	
	/**
	 * Gets the single instance of FishData.
	 * @return single instance of FishData
	 */
	public static FishData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final FishData _instance = new FishData();
	}
}
