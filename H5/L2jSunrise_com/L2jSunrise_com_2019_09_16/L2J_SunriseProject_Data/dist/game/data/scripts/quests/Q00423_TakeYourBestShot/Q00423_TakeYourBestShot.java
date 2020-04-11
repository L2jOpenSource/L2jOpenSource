/*
 * Copyright (C) 2004-2015 L2J DataPack
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
package quests.Q00423_TakeYourBestShot;

import java.util.Arrays;
import java.util.List;

import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.audio.Sound;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;

import quests.Q00249_PoisonedPlainsOfTheLizardmen.Q00249_PoisonedPlainsOfTheLizardmen;

/**
 * Take Your Best Shot (423)
 * @author vGodFather
 */
public class Q00423_TakeYourBestShot extends Quest
{
	// NPC
	private static final int BATRACOS = 32740;
	private static final int JOHNNY = 32744;
	// Item
	private static final int SEER_UGOROS_PASS = 15496;
	// Guard
	private static final int TANTA_GUARD = 18862;
	// Mobs
	private static final List<Integer> MONSTERS = Arrays.asList(22768, 22769, 22770, 22771, 22772, 22773, 22774);
	// Misc
	private static final int SPAWN_CHANCE = 5; // Spawn chance x/1000
	private static final int MIN_LEVEL = 82;
	
	public Q00423_TakeYourBestShot()
	{
		super(423, Q00423_TakeYourBestShot.class.getSimpleName(), "Take Your Best Shot!");
		addStartNpc(JOHNNY, BATRACOS);
		addTalkId(JOHNNY, BATRACOS);
		addFirstTalkId(BATRACOS);
		
		addKillId(TANTA_GUARD);
		addKillId(MONSTERS);
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
		
		if (npc.getId() == JOHNNY)
		{
			if (event.equalsIgnoreCase("32744-04.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				playSound(player, Sound.ITEMSOUND_QUEST_ACCEPT);
			}
			else if (event.equalsIgnoreCase("32744-quit.htm"))
			{
				st.exitQuest(true);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState st = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (npc.getId())
		{
			case JOHNNY:
				switch (st.getState())
				{
					case State.CREATED:
						QuestState _prev = player.getQuestState(Q00249_PoisonedPlainsOfTheLizardmen.class.getSimpleName());
						if ((_prev != null) && _prev.isCompleted() && (player.getLevel() >= MIN_LEVEL))
						{
							htmltext = st.hasQuestItems(SEER_UGOROS_PASS) ? "32744-07.htm" : "32744-01.htm";
						}
						else
						{
							htmltext = "32744-00.htm";
						}
						break;
					case State.STARTED:
						if (st.getInt("cond") == 1)
						{
							htmltext = "32744-05.htm";
						}
						else if (st.getInt("cond") == 2)
						{
							htmltext = "32744-06.htm";
						}
						break;
				}
				break;
			case BATRACOS:
				switch (st.getState())
				{
					case State.CREATED:
						htmltext = st.hasQuestItems(SEER_UGOROS_PASS) ? "32740-05.htm" : "32740-00.htm";
						break;
					case State.STARTED:
						if (st.getInt("cond") == 1)
						{
							htmltext = "32740-02.htm";
						}
						else if (st.getInt("cond") == 2)
						{
							st.giveItems(SEER_UGOROS_PASS, 1);
							playSound(player, Sound.ITEMSOUND_QUEST_FINISH);
							st.unset("cond");
							st.exitQuest(true);
							htmltext = "32740-04.htm";
						}
						break;
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState st = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		if (st == null)
		{
			return htmltext;
		}
		
		if (npc.isInsideRadius(96782, 85918, 0, 100, false, true))
		{
			return "32740-ugoros.htm";
		}
		return "32740.htm";
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		final QuestState st = getQuestState(player, false);
		if (st == null)
		{
			return null;
		}
		
		if (MONSTERS.contains(npc.getId()) && (getRandom(1000) <= SPAWN_CHANCE))
		{
			L2Npc guard = addSpawn(TANTA_GUARD, npc, false);
			attackPlayer((L2Attackable) guard, player);
		}
		else if ((npc.getId() == TANTA_GUARD) && (st.getInt("cond") == 1))
		{
			st.set("cond", "2");
			playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
		}
		return null;
	}
	
	private void attackPlayer(L2Attackable npc, L2PcInstance player)
	{
		npc.setIsRunning(true);
		npc.addDamageHate(player, 0, 999);
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
	}
}
