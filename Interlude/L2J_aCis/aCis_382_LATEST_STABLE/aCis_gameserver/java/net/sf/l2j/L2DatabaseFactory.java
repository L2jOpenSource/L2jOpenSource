package net.sf.l2j;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class L2DatabaseFactory
{
	protected static Logger _log = Logger.getLogger(L2DatabaseFactory.class.getName());
	
	private ComboPooledDataSource _source;
	
	public static L2DatabaseFactory getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public L2DatabaseFactory() throws SQLException
	{
		try
		{
			_source = new ComboPooledDataSource();
			
			_source.setAutoCommitOnClose(true);
			_source.setInitialPoolSize(10);
			_source.setMinPoolSize(10);
			_source.setMaxPoolSize(Math.max(10, Config.DATABASE_MAX_CONNECTIONS));
			_source.setAcquireRetryAttempts(0); // try to obtain connections indefinitely (0 = never quit)
			_source.setAcquireRetryDelay(500); // 500 miliseconds wait before try to acquire connection again
			_source.setCheckoutTimeout(0); // 0 = wait indefinitely for new connection
			_source.setAcquireIncrement(5); // if pool is exhausted, get 5 more connections at a time
			_source.setTestConnectionOnCheckin(false);
			_source.setIdleConnectionTestPeriod(3600); // test idle connection every 60 sec
			_source.setMaxIdleTime(0); // idle connections never expire
			_source.setMaxStatementsPerConnection(100);
			_source.setBreakAfterAcquireFailure(false); // never fail if any way possible
			_source.setDriverClass("com.mysql.cj.jdbc.Driver");
			_source.setJdbcUrl(Config.DATABASE_URL);
			_source.setUser(Config.DATABASE_LOGIN);
			_source.setPassword(Config.DATABASE_PASSWORD);
			
			/* Test the connection */
			_source.getConnection().close();
		}
		catch (SQLException x)
		{
			throw x;
		}
		catch (Exception e)
		{
			throw new SQLException("could not init DB connection:" + e);
		}
	}
	
	public void shutdown()
	{
		try
		{
			_source.close();
		}
		catch (Exception e)
		{
			_log.log(Level.INFO, "", e);
		}
		
		try
		{
			_source = null;
		}
		catch (Exception e)
		{
			_log.log(Level.INFO, "", e);
		}
	}
	
	/**
	 * Use brace as a safty precaution in case name is a reserved word.
	 * @param whatToCheck the list of arguments.
	 * @return the list of arguments between brackets.
	 */
	public static final String safetyString(String... whatToCheck)
	{
		final StringBuilder sb = new StringBuilder();
		for (String word : whatToCheck)
		{
			if (sb.length() > 0)
				sb.append(", ");
			
			sb.append('`');
			sb.append(word);
			sb.append('`');
		}
		return sb.toString();
	}
	
	public Connection getConnection()
	{
		Connection con = null;
		
		while (con == null)
		{
			try
			{
				con = _source.getConnection();
			}
			catch (SQLException e)
			{
				_log.warning("L2DatabaseFactory: getConnection() failed, trying again " + e);
			}
		}
		return con;
	}
	
	private static class SingletonHolder
	{
		protected static final L2DatabaseFactory _instance;
		
		static
		{
			try
			{
				_instance = new L2DatabaseFactory();
			}
			catch (Exception e)
			{
				throw new ExceptionInInitializerError(e);
			}
		}
	}
}