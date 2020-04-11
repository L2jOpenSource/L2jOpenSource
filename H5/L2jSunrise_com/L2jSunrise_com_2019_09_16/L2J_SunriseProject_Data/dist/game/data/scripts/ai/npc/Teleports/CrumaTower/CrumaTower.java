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
package ai.npc.Teleports.CrumaTower;

import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import ai.npc.AbstractNpcAI;

/**
 * Cruma Tower teleport AI.
 * @author vGodFather
 */
public final class CrumaTower extends AbstractNpcAI
{
	// NPC
	private static final int MOZELLA = 30483;
	// Locations
	private static final Location TELEPORT_LOC1 = new Location(17776, 113968, -11671);
	private static final Location TELEPORT_LOC2 = new Location(17680, 113968, -11671);
	// Misc
	private static final int MAX_LEVEL = 55;
	
	public CrumaTower()
	{
		super(CrumaTower.class.getSimpleName(), "ai/npc/Teleports");
		addFirstTalkId(MOZELLA);
		addStartNpc(MOZELLA);
		addTalkId(MOZELLA);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (player.getLevel() <= MAX_LEVEL)
		{
			player.teleToLocation(getRandomBoolean() ? TELEPORT_LOC1 : TELEPORT_LOC2);
			return "";
		}
		
		return "30483-1.html";
	}
}