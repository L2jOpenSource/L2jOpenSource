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
package l2r.tools.gsregistering;

import java.awt.HeadlessException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ResourceBundle;

import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.Server;
import l2r.loginserver.GameServerTable;
import l2r.util.Util;

/**
 * The Class BaseGameServerRegister.
 * @author KenM
 */
public abstract class BaseGameServerRegister
{
	private boolean _loaded = false;
	private ResourceBundle _bundle;
	
	/**
	 * The main method.
	 * @param args the arguments
	 */
	public static void main(String[] args)
	{
		Locale locale = null;
		boolean interactive = true;
		boolean force = false;
		boolean fallback = false;
		BaseTask task = null;
		
		ResourceBundle bundle = null;
		try
		{
			locale = Locale.getDefault();
			bundle = ResourceBundle.getBundle("gsregister.GSRegister", locale, LanguageControl.INSTANCE);
		}
		catch (Throwable t)
		{
			System.out.println("FATAL: Failed to load default translation.");
			System.exit(666);
		}
		
		String arg;
		for (int i = 0; i < args.length; i++)
		{
			arg = args[i];
			
			if (arg.equals("-f") || arg.equals("--force"))
			{
				force = true;
			}
			// --fallback : If an register operation fails due to ID already being in use it will then try to register first available ID
			else if (arg.equals("-b") || arg.equals("--fallback"))
			{
				fallback = true;
			}
			// --register <id> <hexid_dest_dir> : Register GameServer with ID <id> and output hexid on <hexid_dest_dir>
			// Fails if <id> already in use, unless -force is used (overwrites)
			else if (arg.equals("-r") || arg.equals("--register"))
			{
				interactive = false;
				int id = Integer.parseInt(args[++i]);
				String dir = args[++i];
				
				task = new RegisterTask(id, dir, force, fallback);
			}
			// --unregister <id> : Removes GameServer denoted by <id>
			else if (arg.equals("-u") || arg.equals("--unregister"))
			{
				interactive = false;
				String gsId = args[++i];
				if (gsId.equalsIgnoreCase("all"))
				{
					task = new UnregisterAllTask();
				}
				else
				{
					try
					{
						int id = Integer.parseInt(gsId);
						task = new UnregisterTask(id);
					}
					catch (NumberFormatException e)
					{
						if (bundle != null)
						{
							System.out.printf(bundle.getString("wrongUnregisterArg") + Config.EOL, gsId);
						}
						System.exit(1);
					}
				}
			}
			// --language <locale> : Sets the app to use the specified locale, overriding auto-detection
			else if (arg.equals("-l") || arg.equals("--language"))
			{
				String loc = args[++i];
				Locale[] availableLocales = Locale.getAvailableLocales();
				Locale l;
				for (int j = 0; (j < availableLocales.length) && (locale == null); j++)
				{
					l = availableLocales[j];
					if (l.toString().equals(loc))
					{
						locale = l;
					}
				}
				if (locale == null)
				{
					System.out.println("Specified locale '" + loc + "' was not found, using default behaviour.");
				}
				else
				{
					try
					{
						bundle = ResourceBundle.getBundle("gsregister.GSRegister", locale, LanguageControl.INSTANCE);
					}
					catch (Throwable t)
					{
						System.out.println("Failed to load translation ''");
					}
				}
			}
			// --help : Prints usage/arguments/credits
			else if (arg.equals("-h") || arg.equals("--help"))
			{
				interactive = false;
				
				BaseGameServerRegister.printHelp(bundle);
			}
		}
		
		try
		{
			if (interactive)
			{
				BaseGameServerRegister.startCMD(bundle);
			}
			else
			{
				// if there is a task, do it, else the app has already finished
				if (task != null)
				{
					task.setBundle(bundle);
					task.run();
				}
			}
		}
		catch (HeadlessException e)
		{
			BaseGameServerRegister.startCMD(bundle);
		}
	}
	
	/**
	 * Prints the help.
	 * @param bundle the bundle
	 */
	private static void printHelp(ResourceBundle bundle)
	{
		String[] help =
		{
			bundle.getString("purpose"),
			"",
			bundle.getString("options"),
			"-b, --fallback\t\t\t\t" + bundle.getString("fallbackOpt"),
			"-c, --cmd\t\t\t\t" + bundle.getString("cmdOpt"),
			"-f, --force\t\t\t\t" + bundle.getString("forceOpt"),
			"-h, --help\t\t\t\t" + bundle.getString("helpOpt"),
			"-l, --language\t\t\t\t" + bundle.getString("languageOpt"),
			"-r, --register <id> <hexid_dest_dir>\t" + bundle.getString("registerOpt1"),
			"\t\t\t\t\t" + bundle.getString("registerOpt2"),
			"\t\t\t\t\t" + bundle.getString("registerOpt3"),
			"",
			"-u, --unregister <id>|all\t\t" + bundle.getString("unregisterOpt"),
			"",
			bundle.getString("credits"),
			bundle.getString("bugReports") + " http://www.l2reunion.com"
		
		/*
		 * "-b, --fallback\t\t\t\tIf an register operation fails due to ID already being in use it will then try to register first available ID", "-c, --cmd\t\t\t\tForces application to run in command-line mode even if the GUI is supported.",
		 * "-f, --force\t\t\t\tForces GameServer register operations to overwrite a server if necessary", "-h, --help\t\t\t\tPrints this help message", "-l, --language <locale>\t\t\t\tAsks the application to use the specified locale, overriding auto-detection",
		 * "-r, --register <id> <hexid_dest_dir>\tRegister GameServer with ID <id> and output hexid on <hexid_dest_dir>", "\t\t\t\t\tUse a negative value on <id> to register the first available ID", "\t\t\t\t\tFails if <id> already in use, unless --force is used (overwrites)", "",
		 * "-u, --unregister <id>|all\t\tRemoves GameServer denoted by <id>, use \"all\" for removing all registered GameServers", "", "Copyright (C) L2J Team 2008-2012.", "Report bugs: http://www.l2reunion.com"
		 */
		};
		
		for (String str : help)
		{
			System.out.println(str);
		}
	}
	
	/**
	 * Start the CMD.
	 * @param bundle the bundle.
	 */
	private static void startCMD(final ResourceBundle bundle)
	{
		GameServerRegister cmdUi = new GameServerRegister(bundle);
		try
		{
			cmdUi.consoleUI();
		}
		catch (IOException e)
		{
			cmdUi.showError("I/O exception trying to get input from keyboard.", e);
		}
	}
	
	/**
	 * Instantiates a new base game server register.
	 * @param bundle the bundle.
	 */
	public BaseGameServerRegister(ResourceBundle bundle)
	{
		setBundle(bundle);
	}
	
	/**
	 * Load.
	 */
	public void load()
	{
		Server.serverMode = Server.MODE_LOGINSERVER;
		
		Config.load();
		GameServerTable.getInstance();
		
		_loaded = true;
	}
	
	/**
	 * Checks if is loaded.
	 * @return true, if is loaded
	 */
	public boolean isLoaded()
	{
		return _loaded;
	}
	
	/**
	 * Sets the bundle.
	 * @param bundle the bundle to set.
	 */
	public void setBundle(ResourceBundle bundle)
	{
		_bundle = bundle;
	}
	
	/**
	 * Gets the bundle.
	 * @return the bundle.
	 */
	public ResourceBundle getBundle()
	{
		return _bundle;
	}
	
	/**
	 * Show the error.
	 * @param msg the msg.
	 * @param t the t.
	 */
	public abstract void showError(String msg, Throwable t);
	
	/**
	 * Unregister the game server.
	 * @param id the game server id.
	 * @throws SQLException the SQL exception.
	 */
	public static void unregisterGameServer(int id) throws SQLException
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM gameservers WHERE server_id = ?"))
		
		{
			ps.setInt(1, id);
			ps.executeUpdate();
		}
		GameServerTable.getInstance().getRegisteredGameServers().remove(id);
	}
	
	/**
	 * Unregister all game servers.
	 * @throws SQLException the SQL exception
	 */
	public static void unregisterAllGameServers() throws SQLException
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement())
		{
			s.executeUpdate("DELETE FROM gameservers");
		}
		GameServerTable.getInstance().getRegisteredGameServers().clear();
	}
	
	/**
	 * Register a game server.
	 * @param id the id of the game server.
	 * @param outDir the out dir.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void registerGameServer(int id, String outDir) throws IOException
	{
		byte[] hexId = Util.generateHex(16);
		GameServerTable.getInstance().registerServerOnDB(hexId, id, "");
		
		Properties hexSetting = new Properties();
		File file = new File(outDir, "hexid.txt");
		// Create a new empty file only if it doesn't exist
		file.createNewFile();
		try (OutputStream out = new FileOutputStream(file))
		{
			hexSetting.setProperty("ServerID", String.valueOf(id));
			hexSetting.setProperty("HexID", new BigInteger(hexId).toString(16));
			hexSetting.store(out, "The HexId to Auth into LoginServer");
		}
	}
	
	/**
	 * Register first available.
	 * @param outDir the out dir
	 * @return the int
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static int registerFirstAvailable(String outDir) throws IOException
	{
		for (Entry<Integer, String> e : GameServerTable.getInstance().getServerNames().entrySet())
		{
			if (!GameServerTable.getInstance().hasRegisteredGameServerOnId(e.getKey()))
			{
				BaseGameServerRegister.registerGameServer(e.getKey(), outDir);
				return e.getKey();
			}
		}
		return -1;
	}
	
	/**
	 * The Class BaseTask.
	 */
	protected static abstract class BaseTask implements Runnable
	{
		private ResourceBundle _bundle;
		
		/**
		 * Sets the bundle.
		 * @param bundle The bundle to set.
		 */
		public void setBundle(ResourceBundle bundle)
		{
			_bundle = bundle;
		}
		
		/**
		 * Gets the bundle.
		 * @return Returns the bundle.
		 */
		public ResourceBundle getBundle()
		{
			return _bundle;
		}
		
		/**
		 * Show the error.
		 * @param msg the msg
		 * @param t the t
		 */
		public void showError(String msg, Throwable t)
		{
			String title;
			if (getBundle() != null)
			{
				title = getBundle().getString("error");
				msg += Config.EOL + getBundle().getString("reason") + ' ' + t.getLocalizedMessage();
			}
			else
			{
				title = "Error";
				msg += Config.EOL + "Cause: " + t.getLocalizedMessage();
			}
			System.out.println(title + ": " + msg);
		}
	}
	
	/**
	 * The Class RegisterTask.
	 */
	private static class RegisterTask extends BaseTask
	{
		private final int _id;
		private final String _outDir;
		private boolean _force;
		private boolean _fallback;
		
		/**
		 * Instantiates a new register task.
		 * @param id the id.
		 * @param outDir the out dir.
		 * @param force the force.
		 * @param fallback the fallback.
		 */
		public RegisterTask(int id, String outDir, boolean force, boolean fallback)
		{
			_id = id;
			_outDir = outDir;
			_force = force;
			_fallback = fallback;
		}
		
		/**
		 * Sets the actions.
		 * @param force the force.
		 * @param fallback the fallback.
		 */
		@SuppressWarnings("unused")
		public void setActions(boolean force, boolean fallback)
		{
			_force = force;
			_fallback = fallback;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_id < 0)
				{
					int registeredId = BaseGameServerRegister.registerFirstAvailable(_outDir);
					
					if (registeredId < 0)
					{
						System.out.println(getBundle().getString("noFreeId"));
					}
					else
					{
						System.out.printf(getBundle().getString("registrationOk") + Config.EOL, registeredId);
					}
				}
				else
				{
					System.out.printf(getBundle().getString("checkingIdInUse") + Config.EOL, _id);
					if (GameServerTable.getInstance().hasRegisteredGameServerOnId(_id))
					{
						System.out.println(getBundle().getString("yes"));
						if (_force)
						{
							System.out.printf(getBundle().getString("forcingRegistration") + Config.EOL, _id);
							BaseGameServerRegister.unregisterGameServer(_id);
							BaseGameServerRegister.registerGameServer(_id, _outDir);
							System.out.printf(getBundle().getString("registrationOk") + Config.EOL, _id);
						}
						else if (_fallback)
						{
							System.out.println(getBundle().getString("fallingBack"));
							int registeredId = BaseGameServerRegister.registerFirstAvailable(_outDir);
							
							if (registeredId < 0)
							{
								System.out.println(getBundle().getString("noFreeId"));
							}
							else
							{
								System.out.printf(getBundle().getString("registrationOk") + Config.EOL, registeredId);
							}
						}
						else
						{
							System.out.println(getBundle().getString("noAction"));
						}
					}
					else
					{
						System.out.println(getBundle().getString("no"));
						BaseGameServerRegister.registerGameServer(_id, _outDir);
					}
				}
			}
			catch (SQLException e)
			{
				showError(getBundle().getString("sqlErrorRegister"), e);
			}
			catch (IOException e)
			{
				showError(getBundle().getString("ioErrorRegister"), e);
			}
		}
	}
	
	/**
	 * The Class UnregisterTask.
	 */
	private static class UnregisterTask extends BaseTask
	{
		private final int _id;
		
		/**
		 * Instantiates a new unregister task.
		 * @param id the task id.
		 */
		public UnregisterTask(int id)
		{
			_id = id;
			
		}
		
		@Override
		public void run()
		{
			System.out.printf(getBundle().getString("removingGsId") + Config.EOL, _id);
			try
			{
				BaseGameServerRegister.unregisterGameServer(_id);
			}
			catch (SQLException e)
			{
				showError(getBundle().getString("sqlErrorRegister"), e);
			}
		}
	}
	
	/**
	 * The Class UnregisterAllTask.
	 */
	protected static class UnregisterAllTask extends BaseTask
	{
		@Override
		public void run()
		{
			try
			{
				BaseGameServerRegister.unregisterAllGameServers();
			}
			catch (SQLException e)
			{
				showError(getBundle().getString("sqlErrorUnregisterAll"), e);
			}
		}
	}
}
