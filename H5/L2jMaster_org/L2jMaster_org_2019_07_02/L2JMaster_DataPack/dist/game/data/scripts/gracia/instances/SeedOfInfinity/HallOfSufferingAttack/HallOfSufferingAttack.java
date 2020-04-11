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
package gracia.instances.SeedOfInfinity.HallOfSufferingAttack;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.l2jserver.gameserver.ai.CtrlEvent;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.L2Party;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.L2Attackable;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.effects.L2EffectType;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.gameserver.util.Util;

import ai.npc.AbstractNpcAI;
import quests.Q00694_BreakThroughTheHallOfSuffering.Q00694_BreakThroughTheHallOfSuffering;

/**
 * Seed of Infinity (Hall of Suffering Attack) instance zone.
 * @author MaGa1, Sacrifice
 */
public final class HallOfSufferingAttack extends AbstractNpcAI
{
	protected class HSWorld extends InstanceWorld
	{
		protected Map<L2Npc, Boolean> npcList = new HashMap<>();
		protected L2Npc klodekus = null;
		protected L2Npc klanikus = null;
		protected boolean isBossesAttacked = false;
		protected long[] storeTime =
		{
			0,
			0
		};
		
		protected void calcRewardItemId()
		{
			Long finishDiff = storeTime[1] - storeTime[0];
			if (finishDiff < 1200000)
			{
				tag = 13777;
			}
			else if (finishDiff <= 1260000)
			{
				tag = 13778;
			}
			else if (finishDiff <= 1320000)
			{
				tag = 13779;
			}
			else if (finishDiff <= 1380000)
			{
				tag = 13780;
			}
			else if (finishDiff <= 1440000)
			{
				tag = 13781;
			}
			else if (finishDiff <= 1500000)
			{
				tag = 13782;
			}
			else if (finishDiff <= 1560000)
			{
				tag = 13783;
			}
			else if (finishDiff <= 1620000)
			{
				tag = 13784;
			}
			else if (finishDiff <= 1680000)
			{
				tag = 13785;
			}
			else
			{
				tag = 13786;
			}
		}
		
		protected HSWorld()
		{
			tag = -1;
		}
	}
	
	private static final int INSTANCE_PENALTY = 24;
	private static final int INSTANCE_ID = 115;
	
	//@formatter:off
	private static final int[] ENTER_TELEPORT = {-187567, 205570, -9538};
	//@formatter:on
	
	private static final int TUMOR_OF_DEATH = 18704;
	
	private static final int[] TUMOR_MOB_IDS =
	{
		22509, // Fanatic of Infinity
		22510, // Rotten Messenger
		22511, // Zealot of Infinity
		22512, // Body Severer
		22513, // Body Harvester
		22514, // Soul Exploiter
		22515 // Soul Devourer
	};
	
	private static final int[] TWIN_MOB_IDS =
	{
		22509, // Fanatic of Infinity
		22510, // Rotten Messenger
		22511, // Zealot of Infinity
		22512, // Body Severer
		22513 // Body Harvester
	};
	
	private static final int YEHAN_KLODEKUS = 25665;
	private static final int YEHAN_KLANIKUS = 25666;
	private static final int TEPIOS = 32530;
	private static final int DESTROYED_TUMOR = 32531;
	private static final int MOUTH_OF_EKIMUS = 32537;
	
	//@formatter:off
	private static final int[][] ROOM_1_MOBS_SPAWN =
	{
		{22509, -186296, 208200, -9544}, {22509, -186161, 208345, -9544},
		{22509, -186296, 208403, -9544}, {22510, -186107, 208113, -9528},
		{22510, -186350, 208200, -9544}
	};
	
	private static final int[][] ROOM_2_MOBS_SPAWN =
	{
		{22511, -184433, 210953, -9536}, {22511, -184406, 211301, -9536},
		{22509, -184541, 211272, -9544}, {22510, -184244, 211098, -9536},
		{22510, -184352, 211243, -9536}, {22510, -184298, 211330, -9528}
	};
	
	private static final int[][] ROOM_3_MOBS_SPAWN =
	{
		{22512, -182611, 213984, -9520}, {22512, -182908, 214071, -9520},
		{22512, -182962, 213868, -9512}, {22509, -182881, 213955, -9512},
		{22511, -182827, 213781, -9504}, {22511, -182530, 213984, -9528},
		{22510, -182935, 213723, -9512}, {22510, -182557, 213868, -9520}
	};
	
	private static final int[][] ROOM_4_MOBS_SPAWN =
	{
		{22514, -180958, 216860, -9544}, {22514, -181012, 216628, -9536},
		{22514, -181120, 216715, -9536}, {22513, -180661, 216599, -9536},
		{22513, -181039, 216599, -9536}, {22511, -180715, 216599, -9536},
		{22511, -181012, 216889, -9536}, {22512, -180931, 216918, -9536},
		{22512, -180742, 216628, -9536}
	};
	
	private static final int[][] ROOM_5_MOBS_SPAWN =
	{
		{22512, -177372, 217854, -9536}, {22512, -177237, 218140, -9536},
		{22512, -177021, 217647, -9528}, {22513, -177372, 217792, -9544},
		{22513, -177372, 218053, -9536}, {22514, -177291, 217734, -9544},
		{22514, -177264, 217792, -9544}, {22514, -177264, 218053, -9536},
		{22515, -177156, 217792, -9536}, {22515, -177075, 217647, -9528}
	};
	
	private static final int[][] TUMOR_SPAWNS =
	{
		{-186327, 208286, -9544}, {-184429, 211155, -9544},
		{-182811, 213871, -9496}, {-181039, 216633, -9528},
		{-177264, 217760, -9544}
	};
	
	private static final int[][] TWIN_SPAWNS = {{25665, -173727, 218169, -9536}, {25666, -173727, 218049, -9536}};
	
	private static final int[] TEPIOS_SPAWN = {-173727, 218109, -9536};
	//@formatter:on
	
	private static final int BOSS_INVUL_TIME = 30000;
	private static final int BOSS_MINION_SPAWN_TIME = 60000;
	private static final int BOSS_RESSURECT_TIME = 20000;
	
	public HallOfSufferingAttack()
	{
		super(HallOfSufferingAttack.class.getSimpleName(), "gracia/instances");
		
		addStartNpc(MOUTH_OF_EKIMUS, TEPIOS);
		addTalkId(MOUTH_OF_EKIMUS, TEPIOS);
		addAttackId(YEHAN_KLODEKUS, YEHAN_KLANIKUS);
		addKillId(TUMOR_OF_DEATH, YEHAN_KLODEKUS, YEHAN_KLANIKUS);
		
		for (int mobId : TUMOR_MOB_IDS)
		{
			addSkillSeeId(mobId);
			addKillId(mobId);
		}
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
		for (L2PcInstance partyMember : party.getMembers())
		{
			if ((partyMember.getLevel() < 75) || (partyMember.getLevel() > 82))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
			
			if (!Util.checkIfInRange(1000, player, partyMember, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_LOCATION_WHICH_CANNOT_BE_ENTERED_THEREFORE_IT_CANNOT_BE_PROCESSED);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
			
			Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), INSTANCE_ID);
			if (System.currentTimeMillis() < reentertime)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_RE_ENTER_YET);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
			
			QuestState st = partyMember.getQuestState(Q00694_BreakThroughTheHallOfSuffering.class.getSimpleName());
			if ((st == null) || (st.getInt("cond") != 1))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
		}
		return true;
	}
	
	private void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2]);
	}
	
	private int enterInstance(L2PcInstance player, String template, int[] coords)
	{
		int instanceId = 0;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		
		if (world != null)
		{
			if (!(world instanceof HSWorld))
			{
				player.sendPacket(SystemMessageId.YOU_HAVE_ENTERED_ANOTHER_INSTANT_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON);
				return 0;
			}
			teleportPlayer(player, coords, world.getInstanceId());
			return world.getInstanceId();
		}
		
		if (!checkConditions(player))
		{
			return 0;
		}
		
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new HSWorld();
		world.setTemplateId(INSTANCE_ID);
		world.setInstanceId(instanceId);
		world.setStatus(0);
		((HSWorld) world).storeTime[0] = System.currentTimeMillis();
		InstanceManager.getInstance().addWorld(world);
		runTumors((HSWorld) world);
		
		if (player.getParty() == null)
		{
			teleportPlayer(player, coords, instanceId);
			world.addAllowed(player.getObjectId());
		}
		else
		{
			for (L2PcInstance partyMember : player.getParty().getMembers())
			{
				teleportPlayer(partyMember, coords, instanceId);
				world.addAllowed(partyMember.getObjectId());
				if (partyMember.getQuestState(Q00694_BreakThroughTheHallOfSuffering.class.getSimpleName()) == null)
				{
					newQuestState(partyMember);
				}
			}
		}
		return instanceId;
	}
	
	private boolean checkKillProgress(L2Npc mob, HSWorld world)
	{
		if (world.npcList.containsKey(mob))
		{
			world.npcList.put(mob, true);
		}
		for (boolean isDead : world.npcList.values())
		{
			if (!isDead)
			{
				return false;
			}
		}
		return true;
	}
	
	private int[][] getRoomSpawns(int room)
	{
		switch (room)
		{
			case 0:
				return ROOM_1_MOBS_SPAWN;
			case 1:
				return ROOM_2_MOBS_SPAWN;
			case 2:
				return ROOM_3_MOBS_SPAWN;
			case 3:
				return ROOM_4_MOBS_SPAWN;
			case 4:
				return ROOM_5_MOBS_SPAWN;
		}
		return new int[][] {};
	}
	
	private void runTumors(HSWorld world)
	{
		for (int[] mob : getRoomSpawns(world.getStatus()))
		{
			L2Npc npc = addSpawn(mob[0], mob[1], mob[2], mob[3], 0, false, 0, false, world.getInstanceId());
			world.npcList.put(npc, false);
		}
		L2Npc mob = addSpawn(TUMOR_OF_DEATH, TUMOR_SPAWNS[world.getStatus()][0], TUMOR_SPAWNS[world.getStatus()][1], TUMOR_SPAWNS[world.getStatus()][2], 0, false, 0, false, world.getInstanceId());
		mob.disableCoreAI(true);
		mob.setIsImmobilized(true);
		mob.setCurrentHp(mob.getMaxHp() * 0.5);
		world.npcList.put(mob, false);
		world.incStatus();
	}
	
	private void runTwins(HSWorld world)
	{
		world.incStatus();
		world.klodekus = addSpawn(TWIN_SPAWNS[0][0], TWIN_SPAWNS[0][1], TWIN_SPAWNS[0][2], TWIN_SPAWNS[0][3], 0, false, 0, false, world.getInstanceId());
		world.klanikus = addSpawn(TWIN_SPAWNS[1][0], TWIN_SPAWNS[1][1], TWIN_SPAWNS[1][2], TWIN_SPAWNS[1][3], 0, false, 0, false, world.getInstanceId());
		world.klanikus.setIsMortal(false);
		world.klodekus.setIsMortal(false);
	}
	
	private void bossSimpleDie(L2Npc boss)
	{
		synchronized (this)
		{
			if (boss.isDead())
			{
				return;
			}
			boss.setCurrentHp(0);
			boss.setIsDead(true);
		}
		boss.setTarget(null);
		boss.stopMove(null);
		boss.getStatus().stopHpMpRegeneration();
		boss.stopAllEffectsExceptThoseThatLastThroughDeath();
		boss.broadcastStatusUpdate();
		boss.getAI().notifyEvent(CtrlEvent.EVT_DEAD);
		
		if (boss.getWorldRegion() != null)
		{
			boss.getWorldRegion().onDeath(boss);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof HSWorld)
		{
			HSWorld world = (HSWorld) tmpworld;
			if (event.equalsIgnoreCase("spawnBossGuards"))
			{
				if (!world.klanikus.isInCombat() && !world.klodekus.isInCombat())
				{
					world.isBossesAttacked = false;
					return "";
				}
				L2Npc mob = addSpawn(TWIN_MOB_IDS[getRandom(TWIN_MOB_IDS.length)], TWIN_SPAWNS[0][1], TWIN_SPAWNS[0][2], TWIN_SPAWNS[0][3], 0, false, 0, false, npc.getInstanceId());
				((L2Attackable) mob).addDamageHate(((L2Attackable) npc).getMostHated(), 0, 1);
				if (getRandom(100) < 33)
				{
					mob = addSpawn(TWIN_MOB_IDS[getRandom(TWIN_MOB_IDS.length)], TWIN_SPAWNS[1][1], TWIN_SPAWNS[1][2], TWIN_SPAWNS[1][3], 0, false, 0, false, npc.getInstanceId());
					((L2Attackable) mob).addDamageHate(((L2Attackable) npc).getMostHated(), 0, 1);
				}
				startQuestTimer("spawnBossGuards", BOSS_MINION_SPAWN_TIME, npc, null);
			}
			else if (event.equalsIgnoreCase("isTwinSeparated"))
			{
				if (Util.checkIfInRange(500, world.klanikus, world.klodekus, false))
				{
					world.klanikus.setIsInvul(false);
					world.klodekus.setIsInvul(false);
				}
				else
				{
					world.klanikus.setIsInvul(true);
					world.klodekus.setIsInvul(true);
				}
				startQuestTimer("isTwinSeparated", 10000, npc, null);
			}
			else if (event.equalsIgnoreCase("ressurectTwin"))
			{
				Skill skill = SkillData.getInstance().getSkill(5824, 1); // Presentation - District1 Boss Arise lvl.1
				L2Npc aliveTwin = (world.klanikus == npc ? world.klodekus : world.klanikus);
				npc.doRevive();
				npc.doCast(skill);
				npc.setCurrentHp(aliveTwin.getCurrentHp());
				
				L2Character hated = ((L2MonsterInstance) aliveTwin).getMostHated();
				if (hated != null)
				{
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, hated, 1000);
				}
				
				aliveTwin.setIsInvul(true);
				startQuestTimer("uninvul", BOSS_INVUL_TIME, aliveTwin, null);
			}
			else if (event.equals("uninvul"))
			{
				npc.setIsInvul(false);
			}
		}
		return "";
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(Q00694_BreakThroughTheHallOfSuffering.class.getSimpleName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		if (npc.getId() == MOUTH_OF_EKIMUS)
		{
			enterInstance(player, "HallOfSufferingAttack.xml", ENTER_TELEPORT);
			return null;
		}
		return "";
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon, Skill skill)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof HSWorld)
		{
			final HSWorld world = (HSWorld) tmpworld;
			if (!world.isBossesAttacked)
			{
				world.isBossesAttacked = true;
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
				startQuestTimer("spawnBossGuards", BOSS_MINION_SPAWN_TIME, npc, null);
				startQuestTimer("isTwinSeparated", 10000, npc, null);
			}
			else if (damage >= npc.getCurrentHp())
			{
				if (world.klanikus.isDead())
				{
					world.klanikus.setIsDead(false);
					world.klanikus.doDie(attacker);
					world.klodekus.doDie(attacker);
				}
				else if (((HSWorld) tmpworld).klodekus.isDead())
				{
					world.klodekus.setIsDead(false);
					world.klodekus.doDie(attacker);
					world.klanikus.doDie(attacker);
				}
				else
				{
					bossSimpleDie(npc);
					startQuestTimer("ressurectTwin", BOSS_RESSURECT_TIME, npc, null);
				}
			}
		}
		return null;
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, Skill skill, L2Object[] targets, boolean isSummon)
	{
		if (skill.hasEffectType(L2EffectType.REBALANCE_HP, L2EffectType.HP))
		{
			int hate = 2 * skill.getEffectPoint();
			if (hate < 2)
			{
				hate = 1000;
			}
			((L2Attackable) npc).addDamageHate(caster, 0, hate);
		}
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof HSWorld)
		{
			HSWorld world = (HSWorld) tmpworld;
			
			if (npc.getId() == TUMOR_OF_DEATH)
			{
				npc.deleteMe();
				addSpawn(DESTROYED_TUMOR, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, false, npc.getInstanceId());
			}
			if (world.getStatus() < 5)
			{
				if (checkKillProgress(npc, world))
				{
					runTumors(world);
				}
			}
			else if (world.getStatus() == 5)
			{
				if (checkKillProgress(npc, world))
				{
					runTwins(world);
				}
			}
			else if ((world.getStatus() == 6) && ((npc.getId() == YEHAN_KLODEKUS) || (npc.getId() == YEHAN_KLANIKUS)))
			{
				if (world.klanikus.isDead() && world.klodekus.isDead())
				{
					world.incStatus();
					world.storeTime[1] = System.currentTimeMillis();
					world.calcRewardItemId();
					world.klanikus = null;
					world.klodekus = null;
					this.cancelQuestTimers("ressurectTwin");
					this.cancelQuestTimers("spawnBossGuards");
					this.cancelQuestTimers("isTwinSeparated");
					addSpawn(TEPIOS, TEPIOS_SPAWN[0], TEPIOS_SPAWN[1], TEPIOS_SPAWN[2], 0, false, 0, false, world.getInstanceId());
				}
			}
		}
		return "";
	}
}