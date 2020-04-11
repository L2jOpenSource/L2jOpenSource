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
package quests.Q00697_DefendtheHallofErosion;

import l2r.gameserver.enums.audio.Sound;
import l2r.gameserver.instancemanager.SoIManager;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;
import l2r.util.Rnd;

public class Q00697_DefendtheHallofErosion extends Quest
{
	private static final int TEPIOS = 32603;
	private static final int VesperNobleEnhanceStone = 14052;
	
	public Q00697_DefendtheHallofErosion()
	{
		super(697, Q00697_DefendtheHallofErosion.class.getSimpleName(), "Defend the Hall of Erosion");
		addStartNpc(TEPIOS);
		addTalkId(TEPIOS);
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("32603-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			playSound(player, Sound.ITEMSOUND_QUEST_ACCEPT);
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
		
		int cond = st.getInt("cond");
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() < 75)
				{
					htmltext = "32603-00.htm";
					st.exitQuest(true);
				}
				if (SoIManager.getCurrentStage() != 4)
				{
					htmltext = "32603-00a.htm";
					st.exitQuest(true);
				}
				htmltext = "32603-01.htm";
				break;
			case State.STARTED:
				if ((cond == 1) && (st.getInt("defenceDone") == 0))
				{
					htmltext = "32603-04.htm";
				}
				else if ((cond == 1) && (st.getInt("defenceDone") != 0))
				{
					st.giveItems(VesperNobleEnhanceStone, Rnd.get(12, 20));
					htmltext = "32603-05.htm";
					playSound(player, Sound.ITEMSOUND_QUEST_FINISH);
					st.unset("defenceDone");
					st.exitQuest(true);
				}
				break;
		}
		return htmltext;
	}
}