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

import l2r.L2DatabaseFactory;
import l2r.gameserver.dao.SubclassDAO;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.base.SubClass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subclass DAO MySQL implementation.
 * @author vGodFather
 */
public class SubclassDAOMySQLImpl implements SubclassDAO
{
	private static Logger Log = LoggerFactory.getLogger("DAO");
	
	private static final String SELECT = "SELECT class_id,exp,sp,level,class_index FROM character_subclasses WHERE charId=? ORDER BY class_index ASC";
	private static final String INSERT = "INSERT INTO character_subclasses (charId,class_id,exp,sp,level,class_index) VALUES (?,?,?,?,?,?)";
	private static final String UPDATE = "UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=? WHERE charId=? AND class_index =?";
	private static final String DELETE = "DELETE FROM character_subclasses WHERE charId=? AND class_index=?";
	
	@Override
	public void update(L2PcInstance player)
	{
		if (player.getTotalSubClasses() <= 0)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE))
		{
			for (SubClass subClass : player.getSubClasses().values())
			{
				ps.setLong(1, subClass.getExp());
				ps.setInt(2, subClass.getSp());
				ps.setInt(3, subClass.getLevel());
				ps.setInt(4, subClass.getClassId());
				ps.setInt(5, player.getObjectId());
				ps.setInt(6, subClass.getClassIndex());
				ps.addBatch();
			}
			ps.executeBatch();
		}
		catch (Exception e)
		{
			Log.error("Could not store sub class data for " + player, e);
		}
	}
	
	@Override
	public boolean insert(L2PcInstance player, SubClass newClass)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(INSERT))
		{
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, newClass.getClassId());
			ps.setLong(3, newClass.getExp());
			ps.setInt(4, newClass.getSp());
			ps.setInt(5, newClass.getLevel());
			ps.setInt(6, newClass.getClassIndex());
			ps.execute();
		}
		catch (Exception e)
		{
			Log.error("Could not add character sub class for " + player, e);
			return false;
		}
		return true;
	}
	
	@Override
	public void delete(L2PcInstance player, int classIndex)
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
			Log.error("Could not delete subclass for " + player + " to class index " + classIndex, e);
		}
	}
	
	@Override
	public void load(L2PcInstance player)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SELECT))
		{
			ps.setInt(1, player.getObjectId());
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					final SubClass subClass = new SubClass();
					subClass.setClassId(rs.getInt("class_id"));
					subClass.setExp(rs.getLong("exp"));
					subClass.setLevel(rs.getByte("level"));
					subClass.setSp(rs.getInt("sp"));
					subClass.setClassIndex(rs.getInt("class_index"));
					
					// Enforce the correct indexing of _subClasses against their class indexes.
					player.getSubClasses().put(subClass.getClassIndex(), subClass);
				}
			}
		}
		catch (Exception e)
		{
			Log.error("Could not restore classes for " + player, e);
		}
	}
}
