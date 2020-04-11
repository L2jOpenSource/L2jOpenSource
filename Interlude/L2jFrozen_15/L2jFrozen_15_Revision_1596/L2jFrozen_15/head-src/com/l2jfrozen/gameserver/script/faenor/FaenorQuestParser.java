package com.l2jfrozen.gameserver.script.faenor;

import javax.script.ScriptContext;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.script.Parser;
import com.l2jfrozen.gameserver.script.ParserFactory;
import com.l2jfrozen.gameserver.script.ScriptEngine;

/**
 * @author Luis Arias
 */
public class FaenorQuestParser extends FaenorParser
{
	protected static final Logger LOGGER = Logger.getLogger(FaenorQuestParser.class);
	
	@Override
	public void parseScript(final Node questNode, final ScriptContext context)
	{
		if (DEBUG)
		{
			LOGGER.info("Parsing Quest.");
		}
		
		final String questID = attribute(questNode, "ID");
		
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
	
	private void parseQuestDropList(final Node dropList, final String questID) throws NullPointerException
	{
		if (DEBUG)
		{
			LOGGER.info("Parsing Droplist.");
		}
		
		for (Node node = dropList.getFirstChild(); node != null; node = node.getNextSibling())
		{
			if (isNodeName(node, "DROP"))
			{
				parseQuestDrop(node.cloneNode(true), questID);
			}
		}
	}
	
	private void parseQuestDrop(final Node drop, final String questID)// throws NullPointerException
	{
		if (DEBUG)
		{
			LOGGER.info("Parsing Drop.");
		}
		
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
			states = attribute(drop, "States").split(",");
		}
		catch (final NullPointerException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			throw new NullPointerException("Incorrect Drop Data");
		}
		
		if (DEBUG)
		{
			LOGGER.info("Adding Drop to NpcID: " + npcID);
		}
		
		bridge.addQuestDrop(npcID, itemID, min, max, chance, questID, states);
	}
	
	static class FaenorQuestParserFactory extends ParserFactory
	{
		@Override
		public Parser create()
		{
			return new FaenorQuestParser();
		}
	}
	
	static
	{
		ScriptEngine.parserFactories.put(getParserName("Quest"), new FaenorQuestParserFactory());
	}
}
