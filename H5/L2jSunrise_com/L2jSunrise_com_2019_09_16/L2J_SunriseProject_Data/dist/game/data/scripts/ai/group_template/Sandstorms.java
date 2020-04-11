/*
 * Copyright (C) 2004-2013 L2J DataPack
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

import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import ai.npc.AbstractNpcAI;

/**
 * Sandstorms AI.
 * @author Ectis
 */
public class Sandstorms extends AbstractNpcAI
{
	public Sandstorms()
	{
		super(Sandstorms.class.getSimpleName(), "ai/group_template");
		addAggroRangeEnterId(32350); // Sandstorm
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		npc.setTarget(player);
		npc.doCast(SkillData.getInstance().getInfo(5435, 1));
		return super.onAggroRangeEnter(npc, player, isSummon);
	}
}
