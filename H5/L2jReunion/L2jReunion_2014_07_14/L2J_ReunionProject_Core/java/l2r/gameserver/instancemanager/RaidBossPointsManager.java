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
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

import javolution.util.FastMap;
import l2r.L2DatabaseFactory;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kerberos, JIV
 * @version 8/24/10
 */
public class RaidBossPointsManager
{
	private static final Logger _log = LoggerFactory.getLogger(RaidBossPointsManager.class);
	
	private FastMap<Integer, Map<Integer, Integer>> _list;
	
	private final Comparator<Map.Entry<Integer, Integer>> _comparator = new Comparator<Map.Entry<Integer, Integer>>()
	{
		@Override
		public int compare(Map.Entry<Integer, Integer> entry, Map.Entry<Integer, Integer> entry1)
		{
			return entry.getValue().equals(entry1.getValue()) ? 0 : entry.getValue() < entry1.getValue() ? 1 : -1;
		}
	};
	
	public RaidBossPointsManager()
	{
		init();
	}
	
	private final void init()
	{
		_list = new FastMap<>();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT `charId`,`boss_id`,`points` FROM `character_raid_points`"))
		{
			while (rs.next())
			{
				int charId = rs.getInt("charId");
				int bossId = rs.getInt("boss_id");
				int points = rs.getInt("points");
				Map<Integer, Integer> values = _list.get(charId);
				if (values == null)
				{
					values = new FastMap<>();
				}
				values.put(bossId, points);
				_list.put(charId, values);
			}
			_log.info(getClass().getSimpleName() + ": Loaded " + _list.size() + " Characters Raid Points.");
		}
		catch (SQLException e)
		{
			_log.warn(getClass().getSimpleName() + ": Couldnt load raid points ", e);
		}
	}
	
	public final void updatePointsInDB(L2PcInstance player, int raidId, int points)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("REPLACE INTO character_raid_points (`charId`,`boss_id`,`points`) VALUES (?,?,?)"))
		{
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, raidId);
			ps.setInt(3, points);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": Couldn't update char raid points for player: " + player, e);
		}
	}
	
	public final void addPoints(L2PcInstance player, int bossId, int points)
	{
		int ownerId = player.getObjectId();
		Map<Integer, Integer> tmpPoint = _list.get(ownerId);
		if (tmpPoint == null)
		{
			tmpPoint = new FastMap<>();
			tmpPoint.put(bossId, points);
			updatePointsInDB(player, bossId, points);
		}
		else
		{
			int currentPoins = tmpPoint.containsKey(bossId) ? tmpPoint.get(bossId) : 0;
			currentPoins += points;
			tmpPoint.put(bossId, currentPoins);
			updatePointsInDB(player, bossId, currentPoins);
		}
		_list.put(ownerId, tmpPoint);
	}
	
	public final int getPointsByOwnerId(int ownerId)
	{
		Map<Integer, Integer> tmpPoint;
		tmpPoint = _list.get(ownerId);
		int totalPoints = 0;
		
		if ((tmpPoint == null) || tmpPoint.isEmpty())
		{
			return 0;
		}
		
		for (int points : tmpPoint.values())
		{
			totalPoints += points;
		}
		return totalPoints;
	}
	
	public final Map<Integer, Integer> getList(L2PcInstance player)
	{
		return _list.get(player.getObjectId());
	}
	
	public final void cleanUp()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE from character_raid_points WHERE charId > 0"))
		{
			statement.executeUpdate();
			_list.clear();
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": Couldn't clean raid points", e);
		}
	}
	
	public final int calculateRanking(int playerObjId)
	{
		Map<Integer, Integer> rank = getRankList();
		if (rank.containsKey(playerObjId))
		{
			return rank.get(playerObjId);
		}
		return 0;
	}
	
	public Map<Integer, Integer> getRankList()
	{
		Map<Integer, Integer> tmpRanking = new FastMap<>();
		Map<Integer, Integer> tmpPoints = new FastMap<>();
		
		for (int ownerId : _list.keySet())
		{
			int totalPoints = getPointsByOwnerId(ownerId);
			if (totalPoints != 0)
			{
				tmpPoints.put(ownerId, totalPoints);
			}
		}
		ArrayList<Entry<Integer, Integer>> list = new ArrayList<>(tmpPoints.entrySet());
		
		Collections.sort(list, _comparator);
		
		int ranking = 1;
		for (Map.Entry<Integer, Integer> entry : list)
		{
			tmpRanking.put(entry.getKey(), ranking++);
		}
		
		return tmpRanking;
	}
	
	public static final RaidBossPointsManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final RaidBossPointsManager _instance = new RaidBossPointsManager();
	}
}