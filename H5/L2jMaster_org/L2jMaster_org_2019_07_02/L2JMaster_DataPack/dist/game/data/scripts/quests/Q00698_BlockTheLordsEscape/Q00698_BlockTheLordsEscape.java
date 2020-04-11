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
package quests.Q00698_BlockTheLordsEscape;

import com.l2jserver.Config;
import com.l2jserver.gameserver.enums.audio.Sound;
import com.l2jserver.gameserver.instancemanager.SeedOfInfinityManager;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.quest.State;
import com.l2jserver.util.Rnd;

/**
 * Block The Lords Escape (698)
 * @author MaGa1, Sacrifice
 */
public final class Q00698_BlockTheLordsEscape extends Quest
{
	private static final int TEPIOS = 32603;
	
	private static final int VESPER_STONE = 14052;
	
	public Q00698_BlockTheLordsEscape()
	{
		super(698, Q00698_BlockTheLordsEscape.class.getSimpleName(), "Block the Lords Escape");
		
		addStartNpc(TEPIOS);
		addTalkId(TEPIOS);
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
		QuestState st = getQuestState(player, false);
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("32603-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound(Sound.ITEMSOUND_QUEST_ACCEPT);
		}
		return htmltext;
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
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
				if ((player.getLevel() < 75) || (player.getLevel() > 85))
				{
					st.exitQuest(true);
					return "32603-00.htm";
				}
				if (SeedOfInfinityManager.getCurrentStage() != 5)
				{
					st.exitQuest(true);
					return "32603-00a.htm";
				}
				htmltext = "32603-01.htm";
				break;
			case State.STARTED:
				if ((st.getInt("cond") == 1) && (st.getInt("defenceDone") == 1))
				{
					htmltext = "32603-05.htm";
					st.giveItems(VESPER_STONE, (int) Config.RATE_QUEST_REWARD * Rnd.get(5, 8));
					st.playSound(Sound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(true);
				}
				else
				{
					htmltext = "32603-04.htm";
				}
				break;
		}
		return htmltext;
	}
}