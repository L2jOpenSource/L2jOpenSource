package com.l2jfrozen.gameserver.geo.util;

import java.util.Iterator;
import java.util.Set;

import javolution.util.FastCollection.Record;
import javolution.util.FastMap;

@SuppressWarnings("unchecked")
public class L2FastSet<E> extends L2FastCollection<E> implements Set<E>
{
	private static final Object NULL = new Object();
	
	private final FastMap<E, Object> map;
	
	public L2FastSet()
	{
		map = new FastMap<>();
	}
	
	public L2FastSet(final int capacity)
	{
		map = new FastMap<>(capacity);
	}
	
	public L2FastSet(final Set<? extends E> elements)
	{
		map = new FastMap<>(elements.size());
		
		addAll(elements);
	}
	
	@SuppressWarnings("deprecation")
	public L2FastSet<E> setShared(final boolean isShared)
	{
		if (isShared)
		{
			map.shared();
		}
		else
		{
			map.setShared(false);
		}
		return this;
	}
	
	public boolean isShared()
	{
		return map.isShared();
	}
	
	@Override
	public Record head()
	{
		return map.head();
	}
	
	@Override
	public Record tail()
	{
		return map.tail();
	}
	
	@Override
	public E valueOf(final Record record)
	{
		return ((FastMap.Entry<E, Object>) record).getKey();
	}
	
	@Override
	public void delete(final Record record)
	{
		map.remove(((FastMap.Entry<E, Object>) record).getKey());
	}
	
	@Override
	public void delete(final Record record, final E value)
	{
		map.remove(value);
	}
	
	@Override
	public boolean add(final E value)
	{
		return map.put(value, NULL) == null;
	}
	
	@Override
	public void clear()
	{
		map.clear();
	}
	
	@Override
	public boolean contains(final Object o)
	{
		return map.containsKey(o);
	}
	
	@Override
	public boolean isEmpty()
	{
		return map.isEmpty();
	}
	
	@Override
	public Iterator<E> iterator()
	{
		return map.keySet().iterator();
	}
	
	@Override
	public boolean remove(final Object o)
	{
		return map.remove(o) != null;
	}
	
	@Override
	public int size()
	{
		return map.size();
	}
}
