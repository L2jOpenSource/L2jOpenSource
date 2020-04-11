package com.l2jfrozen.util.object;

import java.util.Iterator;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Object;

/**
 * This class ...
 * @author  luisantonioa
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 * @param   <T>
 */
public abstract class L2ObjectMap<T extends L2Object> implements Iterable<T>
{
	
	public abstract int size();
	
	public abstract boolean isEmpty();
	
	public abstract void clear();
	
	public abstract void put(T obj);
	
	public abstract void remove(T obj);
	
	public abstract T get(int id);
	
	public abstract boolean contains(T obj);
	
	@Override
	public abstract Iterator<T> iterator();
	
	public static L2ObjectMap<L2Object> createL2ObjectMap()
	{
		switch (Config.MAP_TYPE)
		{
			case WorldObjectMap:
				return new WorldObjectMap<>();
			default:
				return new WorldObjectTree<>();
		}
	}
}
