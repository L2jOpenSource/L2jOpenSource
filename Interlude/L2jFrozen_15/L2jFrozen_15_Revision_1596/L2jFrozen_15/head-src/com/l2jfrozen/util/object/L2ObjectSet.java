package com.l2jfrozen.util.object;

import java.util.Iterator;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;

/**
 * This class ...
 * @author  luisantonioa
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 * @param   <T>
 */
public abstract class L2ObjectSet<T extends L2Object> implements Iterable<T>
{
	public static L2ObjectSet<L2Object> createL2ObjectSet()
	{
		switch (Config.SET_TYPE)
		{
			case WorldObjectSet:
				return new WorldObjectSet<>();
			default:
				return new L2ObjectHashSet<>();
		}
	}
	
	public static L2ObjectSet<L2PlayableInstance> createL2PlayerSet()
	{
		switch (Config.SET_TYPE)
		{
			case WorldObjectSet:
				return new WorldObjectSet<>();
			default:
				return new L2ObjectHashSet<>();
		}
	}
	
	public abstract int size();
	
	public abstract boolean isEmpty();
	
	public abstract void clear();
	
	public abstract void put(T obj);
	
	public abstract void remove(T obj);
	
	public abstract boolean contains(T obj);
	
	@Override
	public abstract Iterator<T> iterator();
	
}
