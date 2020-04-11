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
package com.l2jserver.tools.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.TimeZone;

import com.l2jserver.tools.util.io.FileWriterStdout;
import com.l2jserver.util.file.filter.SQLFilter;

/**
 * SQL helpers.
 * @author HorridoJoho
 */
public final class SQLUtil
{
	public static Connection connect(String host, String port, String user, String password, String db) throws SQLException
	{
		try (Formatter form = new Formatter())
		{
			String url = form.format("jdbc:mysql://%s:%s", host, port).toString();
			Driver driver = DriverManager.getDriver(url);
			Properties info = new Properties();
			info.put("user", user);
			info.put("password", password);
			info.put("useSSL", "false");
			info.put("serverTimezone", TimeZone.getDefault().getID());
			return driver.connect(url, info);
		}
	}
	
	public static void close(Connection con)
	{
		ResourceUtil.close(con);
	}
	
	public static void createDump(Connection con, String db) throws IOException, SQLException
	{
		try (Formatter form = new Formatter())
		{
			try (Statement s = con.createStatement();
				ResultSet rset = s.executeQuery("SHOW TABLES"))
			{
				File dump = new File("dumps", form.format("%1$s_dump_%2$tY%2$tm%2$td-%2$tH%2$tM%2$tS.sql", db, new GregorianCalendar().getTime()).toString());
				new File("dumps").mkdir();
				dump.createNewFile();
				
				if (rset.last())
				{
					// int rows = rset.getRow();
					rset.beforeFirst();
				}
				
				try (FileWriter fileWriter = new FileWriter(dump);
					FileWriterStdout fws = new FileWriterStdout(fileWriter))
				{
					while (rset.next())
					{
						fws.println("CREATE TABLE `" + rset.getString(1) + "`");
						fws.println("(");
						try (Statement desc = con.createStatement();
							ResultSet dset = desc.executeQuery("DESC " + rset.getString(1)))
						{
							Map<String, List<String>> keys = new HashMap<>();
							boolean isFirst = true;
							while (dset.next())
							{
								if (!isFirst)
								{
									fws.println(",");
								}
								fws.print("\t`" + dset.getString(1) + "`");
								fws.print(" " + dset.getString(2));
								if (dset.getString(3).equals("NO"))
								{
									fws.print(" NOT NULL");
								}
								if (!dset.getString(4).isEmpty())
								{
									if (!keys.containsKey(dset.getString(4)))
									{
										keys.put(dset.getString(4), new ArrayList<String>());
									}
									keys.get(dset.getString(4)).add(dset.getString(1));
								}
								if (dset.getString(5) != null)
								{
									fws.print(" DEFAULT '" + dset.getString(5) + "'");
								}
								if (!dset.getString(6).isEmpty())
								{
									fws.print(" " + dset.getString(6));
								}
								isFirst = false;
							}
							if (keys.containsKey("PRI"))
							{
								fws.println(",");
								fws.print("\tPRIMARY KEY (");
								isFirst = true;
								for (String key : keys.get("PRI"))
								{
									if (!isFirst)
									{
										fws.print(", ");
									}
									fws.print("`" + key + "`");
									isFirst = false;
								}
								fws.print(")");
							}
							if (keys.containsKey("MUL"))
							{
								fws.println(",");
								isFirst = true;
								for (String key : keys.get("MUL"))
								{
									if (!isFirst)
									{
										fws.println(", ");
									}
									fws.print("\tKEY `key_" + key + "` (`" + key + "`)");
									isFirst = false;
								}
							}
							fws.println();
							fws.println(");");
							fws.flush();
						}
						
						try (Statement desc = con.createStatement();
							ResultSet dset = desc.executeQuery("SELECT * FROM " + rset.getString(1)))
						{
							boolean isFirst = true;
							int cnt = 0;
							while (dset.next())
							{
								if ((cnt % 100) == 0)
								{
									fws.println("INSERT INTO `" + rset.getString(1) + "` VALUES ");
								}
								else
								{
									fws.println(",");
								}
								
								fws.print("\t(");
								boolean isInFirst = true;
								for (int i = 1; i <= dset.getMetaData().getColumnCount(); i++)
								{
									if (!isInFirst)
									{
										fws.print(", ");
									}
									
									if (dset.getString(i) == null)
									{
										fws.print("NULL");
									}
									else
									{
										fws.print("'" + dset.getString(i).replace("\'", "\\\'") + "'");
									}
									isInFirst = false;
								}
								fws.print(")");
								isFirst = false;
								
								if ((cnt % 100) == 99)
								{
									fws.println(";");
								}
								cnt++;
							}
							if (!isFirst && ((cnt % 100) != 0))
							{
								fws.println(";");
							}
							fws.println();
							fws.flush();
						}
					}
					fws.flush();
				}
			}
		}
	}
	
	public static void ensureDatabaseUsage(Connection con, String db) throws SQLException
	{
		try (Statement s = con.createStatement())
		{
			s.execute("CREATE DATABASE IF NOT EXISTS `" + db + "`");
			s.execute("USE `" + db + "`");
		}
	}
	
	public static void executeSQLScript(Connection con, File file) throws FileNotFoundException, SQLException
	{
		String line = "";
		try (Statement stmt = con.createStatement();
			Scanner scn = new Scanner(file))
		{
			StringBuilder sb = new StringBuilder();
			while (scn.hasNextLine())
			{
				line = scn.nextLine();
				if (line.startsWith("--"))
				{
					continue;
				}
				else if (line.contains("--"))
				{
					line = line.split("--")[0];
				}
				
				line = line.trim();
				if (!line.isEmpty())
				{
					sb.append(line + System.getProperty("line.separator"));
				}
				
				if (line.endsWith(";"))
				{
					stmt.execute(sb.toString());
					sb = new StringBuilder();
				}
			}
		}
	}
	
	public static void executeDirectoryOfSQLScripts(Connection con, File dir, boolean skipErrors) throws FileNotFoundException, SQLException
	{
		final File[] files = dir.listFiles(new SQLFilter());
		if (files != null)
		{
			Arrays.sort(files);
			for (File file : files)
			{
				if (skipErrors)
				{
					try
					{
						executeSQLScript(con, file);
					}
					catch (Throwable t)
					{
					}
				}
				else
				{
					executeSQLScript(con, file);
				}
			}
		}
	}
}