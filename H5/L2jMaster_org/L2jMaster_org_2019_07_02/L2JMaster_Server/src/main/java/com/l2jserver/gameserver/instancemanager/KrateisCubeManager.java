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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.Config;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.datatables.SpawnTable;
import com.l2jserver.gameserver.model.L2Spawn;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.entity.KrateisCubeEngine;
import com.l2jserver.gameserver.network.clientpackets.Say2;
import com.l2jserver.gameserver.network.serverpackets.CreatureSay;
import com.l2jserver.gameserver.network.serverpackets.ExPVPMatchCCRecord;

/**
 * Krateis Cube Manager.
 * @author U3Games, Sacrifice
 */
public final class KrateisCubeManager
{
	private static final Logger LOG = LoggerFactory.getLogger(KrateisCubeManager.class.getName());
	
	private static final int MATCH_MANAGER = 32503;
	
	private static final Map<L2PcInstance, PlayerData> PLAYER_DATA = new ConcurrentHashMap<>();
	
	private L2Npc _npc = null;
	
	private L2Spawn _npcSpawn = null;
	
	private boolean _registerActive = false;
	
	private ScheduledFuture<?> _scheduledStartKCTask = null;
	
	public KrateisCubeManager()
	{
		boolean spawnManager = false;
		try
		{
			_npcSpawn = new L2Spawn(MATCH_MANAGER);
			_npcSpawn.setLocation(new Location(-70596, -71064, -1416, 0));
			_npcSpawn.setAmount(1);
			_npcSpawn.setRespawnDelay(1);
			SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);
			_npcSpawn.init();
			_npc = _npcSpawn.getLastSpawn();
			_npc.setCurrentHp(_npc.getMaxHp());
			_npc.isAggressive();
			_npc.decayMe();
			_npc.spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());
			spawnManager = true;
		}
		catch (Exception e)
		{
			spawnManager = false;
			LOG.info("Krateis Cube Manager: Error trying to spawn npc: {}", e);
		}
		
		if (spawnManager)
		{
			startEvent();
		}
		else
		{
			LOG.info("Krateis Cube Manager: Event can't be started because npc is disabled!");
		}
	}
	
	private enum MsgType
	{
		INITIALIZED,
		REGISTRATION,
		REGISTRATION_OVER,
		STARTED,
		ABORTED
	}
	
	/**
	 * Add kills in event
	 * @param player
	 * @return
	 */
	public int addPoints(L2PcInstance player)
	{
		final PlayerData playerData = PLAYER_DATA.get(player);
		if (playerData != null)
		{
			return playerData._kills++;
		}
		return 0;
	}
	
	/**
	 * isInside
	 * @param player
	 * @return
	 */
	public static boolean checkIsInsided(L2PcInstance player)
	{
		final PlayerData playerData = PLAYER_DATA.get(player);
		if (playerData == null)
		{
			return false;
		}
		
		if (!playerData._isInside)
		{
			return false;
		}
		return true;
	}
	
	/**
	 * Check if is valid for register in arena
	 * @param player
	 * @return
	 */
	public static boolean checkMaxPlayersArena(int arena)
	{
		int registerCount = 0;
		for (PlayerData playerData : PLAYER_DATA.values())
		{
			if (playerData == null)
			{
				return false;
			}
			
			if (playerData._isInside)
			{
				if (playerData._arena == arena)
				{
					registerCount++;
				}
			}
		}
		
		if (registerCount >= Config.KRATEIS_CUBE_MAX_PLAYER)
		{
			return false;
		}
		return true;
	}
	
	/**
	 * Check if is valid for create instance
	 * @param arena
	 * @return
	 */
	public boolean checkMinPlayersArena(int arena, boolean createInstance)
	{
		int count = 0;
		for (PlayerData playerData : PLAYER_DATA.values())
		{
			if (createInstance)
			{
				if ((playerData._isInside) && (playerData._arena == arena))
				{
					count++;
				}
			}
			else
			{
				if ((playerData._isRegister) && (playerData._arena == arena))
				{
					count++;
				}
			}
		}
		
		if (count >= Config.KRATEIS_CUBE_MIN_PLAYER)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * isRegistered
	 * @param player
	 * @return
	 */
	public boolean checkIsRegistered(L2PcInstance player)
	{
		final PlayerData playerData = PLAYER_DATA.get(player);
		if (playerData == null)
		{
			return false;
		}
		
		if (!playerData._isRegister)
		{
			return false;
		}
		return true;
	}
	
	private void checkRegistered()
	{
		if (_registerActive)
		{
			_registerActive = false;
		}
		
		getNpcManagerMessage(MsgType.REGISTRATION_OVER, 0);
		
		boolean createInstance = false;
		for (int instanceId = 1; instanceId < 4; instanceId++)
		{
			if (checkMinPlayersArena(instanceId, false))
			{
				createInstance = true;
				for (PlayerData playerData : PLAYER_DATA.values())
				{
					if ((playerData._isRegister) && (playerData._arena == instanceId))
					{
						playerData._isInside = true;
						playerData._isRegister = false;
						teleportToInstance(null);
					}
				}
			}
		}
		
		if (createInstance)
		{
			KrateisCubeEngine.getInstance().createInstances();
			getNpcManagerMessage(MsgType.STARTED, 0);
		}
		else
		{
			getNpcManagerMessage(MsgType.ABORTED, 0);
			startEvent();
		}
	}
	
	/**
	 * Delete players of event
	 */
	public void deleteInsideParticipants()
	{
		for (PlayerData playerData : PLAYER_DATA.values())
		{
			if (playerData != null)
			{
				if (playerData._isInside)
				{
					playerData.cleanValues();
				}
			}
		}
	}
	
	/**
	 * Get arena in event
	 * @param player
	 * @return
	 */
	public int getArena(L2PcInstance player)
	{
		final PlayerData playerData = PLAYER_DATA.get(player);
		if (playerData != null)
		{
			return playerData._arena;
		}
		return 0;
	}
	
	/**
	 * Get message to send
	 * @param state
	 * @param time
	 */
	private void getNpcManagerMessage(MsgType state, int time)
	{
		for (L2PcInstance player : _npc.getKnownList().getKnownPlayersInRadius(1500))
		{
			if (player != null)
			{
				switch (state)
				{
					case INITIALIZED:
						player.sendPacket(new CreatureSay(_npc.getObjectId(), Say2.ALL, _npc.getName(), "Register of Krateis Cube initialized!"));
						player.sendPacket(new CreatureSay(_npc.getObjectId(), Say2.ALL, _npc.getName(), time + " minute(s) until the match starts."));
						break;
					case REGISTRATION:
						player.sendPacket(new CreatureSay(_npc.getObjectId(), Say2.ALL, _npc.getName(), time + " minute(s) until the match starts."));
						break;
					case REGISTRATION_OVER:
						player.sendPacket(new CreatureSay(_npc.getObjectId(), Say2.ALL, _npc.getName(), "The registration period for this match is over"));
						break;
					case STARTED:
						player.sendPacket(new CreatureSay(_npc.getObjectId(), Say2.ALL, _npc.getName(), "Krateis Cube match has started!"));
						break;
					case ABORTED:
						player.sendPacket(new CreatureSay(_npc.getObjectId(), Say2.ALL, _npc.getName(), "Krateis Cube cancelled due to lack of participation."));
						break;
				}
			}
		}
		return;
	}
	
	public static List<L2PcInstance> getParticipants()
	{
		final ArrayList<L2PcInstance> participants = new ArrayList<>();
		for (L2PcInstance player : PLAYER_DATA.keySet())
		{
			if (player != null)
			{
				if (checkIsInsided(player))
				{
					if (!participants.contains(player))
					{
						participants.add(player);
					}
				}
			}
		}
		
		if (participants.isEmpty())
		{
			return null;
		}
		return participants;
	}
	
	/**
	 * Get map values of participants.
	 * @return
	 */
	public static Map<String, Integer> getParticipantsMatch()
	{
		final Map<String, Integer> participants = new HashMap<>();
		for (L2PcInstance player : getParticipants())
		{
			if (player != null)
			{
				if (!participants.containsKey(player.getName()))
				{
					participants.put(player.getName(), getPoints(player));
				}
			}
		}
		
		if (participants.isEmpty())
		{
			return null;
		}
		final Map<String, Integer> sortedMap = participants.entrySet().stream().sorted(Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return sortedMap;
	}
	
	/**
	 * Get kills in event
	 * @param player
	 * @return
	 */
	public static int getPoints(L2PcInstance player)
	{
		int kills = 0;
		final PlayerData playerData = PLAYER_DATA.get(player);
		if (playerData != null)
		{
			kills = playerData._kills;
		}
		return kills;
	}
	
	/**
	 * Check if is time to register
	 * @return
	 */
	public boolean isTimeToRegister()
	{
		return _registerActive;
	}
	
	/**
	 * Add a new player to start the event for the specified arena id
	 * @param player
	 * @param arena
	 */
	public void registerPlayer(L2PcInstance player, int arena)
	{
		PlayerData playerData = PLAYER_DATA.get(player);
		playerData = PLAYER_DATA.computeIfAbsent(player, data -> new PlayerData());
		PLAYER_DATA.put(player, playerData);
		playerData._isRegister = true;
		playerData._isInside = false;
		playerData._arena = arena;
		playerData._kills = 0;
	}
	
	public void startEvent()
	{
		if (!_registerActive)
		{
			_registerActive = true;
		}
		
		if (_scheduledStartKCTask != null)
		{
			_scheduledStartKCTask.cancel(false);
		}
		
		final Calendar cal = Calendar.getInstance();
		if (cal.get(Calendar.MINUTE) >= 57)
		{
			cal.add(Calendar.HOUR, 1);
			cal.set(Calendar.MINUTE, 27);
		}
		else if ((cal.get(Calendar.MINUTE) >= 0) && (cal.get(Calendar.MINUTE) <= 26))
		{
			cal.set(Calendar.MINUTE, 27);
		}
		else
		{
			cal.set(Calendar.MINUTE, 57);
			cal.set(Calendar.SECOND, 0);
		}
		
		ThreadPoolManager.getInstance().scheduleEvent(() -> startRegisterTime(), cal.getTimeInMillis() - System.currentTimeMillis());
		getNpcManagerMessage(MsgType.INITIALIZED, (int) (((cal.getTimeInMillis() - System.currentTimeMillis()) / (1000 * 60))));
		
		if (Config.KRATEIS_CUBE_EVENT_LOG_ENABLED)
		{
			LOG.info("Krateis Cube loaded, next match: {}", new Date(cal.getTimeInMillis()).toString());
		}
	}
	
	private void startRegisterTime()
	{
		if (_scheduledStartKCTask != null)
		{
			_scheduledStartKCTask.cancel(false);
		}
		getNpcManagerMessage(MsgType.REGISTRATION, Config.KRATEIS_CUBE_REGISTRATION_TIME);
		_scheduledStartKCTask = ThreadPoolManager.getInstance().scheduleEvent(() -> checkRegistered(), Config.KRATEIS_CUBE_REGISTRATION_TIME * 60 * 1000);
	}
	
	public void teleportOutEvent(L2PcInstance character)
	{
		if (character != null)
		{
			character.setInstanceId(0);
			character.teleToLocation(-70381, -70937, -1428, 0, 0);
		}
		else
		{
			for (L2PcInstance player : PLAYER_DATA.keySet())
			{
				if (checkIsInsided(player))
				{
					player.setInstanceId(0);
					player.teleToLocation(-70381, -70937, -1428, 0, 0);
					player.sendPacket(new ExPVPMatchCCRecord(0));
				}
			}
		}
	}
	
	public void teleportToInstance(L2PcInstance player)
	{
		if (player != null)
		{
			KrateisCubeEngine.getInstance().teleportToInstance(player, getArena(player));
		}
		else
		{
			for (L2PcInstance participant : PLAYER_DATA.keySet())
			{
				if (checkIsInsided(participant))
				{
					KrateisCubeEngine.getInstance().teleportToWaitingRoom(participant, getArena(participant));
				}
			}
		}
	}
	
	public void teleportToWaitingRoomInstance(L2PcInstance player)
	{
		KrateisCubeEngine.getInstance().countDownReturnMessage(player);
		ThreadPoolManager.getInstance().scheduleEvent(() -> KrateisCubeEngine.getInstance().teleportToWaitingRoom(player, getArena(player)), Config.KRATEIS_CUBE_TIME_TO_REVIVE * 1000);
	}
	
	/**
	 * Remove player of data map
	 * @param player
	 * @return
	 */
	public void unregisterPlayer(L2PcInstance player)
	{
		PLAYER_DATA.remove(player);
	}
	
	private static class PlayerData
	{
		protected int _arena;
		protected boolean _isInside;
		protected boolean _isRegister;
		protected int _kills;
		
		protected PlayerData()
		{
			// empty
		}
		
		protected void cleanValues()
		{
			_arena = 0;
			_isInside = false;
			_isRegister = false;
			_kills = 0;
		}
	}
	
	/**
	 * Gets the single instance
	 * @return single instance
	 */
	public static KrateisCubeManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final KrateisCubeManager _instance = new KrateisCubeManager();
	}
}