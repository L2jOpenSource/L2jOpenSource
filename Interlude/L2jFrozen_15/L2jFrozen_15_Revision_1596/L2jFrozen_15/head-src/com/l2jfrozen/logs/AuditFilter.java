package com.l2jfrozen.logs;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**
 * @author ProGramMoS, Lets drink to code!
 */
public class AuditFilter implements Filter
{
	@Override
	public boolean isLoggable(final LogRecord record)
	{
		return record.getLoggerName().equalsIgnoreCase("audit");
	}
}
