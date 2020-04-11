/*
 * Copyright (C) 2004-2014 L2J Server
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
package l2r.gameserver.instancemanager;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import l2r.gameserver.engines.DocumentParser;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.L2WorldRegion;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.zone.AbstractZoneSettings;
import l2r.gameserver.model.zone.L2ZoneForm;
import l2r.gameserver.model.zone.L2ZoneRespawn;
import l2r.gameserver.model.zone.L2ZoneType;
import l2r.gameserver.model.zone.form.ZoneCuboid;
import l2r.gameserver.model.zone.form.ZoneCylinder;
import l2r.gameserver.model.zone.form.ZoneNPoly;
import l2r.gameserver.model.zone.type.L2ArenaZone;
import l2r.gameserver.model.zone.type.L2OlympiadStadiumZone;
import l2r.gameserver.model.zone.type.L2RespawnZone;
import l2r.gameserver.model.zone.type.NpcSpawnTerritory;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This class manages the zones
 * @author durgus
 */
public final class ZoneManager extends DocumentParser
{
	private static final Map<String, AbstractZoneSettings> _settings = new HashMap<>();
	
	private final Map<Class<? extends L2ZoneType>, Map<Integer, ? extends L2ZoneType>> _classZones = new HashMap<>();
	private final Map<String, NpcSpawnTerritory> _spawnTerritories = new HashMap<>();
	private int _lastDynamicId = 300000;
	private List<L2ItemInstance> _debugItems;
	
	/**
	 * Instantiates a new zone manager.
	 */
	protected ZoneManager()
	{
		load();
	}
	
	/**
	 * Reload.
	 */
	public void reload()
	{
		// Get the world regions
		int count = 0;
		final L2WorldRegion[][] worldRegions = L2World.getInstance().getAllWorldRegions();
		
		// Backup old zone settings
		for (Map<Integer, ? extends L2ZoneType> map : _classZones.values())
		{
			for (L2ZoneType zone : map.values())
			{
				if (zone.getSettings() != null)
				{
					_settings.put(zone.getName(), zone.getSettings());
				}
			}
		}
		
		// Clear zones
		for (L2WorldRegion[] worldRegion : worldRegions)
		{
			for (L2WorldRegion element : worldRegion)
			{
				element.getZones().clear();
				count++;
			}
		}
		GrandBossManager.getInstance().getZones().clear();
		_log.info(getClass().getSimpleName() + ": Removed zones in " + count + " regions.");
		
		// Load the zones
		load();
		
		// Re-validate all characters in zones
		for (L2Object obj : L2World.getInstance().getVisibleObjects())
		{
			if (obj instanceof L2Character)
			{
				((L2Character) obj).revalidateZone(true);
			}
		}
		_settings.clear();
	}
	
	@Override
	protected void parseDocument()
	{
		// Get the world regions
		final L2WorldRegion[][] worldRegions = L2World.getInstance().getAllWorldRegions();
		NamedNodeMap attrs;
		Node attribute;
		String zoneName;
		int[][] coords;
		int zoneId, minZ, maxZ;
		String zoneType, zoneShape;
		final List<int[]> rs = new ArrayList<>();
		
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				attrs = n.getAttributes();
				attribute = attrs.getNamedItem("enabled");
				if ((attribute != null) && !Boolean.parseBoolean(attribute.getNodeValue()))
				{
					continue;
				}
				
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("zone".equalsIgnoreCase(d.getNodeName()))
					{
						attrs = d.getAttributes();
						
						attribute = attrs.getNamedItem("type");
						if (attribute != null)
						{
							zoneType = attribute.getNodeValue();
						}
						else
						{
							_log.warn("ZoneData: Missing type for zone in file: " + getCurrentFile().getName());
							continue;
						}
						
						attribute = attrs.getNamedItem("id");
						if (attribute != null)
						{
							zoneId = Integer.parseInt(attribute.getNodeValue());
						}
						else
						{
							zoneId = zoneType.equalsIgnoreCase("NpcSpawnTerritory") ? 0 : _lastDynamicId++;
						}
						
						attribute = attrs.getNamedItem("name");
						if (attribute != null)
						{
							zoneName = attribute.getNodeValue();
						}
						else
						{
							zoneName = null;
						}
						
						// Check zone name for NpcSpawnTerritory. Must exist and to be unique
						if (zoneType.equalsIgnoreCase("NpcSpawnTerritory"))
						{
							if (zoneName == null)
							{
								_log.warn("ZoneData: Missing name for NpcSpawnTerritory in file: " + getCurrentFile().getName() + ", skipping zone");
								continue;
							}
							else if (_spawnTerritories.containsKey(zoneName))
							{
								_log.warn("ZoneData: Name " + zoneName + " already used for another zone, check file: " + getCurrentFile().getName() + ". Skipping zone");
								continue;
							}
						}
						
						minZ = parseInteger(attrs, "minZ");
						maxZ = parseInteger(attrs, "maxZ");
						
						zoneType = parseString(attrs, "type");
						zoneShape = parseString(attrs, "shape");
						
						// Get the zone shape from xml
						L2ZoneForm zoneForm = null;
						try
						{
							coords = null;
							int[] point;
							rs.clear();
							
							for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
							{
								if ("node".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									point = new int[2];
									point[0] = parseInteger(attrs, "X");
									point[1] = parseInteger(attrs, "Y");
									rs.add(point);
								}
							}
							
							coords = rs.toArray(new int[rs.size()][2]);
							
							if ((coords == null) || (coords.length == 0))
							{
								_log.warn(getClass().getSimpleName() + ": ZoneData: missing data for zone: " + zoneId + " XML file: " + getCurrentFile().getName());
								continue;
							}
							
							// Create this zone. Parsing for cuboids is a
							// bit different than for other polygons
							// cuboids need exactly 2 points to be defined.
							// Other polygons need at least 3 (one per
							// vertex)
							if (zoneShape.equalsIgnoreCase("Cuboid"))
							{
								if (coords.length == 2)
								{
									zoneForm = new ZoneCuboid(coords[0][0], coords[1][0], coords[0][1], coords[1][1], minZ, maxZ);
								}
								else
								{
									_log.warn(getClass().getSimpleName() + ": ZoneData: Missing cuboid vertex in sql data for zone: " + zoneId + " in file: " + getCurrentFile().getName());
									continue;
								}
							}
							else if (zoneShape.equalsIgnoreCase("NPoly"))
							{
								// nPoly needs to have at least 3 vertices
								if (coords.length > 2)
								{
									final int[] aX = new int[coords.length];
									final int[] aY = new int[coords.length];
									for (int i = 0; i < coords.length; i++)
									{
										aX[i] = coords[i][0];
										aY[i] = coords[i][1];
									}
									zoneForm = new ZoneNPoly(aX, aY, minZ, maxZ);
								}
								else
								{
									_log.warn(getClass().getSimpleName() + ": ZoneData: Bad data for zone: " + zoneId + " in file: " + getCurrentFile().getName());
									continue;
								}
							}
							else if (zoneShape.equalsIgnoreCase("Cylinder"))
							{
								// A Cylinder zone requires a center point
								// at x,y and a radius
								attrs = d.getAttributes();
								final int zoneRad = Integer.parseInt(attrs.getNamedItem("rad").getNodeValue());
								if ((coords.length == 1) && (zoneRad > 0))
								{
									zoneForm = new ZoneCylinder(coords[0][0], coords[0][1], minZ, maxZ, zoneRad);
								}
								else
								{
									_log.warn(getClass().getSimpleName() + ": ZoneData: Bad data for zone: " + zoneId + " in file: " + getCurrentFile().getName());
									continue;
								}
							}
							else
							{
								_log.warn(getClass().getSimpleName() + ": ZoneData: Unknown shape: \"" + zoneShape + "\"  for zone: " + zoneId + " in file: " + getCurrentFile().getName());
								continue;
							}
						}
						catch (Exception e)
						{
							_log.warn(getClass().getSimpleName() + ": ZoneData: Failed to load zone " + zoneId + " coordinates: " + e.getMessage(), e);
						}
						
						// No further parameters needed, if NpcSpawnTerritory is loading
						if (zoneType.equalsIgnoreCase("NpcSpawnTerritory"))
						{
							_spawnTerritories.put(zoneName, new NpcSpawnTerritory(zoneName, zoneForm));
							continue;
						}
						
						// Create the zone
						Class<?> newZone = null;
						Constructor<?> zoneConstructor = null;
						L2ZoneType temp;
						try
						{
							newZone = Class.forName("l2r.gameserver.model.zone.type.L2" + zoneType);
							zoneConstructor = newZone.getConstructor(int.class);
							temp = (L2ZoneType) zoneConstructor.newInstance(zoneId);
							temp.setZone(zoneForm);
						}
						catch (Exception e)
						{
							_log.warn(getClass().getSimpleName() + ": ZoneData: No such zone type: " + zoneType + " in file: " + getCurrentFile().getName());
							continue;
						}
						
						// Check for additional parameters
						for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							if ("stat".equalsIgnoreCase(cd.getNodeName()))
							{
								attrs = cd.getAttributes();
								String name = attrs.getNamedItem("name").getNodeValue();
								String val = attrs.getNamedItem("val").getNodeValue();
								
								temp.setParameter(name, val);
							}
							else if ("spawn".equalsIgnoreCase(cd.getNodeName()) && (temp instanceof L2ZoneRespawn))
							{
								attrs = cd.getAttributes();
								int spawnX = Integer.parseInt(attrs.getNamedItem("X").getNodeValue());
								int spawnY = Integer.parseInt(attrs.getNamedItem("Y").getNodeValue());
								int spawnZ = Integer.parseInt(attrs.getNamedItem("Z").getNodeValue());
								Node val = attrs.getNamedItem("type");
								((L2ZoneRespawn) temp).parseLoc(spawnX, spawnY, spawnZ, val == null ? null : val.getNodeValue());
							}
							else if ("race".equalsIgnoreCase(cd.getNodeName()) && (temp instanceof L2RespawnZone))
							{
								attrs = cd.getAttributes();
								String race = attrs.getNamedItem("name").getNodeValue();
								String point = attrs.getNamedItem("point").getNodeValue();
								
								((L2RespawnZone) temp).addRaceRespawnPoint(race, point);
							}
						}
						if (checkId(zoneId))
						{
							_log.info(getClass().getSimpleName() + ": Caution: Zone (" + zoneId + ") from file: " + getCurrentFile().getName() + " overrides previos definition.");
						}
						
						if ((zoneName != null) && !zoneName.isEmpty())
						{
							temp.setName(zoneName);
						}
						
						addZone(zoneId, temp);
						
						// Register the zone into any world region it
						// intersects with...
						// currently 11136 test for each zone :>
						int ax, ay, bx, by;
						for (int x = 0; x < worldRegions.length; x++)
						{
							for (int y = 0; y < worldRegions[x].length; y++)
							{
								ax = (x - L2World.OFFSET_X) << L2World.SHIFT_BY;
								bx = ((x + 1) - L2World.OFFSET_X) << L2World.SHIFT_BY;
								ay = (y - L2World.OFFSET_Y) << L2World.SHIFT_BY;
								by = ((y + 1) - L2World.OFFSET_Y) << L2World.SHIFT_BY;
								
								if (temp.getZone().intersectsRectangle(ax, bx, ay, by))
								{
									worldRegions[x][y].addZone(temp);
								}
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public final void load()
	{
		_classZones.clear();
		_spawnTerritories.clear();
		parseDirectory("data/xml/zones", false);
		parseDirectory("data/xml/zones/npcSpawnTerritories", false);
		_log.info(getClass().getSimpleName() + ": Loaded " + _classZones.size() + " zone classes and " + getSize() + " zones.");
		_log.info(getClass().getSimpleName() + ": Loaded " + _spawnTerritories.size() + " NPC spawn territoriers.");
	}
	
	/**
	 * Gets the size.
	 * @return the size
	 */
	public int getSize()
	{
		int i = 0;
		for (Map<Integer, ? extends L2ZoneType> map : _classZones.values())
		{
			i += map.size();
		}
		return i;
	}
	
	/**
	 * Check id.
	 * @param id the id
	 * @return true, if successful
	 */
	public boolean checkId(int id)
	{
		for (Map<Integer, ? extends L2ZoneType> map : _classZones.values())
		{
			if (map.containsKey(id))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Add new zone.
	 * @param <T> the generic type
	 * @param id the id
	 * @param zone the zone
	 */
	@SuppressWarnings("unchecked")
	public <T extends L2ZoneType> void addZone(Integer id, T zone)
	{
		Map<Integer, T> map = (Map<Integer, T>) _classZones.get(zone.getClass());
		if (map == null)
		{
			map = new HashMap<>();
			map.put(id, zone);
			_classZones.put(zone.getClass(), map);
		}
		else
		{
			map.put(id, zone);
		}
	}
	
	/**
	 * Returns all zones registered with the ZoneManager. To minimize iteration processing retrieve zones from L2WorldRegion for a specific location instead.
	 * @return zones
	 * @see #getAllZones(Class)
	 */
	@Deprecated
	public Collection<L2ZoneType> getAllZones()
	{
		final List<L2ZoneType> zones = new ArrayList<>();
		for (Map<Integer, ? extends L2ZoneType> map : _classZones.values())
		{
			zones.addAll(map.values());
		}
		return zones;
	}
	
	/**
	 * Return all zones by class type.
	 * @param <T> the generic type
	 * @param zoneType Zone class
	 * @return Collection of zones
	 */
	@SuppressWarnings("unchecked")
	public <T extends L2ZoneType> Collection<T> getAllZones(Class<T> zoneType)
	{
		return (Collection<T>) _classZones.get(zoneType).values();
	}
	
	/**
	 * Get zone by ID.
	 * @param id the id
	 * @return the zone by id
	 * @see #getZoneById(int, Class)
	 */
	public L2ZoneType getZoneById(int id)
	{
		for (Map<Integer, ? extends L2ZoneType> map : _classZones.values())
		{
			if (map.containsKey(id))
			{
				return map.get(id);
			}
		}
		return null;
	}
	
	/**
	 * Get zone by ID and zone class.
	 * @param <T> the generic type
	 * @param id the id
	 * @param zoneType the zone type
	 * @return zone
	 */
	@SuppressWarnings("unchecked")
	public <T extends L2ZoneType> T getZoneById(int id, Class<T> zoneType)
	{
		return (T) _classZones.get(zoneType).get(id);
	}
	
	/**
	 * Returns all zones from where the object is located.
	 * @param object the object
	 * @return zones
	 */
	public List<L2ZoneType> getZones(L2Object object)
	{
		return getZones(object.getX(), object.getY(), object.getZ());
	}
	
	/**
	 * Gets the zone.
	 * @param <T> the generic type
	 * @param object the object
	 * @param type the type
	 * @return zone from where the object is located by type
	 */
	public <T extends L2ZoneType> T getZone(L2Object object, Class<T> type)
	{
		if (object == null)
		{
			return null;
		}
		return getZone(object.getX(), object.getY(), object.getZ(), type);
	}
	
	/**
	 * Returns all zones from given coordinates (plane).
	 * @param x the x
	 * @param y the y
	 * @return zones
	 */
	public List<L2ZoneType> getZones(int x, int y)
	{
		final L2WorldRegion region = L2World.getInstance().getRegion(x, y);
		final List<L2ZoneType> temp = new ArrayList<>();
		for (L2ZoneType zone : region.getZones())
		{
			if (zone.isInsideZone(x, y))
			{
				temp.add(zone);
			}
		}
		return temp;
	}
	
	/**
	 * Returns all zones from given coordinates.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return zones
	 */
	public List<L2ZoneType> getZones(int x, int y, int z)
	{
		final L2WorldRegion region = L2World.getInstance().getRegion(x, y);
		final List<L2ZoneType> temp = new ArrayList<>();
		for (L2ZoneType zone : region.getZones())
		{
			if (zone.isInsideZone(x, y, z))
			{
				temp.add(zone);
			}
		}
		return temp;
	}
	
	/**
	 * Gets the zone.
	 * @param <T> the generic type
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param type the type
	 * @return zone from given coordinates
	 */
	@SuppressWarnings("unchecked")
	public <T extends L2ZoneType> T getZone(int x, int y, int z, Class<T> type)
	{
		final L2WorldRegion region = L2World.getInstance().getRegion(x, y);
		for (L2ZoneType zone : region.getZones())
		{
			if (zone.isInsideZone(x, y, z) && type.isInstance(zone))
			{
				return (T) zone;
			}
		}
		return null;
	}
	
	/**
	 * Get spawm territory by name
	 * @param name name of territory to search
	 * @return link to zone form
	 */
	public NpcSpawnTerritory getSpawnTerritory(String name)
	{
		return _spawnTerritories.containsKey(name) ? _spawnTerritories.get(name) : null;
	}
	
	/**
	 * Returns all spawm territories from where the object is located
	 * @param object
	 * @return zones
	 */
	public List<NpcSpawnTerritory> getSpawnTerritories(L2Object object)
	{
		List<NpcSpawnTerritory> temp = new ArrayList<>();
		for (NpcSpawnTerritory territory : _spawnTerritories.values())
		{
			if (territory.isInsideZone(object.getX(), object.getY(), object.getZ()))
			{
				temp.add(territory);
			}
		}
		
		return temp;
	}
	
	/**
	 * Gets the arena.
	 * @param character the character
	 * @return the arena
	 */
	public final L2ArenaZone getArena(L2Character character)
	{
		if (character == null)
		{
			return null;
		}
		
		for (L2ZoneType temp : ZoneManager.getInstance().getZones(character.getX(), character.getY(), character.getZ()))
		{
			if ((temp instanceof L2ArenaZone) && temp.isCharacterInZone(character))
			{
				return ((L2ArenaZone) temp);
			}
		}
		
		return null;
	}
	
	/**
	 * Gets the olympiad stadium.
	 * @param character the character
	 * @return the olympiad stadium
	 */
	public final L2OlympiadStadiumZone getOlympiadStadium(L2Character character)
	{
		if (character == null)
		{
			return null;
		}
		
		for (L2ZoneType temp : ZoneManager.getInstance().getZones(character.getX(), character.getY(), character.getZ()))
		{
			if ((temp instanceof L2OlympiadStadiumZone) && temp.isCharacterInZone(character))
			{
				return ((L2OlympiadStadiumZone) temp);
			}
		}
		return null;
	}
	
	/**
	 * For testing purposes only.
	 * @param <T> the generic type
	 * @param obj the obj
	 * @param type the type
	 * @return the closest zone
	 */
	@SuppressWarnings("unchecked")
	public <T extends L2ZoneType> T getClosestZone(L2Object obj, Class<T> type)
	{
		T zone = getZone(obj, type);
		if (zone == null)
		{
			double closestdis = Double.MAX_VALUE;
			for (T temp : (Collection<T>) _classZones.get(type).values())
			{
				double distance = temp.getDistanceToZone(obj);
				if (distance < closestdis)
				{
					closestdis = distance;
					zone = temp;
				}
			}
		}
		return zone;
	}
	
	/**
	 * General storage for debug items used for visualizing zones.
	 * @return list of items
	 */
	public List<L2ItemInstance> getDebugItems()
	{
		if (_debugItems == null)
		{
			_debugItems = new ArrayList<>();
		}
		return _debugItems;
	}
	
	/**
	 * Remove all debug items from l2world.
	 */
	public void clearDebugItems()
	{
		if (_debugItems != null)
		{
			final Iterator<L2ItemInstance> it = _debugItems.iterator();
			while (it.hasNext())
			{
				final L2ItemInstance item = it.next();
				if (item != null)
				{
					item.decayMe();
				}
				it.remove();
			}
		}
	}
	
	/**
	 * Gets the settings.
	 * @param name the name
	 * @return the settings
	 */
	public static AbstractZoneSettings getSettings(String name)
	{
		return _settings.get(name);
	}
	
	/**
	 * Gets the single instance of ZoneManager.
	 * @return single instance of ZoneManager
	 */
	public static final ZoneManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ZoneManager _instance = new ZoneManager();
	}
}
