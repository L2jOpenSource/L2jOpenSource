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

import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.skills.L2Skill;

import ai.npc.AbstractNpcAI;

/**
 * Necromancer of the Valley AI.
 * @author Adry_85
 * @since 2.6.0.0
 */
public class NecromancerOfTheValley extends AbstractNpcAI
{
	// NPCs
	private static final int EXPLODING_ORC_GHOST = 22818;
	private static final int WRATHFUL_ORC_GHOST = 22819;
	private static final int NECROMANCER_OF_THE_VALLEY = 22858;
	// Skill
	private static final SkillHolder SELF_DESTRUCTION = new SkillHolder(6850);
	// Misc
	private static final double HP_PERCENTAGE = 0.60;
	
	public NecromancerOfTheValley()
	{
		super(NecromancerOfTheValley.class.getSimpleName(), "ai/zone/DragonValley");
		addAttackId(NECROMANCER_OF_THE_VALLEY);
		addSpellFinishedId(EXPLODING_ORC_GHOST);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if ((npc.getCurrentHp() < (npc.getMaxHp() * HP_PERCENTAGE)))
		{
			if (getRandom(10) < 1)
			{
				if (getRandomBoolean())
				{
					final L2Npc explodingOrcGhost = addSpawn(EXPLODING_ORC_GHOST, npc.getX(), npc.getY(), npc.getZ(), 0, false, 0, false);
					addAttackDesire(explodingOrcGhost, attacker, 10000);
					addSkillCastDesire(npc, attacker, SELF_DESTRUCTION, 999999999L);
				}
				else
				{
					final L2Npc wrathfulOrcGhost = addSpawn(WRATHFUL_ORC_GHOST, npc.getX(), npc.getY(), npc.getZ(), 0, false, 0, false);
					addAttackDesire(wrathfulOrcGhost, attacker, 10000);
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if ((skill == SELF_DESTRUCTION.getSkill()) && (npc != null) && !npc.isDead())
		{
			npc.doDie(player);
		}
		return super.onSpellFinished(npc, player, skill);
	}
}
