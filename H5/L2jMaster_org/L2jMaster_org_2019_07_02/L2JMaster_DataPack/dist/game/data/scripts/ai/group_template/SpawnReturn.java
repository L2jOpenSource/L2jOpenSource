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
package ai.group_template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.Config;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.data.xml.impl.NpcData;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.model.quest.QuestTimer;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.serverpackets.MagicSkillUse;

import ai.npc.AbstractNpcAI;

/**
 * @author WhiteDev, Sacrifice
 */
public final class SpawnReturn extends AbstractNpcAI
{
	private static final Logger LOG = LoggerFactory.getLogger(SpawnReturn.class);
	
	private static final int MONSTER_RADIUS = 3000;
	private static final int RAIDBOSS_RADIUS = 1200;
	private static final int LEADER_TO_MINION_RADIUS = 1200;
	
	private static final int NPC_DISPEL_FIGHTER_BUFF = 4671;
	
	private SpawnReturn()
	{
		super(SpawnReturn.class.getSimpleName(), "ai/group_template");
		searchAllMonsters();
		searchAllRaidBoss();
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("checkDistance") && (npc instanceof L2RaidBossInstance))
		{
			if (!npc.isInsideRadius(npc.getSpawn().getLocation(), RAIDBOSS_RADIUS, true, false))
			{
				if (Config.SPAWN_RETURN_TELEPORT_RAID)
				{
					teleportToSpawn(npc, player);
				}
				else
				{
					returnToSpawn(npc);
				}
			}
			else
			{
				checkAndStart(npc, player);
			}
		}
		else if ((npc instanceof L2MonsterInstance) && (((L2MonsterInstance) npc).getLeader() != null) && ((L2MonsterInstance) npc).getLeader().isRaid())
		{
			final L2RaidBossInstance raid = (L2RaidBossInstance) ((L2MonsterInstance) npc).getLeader();
			if (!npc.isInsideRadius(raid.getSpawn().getLocation(), LEADER_TO_MINION_RADIUS, true, false))
			{
				if (Config.SPAWN_RETURN_TELEPORT_RAID)
				{
					teleportToSpawn(npc, player);
				}
				else
				{
					returnToSpawn(npc);
				}
			}
			else
			{
				checkAndStart(npc, player);
			}
		}
		else if ((npc instanceof L2MonsterInstance) && (((L2MonsterInstance) npc).getLeader() != null) && (npc.isMinion() || npc.isRaidMinion()))
		{
			final L2MonsterInstance leader = ((L2MonsterInstance) npc).getLeader();
			if ((npc != leader) && !leader.isDead() && !npc.isInsideRadius(leader.getSpawn().getLocation(leader), LEADER_TO_MINION_RADIUS, true, false))
			{
				if (Config.SPAWN_RETURN_TELEPORT_MONSTER)
				{
					teleportToSpawn(npc, player);
				}
				else
				{
					returnToSpawn(npc);
				}
			}
			else if ((leader == null) || leader.isDead())
			{
				if (!npc.isAttackingNow())
				{
					npc.deleteMe();
				}
				else
				{
					checkAndStart(npc, player);
				}
			}
			else
			{
				checkAndStart(npc, player);
			}
		}
		else if (event.equalsIgnoreCase("checkDistance") && !npc.isDead())
		{
			if ((((L2MonsterInstance) npc).getSpawn() != null) && !npc.isInsideRadius(((L2MonsterInstance) npc).getSpawn().getLocation((npc)), MONSTER_RADIUS, true, false))
			{
				if (Config.SPAWN_RETURN_TELEPORT_MONSTER)
				{
					teleportToSpawn(npc, player);
				}
				else
				{
					returnToSpawn(npc);
				}
			}
			else
			{
				checkAndStart(npc, player);
			}
		}
		
		if (event.equalsIgnoreCase("doTeleport"))
		{
			try
			{
				npc.teleToLocation(npc.getSpawn().getLocation(npc));
				getQuestTimers().clear();
			}
			catch (Exception e)
			{
				LOG.error("{}: Can't teleport to respawn. {}", SpawnReturn.class.getSimpleName(), e.getMessage());
				getQuestTimers().clear();
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if ((npc != null) && (player != null) && !npc.isMinion() && (npc.getInstanceId() == 0))
		{
			checkAndStart(npc, player);
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon, Skill skill)
	{
		if ((npc != null) && (npc.getInstanceId() == 0))
		{
			if (attacker != null)
			{
				if (npc.isMinion())
				{
					if (npc instanceof L2MonsterInstance)
					{
						if ((((L2MonsterInstance) npc).getLeader() != null) && !((L2MonsterInstance) npc).getLeader().isDead())
						{
							checkAndStart(npc, attacker);
						}
						else
						{
							cancelDistanceTimer(npc);
						}
					}
				}
				else if (!npc.isDead())
				{
					checkAndStart(npc, attacker);
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (npc instanceof L2RaidBossInstance)
		{
			final L2RaidBossInstance leader = ((L2RaidBossInstance) npc);
			cancelDistanceTimer(leader);
			raidMinionAction(leader);
		}
		else
		{
			final L2MonsterInstance monster = ((L2MonsterInstance) npc);
			cancelDistanceTimer(monster);
			monsterMinionAction(monster);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	private void raidMinionAction(L2RaidBossInstance leader)
	{
		if (leader.hasMinions())
		{
			for (L2MonsterInstance minion : leader.getMinionList().getSpawnedMinions())
			{
				cancelDistanceTimer(minion);
			}
		}
		if (leader.hasMinions() && (leader.getMinionList().getSpawnedMinions().size() > 0))
		{
			checkRaidMinionTargetHate(leader);
		}
	}
	
	private void monsterMinionAction(L2MonsterInstance monster)
	{
		if ((monster != null) && monster.hasMinions() && (monster.getMinionList().getSpawnedMinions().size() > 0))
		{
			checkMinionTargetHate(monster);
		}
	}
	
	private void checkMinionTargetHate(L2MonsterInstance monster)
	{
		if (monster != null)
		{
			for (L2MonsterInstance minion : monster.getMinionList().getSpawnedMinions())
			{
				cancelDistanceTimer(minion);
				if ((minion.getHateList() != null) && !minion.getHateList().isEmpty())
				{
					ThreadPoolManager.getInstance().scheduleAi(() ->
					{
						checkMinionTargetHate(monster);
					}, 15000);
					return;
				}
			}
			monster.getMinionList().onMasterDie(true);
		}
	}
	
	private void checkRaidMinionTargetHate(L2RaidBossInstance raidLeader)
	{
		if (raidLeader != null)
		{
			for (L2MonsterInstance minion : raidLeader.getMinionList().getSpawnedMinions())
			{
				cancelDistanceTimer(minion);
				if (minion.isAttackingNow())
				{
					ThreadPoolManager.getInstance().scheduleAi(() ->
					{
						checkRaidMinionTargetHate(raidLeader);
					}, 15000);
					return;
				}
			}
			raidLeader.getMinionList().onMasterDie(true);
		}
	}
	
	private void searchAllMonsters()
	{
		for (L2NpcTemplate npc : NpcData.getInstance().getAllNpcOfClassType("L2Monster"))
		{
			if (!Config.SPAWN_RETURN_LIST_EXCLUDED.isEmpty() && Config.SPAWN_RETURN_LIST_EXCLUDED.contains(npc.getId()))
			{
				continue;
			}
			setAttackableAggroRangeEnterId(event -> notifyAggroRangeEnter(event.getNpc(), event.getActiveChar(), event.isSummon()), npc.getId());
			addAttackId(npc.getId());
			addKillId(npc.getId());
		}
	}
	
	private void searchAllRaidBoss()
	{
		for (L2NpcTemplate npc : NpcData.getInstance().getAllNpcOfClassType("L2RaidBoss"))
		{
			addAttackId(npc.getId());
			addKillId(npc.getId());
		}
	}
	
	private void checkAndStart(L2Npc npc, L2PcInstance attacker)
	{
		if ((npc != null) && !((L2MonsterInstance) npc).isDead())
		{
			if ((getQuestTimer("checkDistance", npc, attacker) != null) && (attacker != null))
			{
				if (!getQuestTimer("checkDistance", npc, null).getIsActive())
				{
					startQuestTimer("checkDistance", 3000, npc, null);
				}
			}
			else if ((((npc instanceof L2MonsterInstance) //
				&& (npc.isRaidMinion() || npc.isMinion()) //
				&& (((L2MonsterInstance) npc).getLeader() != null) //
				&& !((L2MonsterInstance) npc).getLeader().isDead())))
			{
				startQuestTimer("checkDistance", 3000, npc, null);
			}
			else if (!npc.isDead() && !npc.isRaidMinion())
			{
				startQuestTimer("checkDistance", 3000, npc, null);
			}
		}
	}
	
	private void checkReturnedToSpawn(L2MonsterInstance mob)
	{
		if ((mob != null) && !mob.isDead())
		{
			if ((mob.getSpawn() != null) && (mob.getSpawn().getLocation() != null))
			{
				if (!mob.isInsideRadius(mob.getSpawn().getLocation(mob), 500, false, false))
				{
					ThreadPoolManager.getInstance().scheduleAi(() ->
					{
						checkReturnedToSpawn(mob);
					}, 4000);
				}
				else
				{
					mob.clearAggroList();
					mob.setTarget(null);
					mob.setWalking();
				}
			}
		}
	}
	
	private void teleportToSpawn(L2Npc npc, L2PcInstance player)
	{
		if ((npc instanceof L2RaidBossInstance))
		{
			if (((L2RaidBossInstance) npc).getMinionList().getSpawnedMinions().size() > 0)
			{
				for (L2MonsterInstance minion : ((L2RaidBossInstance) npc).getMinionList().getSpawnedMinions())
				{
					minion.broadcastPacket(new MagicSkillUse(npc, NPC_DISPEL_FIGHTER_BUFF, 1, 1500, 0));
					minion.teleToLocation(npc.getSpawn().getLocation(npc));
					cancelDistanceTimer(minion);
				}
			}
			npc.abortAttack();
			npc.abortCast();
			npc.broadcastPacket(new MagicSkillUse(npc, NPC_DISPEL_FIGHTER_BUFF, 1, 1000, 0));
			cancelDistanceTimer(npc);
			startQuestTimer("doTeleport", 1000, npc, null);
		}
		else if ((npc instanceof L2MonsterInstance) && ((L2MonsterInstance) npc).isRaidMinion())
		{
			final L2MonsterInstance leader = ((L2MonsterInstance) npc).getLeader();
			if ((leader != null) && (leader instanceof L2RaidBossInstance))
			{
				npc.abortAttack();
				npc.abortCast();
				npc.broadcastPacket(new MagicSkillUse(npc, NPC_DISPEL_FIGHTER_BUFF, 1, 1000, 0));
				npc.teleToLocation(leader.getSpawn().getLocation());
				cancelDistanceTimer(npc);
			}
		}
		else if ((npc instanceof L2MonsterInstance) && ((L2MonsterInstance) npc).isMinion())
		{
			final L2MonsterInstance leader = ((L2MonsterInstance) npc).getLeader();
			if (leader != null)
			{
				npc.abortAttack();
				npc.abortCast();
				npc.broadcastPacket(new MagicSkillUse(npc, NPC_DISPEL_FIGHTER_BUFF, 1, 1000, 0));
				npc.teleToLocation(leader.getSpawn().getLocation());
				cancelDistanceTimer(npc);
			}
		}
		else
		{
			if (((L2MonsterInstance) npc).getMinionList().getSpawnedMinions().size() > 0)
			{
				for (L2MonsterInstance minion : ((L2MonsterInstance) npc).getMinionList().getSpawnedMinions())
				{
					minion.broadcastPacket(new MagicSkillUse(npc, NPC_DISPEL_FIGHTER_BUFF, 1, 1500, 0));
					minion.teleToLocation(npc.getSpawn().getLocation());
					cancelDistanceTimer(minion);
				}
			}
			npc.abortAttack();
			npc.abortCast();
			npc.broadcastPacket(new MagicSkillUse(npc, NPC_DISPEL_FIGHTER_BUFF, 1, 1000, 0));
			startQuestTimer("doTeleport", 1000, npc, null);
			cancelDistanceTimer(npc);
		}
	}
	
	/**
	 * Cancel checkDistance timer if necessary
	 * @param npc
	 */
	private void cancelDistanceTimer(L2Npc npc)
	{
		final QuestTimer questTimer = getQuestTimer("checkDistance", npc, null);
		if (questTimer != null)
		{
			questTimer.cancelAndRemove();
		}
	}
	
	private void returnToSpawn(L2Npc npc)
	{
		if (npc instanceof L2MonsterInstance)
		{
			final L2MonsterInstance mob = ((L2MonsterInstance) npc);
			if (mob.isRaidMinion())
			{
				if (mob.getLeader() instanceof L2RaidBossInstance)
				{
					final L2RaidBossInstance leader = (L2RaidBossInstance) mob.getLeader();
					mob.setCurrentHp(mob.getMaxHp());
					mob.setisReturningToSpawnPoint(true);
					mob.clearAggroList();
					mob.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader.getSpawn().getLocation(leader));
					cancelDistanceTimer(npc);
					checkReturnedToSpawn(mob);
				}
			}
			else if (mob.isMinion())
			{
				final L2MonsterInstance leader = mob.getLeader();
				if (leader != null)
				{
					for (L2MonsterInstance minion : leader.getMinionList().getSpawnedMinions())
					{
						minion.setCurrentHp(leader.getMaxHp());
						minion.setisReturningToSpawnPoint(true);
						minion.abortAttack();
						minion.abortCast();
						minion.clearAggroList();
						minion.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, npc);
						cancelDistanceTimer(npc);
						checkReturnedToSpawn(minion);
					}
					leader.abortAttack();
					leader.abortCast();
					leader.setCurrentHp(mob.getMaxHp());
					leader.setisReturningToSpawnPoint(true);
					leader.clearAggroList();
					leader.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader.getSpawn().getLocation(leader));
					cancelDistanceTimer(npc);
					checkReturnedToSpawn(mob);
				}
			}
			else
			{
				// Leader group
				if ((npc instanceof L2MonsterInstance) && (((L2MonsterInstance) npc).getMinionList().getSpawnedMinions().size() > 0))
				{
					final L2MonsterInstance leader = (L2MonsterInstance) npc;
					leader.setCurrentHp(leader.getMaxHp());
					leader.setisReturningToSpawnPoint(true);
					leader.clearAggroList();
					leader.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, leader.getSpawn().getLocation(leader));
					for (L2MonsterInstance minion : leader.getMinionList().getSpawnedMinions())
					{
						minion.setCurrentHp(leader.getMaxHp());
						minion.setisReturningToSpawnPoint(true);
						minion.abortAttack();
						minion.abortCast();
						minion.clearAggroList();
						minion.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, npc);
						cancelDistanceTimer(minion);
						checkReturnedToSpawn(minion);
					}
					cancelDistanceTimer(npc);
					checkReturnedToSpawn((L2MonsterInstance) npc);
				}
				else
				{
					try
					{
						mob.setCurrentHp(mob.getMaxHp());
						mob.setisReturningToSpawnPoint(true);
						mob.clearAggroList();
						mob.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mob.getSpawn().getLocation(mob));
						cancelDistanceTimer(npc);
						checkReturnedToSpawn(mob);
					}
					catch (Exception e)
					{
						LOG.error("{}: Can't go to respawn. {}", SpawnReturn.class.getSimpleName(), e.getMessage());
					}
				}
			}
			
		}
		else if (npc instanceof L2RaidBossInstance)
		{
			final L2RaidBossInstance raid = (L2RaidBossInstance) npc;
			raid.setCurrentHp(raid.getMaxHp());
			raid.setisReturningToSpawnPoint(true);
			raid.clearAggroList();
			raid.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, raid.getSpawn().getLocation(raid));
			for (L2MonsterInstance minion : raid.getMinionList().getSpawnedMinions())
			{
				minion.abortAttack();
				minion.abortCast();
				minion.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, npc);
				cancelDistanceTimer(minion);
			}
			cancelDistanceTimer(npc);
		}
	}
	
	public static void main(String[] args)
	{
		new SpawnReturn();
	}
}
