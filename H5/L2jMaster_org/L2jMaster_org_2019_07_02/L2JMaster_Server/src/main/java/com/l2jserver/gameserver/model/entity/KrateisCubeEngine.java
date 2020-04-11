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
package com.l2jserver.gameserver.model.entity;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.Config;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.instancemanager.KrateisCubeManager;
import com.l2jserver.gameserver.model.L2Spawn;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.skills.AbnormalVisualEffect;
import com.l2jserver.gameserver.network.serverpackets.ExPVPMatchCCMyRecord;
import com.l2jserver.gameserver.network.serverpackets.ExPVPMatchCCRecord;
import com.l2jserver.gameserver.network.serverpackets.ExPVPMatchCCRetire;
import com.l2jserver.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jserver.util.Rnd;

/**
 * Krateis Cube Engine.
 * @author U3Games, Sacrifice
 */
public final class KrateisCubeEngine
{
	private static final Logger LOG = LoggerFactory.getLogger(KrateisCubeEngine.class.getName());
	
	private static final SkillHolder[] BUFF_SKILLS =
	{
		new SkillHolder(1086, 2), // Haste
		new SkillHolder(1204, 2), // Wind Walk
		new SkillHolder(1059, 3), // Empower
		new SkillHolder(1085, 3), // Acumen
		new SkillHolder(1078, 6), // Concentration
		new SkillHolder(1068, 3), // Might
		new SkillHolder(1240, 3), // Guidance
		new SkillHolder(1077, 3), // Focus
		new SkillHolder(1242, 3), // Death Whisper
		new SkillHolder(1062, 2), // Berserker Spirit
	};
	
	private static final int FANTASY_COIN_ID = 13067;
	
	private static final int TIME_DOORS_ROTATION = 25;
	private static final int TIME_WATCHER_ROTATION = 30;
	private static final int TIME_TO_WAITTING = 10;
	
	private static final int WATCHER_BLUE = 18602;
	private static final int WATCHER_RED = 18601;
	
	private static final Set<L2Npc> WATCHER_SET = ConcurrentHashMap.newKeySet();
	
	//@formatter:off
	private static final int[][] WATCHER_SPAWN_70 =
	{
		// 17_15
		{-77907, -77801, -8357}, {-79900, -77809, -8357}, {-81901, -77809, -8357},
		{-83899, -77808, -8357}, {-85901, -77809, -8357}, {-85902, -79802, -8357},
		{-83908, -79808, -8357}, {-81906, -79808, -8357}, {-79908, -79808, -8357},
		{-77909, -79808, -8357}, {-77902, -81803, -8357}, {-79898, -81809, -8357},
		{-81896, -81808, -8357}, {-83898, -81808, -8357}, {-85903, -81809, -8357},
		{-85900, -83806, -8357}, {-83906, -83806, -8357}, {-81910, -83804, -8357},
		{-79908, -83805, -8357}, {-77910, -83808, -8357}, {-77904, -85805, -8357},
		{-79901, -85807, -8357}, {-81901, -85808, -8357}, {-83899, -85809, -8357},
		{-85898, -85808, -8357},
	};
	//@formatter:on
	
	//@formatter:off
	private static final int[][] WATCHER_SPAWN_76 =
	{
		// 17_17
		{-77981, -12401, -8325}, {-79978, -12399, -8325}, {-81982, -12400, -8325},
		{-83981, -12399, -8325}, {-85981, -12401, -8325}, {-85981, -14401, -8325},
		{-83991, -14400, -8325}, {-81988, -14398, -8325}, {-79986, -14399, -8325},
		{-77985, -14401, -8325}, {-77983, -16401, -8325}, {-79978, -16398, -8325},
		{-81982, -16401, -8325}, {-83979, -16399, -8325}, {-85980, -16400, -8325},
		{-85982, -18394, -8325}, {-83988, -18400, -8325}, {-81985, -18399, -8325},
		{-79985, -18400, -8325}, {-77986, -18399, -8325}, {-77982, -20395, -8325},
		{-79983, -20400, -8325}, {-81983, -20397, -8325}, {-83978, -20396, -8325},
		{-85981, -20401, -8325},
	};
	//@formatter:on
	
	//@formatter:off
	private static final int[][] WATCHER_SPAWN_80 =
	{
		// 18_15
		{-52723, -86955, -8328}, {-50727, -86959, -8325}, {-48726, -86961, -8325},
		{-46729, -86960, -8325}, {-44724, -86958, -8325}, {-44721, -84962, -8325},
		{-46717, -84961, -8325}, {-48717, -84962, -8325}, {-50718, -84961, -8325},
		{-52719, -84960, -8327}, {-52720, -82966, -8325}, {-50725, -82958, -8325},
		{-48725, -82959, -8325}, {-46720, -82959, -8325}, {-44722, -82957, -8325},
		{-44720, -80963, -8325}, {-46718, -80959, -8325}, {-48719, -80962, -8325},
		{-50717, -80960, -8325}, {-52716, -80961, -8325}, {-52719, -78965, -8325},
		{-50725, -78959, -8325}, {-48724, -78960, -8325}, {-46722, -78958, -8325},
		{-44722, -78959, -8325}, 
	};
	//@formatter:on
	
	//@formatter:off
	private final int[] DOORS_70_A =
	{
		17150007, 17150009, 17150011, 17150013, 17150016, 17150017, 17150018, 17150019,
		17150020, 17150027, 17150029, 17150031, 17150033, 17150036, 17150037, 17150038,
		17150039, 17150040, 17150047, 17150049, 17150051, 17150053, 17150056, 17150057,
		17150058, 17150059, 17150060, 17150067, 17150069, 17150071, 17150073, 17150076,
		17150077, 17150078, 17150079, 17150080, 17150087, 17150089, 17150091, 17150093,
	};
	//@formatter:on
	
	//@formatter:off
	private final int[] DOORS_70_B =
	{
		17150008, 17150010, 17150012, 17150014, 17150021, 17150022, 17150023, 17150024,
		17150025, 17150028, 17150030, 17150032, 17150034, 17150041, 17150042, 17150043,
		17150044, 17150045, 17150048, 17150050, 17150052, 17150054, 17150061, 17150062,
		17150063, 17150064, 17150065, 17150068, 17150070, 17150072, 17150074, 17150081,
		17150082, 17150083, 17150084, 17150085, 17150088, 17150090, 17150092, 17150094,
	};
	//@formatter:on
	
	//@formatter:off
	private final int[] DOORS_76_A =
	{
		17170007, 17170009, 17170011, 17170013, 17170016, 17170017, 17170018, 17170019,
		17170020, 17170027, 17170029, 17170031, 17170033, 17170036, 17170037, 17170038,
		17170039, 17170040, 17170047, 17170049, 17170051, 17170053, 17170056, 17170057,
		17170058, 17170059, 17170060, 17170067, 17170069, 17170071, 17170073, 17170076,
		17170077, 17170078, 17170079, 17170080, 17170087, 17170089, 17170091, 17170093
	};
	//@formatter:on
	
	//@formatter:off
	private final int[] DOORS_76_B =
	{
		17170008, 17170010, 17170012, 17170014, 17170021, 17170022, 17170023, 17170024,
		17170025, 17170028, 17170030, 17170032, 17170034, 17170041, 17170042, 17170043,
		17170044, 17170045, 17170048, 17170050, 17170052, 17170054, 17170061, 17170062,
		17170063, 17170064, 17170065, 17170068, 17170070, 17170072, 17170074, 17170081,
		17170082, 17170083, 17170084, 17170085, 17170088, 17170090, 17170092, 17170094
	};
	//@formatter:on
	
	//@formatter:off
	private final int[] DOORS_80_A =
	{
		18150207, 18150209, 18150211, 18150213, 18150216, 18150217, 18150218, 18150219,
		18150220, 18150227, 18150229, 18150231, 18150233, 18150236, 18150237, 18150238,
		18150239, 18150240, 18150247, 18150249, 18150251, 18150253, 18150256, 18150257,
		18150258, 18150259, 18150260, 18150267, 18150269, 18150271, 18150273, 18150276,
		18150277, 18150278, 18150279, 18150280, 18150287, 18150289, 18150291, 18150293,
	};
	//@formatter:on
	
	//@formatter:off
	private final int[] DOORS_80_B =
	{
		18150208, 18150210, 18150212, 18150214, 18150221, 18150222, 18150223, 18150224,
		18150225, 18150228, 18150230, 18150232, 18150234, 18150241, 18150242, 18150243,
		18150244, 18150245, 18150248, 18150250, 18150252, 18150254, 18150261, 18150262,
		18150263, 18150264, 18150265, 18150268, 18150270, 18150272, 18150274, 18150281,
		18150282, 18150283, 18150284, 18150285, 18150288, 18150290, 18150292, 18150294,
	};
	//@formatter:on
	
	private int _eventStateOpcode = 0x00;
	
	private final int _instanceLevel70 = 250;
	private final int _instanceLevel76 = 251;
	private final int _instanceLevel80 = 252;
	
	private boolean _watchersRotation;
	private boolean _doorsRotation;
	
	private ScheduledFuture<?> _task = null;
	private ScheduledFuture<?> _taskCountDown = null;
	private ScheduledFuture<?> _taskRotation = null;
	private ScheduledFuture<?> _taskDoorsRotation = null;
	
	public KrateisCubeEngine()
	{
		// Empty
	}
	
	private enum EventState
	{
		DISABLED,
		PROGRESS,
		FINISHED
	}
	
	/**
	 * Add effects of instance.
	 * @param player
	 */
	private void addEffects(L2PcInstance player)
	{
		player.setCurrentHp(player.getMaxHp());
		player.setCurrentCp(player.getMaxCp());
		player.setCurrentMp(player.getMaxMp());
		
		if (player.getSummon() != null)
		{
			player.getSummon().setCurrentHp(player.getSummon().getMaxHp());
			player.getSummon().setCurrentCp(player.getSummon().getMaxCp());
			player.getSummon().setCurrentMp(player.getSummon().getMaxMp());
		}
		
		for (SkillHolder skillHolder : BUFF_SKILLS)
		{
			if (skillHolder != null)
			{
				skillHolder.getSkill().applyEffects(player, player);
				if (player.getSummon() != null)
				{
					skillHolder.getSkill().applyEffects(player.getSummon(), player.getSummon());
				}
			}
		}
		
		player.broadcastUserInfo();
		
		if (player.getSummon() != null)
		{
			player.getSummon().broadcastInfo();
		}
		setProtectionEffects(player);
	}
	
	private void countDownEndMessage()
	{
		if (_taskCountDown != null)
		{
			_taskCountDown.cancel(true);
			_taskCountDown = null;
		}
		
		try
		{
			for (L2PcInstance player : KrateisCubeManager.getParticipants())
			{
				if (player != null)
				{
					ThreadPoolManager.getInstance().scheduleEvent(new countDownTask(player, 10, 3), 1000);
				}
			}
		}
		catch (Exception e)
		{
			LOG.info("Error in count down end message: " + e);
		}
	}
	
	public void countDownReturnMessage(L2PcInstance player)
	{
		ThreadPoolManager.getInstance().scheduleEvent(new countDownTask(player, Config.KRATEIS_CUBE_TIME_TO_REVIVE, 1), 1000);
	}
	
	private void countDownStartMessage()
	{
		try
		{
			for (L2PcInstance player : KrateisCubeManager.getParticipants())
			{
				if (player != null)
				{
					ThreadPoolManager.getInstance().scheduleEvent(new countDownTask(player, TIME_TO_WAITTING, 0), 1000);
				}
			}
		}
		catch (Exception e)
		{
			LOG.info("Error in count down start message: " + e);
		}
	}
	
	private void checkRewardPlayers(L2PcInstance player)
	{
		if (player != null)
		{
			final int amount = KrateisCubeManager.getPoints(player);
			if (amount > 0)
			{
				player.addItem("Krateis Cube", FANTASY_COIN_ID, amount, player, true);
				player.getInventory().updateDatabase();
			}
		}
	}
	
	// TODO:
	public void createInstances()
	{
		if (_task != null)
		{
			_task.cancel(false);
		}
		
		if (KrateisCubeManager.getInstance().checkMinPlayersArena(1, true))
		{
			InstanceManager.getInstance().createInstanceFromTemplate(_instanceLevel70, "KrateisCube70.xml");
			InstanceManager.getInstance().getInstance(_instanceLevel70).setAllowSummon(false);
			InstanceManager.getInstance().getInstance(_instanceLevel70).setPvPInstance(true);
			InstanceManager.getInstance().getInstance(_instanceLevel70).setEmptyDestroyTime((Config.KRATEIS_CUBE_RUNNING_TIME * 1000) + 60000L);
			
		}
		
		if (KrateisCubeManager.getInstance().checkMinPlayersArena(2, true))
		{
			InstanceManager.getInstance().createInstanceFromTemplate(_instanceLevel76, "KrateisCube76.xml");
			InstanceManager.getInstance().getInstance(_instanceLevel76).setAllowSummon(false);
			InstanceManager.getInstance().getInstance(_instanceLevel76).setPvPInstance(true);
			InstanceManager.getInstance().getInstance(_instanceLevel76).setEmptyDestroyTime((Config.KRATEIS_CUBE_RUNNING_TIME * 1000) + 60000L);
		}
		
		if (KrateisCubeManager.getInstance().checkMinPlayersArena(3, true))
		{
			InstanceManager.getInstance().createInstanceFromTemplate(_instanceLevel80, "KrateisCube80.xml");
			InstanceManager.getInstance().getInstance(_instanceLevel80).setAllowSummon(false);
			InstanceManager.getInstance().getInstance(_instanceLevel80).setPvPInstance(true);
			InstanceManager.getInstance().getInstance(_instanceLevel80).setEmptyDestroyTime((Config.KRATEIS_CUBE_RUNNING_TIME * 1000) + 60000L);
		}
		countDownStartMessage();
		_task = ThreadPoolManager.getInstance().scheduleEvent(() -> startKrateisCube(), TIME_TO_WAITTING * 1000);
	}
	
	/**
	 * Check if event is active.
	 * @return
	 */
	public boolean isActive()
	{
		if (_eventStateOpcode == 0x01)
		{
			return true;
		}
		return false;
	}
	
	private void despawnWatchers()
	{
		for (L2Npc npc : WATCHER_SET)
		{
			if (npc != null)
			{
				npc.getSpawn().stopRespawn();
				npc.deleteMe();
			}
		}
		WATCHER_SET.clear();
		spawnWatchers();
	}
	
	private void endKrateisCube()
	{
		if (_task != null)
		{
			_task.cancel(true);
			_task = null;
		}
		
		if (_taskRotation != null)
		{
			_taskRotation.cancel(true);
			_taskRotation = null;
		}
		
		if (_taskDoorsRotation != null)
		{
			_taskDoorsRotation.cancel(true);
			_taskDoorsRotation = null;
		}
		
		setStateEvent(EventState.FINISHED);
		spawnDoors(false);
		despawnWatchers();
		
		final List<L2PcInstance> participants = KrateisCubeManager.getParticipants();
		if (participants != null)
		{
			for (L2PcInstance player : participants)
			{
				if (player != null)
				{
					removeAllEffects(player);
					KrateisCubeManager.getInstance().teleportOutEvent(player);
					checkRewardPlayers(player);
					player.sendPacket(new ExPVPMatchCCRetire());
					player.setCanRevive(true);
				}
			}
			KrateisCubeManager.getInstance().deleteInsideParticipants();
		}
		
		// TODO:
		destroyInstanceId(_instanceLevel70);
		destroyInstanceId(_instanceLevel76);
		destroyInstanceId(_instanceLevel80);
		
		KrateisCubeManager.getInstance().startEvent();
		setStateEvent(EventState.DISABLED);
		
		if (Config.KRATEIS_CUBE_EVENT_LOG_ENABLED)
		{
			LOG.info("Krateis Cube: Match ended!");
		}
	}
	
	private void destroyInstanceId(int instanceId)
	{
		if (InstanceManager.getInstance().getInstance(instanceId) != null)
		{
			InstanceManager.getInstance().destroyInstance(instanceId);
		}
		
		return;
	}
	
	private int getInstanceId(L2PcInstance player, int arena)
	{
		int instanceId = 0;
		switch (arena)
		{
			case 1:
			{
				instanceId = _instanceLevel70;
				break;
			}
			case 2:
			{
				instanceId = _instanceLevel76;
				break;
			}
			case 3:
			{
				instanceId = _instanceLevel80;
				break;
			}
		}
		return instanceId;
	}
	
	private void protectionDisabled(L2PcInstance player)
	{
		player.setIsInvul(false);
		player.setIsParalyzed(false);
		player.stopAbnormalVisualEffect(true, AbnormalVisualEffect.INVINCIBILITY);
	}
	
	/**
	 * Remove all effects.
	 * @param player
	 */
	private void removeAllEffects(L2PcInstance player)
	{
		player.getEffectList().stopAllEffects();
		player.stopAllEffects();
		
		if (player.getSummon() != null)
		{
			player.getSummon().getEffectList().stopAllEffects();
			player.getSummon().stopAllEffects();
		}
		
		player.broadcastUserInfo();
		
		if (player.getSummon() != null)
		{
			player.getSummon().broadcastInfo();
		}
	}
	
	/**
	 * Add effects of instance.
	 * @param player
	 */
	private void respawnEffects(L2PcInstance player)
	{
		removeAllEffects(player);
		
		if (player.isDead())
		{
			((L2Character) player).doRevive(100.0);
		}
		player.broadcastStatusUpdate();
		player.broadcastUserInfo();
	}
	
	private void setProtectionEffects(L2PcInstance player)
	{
		player.setIsInvul(true);
		player.setIsParalyzed(true);
		player.startAbnormalVisualEffect(true, AbnormalVisualEffect.INVINCIBILITY);
		ThreadPoolManager.getInstance().scheduleEvent(() -> protectionDisabled(player), Config.KRATEIS_CUBE_TIME_OF_PROTECTION * 1000);
	}
	
	/**
	 * Set state of event.
	 * @param state
	 * @return
	 */
	private int setStateEvent(EventState state)
	{
		switch (state)
		{
			case DISABLED:
			{
				_eventStateOpcode = 0x00;
				break;
			}
			case PROGRESS:
			{
				_eventStateOpcode = 0x01;
				break;
			}
			case FINISHED:
			{
				_eventStateOpcode = 0x02;
				break;
			}
		}
		return _eventStateOpcode;
	}
	
	private L2Spawn spawnNpc(int npcId, int x, int y, int z, int heading, int respawnTime, int instanceId)
	{
		L2Spawn npcSpawn = null;
		try
		{
			npcSpawn = new L2Spawn(npcId);
			npcSpawn.setXYZ(x, y, z);
			npcSpawn.setHeading(heading);
			npcSpawn.setRespawnDelay(respawnTime);
			npcSpawn.setInstanceId(instanceId);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return npcSpawn;
	}
	
	private void spawnDoors(boolean active)
	{
		if (_taskDoorsRotation != null)
		{
			_taskDoorsRotation.cancel(false);
		}
		
		if (_doorsRotation)
		{
			_doorsRotation = false;
		}
		else
		{
			_doorsRotation = true;
		}
		
		if (InstanceManager.getInstance().getInstance(_instanceLevel70) != null)
		{
			final int[] doorsA = (_doorsRotation) ? DOORS_70_A : DOORS_70_B;
			final int[] doorsB = (_doorsRotation) ? DOORS_70_B : DOORS_70_A;
			if (active)
			{
				openDoors(doorsA, _instanceLevel70);
				closeDoors(doorsB, _instanceLevel70);
			}
			else
			{
				closeDoors(doorsA, _instanceLevel70);
				closeDoors(doorsB, _instanceLevel70);
			}
		}
		
		if (InstanceManager.getInstance().getInstance(_instanceLevel76) != null)
		{
			final int[] doorsA = (_doorsRotation) ? DOORS_76_A : DOORS_76_B;
			final int[] doorsB = (_doorsRotation) ? DOORS_76_B : DOORS_76_A;
			if (active)
			{
				openDoors(doorsA, _instanceLevel76);
				closeDoors(doorsB, _instanceLevel76);
			}
			else
			{
				closeDoors(doorsA, _instanceLevel76);
				closeDoors(doorsB, _instanceLevel76);
			}
		}
		
		if (InstanceManager.getInstance().getInstance(_instanceLevel80) != null)
		{
			final int[] doorsA = (_doorsRotation) ? DOORS_80_A : DOORS_80_B;
			final int[] doorsB = (_doorsRotation) ? DOORS_80_B : DOORS_80_A;
			if (active)
			{
				openDoors(doorsA, _instanceLevel80);
				closeDoors(doorsB, _instanceLevel80);
			}
			else
			{
				closeDoors(doorsA, _instanceLevel80);
				closeDoors(doorsB, _instanceLevel80);
			}
		}
		
		_taskDoorsRotation = ThreadPoolManager.getInstance().scheduleEvent(() -> spawnDoors(active), TIME_DOORS_ROTATION * 1000);
	}
	
	/**
	 * Get doors of instance.
	 * @param doorId
	 * @param instanceId
	 * @return
	 */
	private static L2DoorInstance getDoor(int doorId, int instanceId)
	{
		L2DoorInstance door = null;
		if (instanceId > 0)
		{
			final Instance inst = InstanceManager.getInstance().getInstance(instanceId);
			if (inst != null)
			{
				door = inst.getDoor(doorId);
			}
		}
		return door;
	}
	
	/**
	 * Close doors specified in list.
	 * @param doorsA
	 */
	private static void closeDoors(int[] doors, int instanceId)
	{
		for (int doorId : doors)
		{
			final L2DoorInstance doorInstance = getDoor(doorId, instanceId);
			if (doorInstance != null)
			{
				doorInstance.closeMe();
			}
		}
	}
	
	/**
	 * Open doors specified in list.
	 * @param doorsA
	 */
	private static void openDoors(int[] doors, int instanceId)
	{
		for (int doorId : doors)
		{
			final L2DoorInstance doorInstance = getDoor(doorId, instanceId);
			if (doorInstance != null)
			{
				doorInstance.openMe();
			}
		}
	}
	
	private void spawnWatchers()
	{
		if (_taskRotation != null)
		{
			_taskRotation.cancel(false);
		}
		
		final int watcherA;
		final int watcherB;
		L2Spawn watcherSpawn;
		
		if (_watchersRotation)
		{
			watcherA = WATCHER_BLUE;
			watcherB = WATCHER_RED;
			_watchersRotation = false;
		}
		else
		{
			watcherA = WATCHER_RED;
			watcherB = WATCHER_BLUE;
			_watchersRotation = true;
		}
		
		if (InstanceManager.getInstance().getInstance(_instanceLevel70) != null)
		{
			for (int i = 0; i < 11; i++)
			{
				watcherSpawn = spawnNpc(watcherA, WATCHER_SPAWN_70[i][0], WATCHER_SPAWN_70[i][1], WATCHER_SPAWN_70[i][2], 0, 0, _instanceLevel70);
				WATCHER_SET.add(watcherSpawn.doSpawn());
			}
			
			for (int i = 11; i < 25; i++)
			{
				watcherSpawn = spawnNpc(watcherB, WATCHER_SPAWN_70[i][0], WATCHER_SPAWN_70[i][1], WATCHER_SPAWN_70[i][2], 0, 0, _instanceLevel70);
				WATCHER_SET.add(watcherSpawn.doSpawn());
			}
		}
		
		if (InstanceManager.getInstance().getInstance(_instanceLevel76) != null)
		{
			for (int i = 0; i < 11; i++)
			{
				watcherSpawn = spawnNpc(watcherA, WATCHER_SPAWN_76[i][0], WATCHER_SPAWN_76[i][1], WATCHER_SPAWN_76[i][2], 0, 0, _instanceLevel76);
				WATCHER_SET.add(watcherSpawn.doSpawn());
			}
			
			for (int i = 11; i < 25; i++)
			{
				watcherSpawn = spawnNpc(watcherB, WATCHER_SPAWN_76[i][0], WATCHER_SPAWN_76[i][1], WATCHER_SPAWN_76[i][2], 0, 0, _instanceLevel76);
				WATCHER_SET.add(watcherSpawn.doSpawn());
			}
		}
		
		if (InstanceManager.getInstance().getInstance(_instanceLevel80) != null)
		{
			for (int i = 0; i < 11; i++)
			{
				watcherSpawn = spawnNpc(watcherA, WATCHER_SPAWN_80[i][0], WATCHER_SPAWN_80[i][1], WATCHER_SPAWN_80[i][2], 0, 0, _instanceLevel80);
				WATCHER_SET.add(watcherSpawn.doSpawn());
			}
			
			for (int i = 11; i < 25; i++)
			{
				watcherSpawn = spawnNpc(watcherB, WATCHER_SPAWN_80[i][0], WATCHER_SPAWN_80[i][1], WATCHER_SPAWN_80[i][2], 0, 0, _instanceLevel80);
				WATCHER_SET.add(watcherSpawn.doSpawn());
			}
		}
		_taskRotation = ThreadPoolManager.getInstance().scheduleEvent(() -> despawnWatchers(), TIME_WATCHER_ROTATION * 1000);
	}
	
	private void startKrateisCube()
	{
		if (_task != null)
		{
			_task.cancel(false);
		}
		
		if (_taskCountDown != null)
		{
			_taskCountDown.cancel(false);
		}
		
		_watchersRotation = true;
		_doorsRotation = true;
		spawnDoors(true);
		spawnWatchers();
		teleportAllPlayersToInstance();
		_task = ThreadPoolManager.getInstance().scheduleEvent(() -> endKrateisCube(), Config.KRATEIS_CUBE_RUNNING_TIME * 60 * 1000);
		_taskCountDown = ThreadPoolManager.getInstance().scheduleEvent(() -> countDownEndMessage(), ((Config.KRATEIS_CUBE_RUNNING_TIME - 10) * 60 * 1000));
		setStateEvent(EventState.PROGRESS);
		
		if (Config.KRATEIS_CUBE_EVENT_LOG_ENABLED)
		{
			LOG.info("Krateis Cube: Match initiated!");
		}
	}
	
	private void teleportAllPlayersToInstance()
	{
		int i = 0;
		for (L2PcInstance player : KrateisCubeManager.getParticipants())
		{
			if (player != null)
			{
				final int instanceId = getInstanceId(player, KrateisCubeManager.getInstance().getArena(player));
				if (player.getInstanceId() != instanceId)
				{
					player.setInstanceId(instanceId);
				}
				
				if (instanceId == _instanceLevel70)
				{
					player.teleToLocation(WATCHER_SPAWN_70[i][0], WATCHER_SPAWN_70[i][1], WATCHER_SPAWN_70[i][2], 0, instanceId, 0);
				}
				else if (instanceId == _instanceLevel76)
				{
					player.teleToLocation(WATCHER_SPAWN_76[i][0], WATCHER_SPAWN_76[i][1], WATCHER_SPAWN_76[i][2], 0, instanceId, 0);
				}
				else
				{
					player.teleToLocation(WATCHER_SPAWN_80[i][0], WATCHER_SPAWN_80[i][1], WATCHER_SPAWN_80[i][2], 0, instanceId, 0);
				}
				
				addEffects(player);
				player.sendPacket(new ExShowScreenMessage("The match has started!", 3000));
				player.sendPacket(new ExPVPMatchCCMyRecord(KrateisCubeManager.getPoints(player)));
				player.sendPacket(new ExPVPMatchCCRecord(setStateEvent(EventState.PROGRESS)));
				player.setCanRevive(false);
				i++;
			}
		}
	}
	
	public void teleportToInstance(L2PcInstance player, int arena)
	{
		if (KrateisCubeManager.getParticipants().contains(player))
		{
			final int instanceId = getInstanceId(player, arena);
			if (player.getInstanceId() != instanceId)
			{
				player.setInstanceId(instanceId);
			}
			
			if (instanceId == _instanceLevel70)
			{
				player.teleToLocation(WATCHER_SPAWN_70[Rnd.get(WATCHER_SPAWN_70.length)][0], WATCHER_SPAWN_70[Rnd.get(WATCHER_SPAWN_70.length)][1], WATCHER_SPAWN_70[Rnd.get(WATCHER_SPAWN_70.length)][2], 0, instanceId, 0);
			}
			else if (instanceId == _instanceLevel76)
			{
				player.teleToLocation(WATCHER_SPAWN_76[Rnd.get(WATCHER_SPAWN_76.length)][0], WATCHER_SPAWN_76[Rnd.get(WATCHER_SPAWN_76.length)][1], WATCHER_SPAWN_76[Rnd.get(WATCHER_SPAWN_76.length)][2], 0, instanceId, 0);
			}
			else
			{
				player.teleToLocation(WATCHER_SPAWN_80[Rnd.get(WATCHER_SPAWN_80.length)][0], WATCHER_SPAWN_80[Rnd.get(WATCHER_SPAWN_80.length)][1], WATCHER_SPAWN_80[Rnd.get(WATCHER_SPAWN_80.length)][2], 0, instanceId, 0);
			}
			
			addEffects(player);
		}
	}
	
	// TODO:
	public void teleportToWaitingRoom(L2PcInstance player, int arena)
	{
		if (KrateisCubeManager.getParticipants().contains(player))
		{
			final int instanceId = getInstanceId(player, arena);
			if (player.getInstanceId() != instanceId)
			{
				player.setInstanceId(instanceId);
			}
			
			respawnEffects(player);
			
			if (instanceId == _instanceLevel70)
			{
				player.teleToLocation(-87073, -81994, -8336, 0, instanceId);
			}
			else if (instanceId == _instanceLevel76)
			{
				player.teleToLocation(-87148, -16396, -8336, 0, instanceId);
			}
			else
			{
				player.teleToLocation(-53912, -82732, -8340, 0, instanceId);
			}
		}
	}
	
	private class countDownTask implements Runnable
	{
		private final L2PcInstance _player;
		private final int _time;
		private final int _type;
		
		countDownTask(L2PcInstance player, int time, int isRespawn)
		{
			_player = player;
			_time = time;
			_type = isRespawn;
		}
		
		@Override
		public void run()
		{
			if ((_player != null) && (_time > 0))
			{
				switch (_type)
				{
					case 0:
						_player.sendPacket(new ExShowScreenMessage(_time + " second(s) before the match begins!", 850));
						ThreadPoolManager.getInstance().scheduleEvent(new countDownTask(_player, _time - 1, _type), 1000);
						break;
					case 1:
						_player.sendMessage("Resurrection will take place in the waiting room after " + _time + " second(s)!");
						ThreadPoolManager.getInstance().scheduleEvent(new countDownTask(_player, _time - 1, _type), 1000);
						break;
					case 2:
						_player.sendPacket(new ExShowScreenMessage("-> " + _time + " <-", 850));
						ThreadPoolManager.getInstance().scheduleEvent(new countDownTask(_player, _time - 1, _type), 1000);
						break;
				}
			}
		}
	}
	
	/**
	 * Gets the single instance
	 * @return single instance
	 */
	public static KrateisCubeEngine getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final KrateisCubeEngine _instance = new KrateisCubeEngine();
	}
}