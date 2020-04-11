package com.l2jfrozen.gameserver.model.spawn;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.TerritoryTable;
import com.l2jfrozen.gameserver.geo.GeoData;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.random.Rnd;

import main.EngineModsManager;

/**
 * This class manages the spawn and respawn of a group of L2NpcInstance that are in the same are and have the same type. <B><U> Concept</U> :</B><BR>
 * <BR>
 * L2NpcInstance can be spawned either in a random position into a location area (if Lox=0 and Locy=0), either at an exact position. The heading of the L2NpcInstance can be a random heading if not defined (value= -1) or an exact heading (ex : merchant...).<BR>
 * <BR>
 * @author  Nightmare
 * @version $Revision: 1.9.2.3.2.8 $ $Date: 2005/03/27 15:29:32 $
 */
public class L2Spawn
{
	protected static final Logger LOGGER = Logger.getLogger(L2Spawn.class);
	
	/**
	 * The link on the L2NpcTemplate object containing generic and static properties of this spawn (ex : RewardExp, RewardSP, AggroRange...)
	 */
	private L2NpcTemplate template;
	
	/** The Identifier of this spawn in the spawn table */
	private int id;
	
	/** The identifier of the location area where L2NpcInstance can be spwaned */
	private int location;
	
	/** The maximum number of L2NpcInstance that can manage this L2Spawn */
	private int maximumCount;
	
	/** The current number of L2NpcInstance managed by this L2Spawn */
	private int currentCount;
	
	/** The current number of SpawnTask in progress or stand by of this L2Spawn */
	protected int scheduledCount;
	
	/** The X position of the spwan point */
	private int locX;
	
	/** The Y position of the spwan point */
	private int locY;
	
	/** The Z position of the spwan point */
	private int locZ;
	
	/** The heading of L2NpcInstance when they are spawned */
	private int heading;
	
	/** The delay between a L2NpcInstance remove and its re-spawn */
	private int respawnDelay;
	
	/** Minimum delay RaidBoss */
	private int respawnMinDelay;
	
	/** Maximum delay RaidBoss */
	private int respawnMaxDelay;
	
	/** The generic constructor of L2NpcInstance managed by this L2Spawn */
	private Constructor<?> constructor;
	
	/** If True a L2NpcInstance is respawned each time that another is killed */
	private boolean doRespawn;
	
	private int instanceId = 0;
	
	private L2NpcInstance lastSpawn;
	private static List<SpawnListener> spawnListeners = new ArrayList<>();
	
	/** The task launching the function doSpawn() */
	class SpawnTask implements Runnable
	{
		private final L2NpcInstance oldNpc;
		
		public SpawnTask(final L2NpcInstance pOldNpc)
		{
			oldNpc = pOldNpc;
		}
		
		@Override
		public void run()
		{
			try
			{
				// doSpawn();
				respawnNpc(oldNpc);
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				LOGGER.warn("", e);
			}
			
			scheduledCount--;
		}
	}
	
	/**
	 * Constructor of L2Spawn.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * Each L2Spawn owns generic and static properties (ex : RewardExp, RewardSP, AggroRange...). All of those properties are stored in a different L2NpcTemplate for each type of L2Spawn. Each template is loaded once in the server cache memory (reduce memory use). When a new instance of L2Spawn is
	 * created, server just create a link between the instance and the template. This link is stored in <B>_template</B><BR>
	 * <BR>
	 * Each L2NpcInstance is linked to a L2Spawn that manages its spawn and respawn (delay, location...). This link is stored in <B>_spawn</B> of the L2NpcInstance<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the template of the L2Spawn</li>
	 * <li>Calculate the implementationName used to generate the generic constructor of L2NpcInstance managed by this L2Spawn</li>
	 * <li>Create the generic constructor of L2NpcInstance managed by this L2Spawn</li><BR>
	 * <BR>
	 * @param  mobTemplate            The L2NpcTemplate to link to this L2Spawn
	 * @throws SecurityException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 */
	public L2Spawn(final L2NpcTemplate mobTemplate) throws SecurityException, ClassNotFoundException, NoSuchMethodException
	{
		template = mobTemplate;
		
		if (template == null)
		{
			return;
		}
		
		// The Name of the L2NpcInstance type managed by this L2Spawn
		String implementationName = template.type; // implementing class name
		
		if (mobTemplate.npcId == 30995)
		{
			implementationName = "L2RaceManager";
		}
		
		// if (mobTemplate.npcId == 8050)
		
		if (mobTemplate.npcId >= 31046 && mobTemplate.npcId <= 31053)
		{
			implementationName = "L2SymbolMaker";
		}
		
		// Create the generic constructor of L2NpcInstance managed by this L2Spawn
		final Class<?>[] parameters =
		{
			int.class,
			L2NpcTemplate.class
		};
		constructor = Class.forName("com.l2jfrozen.gameserver.model.actor.instance." + implementationName + "Instance").getConstructor(parameters);
		implementationName = null;
	}
	
	/**
	 * @return the maximum number of L2NpcInstance that this L2Spawn can manage.
	 */
	public int getAmount()
	{
		return maximumCount;
	}
	
	/**
	 * @return the Identifier of this L2Spwan (used as key in the SpawnTable).
	 */
	public int getId()
	{
		return id;
	}
	
	/**
	 * @return the Identifier of the location area where L2NpcInstance can be spwaned.
	 */
	public int getLocation()
	{
		return location;
	}
	
	/**
	 * @return the X position of the spwan point.
	 */
	public int getLocx()
	{
		return locX;
	}
	
	/**
	 * @return the Y position of the spwan point.
	 */
	public int getLocy()
	{
		return locY;
	}
	
	/**
	 * @return the Z position of the spwan point.
	 */
	public int getLocz()
	{
		return locZ;
	}
	
	/**
	 * @return the Identifier of the L2NpcInstance manage by this L2Spwan contained in the L2NpcTemplate.
	 */
	public int getNpcid()
	{
		if (template == null)
		{
			return -1;
		}
		
		return template.npcId;
	}
	
	/**
	 * @return the heading of L2NpcInstance when they are spawned.
	 */
	public int getHeading()
	{
		return heading;
	}
	
	/**
	 * @return the delay between a L2NpcInstance remove and its re-spawn.
	 */
	public int getRespawnDelay()
	{
		return respawnDelay;
	}
	
	/**
	 * @return Min RaidBoss Spawn delay.
	 */
	public int getRespawnMinDelay()
	{
		return respawnMinDelay;
	}
	
	/**
	 * @return Max RaidBoss Spawn delay.
	 */
	public int getRespawnMaxDelay()
	{
		return respawnMaxDelay;
	}
	
	/**
	 * Set the maximum number of L2NpcInstance that this L2Spawn can manage.
	 * @param amount
	 */
	public void setAmount(final int amount)
	{
		maximumCount = amount;
	}
	
	/**
	 * Set the Identifier of this L2Spwan (used as key in the SpawnTable).
	 * @param id
	 */
	public void setId(final int id)
	{
		this.id = id;
	}
	
	/**
	 * Set the Identifier of the location area where L2NpcInstance can be spwaned.
	 * @param location
	 */
	public void setLocation(final int location)
	{
		this.location = location;
	}
	
	/**
	 * Set Minimum Respawn Delay.
	 * @param date
	 */
	public void setRespawnMinDelay(final int date)
	{
		respawnMinDelay = date;
	}
	
	/**
	 * Set Maximum Respawn Delay.
	 * @param date
	 */
	public void setRespawnMaxDelay(final int date)
	{
		respawnMaxDelay = date;
	}
	
	/**
	 * Set the X position of the spwan point.
	 * @param locx
	 */
	public void setLocx(final int locx)
	{
		locX = locx;
	}
	
	/**
	 * Set the Y position of the spwan point.
	 * @param locy
	 */
	public void setLocy(final int locy)
	{
		locY = locy;
	}
	
	/**
	 * Set the Z position of the spwan point.
	 * @param locz
	 */
	public void setLocz(final int locz)
	{
		locZ = locz;
	}
	
	/**
	 * Set the heading of L2NpcInstance when they are spawned.
	 * @param heading
	 */
	public void setHeading(final int heading)
	{
		this.heading = heading;
	}
	
	private boolean customSpawn;
	private boolean customRaidBoss = false;
	
	public void setIsCustomSpawn(boolean custom)
	{
		customSpawn = custom;
	}
	
	public boolean isCustomSpawn()
	{
		return customSpawn;
	}
	
	public boolean isCustomRaidBoss()
	{
		return customRaidBoss;
	}
	
	public void setIsCustomRaidBoss(boolean customRaidBoss)
	{
		this.customRaidBoss = customRaidBoss;
	}
	
	/**
	 * Decrease the current number of L2NpcInstance of this L2Spawn and if necessary create a SpawnTask to launch after the respawn Delay.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Decrease the current number of L2NpcInstance of this L2Spawn</li>
	 * <li>Check if respawn is possible to prevent multiple respawning caused by lag</li>
	 * <li>Update the current number of SpawnTask in progress or stand by of this L2Spawn</li>
	 * <li>Create a new SpawnTask to launch after the respawn Delay</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : A respawn is possible ONLY if doRespawn=True and scheduledCount + currentCount < maximumCount</B></FONT><BR>
	 * <BR>
	 * @param oldNpc
	 */
	public void decreaseCount(/* int npcId */final L2NpcInstance oldNpc)
	{
		// Decrease the current number of L2NpcInstance of this L2Spawn
		currentCount--;
		
		// Check if respawn is possible to prevent multiple respawning caused by lag
		if (doRespawn && scheduledCount + currentCount < maximumCount)
		{
			// Update the current number of SpawnTask in progress or stand by of this L2Spawn
			scheduledCount++;
			
			// Create a new SpawnTask to launch after the respawn Delay
			ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(oldNpc), respawnDelay);
		}
	}
	
	/**
	 * Create the initial spawning and set doRespawn to True.<BR>
	 * <BR>
	 * @return The number of L2NpcInstance that were spawned
	 */
	public int init()
	{
		while (currentCount < maximumCount)
		{
			doSpawn();
		}
		doRespawn = true;
		
		return currentCount;
	}
	
	/**
	 * Create a L2NpcInstance in this L2Spawn.
	 * @return
	 */
	public L2NpcInstance spawnOne()
	{
		return doSpawn();
	}
	
	public void stopRespawn()
	{
		doRespawn = false;
	}
	
	public void startRespawn()
	{
		doRespawn = true;
	}
	
	/**
	 * Create the L2NpcInstance, add it to the world and launch its OnSpawn action.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * L2NpcInstance can be spawned either in a random position into a location area (if Lox=0 and Locy=0), either at an exact position. The heading of the L2NpcInstance can be a random heading if not defined (value= -1) or an exact heading (ex : merchant...).<BR>
	 * <BR>
	 * <B><U> Actions for an random spawn into location area</U> : <I>(if Locx=0 and Locy=0)</I></B><BR>
	 * <BR>
	 * <li>Get L2NpcInstance Init parameters and its generate an Identifier</li>
	 * <li>Call the constructor of the L2NpcInstance</li>
	 * <li>Calculate the random position in the location area (if Locx=0 and Locy=0) or get its exact position from the L2Spawn</li>
	 * <li>Set the position of the L2NpcInstance</li>
	 * <li>Set the HP and MP of the L2NpcInstance to the max</li>
	 * <li>Set the heading of the L2NpcInstance (random heading if not defined : value=-1)</li>
	 * <li>Link the L2NpcInstance to this L2Spawn</li>
	 * <li>Init other values of the L2NpcInstance (ex : from its L2CharTemplate for INT, STR, DEX...) and add it in the world</li>
	 * <li>Lauch the action OnSpawn fo the L2NpcInstance</li><BR>
	 * <BR>
	 * <li>Increase the current number of L2NpcInstance managed by this L2Spawn</li><BR>
	 * <BR>
	 * @return
	 */
	public L2NpcInstance doSpawn()
	{
		L2NpcInstance mob = null;
		try
		{
			// Check if the L2Spawn is not a L2Pet or L2Minion spawn
			if (template.type.equalsIgnoreCase("L2Pet") || template.type.equalsIgnoreCase("L2Minion"))
			{
				currentCount++;
				
				return mob;
			}
			
			// Get L2NpcInstance Init parameters and its generate an Identifier
			final Object[] parameters =
			{
				IdFactory.getInstance().getNextId(),
				template
			};
			
			// Call the constructor of the L2NpcInstance
			// (can be a L2ArtefactInstance, L2FriendlyMobInstance, L2GuardInstance, L2MonsterInstance, L2SiegeGuardInstance, L2BoxInstance,
			// L2FeedableBeastInstance, L2TamedBeastInstance, L2FolkInstance)
			final Object tmp = constructor.newInstance(parameters);
			
			// Must be done before object is spawned into visible world
			((L2Object) tmp).setInstanceId(instanceId);
			
			// Check if the Instance is a L2NpcInstance
			if (!(tmp instanceof L2NpcInstance))
			{
				return mob;
			}
			
			mob = (L2NpcInstance) tmp;
			
			return intializeNpcInstance(mob);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.warn("NPC " + template.npcId + " class not found", e);
		}
		return mob;
	}
	
	/**
	 * @param  mob
	 * @return
	 */
	public L2NpcInstance intializeNpcInstance(final L2NpcInstance mob)
	{
		int newlocx, newlocy, newlocz;
		
		boolean doCorrect = false;
		if (Config.GEODATA > 0)
		{
			switch (Config.GEO_CORRECT_Z)
			{
				case ALL:
					doCorrect = true;
					break;
				case TOWN:
					if (mob != null)
					{
						doCorrect = true;
					}
					break;
				case MONSTER:
					if (mob instanceof L2Attackable)
					{
						doCorrect = true;
					}
					break;
			}
		}
		
		// If Locx=0 and Locy=0, the L2NpcInstance must be spawned in an area defined by location
		if (getLocx() == 0 && getLocy() == 0)
		{
			if (getLocation() == 0)
			{
				return mob;
			}
			
			// Calculate the random position in the location area
			final int p[] = TerritoryTable.getInstance().getRandomPoint(getLocation());
			
			// Set the calculated position of the L2NpcInstance
			newlocx = p[0];
			newlocy = p[1];
			newlocz = GeoData.getInstance().getSpawnHeight(newlocx, newlocy, p[2], p[3], id);
		}
		else
		{
			// The L2NpcInstance is spawned at the exact position (Lox, Locy, Locz)
			newlocx = getLocx();
			newlocy = getLocy();
			newlocz = doCorrect ? GeoData.getInstance().getSpawnHeight(newlocx, newlocy, getLocz(), getLocz(), id) : getLocz();
		}
		
		if (mob != null)
		{
			mob.stopAllEffects();
			
			// Set the HP and MP of the L2NpcInstance to the max
			mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp());
			
			// Set the heading of the L2NpcInstance (random heading if not defined)
			if (getHeading() == -1)
			{
				mob.setHeading(Rnd.nextInt(61794));
			}
			else
			{
				mob.setHeading(getHeading());
			}
			
			// Reset decay info
			mob.setDecayed(false);
			
			// Link the L2NpcInstance to this L2Spawn
			mob.setSpawn(this);
			
			EngineModsManager.onSpawn(mob);
			
			// Init other values of the L2NpcInstance (ex : from its L2CharTemplate for INT, STR, DEX...) and add it in the world as a visible object
			mob.spawnMe(newlocx, newlocy, newlocz);
			
			L2Spawn.notifyNpcSpawned(mob);
			
			lastSpawn = mob;
			
			if (Config.DEBUG)
			{
				LOGGER.debug("spawned Mob ID: " + template.npcId + " ,at: " + mob.getX() + " x, " + mob.getY() + " y, " + mob.getZ() + " z");
			}
			
			for (final Quest quest : mob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SPAWN))
			{
				quest.notifySpawn(mob);
			}
			
			// Increase the current number of L2NpcInstance managed by this L2Spawn
			currentCount++;
		}
		return mob;
	}
	
	public static void addSpawnListener(final SpawnListener listener)
	{
		synchronized (spawnListeners)
		{
			spawnListeners.add(listener);
		}
	}
	
	public static void removeSpawnListener(final SpawnListener listener)
	{
		synchronized (spawnListeners)
		{
			spawnListeners.remove(listener);
		}
	}
	
	public static void notifyNpcSpawned(final L2NpcInstance npc)
	{
		synchronized (spawnListeners)
		{
			for (final SpawnListener listener : spawnListeners)
			{
				listener.npcSpawned(npc);
			}
		}
	}
	
	/**
	 * @param i delay in seconds
	 */
	public void setRespawnDelay(int i)
	{
		if (i < 0)
		{
			LOGGER.warn("respawn delay is negative for spawnId:" + id);
		}
		
		if (i < 10)
		{
			i = 10;
		}
		
		respawnDelay = i * 1000;
	}
	
	public L2NpcInstance getLastSpawn()
	{
		return lastSpawn;
	}
	
	public void setLastSpawn(L2NpcInstance npc)
	{
		lastSpawn = npc;
	}
	
	/**
	 * @param oldNpc
	 */
	public void respawnNpc(final L2NpcInstance oldNpc)
	{
		oldNpc.refreshID();
		/* L2NpcInstance instance = */intializeNpcInstance(oldNpc);
	}
	
	public L2NpcTemplate getTemplate()
	{
		return template;
	}
	
	public int getInstanceId()
	{
		return instanceId;
	}
	
	public void setInstanceId(final int instanceId)
	{
		this.instanceId = instanceId;
	}
	
	public Constructor<?> getConstrcutor()
	{
		return constructor;
	}
}
