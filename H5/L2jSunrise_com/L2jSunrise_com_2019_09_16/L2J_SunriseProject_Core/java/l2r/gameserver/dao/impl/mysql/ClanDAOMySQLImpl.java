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
package l2r.gameserver.dao.impl.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import l2r.L2DatabaseFactory;
import l2r.gameserver.dao.ClanDAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clan DAO MySQL implementation.
 * @author vGodFather
 */
public class ClanDAOMySQLImpl implements ClanDAO
{
	
	private static final Logger LOG = LoggerFactory.getLogger(ClanDAOMySQLImpl.class);
	
	private static final String SELECT_CLAN_PRIVILEGES = "SELECT `privs`, `rank`, `party` FROM `clan_privs` WHERE clan_id=?";
	
	private static final String INSERT_CLAN_PRIVILEGES = "INSERT INTO `clan_privs` (`clan_id`, `rank`, `party`, `privs`) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE `privs`=?";
	
	@Override
	public Map<Integer, Integer> getPrivileges(int clanId)
	{
		final Map<Integer, Integer> result = new HashMap<>();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SELECT_CLAN_PRIVILEGES))
		{
			ps.setInt(1, clanId);
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					final int rank = rs.getInt("rank");
					if (rank == -1)
					{
						continue;
					}
					result.put(rank, rs.getInt("privs"));
				}
			}
		}
		catch (Exception ex)
		{
			LOG.error("Unable to restore clan privileges for clan Id {}!", clanId, ex);
		}
		return result;
	}
	
	@Override
	public void storePrivileges(int clanId, int rank, int privileges)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(INSERT_CLAN_PRIVILEGES))
		{
			ps.setInt(1, clanId);
			ps.setInt(2, rank);
			ps.setInt(3, 0);
			ps.setInt(4, privileges);
			ps.setInt(5, privileges);
			ps.execute();
		}
		catch (Exception ex)
		{
			LOG.error("Unable to store clan privileges for clan Id {}!", clanId, ex);
		}
	}
}
