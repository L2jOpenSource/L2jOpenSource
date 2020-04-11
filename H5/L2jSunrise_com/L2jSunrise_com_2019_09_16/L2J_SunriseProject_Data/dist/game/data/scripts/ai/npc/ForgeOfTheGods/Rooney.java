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
package ai.npc.ForgeOfTheGods;

import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.clientpackets.Say2;

import ai.npc.AbstractNpcAI;

/**
 * Rooney AI
 * @author malyelfik, Adry_85
 */
public final class Rooney extends AbstractNpcAI
{
	// NPC
	private static final int ROONEY = 32049;
	// Locations
	private static final Location[] LOCATIONS =
	{
		new Location(179221, -115743, -3600),
		new Location(177668, -118775, -4080),
		new Location(179906, -108469, -5832),
		new Location(181285, -113798, -6064),
		new Location(181805, -108718, -5832),
		new Location(184131, -117511, -3336),
		new Location(186418, -112998, -3272)
	};
	
	public Rooney()
	{
		super(Rooney.class.getSimpleName(), "ai/npc");
		addSeeCreatureId(ROONEY);
		addSpawn(ROONEY, LOCATIONS[getRandom(LOCATIONS.length)], false, 0);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case "teleport":
			{
				if (!npc.isDecayed())
				{
					npc.setScriptValue(0);
				}
				break;
			}
			case "message1":
			{
				if (!npc.isDecayed())
				{
					broadcastNpcSay(npc, Say2.NPC_ALL, NpcStringId.HURRY_HURRY);
					startQuestTimer("message2", 60000, npc, null);
				}
				break;
			}
			case "message2":
			{
				if (!npc.isDecayed())
				{
					broadcastNpcSay(npc, Say2.NPC_ALL, NpcStringId.I_AM_NOT_THAT_TYPE_OF_PERSON_WHO_STAYS_IN_ONE_PLACE_FOR_A_LONG_TIME);
					startQuestTimer("message3", 60000, npc, null);
				}
				break;
			}
			case "message3":
			{
				if (!npc.isDecayed())
				{
					broadcastNpcSay(npc, Say2.NPC_ALL, NpcStringId.ITS_HARD_FOR_ME_TO_KEEP_STANDING_LIKE_THIS);
					startQuestTimer("message4", 60000, npc, null);
				}
				break;
			}
			case "message4":
			{
				if (!npc.isDecayed())
				{
					broadcastNpcSay(npc, Say2.NPC_ALL, NpcStringId.WHY_DONT_I_GO_THAT_WAY_THIS_TIME);
				}
				break;
			}
		}
		return null;
	}
	
	@Override
	public String onSeeCreature(L2Npc npc, L2Character creature, boolean isSummon)
	{
		if (creature.isPlayer() && npc.isScriptValue(0))
		{
			broadcastNpcSay(npc, Say2.NPC_ALL, NpcStringId.WELCOME);
			startQuestTimer("teleport", 3600000, npc, null);
			startQuestTimer("message1", 60000, npc, null);
			npc.setScriptValue(1);
		}
		return super.onSeeCreature(npc, creature, isSummon);
	}
}