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
package com.l2jserver.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jserver.Config;
import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.data.xml.impl.NpcData;
import com.l2jserver.gameserver.instancemanager.DayNightSpawnManager;
import com.l2jserver.gameserver.instancemanager.ZoneManager;
import com.l2jserver.gameserver.model.L2Spawn;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.util.data.xml.IXmlReader;

/**
 * Spawn data retriever.
 * @author Zoey76
 */
public final class SpawnTable implements IXmlReader
{
	// SQL
	private static final String SELECT_SPAWNS = "SELECT count, npc_templateid, locx, locy, locz, heading, respawn_delay, respawn_random, loc_id, periodOfDay FROM spawnlist";
	private static final String SELECT_CUSTOM_SPAWNS = "SELECT count, npc_templateid, locx, locy, locz, heading, respawn_delay, respawn_random, loc_id, periodOfDay FROM custom_spawnlist";
	
	private static final Map<Integer, Set<L2Spawn>> _spawnTable = new ConcurrentHashMap<>();
	
	private int _xmlSpawnCount = 0;
	
	/**
	 * Wrapper to load all spawns.
	 */
	@Override
	public void load()
	{
		if (!Config.ALT_DEV_NO_SPAWNS)
		{
			fillSpawnTable(false);
			final int spawnCount = _spawnTable.size();
			LOG.info("{}: Loaded " + spawnCount + " npc spawns.", getClass().getSimpleName());
			if (Config.CUSTOM_SPAWNLIST_TABLE)
			{
				fillSpawnTable(true);
				LOG.info("{}: Loaded " + (_spawnTable.size() - spawnCount) + " custom npc spawns.", getClass().getSimpleName());
			}
			
			// Load XML list
			parseDatapackDirectory("data/spawnlist", false);
			LOG.info("{}: Loaded " + _xmlSpawnCount + " npc spawns from XML.", getClass().getSimpleName());
		}
	}
	
	/**
	 * Verifies if the template exists and it's spawnable.
	 * @param npcId the NPC ID
	 * @return {@code true} if the NPC ID belongs to an spawnable template, {@code false} otherwise
	 */
	private boolean checkTemplate(int npcId)
	{
		L2NpcTemplate npcTemplate = NpcData.getInstance().getTemplate(npcId);
		if (npcTemplate == null)
		{
			LOG.warn("{}: Data missing in NPC table for ID: {}.", getClass().getSimpleName(), npcId);
			return false;
		}
		
		if (npcTemplate.isType("L2SiegeGuard") || npcTemplate.isType("L2RaidBoss"))
		{
			// Don't spawn
			return false;
		}
		
		return true;
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		NamedNodeMap attrs;
		for (Node list = doc.getFirstChild(); list != null; list = list.getNextSibling())
		{
			if (list.getNodeName().equalsIgnoreCase("list"))
			{
				attrs = list.getAttributes();
				// skip disabled spawnlists
				if (!Boolean.parseBoolean(attrs.getNamedItem("enabled").getNodeValue()))
				{
					continue;
				}
				for (Node param = list.getFirstChild(); param != null; param = param.getNextSibling())
				{
					attrs = param.getAttributes();
					if (param.getNodeName().equalsIgnoreCase("spawn"))
					{
						String territoryName = null;
						String spawnName = null;
						Map<String, Integer> map = null;
						
						// Check, if spawn name specified
						if (attrs.getNamedItem("name") != null)
						{
							spawnName = parseString(attrs, "name");
						}
						// Check, if spawn territory specified and exists
						if ((attrs.getNamedItem("zone") != null) && (ZoneManager.getInstance().getSpawnTerritory(attrs.getNamedItem("zone").getNodeValue()) != null))
						{
							territoryName = parseString(attrs, "zone");
						}
						
						for (Node npctag = param.getFirstChild(); npctag != null; npctag = npctag.getNextSibling())
						{
							attrs = npctag.getAttributes();
							// Check if there are any AI parameters
							if (npctag.getNodeName().equalsIgnoreCase("AIData"))
							{
								attrs = npctag.getAttributes();
								if (map == null)
								{
									map = new HashMap<>();
								}
								for (Node c = npctag.getFirstChild(); c != null; c = c.getNextSibling())
								{
									// Skip odd nodes
									if (c.getNodeName().equals("#text"))
									{
										continue;
									}
									int val;
									switch (c.getNodeName())
									{
										case "disableRandomAnimation":
										case "disableRandomWalk":
											val = Boolean.parseBoolean(c.getTextContent()) ? 1 : 0;
											break;
										default:
											val = Integer.parseInt(c.getTextContent());
									}
									map.put(c.getNodeName(), val);
								}
							}
							// Check for NPC spawns
							else if (npctag.getNodeName().equalsIgnoreCase("npc"))
							{
								// mandatory
								final int templateId = parseInteger(attrs, "id");
								// coordinates are optional, if territory is specified; mandatory otherwise
								int x = 0;
								int y = 0;
								int z = 0;
								
								try
								{
									x = parseInteger(attrs, "x");
									y = parseInteger(attrs, "y");
									z = parseInteger(attrs, "z");
								}
								catch (NullPointerException npe)
								{
									// x, y, z can be unspecified, if this spawn is territory based, do nothing
								}
								
								if ((x == 0) && (y == 0) && (territoryName == null)) // Both coordinates and zone are unspecified
								{
									LOG.warn("{}: Spawn could not be initialized, both coordinates and zone are unspecified for ID {}", getClass().getSimpleName(), templateId);
									continue;
								}
								
								StatsSet spawnInfo = new StatsSet();
								spawnInfo.set("npcTemplateid", templateId);
								spawnInfo.set("x", x);
								spawnInfo.set("y", y);
								spawnInfo.set("z", z);
								spawnInfo.set("territoryName", territoryName);
								spawnInfo.set("spawnName", spawnName);
								
								// trying to read optional parameters
								if (attrs.getNamedItem("heading") != null)
								{
									spawnInfo.set("heading", parseInteger(attrs, "heading"));
								}
								
								if (attrs.getNamedItem("count") != null)
								{
									spawnInfo.set("count", parseInteger(attrs, "count"));
								}
								
								if (attrs.getNamedItem("respawnDelay") != null)
								{
									spawnInfo.set("respawnDelay", parseInteger(attrs, "respawnDelay"));
								}
								
								if (attrs.getNamedItem("respawnRandom") != null)
								{
									spawnInfo.set("respawnRandom", parseInteger(attrs, "respawnRandom"));
								}
								
								if (attrs.getNamedItem("periodOfDay") != null)
								{
									String period = attrs.getNamedItem("periodOfDay").getNodeValue();
									if (period.equalsIgnoreCase("day") || period.equalsIgnoreCase("night"))
									{
										spawnInfo.set("periodOfDay", period.equalsIgnoreCase("day") ? 1 : 2);
									}
								}
								
								_xmlSpawnCount += addSpawn(spawnInfo, map);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Retrieves spawn data from database.
	 * @param isCustom if {@code true} the spawns are loaded as custom from custom spawn table
	 * @return the spawn count
	 */
	private int fillSpawnTable(boolean isCustom)
	{
		int npcSpawnCount = 0;
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery(isCustom ? SELECT_CUSTOM_SPAWNS : SELECT_SPAWNS))
		{
			while (rs.next())
			{
				StatsSet spawnInfo = new StatsSet();
				int npcId = rs.getInt("npc_templateid");
				
				// Check basic requirements first
				if (!checkTemplate(npcId))
				{
					// Don't spawn
					continue;
				}
				
				spawnInfo.set("npcTemplateid", npcId);
				spawnInfo.set("count", rs.getInt("count"));
				spawnInfo.set("x", rs.getInt("locx"));
				spawnInfo.set("y", rs.getInt("locy"));
				spawnInfo.set("z", rs.getInt("locz"));
				spawnInfo.set("heading", rs.getInt("heading"));
				spawnInfo.set("respawnDelay", rs.getInt("respawn_delay"));
				spawnInfo.set("respawnRandom", rs.getInt("respawn_random"));
				spawnInfo.set("locId", rs.getInt("loc_id"));
				spawnInfo.set("periodOfDay", rs.getInt("periodOfDay"));
				spawnInfo.set("isCustomSpawn", isCustom);
				npcSpawnCount += addSpawn(spawnInfo);
			}
		}
		catch (Exception e)
		{
			LOG.warn("{}: Spawn could not be initialized!", getClass().getSimpleName(), e);
		}
		return npcSpawnCount;
	}
	
	/**
	 * Creates NPC spawn
	 * @param spawnInfo StatsSet of spawn parameters
	 * @param AIData Map of specific AI parameters for this spawn
	 * @return count NPC instances, spawned by this spawn
	 */
	private int addSpawn(StatsSet spawnInfo, Map<String, Integer> AIData)
	{
		L2Spawn spawnDat;
		int ret = 0;
		try
		{
			spawnDat = new L2Spawn(spawnInfo.getInt("npcTemplateid"));
			spawnDat.setAmount(spawnInfo.getInt("count", 1));
			spawnDat.setX(spawnInfo.getInt("x", 0));
			spawnDat.setY(spawnInfo.getInt("y", 0));
			spawnDat.setZ(spawnInfo.getInt("z", 0));
			spawnDat.setHeading(spawnInfo.getInt("heading", -1));
			spawnDat.setRespawnDelay(spawnInfo.getInt("respawnDelay", 0), spawnInfo.getInt("respawnRandom", 0));
			spawnDat.setLocationId(spawnInfo.getInt("locId", 0));
			String territoryName = spawnInfo.getString("territoryName", "");
			String spawnName = spawnInfo.getString("spawnName", "");
			spawnDat.setCustom(spawnInfo.getBoolean("isCustomSpawn", false));
			if (!spawnName.isEmpty())
			{
				spawnDat.setName(spawnName);
			}
			if (!territoryName.isEmpty())
			{
				spawnDat.setSpawnTerritory(ZoneManager.getInstance().getSpawnTerritory(territoryName));
			}
			// Register AI Data for this spawn
			NpcPersonalAIData.getInstance().storeData(spawnDat, AIData);
			switch (spawnInfo.getInt("periodOfDay", 0))
			{
				case 0: // default
					ret += spawnDat.init();
					break;
				case 1: // Day
					DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
					ret = 1;
					break;
				case 2: // Night
					DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
					ret = 1;
					break;
			}
			
			addSpawn(spawnDat);
		}
		catch (Exception e)
		{
			LOG.warn("{}: Spawn could not be initialized!", getClass().getSimpleName(), e);
		}
		return ret;
	}
	
	/**
	 * Wrapper for {@link #addSpawn(StatsSet, Map)}.
	 * @param spawnInfo StatsSet of spawn parameters
	 * @return count NPC instances, spawned by this spawn
	 */
	private int addSpawn(StatsSet spawnInfo)
	{
		return addSpawn(spawnInfo, null);
	}
	
	/**
	 * Gets the spawn data.
	 * @return the spawn data
	 */
	public Map<Integer, Set<L2Spawn>> getSpawnTable()
	{
		return _spawnTable;
	}
	
	/**
	 * Gets the spawns for the NPC Id.
	 * @param npcId the NPC Id
	 * @return the spawn set for the given npcId
	 */
	public Set<L2Spawn> getSpawns(int npcId)
	{
		return _spawnTable.getOrDefault(npcId, Collections.emptySet());
	}
	
	/**
	 * Gets the spawn count for the given NPC ID.
	 * @param npcId the NPC Id
	 * @return the spawn count
	 */
	public int getSpawnCount(int npcId)
	{
		return getSpawns(npcId).size();
	}
	
	/**
	 * Finds a spawn for the given NPC ID.
	 * @param npcId the NPC Id
	 * @return a spawn for the given NPC ID or {@code null}
	 */
	public L2Spawn findAny(int npcId)
	{
		return getSpawns(npcId).stream().findFirst().orElse(null);
	}
	
	/**
	 * Adds a new spawn to the spawn table.
	 * @param spawn the spawn to add
	 * @param storeInDb if {@code true} it'll be saved in the database
	 */
	public void addNewSpawn(L2Spawn spawn, boolean storeInDb)
	{
		addSpawn(spawn);
		
		if (storeInDb)
		{
			final String spawnTable = spawn.isCustom() && Config.CUSTOM_SPAWNLIST_TABLE ? "custom_spawnlist" : "spawnlist";
			try (Connection con = ConnectionFactory.getInstance().getConnection();
				PreparedStatement insert = con.prepareStatement("INSERT INTO " + spawnTable + "(count,npc_templateid,locx,locy,locz,heading,respawn_delay,respawn_random,loc_id) values(?,?,?,?,?,?,?,?,?)"))
			{
				insert.setInt(1, spawn.getAmount());
				insert.setInt(2, spawn.getId());
				insert.setInt(3, spawn.getX());
				insert.setInt(4, spawn.getY());
				insert.setInt(5, spawn.getZ());
				insert.setInt(6, spawn.getHeading());
				insert.setInt(7, spawn.getRespawnDelay() / 1000);
				insert.setInt(8, spawn.getRespawnMaxDelay() - spawn.getRespawnMinDelay());
				insert.setInt(9, spawn.getLocationId());
				insert.execute();
			}
			catch (Exception e)
			{
				LOG.warn("{}: Could not store spawn in the DB!", getClass().getSimpleName(), e);
			}
		}
	}
	
	/**
	 * Delete an spawn from the spawn table.
	 * @param spawn the spawn to delete
	 * @param updateDb if {@code true} database will be updated
	 */
	public void deleteSpawn(L2Spawn spawn, boolean updateDb)
	{
		if (!removeSpawn(spawn))
		{
			return;
		}
		
		if (updateDb)
		{
			try (Connection con = ConnectionFactory.getInstance().getConnection();
				PreparedStatement delete = con.prepareStatement("DELETE FROM " + (spawn.isCustom() ? "custom_spawnlist" : "spawnlist") + " WHERE locx=? AND locy=? AND locz=? AND npc_templateid=? AND heading=?"))
			{
				delete.setInt(1, spawn.getX());
				delete.setInt(2, spawn.getY());
				delete.setInt(3, spawn.getZ());
				delete.setInt(4, spawn.getId());
				delete.setInt(5, spawn.getHeading());
				delete.execute();
			}
			catch (Exception e)
			{
				LOG.warn("{}: Spawn {} could not be removed from DB!", getClass().getSimpleName(), spawn, e);
			}
		}
	}
	
	/**
	 * Add a spawn to the spawn set if present, otherwise add a spawn set and add the spawn to the newly created spawn set.
	 * @param spawn the NPC spawn to add
	 */
	private void addSpawn(L2Spawn spawn)
	{
		_spawnTable.computeIfAbsent(spawn.getId(), k -> ConcurrentHashMap.newKeySet(1)).add(spawn);
	}
	
	/**
	 * Remove a spawn from the spawn set, if the spawn set is empty, remove it as well.
	 * @param spawn the NPC spawn to remove
	 * @return {@code true} if the spawn was successfully removed, {@code false} otherwise
	 */
	private boolean removeSpawn(L2Spawn spawn)
	{
		final Set<L2Spawn> set = _spawnTable.get(spawn.getId());
		if (set != null)
		{
			boolean removed = set.remove(spawn);
			if (set.isEmpty())
			{
				_spawnTable.remove(spawn.getId());
			}
			return removed;
		}
		return false;
	}
	
	/**
	 * Execute a procedure over all spawns.<br>
	 * <font size="4" color="red">Do not use it!</font>
	 * @param function the function to execute
	 * @return {@code true} if all procedures were executed, {@code false} otherwise
	 */
	public boolean forEachSpawn(Function<L2Spawn, Boolean> function)
	{
		for (Set<L2Spawn> set : _spawnTable.values())
		{
			for (L2Spawn spawn : set)
			{
				if (!function.apply(spawn))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	public static SpawnTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SpawnTable _instance = new SpawnTable();
	}
}
