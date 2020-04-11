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
package ai.modifier;

import l2r.gameserver.model.actor.L2Npc;

import ai.npc.AbstractNpcAI;

/**
 * @author , vGodFather
 */
public class NonAttackingNpcs extends AbstractNpcAI
{
	// @formatter:off
	private static final int[] NON_ATTACKING_NPCS =
	{
		// Fairy Trees
		27185, 27186, 27187, 27188,
		
		//
		18811
	};
	// @formatter:on
	
	public NonAttackingNpcs()
	{
		super(NonAttackingNpcs.class.getSimpleName(), "ai/modifiers");
		addSpawnId(NON_ATTACKING_NPCS);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.disableCoreAI(true);
		npc.setIsImmobilized(true);
		return super.onSpawn(npc);
	}
}
