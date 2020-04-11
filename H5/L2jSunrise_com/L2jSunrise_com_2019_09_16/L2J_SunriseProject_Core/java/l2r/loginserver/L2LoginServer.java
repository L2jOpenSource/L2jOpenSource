/*
 * Copyright (C) 2004-2015 L2J Server
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
package l2r.loginserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;

import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.Server;
import l2r.UPnPService;
import l2r.loginserver.mail.MailSystem;
import l2r.loginserver.network.L2LoginClient;
import l2r.loginserver.network.L2LoginPacketHandler;
import l2r.status.Status;

import com.l2jserver.mmocore.SelectorConfig;
import com.l2jserver.mmocore.SelectorThread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author KenM
 */
public final class L2LoginServer
{
	private static final Logger _log = LoggerFactory.getLogger(L2LoginServer.class);
	
	public static final int PROTOCOL_REV = 0x0106;
	private static L2LoginServer _instance;
	private final GameServerListener _gameServerListener;
	private final SelectorThread<L2LoginClient> _selectorThread;
	private Status _statusServer;
	private Thread _restartLoginServer;
	
	public static void main(String[] args) throws Exception
	{
		new L2LoginServer();
	}
	
	public static L2LoginServer getInstance()
	{
		return _instance;
	}
	
	private L2LoginServer() throws Exception
	{
		_instance = this;
		Server.serverMode = Server.MODE_LOGINSERVER;
		// Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		
		/*** Main ***/
		// Create log folder
		File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER);
		logFolder.mkdir();
		
		// Load Config
		Config.load();
		
		// Check binding address
		checkFreePorts();
		
		// Prepare Database
		L2DatabaseFactory.getInstance();
		
		try
		{
			LoginController.load();
		}
		catch (GeneralSecurityException e)
		{
			_log.error("FATAL: Failed initializing LoginController. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		
		GameServerTable.getInstance();
		
		loadBanFile();
		
		if (Config.EMAIL_SYS_ENABLED)
		{
			MailSystem.getInstance();
		}
		
		InetAddress serverAddr = Config.LOGINSERVER_HOSTNAME.equalsIgnoreCase("*") ? null : InetAddress.getByName(Config.LOGINSERVER_HOSTNAME);
		
		L2LoginPacketHandler loginPacketHandler = new L2LoginPacketHandler();
		SelectorHelper sh = new SelectorHelper();
		SelectorConfig sc = new SelectorConfig();
		_selectorThread = new SelectorThread<>(sc, loginPacketHandler, sh, sh, sh);
		
		_gameServerListener = new GameServerListener();
		_gameServerListener.start();
		_log.info("Listening for GameServers on " + Config.GAME_SERVER_LOGIN_HOST + ":" + Config.GAME_SERVER_LOGIN_PORT);
		
		if (Config.IS_TELNET_ENABLED)
		{
			try
			{
				_statusServer = new Status(Server.serverMode);
				_statusServer.start();
			}
			catch (IOException e)
			{
				_log.warn("Failed to start the Telnet Server. Reason: " + e.getMessage(), e);
			}
		}
		else
		{
			_log.info("Telnet server is currently disabled.");
		}
		
		_selectorThread.openServerSocket(serverAddr, Config.PORT_LOGIN);
		_selectorThread.start();
		_log.info(getClass().getSimpleName() + ": is now listening on: " + Config.LOGINSERVER_HOSTNAME + ":" + Config.PORT_LOGIN);
		
		UPnPService.getInstance();
	}
	
	public Status getStatusServer()
	{
		return _statusServer;
	}
	
	public GameServerListener getGameServerListener()
	{
		return _gameServerListener;
	}
	
	private void loadBanFile()
	{
		final File bannedFile = new File("./banned_ip.cfg");
		if (bannedFile.exists() && bannedFile.isFile())
		{
			try (FileInputStream fis = new FileInputStream(bannedFile);
				InputStreamReader is = new InputStreamReader(fis);
				LineNumberReader lnr = new LineNumberReader(is))
			{
				//@formatter:off
				lnr.lines()
					.map(String::trim)
					.filter(l -> !l.isEmpty() && (l.charAt(0) != '#'))
					.forEach(line -> {
						String[] parts = line.split("#", 2); // address[ duration][ # comments]
						line = parts[0];
						parts = line.split("\\s+"); // durations might be aligned via multiple spaces
						String address = parts[0];
						long duration = 0;
						
						if (parts.length > 1)
						{
							try
							{
								duration = Long.parseLong(parts[1]);
							}
							catch (NumberFormatException nfe)
							{
								_log.warn("Skipped: Incorrect ban duration (" + parts[1] + ") on (" + bannedFile.getName() + "). Line: " + lnr.getLineNumber());
								return;
							}
						}
						
						try
						{
							LoginController.getInstance().addBanForAddress(address, duration);
						}
						catch (UnknownHostException e)
						{
							_log.warn("Skipped: Invalid address (" + address + ") on (" + bannedFile.getName() + "). Line: " + lnr.getLineNumber());
						}
					});
				//@formatter:on
			}
			catch (IOException e)
			{
				_log.warn("Error while reading the bans file (" + bannedFile.getName() + "). Details: " + e.getMessage(), e);
			}
			_log.info("Loaded " + LoginController.getInstance().getBannedIps().size() + " IP Bans.");
		}
		else
		{
			_log.warn("IP Bans file (" + bannedFile.getName() + ") is missing or is a directory, skipped.");
		}
		
		if (Config.LOGIN_SERVER_SCHEDULE_RESTART)
		{
			_log.info("Scheduled LS restart after " + Config.LOGIN_SERVER_SCHEDULE_RESTART_TIME + " hours");
			_restartLoginServer = new LoginServerRestart();
			_restartLoginServer.setDaemon(true);
			_restartLoginServer.start();
		}
	}
	
	class LoginServerRestart extends Thread
	{
		public LoginServerRestart()
		{
			setName("LoginServerRestart");
		}
		
		@Override
		public void run()
		{
			while (!isInterrupted())
			{
				try
				{
					Thread.sleep(Config.LOGIN_SERVER_SCHEDULE_RESTART_TIME * 3600000);
				}
				catch (InterruptedException e)
				{
					return;
				}
				shutdown(true);
			}
		}
	}
	
	public void shutdown(boolean restart)
	{
		Runtime.getRuntime().exit(restart ? 2 : 0);
	}
	
	@SuppressWarnings("resource")
	private static void checkFreePorts()
	{
		boolean binded = false;
		while (!binded)
		{
			try
			{
				ServerSocket ss;
				if (Config.LOGINSERVER_HOSTNAME.equalsIgnoreCase("*"))
				{
					ss = new ServerSocket(Config.PORT_LOGIN);
				}
				else
				{
					ss = new ServerSocket(Config.PORT_LOGIN, 50, InetAddress.getByName(Config.LOGINSERVER_HOSTNAME));
				}
				ss.close();
				binded = true;
			}
			catch (Exception e)
			{
				_log.warn("Port " + Config.PORT_LOGIN + " is already binded. Please free it and restart server.");
				binded = false;
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e2)
				{
				}
			}
		}
	}
}
