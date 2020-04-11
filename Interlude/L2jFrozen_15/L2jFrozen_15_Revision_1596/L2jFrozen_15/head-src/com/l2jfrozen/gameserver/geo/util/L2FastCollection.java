package com.l2jfrozen.gameserver.geo.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

import javolution.util.FastCollection;
import javolution.util.FastCollection.Record;

@SuppressWarnings("unchecked")
public abstract class L2FastCollection<E> implements Collection<E>
{
	public abstract Record head();
	
	public abstract Record tail();
	
	public abstract E valueOf(Record record);
	
	public abstract void delete(Record record);
	
	public abstract void delete(Record record, E value);
	
	public final E getFirst()
	{
		final Record first = head().getNext();
		if (first == tail())
		{
			return null;
		}
		
		return valueOf(first);
	}
	
	public final E getLast()
	{
		final Record last = tail().getPrevious();
		if (last == head())
		{
			return null;
		}
		
		return valueOf(last);
	}
	
	public final E removeFirst()
	{
		final Record first = head().getNext();
		if (first == tail())
		{
			return null;
		}
		
		final E value = valueOf(first);
		delete(first, value);
		return value;
	}
	
	public final E removeLast()
	{
		final Record last = tail().getPrevious();
		if (last == head())
		{
			return null;
		}
		
		final E value = valueOf(last);
		delete(last, value);
		return value;
	}
	
	public boolean addAll(final E[] c)
	{
		boolean modified = false;
		
		for (final E e : c)
		{
			if (add(e))
			{
				modified = true;
			}
		}
		
		return modified;
	}
	
	@Override
	public boolean addAll(final Collection<? extends E> c)
	{
		return addAll((Iterable<? extends E>) c);
	}
	
	public boolean addAll(final Iterable<? extends E> c)
	{
		if (c instanceof RandomAccess && c instanceof List<?>)
		{
			return addAll((List<? extends E>) c);
		}
		
		if (c instanceof FastCollection<?>)
		{
			return addAll((FastCollection<? extends E>) c);
		}
		
		if (c instanceof L2FastCollection<?>)
		{
			return addAll((L2FastCollection<? extends E>) c);
		}
		
		boolean modified = false;
		
		for (final E e : c)
		{
			if (add(e))
			{
				modified = true;
			}
		}
		
		return modified;
	}
	
	private boolean addAll(final L2FastCollection<? extends E> c)
	{
		boolean modified = false;
		
		for (Record r = c.head(), end = c.tail(); (r = r.getNext()) != end;)
		{
			if (add(c.valueOf(r)))
			{
				modified = true;
			}
		}
		
		return modified;
	}
	
	private boolean addAll(final FastCollection<? extends E> c)
	{
		boolean modified = false;
		
		for (Record r = c.head(), end = c.tail(); (r = r.getNext()) != end;)
		{
			if (add(c.valueOf(r)))
			{
				modified = true;
			}
		}
		
		return modified;
	}
	
	private boolean addAll(final List<? extends E> c)
	{
		boolean modified = false;
		
		for (int i = 0, size = c.size(); i < size;)
		{
			if (add(c.get(i++)))
			{
				modified = true;
			}
		}
		
		return modified;
	}
	
	public boolean containsAll(final Object[] c)
	{
		for (final Object obj : c)
		{
			if (!contains(obj))
			{
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public boolean containsAll(final Collection<?> c)
	{
		return containsAll((Iterable<? extends E>) c);
	}
	
	public boolean containsAll(final Iterable<?> c)
	{
		if (c instanceof RandomAccess && c instanceof List<?>)
		{
			return containsAll((List<?>) c);
		}
		
		if (c instanceof FastCollection<?>)
		{
			return containsAll((FastCollection<?>) c);
		}
		
		if (c instanceof L2FastCollection<?>)
		{
			return containsAll((L2FastCollection<?>) c);
		}
		
		for (final Object obj : c)
		{
			if (!contains(obj))
			{
				return false;
			}
		}
		
		return true;
	}
	
	private boolean containsAll(final L2FastCollection<?> c)
	{
		for (Record r = c.head(), end = c.tail(); (r = r.getNext()) != end;)
		{
			if (!contains(c.valueOf(r)))
			{
				return false;
			}
		}
		
		return true;
	}
	
	private boolean containsAll(final FastCollection<?> c)
	{
		for (Record r = c.head(), end = c.tail(); (r = r.getNext()) != end;)
		{
			if (!contains(c.valueOf(r)))
			{
				return false;
			}
		}
		
		return true;
	}
	
	private boolean containsAll(final List<?> c)
	{
		for (int i = 0, size = c.size(); i < size;)
		{
			if (!contains(c.get(i++)))
			{
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public boolean removeAll(final Collection<?> c)
	{
		boolean modified = false;
		
		for (Record head = head(), r = tail().getPrevious(), previous; r != head; r = previous)
		{
			previous = r.getPrevious();
			if (c.contains(valueOf(r)))
			{
				delete(r);
				modified = true;
			}
		}
		
		return modified;
	}
	
	@Override
	public boolean retainAll(final Collection<?> c)
	{
		boolean modified = false;
		
		for (Record head = head(), r = tail().getPrevious(), previous; r != head; r = previous)
		{
			previous = r.getPrevious();
			if (!c.contains(valueOf(r)))
			{
				delete(r);
				modified = true;
			}
		}
		
		return modified;
	}
	
	@Override
	public Object[] toArray()
	{
		return toArray(new Object[size()]);
	}
	
	@Override
	public <T> T[] toArray(T[] array)
	{
		final int size = size();
		
		if (array.length != size)
		{
			array = (T[]) Array.newInstance(array.getClass().getComponentType(), size);
		}
		
		if (size == 0 && array.length == 0)
		{
			return array;
		}
		
		int i = 0;
		for (Record r = head(), end = tail(); (r = r.getNext()) != end;)
		{
			array[i++] = (T) valueOf(r);
		}
		
		return array;
	}
}
