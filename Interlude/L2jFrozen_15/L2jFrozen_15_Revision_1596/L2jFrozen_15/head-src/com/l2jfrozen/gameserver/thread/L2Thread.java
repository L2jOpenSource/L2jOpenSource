package com.l2jfrozen.gameserver.thread;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ProGramMoS
 */

public abstract class L2Thread extends Thread
{
	// private static final Logger LOGGER = Logger.getLogger(L2Thread.class.getName);
	
	protected L2Thread()
	{
		super();
	}
	
	protected L2Thread(final String name)
	{
		super(name);
	}
	
	private volatile boolean isAlive = true;
	
	public final void shutdown() throws InterruptedException
	{
		isAlive = false;
		
		join();
	}
	
	@Override
	public final void run()
	{
		try
		{
			while (isAlive)
			{
				final long begin = System.nanoTime();
				
				try
				{
					runTurn();
				}
				finally
				{
					RunnableStatsManager.handleStats(getClass(), System.nanoTime() - begin);
				}
				
				try
				{
					sleepTurn();
				}
				catch (final InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (final Exception e)
		{
			// TODO: Log this exception.
		}
	}
	
	protected abstract void runTurn();
	
	protected abstract void sleepTurn() throws InterruptedException;
	
	public static List<String> getStats(final Thread t)
	{
		final List<String> list = new ArrayList<>();
		
		list.add(t.toString() + " - ID: " + t.getId());
		list.add(" * State: " + t.getState());
		list.add(" * Alive: " + t.isAlive());
		list.add(" * Daemon: " + t.isDaemon());
		list.add(" * Interrupted: " + t.isInterrupted());
		for (final ThreadInfo info : ManagementFactory.getThreadMXBean().getThreadInfo(new long[]
		{
			t.getId()
		}, true, true))
		{
			for (final MonitorInfo monitorInfo : info.getLockedMonitors())
			{
				list.add("==========");
				list.add(" * Locked monitor: " + monitorInfo);
				list.add("\t[" + monitorInfo.getLockedStackDepth() + ".]: at " + monitorInfo.getLockedStackFrame());
			}
			
			for (final LockInfo lockInfo : info.getLockedSynchronizers())
			{
				list.add("==========");
				list.add(" * Locked synchronizer: " + lockInfo);
			}
			
			list.add("==========");
			for (final StackTraceElement trace : info.getStackTrace())
			{
				list.add("\tat " + trace);
			}
		}
		
		return list;
	}
	
}
