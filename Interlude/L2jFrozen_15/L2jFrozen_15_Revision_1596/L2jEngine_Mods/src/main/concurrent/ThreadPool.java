package main.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class handles thread pooling system. It relies on two ThreadPoolExecutor arrays, which poolers number is generated using config.
 * <p>
 * Those arrays hold following pools :
 * </p>
 * <ul>
 * <li>Scheduled pool keeps a track about incoming, future events.</li>
 * <li>Instant pool handles short-life events.</li>
 * </ul>
 */
public final class ThreadPool
{
	private static List<ScheduledThreadPoolExecutor> scheduledPools;
	private static List<ThreadPoolExecutor> instantPools;
	
	private static int threadPoolRandomizer;
	
	/** temp workaround for VM issue */
	private static final long MAX_DELAY = TimeUnit.NANOSECONDS.toMillis(Long.MAX_VALUE - System.nanoTime()) / 2;
	
	public ThreadPool()
	{
		int poolCount = Runtime.getRuntime().availableProcessors();
		
		scheduledPools = new ArrayList<>(poolCount);
		instantPools = new ArrayList<>(poolCount);
		
		for (int i = 0; i < poolCount; i++)
		{
			scheduledPools.add(i, new ScheduledThreadPoolExecutor(6));
			instantPools.add(i, new ThreadPoolExecutor(4, 4, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100000)));
		}
		
		scheduledPools.forEach(s -> s.prestartAllCoreThreads());
		instantPools.forEach(s -> s.prestartAllCoreThreads());
		
		// Launch purge task.
		scheduleAtFixedRate(() ->
		{
			scheduledPools.forEach(t -> t.purge());
			instantPools.forEach(t -> t.purge());
		}, 600000, 600000);
	}
	
	/**
	 * Shutdown thread pooling system correctly. Send different informations.
	 */
	public static void shutdown()
	{
		try
		{
			System.out.println("ThreadPool: Shutting down.");
			
			scheduledPools.forEach(t -> t.shutdown());
			instantPools.forEach(t -> t.shutdown());
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	/**
	 * Executes the given task sometime in the future.
	 * @param r : the task to execute.
	 */
	public static void execute(Runnable r)
	{
		try
		{
			getPool(instantPools).execute(r);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Schedules a one-shot action that becomes enabled after a delay. The pool is chosen based on pools activity.
	 * @param  r     : the task to execute.
	 * @param  delay : the time from now to delay execution.
	 * @return       a ScheduledFuture representing pending completion of the task and whose get() method will return null upon completion.
	 */
	public static ScheduledFuture<?> schedule(Runnable r, long delay)
	{
		try
		{
			return getPool(scheduledPools).schedule(new TaskWrapper(r), validate(delay), TimeUnit.MILLISECONDS);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Schedules a periodic action that becomes enabled after a delay. The pool is chosen based on pools activity.
	 * @param  r      : the task to execute.
	 * @param  delay  : the time from now to delay execution.
	 * @param  period : the period between successive executions.
	 * @return        a ScheduledFuture representing pending completion of the task and whose get() method will throw an exception upon cancellation.
	 */
	public static ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long delay, long period)
	{
		try
		{
			return getPool(scheduledPools).scheduleAtFixedRate(new TaskWrapper(r), validate(delay), validate(period), TimeUnit.MILLISECONDS);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @param              <T> : The pool type.
	 * @param  threadPools : The pool array to check.
	 * @return             the less fed pool.
	 */
	private static <T> T getPool(List<T> threadPools)
	{
		return threadPools.get(threadPoolRandomizer++ % threadPools.size());
	}
	
	/**
	 * @param  delay : The delay to validate.
	 * @return       a secured value, from 0 to MAX_DELAY.
	 */
	private static long validate(long delay)
	{
		return Math.max(0, Math.min(MAX_DELAY, delay));
	}
	
	private static final class TaskWrapper implements Runnable
	{
		private final Runnable r;
		
		public TaskWrapper(Runnable runnable)
		{
			r = runnable;
		}
		
		@Override
		public void run()
		{
			r.run();
		}
	}
	
	public static List<ScheduledThreadPoolExecutor> getScheduledPools()
	{
		return scheduledPools;
	}
	
	public static ThreadPool getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ThreadPool INSTANCE = new ThreadPool();
	}
}