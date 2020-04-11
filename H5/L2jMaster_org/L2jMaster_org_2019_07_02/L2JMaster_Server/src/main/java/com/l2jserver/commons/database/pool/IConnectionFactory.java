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

import javax.sql.DataSource;

/**
 * Connection Factory interface.
 * @author Zoey76
 */
public interface IConnectionFactory
{
	/**
	 * Gets the data source.
	 * @return the data source
	 */
	DataSource getDataSource();
	
	/**
	 * Gets a connection from the pool.
	 * @return a connection
	 */
	Connection getConnection();
	
	/**
	 * Closes the data source.<br>
	 * <i>Same as shutdown.</i>
	 */
	void close();
}
