package com.l2jfrozen.gameserver.taskmanager;

import java.util.concurrent.Future;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * @author NB4L1
 */
public abstract class ExclusiveTask
{
	private final boolean returnIfAlreadyRunning;
	
	private Future<?> future;
	private boolean isRunning;
	private Thread currentThread;
	
	protected ExclusiveTask(final boolean returnIfAlreadyRunning)
	{
		this.returnIfAlreadyRunning = returnIfAlreadyRunning;
	}
	
	protected ExclusiveTask()
	{
		this(false);
	}
	
	public synchronized boolean isScheduled()
	{
		return future != null;
	}
	
	public synchronized final void cancel()
	{
		if (future != null)
		{
			future.cancel(false);
			future = null;
		}
	}
	
	public synchronized final void schedule(final long delay)
	{
		cancel();
		
		future = ThreadPoolManager.getInstance().scheduleEffect(runnable, delay);
	}
	
	public synchronized final void execute()
	{
		ThreadPoolManager.getInstance().executeTask(runnable);
	}
	
	public synchronized final void scheduleAtFixedRate(final long delay, final long period)
	{
		cancel();
		
		future = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(runnable, delay, period);
	}
	
	private final Runnable runnable = () ->
	{
		if (tryLock())
		{
			try
			{
				onElapsed();
			}
			finally
			{
				unlock();
			}
		}
	};
	
	protected abstract void onElapsed();
	
	protected synchronized boolean tryLock()
	{
		if (returnIfAlreadyRunning)
		{
			return !isRunning;
		}
		
		currentThread = Thread.currentThread();
		
		for (;;)
		{
			try
			{
				notifyAll();
				
				if (currentThread != Thread.currentThread())
				{
					return false;
				}
				
				if (!isRunning)
				{
					return true;
				}
				
				wait();
			}
			catch (final InterruptedException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	protected synchronized void unlock()
	{
		isRunning = false;
	}
}
