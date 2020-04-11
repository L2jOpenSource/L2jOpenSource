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
 * @author KID modded for the peace loving fisherman by evill33t
 */

public class FishermanLeaderboard
{
	private static FishermanLeaderboard _instance;
	public Logger _log = Logger.getLogger(FishermanLeaderboard.class.getName());
	public Map<Integer, FishRank> _ranks = new HashMap<>();
	protected Future<?> _actionTask = null;
	protected int TASK_DELAY = Config.RANK_FISHERMAN_INTERVAL;
	protected Long nextTimeUpdateReward = 0L;
	
	public static FishermanLeaderboard getInstance()
	{
		if (_instance == null)
		{
			_instance = new FishermanLeaderboard();
		}
		
		return _instance;
	}
	
	public FishermanLeaderboard()
	{
		engineInit();
	}
	
	public void onCatch(int owner, String name)
	{
		FishRank ar = null;
		if (_ranks.get(owner) == null)
		{
			ar = new FishRank();
		}
		else
		{
			ar = _ranks.get(owner);
		}
		
		ar.cought();
		ar.name = name;
		_ranks.put(owner, ar);
	}
	
	public void onEscape(int owner, String name)
	{
		FishRank ar = null;
		if (_ranks.get(owner) == null)
		{
			ar = new FishRank();
		}
		else
		{
			ar = _ranks.get(owner);
		}
		
		ar.escaped();
		ar.name = name;
		_ranks.put(owner, ar);
	}
	
	public void stopTask()
	{
		if (_actionTask != null)
		{
			_actionTask.cancel(true);
		}
		
		_actionTask = null;
	}
	
	public class FishermanTask implements Runnable
	{
		@Override
		public void run()
		{
			formRank();
			nextTimeUpdateReward = System.currentTimeMillis() + (TASK_DELAY * 60000);
		}
	}
	
	public void startTask()
	{
		if (_actionTask == null)
		{
			_actionTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FishermanTask(), 1000, TASK_DELAY * 60000);
		}
	}
	
	public void formRank()
	{
		Map<Integer, Integer> scores = new HashMap<>();
		for (int obj : _ranks.keySet())
		{
			FishRank ar = _ranks.get(obj);
			scores.put(obj, ar.cought - ar.escaped);
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
		
		FishRank arTop = _ranks.get(idTop);
		
		if (arTop == null)
		{
			Broadcast.toAllOnlinePlayers(new CreatureSay(1, Say2.CRITICAL_ANNOUNCE, "", ("Fisherman: No winners at this time!")));
			_ranks.clear();
			return;
		}
		
		L2PcInstance winner = (L2PcInstance) L2World.getInstance().findObject(idTop);
		
		Broadcast.toAllOnlinePlayers(new CreatureSay(1, Say2.CRITICAL_ANNOUNCE, "", ("Attention Fishermans: " + arTop.name + " is the winner for this time with " + arTop.cought + "/" + arTop.escaped + ". Next calculation in " + Config.RANK_FISHERMAN_INTERVAL + " min(s).")));
		if ((winner != null) && (Config.RANK_FISHERMAN_REWARD_ID > 0) && (Config.RANK_FISHERMAN_REWARD_COUNT > 0))
		{
			winner.getInventory().addItem("FishManager", Config.RANK_FISHERMAN_REWARD_ID, Config.RANK_FISHERMAN_REWARD_COUNT, winner, null);
			if (Config.RANK_FISHERMAN_REWARD_COUNT > 1)
			{
				winner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(Config.RANK_FISHERMAN_REWARD_ID));
			}
			else
			{
				winner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(Config.RANK_FISHERMAN_REWARD_ID));
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
			FishRank ar = _ranks.get(obj);
			scores.put(obj, ar.cought - ar.escaped);
		}
		
		scores = Util.sortMap(scores, false);
		
		int counter = 0, max = 20;
		String pt = "<html><body><center>" + "<font color=\"cc00ad\">TOP " + max + " Fisherman</font><br>";
		
		pt += "<table width=260 border=0 cellspacing=0 cellpadding=0 bgcolor=333333>";
		pt += "<tr> <td align=center>No.</td> <td align=center>Name</td> <td align=center>Cought</td> <td align=center>Escaped</td> </tr>";
		pt += "<tr> <td align=center>&nbsp;</td> <td align=center>&nbsp;</td> <td align=center></td> <td align=center></td> </tr>";
		boolean inTop = false;
		for (int id : scores.keySet())
		{
			if (counter < max)
			{
				FishRank ar = _ranks.get(id);
				pt += tx(counter, ar.name, ar.cought, ar.escaped, id == owner);
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
			FishRank arMe = _ranks.get(owner);
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
				pt += tx(placeMe, arMe.name, arMe.cought, arMe.escaped, true);
			}
		}
		
		pt += "</table>";
		pt += "<br><br>";
		if ((Config.RANK_FISHERMAN_REWARD_ID > 0) && (Config.RANK_FISHERMAN_REWARD_COUNT > 0))
		{
			pt += "Next Reward Time in <font color=\"LEVEL\">" + calcMinTo() + " min(s)</font><br1>";
			pt += "<font color=\"aadd77\">" + Config.RANK_FISHERMAN_REWARD_COUNT + " &#" + Config.RANK_FISHERMAN_REWARD_ID + ";</font>";
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
	
	public class FishRank
	{
		public int cought, escaped;
		public String name;
		
		public FishRank()
		{
			cought = 0;
			escaped = 0;
		}
		
		public void cought()
		{
			cought++;
		}
		
		public void escaped()
		{
			escaped++;
		}
	}
}