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
package l2r.gameserver.script.faenor;

import javax.script.ScriptContext;

import l2r.gameserver.script.Parser;
import l2r.gameserver.script.ParserFactory;
import l2r.gameserver.script.ScriptEngine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * @author Luis Arias
 */
public class FaenorQuestParser extends FaenorParser
{
	protected static final Logger _log = LoggerFactory.getLogger(FaenorQuestParser.class);
	
	@Override
	public void parseScript(Node questNode, ScriptContext context)
	{
		String questID = attribute(questNode, "ID");
		
		for (Node node = questNode.getFirstChild(); node != null; node = node.getNextSibling())
		{
			if (isNodeName(node, "DROPLIST"))
			{
				parseQuestDropList(node.cloneNode(true), questID);
			}
			else if (isNodeName(node, "DIALOG WINDOWS"))
			{
				// parseDialogWindows(node.cloneNode(true));
			}
			else if (isNodeName(node, "INITIATOR"))
			{
				// parseInitiator(node.cloneNode(true));
			}
			else if (isNodeName(node, "STATE"))
			{
				// parseState(node.cloneNode(true));
			}
		}
	}
	
	private void parseQuestDropList(Node dropList, String questID) throws NullPointerException
	{
		for (Node node = dropList.getFirstChild(); node != null; node = node.getNextSibling())
		{
			if (isNodeName(node, "DROP"))
			{
				parseQuestDrop(node.cloneNode(true), questID);
			}
		}
	}
	
	private void parseQuestDrop(Node drop, String questID)
	{
		int npcID;
		int itemID;
		int min;
		int max;
		int chance;
		String[] states;
		try
		{
			npcID = getInt(attribute(drop, "NpcID"));
			itemID = getInt(attribute(drop, "ItemID"));
			min = getInt(attribute(drop, "Min"));
			max = getInt(attribute(drop, "Max"));
			chance = getInt(attribute(drop, "Chance"));
			states = (attribute(drop, "States")).split(",");
		}
		catch (NullPointerException e)
		{
			throw new NullPointerException("Incorrect Drop Data");
		}
		
		_bridge.addQuestDrop(npcID, itemID, min, max, chance, questID, states);
	}
	
	static class FaenorQuestParserFactory extends ParserFactory
	{
		@Override
		public Parser create()
		{
			return (new FaenorQuestParser());
		}
	}
	
	static
	{
		ScriptEngine.parserFactories.put(getParserName("Quest"), new FaenorQuestParserFactory());
	}
}
