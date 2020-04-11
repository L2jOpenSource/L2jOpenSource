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

import static l2r.gameserver.taskmanager.TaskTypes.TYPE_NONE;
import static l2r.gameserver.taskmanager.TaskTypes.TYPE_SHEDULED;
import static l2r.gameserver.taskmanager.TaskTypes.TYPE_TIME;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;
import l2r.L2DatabaseFactory;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.taskmanager.tasks.SoIStageUpdater;
import l2r.gameserver.taskmanager.tasks.TaskAdvent;
import l2r.gameserver.taskmanager.tasks.TaskBirthday;
import l2r.gameserver.taskmanager.tasks.TaskClanLeaderApply;
import l2r.gameserver.taskmanager.tasks.TaskCleanUp;
import l2r.gameserver.taskmanager.tasks.TaskDailySkillReuseClean;
import l2r.gameserver.taskmanager.tasks.TaskGlobalVariablesSave;
import l2r.gameserver.taskmanager.tasks.TaskJython;
import l2r.gameserver.taskmanager.tasks.TaskOlympiadSave;
import l2r.gameserver.taskmanager.tasks.TaskRaidPointsReset;
import l2r.gameserver.taskmanager.tasks.TaskRecom;
import l2r.gameserver.taskmanager.tasks.TaskRestart;
import l2r.gameserver.taskmanager.tasks.TaskScript;
import l2r.gameserver.taskmanager.tasks.TaskSevenSignsUpdate;
import l2r.gameserver.taskmanager.tasks.TaskShutdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Layane
 */
public final class TaskManager
{
	protected static final Logger _log = LoggerFactory.getLogger(TaskManager.class);
	
	private final Map<Integer, Task> _tasks = new ConcurrentHashMap<>();
	protected final List<ExecutedTask> _currentTasks = new FastList<ExecutedTask>().shared();
	
	protected static final String[] SQL_STATEMENTS =
	{
		"SELECT id,task,type,last_activation,param1,param2,param3 FROM global_tasks",
		"UPDATE global_tasks SET last_activation=? WHERE id=?",
		"SELECT id FROM global_tasks WHERE task=?",
		"INSERT INTO global_tasks (task,type,last_activation,param1,param2,param3) VALUES(?,?,?,?,?,?)"
	};
	
	protected TaskManager()
	{
		initializate();
		startAllTasks();
		_log.info(getClass().getSimpleName() + ": Loaded: " + _tasks.size() + " Tasks");
	}
	
	public class ExecutedTask implements Runnable
	{
		int id;
		long lastActivation;
		Task task;
		TaskTypes type;
		String[] params;
		ScheduledFuture<?> scheduled;
		
		public ExecutedTask(Task ptask, TaskTypes ptype, ResultSet rset) throws SQLException
		{
			task = ptask;
			type = ptype;
			id = rset.getInt("id");
			lastActivation = rset.getLong("last_activation");
			params = new String[]
			{
				rset.getString("param1"),
				rset.getString("param2"),
				rset.getString("param3")
			};
		}
		
		@Override
		public void run()
		{
			task.onTimeElapsed(this);
			lastActivation = System.currentTimeMillis();
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(SQL_STATEMENTS[1]))
			{
				statement.setLong(1, lastActivation);
				statement.setInt(2, id);
				statement.executeUpdate();
			}
			catch (SQLException e)
			{
				_log.warn(getClass().getSimpleName() + ": Cannot updated the Global Task " + id + ": " + e.getMessage(), e);
			}
			
			if ((type == TYPE_SHEDULED) || (type == TYPE_TIME))
			{
				stopTask();
			}
		}
		
		@Override
		public boolean equals(Object object)
		{
			if (this == object)
			{
				return true;
			}
			if (!(object instanceof ExecutedTask))
			{
				return false;
			}
			return id == ((ExecutedTask) object).id;
		}
		
		@Override
		public int hashCode()
		{
			return id;
		}
		
		public Task getTask()
		{
			return task;
		}
		
		public TaskTypes getType()
		{
			return type;
		}
		
		public int getId()
		{
			return id;
		}
		
		public String[] getParams()
		{
			return params;
		}
		
		public long getLastActivation()
		{
			return lastActivation;
		}
		
		public void stopTask()
		{
			task.onDestroy();
			
			if (scheduled != null)
			{
				scheduled.cancel(true);
			}
			
			_currentTasks.remove(this);
		}
		
	}
	
	private void initializate()
	{
		registerTask(new SoIStageUpdater());
		registerTask(new TaskAdvent());
		registerTask(new TaskBirthday());
		registerTask(new TaskClanLeaderApply());
		registerTask(new TaskCleanUp());
		registerTask(new TaskDailySkillReuseClean());
		registerTask(new TaskGlobalVariablesSave());
		registerTask(new TaskJython());
		registerTask(new TaskOlympiadSave());
		registerTask(new TaskRaidPointsReset());
		registerTask(new TaskRecom());
		registerTask(new TaskRestart());
		registerTask(new TaskScript());
		registerTask(new TaskSevenSignsUpdate());
		registerTask(new TaskShutdown());
	}
	
	public void registerTask(Task task)
	{
		int key = task.getName().hashCode();
		_tasks.computeIfAbsent(key, k ->
		{
			task.initializate();
			return task;
		});
	}
	
	private void startAllTasks()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SQL_STATEMENTS[0]);
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				Task task = _tasks.get(rset.getString("task").trim().toLowerCase().hashCode());
				if (task == null)
				{
					continue;
				}
				
				final TaskTypes type = TaskTypes.valueOf(rset.getString("type"));
				if (type != TYPE_NONE)
				{
					ExecutedTask current = new ExecutedTask(task, type, rset);
					if (launchTask(current))
					{
						_currentTasks.add(current);
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.error(getClass().getSimpleName() + ": Error while loading Global Task table: " + e.getMessage(), e);
		}
	}
	
	private boolean launchTask(ExecutedTask task)
	{
		final ThreadPoolManager scheduler = ThreadPoolManager.getInstance();
		final TaskTypes type = task.getType();
		long delay, interval;
		switch (type)
		{
			case TYPE_STARTUP:
				task.run();
				return false;
			case TYPE_SHEDULED:
				delay = Long.valueOf(task.getParams()[0]);
				task.scheduled = scheduler.scheduleGeneral(task, delay);
				return true;
			case TYPE_FIXED_SHEDULED:
				delay = Long.valueOf(task.getParams()[0]);
				interval = Long.valueOf(task.getParams()[1]);
				task.scheduled = scheduler.scheduleGeneralAtFixedRate(task, delay, interval);
				return true;
			case TYPE_TIME:
				try
				{
					Date desired = DateFormat.getInstance().parse(task.getParams()[0]);
					long diff = desired.getTime() - System.currentTimeMillis();
					if (diff >= 0)
					{
						task.scheduled = scheduler.scheduleGeneral(task, diff);
						return true;
					}
					_log.info(getClass().getSimpleName() + ": Task " + task.getId() + " is obsoleted.");
				}
				catch (Exception e)
				{
				}
				break;
			case TYPE_SPECIAL:
				ScheduledFuture<?> result = task.getTask().launchSpecial(task);
				if (result != null)
				{
					task.scheduled = result;
					return true;
				}
				break;
			case TYPE_GLOBAL_TASK:
				interval = Long.valueOf(task.getParams()[0]) * 86400000L;
				String[] hour = task.getParams()[1].split(":");
				
				if (hour.length != 3)
				{
					_log.warn(getClass().getSimpleName() + ": Task " + task.getId() + " has incorrect parameters");
					return false;
				}
				
				Calendar check = Calendar.getInstance();
				check.setTimeInMillis(task.getLastActivation() + interval);
				
				Calendar min = Calendar.getInstance();
				try
				{
					min.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour[0]));
					min.set(Calendar.MINUTE, Integer.parseInt(hour[1]));
					min.set(Calendar.SECOND, Integer.parseInt(hour[2]));
				}
				catch (Exception e)
				{
					_log.warn(getClass().getSimpleName() + ": Bad parameter on task " + task.getId() + ": " + e.getMessage(), e);
					return false;
				}
				
				delay = min.getTimeInMillis() - System.currentTimeMillis();
				
				if (check.after(min) || (delay < 0))
				{
					delay += interval;
				}
				task.scheduled = scheduler.scheduleGeneralAtFixedRate(task, delay, interval);
				return true;
			default:
				return false;
		}
		return false;
	}
	
	public static boolean addUniqueTask(String task, TaskTypes type, String param1, String param2, String param3)
	{
		return addUniqueTask(task, type, param1, param2, param3, 0);
	}
	
	public static boolean addUniqueTask(String task, TaskTypes type, String param1, String param2, String param3, long lastActivation)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps1 = con.prepareStatement(SQL_STATEMENTS[2]))
		{
			ps1.setString(1, task);
			try (ResultSet rs = ps1.executeQuery())
			{
				if (!rs.next())
				{
					try (PreparedStatement ps2 = con.prepareStatement(SQL_STATEMENTS[3]))
					{
						ps2.setString(1, task);
						ps2.setString(2, type.toString());
						ps2.setLong(3, lastActivation);
						ps2.setString(4, param1);
						ps2.setString(5, param2);
						ps2.setString(6, param3);
						ps2.execute();
					}
				}
			}
			return true;
		}
		catch (SQLException e)
		{
			_log.warn(TaskManager.class.getSimpleName() + ": Cannot add the unique task: " + e.getMessage(), e);
		}
		return false;
	}
	
	public static boolean addTask(String task, TaskTypes type, String param1, String param2, String param3)
	{
		return addTask(task, type, param1, param2, param3, 0);
	}
	
	public static boolean addTask(String task, TaskTypes type, String param1, String param2, String param3, long lastActivation)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SQL_STATEMENTS[3]))
		{
			statement.setString(1, task);
			statement.setString(2, type.toString());
			statement.setLong(3, lastActivation);
			statement.setString(4, param1);
			statement.setString(5, param2);
			statement.setString(6, param3);
			statement.execute();
			return true;
		}
		catch (SQLException e)
		{
			_log.warn(TaskManager.class.getSimpleName() + ": Cannot add the task:  " + e.getMessage(), e);
		}
		return false;
	}
	
	public static TaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final TaskManager _instance = new TaskManager();
	}
}