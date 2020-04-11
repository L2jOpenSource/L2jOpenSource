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
package com.l2jserver.gameserver.instancemanager.Leaderboards;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.l2jserver.Config;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.clientpackets.Say2;
import com.l2jserver.gameserver.network.serverpackets.CreatureSay;
import com.l2jserver.gameserver.network.serverpackets.ItemList;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.gameserver.util.Broadcast;
import com.l2jserver.util.Util;

/**
 * @author KID
 */

public class ArenaLeaderboard
{
	private static ArenaLeaderboard _instance;
	public Logger _log = Logger.getLogger(ArenaLeaderboard.class.getName());
	public Map<Integer, ArenaRank> _ranks = new HashMap<>();
	protected Future<?> _actionTask = null;
	protected int SAVETASK_DELAY = Config.RANK_ARENA_INTERVAL;
	protected Long nextTimeUpdateReward = 0L;
	
	public static ArenaLeaderboard getInstance()
	{
		if (_instance == null)
		{
			_instance = new ArenaLeaderboard();
		}
		
		return _instance;
	}
	
	public ArenaLeaderboard()
	{
		engineInit();
	}
	
	public void onKill(int owner, String name)
	{
		ArenaRank ar = null;
		if (_ranks.get(owner) == null)
		{
			ar = new ArenaRank();
		}
		else
		{
			ar = _ranks.get(owner);
		}
		
		ar.pvp();
		ar.name = name;
		_ranks.put(owner, ar);
	}
	
	public void onDeath(int owner, String name)
	{
		ArenaRank ar = null;
		if (_ranks.get(owner) == null)
		{
			ar = new ArenaRank();
		}
		else
		{
			ar = _ranks.get(owner);
		}
		
		ar.death();
		ar.name = name;
		_ranks.put(owner, ar);
	}
	
	public void startTask()
	{
		if (_actionTask == null)
		{
			_actionTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ArenaTask(), 1000, SAVETASK_DELAY * 60000);
		}
	}
	
	public void stopTask()
	{
		if (_actionTask != null)
		{
			_actionTask.cancel(true);
		}
		
		_actionTask = null;
	}
	
	public class ArenaTask implements Runnable
	{
		@Override
		public void run()
		{
			formRank();
			nextTimeUpdateReward = System.currentTimeMillis() + (SAVETASK_DELAY * 60000);
		}
	}
	
	public void formRank()
	{
		Map<Integer, Integer> scores = new HashMap<>();
		for (int obj : _ranks.keySet())
		{
			ArenaRank ar = _ranks.get(obj);
			scores.put(obj, ar.kills - ar.death);
		}
		
		int Top = -1;
		int idTop = 0;
		for (int id : scores.keySet())
		{
			if (scores.get(id) > Top)
			{
				idTop = id;
				Top = scores.get(id);
			}
		}
		
		ArenaRank arTop = _ranks.get(idTop);
		
		if (arTop == null)
		{
			Broadcast.toAllOnlinePlayers(new CreatureSay(1, Say2.CRITICAL_ANNOUNCE, "", ("PvP Arena Manager: No winners at this time!")));
			_ranks.clear();
			return;
		}
		
		L2PcInstance winner = (L2PcInstance) L2World.getInstance().findObject(idTop);
		
		Broadcast.toAllOnlinePlayers(new CreatureSay(1, Say2.CRITICAL_ANNOUNCE, "", ("PvP Arena Manager: " + arTop.name + " is the winner for this time with " + arTop.kills + "/" + arTop.death + ". Next calculation in " + Config.RANK_ARENA_INTERVAL + " min(s).")));
		if ((winner != null) && (Config.RANK_ARENA_REWARD_ID > 0) && (Config.RANK_ARENA_REWARD_COUNT > 0))
		{
			winner.getInventory().addItem("ArenaManager", Config.RANK_ARENA_REWARD_ID, Config.RANK_ARENA_REWARD_COUNT, winner, null);
			if (Config.RANK_ARENA_REWARD_COUNT > 1)
			{
				winner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(Config.RANK_ARENA_REWARD_ID));
			}
			else
			{
				winner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(Config.RANK_ARENA_REWARD_ID));
			}
			winner.sendPacket(new ItemList(winner, false));
		}
		_ranks.clear();
	}
	
	public String showHtm(int owner)
	{
		Map<Integer, Integer> scores = new HashMap<>();
		for (int obj : _ranks.keySet())
		{
			ArenaRank ar = _ranks.get(obj);
			scores.put(obj, ar.kills - ar.death);
		}
		
		scores = Util.sortMap(scores, false);
		
		int counter = 0, max = 20;
		String pt = "<html><body><center>" + "<font color=\"cc00ad\">Arena TOP " + max + " Players</font><br>";
		
		pt += "<table width=260 border=0 cellspacing=0 cellpadding=0 bgcolor=333333>";
		pt += "<tr> <td align=center>No.</td> <td align=center>Name</td> <td align=center>Kills</td> <td align=center>Deaths</td> </tr>";
		pt += "<tr> <td align=center>&nbsp;</td> <td align=center>&nbsp;</td> <td align=center></td> <td align=center></td> </tr>";
		boolean inTop = false;
		for (int id : scores.keySet())
		{
			if (counter < max)
			{
				ArenaRank ar = _ranks.get(id);
				pt += tx(counter, ar.name, ar.kills, ar.death, id == owner);
				if (id == owner)
				{
					inTop = true;
				}
				
				counter++;
			}
			else
			{
				break;
			}
		}
		
		if (!inTop)
		{
			ArenaRank arMe = _ranks.get(owner);
			if (arMe != null)
			{
				pt += "<tr> <td align=center>...</td> <td align=center>...</td> <td align=center>...</td> <td align=center>...</td> </tr>";
				int placeMe = 0;
				for (int idMe : scores.keySet())
				{
					placeMe++;
					if (idMe == owner)
					{
						break;
					}
				}
				pt += tx(placeMe, arMe.name, arMe.kills, arMe.death, true);
			}
		}
		
		pt += "</table>";
		pt += "<br><br>";
		if ((Config.RANK_ARENA_REWARD_ID > 0) && (Config.RANK_ARENA_REWARD_COUNT > 0))
		{
			pt += "Next Reward Time in <font color=\"LEVEL\">" + calcMinTo() + " min(s)</font><br1>";
			pt += "<font color=\"aadd77\">" + Config.RANK_ARENA_REWARD_COUNT + " &#" + Config.RANK_ARENA_REWARD_ID + ";</font>";
		}
		
		pt += "</center></body></html>";
		
		return pt;
	}
	
	private int calcMinTo()
	{
		return ((int) (nextTimeUpdateReward - System.currentTimeMillis())) / 60000;
	}
	
	private String tx(int counter, String name, int kills, int deaths, boolean mi)
	{
		String t = "";
		
		t += "	<tr>" + "<td align=center>" + (mi ? "<font color=\"LEVEL\">" : "") + (counter + 1) + ".</td>" + "<td align=center>" + name + "</td>" + "<td align=center>" + kills + "</td>" + "<td align=center>" + deaths + "" + (mi ? "</font>" : "") + " </td>" + "</tr>";
		
		return t;
	}
	
	public void engineInit()
	{
		_ranks = new HashMap<>();
		startTask();
		_log.info(getClass().getSimpleName() + ": Enabled.");
	}
	
	public class ArenaRank
	{
		public int kills, death, classId;
		public String name;
		
		public ArenaRank()
		{
			kills = 0;
			death = 0;
		}
		
		public void pvp()
		{
			kills++;
		}
		
		public void death()
		{
			death++;
		}
	}
}