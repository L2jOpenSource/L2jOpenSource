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
package gracia.instances.HallOfErosionDefence;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.L2CommandChannel;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2QuestGuardInstance;
import l2r.gameserver.model.entity.Instance;
import l2r.gameserver.model.instancezone.InstanceWorld;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.NpcSay;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

import ai.npc.AbstractNpcAI;
import quests.Q00697_DefendtheHallofErosion.Q00697_DefendtheHallofErosion;

public class HallOfErosionDefence extends AbstractNpcAI
{
	protected class HEDWorld extends InstanceWorld
	{
		public List<L2Attackable> npcList = new ArrayList<>();
		public List<L2Npc> alivetumor = new ArrayList<>();
		public List<L2Npc> deadTumors = new ArrayList<>();
		protected L2Npc deadTumor;
		public long startTime = 0;
		public ScheduledFuture<?> finishTask = null;
		
		public synchronized void addTag(int value)
		{
			tag += value;
		}
		
		public HEDWorld()
		{
			tag = -1;
		}
	}
	
	private static final int INSTANCEID = 120;
	private static final int MOUTHOFEKIMUS = 32537;
	private static final int TUMOR_ALIVE = 18708;
	private static final int TUMOR_DEAD = 32535;
	private static final int SEED = 32541;
	
	public int tumorKillCount = 0;
	protected boolean conquestEnded = false;
	private boolean soulwagonSpawned = false;
	private static int seedKills = 0;
	private long tumorRespawnTime;
	
	private static final int[] ENTER_TELEPORT =
	{
		-179659,
		211061,
		-12784
	};
	
	private static int[] NOTMOVE =
	{
		18667,
		18668,
		18708,
		18709,
		18711,
		TUMOR_DEAD
	};
	
	private static final int[] mobs =
	{
		22516,
		22520,
		22522,
		22524,
		22526,
		22532
	};
	
	private static final int[][] SEEDS_SPAWN =
	{
		{
			SEED,
			-178418,
			211653,
			-12029,
			49151,
			0,
			1
		},
		{
			SEED,
			-178417,
			206558,
			-12029,
			16384,
			0,
			1
		},
		{
			SEED,
			-180911,
			206551,
			-12029,
			16384,
			0,
			1
		},
		{
			SEED,
			-180911,
			211652,
			-12029,
			49151,
			0,
			1
		}
	};
	
	private static final int[][] TUMOR_DEAD_SPAWN =
	{
		{
			TUMOR_DEAD,
			-176036,
			210002,
			-11948,
			36863,
			0,
			1
		},
		{
			TUMOR_DEAD,
			-176039,
			208203,
			-11948,
			28672,
			0,
			1
		},
		{
			TUMOR_DEAD,
			-183288,
			208205,
			-11948,
			4096,
			0,
			1
		},
		{
			TUMOR_DEAD,
			-183290,
			210004,
			-11948,
			61439,
			0,
			1
		}
	};
	
	protected static final int[][] TUMOR_ALIVE_SPAWN =
	{
		{
			TUMOR_ALIVE,
			-176036,
			210002,
			-11948,
			36863,
			0,
			1
		},
		{
			TUMOR_ALIVE,
			-176039,
			208203,
			-11948,
			28672,
			0,
			1
		},
		{
			TUMOR_ALIVE,
			-183288,
			208205,
			-11948,
			4096,
			0,
			1
		},
		{
			TUMOR_ALIVE,
			-183290,
			210004,
			-11948,
			61439,
			0,
			1
		}
	};
	
	private static final int[][] ROOMS_MOBS =
	{
		{
			22516,
			-180364,
			211944,
			-12019,
			0,
			60,
			1
		},
		{
			22516,
			-181616,
			211413,
			-12015,
			0,
			60,
			1
		},
		{
			22520,
			-181404,
			211042,
			-12023,
			0,
			60,
			1
		},
		{
			22522,
			-181558,
			212227,
			-12035,
			0,
			60,
			1
		},
		{
			22522,
			-180459,
			212322,
			-12018,
			0,
			60,
			1
		},
		{
			22524,
			-180428,
			211180,
			-12014,
			0,
			60,
			1
		},
		{
			22524,
			-180718,
			212162,
			-12028,
			0,
			60,
			1
		},
		
		{
			22532,
			-183114,
			209397,
			-11923,
			0,
			60,
			1
		},
		{
			22532,
			-182917,
			210495,
			-11925,
			0,
			60,
			1
		},
		{
			22516,
			-183918,
			210225,
			-11934,
			0,
			60,
			1
		},
		{
			22532,
			-183862,
			209909,
			-11932,
			0,
			60,
			1
		},
		{
			22532,
			-183246,
			210631,
			-11923,
			0,
			60,
			1
		},
		{
			22522,
			-182971,
			210522,
			-11924,
			0,
			60,
			1
		},
		{
			22522,
			-183485,
			209406,
			-11921,
			0,
			60,
			1
		},
		
		{
			22516,
			-183032,
			208822,
			-11923,
			0,
			60,
			1
		},
		{
			22516,
			-182709,
			207817,
			-11929,
			0,
			60,
			1
		},
		{
			22520,
			-182964,
			207746,
			-11924,
			0,
			60,
			1
		},
		{
			22520,
			-183385,
			208847,
			-11922,
			0,
			60,
			1
		},
		{
			22526,
			-183684,
			208847,
			-11926,
			0,
			60,
			1
		},
		{
			22526,
			-183530,
			208725,
			-11926,
			0,
			60,
			1
		},
		{
			22532,
			-183968,
			207603,
			-11928,
			0,
			60,
			1
		},
		{
			22532,
			-183608,
			208567,
			-11926,
			0,
			60,
			1
		},
		
		{
			22526,
			-181471,
			207159,
			-12020,
			0,
			60,
			1
		},
		{
			22526,
			-180213,
			207042,
			-12013,
			0,
			60,
			1
		},
		{
			22532,
			-180213,
			206506,
			-12010,
			0,
			60,
			1
		},
		{
			22532,
			-181720,
			206643,
			-12016,
			0,
			60,
			1
		},
		{
			22516,
			-181743,
			206643,
			-12018,
			0,
			60,
			1
		},
		{
			22516,
			-181028,
			205739,
			-12030,
			0,
			60,
			1
		},
		{
			22520,
			-181431,
			205980,
			-12040,
			0,
			60,
			1
		},
		
		{
			22524,
			-178964,
			207168,
			-12014,
			0,
			60,
			1
		},
		{
			22524,
			-177658,
			207037,
			-12019,
			0,
			60,
			1
		},
		{
			22522,
			-177730,
			206558,
			-12016,
			0,
			60,
			1
		},
		{
			22522,
			-179132,
			206650,
			-12011,
			0,
			60,
			1
		},
		{
			22526,
			-179132,
			206155,
			-12017,
			0,
			60,
			1
		},
		{
			22526,
			-178277,
			205754,
			-12031,
			0,
			60,
			1
		},
		{
			22516,
			-178716,
			205802,
			-12020,
			0,
			60,
			1
		},
		
		{
			22532,
			-176565,
			207839,
			-11929,
			0,
			60,
			1
		},
		{
			22532,
			-176281,
			208822,
			-11923,
			0,
			60,
			1
		},
		{
			22520,
			-175791,
			208804,
			-11923,
			0,
			60,
			1
		},
		{
			22520,
			-176259,
			207689,
			-11923,
			0,
			60,
			1
		},
		{
			22526,
			-175849,
			207508,
			-11929,
			0,
			60,
			1
		},
		{
			22526,
			-175453,
			208250,
			-11930,
			0,
			60,
			1
		},
		{
			22524,
			-175738,
			207914,
			-11946,
			0,
			60,
			1
		},
		
		{
			22526,
			-176339,
			209425,
			-11923,
			0,
			60,
			1
		},
		{
			22526,
			-176586,
			210424,
			-11928,
			0,
			60,
			1
		},
		{
			22516,
			-176586,
			210546,
			-11923,
			0,
			60,
			1
		},
		{
			22516,
			-175847,
			209365,
			-11922,
			0,
			60,
			1
		},
		{
			22520,
			-175496,
			209498,
			-11924,
			0,
			60,
			1
		},
		{
			22520,
			-175538,
			210252,
			-11940,
			0,
			60,
			1
		},
		{
			22524,
			-175527,
			209744,
			-11928,
			0,
			60,
			1
		},
		
		{
			22520,
			-177940,
			210876,
			-12005,
			0,
			60,
			1
		},
		{
			22520,
			-178935,
			210903,
			-12018,
			0,
			60,
			1
		},
		{
			22522,
			-179331,
			211365,
			-12013,
			0,
			60,
			1
		},
		{
			22522,
			-177637,
			211579,
			-12015,
			0,
			60,
			1
		},
		{
			22526,
			-177837,
			212356,
			-12037,
			0,
			60,
			1
		},
		{
			22526,
			-179030,
			212261,
			-12018,
			0,
			60,
			1
		},
		{
			22532,
			-178367,
			212328,
			-12031,
			0,
			60,
			1
		},
		{
			18667,
			-179664,
			209443,
			-12476,
			16384,
			120,
			1
		},
		{
			18711,
			-179093,
			209738,
			-12480,
			40279,
			120,
			1
		},
		{
			18667,
			-178248,
			209688,
			-12479,
			24320,
			120,
			1
		},
		{
			18668,
			-177998,
			209100,
			-12480,
			16304,
			120,
			1
		},
		{
			18711,
			-178246,
			208493,
			-12480,
			8968,
			120,
			1
		},
		{
			18668,
			-178808,
			208339,
			-12480,
			-1540,
			120,
			1
		},
		{
			18711,
			-179663,
			208738,
			-12480,
			0,
			120,
			1
		},
		{
			18711,
			-180498,
			208330,
			-12467,
			3208,
			120,
			1
		},
		{
			18667,
			-181070,
			208502,
			-12467,
			-7552,
			120,
			1
		},
		{
			18668,
			-181310,
			209097,
			-12467,
			-16408,
			120,
			1
		},
		{
			18711,
			-181069,
			209698,
			-12467,
			-24792,
			120,
			1
		},
		{
			18668,
			-180228,
			209744,
			-12467,
			25920,
			120,
			1
		}
	};
	
	public HallOfErosionDefence()
	{
		super(HallOfErosionDefence.class.getSimpleName(), "gracia/instances");
		
		addStartNpc(MOUTHOFEKIMUS);
		addTalkId(MOUTHOFEKIMUS);
		addStartNpc(TUMOR_DEAD);
		addTalkId(TUMOR_DEAD);
		
		addSpawnId(NOTMOVE);
		addSpawnId(SEED);
		
		addAggroRangeEnterId(18668);
		
		addKillId(TUMOR_ALIVE);
		addKillId(SEED);
		addKillId(18711);
		
		tumorRespawnTime = 180 * 1000;
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
		
		L2CommandChannel channel = party.getCommandChannel();
		if (channel == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
			return false;
		}
		
		if (channel.getLeader() != player)
		{
			player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}
		
		if ((party.getCommandChannel().getMembers().size() < Config.EROSION_DEFENCE_MIN_PLAYERS) || (party.getCommandChannel().getMembers().size() > Config.EROSION_DEFENCE_MAX_PLAYERS))// 18 27
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
			
			QuestState st = partyMember.getQuestState(Q00697_DefendtheHallofErosion.class.getSimpleName());
			if ((st == null) || (st.getInt("cond") != 1))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				player.getParty().getCommandChannel().broadcastPacket(sm);
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
			if (!(world instanceof HEDWorld))
			{
				player.sendPacket(SystemMessageId.YOU_HAVE_ENTERED_ANOTHER_INSTANT_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON);
				return;
			}
			teleportPlayer(player, coords, world.getInstanceId());
			return;
		}
		
		if (checkConditions(player))
		{
			world = new HEDWorld();
			world.setInstanceId(InstanceManager.getInstance().createDynamicInstance(template));
			world.setTemplateId(INSTANCEID);
			world.setStatus(0);
			((HEDWorld) world).startTime = System.currentTimeMillis();
			InstanceManager.getInstance().addWorld(world);
			_log.info("Hall Of Erosion Defence started " + template + " Instance: " + world.getInstanceId() + " created by player: " + player.getName());
			
			if (player.isInParty())
			{
				for (L2PcInstance partyMember : player.getParty().isInCommandChannel() ? player.getParty().getCommandChannel().getMembers() : player.getParty().getMembers())
				{
					teleportPlayer(partyMember, coords, world.getInstanceId());
					world.addAllowed(partyMember.getObjectId());
				}
			}
			else
			{
				teleportPlayer(player, coords, world.getInstanceId());
				world.addAllowed(player.getObjectId());
			}
			
			((HEDWorld) world).finishTask = ThreadPoolManager.getInstance().scheduleGeneral(new FinishTask((HEDWorld) world), 20 * 60000);
			runTumors((HEDWorld) world);
		}
	}
	
	protected void runTumors(final HEDWorld world)
	{
		for (int[] spawn : ROOMS_MOBS)
		{
			for (int i = 0; i < spawn[6]; i++)
			{
				world.npcList = new ArrayList<>();
				L2Attackable npc = (L2Attackable) addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.getInstanceId());
				npc.getSpawn().setRespawnDelay(spawn[5]);
				npc.getSpawn().setAmount(1);
				if (spawn[5] > 0)
				{
					npc.getSpawn().startRespawn();
				}
				else
				{
					npc.getSpawn().stopRespawn();
				}
				world.npcList.add(npc);
			}
		}
		
		for (int[] spawn : TUMOR_DEAD_SPAWN)
		{
			for (int i = 0; i < spawn[6]; i++)
			{
				L2Npc npc = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.getInstanceId());
				world.deadTumors.add(npc);
				ThreadPoolManager.getInstance().scheduleGeneral(new RegenerationCoffinSpawn(npc, world), 1000);
			}
		}
		
		for (int[] spawn : SEEDS_SPAWN)
		{
			for (int i = 0; i < spawn[6]; i++)
			{
				addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.getInstanceId());
			}
		}
		
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			if (!conquestEnded)
			{
				stopDeadTumors(world);
				for (int[] spawn : TUMOR_ALIVE_SPAWN)
				{
					for (int i = 0; i < spawn[6]; i++)
					{
						L2Npc npc = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.getInstanceId());
						world.alivetumor.add(npc);
					}
				}
				broadCastPacket(world, new ExShowScreenMessage(NpcStringId.THE_TUMOR_INSIDE_S1_HAS_COMPLETELY_REVIVED_NRECOVERED_NEARBY_UNDEAD_ARE_SWARMING_TOWARD_SEED_OF_LIFE, 2, 8000));
			}
		} , 180 * 1000);
		broadCastPacket(world, new ExShowScreenMessage(NpcStringId.YOU_CAN_HEAR_THE_UNDEAD_OF_EKIMUS_RUSHING_TOWARD_YOU_S1_S2_IT_HAS_NOW_BEGUN, 2, 8000));
	}
	
	protected void stopDeadTumors(HEDWorld world)
	{
		if (!world.deadTumors.isEmpty())
		{
			for (L2Npc npc : world.deadTumors)
			{
				if (npc != null)
				{
					npc.deleteMe();
				}
			}
		}
		world.deadTumors.clear();
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if (tmpworld instanceof HEDWorld)
		{
			HEDWorld world = (HEDWorld) tmpworld;
			
			if (event.startsWith("warp"))
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
		if (npc.getId() == MOUTHOFEKIMUS)
		{
			enterInstance(player, "HallOfErosionDefence.xml", ENTER_TELEPORT);
			return "";
		}
		return "";
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof HEDWorld)
		{
			final HEDWorld world = (HEDWorld) tmpworld;
			
			if (npc.getId() == 18668)
			{
				for (int i = 0; i < Rnd.get(1, 4); i++)
				{
					spawnNpc(mobs[Rnd.get(mobs.length)], npc.getLocation(), 0, world.getInstanceId());
				}
				npc.deleteMe();
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
		
		if (npc.getId() == SEED)
		{
			((L2QuestGuardInstance) npc).setPassive(true);
		}
		
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof HEDWorld)
		{
			HEDWorld world = (HEDWorld) tmpworld;
			if (npc.getId() == TUMOR_DEAD)
			{
				world.addTag(1);
			}
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof HEDWorld)
		{
			final HEDWorld world = (HEDWorld) tmpworld;
			if (npc.getId() == TUMOR_ALIVE)
			{
				npc.dropItem(player, 13797, Rnd.get(2, 5));
				npc.deleteMe();
				notifyTumorDeath(npc, world);
				world.deadTumor = spawnNpc(TUMOR_DEAD, npc.getLocation(), 0, world.getInstanceId());
				world.deadTumors.add(world.deadTumor);
				broadCastPacket(world, new ExShowScreenMessage(NpcStringId.THE_TUMOR_INSIDE_S1_HAS_BEEN_DESTROYED_NTHE_NEARBY_UNDEAD_THAT_WERE_ATTACKING_SEED_OF_LIFE_START_LOSING_THEIR_ENERGY_AND_RUN_AWAY, 2, 8000));
				ThreadPoolManager.getInstance().scheduleGeneral(() ->
				{
					world.deadTumor.deleteMe();
					L2Npc tumor = spawnNpc(TUMOR_ALIVE, world.deadTumor.getLocation(), 0, world.getInstanceId());
					world.alivetumor.add(tumor);
					broadCastPacket(world, new ExShowScreenMessage(NpcStringId.THE_TUMOR_INSIDE_S1_HAS_COMPLETELY_REVIVED_NRECOVERED_NEARBY_UNDEAD_ARE_SWARMING_TOWARD_SEED_OF_LIFE, 2, 8000));
				} , tumorRespawnTime);
			}
			
			if (npc.getId() == 18711)
			{
				tumorRespawnTime += 5 * 1000;
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	public String onKillByMob(L2Npc npc, L2Npc killer)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof HEDWorld)
		{
			HEDWorld world = (HEDWorld) tmpworld;
			
			seedKills++;
			if (seedKills >= 1)
			{
				conquestConclusion(world);
			}
		}
		return null;
	}
	
	private void notifyTumorDeath(L2Npc npc, HEDWorld world)
	{
		tumorKillCount++;
		if ((tumorKillCount == 4) && !soulwagonSpawned)
		{
			soulwagonSpawned = true;
			L2Npc soul = spawnNpc(25636, npc.getLocation(), 0, world.getInstanceId());
			NpcSay cs = new NpcSay(soul.getObjectId(), Say2.SHOUT, soul.getId(), NpcStringId.HA_HA_HA);
			soul.broadcastPacket(cs);
		}
	}
	
	private class RegenerationCoffinSpawn implements Runnable
	{
		private final L2Npc _npc;
		private final HEDWorld _world;
		
		public RegenerationCoffinSpawn(L2Npc npc, HEDWorld world)
		{
			_npc = npc;
			_world = world;
		}
		
		@Override
		public void run()
		{
			if (conquestEnded)
			{
				return;
			}
			for (int i = 0; i < 4; i++)
			{
				L2Npc worm = spawnNpc(18709, _npc.getLocation(), 0, _world.getInstanceId());
				_world.deadTumors.add(worm);
			}
		}
	}
	
	class FinishTask implements Runnable
	{
		private final HEDWorld _world;
		
		FinishTask(HEDWorld world)
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
	
	private void conquestConclusion(HEDWorld world)
	{
		if (world.finishTask != null)
		{
			world.finishTask.cancel(false);
			world.finishTask = null;
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
	
	protected void broadCastPacket(HEDWorld world, L2GameServerPacket packet)
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