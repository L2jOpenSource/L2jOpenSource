package com.l2jfrozen.gameserver.communitybbs.BB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.communitybbs.Manager.ForumsBBSManager;
import com.l2jfrozen.gameserver.communitybbs.Manager.TopicBBSManager;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class Forum
{
	// type
	public static final int ROOT = 0;
	public static final int NORMAL = 1;
	public static final int CLAN = 2;
	public static final int MEMO = 3;
	public static final int MAIL = 4;
	// perm
	public static final int INVISIBLE = 0;
	public static final int ALL = 1;
	public static final int CLANMEMBERONLY = 2;
	public static final int OWNERONLY = 3;
	
	private static Logger LOGGER = Logger.getLogger(Forum.class);
	private final List<Forum> children;
	private final Map<Integer, Topic> topic;
	private final int forumId;
	private String forumName;
	// private int forumParent;
	private int forumType;
	private int forumPost;
	private int forumPerm;
	private final Forum fParent;
	private int ownerID;
	private boolean loaded = false;
	
	/**
	 * @param Forumid
	 * @param FParent
	 */
	public Forum(final int Forumid, final Forum FParent)
	{
		forumId = Forumid;
		fParent = FParent;
		children = new ArrayList<>();
		topic = new HashMap<>();
	}
	
	/**
	 * @param name
	 * @param parent
	 * @param type
	 * @param perm
	 * @param OwnerID
	 */
	public Forum(final String name, final Forum parent, final int type, final int perm, final int OwnerID)
	{
		forumName = name;
		forumId = ForumsBBSManager.getInstance().getANewID();
		// forumParent = parent.getID();
		forumType = type;
		forumPost = 0;
		forumPerm = perm;
		fParent = parent;
		ownerID = OwnerID;
		children = new ArrayList<>();
		topic = new HashMap<>();
		parent.children.add(this);
		ForumsBBSManager.getInstance().addForum(this);
		loaded = true;
	}
	
	private void load()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM forums WHERE forum_id=?");
			statement.setInt(1, forumId);
			ResultSet result = statement.executeQuery();
			
			if (result.next())
			{
				forumName = result.getString("forum_name");
				// forumParent = Integer.parseInt(result.getString("forum_parent"));
				forumPost = Integer.parseInt(result.getString("forum_post"));
				forumType = Integer.parseInt(result.getString("forum_type"));
				forumPerm = Integer.parseInt(result.getString("forum_perm"));
				ownerID = Integer.parseInt(result.getString("forum_owner_id"));
			}
			result.close();
			DatabaseUtils.close(statement);
			
			result = null;
			statement = null;
		}
		catch (final Exception e)
		{
			LOGGER.warn("data error on Forum " + forumId + " : " + e);
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
		}
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM topic WHERE topic_forum_id=? ORDER BY topic_id DESC");
			statement.setInt(1, forumId);
			ResultSet result = statement.executeQuery();
			
			while (result.next())
			{
				Topic t = new Topic(Topic.ConstructorType.RESTORE, Integer.parseInt(result.getString("topic_id")), Integer.parseInt(result.getString("topic_forum_id")), result.getString("topic_name"), Long.parseLong(result.getString("topic_date")), result.getString("topic_ownername"), Integer.parseInt(result.getString("topic_ownerid")), Integer.parseInt(result.getString("topic_type")), Integer.parseInt(result.getString("topic_reply")));
				topic.put(t.getID(), t);
				if (t.getID() > TopicBBSManager.getInstance().getMaxID(this))
				{
					TopicBBSManager.getInstance().setMaxID(t.getID(), this);
				}
				t = null;
			}
			result.close();
			DatabaseUtils.close(statement);
			
			result = null;
			statement = null;
		}
		catch (final Exception e)
		{
			LOGGER.warn("data error on Forum " + forumId + " : " + e);
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	private void getChildren()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT forum_id FROM forums WHERE forum_parent=?");
			statement.setInt(1, forumId);
			ResultSet result = statement.executeQuery();
			
			while (result.next())
			{
				final Forum f = new Forum(result.getInt("forum_id"), this);
				children.add(f);
				ForumsBBSManager.getInstance().addForum(f);
			}
			result.close();
			DatabaseUtils.close(statement);
			
			result = null;
			statement = null;
		}
		catch (final Exception e)
		{
			LOGGER.warn("data error on Forum (children): " + e);
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
		}
		
	}
	
	public int getTopicSize()
	{
		vload();
		return topic.size();
	}
	
	public Topic gettopic(final int j)
	{
		vload();
		return topic.get(j);
	}
	
	public void addtopic(final Topic t)
	{
		vload();
		topic.put(t.getID(), t);
	}
	
	public int getID()
	{
		return forumId;
	}
	
	public String getName()
	{
		vload();
		return forumName;
	}
	
	public int getType()
	{
		vload();
		return forumType;
	}
	
	/**
	 * @param  name
	 * @return
	 */
	public Forum getChildByName(final String name)
	{
		vload();
		
		for (final Forum f : children)
		{
			if (f == null || f.getName() == null)
			{
				continue;
			}
			
			if (f.getName().equals(name))
			{
				return f;
			}
		}
		return null;
	}
	
	public void rmTopicByID(final int id)
	{
		topic.remove(id);
	}
	
	public void insertindb()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO forums (forum_id,forum_name,forum_parent,forum_post,forum_type,forum_perm,forum_owner_id) values (?,?,?,?,?,?,?)");
			statement.setInt(1, forumId);
			statement.setString(2, forumName);
			statement.setInt(3, fParent.getID());
			statement.setInt(4, forumPost);
			statement.setInt(5, forumType);
			statement.setInt(6, forumPerm);
			statement.setInt(7, ownerID);
			statement.execute();
			DatabaseUtils.close(statement);
			
			statement = null;
			
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.warn("error while saving new Forum to db " + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	public void vload()
	{
		if (!loaded)
		{
			load();
			getChildren();
			loaded = true;
		}
	}
}
