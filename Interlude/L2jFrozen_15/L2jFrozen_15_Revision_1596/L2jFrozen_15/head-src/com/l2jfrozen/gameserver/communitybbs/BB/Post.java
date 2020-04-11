package com.l2jfrozen.gameserver.communitybbs.BB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.communitybbs.Manager.PostBBSManager;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author Maktakien
 */
public class Post
{
	private static Logger LOGGER = Logger.getLogger(Post.class);
	
	public class CPost
	{
		public int postId;
		public String postOwner;
		public int postOwnerId;
		public long postDate;
		public int postTopicId;
		public int postForumId;
		public String postTxt;
	}
	
	private final List<CPost> post;
	
	// public enum ConstructorType {REPLY, CREATE };
	
	/**
	 * @param postOwner
	 * @param postOwnerID
	 * @param date
	 * @param tid
	 * @param postForumID
	 * @param txt
	 */
	public Post(final String postOwner, final int postOwnerID, final long date, final int tid, final int postForumID, final String txt)
	{
		post = new ArrayList<>();
		CPost cp = new CPost();
		cp.postId = 0;
		cp.postOwner = postOwner;
		cp.postOwnerId = postOwnerID;
		cp.postDate = date;
		cp.postTopicId = tid;
		cp.postForumId = postForumID;
		cp.postTxt = txt;
		post.add(cp);
		insertindb(cp);
		cp = null;
		
	}
	
	public void insertindb(final CPost cp)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO posts (post_id,post_owner_name,post_ownerid,post_date,post_topic_id,post_forum_id,post_txt) values (?,?,?,?,?,?,?)");
			statement.setInt(1, cp.postId);
			statement.setString(2, cp.postOwner);
			statement.setInt(3, cp.postOwnerId);
			statement.setLong(4, cp.postDate);
			statement.setInt(5, cp.postTopicId);
			statement.setInt(6, cp.postForumId);
			statement.setString(7, cp.postTxt);
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
			
			LOGGER.warn("error while saving new Post to db " + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
		
	}
	
	public Post(final Topic t)
	{
		post = new ArrayList<>();
		load(t);
	}
	
	public CPost getCPost(final int id)
	{
		int i = 0;
		
		for (final CPost cp : post)
		{
			if (i++ == id)
			{
				return cp;
			}
		}
		
		return null;
	}
	
	public void deleteme(final Topic t)
	{
		PostBBSManager.getInstance().delPostByTopic(t);
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM posts WHERE post_forum_id=? AND post_topic_id=?");
			statement.setInt(1, t.getForumID());
			statement.setInt(2, t.getID());
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	/**
	 * @param t
	 */
	private void load(final Topic t)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM posts WHERE post_forum_id=? AND post_topic_id=? ORDER BY post_id ASC");
			statement.setInt(1, t.getForumID());
			statement.setInt(2, t.getID());
			ResultSet result = statement.executeQuery();
			while (result.next())
			{
				CPost cp = new CPost();
				cp.postId = Integer.parseInt(result.getString("post_id"));
				cp.postOwner = result.getString("post_owner_name");
				cp.postOwnerId = Integer.parseInt(result.getString("post_ownerid"));
				cp.postDate = Long.parseLong(result.getString("post_date"));
				cp.postTopicId = Integer.parseInt(result.getString("post_topic_id"));
				cp.postForumId = Integer.parseInt(result.getString("post_forum_id"));
				cp.postTxt = result.getString("post_txt");
				post.add(cp);
				cp = null;
			}
			result.close();
			DatabaseUtils.close(statement);
			
			result = null;
			statement = null;
		}
		catch (final Exception e)
		{
			LOGGER.warn("data error on Post " + t.getForumID() + "/" + t.getID() + " : " + e);
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	/**
	 * @param i
	 */
	public void updatetxt(final int i)
	{
		Connection con = null;
		try
		{
			CPost cp = getCPost(i);
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE posts SET post_txt=? WHERE post_id=? AND post_topic_id=? AND post_forum_id=?");
			statement.setString(1, cp.postTxt);
			statement.setInt(2, cp.postId);
			statement.setInt(3, cp.postTopicId);
			statement.setInt(4, cp.postForumId);
			statement.execute();
			DatabaseUtils.close(statement);
			
			cp = null;
			statement = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.warn("error while saving new Post to db " + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
}