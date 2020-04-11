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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.model.L2TeleportLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class ...
 * @version $Revision: 1.3.2.2.2.3 $ $Date: 2005/03/27 15:29:18 $
 */
public class TeleportLocationTable
{
	private static Logger _log = LoggerFactory.getLogger(TeleportLocationTable.class);
	
	private final Map<Integer, L2TeleportLocation> _teleports = new HashMap<>();
	
	protected TeleportLocationTable()
	{
		reloadAll();
	}
	
	public void reloadAll()
	{
		_teleports.clear();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT id, loc_x, loc_y, loc_z, price, fornoble, itemId FROM teleport"))
		{
			L2TeleportLocation teleport;
			while (rs.next())
			{
				teleport = new L2TeleportLocation();
				
				teleport.setTeleId(rs.getInt("id"));
				teleport.setX(rs.getInt("loc_x"));
				teleport.setY(rs.getInt("loc_y"));
				teleport.setZ(rs.getInt("loc_z"));
				teleport.setPrice(rs.getInt("price"));
				teleport.setIsForNoble(rs.getInt("fornoble") == 1);
				teleport.setItemId(rs.getInt("itemId"));
				
				_teleports.put(teleport.getTeleId(), teleport);
			}
			_log.info(getClass().getSimpleName() + ": Loaded " + _teleports.size() + " Teleport Location Templates.");
		}
		catch (Exception e)
		{
			_log.error(getClass().getSimpleName() + ": Error loading Teleport Table.", e);
		}
		
		if (Config.CUSTOM_TELEPORT_TABLE)
		{
			int _cTeleCount = _teleports.size();
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				Statement s = con.createStatement();
				ResultSet rs = s.executeQuery("SELECT id, loc_x, loc_y, loc_z, price, fornoble, itemId FROM custom_teleport"))
			{
				L2TeleportLocation teleport;
				while (rs.next())
				{
					teleport = new L2TeleportLocation();
					teleport.setTeleId(rs.getInt("id"));
					teleport.setX(rs.getInt("loc_x"));
					teleport.setY(rs.getInt("loc_y"));
					teleport.setZ(rs.getInt("loc_z"));
					teleport.setPrice(rs.getInt("price"));
					teleport.setIsForNoble(rs.getInt("fornoble") == 1);
					teleport.setItemId(rs.getInt("itemId"));
					
					_teleports.put(teleport.getTeleId(), teleport);
				}
				_cTeleCount = _teleports.size() - _cTeleCount;
				if (_cTeleCount > 0)
				{
					_log.info(getClass().getSimpleName() + ": Loaded " + _cTeleCount + " Custom Teleport Location Templates.");
				}
			}
			catch (Exception e)
			{
				_log.warn(getClass().getSimpleName() + ": Error while creating custom teleport table " + e.getMessage(), e);
			}
		}
	}
	
	/**
	 * @param id
	 * @return
	 */
	public L2TeleportLocation getTemplate(int id)
	{
		return _teleports.get(id);
	}
	
	public static TeleportLocationTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final TeleportLocationTable _instance = new TeleportLocationTable();
	}
}
