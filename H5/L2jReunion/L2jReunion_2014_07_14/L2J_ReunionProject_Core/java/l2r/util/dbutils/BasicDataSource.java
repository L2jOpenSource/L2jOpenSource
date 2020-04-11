package l2r.util.dbutils;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * The base implementation of the thread pool using DBCP
 * @author G1ta0
 */
public class BasicDataSource implements DataSource
{
	private final PoolingDataSource _source;
	private final ObjectPool _connectionPool;
	
	/**
	 * @param driver The fully qualified Java class name of the JDBC driver to be used.
	 * @param connectURI The connection URL to be passed to our JDBC driver to establish a connection.
	 * @param uname The connection username to be passed to our JDBC driver to establish a connection.
	 * @param passwd The connection password to be passed to our JDBC driver to establish a connection.
	 * @param maxActive The maximum number of active connections that can be allocated from this pool at the same time, or negative for no limit.
	 * @param maxIdle
	 * @param idleTimeOut The minimum amount of time connection may stay in pool (in seconds)
	 * @param idleTestPeriod The period of time to check idle connections (in seconds)
	 * @param poolPreparedStatements
	 */
	public BasicDataSource(String driver, String connectURI, String uname, String passwd, int maxActive, int maxIdle, int idleTimeOut, int idleTestPeriod, boolean poolPreparedStatements)
	{
		GenericObjectPool connectionPool = new GenericObjectPool(null);
		
		connectionPool.setMaxActive(maxActive);
		connectionPool.setMaxIdle(maxIdle);
		connectionPool.setMinIdle(1);
		connectionPool.setMaxWait(-1L);
		connectionPool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_GROW);
		connectionPool.setTestOnBorrow(false);
		connectionPool.setTestWhileIdle(true);
		connectionPool.setTimeBetweenEvictionRunsMillis(idleTestPeriod * 1000L);
		connectionPool.setNumTestsPerEvictionRun(maxActive);
		connectionPool.setMinEvictableIdleTimeMillis(idleTimeOut * 1000L);
		
		GenericKeyedObjectPoolFactory statementPoolFactory = null;
		if (poolPreparedStatements)
		{
			statementPoolFactory = new GenericKeyedObjectPoolFactory(null, -1, GenericObjectPool.WHEN_EXHAUSTED_FAIL, 0L, 1, GenericKeyedObjectPool.DEFAULT_MAX_TOTAL);
		}
		
		Properties connectionProperties = new Properties();
		connectionProperties.put("user", uname);
		connectionProperties.put("password", passwd);
		
		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI, connectionProperties);
		
		@SuppressWarnings("unused")
		PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, connectionPool, statementPoolFactory, "SELECT 1", false, true);
		
		PoolingDataSource dataSource = new PoolingDataSource(connectionPool);
		
		_connectionPool = connectionPool;
		_source = dataSource;
	}
	
	public Connection getConnection(Connection con) throws SQLException
	{
		return (con == null) || con.isClosed() ? con = _source.getConnection() : con;
	}
	
	public int getBusyConnectionCount()
	{
		return _connectionPool.getNumActive();
	}
	
	public int getIdleConnectionCount()
	{
		return _connectionPool.getNumIdle();
	}
	
	public void shutdown() throws Exception
	{
		_connectionPool.close();
	}
	
	@Override
	public PrintWriter getLogWriter()
	{
		return _source.getLogWriter();
	}
	
	@Override
	public void setLogWriter(PrintWriter out)
	{
		_source.setLogWriter(out);
	}
	
	@Override
	public void setLoginTimeout(int seconds)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int getLoginTimeout()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Logger getParentLogger()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public <T> T unwrap(Class<T> iface)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isWrapperFor(Class<?> iface)
	{
		return false;
	}
	
	@Override
	public Connection getConnection() throws SQLException
	{
		return _source.getConnection();
	}
	
	@Override
	public Connection getConnection(String username, String password)
	{
		throw new UnsupportedOperationException();
	}
	
	/*
	 * @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException { // TODO Auto-generated method stub return null; }
	 */
}
