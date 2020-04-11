package com.l2jfrozen.gameserver.taskmanager;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.L2WorldRegion;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

public class KnownListUpdateTaskManager
{
	protected static final Logger LOGGER = Logger.getLogger(DecayTaskManager.class);
	
	private static KnownListUpdateTaskManager instance;
	
	public KnownListUpdateTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new KnownListUpdate(), 1000, 750);
	}
	
	public static KnownListUpdateTaskManager getInstance()
	{
		if (instance == null)
		{
			instance = new KnownListUpdateTaskManager();
		}
		
		return instance;
	}
	
	private class KnownListUpdate implements Runnable
	{
		boolean toggle = false;
		boolean fullUpdate = true;
		
		protected KnownListUpdate()
		{
			// Do nothing
		}
		
		@Override
		public void run()
		{
			try
			{
				for (final L2WorldRegion regions[] : L2World.getInstance().getAllWorldRegions())
				{
					for (final L2WorldRegion r : regions) // go through all world regions
					{
						if (r.isActive()) // and check only if the region is active
						{
							updateRegion(r, fullUpdate, toggle);
						}
					}
				}
				if (toggle)
				{
					toggle = false;
				}
				else
				{
					toggle = true;
				}
				if (fullUpdate)
				{
					fullUpdate = false;
				}
				
			}
			catch (final Throwable e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				LOGGER.warn(e.toString());
			}
		}
	}
	
	public void updateRegion(final L2WorldRegion region, final boolean fullUpdate, final boolean forgetObjects)
	{
		for (final L2Object object : region.getVisibleObjects()) // and for all members in region
		{
			if (!object.isVisible())
			{
				continue; // skip dying objects
			}
			if (forgetObjects)
			{
				object.getKnownList().forgetObjects(); // TODO
				continue;
			}
			if (object instanceof L2PlayableInstance /* || (false && object instanceof L2GuardInstance) */ || fullUpdate)
			{
				for (final L2WorldRegion regi : region.getSurroundingRegions()) // offer members of this and surrounding regions
				{
					for (final L2Object visibleObject : regi.getVisibleObjects())
					{
						if (visibleObject != object)
						{
							object.getKnownList().addKnownObject(visibleObject);
						}
					}
				}
			}
			else if (object instanceof L2Character)
			{
				for (final L2WorldRegion regi : region.getSurroundingRegions()) // offer members of this and surrounding regions
				{
					if (regi.isActive())
					{
						for (final L2Object visibleObject : regi.getVisibleObjects())
						{
							if (visibleObject != object)
							{
								object.getKnownList().addKnownObject(visibleObject);
							}
						}
					}
				}
			}
			
		}
	}
}
