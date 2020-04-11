/*
 * Copyright (C) 2004-2017 L2J Server
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
package l2r.gameserver.dao.factory.impl;

import l2r.gameserver.dao.factory.IDAOFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO Factory implementation.
 * @author vGodFather
 */
public class DAOFactory
{
	private DAOFactory()
	{
		// Hide constructor.
	}
	
	public static IDAOFactory getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		private static final Logger Log = LoggerFactory.getLogger(DAOFactory.class);
		
		protected static final IDAOFactory INSTANCE;
		
		static
		{
			// switch (Config.DATABASE_ENGINE)
			// {
			// case "MSSQL":
			// case "OracleDB":
			// case "PostgreSQL":
			// case "H2":
			// case "HSQLDB":
			// {
			// throw new UnsupportedOperationException(Config.DATABASE_ENGINE + " is not supported!");
			// }
			// default:
			// case "MariaDB":
			// case "MySQL":
			// {
			// INSTANCE = MySQLDAOFactory.INSTANCE;
			// break;
			// }
			// }
			
			INSTANCE = MySQLDAOFactory.INSTANCE;
			Log.info("Using {} DAO Factory.", INSTANCE.getClass().getSimpleName().replace("DAOFactory", ""));
		}
	}
}
