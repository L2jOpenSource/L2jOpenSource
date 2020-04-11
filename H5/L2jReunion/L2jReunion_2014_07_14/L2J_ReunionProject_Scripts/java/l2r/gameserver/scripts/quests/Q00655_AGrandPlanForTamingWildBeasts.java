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
package l2r.gameserver.scripts.quests;

import l2r.gameserver.instancemanager.CHSiegeManager;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.clanhall.SiegableHall;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;

/**
 * Created by GodFather 14.06.2013
 */
public class Q00655_AGrandPlanForTamingWildBeasts extends Quest
{
	private static final int MESSENGER = 35627;
	
	private final static int STONE = 8084;
	private final static int TRAINER_LICENSE = 8293;
	
	private static final SiegableHall BEAST_STRONGHOLD = CHSiegeManager.getInstance().getSiegableHall(63);
	
	public Q00655_AGrandPlanForTamingWildBeasts()
	{
		super(655, Q00655_AGrandPlanForTamingWildBeasts.class.getSimpleName(), "");
		addStartNpc(MESSENGER);
		addTalkId(MESSENGER);
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (npc.getId() == MESSENGER)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (BEAST_STRONGHOLD.getSiege().getAttackers().size() >= 5)
					{
						htmltext = "35627-00.htm";
					}
					else
					{
						htmltext = "35627-01.htm";
						st.setState(State.STARTED);
						st.set("cond", "1");
						st.playSound("ItemSound.quest_accept");
					}
					break;
				case State.STARTED:
					if (st.getQuestItemsCount(STONE) < 10)
					{
						htmltext = "35627-02.htm";
					}
					else
					{
						st.takeItems(STONE, 10);
						st.giveItems(TRAINER_LICENSE, 1);
						st.exitQuest(true);
						htmltext = "35627-03.htm";
					}
					break;
			}
		}
		return htmltext;
	}
	
	public static void checkCrystalofPurity(L2PcInstance player)
	{
		final QuestState st = player.getQuestState(Q00655_AGrandPlanForTamingWildBeasts.class.getSimpleName());
		if ((st != null) && st.isCond(1))
		{
			if (st.getQuestItemsCount(STONE) < 10)
			{
				st.giveItems(STONE, 1);
			}
		}
	}
}