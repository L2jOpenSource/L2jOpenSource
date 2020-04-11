/*
 * Copyright (C) 2004-2015 L2J Server
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
package l2r.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2r.gameserver.GameTimeController;
import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.enums.RaidBossStatus;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2RaidBossInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author godson
 */
public final class DayNightSpawnManager
{
	private static Logger _log = LoggerFactory.getLogger(DayNightSpawnManager.class);
	
	private final List<L2Spawn> _dayCreatures = new ArrayList<>();
	private final List<L2Spawn> _nightCreatures = new ArrayList<>();
	private final Map<L2Spawn, L2RaidBossInstance> _bosses = new HashMap<>();
	
	// private static int _currentState; // 0 = Day, 1 = Night
	
	public static DayNightSpawnManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected DayNightSpawnManager()
	{
		// Prevent external initialization.
	}
	
	public void addDayCreature(L2Spawn spawnDat)
	{
		_dayCreatures.add(spawnDat);
	}
	
	public void addNightCreature(L2Spawn spawnDat)
	{
		_nightCreatures.add(spawnDat);
	}
	
	/**
	 * Spawn Day Creatures, and Unspawn Night Creatures
	 */
	public void spawnDayCreatures()
	{
		spawnCreatures(_nightCreatures, _dayCreatures, "night", "day");
	}
	
	/**
	 * Spawn Night Creatures, and Unspawn Day Creatures
	 */
	public void spawnNightCreatures()
	{
		spawnCreatures(_dayCreatures, _nightCreatures, "day", "night");
	}
	
	/**
	 * Manage Spawn/Respawn
	 * @param unSpawnCreatures List with spawns must be unspawned
	 * @param spawnCreatures List with spawns must be spawned
	 * @param UnspawnLogInfo String for log info for unspawned L2NpcInstance
	 * @param SpawnLogInfo String for log info for spawned L2NpcInstance
	 */
	private void spawnCreatures(List<L2Spawn> unSpawnCreatures, List<L2Spawn> spawnCreatures, String UnspawnLogInfo, String SpawnLogInfo)
	{
		try
		{
			if (!unSpawnCreatures.isEmpty())
			{
				int i = 0;
				for (L2Spawn spawn : unSpawnCreatures)
				{
					if (spawn == null)
					{
						continue;
					}
					
					spawn.stopRespawn();
					L2Npc last = spawn.getLastSpawn();
					if (last != null)
					{
						last.deleteMe();
						i++;
					}
				}
				_log.info(getClass().getSimpleName() + ": Removed " + i + " " + UnspawnLogInfo + " creatures");
			}
			
			int i = 0;
			for (L2Spawn spawnDat : spawnCreatures)
			{
				if (spawnDat == null)
				{
					continue;
				}
				spawnDat.startRespawn();
				spawnDat.doSpawn();
				i++;
			}
			
			_log.info(getClass().getSimpleName() + ": Spawned " + i + " " + SpawnLogInfo + " creatures");
		}
		catch (Exception e)
		{
			_log.warn("Error while spawning creatures: " + e.getMessage(), e);
		}
	}
	
	// vGodFather: missing retail messages
	public void shadowFeelMsg()
	{
		shadowFeelMsg(null);
	}
	
	public void shadowFeelMsg(L2PcInstance activeChar)
	{
		L2Skill skill = SkillData.getInstance().getInfo(294, 1);
		if (skill != null)
		{
			if (activeChar != null)
			{
				sendMessage(activeChar);
				return;
			}
			
			L2World.getInstance().getPlayers().forEach(player -> sendMessage(player));
		}
	}
	
	private void sendMessage(L2PcInstance player)
	{
		if ((player != null) && player.isOnline() && !player.isInOfflineMode() && (player.getRace().ordinal() == 2) && (player.getSkillLevel(294) > 0))
		{
			SystemMessageId messageId = GameTimeController.getInstance().isNight() ? SystemMessageId.NIGHT_EFFECT_S1_APPLIES : SystemMessageId.DAY_EFFECT_S1_DISAPPEARS;
			player.sendPacket(SystemMessage.getSystemMessage(messageId).addSkillName(294));
		}
	}
	
	private void changeMode(int mode)
	{
		if (_nightCreatures.isEmpty() && _dayCreatures.isEmpty() && _bosses.isEmpty())
		{
			return;
		}
		
		switch (mode)
		{
			case 0:
				spawnDayCreatures();
				specialNightBoss(0);
				shadowFeelMsg();
				break;
			case 1:
				spawnNightCreatures();
				specialNightBoss(1);
				shadowFeelMsg();
				break;
			default:
				_log.warn(getClass().getSimpleName() + ": Wrong mode sent");
				break;
		}
	}
	
	public DayNightSpawnManager trim()
	{
		((ArrayList<?>) _nightCreatures).trimToSize();
		((ArrayList<?>) _dayCreatures).trimToSize();
		return this;
	}
	
	public void notifyChangeMode()
	{
		try
		{
			if (GameTimeController.getInstance().isNight())
			{
				changeMode(1);
			}
			else
			{
				changeMode(0);
			}
		}
		catch (Exception e)
		{
			_log.warn("Error while notifyChangeMode(): " + e.getMessage(), e);
		}
	}
	
	public void cleanUp()
	{
		_nightCreatures.clear();
		_dayCreatures.clear();
		_bosses.clear();
	}
	
	private void specialNightBoss(int mode)
	{
		try
		{
			for (L2Spawn spawn : _bosses.keySet())
			{
				L2RaidBossInstance boss = _bosses.get(spawn);
				
				if ((boss == null) && (mode == 1))
				{
					boss = (L2RaidBossInstance) spawn.doSpawn();
					RaidBossSpawnManager.getInstance().notifySpawnNightBoss(boss);
					_bosses.remove(spawn);
					_bosses.put(spawn, boss);
					continue;
				}
				
				if ((boss == null) && (mode == 0))
				{
					continue;
				}
				
				if ((boss != null) && (boss.getId() == 25328) && (boss.getRaidStatus() == RaidBossStatus.ALIVE))
				{
					handleHellmans(boss, mode);
				}
				return;
			}
		}
		catch (Exception e)
		{
			_log.error(getClass().getSimpleName() + ": specialNightBoss exception", e);
		}
	}
	
	private void handleHellmans(L2RaidBossInstance boss, int mode)
	{
		switch (mode)
		{
			case 0:
				boss.deleteMe();
				_log.info(getClass().getSimpleName() + ": Deleting Hellman raidboss");
				break;
			case 1:
				boss.spawnMe();
				boss.setDecayed(false);
				_log.info(getClass().getSimpleName() + ": Spawning Hellman raidboss");
				break;
		}
	}
	
	public L2RaidBossInstance handleBoss(L2Spawn spawnDat)
	{
		if (_bosses.containsKey(spawnDat))
		{
			return _bosses.get(spawnDat);
		}
		
		if (GameTimeController.getInstance().isNight())
		{
			L2RaidBossInstance raidboss = (L2RaidBossInstance) spawnDat.doSpawn();
			_bosses.put(spawnDat, raidboss);
			
			return raidboss;
		}
		
		_bosses.put(spawnDat, null);
		return null;
	}
	
	private static class SingletonHolder
	{
		protected static final DayNightSpawnManager _instance = new DayNightSpawnManager();
	}
}
