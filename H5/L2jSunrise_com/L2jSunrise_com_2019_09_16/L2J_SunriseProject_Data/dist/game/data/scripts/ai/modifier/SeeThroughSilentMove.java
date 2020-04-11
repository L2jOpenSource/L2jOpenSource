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

import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Npc;

import ai.npc.AbstractNpcAI;

/**
 * See Through Silent Move AI.
 * @author Gigiikun
 */
public class SeeThroughSilentMove extends AbstractNpcAI
{
	//@formatter:off
	private static final int[] MONSTERS =
	{
		18001, 18002, 22199, 22215, 22216, 22217, 22327, 22746, 22747, 22748,
		22749, 22750, 22751, 22752, 22753, 22754, 22755, 22756, 22757, 22758,
		22759, 22760, 22761, 22762, 22763, 22764, 22765, 22794, 22795, 22796,
		22797, 22798, 22799, 22800, 22843, 22857, 29009, 29010, 29011, 29012,
		29013,
		
		//DV RBs
		25718,25719,25720,25721,25722,25723,25724,
		//LoA RBs
		25725,25726,25727,
		// Sanctum of the lords of dawn
		18834, 18835,
		// Doorman Zombie, Penance Guard, Chapel Guard
		18343, 22137, 22138,
		//Beleth
		29118, 29119
	};
	//@formatter:on
	
	public SeeThroughSilentMove()
	{
		super(SeeThroughSilentMove.class.getSimpleName(), "ai/modifiers");
		addSpawnId(MONSTERS);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc.isAttackable())
		{
			((L2Attackable) npc).setSeeThroughSilentMove(true);
		}
		return super.onSpawn(npc);
	}
}
