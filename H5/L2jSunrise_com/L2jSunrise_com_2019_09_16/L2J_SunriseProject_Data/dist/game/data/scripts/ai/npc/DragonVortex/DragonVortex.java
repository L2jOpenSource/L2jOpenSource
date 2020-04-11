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
package ai.npc.DragonVortex;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import l2r.gameserver.data.SpawnTable;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import ai.npc.AbstractNpcAI;

/**
 * Dragon Vortex AI.
 * @author UnAfraid, improved by Adry_85 & DreamStage
 */
public final class DragonVortex extends AbstractNpcAI
{
	// NPC
	private static final int VORTEX = 32871;
	// Raids
	//@formatter:off
	private static final int[][] RAIDS =
	{
		// Emerald Horn 29.2%
		{ 25718, 292 },
		// Dust Rider 22.4%
		{ 25719, 224 },
		// Bleeding Fly 17.6%
		{ 25720, 176 },
		// Blackdagger Wing 11.6%
		{ 25721, 116 },
		// Spike Slasher 9.2%
		{ 25723, 92 },
		// Shadow Summoner 5.6%
		{ 25722, 56 },
		// Muscle Bomber 4.4%
		{ 25724, 44 }
	};
	//@formatter:on
	// Item
	private static final int LARGE_DRAGON_BONE = 17248;
	
	// Misc
	private static final int DESPAWN_DELAY = 1800000; // 30min
	private static Set<Integer> _spawnedList = ConcurrentHashMap.newKeySet();
	
	public DragonVortex()
	{
		super(DragonVortex.class.getSimpleName(), "ai/npc");
		addStartNpc(VORTEX);
		addFirstTalkId(VORTEX);
		addTalkId(VORTEX);
		addKillId(25718, 25719, 25720, 25721, 25723, 25722, 25724);
		
		loadSpawns();
	}
	
	private static void loadSpawns()
	{
		addSpawn(32871, new Location(92225, 113873, -3062));
		addSpawn(32871, new Location(108924, 111992, -3028));
		addSpawn(32871, new Location(110116, 125500, -3664));
		addSpawn(32871, new Location(121172, 113348, -3776));
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "32871.html";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if ("Spawn".equals(event))
		{
			if (hasQuestItems(player, LARGE_DRAGON_BONE))
			{
				final int chance = getRandom(1000);
				List<int[]> unspawnedRaids = new ArrayList<>();
				List<int[]> unspawnedCandidates = new ArrayList<>();
				int raidChanceIncrease = 0;
				
				// Iterate over all Raids and check which ones are currently spawned, sum spawned Raids chance for unspawnedRaids List distribution
				for (int[] raidsList : RAIDS)
				{
					int raidChance = raidsList[1];
					if (checkIfNpcSpawned(raidsList[0]))
					{
						raidChanceIncrease += raidChance;
					}
					else
					{
						unspawnedRaids.add(new int[]
						{
							raidsList[0],
							raidChance
						});
					}
				}
				
				// If there are unspawnedRaids onto the new List, distribute the amount of increased chances for each one and spawn a new Raid from the new chances
				if (!unspawnedRaids.isEmpty())
				{
					int unspawnedRaidsSize = unspawnedRaids.size();
					int chanceIncrease = (raidChanceIncrease / unspawnedRaidsSize);
					int raidChanceValue = 0;
					
					for (int[] unspawnedRaidsList : unspawnedRaids)
					{
						raidChanceValue += unspawnedRaidsList[1] + chanceIncrease;
						unspawnedCandidates.add(new int[]
						{
							unspawnedRaidsList[0],
							raidChanceValue
						});
					}
					
					for (int[] unspawnedCandidatesList : unspawnedCandidates)
					{
						if (chance <= unspawnedCandidatesList[1])
						{
							spawnRaid(unspawnedCandidatesList[0], npc, player);
							break;
						}
					}
					return null;
				}
				return "32871-noboss.html";
			}
			return "32871-no.html";
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		_spawnedList.remove(npc.getId());
		return super.onKill(npc, player, isPet);
	}
	
	/**
	 * Method used for spawning a Dragon Vortex Raid and take a Large Dragon Bone from the Player
	 * @param raidId
	 * @param npc
	 * @param player
	 */
	public void spawnRaid(int raidId, L2Npc npc, L2PcInstance player)
	{
		L2Spawn spawnDat = addSpawn(raidId, npc.getX() + getRandom(-500, 500), npc.getY() + getRandom(-500, 500), npc.getZ() + 10, 0, false, DESPAWN_DELAY, true).getSpawn();
		SpawnTable.getInstance().addNewSpawn(spawnDat, false);
		takeItems(player, LARGE_DRAGON_BONE, 1);
		_spawnedList.add(raidId);
	}
	
	/**
	 * Method used for checking if npc is spawned
	 * @param npcId
	 * @return if npc is spawned
	 */
	public boolean checkIfNpcSpawned(int npcId)
	{
		return _spawnedList.contains(npcId);
	}
}