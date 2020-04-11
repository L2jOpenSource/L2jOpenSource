package com.l2jfrozen.util.database;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class L2DatabaseFactory_HikariCP extends L2DatabaseFactory
{
	static Logger LOGGER = Logger.getLogger(L2DatabaseFactory_HikariCP.class);
	private HikariDataSource source;
	
	public L2DatabaseFactory_HikariCP()
	{
		try
		{
			HikariConfig config = new HikariConfig();
			config.setDriverClassName("org.mariadb.jdbc.Driver");
			config.setJdbcUrl(Config.DATABASE_URL);
			config.setUsername(Config.DATABASE_USER);
			config.setPassword(Config.DATABASE_PASSWORD);
			
			// Typical configuration - Visit: https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
			// Also - http://assets.en.oreilly.com/1/event/21/Connector_J%20Performance%20Gems%20Presentation.pdf
			
			// Recommended
			config.addDataSourceProperty("prepStmtCacheSize", "250");
			config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
			config.addDataSourceProperty("cachePrepStmts", "true");
			config.addDataSourceProperty("useServerPrepStmts", "true");
			
			// Optionals - Performance
			config.addDataSourceProperty("useLocalSessionState", "true");
			config.addDataSourceProperty("useLocalTransactionState", "true");
			config.addDataSourceProperty("rewriteBatchedStatements", "true");
			config.addDataSourceProperty("cacheResultSetMetadata", "true");
			config.addDataSourceProperty("cacheServerConfiguration", "true");
			config.addDataSourceProperty("useLocalSessionState", "true");
			config.addDataSourceProperty("elideSetAutoCommits", "true");
			config.addDataSourceProperty("maintainTimeStats", "false");
			
			source = new HikariDataSource(config);
		}
		catch (final Exception e)
		{
			LOGGER.error("HikariCP: Could not init DB connection", e);
			System.exit(1);
		}
	}
	
	@Override
	public Connection getConnection()
	{
		Connection con = null;
		
		while (con == null && source != null)
		{
			try
			{
				con = source.getConnection();
			}
			catch (final SQLException e)
			{
				LOGGER.error("L2DatabaseFactory.getConnection: Failed to establish connection ", e);
			}
		}
		
		return con;
		
	}
	
	@Override
	public void shutdown()
	{
		try
		{
			// sleep 10 seconds before the final source shutdown
			Thread.sleep(10000);
			// Close connection
			source.close();
			// release source
			source = null;
		}
		catch (Exception e)
		{
			LOGGER.error("L2DatabaseFactory.shutdown : Something went wrong", e);
		}
	}
}
