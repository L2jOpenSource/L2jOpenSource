package com.l2jfrozen.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author Kerberos
 */

public class RaidBossPointsManager
{
	private final static Logger LOGGER = Logger.getLogger(RaidBossPointsManager.class);
	private static final String REPLACE_CHARACTER_RAID_POINTS = "REPLACE INTO character_raid_points (`charId`,`boss_id`,`points`) VALUES (?,?,?)";
	private static final String DELETE_CHARACTER_RAID_POINTS = "DELETE FROM character_raid_points";
	private static final String SELECT_CHARACTER_RAID_POINTS_CHAR_ID = "SELECT charId FROM character_raid_points";
	private static final String SELECT_CHARACTER_RAID_POINTS_BOSS_ID_POINTS = "SELECT boss_id,points FROM character_raid_points WHERE charId=?";
	
	protected static Map<Integer, Map<Integer, Integer>> list;
	
	private static final Comparator<Map.Entry<Integer, Integer>> comparator = (entry, entry1) -> entry.getValue().equals(entry1.getValue()) ? 0 : entry.getValue() < entry1.getValue() ? 1 : -1;
	
	public final static void init()
	{
		list = new HashMap<>();
		List<Integer> chars = new ArrayList<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CHARACTER_RAID_POINTS_CHAR_ID);
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				chars.add(rset.getInt("charId"));
			}
			
			for (int charId : chars)
			{
				Map<Integer, Integer> values = new HashMap<>();
				
				try (PreparedStatement stmt = con.prepareStatement(SELECT_CHARACTER_RAID_POINTS_BOSS_ID_POINTS))
				{
					stmt.setInt(1, charId);
					
					try (ResultSet result = stmt.executeQuery())
					{
						while (result.next())
						{
							values.put(result.getInt("boss_id"), result.getInt("points"));
						}
					}
				}
				list.put(charId, values);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("RaidPointsManager.init : Couldnt load raid points " + e.getMessage());
		}
	}
	
	public final static void updatePointsInDB(final L2PcInstance player, final int raidId, final int points)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(REPLACE_CHARACTER_RAID_POINTS))
		{
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, raidId);
			statement.setInt(3, points);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("RaidBossPointsManager.updatePointsInDB : Could not update char raid points", e);
		}
	}
	
	public final static void addPoints(final L2PcInstance player, final int bossId, final int points)
	{
		final int ownerId = player.getObjectId();
		Map<Integer, Integer> tmpPoint = new HashMap<>();
		if (list == null)
		{
			list = new HashMap<>();
		}
		tmpPoint = list.get(ownerId);
		if (tmpPoint == null || tmpPoint.isEmpty())
		{
			tmpPoint = new HashMap<>();
			tmpPoint.put(bossId, points);
			updatePointsInDB(player, bossId, points);
		}
		else
		{
			final int currentPoins = tmpPoint.containsKey(bossId) ? tmpPoint.get(bossId).intValue() : 0;
			tmpPoint.remove(bossId);
			tmpPoint.put(bossId, currentPoins == 0 ? points : currentPoins + points);
			updatePointsInDB(player, bossId, currentPoins == 0 ? points : currentPoins + points);
		}
		list.remove(ownerId);
		list.put(ownerId, tmpPoint);
	}
	
	public final static int getPointsByOwnerId(final int ownerId)
	{
		Map<Integer, Integer> tmpPoint = new HashMap<>();
		if (list == null)
		{
			list = new HashMap<>();
		}
		tmpPoint = list.get(ownerId);
		int totalPoints = 0;
		
		if (tmpPoint == null || tmpPoint.isEmpty())
		{
			return 0;
		}
		
		for (final int bossId : tmpPoint.keySet())
		{
			totalPoints += tmpPoint.get(bossId);
		}
		return totalPoints;
	}
	
	public final static Map<Integer, Integer> getList(final L2PcInstance player)
	{
		return list.get(player.getObjectId());
	}
	
	public final static void cleanUp()
	{
		list.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_CHARACTER_RAID_POINTS))
		{
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("RaidBossPointsManager.cleanUp : Could not delete all raid points from character_raid_points table", e);
		}
	}
	
	public final static int calculateRanking(final int playerObjId)
	{
		final Map<Integer, Integer> tmpRanking = new HashMap<>();
		final Map<Integer, Integer> tmpPoints = new HashMap<>();
		int totalPoints;
		
		for (final int ownerId : list.keySet())
		{
			totalPoints = getPointsByOwnerId(ownerId);
			if (totalPoints != 0)
			{
				tmpPoints.put(ownerId, totalPoints);
			}
		}
		final ArrayList<Entry<Integer, Integer>> list = new ArrayList<>(tmpPoints.entrySet());
		
		Collections.sort(list, comparator);
		
		int ranking = 1;
		for (final Map.Entry<Integer, Integer> entry : list)
		{
			tmpRanking.put(entry.getKey(), ranking++);
		}
		
		if (tmpRanking.containsKey(playerObjId))
		{
			return tmpRanking.get(playerObjId);
		}
		return 0;
	}
	
	public static Map<Integer, Integer> getRankList()
	{
		final Map<Integer, Integer> tmpRanking = new HashMap<>();
		final Map<Integer, Integer> tmpPoints = new HashMap<>();
		int totalPoints;
		
		for (final int ownerId : list.keySet())
		{
			totalPoints = getPointsByOwnerId(ownerId);
			if (totalPoints != 0)
			{
				tmpPoints.put(ownerId, totalPoints);
			}
		}
		final ArrayList<Entry<Integer, Integer>> list = new ArrayList<>(tmpPoints.entrySet());
		
		Collections.sort(list, comparator);
		
		int ranking = 1;
		for (final Map.Entry<Integer, Integer> entry : list)
		{
			tmpRanking.put(entry.getKey(), ranking++);
		}
		
		return tmpRanking;
	}
}