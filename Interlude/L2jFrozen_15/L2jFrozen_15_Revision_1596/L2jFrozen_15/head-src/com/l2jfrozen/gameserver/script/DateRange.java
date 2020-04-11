package com.l2jfrozen.gameserver.script;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * @author Luis Arias
 */
public class DateRange
{
	private static Logger LOGGER = Logger.getLogger(DateRange.class);
	
	private final Date startDate, endDate;
	
	public DateRange(final Date from, final Date to)
	{
		startDate = from;
		endDate = to;
	}
	
	public static DateRange parse(final String dateRange, final DateFormat format)
	{
		final String[] date = dateRange.split("-");
		if (date.length == 2)
		{
			try
			{
				final Date start = format.parse(date[0]);
				final Date end = format.parse(date[1]);
				
				return new DateRange(start, end);
			}
			catch (final ParseException e)
			{
				LOGGER.error("Invalid Date Format.");
				e.printStackTrace();
			}
		}
		return new DateRange(null, null);
	}
	
	public boolean isValid()
	{
		return startDate == null || endDate == null;
	}
	
	public boolean isWithinRange(final Date date)
	{
		return date.after(startDate) && date.before(endDate);
	}
	
	public Date getEndDate()
	{
		return endDate;
	}
	
	public Date getStartDate()
	{
		return startDate;
	}
}
