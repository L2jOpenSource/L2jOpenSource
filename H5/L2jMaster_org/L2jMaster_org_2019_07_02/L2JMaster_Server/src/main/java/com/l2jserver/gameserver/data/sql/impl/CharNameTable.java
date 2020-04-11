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
package com.l2jserver.gameserver.data.sql.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.Config;
import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * Loads name and access level for all players.
 * @version 2005/03/27
 */
public class CharNameTable
{
	private static Logger _log = Logger.getLogger(CharNameTable.class.getName());
	
	private final Map<Integer, String> _chars = new ConcurrentHashMap<>();
	private final Map<Integer, Integer> _accessLevels = new ConcurrentHashMap<>();
	
	private final Map<Integer, Integer> _clan = new HashMap<>();
	private final Map<Integer, Integer> _level = new HashMap<>();
	private final Map<Integer, Integer> _sex = new HashMap<>();
	private final Map<Integer, Integer> _lastAccess = new HashMap<>();
	private final Map<Integer, Integer> _pvp = new HashMap<>();
	private final Map<Integer, Integer> _pk = new HashMap<>();
	private final Map<Integer, Integer> _dead = new HashMap<>();
	
	protected CharNameTable()
	{
		if (Config.CACHE_CHAR_NAMES)
		{
			loadAll();
		}
	}
	
	public final void addName(L2PcInstance player)
	{
		if (player != null)
		{
			addName(player.getObjectId(), player.getName());
			_accessLevels.put(player.getObjectId(), player.getAccessLevel().getLevel());
		}
	}
	
	private final void addName(int objectId, String name)
	{
		if (name != null)
		{
			if (!name.equals(_chars.get(objectId)))
			{
				_chars.put(objectId, name);
			}
		}
	}
	
	public final void removeName(int objId)
	{
		_chars.remove(objId);
		_accessLevels.remove(objId);
	}
	
	public final int getIdByName(String name)
	{
		if ((name == null) || name.isEmpty())
		{
			return -1;
		}
		
		for (Entry<Integer, String> entry : _chars.entrySet())
		{
			if (entry.getValue().equalsIgnoreCase(name))
			{
				return entry.getKey();
			}
		}
		
		if (Config.CACHE_CHAR_NAMES)
		{
			return -1;
		}
		
		int id = -1;
		int accessLevel = 0;
		
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT charId,accesslevel FROM characters WHERE char_name=?"))
		{
			ps.setString(1, name);
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					id = rs.getInt(1);
					accessLevel = rs.getInt(2);
				}
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Could not check existing char name: " + e.getMessage(), e);
		}
		
		if (id > 0)
		{
			_chars.put(id, name);
			_accessLevels.put(id, accessLevel);
			return id;
		}
		
		return -1; // not found
	}
	
	public final String getNameById(int id)
	{
		if (id <= 0)
		{
			return null;
		}
		
		String name = _chars.get(id);
		if (name != null)
		{
			return name;
		}
		
		if (Config.CACHE_CHAR_NAMES)
		{
			return null;
		}
		
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT char_name,accesslevel FROM characters WHERE charId=?"))
		{
			ps.setInt(1, id);
			try (ResultSet rset = ps.executeQuery())
			{
				if (rset.next())
				{
					name = rset.getString(1);
					_chars.put(id, name);
					_accessLevels.put(id, rset.getInt(2));
					return name;
				}
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Could not check existing char id: " + e.getMessage(), e);
		}
		
		return null; // not found
	}
	
	public final int getAccessLevelById(int objectId)
	{
		if (getNameById(objectId) != null)
		{
			return _accessLevels.get(objectId);
		}
		
		return 0;
	}
	
	public synchronized boolean doesCharNameExist(String name)
	{
		boolean result = true;
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT account_name FROM characters WHERE char_name=?"))
		{
			ps.setString(1, name);
			try (ResultSet rs = ps.executeQuery())
			{
				result = rs.next();
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Could not check existing charname: " + e.getMessage(), e);
		}
		return result;
	}
	
	public int getAccountCharacterCount(String account)
	{
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT COUNT(char_name) FROM characters WHERE account_name=?"))
		{
			ps.setString(1, account);
			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					return rset.getInt(1);
				}
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Could not check existing char count: " + e.getMessage(), e);
		}
		return 0;
	}
	
	private void loadAll()
	{
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT charId, char_name, accesslevel FROM characters"))
		{
			while (rs.next())
			{
				final int id = rs.getInt(1);
				_chars.put(id, rs.getString(2));
				_accessLevels.put(id, rs.getInt(3));
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Could not load char name: " + e.getMessage(), e);
		}
		_log.info(getClass().getSimpleName() + ": Loaded " + _chars.size() + " char names.");
	}
	
	public static CharNameTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CharNameTable _instance = new CharNameTable();
	}
	
	/**
	 * Obtenemos el sex de un char segun su Id
	 * @param objectId
	 * @return (0)Male (1)Famele
	 */
	public final int getCharSexById(int objectId)
	{
		if (getNameById(objectId) != null)
		{
			return _sex.get(objectId);
		}
		
		return 0;
	}
	
	/**
	 * Obtenemos el id de un clan segun el Id del char.
	 * @param objectId
	 * @return
	 */
	public final int getCharClanById(int objectId)
	{
		if (getNameById(objectId) != null)
		{
			return _clan.get(objectId);
		}
		
		return 0;
	}
	
	/**
	 * Obtenemos el Level de un char segun su Id
	 * @param objectId
	 * @return
	 */
	public final int getCharLevelById(int objectId)
	{
		if (getNameById(objectId) != null)
		{
			return _level.get(objectId);
		}
		
		return 0;
	}
	
	/**
	 * Obtenemos cuando se conecto por ultima ves
	 * @param objectId
	 * @return
	 */
	public final int getCharLastAccess(int objectId)
	{
		if (getNameById(objectId) != null)
		{
			return _lastAccess.get(objectId);
		}
		
		return 0;
	}
	
	/**
	 * Obtenemos los pvp de un player
	 * @param objectId
	 * @return
	 */
	public final int getCharPvp(int objectId)
	{
		if (getNameById(objectId) != null)
		{
			return _pvp.get(objectId);
		}
		
		return 0;
	}
	
	/**
	 * Obtenemos los pk de un player
	 * @param objectId
	 * @return
	 */
	public final int getCharPk(int objectId)
	{
		if (getNameById(objectId) != null)
		{
			return _pk.get(objectId);
		}
		
		return 0;
	}
	
	/**
	 * Obtenemos las muertes en pvp de un player
	 * @param objectId
	 * @return
	 */
	public final int getCharDead(int objectId)
	{
		if (getNameById(objectId) != null)
		{
			return _dead.get(objectId);
		}
		
		return 0;
	}
}
