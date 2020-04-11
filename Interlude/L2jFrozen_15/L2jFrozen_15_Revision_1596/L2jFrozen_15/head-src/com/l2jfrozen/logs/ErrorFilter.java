package com.l2jfrozen.logs;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**
 * @author ProGramMoS
 */

public class ErrorFilter implements Filter
{
	@Override
	public boolean isLoggable(final LogRecord record)
	{
		return record.getThrown() != null;
	}
	
}
