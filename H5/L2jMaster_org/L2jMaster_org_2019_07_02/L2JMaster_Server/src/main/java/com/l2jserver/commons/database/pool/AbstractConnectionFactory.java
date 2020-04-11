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
package com.l2jserver.commons.database.pool;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Connection Factory.
 * @author Zoey76
 */
public abstract class AbstractConnectionFactory implements IConnectionFactory
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractConnectionFactory.class);
	
	@Override
	public Connection getConnection()
	{
		Connection con = null;
		while (con == null)
		{
			try
			{
				con = getDataSource().getConnection();
			}
			catch (SQLException e)
			{
				LOG.warn("{}: Unable to get a connection!", getClass().getSimpleName(), e);
			}
		}
		return con;
	}
}
