package com.l2jfrozen.util.object;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jfrozen.gameserver.model.L2Object;

/**
 * This class ...
 * @author  luisantonioa
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 * @param   <T>
 */
public class WorldObjectMap<T extends L2Object> extends L2ObjectMap<T>
{
	Map<Integer, T> objectMap = new ConcurrentHashMap<>();
	
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
		if (obj != null)
		{
			objectMap.put(obj.getObjectId(), obj);
		}
	}
	
	@Override
	public void remove(final T obj)
	{
		if (obj != null)
		{
			objectMap.remove(obj.getObjectId());
		}
	}
	
	@Override
	public T get(final int id)
	{
		return objectMap.get(id);
	}
	
	@Override
	public boolean contains(final T obj)
	{
		if (obj == null)
		{
			return false;
		}
		return objectMap.get(obj.getObjectId()) != null;
	}
	
	@Override
	public Iterator<T> iterator()
	{
		return objectMap.values().iterator();
	}
	
}
