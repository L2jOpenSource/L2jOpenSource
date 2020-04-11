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
package ai.npc.Teleports.Klein;

import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.variables.NpcVariables;

import ai.npc.AbstractNpcAI;

/**
 * Klein AI.
 * @author St3eT-vGodFather
 */
public final class Klein extends AbstractNpcAI
{
	// NPCs
	private static final int KLEIN = 31540;
	// Items
	private static final int STONE = 7267; // Vacualite Floating Stone
	// Locations
	private static final Location TELE_LOC = new Location(183829, -115406, -3326);
	
	public Klein()
	{
		super(Klein.class.getSimpleName(), "ai/npc/Teleports");
		addStartNpc(KLEIN);
		addFirstTalkId(KLEIN);
		addTalkId(KLEIN);
		addSpawnId(KLEIN);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final NpcVariables vars = npc.getVariables();
		String htmltext = null;
		switch (event)
		{
			case "31540-01.html":
			case "31540-02.html":
			{
				htmltext = event;
			}
			case "2002":
			{
				if ((System.currentTimeMillis() - npc.getVariables().getInt("i_ai4")) > 3600)
				{
					vars.set("i_ai3", vars.getInt("i_ai2", 0));
					vars.set("i_ai2", vars.getInt("i_ai1", 0));
					vars.set("i_ai1", vars.getInt("i_ai0", 0));
					vars.set("i_ai0", 0);
					vars.set("i_ai4", System.currentTimeMillis());
				}
				break;
			}
			case "enter":
			{
				final int i0 = vars.getInt("i_ai3", 0) + vars.getInt("i_ai2", 0) + vars.getInt("i_ai1", 0) + vars.getInt("i_ai0", 0);
				if (i0 < 50)
				{
					htmltext = "31540-03.html";
				}
				else if (i0 < 100)
				{
					htmltext = "31540-04.html";
				}
				else if (i0 < 150)
				{
					htmltext = "31540-05.html";
				}
				else if (i0 < 200)
				{
					htmltext = "31540-06.html";
				}
				else
				{
					htmltext = "31540-07.html";
				}
				break;
			}
			case "teleportInside":
			{
				if (hasQuestItems(player, STONE))
				{
					vars.set("i_ai0", vars.getInt("i_ai0", 0) + 1);
					player.teleToLocation(TELE_LOC);
					player.setVar("valakas_last_enter", String.valueOf(System.currentTimeMillis() + 7200000));
				}
				else
				{
					htmltext = "31540-08.html";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.getVariables().set("i_ai4", System.currentTimeMillis());
		startQuestTimer("2002", 10000, npc, null, true);
		return super.onSpawn(npc);
	}
}
