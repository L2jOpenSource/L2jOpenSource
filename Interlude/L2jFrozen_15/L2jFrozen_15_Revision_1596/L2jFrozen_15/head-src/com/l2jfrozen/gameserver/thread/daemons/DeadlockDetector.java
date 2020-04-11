package com.l2jfrozen.gameserver.thread.daemons;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.thread.L2Thread;
import com.l2jfrozen.util.Util;

import javolution.util.FastSet;

/**
 * @author  ProGramMoS
 * @version 0.4 Stable
 */
public final class DeadlockDetector implements Runnable
{
	protected static final Logger LOGGER = Logger.getLogger(DeadlockDetector.class);
	private final Set<Long> logged = new FastSet<>();
	
	private static DeadlockDetector instance;
	
	public static DeadlockDetector getInstance()
	{
		if (instance == null)
		{
			instance = new DeadlockDetector();
		}
		
		return instance;
	}
	
	private DeadlockDetector()
	{
		LOGGER.info("DeadlockDetector daemon started.");
	}
	
	@Override
	public void run()
	{
		final long[] ids = findDeadlockedThreadIDs();
		
		if (ids == null)
		{
			return;
		}
		
		final List<Thread> deadlocked = new ArrayList<>();
		
		for (final long id : ids)
		{
			if (logged.add(id))
			{
				deadlocked.add(findThreadById(id));
			}
		}
		
		if (!deadlocked.isEmpty())
		{
			Util.printSection("Deadlocked Thread(s)");
			
			for (final Thread thread : deadlocked)
			{
				for (final String line : L2Thread.getStats(thread))
				{
					LOGGER.error(line);
				}
			}
			
			Util.printSection("End");
		}
	}
	
	private long[] findDeadlockedThreadIDs()
	{
		if (ManagementFactory.getThreadMXBean().isSynchronizerUsageSupported())
		{
			return ManagementFactory.getThreadMXBean().findDeadlockedThreads();
		}
		return ManagementFactory.getThreadMXBean().findMonitorDeadlockedThreads();
	}
	
	private Thread findThreadById(final long id)
	{
		for (final Thread thread : Thread.getAllStackTraces().keySet())
		{
			if (thread.getId() == id)
			{
				return thread;
			}
		}
		
		throw new IllegalStateException("Deadlocked Thread not found!");
		
	}
	
}
