package main.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import com.l2jfrozen.gameserver.util.StringUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

import main.engine.AbstractMod;
import main.holders.DataValueHolder;
import main.holders.ValuesHolder;

/**
 * Class responsible for keeping the information stored in the DB of all mods
 * @author fissban
 */
public class ModsData
{
	private static final Logger LOG = Logger.getLogger(ModsData.class.getName());
	
	private static final String TABLE_FORMAT = "(id int(10) NOT NULL AUTO_INCREMENT, charId int(10), event varchar(255), val varchar(2500), PRIMARY KEY (id))";
	
	private static final String SELECT_DB = "SELECT charId,val,event FROM engine_%table_name%";
	private static final String UPDATE_DB = "UPDATE engine_%table_name% SET val=? WHERE event=? AND charId=?";
	private static final String INSERT_DB = "INSERT INTO engine_%table_name% (val,event,charId) VALUES (?,?,?)";
	private static final String DELETE_DB_1 = "DELETE FROM engine_%table_name% WHERE event=? AND charId=?";
	private static final String DELETE_DB_2 = "DELETE FROM engine_%table_name%";
	
	/** Map with all mods values. */
	private static Map<Integer, List<ValuesHolder>> playersValuesDb = new LinkedHashMap<>();
	
	private final static ReentrantLock dbLock = new ReentrantLock();
	
	public ModsData()
	{
		//
	}
	
	/**
	 * It removes all the events of the players of certain mods.
	 * @param mod
	 */
	public static void remove(AbstractMod mod)
	{
		dbLock.lock();
		
		// Search in memory
		for (Entry<Integer, List<ValuesHolder>> entry : playersValuesDb.entrySet())
		{
			ValuesHolder aux = null;
			int objectId = entry.getKey();
			for (ValuesHolder vh : entry.getValue())
			{
				if (vh.getMod().equals(mod.getClass().getSimpleName()))
				{
					aux = vh;
					break;
				}
			}
			// Remove from memory
			if (aux != null)
			{
				playersValuesDb.get(objectId).remove(aux);
			}
		}
		
		// Remove from DB
		String query = DELETE_DB_2.replace("%table_name%", mod.getClass().getSimpleName().toLowerCase());
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(query))
		{
			statement.execute();
		}
		catch (Exception e)
		{
			LOG.warning("Can't delete: mod:" + mod.getClass().getSimpleName() + " " + e);
			e.printStackTrace();
		}
		finally
		{
			dbLock.unlock();
		}
	}
	
	/**
	 * Removes events from a specific player and mod.
	 * @param objectId
	 * @param event
	 * @param mod
	 */
	public static void remove(int objectId, String event, AbstractMod mod)
	{
		dbLock.lock();
		
		ValuesHolder aux = null;
		// Search in memory
		for (ValuesHolder vh : playersValuesDb.get(objectId))
		{
			if (vh.getMod().equals(mod.getClass().getSimpleName()) && vh.getEvent().equals(event))
			{
				aux = vh;
				break;
			}
		}
		// Remove from memory
		if (aux != null)
		{
			playersValuesDb.get(objectId).remove(aux);
		}
		// Remove from DB
		String query = DELETE_DB_1.replace("%table_name%", mod.getClass().getSimpleName().toLowerCase());
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(query))
		{
			statement.setString(1, event);
			statement.setInt(2, objectId);
			statement.execute();
		}
		catch (Exception e)
		{
			LOG.warning("Can't delete event: " + event + " mod: " + mod.getClass().getSimpleName() + " player objectId :" + objectId + e);
			e.printStackTrace();
		}
		finally
		{
			dbLock.unlock();
		}
	}
	
	/**
	 * Gets the values of a player in a given event and mod.
	 * @param  objectId
	 * @param  event    {startsWith}
	 * @param  mod
	 * @return          {@code Map<String[(event.split(" ")[1])], DataValueHolder> -> String}
	 */
	public static Map<String, DataValueHolder> getCollection(int objectId, String event, AbstractMod mod)
	{
		LinkedHashMap<String, DataValueHolder> map = new LinkedHashMap<>();
		
		if (playersValuesDb.containsKey(objectId))
		{
			for (ValuesHolder vh : playersValuesDb.get(objectId))
			{
				// Seek only the values of the mod in question
				if (vh.getMod().equals(mod.getClass().getSimpleName()))
				{
					// Look for the event in question
					if (vh.getEvent().startsWith(event))
					{
						// Refund the value of the event and mod we seek.
						map.put(vh.getEvent().split(" ")[1], vh.getValue());
					}
				}
			}
		}
		
		return map;
	}
	
	/**
	 * Get the value of a player in a given event and mod.
	 * @param  objectId
	 * @param  event
	 * @param  mod
	 * @return
	 */
	public static DataValueHolder get(int objectId, String event, AbstractMod mod)
	{
		if (playersValuesDb.containsKey(objectId))
		{
			for (ValuesHolder vh : playersValuesDb.get(objectId))
			{
				// Seek only the values of the mod in question
				if (vh.getMod().equals(mod.getClass().getSimpleName()))
				{
					// Look for the event in question
					if (vh.getEvent().equals(event))
					{
						// Refund the value of the event and mod we seek.
						return vh.getValue();
					}
				}
			}
		}
		
		return new DataValueHolder(null);
	}
	
	public static void set(int objectId, String event, String value, AbstractMod mod)
	{
		String modName = mod.getClass().getSimpleName();
		set(objectId, event, value, modName);
	}
	
	public static void set(int objectId, String event, String value, Class<? extends AbstractMod> mod)
	{
		String modName = mod.getSimpleName();
		set(objectId, event, value, modName);
	}
	
	/**
	 * Save data in memory and then in db
	 * @param objectId
	 * @param event
	 * @param value
	 * @param modName
	 */
	public static void set(int objectId, String event, String value, String modName)
	{
		dbLock.lock();
		
		boolean updateInfo = false;
		boolean needQuery = false;
		// memory check.
		if (playersValuesDb.containsKey(objectId))
		{
			for (ValuesHolder vh : playersValuesDb.get(objectId))
			{
				if (vh.getEvent().equals(event) && vh.getMod().equals(modName) && !vh.getValue().getString().equals(value))
				{
					// I indicate that a db query is required.
					needQuery = true;
					// It requires updating the DB
					updateInfo = true;
					// update the value in memory.
					vh.setValue(value);
					break;
				}
			}
		}
		else
		{
			// I indicate that a db query is required.
			needQuery = true;
			// It initializes the list of values of this character
			playersValuesDb.put(objectId, new ArrayList<>());
		}
		
		if (!updateInfo)
		{
			// I indicate that a db query is required.
			needQuery = true;
			// information saved in memory
			playersValuesDb.get(objectId).add(new ValuesHolder(modName, event, value));
		}
		
		if (needQuery)
		{
			String query = updateInfo ? UPDATE_DB : INSERT_DB;
			query = query.replace("%table_name%", modName);
			// Update or insert values.
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(query))
			{
				statement.setString(1, value);
				statement.setString(2, event);
				statement.setInt(3, objectId);
				statement.execute();
			}
			catch (Exception e)
			{
				LOG.warning("Can't " + (updateInfo ? "update " : "insert ") + event + " to DB " + e.getMessage());
				e.printStackTrace();
			}
			finally
			{
				dbLock.unlock();
			}
			return;
		}
		
		dbLock.unlock();
	}
	
	public static void loadValuesFromMod(AbstractMod mod)
	{
		String modName = mod.getClass().getSimpleName();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement statement = con.createStatement())
		{
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS engine_" + modName + " " + TABLE_FORMAT);
			
			loadValues(modName, con);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		// LOG.info(ModsData.class.getSimpleName() + " load values from players in " + mod.getClass().getSimpleName());
	}
	
	/**
	 * Load values from one mod
	 * @param modName
	 * @param con
	 */
	public static void loadValues(String modName, Connection con)
	{
		int count = 0;
		
		String query = SELECT_DB.replace("%table_name%", modName);
		
		try (PreparedStatement statement = con.prepareStatement(query);
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				int objId = rset.getInt("charId");
				String value = rset.getString("val");
				String event = rset.getString("event");
				
				if (!playersValuesDb.containsKey(objId))
				{
					playersValuesDb.put(objId, new ArrayList<>());
				}
				
				playersValuesDb.get(objId).add(new ValuesHolder(modName, event, value));
				
				count++;
			}
		}
		catch (Exception e)
		{
			LOG.warning("Can't load values from DB" + e.getMessage());
			e.printStackTrace();
		}
		
		StringUtil.printSection("ModsData: Loaded info " + modName + " -> " + count);
	}
}
