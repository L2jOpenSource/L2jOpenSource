/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.commons.database.pool.impl;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.Config;
import com.l2jserver.commons.database.pool.AbstractConnectionFactory;
import com.l2jserver.commons.database.pool.IConnectionFactory;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * C3P0 Connection Factory implementation.<br>
 * <b>Note that this class is not public to prevent external initialization.</b><br>
 * <b>Access it through {@link ConnectionFactory} and proper configuration.</b>
 * @author Zoey76
 */
final class C3P0ConnectionFactory extends AbstractConnectionFactory
{
	private static final Logger LOG = LoggerFactory.getLogger(C3P0ConnectionFactory.class);
	
	private final ComboPooledDataSource _dataSource;
	
	public C3P0ConnectionFactory()
	{
		if (Config.DATABASE_MAX_CONNECTIONS < 2)
		{
			Config.DATABASE_MAX_CONNECTIONS = 2;
			LOG.warn("A minimum of {} database connections are required.", Config.DATABASE_MAX_CONNECTIONS);
		}
		
		_dataSource = new ComboPooledDataSource();
		_dataSource.setAutoCommitOnClose(true);
		
		_dataSource.setInitialPoolSize(10);
		_dataSource.setMinPoolSize(10);
		_dataSource.setMaxPoolSize(Math.max(10, Config.DATABASE_MAX_CONNECTIONS));
		
		_dataSource.setAcquireRetryAttempts(0); // try to obtain connections indefinitely (0 = never quit)
		_dataSource.setAcquireRetryDelay(500); // 500 milliseconds wait before try to acquire connection again
		_dataSource.setCheckoutTimeout(0); // 0 = wait indefinitely for new connection if pool is exhausted
		_dataSource.setAcquireIncrement(5); // if pool is exhausted, get 5 more connections at a time
		// cause there is a "long" delay on acquire connection
		// so taking more than one connection at once will make connection pooling
		// more effective.
		
		// this "connection_test_table" is automatically created if not already there
		_dataSource.setAutomaticTestTable("connection_test_table");
		_dataSource.setTestConnectionOnCheckin(false);
		
		// testing OnCheckin used with IdleConnectionTestPeriod is faster than testing on checkout
		
		_dataSource.setIdleConnectionTestPeriod(3600); // test idle connection every 60 sec
		_dataSource.setMaxIdleTime(Config.DATABASE_MAX_IDLE_TIME); // 0 = idle connections never expire
		// *THANKS* to connection testing configured above
		// but I prefer to disconnect all connections not used
		// for more than 1 hour
		
		// enables statement caching, there is a "semi-bug" in c3p0 0.9.0 but in 0.9.0.2 and later it's fixed
		_dataSource.setMaxStatementsPerConnection(100);
		
		_dataSource.setBreakAfterAcquireFailure(false); // never fail if any way possible
		// setting this to true will make
		// c3p0 "crash" and refuse to work
		// till restart thus making acquire
		// errors "FATAL" ... we don't want that
		// it should be possible to recover
		try
		{
			_dataSource.setDriverClass(Config.DATABASE_DRIVER);
		}
		catch (PropertyVetoException e)
		{
			LOG.error("There has been a problem setting the driver class!", e);
		}
		_dataSource.setJdbcUrl(Config.DATABASE_URL);
		_dataSource.setUser(Config.DATABASE_LOGIN);
		_dataSource.setPassword(Config.DATABASE_PASSWORD);
		
		/* Test the connection */
		try
		{
			_dataSource.getConnection().close();
		}
		catch (SQLException e)
		{
			LOG.warn("There has been a problem closing the test connection!", e);
		}
		
		LOG.debug("Database connection working.");
	}
	
	@Override
	public void close()
	{
		try
		{
			_dataSource.close();
		}
		catch (Exception e)
		{
			LOG.warn("There has been a problem closing the data source!", e);
		}
	}
	
	@Override
	public DataSource getDataSource()
	{
		return _dataSource;
	}
	
	public static IConnectionFactory getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final IConnectionFactory INSTANCE = new C3P0ConnectionFactory();
	}
}
