package com.l2jfrozen.logs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import javolution.text.TextBuilder;

/**
 * This class ...
 * @version $Revision: 1.1.4.1 $ $Date: 2005/02/06 16:14:46 $
 */

public class ChatLogFormatter extends Formatter
{
	private static final String CRLF = "\r\n";
	
	private final SimpleDateFormat dateFmt = new SimpleDateFormat("dd MMM H:mm:ss");
	
	@Override
	public String format(final LogRecord record)
	{
		final Object[] params = record.getParameters();
		final TextBuilder output = new TextBuilder();
		output.append('[');
		output.append(dateFmt.format(new Date(record.getMillis())));
		output.append(']');
		output.append(' ');
		if (params != null)
		{
			for (final Object p : params)
			{
				output.append(p);
				output.append(' ');
			}
		}
		output.append(record.getMessage());
		output.append(CRLF);
		
		return output.toString();
	}
}
