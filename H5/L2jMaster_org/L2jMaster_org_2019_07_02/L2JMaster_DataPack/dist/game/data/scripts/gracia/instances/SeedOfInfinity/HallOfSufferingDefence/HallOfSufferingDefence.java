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
package gracia.instances.SeedOfInfinity.HallOfSufferingDefence;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.l2jserver.gameserver.ai.CtrlEvent;
import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.L2Party;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Attackable;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.effects.L2EffectType;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.NpcStringId;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.gameserver.util.Util;
import com.l2jserver.util.Rnd;

import ai.npc.AbstractNpcAI;
import quests.Q00695_DefendTheHallOfSuffering.Q00695_DefendTheHallOfSuffering;

/**
 * Seed of Infinity (Hall of Suffering Defense) instance zone.
 * @author MaGa1, Sacrifice
 */
public final class HallOfSufferingDefence extends AbstractNpcAI
{
	protected class DHSWorld extends InstanceWorld
	{
		protected Map<L2Npc, Boolean> npcList = new HashMap<>();
		protected List<L2Npc> tumor = new ArrayList<>();
		protected int tumorIndex = 300;
		protected int tumorcount = 0;
		protected L2Npc klodekus = null;
		protected L2Npc klanikus = null;
		protected boolean isBossesAttacked = false;
		protected long[] storeTime =
		{
			0,
			0
		};
		
		protected synchronized void TumorIndex(int value)
		{
			tumorIndex += value;
		}
		
		protected synchronized void TumorCount(int value)
		{
			tumorcount += value;
		}
		
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
		
		protected DHSWorld()
		{
			tag = -1;
		}
	}
	
	private static final int INSTANCE_ID = 116;
	
	private static final int TUMOR_OF_DEATH = 18704;
	private static final int DESTROYED_TUMOR = 18705;
	
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
	private static final int MOUTH_OF_EKIMUS = 32537;
	
	protected int tumorIndex = 300;
	private boolean doCountCoffinNotifications = false;
	
	//@formatter:off
	private static final int[][] ROOM_1_MOBS_SPAWN =
	{
		{22509, -173712, 217838, -9559}, {22509, -173489, 218281, -9557},
		{22509, -173824, 218389, -9558}, {22510, -174018, 217970, -9559},
		{22510, -173382, 218198, -9547}
	};
	
	private static final int[][] ROOM_2_MOBS_SPAWN =
	{
		{22511, -173456, 217976, -9556}, {22511, -173673, 217951, -9547},
		{22509, -173622, 218233, -9547}, {22510, -173775, 218218, -9545},
		{22510, -173660, 217980, -9542}, {22510, -173712, 217838, -9559}
	};
	
	private static final int[][] ROOM_3_MOBS_SPAWN =
	{
		{22512, -173489, 218281, -9557}, {22512, -173824, 218389, -9558},
		{22512, -174018, 217970, -9559}, {22509, -173382, 218198, -9547},
		{22511, -173456, 217976, -9556}, {22511, -173673, 217951, -9547},
		{22510, -173622, 218233, -9547}, {22510, -173775, 218218, -9545}
	};
	
	private static final int[][] ROOM_4_MOBS_SPAWN =
	{
		{22514, -173660, 217980, -9542}, {22514, -173712, 217838, -9559},
		{22514, -173489, 218281, -9557}, {22513, -173824, 218389, -9558},
		{22513, -174018, 217970, -9559}, {22511, -173382, 218198, -9547},
		{22511, -173456, 217976, -9556}, {22512, -173673, 217951, -9547},
		{22512, -173622, 218233, -9547}
	};
	
	private static final int[][] ROOM_5_MOBS_SPAWN =
	{
		{22512, -173775, 218218, -9545}, {22512, -173660, 217980, -9542},
		{22512, -173712, 217838, -9559}, {22513, -173489, 218281, -9557},
		{22513, -173824, 218389, -9558}, {22514, -174018, 217970, -9559},
		{22514, -173382, 218198, -9547}, {22514, -173456, 217976, -9556},
		{22515, -173673, 217951, -9547}, {22515, -173622, 218233, -9547}
	};
	
	private static final int[][] TUMOR_SPAWNS =
	{
		{-173727, 218109, -9536}, {-173727, 218109, -9536},
		{-173727, 218109, -9536}, {-173727, 218109, -9536},
		{-173727, 218109, -9536}
	};
	
	private static final int[][] TWIN_SPAWNS = {{25665, -173727, 218169, -9536}, {25666, -173727, 218049, -9536}};
	
	private static final int[] TEPIOS_SPAWN = {-173695, 218052, -9538};
	// @formatter:on
	
	private static final int BOSS_INVUL_TIME = 30000;
	private static final int BOSS_MINION_SPAWN_TIME = 60000;
	private static final int BOSS_RESSURECT_TIME = 20000;
	
	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;
	
	public HallOfSufferingDefence()
	{
		super(HallOfSufferingDefence.class.getSimpleName(), "gracia/instances");
		
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
	
	protected class teleCoord
	{
		int instanceId;
		int x;
		int y;
		int z;
	}
	
	private boolean checkConditions(L2PcInstance player)
	{
		L2Party party = player.getParty();
		if (party == null)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_IN_PARTY_CANT_ENTER));
			return false;
		}
		if (party.getLeader() != player)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER));
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
			
			QuestState st = partyMember.getQuestState(Q00695_DefendTheHallOfSuffering.class.getSimpleName());
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
	
	private void teleportplayer(L2PcInstance player, teleCoord teleto)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(teleto.instanceId);
		player.teleToLocation(teleto.x, teleto.y, teleto.z);
		return;
	}
	
	private int enterInstance(L2PcInstance player, String template, teleCoord teleto)
	{
		int instanceId = 0;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		
		if (world != null)
		{
			if (!(world instanceof DHSWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ENTERED_ANOTHER_INSTANT_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON));
				return 0;
			}
			teleto.instanceId = world.getInstanceId();
			teleportplayer(player, teleto);
			return instanceId;
		}
		
		if (!checkConditions(player))
		{
			return 0;
		}
		
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new DHSWorld();
		world.setTemplateId(INSTANCE_ID);
		world.setInstanceId(instanceId);
		world.setStatus(0);
		((DHSWorld) world).storeTime[0] = System.currentTimeMillis();
		InstanceManager.getInstance().addWorld(world);
		runTumors((DHSWorld) world);
		teleto.instanceId = instanceId;
		
		if (player.getParty() == null)
		{
			teleportplayer(player, teleto);
			world.addAllowed(player.getObjectId());
		}
		else
		{
			for (L2PcInstance partyMember : player.getParty().getMembers())
			{
				teleportplayer(partyMember, teleto);
				world.addAllowed(partyMember.getObjectId());
			}
		}
		return instanceId;
	}
	
	protected void exitInstance(L2PcInstance player, teleCoord tele)
	{
		player.setInstanceId(0);
		player.teleToLocation(tele.x, tele.y, tele.z);
		L2Summon pet = player.getSummon();
		if (pet != null)
		{
			pet.setInstanceId(0);
			pet.teleToLocation(tele.x, tele.y, tele.z);
		}
	}
	
	private boolean checkKillProgress(L2Npc mob, DHSWorld world)
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
	
	private void runTumors(DHSWorld world)
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
	
	private void runTwins(DHSWorld world)
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
		if (tmpworld instanceof DHSWorld)
		{
			DHSWorld world = (DHSWorld) tmpworld;
			if (event.equalsIgnoreCase("spawnBossGuards"))
			{
				L2Npc mob = addSpawn(TWIN_MOB_IDS[Rnd.get(TWIN_MOB_IDS.length)], TWIN_SPAWNS[0][1], TWIN_SPAWNS[0][2], TWIN_SPAWNS[0][3], 0, false, 0, false, npc.getInstanceId());
				((L2Attackable) mob).addDamageHate(((L2Attackable) npc).getMostHated(), 0, 1);
				if (Rnd.get(100) < 33)
				{
					mob = addSpawn(TWIN_MOB_IDS[Rnd.get(TWIN_MOB_IDS.length)], TWIN_SPAWNS[1][1], TWIN_SPAWNS[1][2], TWIN_SPAWNS[1][3], 0, false, 0, false, npc.getInstanceId());
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
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(Q00695_DefendTheHallOfSuffering.class.getSimpleName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (npc.getId() == MOUTH_OF_EKIMUS)
		{
			teleCoord tele = new teleCoord();
			tele.x = -174701;
			tele.y = 218109;
			tele.z = -9592;
			enterInstance(player, "HallOfSufferingDefence.xml", tele);
			return null;
		}
		return "";
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
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon, Skill skill)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof DHSWorld)
		{
			if (!((DHSWorld) tmpworld).isBossesAttacked)
			{
				((DHSWorld) tmpworld).isBossesAttacked = true;
				Calendar reenter = Calendar.getInstance();
				reenter.set(Calendar.MINUTE, RESET_MIN);
				
				if (reenter.get(Calendar.HOUR_OF_DAY) >= RESET_HOUR)
				{
					reenter.add(Calendar.DATE, 1);
				}
				reenter.set(Calendar.HOUR_OF_DAY, RESET_HOUR);
				
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_FROM_HERE_S1_S_ENTRY_HAS_BEEN_RESTRICTED);
				sm.addString(InstanceManager.getInstance().getInstanceIdName(tmpworld.getTemplateId()));
				
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
				if (((DHSWorld) tmpworld).klanikus.isDead())
				{
					((DHSWorld) tmpworld).klanikus.setIsDead(false);
					((DHSWorld) tmpworld).klanikus.doDie(attacker);
					((DHSWorld) tmpworld).klodekus.doDie(attacker);
				}
				else if (((DHSWorld) tmpworld).klodekus.isDead())
				{
					((DHSWorld) tmpworld).klodekus.setIsDead(false);
					((DHSWorld) tmpworld).klodekus.doDie(attacker);
					((DHSWorld) tmpworld).klanikus.doDie(attacker);
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
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof DHSWorld)
		{
			Location loc = npc.getLocation();
			DHSWorld world = (DHSWorld) tmpworld;
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
					doCountCoffinNotifications = false;
				}
			}
			
			if (npc.getId() == TUMOR_OF_DEATH)
			{
				world.TumorIndex(-50);
				world.TumorCount(1);
				doCountCoffinNotifications = true;
				notifyCoffinActivity(npc, world);
				if (world.tumorcount == 5)
				{
					npc.deleteMe();
					L2Npc deadTumor = addSpawn(DESTROYED_TUMOR, loc.getX(), loc.getY(), loc.getZ(), 0, false, 0, false, world.getInstanceId());
					world.tumor.add(deadTumor);
				}
			}
		}
		return "";
	}
	
	private void notifyCoffinActivity(L2Npc npc, DHSWorld world)
	{
		if (!doCountCoffinNotifications)
		{
			return;
		}
		
		if (world.tumorIndex == 200)
		{
			broadCastPacket(world, new ExShowScreenMessage(NpcStringId.THE_AREA_NEAR_THE_TUMOR_IS_FULL_OF_OMINOUS_ENERGY, 2, 8000));
		}
		else if (world.tumorIndex == 100)
		{
			broadCastPacket(world, new ExShowScreenMessage(NpcStringId.YOU_CAN_FEEL_THE_SURGING_ENERGY_OF_DEATH_FROM_THE_TUMOR, 2, 8000));
		}
		if (world.tumorIndex <= 0)
		{
			Location loc = npc.getLocation();
			
			for (L2Npc npcs : world.tumor)
			{
				if (npcs != null)
				{
					npcs.deleteMe();
				}
			}
			L2Npc aliveTumor = addSpawn(TUMOR_OF_DEATH, loc.getX(), loc.getY(), loc.getZ(), 0, false, 0, false, world.getInstanceId());
			world.npcList.put(aliveTumor, false);
			doCountCoffinNotifications = false;
		}
	}
	
	private void broadCastPacket(DHSWorld world, L2GameServerPacket packet)
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
