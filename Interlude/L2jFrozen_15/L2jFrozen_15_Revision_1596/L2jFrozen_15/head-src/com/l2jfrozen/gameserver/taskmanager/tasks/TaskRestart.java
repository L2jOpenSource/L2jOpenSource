package com.l2jfrozen.gameserver.taskmanager.tasks;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.Shutdown;
import com.l2jfrozen.gameserver.taskmanager.Task;
import com.l2jfrozen.gameserver.taskmanager.TaskManager.ExecutedTask;

/**
 * @author Layane
 */
public final class TaskRestart extends Task
{
	private static final Logger LOGGER = Logger.getLogger(TaskRestart.class);
	public static final String NAME = "restart";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(final ExecutedTask task)
	{
		LOGGER.info("[GlobalTask] Server Restart launched.");
		
		Shutdown.getInstance().startShutdown(null, Integer.parseInt(task.getParams()[2]), true);
	}
}