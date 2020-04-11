/*
 * Copyright (C) 2004-2013 L2J DataPack
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
package gracia.instances.HeartInfinityDefence;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Instance;
import l2r.gameserver.model.instancezone.InstanceWorld;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.zone.L2ZoneType;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.NpcSay;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

import quests.Q00697_DefendtheHallofErosion.Q00697_DefendtheHallofErosion;

public class HeartInfinityDefence extends Quest
{
	private class HIDWorld extends InstanceWorld
	{
		public List<L2Npc> npcList = new ArrayList<>();
		public List<L2Npc> deadTumors = new ArrayList<>();
		protected L2Npc deadTumor;
		public long startTime = 0;
		protected ScheduledFuture<?> finishTask = null, timerTask = null, wagonSpawnTask = null;
		
		public HIDWorld()
		{
		}
	}
	
	private static final String qn = "HeartInfinityDefence";
	private static final int INSTANCEID = 122;
	
	private static final int ABYSSGAZE = 32539;
	private static final int ALIVETUMOR = 18708;
	private static final int DEADTUMOR = 32535;
	private static final int SOULWAGON = 22523;
	private static final int REGENERATION_COFFIN = 18709;
	private static final int maxCoffins = 20;
	
	L2Npc preawakenedEchmus = null;
	protected int coffinsCreated = 0;
	protected long tumorRespawnTime = 0;
	protected long wagonRespawnTime = 0;
	protected boolean conquestBegun = false;
	protected boolean conquestEnded = false;
	
	private static final int[] ENTER_TELEPORT =
	{
		-179284,
		205990,
		-15520
	};
	
	private static int[] NOTMOVE =
	{
		18668,
		18711,
		ALIVETUMOR,
		DEADTUMOR
	};
	
	private static final int[] mobs =
	{
		22516,
		22520,
		22522,
		22524
	};
	
	protected static final int[][] ROOMS_MOBS =
	{
		{
			22520,
			-179949,
			206751,
			-15521,
			0,
			60,
			1
		},
		{
			22522,
			-179949,
			206505,
			-15522,
			0,
			60,
			1
		},
		{
			22532,
			-180207,
			206362,
			-15512,
			0,
			60,
			1
		},
		{
			22520,
			-180046,
			206109,
			-15511,
			0,
			60,
			1
		},
		{
			22526,
			-179843,
			205686,
			-15511,
			0,
			60,
			1
		},
		{
			22522,
			-179658,
			206002,
			-15518,
			0,
			60,
			1
		},
		{
			22526,
			-179465,
			205648,
			-15498,
			0,
			60,
			1
		},
		{
			22520,
			-179262,
			205896,
			-15536,
			0,
			60,
			1
		},
		{
			22526,
			-178907,
			205950,
			-15509,
			0,
			60,
			1
		},
		{
			22532,
			-179128,
			206423,
			-15528,
			0,
			60,
			1
		},
		{
			22522,
			-179262,
			206890,
			-15524,
			0,
			60,
			1
		},
		{
			22516,
			-177238,
			208020,
			-15521,
			0,
			60,
			1
		},
		{
			22522,
			-177202,
			207533,
			-15516,
			0,
			60,
			1
		},
		{
			22532,
			-176900,
			207515,
			-15511,
			0,
			60,
			1
		},
		{
			22524,
			-176847,
			208022,
			-15523,
			0,
			60,
			1
		},
		{
			22516,
			-176187,
			207648,
			-15499,
			0,
			60,
			1
		},
		{
			22522,
			-176044,
			208039,
			-15524,
			0,
			60,
			1
		},
		{
			22532,
			-176346,
			208233,
			-15533,
			0,
			60,
			1
		},
		{
			22524,
			-176440,
			208594,
			-15527,
			0,
			60,
			1
		},
		{
			22516,
			-176938,
			208543,
			-15529,
			0,
			60,
			1
		},
		{
			22524,
			-176763,
			208108,
			-15523,
			0,
			60,
			1
		},
		{
			22522,
			-176483,
			207762,
			-15520,
			0,
			60,
			1
		},
		{
			22526,
			-177125,
			210766,
			-15517,
			0,
			60,
			1
		},
		{
			22520,
			-176810,
			210587,
			-15515,
			0,
			60,
			1
		},
		{
			22522,
			-176820,
			210975,
			-15528,
			0,
			60,
			1
		},
		{
			22524,
			-176412,
			210873,
			-15510,
			0,
			60,
			1
		},
		{
			22526,
			-176261,
			211197,
			-15511,
			0,
			60,
			1
		},
		{
			22520,
			-176241,
			211649,
			-15503,
			0,
			60,
			1
		},
		{
			22522,
			-176656,
			211884,
			-15516,
			0,
			60,
			1
		},
		{
			22524,
			-176798,
			211540,
			-15541,
			0,
			60,
			1
		},
		{
			22526,
			-177130,
			211694,
			-15533,
			0,
			60,
			1
		},
		{
			22520,
			-177436,
			211384,
			-15519,
			0,
			60,
			1
		},
		{
			22522,
			-177138,
			211159,
			-15524,
			0,
			60,
			1
		},
		{
			22526,
			-179311,
			212338,
			-15521,
			0,
			60,
			1
		},
		{
			22522,
			-178985,
			212502,
			-15513,
			0,
			60,
			1
		},
		{
			22516,
			-179313,
			212730,
			-15522,
			0,
			60,
			1
		},
		{
			22520,
			-178966,
			213036,
			-15510,
			0,
			60,
			1
		},
		{
			22526,
			-179352,
			213172,
			-15511,
			0,
			60,
			1
		},
		{
			22522,
			-179696,
			213427,
			-15514,
			0,
			60,
			1
		},
		{
			22516,
			-180096,
			213140,
			-15519,
			0,
			60,
			1
		},
		{
			22520,
			-179957,
			212724,
			-15530,
			0,
			60,
			1
		},
		{
			22522,
			-180240,
			212578,
			-15518,
			0,
			60,
			1
		},
		{
			22520,
			-179891,
			212271,
			-15520,
			0,
			60,
			1
		},
		{
			22516,
			-179593,
			212488,
			-15523,
			0,
			60,
			1
		},
		{
			22516,
			-181746,
			211163,
			-15515,
			0,
			60,
			1
		},
		{
			22522,
			-182002,
			211394,
			-15522,
			0,
			60,
			1
		},
		{
			22526,
			-181796,
			211647,
			-15511,
			0,
			60,
			1
		},
		{
			22516,
			-182160,
			211615,
			-15518,
			0,
			60,
			1
		},
		{
			22520,
			-182322,
			211843,
			-15510,
			0,
			60,
			1
		},
		{
			22522,
			-182720,
			211686,
			-15519,
			0,
			60,
			1
		},
		{
			22520,
			-182721,
			211231,
			-15535,
			0,
			60,
			1
		},
		{
			22516,
			-182957,
			210874,
			-15515,
			0,
			60,
			1
		},
		{
			22526,
			-182705,
			210606,
			-15526,
			0,
			60,
			1
		},
		{
			22520,
			-182359,
			210871,
			-15526,
			0,
			60,
			1
		},
		{
			22522,
			-182051,
			210644,
			-15521,
			0,
			60,
			1
		},
		{
			22524,
			-181994,
			208452,
			-15514,
			0,
			60,
			1
		},
		{
			22516,
			-182342,
			208661,
			-15510,
			0,
			60,
			1
		},
		{
			22526,
			-182471,
			208349,
			-15519,
			0,
			60,
			1
		},
		{
			22520,
			-182960,
			208335,
			-15504,
			0,
			60,
			1
		},
		{
			22524,
			-182751,
			207925,
			-15514,
			0,
			60,
			1
		},
		{
			22516,
			-182671,
			207442,
			-15531,
			0,
			60,
			1
		},
		{
			22520,
			-182276,
			207438,
			-15527,
			0,
			60,
			1
		},
		{
			22526,
			-181975,
			207353,
			-15520,
			0,
			60,
			1
		},
		{
			22524,
			-181989,
			207795,
			-15527,
			0,
			60,
			1
		},
		{
			22516,
			-181744,
			207997,
			-15520,
			0,
			60,
			1
		},
		{
			22520,
			-182318,
			208070,
			-15523,
			0,
			60,
			1
		},
		{
			18667,
			-178430,
			211520,
			-15504,
			28672,
			120,
			1
		},
		{
			18668,
			-177341,
			209129,
			-15504,
			16384,
			120,
			1
		},
		{
			18711,
			-178067,
			207862,
			-15504,
			8192,
			120,
			1
		},
		{
			18667,
			-180679,
			207614,
			-15520,
			61439,
			120,
			1
		},
		{
			18668,
			-181761,
			209949,
			-15520,
			49151,
			120,
			1
		},
		{
			18711,
			-180328,
			211686,
			-15504,
			36863,
			120,
			1
		}
	};
	
	protected static final int[][] ROOMS_TUMORS =
	{
		{
			32535,
			-179779,
			212540,
			-15520,
			49151,
			0,
			1
		},
		{
			32535,
			-177028,
			211135,
			-15520,
			36863,
			0,
			1
		},
		{
			32535,
			-176355,
			208043,
			-15520,
			28672,
			0,
			1
		},
		{
			32535,
			-179284,
			205990,
			-15520,
			16384,
			0,
			1
		},
		{
			32535,
			-182268,
			208218,
			-15520,
			4096,
			0,
			1
		},
		{
			32535,
			-182069,
			211140,
			-15520,
			61439,
			0,
			1
		}
	};
	
	protected static final int[][] ROOMS_ALIVE_TUMORS =
	{
		{
			18708,
			-179779,
			212540,
			-15520,
			49151,
			0,
			1
		},
		{
			18708,
			-177028,
			211135,
			-15520,
			36863,
			0,
			1
		},
		{
			18708,
			-176355,
			208043,
			-15520,
			28672,
			0,
			1
		},
		{
			18708,
			-179284,
			205990,
			-15520,
			16384,
			0,
			1
		},
		{
			18708,
			-182268,
			208218,
			-15520,
			4096,
			0,
			1
		},
		{
			18708,
			-182069,
			211140,
			-15520,
			61439,
			0,
			1
		}
	};
	
	protected static final int[][] ROOMS_BOSSES =
	{
		{
			25637,
			-179303,
			213090,
			-15504,
			49151,
			0,
			1
		},
		{
			25638,
			-176426,
			211219,
			-15504,
			36863,
			0,
			1
		},
		{
			25639,
			-177040,
			207870,
			-15504,
			28672,
			0,
			1
		},
		{
			25640,
			-179762,
			206479,
			-15504,
			16384,
			0,
			1
		},
		{
			25641,
			-182388,
			207599,
			-15504,
			4096,
			0,
			1
		},
		{
			25642,
			-182733,
			211096,
			-15504,
			61439,
			0,
			1
		}
	};
	
	protected static int[][] SOULWAGON_SPAWN =
	{
		{
			SOULWAGON,
			-180003,
			206703,
			-15520,
			0,
			0,
			-1
		},
		{
			SOULWAGON,
			-180056,
			216162,
			-15511,
			0,
			0,
			-1
		},
		{
			SOULWAGON,
			-179586,
			205657,
			-15499,
			0,
			0,
			-1
		},
		{
			SOULWAGON,
			-179029,
			205991,
			-15518,
			0,
			0,
			-1
		},
		{
			SOULWAGON,
			-178960,
			206658,
			-15526,
			0,
			0,
			-1
		},
		{
			SOULWAGON,
			-179350,
			206484,
			-15524,
			0,
			0,
			-1
		},
		{
			SOULWAGON,
			-179526,
			206900,
			-15520,
			0,
			0,
			-1
		},
		{
			SOULWAGON,
			-179329,
			205916,
			-15532,
			0,
			0,
			-1
		}
	};
	
	public HeartInfinityDefence()
	{
		super(-1, HeartInfinityDefence.class.getSimpleName(), "gracia/instances");
		
		addStartNpc(ABYSSGAZE);
		addTalkId(ABYSSGAZE);
		addStartNpc(DEADTUMOR);
		addTalkId(DEADTUMOR);
		addStartNpc(32536);
		addTalkId(32536);
		
		for (int id : NOTMOVE)
		{
			addSpawnId(id);
		}
		addSpawnId(SOULWAGON);
		
		addAggroRangeEnterId(18668);
		
		addKillId(ALIVETUMOR);
		addKillId(18711);
		
		tumorRespawnTime = 180 * 1000;
		wagonRespawnTime = 60 * 1000;
		
		addEnterZoneId(200032);
	}
	
	private void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2]);
	}
	
	private boolean checkConditions(L2PcInstance player)
	{
		if (player.isGM())
		{
			return true;
		}
		
		L2Party party = player.getParty();
		if (party == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}
		if (party.getLeader() != player)
		{
			player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}
		if ((party.getCommandChannel() == null) || (party.getCommandChannel().getLeader() != player))
		{
			player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}
		if ((party.getCommandChannel().getMembers().size() < Config.HEART_DEFENCE_MIN_PLAYERS) || (party.getCommandChannel().getMembers().size() > Config.HEART_DEFENCE_MAX_PLAYERS))// 18 27
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED);
			party.getCommandChannel().broadcastPacket(sm);
			return false;
		}
		for (L2PcInstance partyMember : party.getCommandChannel().getMembers())
		{
			if ((partyMember.getLevel() < 75) || (partyMember.getLevel() > 85))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(2097);
				sm.addPcName(partyMember);
				party.getCommandChannel().broadcastPacket(sm);
				return false;
			}
			
			if (!Util.checkIfInRange(1000, player, partyMember, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(2096);
				sm.addPcName(partyMember);
				party.getCommandChannel().broadcastPacket(sm);
				return false;
			}
			
			Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), INSTANCEID);
			if (System.currentTimeMillis() < reentertime)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(2100);
				sm.addPcName(partyMember);
				party.getCommandChannel().broadcastPacket(sm);
				return false;
			}
		}
		return true;
	}
	
	protected void enterInstance(L2PcInstance player, String template, int[] coords)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		
		if (world != null)
		{
			if (!(world instanceof HIDWorld))
			{
				player.sendPacket(SystemMessageId.YOU_HAVE_ENTERED_ANOTHER_INSTANT_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON);
				return;
			}
			teleportPlayer(player, coords, world.getInstanceId());
			return;
		}
		
		if (checkConditions(player))
		{
			world = new HIDWorld();
			world.setInstanceId(InstanceManager.getInstance().createDynamicInstance(template));
			world.setTemplateId(INSTANCEID);
			world.setStatus(0);
			InstanceManager.getInstance().addWorld(world);
			_log.info("Heart Infinity Defence started " + template + " Instance: " + world.getInstanceId() + " created by player: " + player.getName());
			
			if (player.isGM())
			{
				teleportPlayer(player, coords, world.getInstanceId());
				world.addAllowed(player.getObjectId());
			}
			
			if ((player.getParty() == null) || (player.getParty().getCommandChannel() == null))
			{
				teleportPlayer(player, coords, world.getInstanceId());
				world.addAllowed(player.getObjectId());
			}
			else
			{
				for (L2PcInstance partyMember : player.getParty().getCommandChannel().getMembers())
				{
					teleportPlayer(partyMember, coords, world.getInstanceId());
					world.addAllowed(partyMember.getObjectId());
					if (partyMember.getQuestState(qn) == null)
					{
						newQuestState(partyMember);
					}
				}
			}
			((HIDWorld) world).startTime = System.currentTimeMillis();
			((HIDWorld) world).finishTask = ThreadPoolManager.getInstance().scheduleGeneral(new FinishTask((HIDWorld) world), 30 * 60000);
			((HIDWorld) world).timerTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new TimerTask((HIDWorld) world), 298 * 1000, 5 * 60 * 1000);
			conquestBegins((HIDWorld) world);
		}
	}
	
	private void conquestBegins(final HIDWorld world)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			broadCastPacket(world, new ExShowScreenMessage(NpcStringId.YOU_CAN_HEAR_THE_UNDEAD_OF_EKIMUS_RUSHING_TOWARD_YOU_S1_S2_IT_HAS_NOW_BEGUN, 2, 8000));
			for (int[] spawn1 : ROOMS_MOBS)
			{
				for (int i1 = 0; i1 < spawn1[6]; i1++)
				{
					L2Npc npc1 = addSpawn(spawn1[0], spawn1[1], spawn1[2], spawn1[3], spawn1[4], false, 0, false, world.getInstanceId());
					npc1.getSpawn().setRespawnDelay(spawn1[5]);
					npc1.getSpawn().setAmount(1);
					if (spawn1[5] > 0)
					{
						npc1.getSpawn().startRespawn();
					}
					else
					{
						npc1.getSpawn().stopRespawn();
					}
				}
			}
			
			for (int[] spawn2 : ROOMS_TUMORS)
			{
				for (int i2 = 0; i2 < spawn2[6]; i2++)
				{
					L2Npc npc2 = addSpawn(spawn2[0], spawn2[1], spawn2[2], spawn2[3], spawn2[4], false, 0, false, world.getInstanceId());
					world.deadTumors.add(npc2);
				}
			}
			
			InstanceManager.getInstance().getInstance(world.getInstanceId()).getDoor(14240102).openMe();
			preawakenedEchmus = addSpawn(29161, -179534, 208510, -15496, 16342, false, 0, false, world.getInstanceId());
			
			ThreadPoolManager.getInstance().scheduleGeneral(() ->
			{
				if (!conquestEnded)
				{
					if (!world.deadTumors.isEmpty())
					{
						for (L2Npc npc : world.deadTumors)
						{
							if (npc != null)
							{
								spawnCoffin(npc, world);
							}
						}
					}
				}
			} , 60000);
			
			ThreadPoolManager.getInstance().scheduleGeneral(() ->
			{
				if (!conquestEnded)
				{
					if (!world.deadTumors.isEmpty())
					{
						for (L2Npc npc3 : world.deadTumors)
						{
							if (npc3 != null)
							{
								npc3.deleteMe();
							}
						}
						world.deadTumors.clear();
					}
					
					for (int[] spawn : ROOMS_ALIVE_TUMORS)
					{
						for (int i = 0; i < spawn[6]; i++)
						{
							L2Npc npc4 = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.getInstanceId());
							npc4.setCurrentHp(npc4.getMaxHp() * .5);
							world.deadTumors.add(npc4);
						}
					}
					broadCastPacket(world, new ExShowScreenMessage(NpcStringId.THE_TUMOR_INSIDE_S1_HAS_COMPLETELY_REVIVED_NEKIMUS_STARTED_TO_REGAIN_HIS_ENERGY_AND_IS_DESPERATELY_LOOKING_FOR_HIS_PREY, 2, 8000));
				}
			} , tumorRespawnTime);
			
			world.wagonSpawnTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> addSpawn(SOULWAGON, -179544, 207400, -15496, 0, false, 0, false, world.getInstanceId()), 1000, wagonRespawnTime);
		} , 20000);
	}
	
	void spawnCoffin(L2Npc npc, HIDWorld world)
	{
		spawnNpc(REGENERATION_COFFIN, npc.getLocation(), 0, world.getInstanceId());
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if (tmpworld instanceof HIDWorld)
		{
			HIDWorld world = (HIDWorld) tmpworld;
			
			if (event.startsWith("warpechmus"))
			{
				broadCastPacket(world, new ExShowScreenMessage(NpcStringId.S1S_PARTY_HAS_MOVED_TO_A_DIFFERENT_LOCATION_THROUGH_THE_CRACK_IN_THE_TUMOR, 2, 8000));
				for (L2PcInstance partyMember : player.getParty().getMembers())
				{
					if (partyMember.isInsideRadius(player, 800, true, false))
					{
						partyMember.teleToLocation(-179548, 209584, -15504, true);
					}
				}
			}
			else if (event.startsWith("reenterechmus"))
			{
				player.destroyItemByItemId("SOI", 13797, 3, player, true);
				for (L2PcInstance partyMember : player.getParty().getMembers())
				{
					if (partyMember.isInsideRadius(player, 400, true, false))
					{
						partyMember.teleToLocation(-179548, 209584, -15504, true);
					}
				}
			}
			else if (event.startsWith("warp"))
			{
				L2Npc victim = null;
				victim = world.deadTumor;
				if (victim != null)
				{
					world.deadTumors.add(victim);
				}
				
				player.destroyItemByItemId("SOI", 13797, 1, player, true);
				Location loc = world.deadTumors.get(Rnd.get(world.deadTumors.size())).getLocation();
				if (loc != null)
				{
					broadCastPacket(world, new ExShowScreenMessage(NpcStringId.S1S_PARTY_HAS_MOVED_TO_A_DIFFERENT_LOCATION_THROUGH_THE_CRACK_IN_THE_TUMOR, 2, 8000));
					for (L2PcInstance partyMember : player.getParty().getMembers())
					{
						if (partyMember.isInsideRadius(player, 500, true, false))
						{
							partyMember.teleToLocation(loc, true);
						}
					}
				}
			}
		}
		return "";
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getId();
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		if (npcId == ABYSSGAZE)
		{
			enterInstance(player, "HeartInfinityDefence.xml", ENTER_TELEPORT);
		}
		return "";
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof HIDWorld)
		{
			final HIDWorld world = (HIDWorld) tmpworld;
			
			if (npc.getId() == 18668)
			{
				for (int i = 0; i < Rnd.get(1, 4); i++)
				{
					spawnNpc(mobs[Rnd.get(mobs.length)], npc.getLocation(), 0, world.getInstanceId());
				}
				npc.doDie(npc);
			}
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		if (Util.contains(NOTMOVE, npc.getId()))
		{
			npc.setIsNoRndWalk(true);
			npc.setIsImmobilized(true);
		}
		
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof HIDWorld)
		{
			if (npc.getId() == SOULWAGON)
			{
				((L2MonsterInstance) npc).setPassive(true);
			}
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof HIDWorld)
		{
			final HIDWorld world = (HIDWorld) tmpworld;
			final Location loc = npc.getLocation();
			if (npc.getId() == ALIVETUMOR)
			{
				npc.dropItem(player, 13797, Rnd.get(2, 5));
				npc.deleteMe();
				world.deadTumor = spawnNpc(DEADTUMOR, loc, 0, world.getInstanceId());
				world.deadTumors.add(world.deadTumor);
				wagonRespawnTime += 10000;
				broadCastPacket(world, new ExShowScreenMessage(NpcStringId.THE_TUMOR_INSIDE_S1_HAS_BEEN_DESTROYED_NTHE_SPEED_THAT_EKIMUS_CALLS_OUT_HIS_PREY_HAS_SLOWED_DOWN, 2, 8000));
				
				ThreadPoolManager.getInstance().scheduleGeneral(() ->
				{
					world.deadTumor.deleteMe();
					L2Npc alivetumor = spawnNpc(ALIVETUMOR, loc, 0, world.getInstanceId());
					alivetumor.setCurrentHp(alivetumor.getMaxHp() * .25);
					world.npcList.add(alivetumor);
					wagonRespawnTime -= 10000;
					broadCastPacket(world, new ExShowScreenMessage(NpcStringId.THE_TUMOR_INSIDE_S1_HAS_COMPLETELY_REVIVED_NEKIMUS_STARTED_TO_REGAIN_HIS_ENERGY_AND_IS_DESPERATELY_LOOKING_FOR_HIS_PREY, 2, 8000));
				} , tumorRespawnTime);
			}
			
			if (npc.getId() == 18711)
			{
				tumorRespawnTime += 5 * 1000;
			}
		}
		return "";
	}
	
	protected void notifyWagonArrived(L2Npc npc, HIDWorld world)
	{
		coffinsCreated++;
		if (coffinsCreated == 20)
		{
			conquestConclusion(world);
		}
		else
		{
			NpcSay cs = new NpcSay(preawakenedEchmus.getObjectId(), Say2.SHOUT, preawakenedEchmus.getId(), NpcStringId.BRING_MORE_MORE_SOULS);
			preawakenedEchmus.broadcastPacket(cs);
			ExShowScreenMessage message = new ExShowScreenMessage(NpcStringId.THE_SOUL_COFFIN_HAS_AWAKENED_EKIMUS, 2, 8000);
			message.addStringParameter(Integer.toString(maxCoffins - coffinsCreated));
			broadCastPacket(world, message);
		}
	}
	
	private class TimerTask implements Runnable
	{
		private final HIDWorld _world;
		
		TimerTask(HIDWorld world)
		{
			_world = world;
		}
		
		@Override
		public void run()
		{
			long time = ((_world.startTime + (25 * 60 * 1000L)) - System.currentTimeMillis()) / 60000;
			if (time == 0)
			{
				conquestConclusion(_world);
			}
			else
			{
				if (time == 15)
				{
					for (int[] spawn : ROOMS_BOSSES)
					{
						for (int i = 0; i < spawn[6]; i++)
						{
							addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, _world.getInstanceId());
						}
					}
				}
			}
		}
	}
	
	class FinishTask implements Runnable
	{
		private final HIDWorld _world;
		
		FinishTask(HIDWorld world)
		{
			_world = world;
		}
		
		@Override
		public void run()
		{
			if (_world != null)
			{
				conquestEnded = true;
				final Instance inst = InstanceManager.getInstance().getInstance(_world.getInstanceId());
				if (inst != null)
				{
					for (int objId : _world.getAllowed())
					{
						L2PcInstance player = L2World.getInstance().getPlayer(objId);
						QuestState st = player.getQuestState(Q00697_DefendtheHallofErosion.class.getSimpleName());
						if ((st != null) && (st.getInt("cond") == 1))
						{
							st.set("defenceDone", 1);
						}
					}
					broadCastPacket(_world, new ExShowScreenMessage(NpcStringId.CONGRATULATIONS_YOU_HAVE_SUCCEEDED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE, 2, 8000));
					inst.removeNpcs();
					if (inst.getPlayers().isEmpty())
					{
						inst.setDuration(5 * 60000);
					}
					else
					{
						inst.setDuration(5 * 60000);
						inst.setEmptyDestroyTime(5 * 60000);
					}
				}
			}
		}
	}
	
	protected void conquestConclusion(HIDWorld world)
	{
		if (world.timerTask != null)
		{
			world.timerTask.cancel(false);
		}
		
		if (world.finishTask != null)
		{
			world.finishTask.cancel(false);
		}
		
		if (world.wagonSpawnTask != null)
		{
			world.wagonSpawnTask.cancel(false);
		}
		
		broadCastPacket(world, new ExShowScreenMessage(NpcStringId.YOU_HAVE_FAILED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE, 2, 8000));
		
		conquestEnded = true;
		final Instance inst = InstanceManager.getInstance().getInstance(world.getInstanceId());
		if (inst != null)
		{
			inst.removeNpcs();
			if (inst.getPlayers().isEmpty())
			{
				inst.setDuration(5 * 60000);
			}
			else
			{
				inst.setDuration(5 * 60000);
				inst.setEmptyDestroyTime(5 * 60000);
			}
		}
		
	}
	
	@Override
	public final String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (character instanceof L2Attackable)
		{
			final L2Attackable npc = (L2Attackable) character;
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof HIDWorld)
			{
				HIDWorld world = (HIDWorld) tmpworld;
				if (npc.getId() == SOULWAGON)
				{
					notifyWagonArrived(npc, world);
					npc.deleteMe();
				}
			}
		}
		return null;
	}
	
	protected void broadCastPacket(HIDWorld world, L2GameServerPacket packet)
	{
		for (int objId : world.getAllowed())
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objId);
			if ((player != null) && player.isOnline() && (player.getInstanceId() == world.getInstanceId()))
			{
				player.sendPacket(packet);
			}
		}
	}
}