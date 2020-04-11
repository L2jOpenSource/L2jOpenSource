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
package quests.Q00693_DefeatingDragonkinRemnants;

import l2r.gameserver.enums.audio.Sound;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.util.Rnd;

/**
 * @author GodFather
 */
public class Q00693_DefeatingDragonkinRemnants extends Quest
{
	private static final int EDRIC = 32527;
	private static final int REWARD_CHANCE = 60;
	
	public Q00693_DefeatingDragonkinRemnants()
	{
		super(693, Q00693_DefeatingDragonkinRemnants.class.getSimpleName(), "Defeating Dragonkin Remnants");
		addStartNpc(EDRIC);
		addTalkId(EDRIC);
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
		if (npc.getId() == EDRIC)
		{
			if (event.equalsIgnoreCase("32527-05.htm"))
			{
				st.setState((byte) 1);
				st.set("cond", "1");
				st.unset("timeDiff");
				playSound(player, Sound.ITEMSOUND_QUEST_ACCEPT);
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
		if (npc.getId() == EDRIC)
		{
			if (player.getLevel() < 75)
			{
				htmltext = "32527-00.htm";
			}
			else if (st.getState() == 0)
			{
				htmltext = "32527-01.htm";
			}
			else if (player.isGM())
			{
				st.setState((byte) 1);
				st.set("cond", "1");
				htmltext = "32527-10.html";
			}
			else if (st.getInt("cond") == 1)
			{
				L2Party party = player.getParty();
				if (st.getInt("timeDiff") > 0)
				{
					if (giveReward(st, st.getInt("timeDiff")))
					{
						htmltext = "32527-reward.html";
					}
					else
					{
						htmltext = "32527-noreward.html";
					}
					
					st.unset("timeDiff");
					st.unset("cond");
					playSound(player, Sound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(true);
				}
				else if (party == null)
				{
					htmltext = "32527-noparty.html";
				}
				else if (!party.getLeader().equals(player))
				{
					htmltext = prepareHtml(player, "32527-noleader.html", party.getLeader().getName());
				}
				else
				{
					for (L2PcInstance pm : party.getMembers())
					{
						QuestState state = pm.getQuestState(getName());
						if ((state == null) || (state.getInt("cond") != 1))
						{
							return prepareHtml(player, "32527-noquest.html", pm.getName());
						}
					}
					htmltext = "32527-10.html";
				}
			}
		}
		return htmltext;
	}
	
	private boolean giveReward(QuestState st, int finishDiff)
	{
		if (Rnd.get(100) < REWARD_CHANCE)
		{
			if (finishDiff == 0)
			{
				return false;
			}
			if (finishDiff < 5)
			{
				st.giveItems(14638, 1L);
			}
			else if (finishDiff < 10)
			{
				st.giveItems(14637, 1L);
			}
			else if (finishDiff < 15)
			{
				st.giveItems(14636, 1L);
			}
			else if (finishDiff < 20)
			{
				st.giveItems(14635, 1L);
			}
			return true;
		}
		return false;
	}
	
	private String prepareHtml(L2PcInstance player, String filename, String replace)
	{
		String htmltext = getNoQuestMsg(player);
		
		htmltext = getHtm(player, player.getHtmlPrefix(), filename);
		htmltext = htmltext.replace("%replace%", replace);
		
		return htmltext;
	}
}