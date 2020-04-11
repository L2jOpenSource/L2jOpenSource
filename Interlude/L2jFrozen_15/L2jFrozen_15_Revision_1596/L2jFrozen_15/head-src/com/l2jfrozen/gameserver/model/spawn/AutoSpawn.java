package com.l2jfrozen.gameserver.model.spawn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.datatables.sql.SpawnTable;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.model.Location;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.database.L2DatabaseFactory;
import com.l2jfrozen.util.random.Rnd;

/**
 * Auto Spawn Handler Allows spawning of a NPC object based on a timer. (From the official idea used for the Merchant and Blacksmith of Mammon) General Usage: - Call registerSpawn() with the parameters listed below. int npcId int[][] spawnPoints or specify NULL to add points later. int initialDelay
 * (If < 0 = default value) int respawnDelay (If < 0 = default value) int despawnDelay (If < 0 = default value or if = 0, function disabled) spawnPoints is a standard two-dimensional int array containing X,Y and Z coordinates. The default respawn/despawn delays are currently every hour (as for
 * Mammon on official servers). - The resulting AutoSpawnInstance object represents the newly added spawn index. - The internal methods of this object can be used to adjust random spawning, for instance a call to setRandomSpawn(1, true); would set the spawn at index 1 to be randomly rather than
 * sequentially-based. - Also they can be used to specify the number of NPC instances to spawn using setSpawnCount(), and broadcast a message to all users using setBroadcast(). Random Spawning = OFF by default Broadcasting = OFF by default
 * @author Tempy
 */
public class AutoSpawn
{
	protected static final Logger LOGGER = Logger.getLogger(AutoSpawn.class);
	private static AutoSpawn instance;
	
	private static final String SELECT_RANDOWM_SPAWN = "SELECT groupId,npcId,count,initialDelay,respawnDelay,despawnDelay,broadcastSpawn,randomSpawn FROM random_spawn ORDER BY groupId ASC";
	private static final String SELECT_RANDOWM_SPAWN_LOC_BY_GROUP_ID = "SELECT x,y,z,heading FROM random_spawn_loc WHERE groupId=?";
	
	private static final int DEFAULT_INITIAL_SPAWN = 30000; // 30 seconds after registration
	private static final int DEFAULT_RESPAWN = 3600000; // 1 hour in millisecs
	private static final int DEFAULT_DESPAWN = 3600000; // 1 hour in millisecs
	
	protected Map<Integer, AutoSpawnInstance> registeredSpawns;
	protected Map<Integer, ScheduledFuture<?>> runningSpawns;
	
	protected boolean activeState = true;
	
	private AutoSpawn()
	{
		registeredSpawns = new HashMap<>();
		runningSpawns = new HashMap<>();
		
		restoreSpawnData();
	}
	
	public static AutoSpawn getInstance()
	{
		if (instance == null)
		{
			instance = new AutoSpawn();
		}
		
		return instance;
	}
	
	public int size()
	{
		synchronized (registeredSpawns)
		{
			return registeredSpawns.size();
		}
		
	}
	
	private void restoreSpawnData()
	{
		int numLoaded = 0;
		
		// Restore spawn group data, then the location data.
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_RANDOWM_SPAWN);
			ResultSet rs = statement.executeQuery())
		{
			while (rs.next())
			{
				// Register random spawn group, set various options on the
				// created spawn instance.
				AutoSpawnInstance spawnInst = registerSpawn(rs.getInt("npcId"), rs.getInt("initialDelay"), rs.getInt("respawnDelay"), rs.getInt("despawnDelay"));
				
				spawnInst.setSpawnCount(rs.getInt("count"));
				spawnInst.setBroadcast(rs.getBoolean("broadcastSpawn"));
				spawnInst.setRandomSpawn(rs.getBoolean("randomSpawn"));
				numLoaded++;
				
				// Restore the spawn locations for this spawn group/instance.
				
				try (PreparedStatement statement2 = con.prepareStatement(SELECT_RANDOWM_SPAWN_LOC_BY_GROUP_ID))
				{
					statement2.setInt(1, rs.getInt("groupId"));
					
					try (ResultSet rs2 = statement2.executeQuery())
					{
						// Add each location to the spawn group/instance.
						while (rs2.next())
						{
							spawnInst.addSpawnLocation(rs2.getInt("x"), rs2.getInt("y"), rs2.getInt("z"), rs2.getInt("heading"));
						}
					}
				}
			}
			
			if (Config.DEBUG)
			{
				LOGGER.debug("AutoSpawnHandler: Loaded " + numLoaded + " spawn group(s) from the database.");
			}
		}
		catch (Exception e)
		{
			LOGGER.warn("AutoSpawn.restorer: Could not restore spawn data", e);
		}
	}
	
	/**
	 * Registers a spawn with the given parameters with the spawner, and marks it as active. Returns a AutoSpawnInstance containing info about the spawn.
	 * @param  npcId
	 * @param  spawnPoints
	 * @param  initialDelay (If < 0 = default value)
	 * @param  respawnDelay (If < 0 = default value)
	 * @param  despawnDelay (If < 0 = default value or if = 0, function disabled)
	 * @return              AutoSpawnInstance spawnInst
	 */
	public AutoSpawnInstance registerSpawn(final int npcId, final int[][] spawnPoints, int initialDelay, int respawnDelay, int despawnDelay)
	{
		if (initialDelay < 0)
		{
			initialDelay = DEFAULT_INITIAL_SPAWN;
		}
		
		if (respawnDelay < 0)
		{
			respawnDelay = DEFAULT_RESPAWN;
		}
		
		if (despawnDelay < 0)
		{
			despawnDelay = DEFAULT_DESPAWN;
		}
		
		final AutoSpawnInstance newSpawn = new AutoSpawnInstance(npcId, initialDelay, respawnDelay, despawnDelay);
		
		if (spawnPoints != null)
		{
			for (final int[] spawnPoint : spawnPoints)
			{
				newSpawn.addSpawnLocation(spawnPoint);
			}
		}
		
		final int newId = IdFactory.getInstance().getNextId();
		newSpawn.objectId = newId;
		
		synchronized (registeredSpawns)
		{
			registeredSpawns.put(newId, newSpawn);
		}
		
		setSpawnActive(newSpawn, true);
		
		if (Config.DEBUG)
		{
			LOGGER.debug("AutoSpawnHandler: Registered auto spawn for NPC ID " + npcId + " (Object ID = " + newId + ").");
		}
		
		return newSpawn;
	}
	
	/**
	 * Registers a spawn with the given parameters with the spawner, and marks it as active. Returns a AutoSpawnInstance containing info about the spawn. <BR>
	 * <B>Warning:</B> Spawn locations must be specified separately using addSpawnLocation().
	 * @param  npcId
	 * @param  initialDelay (If < 0 = default value)
	 * @param  respawnDelay (If < 0 = default value)
	 * @param  despawnDelay (If < 0 = default value or if = 0, function disabled)
	 * @return              spawnInst
	 */
	public AutoSpawnInstance registerSpawn(final int npcId, final int initialDelay, final int respawnDelay, final int despawnDelay)
	{
		return registerSpawn(npcId, null, initialDelay, respawnDelay, despawnDelay);
	}
	
	/**
	 * Remove a registered spawn from the list, specified by the given spawn instance.
	 * @param  spawnInst
	 * @return           removedSuccessfully
	 */
	public boolean removeSpawn(final AutoSpawnInstance spawnInst)
	{
		synchronized (registeredSpawns)
		{
			
			if (!registeredSpawns.containsValue(spawnInst))
			{
				return false;
			}
			
			// Try to remove from the list of registered spawns if it exists.
			registeredSpawns.remove(spawnInst.getNpcId());
			
			synchronized (runningSpawns)
			{
				
				// Cancel the currently associated running scheduled task.
				final ScheduledFuture<?> respawnTask = runningSpawns.remove(spawnInst.objectId);
				
				try
				{
					respawnTask.cancel(false);
					
				}
				catch (final Exception e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
					
					LOGGER.warn("AutoSpawnHandler: Could not auto spawn for NPC ID " + spawnInst.npcId + " (Object ID = " + spawnInst.objectId + "): " + e);
					
					return false;
				}
				
			}
			
			if (Config.DEBUG)
			{
				LOGGER.debug("AutoSpawnHandler: Removed auto spawn for NPC ID " + spawnInst.npcId + " (Object ID = " + spawnInst.objectId + ").");
			}
			
		}
		
		return true;
	}
	
	/**
	 * Remove a registered spawn from the list, specified by the given spawn object ID.
	 * @param objectId
	 */
	public void removeSpawn(final int objectId)
	{
		AutoSpawnInstance spawn_inst = null;
		
		synchronized (registeredSpawns)
		{
			spawn_inst = registeredSpawns.get(objectId);
		}
		
		removeSpawn(spawn_inst);
		
	}
	
	/**
	 * Sets the active state of the specified spawn.
	 * @param spawnInst
	 * @param isActive
	 */
	public void setSpawnActive(final AutoSpawnInstance spawnInst, final boolean isActive)
	{
		if (spawnInst == null)
		{
			return;
		}
		
		final int objectId = spawnInst.objectId;
		
		if (isSpawnRegistered(objectId))
		{
			ScheduledFuture<?> spawnTask = null;
			
			if (isActive)
			{
				AutoSpawner rs = new AutoSpawner(objectId);
				
				if (spawnInst.desDelay > 0)
				{
					spawnTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(rs, spawnInst.initDelay, spawnInst.resDelay);
				}
				else
				{
					spawnTask = ThreadPoolManager.getInstance().scheduleEffect(rs, spawnInst.initDelay);
				}
				
				synchronized (runningSpawns)
				{
					runningSpawns.put(objectId, spawnTask);
				}
				
				rs = null;
			}
			else
			{
				AutoDespawner rd = new AutoDespawner(objectId);
				
				synchronized (runningSpawns)
				{
					spawnTask = runningSpawns.remove(objectId);
				}
				
				if (spawnTask != null)
				{
					spawnTask.cancel(false);
				}
				
				ThreadPoolManager.getInstance().scheduleEffect(rd, 0);
				rd = null;
			}
			
			spawnInst.setSpawnActive(isActive);
			
			spawnTask = null;
		}
	}
	
	/**
	 * Sets the active state of all auto spawn instances to that specified, and cancels the scheduled spawn task if necessary.
	 * @param isActive
	 */
	public void setAllActive(final boolean isActive)
	{
		if (activeState == isActive)
		{
			return;
		}
		
		Collection<AutoSpawnInstance> instances;
		synchronized (registeredSpawns)
		{
			instances = registeredSpawns.values();
		}
		
		for (final AutoSpawnInstance spawnInst : instances)
		{
			setSpawnActive(spawnInst, isActive);
		}
		
		activeState = isActive;
	}
	
	/**
	 * Returns the number of milliseconds until the next occurrence of the given spawn.
	 * @param  spawnInst
	 * @return
	 */
	public final long getTimeToNextSpawn(final AutoSpawnInstance spawnInst)
	{
		if (spawnInst == null)
		{
			return -1;
		}
		final int objectId = spawnInst.getObjectId();
		
		synchronized (runningSpawns)
		{
			
			final ScheduledFuture<?> future_task = runningSpawns.get(objectId);
			if (future_task != null)
			{
				return future_task.getDelay(TimeUnit.MILLISECONDS);
			}
		}
		
		return -1;
	}
	
	/**
	 * Attempts to return the AutoSpawnInstance associated with the given NPC or Object ID type. <BR>
	 * Note: If isObjectId == false, returns first instance for the specified NPC ID.
	 * @param  id
	 * @param  isObjectId
	 * @return            AutoSpawnInstance spawnInst
	 */
	public final AutoSpawnInstance getAutoSpawnInstance(final int id, final boolean isObjectId)
	{
		if (isObjectId)
		{
			return registeredSpawns.get(id);
		}
		
		Collection<AutoSpawnInstance> instances;
		synchronized (registeredSpawns)
		{
			instances = registeredSpawns.values();
		}
		
		for (final AutoSpawnInstance spawnInst : instances)
		{
			if (spawnInst.getNpcId() == id)
			{
				return spawnInst;
			}
		}
		return null;
	}
	
	public Map<Integer, AutoSpawnInstance> getAutoSpawnInstances(final int npcId)
	{
		final Map<Integer, AutoSpawnInstance> spawnInstList = new HashMap<>();
		
		Collection<AutoSpawnInstance> instances;
		synchronized (registeredSpawns)
		{
			instances = registeredSpawns.values();
		}
		
		for (final AutoSpawnInstance spawnInst : instances)
		{
			if (spawnInst.getNpcId() == npcId)
			{
				spawnInstList.put(spawnInst.getObjectId(), spawnInst);
			}
		}
		
		return spawnInstList;
	}
	
	/**
	 * Tests if the specified object ID is assigned to an auto spawn.
	 * @param  objectId
	 * @return          boolean isAssigned
	 */
	public final boolean isSpawnRegistered(final int objectId)
	{
		synchronized (registeredSpawns)
		{
			return registeredSpawns.containsKey(objectId);
		}
		
	}
	
	/**
	 * Tests if the specified spawn instance is assigned to an auto spawn.
	 * @param  spawnInst
	 * @return           boolean isAssigned
	 */
	public boolean isSpawnRegistered(final AutoSpawnInstance spawnInst)
	{
		synchronized (registeredSpawns)
		{
			return registeredSpawns.containsValue(spawnInst);
		}
		
	}
	
	/**
	 * AutoSpawner Class <BR>
	 * <BR>
	 * This handles the main spawn task for an auto spawn instance, and initializes a despawner if required.
	 * @author Tempy
	 */
	private class AutoSpawner implements Runnable
	{
		private final int objectId;
		
		protected AutoSpawner(final int objectId)
		{
			this.objectId = objectId;
		}
		
		@Override
		public void run()
		{
			try
			{
				AutoSpawnInstance spawnInst = null;
				
				synchronized (registeredSpawns)
				{
					// Retrieve the required spawn instance for this spawn task.
					spawnInst = registeredSpawns.get(objectId);
					
				}
				
				// If the spawn is not scheduled to be active, cancel the spawn
				// task.
				if (!spawnInst.isSpawnActive())
				{
					return;
				}
				
				Location[] locationList = spawnInst.getLocationList();
				
				// If there are no set co-ordinates, cancel the spawn task.
				if (locationList.length == 0)
				{
					LOGGER.info("AutoSpawnHandler: No location co-ords specified for spawn instance (Object ID = " + objectId + ").");
					return;
				}
				
				final int locationCount = locationList.length;
				int locationIndex = Rnd.nextInt(locationCount);
				
				/*
				 * If random spawning is disabled, the spawn at the next set of co-ordinates after the last. If the index is greater than the number of possible spawns, reset the counter to zero.
				 */
				if (!spawnInst.isRandomSpawn())
				{
					locationIndex = spawnInst.lastLocIndex;
					locationIndex++;
					
					if (locationIndex == locationCount)
					{
						locationIndex = 0;
					}
					
					spawnInst.lastLocIndex = locationIndex;
				}
				
				// Set the X, Y and Z co-ordinates, where this spawn will take
				// place.
				final int x = locationList[locationIndex].getX();
				final int y = locationList[locationIndex].getY();
				final int z = locationList[locationIndex].getZ();
				final int heading = locationList[locationIndex].getHeading();
				
				// Fetch the template for this NPC ID and create a new spawn.
				L2NpcTemplate npcTemp = NpcTable.getInstance().getTemplate(spawnInst.getNpcId());
				
				if (npcTemp == null)
				{
					LOGGER.warn("Couldnt find NPC id" + spawnInst.getNpcId() + " Try to update your DP");
					return;
				}
				
				L2Spawn newSpawn = new L2Spawn(npcTemp);
				
				newSpawn.setLocx(x);
				newSpawn.setLocy(y);
				newSpawn.setLocz(z);
				
				if (heading != -1)
				{
					newSpawn.setHeading(heading);
				}
				
				newSpawn.setAmount(spawnInst.getSpawnCount());
				
				if (spawnInst.desDelay == 0)
				{
					newSpawn.setRespawnDelay(spawnInst.resDelay);
				}
				
				// Add the new spawn information to the spawn table, but do not
				// store it.
				SpawnTable.getInstance().addNewSpawn(newSpawn, false);
				L2NpcInstance npcInst = null;
				
				if (spawnInst.spawnCount == 1)
				{
					npcInst = newSpawn.doSpawn();
					npcInst.setXYZ(npcInst.getX(), npcInst.getY(), npcInst.getZ());
					spawnInst.addNpcInstance(npcInst);
				}
				else
				{
					for (int i = 0; i < spawnInst.spawnCount; i++)
					{
						npcInst = newSpawn.doSpawn();
						
						// To prevent spawning of more than one NPC in the exact
						// same spot,
						// move it slightly by a small random offset.
						npcInst.setXYZ(npcInst.getX() + Rnd.nextInt(50), npcInst.getY() + Rnd.nextInt(50), npcInst.getZ());
						
						// Add the NPC instance to the list of managed
						// instances.
						spawnInst.addNpcInstance(npcInst);
					}
				}
				
				String nearestTown = MapRegionTable.getInstance().getClosestTownName(npcInst);
				
				// Announce to all players that the spawn has taken place, with
				// the nearest town location.
				if (spawnInst.isBroadcasting() && (npcInst != null))
				{
					Announcements.getInstance().announceToAll("The " + npcInst.getName() + " has spawned near " + nearestTown + "!");
				}
				
				if (Config.DEBUG)
				{
					LOGGER.info("AutoSpawnHandler: Spawned NPC ID " + spawnInst.getNpcId() + " at " + x + ", " + y + ", " + z + " (Near " + nearestTown + ") for " + spawnInst.getRespawnDelay() / 60000 + " minute(s).");
				}
				
				// If there is no despawn time, do not create a despawn task.
				if (spawnInst.getDespawnDelay() > 0)
				{
					AutoDespawner rd = new AutoDespawner(objectId);
					ThreadPoolManager.getInstance().scheduleAi(rd, spawnInst.getDespawnDelay() - 1000);
					rd = null;
				}
				
				nearestTown = null;
				spawnInst = null;
				npcInst = null;
				newSpawn = null;
				npcTemp = null;
				locationList = null;
			}
			catch (final Exception e)
			{
				LOGGER.warn("AutoSpawnHandler: An error occurred while initializing spawn instance (Object ID = " + objectId + "): " + e);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * AutoDespawner Class <BR>
	 * <BR>
	 * Simply used as a secondary class for despawning an auto spawn instance.
	 * @author Tempy
	 */
	private class AutoDespawner implements Runnable
	{
		private final int objectId;
		
		protected AutoDespawner(final int objectId)
		{
			this.objectId = objectId;
		}
		
		@Override
		public void run()
		{
			try
			{
				AutoSpawnInstance spawnInst = null;
				synchronized (registeredSpawns)
				{
					spawnInst = registeredSpawns.get(objectId);
				}
				
				if (spawnInst == null)
				{
					LOGGER.info("AutoSpawnHandler: No spawn registered for object ID = " + objectId + ".");
					return;
				}
				
				final L2NpcInstance[] npc_instances = spawnInst.getNPCInstanceList();
				if (npc_instances == null)
				{
					LOGGER.info("AutoSpawnHandler: No spawn registered");
					return;
				}
				
				for (final L2NpcInstance npcInst : npc_instances)
				{
					if (npcInst == null)
					{
						continue;
					}
					
					npcInst.deleteMe();
					spawnInst.removeNpcInstance(npcInst);
					
					if (Config.DEBUG)
					{
						LOGGER.info("AutoSpawnHandler: Spawns removed for spawn instance (Object ID = " + objectId + ").");
					}
				}
				
				spawnInst = null;
			}
			catch (final Exception e)
			{
				// if(Config.ENABLE_ALL_EXCEPTIONS)
				// e.printStackTrace();
				
				LOGGER.warn("AutoSpawnHandler: An error occurred while despawning spawn (Object ID = " + objectId + "): " + e);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * AutoSpawnInstance Class <BR>
	 * <BR>
	 * Stores information about a registered auto spawn.
	 * @author Tempy
	 */
	public class AutoSpawnInstance
	{
		protected int objectId;
		
		protected int spawnIndex;
		
		protected int npcId;
		
		protected int initDelay;
		
		protected int resDelay;
		
		protected int desDelay;
		
		protected int spawnCount = 1;
		
		protected int lastLocIndex = -1;
		
		private final List<L2NpcInstance> npcList = new ArrayList<>();
		
		private final List<Location> locList = new ArrayList<>();
		
		private boolean spawnActive;
		
		private boolean randomSpawn = false;
		
		private boolean broadcastAnnouncement = false;
		
		protected AutoSpawnInstance(final int npcId, final int initDelay, final int respawnDelay, final int despawnDelay)
		{
			this.npcId = npcId;
			this.initDelay = initDelay;
			resDelay = respawnDelay;
			desDelay = despawnDelay;
		}
		
		protected void setSpawnActive(final boolean activeValue)
		{
			spawnActive = activeValue;
		}
		
		protected boolean addNpcInstance(final L2NpcInstance npcInst)
		{
			return npcList.add(npcInst);
		}
		
		protected boolean removeNpcInstance(final L2NpcInstance npcInst)
		{
			return npcList.remove(npcInst);
		}
		
		public int getObjectId()
		{
			return objectId;
		}
		
		public int getInitialDelay()
		{
			return initDelay;
		}
		
		public int getRespawnDelay()
		{
			return resDelay;
		}
		
		public int getDespawnDelay()
		{
			return desDelay;
		}
		
		public int getNpcId()
		{
			return npcId;
		}
		
		public int getSpawnCount()
		{
			return spawnCount;
		}
		
		public Location[] getLocationList()
		{
			return locList.toArray(new Location[locList.size()]);
		}
		
		public L2NpcInstance[] getNPCInstanceList()
		{
			L2NpcInstance[] ret;
			
			synchronized (npcList)
			{
				ret = new L2NpcInstance[npcList.size()];
				npcList.toArray(ret);
			}
			
			return ret;
		}
		
		public L2Spawn[] getSpawns()
		{
			final List<L2Spawn> npcSpawns = new ArrayList<>();
			
			for (final L2NpcInstance npcInst : npcList)
			{
				npcSpawns.add(npcInst.getSpawn());
			}
			
			return npcSpawns.toArray(new L2Spawn[npcSpawns.size()]);
		}
		
		public void setSpawnCount(final int spawnCount)
		{
			this.spawnCount = spawnCount;
		}
		
		public void setRandomSpawn(final boolean randValue)
		{
			randomSpawn = randValue;
		}
		
		public void setBroadcast(final boolean broadcastValue)
		{
			broadcastAnnouncement = broadcastValue;
		}
		
		public boolean isSpawnActive()
		{
			return spawnActive;
		}
		
		public boolean isRandomSpawn()
		{
			return randomSpawn;
		}
		
		public boolean isBroadcasting()
		{
			return broadcastAnnouncement;
		}
		
		public boolean addSpawnLocation(final int x, final int y, final int z, final int heading)
		{
			return locList.add(new Location(x, y, z, heading));
		}
		
		public boolean addSpawnLocation(final int[] spawnLoc)
		{
			if (spawnLoc.length != 3)
			{
				return false;
			}
			
			return addSpawnLocation(spawnLoc[0], spawnLoc[1], spawnLoc[2], -1);
		}
		
		public Location removeSpawnLocation(final int locIndex)
		{
			try
			{
				return locList.remove(locIndex);
			}
			catch (final IndexOutOfBoundsException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				return null;
			}
		}
	}
}
