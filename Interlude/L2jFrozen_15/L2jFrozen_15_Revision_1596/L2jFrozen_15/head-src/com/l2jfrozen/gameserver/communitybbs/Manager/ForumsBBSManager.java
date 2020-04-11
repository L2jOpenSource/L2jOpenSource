package com.l2jfrozen.gameserver.communitybbs.Manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.communitybbs.BB.Forum;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class ForumsBBSManager extends BaseBBSManager
{
	private static Logger LOGGER = Logger.getLogger(ForumsBBSManager.class);
	private final List<Forum> table = new CopyOnWriteArrayList<>();
	private static ForumsBBSManager instance;
	private int lastid = 1;
	
	public static ForumsBBSManager getInstance()
	{
		if (instance == null)
		{
			instance = new ForumsBBSManager();
		}
		return instance;
	}
	
	public ForumsBBSManager()
	{
		load();
	}
	
	public void addForum(final Forum ff)
	{
		if (ff == null)
		{
			return;
		}
		
		table.add(ff);
		
		if (ff.getID() > lastid)
		{
			lastid = ff.getID();
		}
	}
	
	/**
	 *
	 */
	private void load()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT forum_id FROM forums WHERE forum_type=0");
			ResultSet result = statement.executeQuery();
			while (result.next())
			{
				final Forum f = new Forum(result.getInt("forum_id"), null);
				addForum(f);
			}
			result.close();
			DatabaseUtils.close(statement);
			
			result = null;
			statement = null;
		}
		catch (final Exception e)
		{
			LOGGER.warn("data error on Forum (root): " + e);
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	public void initRoot()
	{
		for (final Forum f : table)
		{
			f.vload();
		}
		LOGGER.info("Loaded " + table.size() + " forums. Last forum id used: " + lastid);
	}
	
	@Override
	public void parsecmd(final String command, final L2PcInstance activeChar)
	{
		//
	}
	
	/**
	 * @param  Name
	 * @return
	 */
	public Forum getForumByName(final String Name)
	{
		for (final Forum f : table)
		{
			if (f.getName().equals(Name))
			{
				return f;
			}
		}
		
		return null;
	}
	
	/**
	 * @param  name
	 * @param  parent
	 * @param  type
	 * @param  perm
	 * @param  oid
	 * @return
	 */
	public Forum createNewForum(final String name, final Forum parent, final int type, final int perm, final int oid)
	{
		final Forum forum = new Forum(name, parent, type, perm, oid);
		forum.insertindb();
		return forum;
	}
	
	/**
	 * @return
	 */
	public int getANewID()
	{
		return ++lastid;
	}
	
	/**
	 * @param  idf
	 * @return
	 */
	public Forum getForumByID(final int idf)
	{
		for (final Forum f : table)
		{
			if (f.getID() == idf)
			{
				return f;
			}
		}
		return null;
	}
	
	@Override
	public void parsewrite(final String ar1, final String ar2, final String ar3, final String ar4, final String ar5, final L2PcInstance activeChar)
	{
		//
	}
}
