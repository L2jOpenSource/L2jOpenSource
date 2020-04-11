package com.l2jfrozen.gameserver.thread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.ArrayUtils;

/**
 * @author ProGramMoS
 */

public final class RunnableStatsManager
{
	protected static final Map<Class<?>, ClassStat> classStats = new HashMap<>();
	
	private static final class ClassStat
	{
		private String[] methodNames = new String[0];
		private MethodStat[] methodStats = new MethodStat[0];
		
		protected ClassStat(final Class<?> clazz)
		{
			classStats.put(clazz, this);
		}
		
		protected MethodStat getMethodStat(String methodName, final boolean synchronizedAlready)
		{
			for (int i = 0; i < methodNames.length; i++)
			{
				if (methodNames[i].equals(methodName))
				{
					return methodStats[i];
				}
			}
			
			if (!synchronizedAlready)
			{
				synchronized (this)
				{
					return getMethodStat(methodName, true);
				}
			}
			
			methodName = methodName.intern();
			
			final MethodStat methodStat = new MethodStat();
			
			methodNames = (String[]) ArrayUtils.add(methodNames, methodName);
			methodStats = (MethodStat[]) ArrayUtils.add(methodStats, methodStat);
			
			return methodStat;
		}
	}
	
	protected static final class MethodStat
	{
		private final ReentrantLock lock = new ReentrantLock();
		
		private long min = Long.MAX_VALUE;
		private long max = Long.MIN_VALUE;
		
		protected void handleStats(final long runTime)
		{
			lock.lock();
			try
			{
				min = Math.min(min, runTime);
				max = Math.max(max, runTime);
			}
			finally
			{
				lock.unlock();
			}
		}
	}
	
	private static ClassStat getClassStat(final Class<?> clazz, final boolean synchronizedAlready)
	{
		final ClassStat classStat = classStats.get(clazz);
		
		if (classStat != null)
		{
			return classStat;
		}
		
		if (!synchronizedAlready)
		{
			synchronized (RunnableStatsManager.class)
			{
				return getClassStat(clazz, true);
			}
		}
		
		return new ClassStat(clazz);
	}
	
	public static void handleStats(final Class<? extends Runnable> clazz, final long runTime)
	{
		handleStats(clazz, "run()", runTime);
	}
	
	public static void handleStats(final Class<?> clazz, final String methodName, final long runTime)
	{
		getClassStat(clazz, false).getMethodStat(methodName, false).handleStats(runTime);
	}
}
