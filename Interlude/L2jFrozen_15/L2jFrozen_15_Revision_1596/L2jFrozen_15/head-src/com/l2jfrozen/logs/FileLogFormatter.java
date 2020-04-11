package com.l2jfrozen.logs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import javolution.text.TextBuilder;

/**
 * This class ...
 * @version $Revision: 1.1.4.1 $ $Date: 2005/03/27 15:30:08 $
 * @author  ProGramMoS
 */

public class FileLogFormatter extends Formatter
{
	private static final String CRLF = "\r\n";
	private static final String tab = "\t";
	private final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss,SSS");
	
	@Override
	public String format(final LogRecord record)
	{
		final TextBuilder output = new TextBuilder();
		
		return output.append(dateFmt.format(new Date(record.getMillis()))).append(tab).append(record.getLevel().getName()).append(tab).append(record.getThreadID()).append(tab).append(record.getLoggerName()).append(tab).append(record.getMessage()).append(CRLF).toString();
	}
}
