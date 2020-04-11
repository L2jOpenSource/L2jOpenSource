package com.l2jfrozen.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.util.database.L2DatabaseFactory;

public class ServerVariables
{
	private static final Logger LOGGER = Logger.getLogger(ServerVariables.class);
	public static final String SELECT_SERVER_VARIABLES = "SELECT variable, value FROM server_variables";
	public static final String INSERT_SERVER_VARIABLE = "INSERT INTO server_variables (variable, value) VALUES (?,?)";
	public static final String DELETE_SERVER_VARIABLE = "DELETE FROM server_variables WHERE variable=?";
	
	private Map<String, String> variables = new ConcurrentHashMap<>();
	
	/**
	 * @param  variable
	 * @param  defaultValue In case that <b>variable</b> does not exists, you need to indicate a default value to be return
	 * @return              the value of the variable mapped
	 */
	public String getVariable(String variable, String defaultValue)
	{
		return variables.getOrDefault(variable, defaultValue);
	}
	
	/**
	 * @param  variable
	 * @param  defaultValue In case that <b>variable</b> does not exists, you need to indicate a default value to be return
	 * @return              the value of the variable mapped
	 */
	public int getVariableInt(String variable, int defaultValue)
	{
		if (variables.get(variable) == null)
		{
			return defaultValue;
		}
		
		return Integer.parseInt(variables.get(variable));
	}
	
	/**
	 * @param  variable
	 * @param  defaultValue In case that <b>variable</b> does not exists, you need to indicate a default value to be return
	 * @return              the value of the variable mapped
	 */
	public long getVariableLong(String variable, long defaultValue)
	{
		if (variables.get(variable) == null)
		{
			return defaultValue;
		}
		
		return Long.parseLong(variables.get(variable));
	}
	
	/**
	 * @param  variable
	 * @param  defaultValue In case that <b>variable</b> does not exists, you need to indicate a default value to be return
	 * @return              the value of the variable mapped
	 */
	public boolean getVariableBoolean(String variable, boolean defaultValue)
	{
		if (variables.get(variable) == null)
		{
			return defaultValue;
		}
		
		return Boolean.parseBoolean(variables.get(variable));
	}
	
	public void setVariable(String variable, int value, boolean saveInDB)
	{
		setVariable(variable, String.valueOf(value), saveInDB);
	}
	
	public void setVariable(String variable, long value, boolean saveInDB)
	{
		setVariable(variable, String.valueOf(value), saveInDB);
	}
	
	public void setVariable(String variable, boolean value, boolean saveInDB)
	{
		setVariable(variable, String.valueOf(value), saveInDB);
	}
	
	/**
	 * @param variable
	 * @param value
	 * @param saveInDB (Optional) If you <b>dont want</b> to save the variable and value in the database put <b>false</b><br>
	 *                     When you set a variable with the same name, the old value will be replaced. <br>
	 *                     If you replace the variable with the new value <b>and do not save in the data base</b>, the old variable will be read from database when player log in game.
	 */
	public void setVariable(String variable, String value, boolean saveInDB)
	{
		variables.put(variable, value);
		
		if (saveInDB)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement pstDelete = con.prepareStatement(DELETE_SERVER_VARIABLE);
				PreparedStatement pst = con.prepareStatement(INSERT_SERVER_VARIABLE))
			{
				pstDelete.setString(1, variable);
				pstDelete.executeUpdate();
				
				pst.setString(1, variable.trim());
				pst.setString(2, value);
				pst.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.error("ServerVariables.setVariable : Problem when tried to save server variable into database ", e);
			}
		}
	}
	
	/**
	 * @param variable
	 * @param removeInDB (Optional) If you want to keep the variable and value in the database put <b>false</b>
	 */
	public void removeVariable(String variable, boolean removeInDB)
	{
		variables.remove(variable);
		
		if (removeInDB)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement pst = con.prepareStatement(DELETE_SERVER_VARIABLE))
			{
				pst.setString(1, variable);
				pst.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.error("ServerVariables.removeVariable : Problem when tried to remove server variable from database ", e);
			}
		}
	}
	
	/**
	 * Read server variables from database
	 */
	public void loadVariables()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement pst = con.prepareStatement(SELECT_SERVER_VARIABLES);
			ResultSet rset = pst.executeQuery())
		{
			while (rset.next())
			{
				variables.put(rset.getString("variable"), rset.getString("value"));
			}
		}
		catch (Exception e)
		{
			LOGGER.error("ServerVariables.loadVariables : Problem when tried to get server variables from database", e);
		}
	}
	
	private static final class SingletonHolder
	{
		static final ServerVariables instance = new ServerVariables();
	}
	
	public static ServerVariables getInstance()
	{
		return SingletonHolder.instance;
	}
}
