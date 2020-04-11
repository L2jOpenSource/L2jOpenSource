package com.l2jfrozen.logs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import javolution.text.TextBuilder;

/**
 * @author ProGramMoS, Lets drink to code!
 */
public class AuditFormatter extends Formatter
{
	private static final String CRLF = "\r\n";
	private final SimpleDateFormat dateFmt = new SimpleDateFormat("dd MMM H:mm:ss");
	
	@Override
	public String format(final LogRecord record)
	{
		final TextBuilder output = new TextBuilder();
		output.append('[');
		output.append(dateFmt.format(new Date(record.getMillis())));
		output.append(']');
		output.append(' ');
		output.append(record.getMessage());
		for (final Object p : record.getParameters())
		{
			if (p == null)
			{
				continue;
			}
			output.append(',');
			output.append(' ');
			output.append(p.toString());
		}
		output.append(CRLF);
		
		return output.toString();
	}
}
