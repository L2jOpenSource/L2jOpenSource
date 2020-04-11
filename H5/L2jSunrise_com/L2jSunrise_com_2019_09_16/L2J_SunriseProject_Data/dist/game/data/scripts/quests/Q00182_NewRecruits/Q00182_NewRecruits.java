/*
 * Copyright (C) 2004-2017 L2J DataPack
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
package quests.Q00182_NewRecruits;

import l2r.gameserver.enums.CategoryType;
import l2r.gameserver.enums.Race;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;

/**
 * New Recruits (182)
 * @author Adry_85
 * @since 2.6.0.0
 */
public class Q00182_NewRecruits extends Quest
{
	// NPCs
	private static final int KEKROPUS = 32138;
	private static final int MENACING_MACHINE = 32258;
	// Misc
	private static final int MIN_LEVEL = 17;
	private static final int MAX_LEVEL = 21;
	// Rewards
	private static final int RED_CRESCENT_EARRING = 10122;
	private static final int RING_OF_DEVOTION = 10124;
	
	public Q00182_NewRecruits()
	{
		super(182, Q00182_NewRecruits.class.getSimpleName(), "New Recruits");
		addStartNpc(KEKROPUS);
		addTalkId(KEKROPUS, MENACING_MACHINE);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = getQuestState(player, false);
		if (st == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "32138-03.htm":
			{
				if ((player.getLevel() >= MIN_LEVEL) && (player.getLevel() <= MAX_LEVEL) && player.isInCategory(CategoryType.FIRST_CLASS_GROUP))
				{
					htmltext = event;
				}
				break;
			}
			case "32138-04.htm":
			{
				if ((player.getLevel() >= MIN_LEVEL) && (player.getLevel() <= MAX_LEVEL) && player.isInCategory(CategoryType.FIRST_CLASS_GROUP))
				{
					st.startQuest();
					st.setMemoState(1);
					htmltext = event;
				}
				break;
			}
			case "32258-02.html":
			case "32258-03.html":
			{
				if (st.isMemoState(1))
				{
					htmltext = event;
				}
				break;
			}
			case "32258-04.html":
			{
				if (st.isMemoState(1))
				{
					giveItems(player, RED_CRESCENT_EARRING, 2);
					st.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
			case "32258-05.html":
			{
				if (st.isMemoState(1))
				{
					giveItems(player, RING_OF_DEVOTION, 2);
					st.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState st = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		if (st.isCompleted())
		{
			if (npc.getId() == KEKROPUS)
			{
				htmltext = getAlreadyCompletedMsg(player);
			}
		}
		else if (st.isCreated())
		{
			if (player.getRace() == Race.KAMAEL)
			{
				htmltext = "32138-01.htm";
			}
			else if ((player.getLevel() >= MIN_LEVEL) && (player.getLevel() <= MAX_LEVEL) && player.isInCategory(CategoryType.FIRST_CLASS_GROUP))
			{
				htmltext = "32138-02.htm";
			}
			else if ((player.getLevel() < MIN_LEVEL) || (player.getLevel() > MAX_LEVEL) || !player.isInCategory(CategoryType.FIRST_CLASS_GROUP))
			{
				htmltext = "32138-05.htm";
			}
		}
		else if (st.isStarted())
		{
			switch (npc.getId())
			{
				case KEKROPUS:
				{
					if (st.isMemoState(1))
					{
						htmltext = "32138-06.html";
					}
					break;
				}
				case MENACING_MACHINE:
				{
					if (st.isMemoState(1))
					{
						htmltext = "32258-01.html";
					}
					break;
				}
			}
		}
		return htmltext;
	}
}
