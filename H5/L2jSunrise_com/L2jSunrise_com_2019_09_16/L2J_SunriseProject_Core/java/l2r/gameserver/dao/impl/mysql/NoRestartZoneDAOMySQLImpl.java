/*
 * Copyright (C) 2004-2017 L2J Server
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
package l2r.gameserver.dao.impl.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import l2r.L2DatabaseFactory;
import l2r.gameserver.dao.NoRestartZoneDAO;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.zone.type.L2NoRestartZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Item Reuse DAO MySQL implementation.
 * @author vGodFather
 */
public class NoRestartZoneDAOMySQLImpl implements NoRestartZoneDAO
{
	private static Logger Log = LoggerFactory.getLogger("DAO");
	
	private static final String DELETE = "DELETE FROM character_norestart_zone_time WHERE charId = ?";
	private static final String SELECT = "SELECT time_limit FROM character_norestart_zone_time WHERE charId = ?";
	private static final String UPDATE = "REPLACE INTO character_norestart_zone_time (charId, time_limit) VALUES (?,?)";
	
	@Override
	public void delete(L2PcInstance player)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE))
		{
			ps.setInt(1, player.getObjectId());
			ps.execute();
		}
		catch (Exception e)
		{
			Log.warn("Could not delete player's " + player + " item reuse data!", e);
		}
	}
	
	@Override
	public void update(L2PcInstance player, L2NoRestartZone zone)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE))
		{
			statement.setInt(1, player.getObjectId());
			statement.setLong(2, System.currentTimeMillis() + (zone.getRestartAllowedTime() * 1000));
			statement.execute();
		}
		catch (SQLException e)
		{
			Log.warn("Cannot store zone norestart limit for character " + player.getObjectId(), e);
		}
	}
	
	@Override
	public void load(L2PcInstance player)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps1 = con.prepareStatement(SELECT))
		{
			ps1.setInt(1, player.getObjectId());
			try (ResultSet rset = ps1.executeQuery())
			{
				if (rset.next())
				{
					player.setZoneRestartLimitTime(rset.getLong("time_limit"));
					try (PreparedStatement ps2 = con.prepareStatement(DELETE))
					{
						ps2.setInt(1, player.getObjectId());
						ps2.executeUpdate();
					}
				}
			}
		}
		catch (Exception e)
		{
			Log.warn("Could not restore " + this + " zone restart time: " + e.getMessage(), e);
		}
	}
}
