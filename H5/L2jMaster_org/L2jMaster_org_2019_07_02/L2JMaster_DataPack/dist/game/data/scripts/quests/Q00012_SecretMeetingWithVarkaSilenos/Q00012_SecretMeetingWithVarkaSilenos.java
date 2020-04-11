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
package quests.Q00012_SecretMeetingWithVarkaSilenos;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;

/**
 * Secret Meeting With Varka Silenos (12)
 * @author ivantotov
 */
public final class Q00012_SecretMeetingWithVarkaSilenos extends Quest
{
	// NPCs
	private static final int HELMUT = 31258;
	private static final int CADMON = 31296;
	private static final int NARAN_ASHANUK = 31378;
	// Item
	private static final int MUNITIONS_BOX = 7232;
	// Misc
	private static final int MIN_LEVEL = 74;
	
	public Q00012_SecretMeetingWithVarkaSilenos()
	{
		super(12, Q00012_SecretMeetingWithVarkaSilenos.class.getSimpleName(), "Secret Meeting With Varka Silenos");
		addStartNpc(CADMON);
		addTalkId(CADMON, HELMUT, NARAN_ASHANUK);
		registerQuestItems(MUNITIONS_BOX);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "31296-03.htm":
			{
				qs.startQuest();
				qs.setMemoState(11);
				htmltext = event;
				break;
			}
			case "31258-02.html":
			{
				giveItems(player, MUNITIONS_BOX, 1);
				qs.setMemoState(21);
				qs.setCond(2, true);
				htmltext = event;
				break;
			}
			case "31378-02.html":
			{
				if (hasQuestItems(player, MUNITIONS_BOX))
				{
					addExpAndSp(player, 233125, 18142);
					qs.exitQuest(false, true);
					htmltext = event;
				}
				else
				{
					htmltext = "31378-03.html";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		if (qs.isCreated())
		{
			if (npc.getId() == CADMON)
			{
				htmltext = (player.getLevel() >= MIN_LEVEL) ? "31296-01.htm" : "31296-02.html";
			}
		}
		else if (qs.isStarted())
		{
			if (npc.getId() == CADMON)
			{
				if (qs.isMemoState(11))
				{
					htmltext = "31296-04.html";
				}
			}
			else if (npc.getId() == HELMUT)
			{
				if (qs.isMemoState(11))
				{
					htmltext = "31258-01.html";
				}
				else if (qs.isMemoState(21))
				{
					htmltext = "31258-03.html";
				}
			}
			else if (npc.getId() == NARAN_ASHANUK)
			{
				if (hasQuestItems(player, MUNITIONS_BOX) && qs.isMemoState(21))
				{
					htmltext = "31378-01.html";
				}
			}
		}
		else if (qs.isCompleted())
		{
			if (npc.getId() == CADMON)
			{
				htmltext = getAlreadyCompletedMsg(player);
			}
		}
		return htmltext;
	}
}