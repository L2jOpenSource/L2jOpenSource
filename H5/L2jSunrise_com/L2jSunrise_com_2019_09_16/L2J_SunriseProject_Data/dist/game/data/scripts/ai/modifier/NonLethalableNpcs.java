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
 * @author UnAfraid, vGodFather
 */
public class NonLethalableNpcs extends AbstractNpcAI
{
	private static final int[] NON_LETHAL_NPCS =
	{
		//@formatter:off
		// Headquarter
		35062,
		
		//Antharas minions
		29069, 29070, 29071, 29072, 29073, 29074, 29075, 29076, 29190
		//@formatter:on
	};
	
	public NonLethalableNpcs()
	{
		super(NonLethalableNpcs.class.getSimpleName(), "ai/modifiers");
		addSpawnId(NON_LETHAL_NPCS);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setLethalable(false);
		return super.onSpawn(npc);
	}
}
