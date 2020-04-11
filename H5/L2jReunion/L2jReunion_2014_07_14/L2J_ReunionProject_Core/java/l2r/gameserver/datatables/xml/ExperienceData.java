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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This class holds the Experience points for each level for players and pets.
 * @author mrTJO
 */
public final class ExperienceData extends DocumentParser
{
	private final Map<Integer, Long> _expTable = new HashMap<>();
	
	private byte MAX_LEVEL;
	private byte MAX_PET_LEVEL;
	
	/**
	 * Instantiates a new experience table.
	 */
	protected ExperienceData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_expTable.clear();
		parseDatapackFile("data/xml/stats/experience.xml");
		_log.info(getClass().getSimpleName() + ": Loaded " + _expTable.size() + " levels.");
		_log.info(getClass().getSimpleName() + ": Max Player Level is: " + (MAX_LEVEL - 1));
		_log.info(getClass().getSimpleName() + ": Max Pet Level is: " + (MAX_PET_LEVEL - 1));
	}
	
	@Override
	protected void parseDocument()
	{
		final Node table = getCurrentDocument().getFirstChild();
		final NamedNodeMap tableAttr = table.getAttributes();
		
		MAX_LEVEL = (byte) (Byte.parseByte(tableAttr.getNamedItem("maxLevel").getNodeValue()) + 1);
		MAX_PET_LEVEL = (byte) (Byte.parseByte(tableAttr.getNamedItem("maxPetLevel").getNodeValue()) + 1);
		
		NamedNodeMap attrs;
		for (Node n = table.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("experience".equals(n.getNodeName()))
			{
				attrs = n.getAttributes();
				_expTable.put(parseInteger(attrs, "level"), parseLong(attrs, "tolevel"));
			}
		}
	}
	
	/**
	 * Gets the exp for level.
	 * @param level the level required.
	 * @return the experience points required to reach the given level.
	 */
	public long getExpForLevel(int level)
	{
		try
		{
			return _expTable.get(level);
		}
		catch (Exception e)
		{
			_log.error(getClass().getSimpleName() + " incoming level is: " + String.valueOf(level));
			
			if ((_expTable != null) && !_expTable.isEmpty())
			{
				if (_expTable.get(level) != null)
				{
					_log.error(getClass().getSimpleName() + " _expTable get(level) is: " + String.valueOf(_expTable.get(level)));
				}
				else
				{
					_log.error(getClass().getSimpleName() + " _expTable.get(level) is NULL");
				}
			}
			else
			{
				_log.error(getClass().getSimpleName() + " _expTable is NULL");
			}
			
			return 25314105600L;
		}
	}
	
	/**
	 * Gets the max level.
	 * @return the maximum level acquirable by a player.
	 */
	public byte getMaxLevel()
	{
		return MAX_LEVEL;
	}
	
	/**
	 * Gets the max pet level.
	 * @return the maximum level acquirable by a pet.
	 */
	public byte getMaxPetLevel()
	{
		return MAX_PET_LEVEL;
	}
	
	/**
	 * Gets the single instance of ExperienceTable.
	 * @return single instance of ExperienceTable
	 */
	public static ExperienceData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ExperienceData _instance = new ExperienceData();
	}
}
