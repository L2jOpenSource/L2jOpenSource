/*
 * Copyright (C) 2004-2019 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.individual;

import com.l2jserver.gameserver.instancemanager.GrandBossManager;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.zone.type.L2BossZone;
import com.l2jserver.gameserver.network.NpcStringId;
import com.l2jserver.gameserver.network.serverpackets.CameraMode;
import com.l2jserver.gameserver.network.serverpackets.SocialAction;

import ai.npc.AbstractNpcAI;

/**
 * VanHalter AI
 * @author ShinichiYao
 */
public final class VanHalter extends AbstractNpcAI
{
	private static final int TRIOL_HIGH_PRIEST = 22171;
	private static final int ANDREAS_CAPTAIN = 22188;
	private static final int MOVIE_NPC = 13014;
	private static final int RITUAL_OFFERING = 32038;
	private static final int VAN_HALTER = 29062;
	private static final Location[] MOVIE_NPC_LOC =
	{
		new Location(-16397, -55200, -10449),
		new Location(-16397, -55200, -10051),
		new Location(-16397, -55200, -9741),
		new Location(-16397, -55200, -9394),
		new Location(-16397, -55197, -8739)
	};
	private static final Location RITUAL_OFFERING_LOC = new Location(-16384, -53197, -10439, 15992);
	private static final Location VAN_HALTER_LOC = new Location(-16397, -53308, -10448, 16384);
	private static final Location ANDREAS_CAPTAIN_LOC = new Location(-16392, -52124, -10592, 0);
	
	private static final SkillHolder CURSE_POISON = new SkillHolder(1168, 7);
	
	private static final int RESPAWN = 129600000; // 36 h
	private static final int RANDOM_RESPAWN = 86400000; // 24 h
	
	private static final L2BossZone ZONE = GrandBossManager.getInstance().getZone(-16373, -53562, -10300);
	
	protected static final int[] DOOR_OF_ALTAR =
	{
		19160014,
		19160015
	};
	protected static final int[] DOOR_OF_SACRIFICE =
	{
		19160016,
		19160017
	};
	private static int DOOR_STATE = 0;
	
	private static boolean IS_SPAWNED = false;
	
	private L2Npc _ritualOffering;
	private L2Npc _VanHalter;
	private L2Npc _AndreasCaptain;
	
	private VanHalter()
	{
		super(VanHalter.class.getSimpleName(), "ai/individual");
		addKillId(TRIOL_HIGH_PRIEST, ANDREAS_CAPTAIN, VAN_HALTER);
		spawnVanHalter();
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case "check":
				if (IS_SPAWNED && (DOOR_STATE != 2))
				{
					return "32068-01.html";
				}
				else if (IS_SPAWNED && (DOOR_STATE == 2))
				{
					return "32068-03.html";
				}
				else
				{
					return "32068-02.html";
				}
			case "respawn":
				spawnVanHalter();
				return null;
			case "despawn":
				if (!npc.isDead() && !npc.isInCombat())
				{
					npc.deleteMe();
					startRespawn();
				}
				return null;
			case "opendoor":
				if (DOOR_STATE == 0)
				{
					setDoors(1);
					startQuestTimer("closedoor", 120000, null, null);
				}
				break;
			case "closedoor":
				if (DOOR_STATE == 1)
				{
					setDoors(0);
				}
				break;
			case "ANIMATION":
				_ritualOffering = addSpawn(RITUAL_OFFERING, RITUAL_OFFERING_LOC);
				startQuestTimer("MOVIE_1", 2000, null, null);
				break;
			case "MOVIE_1":
				for (L2PcInstance players : ZONE.getPlayersInside())
				{
					if (players.isInsideRadius(VAN_HALTER_LOC, 2550, true, false))
					{
						stopPc(players);
						specialCamera(players, _VanHalter, 50, 0, 0, 0, 15000, 90, 0, 0, 0, 0);
					}
				}
				final L2Npc movieNpc4 = addSpawn(MOVIE_NPC, MOVIE_NPC_LOC[4], false, 60000);
				startQuestTimer("MOVIE_2", 16, movieNpc4, null);
				break;
			case "MOVIE_2":
				for (L2PcInstance players : ZONE.getPlayersInside())
				{
					if (players.isInsideRadius(VAN_HALTER_LOC, 2550, true, false))
					{
						specialCamera(players, npc, 1842, 0, 0, 0, 15000, 100, -3, 0, 0, 0);
					}
				}
				startQuestTimer("MOVIE_3", 1, npc, null);
				break;
			case "MOVIE_3":
				for (L2PcInstance players : ZONE.getPlayersInside())
				{
					if (players.isInsideRadius(VAN_HALTER_LOC, 2550, true, false))
					{
						specialCamera(players, npc, 1861, 0, 0, 1500, 15000, 97, -10, 0, 0, 0);
					}
				}
				final L2Npc movieNpc3 = addSpawn(MOVIE_NPC, MOVIE_NPC_LOC[3], false, 60000);
				startQuestTimer("MOVIE_4", 1500, movieNpc3, null);
				break;
			case "MOVIE_4":
				for (L2PcInstance players : ZONE.getPlayersInside())
				{
					if (players.isInsideRadius(VAN_HALTER_LOC, 2550, true, false))
					{
						specialCamera(players, npc, 1876, 0, 0, 0, 15000, 97, 12, 0, 0, 0);
					}
				}
				startQuestTimer("MOVIE_5", 1, npc, null);
				break;
			case "MOVIE_5":
				for (L2PcInstance players : ZONE.getPlayersInside())
				{
					if (players.isInsideRadius(VAN_HALTER_LOC, 2550, true, false))
					{
						specialCamera(players, npc, 1839, 0, 0, 1500, 15000, 94, 0, 0, 0, 0);
					}
				}
				final L2Npc movieNpc2 = addSpawn(MOVIE_NPC, MOVIE_NPC_LOC[2], false, 60000);
				startQuestTimer("MOVIE_6", 1500, movieNpc2, null);
				break;
			case "MOVIE_6":
				for (L2PcInstance players : ZONE.getPlayersInside())
				{
					if (players.isInsideRadius(VAN_HALTER_LOC, 2550, true, false))
					{
						specialCamera(players, npc, 1872, 0, 0, 0, 15000, 94, 15, 0, 0, 0);
					}
				}
				startQuestTimer("MOVIE_7", 1, npc, null);
				break;
			case "MOVIE_7":
				for (L2PcInstance players : ZONE.getPlayersInside())
				{
					if (players.isInsideRadius(VAN_HALTER_LOC, 2550, true, false))
					{
						specialCamera(players, npc, 1839, 0, 0, 1500, 15000, 92, 0, 0, 0, 0);
					}
				}
				final L2Npc movieNpc1 = addSpawn(MOVIE_NPC, MOVIE_NPC_LOC[1], false, 60000);
				startQuestTimer("MOVIE_8", 1500, movieNpc1, null);
				break;
			case "MOVIE_8":
				for (L2PcInstance players : ZONE.getPlayersInside())
				{
					if (players.isInsideRadius(VAN_HALTER_LOC, 2550, true, false))
					{
						specialCamera(players, npc, 1872, 0, 0, 0, 15000, 92, 15, 0, 0, 0);
					}
				}
				startQuestTimer("MOVIE_9", 1, npc, null);
				break;
			case "MOVIE_9":
				for (L2PcInstance players : ZONE.getPlayersInside())
				{
					if (players.isInsideRadius(VAN_HALTER_LOC, 2550, true, false))
					{
						specialCamera(players, npc, 1839, 0, 0, 1500, 15000, 90, 5, 0, 0, 0);
					}
				}
				final L2Npc movieNpc0 = addSpawn(MOVIE_NPC, MOVIE_NPC_LOC[0], false, 60000);
				startQuestTimer("MOVIE_10", 1500, movieNpc0, null);
				break;
			case "MOVIE_10":
				for (L2PcInstance players : ZONE.getPlayersInside())
				{
					if (players.isInsideRadius(VAN_HALTER_LOC, 2550, true, false))
					{
						specialCamera(players, npc, 1872, 0, 0, 0, 15000, 90, 5, 0, 0, 0);
					}
				}
				startQuestTimer("MOVIE_11", 1, npc, null);
				break;
			case "MOVIE_11":
				for (L2PcInstance players : ZONE.getPlayersInside())
				{
					if (players.isInsideRadius(VAN_HALTER_LOC, 2550, true, false))
					{
						specialCamera(players, npc, 2002, 0, 0, 1500, 15000, 90, 2, 0, 0, 0);
					}
				}
				startQuestTimer("MOVIE_12", 2000, null, null);
				break;
			case "MOVIE_12":
				for (L2PcInstance players : ZONE.getPlayersInside())
				{
					if (players.isInsideRadius(VAN_HALTER_LOC, 2550, true, false))
					{
						specialCamera(players, _VanHalter, 50, 0, 0, 0, 15000, 90, 10, 0, 0, 0);
					}
				}
				startQuestTimer("MOVIE_13", 1000, null, null);
				break;
			case "MOVIE_13":
				_VanHalter.setTarget(_ritualOffering);
				_VanHalter.doCast(CURSE_POISON);
				startQuestTimer("MOVIE_14", 4700, null, null);
				break;
			case "MOVIE_14":
				_ritualOffering.sendPacket(new SocialAction(_ritualOffering.getObjectId(), 1));
				startQuestTimer("MOVIE_15", 4300, null, null);
				break;
			case "MOVIE_15":
				_ritualOffering.deleteMe();
				_ritualOffering = null;
				for (L2PcInstance players : ZONE.getPlayersInside())
				{
					if (players.isInsideRadius(VAN_HALTER_LOC, 2550, true, false))
					{
						specialCamera(players, _VanHalter, 100, 0, 0, 1500, 15000, 90, 15, 0, 0, 0);
					}
				}
				startQuestTimer("MOVIE_16", 2000, null, null);
				break;
			case "MOVIE_16":
				for (L2PcInstance players : ZONE.getPlayersInside())
				{
					if (players.isInsideRadius(VAN_HALTER_LOC, 2550, true, false))
					{
						specialCamera(players, _VanHalter, 5200, 0, 0, 9500, 6000, 90, -10, 0, 0, 0);
					}
				}
				startQuestTimer("MOVIE_END", 6000, null, null);
				break;
			case "MOVIE_END":
				for (L2PcInstance players : ZONE.getPlayersInside())
				{
					startPc(players);
				}
				break;
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		switch (npc.getId())
		{
			case TRIOL_HIGH_PRIEST:
				if (IS_SPAWNED && (DOOR_STATE == 0))
				{
					startQuestTimer("opendoor", 60000, null, null);
				}
				break;
			case ANDREAS_CAPTAIN:
				if (IS_SPAWNED && (DOOR_STATE != 2))
				{
					setDoors(2);
					for (L2PcInstance players : ZONE.getPlayersInside())
					{
						showOnScreenMsg(players, NpcStringId.THE_DOOR_TO_THE_3RD_FLOOR_OF_THE_ALTAR_IS_NOW_OPEN, 2, 5000);
					}
					startQuestTimer("ANIMATION", 5000, null, null);
				}
				break;
			case VAN_HALTER:
				startRespawn();
				break;
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	private void spawnVanHalter()
	{
		final String respawn = loadGlobalQuestVar("Respawn");
		final long remain = (!respawn.isEmpty()) ? Long.parseLong(respawn) - System.currentTimeMillis() : 0;
		if (remain > 0)
		{
			startQuestTimer("respawn", remain, null, null);
			return;
		}
		_VanHalter = addSpawn(VAN_HALTER, VAN_HALTER_LOC, false, 0);
		_AndreasCaptain = addSpawn(ANDREAS_CAPTAIN, ANDREAS_CAPTAIN_LOC, false, 0);
		setDoors(0);
		IS_SPAWNED = true;
		startQuestTimer("despawn", 10800000, _VanHalter, null);
		startQuestTimer("despawn", 10800000, _AndreasCaptain, null);
	}
	
	private void startRespawn()
	{
		IS_SPAWNED = false;
		setDoors(0);
		final int respawnTime = RESPAWN - getRandom(RANDOM_RESPAWN);
		saveGlobalQuestVar("Respawn", Long.toString(System.currentTimeMillis() + respawnTime));
		startQuestTimer("respawn", respawnTime, null, null);
	}
	
	private void stopPc(L2PcInstance player)
	{
		if (player != null)
		{
			player.setTarget(null);
			player.stopMove(null);
			player.setIsInvul(true);
			player.setIsImmobilized(true);
			player.teleToLocation(player);
			player.sendPacket(new CameraMode(1));
		}
	}
	
	private void startPc(L2PcInstance player)
	{
		if (player != null)
		{
			player.setIsInvul(false);
			player.setIsImmobilized(false);
			player.sendPacket(new CameraMode(0));
		}
	}
	
	private void setDoors(int val)
	{
		switch (val)
		{
			case 0:
				for (int doorId : DOOR_OF_ALTAR)
				{
					closeDoor(doorId, 0);
				}
				for (int doorId : DOOR_OF_SACRIFICE)
				{
					closeDoor(doorId, 0);
				}
				DOOR_STATE = 0;
				break;
			case 1:
				for (int doorId : DOOR_OF_ALTAR)
				{
					openDoor(doorId, 0);
				}
				for (int doorId : DOOR_OF_SACRIFICE)
				{
					closeDoor(doorId, 0);
				}
				DOOR_STATE = 1;
				break;
			case 2:
				for (int doorId : DOOR_OF_ALTAR)
				{
					closeDoor(doorId, 0);
				}
				for (int doorId : DOOR_OF_SACRIFICE)
				{
					openDoor(doorId, 0);
				}
				DOOR_STATE = 2;
				break;
		}
	}
	
	public static void main(String[] args)
	{
		new VanHalter();
	}
}