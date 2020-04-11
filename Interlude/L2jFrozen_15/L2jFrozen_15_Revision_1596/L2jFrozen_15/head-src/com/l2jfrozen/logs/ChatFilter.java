package com.l2jfrozen.logs;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class ChatFilter implements Filter
{
	@Override
	public boolean isLoggable(final LogRecord record)
	{
		return record.getLoggerName() == "chat";
	}
}
