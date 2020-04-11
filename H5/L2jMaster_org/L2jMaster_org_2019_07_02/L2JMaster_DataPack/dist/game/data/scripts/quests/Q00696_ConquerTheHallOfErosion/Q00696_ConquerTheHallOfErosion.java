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
package quests.Q00696_ConquerTheHallOfErosion;

import com.l2jserver.gameserver.enums.audio.Sound;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.quest.State;

/**
 * Conquer The Hall Of Erosion (696)
 * @author MaGa1, Sacrifice
 */
public final class Q00696_ConquerTheHallOfErosion extends Quest
{
	private static final int TEPIOS = 32603;
	private static final int COHEMENES = 25634;
	
	private static final int MARK_OF_KEUCEREUS_STAGE_1 = 13691;
	private static final int MARK_OF_KEUCEREUS_STAGE_2 = 13692;
	
	public Q00696_ConquerTheHallOfErosion()
	{
		super(696, Q00696_ConquerTheHallOfErosion.class.getSimpleName(), "Conquer The Hall Of Erosion");
		
		addStartNpc(TEPIOS);
		addTalkId(TEPIOS);
		addKillId(COHEMENES);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = getQuestState(player, false);
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("32603-02.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound(Sound.ITEMSOUND_QUEST_ACCEPT);
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
		
		if (st.getInt("cohemenesDone") < 0)
		{
			st.set("cohemenesDone", 0);
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				if ((player.getLevel() >= 75) && (player.getLevel() <= 85))
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
				if (st.getInt("cohemenesDone") != 0)
				{
					if (st.getQuestItemsCount(MARK_OF_KEUCEREUS_STAGE_2) < 1)
					{
						st.takeItems(MARK_OF_KEUCEREUS_STAGE_1, 1);
						st.giveItems(MARK_OF_KEUCEREUS_STAGE_2, 1);
					}
					htmltext = "32603-04.htm";
					st.playSound(Sound.ITEMSOUND_QUEST_FINISH);
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
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, 1);
		
		if (partyMember == null)
		{
			return super.onKill(npc, player, isSummon);
		}
		
		QuestState st = getQuestState(partyMember, false);
		if (st == null)
		{
			return null;
		}
		
		if (st.getInt("cond") == 1)
		{
			st.set("cohemenesDone", 1);
		}
		
		if (player.getParty() != null)
		{
			QuestState st2;
			for (L2PcInstance pmember : player.getParty().getMembers())
			{
				st2 = pmember.getQuestState(Q00696_ConquerTheHallOfErosion.class.getSimpleName());
				if ((st2 != null) && (st.getInt("cond") == 1) && (pmember.getObjectId() != partyMember.getObjectId()))
				{
					st.set("cohemenesDone", 1);
				}
			}
		}
		return super.onKill(npc, player, isSummon);
	}
}