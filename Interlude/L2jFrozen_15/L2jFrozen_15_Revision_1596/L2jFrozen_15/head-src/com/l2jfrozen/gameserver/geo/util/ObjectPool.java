package com.l2jfrozen.gameserver.geo.util;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.thread.L2Thread;

public abstract class ObjectPool<E>
{
	protected static final WeakHashMap<ObjectPool<?>, Object> POOLS = new WeakHashMap<>();
	
	static
	{
		new L2Thread(ObjectPool.class.getName())
		{
			@Override
			protected void runTurn()
			{
				try
				{
					for (final ObjectPool<?> pool : POOLS.keySet())
					{
						if (pool != null)
						{
							pool.purge();
						}
					}
				}
				catch (final ConcurrentModificationException e)
				{
					// skip it
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
					
				}
			}
			
			@Override
			protected void sleepTurn() throws InterruptedException
			{
				Thread.sleep(60000);
			}
		}.start();
	}
	
	private final ReentrantLock lock = new ReentrantLock();
	
	private Object[] elements = new Object[0];
	private long[] access = new long[0];
	private int size = 0;
	
	protected ObjectPool()
	{
		POOLS.put(this, POOLS);
	}
	
	public int getCurrentSize()
	{
		lock.lock();
		try
		{
			return size;
		}
		finally
		{
			lock.unlock();
		}
	}
	
	protected int getMaximumSize()
	{
		return Integer.MAX_VALUE;
	}
	
	protected long getMaxLifeTime()
	{
		return 120000; // 2 min
	}
	
	public void clear()
	{
		lock.lock();
		try
		{
			elements = new Object[0];
			access = new long[0];
			size = 0;
		}
		finally
		{
			lock.unlock();
		}
	}
	
	public void store(final E e)
	{
		if (getCurrentSize() >= getMaximumSize())
		{
			return;
		}
		
		reset(e);
		
		lock.lock();
		try
		{
			if (size == elements.length)
			{
				elements = Arrays.copyOf(elements, elements.length + 10);
				access = Arrays.copyOf(access, access.length + 10);
			}
			
			elements[size] = e;
			access[size] = System.currentTimeMillis();
			
			size++;
		}
		finally
		{
			lock.unlock();
		}
	}
	
	protected void reset(final E e)
	{
	}
	
	@SuppressWarnings("unchecked")
	public E get()
	{
		Object obj = null;
		
		lock.lock();
		try
		{
			if (size > 0)
			{
				size--;
				
				obj = elements[size];
				
				elements[size] = null;
				access[size] = 0;
			}
		}
		finally
		{
			lock.unlock();
		}
		
		return obj == null ? create() : (E) obj;
	}
	
	protected abstract E create();
	
	public void purge()
	{
		lock.lock();
		try
		{
			int newIndex = 0;
			for (int oldIndex = 0; oldIndex < elements.length; oldIndex++)
			{
				final Object obj = elements[oldIndex];
				final long time = access[oldIndex];
				
				elements[oldIndex] = null;
				access[oldIndex] = 0;
				
				if (obj == null || time + getMaxLifeTime() < System.currentTimeMillis())
				{
					continue;
				}
				
				elements[newIndex] = obj;
				access[newIndex] = time;
				
				newIndex++;
			}
			
			elements = Arrays.copyOf(elements, newIndex);
			access = Arrays.copyOf(access, newIndex);
			size = newIndex;
		}
		finally
		{
			lock.unlock();
		}
	}
}
