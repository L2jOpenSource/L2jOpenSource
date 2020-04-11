package com.l2jfrozen.loginserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.logging.LogManager;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.l2jfrozen.Config;
import com.l2jfrozen.FService;
import com.l2jfrozen.L2Frozen;
import com.l2jfrozen.ServerType;
import com.l2jfrozen.gameserver.datatables.GameServerTable;
import com.l2jfrozen.netcore.NetcoreConfig;
import com.l2jfrozen.netcore.SelectorConfig;
import com.l2jfrozen.netcore.SelectorThread;
import com.l2jfrozen.util.Util;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class L2LoginServer
{
	public static final int PROTOCOL_REV = 0x0102;
	
	private static L2LoginServer instance;
	private final Logger LOGGER = Logger.getLogger(L2LoginServer.class);
	private GameServerListener gameServerListener;
	private SelectorThread<L2LoginClient> selectorThread;
	
	public static void main(final String[] args)
	{
		PropertyConfigurator.configure(FService.LOG_CONF_FILE);
		instance = new L2LoginServer();
	}
	
	public static L2LoginServer getInstance()
	{
		return instance;
	}
	
	public L2LoginServer()
	{
		ServerType.serverMode = ServerType.MODE_LOGINSERVER;
		
		// Create input stream for LOGGER file -- or store file data into memory
		InputStream is = null;
		try
		{
			// check for legacy Implementation
			File log_conf_file = new File(FService.LOG_CONF_FILE);
			if (!log_conf_file.exists())
			{
				// old file position
				log_conf_file = new File(FService.LEGACY_LOG_CONF_FILE);
			}
			
			is = new FileInputStream(log_conf_file);
			LogManager.getLogManager().readConfiguration(is);
			
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (is != null)
			{
				try
				{
					
					is.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
			
		}
		
		// Team info
		L2Frozen.info();
		
		// Load LoginServer Configs
		Config.load();
		
		Util.printSection("Database");
		// Prepare Database
		try
		{
			L2DatabaseFactory.getInstance();
		}
		catch (Exception e)
		{
			LOGGER.fatal("Failed initializing database", e);
			System.exit(1);
		}
		
		try
		{
			LoginController.load();
		}
		catch (final GeneralSecurityException e)
		{
			LOGGER.fatal("Failed initializing LoginController", e);
			System.exit(1);
		}
		
		try
		{
			GameServerTable.load();
		}
		catch (final GeneralSecurityException e)
		{
			LOGGER.fatal("Failed to load GameServerTable", e);
			System.exit(1);
		}
		catch (final Exception e)
		{
			LOGGER.fatal("Failed to load GameServerTable", e);
			
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			System.exit(1);
		}
		
		InetAddress bindAddress = null;
		if (!Config.LOGIN_BIND_ADDRESS.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.LOGIN_BIND_ADDRESS);
			}
			catch (final UnknownHostException e1)
			{
				LOGGER.warn("WARNING: The LoginServer bind address is invalid, using all avaliable IPs", e1);
			}
		}
		
		final SelectorConfig sc = new SelectorConfig();
		sc.setMaxReadPerPass(NetcoreConfig.getInstance().MMO_MAX_READ_PER_PASS);
		sc.setMaxSendPerPass(NetcoreConfig.getInstance().MMO_MAX_SEND_PER_PASS);
		sc.setSleepTime(NetcoreConfig.getInstance().MMO_SELECTOR_SLEEP_TIME);
		sc.setHelperBufferCount(NetcoreConfig.getInstance().MMO_HELPER_BUFFER_COUNT);
		
		final L2LoginPacketHandler lph = new L2LoginPacketHandler();
		final SelectorHelper sh = new SelectorHelper();
		try
		{
			selectorThread = new SelectorThread<>(sc, sh, lph, sh, sh);
		}
		catch (final IOException e)
		{
			LOGGER.fatal("Failed to open Selector", e);
			System.exit(1);
		}
		
		try
		{
			gameServerListener = new GameServerListener();
			gameServerListener.start();
			LOGGER.info("Listening for GameServers on " + Config.GAME_SERVER_LOGIN_HOST + ":" + Config.GAME_SERVER_LOGIN_PORT);
		}
		catch (final IOException e)
		{
			LOGGER.fatal("Failed to start the Game Server Listener" + e);
			System.exit(1);
		}
		
		try
		{
			selectorThread.openServerSocket(bindAddress, Config.PORT_LOGIN);
			selectorThread.start();
			LOGGER.info("Login Server ready on " + (bindAddress == null ? "*" : bindAddress.getHostAddress()) + ":" + Config.PORT_LOGIN);
			
		}
		catch (final IOException e)
		{
			LOGGER.error("Failed to open server socket", e);
			System.exit(1);
		}
		
		bindAddress = null;
	}
	
	public GameServerListener getGameServerListener()
	{
		return gameServerListener;
	}
	
	public void shutdown(final boolean restart)
	{
		LoginController.getInstance().shutdown();
		System.gc();
		Runtime.getRuntime().exit(restart ? 2 : 0);
	}
}
