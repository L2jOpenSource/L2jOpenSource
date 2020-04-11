package net.sf.l2j.loginserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.LogManager;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.mmocore.SelectorConfig;
import net.sf.l2j.commons.mmocore.SelectorThread;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.loginserver.data.manager.GameServerManager;
import net.sf.l2j.loginserver.data.manager.IpBanManager;
import net.sf.l2j.loginserver.data.sql.AccountTable;
import net.sf.l2j.loginserver.network.LoginClient;
import net.sf.l2j.loginserver.network.LoginPacketHandler;

public class LoginServer
{
	private static final CLogger LOGGER = new CLogger(LoginServer.class.getName());
	
	public static final int PROTOCOL_REV = 0x0102;
	
	private static LoginServer _loginServer;
	
	private GameServerListener _gameServerListener;
	private SelectorThread<LoginClient> _selectorThread;
	
	public static void main(String[] args) throws Exception
	{
		_loginServer = new LoginServer();
	}
	
	public LoginServer() throws Exception
	{
		// Create log folder
		new File("./log").mkdir();
		new File("./log/console").mkdir();
		new File("./log/error").mkdir();
		
		// Create input stream for log file -- or store file data into memory
		try (InputStream is = new FileInputStream(new File("config/logging.properties")))
		{
			LogManager.getLogManager().readConfiguration(is);
		}
		
		StringUtil.printSection("aCis");
		
		// Initialize config
		Config.loadLoginServer();
		
		// Factories
		L2DatabaseFactory.getInstance();
		
		AccountTable.getInstance();
		
		StringUtil.printSection("LoginController");
		LoginController.getInstance();
		
		StringUtil.printSection("GameServerManager");
		GameServerManager.getInstance();
		
		StringUtil.printSection("Ban List");
		IpBanManager.getInstance();
		
		StringUtil.printSection("IP, Ports & Socket infos");
		InetAddress bindAddress = null;
		if (!Config.LOGIN_BIND_ADDRESS.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.LOGIN_BIND_ADDRESS);
			}
			catch (UnknownHostException uhe)
			{
				LOGGER.error("The LoginServer bind address is invalid, using all available IPs.", uhe);
			}
		}
		
		final SelectorConfig sc = new SelectorConfig();
		sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;
		
		final LoginPacketHandler lph = new LoginPacketHandler();
		final SelectorHelper sh = new SelectorHelper();
		try
		{
			_selectorThread = new SelectorThread<>(sc, sh, lph, sh, sh);
		}
		catch (IOException ioe)
		{
			LOGGER.error("Failed to open selector.", ioe);
			
			System.exit(1);
		}
		
		try
		{
			_gameServerListener = new GameServerListener();
			_gameServerListener.start();
			
			LOGGER.info("Listening for gameservers on {}:{}.", Config.GAME_SERVER_LOGIN_HOST, Config.GAME_SERVER_LOGIN_PORT);
		}
		catch (IOException ioe)
		{
			LOGGER.error("Failed to start the gameserver listener.", ioe);
			
			System.exit(1);
		}
		
		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_LOGIN);
		}
		catch (IOException ioe)
		{
			LOGGER.error("Failed to open server socket.", ioe);
			
			System.exit(1);
		}
		_selectorThread.start();
		LOGGER.info("Loginserver ready on {}:{}.", (bindAddress == null) ? "*" : bindAddress.getHostAddress(), Config.PORT_LOGIN);
		
		StringUtil.printSection("Waiting for gameserver answer");
	}
	
	public static LoginServer getInstance()
	{
		return _loginServer;
	}
	
	public GameServerListener getGameServerListener()
	{
		return _gameServerListener;
	}
	
	public void shutdown(boolean restart)
	{
		Runtime.getRuntime().exit(restart ? 2 : 0);
	}
}