package com.l2jfrozen.gameserver.cache;

import javolution.context.ObjectFactory;
import javolution.lang.Reusable;
import javolution.util.FastCollection;
import javolution.util.FastComparator;
import javolution.util.FastList;
import javolution.util.FastMap;

/**
 * @author Layane
 * @param  <K>
 * @param  <V>
 */
@SuppressWarnings("rawtypes")
public class FastMRUCache<K, V> extends FastCollection implements Reusable
{
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_CAPACITY = 50;
	private static final int DEFAULT_FORGET_TIME = 300000; // 5 Minutes
	
	private final FastMap<K, CacheNode> cache = new FastMap<K, CacheNode>().setKeyComparator(FastComparator.DIRECT);
	private FastMap<K, V> fMap;
	private final FastList<K> mruList = new FastList<>();
	private int cacheSize;
	private int forgetTime;
	
	class CacheNode
	{
		long lastModified;
		V node;
		
		public CacheNode(final V object)
		{
			lastModified = System.currentTimeMillis();
			node = object;
		}
		
		@Override
		public boolean equals(final Object object)
		{
			return node == object;
		}
	}
	
	/**
	 * Holds the set factory.
	 */
	private static final ObjectFactory FACTORY = new ObjectFactory()
	{
		
		@Override
		public Object create()
		{
			return new FastMRUCache();
		}
		
		@Override
		public void cleanup(final Object obj)
		{
			((FastMRUCache) obj).reset();
		}
	};
	
	/**
	 * Returns a set allocated from the stack when executing in a PoolContext.
	 * @return a new, pre-allocated or recycled set instance.
	 */
	public static FastMRUCache newInstance()
	{
		return (FastMRUCache) FACTORY.object();
	}
	
	public FastMRUCache()
	{
		this(new FastMap<K, V>(), DEFAULT_CAPACITY, DEFAULT_FORGET_TIME);
	}
	
	public FastMRUCache(final FastMap<K, V> map)
	{
		this(map, DEFAULT_CAPACITY, DEFAULT_FORGET_TIME);
	}
	
	public FastMRUCache(final FastMap<K, V> map, final int max)
	{
		this(map, max, DEFAULT_FORGET_TIME);
	}
	
	public FastMRUCache(final FastMap<K, V> map, final int max, final int forgetTime)
	{
		fMap = map;
		cacheSize = max;
		this.forgetTime = forgetTime;
		fMap.setKeyComparator(FastComparator.DIRECT);
	}
	
	// Implements Reusable.
	@Override
	public synchronized void reset()
	{
		fMap.reset();
		cache.reset();
		mruList.reset();
		fMap.setKeyComparator(FastComparator.DIRECT);
		cache.setKeyComparator(FastComparator.DIRECT);
	}
	
	public synchronized V get(final K key)
	{
		V result;
		
		if (!cache.containsKey(key))
		{
			if (mruList.size() >= cacheSize)
			{
				
				cache.remove(mruList.getLast());
				mruList.removeLast();
			}
			
			result = fMap.get(key);
			
			cache.put(key, new CacheNode(result));
			mruList.addFirst(key);
		}
		else
		{
			final CacheNode current = cache.get(key);
			
			if (current.lastModified + forgetTime <= System.currentTimeMillis())
			{
				current.lastModified = System.currentTimeMillis();
				current.node = fMap.get(key);
				cache.put(key, current);
			}
			
			mruList.remove(key);
			mruList.addFirst(key);
			
			result = current.node;
		}
		
		return result;
	}
	
	@Override
	public synchronized boolean remove(final Object key)
	{
		cache.remove(key);
		mruList.remove(key);
		return fMap.remove(key) == key;
	}
	
	public FastMap<K, V> getContentMap()
	{
		return fMap;
	}
	
	@Override
	public int size()
	{
		return mruList.size();
	}
	
	public int capacity()
	{
		return cacheSize;
	}
	
	public int getForgetTime()
	{
		return forgetTime;
	}
	
	@Override
	public synchronized void clear()
	{
		cache.clear();
		mruList.clear();
		fMap.clear();
	}
	
	// Implements FastCollection abstract method.
	@Override
	public final Record head()
	{
		return mruList.head();
	}
	
	@Override
	public final Record tail()
	{
		return mruList.tail();
	}
	
	@Override
	public final Object valueOf(final Record record)
	{
		return ((FastMap.Entry) record).getKey();
	}
	
	@Override
	public final void delete(final Record record)
	{
		remove(((FastMap.Entry) record).getKey());
	}
}
