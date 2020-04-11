package main.engine.events.daily;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Future;

import com.l2jfrozen.gameserver.network.clientpackets.Say2;

import main.EngineModsManager;
import main.concurrent.ThreadPool;
import main.engine.AbstractMod;
import main.enums.WeekDayType;
import main.util.UtilMessage;

/**
 * @author fissban
 */
public class AbstractEvent extends AbstractMod
{
	/** Scheduled START and END */
	protected List<Future<?>> scheduledStateEvent = new ArrayList<>();
	
	@Override
	public void onModState()
	{
		//
	}
	
	@Override
	public void endMod()
	{
		// Announce to all players
		UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "Event " + getClass().getSimpleName() + ": End!");
		LOG.info("Event " + getClass().getSimpleName() + ": End!");
		
		super.endMod();
	}
	
	@Override
	public void startMod()
	{
		UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "Event " + getClass().getSimpleName() + ": Start!");
		LOG.info("Event " + getClass().getSimpleName() + ": Start!");
		
		super.startMod();
	}
	
	/**
	 * The start and end threads of events are canceled in case they are not destroyed by VM
	 */
	@Override
	public void cancelScheduledState()
	{
		scheduledStateEvent.forEach(run -> run.cancel(false));
	}
	
	// XXX REGISTER EVENTS ------------------------------------------------------------------------------------------
	
	/**
	 * The mod is registered to run only on certain days of the week.<br>
	 * one thread start and one thread end will be created for each day us to add to the list.
	 * @param day <br>
	 *                <li>{@link WeekDayType#SUNDAY}</li>
	 *                <li>{@link WeekDayType#MONDAY}</li>
	 *                <li>{@link WeekDayType#TUESDAY}</li>
	 *                <li>{@link WeekDayType#WEDNESDAY}</li>
	 *                <li>{@link WeekDayType#THURSDAY}</li>
	 *                <li>{@link WeekDayType#FRIDAY}</li>
	 *                <li>{@link WeekDayType#SATURDAY}</li>
	 */
	public void registerEvent(boolean config, List<WeekDayType> day)
	{
		EngineModsManager.registerMod(this);
		if (config)
		{
			day.forEach(d -> startEvent(d));
		}
	}
	
	/**
	 * The mod is registered to run only on certain days of the week.<br>
	 * one thread start and one thread end will be created for each day us to add to the list.
	 * @param day <br>
	 *                <li>{@link WeekDayType#SUNDAY}</li>
	 *                <li>{@link WeekDayType#MONDAY}</li>
	 *                <li>{@link WeekDayType#TUESDAY}</li>
	 *                <li>{@link WeekDayType#WEDNESDAY}</li>
	 *                <li>{@link WeekDayType#THURSDAY}</li>
	 *                <li>{@link WeekDayType#FRIDAY}</li>
	 *                <li>{@link WeekDayType#SATURDAY}</li>
	 */
	public void registerEvent(boolean config, String start, String end)
	{
		if (config)
		{
			EngineModsManager.registerMod(this);
			
			startEvent(start, end);
		}
	}
	
	/**
	 * The mod is registered to run only on certain days of the week.<br>
	 * one thread start and one thread end will be created for each day us to add to the list.
	 * @param day <br>
	 *                <li>{@link WeekDayType#SUNDAY}</li>
	 *                <li>{@link WeekDayType#MONDAY}</li>
	 *                <li>{@link WeekDayType#TUESDAY}</li>
	 *                <li>{@link WeekDayType#WEDNESDAY}</li>
	 *                <li>{@link WeekDayType#THURSDAY}</li>
	 *                <li>{@link WeekDayType#FRIDAY}</li>
	 *                <li>{@link WeekDayType#SATURDAY}</li>
	 */
	private void startEvent(WeekDayType day)
	{
		int weekToStartEvent = 1;
		
		while (weekToStartEvent >= 0)
		{
			// Variable that decides the number of days to start the event
			int eventTime = -1;
			// Controls the number of days left to start the event
			int missingDayToStart = 0;
			// Simple auxiliary to know what day of the week it is.
			GregorianCalendar time = new GregorianCalendar();
			// You get the value of the day of the week
			int i = time.get(Calendar.DAY_OF_WEEK);
			// It looks for how many days are left to reach that day
			while (eventTime < 0)
			{
				if (WeekDayType.values()[i - 1] == day)
				{
					eventTime = missingDayToStart;
				}
				else
				{
					i++;
					missingDayToStart++;
					if (i > WeekDayType.values().length)
					{
						i = 1;
					}
				}
			}
			
			eventTime += weekToStartEvent * 7;
			// one thread where we indicate when to start the event and the actions to take will be created.
			time.add(Calendar.DAY_OF_YEAR, eventTime);
			long timeStart = time.getTimeInMillis() - System.currentTimeMillis();
			
			scheduledStateEvent.add(ThreadPool.schedule(new ScheduleStart(), timeStart < 0 ? 0 : timeStart));
			// one thread where we indicate when to end the event and the actions to take will be created.
			time.add(Calendar.DAY_OF_YEAR, eventTime + 1);
			scheduledStateEvent.add(ThreadPool.schedule(new ScheduleEnd(), time.getTimeInMillis() - System.currentTimeMillis()));
			
			weekToStartEvent--;
		}
	}
	
	/**
	 * @param start : date DD-MM-AAAA
	 * @param end   : date DD-MM-AAAA
	 */
	public void startEvent(String start, String end)
	{
		// the day you get the month and year of start and end of the event.
		try
		{
			StringTokenizer parse = null;
			// Date start -> parse
			parse = new StringTokenizer(start, "-");
			int diaStart = Integer.parseInt(parse.nextToken());
			int mesStart = Integer.parseInt(parse.nextToken());
			int anioStart = Integer.parseInt(parse.nextToken());
			// Date end -> parse
			parse = new StringTokenizer(end, "-");
			int diaEnd = Integer.parseInt(parse.nextToken());
			int mesEnd = Integer.parseInt(parse.nextToken());
			int anioEnd = Integer.parseInt(parse.nextToken());
			
			// Create calendar
			GregorianCalendar timeStart = new GregorianCalendar();
			timeStart.set(anioStart, mesStart, diaStart, 0, 0, 0);
			
			GregorianCalendar timeEnd = new GregorianCalendar();
			timeEnd.set(anioEnd, mesEnd, diaEnd, 0, 0, 0);
			
			long hoy = System.currentTimeMillis();
			
			LOG.warning("Event " + getClass().getSimpleName() + ": Start! -> " + timeStart.getTime().toString());
			LOG.warning("Event " + getClass().getSimpleName() + ": End! -> " + timeEnd.getTime().toString());
			
			// If the end date of the event is less than today's event should not run.
			if (timeEnd.getTimeInMillis() < hoy)
			{
				return;
			}
			
			// If the end date of the event is less than the start must be corrected.
			if (timeStart.getTimeInMillis() >= timeEnd.getTimeInMillis())
			{
				LOG.warning("Event " + getClass().getSimpleName() + ": The start date of the event can not be greater than or equal to the end of the event");
				return;
			}
			
			// one thread where we indicate when to start the event and the actions to take is created.
			long time = 0L;
			if ((timeStart.getTimeInMillis() - hoy) > 0)
			{
				time = timeStart.getTimeInMillis() - hoy;
			}
			scheduledStateEvent.add(ThreadPool.schedule(new ScheduleStart(), time));
			// one thread where we indicate when to end the event and the actions to take is created.
			scheduledStateEvent.add(ThreadPool.schedule(new ScheduleEnd(), timeEnd.getTimeInMillis() - hoy));
		}
		catch (Exception e)
		{
			LOG.warning("Event " + getClass().getSimpleName() + ": The date of the event register is invalid");
			e.printStackTrace();
			return;
		}
	}
	
	// XXX TASK's ---------------------------------------------------------------------------------------------------
	
	/**
	 * Start Event
	 */
	protected class ScheduleStart implements Runnable
	{
		@Override
		public void run()
		{
			startMod();
		}
	}
	
	/**
	 * End Event
	 */
	protected class ScheduleEnd implements Runnable
	{
		@Override
		public void run()
		{
			endMod();
		}
	}
}
