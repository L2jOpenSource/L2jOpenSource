package com.l2jfrozen.gameserver.model.entity.event.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * @author Shyla
 */
public class EventsGlobalTask implements Runnable
{
	protected static final Logger LOGGER = Logger.getLogger(EventsGlobalTask.class);
	
	private static EventsGlobalTask instance;
	
	private boolean destroy = false;
	
	private final Hashtable<String, ArrayList<EventTask>> time_to_tasks = new Hashtable<>(); // time is in hh:mm
	private final Hashtable<String, ArrayList<EventTask>> eventid_to_tasks = new Hashtable<>();
	
	private EventsGlobalTask()
	{
		
		ThreadPoolManager.getInstance().scheduleGeneral(this, 5000);
		
	}
	
	public static EventsGlobalTask getInstance()
	{
		if (instance == null)
		{
			instance = new EventsGlobalTask();
		}
		return instance;
		
	}
	
	public void registerNewEventTask(final EventTask event)
	{
		if (event == null || event.getEventIdentifier() == null || event.getEventIdentifier().equals("") || event.getEventStartTime() == null || event.getEventStartTime().equals(""))
		{
			LOGGER.error("registerNewEventTask: eventTask must be not null as its identifier and startTime ");
			return;
		}
		
		ArrayList<EventTask> savedTasksForTime = time_to_tasks.get(event.getEventStartTime());
		ArrayList<EventTask> savedTasksForId = eventid_to_tasks.get(event.getEventIdentifier());
		
		if (savedTasksForTime != null)
		{
			if (!savedTasksForTime.contains(event))
			{
				savedTasksForTime.add(event);
			}
		}
		else
		{
			savedTasksForTime = new ArrayList<>();
			savedTasksForTime.add(event);
		}
		
		time_to_tasks.put(event.getEventStartTime(), savedTasksForTime);
		
		if (savedTasksForId != null)
		{
			if (!savedTasksForId.contains(event))
			{
				savedTasksForId.add(event);
			}
		}
		else
		{
			savedTasksForId = new ArrayList<>();
			savedTasksForId.add(event);
		}
		
		eventid_to_tasks.put(event.getEventIdentifier(), savedTasksForId);
		
		if (Config.DEBUG)
		{
			LOGGER.info("Added Event: " + event.getEventIdentifier());
			
			// check Info
			for (String time : time_to_tasks.keySet())
			{
				
				// LOGGER.info("--Time: "+time);
				final ArrayList<EventTask> tasks = time_to_tasks.get(time);
				
				final Iterator<EventTask> taskIt = tasks.iterator();
				
				while (taskIt.hasNext())
				{
					final EventTask actual_event = taskIt.next();
					LOGGER.info("	--Registered Event: " + actual_event.getEventIdentifier());
				}
				
			}
			
			for (String event_id : eventid_to_tasks.keySet())
			{
				LOGGER.info("--Event: " + event_id);
				ArrayList<EventTask> times = eventid_to_tasks.get(event_id);
				
				Iterator<EventTask> timesIt = times.iterator();
				
				while (timesIt.hasNext())
				{
					final EventTask actual_time = timesIt.next();
					LOGGER.info("	--Registered Time: " + actual_time.getEventStartTime());
				}
				
			}
		}
		
	}
	
	public void clearEventTasksByEventName(String eventId)
	{
		if (eventId == null)
		{
			LOGGER.error("registerNewEventTask: eventTask must be not null as its identifier and startTime ");
			return;
		}
		
		if (eventId.equalsIgnoreCase("all"))
		{
			time_to_tasks.clear();
			eventid_to_tasks.clear();
		}
		else
		{
			ArrayList<EventTask> oldTasksForId = eventid_to_tasks.get(eventId);
			
			if (oldTasksForId != null)
			{
				for (EventTask actual : oldTasksForId)
				{
					ArrayList<EventTask> oldTasksForTime = time_to_tasks.get(actual.getEventStartTime());
					
					if (oldTasksForTime != null)
					{
						oldTasksForTime.remove(actual);
						time_to_tasks.put(actual.getEventStartTime(), oldTasksForTime);
					}
					
				}
				eventid_to_tasks.remove(eventId);
			}
		}
		
	}
	
	public void deleteEventTask(EventTask event)
	{
		if (event == null || event.getEventIdentifier() == null || event.getEventIdentifier().equals("") || event.getEventStartTime() == null || event.getEventStartTime().equals(""))
		{
			LOGGER.error("registerNewEventTask: eventTask must be not null as its identifier and startTime ");
			return;
		}
		
		if (time_to_tasks.size() < 0)
		{
			return;
		}
		
		ArrayList<EventTask> oldTasksForId = eventid_to_tasks.get(event.getEventIdentifier());
		ArrayList<EventTask> oldTasksForTime = time_to_tasks.get(event.getEventStartTime());
		
		if (oldTasksForId != null)
		{
			oldTasksForId.remove(event);
			eventid_to_tasks.put(event.getEventIdentifier(), oldTasksForId);
		}
		
		if (oldTasksForTime != null)
		{
			oldTasksForTime.remove(event);
			time_to_tasks.put(event.getEventStartTime(), oldTasksForTime);
		}
		
	}
	
	private void checkRegisteredEvents()
	{
		
		if (time_to_tasks.size() < 0)
		{
			return;
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int min = calendar.get(Calendar.MINUTE);
		
		String hourStr = "";
		String minStr = "";
		
		if (hour < 10)
		{
			hourStr = "0" + hour;
		}
		else
		{
			hourStr = "" + hour;
		}
		
		if (min < 10)
		{
			minStr = "0" + min;
		}
		else
		{
			minStr = "" + min;
		}
		
		final String currentTime = hourStr + ":" + minStr;
		
		// LOGGER.info("Current Time: "+currentTime);
		final ArrayList<EventTask> registeredEventsAtCurrentTime = time_to_tasks.get(currentTime);
		
		if (registeredEventsAtCurrentTime != null)
		{
			for (EventTask actualEvent : registeredEventsAtCurrentTime)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(actualEvent, 5000);
			}
		}
	}
	
	public void destroyLocalInstance()
	{
		destroy = true;
		instance = null;
	}
	
	@Override
	public void run()
	{
		while (!destroy)
		{
			// start time checker
			checkRegisteredEvents();
			
			try
			{
				Thread.sleep(60000); // 1 minute
			}
			catch (InterruptedException e)
			{
				LOGGER.error("EventsGlobalTask.run : Something interrupted the thread", e);
			}
		}
	}
	
}
