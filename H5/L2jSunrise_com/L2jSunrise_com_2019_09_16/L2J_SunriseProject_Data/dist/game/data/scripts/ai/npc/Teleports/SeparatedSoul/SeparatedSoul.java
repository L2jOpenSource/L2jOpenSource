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
package ai.npc.Teleports.SeparatedSoul;

import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import ai.npc.AbstractNpcAI;

/**
 * Separated Soul teleport AI.
 * @author UnAfraid, improved by Adry_85, Zealar
 */
public final class SeparatedSoul extends AbstractNpcAI
{
	// NPCs
	private static final int[] SEPARATED_SOULS =
	{
		32864,
		32865,
		32866,
		32867,
		32868,
		32869,
		32870,
		32891
	};
	
	// Items
	private static final int WILL_OF_ANTHARAS = 17266;
	private static final int SEALED_BLOOD_CRYSTAL = 17267;
	private static final int ANTHARAS_BLOOD_CRYSTAL = 17268;
	// Misc
	private static final int MIN_LEVEL = 80;
	// Locations
	private static final Map<Integer, Location> LOCATIONS = new HashMap<>();
	
	static
	{
		LOCATIONS.put(1, new Location(117046, 76798, -2696)); // Hunter's Village
		LOCATIONS.put(2, new Location(99218, 110283, -3696)); // The Center of Dragon Valley
		LOCATIONS.put(3, new Location(116992, 113716, -3056)); // Deep inside Dragon Valley(North)
		LOCATIONS.put(4, new Location(113203, 121063, -3712)); // Deep inside Dragon Valley (South)
		LOCATIONS.put(5, new Location(146129, 111232, -3568)); // Antharas' Lair - Magic Force Field Bridge
		LOCATIONS.put(6, new Location(148447, 110582, -3944)); // Deep inside Antharas' Lair
		LOCATIONS.put(7, new Location(73122, 118351, -3714)); // Entrance to Dragon Valley
		LOCATIONS.put(8, new Location(131116, 114333, -3704)); // Entrance of Antharas' Lair
	}
	
	public SeparatedSoul()
	{
		super(SeparatedSoul.class.getSimpleName(), "ai/npc/Teleports");
		addStartNpc(SEPARATED_SOULS);
		addTalkId(SEPARATED_SOULS);
		addFirstTalkId(SEPARATED_SOULS);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return npc.getId() + ".htm";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final int ask = Integer.parseInt(event);
		switch (ask)
		{
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			{
				if (player.getLevel() >= MIN_LEVEL)
				{
					player.teleToLocation(LOCATIONS.get(ask), false);
				}
				else
				{
					return "no-level.htm";
				}
				break;
			}
			case 23241:
			{
				if (hasQuestItems(player, WILL_OF_ANTHARAS, SEALED_BLOOD_CRYSTAL))
				{
					takeItems(player, WILL_OF_ANTHARAS, 1);
					takeItems(player, SEALED_BLOOD_CRYSTAL, 1);
					giveItems(player, ANTHARAS_BLOOD_CRYSTAL, 1);
				}
				else
				{
					return "no-items.htm";
				}
			}
			case 23242:
			{
				return "separatedsoul.htm";
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
}