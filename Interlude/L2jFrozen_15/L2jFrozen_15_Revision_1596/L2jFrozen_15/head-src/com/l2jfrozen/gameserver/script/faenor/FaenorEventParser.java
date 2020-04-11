package com.l2jfrozen.gameserver.script.faenor;

import java.util.Date;

import javax.script.ScriptContext;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.script.DateRange;
import com.l2jfrozen.gameserver.script.IntList;
import com.l2jfrozen.gameserver.script.Parser;
import com.l2jfrozen.gameserver.script.ParserFactory;
import com.l2jfrozen.gameserver.script.ScriptEngine;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * @author Luis Arias
 */
public class FaenorEventParser extends FaenorParser
{
	static Logger LOGGER = Logger.getLogger(FaenorEventParser.class);
	private DateRange eventDates = null;
	
	@Override
	public void parseScript(final Node eventNode, final ScriptContext context)
	{
		final String ID = attribute(eventNode, "ID");
		
		if (DEBUG)
		{
			LOGGER.debug("Parsing Event \"" + ID + "\"");
		}
		
		eventDates = DateRange.parse(attribute(eventNode, "Active"), DATE_FORMAT);
		
		final Date currentDate = new Date();
		if (eventDates.getEndDate().before(currentDate))
		{
			LOGGER.info("Event ID: (" + ID + ") has passed... Ignored.");
			return;
		}
		
		if (eventDates.getStartDate().after(currentDate))
		{
			LOGGER.info("Event ID: (" + ID + ") is not active yet... Ignored.");
			ThreadPoolManager.getInstance().scheduleGeneral(() -> parseEventDropAndMessage(eventNode), eventDates.getStartDate().getTime() - currentDate.getTime());
			return;
		}
		
		parseEventDropAndMessage(eventNode);
	}
	
	protected void parseEventDropAndMessage(final Node eventNode)
	{
		
		for (Node node = eventNode.getFirstChild(); node != null; node = node.getNextSibling())
		{
			
			if (isNodeName(node, "DropList"))
			{
				parseEventDropList(node);
			}
			else if (isNodeName(node, "Message"))
			{
				parseEventMessage(node);
			}
		}
	}
	
	private void parseEventMessage(final Node sysMsg)
	{
		if (DEBUG)
		{
			LOGGER.debug("Parsing Event Message.");
		}
		
		try
		{
			final String type = attribute(sysMsg, "Type");
			final String[] message = attribute(sysMsg, "Msg").split("\n");
			
			if (type.equalsIgnoreCase("OnJoin"))
			{
				bridge.onPlayerLogin(message, eventDates);
			}
		}
		catch (final Exception e)
		{
			LOGGER.warn("Error in event parser.");
			e.printStackTrace();
		}
	}
	
	private void parseEventDropList(final Node dropList)
	{
		if (DEBUG)
		{
			LOGGER.debug("Parsing Droplist.");
		}
		
		for (Node node = dropList.getFirstChild(); node != null; node = node.getNextSibling())
		{
			if (isNodeName(node, "AllDrop"))
			{
				parseEventDrop(node);
			}
		}
	}
	
	private void parseEventDrop(final Node drop)
	{
		if (DEBUG)
		{
			LOGGER.debug("Parsing Drop.");
		}
		
		try
		{
			final int[] items = IntList.parse(attribute(drop, "Items"));
			final int[] count = IntList.parse(attribute(drop, "Count"));
			final double chance = getPercent(attribute(drop, "Chance"));
			
			bridge.addEventDrop(items, count, chance, eventDates);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.warn("ERROR(parseEventDrop):" + e.getMessage());
		}
	}
	
	static class FaenorEventParserFactory extends ParserFactory
	{
		@Override
		public Parser create()
		{
			return new FaenorEventParser();
		}
	}
	
	static
	{
		ScriptEngine.parserFactories.put(getParserName("Event"), new FaenorEventParserFactory());
	}
}
