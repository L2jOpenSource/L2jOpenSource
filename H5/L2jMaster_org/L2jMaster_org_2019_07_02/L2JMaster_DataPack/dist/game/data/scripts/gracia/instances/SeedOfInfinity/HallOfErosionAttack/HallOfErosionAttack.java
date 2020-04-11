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
package gracia.instances.SeedOfInfinity.HallOfErosionAttack;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.instancemanager.SeedOfInfinityManager;
import com.l2jserver.gameserver.model.L2Party;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.entity.Instance;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.NpcStringId;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.clientpackets.Say2;
import com.l2jserver.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jserver.gameserver.network.serverpackets.NpcSay;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.gameserver.util.Util;
import com.l2jserver.util.Rnd;

import ai.npc.AbstractNpcAI;
import quests.Q00696_ConquerTheHallOfErosion.Q00696_ConquerTheHallOfErosion;

/**
 * Seed of Infinity (Hall of Erosion Attack) instance zone.
 * @author MaGa1, Sacrifice
 */
public final class HallOfErosionAttack extends AbstractNpcAI
{
	protected class HEWorld extends InstanceWorld
	{
		protected final List<L2Npc> npcList = new ArrayList<>();
		protected final List<L2Npc> deadTumors = new ArrayList<>();
		protected int tumorCount = 0;
		protected L2Npc cohemenes = null;
		protected L2Npc deadTumor;
		protected boolean isBossAttacked = false;
		
		protected final long[] startTime =
		{
			0,
			0
		};
		
		protected synchronized void addTumorCount(int value)
		{
			tumorCount += value;
		}
		
		protected synchronized void addTag(int value)
		{
			tag += value;
		}
		
		protected HEWorld()
		{
			tag = -1;
		}
	}
	
	private static final int INSTANCE_PENALTY = 24;
	private static final int INSTANCE_ID = 119;
	
	private static final int TUMOR_OF_DEATH = 18708;
	private static final int SOUL_COFFIN = 18710;
	private static final int COHEMENES = 25634;
	private static final int DESTROYED_TUMOR = 32535;
	private static final int MOUTH_OF_EKIMUS = 32537;
	
	private static final int TEAR_OF_A_FREED_SOUL_ITEM_ID = 13797;
	
	private static final int ENERGY_OF_GENERATION_SKILL_ID = 5940;
	
	protected boolean conquestEnded = false;
	private long tumorRespawnTime;
	
	//@formatter:off
	private static final int[] ENTER_TELEPORT = {-179659, 211061, -12784};
	
	private static final int[] NOT_MOVE = {TUMOR_OF_DEATH, SOUL_COFFIN, DESTROYED_TUMOR, 18780};
	
	private static final int[][] ROOMS_MOBS_SPAWN =
	{
		{22516, -180364, 211944, -12019, 0, 60, 1}, {22516, -181616, 211413, -12015, 0, 60, 1},
		{22520, -181404, 211042, -12023, 0, 60, 1}, {22522, -181558, 212227, -12035, 0, 60, 1},
		{22522, -180459, 212322, -12018, 0, 60, 1}, {22524, -180428, 211180, -12014, 0, 60, 1},
		{22524, -180718, 212162, -12028, 0, 60, 1}, {22532, -183114, 209397, -11923, 0, 60, 1},
		{22532, -182917, 210495, -11925, 0, 60, 1}, {22516, -183918, 210225, -11934, 0, 60, 1},
		{22532, -183862, 209909, -11932, 0, 60, 1}, {22532, -183246, 210631, -11923, 0, 60, 1},
		{22522, -182971, 210522, -11924, 0, 60, 1}, {22522, -183485, 209406, -11921, 0, 60, 1},
		{22516, -183032, 208822, -11923, 0, 60, 1}, {22516, -182709, 207817, -11929, 0, 60, 1},
		{22520, -182964, 207746, -11924, 0, 60, 1}, {22520, -183385, 208847, -11922, 0, 60, 1},
		{22526, -183684, 208847, -11926, 0, 60, 1}, {22526, -183530, 208725, -11926, 0, 60, 1},
		{22532, -183968, 207603, -11928, 0, 60, 1}, {22532, -183608, 208567, -11926, 0, 60, 1},
		{22526, -181471, 207159, -12020, 0, 60, 1}, {22526, -180213, 207042, -12013, 0, 60, 1},
		{22532, -180213, 206506, -12010, 0, 60, 1}, {22532, -181720, 206643, -12016, 0, 60, 1},
		{22516, -181743, 206643, -12018, 0, 60, 1}, {22516, -181028, 205739, -12030, 0, 60, 1},
		{22520, -181431, 205980, -12040, 0, 60, 1}, {22524, -178964, 207168, -12014, 0, 60, 1},
		{22524, -177658, 207037, -12019, 0, 60, 1}, {22522, -177730, 206558, -12016, 0, 60, 1},
		{22522, -179132, 206650, -12011, 0, 60, 1}, {22526, -179132, 206155, -12017, 0, 60, 1},
		{22526, -178277, 205754, -12031, 0, 60, 1}, {22516, -178716, 205802, -12020, 0, 60, 1},
		{22532, -176565, 207839, -11929, 0, 60, 1}, {22532, -176281, 208822, -11923, 0, 60, 1},
		{22520, -175791, 208804, -11923, 0, 60, 1}, {22520, -176259, 207689, -11923, 0, 60, 1},
		{22526, -175849, 207508, -11929, 0, 60, 1}, {22526, -175453, 208250, -11930, 0, 60, 1},
		{22524, -175738, 207914, -11946, 0, 60, 1}, {22526, -176339, 209425, -11923, 0, 60, 1},
		{22526, -176586, 210424, -11928, 0, 60, 1}, {22516, -176586, 210546, -11923, 0, 60, 1},
		{22516, -175847, 209365, -11922, 0, 60, 1}, {22520, -175496, 209498, -11924, 0, 60, 1},
		{22520, -175538, 210252, -11940, 0, 60, 1}, {22524, -175527, 209744, -11928, 0, 60, 1},
		{22520, -177940, 210876, -12005, 0, 60, 1}, {22520, -178935, 210903, -12018, 0, 60, 1},
		{22522, -179331, 211365, -12013, 0, 60, 1}, {22522, -177637, 211579, -12015, 0, 60, 1},
		{22526, -177837, 212356, -12037, 0, 60, 1}, {22526, -179030, 212261, -12018, 0, 60, 1},
		{22532, -178367, 212328, -12031, 0, 60, 1}
	};
	
	private static final int[][] ROOMS_TUMORS_SPAWN =
	{
		{18780, -180911, 211652, -12029, 49151, 240, 1}, {18780, -180911, 206551, -12029, 16384, 240, 1},
		{18780, -178417, 206558, -12032, 16384, 240, 1}, {18780, -178418, 211653, -12029, 49151, 240, 1},
		{18708, -183290, 210004, -11948, 61439, 0, 1}, {18708, -183288, 208205, -11948, 4096, 0, 1},
		{18708, -176039, 208203, -11948, 28672, 0, 1}, {18708, -176036, 210002, -11948, 36863, 0, 1},
		{18668, -179664, 209443, -12476, 16384, 120, 1}, {18668, -179093, 209738, -12480, 40279, 120, 1},
		{18668, -178248, 209688, -12479, 24320, 120, 1}, {18668, -177998, 209100, -12480, 16304, 120, 1},
		{18668, -178246, 208493, -12480, 8968, 120, 1}, {18668, -178808, 208339, -12480, -1540, 120, 1},
		{18668, -179663, 208738, -12480, 0, 120, 1}, {18668, -180498, 208330, -12467, 3208, 120, 1},
		{18668, -181070, 208502, -12467, -7552, 120, 1}, {18668, -181310, 209097, -12467, -16408, 120, 1},
		{18668, -181069, 209698, -12467, -24792, 120, 1}, {18668, -180228, 209744, -12467, 25920, 120, 1}
	};
	
	private static final int[][] COHEMENES_SPAWN =
	{
		{25634, -178472, 211823, -12025, 0, 0, -1},
		{25634, -180926, 211887, -12029, 0, 0, -1},
		{25634, -180906, 206635, -12032, 0, 0, -1},
		{25634, -178492, 206426, -12023, 0, 0, -1}
	};
	//@formatter:on
	
	public HallOfErosionAttack()
	{
		super(HallOfErosionAttack.class.getSimpleName(), "gracia/instances");
		
		addStartNpc(MOUTH_OF_EKIMUS, DESTROYED_TUMOR);
		addTalkId(MOUTH_OF_EKIMUS, DESTROYED_TUMOR);
		addSpawnId(COHEMENES);
		
		for (int id : NOT_MOVE)
		{
			addSpawnId(id);
		}
		
		addAttackId(COHEMENES);
		addKillId(TUMOR_OF_DEATH, COHEMENES, SOUL_COFFIN);
		
		tumorRespawnTime = 180 * 1000;
	}
	
	private void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2]);
	}
	
	private boolean checkConditions(L2PcInstance player)
	{
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
			player.sendPacket(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
			return false;
		}
		if ((party.getCommandChannel().getMembers().size() < 18) || (party.getCommandChannel().getMembers().size() > 27))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
			party.getCommandChannel().broadcastPacket(sm);
			return false;
		}
		for (L2PcInstance partyMember : party.getCommandChannel().getMembers())
		{
			if ((partyMember.getLevel() < 75) || (partyMember.getLevel() > 85))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				party.getCommandChannel().broadcastPacket(sm);
				return false;
			}
			
			if (!Util.checkIfInRange(1000, player, partyMember, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_LOCATION_WHICH_CANNOT_BE_ENTERED_THEREFORE_IT_CANNOT_BE_PROCESSED);
				sm.addPcName(partyMember);
				party.getCommandChannel().broadcastPacket(sm);
				return false;
			}
			
			Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), INSTANCE_ID);
			if (System.currentTimeMillis() < reentertime)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_RE_ENTER_YET);
				sm.addPcName(partyMember);
				party.getCommandChannel().broadcastPacket(sm);
				return false;
			}
			
			QuestState st = partyMember.getQuestState(Q00696_ConquerTheHallOfErosion.class.getSimpleName());
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
	
	private int enterInstance(L2PcInstance player, String template, int[] coords)
	{
		int instanceId = 0;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		
		if (world != null)
		{
			if (!(world instanceof HEWorld))
			{
				player.sendPacket(SystemMessageId.YOU_HAVE_ENTERED_ANOTHER_INSTANT_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON);
				return 0;
			}
			teleportPlayer(player, coords, world.getInstanceId());
			return instanceId;
		}
		
		if (!checkConditions(player))
		{
			return 0;
		}
		
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new HEWorld();
		world.setTemplateId(INSTANCE_ID);
		world.setInstanceId(instanceId);
		world.setStatus(0);
		((HEWorld) world).startTime[0] = System.currentTimeMillis();
		InstanceManager.getInstance().addWorld(world);
		
		if ((player.getParty() == null) || (player.getParty().getCommandChannel() == null))
		{
			teleportPlayer(player, coords, instanceId);
			world.addAllowed(player.getObjectId());
		}
		else
		{
			for (L2PcInstance partyMember : player.getParty().getCommandChannel().getMembers())
			{
				teleportPlayer(partyMember, coords, instanceId);
				world.addAllowed(partyMember.getObjectId());
				if (partyMember.getQuestState(Q00696_ConquerTheHallOfErosion.class.getSimpleName()) == null)
				{
					newQuestState(partyMember);
				}
			}
		}
		runTumors((HEWorld) world);
		
		return instanceId;
	}
	
	private void runTumors(HEWorld world)
	{
		for (int[] spawn : ROOMS_MOBS_SPAWN)
		{
			for (int i = 0; i < spawn[6]; i++)
			{
				L2Npc npc = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.getInstanceId());
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
			}
		}
		
		for (int[] spawn : ROOMS_TUMORS_SPAWN)
		{
			for (int i = 0; i < spawn[6]; i++)
			{
				L2Npc npc = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.getInstanceId());
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
		broadCastPacket(world, new ExShowScreenMessage(NpcStringId.YOU_CAN_HEAR_THE_UNDEAD_OF_EKIMUS_RUSHING_TOWARD_YOU_S1_S2_IT_HAS_NOW_BEGUN, 2, 8000));
	}
	
	private void stopTumors(HEWorld world)
	{
		if (!world.npcList.isEmpty())
		{
			for (L2Npc npc : world.npcList)
			{
				if (npc != null)
				{
					npc.getSpawn().stopRespawn();
					npc.deleteMe();
				}
			}
		}
		world.npcList.clear();
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if (tmpworld instanceof HEWorld)
		{
			HEWorld world = (HEWorld) tmpworld;
			
			if (event.startsWith("warp"))
			{
				L2Npc victim = null;
				victim = world.deadTumor;
				if (victim != null)
				{
					world.deadTumors.add(victim);
				}
				
				player.destroyItemByItemId("SOI", TEAR_OF_A_FREED_SOUL_ITEM_ID, 1, player, true);
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
		QuestState st = player.getQuestState(Q00696_ConquerTheHallOfErosion.class.getSimpleName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		if (npc.getId() == MOUTH_OF_EKIMUS)
		{
			enterInstance(player, "HallOfErosionAttack.xml", ENTER_TELEPORT);
			return null;
		}
		return "";
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon, Skill skill)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof HEWorld)
		{
			final HEWorld world = (HEWorld) tmpworld;
			if (!world.isBossAttacked)
			{
				world.isBossAttacked = true;
				Calendar reenter = Calendar.getInstance();
				reenter.add(Calendar.HOUR, INSTANCE_PENALTY);
				
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_FROM_HERE_S1_S_ENTRY_HAS_BEEN_RESTRICTED);
				sm.addInstanceName(tmpworld.getTemplateId());
				
				for (int objectId : tmpworld.getAllowed())
				{
					L2PcInstance player = L2World.getInstance().getPlayer(objectId);
					if ((player != null) && player.isOnline())
					{
						InstanceManager.getInstance().setInstanceTime(objectId, tmpworld.getTemplateId(), reenter.getTimeInMillis());
						player.sendPacket(sm);
					}
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		if (Util.contains(NOT_MOVE, npc.getId()))
		{
			npc.setIsNoRndWalk(true);
			npc.setIsImmobilized(true);
		}
		
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof HEWorld)
		{
			HEWorld world = (HEWorld) tmpworld;
			if (npc.getId() == TUMOR_OF_DEATH)
			{
				world.addTumorCount(1);
				if ((world.tumorCount == 4) && (world.cohemenes != null))
				{
					world.cohemenes.deleteMe();
					broadCastPacket(world, new ExShowScreenMessage(NpcStringId.YOU_HAVE_FAILED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE, 2, 8000));
					finishInstance(world);
					conquestEnded = true;
					stopTumors(world);
				}
			}
			
			if (npc.getId() == DESTROYED_TUMOR)
			{
				world.addTag(1);
				
				for (L2PcInstance players : L2World.getInstance().getPlayers())
				{
					if (Util.checkIfInRange(Rnd.get(300, 500), npc, players, true) && !players.isAffectedBySkill(ENERGY_OF_GENERATION_SKILL_ID))
					{
						npc.setTarget(players);
						npc.doCast(SkillData.getInstance().getSkill(ENERGY_OF_GENERATION_SKILL_ID, Rnd.get(1, 12)));
					}
				}
			}
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof HEWorld)
		{
			HEWorld world = (HEWorld) tmpworld;
			Location loc = npc.getLocation();
			if (npc.getId() == TUMOR_OF_DEATH)
			{
				world.addTumorCount(-1);
				((L2MonsterInstance) npc).dropItem(player, TEAR_OF_A_FREED_SOUL_ITEM_ID, Rnd.get(2, 5));
				npc.deleteMe();
				world.deadTumor = addSpawn(DESTROYED_TUMOR, loc.getX(), loc.getY(), loc.getZ(), 0, false, 0, false, world.getInstanceId());
				world.deadTumors.add(world.deadTumor);
				ThreadPoolManager.getInstance().scheduleGeneral(new TumorRevival(world.deadTumor, world), tumorRespawnTime);
				ThreadPoolManager.getInstance().scheduleGeneral(new RegenerationCoffinSpawn(world.deadTumor, world), 20000);
				if (world.tumorCount >= 1)
				{
					broadCastPacket(world, new ExShowScreenMessage(NpcStringId.THE_TUMOR_INSIDE_S1_HAS_BEEN_DESTROYED_NIN_ORDER_TO_DRAW_OUT_THE_COWARDLY_COHEMENES_YOU_MUST_DESTROY_ALL_THE_TUMORS, 2, 8000));
				}
				
				if ((world.tumorCount == 0) && (world.cohemenes == null))
				{
					broadCastPacket(world, new ExShowScreenMessage(NpcStringId.ALL_THE_TUMORS_INSIDE_S1_HAVE_BEEN_DESTROYED_DRIVEN_INTO_A_CORNER_COHEMENES_APPEARS_CLOSE_BY, 2, 8000));
					int[] spawn = COHEMENES_SPAWN[Rnd.get(0, COHEMENES_SPAWN.length - 1)];
					L2Npc n = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.getInstanceId());
					n.broadcastPacket(new NpcSay(n.getObjectId(), Say2.SHOUT, n.getId(), NpcStringId.CMON_CMON_SHOW_YOUR_FACE_YOU_LITTLE_RATS_LET_ME_SEE_WHAT_THE_DOOMED_WEAKLINGS_ARE_SCHEMING));
					world.cohemenes = n;
				}
			}
			if (npc.getId() == COHEMENES)
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.SHOUT, npc.getId(), NpcStringId.KEU_I_WILL_LEAVE_FOR_NOW_BUT_DONT_THINK_THIS_IS_OVER_THE_SEED_OF_INFINITY_CAN_NEVER_DIE));
				for (int objId : world.getAllowed())
				{
					L2PcInstance pl = L2World.getInstance().getPlayer(objId);
					QuestState st = pl.getQuestState(Q00696_ConquerTheHallOfErosion.class.getSimpleName());
					if ((st != null) && (st.getInt("cond") == 1))
					{
						st.set("cohemenes", "1");
					}
				}
				broadCastPacket(world, new ExShowScreenMessage(NpcStringId.CONGRATULATIONS_YOU_HAVE_SUCCEEDED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE, 2, 8000));
				world.cohemenes = null;
				conquestEnded = true;
				finishInstance(world);
				stopTumors(world);
				SeedOfInfinityManager.notifyCohemenesKill();
			}
			
			if (npc.getId() == SOUL_COFFIN)
			{
				tumorRespawnTime += 10 * 1000;
			}
		}
		return "";
	}
	
	private void finishInstance(InstanceWorld world)
	{
		if (world instanceof HEWorld)
		{
			Calendar reenter = Calendar.getInstance();
			reenter.set(Calendar.MINUTE, 30);
			
			if (reenter.get(Calendar.HOUR_OF_DAY) >= 6)
			{
				reenter.add(Calendar.DATE, 1);
			}
			reenter.set(Calendar.HOUR_OF_DAY, 6);
			
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_FROM_HERE_S1_S_ENTRY_HAS_BEEN_RESTRICTED);
			sm.addInstanceName(world.getTemplateId());
			
			for (int objectId : world.getAllowed())
			{
				L2PcInstance obj = L2World.getInstance().getPlayer(objectId);
				if ((obj != null) && obj.isOnline())
				{
					InstanceManager.getInstance().setInstanceTime(objectId, world.getTemplateId(), reenter.getTimeInMillis());
					obj.sendPacket(sm);
				}
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.getInstanceId());
			inst.setDuration(5 * 60000);
			inst.setEmptyDestroyTime(0);
		}
	}
	
	private class TumorRevival implements Runnable
	{
		private final L2Npc _deadTumor;
		private final HEWorld _world;
		
		protected TumorRevival(L2Npc deadTumor, HEWorld world)
		{
			_deadTumor = world.deadTumor;
			_world = world;
		}
		
		@Override
		public void run()
		{
			if (conquestEnded)
			{
				return;
			}
			
			L2Npc tumor = addSpawn(TUMOR_OF_DEATH, _deadTumor.getLocation().getX(), _deadTumor.getLocation().getY(), _deadTumor.getLocation().getZ(), 0, false, 0, false, _world.getInstanceId());
			_world.npcList.add(tumor);
			_deadTumor.deleteMe();
			_world.addTag(-1);
		}
	}
	
	private class RegenerationCoffinSpawn implements Runnable
	{
		private final L2Npc _deadTumor;
		private final HEWorld _world;
		
		protected RegenerationCoffinSpawn(L2Npc deadTumor, HEWorld world)
		{
			_deadTumor = world.deadTumor;
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
				L2Npc soulCoffin = addSpawn(SOUL_COFFIN, _deadTumor.getLocation().getX(), _deadTumor.getLocation().getY(), _deadTumor.getLocation().getZ(), 0, true, 0, false, _world.getInstanceId());
				_world.npcList.add(soulCoffin);
			}
		}
	}
	
	private void broadCastPacket(HEWorld world, L2GameServerPacket packet)
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