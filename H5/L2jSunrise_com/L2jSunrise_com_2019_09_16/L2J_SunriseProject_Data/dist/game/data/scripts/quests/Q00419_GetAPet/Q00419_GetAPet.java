/*
 * Copyright (C) 2004-2016 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q00419_GetAPet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import l2r.gameserver.enums.audio.Sound;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;
import l2r.util.Rnd;

/**
 * Get A Pet (419)
 * @author vGodFather
 */
public class Q00419_GetAPet extends Quest
{
	// Required items
	private static final int REQUIRED_SPIDER_LEGS = 50;
	
	// Quest items
	private static final int ANIMAL_LOVERS_LIST1 = 3417;
	private static final int ANIMAL_SLAYER_LIST1 = 3418;
	private static final int ANIMAL_SLAYER_LIST2 = 3419;
	private static final int ANIMAL_SLAYER_LIST3 = 3420;
	private static final int ANIMAL_SLAYER_LIST4 = 3421;
	private static final int ANIMAL_SLAYER_LIST5 = 3422;
	private static final int SPIDER_LEG1 = 3423;
	private static final int SPIDER_LEG2 = 3424;
	private static final int SPIDER_LEG3 = 3425;
	private static final int SPIDER_LEG4 = 3426;
	private static final int SPIDER_LEG5 = 3427;
	private static final int ANIMAL_SLAYER_LIST6 = 10164;
	private static final int SPIDER_LEG6 = 10165;
	
	private static final int[] QUESTITEMS =
	{
		ANIMAL_LOVERS_LIST1,
		ANIMAL_SLAYER_LIST1,
		ANIMAL_SLAYER_LIST2,
		ANIMAL_SLAYER_LIST3,
		ANIMAL_SLAYER_LIST4,
		ANIMAL_SLAYER_LIST5,
		SPIDER_LEG1,
		SPIDER_LEG2,
		SPIDER_LEG3,
		SPIDER_LEG4,
		SPIDER_LEG5,
		ANIMAL_SLAYER_LIST6,
		SPIDER_LEG6
	};
	
	private static final int SPIDER_LEG_DROP = 100;
	
	// 1 humans
	private static final int SPIDER_H1 = 20103;
	private static final int SPIDER_H2 = 20106;
	private static final int SPIDER_H3 = 20108;
	// 2 elves
	private static final int SPIDER_LE1 = 20460;
	private static final int SPIDER_LE2 = 20308;
	private static final int SPIDER_LE3 = 20466;
	// 3 dark elves
	private static final int SPIDER_DE1 = 20025;
	private static final int SPIDER_DE2 = 20105;
	private static final int SPIDER_DE3 = 20034;
	// 4 orcs
	private static final int SPIDER_O1 = 20474;
	private static final int SPIDER_O2 = 20476;
	private static final int SPIDER_O3 = 20478;
	// 5 dwarves
	private static final int SPIDER_D1 = 20403;
	private static final int SPIDER_D2 = 20508;
	// 6 kamael
	private static final int SPIDER_K1 = 22244;
	
	private static final int[] TOKILL =
	{
		SPIDER_H1,
		SPIDER_H2,
		SPIDER_H3,
		SPIDER_LE1,
		SPIDER_LE2,
		SPIDER_LE3,
		SPIDER_DE1,
		SPIDER_DE2,
		SPIDER_DE3,
		SPIDER_O1,
		SPIDER_O2,
		SPIDER_O3,
		SPIDER_D1,
		SPIDER_D2,
		SPIDER_K1
	};
	
	// NPCs
	private static final int PET_MANAGER_MARTIN = 30731;
	private static final int GK_BELLA = 30256;
	private static final int MC_ELLIE = 30091;
	private static final int GD_METTY = 30072;
	
	// Rewards
	private static final int WOLF_COLLAR = 2375;
	
	public Q00419_GetAPet()
	{
		super(419, Q00419_GetAPet.class.getSimpleName(), "Get A Pet");
		
		addStartNpc(PET_MANAGER_MARTIN);
		addTalkId(PET_MANAGER_MARTIN, GK_BELLA, MC_ELLIE, GD_METTY);
		addKillId(TOKILL);
		
		questItemIds = QUESTITEMS;
	}
	
	private long getCountOfProofs(QuestState st)
	{
		switch (st.getPlayer().getRace().ordinal())
		{
			case 0:
				return st.getQuestItemsCount(SPIDER_LEG1);
			case 1:
				return st.getQuestItemsCount(SPIDER_LEG2);
			case 2:
				return st.getQuestItemsCount(SPIDER_LEG3);
			case 3:
				return st.getQuestItemsCount(SPIDER_LEG4);
			case 4:
				return st.getQuestItemsCount(SPIDER_LEG5);
			case 5:
				return st.getQuestItemsCount(SPIDER_LEG6);
			default:
				return 0;
		}
	}
	
	private String checkQuestions(QuestState st)
	{
		String htmltext = null;
		int question = 1;
		String quiz = st.get("quiz");
		int answers = st.getInt("answers");
		if (answers < 10)
		{
			List<String> temp = Arrays.asList(quiz.split("\\s"));
			List<String> questions = new ArrayList<>(temp);
			
			int index = getRandom(questions.size());
			question = Integer.parseInt(questions.get(index));
			questions.remove(index);
			
			String questionsF = "";
			Iterator<String> i = questions.iterator();
			while (i.hasNext())
			{
				questionsF += i.hasNext() ? i.next() + " " : i.next();
			}
			
			st.set("quiz", questionsF);
			htmltext = "419_q" + String.valueOf(question) + ".htm";
		}
		else if (answers == 10)
		{
			st.giveItems(WOLF_COLLAR, 1);
			st.takeItems(ANIMAL_LOVERS_LIST1, -1);
			st.exitQuest(true);
			st.playSound(Sound.ITEMSOUND_QUEST_FINISH);
			htmltext = "Completed.htm";
		}
		
		return htmltext;
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
		int state = st.getState();
		int progress = st.getInt("progress");
		
		if (state == State.CREATED)
		{
			st.set("cond", "0");
			if (event.equalsIgnoreCase("details"))
			{
				htmltext = "419_confirm.htm";
			}
			else if (event.equalsIgnoreCase("agree"))
			{
				st.setState(State.STARTED);
				st.set("step", "STARTED");
				st.set("cond", "1");
				int race = player.getRace().ordinal();
				if (race == 0)
				{
					st.giveItems(ANIMAL_SLAYER_LIST1, 1);
					htmltext = "419_slay_0.htm";
				}
				else if (race == 1)
				{
					st.giveItems(ANIMAL_SLAYER_LIST2, 1);
					htmltext = "419_slay_1.htm";
				}
				else if (race == 2)
				{
					st.giveItems(ANIMAL_SLAYER_LIST3, 1);
					htmltext = "419_slay_2.htm";
				}
				else if (race == 3)
				{
					st.giveItems(ANIMAL_SLAYER_LIST4, 1);
					htmltext = "419_slay_3.htm";
				}
				else if (race == 4)
				{
					st.giveItems(ANIMAL_SLAYER_LIST5, 1);
					htmltext = "419_slay_4.htm";
				}
				else if (race == 5)
				{
					st.giveItems(ANIMAL_SLAYER_LIST6, 1);
					htmltext = "419_slay_5.htm";
				}
				else
				{
					htmltext = "Error: unknown race...";
					st.exitQuest(true);
					playSound(player, Sound.ITEMSOUND_QUEST_ACCEPT);
				}
			}
			else if (event.equalsIgnoreCase("disagree"))
			{
				st.exitQuest(true);
				htmltext = "419_cancelled.htm";
			}
		}
		else if ((state == State.STARTED) && (progress == 7))
		{
			if (event.equalsIgnoreCase("tryme"))
			{
				st.set("quiz", "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15");
				st.set("answers", "0");
				htmltext = checkQuestions(st);
			}
			else if (event.equalsIgnoreCase("wrong"))
			{
				st.set("step", "SLAYED");
				st.set("progress", "0");
				st.unset("quiz");
				st.unset("answers");
				htmltext = "419_failed.htm";
			}
			else if (event.equalsIgnoreCase("right"))
			{
				st.set("answers", String.valueOf(st.getInt("answers") + 1));
				htmltext = checkQuestions(st);
			}
		}
		else if ((state == State.STARTED) && st.get("step").equalsIgnoreCase("SLAYED"))
		{
			if (event.equalsIgnoreCase("talk"))
			{
				st.set("progress", "0");
				int race = player.getRace().ordinal();
				if (race == 0)
				{
					st.takeItems(SPIDER_LEG1, REQUIRED_SPIDER_LEGS);
					st.takeItems(ANIMAL_SLAYER_LIST1, 1);
				}
				else if (race == 1)
				{
					st.takeItems(SPIDER_LEG2, REQUIRED_SPIDER_LEGS);
					st.takeItems(ANIMAL_SLAYER_LIST2, 1);
				}
				else if (race == 2)
				{
					st.takeItems(SPIDER_LEG3, REQUIRED_SPIDER_LEGS);
					st.takeItems(ANIMAL_SLAYER_LIST3, 1);
				}
				else if (race == 3)
				{
					st.takeItems(SPIDER_LEG4, REQUIRED_SPIDER_LEGS);
					st.takeItems(ANIMAL_SLAYER_LIST4, 1);
				}
				else if (race == 4)
				{
					st.takeItems(SPIDER_LEG5, REQUIRED_SPIDER_LEGS);
					st.takeItems(ANIMAL_SLAYER_LIST5, 1);
				}
				else if (race == 5)
				{
					st.takeItems(SPIDER_LEG6, REQUIRED_SPIDER_LEGS);
					st.takeItems(ANIMAL_SLAYER_LIST6, 1);
				}
				
				st.giveItems(ANIMAL_LOVERS_LIST1, 1);
				htmltext = "419_talk.htm";
			}
			else if (event.equalsIgnoreCase("talk1"))
			{
				htmltext = "419_bella_2.htm";
			}
			else if (event.equalsIgnoreCase("talk2"))
			{
				st.set("progress", String.valueOf(st.getInt("progress") | 1));
				htmltext = "419_bella_3.htm";
			}
			else if (event.equalsIgnoreCase("talk3"))
			{
				st.set("progress", String.valueOf(st.getInt("progress") | 2));
				htmltext = "419_ellie_2.htm";
			}
			else if (event.equalsIgnoreCase("talk4"))
			{
				st.set("progress", String.valueOf(st.getInt("progress") | 4));
				htmltext = "419_metty_2.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		final QuestState st = getQuestState(talker, true);
		String htmltext = Quest.getNoQuestMsg(talker);
		
		if (st == null)
		{
			return htmltext;
		}
		
		int npcId = npc.getId();
		int state = st.getState();
		String step = st.get("step") != null ? st.get("step") : "";
		
		if ((npcId != PET_MANAGER_MARTIN) && (state == State.STARTED) && !step.equalsIgnoreCase("SLAYED"))
		{
			return htmltext;
		}
		
		if (state == State.COMPLETED)
		{
			st.setState(State.CREATED);
		}
		
		if (npcId == PET_MANAGER_MARTIN)
		{
			if (state == State.CREATED)
			{
				if (talker.getLevel() < 15)
				{
					st.exitQuest(true);
					htmltext = "419_low_level.htm";
				}
				else
				{
					htmltext = "Start.htm";
				}
			}
			
			if ((state == State.STARTED) && step.equalsIgnoreCase("STARTED"))
			{
				long proofs = getCountOfProofs(st);
				if (proofs == 0)
				{
					htmltext = "419_no_slay.htm";
				}
				else if (proofs < REQUIRED_SPIDER_LEGS)
				{
					htmltext = "419_pending_slay.htm";
				}
				else
				{
					st.set("step", "SLAYED");
					htmltext = "Slayed.htm";
				}
			}
			
			if ((state == State.STARTED) && step.equalsIgnoreCase("SLAYED"))
			{
				int progress = st.getInt("progress");
				htmltext = progress == 7 ? "Talked.htm" : "419_pending_talk.htm";
			}
		}
		else if ((state == State.STARTED) && step.equalsIgnoreCase("SLAYED"))
		{
			if (npcId == GK_BELLA)
			{
				htmltext = "419_bella_1.htm";
			}
			else if (npcId == MC_ELLIE)
			{
				htmltext = "419_ellie_1.htm";
			}
			else if (npcId == GD_METTY)
			{
				htmltext = "419_metty_1.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		QuestState st = killer.getQuestState(getName());
		if (st == null)
		{
			return super.onKill(npc, killer, isSummon);
		}
		
		int state = st.getState();
		if (state != State.STARTED)
		{
			return super.onKill(npc, killer, isSummon);
		}
		
		int npcId = npc.getId();
		long collected = getCountOfProofs(st);
		if (collected < REQUIRED_SPIDER_LEGS)
		{
			List<Integer> npcs = new ArrayList<>();
			int item = 0;
			int race = killer.getRace().ordinal();
			switch (race)
			{
				case 0:
					npcs.add(SPIDER_H1);
					npcs.add(SPIDER_H2);
					npcs.add(SPIDER_H3);
					item = SPIDER_LEG1;
					break;
				case 1:
					npcs.add(SPIDER_LE1);
					npcs.add(SPIDER_LE2);
					npcs.add(SPIDER_LE3);
					item = SPIDER_LEG2;
					break;
				case 2:
					npcs.add(SPIDER_DE1);
					npcs.add(SPIDER_DE2);
					npcs.add(SPIDER_DE3);
					item = SPIDER_LEG3;
					break;
				case 3:
					npcs.add(SPIDER_O1);
					npcs.add(SPIDER_O2);
					npcs.add(SPIDER_O3);
					item = SPIDER_LEG4;
					break;
				case 4:
					npcs.add(SPIDER_D1);
					npcs.add(SPIDER_D2);
					item = SPIDER_LEG5;
					break;
				case 5:
					npcs.add(SPIDER_K1);
					item = SPIDER_LEG6;
					break;
					
				default:
					break;
			}
			
			if (npcs.contains(npcId))
			{
				if (Rnd.chance(SPIDER_LEG_DROP))
				{
					giveItems(killer, item, 1);
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}