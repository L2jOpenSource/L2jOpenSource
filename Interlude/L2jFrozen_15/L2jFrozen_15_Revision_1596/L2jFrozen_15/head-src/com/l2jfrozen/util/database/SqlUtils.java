package com.l2jfrozen.util.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.util.CloseUtil;

public class SqlUtils
{
	private static Logger LOGGER = Logger.getLogger(SqlUtils.class);
	
	private static SqlUtils instance;
	
	public static SqlUtils getInstance()
	{
		if (instance == null)
		{
			instance = new SqlUtils();
		}
		
		return instance;
	}
	
	public static Integer getIntValue(final String resultField, final String tableName, final String whereClause)
	{
		String query = "";
		Integer res = null;
		
		PreparedStatement statement = null;
		ResultSet rset = null;
		
		Connection con = null;
		try
		{
			query = L2DatabaseFactory.getInstance().prepQuerySelect(new String[]
			{
				resultField
			}, tableName, whereClause, true);
			
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(query);
			rset = statement.executeQuery();
			
			if (rset.next())
			{
				res = rset.getInt(1);
			}
		}
		catch (final Exception e)
		{
			LOGGER.warn("Error in query '" + query + "':" + e);
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (rset != null)
				{
					DatabaseUtils.close(rset);
					rset = null;
				}
				
				if (statement != null)
				{
					DatabaseUtils.close(statement);
					statement = null;
				}
				
				query = null;
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
			}
			
			CloseUtil.close(con);
			con = null;
			
		}
		
		return res;
	}
	
}
