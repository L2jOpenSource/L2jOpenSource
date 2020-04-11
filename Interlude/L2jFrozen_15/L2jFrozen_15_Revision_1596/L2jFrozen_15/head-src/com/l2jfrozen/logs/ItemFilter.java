package com.l2jfrozen.logs;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;

/**
 * @author Advi
 */

public class ItemFilter implements Filter
{
	private String excludeProcess;
	private String excludeItemType;
	
	// This is example how to exclude consuming of shots and arrows from logging
	// private String excludeProcess = "Consume";
	// private String excludeItemType = "Arrow, Shot";
	
	@Override
	public boolean isLoggable(final LogRecord record)
	{
		if (record.getLoggerName() != "item")
		{
			return false;
		}
		
		if (excludeProcess != null)
		{
			// if (record.getMessage() == null) return true;
			final String[] messageList = record.getMessage().split(":");
			
			if (messageList.length < 2 || !excludeProcess.contains(messageList[1]))
			{
				return true;
			}
		}
		if (excludeItemType != null)
		{
			// if (record.getParameters() == null || record.getParameters().length == 0 || !(record.getParameters()[0] instanceof L2ItemInstance))
			// return true;
			final L2ItemInstance item = (L2ItemInstance) record.getParameters()[0];
			
			if (!excludeItemType.contains(item.getItemType().toString()))
			{
				return true;
			}
		}
		return excludeProcess == null && excludeItemType == null;
	}
}
