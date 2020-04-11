package com.l2jfrozen.gameserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.controllers.TradeController;
import com.l2jfrozen.gameserver.datatables.OfflineTradeTable;
import com.l2jfrozen.gameserver.managers.CastleManorManager;
import com.l2jfrozen.gameserver.managers.CursedWeaponsManager;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.managers.ItemsOnGroundManager;
import com.l2jfrozen.gameserver.managers.QuestManager;
import com.l2jfrozen.gameserver.managers.RaidBossSpawnManager;
import com.l2jfrozen.gameserver.managers.SchemeBufferManager;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSignsFestival;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.gameserverpackets.ServerStatus;
import com.l2jfrozen.gameserver.network.serverpackets.ServerClose;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.thread.LoginServerThread;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.database.L2DatabaseFactory;

import main.EngineModsManager;

/**
 * This class provides the functions for shutting down and restarting the server It closes all open client connections and saves all data.
 * @version $Revision: 1.2.4.6 $ $Date: 2009/05/12 19:45:09 $
 */
public class Shutdown extends Thread
{
	private static final String UPDATE_ALL_CHARACTERS_OFFLINE = "UPDATE characters SET online=0";
	
	public enum ShutdownModeType1
	{
		SIGTERM("Terminating"),
		SHUTDOWN("Shutting down"),
		RESTART("Restarting"),
		ABORT("Aborting"),
		TASK_SHUT("Shuting down"),
		TASK_RES("Restarting"),
		TELL_SHUT("Shuting down"),
		TELL_RES("Restarting");
		
		private final String modeText;
		
		ShutdownModeType1(final String modeText)
		{
			this.modeText = modeText;
		}
		
		public String getText()
		{
			return modeText;
		}
	}
	
	protected static final Logger LOGGER = Logger.getLogger(Shutdown.class);
	
	private static Shutdown instance;
	private static Shutdown counterInstance = null;
	private int secondsShut;
	private int shutdownMode;
	private boolean shutdownStarted;
	
	/** 0 */
	public static final int SIGTERM = 0;
	/** 1 */
	public static final int GM_SHUTDOWN = 1;
	/** 2 */
	public static final int GM_RESTART = 2;
	/** 3 */
	public static final int ABORT = 3;
	/** 4 */
	public static final int TASK_SHUTDOWN = 4;
	/** 5 */
	public static final int TASK_RESTART = 5;
	
	private static final String[] MODE_TEXT =
	{
		"SIGTERM",
		"shutting down",
		"restarting",
		"aborting", // standart
		"shutting down",
		"restarting", // task
	};
	
	/**
	 * Default constructor is only used internal to create the shutdown-hook instance
	 */
	public Shutdown()
	{
		secondsShut = -1;
		shutdownMode = SIGTERM;
		shutdownStarted = false;
	}
	
	/**
	 * This creates a count down instance of Shutdown.
	 * @param seconds how many seconds until shutdown
	 * @param restart true is the server shall restart after shutdown
	 * @param task
	 */
	public Shutdown(int seconds, final boolean restart, final boolean task)
	{
		if (seconds < 0)
		{
			seconds = 0;
		}
		
		secondsShut = seconds;
		
		if (restart)
		{
			if (!task)
			{
				shutdownMode = GM_RESTART;
			}
			else
			{
				shutdownMode = TASK_RESTART;
			}
		}
		else
		{
			if (!task)
			{
				shutdownMode = GM_SHUTDOWN;
			}
			else
			{
				shutdownMode = TASK_SHUTDOWN;
			}
		}
		
		shutdownStarted = false;
	}
	
	/**
	 * get the shutdown-hook instance the shutdown-hook instance is created by the first call of this function, but it has to be registered externally.
	 * @return instance of Shutdown, to be used as shutdown hook
	 */
	public static Shutdown getInstance()
	{
		if (instance == null)
		{
			instance = new Shutdown();
		}
		return instance;
	}
	
	public boolean isShutdownStarted()
	{
		boolean output = shutdownStarted;
		
		// if a counter is started, the value of shutdownstarted is of counterinstance
		if (counterInstance != null)
		{
			output = counterInstance.shutdownStarted;
		}
		
		return output;
	}
	
	/**
	 * this function is called, when a new thread starts if this thread is the thread of getInstance, then this is the shutdown hook and we save all data and disconnect all clients. after this thread ends, the server will completely exit if this is not the thread of getInstance, then this is a countdown
	 * thread. we start the countdown, and when we finished it, and it was not aborted, we tell the shutdown-hook why we call exit, and then call exit when the exit status of the server is 1, startServer.sh / startServer.bat will restart the server.
	 */
	@Override
	public void run()
	{
		if (this == instance)
		{
			closeServer();
		}
		else
		{
			// gm shutdown: send warnings and then call exit to start shutdown sequence
			countdown();
			
			if (shutdownMode != ABORT)
			{
				// last point where logging is operational :(
				LOGGER.warn("GM shutdown countdown is over. " + MODE_TEXT[shutdownMode] + " NOW!");
				closeServer();
			}
		}
	}
	
	/**
	 * This functions starts a shutdown countdown
	 * @param activeChar GM who issued the shutdown command
	 * @param seconds    seconds until shutdown
	 * @param restart    true if the server will restart after shutdown
	 */
	public void startShutdown(final L2PcInstance activeChar, final int seconds, final boolean restart)
	{
		if (restart)
		{
			shutdownMode = GM_RESTART;
		}
		else
		{
			shutdownMode = GM_SHUTDOWN;
		}
		
		if (activeChar != null)
		{
			LOGGER.warn("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") issued shutdown command. " + MODE_TEXT[shutdownMode] + " in " + seconds + " seconds!");
		}
		
		if (shutdownMode > 0)
		{
			Announcements an = Announcements.getInstance();
			an.announceToAll("Server is " + MODE_TEXT[shutdownMode] + " in " + seconds + " seconds!");
			an.announceToAll("Please exit game now!!");
		}
		
		if (counterInstance != null)
		{
			counterInstance.abort();
		}
		
		// the main instance should only run for shutdown hook, so we start a new instance
		counterInstance = new Shutdown(seconds, restart, false);
		counterInstance.start();
	}
	
	public int getCountdown()
	{
		return secondsShut;
	}
	
	/**
	 * This function aborts a running countdown
	 * @param activeChar GM who issued the abort command
	 */
	public void abort(final L2PcInstance activeChar)
	{
		Announcements an = Announcements.getInstance();
		
		if (activeChar != null)
		{
			LOGGER.warn("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") issued shutdown ABORT. " + MODE_TEXT[shutdownMode] + " has been stopped!");
		}
		else
		{
			LOGGER.warn("External Service issued shutdown ABORT. " + MODE_TEXT[shutdownMode] + " has been stopped!");
		}
		
		an.announceToAll("Server aborts " + MODE_TEXT[shutdownMode] + " and continues normal operation!");
		an = null;
		
		if (counterInstance != null)
		{
			counterInstance.abort();
		}
	}
	
	/**
	 * set shutdown mode to ABORT
	 */
	private void abort()
	{
		shutdownMode = ABORT;
	}
	
	/**
	 * this counts the countdown and reports it to all players countdown is aborted if mode changes to ABORT
	 */
	/**
	 * this counts the countdown and reports it to all players countdown is aborted if mode changes to ABORT
	 */
	private void countdown()
	{
		try
		{
			while (secondsShut > 0)
			{
				int seconds;
				int minutes;
				int hours;
				
				seconds = secondsShut;
				minutes = seconds / 60;
				hours = seconds / 3600;
				
				// announce only every minute after 10 minutes left and every second after 20 seconds
				if ((seconds <= 20 || seconds == minutes * 10) && seconds <= 600 && hours <= 1)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS);
					sm.addString(Integer.toString(seconds));
					Announcements.getInstance().announceToAll(sm);
					sm = null;
				}
				
				try
				{
					if (seconds <= 60)
					{
						LoginServerThread.getInstance().setServerStatus(ServerStatus.STATUS_DOWN);
					}
				}
				catch (final Exception e)
				{
					// do nothing, we maybe are not connected to LS anymore
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
				}
				
				secondsShut--;
				
				final int delay = 1000; // milliseconds
				Thread.sleep(delay);
				
				if (shutdownMode == ABORT)
				{
					break;
				}
			}
		}
		catch (final InterruptedException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
	}
	
	private void closeServer()
	{
		shutdownStarted = true;
		
		try
		{
			LoginServerThread.getInstance().interrupt();
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				t.printStackTrace();
			}
		}
		
		// saveData sends messages to exit players, so shutdown selector after it
		saveData();
		
		try
		{
			GameTimeController.getInstance().stopTimer();
			GameServer.getSelectorThread().shutdown();
			ThreadPoolManager.getInstance().shutdown();
			
			LOGGER.info("Committing all data, last chance...");
			
			L2DatabaseFactory.getInstance().shutdown();
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				t.printStackTrace();
			}
		}
		
		LOGGER.info("All database data committed.");
		
		System.runFinalization();
		System.gc();
		
		LOGGER.info("Memory cleanup, recycled unused objects.");
		
		LOGGER.info("[STATUS] Server shutdown successfully.");
		
		if (instance.shutdownMode == GM_RESTART)
		{
			Runtime.getRuntime().halt(2);
		}
		else if (instance.shutdownMode == TASK_RESTART)
		{
			Runtime.getRuntime().halt(5);
		}
		else if (instance.shutdownMode == TASK_SHUTDOWN)
		{
			Runtime.getRuntime().halt(4);
		}
		else
		{
			Runtime.getRuntime().halt(0);
		}
	}
	
	/**
	 * this sends a last byebye, disconnects all players and saves data
	 */
	private synchronized void saveData()
	{
		Announcements an = Announcements.getInstance();
		
		switch (shutdownMode)
		{
			case SIGTERM:
				LOGGER.info("SIGTERM received. Shutting down NOW!");
				break;
			
			case GM_SHUTDOWN:
				LOGGER.info("GM shutdown received. Shutting down NOW!");
				break;
			
			case GM_RESTART:
				LOGGER.info("GM restart received. Restarting NOW!");
				break;
			
			case TASK_SHUTDOWN:
				LOGGER.info("Auto task shutdown received. Shutting down NOW!");
				break;
			
			case TASK_RESTART:
				LOGGER.info("Auto task restart received. Restarting NOW!");
				break;
		}
		
		try
		{
			an.announceToAll("Server is " + MODE_TEXT[shutdownMode] + " NOW!");
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				t.printStackTrace();
			}
		}
		
		try
		{
			if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
			{
				OfflineTradeTable.storeOffliners();
			}
		}
		catch (final Throwable t)
		{
			LOGGER.error("Error saving offline shops.", t);
		}
		
		// Save all Grandboss status
		GrandBossManager.getInstance().cleanUp();
		LOGGER.info("GrandBossManager: All Grand Boss info saved!!");
		
		EngineModsManager.onShutDown();
		
		// Disconnect all the players from the server
		disconnectAllCharacters();
		
		// Save players data!
		saveAllPlayers();
		setAllCharacterOffline();
		SchemeBufferManager.getInstance().saveSchemes();
		
		// Seven Signs data is now saved along with Festival data.
		if (!SevenSigns.getInstance().isSealValidationPeriod())
		{
			SevenSignsFestival.getInstance().saveFestivalData(false);
		}
		
		// Save Seven Signs data before closing. :)
		SevenSigns.getInstance().saveSevenSignsData(null, true);
		LOGGER.info("SevenSigns: All info saved!!");
		
		// Save all raidboss status
		RaidBossSpawnManager.getInstance().cleanUp();
		LOGGER.info("RaidBossSpawnManager: All raidboss info saved!!");
		
		// Save data CountStore
		TradeController.getInstance().dataCountStore();
		LOGGER.info("TradeController: All count Item Saved");
		
		// Save Olympiad status
		try
		{
			Olympiad.getInstance().saveOlympiadStatus();
		}
		catch (final Exception e)
		{
			LOGGER.error("Error saving Olympiadds. ", e);
		}
		
		// Save Cursed Weapons data before closing.
		CursedWeaponsManager.getInstance().saveData();
		
		// Save all manor data
		CastleManorManager.getInstance().save();
		
		// Save all global (non-player specific) Quest data that needs to persist after reboot
		if (!Config.ALT_DEV_NO_QUESTS)
		{
			QuestManager.getInstance().save();
		}
		
		// Save items on ground before closing
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().saveInDb();
			ItemsOnGroundManager.getInstance().cleanUp();
			LOGGER.info("ItemsOnGroundManager: All items on ground saved!!");
		}
	}
	
	private void saveAllPlayers()
	{
		LOGGER.info("Saving all players data...");
		
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			if (player == null)
			{
				continue;
			}
			
			try
			{
				player.store();
			}
			catch (final Throwable t)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					t.printStackTrace();
				}
			}
		}
	}
	
	private void setAllCharacterOffline()
	{
		LOGGER.info("Placing all players offline...");
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement pst = con.prepareStatement(UPDATE_ALL_CHARACTERS_OFFLINE))
		{
			pst.executeUpdate();
		}
		catch (SQLException e)
		{
			LOGGER.error("IdFactory.setAllCharacterOffline : Could not set all player offline", e);
		}
	}
	
	/**
	 * this disconnects all clients from the server
	 */
	private void disconnectAllCharacters()
	{
		LOGGER.info("Disconnecting all players from the Server...");
		
		for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			if (player == null)
			{
				continue;
			}
			
			try
			{
				// Player Disconnect
				if (player.getClient() != null)
				{
					player.getClient().sendPacket(ServerClose.STATIC_PACKET);
					player.getClient().close(0);
					player.getClient().setActiveChar(null);
					player.setClient(null);
					
				}
			}
			catch (final Throwable t)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					t.printStackTrace();
				}
			}
		}
		
	}
	
}
