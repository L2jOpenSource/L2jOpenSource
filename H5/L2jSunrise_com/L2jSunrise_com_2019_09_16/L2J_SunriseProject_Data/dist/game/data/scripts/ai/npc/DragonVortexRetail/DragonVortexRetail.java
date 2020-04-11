package ai.npc.DragonVortexRetail;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import ai.npc.AbstractNpcAI;

/**
 * @author vGodFather
 */
public class DragonVortexRetail extends AbstractNpcAI
{
	private final boolean debug = false;
	private final boolean _unlimitedBosses = true; // change this to false in order to allow only one boss per vortex
	private static final int VORTEX_1 = 32871;
	private static final int VORTEX_2 = 32892;
	private static final int VORTEX_3 = 32893;
	private static final int VORTEX_4 = 32894;
	
	protected final List<L2Npc> bosses1 = new CopyOnWriteArrayList<>();
	protected final List<L2Npc> bosses2 = new CopyOnWriteArrayList<>();
	protected final List<L2Npc> bosses3 = new CopyOnWriteArrayList<>();
	protected final List<L2Npc> bosses4 = new CopyOnWriteArrayList<>();
	
	private ScheduledFuture<?> _despawnTask1;
	private ScheduledFuture<?> _despawnTask2;
	private ScheduledFuture<?> _despawnTask3;
	private ScheduledFuture<?> _despawnTask4;
	
	protected boolean progress1 = false;
	protected boolean progress2 = false;
	protected boolean progress3 = false;
	protected boolean progress4 = false;
	
	private static final int LARGE_DRAGON_BONE = 17248;
	
	private static final int[] RAIDS =
	{
		25718, // Emerald Horn
		25719, // Dust Rider
		25720, // Bleeding Fly
		25721, // Blackdagger Wing
		25722, // Shadow Summoner
		25723, // Spike Slasher
		25724, // Muscle Bomber
	};
	
	private L2Npc boss1;
	private L2Npc boss2;
	private L2Npc boss3;
	private L2Npc boss4;
	
	protected int boss1ObjId = 0;
	protected int boss2ObjId = 0;
	protected int boss3ObjId = 0;
	protected int boss4ObjId = 0;
	
	private static final int DESPAWN_DELAY = 1800000; // 30min
	
	public DragonVortexRetail()
	{
		super(DragonVortexRetail.class.getSimpleName(), "ai/npc");
		addFirstTalkId(VORTEX_1, VORTEX_2, VORTEX_3, VORTEX_4);
		addStartNpc(VORTEX_1, VORTEX_2, VORTEX_3, VORTEX_4);
		addTalkId(VORTEX_1, VORTEX_2, VORTEX_3, VORTEX_4);
		addKillId(RAIDS);
		
		loadSpawns();
	}
	
	private static void loadSpawns()
	{
		addSpawn(32871, new Location(92225, 113873, -3062));
		addSpawn(32892, new Location(108924, 111992, -3028));
		addSpawn(32893, new Location(110116, 125500, -3664));
		addSpawn(32894, new Location(121172, 113348, -3776));
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "32871.html";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("Spawn"))
		{
			if (!hasQuestItems(player, LARGE_DRAGON_BONE))
			{
				return "32871-02.html";
			}
			
			final int random = getRandom(1000);
			int raid = 0;
			if (random < 292)
			{
				raid = RAIDS[0]; // Emerald Horn 29.2%
			}
			else if (random < 516)
			{
				raid = RAIDS[1]; // Dust Rider 22.4%
			}
			else if (random < 692)
			{
				raid = RAIDS[2]; // Bleeding Fly 17.6%
			}
			else if (random < 808)
			{
				raid = RAIDS[3]; // Blackdagger Wing 11.6%
			}
			else if (random < 900)
			{
				raid = RAIDS[4]; // Spike Slasher 9.2%
			}
			else if (random < 956)
			{
				raid = RAIDS[5]; // Shadow Summoner 5.6%
			}
			else
			{
				raid = RAIDS[6]; // Muscle Bomber 4.4%
			}
			
			switch (npc.getId())
			{
				case VORTEX_1:
				{
					if (!_unlimitedBosses)
					{
						if (progress1)
						{
							if (debug)
							{
								_log.info("Dragon Vortex 32871 has a Raid at Loc: X: " + String.valueOf(boss1.getX()) + " Y: " + String.valueOf(boss1.getY()) + " Z: " + String.valueOf(boss1.getZ()));
							}
							return "32871-03.html";
						}
						
						takeItems(player, LARGE_DRAGON_BONE, 1);
						boss1 = addSpawn(raid, npc.getX() + getRandom(-500, 500), npc.getY() + getRandom(-500, 500), npc.getZ() + 10, 0, false, 0, true);
						progress1 = true;
						bosses1.add(boss1);
						boss1ObjId = boss1.getObjectId();
						_despawnTask1 = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnFirstVortexBoss(), DESPAWN_DELAY);
					}
					else
					{
						takeItems(player, LARGE_DRAGON_BONE, 1);
						addSpawn(raid, npc.getX() + getRandom(-500, 500), npc.getY() + getRandom(-500, 500), npc.getZ() + 10, 0, false, DESPAWN_DELAY, true);
					}
					return "32871-01.html";
				}
				case VORTEX_2:
				{
					if (!_unlimitedBosses)
					{
						if (progress2)
						{
							if (debug)
							{
								_log.info("Dragon Vortex 32892 has a Raid at Loc: X: " + String.valueOf(boss2.getX()) + " Y: " + String.valueOf(boss2.getY()) + " Z: " + String.valueOf(boss2.getZ()));
							}
							return "32871-03.html";
						}
						
						takeItems(player, LARGE_DRAGON_BONE, 1);
						boss2 = addSpawn(raid, npc.getX() + getRandom(-500, 500), npc.getY() + getRandom(-500, 500), npc.getZ() + 10, 0, false, 0, true);
						progress2 = true;
						bosses2.add(boss2);
						boss2ObjId = boss2.getObjectId();
						_despawnTask2 = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnSecondVortexBoss(), DESPAWN_DELAY);
					}
					else
					{
						takeItems(player, LARGE_DRAGON_BONE, 1);
						addSpawn(raid, npc.getX() + getRandom(-500, 500), npc.getY() + getRandom(-500, 500), npc.getZ() + 10, 0, false, DESPAWN_DELAY, true);
					}
					return "32871-01.html";
				}
				case VORTEX_3:
				{
					if (!_unlimitedBosses)
					{
						if (progress3)
						{
							if (debug)
							{
								_log.info("Dragon Vortex 32893 has a Raid at Loc: X: " + String.valueOf(boss3.getX()) + " Y: " + String.valueOf(boss3.getY()) + " Z: " + String.valueOf(boss3.getZ()));
							}
							return "32871-03.html";
						}
						
						takeItems(player, LARGE_DRAGON_BONE, 1);
						boss3 = addSpawn(raid, npc.getX() + getRandom(-500, 500), npc.getY() + getRandom(-500, 500), npc.getZ() + 10, 0, false, 0, true);
						progress3 = true;
						bosses3.add(boss3);
						boss3ObjId = boss3.getObjectId();
						_despawnTask3 = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnThirdVortexBoss(), DESPAWN_DELAY);
					}
					else
					{
						takeItems(player, LARGE_DRAGON_BONE, 1);
						addSpawn(raid, npc.getX() + getRandom(-500, 500), npc.getY() + getRandom(-500, 500), npc.getZ() + 10, 0, false, DESPAWN_DELAY, true);
					}
					return "32871-01.html";
				}
				case VORTEX_4:
				{
					if (!_unlimitedBosses)
					{
						if (progress4)
						{
							if (debug)
							{
								_log.info("Dragon Vortex 32894 has a Raid at Loc: X: " + String.valueOf(boss4.getX()) + " Y: " + String.valueOf(boss4.getY()) + " Z: " + String.valueOf(boss4.getZ()));
							}
							return "32871-03.html";
						}
						
						takeItems(player, LARGE_DRAGON_BONE, 1);
						boss4 = addSpawn(raid, npc.getX() + getRandom(-500, 500), npc.getY() + getRandom(-500, 500), npc.getZ() + 10, 0, false, 0, true);
						progress4 = true;
						bosses4.add(boss4);
						boss4ObjId = boss4.getObjectId();
						_despawnTask4 = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnFourthVortexBoss(), DESPAWN_DELAY);
					}
					else
					{
						takeItems(player, LARGE_DRAGON_BONE, 1);
						addSpawn(raid, npc.getX() + getRandom(-500, 500), npc.getY() + getRandom(-500, 500), npc.getZ() + 10, 0, false, DESPAWN_DELAY, true);
					}
					return "32871-01.html";
				}
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		int npcObjId = npc.getObjectId();
		
		if ((boss1ObjId != 0) && (npcObjId == boss1ObjId) && progress1)
		{
			progress1 = false;
			boss1ObjId = 0;
			bosses1.clear();
			if (_despawnTask1 != null)
			{
				_despawnTask1.cancel(true);
			}
		}
		
		if ((boss2ObjId != 0) && (npcObjId == boss2ObjId) && progress2)
		{
			progress2 = false;
			boss2ObjId = 0;
			bosses2.clear();
			if (_despawnTask2 != null)
			{
				_despawnTask2.cancel(true);
			}
		}
		
		if ((boss3ObjId != 0) && (npcObjId == boss3ObjId) && progress3)
		{
			progress3 = false;
			boss3ObjId = 0;
			bosses3.clear();
			if (_despawnTask3 != null)
			{
				_despawnTask3.cancel(true);
			}
		}
		
		if ((boss4ObjId != 0) && (npcObjId == boss4ObjId) && progress4)
		{
			progress4 = false;
			boss4ObjId = 0;
			bosses4.clear();
			if (_despawnTask4 != null)
			{
				_despawnTask4.cancel(true);
			}
		}
		
		return super.onKill(npc, player, isSummon);
	}
	
	protected class SpawnFirstVortexBoss implements Runnable
	{
		@Override
		public void run()
		{
			for (L2Npc boss : bosses1)
			{
				if (boss != null)
				{
					boss.deleteMe();
					progress1 = false;
				}
			}
			
			boss1ObjId = 0;
			bosses1.clear();
		}
	}
	
	protected class SpawnSecondVortexBoss implements Runnable
	{
		@Override
		public void run()
		{
			for (L2Npc boss : bosses2)
			{
				if (boss != null)
				{
					boss.deleteMe();
					progress2 = false;
				}
			}
			
			boss2ObjId = 0;
			bosses2.clear();
		}
	}
	
	protected class SpawnThirdVortexBoss implements Runnable
	{
		@Override
		public void run()
		{
			for (L2Npc boss : bosses3)
			{
				if (boss != null)
				{
					boss.deleteMe();
					progress3 = false;
				}
			}
			
			boss3ObjId = 0;
			bosses3.clear();
		}
	}
	
	protected class SpawnFourthVortexBoss implements Runnable
	{
		@Override
		public void run()
		{
			for (L2Npc boss : bosses4)
			{
				if (boss != null)
				{
					boss.deleteMe();
					progress4 = false;
				}
			}
			
			boss4ObjId = 0;
			bosses4.clear();
		}
	}
}