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
 package com.l2jserver.gameserver.communitybbs.Manager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.communitybbs.BB.Forum;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

public class ForumsBBSManager extends BaseBBSManager
{
	private static final Logger LOG = LoggerFactory.getLogger(ForumsBBSManager.class);
	private final List<Forum> _table;
	private int _lastid = 1;
	
	protected ForumsBBSManager()
	{
		_table = new ArrayList<>();
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT forum_id FROM forums WHERE forum_type = 0"))
		{
			while (rs.next())
			{
				int forumId = rs.getInt("forum_id");
				Forum f = new Forum(forumId, null);
				addForum(f);
			}
		}
		catch (Exception e)
		{
			LOG.warn("Data error on Forum (root)!", e);
		}
	}
	
	public void initRoot()
	{
		for (Forum f : _table)
		{
			f.vload();
		}
		LOG.info("Loaded " + _table.size() + " forums. Last forum id used: " + _lastid);
	}
	
	public void addForum(Forum ff)
	{
		if (ff == null)
		{
			return;
		}
		
		_table.add(ff);
		
		if (ff.getID() > _lastid)
		{
			_lastid = ff.getID();
		}
	}
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
	}
	
	public Forum getForumByName(String name)
	{
		for (Forum f : _table)
		{
			if (f.getName().equals(name))
			{
				return f;
			}
		}
		return null;
	}
	
	public Forum createNewForum(String name, Forum parent, int type, int perm, int oid)
	{
		Forum forum = new Forum(name, parent, type, perm, oid);
		forum.insertIntoDb();
		return forum;
	}
	
	public int getANewID()
	{
		return ++_lastid;
	}
	
	public Forum getForumByID(int idf)
	{
		for (Forum f : _table)
		{
			if (f.getID() == idf)
			{
				return f;
			}
		}
		return null;
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
		
	}
	
	public static ForumsBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ForumsBBSManager _instance = new ForumsBBSManager();
	}
}