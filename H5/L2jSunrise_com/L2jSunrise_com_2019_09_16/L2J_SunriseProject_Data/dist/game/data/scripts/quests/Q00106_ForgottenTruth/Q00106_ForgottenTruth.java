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
package quests.Q00106_ForgottenTruth;

import l2r.gameserver.enums.QuestSound;
import l2r.gameserver.enums.Race;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.ItemHolder;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.serverpackets.SocialAction;
import l2r.gameserver.util.Util;

import quests.Q00281_HeadForTheHills.Q00281_HeadForTheHills;

/**
 * Forgotten Truth (106)
 * @author janiko
 */
public final class Q00106_ForgottenTruth extends Quest
{
	// NPCs
	private static final int THIFIELL = 30358;
	private static final int KARTA = 30133;
	// Monster
	private static final int TUMRAN_ORC_BRIGAND = 27070;
	// Items
	private static final int ONYX_TALISMAN1 = 984;
	private static final int ONYX_TALISMAN2 = 985;
	private static final int ANCIENT_SCROLL = 986;
	private static final int ANCIENT_CLAY_TABLET = 987;
	private static final int KARTAS_TRANSLATION = 988;
	// Misc
	private static final int MIN_LVL = 10;
	// Rewards
	private static final int ELDRITCH_DAGGER = 989;
	private static final int ELDRITCH_STAFF = 2373;
	private static final ItemHolder[] REWARDS =
	{
		new ItemHolder(1060, 100), // Lesser Healing Potion
		new ItemHolder(4412, 10), // Echo Crystal - Theme of Battle
		new ItemHolder(4413, 10), // Echo Crystal - Theme of Love
		new ItemHolder(4414, 10), // Echo Crystal - Theme of Solitude
		new ItemHolder(4415, 10), // Echo Crystal - Theme of Feast
		new ItemHolder(4416, 10), // Echo Crystal - Theme of Celebration
	};
	
	public Q00106_ForgottenTruth()
	{
		super(106, Q00106_ForgottenTruth.class.getSimpleName(), "Forgotten Truth");
		addStartNpc(THIFIELL);
		addTalkId(THIFIELL, KARTA);
		addKillId(TUMRAN_ORC_BRIGAND);
		registerQuestItems(KARTAS_TRANSLATION, ONYX_TALISMAN1, ONYX_TALISMAN2, ANCIENT_SCROLL, ANCIENT_CLAY_TABLET);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = null;
		if (st == null)
		{
			return htmltext;
		}
		switch (event)
		{
			case "30358-04.htm":
			{
				htmltext = event;
				break;
			}
			case "30358-05.htm":
			{
				if (st.isCreated())
				{
					st.startQuest();
					st.giveItems(ONYX_TALISMAN1, 1);
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState st = killer.getQuestState(getName());
		if ((st != null) && st.isCond(2) && Util.checkIfInRange(1500, npc, killer, true))
		{
			if ((getRandom(100) < 20) && st.hasQuestItems(ONYX_TALISMAN2))
			{
				if (!st.hasQuestItems(ANCIENT_SCROLL))
				{
					st.giveItems(ANCIENT_SCROLL, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				else if (!st.hasQuestItems(ANCIENT_CLAY_TABLET))
				{
					st.setCond(3, true);
					st.giveItems(ANCIENT_CLAY_TABLET, 1);
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		final QuestState st = getQuestState(talker, true);
		String htmltext = getNoQuestMsg(talker);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (npc.getId())
		{
			case THIFIELL:
			{
				switch (st.getState())
				{
					case State.CREATED:
					{
						if (talker.getRace() == Race.DARK_ELF)
						{
							htmltext = talker.getLevel() >= MIN_LVL ? "30358-03.htm" : "30358-02.htm";
						}
						else
						{
							htmltext = "30358-01.htm";
						}
						break;
					}
					case State.STARTED:
					{
						if (hasAtLeastOneQuestItem(talker, ONYX_TALISMAN1, ONYX_TALISMAN2) && !st.hasQuestItems(KARTAS_TRANSLATION))
						{
							htmltext = "30358-06.html";
						}
						else if (st.isCond(4) && st.hasQuestItems(KARTAS_TRANSLATION))
						{
							Q00281_HeadForTheHills.giveNewbieReward(talker);
							talker.sendPacket(new SocialAction(talker.getObjectId(), 3));
							showOnScreenMsg(talker, NpcStringId.ACQUISITION_OF_RACE_SPECIFIC_WEAPON_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000);
							
							st.addExpAndSp(24195, 2074);
							st.giveAdena(10266, true);
							
							st.giveItems(st.getPlayer().isMageClass() ? ELDRITCH_STAFF : ELDRITCH_DAGGER, 1);
							
							for (ItemHolder reward : REWARDS)
							{
								giveItems(talker, reward);
							}
							
							st.exitQuest(false, true);
							htmltext = "30358-07.html";
						}
						break;
					}
					case State.COMPLETED:
					{
						htmltext = getAlreadyCompletedMsg(talker);
						break;
					}
				}
				break;
			}
			case KARTA:
			{
				if (st.isStarted())
				{
					switch (st.getCond())
					{
						case 1:
						{
							if (st.hasQuestItems(ONYX_TALISMAN1))
							{
								st.setCond(2, true);
								st.takeItems(ONYX_TALISMAN1, -1);
								st.giveItems(ONYX_TALISMAN2, 1);
								htmltext = "30133-01.html";
							}
							break;
						}
						case 2:
						{
							if (st.hasQuestItems(ONYX_TALISMAN2))
							{
								htmltext = "30133-02.html";
							}
							break;
						}
						case 3:
						{
							if (st.hasQuestItems(ANCIENT_SCROLL, ANCIENT_CLAY_TABLET))
							{
								st.setCond(4, true);
								takeItems(talker, -1, ANCIENT_SCROLL, ANCIENT_CLAY_TABLET, ONYX_TALISMAN2);
								st.giveItems(KARTAS_TRANSLATION, 1);
								htmltext = "30133-03.html";
							}
							break;
						}
						case 4:
						{
							if (st.hasQuestItems(KARTAS_TRANSLATION))
							{
								htmltext = "30133-04.html";
							}
							break;
						}
					}
				}
				break;
			}
		}
		return htmltext;
	}
}
