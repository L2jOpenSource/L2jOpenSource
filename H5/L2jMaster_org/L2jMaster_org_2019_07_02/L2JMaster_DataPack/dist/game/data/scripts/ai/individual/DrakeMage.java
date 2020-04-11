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
package ai.individual;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.SkillHolder;

import ai.npc.AbstractNpcAI;

/**
 * Drake Mage AI.
 * @author MaGa
 */
public final class DrakeMage extends AbstractNpcAI
{
	private static final int DRAKE_MAGE = 22851;
	// Skills
	private static final SkillHolder COMPLETE_RECOVERY = new SkillHolder(6765); // Complete Recovery
	// Misc
	private static final double MIN_HP_PERCENTAGE = 0.30;
	
	public DrakeMage()
	{
		super(DrakeMage.class.getSimpleName(), "ai/individual");
		addAttackId(DRAKE_MAGE);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if ((npc.getCurrentHp() < (npc.getMaxHp() * MIN_HP_PERCENTAGE)))
		{
			npc.setTarget(npc);
			npc.doCast(COMPLETE_RECOVERY.getSkill());
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	public static void main(String[] args)
	{
		new DrakeMage();
	}
}