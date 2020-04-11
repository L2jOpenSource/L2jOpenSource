/*
 * Copyright (C) 2004-2017 L2J DataPack
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
package ai.npc.Teleports.GhostChamberlainOfElmoreden;

import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import ai.npc.AbstractNpcAI;

/**
 * Ghost Chamberlain of Elmoreden AI.
 * @author Adry_85
 * @since 2.6.0.0
 */
public class GhostChamberlainOfElmoreden extends AbstractNpcAI
{
	// NPCs
	private static final int GHOST_CHAMBERLAIN_OF_ELMOREDEN_1 = 31919;
	private static final int GHOST_CHAMBERLAIN_OF_ELMOREDEN_2 = 31920;
	// Items
	private static final int USED_GRAVE_PASS = 7261;
	private static final int ANTIQUE_BROOCH = 7262;
	// Locations
	private static final Location FOUR_SEPULCHERS_LOC = new Location(178127, -84435, -7215);
	private static final Location IMPERIAL_TOMB_LOC = new Location(186699, -75915, -2826);
	
	public GhostChamberlainOfElmoreden()
	{
		super(GhostChamberlainOfElmoreden.class.getSimpleName(), "ai/npc/Teleports");
		addStartNpc(GHOST_CHAMBERLAIN_OF_ELMOREDEN_1, GHOST_CHAMBERLAIN_OF_ELMOREDEN_2);
		addTalkId(GHOST_CHAMBERLAIN_OF_ELMOREDEN_1, GHOST_CHAMBERLAIN_OF_ELMOREDEN_2);
		addFirstTalkId(GHOST_CHAMBERLAIN_OF_ELMOREDEN_1, GHOST_CHAMBERLAIN_OF_ELMOREDEN_2);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("FOUR_SEPULCHERS"))
		{
			if (hasQuestItems(player, USED_GRAVE_PASS))
			{
				takeItems(player, USED_GRAVE_PASS, 1);
				player.teleToLocation(FOUR_SEPULCHERS_LOC);
			}
			else if (hasQuestItems(player, ANTIQUE_BROOCH))
			{
				player.teleToLocation(FOUR_SEPULCHERS_LOC);
			}
			else
			{
				return npc.getId() + "-01.html";
			}
		}
		else if (event.equals("IMPERIAL_TOMB"))
		{
			if (hasQuestItems(player, USED_GRAVE_PASS))
			{
				takeItems(player, USED_GRAVE_PASS, 1);
				player.teleToLocation(IMPERIAL_TOMB_LOC);
			}
			else if (hasQuestItems(player, ANTIQUE_BROOCH))
			{
				player.teleToLocation(IMPERIAL_TOMB_LOC);
			}
			else
			{
				return npc.getId() + "-01.html";
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
}