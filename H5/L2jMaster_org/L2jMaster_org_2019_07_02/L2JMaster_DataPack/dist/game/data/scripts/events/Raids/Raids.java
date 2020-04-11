/*
 * Copyright (C) 2004-2019 L2J DataPack
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
package events.Raids;

import java.util.ArrayList;
import java.util.List;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.util.Broadcast;

public class Raids extends Quest
{
	private static final int START_EVENT_TIME = 240;// minutes
	private static final int TIME_EVENT = 30;// minutes
	
	private static List<L2Npc> _npc_spawn = new ArrayList<>();
	
	private static final int[] _raids =
	{
		1014,
		1015,
		1016,
		1017,
		1018,
		1019
	};
	
	private static final String[] RAIDS_NAME =
	{
		"Ancient Soul Devourer",
		"Dust Rider",
		"Bleeding Fly",
		"Shadow Summoner",
		"Spike Slasher",
		"Muscle Bomber"
	};
	
	private static final String[] SPAWN_LOC =
	{
		"in Aden.",
		"in Dion.",
		"in Heine.",
		"in Gludin.",
		"in Rune.",
		"in Dwarven Village."
	};
	
	/**
	 * x, y, z.
	 */
	private static final int[][] _spawns =
	{
		{
			147441,
			28118,
			-2268 // Aden
		},
		{
			18635,
			145378,
			-3125 // Dion
		},
		{
			110941,
			220171,
			-3677 // Heine
		},
		{
			-83737,
			150798,
			-3129 // Gludin
		},
		{
			44111,
			-50530,
			-797 // Rune
		},
		{
			116551,
			-182417,
			-1525 // Dwarven Village
		}
	};
	
	/**
	 * ItemdId, Chance, Max Drop, Min Drop.
	 */
	private static final int[][] DROPLIST =
	{
		// itemId, chance, min amount, max amount
		{
			1540,
			80,
			10,
			15
		}, // Quick Healing Potion
		{
			1538,
			80,
			5,
			10
		}, // Blessed Scroll of Escape
		{
			3936,
			80,
			5,
			10
		}, // Blessed Scroll of Ressurection
		{
			6387,
			80,
			5,
			10
		}, // Blessed Scroll of Ressurection Pets
		{
			22025,
			80,
			5,
			10
		}, // Powerful Healing Potion
		{
			6622,
			80,
			1,
			1
		}, // Giant's Codex
		{
			20034,
			80,
			1,
			1
		}, // Revita Pop
		{
			20004,
			80,
			1,
			1
		}, // Energy Ginseng
		{
			1458,
			80,
			50,
			100
		}, // Crystal D-Grade
		{
			1459,
			80,
			40,
			80
		}, // Crystal C-Grade
		{
			1460,
			80,
			30,
			60
		}, // Crystal B-Grade
		{
			1461,
			80,
			20,
			30
		}, // Crystal A-Grade
		{
			1462,
			80,
			10,
			20
		} // Crystal S-Grade
	};
	
	public Raids()
	{
		super(-1, Raids.class.getSimpleName(), "custom");
		
		addKillId(_raids);
		
		startQuestTimer("SpawnRaid", START_EVENT_TIME * 60000, null, null);
	}
	
	public static void main(String[] args)
	{
		new Raids();
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		cancelQuestTimer("DespawnRaid", null, null);
		startQuestTimer("SpawnRaid", TIME_EVENT * 60000, null, null);
		
		dropItem(npc, killer, DROPLIST);
		_npc_spawn.clear();
		Broadcast.toAllOnlinePlayers("Congratulations, the city is safe now!");
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("SpawnRaid"))
		{
			final int random = getRandom(_raids.length - 1);
			L2Npc mobs = addSpawn(_raids[random], _spawns[random][0], _spawns[random][1], _spawns[random][2], 0, false, 0);
			_npc_spawn.add(mobs);
			Broadcast.toAllOnlinePlayers("The kingdom residents are concerned about the frequent appearance of  huge monsters near the cities!.");
			Broadcast.toAllOnlinePlayers("Hurry to kill the monster, so that innocent people could sleep soundly!.");
			Broadcast.toAllOnlinePlayers(RAIDS_NAME[random] + " Spawn " + SPAWN_LOC[random]);
			Broadcast.toAllOnlinePlayers("Have " + TIME_EVENT + " minutes to kill.");
			
			startQuestTimer("DespawnRaid", TIME_EVENT * 60000, null, null);
			return null;
		}
		if (event.equals("DespawnRaid"))
		{
			if (!_npc_spawn.isEmpty())
			{
				for (L2Npc h : _npc_spawn)
				{
					h.deleteMe();
				}
			}
			_npc_spawn.clear();
			startQuestTimer("SpawnRaid", 21600000, null, null);// each 3 days spawn raid
			return null;
		}
		return null;
	}
	
	private static void dropItem(L2Npc mob, L2PcInstance player, int[][] droplist)
	{
		final int chance = getRandom(100);
		
		for (int[] drop : droplist)
		{
			if (chance > drop[1])
			{
				((L2MonsterInstance) mob).dropItem(player, drop[0], getRandom(drop[2], drop[3]));
				return;
			}
		}
	}
}