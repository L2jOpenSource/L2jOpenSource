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
package com.l2jserver.gameserver.instancemanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.data.xml.impl.DoorData;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.util.Rnd;

/**
 * Seed Of Infinity Manager.
 * @author MaGa1, Sacrifice
 */
public final class SeedOfInfinityManager
{
	// Logger
	private static final Logger LOG = LoggerFactory.getLogger(SeedOfInfinityManager.class.getSimpleName());
	
	private static final int DOOR_ID = 14240102;
	
	private static final int ZONE_ID = 200033;
	
	private static final long SOI_OPEN_TIME = 86400000; // 24 Hours
	
	private static final Location[] OPEN_SEED_TELEPORT_LOCS =
	{
		new Location(-179537, 209551, -15504),
		new Location(-179779, 212540, -15520),
		new Location(-177028, 211135, -15520),
		new Location(-176355, 208043, -15520),
		new Location(-179284, 205990, -15520),
		new Location(-182268, 208218, -15520),
		new Location(-182069, 211140, -15520),
		new Location(-176036, 210002, -11948),
		new Location(-176039, 208203, -11949),
		new Location(-183288, 208205, -11939),
		new Location(-183290, 210004, -11939),
		new Location(-187776, 205696, -9536),
		new Location(-186327, 208286, -9536),
		new Location(-184429, 211155, -9536),
		new Location(-182811, 213871, -9504),
		new Location(-180921, 216789, -9536),
		new Location(-177264, 217760, -9536),
		new Location(-173727, 218169, -9536)
	};
	
	private static SeedOfInfinityManager _instance = null;
	
	public static SeedOfInfinityManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new SeedOfInfinityManager();
		}
		return _instance;
	}
	
	public SeedOfInfinityManager()
	{
		if (isSeedOpen())
		{
			openSeed(getOpenedTime());
		}
		LOG.info("{}: Current stage is: {}", SeedOfInfinityManager.class.getSimpleName(), getCurrentStage());
	}
	
	public static int getCurrentStage()
	{
		return GlobalVariablesManager.getInstance().getInt("SoI_Stage");
	}
	
	public static long getOpenedTime()
	{
		if (getCurrentStage() != 3)
		{
			return 0L;
		}
		return (GlobalVariablesManager.getInstance().getLong("SoI_Opened") * 1000L) - System.currentTimeMillis();
	}
	
	public static void setCurrentStage(int stage)
	{
		if (getCurrentStage() == stage)
		{
			return;
		}
		if (stage == 3)
		{
			openSeed(SOI_OPEN_TIME);
		}
		else if (isSeedOpen())
		{
			closeSeed();
		}
		GlobalVariablesManager.getInstance().set("SoI_Stage", stage);
		setCohemenesCount(0);
		setEkimusCount(0);
		setHoEDefCount(0);
		LOG.info("{}: Set to stage {}", SeedOfInfinityManager.class.getSimpleName(), stage);
	}
	
	public static boolean isSeedOpen()
	{
		return getOpenedTime() > 0L;
	}
	
	public static void openSeed(long time)
	{
		if (time <= 0L)
		{
			return;
		}
		GlobalVariablesManager.getInstance().set("SoI_Opened", (System.currentTimeMillis() + time) / 1000L);
		LOG.info("{}: Opening the seed at {}", SeedOfInfinityManager.class.getSimpleName(), formatTime((int) time / 1000));
		DoorData.getInstance().getDoor(DOOR_ID).openMe();
		
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			closeSeed();
			setCurrentStage(4);
		}, time);
	}
	
	public static void closeSeed()
	{
		LOG.info("{}: Closing the seed", SeedOfInfinityManager.class.getSimpleName());
		GlobalVariablesManager.getInstance().set("SoI_Opened", 0L);
		DoorData.getInstance().getDoor(DOOR_ID).closeMe();
		for (L2PcInstance ch : ZoneManager.getInstance().getZoneById(ZONE_ID).getPlayersInside())
		{
			if (ch != null)
			{
				ch.teleToLocation(-183285, 205996, -12896);
			}
		}
	}
	
	public static void notifyCohemenesKill()
	{
		if (getCurrentStage() == 1)
		{
			if (getCohemenesCount() < 9)
			{
				setCohemenesCount(getCohemenesCount() + 1);
			}
			else
			{
				setCurrentStage(2);
			}
		}
	}
	
	public static void notifyEkimusKill()
	{
		if (getCurrentStage() == 2)
		{
			if (getEkimusCount() < 5)
			{
				setEkimusCount(getEkimusCount() + 1);
			}
			else
			{
				setCurrentStage(3);
			}
		}
	}
	
	public static void notifyHoEDefSuccess()
	{
		if (getCurrentStage() == 4)
		{
			if (getHoEDefCount() < 9)
			{
				setHoEDefCount(getHoEDefCount() + 1);
			}
			else
			{
				setCurrentStage(5);
			}
		}
	}
	
	public static void setCohemenesCount(int i)
	{
		GlobalVariablesManager.getInstance().set("SoI_CohemenesCount", i);
	}
	
	public static void setEkimusCount(int i)
	{
		GlobalVariablesManager.getInstance().set("SoI_EkimusCount", i);
	}
	
	public static void setHoEDefCount(int i)
	{
		GlobalVariablesManager.getInstance().set("SoI_HoEDefKillCount", i);
	}
	
	public static int getCohemenesCount()
	{
		return GlobalVariablesManager.getInstance().getInt("SoI_CohemenesCount");
	}
	
	public static int getEkimusCount()
	{
		return GlobalVariablesManager.getInstance().getInt("SoI_EkimusCount");
	}
	
	public static int getHoEDefCount()
	{
		return GlobalVariablesManager.getInstance().getInt("SoI_HoEDefKillCount");
	}
	
	public static String formatTime(int time)
	{
		if (time == 0)
		{
			return "now";
		}
		time = Math.abs(time);
		String ret = "";
		final long numDays = time / 86400;
		time -= numDays * 86400;
		final long numHours = time / 3600;
		time -= numHours * 3600;
		final long numMins = time / 60;
		time -= numMins * 60;
		final long numSeconds = time;
		if (numDays > 0)
		{
			ret += numDays + "d ";
		}
		if (numHours > 0)
		{
			ret += numHours + "h ";
		}
		if (numMins > 0)
		{
			ret += numMins + "m ";
		}
		if (numSeconds > 0)
		{
			ret += numSeconds + "s";
		}
		return ret.trim();
	}
	
	public static void teleportInSeed(L2PcInstance player)
	{
		player.teleToLocation(OPEN_SEED_TELEPORT_LOCS[Rnd.get(OPEN_SEED_TELEPORT_LOCS.length)], false);
	}
}
