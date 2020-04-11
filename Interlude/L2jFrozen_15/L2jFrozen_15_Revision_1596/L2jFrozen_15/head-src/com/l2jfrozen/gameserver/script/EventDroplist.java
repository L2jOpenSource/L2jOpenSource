package com.l2jfrozen.gameserver.script;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class manage drop of Special Events created by GM for a defined period. During a Special Event all L2Attackable can drop extra Items. Those extra Items are defined in the table <B>allNpcDateDrops</B>. Each Special Event has a start and end date to stop to drop extra Items automaticaly.<BR>
 * <BR>
 */
public class EventDroplist
{
	private static EventDroplist instance;
	
	/** The table containing all DataDrop object */
	private final List<DateDrop> allNpcDateDrops;
	
	public static EventDroplist getInstance()
	{
		if (instance == null)
		{
			instance = new EventDroplist();
		}
		
		return instance;
	}
	
	public class DateDrop
	{
		/** Start and end date of the Event */
		public DateRange dateRange;
		
		/** The table containing Item identifier that can be dropped as extra Items during the Event */
		public int[] items;
		
		/** The min number of Item dropped in one time during this Event */
		public int min;
		
		/** The max number of Item dropped in one time during this Event */
		public int max;
		
		/** The rate of drop for this Event */
		public int chance;
	}
	
	/**
	 * Constructor of EventDroplist.<BR>
	 * <BR>
	 */
	private EventDroplist()
	{
		allNpcDateDrops = new ArrayList<>();
	}
	
	/**
	 * Create and Init a new DateDrop then add it to the allNpcDateDrops of EventDroplist .<BR>
	 * <BR>
	 * @param items  The table containing all item identifier of this DateDrop
	 * @param count  The table containing min and max value of this DateDrop
	 * @param chance The chance to obtain this drop
	 * @param range  The DateRange object to add to this DateDrop
	 */
	public void addGlobalDrop(final int[] items, final int[] count, final int chance, final DateRange range)
	{
		
		DateDrop date = new DateDrop();
		
		date.dateRange = range;
		date.items = items;
		date.min = count[0];
		date.max = count[1];
		date.chance = chance;
		
		allNpcDateDrops.add(date);
		date = null;
	}
	
	/**
	 * @return all DateDrop of EventDroplist allNpcDateDrops within the date range.
	 */
	public List<DateDrop> getAllDrops()
	{
		final List<DateDrop> list = new ArrayList<>();
		
		for (final DateDrop drop : allNpcDateDrops)
		{
			Date currentDate = new Date();
			// LOGGER.info("From: "+drop.from+" To: "+drop.to+" Now: "+ currentDate);
			if (drop.dateRange.isWithinRange(currentDate))
			{
				list.add(drop);
			}
			
			currentDate = null;
		}
		
		return list;
	}
	
}
