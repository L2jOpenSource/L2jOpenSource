package l2r.gameserver.scripts.ai.npc;

import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;

public class DragonVortex extends Quest
{
	private static final int VORTEX_1 = 32871;
	private static final int VORTEX_2 = 32892;
	private static final int VORTEX_3 = 32893;
	private static final int VORTEX_4 = 32894;
	
	protected final FastList<L2Npc> bosses1 = new FastList<>();
	protected final FastList<L2Npc> bosses2 = new FastList<>();
	protected final FastList<L2Npc> bosses3 = new FastList<>();
	protected final FastList<L2Npc> bosses4 = new FastList<>();
	
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
		25724, // Muscle Bomber
		25723, // Spike Slasher
		25722, // Shadow Summoner
		25721, // Blackdagger Wing
		25720, // Bleeding Fly
		25719, // Dust Rider
		25718, // Emerald Horn
	};
	
	private L2Npc boss1;
	private L2Npc boss2;
	private L2Npc boss3;
	private L2Npc boss4;
	
	protected int boss1ObjId = 0;
	protected int boss2ObjId = 0;
	protected int boss3ObjId = 0;
	protected int boss4ObjId = 0;
	
	private static final int DESPAWN_DELAY = 3600000;
	
	public DragonVortex(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addFirstTalkId(VORTEX_1, VORTEX_2, VORTEX_3, VORTEX_4);
		addStartNpc(VORTEX_1, VORTEX_2, VORTEX_3, VORTEX_4);
		addTalkId(VORTEX_1, VORTEX_2, VORTEX_3, VORTEX_4);
		
		for (int i : RAIDS)
		{
			addKillId(i);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("Spawn"))
		{
			if (!hasQuestItems(player, LARGE_DRAGON_BONE))
			{
				return "32871-02.htm";
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
			
			if (npc.getId() == VORTEX_1)
			{
				if (progress1)
				{
					return "32871-03.htm";
				}
				
				takeItems(player, LARGE_DRAGON_BONE, 1);
				boss1 = addSpawn(raid, new Location(player.getX() - 300, player.getY() - 100, player.getZ() - 2, player.getHeading()), false, 0);
				progress1 = true;
				if (boss1 != null)
				{
					bosses1.add(boss1);
					boss1ObjId = boss1.getObjectId();
					_despawnTask1 = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnFirstVortrexBoss(), DESPAWN_DELAY);
				}
				return "32871-01.htm";
			}
			
			if (npc.getId() == VORTEX_2)
			{
				if (progress2)
				{
					return "32871-03.htm";
				}
				
				takeItems(player, LARGE_DRAGON_BONE, 1);
				boss2 = addSpawn(raid, new Location(player.getX() - 300, player.getY() - 100, player.getZ() - 2, player.getHeading()), false, 0);
				progress2 = true;
				if (boss2 != null)
				{
					bosses2.add(boss2);
					boss2ObjId = boss2.getObjectId();
					_despawnTask2 = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnSecondVortrexBoss(), DESPAWN_DELAY);
				}
				return "32871-01.htm";
			}
			
			if (npc.getId() == VORTEX_3)
			{
				if (progress3)
				{
					return "32871-03.htm";
				}
				
				takeItems(player, LARGE_DRAGON_BONE, 1);
				boss3 = addSpawn(raid, new Location(player.getX() - 300, player.getY() - 100, player.getZ() - 2, player.getHeading()), false, 0);
				progress3 = true;
				if (boss3 != null)
				{
					bosses3.add(boss3);
					boss3ObjId = boss3.getObjectId();
					_despawnTask3 = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnThirdVortrexBoss(), DESPAWN_DELAY);
				}
				return "32871-01.htm";
			}
			
			if (npc.getId() == VORTEX_4)
			{
				if (progress4)
				{
					return "32871-03.htm";
				}
				
				takeItems(player, LARGE_DRAGON_BONE, 1);
				boss4 = addSpawn(raid, new Location(player.getX() - 300, player.getY() - 100, player.getZ() - 2, player.getHeading()), false, 0);
				progress4 = true;
				if (boss4 != null)
				{
					bosses4.add(boss4);
					boss4ObjId = boss4.getObjectId();
					_despawnTask4 = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnFourthVortrexBoss(), DESPAWN_DELAY);
				}
				return "32871-01.htm";
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		return "32871.htm";
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
	
	protected class SpawnFirstVortrexBoss implements Runnable
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
	
	protected class SpawnSecondVortrexBoss implements Runnable
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
	
	protected class SpawnThirdVortrexBoss implements Runnable
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
	
	protected class SpawnFourthVortrexBoss implements Runnable
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
	
	public static void main(String[] args)
	{
		new DragonVortex(-1, "DragonVortex", "ai/npc");
	}
}