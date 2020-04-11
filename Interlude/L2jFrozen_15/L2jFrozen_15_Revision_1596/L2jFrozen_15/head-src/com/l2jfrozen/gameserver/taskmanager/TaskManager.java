package com.l2jfrozen.gameserver.taskmanager;

import static com.l2jfrozen.gameserver.taskmanager.TaskTypes.TYPE_FIXED_SHEDULED;
import static com.l2jfrozen.gameserver.taskmanager.TaskTypes.TYPE_GLOBAL_TASK;
import static com.l2jfrozen.gameserver.taskmanager.TaskTypes.TYPE_NONE;
import static com.l2jfrozen.gameserver.taskmanager.TaskTypes.TYPE_SHEDULED;
import static com.l2jfrozen.gameserver.taskmanager.TaskTypes.TYPE_SPECIAL;
import static com.l2jfrozen.gameserver.taskmanager.TaskTypes.TYPE_STARTUP;
import static com.l2jfrozen.gameserver.taskmanager.TaskTypes.TYPE_TIME;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.taskmanager.tasks.TaskRaidPointsReset;
import com.l2jfrozen.gameserver.taskmanager.tasks.TaskRecom;
import com.l2jfrozen.gameserver.taskmanager.tasks.TaskRestart;
import com.l2jfrozen.gameserver.taskmanager.tasks.TaskSevenSignsUpdate;
import com.l2jfrozen.gameserver.taskmanager.tasks.TaskShutdown;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author ProGramMoS
 */
public final class TaskManager
{
	protected static final Logger LOGGER = Logger.getLogger(TaskManager.class);
	private static TaskManager instance;
	private static final String SELECT_GLOBAL_TASKS = "SELECT id,task,type,last_activation,param1,param2,param3 FROM global_tasks";
	private static final String UPDATE_GLOBAL_TASK_LAST_ACTIVATION = "UPDATE global_tasks SET last_activation=? WHERE id=?";
	private static final String SELECT_GLOBAL_TASK_ID_BY_TASK = "SELECT id FROM global_tasks WHERE task=?";
	private static final String INSERT_GLOBAL_TASK = "INSERT INTO global_tasks (task,type,last_activation,param1,param2,param3) VALUES(?,?,?,?,?,?)";
	
	private final Map<Integer, Task> tasks = new HashMap<>();
	protected final List<ExecutedTask> currentTasks = new ArrayList<>();
	
	public class ExecutedTask implements Runnable
	{
		int id;
		long lastActivation;
		
		Task task;
		TaskTypes type;
		String[] params;
		ScheduledFuture<?> scheduled;
		
		public ExecutedTask(final Task ptask, final TaskTypes ptype, final ResultSet rset) throws SQLException
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
				PreparedStatement statement = con.prepareStatement(UPDATE_GLOBAL_TASK_LAST_ACTIVATION))
			{
				statement.setLong(1, lastActivation);
				statement.setInt(2, id);
				statement.executeUpdate();
			}
			catch (SQLException e)
			{
				LOGGER.warn("cannot updated the Global Task " + id + ": " + e.getMessage());
			}
			
			if (type == TYPE_SHEDULED || type == TYPE_TIME)
			{
				stopTask();
			}
		}
		
		@Override
		public boolean equals(final Object object)
		{
			if (object == null)
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
			
			currentTasks.remove(this);
		}
		
	}
	
	public static TaskManager getInstance()
	{
		if (instance == null)
		{
			instance = new TaskManager();
		}
		return instance;
	}
	
	private TaskManager()
	{
		initializate();
		startAllTasks();
	}
	
	private void initializate()
	{
		registerTask(new TaskRaidPointsReset());
		registerTask(new TaskRecom());
		registerTask(new TaskRestart());
		registerTask(new TaskSevenSignsUpdate());
		registerTask(new TaskShutdown());
	}
	
	public void registerTask(final Task task)
	{
		final int key = task.getName().hashCode();
		if (!tasks.containsKey(key))
		{
			tasks.put(key, task);
			task.initializate();
		}
	}
	
	private void startAllTasks()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_GLOBAL_TASKS);
			ResultSet rset = statement.executeQuery();)
		{
			while (rset.next())
			{
				Task task = tasks.get(rset.getString("task").trim().toLowerCase().hashCode());
				
				if (task == null)
				{
					continue;
				}
				
				TaskTypes type = TaskTypes.valueOf(rset.getString("type"));
				
				if (type != TYPE_NONE)
				{
					ExecutedTask current = new ExecutedTask(task, type, rset);
					if (launchTask(current))
					{
						currentTasks.add(current);
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Error while loading Global Task table ", e);
		}
	}
	
	private boolean launchTask(final ExecutedTask task)
	{
		final ThreadPoolManager scheduler = ThreadPoolManager.getInstance();
		final TaskTypes type = task.getType();
		
		if (type == TYPE_STARTUP)
		{
			task.run();
			return false;
		}
		else if (type == TYPE_SHEDULED)
		{
			final long delay = Long.valueOf(task.getParams()[0]);
			task.scheduled = scheduler.scheduleGeneral(task, delay);
			return true;
		}
		else if (type == TYPE_FIXED_SHEDULED)
		{
			final long delay = Long.valueOf(task.getParams()[0]);
			final long interval = Long.valueOf(task.getParams()[1]);
			
			task.scheduled = scheduler.scheduleGeneralAtFixedRate(task, delay, interval);
			return true;
		}
		else if (type == TYPE_TIME)
		{
			try
			{
				final Date desired = DateFormat.getInstance().parse(task.getParams()[0]);
				final long diff = desired.getTime() - System.currentTimeMillis();
				if (diff >= 0)
				{
					task.scheduled = scheduler.scheduleGeneral(task, diff);
					return true;
				}
				LOGGER.info("Task " + task.getId() + " is obsoleted.");
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
		}
		else if (type == TYPE_SPECIAL)
		{
			final ScheduledFuture<?> result = task.getTask().launchSpecial(task);
			if (result != null)
			{
				task.scheduled = result;
				return true;
			}
		}
		else if (type == TYPE_GLOBAL_TASK)
		{
			final long interval = Long.valueOf(task.getParams()[0]) * 86400000L;
			final String[] hour = task.getParams()[1].split(":");
			
			if (hour.length != 3)
			{
				LOGGER.warn("Task " + task.getId() + " has incorrect parameters");
				return false;
			}
			
			final Calendar check = Calendar.getInstance();
			check.setTimeInMillis(task.getLastActivation() + interval);
			
			final Calendar min = Calendar.getInstance();
			try
			{
				min.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hour[0]));
				min.set(Calendar.MINUTE, Integer.valueOf(hour[1]));
				min.set(Calendar.SECOND, Integer.valueOf(hour[2]));
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				LOGGER.warn("Bad parameter on task " + task.getId() + ": " + e.getMessage());
				return false;
			}
			
			long delay = min.getTimeInMillis() - System.currentTimeMillis();
			
			if (check.after(min) || delay < 0)
			{
				delay += interval;
			}
			
			task.scheduled = scheduler.scheduleGeneralAtFixedRate(task, delay, interval);
			
			return true;
		}
		
		return false;
	}
	
	public static boolean addUniqueTask(final String task, final TaskTypes type, final String param1, final String param2, final String param3)
	{
		return addUniqueTask(task, type, param1, param2, param3, 0);
	}
	
	public static boolean addUniqueTask(final String task, final TaskTypes type, final String param1, final String param2, final String param3, final long lastActivation)
	{
		boolean output = false;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement statement = con.prepareStatement(SELECT_GLOBAL_TASK_ID_BY_TASK))
			{
				statement.setString(1, task);
				ResultSet rset = statement.executeQuery();
				
				if (!rset.next())
				{
					try (PreparedStatement stmt = con.prepareStatement(INSERT_GLOBAL_TASK))
					{
						stmt.setString(1, task);
						stmt.setString(2, type.toString());
						stmt.setLong(3, lastActivation);
						stmt.setString(4, param1);
						stmt.setString(5, param2);
						stmt.setString(6, param3);
						stmt.executeUpdate();
					}
				}
			}
			
			output = true;
		}
		catch (SQLException e)
		{
			LOGGER.warn("cannot add the unique task: " + e.getMessage());
		}
		
		return output;
	}
	
	public static boolean addTask(final String task, final TaskTypes type, final String param1, final String param2, final String param3)
	{
		return addTask(task, type, param1, param2, param3, 0);
	}
	
	public static boolean addTask(final String task, final TaskTypes type, final String param1, final String param2, final String param3, final long lastActivation)
	{
		boolean output = false;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_GLOBAL_TASK))
		{
			statement.setString(1, task);
			statement.setString(2, type.toString());
			statement.setLong(3, lastActivation);
			statement.setString(4, param1);
			statement.setString(5, param2);
			statement.setString(6, param3);
			statement.executeUpdate();
			output = true;
		}
		catch (SQLException e)
		{
			LOGGER.warn("cannot add the task:  " + e.getMessage());
		}
		
		return output;
	}
}
