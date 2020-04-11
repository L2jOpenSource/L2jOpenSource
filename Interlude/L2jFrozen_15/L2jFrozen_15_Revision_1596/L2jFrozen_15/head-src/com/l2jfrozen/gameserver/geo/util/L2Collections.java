package com.l2jfrozen.gameserver.geo.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@SuppressWarnings("unchecked")
public final class L2Collections
{
	protected static final Object[] EMPTY_ARRAY = new Object[0];
	
	private static final class EmptyListIterator implements ListIterator<Object>
	{
		protected static final ListIterator<Object> INSTANCE = new EmptyListIterator();
		
		@Override
		public boolean hasNext()
		{
			return false;
		}
		
		@Override
		public Object next()
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public boolean hasPrevious()
		{
			return false;
		}
		
		@Override
		public Object previous()
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public int nextIndex()
		{
			return 0;
		}
		
		@Override
		public int previousIndex()
		{
			return -1;
		}
		
		@Override
		public void add(final Object obj)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void set(final Object obj)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
	
	private static class EmptyCollection implements Collection<Object>
	{
		protected static final Collection<Object> INSTANCE = new EmptyCollection();
		
		protected EmptyCollection()
		{
			
		}
		
		@Override
		public boolean add(final Object e)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public boolean addAll(final Collection<? extends Object> c)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void clear()
		{
		}
		
		@Override
		public boolean contains(final Object o)
		{
			return false;
		}
		
		@Override
		public boolean containsAll(final Collection<?> c)
		{
			return false;
		}
		
		@Override
		public boolean isEmpty()
		{
			return true;
		}
		
		@Override
		public Iterator<Object> iterator()
		{
			return emptyListIterator();
		}
		
		@Override
		public boolean remove(final Object o)
		{
			return false;
		}
		
		@Override
		public boolean removeAll(final Collection<?> c)
		{
			return false;
		}
		
		@Override
		public boolean retainAll(final Collection<?> c)
		{
			return false;
		}
		
		@Override
		public int size()
		{
			return 0;
		}
		
		@Override
		public Object[] toArray()
		{
			return EMPTY_ARRAY;
		}
		
		@Override
		public <T> T[] toArray(T[] a)
		{
			if (a.length != 0)
			{
				a = (T[]) Array.newInstance(a.getClass().getComponentType(), 0);
			}
			
			return a;
		}
	}
	
	private static final class EmptySet extends EmptyCollection implements Set<Object>
	{
		protected static final Set<Object> INSTANCE = new EmptySet();
	}
	
	private static final class EmptyList extends EmptyCollection implements List<Object>
	{
		protected static final List<Object> INSTANCE = new EmptyList();
		
		@Override
		public void add(final int index, final Object element)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public boolean addAll(final int index, final Collection<?> c)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public Object get(final int index)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public int indexOf(final Object o)
		{
			return -1;
		}
		
		@Override
		public int lastIndexOf(final Object o)
		{
			return -1;
		}
		
		@Override
		public ListIterator<Object> listIterator()
		{
			return emptyListIterator();
		}
		
		@Override
		public ListIterator<Object> listIterator(final int index)
		{
			return emptyListIterator();
		}
		
		@Override
		public Object remove(final int index)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public Object set(final int index, final Object element)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public List<Object> subList(final int fromIndex, final int toIndex)
		{
			throw new UnsupportedOperationException();
		}
	}
	
	private static final class EmptyMap implements Map<Object, Object>
	{
		protected static final Map<Object, Object> INSTANCE = new EmptyMap();
		
		@Override
		public void clear()
		{
		}
		
		@Override
		public boolean containsKey(final Object key)
		{
			return false;
		}
		
		@Override
		public boolean containsValue(final Object value)
		{
			return false;
		}
		
		@Override
		public Set<Map.Entry<Object, Object>> entrySet()
		{
			return emptySet();
		}
		
		@Override
		public Object get(final Object key)
		{
			return null;
		}
		
		@Override
		public boolean isEmpty()
		{
			return true;
		}
		
		@Override
		public Set<Object> keySet()
		{
			return emptySet();
		}
		
		@Override
		public Object put(final Object key, final Object value)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void putAll(final Map<?, ?> m)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public Object remove(final Object key)
		{
			return null;
		}
		
		@Override
		public int size()
		{
			return 0;
		}
		
		@Override
		public Collection<Object> values()
		{
			return emptyCollection();
		}
	}
	
	private static final class EmptyBunch implements Bunch<Object>
	{
		protected static final Bunch<Object> INSTANCE = new EmptyBunch();
		
		@Override
		public Bunch<Object> add(final Object e)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public Bunch<Object> addAll(final Iterable<?> c)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public Bunch<Object> addAll(final Object[] array)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void clear()
		{
		}
		
		@Override
		public boolean contains(final Object o)
		{
			return false;
		}
		
		@Override
		public Object get(final int index)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public boolean isEmpty()
		{
			return true;
		}
		
		@Override
		public Object[] moveToArray()
		{
			return EMPTY_ARRAY;
		}
		
		@Override
		public <T> T[] moveToArray(T[] array)
		{
			if (array.length != 0)
			{
				array = (T[]) Array.newInstance(array.getClass().getComponentType(), 0);
			}
			
			return array;
		}
		
		@Override
		public <T> T[] moveToArray(final Class<T> clazz)
		{
			return (T[]) Array.newInstance(clazz, 0);
		}
		
		@Override
		public List<Object> moveToList(final List<Object> list)
		{
			return list;
		}
		
		@Override
		public Bunch<Object> remove(final Object o)
		{
			return this;
		}
		
		@Override
		public Object remove(final int index)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public Object set(final int index, final Object value)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public int size()
		{
			return 0;
		}
		
		@Override
		public Bunch<Object> cleanByFilter(final Filter<Object> filter)
		{
			return this;
		}
	}
	
	protected static <T> ListIterator<T> emptyListIterator()
	{
		return (ListIterator<T>) EmptyListIterator.INSTANCE;
	}
	
	protected static <T> Collection<T> emptyCollection()
	{
		return (Collection<T>) EmptyCollection.INSTANCE;
	}
	
	public static <T> Set<T> emptySet()
	{
		return (Set<T>) EmptySet.INSTANCE;
	}
	
	public static <T> List<T> emptyList()
	{
		return (List<T>) EmptyList.INSTANCE;
	}
	
	public static <K, V> Map<K, V> emptyMap()
	{
		return (Map<K, V>) EmptyMap.INSTANCE;
	}
	
	public static <T> Bunch<T> emptyBunch()
	{
		return (Bunch<T>) EmptyBunch.INSTANCE;
	}
	
	public static <T> Iterable<T> filteredIterable(final Class<T> clazz, final Iterable<? super T> iterable)
	{
		return filteredIterable(clazz, iterable, null);
	}
	
	public static <T> Iterable<T> filteredIterable(final Class<T> clazz, final Iterable<? super T> iterable, final Filter<T> filter)
	{
		return new FilteredIterable<>(clazz, iterable, filter);
	}
	
	public static <T> Iterator<T> filteredIterator(final Class<T> clazz, final Iterable<? super T> iterable)
	{
		return filteredIterator(clazz, iterable, null);
	}
	
	public static <T> Iterator<T> filteredIterator(final Class<T> clazz, final Iterable<? super T> iterable, final Filter<T> filter)
	{
		return new FilteredIterator<>(clazz, iterable, filter);
	}
	
	public interface Filter<E>
	{
		public boolean accept(E element);
	}
	
	private static final class FilteredIterable<E> implements Iterable<E>
	{
		private final Iterable<? super E> iterable;
		private final Filter<E> filter;
		private final Class<E> clazz;
		
		protected FilteredIterable(final Class<E> clazz, final Iterable<? super E> iterable, final Filter<E> filter)
		{
			this.iterable = iterable;
			this.filter = filter;
			this.clazz = clazz;
		}
		
		@Override
		public Iterator<E> iterator()
		{
			return filteredIterator(clazz, iterable, filter);
		}
	}
	
	private static final class FilteredIterator<E> implements Iterator<E>
	{
		private final Iterator<? super E> iterator;
		private final Filter<E> filter;
		private final Class<E> clazz;
		
		private E nextE;
		
		protected FilteredIterator(final Class<E> clazz, final Iterable<? super E> iterable, final Filter<E> filter)
		{
			iterator = iterable.iterator();
			this.filter = filter;
			this.clazz = clazz;
			
			step();
		}
		
		@Override
		public boolean hasNext()
		{
			return nextE != null;
		}
		
		@Override
		public E next()
		{
			if (!hasNext())
			{
				throw new NoSuchElementException();
			}
			
			final E next = nextE;
			
			step();
			
			return next;
		}
		
		private void step()
		{
			while (iterator.hasNext())
			{
				final Object next = iterator.next();
				
				if (next == null || !clazz.isInstance(next))
				{
					continue;
				}
				
				if (filter == null || filter.accept((E) next))
				{
					nextE = (E) next;
					return;
				}
			}
			
			nextE = null;
		}
		
		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
	
	public static <S, T> Iterable<T> convertingIterable(final Iterable<? extends S> iterable, final Converter<S, T> converter)
	{
		return new ConvertingIterable<>(iterable, converter);
	}
	
	public static <S, T> Iterator<T> convertingIterator(final Iterable<? extends S> iterable, final Converter<S, T> converter)
	{
		return new ConvertingIterator<>(iterable, converter);
	}
	
	public interface Converter<S, T>
	{
		public T convert(S src);
	}
	
	private static final class ConvertingIterable<S, T> implements Iterable<T>
	{
		private final Iterable<? extends S> iterable;
		private final Converter<S, T> converter;
		
		protected ConvertingIterable(final Iterable<? extends S> iterable, final Converter<S, T> converter)
		{
			this.iterable = iterable;
			this.converter = converter;
		}
		
		@Override
		public Iterator<T> iterator()
		{
			return convertingIterator(iterable, converter);
		}
	}
	
	private static final class ConvertingIterator<S, T> implements Iterator<T>
	{
		private final Iterator<? extends S> iterator;
		private final Converter<S, T> converter;
		
		private T nextE;
		
		protected ConvertingIterator(final Iterable<? extends S> iterable, final Converter<S, T> converter)
		{
			iterator = iterable.iterator();
			this.converter = converter;
			
			step();
		}
		
		@Override
		public boolean hasNext()
		{
			return nextE != null;
		}
		
		@Override
		public T next()
		{
			if (!hasNext())
			{
				throw new NoSuchElementException();
			}
			
			final T next = nextE;
			
			step();
			
			return next;
		}
		
		private void step()
		{
			while (iterator.hasNext())
			{
				final S src = iterator.next();
				
				if (src == null)
				{
					continue;
				}
				
				final T next = converter.convert(src);
				
				if (next != null)
				{
					nextE = next;
					return;
				}
			}
			
			nextE = null;
		}
		
		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
	
	public static <T> Iterable<T> concatenatedIterable(final Iterable<? extends T> iterable1, final Iterable<? extends T> iterable2)
	{
		return new ConcatenatedIterable<>(iterable1, iterable2);
	}
	
	public static <T> Iterable<T> concatenatedIterable(final Iterable<? extends T> iterable1, final Iterable<? extends T> iterable2, final Iterable<? extends T> iterable3)
	{
		return new ConcatenatedIterable<>(iterable1, iterable2, iterable3);
	}
	
	public static <T> Iterable<T> concatenatedIterable(final Iterable<? extends T>... iterables)
	{
		return new ConcatenatedIterable<>(iterables);
	}
	
	public static <T> Iterator<T> concatenatedIterator(final Iterable<? extends T> iterable1, final Iterable<? extends T> iterable2)
	{
		return new ConcatenatedIterator<>(iterable1, iterable2);
	}
	
	public static <T> Iterator<T> concatenatedIterator(final Iterable<? extends T> iterable1, final Iterable<? extends T> iterable2, final Iterable<? extends T> iterable3)
	{
		return new ConcatenatedIterator<>(iterable1, iterable2, iterable3);
	}
	
	public static <T> Iterator<T> concatenatedIterator(final Iterable<? extends T>... iterables)
	{
		return new ConcatenatedIterator<>(iterables);
	}
	
	private static final class ConcatenatedIterable<E> implements Iterable<E>
	{
		private final Iterable<? extends E>[] iterables;
		
		protected ConcatenatedIterable(final Iterable<? extends E>... iterables)
		{
			this.iterables = iterables;
		}
		
		@Override
		public Iterator<E> iterator()
		{
			return concatenatedIterator(iterables);
		}
	}
	
	private static final class ConcatenatedIterator<E> implements Iterator<E>
	{
		private final Iterable<? extends E>[] iterables;
		
		private Iterator<? extends E> iterator;
		private int index = -1;
		
		protected ConcatenatedIterator(final Iterable<? extends E>... iterables)
		{
			this.iterables = iterables;
			
			validateIterator();
		}
		
		@Override
		public boolean hasNext()
		{
			validateIterator();
			
			return iterator != null && iterator.hasNext();
		}
		
		@Override
		public E next()
		{
			if (!hasNext())
			{
				throw new NoSuchElementException();
			}
			
			return iterator.next();
		}
		
		private void validateIterator()
		{
			while (iterator == null || !iterator.hasNext())
			{
				index++;
				
				if (index >= iterables.length)
				{
					return;
				}
				
				iterator = iterables[index].iterator();
			}
		}
		
		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static final ObjectPool<ArrayList> ARRAY_LISTS = new ObjectPool<ArrayList>()
	{
		@Override
		protected void reset(final ArrayList list)
		{
			list.clear();
		}
		
		@Override
		protected ArrayList create()
		{
			return new ArrayList();
		}
	};
	
	@SuppressWarnings("rawtypes")
	private static final ObjectPool<L2FastSet> L2_FAST_SETS = new ObjectPool<L2FastSet>()
	{
		@Override
		protected void reset(final L2FastSet list)
		{
			list.clear();
		}
		
		@Override
		protected L2FastSet create()
		{
			return new L2FastSet();
		}
	};
	
	public static <T> ArrayList<T> newArrayList()
	{
		return ARRAY_LISTS.get();
	}
	
	public static void recycle(final ArrayList<?> arrayList)
	{
		ARRAY_LISTS.store(arrayList);
	}
	
	public static <T> L2FastSet<T> newL2FastSet()
	{
		return L2_FAST_SETS.get();
	}
	
	public static void recycle(final L2FastSet<?> l2FastSet)
	{
		L2_FAST_SETS.store(l2FastSet);
	}
}
