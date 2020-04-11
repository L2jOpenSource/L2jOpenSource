package com.l2jfrozen.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.GmListTable;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.datatables.sql.SpawnTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.skills.Stats;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.logs.Log;
import com.l2jfrozen.util.database.L2DatabaseFactory;
import com.l2jfrozen.util.random.Rnd;

/**
 * @author godson
 * @author ReynalDev
 */
public class RaidBossSpawnManager
{
	static Logger LOGGER = Logger.getLogger(RaidBossSpawnManager.class);
	
	protected static Map<Integer, L2RaidBossInstance> bosses = new HashMap<>();
	protected static Map<Integer, L2Spawn> spawns = new HashMap<>();
	protected static Map<Integer, StatsSet> storedInfo = new HashMap<>();
	protected static Map<Integer, ScheduledFuture<?>> schedules = new HashMap<>();
	
	private static final String SELECT_RAIDBOSSES = "SELECT boss_id,amount,loc_x,loc_y,loc_z,heading,respawn_min_delay,respawn_max_delay,respawn_time,currentHp,currentMp FROM raidboss_spawnlist ORDER BY boss_id";
	private static final String INSERT_RAIDBOSS = "INSERT INTO raidboss_spawnlist (boss_id,amount,loc_x,loc_y,loc_z,heading,respawn_time,currentHp,currentMp) VALUES(?,?,?,?,?,?,?,?,?)";
	private static final String DELETE_RAIDBOSS_BY_BOSS_ID = "DELETE FROM raidboss_spawnlist WHERE boss_id=?";
	private static final String UPDATE_RAIDBOSS_BY_BOSS_ID = "UPDATE raidboss_spawnlist SET respawn_time=?, currentHP=?, currentMP=? WHERE boss_id=?";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	
	public static enum StatusEnum
	{
		ALIVE,
		DEAD,
		UNDEFINED
	}
	
	public RaidBossSpawnManager()
	{
		init();
	}
	
	public static RaidBossSpawnManager getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private void init()
	{
		bosses.clear();
		schedules.clear();
		storedInfo.clear();
		spawns.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_RAIDBOSSES);
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				L2NpcTemplate template = getValidTemplate(rset.getInt("boss_id"));
				
				if (template != null)
				{
					L2Spawn spawnDat = new L2Spawn(template);
					spawnDat.setLocx(rset.getInt("loc_x"));
					spawnDat.setLocy(rset.getInt("loc_y"));
					spawnDat.setLocz(rset.getInt("loc_z"));
					spawnDat.setAmount(rset.getInt("amount"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnMinDelay(rset.getInt("respawn_min_delay"));
					spawnDat.setRespawnMaxDelay(rset.getInt("respawn_max_delay"));
					long respawnTime = rset.getLong("respawn_time");
					
					StatsSet info = new StatsSet();
					info.set("respawnTime", respawnTime);
					storedInfo.put(rset.getInt("boss_id"), info);
					
					addNewSpawn(spawnDat, respawnTime, rset.getDouble("currentHP"), rset.getDouble("currentMP"), false);
				}
				else
				{
					LOGGER.warn("RaidBossSpawnManager: NPC RaidBoss ID " + rset.getInt("boss_id") + " it does no exist in 'npc' or 'custom_npc' table ");
				}
			}
			
			LOGGER.info("RaidBossSpawnManager: Loaded " + bosses.size() + " Instances");
			LOGGER.info("RaidBossSpawnManager: Scheduled " + schedules.size() + " Instances");
		}
		catch (Exception e)
		{
			LOGGER.error("RaidBossSpawnManager.init: Could not load raidboss_spawnlist table", e);
		}
	}
	
	private class SpawnSchedule implements Runnable
	{
		private int bossId;
		
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
				raidboss = DayNightSpawnManager.getInstance().handleBoss(spawns.get(bossId));
			}
			else
			{
				raidboss = (L2RaidBossInstance) spawns.get(bossId).doSpawn();
			}
			
			if (raidboss != null)
			{
				raidboss.setRaidStatus(StatusEnum.ALIVE);
				
				StatsSet info = new StatsSet();
				info.set("currentHP", raidboss.getCurrentHp());
				info.set("currentMP", raidboss.getCurrentMp());
				info.set("respawnTime", 0L);
				
				storedInfo.put(bossId, info);
				
				String text = "Spawning Raid Boss " + raidboss.getName() + " (" + raidboss.getNpcId() + ").";
				GmListTable.broadcastMessageToGMs(text);
				LOGGER.info(text);
				
				if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
				{
					Announcements.getInstance().announceToAll("Raid boss " + raidboss.getName() + " spawned in world.");
				}
				
				bosses.put(bossId, raidboss);
				
				String textLog = raidboss.getName() + "(" + raidboss.getNpcId() + ") spawned in the word";
				Log.add(textLog, "RaidBossSpawnManager");
			}
			
			schedules.remove(bossId);
			
			// To update immediately the database, used for website to show up RaidBoss status.
			updateRaidBossInDb(bossId);
		}
	}
	
	public void updateStatus(L2RaidBossInstance boss, boolean isBossDead)
	{
		if (!storedInfo.containsKey(boss.getNpcId()))
		{
			return;
		}
		
		StatsSet info = storedInfo.get(boss.getNpcId());
		
		if (isBossDead)
		{
			boss.setRaidStatus(StatusEnum.DEAD);
			
			long respawnTime;
			int respawnMinDelay = boss.getSpawn().getRespawnMinDelay();
			int respawnMaxDelay = boss.getSpawn().getRespawnMaxDelay();
			long respawnDelay = Rnd.get((int) (respawnMinDelay * 1000 * Config.RAID_MIN_RESPAWN_MULTIPLIER), (int) (respawnMaxDelay * 1000 * Config.RAID_MAX_RESPAWN_MULTIPLIER));
			respawnTime = System.currentTimeMillis() + respawnDelay;
			
			info.set("currentHP", boss.getMaxHp());
			info.set("currentMP", boss.getMaxMp());
			info.set("respawnTime", respawnTime);
			
			String text = boss.getName() + "(" + boss.getNpcId() + ") defeated, next respawn at " + DATE_FORMAT.format(respawnTime);
			LOGGER.info(text);
			Log.add(text, "RaidBossSpawnManager");
			
			ScheduledFuture<?> futureSpawn = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnSchedule(boss.getNpcId()), respawnDelay);
			schedules.put(boss.getNpcId(), futureSpawn);
			
			// To update immediately the database, used for website to show up RaidBoss status.
			updateRaidBossInDb(boss.getNpcId());
		}
		else
		{
			boss.setRaidStatus(StatusEnum.ALIVE);
			
			info.set("currentHP", boss.getCurrentHp());
			info.set("currentMP", boss.getCurrentMp());
			info.set("respawnTime", 0L);
		}
		
		storedInfo.put(boss.getNpcId(), info);
	}
	
	public void addNewSpawn(L2Spawn spawnDat, long respawnTime, double currentHP, double currentMP, boolean storeInDb)
	{
		if (spawnDat == null)
		{
			return;
		}
		
		if (spawns.containsKey(spawnDat.getNpcid()))
		{
			return;
		}
		
		int bossId = spawnDat.getNpcid();
		long time = System.currentTimeMillis();
		
		SpawnTable.getInstance().addNewSpawn(spawnDat, false);
		
		if (respawnTime == 0L || time > respawnTime)
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
				double bonus = raidboss.getStat().calcStat(Stats.MAX_HP, 1, raidboss, null);
				
				if (Config.DEBUG)
				{
					LOGGER.info(" bossId: " + bossId);
					LOGGER.info(" 	maxHp: " + raidboss.getMaxHp());
					LOGGER.info(" 	currHp: " + (int) currentHP);
					LOGGER.info(" 	bonusHp: " + bonus);
					LOGGER.info(" 	calculatedHp: " + (int) (bonus * currentHP));
				}
				
				// if new spawn, the currentHp is equal to maxHP/bonus, so set it to max
				if ((int) (bonus * currentHP) == raidboss.getMaxHp())
				{
					currentHP = (raidboss.getMaxHp());
				}
				
				raidboss.setCurrentHp(currentHP);
				raidboss.setCurrentMp(currentMP);
				raidboss.setRaidStatus(StatusEnum.ALIVE);
				
				bosses.put(bossId, raidboss);
				
				StatsSet info = new StatsSet();
				info.set("currentHP", currentHP);
				info.set("currentMP", currentMP);
				info.set("respawnTime", 0L);
				
				storedInfo.put(bossId, info);
			}
		}
		else
		{
			long spawnTime = respawnTime - System.currentTimeMillis();
			
			ScheduledFuture<?> futureSpawn = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnSchedule(bossId), spawnTime);
			schedules.put(bossId, futureSpawn);
		}
		
		spawns.put(bossId, spawnDat);
		
		if (storeInDb)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(INSERT_RAIDBOSS))
			{
				statement.setInt(1, spawnDat.getNpcid());
				statement.setInt(2, spawnDat.getAmount());
				statement.setInt(3, spawnDat.getLocx());
				statement.setInt(4, spawnDat.getLocy());
				statement.setInt(5, spawnDat.getLocz());
				statement.setInt(6, spawnDat.getHeading());
				statement.setLong(7, respawnTime);
				statement.setDouble(8, currentHP);
				statement.setDouble(9, currentMP);
				statement.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.error("RaidBossSpawnManager: Could not insert raidboss ID" + bossId + " in raidboss_spawnlist table ", e);
			}
		}
	}
	
	public void deleteSpawn(L2Spawn spawnDat, boolean updateDb)
	{
		if (spawnDat == null)
		{
			return;
		}
		
		if (!spawns.containsKey(spawnDat.getNpcid()))
		{
			return;
		}
		
		int bossId = spawnDat.getNpcid();
		
		SpawnTable.getInstance().deleteSpawn(spawnDat, false);
		spawns.remove(bossId);
		
		if (bosses.containsKey(bossId))
		{
			bosses.remove(bossId);
		}
		
		if (schedules.containsKey(bossId))
		{
			ScheduledFuture<?> f = schedules.get(bossId);
			f.cancel(true);
			schedules.remove(bossId);
		}
		
		if (storedInfo.containsKey(bossId))
		{
			storedInfo.remove(bossId);
		}
		
		if (updateDb)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(DELETE_RAIDBOSS_BY_BOSS_ID))
			{
				statement.setInt(1, bossId);
				statement.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.error("RaidBossSpawnManager: Could not delete raidboss ID " + bossId + " from raidboss_spawnlist table ", e);
			}
		}
	}
	
	/**
	 * This method save ONE Raid Boss record in the database
	 * @param raidBossId : <B>ID</B> of the raid boss to save in database
	 */
	public void updateRaidBossInDb(int raidBossId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			StatsSet info = storedInfo.get(raidBossId);
			if (info != null)
			{
				try (PreparedStatement statement = con.prepareStatement(UPDATE_RAIDBOSS_BY_BOSS_ID))
				{
					statement.setLong(1, info.getLong("respawnTime"));
					statement.setDouble(2, info.getDouble("currentHP"));
					statement.setDouble(3, info.getDouble("currentMP"));
					statement.setInt(4, raidBossId);
					statement.executeUpdate();
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.error("RaidBossSpawnManager.updateRaidBossInDb: Could not update RaidBoss with ID " + raidBossId, e);
		}
	}
	
	/**
	 * This method save all Raid Bosses when server is shutdown or restart
	 */
	public void updateDb()
	{
		int count = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_RAIDBOSS_BY_BOSS_ID))
		{
			for (int bossId : storedInfo.keySet())
			{
				L2RaidBossInstance boss = bosses.get(bossId);
				if (boss != null)
				{
					StatsSet info = storedInfo.get(bossId);
					if (info != null)
					{
						ps.setLong(1, info.getLong("respawnTime"));
						ps.setDouble(2, info.getDouble("currentHP"));
						ps.setDouble(3, info.getDouble("currentMP"));
						ps.setInt(4, bossId);
						ps.addBatch();
						count++;
					}
				}
			}
			
			if (count > 0)
			{
				ps.executeBatch();
			}
		}
		catch (Exception e)
		{
			LOGGER.error("RaidBossSpawnManager.updateDb : Could not save RaidBoss data in raidboss_spawnlist table", e);
		}
	}
	
	public String[] getAllRaidBossStatus()
	{
		String[] msg = new String[bosses == null ? 0 : bosses.size()];
		
		if (bosses == null)
		{
			msg[0] = "None";
			return msg;
		}
		
		int index = 0;
		
		for (int i : bosses.keySet())
		{
			L2RaidBossInstance boss = bosses.get(i);
			
			msg[index] = boss.getName() + ": " + boss.getRaidStatus().name();
			index++;
		}
		
		return msg;
	}
	
	public String getRaidBossStatus(int bossId)
	{
		String msg = "RaidBoss Status....\n";
		
		if (bosses == null)
		{
			msg += "None";
			return msg;
		}
		
		if (bosses.containsKey(bossId))
		{
			L2RaidBossInstance boss = bosses.get(bossId);
			msg += boss.getName() + ": " + boss.getRaidStatus().name();
		}
		
		return msg;
	}
	
	public StatusEnum getRaidBossStatusId(int bossId)
	{
		if (bosses.containsKey(bossId))
		{
			return bosses.get(bossId).getRaidStatus();
		}
		else if (schedules.containsKey(bossId))
		{
			return StatusEnum.DEAD;
		}
		else
		{
			return StatusEnum.UNDEFINED;
		}
	}
	
	public L2NpcTemplate getValidTemplate(int bossId)
	{
		L2NpcTemplate template = NpcTable.getInstance().getTemplate(bossId);
		
		if (template == null)
		{
			return null;
		}
		
		if (!template.type.equalsIgnoreCase("L2RaidBoss"))
		{
			return null;
		}
		
		return template;
	}
	
	public void notifySpawnNightBoss(L2RaidBossInstance raidboss)
	{
		StatsSet info = new StatsSet();
		info.set("currentHP", raidboss.getCurrentHp());
		info.set("currentMP", raidboss.getCurrentMp());
		info.set("respawnTime", 0L);
		
		raidboss.setRaidStatus(StatusEnum.ALIVE);
		
		storedInfo.put(raidboss.getNpcId(), info);
		
		GmListTable.broadcastMessageToGMs("Spawning Raid Boss " + raidboss.getName());
		
		bosses.put(raidboss.getNpcId(), raidboss);
	}
	
	public boolean isDefined(int bossId)
	{
		return spawns.containsKey(bossId);
	}
	
	public Map<Integer, L2RaidBossInstance> getBosses()
	{
		return bosses;
	}
	
	public Map<Integer, L2Spawn> getSpawns()
	{
		return spawns;
	}
	
	public void reloadBosses()
	{
		init();
	}
	
	/**
	 * Saves all raidboss status and then clears all info from memory, including all schedules.
	 */
	public void cleanUp()
	{
		updateDb();
		
		bosses.clear();
		
		if (schedules != null)
		{
			for (Integer bossId : schedules.keySet())
			{
				ScheduledFuture<?> f = schedules.get(bossId);
				f.cancel(true);
			}
			schedules.clear();
		}
		
		storedInfo.clear();
		spawns.clear();
	}
	
	public StatsSet getStatsSet(int bossId)
	{
		return storedInfo.get(bossId);
	}
	
	public L2RaidBossInstance getBoss(int bossId)
	{
		return bosses.get(bossId);
	}
	
	private static class SingletonHolder
	{
		protected static RaidBossSpawnManager instance = new RaidBossSpawnManager();
	}
}
