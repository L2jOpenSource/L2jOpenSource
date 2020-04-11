/*
 * Copyright (C) 2004-2014 L2J Server
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
package l2r;

import java.sql.Connection;
import java.sql.SQLException;

import l2r.util.dbutils.BasicDataSource;

/**
 * This class manages the database connections.
 */
public class L2DatabaseFactory extends BasicDataSource
{
	private static final L2DatabaseFactory _instance = new L2DatabaseFactory();
	
	public static final L2DatabaseFactory getInstance()
	{
		return _instance;
	}
	
	public L2DatabaseFactory()
	{
		super(Config.DATABASE_DRIVER, Config.DATABASE_URL, Config.DATABASE_LOGIN, Config.DATABASE_PASSWORD, Config.DATABASE_MAX_CONNECTIONS, Config.DATABASE_MAX_CONNECTIONS, Config.DATABASE_MAX_IDLE_TIME, Config.DATABASE_IDLE_TEST_PERIOD, false);
	}
	
	/**
	 * Prepared query select.
	 * @param fields the fields
	 * @param tableName the table name
	 * @param whereClause the where clause
	 * @return the string
	 */
	public final String prepQuerySelect(String[] fields, String tableName, String whereClause)
	{
		String query = "SELECT " + fields + " FROM " + tableName + " WHERE " + whereClause;
		return query;
	}
	
	@Override
	public Connection getConnection() throws SQLException
	{
		return getConnection(null);
	}
}
