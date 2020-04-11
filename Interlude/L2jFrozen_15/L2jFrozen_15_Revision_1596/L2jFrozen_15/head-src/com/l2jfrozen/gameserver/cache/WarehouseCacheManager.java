package com.l2jfrozen.gameserver.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * @author -Nemesiss-
 */
public class WarehouseCacheManager
{
	private static WarehouseCacheManager instance;
	protected final Map<L2PcInstance, Long> cachedWh;
	protected final long cacheTime;
	
	public static WarehouseCacheManager getInstance()
	{
		if (instance == null)
		{
			instance = new WarehouseCacheManager();
		}
		
		return instance;
	}
	
	private WarehouseCacheManager()
	{
		cacheTime = Config.WAREHOUSE_CACHE_TIME * 60000L; // 60*1000 = 60000
		cachedWh = new ConcurrentHashMap<>();
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new CacheScheduler(), 120000, 60000);
	}
	
	public void addCacheTask(final L2PcInstance pc)
	{
		cachedWh.put(pc, System.currentTimeMillis());
	}
	
	public void remCacheTask(final L2PcInstance pc)
	{
		cachedWh.remove(pc);
	}
	
	public class CacheScheduler implements Runnable
	{
		@Override
		public void run()
		{
			final long cTime = System.currentTimeMillis();
			for (final L2PcInstance pc : cachedWh.keySet())
			{
				if (cTime - cachedWh.get(pc) > cacheTime)
				{
					pc.clearWarehouse();
					cachedWh.remove(pc);
				}
			}
		}
	}
}
