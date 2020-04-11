package l2r.gameserver.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class TimeUtils
{
	public static final long SECOND_IN_MILLIS = 1000L;
	public static final long MINUTE_IN_MILLIS = 60000L;
	public static final long HOUR_IN_MILLIS = 3600000L;
	public static final long DAY_IN_MILLIS = 86400000L;
	private static final SimpleDateFormat SIMPLE_FORMAT = new SimpleDateFormat("HH:mm dd.MM.yyyy");
	
	public static String toSimpleFormat(Calendar cal)
	{
		return SIMPLE_FORMAT.format(cal.getTime());
	}
	
	public static String toSimpleFormat(long cal)
	{
		return SIMPLE_FORMAT.format(cal);
	}
	
	public static String minutesToFullString(int period)
	{
		StringBuilder sb = new StringBuilder();
		
		if (period > 1440)
		{
			sb.append((period - (period % 1440)) / 1440).append(" P.");
			period = period % 1440;
		}
		
		if (period > 60)
		{
			if (sb.length() > 0)
			{
				sb.append(", ");
			}
			
			sb.append((period - (period % 60)) / 60).append(" Ρ‡.");
			
			period = period % 60;
		}
		
		if (period > 0)
		{
			if (sb.length() > 0)
			{
				sb.append(", ");
			}
			
			sb.append(period).append(" min.");
		}
		if (sb.length() < 1)
		{
			sb.append("less than 1 minute.");
		}
		
		return sb.toString();
	}
	
	public static long getMilisecondsToNextDay(List<Integer> days, int hourOfTheEvent)
	{
		return getMilisecondsToNextDay(days, hourOfTheEvent, 5);
	}
	
	public static long getMilisecondsToNextDay(List<Integer> days, int hourOfTheEvent, int minuteOfTheEvent)
	{
		int[] hours = new int[days.size()];
		for (int i = 0; i < hours.length; i++)
		{
			hours[i] = days.get(i).intValue();
		}
		return getMilisecondsToNextDay(hours, hourOfTheEvent, minuteOfTheEvent);
	}
	
	/**
	 * Getting Time in Milliseconds to the closest day. If every day already passed, it's getting closest day of next month Event Time: Millisecond: 0, Second: 0, Minute: 0, Hour: hourOfTheEvent
	 * @param days Array of specific days in the month
	 * @param hourOfTheEvent hour of the day, when clock will stop
	 * @param minuteOfTheEvent
	 * @return Time in milliseconds to that day
	 */
	public static long getMilisecondsToNextDay(int[] days, int hourOfTheEvent, int minuteOfTheEvent)
	{
		Calendar tempCalendar = Calendar.getInstance();
		tempCalendar.set(Calendar.SECOND, 0);
		tempCalendar.set(Calendar.MILLISECOND, 0);
		tempCalendar.set(Calendar.HOUR_OF_DAY, hourOfTheEvent);
		tempCalendar.set(Calendar.MINUTE, minuteOfTheEvent);
		
		final long currentTime = System.currentTimeMillis();
		Calendar eventCalendar = Calendar.getInstance();
		
		boolean found = false;
		long smallest = Long.MAX_VALUE;// In case, we need to make it in next month
		
		for (int day : days)
		{
			tempCalendar.set(Calendar.DAY_OF_MONTH, day);
			long timeInMillis = tempCalendar.getTimeInMillis();
			
			// If time is smaller than current
			if (timeInMillis <= currentTime)
			{
				if (timeInMillis < smallest)
				{
					smallest = timeInMillis;
				}
				continue;
			}
			
			// If event time wasn't chosen yet or its smaller than current Event Time
			if (!found || (timeInMillis < eventCalendar.getTimeInMillis()))
			{
				found = true;
				eventCalendar.setTimeInMillis(timeInMillis);
			}
		}
		
		if (!found)
		{
			eventCalendar.setTimeInMillis(smallest);// Smallest time + One Month
			eventCalendar.add(Calendar.MONTH, 1);
		}
		return eventCalendar.getTimeInMillis() - currentTime;
	}
}
