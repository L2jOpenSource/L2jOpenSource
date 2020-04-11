package com.l2jfrozen.gameserver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.Memory;
import com.l2jfrozen.util.Util;

/**
 * Server status
 * @author  Nefer
 * @version 1.0
 */
public class ServerStatus
{
	protected static final Logger LOGGER = Logger.getLogger("Loader");
	protected ScheduledFuture<?> scheduledTask;
	
	protected ServerStatus()
	{
		scheduledTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ServerStatusTask(), 1800000, 3600000);
	}
	
	protected class ServerStatusTask implements Runnable
	{
		protected final SimpleDateFormat fmt = new SimpleDateFormat("H:mm.");
		
		@Override
		public void run()
		{
			int activePlayers = 0;
			int offlinePlayers = 0;
			
			for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				if (player.isInOfflineMode())
				{
					offlinePlayers++;
				}
				else
				{
					activePlayers++;
				}
			}
			
			Util.printSection("Server Status");
			LOGGER.info("Server Time: " + fmt.format(new Date(System.currentTimeMillis())));
			LOGGER.info("Active Players Online: " + activePlayers);
			LOGGER.info("Offline Players Online: " + offlinePlayers);
			LOGGER.info("Threads: " + Thread.activeCount());
			LOGGER.info("Free Memory: " + Memory.getFreeMemory() + " MB");
			LOGGER.info("Used memory: " + Memory.getUsedMemory() + " MB");
			Util.printSection("Server Status");
		}
	}
	
	public static ServerStatus getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ServerStatus instance = new ServerStatus();
	}
}