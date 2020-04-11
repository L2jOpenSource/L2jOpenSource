package quests.Q10292_SevenSignsGirlofDoubt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2r.gameserver.enums.audio.Sound;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;
import l2r.gameserver.util.Util;

import quests.Q00198_SevenSignsEmbryo.Q00198_SevenSignsEmbryo;

public class Q10292_SevenSignsGirlofDoubt extends Quest
{
	// NPC
	private static final int Hardin = 30832;
	private static final int Wood = 32593;
	private static final int Franz = 32597;
	private static final int Elcadia = 32784;
	private static final int Gruff_looking_Man = 32862;
	private static final int Jeina = 32617;
	// Item
	private static final int Elcadias_Mark = 17226;
	// Mobs
	private static final int[] Mobs =
	{
		22801,
		22802,
		22804,
		22805
	};
	
	private final Map<Integer, InstanceHolder> instanceWorlds = new HashMap<>();
	
	protected static class InstanceHolder
	{
		// List
		List<L2Npc> mobs = new ArrayList<>();
		// State
		boolean spawned = false;
	}
	
	public Q10292_SevenSignsGirlofDoubt()
	{
		super(10292, Q10292_SevenSignsGirlofDoubt.class.getSimpleName(), "Seven Signs, Girl of Doubt");
		addStartNpc(Wood);
		addTalkId(Wood);
		addTalkId(Franz);
		addTalkId(Hardin);
		addTalkId(Elcadia);
		addTalkId(Gruff_looking_Man);
		addTalkId(Jeina);
		addKillId(27422);
		for (int _npc : Mobs)
		{
			addKillId(_npc);
		}
		
		questItemIds = new int[]
		{
			Elcadias_Mark
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(getName());
		
		int instanceId = npc.getInstanceId();
		InstanceHolder holder = instanceWorlds.get(instanceId);
		if ((holder == null) && (instanceId > 0))
		{
			holder = new InstanceHolder();
			instanceWorlds.put(instanceId, holder);
		}
		
		if (st == null)
		{
			return htmltext;
		}
		if (event.equalsIgnoreCase("evil_despawn"))
		{
			if (holder != null)
			{
				holder.spawned = false;
				for (L2Npc h : holder.mobs)
				{
					if (h != null)
					{
						h.deleteMe();
					}
				}
				holder.mobs.clear();
				instanceWorlds.remove(instanceId);
			}
			return null;
		}
		else if (npc.getId() == Wood)
		{
			if (event.equalsIgnoreCase("32593-05.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				playSound(player, Sound.ITEMSOUND_QUEST_ACCEPT);
			}
		}
		else if (npc.getId() == Franz)
		{
			if (event.equalsIgnoreCase("32597-08.htm"))
			{
				st.set("cond", "2");
				playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		else if (npc.getId() == Hardin)
		{
			if (event.equalsIgnoreCase("30832-02.html"))
			{
				st.set("cond", "8");
				playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		else if (npc.getId() == Elcadia)
		{
			if (event.equalsIgnoreCase("32784-03.html"))
			{
				st.set("cond", "3");
				playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
			}
			else if (event.equalsIgnoreCase("32784-14.html"))
			{
				st.set("cond", "7");
				playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
			}
			else if (event.equalsIgnoreCase("spawn"))
			{
				if (holder != null)
				{
					if (!holder.spawned)
					{
						st.takeItems(Elcadias_Mark, -1);
						holder.spawned = true;
						L2Npc evil = addSpawn(27422, 89440, -238016, -9632, 335, false, 0, false, player.getInstanceId());
						evil.setIsNoRndWalk(true);
						holder.mobs.add(evil);
						L2Npc evil1 = addSpawn(27424, 89524, -238131, -9632, 56, false, 0, false, player.getInstanceId());
						evil1.setIsNoRndWalk(true);
						holder.mobs.add(evil1);
						startQuestTimer("evil_despawn", 60000, evil, player);
						return null;
					}
				}
				htmltext = "32593-02.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = getQuestState(player, true);
		
		if (st == null)
		{
			return htmltext;
		}
		else if (npc.getId() == Wood)
		{
			if (st.getState() == State.COMPLETED)
			{
				htmltext = "32593-02.htm";
			}
			else if (player.getLevel() < 81)
			{
				htmltext = "32593-03.htm";
			}
			else if ((player.getQuestState(Q00198_SevenSignsEmbryo.class.getSimpleName()) == null) || (player.getQuestState(Q00198_SevenSignsEmbryo.class.getSimpleName()).getState() != State.COMPLETED))
			{
				htmltext = "32593-03.htm";
			}
			else if (st.getState() == State.CREATED)
			{
				htmltext = "32593-01.htm";
			}
			else if (st.getInt("cond") >= 1)
			{
				htmltext = "32593-07.html";
			}
		}
		else if (npc.getId() == Franz)
		{
			if (st.getInt("cond") == 1)
			{
				htmltext = "32597-01.htm";
			}
			else if (st.getInt("cond") == 2)
			{
				htmltext = "32597-03.html";
			}
		}
		else if (npc.getId() == Elcadia)
		{
			if (st.getInt("cond") == 2)
			{
				htmltext = "32784-01.html";
			}
			else if (st.getInt("cond") == 3)
			{
				htmltext = "32784-04.html";
			}
			else if (st.getInt("cond") == 4)
			{
				playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
				st.set("cond", "5");
				htmltext = "32784-05.html";
			}
			else if (st.getInt("cond") == 5)
			{
				playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
				htmltext = "32784-05.html";
			}
			else if (st.getInt("cond") == 6)
			{
				playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
				htmltext = "32784-11.html";
			}
			else if (st.getInt("cond") == 8)
			{
				if (player.isSubClassActive())
				{
					htmltext = "32784-18.html";
				}
				else
				{
					playSound(player, Sound.ITEMSOUND_QUEST_FINISH);
					st.addExpAndSp(10000000, 1000000);
					st.exitQuest(false);
					htmltext = "32784-16.html";
				}
			}
		}
		else if (npc.getId() == Hardin)
		{
			if (st.getInt("cond") == 7)
			{
				htmltext = "30832-01.html";
			}
			else if (st.getInt("cond") == 8)
			{
				htmltext = "30832-04.html";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getName());
		
		if ((st != null) && (st.getInt("cond") == 3) && Util.contains(Mobs, npc.getId()) && (st.getQuestItemsCount(Elcadias_Mark) < 10) && (st.getQuestItemsCount(Elcadias_Mark) != 9))
		{
			st.giveItems(Elcadias_Mark, 1);
			playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
		}
		else if ((st != null) && (st.getInt("cond") == 3) && Util.contains(Mobs, npc.getId()) && (st.getQuestItemsCount(Elcadias_Mark) >= 9))
		{
			st.giveItems(Elcadias_Mark, 1);
			playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
			st.set("cond", "4");
		}
		else if ((st != null) && (st.getInt("cond") == 5) && (npc.getId() == 27422))
		{
			int instanceid = npc.getInstanceId();
			InstanceHolder holder = instanceWorlds.get(instanceid);
			if (holder == null)
			{
				return null;
			}
			for (L2Npc h : holder.mobs)
			{
				if (h != null)
				{
					h.deleteMe();
				}
			}
			holder.spawned = false;
			holder.mobs.clear();
			instanceWorlds.remove(instanceid);
			st.set("cond", "6");
			playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
		}
		return super.onKill(npc, player, isPet);
	}
}
