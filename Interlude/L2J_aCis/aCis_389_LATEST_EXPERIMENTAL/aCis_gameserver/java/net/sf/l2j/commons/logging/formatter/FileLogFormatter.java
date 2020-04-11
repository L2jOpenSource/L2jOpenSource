package net.sf.l2j.commons.logging.formatter;

import java.util.logging.LogRecord;

import net.sf.l2j.commons.logging.MasterFormatter;

public class FileLogFormatter extends MasterFormatter
{
	@Override
	public String format(LogRecord record)
	{
		return "[" + getFormatedDate(record.getMillis()) + "]" + SPACE + record.getLevel().getName() + SPACE + record.getMessage() + CRLF;
	}
}