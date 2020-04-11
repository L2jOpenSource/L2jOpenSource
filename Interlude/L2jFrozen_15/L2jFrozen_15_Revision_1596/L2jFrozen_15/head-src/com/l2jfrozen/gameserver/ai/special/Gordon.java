package com.l2jfrozen.gameserver.ai.special;

import java.util.Collection;

import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.datatables.sql.SpawnTable;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;

/**
 * Gordon AI
 * @author  TOFIZ
 * @version $Revision: 1.1 $ $Date: 2008/08/21 $
 */
public class Gordon extends Quest implements Runnable
{
	private static final int GORDON = 29095;
	private static int npcMoveX = 0;
	private static int npcMoveY = 0;
	private static int isWalkTo = 0;
	private static int npcBlock = 0;
	private static int x = 0;
	private static int y = 0;
	private static int z = 0;
	private static final int[][] WALKS =
	{
		{
			141569,
			-45908,
			-2387
		},
		{
			142494,
			-45456,
			-2397
		},
		{
			142922,
			-44561,
			-2395
		},
		{
			143672,
			-44130,
			-2398
		},
		{
			144557,
			-43378,
			-2325
		},
		{
			145839,
			-43267,
			-2301
		},
		{
			147044,
			-43601,
			-2307
		},
		{
			148140,
			-43206,
			-2303
		},
		{
			148815,
			-43434,
			-2328
		},
		{
			149862,
			-44151,
			-2558
		},
		{
			151037,
			-44197,
			-2708
		},
		{
			152555,
			-42756,
			-2836
		},
		{
			154808,
			-39546,
			-3236
		},
		{
			155333,
			-39962,
			-3272
		},
		{
			156531,
			-41240,
			-3470
		},
		{
			156863,
			-43232,
			-3707
		},
		{
			156783,
			-44198,
			-3764
		},
		{
			158169,
			-45163,
			-3541
		},
		{
			158952,
			-45479,
			-3473
		},
		{
			160039,
			-46514,
			-3634
		},
		{
			160244,
			-47429,
			-3656
		},
		{
			159155,
			-48109,
			-3665
		},
		{
			159558,
			-51027,
			-3523
		},
		{
			159396,
			-53362,
			-3244
		},
		{
			160872,
			-56556,
			-2789
		},
		{
			160857,
			-59072,
			-2613
		},
		{
			160410,
			-59888,
			-2647
		},
		{
			158770,
			-60173,
			-2673
		},
		{
			156368,
			-59557,
			-2638
		},
		{
			155188,
			-59868,
			-2642
		},
		{
			154118,
			-60591,
			-2731
		},
		{
			153571,
			-61567,
			-2821
		},
		{
			153457,
			-62819,
			-2886
		},
		{
			152939,
			-63778,
			-3003
		},
		{
			151816,
			-64209,
			-3120
		},
		{
			147655,
			-64826,
			-3433
		},
		{
			145422,
			-64576,
			-3369
		},
		{
			144097,
			-64320,
			-3404
		},
		{
			140780,
			-61618,
			-3096
		},
		{
			139688,
			-61450,
			-3062
		},
		{
			138267,
			-61743,
			-3056
		},
		{
			138613,
			-58491,
			-3465
		},
		{
			138139,
			-57252,
			-3517
		},
		{
			139555,
			-56044,
			-3310
		},
		{
			139107,
			-54537,
			-3240
		},
		{
			139279,
			-53781,
			-3091
		},
		{
			139810,
			-52687,
			-2866
		},
		{
			139657,
			-52041,
			-2793
		},
		{
			139215,
			-51355,
			-2698
		},
		{
			139334,
			-50514,
			-2594
		},
		{
			139817,
			-49715,
			-2449
		},
		{
			139824,
			-48976,
			-2263
		},
		{
			140130,
			-47578,
			-2213
		},
		{
			140483,
			-46339,
			-2382
		},
		{
			141569,
			-45908,
			-2387
		}
	};
	
	private static boolean isAttacked = false;
	private static boolean isSpawned = false;
	
	public Gordon(final int id, final String name, final String descr)
	{
		super(id, name, descr);
		
		addEventId(GORDON, Quest.QuestEventType.ON_KILL);
		addEventId(GORDON, Quest.QuestEventType.ON_ATTACK);
		addEventId(GORDON, Quest.QuestEventType.ON_SPAWN);
		
		// wait 2 minutes after Start AI
		startQuestTimer("check_ai", 120000, null, null, true);
		
		isSpawned = false;
		isAttacked = false;
		isWalkTo = 1;
		npcMoveX = 0;
		npcMoveY = 0;
		npcBlock = 0;
	}
	
	@Override
	public String onAdvEvent(final String event, final L2NpcInstance npc, final L2PcInstance player)
	{
		x = WALKS[isWalkTo - 1][0];
		y = WALKS[isWalkTo - 1][1];
		z = WALKS[isWalkTo - 1][2];
		if (event.equalsIgnoreCase("time_isAttacked"))
		{
			isAttacked = false;
			if (npc.getNpcId() == GORDON)
			{
				npc.setWalking();
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(x, y, z, 0));
			}
		}
		else if (event.equalsIgnoreCase("check_ai"))
		{
			cancelQuestTimer("check_ai", null, null);
			if (!isSpawned)
			{
				final L2NpcInstance gordon_ai = findTemplate(GORDON);
				if (gordon_ai != null)
				{
					isSpawned = true;
					startQuestTimer("Start", 1000, gordon_ai, null, true);
					return super.onAdvEvent(event, npc, player);
				}
			}
		}
		else if (event.equalsIgnoreCase("Start"))
		{
			// startQuestTimer("Start", 1000, npc, null);
			if (npc != null && isSpawned)
			{
				// check if player have Cursed Weapon and in radius
				if (npc.getNpcId() == GORDON)
				{
					final Collection<L2PcInstance> chars = npc.getKnownList().getKnownPlayers().values();
					if (chars != null && chars.size() > 0)
					{
						for (final L2PcInstance pc : chars)
						{
							if (pc.isCursedWeaponEquipped() && pc.isInsideRadius(npc, 5000, false, false))
							{
								npc.setRunning();
								((L2Attackable) npc).addDamageHate(pc, 0, 9999);
								npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, pc);
								isAttacked = true;
								cancelQuestTimer("time_isAttacked", null, null);
								startQuestTimer("time_isAttacked", 180000, npc, null);
								return super.onAdvEvent(event, npc, player);
							}
						}
					}
				}
				// end check
				if (isAttacked)
				{
					return super.onAdvEvent(event, npc, player);
				}
				
				if (npc.getNpcId() == GORDON && npc.getX() - 50 <= x && npc.getX() + 50 >= x && npc.getY() - 50 <= y && npc.getY() + 50 >= y)
				{
					isWalkTo++;
					if (isWalkTo > 55)
					{
						isWalkTo = 1;
					}
					x = WALKS[isWalkTo - 1][0];
					y = WALKS[isWalkTo - 1][1];
					z = WALKS[isWalkTo - 1][2];
					npc.setWalking();
					// TODO: find better way to prevent teleporting to the home location
					npc.getSpawn().setLocx(x);
					npc.getSpawn().setLocy(y);
					npc.getSpawn().setLocz(z);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(x, y, z, 0));
				}
				// Test for unblock Npc
				if (npc.getX() != npcMoveX && npc.getY() != npcMoveY)
				{
					npcMoveX = npc.getX();
					npcMoveY = npc.getY();
					npcBlock = 0;
				}
				else if (npc.getNpcId() == GORDON)
				{
					npcBlock++;
					if (npcBlock > 2)
					{
						npc.teleToLocation(x, y, z);
						return super.onAdvEvent(event, npc, player);
					}
					if (npcBlock > 0)
					{
						// TODO: find better way to prevent teleporting to the home location
						npc.getSpawn().setLocx(x);
						npc.getSpawn().setLocy(y);
						npc.getSpawn().setLocz(z);
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(x, y, z, 0));
					}
				}
				// End Test unblock Npc
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onSpawn(final L2NpcInstance npc)
	{
		if (npc.getNpcId() == GORDON && npcBlock == 0)
		{
			isSpawned = true;
			isWalkTo = 1;
			startQuestTimer("Start", 1000, npc, null);
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAttack(final L2NpcInstance npc, final L2PcInstance player, final int damage, final boolean isPet)
	{
		if (npc.getNpcId() == GORDON)
		{
			isAttacked = true;
			cancelQuestTimer("time_isAttacked", null, null);
			startQuestTimer("time_isAttacked", 180000, npc, null);
			if (player != null)
			{
				npc.setRunning();
				((L2Attackable) npc).addDamageHate(player, 0, 100);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
		}
		return super.onAttack(npc, player, damage, isPet);
	}
	
	@Override
	public String onKill(final L2NpcInstance npc, final L2PcInstance killer, final boolean isPet)
	{
		if (npc.getNpcId() == GORDON)
		{
			cancelQuestTimer("Start", null, null);
			cancelQuestTimer("time_isAttacked", null, null);
			isSpawned = false;
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public L2NpcInstance findTemplate(final int npcId)
	{
		L2NpcInstance npc = null;
		for (final L2Spawn spawn : SpawnTable.getInstance().getSpawnTable().values())
		{
			if (spawn != null && spawn.getNpcid() == npcId)
			{
				npc = spawn.getLastSpawn();
				break;
			}
		}
		return npc;
	}
	
	@Override
	public void run()
	{
	}
}
