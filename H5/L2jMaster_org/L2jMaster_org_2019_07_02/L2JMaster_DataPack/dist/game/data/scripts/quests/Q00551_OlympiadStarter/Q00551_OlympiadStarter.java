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
package quests.Q00551_OlympiadStarter;

import com.l2jserver.gameserver.enums.QuestType;
import com.l2jserver.gameserver.enums.audio.Sound;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.olympiad.CompetitionType;
import com.l2jserver.gameserver.model.olympiad.Participant;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.quest.State;

/**
 * Olympiad Starter (551)
 * @author Gnacik, Adry_85
 */
public class Q00551_OlympiadStarter extends Quest
{
	private static final int MANAGER = 31688;
	
	private static final int CERT_3 = 17238;
	private static final int CERT_5 = 17239;
	private static final int CERT_10 = 17240;
	
	private static final int OLY_CHEST = 17169;
	private static final int MEDAL_OF_GLORY = 21874;
	
	public Q00551_OlympiadStarter()
	{
		super(551, Q00551_OlympiadStarter.class.getSimpleName(), "Olympiad Starter");
		addStartNpc(MANAGER);
		addTalkId(MANAGER);
		registerQuestItems(CERT_3, CERT_5, CERT_10);
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
		
		switch (event)
		{
			case "31688-03.html":
			{
				st.startQuest();
				st.setMemoState(1);
				st.setMemoStateEx(1, 0);
				break;
			}
			case "31688-04.html":
			{
				if ((st.getQuestItemsCount(CERT_3) + st.getQuestItemsCount(CERT_5)) > 0)
				{
					if (st.hasQuestItems(CERT_3))
					{
						st.giveItems(OLY_CHEST, 1);
						st.takeItems(CERT_3, -1);
					}
					
					if (st.hasQuestItems(CERT_5))
					{
						st.giveItems(OLY_CHEST, 1);
						st.giveItems(MEDAL_OF_GLORY, 3);
						st.takeItems(CERT_5, -1);
					}
					
					st.exitQuest(QuestType.DAILY, true);
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public void onOlympiadLose(L2PcInstance loser, CompetitionType type)
	{
		if (loser != null)
		{
			final QuestState st = getQuestState(loser, false);
			if ((st != null) && st.isStarted() && st.isMemoState(1))
			{
				final int memoStateEx = st.getMemoStateEx(1);
				if (memoStateEx == 9)
				{
					st.setMemoStateEx(1, st.getMemoStateEx(1) + 1);
					st.setMemoState(2);
					st.setCond(2, true);
					st.giveItems(CERT_10, 1);
				}
				else if (memoStateEx < 9)
				{
					if (st.isMemoStateEx(1, 2))
					{
						st.giveItems(CERT_3, 1);
					}
					else if (st.isMemoStateEx(1, 4))
					{
						st.giveItems(CERT_5, 1);
					}
					
					st.setMemoStateEx(1, st.getMemoStateEx(1) + 1);
					st.playSound(Sound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
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
			if ((st != null) && st.isStarted() && st.isMemoState(1))
			{
				final int memoStateEx = st.getMemoStateEx(1);
				if (memoStateEx == 9)
				{
					st.setMemoStateEx(1, st.getMemoStateEx(1) + 1);
					st.setMemoState(2);
					st.setCond(2, true);
					st.giveItems(CERT_10, 1);
				}
				else if (memoStateEx < 9)
				{
					if (st.isMemoStateEx(1, 2))
					{
						st.giveItems(CERT_3, 1);
					}
					else if (st.isMemoStateEx(1, 4))
					{
						st.giveItems(CERT_5, 1);
					}
					
					st.setMemoStateEx(1, st.getMemoStateEx(1) + 1);
					st.playSound(Sound.ITEMSOUND_QUEST_ITEMGET);
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
			if ((st != null) && st.isStarted() && st.isMemoState(1))
			{
				final int memoStateEx = st.getMemoStateEx(1);
				if (memoStateEx == 9)
				{
					st.setMemoStateEx(1, st.getMemoStateEx(1) + 1);
					st.setMemoState(2);
					st.setCond(2, true);
					st.giveItems(CERT_10, 1);
				}
				else if (memoStateEx < 9)
				{
					if (st.isMemoStateEx(1, 2))
					{
						st.giveItems(CERT_3, 1);
					}
					else if (st.isMemoStateEx(1, 4))
					{
						st.giveItems(CERT_5, 1);
					}
					
					st.setMemoStateEx(1, st.getMemoStateEx(1) + 1);
					st.playSound(Sound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = getQuestState(player, true);
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
		else if (st.isStarted())
		{
			if (st.isMemoState(1))
			{
				htmltext = (((st.getQuestItemsCount(CERT_3) + st.getQuestItemsCount(CERT_5) + st.getQuestItemsCount(CERT_10)) > 0) ? "31688-07.html" : "31688-06.html");
			}
			else if (st.isMemoState(2))
			{
				st.giveItems(OLY_CHEST, 4);
				st.giveItems(MEDAL_OF_GLORY, 5);
				st.exitQuest(QuestType.DAILY, true);
				htmltext = "31688-04.html";
			}
		}
		return htmltext;
	}
}