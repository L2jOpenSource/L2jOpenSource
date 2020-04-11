package com.l2jfrozen.gameserver.controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlEvent;
import com.l2jfrozen.gameserver.managers.DayNightSpawnManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * This class ...
 * @version $Revision: 1.1.4.8 $ $Date: 2005/04/06 16:13:24 $
 */
public class GameTimeController
{
	static final Logger LOGGER = Logger.getLogger(GameTimeController.class);
	
	public static final int TICKS_PER_SECOND = 10;
	public static final int MILLIS_IN_TICK = 1000 / TICKS_PER_SECOND;
	
	private static GameTimeController instance = new GameTimeController();
	
	protected static int gameTicks;
	protected static long gameStartTime;
	protected static boolean isNight = false;
	
	private static List<L2Character> movingObjects = new ArrayList<>();
	
	protected static TimerThread timer;
	private final ScheduledFuture<?> timerWatcher;
	
	/**
	 * one ingame day is 240 real minutes
	 * @return
	 */
	public static GameTimeController getInstance()
	{
		return instance;
	}
	
	private GameTimeController()
	{
		gameStartTime = System.currentTimeMillis() - 3600000; // offset so that the server starts a day begin
		gameTicks = 3600000 / MILLIS_IN_TICK; // offset so that the server starts a day begin
		
		timer = new TimerThread();
		timer.start();
		
		timerWatcher = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
		{
			if (!timer.isAlive())
			{
				final String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
				LOGGER.warn(time + " TimerThread stop with following error. restart it.");
				if (timer.error != null)
				{
					timer.error.printStackTrace();
				}
				
				timer = new TimerThread();
				timer.start();
			}
		}, 0, 1000);
		
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
		{
			
			int hour = getGameTime() / 60 % 24; // Time in hour
			boolean tempIsNight = hour < 6;
			
			// If diff day/night state
			if (tempIsNight != isNight)
			{
				// Set current day/night varible to value of temp varible
				isNight = tempIsNight;
				DayNightSpawnManager.getInstance().notifyChangeMode();
			}
		}, 0, 1000);
		
	}
	
	public boolean isNowNight()
	{
		return isNight;
	}
	
	public int getGameTime()
	{
		return gameTicks / (TICKS_PER_SECOND * 10);
	}
	
	public static int getGameTicks()
	{
		return gameTicks;
	}
	
	/**
	 * Add a L2Character to movingObjects of GameTimeController.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All L2Character in movement are identified in <B>movingObjects</B> of GameTimeController.<BR>
	 * <BR>
	 * @param cha The L2Character to add to movingObjects of GameTimeController
	 */
	public synchronized void registerMovingObject(final L2Character cha)
	{
		if (cha == null)
		{
			return;
		}
		
		if (!movingObjects.contains(cha))
		{
			movingObjects.add(cha);
		}
	}
	
	/**
	 * Move all L2Characters contained in movingObjects of GameTimeController.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All L2Character in movement are identified in <B>movingObjects</B> of GameTimeController.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Update the position of each L2Character</li>
	 * <li>If movement is finished, the L2Character is removed from movingObjects</li>
	 * <li>Create a task to update the knownObject and knowPlayers of each L2Character that finished its movement and of their already known L2Object then notify AI with EVT_ARRIVED</li><BR>
	 * <BR>
	 */
	protected synchronized void moveObjects()
	{
		// Get all L2Character from the ArrayList movingObjects and put them into a table
		final L2Character[] chars = movingObjects.toArray(new L2Character[movingObjects.size()]);
		
		// Create an ArrayList to contain all L2Character that are arrived to destination
		List<L2Character> ended = null;
		
		// Go throw the table containing L2Character in movement
		for (final L2Character cha : chars)
		{
			
			// Update the position of the L2Character and return True if the movement is finished
			final boolean end = cha.updatePosition(gameTicks);
			
			// If movement is finished, the L2Character is removed from movingObjects and added to the ArrayList ended
			if (end)
			{
				movingObjects.remove(cha);
				if (ended == null)
				{
					ended = new ArrayList<>();
				}
				
				ended.add(cha);
			}
		}
		
		// Create a task to update the knownObject and knowPlayers of each L2Character that finished its movement and of their already known L2Object
		// then notify AI with EVT_ARRIVED
		// TODO: maybe a general TP is needed for that kinda stuff (all knownlist updates should be done in a TP anyway).
		if (ended != null)
		{
			ThreadPoolManager.getInstance().executeTask(new MovingObjectArrived(ended));
		}
		
	}
	
	public void stopTimer()
	{
		timerWatcher.cancel(true);
		timer.interrupt();
	}
	
	class TimerThread extends Thread
	{
		protected Exception error;
		
		public TimerThread()
		{
			super("GameTimeController");
			setDaemon(true);
			setPriority(MAX_PRIORITY);
			error = null;
		}
		
		@Override
		public void run()
		{
			
			for (;;)
			{
				final int oldTicks = gameTicks; // save old ticks value to avoid moving objects 2x in same tick
				long runtime = System.currentTimeMillis() - gameStartTime; // from server boot to now
				
				gameTicks = (int) (runtime / MILLIS_IN_TICK); // new ticks value (ticks now)
				
				if (oldTicks != gameTicks)
				{
					moveObjects(); // XXX: if this makes objects go slower, remove it
					// but I think it can't make that effect. is it better to call moveObjects() twice in same
					// tick to make-up for missed tick ? or is it better to ignore missed tick ?
					// (will happen very rarely but it will happen ... on garbage collection definitely)
				}
				
				runtime = System.currentTimeMillis() - gameStartTime - runtime;
				
				// calculate sleep time... time needed to next tick minus time it takes to call moveObjects()
				final int sleepTime = 1 + MILLIS_IN_TICK - (int) runtime % MILLIS_IN_TICK;
				
				// LOGGER.finest("TICK: "+_gameTicks);
				
				try
				{
					sleep(sleepTime); // hope other threads will have much more cpu time available now
					
				}
				catch (final InterruptedException ie)
				{
					// nothing
				}
				// SelectorThread most of all
			}
			
		}
	}
	
	/**
	 * Update the knownObject and knowPlayers of each L2Character that finished its movement and of their already known L2Object then notify AI with EVT_ARRIVED.<BR>
	 * <BR>
	 */
	class MovingObjectArrived implements Runnable
	{
		private final List<L2Character> ended;
		
		MovingObjectArrived(final List<L2Character> ended)
		{
			this.ended = ended;
		}
		
		@Override
		public void run()
		{
			for (final L2Character cha : ended)
			{
				try
				{
					cha.getKnownList().updateKnownObjects();
					cha.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);
				}
				catch (final NullPointerException e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
}
