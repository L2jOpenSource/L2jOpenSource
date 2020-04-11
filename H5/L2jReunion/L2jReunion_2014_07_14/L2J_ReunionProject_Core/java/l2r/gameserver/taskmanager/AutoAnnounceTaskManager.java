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
package l2r.gameserver.taskmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import javolution.util.FastList;
import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.util.Broadcast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nBd
 */
public class AutoAnnounceTaskManager
{
	private static final Logger _log = LoggerFactory.getLogger(AutoAnnounceTaskManager.class);
	
	protected final List<AutoAnnouncement> _announces = new FastList<>();
	private int _nextId = 1;
	
	protected AutoAnnounceTaskManager()
	{
		restore();
	}
	
	public List<AutoAnnouncement> getAutoAnnouncements()
	{
		return _announces;
	}
	
	public void restore()
	{
		if (!_announces.isEmpty())
		{
			for (AutoAnnouncement a : _announces)
			{
				a.stopAnnounce();
			}
			
			_announces.clear();
		}
		
		int count = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet data = s.executeQuery("SELECT * FROM auto_announcements"))
		{
			while (data.next())
			{
				int id = data.getInt("id");
				long initial = data.getLong("initial");
				long delay = data.getLong("delay");
				int repeat = data.getInt("cycle");
				String memo = data.getString("memo");
				boolean isCritical = Boolean.parseBoolean(data.getString("isCritical"));
				String[] text = memo.split("/n");
				ThreadPoolManager.getInstance().scheduleGeneral(new AutoAnnouncement(id, delay, repeat, text, isCritical), initial);
				count++;
				if (_nextId <= id)
				{
					_nextId = id + 1;
				}
			}
		}
		catch (Exception e)
		{
			_log.error("AutoAnnoucements: Failed to load announcements data.", e);
		}
		_log.info("AutoAnnoucements: Loaded " + count + " Auto Annoucement Data.");
	}
	
	public void addAutoAnnounce(long initial, long delay, int repeat, String memo, boolean isCritical)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO auto_announcements (id, initial, delay, cycle, memo, isCritical) VALUES (?,?,?,?,?,?)"))
		{
			statement.setInt(1, _nextId);
			statement.setLong(2, initial);
			statement.setLong(3, delay);
			statement.setInt(4, repeat);
			statement.setString(5, memo);
			statement.setString(6, String.valueOf(isCritical));
			statement.execute();
			
			String[] text = memo.split("/n");
			ThreadPoolManager.getInstance().scheduleGeneral(new AutoAnnouncement(_nextId++, delay, repeat, text, isCritical), initial);
		}
		catch (Exception e)
		{
			_log.error("AutoAnnoucements: Failed to add announcements data.", e);
		}
	}
	
	public void deleteAutoAnnounce(int index)
	{
		AutoAnnouncement a = _announces.remove(index);
		a.stopAnnounce();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM auto_announcements WHERE id = ?"))
		{
			statement.setInt(1, a.getId());
			statement.execute();
			
		}
		catch (Exception e)
		{
			_log.error("AutoAnnoucements: Failed to delete announcements data.", e);
		}
	}
	
	public class AutoAnnouncement implements Runnable
	{
		private final int _id;
		private final long _delay;
		private int _repeat = -1;
		private final String[] _memo;
		private boolean _stopped = false;
		private final boolean _isCritical;
		
		public AutoAnnouncement(int id, long delay, int repeat, String[] memo, boolean isCritical)
		{
			_id = id;
			_delay = delay;
			_repeat = repeat;
			_memo = memo;
			_isCritical = isCritical;
			if (!_announces.contains(this))
			{
				_announces.add(this);
			}
		}
		
		public int getId()
		{
			return _id;
		}
		
		public String[] getMemo()
		{
			return _memo;
		}
		
		public void stopAnnounce()
		{
			_stopped = true;
		}
		
		public boolean isCritical()
		{
			return _isCritical;
		}
		
		@Override
		public void run()
		{
			if (!_stopped && (_repeat != 0))
			{
				for (String text : _memo)
				{
					announce(text, _isCritical);
				}
				
				if (_repeat > 0)
				{
					_repeat--;
				}
				ThreadPoolManager.getInstance().scheduleGeneral(this, _delay);
			}
			else
			{
				stopAnnounce();
			}
		}
	}
	
	public void announce(String text, boolean isCritical)
	{
		Broadcast.announceToOnlinePlayers(text, isCritical);
		if (Config.LOG_AUTO_ANNOUNCEMENTS)
		{
			_log.info((isCritical ? "Critical AutoAnnounce" : "AutoAnnounce") + ": " + text);
		}
	}
	
	public static AutoAnnounceTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AutoAnnounceTaskManager _instance = new AutoAnnounceTaskManager();
	}
}
