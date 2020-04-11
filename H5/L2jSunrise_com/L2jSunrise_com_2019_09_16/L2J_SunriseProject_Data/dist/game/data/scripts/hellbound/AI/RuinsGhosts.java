/*
 * Copyright (C) 2004-2016 L2J DataPack
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
package hellbound.AI;

import java.util.Arrays;
import java.util.List;

import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.L2Skill;

import ai.npc.AbstractNpcAI;

/**
 * RuinsGhosts AI.
 * @author vGodFather
 */
public class RuinsGhosts extends AbstractNpcAI
{
	// NPCs
	private static final List<Integer> NPCS = Arrays.asList(18463, 18464, 18465);
	// Items
	private static final int HOLY_WATER = 9673;
	// Skills
	private static final int SKILL_HOLY_WATER = 2358;
	// Misc
	private static final String MSG = "The holy water affects Remnants Ghost. You have freed his soul.";
	
	public RuinsGhosts()
	{
		super(RuinsGhosts.class.getSimpleName(), "ai/group_template");
		addSpawnId(NPCS);
		addSkillSeeId(NPCS);
	}
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		npc.setIsMortal(false);
		return super.onSpawn(npc);
	}
	
	@Override
	public final String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		if ((skill.getId() == SKILL_HOLY_WATER) && !npc.isDead() && (targets.length > 0) && (targets[0] == npc))
		{
			if (npc.getCurrentHp() < (npc.getMaxHp() * 0.02)) // Lower, than 2%
			{
				takeItems(caster, HOLY_WATER, 1);
				npc.setIsInvul(false);
				npc.doDie(caster);
				caster.sendMessage(MSG);
			}
		}
		
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}
}
