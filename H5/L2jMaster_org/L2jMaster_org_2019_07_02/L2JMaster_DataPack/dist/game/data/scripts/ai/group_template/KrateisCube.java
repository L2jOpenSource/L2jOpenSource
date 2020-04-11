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
package ai.group_template;

import com.l2jserver.Config;
import com.l2jserver.gameserver.instancemanager.KrateisCubeManager;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.stat.PcStat;
import com.l2jserver.gameserver.model.actor.status.PcStatus;
import com.l2jserver.gameserver.model.events.EventType;
import com.l2jserver.gameserver.model.events.ListenerRegisterType;
import com.l2jserver.gameserver.model.events.annotations.RegisterEvent;
import com.l2jserver.gameserver.model.events.annotations.RegisterType;
import com.l2jserver.gameserver.model.events.impl.character.OnCreatureKill;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ExPVPMatchCCMyRecord;
import com.l2jserver.gameserver.network.serverpackets.ExPVPMatchCCRecord;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.util.Rnd;

import ai.npc.AbstractNpcAI;

/**
 * Krateis Cube AI.
 * @author U3Games
 */
public final class KrateisCube extends AbstractNpcAI
{
	// Npc Watcher
	private static final int RED_WATCHER = 18601;
	private static final int BLUE_WATCHER = 18602;
	
	// Values
	private static final int DEBUFF_DELAY = 3;
	private static boolean IN_DELAY = false;
	
	// Skills
	private static SkillHolder[] DEBUFF_SKILLS =
	{
		new SkillHolder(1160, 15), // Slow Lv 15
		new SkillHolder(1167, 6), // Poisonous Cloud Lv 6
		new SkillHolder(1164, 19), // Curse Weakness Lv 19
		new SkillHolder(1064, 14), // Silence 14
	};
	
	// Mobs List
	private static final int[] MOBS =
	{
		18579,
		18580,
		18581,
		18582,
		18583,
		18584,
		18585,
		18586,
		18587,
		18588,
		18589,
		18590,
		18591,
		18592,
		18593,
		18594,
		18595,
		18596,
		18597,
		18598,
		18599,
		18600,
		18601,
		18602,
		18603,
		18604,
		18605,
		18606
	};
	
	private KrateisCube()
	{
		super(KrateisCube.class.getSimpleName(), "ai/group_template");
		addFirstTalkId(RED_WATCHER, BLUE_WATCHER);
		addSpawnId(RED_WATCHER, BLUE_WATCHER);
		addSeeCreatureId(RED_WATCHER);
		addKillId(RED_WATCHER);
		addKillId(MOBS);
	}
	
	@RegisterEvent(EventType.ON_CREATURE_KILL)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerKill(OnCreatureKill event)
	{
		L2PcInstance killer = event.getAttacker().getActingPlayer();
		if ((killer != null) && (killer.isPlayer()) && (KrateisCubeManager.checkIsInsided(killer)))
		{
			pointEffect(killer, true);
		}
		
		return;
	}
	
	@RegisterEvent(EventType.ON_CREATURE_KILL)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerDead(OnCreatureKill event)
	{
		L2PcInstance target = event.getTarget().getActingPlayer();
		if ((target != null) && (target.isPlayer()) && (KrateisCubeManager.checkIsInsided(target)))
		{
			KrateisCubeManager.getInstance().teleportToWaitingRoomInstance(target);
		}
		
		return;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (IN_DELAY)
		{
			if (event.equalsIgnoreCase("DEBUFF_EFFECTS"))
			{
				SkillHolder sh = DEBUFF_SKILLS[Rnd.get(DEBUFF_SKILLS.length)];
				sh.getSkill().applyEffects(npc, player);
				IN_DELAY = false;
			}
		}
		
		return null;
	}
	
	@Override
	public String onSeeCreature(L2Npc npc, L2Character creature, boolean isSummon)
	{
		if (!IN_DELAY)
		{
			if ((npc != null) && (creature != null) && (creature.isPlayable()))
			{
				L2PcInstance player = creature.getActingPlayer();
				if ((player != null) && (KrateisCubeManager.checkIsInsided(player)))
				{
					for (L2PcInstance target : npc.getKnownList().getKnownPlayersInRadius(800))
					{
						if ((target != null) && (target == player))
						{
							startQuestTimer("DEBUFF_EFFECTS", DEBUFF_DELAY * 1000, npc, target, false);
							IN_DELAY = true;
						}
					}
				}
			}
		}
		
		return super.onSeeCreature(npc, creature, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (npc.getId() == RED_WATCHER)
		{
			cancelQuestTimer("DEBUFF_EFFECTS", npc, killer);
			IN_DELAY = false;
			npc.deleteMe();
		}
		else if (npc.getId() == BLUE_WATCHER)
		{
			if (!killer.isDead())
			{
				getRandomRestored(killer);
				npc.deleteMe();
			}
		}
		else
		{
			if ((killer != null) && (KrateisCubeManager.checkIsInsided(killer)))
			{
				pointEffect(killer, false);
			}
		}
		
		return super.onKill(npc, killer, isSummon);
	}
	
	/**
	 * Get random bonus.
	 * @param player
	 */
	private void getRandomRestored(L2PcInstance player)
	{
		// Get values
		PcStatus pcStatus = player.getStatus();
		PcStat pcStat = player.getStat();
		
		switch (Rnd.get(0, 5))
		{
			case 0:
			{
				// Restore HP
				pcStatus.setCurrentHp(pcStat.getMaxHp());
				
				// Message
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_HAS_BEEN_RESTORED);
				sm.addInt(pcStat.getMaxHp());
				player.sendPacket(sm);
				break;
			}
			case 1:
			{
				// Restore MP
				pcStatus.setCurrentHp(pcStat.getMaxMp());
				
				// Message
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MP_HAS_BEEN_RESTORED);
				sm.addInt(pcStat.getMaxMp());
				player.sendPacket(sm);
				break;
			}
			case 2:
			{
				// Restore CP
				pcStatus.setCurrentHp(pcStat.getMaxCp());
				
				// Message
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CP_HAS_BEEN_RESTORED);
				sm.addInt(pcStat.getMaxCp());
				player.sendPacket(sm);
				break;
			}
		}
		
		return;
	}
	
	/**
	 * Points effects.
	 * @param player
	 * @param isPlayer
	 */
	private void pointEffect(L2PcInstance player, boolean isPlayer)
	{
		int rate = 0;
		if (isPlayer)
		{
			rate = Config.KRATEIS_REWARD_TO_KILL_PLAYER;
		}
		else
		{
			rate = Config.KRATEIS_REWARD_TO_KILL_MOB;
		}
		
		// Add points
		for (int i = 0; i < rate; i++)
		{
			KrateisCubeManager.getInstance().addPoints(player);
		}
		
		// Score on Top screen
		player.sendPacket(new ExPVPMatchCCMyRecord(KrateisCubeManager.getPoints(player)));
		
		// Score on Click to button
		player.sendPacket(new ExPVPMatchCCRecord(1));
		
		return;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.disableCoreAI(true);
		npc.setIsImmobilized(true);
		npc.abortAttack();
		npc.abortCast();
		return super.onSpawn(npc);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return null;
	}
	
	public static void main(String[] args)
	{
		new KrateisCube();
	}
}