package com.l2jfrozen.gameserver.thread;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.util.StringUtil;

import javolution.util.FastSet;

/**
 * <p>
 * This class is made to handle all the ThreadPools used in L2j.
 * </p>
 * <p>
 * Scheduled Tasks can either be sent to a {@link #generalScheduledThreadPool "general"} or {@link #effectsScheduledThreadPool "effects"} {@link ScheduledThreadPoolExecutor ScheduledThreadPool}: The "effects" one is used for every effects (skills, hp/mp regen ...) while the "general" one is used for
 * everything else that needs to be scheduled.<br>
 * There also is an {@link #aiScheduledThreadPool "ai"} {@link ScheduledThreadPoolExecutor ScheduledThreadPool} used for AI Tasks.
 * </p>
 * <p>
 * Tasks can be sent to {@link ScheduledThreadPoolExecutor ScheduledThreadPool} either with:
 * <ul>
 * <li>{@link #scheduleEffect(Runnable, long)} : for effects Tasks that needs to be executed only once.</li>
 * <li>{@link #scheduleGeneral(Runnable, long)} : for scheduled Tasks that needs to be executed once.</li>
 * <li>{@link #scheduleAi(Runnable, long)} : for AI Tasks that needs to be executed once</li>
 * </ul>
 * or
 * <ul>
 * <li>{@link #scheduleEffectAtFixedRate(Runnable, long, long)} : for effects Tasks that needs to be executed periodicaly.</li>
 * <li>{@link #scheduleGeneralAtFixedRate(Runnable, long, long)} : for scheduled Tasks that needs to be executed periodicaly.</li>
 * <li>{@link #scheduleAiAtFixedRate(Runnable, long, long)} : for AI Tasks that needs to be executed periodicaly</li>
 * </ul>
 * </p>
 * <p>
 * For all Tasks that should be executed with no delay asynchronously in a ThreadPool there also are usual {@link ThreadPoolExecutor ThreadPools} that can grow/shrink according to their load.:
 * <ul>
 * <li>{@link #generalPacketsThreadPool GeneralPackets} where most packets handler are executed.</li>
 * <li>{@link #ioPacketsThreadPool I/O Packets} where all the i/o packets are executed.</li>
 * <li>There will be an AI ThreadPool where AI events should be executed</li>
 * <li>A general ThreadPool where everything else that needs to run asynchronously with no delay should be executed ({@link com.l2jfrozen.gameserver.model.actor.knownlist KnownList} updates, SQL updates/inserts...)?</li>
 * </ul>
 * </p>
 * @author -Wooden-
 */
public class ThreadPoolManager
{
	protected static final Logger LOGGER = Logger.getLogger(ThreadPoolManager.class);
	
	private static final class RunnableWrapper implements Runnable
	{
		private final Runnable runnable;
		
		public RunnableWrapper(final Runnable r)
		{
			runnable = r;
		}
		
		@Override
		public final void run()
		{
			try
			{
				runnable.run();
			}
			catch (final Throwable e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				final Thread t = Thread.currentThread();
				final UncaughtExceptionHandler h = t.getUncaughtExceptionHandler();
				if (h != null)
				{
					h.uncaughtException(t, e);
				}
			}
		}
	}
	
	protected ScheduledThreadPoolExecutor effectsScheduledThreadPool;
	protected ScheduledThreadPoolExecutor generalScheduledThreadPool;
	protected ScheduledThreadPoolExecutor aiScheduledThreadPool;
	private final ThreadPoolExecutor generalPacketsThreadPool;
	private final ThreadPoolExecutor ioPacketsThreadPool;
	private final ThreadPoolExecutor generalThreadPool;
	
	/** temp workaround for VM issue */
	private static final long MAX_DELAY = Long.MAX_VALUE / 1000000 / 2;
	
	private boolean shutdown;
	
	public static ThreadPoolManager getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private ThreadPoolManager()
	{
		effectsScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.THREAD_P_EFFECTS, new PriorityThreadFactory("EffectsSTPool", Thread.NORM_PRIORITY));
		generalScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.THREAD_P_GENERAL, new PriorityThreadFactory("GeneralSTPool", Thread.NORM_PRIORITY));
		ioPacketsThreadPool = new ThreadPoolExecutor(Config.IO_PACKET_THREAD_CORE_SIZE, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new PriorityThreadFactory("I/O Packet Pool", Thread.NORM_PRIORITY + 1));
		generalPacketsThreadPool = new ThreadPoolExecutor(Config.GENERAL_PACKET_THREAD_CORE_SIZE, Config.GENERAL_PACKET_THREAD_CORE_SIZE + 2, 15L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new PriorityThreadFactory("Normal Packet Pool", Thread.NORM_PRIORITY + 1));
		generalThreadPool = new ThreadPoolExecutor(Config.GENERAL_THREAD_CORE_SIZE, Config.GENERAL_THREAD_CORE_SIZE + 2, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new PriorityThreadFactory("General Pool", Thread.NORM_PRIORITY));
		aiScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.AI_MAX_THREAD, new PriorityThreadFactory("AISTPool", Thread.NORM_PRIORITY));
		
		scheduleGeneralAtFixedRate(new PurgeTask(), 10 * 60 * 1000L, 5 * 60 * 1000L);
	}
	
	public static long validateDelay(long delay)
	{
		if (delay < 0)
		{
			delay = 0;
		}
		else if (delay > MAX_DELAY)
		{
			delay = MAX_DELAY;
		}
		return delay;
	}
	
	public ScheduledFuture<?> scheduleEffect(final Runnable r, long delay)
	{
		if (effectsScheduledThreadPool.isShutdown())
		{
			return null;
		}
		
		try
		{
			delay = ThreadPoolManager.validateDelay(delay);
			return effectsScheduledThreadPool.schedule(new RunnableWrapper(r), delay, TimeUnit.MILLISECONDS);
		}
		catch (final RejectedExecutionException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return null;
		}
	}
	
	public ScheduledFuture<?> scheduleEffectAtFixedRate(final Runnable r, long initial, long delay)
	{
		if (effectsScheduledThreadPool.isShutdown())
		{
			return null;
		}
		
		try
		{
			delay = ThreadPoolManager.validateDelay(delay);
			initial = ThreadPoolManager.validateDelay(initial);
			return effectsScheduledThreadPool.scheduleAtFixedRate(new RunnableWrapper(r), initial, delay, TimeUnit.MILLISECONDS);
		}
		catch (final RejectedExecutionException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return null; /* shutdown, ignore */
		}
	}
	
	@Deprecated
	public boolean removeEffect(final RunnableScheduledFuture<?> r)
	{
		return effectsScheduledThreadPool.remove(r);
	}
	
	public ScheduledFuture<?> scheduleGeneral(final Runnable r, long delay)
	{
		if (generalScheduledThreadPool.isShutdown())
		{
			return null;
		}
		
		try
		{
			delay = ThreadPoolManager.validateDelay(delay);
			return generalScheduledThreadPool.schedule(new RunnableWrapper(r), delay, TimeUnit.MILLISECONDS);
		}
		catch (final RejectedExecutionException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return null; /* shutdown, ignore */
		}
	}
	
	public ScheduledFuture<?> scheduleGeneralAtFixedRate(final Runnable r, long initial, long delay)
	{
		if (generalScheduledThreadPool.isShutdown())
		{
			return null;
		}
		
		try
		{
			delay = ThreadPoolManager.validateDelay(delay);
			initial = ThreadPoolManager.validateDelay(initial);
			return generalScheduledThreadPool.scheduleAtFixedRate(new RunnableWrapper(r), initial, delay, TimeUnit.MILLISECONDS);
		}
		catch (final RejectedExecutionException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return null; /* shutdown, ignore */
		}
	}
	
	// @Deprecated
	public boolean removeGeneral(final Runnable r)
	{
		return generalScheduledThreadPool.remove(r);
	}
	
	public ScheduledFuture<?> scheduleAi(final Runnable r, long delay)
	{
		if (aiScheduledThreadPool.isShutdown())
		{
			return null;
		}
		
		try
		{
			delay = ThreadPoolManager.validateDelay(delay);
			return aiScheduledThreadPool.schedule(new RunnableWrapper(r), delay, TimeUnit.MILLISECONDS);
		}
		catch (final RejectedExecutionException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return null; /* shutdown, ignore */
		}
	}
	
	public ScheduledFuture<?> scheduleAiAtFixedRate(final Runnable r, long initial, long delay)
	{
		if (aiScheduledThreadPool.isShutdown())
		{
			return null;
		}
		
		try
		{
			delay = ThreadPoolManager.validateDelay(delay);
			initial = ThreadPoolManager.validateDelay(initial);
			return aiScheduledThreadPool.scheduleAtFixedRate(new RunnableWrapper(r), initial, delay, TimeUnit.MILLISECONDS);
		}
		catch (final RejectedExecutionException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return null; /* shutdown, ignore */
		}
	}
	
	public void executePacket(final Runnable pkt)
	{
		if (!generalPacketsThreadPool.isShutdown())
		{
			generalPacketsThreadPool.execute(pkt);
		}
	}
	
	public void executeCommunityPacket(final Runnable r)
	{
		if (!generalPacketsThreadPool.isShutdown())
		{
			generalPacketsThreadPool.execute(r);
		}
	}
	
	public void executeIOPacket(final Runnable pkt)
	{
		if (!ioPacketsThreadPool.isShutdown())
		{
			ioPacketsThreadPool.execute(pkt);
		}
	}
	
	public void executeTask(final Runnable r)
	{
		if (!generalThreadPool.isShutdown())
		{
			generalThreadPool.execute(r);
		}
	}
	
	public void executeAi(final Runnable r)
	{
		if (!aiScheduledThreadPool.isShutdown())
		{
			aiScheduledThreadPool.execute(new RunnableWrapper(r));
		}
	}
	
	public String[] getStats()
	{
		return new String[]
		{
			"STP:",
			" + Effects:",
			" |- ActiveThreads:   " + effectsScheduledThreadPool.getActiveCount(),
			" |- getCorePoolSize: " + effectsScheduledThreadPool.getCorePoolSize(),
			" |- PoolSize:        " + effectsScheduledThreadPool.getPoolSize(),
			" |- MaximumPoolSize: " + effectsScheduledThreadPool.getMaximumPoolSize(),
			" |- CompletedTasks:  " + effectsScheduledThreadPool.getCompletedTaskCount(),
			" |- ScheduledTasks:  " + (effectsScheduledThreadPool.getTaskCount() - effectsScheduledThreadPool.getCompletedTaskCount()),
			" | -------",
			" + General:",
			" |- ActiveThreads:   " + generalScheduledThreadPool.getActiveCount(),
			" |- getCorePoolSize: " + generalScheduledThreadPool.getCorePoolSize(),
			" |- PoolSize:        " + generalScheduledThreadPool.getPoolSize(),
			" |- MaximumPoolSize: " + generalScheduledThreadPool.getMaximumPoolSize(),
			" |- CompletedTasks:  " + generalScheduledThreadPool.getCompletedTaskCount(),
			" |- ScheduledTasks:  " + (generalScheduledThreadPool.getTaskCount() - generalScheduledThreadPool.getCompletedTaskCount()),
			" | -------",
			" + AI:",
			" |- ActiveThreads:   " + aiScheduledThreadPool.getActiveCount(),
			" |- getCorePoolSize: " + aiScheduledThreadPool.getCorePoolSize(),
			" |- PoolSize:        " + aiScheduledThreadPool.getPoolSize(),
			" |- MaximumPoolSize: " + aiScheduledThreadPool.getMaximumPoolSize(),
			" |- CompletedTasks:  " + aiScheduledThreadPool.getCompletedTaskCount(),
			" |- ScheduledTasks:  " + (aiScheduledThreadPool.getTaskCount() - aiScheduledThreadPool.getCompletedTaskCount()),
			"TP:",
			" + Packets:",
			" |- ActiveThreads:   " + generalPacketsThreadPool.getActiveCount(),
			" |- getCorePoolSize: " + generalPacketsThreadPool.getCorePoolSize(),
			" |- MaximumPoolSize: " + generalPacketsThreadPool.getMaximumPoolSize(),
			" |- LargestPoolSize: " + generalPacketsThreadPool.getLargestPoolSize(),
			" |- PoolSize:        " + generalPacketsThreadPool.getPoolSize(),
			" |- CompletedTasks:  " + generalPacketsThreadPool.getCompletedTaskCount(),
			" |- QueuedTasks:     " + generalPacketsThreadPool.getQueue().size(),
			" | -------",
			" + I/O Packets:",
			" |- ActiveThreads:   " + ioPacketsThreadPool.getActiveCount(),
			" |- getCorePoolSize: " + ioPacketsThreadPool.getCorePoolSize(),
			" |- MaximumPoolSize: " + ioPacketsThreadPool.getMaximumPoolSize(),
			" |- LargestPoolSize: " + ioPacketsThreadPool.getLargestPoolSize(),
			" |- PoolSize:        " + ioPacketsThreadPool.getPoolSize(),
			" |- CompletedTasks:  " + ioPacketsThreadPool.getCompletedTaskCount(),
			" |- QueuedTasks:     " + ioPacketsThreadPool.getQueue().size(),
			" | -------",
			" + General Tasks:",
			" |- ActiveThreads:   " + generalThreadPool.getActiveCount(),
			" |- getCorePoolSize: " + generalThreadPool.getCorePoolSize(),
			" |- MaximumPoolSize: " + generalThreadPool.getMaximumPoolSize(),
			" |- LargestPoolSize: " + generalThreadPool.getLargestPoolSize(),
			" |- PoolSize:        " + generalThreadPool.getPoolSize(),
			" |- CompletedTasks:  " + generalThreadPool.getCompletedTaskCount(),
			" |- QueuedTasks:     " + generalThreadPool.getQueue().size(),
			" | -------",
			" + Javolution stats:",
			" |- FastSet:        " + FastSet.report(),
			" | -------"
		};
	}
	
	private static class PriorityThreadFactory implements ThreadFactory
	{
		private final int prio;
		private final String name;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		
		public PriorityThreadFactory(final String name, final int prio)
		{
			this.prio = prio;
			this.name = name;
			group = new ThreadGroup(this.name);
		}
		
		@Override
		public Thread newThread(final Runnable r)
		{
			final Thread t = new Thread(group, r);
			t.setName(name + "-" + threadNumber.getAndIncrement());
			t.setPriority(prio);
			return t;
		}
		
		public ThreadGroup getGroup()
		{
			return group;
		}
	}
	
	public void shutdown()
	{
		shutdown = true;
		try
		{
			effectsScheduledThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			generalScheduledThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			generalPacketsThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			ioPacketsThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			generalThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			effectsScheduledThreadPool.shutdown();
			generalScheduledThreadPool.shutdown();
			generalPacketsThreadPool.shutdown();
			ioPacketsThreadPool.shutdown();
			generalThreadPool.shutdown();
			LOGGER.info("All ThreadPools are now stopped.");
			
		}
		catch (final InterruptedException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.warn("", e);
		}
	}
	
	public boolean isShutdown()
	{
		return shutdown;
	}
	
	public void purge()
	{
		effectsScheduledThreadPool.purge();
		generalScheduledThreadPool.purge();
		aiScheduledThreadPool.purge();
		ioPacketsThreadPool.purge();
		generalPacketsThreadPool.purge();
		generalThreadPool.purge();
	}
	
	public String getPacketStats()
	{
		final StringBuilder sb = new StringBuilder(1000);
		final ThreadFactory tf = generalPacketsThreadPool.getThreadFactory();
		if (tf instanceof PriorityThreadFactory)
		{
			final PriorityThreadFactory ptf = (PriorityThreadFactory) tf;
			final int count = ptf.getGroup().activeCount();
			final Thread[] threads = new Thread[count + 2];
			ptf.getGroup().enumerate(threads);
			StringUtil.append(sb, "General Packet Thread Pool:\r\n" + "Tasks in the queue: ", String.valueOf(generalPacketsThreadPool.getQueue().size()), "\r\n" + "Showing threads stack trace:\r\n" + "There should be ", String.valueOf(count), " Threads\r\n");
			for (final Thread t : threads)
			{
				if (t == null)
				{
					continue;
				}
				
				StringUtil.append(sb, t.getName(), "\r\n");
				for (final StackTraceElement ste : t.getStackTrace())
				{
					StringUtil.append(sb, ste.toString(), "\r\n");
				}
			}
		}
		
		sb.append("Packet Tp stack traces printed.\r\n");
		
		return sb.toString();
	}
	
	public String getIOPacketStats()
	{
		final StringBuilder sb = new StringBuilder(1000);
		final ThreadFactory tf = ioPacketsThreadPool.getThreadFactory();
		
		if (tf instanceof PriorityThreadFactory)
		{
			final PriorityThreadFactory ptf = (PriorityThreadFactory) tf;
			final int count = ptf.getGroup().activeCount();
			final Thread[] threads = new Thread[count + 2];
			ptf.getGroup().enumerate(threads);
			StringUtil.append(sb, "I/O Packet Thread Pool:\r\n" + "Tasks in the queue: ", String.valueOf(ioPacketsThreadPool.getQueue().size()), "\r\n" + "Showing threads stack trace:\r\n" + "There should be ", String.valueOf(count), " Threads\r\n");
			
			for (final Thread t : threads)
			{
				if (t == null)
				{
					continue;
				}
				
				StringUtil.append(sb, t.getName(), "\r\n");
				
				for (final StackTraceElement ste : t.getStackTrace())
				{
					StringUtil.append(sb, ste.toString(), "\r\n");
				}
			}
		}
		
		sb.append("Packet Tp stack traces printed.\r\n");
		
		return sb.toString();
	}
	
	public String getGeneralStats()
	{
		final StringBuilder sb = new StringBuilder(1000);
		final ThreadFactory tf = generalThreadPool.getThreadFactory();
		
		if (tf instanceof PriorityThreadFactory)
		{
			final PriorityThreadFactory ptf = (PriorityThreadFactory) tf;
			final int count = ptf.getGroup().activeCount();
			final Thread[] threads = new Thread[count + 2];
			ptf.getGroup().enumerate(threads);
			StringUtil.append(sb, "General Thread Pool:\r\n" + "Tasks in the queue: ", String.valueOf(generalThreadPool.getQueue().size()), "\r\n" + "Showing threads stack trace:\r\n" + "There should be ", String.valueOf(count), " Threads\r\n");
			
			for (final Thread t : threads)
			{
				if (t == null)
				{
					continue;
				}
				
				StringUtil.append(sb, t.getName(), "\r\n");
				
				for (final StackTraceElement ste : t.getStackTrace())
				{
					StringUtil.append(sb, ste.toString(), "\r\n");
				}
			}
		}
		
		sb.append("Packet Tp stack traces printed.\r\n");
		
		return sb.toString();
	}
	
	protected class PurgeTask implements Runnable
	{
		@Override
		public void run()
		{
			effectsScheduledThreadPool.purge();
			generalScheduledThreadPool.purge();
			aiScheduledThreadPool.purge();
		}
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ThreadPoolManager instance = new ThreadPoolManager();
	}
}