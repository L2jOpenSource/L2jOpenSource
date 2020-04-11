package com.l2jfrozen.gameserver.taskmanager;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * @author la2 Lets drink to code!
 */
public class DecayTaskManager
{
	protected static final Logger LOGGER = Logger.getLogger(DecayTaskManager.class);
	protected Map<L2Character, Long> decayTasks = new ConcurrentHashMap<>();
	
	private static DecayTaskManager instance;
	
	public DecayTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new DecayScheduler(), 10000, 5000);
	}
	
	public static DecayTaskManager getInstance()
	{
		if (instance == null)
		{
			instance = new DecayTaskManager();
		}
		
		return instance;
	}
	
	public void addDecayTask(final L2Character actor)
	{
		decayTasks.put(actor, System.currentTimeMillis());
	}
	
	public void addDecayTask(final L2Character actor, final int interval)
	{
		decayTasks.put(actor, System.currentTimeMillis() + interval);
	}
	
	public void cancelDecayTask(final L2Character actor)
	{
		try
		{
			decayTasks.remove(actor);
		}
		catch (final NoSuchElementException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
	}
	
	private class DecayScheduler implements Runnable
	{
		protected DecayScheduler()
		{
			// Do nothing
		}
		
		@Override
		public void run()
		{
			final Long current = System.currentTimeMillis();
			int delay;
			try
			{
				if (decayTasks != null)
				{
					for (L2Character actor : decayTasks.keySet())
					{
						if (actor instanceof L2RaidBossInstance)
						{
							delay = 30000;
						}
						else
						{
							delay = 8500;
						}
						if (current - decayTasks.get(actor) > delay)
						{
							actor.onDecay();
							decayTasks.remove(actor);
						}
					}
				}
			}
			catch (Exception e)
			{
				LOGGER.error("DecayTaskManager.DecayScheduler.run : Something went wrong during decay task. ", e);
			}
		}
	}
	
	@Override
	public String toString()
	{
		String ret = "============= DecayTask Manager Report ============\r\n";
		ret += "Tasks count: " + decayTasks.size() + "\r\n";
		ret += "Tasks dump:\r\n";
		
		final Long current = System.currentTimeMillis();
		for (final L2Character actor : decayTasks.keySet())
		{
			ret += "Class/Name: " + actor.getClass().getSimpleName() + "/" + actor.getName() + " decay timer: " + (current - decayTasks.get(actor)) + "\r\n";
		}
		
		return ret;
	}
}
