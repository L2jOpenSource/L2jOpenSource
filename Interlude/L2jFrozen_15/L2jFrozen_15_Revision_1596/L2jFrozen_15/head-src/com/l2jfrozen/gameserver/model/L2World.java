package com.l2jfrozen.gameserver.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.GmListTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.util.Point3D;
import com.l2jfrozen.util.object.L2ObjectMap;
import com.l2jfrozen.util.object.L2ObjectSet;

import main.data.memory.ObjectData;
import main.holders.objects.ObjectHolder;

/**
 * This class ...
 * @version $Revision: 1.21.2.5.2.7 $ $Date: 2005/03/27 15:29:32 $
 */
public final class L2World
{
	
	/** The LOGGER. */
	private static Logger LOGGER = Logger.getLogger(L2World.class);
	
	/*
	 * biteshift, defines number of regions note, shifting by 15 will result in regions corresponding to map tiles shifting by 12 divides one tile to 8x8 regions
	 */
	/** The Constant SHIFT_BY. */
	public static final int SHIFT_BY = 12;
	
	/** Map dimensions. */
	public static final int MAP_MIN_X = Config.WORLD_SIZE_MIN_X; // -131072
	
	/** The Constant MAP_MAX_X. */
	public static final int MAP_MAX_X = Config.WORLD_SIZE_MAX_X; // 228608
	
	/** The Constant MAP_MIN_Y. */
	public static final int MAP_MIN_Y = Config.WORLD_SIZE_MIN_Y; // -262144
	
	/** The Constant MAP_MAX_Y. */
	public static final int MAP_MAX_Y = Config.WORLD_SIZE_MAX_Y; // 262144
	
	/** calculated offset used so top left region is 0,0. */
	public static final int OFFSET_X = Math.abs(MAP_MIN_X >> SHIFT_BY);
	
	/** The Constant OFFSET_Y. */
	public static final int OFFSET_Y = Math.abs(MAP_MIN_Y >> SHIFT_BY);
	
	/** number of regions. */
	private static final int REGIONS_X = (MAP_MAX_X >> SHIFT_BY) + OFFSET_X;
	
	/** The Constant REGIONS_Y. */
	private static final int REGIONS_Y = (MAP_MAX_Y >> SHIFT_BY) + OFFSET_Y;
	
	// public static final int WORLD_SIZE_X = L2World.MAP_MAX_X - L2World.MAP_MIN_X + 1 >> 15;
	// public static final int WORLD_SIZE_Y = L2World.MAP_MAX_Y - L2World.MAP_MIN_Y + 1 >> 15;
	
	// private HashMap<String, L2PcInstance> allGms;
	
	/** HashMap(String Player name, L2PcInstance) containing all the players in game. */
	private static Map<String, L2PcInstance> allPlayers = new ConcurrentHashMap<>();
	
	/** L2ObjectHashMap(L2Object) containing all visible objects. */
	private static L2ObjectMap<L2Object> allObjects;
	
	/** List with the pets instances and their owner id. */
	private final Map<Integer, L2PetInstance> petsInstance;
	
	private static L2World instance = null;
	
	private L2WorldRegion[][] worldRegions;
	
	/**
	 * Constructor of L2World.<BR>
	 * <BR>
	 */
	private L2World()
	{
		// allGms = new HashMap<String, L2PcInstance>();
		// allPlayers = new ConcurrentHashMap<>()();
		
		petsInstance = new ConcurrentHashMap<>();
		allObjects = L2ObjectMap.createL2ObjectMap();
		
		initRegions();
	}
	
	/**
	 * Gets the single instance of L2World.
	 * @return the current instance of L2World.
	 */
	public static L2World getInstance()
	{
		if (instance == null)
		{
			instance = new L2World();
		}
		return instance;
	}
	
	/**
	 * Add L2Object object in allObjects.<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Withdraw an item from the warehouse, create an item</li>
	 * <li>Spawn a L2Character (PC, NPC, Pet)</li><BR>
	 * @param object the object
	 */
	public void storeObject(final L2Object object)
	{
		if (allObjects.get(object.getObjectId()) != null)
		{
			if (Config.DEBUG)
			{
				LOGGER.warn("[L2World] objectId " + object.getObjectId() + " already exist in OID map!");
			}
			
			return;
		}
		
		allObjects.put(object);
	}
	
	/**
	 * Time store object.
	 * @param  object the object
	 * @return        the long
	 */
	public long timeStoreObject(final L2Object object)
	{
		long time = System.currentTimeMillis();
		allObjects.put(object);
		time -= System.currentTimeMillis();
		
		return time;
	}
	
	/**
	 * Remove L2Object object from allObjects of L2World.<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Delete item from inventory, tranfer Item from inventory to warehouse</li>
	 * <li>Crystallize item</li>
	 * <li>Remove NPC/PC/Pet from the world</li><BR>
	 * @param object L2Object to remove from allObjects of L2World
	 */
	public void removeObject(final L2Object object)
	{
		allObjects.remove(object); // suggestion by whatev
		// IdFactory.getInstance().releaseId(object.getObjectId());
	}
	
	/**
	 * Removes the objects.
	 * @param list the list
	 */
	public void removeObjects(final List<L2Object> list)
	{
		for (final L2Object o : list)
		{
			allObjects.remove(o); // suggestion by whatev
			// IdFactory.getInstance().releaseId(object.getObjectId());
		}
	}
	
	/**
	 * Removes the objects.
	 * @param objects the objects
	 */
	public void removeObjects(final L2Object[] objects)
	{
		for (final L2Object o : objects)
		{
			allObjects.remove(o); // suggestion by whatev
			// IdFactory.getInstance().releaseId(object.getObjectId());
		}
	}
	
	/**
	 * Time remove object.
	 * @param  object the object
	 * @return        the long
	 */
	public long timeRemoveObject(final L2Object object)
	{
		long time = System.currentTimeMillis();
		allObjects.remove(object);
		time -= System.currentTimeMillis();
		
		return time;
	}
	
	/**
	 * Return the L2Object object that belongs to an ID or null if no object found.<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Client packets : Action, AttackRequest, RequestJoinParty, RequestJoinPledge...</li><BR>
	 * @param  oID Identifier of the L2Object
	 * @return     the l2 object
	 */
	public L2Object findObject(final int oID)
	{
		return allObjects.get(oID);
	}
	
	/**
	 * Time find object.
	 * @param  objectID the object id
	 * @return          the long
	 */
	public long timeFindObject(final int objectID)
	{
		long time = System.currentTimeMillis();
		allObjects.get(objectID);
		time -= System.currentTimeMillis();
		
		return time;
	}
	
	/**
	 * Added by Tempy - 08 Aug 05 Allows easy retrevial of all visible objects in world. -- do not use that fucntion, its unsafe!
	 * @return the all visible objects
	 */
	public final L2ObjectMap<L2Object> getAllVisibleObjects()
	{
		return allObjects;
	}
	
	/**
	 * Get the count of all visible objects in world.<br>
	 * <br>
	 * @return count off all L2World objects
	 */
	public final int getAllVisibleObjectsCount()
	{
		return allObjects.size();
	}
	
	/**
	 * Return a table containing all GMs.<BR>
	 * <BR>
	 * @return the all g ms
	 */
	public List<L2PcInstance> getAllGMs()
	{
		return GmListTable.getInstance().getAllGms(true);
	}
	
	/**
	 * Return a collection containing all players in game.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Read-only, please! </B></FONT><BR>
	 * <BR>
	 * @return the all players
	 */
	public Collection<L2PcInstance> getAllPlayers()
	{
		return allPlayers.values();
	}
	
	/**
	 * Return how many players are online.<BR>
	 * <BR>
	 * @return number of online players.
	 */
	public static Integer getAllPlayersCount()
	{
		return allPlayers.size();
	}
	
	/**
	 * Return the player instance corresponding to the given name.
	 * @param  name Name of the player to get Instance
	 * @return      the player
	 */
	public L2PcInstance getPlayer(final String name)
	{
		return allPlayers.get(name.toLowerCase());
	}
	
	/**
	 * Gets the player.
	 * @param  playerObjId the player obj id
	 * @return             the player
	 */
	public L2PcInstance getPlayer(final int playerObjId)
	{
		for (final L2PcInstance actual : allPlayers.values())
		{
			if (actual.getObjectId() == playerObjId)
			{
				return actual;
			}
		}
		return null;
	}
	
	/**
	 * Return a collection containing all pets in game.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Read-only, please! </B></FONT><BR>
	 * <BR>
	 * @return the all pets
	 */
	public Collection<L2PetInstance> getAllPets()
	{
		return petsInstance.values();
	}
	
	/**
	 * Return the pet instance from the given ownerId.<BR>
	 * <BR>
	 * @param  ownerId ID of the owner
	 * @return         the pet
	 */
	public L2PetInstance getPet(final int ownerId)
	{
		return petsInstance.get(ownerId);
	}
	
	/**
	 * Add the given pet instance from the given ownerId.<BR>
	 * <BR>
	 * @param  ownerId ID of the owner
	 * @param  pet     L2PetInstance of the pet
	 * @return         the l2 pet instance
	 */
	public L2PetInstance addPet(final int ownerId, final L2PetInstance pet)
	{
		return petsInstance.put(ownerId, pet);
	}
	
	/**
	 * Remove the given pet instance.<BR>
	 * <BR>
	 * @param ownerId ID of the owner
	 */
	public void removePet(final int ownerId)
	{
		petsInstance.remove(ownerId);
	}
	
	/**
	 * Remove the given pet instance.<BR>
	 * <BR>
	 * @param pet the pet to remove
	 */
	public void removePet(final L2PetInstance pet)
	{
		petsInstance.values().remove(pet);
	}
	
	/**
	 * Add a L2Object in the world.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * L2Object (including L2PcInstance) are identified in <B>visibleObjects</B> of his current L2WorldRegion and in <B>knownObjects</B> of other surrounding L2Characters <BR>
	 * L2PcInstance are identified in <B>allPlayers</B> of L2World, in <B>allPlayers</B> of his current L2WorldRegion and in <B>knownPlayer</B> of other surrounding L2Characters <BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Add the L2Object object in allPlayers* of L2World</li>
	 * <li>Add the L2Object object in gmList** of GmListTable</li>
	 * <li>Add object in knownObjects and knownPlayer* of all surrounding L2WorldRegion L2Characters</li> <BR>
	 * <li>If object is a L2Character, add all surrounding L2Object in its knownObjects and all surrounding L2PcInstance in its knownPlayer</li><BR>
	 * <I>* only if object is a L2PcInstance</I><BR>
	 * <I>** only if object is a GM L2PcInstance</I><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object in visibleObjects and allPlayers* of L2WorldRegion (need synchronisation)</B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object to allObjects and allPlayers* of L2World (need synchronisation)</B></FONT><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Drop an Item</li>
	 * <li>Spawn a L2Character</li>
	 * <li>Apply Death Penalty of a L2PcInstance</li><BR>
	 * <BR>
	 * @param object    L2object to add in the world
	 * @param newRegion the new region
	 * @param dropper   L2Character who has dropped the object (if necessary)
	 */
	public void addVisibleObject(final L2Object object, final L2WorldRegion newRegion, final L2Character dropper)
	{
		// If selected L2Object is a L2PcIntance, add it in L2ObjectHashSet(L2PcInstance) allPlayers of L2World
		//
		if (object instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) object;
			L2PcInstance tmp = allPlayers.get(player.getName().toLowerCase());
			if (tmp != null && tmp != player) // just kick the player previous instance
			{
				tmp.store(); // Store character and items
				tmp.logout();
				
				if (tmp.getClient() != null)
				{
					tmp.getClient().setActiveChar(null); // prevent deleteMe from being called a second time on disconnection
				}
				
				tmp = null;
				/*
				 * if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && tmp.isOffline()) { LOGGER.warn("Offline: Duplicate character!? Closing offline character (" + tmp.getName() + ")"); tmp.store(); // Store character and items tmp.logout(); if(tmp.getClient() != null) {
				 * tmp.getClient().setActiveChar(null); // prevent deleteMe from being called a second time on disconnection } tmp = null; } else { LOGGER.warn("EnterWorld: Duplicate character!? Closing both characters (" + player.getName() + ")"); L2GameClient client = player.getClient(); player.store(); // Store
				 * character player.deleteMe(); client.setActiveChar(null); // prevent deleteMe from being called a second time on disconnection client = tmp.getClient(); tmp.store(); // Store character and items tmp.deleteMe(); if(client != null) { client.setActiveChar(null); // prevent deleteMe from being called
				 * a second time on disconnection } tmp = null; return; }
				 */
			}
			
			if (!newRegion.isActive())
			{
				return;
			}
			// Get all visible objects contained in the visibleObjects of L2WorldRegions
			// in a circular area of 2000 units
			final List<L2Object> visibles = getVisibleObjects(object, 2000);
			if (Config.DEBUG)
			{
				LOGGER.debug("objects in range:" + visibles.size());
			}
			
			// tell the player about the surroundings
			// Go through the visible objects contained in the circular area
			for (final L2Object visible : visibles)
			{
				if (visible == null)
				{
					continue;
				}
				
				// Add the object in L2ObjectHashSet(L2Object) knownObjects of the visible L2Character according to conditions :
				// - L2Character is visible
				// - object is not already known
				// - object is in the watch distance
				// If L2Object is a L2PcInstance, add L2Object in L2ObjectHashSet(L2PcInstance) knownPlayer of the visible L2Character
				visible.getKnownList().addKnownObject(object);
				
				// Add the visible L2Object in L2ObjectHashSet(L2Object) knownObjects of the object according to conditions
				// If visible L2Object is a L2PcInstance, add visible L2Object in L2ObjectHashSet(L2PcInstance) knownPlayer of the object
				object.getKnownList().addKnownObject(visible);
			}
			
			if (!player.isTeleporting())
			{
				// L2PcInstance tmp = allPlayers.get(player.getName().toLowerCase());
				if (tmp != null)
				{
					LOGGER.warn("Teleporting: Duplicate character!? Closing both characters (" + player.getName() + ")");
					player.closeNetConnection();
					tmp.closeNetConnection();
					return;
				}
				
			}
			
			synchronized (allPlayers)
			{
				allPlayers.put(player.getName().toLowerCase(), player);
				
			}
			
			player = null;
		}
		
		// Get all visible objects contained in the visibleObjects of L2WorldRegions
		// in a circular area of 2000 units
		List<L2Object> visibles = getVisibleObjects(object, 2000);
		
		if (Config.DEBUG)
		{
			LOGGER.debug("objects in range:" + visibles.size());
		}
		
		// tell the player about the surroundings
		// Go through the visible objects contained in the circular area
		for (final L2Object visible : visibles)
		{
			// Add the object in L2ObjectHashSet(L2Object) knownObjects of the visible L2Character according to conditions :
			// - L2Character is visible
			// - object is not already known
			// - object is in the watch distance
			// If L2Object is a L2PcInstance, add L2Object in L2ObjectHashSet(L2PcInstance) knownPlayer of the visible L2Character
			visible.getKnownList().addKnownObject(object, dropper);
			
			// Add the visible L2Object in L2ObjectHashSet(L2Object) knownObjects of the object according to conditions
			// If visible L2Object is a L2PcInstance, add visible L2Object in L2ObjectHashSet(L2PcInstance) knownPlayer of the object
			object.getKnownList().addKnownObject(visible, dropper);
		}
		
		visibles = null;
	}
	
	/**
	 * Add the L2PcInstance to allPlayers of L2World.
	 * @param cha the cha
	 */
	public void addToAllPlayers(final L2PcInstance cha)
	{
		allPlayers.put(cha.getName().toLowerCase(), cha);
	}
	
	/**
	 * Remove the L2PcInstance from allPlayers of L2World.<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Remove a player fom the visible objects</li><BR>
	 * @param cha the cha
	 */
	public void removeFromAllPlayers(final L2PcInstance cha)
	{
		if (cha != null && !cha.isTeleporting())
		{
			if (Config.DEBUG)
			{
				LOGGER.info("Removed player: " + cha.getName().toLowerCase());
			}
			allPlayers.remove(cha.getName().toLowerCase());
		}
	}
	
	/**
	 * Remove a L2Object from the world.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * L2Object (including L2PcInstance) are identified in <B>visibleObjects</B> of his current L2WorldRegion and in <B>knownObjects</B> of other surrounding L2Characters <BR>
	 * L2PcInstance are identified in <B>allPlayers</B> of L2World, in <B>allPlayers</B> of his current L2WorldRegion and in <B>knownPlayer</B> of other surrounding L2Characters <BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove the L2Object object from allPlayers* of L2World</li>
	 * <li>Remove the L2Object object from visibleObjects and allPlayers* of L2WorldRegion</li>
	 * <li>Remove the L2Object object from gmList** of GmListTable</li>
	 * <li>Remove object from knownObjects and knownPlayer* of all surrounding L2WorldRegion L2Characters</li><BR>
	 * <li>If object is a L2Character, remove all L2Object from its knownObjects and all L2PcInstance from its knownPlayer</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from allObjects of L2World</B></FONT><BR>
	 * <BR>
	 * <I>* only if object is a L2PcInstance</I><BR>
	 * <I>** only if object is a GM L2PcInstance</I><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Pickup an Item</li>
	 * <li>Decay a L2Character</li><BR>
	 * <BR>
	 * @param object    L2object to remove from the world
	 * @param oldRegion the old region
	 */
	public void removeVisibleObject(final L2Object object, final L2WorldRegion oldRegion)
	{
		if (object == null)
		{
			return;
		}
		
		// removeObject(object);
		
		if (oldRegion != null)
		{
			// Remove the object from the L2ObjectHashSet(L2Object) visibleObjects of L2WorldRegion
			// If object is a L2PcInstance, remove it from the L2ObjectHashSet(L2PcInstance) allPlayers of this L2WorldRegion
			oldRegion.removeVisibleObject(object);
			
			// Go through all surrounding L2WorldRegion L2Characters
			for (final L2WorldRegion reg : oldRegion.getSurroundingRegions())
			{
				for (final L2Object obj : reg.getVisibleObjects())
				{
					// Remove the L2Object from the L2ObjectHashSet(L2Object) knownObjects of the surrounding L2WorldRegion L2Characters
					// If object is a L2PcInstance, remove the L2Object from the L2ObjectHashSet(L2PcInstance) knownPlayer of the surrounding L2WorldRegion L2Characters
					// If object is targeted by one of the surrounding L2WorldRegion L2Characters, cancel ATTACK and cast
					if (obj != null && obj.getKnownList() != null)
					{
						obj.getKnownList().removeKnownObject(object);
					}
					
					// Remove surrounding L2WorldRegion L2Characters from the L2ObjectHashSet(L2Object) knownObjects of object
					// If surrounding L2WorldRegion L2Characters is a L2PcInstance, remove it from the L2ObjectHashSet(L2PcInstance) knownPlayer of object
					//
					if (object.getKnownList() != null)
					{
						object.getKnownList().removeKnownObject(obj);
					}
				}
			}
			
			// If object is a L2Character :
			// Remove all L2Object from L2ObjectHashSet(L2Object) containing all L2Object detected by the L2Character
			// Remove all L2PcInstance from L2ObjectHashSet(L2PcInstance) containing all player ingame detected by the L2Character
			object.getKnownList().removeAllKnownObjects();
			
			// If selected L2Object is a L2PcIntance, remove it from L2ObjectHashSet(L2PcInstance) allPlayers of L2World
			if (object instanceof L2PcInstance)
			{
				if (!((L2PcInstance) object).isTeleporting())
				{
					removeFromAllPlayers((L2PcInstance) object);
				}
				
				// If selected L2Object is a GM L2PcInstance, remove it from Set(L2PcInstance) gmList of GmListTable
				// if (((L2PcInstance)object).isGM())
				// GmListTable.getInstance().deleteGm((L2PcInstance)object);
			}
			
		}
	}
	
	/**
	 * Return all visible objects of the L2WorldRegion object's and of its surrounding L2WorldRegion.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All visible object are identified in <B>visibleObjects</B> of their current L2WorldRegion <BR>
	 * All surrounding L2WorldRegion are identified in <B>_surroundingRegions</B> of the selected L2WorldRegion in order to scan a large area around a L2Object<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Find Close Objects for L2Character</li><BR>
	 * @param  object L2object that determine the current L2WorldRegion
	 * @return        the visible objects
	 */
	public List<L2Object> getVisibleObjects(final L2Object object)
	{
		if (object == null)
		{
			return null;
		}
		
		final L2WorldRegion reg = object.getWorldRegion();
		
		if (reg == null)
		{
			return null;
		}
		
		// Create an List in order to contain all visible L2Object
		final List<L2Object> result = new ArrayList<>();
		
		// Create a List containing all regions around the current region
		List<L2WorldRegion> regions = reg.getSurroundingRegions();
		
		// Go through the List of region
		for (int i = 0; regions != null && i < regions.size(); i++)
		{
			// Go through visible objects of the selected region
			L2ObjectSet<L2Object> actual_objectSet = null;
			if (regions.get(i) != null)
			{
				actual_objectSet = regions.get(i).getVisibleObjects();
			}
			
			if (actual_objectSet != null && actual_objectSet.size() > 0)
			{
				for (final L2Object objectInstance : actual_objectSet)
				{
					if (objectInstance == null)
					{
						continue;
					}
					
					if (objectInstance.equals(object))
					{
						continue; // skip our own character
					}
					
					if (!objectInstance.isVisible())
					{
						continue; // skip dying objects
					}
					
					result.add(objectInstance);
				}
			}
		}
		
		regions = null;
		
		return result;
	}
	
	/**
	 * Return all visible objects of the L2WorldRegions in the circular area (radius) centered on the object.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All visible object are identified in <B>visibleObjects</B> of their current L2WorldRegion <BR>
	 * All surrounding L2WorldRegion are identified in <B>_surroundingRegions</B> of the selected L2WorldRegion in order to scan a large area around a L2Object<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Define the aggrolist of monster</li>
	 * <li>Define visible objects of a L2Object</li>
	 * <li>Skill : Confusion...</li> <BR>
	 * @param  object L2object that determine the center of the circular area
	 * @param  radius Radius of the circular area
	 * @return        the visible objects
	 */
	public List<L2Object> getVisibleObjects(final L2Object object, final int radius)
	{
		if (object == null || !object.isVisible())
		{
			return new ArrayList<>();
		}
		
		final L2WorldRegion region = object.getWorldRegion();
		
		if (region == null)
		{
			return new ArrayList<>();
		}
		
		final int x = object.getX();
		final int y = object.getY();
		final int sqRadius = radius * radius;
		
		// Create an List in order to contain all visible L2Object
		final List<L2Object> result = new ArrayList<>();
		
		// Create an List containing all regions around the current region
		List<L2WorldRegion> regions = region.getSurroundingRegions();
		
		// Go through the List of region
		for (int i = 0; regions != null && i < regions.size(); i++)
		{
			// Go through visible objects of the selected region
			for (final L2Object objectInstance : regions.get(i).getVisibleObjects())
			{
				if (objectInstance == null)
				{
					continue;
				}
				
				if (objectInstance.equals(object))
				{
					continue; // skip our own character
				}
				
				final int x1 = objectInstance.getX();
				final int y1 = objectInstance.getY();
				
				final double dx = x1 - x;
				// if (dx > radius || -dx > radius)
				// continue;
				final double dy = y1 - y;
				// if (dy > radius || -dy > radius)
				// continue;
				
				// If the visible object is inside the circular area
				// add the object to the List result
				if (dx * dx + dy * dy < sqRadius)
				{
					result.add(objectInstance);
				}
			}
		}
		
		regions = null;
		
		return result;
	}
	
	/**
	 * Return all visible objects of the L2WorldRegions in the spheric area (radius) centered on the object.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All visible object are identified in <B>visibleObjects</B> of their current L2WorldRegion <BR>
	 * All surrounding L2WorldRegion are identified in <B>_surroundingRegions</B> of the selected L2WorldRegion in order to scan a large area around a L2Object<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Define the target list of a skill</li>
	 * <li>Define the target list of a polearme attack</li><BR>
	 * <BR>
	 * @param  object L2object that determine the center of the circular area
	 * @param  radius Radius of the spheric area
	 * @return        the visible objects3 d
	 */
	public List<L2Object> getVisibleObjects3D(final L2Object object, final int radius)
	{
		if (object == null || !object.isVisible())
		{
			return new ArrayList<>();
		}
		
		final int x = object.getX();
		final int y = object.getY();
		final int z = object.getZ();
		final int sqRadius = radius * radius;
		
		// Create an List in order to contain all visible L2Object
		final List<L2Object> result = new ArrayList<>();
		
		// Create an List containing all regions around the current region
		List<L2WorldRegion> regions = object.getWorldRegion().getSurroundingRegions();
		
		// Go through visible object of the selected region
		for (final L2WorldRegion region : regions)
		{
			for (final L2Object objectInstance : region.getVisibleObjects())
			{
				if (objectInstance == null)
				{
					continue;
				}
				
				if (objectInstance.equals(object))
				{
					continue; // skip our own character
				}
				
				final int x1 = objectInstance.getX();
				final int y1 = objectInstance.getY();
				final int z1 = objectInstance.getZ();
				
				final long dx = x1 - x;
				// if (dx > radius || -dx > radius)
				// continue;
				final long dy = y1 - y;
				// if (dy > radius || -dy > radius)
				// continue;
				final long dz = z1 - z;
				
				if (dx * dx + dy * dy + dz * dz < sqRadius)
				{
					result.add(objectInstance);
				}
			}
		}
		
		regions = null;
		
		return result;
	}
	
	/**
	 * Return all visible players of the L2WorldRegion object's and of its surrounding L2WorldRegion.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All visible object are identified in <B>visibleObjects</B> of their current L2WorldRegion <BR>
	 * All surrounding L2WorldRegion are identified in <B>_surroundingRegions</B> of the selected L2WorldRegion in order to scan a large area around a L2Object<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Find Close Objects for L2Character</li><BR>
	 * @param  object L2object that determine the current L2WorldRegion
	 * @return        the visible playable
	 */
	public List<L2PlayableInstance> getVisiblePlayable(final L2Object object)
	{
		L2WorldRegion reg = object.getWorldRegion();
		
		if (reg == null)
		{
			return null;
		}
		
		// Create an List in order to contain all visible L2Object
		final List<L2PlayableInstance> result = new ArrayList<>();
		
		// Create a List containing all regions around the current region
		List<L2WorldRegion> regions = reg.getSurroundingRegions();
		
		// Go through the List of region
		for (final L2WorldRegion region : regions)
		{
			// Create an Iterator to go through the visible L2Object of the L2WorldRegion
			Iterator<L2PlayableInstance> playables = region.iterateAllPlayers();
			
			// Go through visible object of the selected region
			while (playables.hasNext())
			{
				L2PlayableInstance objectInstance = playables.next();
				
				if (objectInstance == null)
				{
					continue;
				}
				
				if (objectInstance.equals(object))
				{
					continue; // skip our own character
				}
				
				if (!objectInstance.isVisible())
				{
					continue; // skip dying objects
				}
				
				if (ObjectData.get(ObjectHolder.class, objectInstance).isDifferentWorld(object))
				{
					continue;
				}
				
				result.add(objectInstance);
				
				objectInstance = null;
			}
			
			playables = null;
		}
		
		reg = null;
		regions = null;
		
		return result;
	}
	
	/**
	 * Calculate the current L2WorldRegions of the object according to its position (x,y).<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Set position of a new L2Object (drop, spawn...)</li>
	 * <li>Update position of a L2Object after a mouvement</li><BR>
	 * @param  point the point
	 * @return       the region
	 */
	public L2WorldRegion getRegion(final Point3D point)
	{
		return worldRegions[(point.getX() >> SHIFT_BY) + OFFSET_X][(point.getY() >> SHIFT_BY) + OFFSET_Y];
	}
	
	/**
	 * Gets the region.
	 * @param  x the x
	 * @param  y the y
	 * @return   the region
	 */
	public L2WorldRegion getRegion(final int x, final int y)
	{
		return worldRegions[(x >> SHIFT_BY) + OFFSET_X][(y >> SHIFT_BY) + OFFSET_Y];
	}
	
	/**
	 * Returns the whole 2d array containing the world regions used by ZoneData.java to setup zones inside the world regions
	 * @return the all world regions
	 */
	public L2WorldRegion[][] getAllWorldRegions()
	{
		return worldRegions;
	}
	
	/**
	 * Check if the current L2WorldRegions of the object is valid according to its position (x,y).<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Init L2WorldRegions</li><BR>
	 * @param  x X position of the object
	 * @param  y Y position of the object
	 * @return   True if the L2WorldRegion is valid
	 */
	private boolean validRegion(final int x, final int y)
	{
		return x >= 0 && x <= REGIONS_X && y >= 0 && y <= REGIONS_Y;
	}
	
	/**
	 * Init each L2WorldRegion and their surrounding table.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All surrounding L2WorldRegion are identified in <B>_surroundingRegions</B> of the selected L2WorldRegion in order to scan a large area around a L2Object<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Constructor of L2World</li><BR>
	 */
	private void initRegions()
	{
		worldRegions = new L2WorldRegion[REGIONS_X + 1][REGIONS_Y + 1];
		
		for (int i = 0; i <= REGIONS_X; i++)
		{
			for (int j = 0; j <= REGIONS_Y; j++)
			{
				worldRegions[i][j] = new L2WorldRegion(i, j);
			}
		}
		
		for (int x = 0; x <= REGIONS_X; x++)
		{
			for (int y = 0; y <= REGIONS_Y; y++)
			{
				for (int a = -1; a <= 1; a++)
				{
					for (int b = -1; b <= 1; b++)
					{
						if (validRegion(x + a, y + b))
						{
							worldRegions[x + a][y + b].addSurroundingRegion(worldRegions[x][y]);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Deleted all spawns in the world.
	 */
	public synchronized void deleteVisibleNpcSpawns()
	{
		LOGGER.info("Deleting all visible NPC's.");
		
		for (int i = 0; i <= REGIONS_X; i++)
		{
			for (int j = 0; j <= REGIONS_Y; j++)
			{
				worldRegions[i][j].deleteVisibleNpcSpawns();
			}
		}
		LOGGER.info("All visible NPC's deleted.");
	}
	
	/**
	 * Gets the account players.
	 * @param  account_name the account_name
	 * @return              the account players
	 */
	public List<L2PcInstance> getAccountPlayers(String account_name)
	{
		List<L2PcInstance> players_for_account = new ArrayList<>();
		
		for (L2PcInstance actual : allPlayers.values())
		{
			if (actual.getAccountName().equals(account_name))
			{
				players_for_account.add(actual);
			}
		}
		
		return players_for_account;
	}
}
