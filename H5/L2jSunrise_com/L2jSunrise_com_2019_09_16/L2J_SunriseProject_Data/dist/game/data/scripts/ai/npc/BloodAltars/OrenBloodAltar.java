/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.npc.BloodAltars;

import java.util.ArrayList;
import java.util.List;

import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.util.Rnd;

import ai.npc.AbstractNpcAI;

public class OrenBloodAltar extends AbstractNpcAI
{
	private static final long delay = Config.CHANGE_STATUS * 60 * 1000;
	protected static boolean bossesSpawned = false;
	
	private final List<L2Npc> deadnpcs = new ArrayList<>();
	private final List<L2Npc> alivenpcs = new ArrayList<>();
	private final List<L2Npc> bosses = new ArrayList<>();
	
	protected boolean progress1 = false;
	protected boolean progress2 = false;
	
	private static final int[][] bossGroups =
	{
		{
			25767,
			80328,
			46792,
			-3189,
			36123
		},
		{
			25770,
			80520,
			47368,
			-3193,
			36736
		}
	};
	
	private static final int[][] BLOODALTARS_DEAD_NPC =
	{
		{
			4328,
			80328,
			47192,
			-3176,
			53375
		},
		{
			4328,
			80040,
			47176,
			-3176,
			16000
		},
		{
			4327,
			80184,
			47272,
			-3178,
			6000
		}
	};
	
	private static final int[][] BLOODALTARS_ALIVE_NPC =
	{
		{
			4325,
			80328,
			47192,
			-3176,
			53375
		},
		{
			4325,
			80040,
			47176,
			-3176,
			16000
		},
		{
			4324,
			80184,
			47272,
			-3178,
			6000
		}
	};
	
	public OrenBloodAltar()
	{
		super(OrenBloodAltar.class.getSimpleName(), "ai/npc");
		
		manageNpcs(true);
		
		addKillId(25767);
		addKillId(25770);
		
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
	
	protected void manageBosses(boolean spawn)
	{
		if (spawn)
		{
			for (int[] bossspawn : bossGroups)
			{
				L2Npc boss = addSpawn(bossspawn[0], bossspawn[1], bossspawn[2], bossspawn[3], bossspawn[4], false, 0, false);
				if (boss != null)
				{
					bosses.add(boss);
				}
			}
		}
		else
		{
			if (!bosses.isEmpty())
			{
				for (L2Npc boss : bosses)
				{
					if (boss != null)
					{
						boss.deleteMe();
					}
				}
			}
		}
	}
	
	protected void changestatus()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			if (Rnd.chance(Config.CHANCE_SPAWN))
			{
				if (!bossesSpawned)
				{
					manageNpcs(false);
					manageBosses(true);
					bossesSpawned = true;
				}
				else
				{
					manageBosses(false);
					manageNpcs(true);
					bossesSpawned = false;
					ThreadPoolManager.getInstance().scheduleGeneral(() -> changestatus(), Config.RESPAWN_TIME * 60 * 1000);
				}
			}
		} , 10000);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final int npcId = npc.getId();
		
		if (npcId == 25767)
		{
			progress1 = true;
		}
		
		if (npcId == 25770)
		{
			progress2 = true;
		}
		
		if (progress1 && progress2)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(() ->
			{
				progress1 = false;
				progress2 = false;
				
				manageBosses(false);
				manageNpcs(true);
				bossesSpawned = false;
				ThreadPoolManager.getInstance().scheduleGeneral(() -> changestatus(), Config.RESPAWN_TIME * 60 * 1000);
			} , 30000);
		}
		return super.onKill(npc, player, isSummon);
	}
}