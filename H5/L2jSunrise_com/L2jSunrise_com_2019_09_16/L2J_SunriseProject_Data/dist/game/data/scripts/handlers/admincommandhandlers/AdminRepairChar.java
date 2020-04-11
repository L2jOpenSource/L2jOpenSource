/*
 * Copyright (C) 2004-2013 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import l2r.L2DatabaseFactory;
import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles following admin commands: - delete = deletes target
 * @version $Revision: 1.1.2.6.2.3 $ $Date: 2005/04/11 10:05:59 $
 */
public class AdminRepairChar implements IAdminCommandHandler
{
	private static Logger _log = LoggerFactory.getLogger(AdminRepairChar.class);
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_restore",
		"admin_repair"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		handleRepair(command);
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void handleRepair(String command)
	{
		String[] parts = command.split(" ");
		if (parts.length != 2)
		{
			return;
		}
		int objId = 0;
		String cmd = "UPDATE characters SET x=-84318, y=244579, z=-3730 WHERE char_name=?";
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement statement = con.prepareStatement(cmd))
			{
				statement.setString(1, parts[1]);
				statement.execute();
			}
			
			try (PreparedStatement statement = con.prepareStatement("SELECT charId FROM characters where char_name=?"))
			{
				statement.setString(1, parts[1]);
				try (ResultSet rset = statement.executeQuery())
				{
					
					if (rset.next())
					{
						objId = rset.getInt(1);
					}
				}
			}
			
			if (objId == 0)
			{
				con.close();
				return;
			}
			
			// connection = L2DatabaseFactory.getInstance().getConnection();
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE charId=?"))
			{
				statement.setInt(1, objId);
				statement.execute();
			}
			
			// connection = L2DatabaseFactory.getInstance().getConnection();
			try (PreparedStatement statement = con.prepareStatement("UPDATE items SET loc=\"INVENTORY\" WHERE owner_id=?"))
			{
				statement.setInt(1, objId);
				statement.execute();
			}
		}
		catch (Exception e)
		{
			_log.warn("could not repair char:", e);
		}
	}
}
