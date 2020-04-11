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
package com.l2jserver.tools.dbinstaller.frontend.stdio;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.Scanner;
import java.util.prefs.Preferences;

import com.l2jserver.tools.dbinstaller.RunTasks;
import com.l2jserver.tools.util.SQLUtil;
import com.l2jserver.tools.util.app.IApplicationFrontend;
import com.l2jserver.tools.util.io.CloseShieldedInputStream;

/**
 * @author mrTJO, HorridoJoho
 */
public class DBInstallerStdio implements IApplicationFrontend
{
	Connection _con;
	
	public DBInstallerStdio(String db, String dir, String cleanUpScript)
	{
		reportInfo(false, "Welcome to L2J Database installer");
		Preferences prop = Preferences.userRoot();
		while (_con == null)
		{
			String dbHost = requestUserInput("Host ({0}): ", prop.get("dbHost_" + db, "localhost"));
			String dbPort = requestUserInput("Port ({0}): ", prop.get("dbPort_" + db, "3306"));
			String dbUser = requestUserInput("Username ({0}): ", prop.get("dbUser_" + db, "root"));
			String dbPass = requestUserInput("Password: ");
			String dbDbse = requestUserInput("Database ({0}): ", prop.get("dbDbse_" + db, db));
			
			dbHost = dbHost.isEmpty() ? prop.get("dbHost_" + db, "localhost") : dbHost;
			dbPort = dbPort.isEmpty() ? prop.get("dbPort_" + db, "3306") : dbPort;
			dbUser = dbUser.isEmpty() ? prop.get("dbUser_" + db, "root") : dbUser;
			dbDbse = dbDbse.isEmpty() ? prop.get("dbDbse_" + db, db) : dbDbse;
			
			try
			{
				reportInfo(false, "Connecting to SQL server at {0}:{1} with user {2}...", dbHost, dbPort, dbUser);
				_con = SQLUtil.connect(dbHost, dbPort, dbUser, dbPass, dbDbse);
				db = dbDbse;
			}
			catch (Exception e)
			{
				reportError(true, e, "Failed to establish connection with user {0} to SQL server at {1}:{2}!", dbUser, dbHost, dbPort);
			}
		}
		
		String resp = requestUserInput("(C)lean install, (U)pdate or (Q)uit? ");
		if (resp.equalsIgnoreCase("c"))
		{
			if (requestUserConfirm("A clean install will delete your current database! Do you want to continue? (Y/N)? "))
			{
				run(dir, cleanUpScript, db, resp);
			}
		}
		else if (resp.equalsIgnoreCase("u"))
		{
			run(dir, cleanUpScript, db, resp);
		}
	}
	
	/**
	 * Database Console Installer constructor.
	 * @param defDatabase the default database name
	 * @param dir the SQL script's directory
	 * @param cleanUpScript the clean up SQL script
	 * @param host the host name
	 * @param port the port
	 * @param user the user name
	 * @param pass the password
	 * @param database the database name
	 * @param mode the mode, c: Clean, u:update
	 */
	public DBInstallerStdio(String defDatabase, String dir, String cleanUpScript, String host, String port, String user, String pass, String database, String mode)
	{
		if ((database == null) || database.isEmpty())
		{
			database = defDatabase;
		}
		
		try
		{
			reportInfo(false, "Connecting to SQL server at {0}:{1} with user {2}...", host, port, user);
			_con = SQLUtil.connect(host, port, user, pass, database);
		}
		catch (Exception e)
		{
			reportError(true, e, "Failed to establish connection with user {0} to SQL server at {1}:{2}!", user, host, port);
			return;
		}
		
		run(dir, cleanUpScript, database, mode);
	}
	
	private void run(String dir, String cleanUpScript, String database, String mode)
	{
		try
		{
			SQLUtil.ensureDatabaseUsage(_con, database);
		}
		catch (Exception e)
		{
			reportError(true, e, MessageFormat.format("Failed to ensure that the database {0} exists and is in use!", database));
			return;
		}
		
		if ((mode != null) && ("c".equalsIgnoreCase(mode) || "u".equalsIgnoreCase(mode)))
		{
			final RunTasks rt = new RunTasks(this, _con, database, dir, cleanUpScript, "c".equalsIgnoreCase(mode));
			rt.run();
		}
	}
	
	private void clearPendingInput()
	{
		try
		{
			System.in.read(new byte[System.in.available()]);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void drawReportAttention(PrintStream ps)
	{
		ps.println("Press any key to continue...");
		try
		{
			clearPendingInput();
			System.in.read();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void reportInfo(boolean drawAttention, String message)
	{
		System.out.println(message);
		if (drawAttention)
		{
			drawReportAttention(System.out);
		}
	}
	
	@Override
	public void reportInfo(boolean drawAttention, String message, Object... args)
	{
		reportInfo(drawAttention, MessageFormat.format(message, args));
	}
	
	@Override
	public void reportWarn(boolean drawAttention, String message)
	{
		System.out.println(message);
		if (drawAttention)
		{
			drawReportAttention(System.out);
		}
	}
	
	@Override
	public void reportWarn(boolean drawAttention, String message, Object... args)
	{
		reportWarn(drawAttention, MessageFormat.format(message, args));
	}
	
	@Override
	public void reportError(boolean drawAttention, String message)
	{
		System.err.println(message);
		if (drawAttention)
		{
			drawReportAttention(System.err);
		}
	}
	
	@Override
	public void reportError(boolean drawAttention, Throwable t, String message)
	{
		if (t != null)
		{
			message += MessageFormat.format("\n\nReason:\n{0}", t.getMessage());
		}
		reportError(drawAttention, message);
	}
	
	@Override
	public void reportError(boolean drawAttention, Throwable t, String message, Object... args)
	{
		reportError(drawAttention, t, MessageFormat.format(message, args));
	}
	
	@Override
	public String requestUserInput(String message, Object... args)
	{
		clearPendingInput();
		System.out.print(MessageFormat.format(message, args));
		String res = "";
		try (Scanner scn = new Scanner(new CloseShieldedInputStream(System.in)))
		{
			res = scn.nextLine();
		}
		return res;
	}
	
	@Override
	public boolean requestUserConfirm(String message, Object... args)
	{
		clearPendingInput();
		System.out.print(MessageFormat.format(message, args));
		String res = "";
		try (Scanner scn = new Scanner(new CloseShieldedInputStream(System.in)))
		{
			res = scn.next();
		}
		return res.equalsIgnoreCase("y") ? true : false;
	}
	
	@Override
	public void close()
	{
	}
}