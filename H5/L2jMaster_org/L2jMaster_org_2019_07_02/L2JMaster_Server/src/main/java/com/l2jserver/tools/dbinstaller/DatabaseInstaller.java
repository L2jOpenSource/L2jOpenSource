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

import java.awt.HeadlessException;

import javax.swing.UIManager;

import com.l2jserver.tools.dbinstaller.frontend.stdio.DBInstallerStdio;
import com.l2jserver.tools.dbinstaller.frontend.swing.DBConfigGUI;
import com.l2jserver.tools.util.app.IApplicationFrontend;

/**
 * Code basically merged from LauncherLS/GS to remove the code duplication.
 * @author HorridoJoho
 */
public final class DatabaseInstaller
{
	private static IApplicationFrontend _frontend;
	
	private final String _installScriptsPath;
	private final String _cleanScript;
	private final String _defaultDatabase;
	
	public DatabaseInstaller(String installScriptsPath, String cleanScript, String defaultDatabase)
	{
		_installScriptsPath = installScriptsPath;
		_cleanScript = cleanScript;
		_defaultDatabase = defaultDatabase;
	}
	
	public void run(String args[])
	{
		if ((args != null) && (args.length > 0))
		{
			new DBInstallerStdio(_defaultDatabase, _installScriptsPath, _cleanScript, getArg("-h", args), getArg("-p", args), getArg("-u", args), getArg("-pw", args), getArg("-d", args), getArg("-m", args));
			return;
		}
		
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			new DBConfigGUI(_defaultDatabase, _installScriptsPath, _cleanScript);
		}
		catch (HeadlessException e)
		{
			new DBInstallerStdio(_defaultDatabase, _installScriptsPath, _cleanScript);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	public String getPath()
	{
		return _installScriptsPath;
	}
	
	public String getCleanScript()
	{
		return _cleanScript;
	}
	
	public String getDefaultDatabase()
	{
		return _defaultDatabase;
	}
	
	public static IApplicationFrontend getFrontend()
	{
		return _frontend;
	}
	
	protected static String getArg(String arg, String[] args)
	{
		try
		{
			int i = 0;
			do
			{
				// Nothing is missing here.
			}
			while (!arg.equalsIgnoreCase(args[i++]));
			return args[i];
		}
		catch (Exception e)
		{
			return null;
		}
	}
}