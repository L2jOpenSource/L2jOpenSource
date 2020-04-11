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

import l2r.gameserver.GeoData;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.interfaces.ILocational;
import l2r.gameserver.util.Util;

import ai.npc.AbstractNpcAI;

/**
 * Flee Monsters AI.
 * @author Pandragon, Nos
 */
public final class FleeMonsters extends AbstractNpcAI
{
	// NPCs
	private static final int[] MOBS =
	{
		18150, // Victim
		18151, // Victim
		18152, // Victim
		18153, // Victim
		18154, // Victim
		18155, // Victim
		18156, // Victim
		18157, // Victim
		20002, // Rabbit
		20432, // Elpy
		22228, // Grey Elpy
		25604, // Mutated Elpy
	};
	// Misc
	private static final int FLEE_DISTANCE = 500;
	
	public FleeMonsters()
	{
		super(FleeMonsters.class.getSimpleName(), "ai/group_template");
		addAttackId(MOBS);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		npc.disableCoreAI(true);
		npc.setRunning();
		
		final L2Summon summon = isSummon ? attacker.getSummon() : null;
		final ILocational attackerLoc = summon == null ? attacker : summon;
		final double radians = Math.toRadians(Util.calculateAngleFrom(attackerLoc, npc));
		final int posX = (int) (npc.getX() + (FLEE_DISTANCE * Math.cos(radians)));
		final int posY = (int) (npc.getY() + (FLEE_DISTANCE * Math.sin(radians)));
		final int posZ = npc.getZ();
		
		final Location destination = GeoData.getInstance().moveCheck(npc.getX(), npc.getY(), npc.getZ(), posX, posY, posZ, attacker.getInstanceId());
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, destination);
		return super.onAttack(npc, attacker, damage, isSummon);
	}
}
