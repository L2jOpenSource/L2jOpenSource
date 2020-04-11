package com.l2jfrozen.gameserver.geo.util;

import java.util.Arrays;
import java.util.Iterator;

@SuppressWarnings("unchecked")
public class LookupTable<T> implements Iterable<T>
{
	private static final Object[] EMPTY_ARRAY = new Object[0];
	
	private Object[] array = EMPTY_ARRAY;
	
	private int offset = 0;
	
	private int size = 0;
	
	public int size()
	{
		return size;
	}
	
	public boolean isEmpty()
	{
		return size == 0;
	}
	
	public void clear(final boolean force)
	{
		if (force)
		{
			array = EMPTY_ARRAY;
		}
		else
		{
			Arrays.fill(array, null);
		}
		
		offset = 0;
		size = 0;
	}
	
	/**
	 * @param  key
	 * @return     the mapped value if exists, or null if not
	 */
	public T get(final int key)
	{
		final int index = key + offset;
		
		if (index < 0 || array.length <= index)
		{
			return null;
		}
		
		return (T) array[index];
	}
	
	/**
	 * @param key
	 * @param newValue
	 */
	public void set(final int key, final T newValue)
	{
		final int index = key + offset;
		
		if (0 <= index && index < array.length)
		{
			final T oldValue = (T) array[index];
			
			array[index] = newValue;
			
			if (oldValue != null && oldValue != newValue)
			{
				replacedValue(key, oldValue, newValue);
			}
			
			if (oldValue == null)
			{
				if (newValue != null)
				{
					size++;
				}
			}
			else
			{
				if (newValue == null)
				{
					size--;
				}
			}
			
			return;
		}
		
		size++;
		
		if (array.length == 0)
		{
			array = new Object[]
			{
				newValue
			};
			offset = -1 * key;
			return;
		}
		
		final int minimumKey = Math.min(0 - offset, key);
		final int maximumKey = Math.max((array.length - 1) - offset, key);
		
		final Object[] newArray = new Object[maximumKey - minimumKey + 1];
		final int newOffset = -1 * minimumKey;
		
		System.arraycopy(array, 0, newArray, newOffset - offset, array.length);
		
		array = newArray;
		offset = newOffset;
		
		array[key + offset] = newValue;
	}
	
	/**
	 * Called when an existing mapping gets overwritten by a different one.
	 * @param key
	 * @param oldValue
	 * @param newValue
	 */
	protected void replacedValue(final int key, final T oldValue, final T newValue)
	{
	}
	
	@Override
	public Iterator<T> iterator()
	{
		return L2Arrays.iterator(array, false);
	}
}
