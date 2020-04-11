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
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastMap;
import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.Announcements;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.datatables.SpawnTable;
import l2r.gameserver.datatables.sql.NpcTable;
import l2r.gameserver.enums.RaidBossStatus;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.actor.instance.L2RaidBossInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.util.Rnd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.reunion.configsEngine.CustomServerConfigs;

/**
 * Raid Boss spawn manager.
 * @author godson
 */
public class RaidBossSpawnManager
{
	private static final Logger _log = LoggerFactory.getLogger(RaidBossSpawnManager.class);
	
	protected static final Map<Integer, L2RaidBossInstance> _bosses = new FastMap<>();
	protected static final Map<Integer, L2Spawn> _spawns = new FastMap<>();
	protected static final Map<Integer, StatsSet> _storedInfo = new FastMap<>();
	protected static final Map<Integer, ScheduledFuture<?>> _schedules = new FastMap<>();
	
	/**
	 * Instantiates a new raid boss spawn manager.
	 */
	protected RaidBossSpawnManager()
	{
		load();
	}
	
	/**
	 * Load.
	 */
	public void load()
	{
		_bosses.clear();
		_spawns.clear();
		_storedInfo.clear();
		_schedules.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM raidboss_spawnlist ORDER BY boss_id");
			ResultSet rset = statement.executeQuery())
		{
			L2Spawn spawnDat;
			L2NpcTemplate template;
			long respawnTime;
			while (rset.next())
			{
				template = getValidTemplate(rset.getInt("boss_id"));
				if (template != null)
				{
					spawnDat = new L2Spawn(template);
					spawnDat.setX(rset.getInt("loc_x"));
					spawnDat.setY(rset.getInt("loc_y"));
					spawnDat.setZ(rset.getInt("loc_z"));
					spawnDat.setAmount(rset.getInt("amount"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"), rset.getInt("respawn_random"));
					respawnTime = rset.getLong("respawn_time");
					
					addNewSpawn(spawnDat, respawnTime, rset.getDouble("currentHP"), rset.getDouble("currentMP"), false);
				}
				else
				{
					_log.warn(getClass().getSimpleName() + ": Could not load raidboss #" + rset.getInt("boss_id") + " from DB");
				}
			}
			
			_log.info(getClass().getSimpleName() + ": Loaded " + _bosses.size() + " Instances");
			_log.info(getClass().getSimpleName() + ": Scheduled " + _schedules.size() + " Instances");
		}
		catch (SQLException e)
		{
			_log.warn(getClass().getSimpleName() + ": Couldnt load raidboss_spawnlist table");
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": Error while initializing RaidBossSpawnManager: " + e.getMessage(), e);
		}
	}
	
	private static class SpawnSchedule implements Runnable
	{
		private static final Logger _log = LoggerFactory.getLogger(SpawnSchedule.class);
		
		private final int bossId;
		
		/**
		 * Instantiates a new spawn schedule.
		 * @param npcId the npc id
		 */
		public SpawnSchedule(int npcId)
		{
			bossId = npcId;
		}
		
		@Override
		public void run()
		{
			L2RaidBossInstance raidboss = null;
			
			if (bossId == 25328)
			{
				raidboss = DayNightSpawnManager.getInstance().handleBoss(_spawns.get(bossId));
			}
			else
			{
				raidboss = (L2RaidBossInstance) _spawns.get(bossId).doSpawn();
			}
			
			if (raidboss != null)
			{
				raidboss.setRaidStatus(RaidBossStatus.ALIVE);
				
				final StatsSet info = new StatsSet();
				info.set("currentHP", raidboss.getCurrentHp());
				info.set("currentMP", raidboss.getCurrentMp());
				info.set("respawnTime", 0L);
				
				_storedInfo.put(bossId, info);
				
				_log.info(getClass().getSimpleName() + ": Spawning Raid Boss " + raidboss.getName());
				
				_bosses.put(bossId, raidboss);
			}
			
			_schedules.remove(bossId);
			
			if ((raidboss != null) && CustomServerConfigs.ANNOUNCE_DEATH_REVIVE_OF_RAIDS)
			{
				Announcements.getInstance().handleAnnounce("RaidBoss Manager: " + raidboss.getName() + " has spawned!", 0, true);
			}
		}
	}
	
	/**
	 * Update status.
	 * @param boss the boss
	 * @param isBossDead the is boss dead
	 */
	public void updateStatus(L2RaidBossInstance boss, boolean isBossDead)
	{
		if (!_storedInfo.containsKey(boss.getId()))
		{
			return;
		}
		
		final StatsSet info = _storedInfo.get(boss.getId());
		
		if (isBossDead)
		{
			boss.setRaidStatus(RaidBossStatus.DEAD);
			
			final int respawnMinDelay = (int) (boss.getSpawn().getRespawnMinDelay() * Config.RAID_MIN_RESPAWN_MULTIPLIER);
			final int respawnMaxDelay = (int) (boss.getSpawn().getRespawnMaxDelay() * Config.RAID_MAX_RESPAWN_MULTIPLIER);
			final int respawnDelay = Rnd.get(respawnMinDelay, respawnMaxDelay);
			final long respawnTime = Calendar.getInstance().getTimeInMillis() + respawnDelay;
			
			info.set("currentHP", boss.getMaxHp());
			info.set("currentMP", boss.getMaxMp());
			info.set("respawnTime", respawnTime);
			
			if (!_schedules.containsKey(boss.getId()) && ((respawnMinDelay > 0) || (respawnMaxDelay > 0)))
			{
				final Calendar time = Calendar.getInstance();
				time.setTimeInMillis(respawnTime);
				_log.info(getClass().getSimpleName() + ": Updated " + boss.getName() + " respawn time to " + time.getTime());
				
				_schedules.put(boss.getId(), ThreadPoolManager.getInstance().scheduleGeneral(new SpawnSchedule(boss.getId()), respawnDelay));
				updateDb();
			}
		}
		else
		{
			boss.setRaidStatus(RaidBossStatus.ALIVE);
			
			info.set("currentHP", boss.getCurrentHp());
			info.set("currentMP", boss.getCurrentMp());
			info.set("respawnTime", 0L);
		}
		_storedInfo.put(boss.getId(), info);
	}
	
	/**
	 * Adds the new spawn.
	 * @param spawnDat the spawn dat
	 * @param respawnTime the respawn time
	 * @param currentHP the current hp
	 * @param currentMP the current mp
	 * @param storeInDb the store in db
	 */
	public void addNewSpawn(L2Spawn spawnDat, long respawnTime, double currentHP, double currentMP, boolean storeInDb)
	{
		if (spawnDat == null)
		{
			return;
		}
		if (_spawns.containsKey(spawnDat.getId()))
		{
			return;
		}
		
		final int bossId = spawnDat.getId();
		final long time = Calendar.getInstance().getTimeInMillis();
		
		SpawnTable.getInstance().addNewSpawn(spawnDat, false);
		
		if ((respawnTime == 0L) || (time > respawnTime))
		{
			L2RaidBossInstance raidboss = null;
			
			if (bossId == 25328)
			{
				raidboss = DayNightSpawnManager.getInstance().handleBoss(spawnDat);
			}
			else
			{
				raidboss = (L2RaidBossInstance) spawnDat.doSpawn();
			}
			
			if (raidboss != null)
			{
				raidboss.setCurrentHp(currentHP);
				raidboss.setCurrentMp(currentMP);
				raidboss.setRaidStatus(RaidBossStatus.ALIVE);
				
				_bosses.put(bossId, raidboss);
				
				final StatsSet info = new StatsSet();
				info.set("currentHP", currentHP);
				info.set("currentMP", currentMP);
				info.set("respawnTime", 0L);
				
				_storedInfo.put(bossId, info);
			}
		}
		else
		{
			final long spawnTime = respawnTime - Calendar.getInstance().getTimeInMillis();
			_schedules.put(bossId, ThreadPoolManager.getInstance().scheduleGeneral(new SpawnSchedule(bossId), spawnTime));
		}
		
		_spawns.put(bossId, spawnDat);
		
		if (storeInDb)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("INSERT INTO raidboss_spawnlist (boss_id,amount,loc_x,loc_y,loc_z,heading,respawn_time,currentHp,currentMp) VALUES(?,?,?,?,?,?,?,?,?)"))
			{
				statement.setInt(1, spawnDat.getId());
				statement.setInt(2, spawnDat.getAmount());
				statement.setInt(3, spawnDat.getX());
				statement.setInt(4, spawnDat.getY());
				statement.setInt(5, spawnDat.getZ());
				statement.setInt(6, spawnDat.getHeading());
				statement.setLong(7, respawnTime);
				statement.setDouble(8, currentHP);
				statement.setDouble(9, currentMP);
				statement.execute();
			}
			catch (Exception e)
			{
				// problem with storing spawn
				_log.warn(getClass().getSimpleName() + ": Could not store raidboss #" + bossId + " in the DB:" + e.getMessage(), e);
			}
		}
	}
	
	/**
	 * Delete spawn.
	 * @param spawnDat the spawn dat
	 * @param updateDb the update db
	 */
	public void deleteSpawn(L2Spawn spawnDat, boolean updateDb)
	{
		if (spawnDat == null)
		{
			return;
		}
		
		final int bossId = spawnDat.getId();
		if (!_spawns.containsKey(bossId))
		{
			return;
		}
		
		SpawnTable.getInstance().deleteSpawn(spawnDat, false);
		_spawns.remove(bossId);
		
		if (_bosses.containsKey(bossId))
		{
			_bosses.remove(bossId);
		}
		
		if (_schedules.containsKey(bossId))
		{
			final ScheduledFuture<?> f = _schedules.remove(bossId);
			f.cancel(true);
		}
		
		if (_storedInfo.containsKey(bossId))
		{
			_storedInfo.remove(bossId);
		}
		
		if (updateDb)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("DELETE FROM raidboss_spawnlist WHERE boss_id=?"))
			{
				statement.setInt(1, bossId);
				statement.execute();
			}
			catch (Exception e)
			{
				// problem with deleting spawn
				_log.warn(getClass().getSimpleName() + ": Could not remove raidboss #" + bossId + " from DB: " + e.getMessage(), e);
			}
		}
	}
	
	/**
	 * Update database.
	 */
	private void updateDb()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE raidboss_spawnlist SET respawn_time = ?, currentHP = ?, currentMP = ? WHERE boss_id = ?"))
		{
			for (Integer bossId : _storedInfo.keySet())
			{
				if (bossId == null)
				{
					continue;
				}
				
				L2RaidBossInstance boss = _bosses.get(bossId);
				
				if (boss == null)
				{
					continue;
				}
				
				if (boss.getRaidStatus().equals(RaidBossStatus.ALIVE))
				{
					updateStatus(boss, false);
				}
				
				StatsSet info = _storedInfo.get(bossId);
				
				if (info == null)
				{
					continue;
				}
				
				try
				{
					statement.setLong(1, info.getLong("respawnTime"));
					statement.setDouble(2, info.getDouble("currentHP"));
					statement.setDouble(3, info.getDouble("currentMP"));
					statement.setInt(4, bossId);
					statement.executeUpdate();
					statement.clearParameters();
				}
				catch (SQLException e)
				{
					_log.warn(getClass().getSimpleName() + ": Couldnt update raidboss_spawnlist table " + e.getMessage(), e);
				}
			}
		}
		catch (SQLException e)
		{
			_log.warn(getClass().getSimpleName() + ": SQL error while updating RaidBoss spawn to database: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Gets the all raid boss status.
	 * @return the all raid boss status
	 */
	public String[] getAllRaidBossStatus()
	{
		final String[] msg = new String[(_bosses == null) ? 0 : _bosses.size()];
		
		if (_bosses == null)
		{
			msg[0] = "None";
			return msg;
		}
		
		int index = 0;
		
		for (int i : _bosses.keySet())
		{
			L2RaidBossInstance boss = _bosses.get(i);
			
			msg[index++] = boss.getName() + ": " + boss.getRaidStatus().name();
		}
		
		return msg;
	}
	
	/**
	 * Gets the raid boss status.
	 * @param bossId the boss id
	 * @return the raid boss status
	 */
	public String getRaidBossStatus(int bossId)
	{
		String msg = "RaidBoss Status..." + Config.EOL;
		
		if (_bosses == null)
		{
			msg += "None";
			return msg;
		}
		
		if (_bosses.containsKey(bossId))
		{
			final L2RaidBossInstance boss = _bosses.get(bossId);
			
			msg += boss.getName() + ": " + boss.getRaidStatus().name();
		}
		
		return msg;
	}
	
	/**
	 * Gets the raid boss status id.
	 * @param bossId the boss id
	 * @return the raid boss status id
	 */
	public RaidBossStatus getRaidBossStatusId(int bossId)
	{
		if (_bosses.containsKey(bossId))
		{
			return _bosses.get(bossId).getRaidStatus();
		}
		else if (_schedules.containsKey(bossId))
		{
			return RaidBossStatus.DEAD;
		}
		else
		{
			return RaidBossStatus.UNDEFINED;
		}
	}
	
	/**
	 * Gets the valid template.
	 * @param bossId the boss id
	 * @return the valid template
	 */
	public L2NpcTemplate getValidTemplate(int bossId)
	{
		final L2NpcTemplate template = NpcTable.getInstance().getTemplate(bossId);
		if (template == null)
		{
			return null;
		}
		if (!template.isType("L2RaidBoss"))
		{
			return null;
		}
		return template;
	}
	
	/**
	 * Notify spawn night boss.
	 * @param raidboss the raidboss
	 */
	public void notifySpawnNightBoss(L2RaidBossInstance raidboss)
	{
		final StatsSet info = new StatsSet();
		info.set("currentHP", raidboss.getCurrentHp());
		info.set("currentMP", raidboss.getCurrentMp());
		info.set("respawnTime", 0L);
		
		raidboss.setRaidStatus(RaidBossStatus.ALIVE);
		
		_storedInfo.put(raidboss.getId(), info);
		
		_log.info(getClass().getSimpleName() + ": Spawning Night Raid Boss " + raidboss.getName());
		
		_bosses.put(raidboss.getId(), raidboss);
	}
	
	/**
	 * Checks if the boss is defined.
	 * @param bossId the boss id
	 * @return {@code true} if is defined
	 */
	public boolean isDefined(int bossId)
	{
		return _spawns.containsKey(bossId);
	}
	
	/**
	 * Gets the bosses.
	 * @return the bosses
	 */
	public Map<Integer, L2RaidBossInstance> getBosses()
	{
		return _bosses;
	}
	
	/**
	 * Gets the spawns.
	 * @return the spawns
	 */
	public Map<Integer, L2Spawn> getSpawns()
	{
		return _spawns;
	}
	
	/**
	 * Gets the stored info.
	 * @return the stored info
	 */
	public Map<Integer, StatsSet> getStoredInfo()
	{
		return _storedInfo;
	}
	
	/**
	 * Saves and clears the raid bosses status, including all schedules.
	 */
	public void cleanUp()
	{
		updateDb();
		
		_bosses.clear();
		
		if (_schedules != null)
		{
			for (Integer bossId : _schedules.keySet())
			{
				ScheduledFuture<?> f = _schedules.get(bossId);
				f.cancel(true);
			}
			_schedules.clear();
		}
		
		_storedInfo.clear();
		_spawns.clear();
	}
	
	/**
	 * Gets the single instance of RaidBossSpawnManager.
	 * @return single instance of RaidBossSpawnManager
	 */
	public static RaidBossSpawnManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final RaidBossSpawnManager _instance = new RaidBossSpawnManager();
	}
}
