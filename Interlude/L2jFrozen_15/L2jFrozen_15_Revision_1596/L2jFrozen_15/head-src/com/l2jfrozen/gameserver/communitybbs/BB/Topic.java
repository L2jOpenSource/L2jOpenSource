package com.l2jfrozen.gameserver.communitybbs.BB;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.communitybbs.Manager.TopicBBSManager;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class Topic
{
	private static Logger LOGGER = Logger.getLogger(Topic.class);
	public static final int MORMAL = 0;
	public static final int MEMO = 1;
	
	private final int id;
	private final int forumId;
	private final String topicName;
	private final long date;
	private final String ownerName;
	private final int ownerId;
	private final int type;
	private final int cReply;
	
	/**
	 * @param ct
	 * @param id
	 * @param fid
	 * @param name
	 * @param date
	 * @param oname
	 * @param oid
	 * @param type
	 * @param Creply
	 */
	public Topic(final ConstructorType ct, final int id, final int fid, final String name, final long date, final String oname, final int oid, final int type, final int Creply)
	{
		this.id = id;
		forumId = fid;
		topicName = name;
		this.date = date;
		ownerName = oname;
		ownerId = oid;
		this.type = type;
		cReply = Creply;
		TopicBBSManager.getInstance().addTopic(this);
		
		if (ct == ConstructorType.CREATE)
		{
			insertindb();
		}
	}
	
	/**
	 *
	 */
	public void insertindb()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO topic (topic_id,topic_forum_id,topic_name,topic_date,topic_ownername,topic_ownerid,topic_type,topic_reply) values (?,?,?,?,?,?,?,?)");
			statement.setInt(1, id);
			statement.setInt(2, forumId);
			statement.setString(3, topicName);
			statement.setLong(4, date);
			statement.setString(5, ownerName);
			statement.setInt(6, ownerId);
			statement.setInt(7, type);
			statement.setInt(8, cReply);
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
			
			LOGGER.warn("error while saving new Topic to db " + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
		
	}
	
	public enum ConstructorType
	{
		RESTORE,
		CREATE
	}
	
	/**
	 * @return
	 */
	public int getID()
	{
		return id;
	}
	
	public int getForumID()
	{
		return forumId;
	}
	
	/**
	 * @return
	 */
	public String getName()
	{
		return topicName;
	}
	
	public String getOwnerName()
	{
		return ownerName;
	}
	
	public void deleteme(final Forum f)
	{
		TopicBBSManager.getInstance().delTopic(this);
		f.rmTopicByID(getID());
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM topic WHERE topic_id=? AND topic_forum_id=?");
			statement.setInt(1, getID());
			statement.setInt(2, f.getID());
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
	 * @return
	 */
	public long getDate()
	{
		return date;
	}
}
