package com.l2jfrozen.util.database;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public abstract class L2DatabaseFactory
{
	private static final Logger LOGGER = Logger.getLogger(L2DatabaseFactory.class);
	
	protected static L2DatabaseFactory instance;
	
	public static L2DatabaseFactory getInstance()
	{
		if (instance == null)
		{
			instance = new L2DatabaseFactory_HikariCP();
			
			LOGGER.info("You are using DBMS: MariaDB");
			LOGGER.info("You are using JDBC Pool: HikariCP");
		}
		
		return instance;
	}
	
	public final String prepQuerySelect(final String[] fields, final String tableName, final String whereClause, final boolean returnOnlyTopRecord)
	{
		String msSqlTop1 = "";
		String mySqlTop1 = "";
		if (returnOnlyTopRecord)
		{
			mySqlTop1 = " Limit 1 ";
		}
		final String query = "SELECT " + msSqlTop1 + safetyString(fields) + " FROM " + tableName + " WHERE " + whereClause + mySqlTop1;
		return query;
	}
	
	public final String safetyString(final String... whatToCheck)
	{
		// NOTE: Use brace as a safty precaution just incase name is a reserved word
		final char braceLeft;
		final char braceRight;
		
		braceLeft = '`';
		braceRight = '`';
		
		int length = 0;
		
		for (final String word : whatToCheck)
		{
			length += word.length() + 4;
		}
		
		final StringBuilder sbResult = new StringBuilder(length);
		
		for (final String word : whatToCheck)
		{
			if (sbResult.length() > 0)
			{
				sbResult.append(", ");
			}
			
			sbResult.append(braceLeft);
			sbResult.append(word);
			sbResult.append(braceRight);
		}
		
		return sbResult.toString();
	}
	
	public static void close(final Connection con)
	{
		if (con == null)
		{
			return;
		}
		
		try
		{
			con.close();
		}
		catch (final SQLException e)
		{
			LOGGER.error("Failed to close database connection!", e);
		}
	}
	
	public abstract Connection getConnection() throws SQLException;
	
	public abstract void shutdown();
	
}
