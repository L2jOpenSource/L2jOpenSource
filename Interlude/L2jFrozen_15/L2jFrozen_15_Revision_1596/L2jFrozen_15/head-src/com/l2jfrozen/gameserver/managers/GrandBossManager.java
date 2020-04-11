package com.l2jfrozen.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.datatables.xml.ZoneData;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;
import com.l2jfrozen.gameserver.model.zone.type.L2BossZone;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class handles all Grand Bosses:
 * <ul>
 * <li>22215-22217 Tyrannosaurus</li>
 * <li>25333-25338 Anakazel</li>
 * <li>29001 Queen Ant</li>
 * <li>29006 Core</li>
 * <li>29014 Orfen</li>
 * <li>29019 Antharas</li>
 * <li>29020 Baium</li>
 * <li>29022 Zaken</li>
 * <li>29028 Valakas</li>
 * <li>29045 Frintezza</li>
 * <li>29046-29047 Scarlet van Halisha</li>
 * </ul>
 * It handles the saving of hp, mp, location, and status of all Grand Bosses. It also manages the zones associated with the Grand Bosses. NOTE: The current version does NOT spawn the Grand Bosses, it just stores and retrieves the values on reboot/startup, for AI scripts to utilize as needed.
 * @author DaRkRaGe Revised by Emperorc
 */
public class GrandBossManager
{
	protected static final Logger LOGGER = Logger.getLogger(GrandBossManager.class);
	private static final String SELECT_GRAND_BOSS_DATA = "SELECT boss_id,loc_x,loc_y,loc_z,heading,respawn_time,currentHP,currentMP,status FROM grandboss_data ORDER BY boss_id";
	private static final String DELETE_GRAND_BOSS_LIST = "DELETE FROM grandboss_list";
	private static final String INSERT_GRAND_BOSS_LIST = "INSERT INTO grandboss_list (player_id,zone) VALUES (?,?)";
	private static final String UPDATE_GRAND_BOSS_DATA = "UPDATE grandboss_data SET loc_x=?, loc_y=?, loc_z=?, heading=?, respawn_time=?, currentHP=?, currentMP=?, status=? WHERE boss_id=?";
	private static final String UPDATE_GRAND_BOSS_DATA2 = "UPDATE grandboss_data SET status = ? WHERE boss_id=?";
	
	private static GrandBossManager instance;
	
	protected static Map<Integer, L2GrandBossInstance> bosses;
	
	protected static Map<Integer, StatsSet> storedInfo;
	
	private Map<Integer, Integer> bossStatus;
	
	public static GrandBossManager getInstance()
	{
		if (instance == null)
		{
			instance = new GrandBossManager();
		}
		return instance;
	}
	
	public GrandBossManager()
	{
		init();
	}
	
	private void init()
	{
		bosses = new ConcurrentHashMap<>();
		storedInfo = new ConcurrentHashMap<>();
		bossStatus = new ConcurrentHashMap<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_GRAND_BOSS_DATA);
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				// Read all info from DB, and store it for AI to read and decide what to do
				// faster than accessing DB in real time
				StatsSet info = new StatsSet();
				int bossId = rset.getInt("boss_id");
				info.set("loc_x", rset.getInt("loc_x"));
				info.set("loc_y", rset.getInt("loc_y"));
				info.set("loc_z", rset.getInt("loc_z"));
				info.set("heading", rset.getInt("heading"));
				info.set("respawn_time", rset.getLong("respawn_time"));
				double HP = rset.getDouble("currentHP"); // jython doesn't recognize doubles
				int true_HP = (int) HP; // so use java's ability to type cast
				info.set("currentHP", true_HP); // to convert double to int
				double MP = rset.getDouble("currentMP");
				int true_MP = (int) MP;
				info.set("currentMP", true_MP);
				bossStatus.put(bossId, rset.getInt("status"));
				
				storedInfo.put(bossId, info);
				info = null;
			}
		}
		catch (Exception e)
		{
			LOGGER.error("GrandBossManager.init : Could not load grandboss_data table");
		}
	}
	
	public L2BossZone getZone(L2Character character)
	{
		for (L2ZoneType zone : ZoneData.getInstance().getAllZones().values())
		{
			if (zone instanceof L2BossZone)
			{
				L2BossZone temp = (L2BossZone) zone;
				
				if (temp.isInsideZone(character))
				{
					return temp;
				}
			}
		}
		
		return null;
	}
	
	public L2BossZone getZone(int x, int y, int z)
	{
		for (L2ZoneType zone : ZoneData.getInstance().getAllZones().values())
		{
			if (zone instanceof L2BossZone)
			{
				L2BossZone temp = (L2BossZone) zone;
				
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
		return temp.getZoneName().equalsIgnoreCase(zoneType);
	}
	
	public Integer getBossStatus(int bossId)
	{
		return bossStatus.get(bossId);
	}
	
	public void setBossStatus(int bossId, int status)
	{
		bossStatus.put(bossId, status);
		LOGGER.info(getClass().getSimpleName() + ": Updated " + NpcTable.getInstance().getTemplate(bossId).getName() + "(" + bossId + ") status to " + status);
		updateDb(bossId, true);
	}
	
	public void addBoss(L2GrandBossInstance boss)
	{
		if (boss != null)
		{
			bosses.put(boss.getNpcId(), boss);
		}
	}
	
	public L2GrandBossInstance getBoss(int bossId)
	{
		return bosses.get(bossId);
	}
	
	public L2GrandBossInstance deleteBoss(int bossId)
	{
		return bosses.remove(bossId);
	}
	
	public StatsSet getStatsSet(int bossId)
	{
		return storedInfo.get(bossId);
	}
	
	public void setStatsSet(int bossId, StatsSet info)
	{
		if (storedInfo.containsKey(bossId))
		{
			storedInfo.remove(bossId);
		}
		
		storedInfo.put(bossId, info);
		// Update immediately status in Database.
		fastStoreToDb();
	}
	
	private void fastStoreToDb()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();)
		{
			for (int bossId : storedInfo.keySet())
			{
				L2GrandBossInstance boss = bosses.get(bossId);
				StatsSet info = storedInfo.get(bossId);
				
				if (boss == null || info == null)
				{
					PreparedStatement update2 = con.prepareStatement(UPDATE_GRAND_BOSS_DATA2);
					update2.setInt(1, bossStatus.get(bossId));
					update2.setInt(2, bossId);
					update2.executeUpdate();
					update2.close();
				}
				else
				{
					PreparedStatement update1 = con.prepareStatement(UPDATE_GRAND_BOSS_DATA);
					update1.setInt(1, boss.getX());
					update1.setInt(2, boss.getY());
					update1.setInt(3, boss.getZ());
					update1.setInt(4, boss.getHeading());
					update1.setLong(5, info.getLong("respawn_time"));
					double hp = boss.getCurrentHp();
					double mp = boss.getCurrentMp();
					if (boss.isDead())
					{
						hp = boss.getMaxHp();
						mp = boss.getMaxMp();
					}
					update1.setDouble(6, hp);
					update1.setDouble(7, mp);
					update1.setInt(8, bossStatus.get(bossId));
					update1.setInt(9, bossId);
					update1.executeUpdate();
					update1.close();
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.warn("GrandBossManager.fastStoreToDb : Couldn't store grandbosses to database", e);
		}
	}
	
	private void storeToDb()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement deleteStatement = con.prepareStatement(DELETE_GRAND_BOSS_LIST);
			PreparedStatement insertStatement = con.prepareStatement(INSERT_GRAND_BOSS_LIST))
		{
			deleteStatement.executeUpdate();
			
			for (L2BossZone zone : getAllBossZones())
			{
				if (zone == null)
				{
					continue;
				}
				
				List<Integer> list = zone.getAllowedPlayers();
				
				if (list == null || list.isEmpty())
				{
					continue;
				}
				
				int count = 0;
				
				for (L2PcInstance player : zone.getPlayersInside())
				{
					if (player.isGM())
					{
						continue;
					}
					
					insertStatement.setInt(1, player.getObjectId());
					insertStatement.setInt(2, zone.getZoneId());
					insertStatement.addBatch();
					
					if (count % 500 == 0)
					{
						insertStatement.executeBatch();
					}
				}
				
				insertStatement.executeBatch();
			}
			
			for (Integer bossId : storedInfo.keySet())
			{
				L2GrandBossInstance boss = bosses.get(bossId);
				StatsSet info = storedInfo.get(bossId);
				
				if (boss == null || info == null)
				{
					try (PreparedStatement update2 = con.prepareStatement(UPDATE_GRAND_BOSS_DATA2))
					{
						update2.setInt(1, bossStatus.get(bossId));
						update2.setInt(2, bossId);
						update2.executeUpdate();
					}
				}
				else
				{
					try (PreparedStatement update1 = con.prepareStatement(UPDATE_GRAND_BOSS_DATA))
					{
						update1.setInt(1, boss.getX());
						update1.setInt(2, boss.getY());
						update1.setInt(3, boss.getZ());
						update1.setInt(4, boss.getHeading());
						update1.setLong(5, info.getLong("respawn_time"));
						double hp = boss.getCurrentHp();
						double mp = boss.getCurrentMp();
						
						if (boss.isDead())
						{
							hp = boss.getMaxHp();
							mp = boss.getMaxMp();
						}
						
						update1.setDouble(6, hp);
						update1.setDouble(7, mp);
						update1.setInt(8, bossStatus.get(bossId));
						update1.setInt(9, bossId);
						update1.executeUpdate();
					}
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.warn("GrandBossManager.storeToDb: Couldn't store grandbosses to database:" + e);
		}
	}
	
	private void updateDb(int bossId, boolean statusOnly)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();)
		{
			L2GrandBossInstance boss = bosses.get(bossId);
			StatsSet info = storedInfo.get(bossId);
			
			if (statusOnly || boss == null || info == null)
			{
				try (PreparedStatement statement = con.prepareStatement(UPDATE_GRAND_BOSS_DATA2))
				{
					statement.setInt(1, bossStatus.get(bossId));
					statement.setInt(2, bossId);
					statement.executeUpdate();
				}
			}
			else
			{
				try (PreparedStatement statement = con.prepareStatement(UPDATE_GRAND_BOSS_DATA))
				{
					statement.setInt(1, boss.getX());
					statement.setInt(2, boss.getY());
					statement.setInt(3, boss.getZ());
					statement.setInt(4, boss.getHeading());
					statement.setLong(5, info.getLong("respawn_time"));
					double hp = boss.getCurrentHp();
					double mp = boss.getCurrentMp();
					
					if (boss.isDead())
					{
						hp = boss.getMaxHp();
						mp = boss.getMaxMp();
					}
					
					statement.setDouble(6, hp);
					statement.setDouble(7, mp);
					statement.setInt(8, bossStatus.get(bossId));
					statement.setInt(9, bossId);
					statement.executeUpdate();
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.warn("GrandBossManager.updateDb: Couldn't update grandbosses to database", e);
		}
	}
	
	/**
	 * Saves all Grand Boss info and then clears all info from memory, including all schedules.
	 */
	public void cleanUp()
	{
		storeToDb();
		
		bosses.clear();
		storedInfo.clear();
		bossStatus.clear();
	}
	
	public L2NpcTemplate getValidTemplate(int bossId)
	{
		L2NpcTemplate template = NpcTable.getInstance().getTemplate(bossId);
		if (template == null)
		{
			return null;
		}
		
		if (!template.type.equalsIgnoreCase("L2GrandBoss"))
		{
			return null;
		}
		
		return template;
	}
	
	public boolean isDefined(int bossId) // into database
	{
		return bossStatus.get(bossId) != null;
	}
	
	public List<L2BossZone> getAllBossZones()
	{
		List<L2BossZone> zoneList = new ArrayList<>();
		
		for (L2ZoneType zone : ZoneData.getInstance().getAllZones().values())
		{
			if (zone instanceof L2BossZone)
			{
				zoneList.add((L2BossZone) zone);
			}
		}
		
		return zoneList;
	}
}
