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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2r.L2DatabaseFactory;
import l2r.gameserver.Announcements;
import l2r.gameserver.datatables.sql.NpcTable;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2GrandBossInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.zone.type.L2BossZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.reunion.configsEngine.CustomServerConfigs;

/**
 * @author DaRkRaGe Revised by Emperorc
 */
public class GrandBossManager
{
	// SQL queries
	private static final String DELETE_GRAND_BOSS_LIST = "DELETE FROM grandboss_list";
	private static final String INSERT_GRAND_BOSS_LIST = "INSERT INTO grandboss_list (player_id,zone) VALUES (?,?)";
	private static final String UPDATE_GRAND_BOSS_DATA = "UPDATE grandboss_data set loc_x = ?, loc_y = ?, loc_z = ?, heading = ?, respawn_time = ?, currentHP = ?, currentMP = ?, status = ? where boss_id = ?";
	private static final String UPDATE_GRAND_BOSS_DATA2 = "UPDATE grandboss_data set status = ? where boss_id = ?";
	
	protected static Logger _log = LoggerFactory.getLogger(GrandBossManager.class);
	
	protected static Map<Integer, L2GrandBossInstance> _bosses = new FastMap<>();
	
	protected static Map<Integer, StatsSet> _storedInfo = new HashMap<>();
	
	private final Map<Integer, Integer> _bossStatus = new HashMap<>();
	
	private final List<L2BossZone> _zones = new FastList<>();
	
	protected GrandBossManager()
	{
		init();
	}
	
	private void init()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT * from grandboss_data ORDER BY boss_id"))
		{
			while (rs.next())
			{
				// Read all info from DB, and store it for AI to read and decide what to do
				// faster than accessing DB in real time
				StatsSet info = new StatsSet();
				int bossId = rs.getInt("boss_id");
				info.set("loc_x", rs.getInt("loc_x"));
				info.set("loc_y", rs.getInt("loc_y"));
				info.set("loc_z", rs.getInt("loc_z"));
				info.set("heading", rs.getInt("heading"));
				info.set("respawn_time", rs.getLong("respawn_time"));
				double HP = rs.getDouble("currentHP"); // jython doesn't recognize doubles
				int true_HP = (int) HP; // so use java's ability to type cast
				info.set("currentHP", true_HP); // to convert double to int
				double MP = rs.getDouble("currentMP");
				int true_MP = (int) MP;
				info.set("currentMP", true_MP);
				int status = rs.getInt("status");
				_bossStatus.put(bossId, status);
				_storedInfo.put(bossId, info);
				_log.info(getClass().getSimpleName() + ": " + NpcTable.getInstance().getTemplate(bossId).getName() + "(" + bossId + ") status is " + status + ".");
				if (status > 0)
				{
					_log.info(getClass().getSimpleName() + ": Next spawn date of " + NpcTable.getInstance().getTemplate(bossId).getName() + " is " + new Date(info.getLong("respawn_time")) + ".");
				}
			}
			_log.info(getClass().getSimpleName() + ": Loaded " + _storedInfo.size() + " Instances");
		}
		catch (SQLException e)
		{
			_log.warn(getClass().getSimpleName() + ": Could not load grandboss_data table: " + e.getMessage(), e);
		}
		catch (Exception e)
		{
			_log.warn("Error while initializing GrandBossManager: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Zone Functions
	 */
	public void initZones()
	{
		Map<Integer, List<Integer>> zones = new HashMap<>();
		
		if (_zones == null)
		{
			_log.warn(getClass().getSimpleName() + ": Could not read Grand Boss zone data");
			return;
		}
		
		for (L2BossZone zone : _zones)
		{
			if (zone == null)
			{
				continue;
			}
			zones.put(zone.getId(), new ArrayList<>());
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT * from grandboss_list ORDER BY player_id"))
		{
			while (rs.next())
			{
				int id = rs.getInt("player_id");
				int zone_id = rs.getInt("zone");
				zones.get(zone_id).add(id);
			}
			_log.info(getClass().getSimpleName() + ": Initialized " + _zones.size() + " Grand Boss Zones");
		}
		catch (SQLException e)
		{
			_log.warn(getClass().getSimpleName() + ": Could not load grandboss_list table: " + e.getMessage(), e);
		}
		catch (Exception e)
		{
			_log.warn("Error while initializing GrandBoss zones: " + e.getMessage(), e);
		}
		
		for (L2BossZone zone : _zones)
		{
			if (zone == null)
			{
				continue;
			}
			zone.setAllowedPlayers(zones.get(zone.getId()));
		}
		
		zones.clear();
	}
	
	public void addZone(L2BossZone zone)
	{
		if (_zones != null)
		{
			_zones.add(zone);
		}
	}
	
	public final L2BossZone getZone(int zoneId)
	{
		if (_zones != null)
		{
			for (L2BossZone temp : _zones)
			{
				if (temp.getId() == zoneId)
				{
					return temp;
				}
			}
		}
		return null;
	}
	
	public final L2BossZone getZone(L2Character character)
	{
		if (_zones != null)
		{
			for (L2BossZone temp : _zones)
			{
				if (temp.isCharacterInZone(character))
				{
					return temp;
				}
			}
		}
		return null;
	}
	
	public final L2BossZone getZone(Location loc)
	{
		return getZone(loc.getX(), loc.getY(), loc.getZ());
	}
	
	public final L2BossZone getZone(int x, int y, int z)
	{
		if (_zones != null)
		{
			for (L2BossZone temp : _zones)
			{
				if (temp.isInsideZone(x, y, z))
				{
					return temp;
				}
			}
		}
		return null;
	}
	
	public boolean checkIfInZone(String zoneType, L2Object obj)
	{
		L2BossZone temp = getZone(obj.getX(), obj.getY(), obj.getZ());
		if (temp == null)
		{
			return false;
		}
		
		return temp.getName().equalsIgnoreCase(zoneType);
	}
	
	public boolean checkIfInZone(L2PcInstance player)
	{
		if (player == null)
		{
			return false;
		}
		L2BossZone temp = getZone(player.getX(), player.getY(), player.getZ());
		if (temp == null)
		{
			return false;
		}
		
		return true;
	}
	
	public int getBossStatus(int bossId)
	{
		return _bossStatus.get(bossId);
	}
	
	public void setBossStatus(int bossId, int status)
	{
		_bossStatus.put(bossId, status);
		_log.info(getClass().getSimpleName() + ": Updated " + NpcTable.getInstance().getTemplate(bossId).getName() + "(" + bossId + ") status to " + status);
		updateDb(bossId, true);
		
		if ((status == 0) && CustomServerConfigs.ANNOUNCE_DEATH_REVIVE_OF_RAIDS)
		{
			Announcements.getInstance().handleAnnounce("RaidBoss Manager: " + NpcTable.getInstance().getTemplate(bossId).getName() + " has spawned!", 0, true);
		}
	}
	
	/**
	 * Adds a L2GrandBossInstance to the list of bosses.
	 * @param boss
	 */
	public void addBoss(L2GrandBossInstance boss)
	{
		if (boss != null)
		{
			_bosses.put(boss.getId(), boss);
		}
	}
	
	public L2GrandBossInstance getBoss(int bossId)
	{
		return _bosses.get(bossId);
	}
	
	public StatsSet getStatsSet(int bossId)
	{
		return _storedInfo.get(bossId);
	}
	
	public void setStatsSet(int bossId, StatsSet info)
	{
		_storedInfo.put(bossId, info);
		updateDb(bossId, false);
	}
	
	private void storeToDb()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement delete = con.prepareStatement(DELETE_GRAND_BOSS_LIST))
		{
			delete.executeUpdate();
			
			try (PreparedStatement insert = con.prepareStatement(INSERT_GRAND_BOSS_LIST))
			{
				for (L2BossZone zone : _zones)
				{
					if (zone == null)
					{
						continue;
					}
					Integer id = zone.getId();
					List<Integer> list = zone.getAllowedPlayers();
					if ((list == null) || list.isEmpty())
					{
						continue;
					}
					for (Integer player : list)
					{
						insert.setInt(1, player);
						insert.setInt(2, id);
						insert.executeUpdate();
						insert.clearParameters();
					}
				}
			}
			for (Entry<Integer, StatsSet> e : _storedInfo.entrySet())
			{
				final L2GrandBossInstance boss = _bosses.get(e.getKey());
				StatsSet info = e.getValue();
				if ((boss == null) || (info == null))
				{
					try (PreparedStatement update = con.prepareStatement(UPDATE_GRAND_BOSS_DATA2))
					{
						update.setInt(1, _bossStatus.get(e.getKey()));
						update.setInt(2, e.getKey());
						update.executeUpdate();
						update.clearParameters();
					}
				}
				else
				{
					try (PreparedStatement update = con.prepareStatement(UPDATE_GRAND_BOSS_DATA))
					{
						update.setInt(1, boss.getX());
						update.setInt(2, boss.getY());
						update.setInt(3, boss.getZ());
						update.setInt(4, boss.getHeading());
						update.setLong(5, info.getLong("respawn_time"));
						double hp = boss.getCurrentHp();
						double mp = boss.getCurrentMp();
						if (boss.isDead())
						{
							hp = boss.getMaxHp();
							mp = boss.getMaxMp();
						}
						update.setDouble(6, hp);
						update.setDouble(7, mp);
						update.setInt(8, _bossStatus.get(e.getKey()));
						update.setInt(9, e.getKey());
						update.executeUpdate();
						update.clearParameters();
					}
				}
			}
		}
		catch (SQLException e)
		{
			_log.warn(getClass().getSimpleName() + ": Couldn't store grandbosses to database:" + e.getMessage(), e);
		}
	}
	
	private void updateDb(int bossId, boolean statusOnly)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			L2GrandBossInstance boss = _bosses.get(bossId);
			StatsSet info = _storedInfo.get(bossId);
			
			if (statusOnly || (boss == null) || (info == null))
			{
				try (PreparedStatement ps = con.prepareStatement(UPDATE_GRAND_BOSS_DATA2))
				{
					ps.setInt(1, _bossStatus.get(bossId));
					ps.setInt(2, bossId);
					ps.executeUpdate();
				}
			}
			else
			{
				try (PreparedStatement ps = con.prepareStatement(UPDATE_GRAND_BOSS_DATA))
				{
					ps.setInt(1, boss.getX());
					ps.setInt(2, boss.getY());
					ps.setInt(3, boss.getZ());
					ps.setInt(4, boss.getHeading());
					ps.setLong(5, info.getLong("respawn_time"));
					double hp = boss.getCurrentHp();
					double mp = boss.getCurrentMp();
					if (boss.isDead())
					{
						hp = boss.getMaxHp();
						mp = boss.getMaxMp();
					}
					ps.setDouble(6, hp);
					ps.setDouble(7, mp);
					ps.setInt(8, _bossStatus.get(bossId));
					ps.setInt(9, bossId);
					ps.executeUpdate();
				}
			}
		}
		catch (SQLException e)
		{
			_log.warn(getClass().getSimpleName() + ": Couldn't update grandbosses to database:" + e.getMessage(), e);
		}
	}
	
	/**
	 * Saves all Grand Boss info and then clears all info from memory, including all schedules.
	 */
	public void cleanUp()
	{
		storeToDb();
		
		_bosses.clear();
		_storedInfo.clear();
		_bossStatus.clear();
		_zones.clear();
	}
	
	public List<L2BossZone> getZones()
	{
		return _zones;
	}
	
	public static GrandBossManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final GrandBossManager _instance = new GrandBossManager();
	}
}
