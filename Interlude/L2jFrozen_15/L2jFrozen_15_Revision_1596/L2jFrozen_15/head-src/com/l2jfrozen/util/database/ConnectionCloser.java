package com.l2jfrozen.util.database;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

/**
 * @author Shyla
 */
public class ConnectionCloser implements Runnable
{
	private static final Logger LOGGER = Logger.getLogger(ConnectionCloser.class);
	
	private final Connection c;
	private final RuntimeException exp;
	
	public ConnectionCloser(final Connection con, final RuntimeException e)
	{
		c = con;
		exp = e;
	}
	
	@Override
	public void run()
	{
		try
		{
			if (c != null && !c.isClosed())
			{
				LOGGER.warn("Unclosed connection! Trace: " + exp);
				// c.close();
				
			}
		}
		catch (final SQLException e)
		{
			// the close operation could generate exception, but there is not any problem
			// e.printStackTrace();
		}
		
	}
}
