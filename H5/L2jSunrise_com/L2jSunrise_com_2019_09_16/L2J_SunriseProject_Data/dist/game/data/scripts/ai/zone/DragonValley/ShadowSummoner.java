/*
 * Copyright (C) 2004-2017 L2J DataPack
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
package ai.zone.DragonValley;

import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.util.Util;

import ai.npc.AbstractNpcAI;

/**
 * Shadow Summoner AI.
 * @author Zoey76
 */
public class ShadowSummoner extends AbstractNpcAI
{
	// NPCs
	private static final int SHADOW_SUMMONER = 25722;
	private static final int DEMONS_BANQUET_1 = 25730;
	private static final int DEMONS_BANQUET_2 = 25731;
	// Skills
	private static final SkillHolder SUMMON_SKELETON = new SkillHolder(6835, 1);
	// Variables
	private static final String LOW_HP_FLAG = "LOW_HP_FLAG";
	private static final String LIMIT_FLAG = "LIMIT_FLAG";
	// Timers
	private static final String SUMMON_TIMER = "SUMMON_TIMER";
	private static final String FEED_TIMER = "FEED_TIMER";
	private static final String LIMIT_TIMER = "LIMIT_TIMER";
	private static final String DELAY_TIMER = "DELAY_TIMER";
	// Misc
	private static final int MAX_CHASE_DIST = 2500;
	private static final double MIN_HP_PERCENTAGE = 0.25;
	
	public ShadowSummoner()
	{
		super(ShadowSummoner.class.getSimpleName(), "ai/zone/DragonValley");
		addAttackId(SHADOW_SUMMONER);
		addSeeCreatureId(SHADOW_SUMMONER);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (Util.calculateDistance(npc, npc.getSpawn(), false, false) > MAX_CHASE_DIST)
		{
			npc.teleToLocation(npc.getSpawn().getX(), npc.getSpawn().getY(), npc.getSpawn().getZ());
		}
		
		if ((npc.getCurrentHp() < (npc.getMaxHp() * MIN_HP_PERCENTAGE)) && !npc.getVariables().getBoolean(LOW_HP_FLAG, false))
		{
			npc.getVariables().set(LOW_HP_FLAG, true);
			startQuestTimer(SUMMON_TIMER, 1000, npc, attacker);
			startQuestTimer(FEED_TIMER, 30000, npc, attacker);
			startQuestTimer(LIMIT_TIMER, 600000, npc, attacker);
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onSeeCreature(L2Npc npc, L2Character creature, boolean isSummon)
	{
		if (!creature.isPlayer())
		{
			if (creature.getId() == DEMONS_BANQUET_2)
			{
				((L2Attackable) npc).clearAggroList();
				addAttackDesire(npc, creature, 9999999999999999L);
			}
		}
		return super.onSeeCreature(npc, creature, isSummon);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (npc.isDead())
		{
			return super.onAdvEvent(event, npc, player);
		}
		
		if (SUMMON_TIMER.equals(event))
		{
			if (!npc.getVariables().getBoolean(LIMIT_FLAG, false))
			{
				startQuestTimer(DELAY_TIMER, 5000, npc, player);
				startQuestTimer(SUMMON_TIMER, 30000, npc, player);
			}
		}
		else if (FEED_TIMER.equals(event))
		{
			if (!npc.getVariables().getBoolean(LIMIT_FLAG, false))
			{
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK);
				startQuestTimer(FEED_TIMER, 30000, npc, player);
			}
		}
		else if (LIMIT_TIMER.equals(event))
		{
			npc.getVariables().set(LIMIT_FLAG, true);
		}
		else if (DELAY_TIMER.equals(event))
		{
			addSkillCastDesire(npc, npc, SUMMON_SKELETON, 9999999999900000L);
			final L2Npc demonsBanquet = addSpawn(getRandom(2) < 1 ? DEMONS_BANQUET_1 : DEMONS_BANQUET_2, npc.getX() + 150, npc.getY() + 150, npc.getZ(), npc.getHeading(), false, 0);
			addAttackDesire(demonsBanquet, player, 10000);
		}
		return super.onAdvEvent(event, npc, player);
	}
}