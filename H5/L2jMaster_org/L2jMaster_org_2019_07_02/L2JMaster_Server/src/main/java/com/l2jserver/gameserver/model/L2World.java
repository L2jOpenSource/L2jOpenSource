/*
 * Copyright (C) 2004-2019 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.Config;
import com.l2jserver.gameserver.data.sql.impl.CharNameTable;
import com.l2jserver.gameserver.data.xml.impl.AdminData;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PetInstance;

public final class L2World
{
	private static final Logger LOG = LoggerFactory.getLogger(L2World.class);
	
	/** Gracia border Flying objects not allowed to the east of it. */
	public static final int GRACIA_MAX_X = -166168;
	public static final int GRACIA_MAX_Z = 6105;
	public static final int GRACIA_MIN_Z = -895;
	
	/** Biteshift, defines number of regions note, shifting by 15 will result in regions corresponding to map tiles shifting by 12 divides one tile to 8x8 regions. */
	public static final int SHIFT_BY = 12;
	
	private static final int TILE_SIZE = 32768;
	
	/** Map dimensions */
	public static final int TILE_X_MIN = 11;
	public static final int TILE_Y_MIN = 10;
	public static final int TILE_X_MAX = 26;
	public static final int TILE_Y_MAX = 26;
	public static final int TILE_ZERO_COORD_X = 20;
	public static final int TILE_ZERO_COORD_Y = 18;
	public static final int MAP_MIN_X = (TILE_X_MIN - TILE_ZERO_COORD_X) * TILE_SIZE;
	public static final int MAP_MIN_Y = (TILE_Y_MIN - TILE_ZERO_COORD_Y) * TILE_SIZE;
	
	public static final int MAP_MAX_X = ((TILE_X_MAX - TILE_ZERO_COORD_X) + 1) * TILE_SIZE;
	public static final int MAP_MAX_Y = ((TILE_Y_MAX - TILE_ZERO_COORD_Y) + 1) * TILE_SIZE;
	
	/** calculated offset used so top left region is 0,0 */
	public static final int OFFSET_X = Math.abs(MAP_MIN_X >> SHIFT_BY);
	public static final int OFFSET_Y = Math.abs(MAP_MIN_Y >> SHIFT_BY);
	
	/** number of regions */
	private static final int REGIONS_X = (MAP_MAX_X >> SHIFT_BY) + OFFSET_X;
	private static final int REGIONS_Y = (MAP_MAX_Y >> SHIFT_BY) + OFFSET_Y;
	
	/** Map containing all the players in game. */
	private final Map<Integer, L2PcInstance> _allPlayers = new ConcurrentHashMap<>();
	/** Map containing all visible objects. */
	private final Map<Integer, L2Object> _allObjects = new ConcurrentHashMap<>();
	/** Map with the pets instances and their owner ID. */
	private final Map<Integer, L2PetInstance> _petsInstance = new ConcurrentHashMap<>();
	
	private L2WorldRegion[][] _worldRegions;
	
	/** Constructor of L2World. */
	protected L2World()
	{
		initRegions();
	}
	
	/**
	 * Adds an object to the world.<br>
	 * <B><U>Example of use</U>:</B>
	 * <ul>
	 * <li>Withdraw an item from the warehouse, create an item</li>
	 * <li>Spawn a L2Character (PC, NPC, Pet)</li>
	 * </ul>
	 * @param object
	 */
	public void storeObject(L2Object object)
	{
		if (_allObjects.containsKey(object.getObjectId()))
		{
			LOG.warn("Current object: {} already exist in OID map!", object);
			LOG.warn("---------------------- End ---------------------");
			return;
		}
		_allObjects.put(object.getObjectId(), object);
	}
	
	/**
	 * Removes an object from the world.<br>
	 * <B><U>Example of use</U>:</B>
	 * <ul>
	 * <li>Delete item from inventory, transfer Item from inventory to warehouse</li>
	 * <li>Crystallize item</li>
	 * <li>Remove NPC/PC/Pet from the world</li>
	 * </ul>
	 * @param object the object to remove
	 */
	public void removeObject(L2Object object)
	{
		_allObjects.remove(object.getObjectId());
	}
	
	/**
	 * <B><U> Example of use</U>:</B>
	 * <ul>
	 * <li>Client packets : Action, AttackRequest, RequestJoinParty, RequestJoinPledge...</li>
	 * </ul>
	 * @param objectId Identifier of the L2Object
	 * @return the L2Object object that belongs to an ID or null if no object found.
	 */
	public L2Object findObject(int objectId)
	{
		return _allObjects.get(objectId);
	}
	
	public Collection<L2Object> getVisibleObjects()
	{
		return _allObjects.values();
	}
	
	/**
	 * Get the count of all visible objects in world.
	 * @return count off all L2World objects
	 */
	public int getVisibleObjectsCount()
	{
		return _allObjects.size();
	}
	
	public List<L2PcInstance> getAllGMs()
	{
		return AdminData.getInstance().getAllGms(true);
	}
	
	public Collection<L2PcInstance> getPlayers()
	{
		return _allPlayers.values();
	}
	
	/**
	 * Gets all players sorted by the given comparator.
	 * @param comparator the comparator
	 * @return the players sorted by the comparator
	 */
	public L2PcInstance[] getPlayersSortedBy(Comparator<L2PcInstance> comparator)
	{
		final L2PcInstance[] players = _allPlayers.values().toArray(new L2PcInstance[_allPlayers.values().size()]);
		Arrays.sort(players, comparator);
		return players;
	}
	
	/**
	 * Return how many players are online.
	 * @return number of online players.
	 */
	public int getAllPlayersCount()
	{
		return _allPlayers.size();
	}
	
	/**
	 * <B>If you have access to player objectId use {@link #getPlayer(int playerObjId)}</B>
	 * @param name Name of the player to get Instance
	 * @return the player instance corresponding to the given name.
	 */
	public L2PcInstance getPlayer(String name)
	{
		return getPlayer(CharNameTable.getInstance().getIdByName(name));
	}
	
	/**
	 * @param objectId of the player to get Instance
	 * @return the player instance corresponding to the given object ID.
	 */
	public L2PcInstance getPlayer(int objectId)
	{
		return _allPlayers.get(objectId);
	}
	
	/**
	 * @param ownerId ID of the owner
	 * @return the pet instance from the given ownerId.
	 */
	public L2PetInstance getPet(int ownerId)
	{
		return _petsInstance.get(ownerId);
	}
	
	/**
	 * Add the given pet instance from the given ownerId.
	 * @param ownerId ID of the owner
	 * @param pet L2PetInstance of the pet
	 * @return
	 */
	public L2PetInstance addPet(int ownerId, L2PetInstance pet)
	{
		return _petsInstance.put(ownerId, pet);
	}
	
	/**
	 * Remove the given pet instance.
	 * @param ownerId ID of the owner
	 */
	public void removePet(int ownerId)
	{
		_petsInstance.remove(ownerId);
	}
	
	/**
	 * Remove the given pet instance.
	 * @param pet the pet to remove
	 */
	public void removePet(L2PetInstance pet)
	{
		_petsInstance.remove(pet.getOwner().getObjectId());
	}
	
	/**
	 * Add a L2Object in the world. <B><U> Concept</U> :</B> L2Object (including L2PcInstance) are identified in <B>_visibleObjects</B> of his current L2WorldRegion and in <B>_knownObjects</B> of other surrounding L2Characters <BR>
	 * L2PcInstance are identified in <B>_allPlayers</B> of L2World, in <B>_allPlayers</B> of his current L2WorldRegion and in <B>_knownPlayer</B> of other surrounding L2Characters <B><U> Actions</U> :</B>
	 * <li>Add the L2Object object in _allPlayers* of L2World</li>
	 * <li>Add the L2Object object in _gmList** of GmListTable</li>
	 * <li>Add object in _knownObjects and _knownPlayer* of all surrounding L2WorldRegion L2Characters</li><BR>
	 * <li>If object is a L2Character, add all surrounding L2Object in its _knownObjects and all surrounding L2PcInstance in its _knownPlayer</li><BR>
	 * <I>* only if object is a L2PcInstance</I><BR>
	 * <I>** only if object is a GM L2PcInstance</I> <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object in _visibleObjects and _allPlayers* of L2WorldRegion (need synchronisation)</B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object to _allObjects and _allPlayers* of L2World (need synchronisation)</B></FONT> <B><U> Example of use </U> :</B>
	 * <li>Drop an Item</li>
	 * <li>Spawn a L2Character</li>
	 * <li>Apply Death Penalty of a L2PcInstance</li>
	 * @param object L2object to add in the world
	 * @param newRegion L2WorldRegion in wich the object will be add (not used)
	 */
	public void addVisibleObject(L2Object object, L2WorldRegion newRegion)
	{
		if (!newRegion.isActive())
		{
			return;
		}
		// Get all visible objects contained in the _visibleObjects of L2WorldRegions
		// in a circular area of 2000 units
		List<L2Object> visibles = getVisibleObjects(object, 2000);
		if (Config.DEBUG)
		{
			LOG.debug("objects in range: {}", visibles.size());
		}
		
		// tell the player about the surroundings
		// Go through the visible objects contained in the circular area
		for (L2Object visible : visibles)
		{
			if (visible == null)
			{
				continue;
			}
			
			// Add the object in L2ObjectHashSet(L2Object) _knownObjects of the visible L2Character according to conditions :
			// - L2Character is visible
			// - object is not already known
			// - object is in the watch distance
			// If L2Object is a L2PcInstance, add L2Object in L2ObjectHashSet(L2PcInstance) _knownPlayer of the visible L2Character
			visible.getKnownList().addKnownObject(object);
			
			// Add the visible L2Object in L2ObjectHashSet(L2Object) _knownObjects of the object according to conditions
			// If visible L2Object is a L2PcInstance, add visible L2Object in L2ObjectHashSet(L2PcInstance) _knownPlayer of the object
			object.getKnownList().addKnownObject(visible);
		}
	}
	
	/**
	 * Adds the player to the world.
	 * @param player the player to add
	 */
	public void addPlayerToWorld(L2PcInstance player)
	{
		_allPlayers.put(player.getObjectId(), player);
	}
	
	/**
	 * Remove the player from the world.
	 * @param player the player to remove
	 */
	public void removeFromAllPlayers(L2PcInstance player)
	{
		_allPlayers.remove(player.getObjectId());
	}
	
	/**
	 * Remove an object from the world.<br>
	 * <B><U> Concept</U> :</B> L2Object (including L2PcInstance) are identified in <B>_visibleObjects</B> of his current L2WorldRegion and in <B>_knownObjects</B> of other surrounding L2Characters <BR>
	 * L2PcInstance are identified in <B>_allPlayers</B> of L2World, in <B>_allPlayers</B> of his current L2WorldRegion and in <B>_knownPlayer</B> of other surrounding L2Characters <B><U> Actions</U> :</B>
	 * <li>Remove the L2Object object from _allPlayers* of L2World</li>
	 * <li>Remove the L2Object object from _visibleObjects and _allPlayers* of L2WorldRegion</li>
	 * <li>Remove the L2Object object from _gmList** of GmListTable</li>
	 * <li>Remove object from _knownObjects and _knownPlayer* of all surrounding L2WorldRegion L2Characters</li><BR>
	 * <li>If object is a L2Character, remove all L2Object from its _knownObjects and all L2PcInstance from its _knownPlayer</li> <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World</B></FONT> <I>* only if object is a L2PcInstance</I><BR>
	 * <I>** only if object is a GM L2PcInstance</I> <B><U> Example of use </U> :</B>
	 * <li>Pickup an Item</li>
	 * <li>Decay a L2Character</li>
	 * @param object L2object to remove from the world
	 * @param oldWorldRegion L2WorldRegion in which the object was before removing
	 */
	public void removeVisibleObject(L2Object object, L2WorldRegion oldWorldRegion)
	{
		if (object == null)
		{
			return;
		}
		
		if (oldWorldRegion == null)
		{
			return;
		}
		
		// Removes the object from the visible objects of world region.
		// If object is a player, removes it from the players map of this world region.
		oldWorldRegion.removeVisibleObject(object);
		
		// Goes through all surrounding world region's creatures.
		// And removes the object from their known lists.
		for (L2WorldRegion worldRegion : oldWorldRegion.getSurroundingRegions())
		{
			for (L2Object obj : worldRegion.getVisibleObjects().values())
			{
				if (obj != null)
				{
					obj.getKnownList().removeKnownObject(object);
				}
			}
		}
		
		// Removes all objects from the object's known list.
		object.getKnownList().removeAllKnownObjects();
	}
	
	/**
	 * Return all visible objects of the L2WorldRegion object's and of its surrounding L2WorldRegion. <B><U> Concept</U> :</B> All visible object are identified in <B>_visibleObjects</B> of their current L2WorldRegion <BR>
	 * All surrounding L2WorldRegion are identified in <B>_surroundingRegions</B> of the selected L2WorldRegion in order to scan a large area around a L2Object <B><U> Example of use </U> :</B>
	 * <li>Find Close Objects for L2Character</li><BR>
	 * @param object L2object that determine the current L2WorldRegion
	 * @return
	 */
	public List<L2Object> getVisibleObjects(L2Object object)
	{
		L2WorldRegion reg = object.getWorldRegion();
		
		if (reg == null)
		{
			return null;
		}
		
		// Create a list in order to contain all visible objects.
		final List<L2Object> result = new LinkedList<>();
		for (L2WorldRegion regi : reg.getSurroundingRegions())
		{
			// Go through visible objects of the selected region
			for (L2Object _object : regi.getVisibleObjects().values())
			{
				if ((_object == null) || _object.equals(object))
				{
					continue; // skip our own character
				}
				else if (!_object.isVisible())
				{
					continue; // skip dying objects
				}
				result.add(_object);
			}
		}
		
		return result;
	}
	
	/**
	 * Return all visible objects of the L2WorldRegions in the circular area (radius) centered on the object. <B><U> Concept</U> :</B> All visible object are identified in <B>_visibleObjects</B> of their current L2WorldRegion <BR>
	 * All surrounding L2WorldRegion are identified in <B>_surroundingRegions</B> of the selected L2WorldRegion in order to scan a large area around a L2Object <B><U> Example of use </U> :</B>
	 * <li>Define the aggrolist of monster</li>
	 * <li>Define visible objects of a L2Object</li>
	 * <li>Skill : Confusion...</li><BR>
	 * @param object L2object that determine the center of the circular area
	 * @param radius Radius of the circular area
	 * @return
	 */
	public List<L2Object> getVisibleObjects(L2Object object, int radius)
	{
		if ((object == null) || !object.isVisible())
		{
			return new ArrayList<>();
		}
		
		final int sqRadius = radius * radius;
		
		// Create a list in order to contain all visible objects.
		final List<L2Object> result = new LinkedList<>();
		for (L2WorldRegion regi : object.getWorldRegion().getSurroundingRegions())
		{
			// Go through visible objects of the selected region
			for (L2Object _object : regi.getVisibleObjects().values())
			{
				if ((_object == null) || _object.equals(object))
				{
					continue; // skip our own character
				}
				
				if (sqRadius > object.calculateDistance(_object, false, true))
				{
					result.add(_object);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Return all visible objects of the L2WorldRegions in the spheric area (radius) centered on the object. <B><U> Concept</U> :</B> All visible object are identified in <B>_visibleObjects</B> of their current L2WorldRegion <BR>
	 * All surrounding L2WorldRegion are identified in <B>_surroundingRegions</B> of the selected L2WorldRegion in order to scan a large area around a L2Object <B><U> Example of use </U> :</B>
	 * <li>Define the target list of a skill</li>
	 * <li>Define the target list of a polearme attack</li>
	 * @param object L2object that determine the center of the circular area
	 * @param radius Radius of the spheric area
	 * @return
	 */
	public List<L2Object> getVisibleObjects3D(L2Object object, int radius)
	{
		if ((object == null) || !object.isVisible())
		{
			return new ArrayList<>();
		}
		
		final int sqRadius = radius * radius;
		
		// Create a list in order to contain all visible objects.
		final List<L2Object> result = new LinkedList<>();
		for (L2WorldRegion regi : object.getWorldRegion().getSurroundingRegions())
		{
			for (L2Object _object : regi.getVisibleObjects().values())
			{
				if ((_object == null) || _object.equals(object))
				{
					continue; // skip our own character
				}
				
				if (sqRadius > object.calculateDistance(_object, true, true))
				{
					result.add(_object);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * <B><U> Concept</U> :</B> All visible object are identified in <B>_visibleObjects</B> of their current L2WorldRegion <BR>
	 * All surrounding L2WorldRegion are identified in <B>_surroundingRegions</B> of the selected L2WorldRegion in order to scan a large area around a L2Object <B><U> Example of use </U> :</B>
	 * <li>Find Close Objects for L2Character</li><BR>
	 * @param object L2object that determine the current L2WorldRegion
	 * @return all visible players of the L2WorldRegion object's and of its surrounding L2WorldRegion.
	 */
	public List<L2Playable> getVisiblePlayable(L2Object object)
	{
		L2WorldRegion reg = object.getWorldRegion();
		if (reg == null)
		{
			return null;
		}
		
		// Create a list in order to contain all visible objects.
		final List<L2Playable> result = new LinkedList<>();
		for (L2WorldRegion regi : reg.getSurroundingRegions())
		{
			// Create an Iterator to go through the visible L2Object of the L2WorldRegion
			Map<Integer, L2Playable> _allpls = regi.getVisiblePlayable();
			Collection<L2Playable> _playables = _allpls.values();
			// Go through visible object of the selected region
			for (L2Playable _object : _playables)
			{
				if ((_object == null) || _object.equals(object))
				{
					continue; // skip our own character
				}
				
				if (!_object.isVisible())
				{
					continue; // skip dying objects
				}
				
				result.add(_object);
			}
		}
		
		return result;
	}
	
	/**
	 * Calculate the current L2WorldRegions of the object according to its position (x,y). <B><U> Example of use </U> :</B>
	 * <li>Set position of a new L2Object (drop, spawn...)</li>
	 * <li>Update position of a L2Object after a movement</li><BR>
	 * @param point position of the object
	 * @return
	 */
	public L2WorldRegion getRegion(Location point)
	{
		return _worldRegions[(point.getX() >> SHIFT_BY) + OFFSET_X][(point.getY() >> SHIFT_BY) + OFFSET_Y];
	}
	
	public L2WorldRegion getRegion(int x, int y)
	{
		return _worldRegions[(x >> SHIFT_BY) + OFFSET_X][(y >> SHIFT_BY) + OFFSET_Y];
	}
	
	/**
	 * Returns the whole 2d array containing the world regions used by ZoneData.java to setup zones inside the world regions
	 * @return
	 */
	public L2WorldRegion[][] getWorldRegions()
	{
		return _worldRegions;
	}
	
	/**
	 * Check if the current L2WorldRegions of the object is valid according to its position (x,y). <B><U> Example of use </U> :</B>
	 * <li>Init L2WorldRegions</li><BR>
	 * @param x X position of the object
	 * @param y Y position of the object
	 * @return True if the L2WorldRegion is valid
	 */
	private boolean validRegion(int x, int y)
	{
		return ((x >= 0) && (x <= REGIONS_X) && (y >= 0) && (y <= REGIONS_Y));
	}
	
	/**
	 * Initialize the world regions.
	 */
	private void initRegions()
	{
		_worldRegions = new L2WorldRegion[REGIONS_X + 1][REGIONS_Y + 1];
		
		for (int i = 0; i <= REGIONS_X; i++)
		{
			for (int j = 0; j <= REGIONS_Y; j++)
			{
				_worldRegions[i][j] = new L2WorldRegion(i, j);
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
							_worldRegions[x + a][y + b].addSurroundingRegion(_worldRegions[x][y]);
						}
					}
				}
			}
		}
		
		LOG.info("L2World: ({} by {}) World Region Grid set up.", REGIONS_X, REGIONS_Y);
		
	}
	
	/**
	 * Deleted all spawns in the world.
	 */
	public void deleteVisibleNpcSpawns()
	{
		LOG.info("Deleting all visible NPC's.");
		for (int i = 0; i <= REGIONS_X; i++)
		{
			for (int j = 0; j <= REGIONS_Y; j++)
			{
				_worldRegions[i][j].deleteVisibleNpcSpawns();
			}
		}
		LOG.info("All visible NPC's deleted.");
	}
	
	/**
	 * @return the current instance of L2World
	 */
	public static L2World getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final L2World _instance = new L2World();
	}
	
	/**
	 * @return
	 */
	public Object getAllPlayersArray()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
