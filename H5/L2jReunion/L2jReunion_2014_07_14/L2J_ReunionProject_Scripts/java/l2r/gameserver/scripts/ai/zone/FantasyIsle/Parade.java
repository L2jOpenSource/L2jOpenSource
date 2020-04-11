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
package l2r.gameserver.scripts.ai.zone.FantasyIsle;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;
import l2r.gameserver.GameTimeController;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.network.serverpackets.NpcSay;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;

/**
 * Fantasy Isle Parade
 * @author JOJO
 */
public class Parade extends AbstractNpcAI
{
	private static final boolean DEBUG = true;
	protected final int[] ACTORS =
	{
		32381,
		32379,
		32381,
		32382,
		32383,
		32384,
		32381,
		32385,
		32381,
		32384,
		32383,
		32382,
		
		32386,
		32387,
		32388,
		32389,
		32390,
		
		32391,
		32392,
		32393,
		32394,
		32395,
		
		32396,
		32397,
		32398,
		32399,
		32400,
		
		32401,
		32402,
		32403,
		32404,
		
		32405,
		32406,
		32407,
		32408,
		
		32409,
		32411,
		32412,
		32413,
		32414,
		32415,
		
		32416,
		32417,
		32418,
		32419,
		32420,
		
		32421,
		32422,
		32423,
		32429,
		32430,
		
		32447,
		32448,
		32449,
		32450,
		
		32451,
		32452,
		32453,
		32454,
		32455,
		32456,
		
		0,
		0,
		0,
		32415,
	};
	
	// (Northbound 270 degrees) Route 1
	private final int[][] START1 =
	{
		{
			-54780,
			-56810,
			-2015,
			49152
		},
		{
			-54860,
			-56810,
			-2015,
			49152
		},
		{
			-54940,
			-56810,
			-2015,
			49152
		}
	};
	private final int[][] GOAL1 =
	{
		{
			-54780,
			-57965,
			-2015,
			49152
		},
		{
			-54860,
			-57965,
			-2015,
			49152
		},
		{
			-54940,
			-57965,
			-2015,
			49152
		}
	};
	// (Westbound 180 degrees) Route 2
	private final int[][] START2 =
	{
		{
			-55715,
			-58900,
			-2015,
			32768
		},
		{
			-55715,
			-58820,
			-2015,
			32768
		},
		{
			-55715,
			-58740,
			-2015,
			32768
		}
	};
	private final int[][] GOAL2 =
	{
		{
			-60850,
			-58900,
			-2015,
			32768
		},
		{
			-60850,
			-58820,
			-2015,
			32768
		},
		{
			-60850,
			-58740,
			-2015,
			32768
		}
	};
	// (Southbound 90 degrees) Route 3
	private final int[][] START3 =
	{
		{
			-61790,
			-57965,
			-2015,
			16384
		},
		{
			-61710,
			-57965,
			-2015,
			16384
		},
		{
			-61630,
			-57965,
			-2015,
			16384
		}
	};
	private final int[][] GOAL3 =
	{
		{
			-61790,
			-53890,
			-2116,
			16384
		},
		{
			-61710,
			-53890,
			-2116,
			16384
		},
		{
			-61630,
			-53890,
			-2116,
			16384
		}
	};
	// (Eastbound 0 degrees) Route 4
	private final int[][] START4 =
	{
		{
			-60840,
			-52990,
			-2108,
			0
		},
		{
			-60840,
			-53070,
			-2108,
			0
		},
		{
			-60840,
			-53150,
			-2108,
			0
		}
	};
	private final int[][] GOAL4 =
	{
		{
			-58620,
			-52990,
			-2015,
			0
		},
		{
			-58620,
			-53070,
			-2015,
			0
		},
		{
			-58620,
			-53150,
			-2015,
			0
		}
	};
	// (To 315 degrees northeast) Route 5
	private final int[][] START5 =
	{
		{
			-57233,
			-53554,
			-2015,
			57344
		},
		{
			-57290,
			-53610,
			-2015,
			57344
		},
		{
			-57346,
			-53667,
			-2015,
			57344
		}
	};
	private final int[][] GOAL5 =
	{
		{
			-55338,
			-55435,
			-2015,
			57344
		},
		{
			-55395,
			-55491,
			-2015,
			57344
		},
		{
			-55451,
			-55547,
			-2015,
			57344
		}
	};
	
	protected final int[][][] START =
	{
		START1,
		START2,
		START3,
		START4,
		START5
	};
	protected final int[][][] GOAL =
	{
		GOAL1,
		GOAL2,
		GOAL3,
		GOAL4,
		GOAL5
	};
	
	protected ScheduledFuture<?> spawnTask;
	protected ScheduledFuture<?> deleteTask;
	protected ScheduledFuture<?> cleanTask;
	
	protected int npcIndex;
	protected FastList<L2Npc> spawns;
	
	protected void load()
	{
		npcIndex = 0;
		spawns = new FastList<L2Npc>().shared();
	}
	
	protected void clean()
	{
		for (Iterator<L2Npc> it = spawns.iterator(); it.hasNext();)
		{
			L2Npc actor = it.next();
			if (actor != null)
			{
				actor.deleteMe();
				it.remove();
			}
		}
		spawns = null;
	}
	
	public Parade(String name, String descr)
	{
		super(name, descr);
		
		final long diff = timeLeftMilli(8, 0, 0), cycle = 3600000L; // 8:00 start time, repeated every 6 hours
		if (DEBUG)
		{
			SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			_log.info("Fantasy Isle: Parade script starting at " + format.format(System.currentTimeMillis() + diff) + " and is scheduled each next " + (cycle / 3600000) + " hours.");
		}
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Start(), diff, cycle);
	}
	
	private long timeLeftMilli(int hh, int mm, int ss)
	{
		int now = (GameTimeController.getInstance().getGameTicks() * 60) / 100;
		int dd = ((hh * 3600) + (mm * 60) + ss) - (now % 86400);
		if (dd < 0)
		{
			dd += 86400;
		}
		
		return (dd * 1000L) / 6L;
	}
	
	protected class Start implements Runnable
	{
		@Override
		public void run()
		{
			load();
			spawnTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Spawn(), 0, 5000);
			deleteTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Delete(), 10000, 1000);
			cleanTask = ThreadPoolManager.getInstance().scheduleGeneral(new Clean(), 420000);
		}
	}
	
	protected class Spawn implements Runnable
	{
		@Override
		public void run()
		{
			for (int i = 0; i < 3; ++i)
			{
				if (npcIndex >= ACTORS.length)
				{
					spawnTask.cancel(false);
					break;
				}
				int npcId = ACTORS[npcIndex++];
				if (npcId == 0)
				{
					continue;
				}
				for (int route = 0; route < 5; ++route)
				{ // TODO:Provisional
					int[] start = START[route][i];
					int[] goal = GOAL[route][i];
					L2Npc actor = addSpawn(npcId, start[0], start[1], start[2], start[3], false, 0);
					actor.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(goal[0], goal[1], goal[2], goal[3]));
					spawns.add(actor);// TODO:NPE
				}
			}
		}
	}
	
	protected class Delete implements Runnable
	{
		@Override
		public void run()
		{
			if (spawns != null)
			{
				if (spawns.size() > 0)
				{
					for (Iterator<L2Npc> it = spawns.iterator(); it.hasNext();)
					{
						L2Npc actor = it.next();
						if (actor != null)
						{
							if (actor.getPlanDistanceSq(actor.getXdestination(), actor.getYdestination()) < (100 * 100))// TODO:NPE
							{
								actor.deleteMe();
								it.remove();
							}
							else if (!actor.isMoving())
							{
								actor.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(actor.getXdestination(), actor.getYdestination(), actor.getZdestination(), actor.getHeading()));
								System.out.println("__BASENAME__:__LINE__: " + actor.getId() + " " + actor.getX() + "," + actor.getY() + "," + actor.getZ() + "," + actor.getHeading() + " -> " + actor.getXdestination() + "," + actor.getYdestination() + "," + actor.getZdestination() + " " + (actor.hasAI() ? actor.getAI().getIntention().name() : "NOAI"));
								actor.broadcastPacket(new NpcSay(actor.getObjectId(), 0, actor.getId(), actor.getId() + "/" + actor.getXdestination() + "," + actor.getYdestination() + "," + actor.getZdestination()));
							}
						}
					}
					if (spawns.size() == 0)
					{
						deleteTask.cancel(false);// TODO:NPE
					}
				}
			}
		}
	}
	
	protected class Clean implements Runnable
	{
		@Override
		public void run()
		{
			spawnTask.cancel(false);
			spawnTask = null;
			deleteTask.cancel(false);
			deleteTask = null;
			cleanTask.cancel(false);
			cleanTask = null;
			clean();
		}
	}
	
	public static void main(String[] args)
	{
		new Parade("Parade", "fantasy_isle");
	}
}