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
package quests.Q00553_OlympiadUndefeated;

import l2r.gameserver.enums.QuestType;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.olympiad.Participant;
import l2r.gameserver.model.entity.olympiad.enums.CompetitionType;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;

/**
 * Olympiad Undefeated (553)
 * @author lion
 */
public class Q00553_OlympiadUndefeated extends Quest
{
	// NPC
	private static final int MANAGER = 31688;
	// Items
	private static final int WIN_CONF_2 = 17244;
	private static final int WIN_CONF_5 = 17245;
	private static final int WIN_CONF_10 = 17246;
	private static final int OLY_CHEST = 17169;
	private static final int MEDAL_OF_GLORY = 21874;
	
	public Q00553_OlympiadUndefeated()
	{
		super(553, Q00553_OlympiadUndefeated.class.getSimpleName(), "Olympiad Undefeated");
		addStartNpc(MANAGER);
		addTalkId(MANAGER);
		registerQuestItems(WIN_CONF_2, WIN_CONF_5, WIN_CONF_10);
		addOlympiadMatchFinishId();
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = getQuestState(player, false);
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		String htmltext = event;
		
		if (event.equalsIgnoreCase("31688-03.html"))
		{
			st.startQuest();
		}
		else if (event.equalsIgnoreCase("31688-04.html"))
		{
			final long count = st.getQuestItemsCount(WIN_CONF_2) + st.getQuestItemsCount(WIN_CONF_5);
			if (count > 0)
			{
				if ((st.getQuestItemsCount(WIN_CONF_2) > 0) && (st.getQuestItemsCount(WIN_CONF_5) > 0))
				{
					st.giveItems(OLY_CHEST, 5);
					st.giveItems(MEDAL_OF_GLORY, 5);
				}
				else
				{
					st.giveItems(OLY_CHEST, 1);
				}
				
				st.exitQuest(QuestType.DAILY, true);
			}
			else
			{
				htmltext = getNoQuestMsg(player);
			}
		}
		else if (event.equalsIgnoreCase("31688-06.html"))
		{
			if ((st.getQuestItemsCount(WIN_CONF_2) > 0) && (st.getQuestItemsCount(WIN_CONF_5) > 0) && (st.getQuestItemsCount(WIN_CONF_10) > 0))
			{
				st.giveItems(OLY_CHEST, 6);
				st.giveItems(MEDAL_OF_GLORY, 10);
				st.exitQuest(QuestType.DAILY, true);
			}
			else if ((st.getQuestItemsCount(WIN_CONF_2) > 0) && (st.getQuestItemsCount(WIN_CONF_5) > 0))
			{
				st.giveItems(OLY_CHEST, 5);
				st.giveItems(MEDAL_OF_GLORY, 5);
				st.exitQuest(QuestType.DAILY, true);
			}
			else
			{
				htmltext = getNoQuestMsg(player);
			}
		}
		return htmltext;
	}
	
	@Override
	public void onOlympiadMatchFinish(Participant winner, Participant looser, CompetitionType type)
	{
		if (winner != null)
		{
			final L2PcInstance player = winner.getPlayer();
			if (player == null)
			{
				return;
			}
			
			final QuestState st = getQuestState(player, false);
			if ((st != null) && st.isStarted() && (st.isCond(1)))
			{
				final int matches = st.getInt("undefeatable") + 1;
				st.set("undefeatable", String.valueOf(matches));
				switch (matches)
				{
					case 2:
						if (!st.hasQuestItems(WIN_CONF_2))
						{
							st.giveItems(WIN_CONF_2, 1);
						}
						break;
					case 5:
						if (!st.hasQuestItems(WIN_CONF_5))
						{
							st.giveItems(WIN_CONF_5, 1);
						}
						break;
					case 10:
						if (!st.hasQuestItems(WIN_CONF_10))
						{
							st.giveItems(WIN_CONF_10, 1);
							st.setCond(2);
						}
						break;
				}
			}
		}
		
		if (looser != null)
		{
			final L2PcInstance player = looser.getPlayer();
			if (player == null)
			{
				return;
			}
			
			final QuestState st = getQuestState(player, false);
			if ((st != null) && st.isStarted() && (st.isCond(1)))
			{
				st.unset("undefeatable");
				st.takeItems(WIN_CONF_2, -1);
				st.takeItems(WIN_CONF_5, -1);
				st.takeItems(WIN_CONF_10, -1);
			}
		}
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = getQuestState(player, true);
		if (st == null)
		{
			return htmltext;
		}
		
		if ((player.getLevel() < 75) || !player.isNoble())
		{
			htmltext = "31688-00.htm";
		}
		else if (st.isCreated())
		{
			htmltext = "31688-01.htm";
		}
		else if (st.isCompleted())
		{
			if (st.isNowAvailable())
			{
				st.setState(State.CREATED);
				htmltext = (player.getLevel() < 75) || !player.isNoble() ? "31688-00.htm" : "31688-01.htm";
			}
			else
			{
				htmltext = "31688-05.html";
			}
		}
		else
		{
			final long count = st.getQuestItemsCount(WIN_CONF_2) + st.getQuestItemsCount(WIN_CONF_5) + st.getQuestItemsCount(WIN_CONF_10);
			if ((count >= 3) && st.isCond(2))
			{
				st.giveItems(OLY_CHEST, 6);
				st.giveItems(MEDAL_OF_GLORY, 10);
				st.exitQuest(QuestType.DAILY, true);
				htmltext = "31688-04.html";
			}
			else
			{
				if (count > 2)
				{
					htmltext = "31688-w" + 2 + ".html";
				}
				else
				{
					htmltext = "31688-w" + count + ".html";
				}
			}
		}
		return htmltext;
	}
}
