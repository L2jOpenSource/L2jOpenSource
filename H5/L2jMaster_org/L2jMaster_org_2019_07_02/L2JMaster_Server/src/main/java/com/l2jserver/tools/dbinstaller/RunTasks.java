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
package com.l2jserver.tools.dbinstaller;

import java.io.File;
import java.sql.Connection;
import java.util.prefs.Preferences;

import com.l2jserver.tools.util.SQLUtil;
import com.l2jserver.tools.util.app.IApplicationFrontend;
import com.l2jserver.util.file.filter.SQLFilter;

/**
 * @author mrTJO, HorridoJoho
 */
public class RunTasks extends Thread
{
	IApplicationFrontend _frontend;
	Connection _con;
	boolean _cleanInstall;
	String _db;
	String _sqlDir;
	String _cleanUpFile;
	
	public RunTasks(IApplicationFrontend frontend, Connection con, String db, String sqlDir, String cleanUpFile, boolean cleanInstall)
	{
		_frontend = frontend;
		_con = con;
		_db = db;
		_cleanInstall = cleanInstall;
		_sqlDir = sqlDir;
		_cleanUpFile = cleanUpFile;
	}
	
	@Override
	public void run()
	{
		try
		{
			File sqlDir = new File(_sqlDir);
			if (!sqlDir.exists())
			{
				_frontend.reportError(true, null, "The directory {0} does not exist!", sqlDir.getAbsolutePath());
				return;
			}
			
			try
			{
				_frontend.reportInfo(false, "Backup database...");
				SQLUtil.createDump(_con, _db);
			}
			catch (Exception e)
			{
				_frontend.reportError(true, e, "Failed to backup database!");
				return;
			}
			
			File updDir = new File(sqlDir, "updates");
			Preferences prefs = Preferences.userRoot();
			if (_cleanInstall)
			{
				try
				{
					_frontend.reportInfo(false, "Executing SQL cleanup script...");
					SQLUtil.executeSQLScript(_con, new File(_cleanUpFile));
				}
				catch (Exception e)
				{
					_frontend.reportError(true, e, "Failed to execute SQL cleanup script {0}!", _cleanUpFile);
					return;
				}
				
				if (updDir.exists())
				{
					StringBuilder sb = new StringBuilder();
					for (File cf : updDir.listFiles(new SQLFilter()))
					{
						sb.append(cf.getName() + ';');
					}
					prefs.put(_db + "_upd", sb.toString());
				}
			}
			else if (updDir.exists())
			{
				_frontend.reportInfo(false, "Installing update SQL scripts...");
				for (File cf : updDir.listFiles(new SQLFilter()))
				{
					if (!prefs.get(_db + "_upd", "").contains(cf.getName()))
					{
						if (!_cleanInstall)
						{
							try
							{
								_frontend.reportInfo(false, "Installing {0}...", cf);
								SQLUtil.executeSQLScript(_con, cf);
							}
							catch (Exception e)
							{
								_frontend.reportError(true, e, "Failed to execute SQL update file {0}!", cf);
								return;
							}
						}
						prefs.put(_db + "_upd", prefs.get(_db + "_upd", "") + cf.getName() + ";");
					}
				}
			}
			
			_frontend.reportInfo(false, "Installing basic SQL scripts...");
			for (File f : sqlDir.listFiles(new SQLFilter()))
			{
				try
				{
					_frontend.reportInfo(false, "Installing {0}...", f);
					SQLUtil.executeSQLScript(_con, f);
				}
				catch (Exception e)
				{
					_frontend.reportError(true, e, "Failed to execute basics SQL file {0}!", f);
					return;
				}
			}
			
			File cusDir = new File(_sqlDir, "custom");
			if (cusDir.exists() && _frontend.requestUserConfirm("Install custom tables? (Y/N) "))
			{
				_frontend.reportInfo(false, "Installing custom tables...");
				for (File f : cusDir.listFiles(new SQLFilter()))
				{
					try
					{
						_frontend.reportInfo(false, "Installing {0}...", f);
						SQLUtil.executeSQLScript(_con, f);
					}
					catch (Exception e)
					{
						_frontend.reportError(true, e, "Failed to execute customs SQL file {0}!", f);
					}
				}
			}
			
			File modsDir = new File(_sqlDir, "mods");
			if (modsDir.exists() && _frontend.requestUserConfirm("Install mod tables? (Y/N) "))
			{
				_frontend.reportInfo(false, "Installing mod tables...");
				for (File f : modsDir.listFiles(new SQLFilter()))
				{
					try
					{
						_frontend.reportInfo(false, "Installing {0}...", f);
						SQLUtil.executeSQLScript(_con, f);
					}
					catch (Exception e)
					{
						_frontend.reportError(true, e, "Failed to execute mods SQL file {0}!", f);
					}
				}
			}
			
			_frontend.reportInfo(true, "Database installation complete.");
		}
		finally
		{
			SQLUtil.close(_con);
			_frontend.close();
		}
	}
}