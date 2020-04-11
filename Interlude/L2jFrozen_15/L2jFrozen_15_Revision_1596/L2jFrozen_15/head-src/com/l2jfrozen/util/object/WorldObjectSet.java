package com.l2jfrozen.util.object;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jfrozen.gameserver.model.L2Object;

/**
 * @author luisantonioa
 * @param  <T>
 */
public class WorldObjectSet<T extends L2Object> extends L2ObjectSet<T>
{
	private final Map<Integer, T> objectMap;
	
	public WorldObjectSet()
	{
		objectMap = new ConcurrentHashMap<>();
	}
	
	@Override
	public int size()
	{
		return objectMap.size();
	}
	
	@Override
	public boolean isEmpty()
	{
		return objectMap.isEmpty();
	}
	
	@Override
	public void clear()
	{
		objectMap.clear();
	}
	
	@Override
	public void put(final T obj)
	{
		objectMap.put(obj.getObjectId(), obj);
	}
	
	@Override
	public void remove(final T obj)
	{
		objectMap.remove(obj.getObjectId());
	}
	
	@Override
	public boolean contains(final T obj)
	{
		return objectMap.containsKey(obj.getObjectId());
	}
	
	@Override
	public Iterator<T> iterator()
	{
		return objectMap.values().iterator();
	}
	
}
