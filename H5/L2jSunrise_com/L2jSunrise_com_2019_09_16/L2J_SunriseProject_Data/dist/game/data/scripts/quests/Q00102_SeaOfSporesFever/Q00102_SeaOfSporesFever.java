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
package quests.Q00102_SeaOfSporesFever;

import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.enums.QuestSound;
import l2r.gameserver.enums.Race;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.ItemHolder;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;
import l2r.gameserver.network.NpcStringId;

/**
 * Sea of Spores Fever (102)
 * @author xban1x
 */
public class Q00102_SeaOfSporesFever extends Quest
{
	// NPCs
	private static final int COBENDELL = 30156;
	private static final int BERROS = 30217;
	private static final int VELTRESS = 30219;
	private static final int RAYEN = 30221;
	private static final int ALBERIUS = 30284;
	private static final int GARTRANDELL = 30285;
	// Monsters
	private static final int DRYAD = 20013;
	private static final int DRYAD_ELDER = 20019;
	// Items
	private static final int SWORD_OF_SENTINEL = 743;
	private static final int STAFF_OF_SENTINEL = 744;
	private static final int ALBERIUS_LIST = 746;
	private static final int ALBERIUS_LETTER = 964;
	private static final int EVERGREEN_AMULET = 965;
	private static final int DRYADS_TEAR = 966;
	private static final int COBENDELLS_MEDICINE1 = 1130;
	private static final int COBENDELLS_MEDICINE2 = 1131;
	private static final int COBENDELLS_MEDICINE3 = 1132;
	private static final int COBENDELLS_MEDICINE4 = 1133;
	private static final int COBENDELLS_MEDICINE5 = 1134;
	private static final int SOULSHOT_NO_GRADE = 1835;
	private static final int SPIRITSHOT_NO_GRADE = 2509;
	// Misc
	private static final int MIN_LVL = 12;
	private static final Map<Integer, Integer> SENTINELS = new HashMap<>();
	
	static
	{
		SENTINELS.put(GARTRANDELL, COBENDELLS_MEDICINE5);
		SENTINELS.put(RAYEN, COBENDELLS_MEDICINE4);
		SENTINELS.put(VELTRESS, COBENDELLS_MEDICINE3);
		SENTINELS.put(BERROS, COBENDELLS_MEDICINE2);
		SENTINELS.put(ALBERIUS, COBENDELLS_MEDICINE1);
	}
	
	// Rewards
	private static final ItemHolder[] REWARDS =
	{
		new ItemHolder(1060, 100), // Lesser Healing Potion
		new ItemHolder(4412, 10), // Echo Crystal - Theme of Battle
		new ItemHolder(4413, 10), // Echo Crystal - Theme of Love
		new ItemHolder(4414, 10), // Echo Crystal - Theme of Solitude
		new ItemHolder(4415, 10), // Echo Crystal - Theme of Feast
		new ItemHolder(4416, 10), // Echo Crystal - Theme of Celebration
	};
	
	public Q00102_SeaOfSporesFever()
	{
		super(102, Q00102_SeaOfSporesFever.class.getSimpleName(), "Sea of Spores Fever");
		addStartNpc(ALBERIUS);
		addTalkId(ALBERIUS, COBENDELL, GARTRANDELL, BERROS, VELTRESS, RAYEN);
		addKillId(DRYAD, DRYAD_ELDER);
		registerQuestItems(ALBERIUS_LIST, ALBERIUS_LETTER, EVERGREEN_AMULET, DRYADS_TEAR, COBENDELLS_MEDICINE1, COBENDELLS_MEDICINE2, COBENDELLS_MEDICINE3, COBENDELLS_MEDICINE4, COBENDELLS_MEDICINE5);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if ((st != null) && event.equals("30284-02.htm"))
		{
			st.startQuest();
			st.giveItems(ALBERIUS_LETTER, 1);
			return event;
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState st = killer.getQuestState(getName());
		if ((st != null) && st.isCond(2) && (getRandom(10) < 3))
		{
			st.giveItems(DRYADS_TEAR, 1);
			if (st.getQuestItemsCount(DRYADS_TEAR) < 10)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			else
			{
				st.setCond(3, true);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState st = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		if (st != null)
		{
			switch (npc.getId())
			{
				case ALBERIUS:
				{
					switch (st.getState())
					{
						case State.CREATED:
						{
							htmltext = player.getRace() == Race.ELF ? player.getLevel() >= MIN_LVL ? "30284-07.htm" : "30284-08.htm" : "30284-00.htm";
							break;
						}
						case State.STARTED:
						{
							switch (st.getCond())
							{
								case 1:
								{
									if (st.hasQuestItems(ALBERIUS_LETTER))
									{
										htmltext = "30284-03.html";
									}
									break;
								}
								case 2:
								{
									if (st.hasQuestItems(EVERGREEN_AMULET))
									{
										htmltext = "30284-09.html";
									}
									break;
								}
								case 4:
								{
									if (st.hasQuestItems(COBENDELLS_MEDICINE1))
									{
										st.takeItems(COBENDELLS_MEDICINE1, 1);
										st.giveItems(ALBERIUS_LIST, 1);
										st.setCond(5);
										htmltext = "30284-04.html";
									}
									break;
								}
								case 5:
								{
									if (hasAtLeastOneQuestItem(player, COBENDELLS_MEDICINE1, COBENDELLS_MEDICINE2, COBENDELLS_MEDICINE3, COBENDELLS_MEDICINE4, COBENDELLS_MEDICINE5))
									{
										htmltext = "30284-05.html";
									}
									break;
								}
								case 6:
								{
									if (!hasAtLeastOneQuestItem(player, COBENDELLS_MEDICINE1, COBENDELLS_MEDICINE2, COBENDELLS_MEDICINE3, COBENDELLS_MEDICINE4, COBENDELLS_MEDICINE5))
									{
										showOnScreenMsg(player, NpcStringId.ACQUISITION_OF_RACE_SPECIFIC_WEAPON_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000);
										
										for (ItemHolder reward : REWARDS)
										{
											st.giveItems(reward);
										}
										
										if (player.isMageClass())
										{
											st.giveItems(STAFF_OF_SENTINEL, 1);
											st.giveItems(SPIRITSHOT_NO_GRADE, 500);
										}
										else
										{
											st.giveItems(SWORD_OF_SENTINEL, 1);
											st.giveItems(SOULSHOT_NO_GRADE, 500);
										}
										
										st.addExpAndSp(30202, 1339);
										st.giveAdena(6331, true);
										st.exitQuest(false, true);
										htmltext = "30284-06.html";
									}
									break;
								}
							}
							break;
						}
						case State.COMPLETED:
						{
							htmltext = getAlreadyCompletedMsg(player);
							break;
						}
					}
					break;
				}
				case COBENDELL:
				{
					switch (st.getCond())
					{
						case 1:
						{
							if (st.hasQuestItems(ALBERIUS_LETTER))
							{
								st.takeItems(ALBERIUS_LETTER, 1);
								st.giveItems(EVERGREEN_AMULET, 1);
								st.setCond(2, true);
								htmltext = "30156-03.html";
							}
							break;
						}
						case 2:
						{
							if (st.hasQuestItems(EVERGREEN_AMULET) && (st.getQuestItemsCount(DRYADS_TEAR) < 10))
							{
								htmltext = "30156-04.html";
							}
							break;
						}
						case 3:
						{
							if (st.getQuestItemsCount(DRYADS_TEAR) >= 10)
							{
								st.takeItems(EVERGREEN_AMULET, -1);
								st.takeItems(DRYADS_TEAR, -1);
								st.giveItems(COBENDELLS_MEDICINE1, 1);
								st.giveItems(COBENDELLS_MEDICINE2, 1);
								st.giveItems(COBENDELLS_MEDICINE3, 1);
								st.giveItems(COBENDELLS_MEDICINE4, 1);
								st.giveItems(COBENDELLS_MEDICINE5, 1);
								st.setCond(4, true);
								htmltext = "30156-05.html";
							}
							break;
						}
						case 4:
						{
							if (hasAtLeastOneQuestItem(player, COBENDELLS_MEDICINE1, COBENDELLS_MEDICINE2, COBENDELLS_MEDICINE3, COBENDELLS_MEDICINE4, COBENDELLS_MEDICINE5))
							{
								htmltext = "30156-06.html";
							}
							break;
						}
						case 5:
						{
							if (hasAtLeastOneQuestItem(player, COBENDELLS_MEDICINE1, COBENDELLS_MEDICINE2, COBENDELLS_MEDICINE3, COBENDELLS_MEDICINE4, COBENDELLS_MEDICINE5))
							{
								htmltext = "30156-07.html";
							}
							break;
						}
					}
					break;
				}
				case GARTRANDELL:
				case RAYEN:
				case VELTRESS:
				case BERROS:
				{
					if (st.hasQuestItems(ALBERIUS_LIST, SENTINELS.get(npc.getId())))
					{
						st.takeItems(SENTINELS.get(npc.getId()), -1);
						if (!hasAtLeastOneQuestItem(player, COBENDELLS_MEDICINE1, COBENDELLS_MEDICINE2, COBENDELLS_MEDICINE3, COBENDELLS_MEDICINE4, COBENDELLS_MEDICINE5))
						{
							st.setCond(6);
						}
						htmltext = npc.getId() + "-01.html";
					}
					break;
				}
			}
		}
		return htmltext;
	}
}
