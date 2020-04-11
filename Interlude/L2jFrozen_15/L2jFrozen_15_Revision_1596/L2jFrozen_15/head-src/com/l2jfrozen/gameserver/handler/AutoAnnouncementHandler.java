package com.l2jfrozen.gameserver.handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

import javolution.text.TextBuilder;

/**
 * Auto Announcment Handler Automatically send announcment at a set time interval.
 * @author chief
 */
public class AutoAnnouncementHandler
{
	protected static final Logger LOGGER = Logger.getLogger(AutoAnnouncementHandler.class);
	private static AutoAnnouncementHandler instance;
	private static final long DEFAULT_ANNOUNCEMENT_DELAY = 180000; // 3 mins by default
	protected Map<Integer, AutoAnnouncementInstance> registeredAnnouncements;
	
	protected AutoAnnouncementHandler()
	{
		registeredAnnouncements = new HashMap<>();
		restoreAnnouncementData();
	}
	
	private void restoreAnnouncementData()
	{
		int numLoaded = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			statement = con.prepareStatement("SELECT * FROM auto_announcements ORDER BY id");
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				numLoaded++;
				
				registerGlobalAnnouncement(rs.getInt("id"), rs.getString("announcement"), rs.getLong("delay"));
				
			}
			
			DatabaseUtils.close(statement);
			rs.close();
			statement = null;
			rs = null;
			
			if (numLoaded > 0)
			{
				LOGGER.info("GameServer: Loaded " + numLoaded + " Auto Announcements.");
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			// ignore
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
			
		}
	}
	
	/**
	 * @param activeChar
	 */
	public void listAutoAnnouncements(final L2PcInstance activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		TextBuilder replyMSG = new TextBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40></td>");
		replyMSG.append("<button value=\"Main\" action=\"bypass -h admin_admin\" width=50 height=15 back=\"L2UI_ct1.button_df\" " + "fore=\"L2UI_ct1.button_df\"><br>");
		
		replyMSG.append("<td width=180><center>Auto Announcement Menu</center></td>");
		replyMSG.append("<td width=40></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>Add new auto announcement:</center>");
		replyMSG.append("<center><multiedit var=\"new_autoannouncement\" width=240 height=30></center><br>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>Delay: <edit var=\"delay\" width=70></center>");
		replyMSG.append("<center>Note: Time in Seconds 60s = 1 min.</center>");
		replyMSG.append("<center>Note2: Minimum Time is 30 Seconds.</center>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center><table><tr><td>");
		replyMSG.append("<button value=\"Add\" action=\"bypass -h admin_add_autoannouncement $delay $new_autoannouncement\" width=60 " + "height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td>");
		replyMSG.append("</td></tr></table></center>");
		replyMSG.append("<br>");
		
		for (final AutoAnnouncementInstance announcementInst : AutoAnnouncementHandler.getInstance().values())
		{
			replyMSG.append("<table width=260><tr><td width=220>[" + announcementInst.getDefaultDelay() + "s] " + announcementInst.getDefaultTexts().toString() + "</td><td width=40>");
			replyMSG.append("<button value=\"Delete\" action=\"bypass -h admin_del_autoannouncement " + announcementInst.getDefaultId() + "\" width=60 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>");
		}
		
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
		adminReply = null;
		replyMSG = null;
	}
	
	/**
	 * @return
	 */
	public static AutoAnnouncementHandler getInstance()
	{
		if (instance == null)
		{
			instance = new AutoAnnouncementHandler();
		}
		
		return instance;
	}
	
	/**
	 * @return
	 */
	public int size()
	{
		return registeredAnnouncements.size();
	}
	
	/**
	 * Registers a globally active autoannouncement.<BR>
	 * Returns the associated auto announcement instance.
	 * @param  id
	 * @param  announcementTexts
	 * @param  announcementDelay announcementDelay (-1 = default delay)
	 * @return                   AutoAnnouncementInstance announcementInst
	 */
	public AutoAnnouncementInstance registerGlobalAnnouncement(final int id, final String announcementTexts, final long announcementDelay)
	{
		return registerAnnouncement(id, announcementTexts, announcementDelay);
	}
	
	/**
	 * Registers a NON globally-active auto announcement <BR>
	 * Returns the associated auto chat instance.
	 * @param  id
	 * @param  announcementTexts
	 * @param  announcementDelay announcementDelay (-1 = default delay)
	 * @return                   AutoAnnouncementInstance announcementInst
	 */
	public AutoAnnouncementInstance registerAnnouncment(final int id, final String announcementTexts, final long announcementDelay)
	{
		return registerAnnouncement(id, announcementTexts, announcementDelay);
	}
	
	public AutoAnnouncementInstance registerAnnouncment(final String announcementTexts, final long announcementDelay)
	{
		final int nextId = nextAutoAnnouncmentId();
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO auto_announcements (id,announcement,delay) " + "VALUES (?,?,?)");
			statement.setInt(1, nextId);
			statement.setString(2, announcementTexts);
			statement.setLong(3, announcementDelay);
			
			statement.executeUpdate();
			
			DatabaseUtils.close(statement);
			statement = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.error("System: Could Not Insert Auto Announcment into DataBase: Reason: " + "Duplicate Id");
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
			
		}
		return registerAnnouncement(nextId, announcementTexts, announcementDelay);
	}
	
	public int nextAutoAnnouncmentId()
	{
		
		int nextId = 0;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			statement = con.prepareStatement("SELECT id FROM auto_announcements ORDER BY id");
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				if (rs.getInt("id") > nextId)
				{
					nextId = rs.getInt("id");
				}
			}
			
			DatabaseUtils.close(statement);
			rs.close();
			statement = null;
			rs = null;
			
			nextId++;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
			
		}
		return nextId;
	}
	
	/**
	 * @param  id
	 * @param  announcementTexts
	 * @param  chatDelay
	 * @return
	 */
	private final AutoAnnouncementInstance registerAnnouncement(final int id, final String announcementTexts, long chatDelay)
	{
		AutoAnnouncementInstance announcementInst = null;
		
		if (chatDelay < 0)
		{
			chatDelay = DEFAULT_ANNOUNCEMENT_DELAY;
		}
		
		if (registeredAnnouncements.containsKey(id))
		{
			announcementInst = registeredAnnouncements.get(id);
		}
		else
		{
			announcementInst = new AutoAnnouncementInstance(id, announcementTexts, chatDelay);
		}
		
		registeredAnnouncements.put(id, announcementInst);
		
		return announcementInst;
	}
	
	/**
	 * @return
	 */
	public Collection<AutoAnnouncementInstance> values()
	{
		return registeredAnnouncements.values();
	}
	
	/**
	 * Removes and cancels ALL auto announcement for the given announcement id.
	 * @param  id
	 * @return    boolean removedSuccessfully
	 */
	public boolean removeAnnouncement(final int id)
	{
		final AutoAnnouncementInstance announcementInst = registeredAnnouncements.get(id);
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM auto_announcements WHERE id=?");
			statement.setInt(1, announcementInst.getDefaultId());
			statement.executeUpdate();
			DatabaseUtils.close(statement);
			statement = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.error("Could not Delete Auto Announcement in Database, Reason:", e);
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
			
		}
		
		return removeAnnouncement(announcementInst);
	}
	
	/**
	 * Removes and cancels ALL auto announcement for the given announcement instance.
	 * @param  announcementInst
	 * @return                  boolean removedSuccessfully
	 */
	public boolean removeAnnouncement(final AutoAnnouncementInstance announcementInst)
	{
		if (announcementInst == null)
		{
			return false;
		}
		
		registeredAnnouncements.remove(announcementInst.getDefaultId());
		announcementInst.setActive(false);
		
		return true;
	}
	
	/**
	 * Returns the associated auto announcement instance either by the given announcement ID or object ID.
	 * @param  id
	 * @return    AutoAnnouncementInstance announcementInst
	 */
	public AutoAnnouncementInstance getAutoAnnouncementInstance(final int id)
	{
		return registeredAnnouncements.get(id);
	}
	
	/**
	 * Sets the active state of all auto announcement instances to that specified, and cancels the scheduled chat task if necessary.
	 * @param isActive
	 */
	public void setAutoAnnouncementActive(final boolean isActive)
	{
		for (final AutoAnnouncementInstance announcementInst : registeredAnnouncements.values())
		{
			announcementInst.setActive(isActive);
		}
	}
	
	/**
	 * Auto Announcement Instance
	 */
	public class AutoAnnouncementInstance
	{
		private long defaultDelay = DEFAULT_ANNOUNCEMENT_DELAY;
		private String defaultTexts;
		private boolean defaultRandom = false;
		private final Integer defaultId;
		
		private boolean isActive;
		
		public ScheduledFuture<?> chatTask;
		
		/**
		 * @param id
		 * @param announcementTexts
		 * @param announcementDelay
		 */
		protected AutoAnnouncementInstance(final int id, final String announcementTexts, final long announcementDelay)
		{
			defaultId = id;
			defaultTexts = announcementTexts;
			defaultDelay = announcementDelay * 1000;
			
			setActive(true);
		}
		
		/**
		 * @return
		 */
		public boolean isActive()
		{
			return isActive;
		}
		
		/**
		 * @return
		 */
		public boolean isDefaultRandom()
		{
			return defaultRandom;
		}
		
		/**
		 * @return
		 */
		public long getDefaultDelay()
		{
			return defaultDelay;
		}
		
		/**
		 * @return
		 */
		public String getDefaultTexts()
		{
			return defaultTexts;
		}
		
		/**
		 * @return
		 */
		public Integer getDefaultId()
		{
			return defaultId;
		}
		
		/**
		 * @param delayValue
		 */
		public void setDefaultChatDelay(final long delayValue)
		{
			defaultDelay = delayValue;
		}
		
		/**
		 * @param textsValue
		 */
		public void setDefaultChatTexts(final String textsValue)
		{
			defaultTexts = textsValue;
		}
		
		/**
		 * @param randValue
		 */
		public void setDefaultRandom(final boolean randValue)
		{
			defaultRandom = randValue;
		}
		
		/**
		 * @param activeValue
		 */
		public void setActive(final boolean activeValue)
		{
			if (isActive == activeValue)
			{
				return;
			}
			
			isActive = activeValue;
			
			if (isActive())
			{
				AutoAnnouncementRunner acr = new AutoAnnouncementRunner(defaultId);
				chatTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(acr, defaultDelay, defaultDelay);
				acr = null;
			}
			else
			{
				chatTask.cancel(false);
			}
		}
		
		/**
		 * Auto Announcement Runner <BR>
		 * <BR>
		 * Represents the auto announcement scheduled task for each announcement instance.
		 * @author chief
		 */
		private class AutoAnnouncementRunner implements Runnable
		{
			protected int id;
			
			protected AutoAnnouncementRunner(final int pId)
			{
				id = pId;
			}
			
			@Override
			public synchronized void run()
			{
				AutoAnnouncementInstance announcementInst = registeredAnnouncements.get(id);
				
				String text;
				
				text = announcementInst.getDefaultTexts();
				
				if (text == null)
				{
					return;
				}
				
				Announcements.getInstance().announceToAll(text);
				text = null;
				announcementInst = null;
			}
		}
	}
}
