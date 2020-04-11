/*
 * Copyright (C) 2004-2013 L2J DataPack
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
package quests.Q00696_ConquertheHallofErosion;

import l2r.gameserver.enums.audio.Sound;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;

public final class Q00696_ConquertheHallofErosion extends Quest
{
	private static final int TEPIOS = 32603;
	private static final int MARK_OF_KEUCEREUS_STAGE_1 = 13691;
	private static final int MARK_OF_KEUCEREUS_STAGE_2 = 13692;
	
	public Q00696_ConquertheHallofErosion()
	{
		super(696, Q00696_ConquertheHallofErosion.class.getSimpleName(), "Conquer the Hall of Erosion");
		addStartNpc(TEPIOS);
		addTalkId(TEPIOS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("32603-02.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			playSound(player, Sound.ITEMSOUND_QUEST_ACCEPT);
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
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 75)
				{
					if ((st.getQuestItemsCount(MARK_OF_KEUCEREUS_STAGE_1) > 0) || (st.getQuestItemsCount(MARK_OF_KEUCEREUS_STAGE_2) > 0))
					{
						htmltext = "32603-01.htm";
					}
					else
					{
						htmltext = "32603-05.htm";
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "32603-00.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				if (st.getInt("cohemenes") != 0)
				{
					if (st.getQuestItemsCount(MARK_OF_KEUCEREUS_STAGE_2) < 1)
					{
						st.takeItems(MARK_OF_KEUCEREUS_STAGE_1, 1);
						st.giveItems(MARK_OF_KEUCEREUS_STAGE_2, 1);
					}
					htmltext = "32603-04.htm";
					playSound(player, Sound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(true);
				}
				else
				{
					htmltext = "32603-01a.htm";
				}
				break;
		}
		return htmltext;
	}
}