/*
 * Copyright (C) 2004-2015 L2J DataPack
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

import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.skills.L2Skill;

import ai.npc.AbstractNpcAI;

/**
 * Summon Pc AI.<br>
 * Summon the player to the NPC on attack.
 * @author Zoey76
 */
public final class SummonPc extends AbstractNpcAI
{
	// NPCs
	private static final int PORTA = 20213;
	private static final int PERUM = 20221;
	// Skill
	private static final SkillHolder SUMMON_PC = new SkillHolder(4161, 1);
	// Misc
	private static final int MIN_DISTANCE = 300;
	private static final int MIN_DISTANCE_MOST_HATED = 100;
	
	public SummonPc()
	{
		super(SummonPc.class.getSimpleName(), "ai/group_template");
		addAttackId(PORTA, PERUM);
		addSpellFinishedId(PORTA, PERUM);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		final boolean attacked = npc.getVariables().getBoolean("attacked", false);
		if (attacked)
		{
			return super.onAttack(npc, attacker, damage, isSummon);
		}
		
		final int chance = getRandom(100);
		final double distance = npc.calculateDistance(attacker, true, false);
		if (distance > MIN_DISTANCE)
		{
			if (chance < 50)
			{
				doSummonPc(npc, attacker);
			}
		}
		else if (distance > MIN_DISTANCE_MOST_HATED)
		{
			final L2Attackable monster = (L2Attackable) npc;
			if (monster.getMostHated() != null)
			{
				if (((monster.getMostHated() == attacker) && (chance < 50)) || (chance < 10))
				{
					doSummonPc(npc, attacker);
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if ((skill.getId() == SUMMON_PC.getSkillId()) && !npc.isDead() && npc.getVariables().getBoolean("attacked", false))
		{
			player.teleToLocation(npc);
			npc.getVariables().set("attacked", false);
			
			// TODO(Zoey76): Teleport removes the player from all known lists, affecting aggro lists.
			addAttackDesire(npc, player);
		}
		return super.onSpellFinished(npc, player, skill);
	}
	
	private static void doSummonPc(L2Npc npc, L2PcInstance attacker)
	{
		if ((SUMMON_PC.getSkill().getMpConsume() < npc.getCurrentMp()) && (SUMMON_PC.getSkill().getHpConsume() < npc.getCurrentHp()) && !npc.isSkillDisabled(SUMMON_PC.getSkill()))
		{
			npc.setTarget(attacker);
			npc.doCast(SUMMON_PC.getSkill());
			npc.getVariables().set("attacked", true);
		}
	}
}