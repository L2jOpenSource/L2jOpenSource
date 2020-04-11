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
package ai.npc.FreyasSteward;

import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import ai.npc.AbstractNpcAI;

/**
 * Freya's Steward AI.
 * @author Adry_85
 */
public final class FreyasSteward extends AbstractNpcAI
{
	// NPC
	private static final int FREYAS_STEWARD = 32029;
	// Location
	private static final Location TELEPORT_LOC = new Location(103045, -124361, -2768);
	// Misc
	private static final int MIN_LEVEL = 82;
	
	public FreyasSteward()
	{
		super(FreyasSteward.class.getSimpleName(), "ai/npc");
		addStartNpc(FREYAS_STEWARD);
		addFirstTalkId(FREYAS_STEWARD);
		addTalkId(FREYAS_STEWARD);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "32029.html";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		if ("teleToJinia".equals(event))
		{
			if (player.getLevel() >= MIN_LEVEL)
			{
				player.teleToLocation(TELEPORT_LOC);
			}
			else
			{
				htmltext = "32029-1.html";
			}
		}
		return htmltext;
	}
}