package com.l2jfrozen.util.object;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.l2jfrozen.gameserver.model.L2Object;

/**
 * @author dishkols
 * @param  <T>
 */
public class WorldObjectTree<T extends L2Object> extends L2ObjectMap<T>
{
	private final TreeMap<Integer, T> objectMap = new TreeMap<>();
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();
	
	/**
	 * @see com.l2jfrozen.util.object.L2ObjectMap#size()
	 */
	@Override
	public int size()
	{
		r.lock();
		try
		{
			return objectMap.size();
		}
		finally
		{
			r.unlock();
		}
	}
	
	/**
	 * @see com.l2jfrozen.util.object.L2ObjectMap#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		r.lock();
		try
		{
			return objectMap.isEmpty();
		}
		finally
		{
			r.unlock();
		}
	}
	
	/**
	 * @see com.l2jfrozen.util.object.L2ObjectMap#clear()
	 */
	@Override
	public void clear()
	{
		w.lock();
		try
		{
			objectMap.clear();
		}
		finally
		{
			w.unlock();
		}
	}
	
	@Override
	public void put(final T obj)
	{
		if (obj != null)
		{
			w.lock();
			try
			{
				objectMap.put(obj.getObjectId(), obj);
			}
			finally
			{
				w.unlock();
			}
		}
	}
	
	@Override
	public void remove(final T obj)
	{
		if (obj != null)
		{
			w.lock();
			try
			{
				objectMap.remove(obj.getObjectId());
			}
			finally
			{
				w.unlock();
			}
		}
	}
	
	/**
	 * @see com.l2jfrozen.util.object.L2ObjectMap#get(int)
	 */
	@Override
	public T get(final int id)
	{
		r.lock();
		try
		{
			return objectMap.get(id);
		}
		finally
		{
			r.unlock();
		}
	}
	
	@Override
	public boolean contains(final T obj)
	{
		if (obj == null)
		{
			return false;
		}
		r.lock();
		try
		{
			return objectMap.containsValue(obj);
		}
		finally
		{
			r.unlock();
		}
	}
	
	/**
	 * @see com.l2jfrozen.util.object.L2ObjectMap#iterator()
	 */
	@Override
	public Iterator<T> iterator()
	{
		r.lock();
		try
		{
			return objectMap.values().iterator();
		}
		finally
		{
			r.unlock();
		}
	}
	
}
