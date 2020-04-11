/*
 * Copyright (C) 2004-2015 L2J Server
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
package l2r.gameserver.data.xml.impl;

import java.util.Arrays;

import l2r.util.data.xml.IXmlReader.IXmlReader;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This class holds the Player Xp Percent Lost Data for each level for players.
 * @author Zealar
 */
public final class PlayerXpPercentLostData implements IXmlReader
{
	private final int _maxlevel = ExperienceData.getInstance().getMaxLevel();
	private final double[] _playerXpPercentLost = new double[_maxlevel + 1];
	
	protected PlayerXpPercentLostData()
	{
		Arrays.fill(_playerXpPercentLost, 1.);
		load();
	}
	
	@Override
	public void load()
	{
		parseDatapackFile("data/xml/stats/chars/playerXpPercentLost.xml");
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
					if ("xpLost".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						_playerXpPercentLost[parseInteger(attrs, "level")] = parseDouble(attrs, "val");
					}
				}
			}
		}
	}
	
	public double getXpPercent(final int level)
	{
		if (level > _maxlevel)
		{
			LOGGER.warn("Require to high level inside PlayerXpPercentLostData (" + level + ")");
			return _playerXpPercentLost[_maxlevel];
		}
		return _playerXpPercentLost[level];
	}
	
	/**
	 * Gets the single instance of PlayerXpPercentLostData.
	 * @return single instance of PlayerXpPercentLostData.
	 */
	public static PlayerXpPercentLostData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PlayerXpPercentLostData _instance = new PlayerXpPercentLostData();
	}
}
