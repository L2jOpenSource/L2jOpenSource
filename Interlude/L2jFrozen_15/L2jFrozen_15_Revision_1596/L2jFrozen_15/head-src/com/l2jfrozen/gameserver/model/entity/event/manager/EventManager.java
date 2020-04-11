package com.l2jfrozen.gameserver.model.entity.event.manager;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.TvT;

/**
 * @author Shyla
 */
public class EventManager
{
	protected static final Logger LOGGER = Logger.getLogger(EventManager.class);
	private static EventManager instance = null;
	
	private EventManager()
	{
	}
	
	public static EventManager getInstance()
	{
		if (instance == null)
		{
			instance = new EventManager();
		}
		return instance;
	}
	
	public void startEventRegistration()
	{
		if (Config.TVT_EVENT_ENABLED)
		{
			registerTvT();
		}
		
		if (Config.CTF_EVENT_ENABLED)
		{
			registerCTF();
		}
		
		if (Config.DM_EVENT_ENABLED)
		{
			registerDM();
		}
	}
	
	private static void registerTvT()
	{
		TvT.loadData();
		
		if (!TvT.checkStartJoinOk())
		{
			LOGGER.error("registerTvT: TvT Event is not setted Properly");
		}
		
		// clear all tvt
		EventsGlobalTask.getInstance().clearEventTasksByEventName(TvT.get_eventName());
		
		for (String time : Config.TVT_TIMES_LIST)
		{
			TvT newInstance = TvT.getNewInstance();
			// LOGGER.info("registerTvT: reg.time: "+time);
			newInstance.setEventStartTime(time);
			EventsGlobalTask.getInstance().registerNewEventTask(newInstance);
		}
	}
	
	private static void registerCTF()
	{
		CTF.loadData();
		
		if (!CTF.checkStartJoinOk())
		{
			LOGGER.error("registerCTF: CTF Event is not setted Properly");
		}
		
		// clear all tvt
		EventsGlobalTask.getInstance().clearEventTasksByEventName(CTF.getEventName());
		
		for (String time : Config.CTF_TIMES_LIST)
		{
			CTF newInstance = CTF.getNewInstance();
			// LOGGER.info("registerCTF: reg.time: "+time);
			newInstance.setEventStartTime(time);
			EventsGlobalTask.getInstance().registerNewEventTask(newInstance);
		}
	}
	
	private static void registerDM()
	{
		DM.loadData();
		if (!DM.checkStartJoinOk())
		{
			LOGGER.error("registerDM: DM Event is not setted Properly");
		}
		
		// clear all tvt
		EventsGlobalTask.getInstance().clearEventTasksByEventName(DM.get_eventName());
		
		for (String time : Config.DM_TIMES_LIST)
		{
			DM newInstance = DM.getNewInstance();
			// LOGGER.info("registerDM: reg.time: "+time);
			newInstance.setEventStartTime(time);
			EventsGlobalTask.getInstance().registerNewEventTask(newInstance);
		}
	}
}
