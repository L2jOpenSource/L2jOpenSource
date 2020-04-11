package com.l2jfrozen.gameserver.taskmanager;

import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.taskmanager.TaskManager.ExecutedTask;

/**
 * @author Layane
 */
public abstract class Task
{
	private static Logger LOGGER = Logger.getLogger(Task.class);
	
	public void initializate()
	{
		if (Config.DEBUG)
		{
			LOGGER.info("Task" + getName() + " inializate");
		}
	}
	
	public ScheduledFuture<?> launchSpecial(final ExecutedTask instance)
	{
		return null;
	}
	
	public abstract String getName();
	
	public abstract void onTimeElapsed(ExecutedTask task);
	
	public void onDestroy()
	{
	}
}
