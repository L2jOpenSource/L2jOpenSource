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
package ai.individual;

import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.ItemHolder;

import ai.npc.AbstractNpcAI;

/**
 * Eva's Gift Box AI.
 * @author St3eT
 */
public final class EvasGiftBox extends AbstractNpcAI
{
	// NPC
	private static final int BOX = 32342; // Eva's Gift Box
	// Skill
	private static final int BUFF = 1073; // Kiss of Eva
	// Items
	private static final ItemHolder CORAL = new ItemHolder(9692, 1); // Red Coral
	private static final ItemHolder CRYSTAL = new ItemHolder(9693, 1); // Crystal Fragment
	
	public EvasGiftBox()
	{
		super(EvasGiftBox.class.getSimpleName(), "ai/individual");
		addKillId(BOX);
		addSpawnId(BOX);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (killer.getFirstEffect(BUFF) != null)
		{
			if (getRandomBoolean())
			{
				npc.dropItem(killer, CRYSTAL);
			}
			else if (getRandom(100) < 33)
			{
				npc.dropItem(killer, CORAL);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsNoRndWalk(true);
		return super.onSpawn(npc);
	}
}