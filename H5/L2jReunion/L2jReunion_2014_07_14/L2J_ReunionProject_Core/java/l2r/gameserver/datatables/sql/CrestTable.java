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
package l2r.gameserver.datatables.sql;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.enums.CrestType;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.L2Crest;
import l2r.util.file.filter.BMPFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nos
 */
public final class CrestTable
{
	private final static Logger _log = LoggerFactory.getLogger(CrestTable.class);
	
	private final Map<Integer, L2Crest> _crests = new ConcurrentHashMap<>();
	private final AtomicInteger _nextId = new AtomicInteger(1);
	
	protected CrestTable()
	{
		load();
	}
	
	public synchronized void load()
	{
		_crests.clear();
		Set<Integer> crestsInUse = new HashSet<>();
		for (L2Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getCrestId() != 0)
			{
				crestsInUse.add(clan.getCrestId());
			}
			
			if (clan.getCrestLargeId() != 0)
			{
				crestsInUse.add(clan.getCrestLargeId());
			}
			
			if (clan.getAllyCrestId() != 0)
			{
				crestsInUse.add(clan.getAllyCrestId());
			}
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement statement = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = statement.executeQuery("SELECT `crest_id`, `data`, `type` FROM `crests` ORDER BY `crest_id` DESC"))
		{
			while (rs.next())
			{
				int id = rs.getInt("crest_id");
				
				if (_nextId.get() <= id)
				{
					_nextId.set(id + 1);
				}
				
				// delete all unused crests except the last one we dont want to reuse
				// a crest id because client will display wrong crest if its reused
				if (!crestsInUse.contains(id) && (id != (_nextId.get() - 1)))
				{
					rs.deleteRow();
					continue;
				}
				
				byte[] data = rs.getBytes("data");
				CrestType crestType = CrestType.getById(rs.getInt("type"));
				if (crestType != null)
				{
					_crests.put(id, new L2Crest(id, data, crestType));
				}
				else
				{
					_log.warn("Unknown crest type found in database. Type:" + rs.getInt("type"));
				}
			}
			
		}
		catch (SQLException e)
		{
			_log.warn("There was an error while loading crests from database:", e);
		}
		
		moveOldCrestsToDb(crestsInUse);
		
		_log.info(getClass().getSimpleName() + ": Loaded " + _crests.size() + " Crests.");
		
		for (L2Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getCrestId() != 0)
			{
				if (getCrest(clan.getCrestId()) == null)
				{
					_log.info("Removing non-existent crest for clan " + clan.getName() + " [" + clan.getId() + "], crestId:" + clan.getCrestId());
					clan.setCrestId(0);
					clan.changeClanCrest(0);
				}
			}
			
			if (clan.getCrestLargeId() != 0)
			{
				if (getCrest(clan.getCrestLargeId()) == null)
				{
					_log.info("Removing non-existent large crest for clan " + clan.getName() + " [" + clan.getId() + "], crestLargeId:" + clan.getCrestLargeId());
					clan.setCrestLargeId(0);
					clan.changeLargeCrest(0);
				}
			}
			
			if (clan.getAllyCrestId() != 0)
			{
				if (getCrest(clan.getAllyCrestId()) == null)
				{
					_log.info("Removing non-existent ally crest for clan " + clan.getName() + " [" + clan.getId() + "], allyCrestId:" + clan.getAllyCrestId());
					clan.setAllyCrestId(0);
					clan.changeAllyCrest(0, true);
				}
			}
		}
	}
	
	/**
	 * Moves old crests from data/crests folder to database and deletes crest folder<br>
	 * <b>TODO:</b> remove it after some time
	 * @param crestsInUse the set of crests in use
	 */
	private void moveOldCrestsToDb(Set<Integer> crestsInUse)
	{
		final File crestDir = new File(Config.DATAPACK_ROOT, "data/crests/");
		if (crestDir.exists())
		{
			for (File file : crestDir.listFiles(new BMPFilter()))
			{
				try
				{
					final byte[] data = Files.readAllBytes(file.toPath());
					if (file.getName().startsWith("Crest_Large_"))
					{
						final int crestId = Integer.parseInt(file.getName().substring(12, file.getName().length() - 4));
						if (crestsInUse.contains(crestId))
						{
							final L2Crest crest = createCrest(data, CrestType.PLEDGE_LARGE);
							if (crest != null)
							{
								for (L2Clan clan : ClanTable.getInstance().getClans())
								{
									if (clan.getCrestLargeId() == crestId)
									{
										clan.setCrestLargeId(0);
										clan.changeLargeCrest(crest.getId());
									}
								}
							}
						}
					}
					else if (file.getName().startsWith("Crest_"))
					{
						final int crestId = Integer.parseInt(file.getName().substring(6, file.getName().length() - 4));
						if (crestsInUse.contains(crestId))
						{
							L2Crest crest = createCrest(data, CrestType.PLEDGE);
							if (crest != null)
							{
								for (L2Clan clan : ClanTable.getInstance().getClans())
								{
									if (clan.getCrestId() == crestId)
									{
										clan.setCrestId(0);
										clan.changeClanCrest(crest.getId());
									}
								}
							}
						}
					}
					else if (file.getName().startsWith("AllyCrest_"))
					{
						final int crestId = Integer.parseInt(file.getName().substring(10, file.getName().length() - 4));
						if (crestsInUse.contains(crestId))
						{
							final L2Crest crest = createCrest(data, CrestType.ALLY);
							if (crest != null)
							{
								for (L2Clan clan : ClanTable.getInstance().getClans())
								{
									if (clan.getAllyCrestId() == crestId)
									{
										clan.setAllyCrestId(0);
										clan.changeAllyCrest(crest.getId(), false);
									}
								}
							}
						}
					}
					file.delete();
				}
				catch (Exception e)
				{
					_log.warn("There was an error while moving crest file " + file.getName() + " to database:", e);
				}
			}
			crestDir.delete();
		}
	}
	
	/**
	 * @param crestId The crest id
	 * @return {@code L2Crest} if crest is found, {@code null} if crest was not found.
	 */
	public L2Crest getCrest(int crestId)
	{
		return _crests.get(crestId);
	}
	
	/**
	 * Creates a {@code L2Crest} object and inserts it in database and cache.
	 * @param data
	 * @param crestType
	 * @return {@code L2Crest} on success, {@code null} on failure.
	 */
	public L2Crest createCrest(byte[] data, CrestType crestType)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement statement = con.prepareStatement("INSERT INTO `crests`(`crest_id`, `data`, `type`) VALUES(?, ?, ?)"))
			{
				final L2Crest crest = new L2Crest(getNextId(), data, crestType);
				statement.setInt(1, crest.getId());
				statement.setBytes(2, crest.getData());
				statement.setInt(3, crest.getType().getId());
				statement.executeUpdate();
				_crests.put(crest.getId(), crest);
				return crest;
			}
		}
		catch (SQLException e)
		{
			_log.warn("There was an error while saving crest in database:", e);
		}
		return null;
	}
	
	/**
	 * Removes crest from database and cache.
	 * @param crestId the id of crest to be removed.
	 */
	public void removeCrest(int crestId)
	{
		_crests.remove(crestId);
		
		// avoid removing last crest id we dont want to lose index...
		// because client will display wrong crest if its reused
		if (crestId == (_nextId.get() - 1))
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM `crests` WHERE `crest_id` = ?"))
		{
			statement.setInt(1, crestId);
			statement.executeUpdate();
		}
		catch (SQLException e)
		{
			_log.warn("There was an error while deleting crest from database:", e);
		}
	}
	
	/**
	 * @return The next crest id.
	 */
	public int getNextId()
	{
		return _nextId.getAndIncrement();
	}
	
	public static CrestTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CrestTable _instance = new CrestTable();
	}
}
