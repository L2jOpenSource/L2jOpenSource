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
package ai.npc.BloodAltars;

import java.util.ArrayList;
import java.util.List;

import com.l2jserver.Config;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.util.Rnd;

import ai.npc.AbstractNpcAI;

public class RuneBloodAltar extends AbstractNpcAI
{
	private static final long delay = Config.CHANGE_STATUS * 60 * 1000;
	
	private final List<L2Npc> deadnpcs = new ArrayList<>();
	private final List<L2Npc> alivenpcs = new ArrayList<>();
	
	private static final int[][] BLOODALTARS_DEAD_NPC =
	{
		{
			4327,
			28008,
			-48984,
			-1328,
			16383
		},
		{
			4328,
			27864,
			-49048,
			-1312,
			24575
		},
		{
			4328,
			28152,
			-49048,
			-1312,
			8191
		}
	};
	
	private static final int[][] BLOODALTARS_ALIVE_NPC =
	{
		{
			4324,
			28008,
			-48984,
			-1328,
			16383
		},
		{
			4325,
			27864,
			-49048,
			-1312,
			24575
		},
		{
			4325,
			28152,
			-49048,
			-1312,
			8191
		}
	};
	
	public RuneBloodAltar()
	{
		super(RuneBloodAltar.class.getSimpleName(), "ai/npc");
		
		manageNpcs(true);
		
		ThreadPoolManager.getInstance().scheduleGeneral(() -> changestatus(), delay);
	}
	
	protected void manageNpcs(boolean spawnAlive)
	{
		if (spawnAlive)
		{
			for (int[] spawn : BLOODALTARS_ALIVE_NPC)
			{
				L2Npc npc = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false);
				if (npc != null)
				{
					alivenpcs.add(npc);
				}
			}
			
			if (!deadnpcs.isEmpty())
			{
				for (L2Npc npc : deadnpcs)
				{
					if (npc != null)
					{
						npc.deleteMe();
					}
				}
			}
			deadnpcs.clear();
		}
		else
		{
			for (int[] spawn : BLOODALTARS_DEAD_NPC)
			{
				L2Npc npc = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false);
				if (npc != null)
				{
					deadnpcs.add(npc);
				}
			}
			
			if (!alivenpcs.isEmpty())
			{
				for (L2Npc npc : alivenpcs)
				{
					if (npc != null)
					{
						npc.deleteMe();
					}
				}
			}
			alivenpcs.clear();
		}
	}
	
	protected void changestatus()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			if (Rnd.chance(Config.CHANCE_SPAWN))
			{
				boolean aliveSpawned = false;
				if (!aliveSpawned)
				{
					manageNpcs(false);
				}
				else
				{
					manageNpcs(true);
					ThreadPoolManager.getInstance().scheduleGeneral(() -> changestatus(), Config.RESPAWN_TIME * 60 * 1000);
				}
			}
		}, 10000);
	}
	
	public static void main(String[] args)
	{
		new RuneBloodAltar();
	}
}