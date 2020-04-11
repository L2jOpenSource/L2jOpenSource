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
package com.l2jserver.gameserver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;

import com.l2jserver.Config;

public class GameServerRestart
{
	private static GameServerRestart _instance = null;
	protected static final Logger _log = Logger.getLogger(GameServerRestart.class.getName());
	private Calendar NextRestart;
	private final SimpleDateFormat format = new SimpleDateFormat("HH:mm");
	
	public static GameServerRestart getInstance()
	{
		if (_instance == null)
		{
			_instance = new GameServerRestart();
		}
		return _instance;
	}
	
	public String getRestartNextTime()
	{
		if (this.NextRestart.getTime() != null)
		{
			return this.format.format(this.NextRestart.getTime());
		}
		
		return "[Auto Restart]: Error on getRestartNextTime.";
	}
	
	public void StartCalculationOfNextRestartTime()
	{
		_log.info("Auto Restart System: Enabled.");
		try
		{
			Calendar currentTime = Calendar.getInstance();
			Calendar testStartTime = null;
			long flush2 = 0L;
			long timeL = 0L;
			int count = 0;
			
			for (String timeOfDay : Config.GAME_SERVER_AUTO_RESTART_INTERVAL)
			{
				testStartTime = Calendar.getInstance();
				testStartTime.setLenient(true);
				String[] splitTimeOfDay = timeOfDay.split(":");
				testStartTime.set(11, Integer.parseInt(splitTimeOfDay[0]));
				testStartTime.set(12, Integer.parseInt(splitTimeOfDay[1]));
				testStartTime.set(13, 0);
				
				if (testStartTime.getTimeInMillis() < currentTime.getTimeInMillis())
				{
					testStartTime.add(5, 1);
				}
				
				timeL = testStartTime.getTimeInMillis() - currentTime.getTimeInMillis();
				
				if (count == 0)
				{
					flush2 = timeL;
					this.NextRestart = testStartTime;
				}
				
				if (timeL < flush2)
				{
					flush2 = timeL;
					this.NextRestart = testStartTime;
				}
				
				count++;
			}
			
			_log.info("Auto Restart System: Next restart time: " + this.NextRestart.getTime().toString());
			ThreadPoolManager.getInstance().scheduleGeneral(new StartRestartTask(), flush2);
		}
		catch (Exception e)
		{
			System.out.println("Auto Restart System: The auto restart has problem with the config file, please, check and correct it!");
		}
	}
	
	class StartRestartTask implements Runnable
	{
		StartRestartTask()
		{
		}
		
		@Override
		public void run()
		{
			GameServerRestart._log.info("Auto Restart System: Auto restart started.");
			Shutdown.getInstance().autoRestart(Config.GAME_SERVER_AUTO_RESTART_COUNTDOWN);
		}
	}
}