package main.data.memory;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import main.concurrent.ThreadPool;
import main.holders.WorldHolder;
import main.holders.objects.ObjectHolder;
import main.holders.objects.PlayerHolder;

/**
 * @author fissban
 */
public class WorldData
{
	// Delay for destroy empty worlds.
	private static final int DELAY_AUTO_PURGE = 10 * 60 * 1000;// 10 min
	// All worlds are created.
	private static volatile Map<Integer, WorldHolder> worlds = new LinkedHashMap<>();
	
	public WorldData()
	{
		//
	}
	
	public static void init()
	{
		autoPurgeEmptyWorld();
	}
	
	/**
	 * Create new world
	 * @param  id
	 * @return
	 */
	public static synchronized WorldHolder create(int id, boolean removeIfEmpty)
	{
		if (!worlds.containsKey(id))
		{
			// create new World
			WorldHolder world = new WorldHolder(id, removeIfEmpty);
			// Safe in memory
			worlds.put(id, world);
			return world;
		}
		
		return worlds.get(id);
	}
	
	public static synchronized void destroy(int id)
	{
		if (worlds.containsKey(id))
		{
			// search all Object
			worlds.get(id).getAll(ObjectHolder.class).forEach(i ->
			{
				if (i instanceof PlayerHolder)
				{
					// set world id == 0
					i.setWorldId(0);
				}
				else
				{
					// remove object from world
					i.getInstance().decayMe();
				}
			});
			
			worlds.remove(id);
		}
	}
	
	/**
	 * Get a world according to your id
	 * @param  id
	 * @return
	 */
	public static synchronized WorldHolder get(int id)
	{
		return worlds.get(id);
	}
	
	/**
	 * Get all worlds
	 * @return
	 */
	public static synchronized Collection<WorldHolder> getAll()
	{
		return worlds.values();
	}
	
	/**
	 * Check if world exist.
	 * @param  id
	 * @return
	 */
	public static boolean existWorld(int id)
	{
		return worlds.containsKey(id);
	}
	
	// XXX PURGE -----------------------------------------------------------------------------------------------------
	
	/**
	 * Purge empty worlds.
	 */
	private static synchronized void autoPurgeEmptyWorld()
	{
		ThreadPool.scheduleAtFixedRate(() ->
		{
			Iterator<WorldHolder> it = worlds.values().iterator();
			
			while (it.hasNext())
			{
				WorldHolder world = it.next();
				
				if (world.isRemoveIfEmpty() && world.getAll(PlayerHolder.class).isEmpty())
				{
					// search all objects
					world.getAll(ObjectHolder.class).forEach(i -> i.getInstance().decayMe());
					
					// remove from map
					it.remove();
				}
			}
			
		}, DELAY_AUTO_PURGE, DELAY_AUTO_PURGE);
	}
}
