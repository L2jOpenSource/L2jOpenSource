package com.l2jfrozen.logs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import com.l2jfrozen.Config;

import javolution.text.TextBuilder;

/**
 * This class ...
 * @version $Revision: 1.1.4.2 $ $Date: 2005/03/27 15:30:08 $
 * @author  ProGramMoS
 */

public class ConsoleLogFormatter extends Formatter
{
	private static final String CRLF = "\r\n";
	
	@Override
	public String format(final LogRecord record)
	{
		final TextBuilder output = new TextBuilder();
		output.append(record.getMessage());
		output.append(CRLF);
		if (record.getThrown() != null)
		{
			try
			{
				final StringWriter sw = new StringWriter();
				final PrintWriter pw = new PrintWriter(sw);
				record.getThrown().printStackTrace(pw);
				pw.close();
				output.append(sw.toString());
				output.append(CRLF);
			}
			catch (final Exception ex)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					ex.printStackTrace();
				}
				
			}
		}
		
		return output.toString();
	}
}
