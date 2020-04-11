package com.l2jfrozen.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class ...
 * @version $Revision: 1.3.2.2.2.1 $ $Date: 2005/03/27 15:29:18 $
 */
public class CharNameTable
{
	private final static Logger LOGGER = Logger.getLogger(CharNameTable.class);
	private static final String SELECT_CHARACTER_ACCOUNT_NAME = "SELECT account_name FROM characters WHERE char_name=? ";
	private static final String SELECT_CHARACTERS_COUNT = "SELECT COUNT(char_name) FROM characters WHERE account_name=? ";
	private static final String SELECT_CHARACTERS_BY_IP = "SELECT count(char_name) FROM " + Config.LOGINSERVER_DB + ".accounts a, " + Config.GAMESERVER_DB + ".characters c where a.login = c.account_name and a.lastIP=? ";
	
	private static CharNameTable instance;
	
	public static CharNameTable getInstance()
	{
		if (instance == null)
		{
			instance = new CharNameTable();
		}
		return instance;
	}
	
	public synchronized boolean doesCharNameExist(final String name)
	{
		boolean result = true;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CHARACTER_ACCOUNT_NAME);)
		{
			statement.setString(1, name);
			
			try (ResultSet rset = statement.executeQuery())
			{
				result = rset.next();
			}
		}
		catch (SQLException e)
		{
			LOGGER.error("Could not check existing charname", e);
		}
		return result;
	}
	
	public int accountCharNumber(final String account)
	{
		int number = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CHARACTERS_COUNT))
		{
			statement.setString(1, account);
			
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					number = rset.getInt(1);
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.error("Could not check existing char number", e);
		}
		
		return number;
	}
	
	public int ipCharNumber(final String ip)
	{
		int number = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CHARACTERS_BY_IP))
		{
			statement.setString(1, ip);
			
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					number = rset.getInt(1);
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.error("CharNameTable.ipCharNumber : Could not check existing char number", e);
		}
		
		return number;
	}
}
