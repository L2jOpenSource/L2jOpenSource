package com.l2jfrozen.logs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;

import javolution.text.TextBuilder;

public class ItemLogFormatter extends Formatter
{
	private static final String CRLF = "\r\n";
	private final SimpleDateFormat dateFmt = new SimpleDateFormat("dd MMM H:mm:ss");
	
	@Override
	public String format(final LogRecord record)
	{
		final Object[] params = record.getParameters();
		final TextBuilder output = new TextBuilder();
		
		output.append("[" + dateFmt.format(new Date(record.getMillis())) + "] ");
		output.append(record.getMessage() + " ");
		
		if (params != null)
		{
			for (final Object p : params)
			{
				if (p == null)
				{
					continue;
				}
				output.append("| ");
				if (p instanceof L2ItemInstance)
				{
					final L2ItemInstance item = (L2ItemInstance) p;
					output.append("item " + item.getObjectId() + ": ");
					if (item.getEnchantLevel() > 0)
					{
						output.append("+" + item.getEnchantLevel() + " ");
					}
					output.append(item.getItem().getName() + "(" + item.getCount() + ")");
				}
				else
				{
					output.append(p.toString());
				}
			}
		}
		output.append(CRLF);
		return output.toString();
	}
	
}
