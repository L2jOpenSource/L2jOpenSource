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

import l2r.L2DatabaseFactory;
import l2r.gameserver.dao.ShortcutDAO;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shortcut DAO MySQL implementation.
 * @author vGodFather
 */
public class ShortcutDAOMySQLImpl implements ShortcutDAO
{
	private static Logger Log = LoggerFactory.getLogger("DAO");
	
	private static final String DELETE = "DELETE FROM character_shortcuts WHERE charId=? AND class_index=?";
	
	@Override
	public boolean delete(L2PcInstance player, int classIndex)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE))
		{
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, classIndex);
			ps.execute();
		}
		catch (Exception e)
		{
			Log.error("Could not modify sub class for " + player + " to class index " + classIndex, e);
			return false;
		}
		return true;
	}
}
