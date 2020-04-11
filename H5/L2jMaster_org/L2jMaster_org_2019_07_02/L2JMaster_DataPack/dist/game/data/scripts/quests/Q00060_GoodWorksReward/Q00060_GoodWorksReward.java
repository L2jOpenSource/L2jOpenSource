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
package quests.Q00060_GoodWorksReward;

import com.l2jserver.gameserver.enums.CategoryType;
import com.l2jserver.gameserver.enums.Race;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.base.ClassId;
import com.l2jserver.gameserver.model.itemcontainer.Inventory;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.network.NpcStringId;
import com.l2jserver.gameserver.network.clientpackets.Say2;
import com.l2jserver.gameserver.network.serverpackets.NpcSay;
import com.l2jserver.gameserver.util.Util;

import quests.Q00211_TrialOfTheChallenger.Q00211_TrialOfTheChallenger;
import quests.Q00212_TrialOfDuty.Q00212_TrialOfDuty;
import quests.Q00213_TrialOfTheSeeker.Q00213_TrialOfTheSeeker;
import quests.Q00214_TrialOfTheScholar.Q00214_TrialOfTheScholar;
import quests.Q00215_TrialOfThePilgrim.Q00215_TrialOfThePilgrim;
import quests.Q00216_TrialOfTheGuildsman.Q00216_TrialOfTheGuildsman;
import quests.Q00217_TestimonyOfTrust.Q00217_TestimonyOfTrust;
import quests.Q00218_TestimonyOfLife.Q00218_TestimonyOfLife;
import quests.Q00219_TestimonyOfFate.Q00219_TestimonyOfFate;
import quests.Q00220_TestimonyOfGlory.Q00220_TestimonyOfGlory;
import quests.Q00221_TestimonyOfProsperity.Q00221_TestimonyOfProsperity;
import quests.Q00222_TestOfTheDuelist.Q00222_TestOfTheDuelist;
import quests.Q00223_TestOfTheChampion.Q00223_TestOfTheChampion;
import quests.Q00224_TestOfSagittarius.Q00224_TestOfSagittarius;
import quests.Q00225_TestOfTheSearcher.Q00225_TestOfTheSearcher;
import quests.Q00226_TestOfTheHealer.Q00226_TestOfTheHealer;
import quests.Q00227_TestOfTheReformer.Q00227_TestOfTheReformer;
import quests.Q00228_TestOfMagus.Q00228_TestOfMagus;
import quests.Q00229_TestOfWitchcraft.Q00229_TestOfWitchcraft;
import quests.Q00230_TestOfTheSummoner.Q00230_TestOfTheSummoner;
import quests.Q00231_TestOfTheMaestro.Q00231_TestOfTheMaestro;
import quests.Q00232_TestOfTheLord.Q00232_TestOfTheLord;
import quests.Q00233_TestOfTheWarSpirit.Q00233_TestOfTheWarSpirit;

/**
 * Good Work's Reward (60)
 * @author ivantotov
 */
public final class Q00060_GoodWorksReward extends Quest
{
	// NPCs
	private static final int GROCER_HELVERIA = 30081;
	private static final int BLACK_MARKETEER_OF_MAMMON = 31092;
	private static final int BLUEPRINT_SELLER_DAEGER = 31435;
	private static final int MARK = 32487;
	// Items
	private static final int BLOODY_CLOTH_FRAGMENT = 10867;
	private static final int HELVETIAS_ANTIDOTE = 10868;
	// Reward
	private static final int MARK_OF_CHALLENGER = 2627;
	private static final int MARK_OF_DUTY = 2633;
	private static final int MARK_OF_SEEKER = 2673;
	private static final int MARK_OF_SCHOLAR = 2674;
	private static final int MARK_OF_PILGRIM = 2721;
	private static final int MARK_OF_TRUST = 2734;
	private static final int MARK_OF_DUELIST = 2762;
	private static final int MARK_OF_SEARCHER = 2809;
	private static final int MARK_OF_HEALER = 2820;
	private static final int MARK_OF_REFORMER = 2821;
	private static final int MARK_OF_MAGUS = 2840;
	private static final int MARK_OF_MAESTRO = 2867;
	private static final int MARK_OF_WARSPIRIT = 2879;
	private static final int MARK_OF_GUILDSMAN = 3119;
	private static final int MARK_OF_LIFE = 3140;
	private static final int MARK_OF_FATE = 3172;
	private static final int MARK_OF_GLORY = 3203;
	private static final int MARK_OF_PROSPERITY = 3238;
	private static final int MARK_OF_CHAMPION = 3276;
	private static final int MARK_OF_SAGITTARIUS = 3293;
	private static final int MARK_OF_WITCHCRAFT = 3307;
	private static final int MARK_OF_SUMMONER = 3336;
	private static final int MARK_OF_LORD = 3390;
	// Quest Monster
	private static final int PURSUER = 27340;
	// Misc
	private static final int MIN_LEVEL = 39;
	private static final int ONE_MILLION = 1000000;
	private static final int TWO_MILLION = 2000000;
	private static final int THREE_MILLION = 3000000;
	
	public Q00060_GoodWorksReward()
	{
		super(60, Q00060_GoodWorksReward.class.getSimpleName(), "Good Work's Reward");
		addStartNpc(BLUEPRINT_SELLER_DAEGER);
		addTalkId(BLUEPRINT_SELLER_DAEGER, GROCER_HELVERIA, BLACK_MARKETEER_OF_MAMMON, MARK);
		addKillId(PURSUER);
		addSpawnId(PURSUER);
		registerQuestItems(BLOODY_CLOTH_FRAGMENT, HELVETIAS_ANTIDOTE);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		
		if ("DESPAWN".equals(event))
		{
			npc.broadcastPacket(new NpcSay(npc, Say2.NPC_ALL, NpcStringId.YOU_HAVE_GOOD_LUCK_I_SHALL_RETURN));
			final L2Npc npc0 = npc.getVariables().getObject("npc0", L2Npc.class);
			if (npc0 != null)
			{
				npc0.getVariables().set("SPAWNED", false);
			}
			npc.deleteMe();
			return super.onAdvEvent(event, npc, player);
		}
		
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "31435-07.htm":
			{
				if (qs.isCreated())
				{
					qs.startQuest();
					qs.setMemoState(1);
					htmltext = event;
				}
				break;
			}
			case "31435-02.htm":
			{
				htmltext = event;
				break;
			}
			case "31435-10.html":
			{
				if (qs.isMemoState(3))
				{
					qs.setMemoState(4);
					qs.setCond(4, true);
					htmltext = event;
				}
				break;
			}
			case "31435-14.html":
			{
				if (qs.isMemoState(8))
				{
					qs.setMemoState(9);
					qs.setCond(9, true);
					htmltext = event;
				}
				break;
			}
			case "30081-02.html":
			{
				if (qs.isMemoState(4))
				{
					htmltext = event;
				}
				break;
			}
			case "30081-03.html":
			{
				if (qs.isMemoState(4))
				{
					takeItems(player, BLOODY_CLOTH_FRAGMENT, -1);
					qs.setMemoState(5);
					qs.setCond(5, true);
					htmltext = event;
				}
				break;
			}
			case "30081-05.html":
			{
				final int memoState = qs.getMemoState();
				if ((memoState >= 5) && (memoState <= 6))
				{
					if (getQuestItemsCount(player, Inventory.ADENA_ID) >= THREE_MILLION)
					{
						giveItems(player, HELVETIAS_ANTIDOTE, 1);
						takeItems(player, Inventory.ADENA_ID, THREE_MILLION);
						qs.setMemoState(7);
						qs.setCond(7, true);
						htmltext = event;
					}
					else
					{
						qs.setMemoState(6);
						qs.setCond(6, true);
						htmltext = "30081-06.html";
					}
				}
				break;
			}
			case "30081-07.html":
			{
				if (qs.isMemoState(5))
				{
					qs.setMemoState(6);
					qs.setCond(6, true);
					htmltext = event;
				}
				break;
			}
			case "REPLY_1":
			{
				if (qs.isMemoState(10))
				{
					if (player.hasQuestCompleted(Q00211_TrialOfTheChallenger.class.getSimpleName()) || player.hasQuestCompleted(Q00212_TrialOfDuty.class.getSimpleName()) || player.hasQuestCompleted(Q00213_TrialOfTheSeeker.class.getSimpleName())
						|| player.hasQuestCompleted(Q00214_TrialOfTheScholar.class.getSimpleName()) || player.hasQuestCompleted(Q00215_TrialOfThePilgrim.class.getSimpleName()) || player.hasQuestCompleted(Q00216_TrialOfTheGuildsman.class.getSimpleName()))
					{
						if (player.hasQuestCompleted(Q00217_TestimonyOfTrust.class.getSimpleName()) || player.hasQuestCompleted(Q00218_TestimonyOfLife.class.getSimpleName()) || player.hasQuestCompleted(Q00219_TestimonyOfFate.class.getSimpleName())
							|| player.hasQuestCompleted(Q00220_TestimonyOfGlory.class.getSimpleName()) || player.hasQuestCompleted(Q00221_TestimonyOfProsperity.class.getSimpleName()))
						{
							
							if (player.hasQuestCompleted(Q00222_TestOfTheDuelist.class.getSimpleName()) || player.hasQuestCompleted(Q00223_TestOfTheChampion.class.getSimpleName()) || player.hasQuestCompleted(Q00224_TestOfSagittarius.class.getSimpleName())
								|| player.hasQuestCompleted(Q00225_TestOfTheSearcher.class.getSimpleName()) || player.hasQuestCompleted(Q00226_TestOfTheHealer.class.getSimpleName()) || player.hasQuestCompleted(Q00227_TestOfTheReformer.class.getSimpleName())
								|| player.hasQuestCompleted(Q00228_TestOfMagus.class.getSimpleName()) || player.hasQuestCompleted(Q00229_TestOfWitchcraft.class.getSimpleName()) || player.hasQuestCompleted(Q00230_TestOfTheSummoner.class.getSimpleName())
								|| player.hasQuestCompleted(Q00231_TestOfTheMaestro.class.getSimpleName()) || player.hasQuestCompleted(Q00232_TestOfTheLord.class.getSimpleName()) || player.hasQuestCompleted(Q00233_TestOfTheWarSpirit.class.getSimpleName()))
							{
								qs.setMemoStateEx(1, 3);
							}
							else
							{
								qs.setMemoStateEx(1, 2);
							}
						}
						else if (player.hasQuestCompleted(Q00222_TestOfTheDuelist.class.getSimpleName()) || player.hasQuestCompleted(Q00223_TestOfTheChampion.class.getSimpleName()) || player.hasQuestCompleted(Q00224_TestOfSagittarius.class.getSimpleName())
							|| player.hasQuestCompleted(Q00225_TestOfTheSearcher.class.getSimpleName()) || player.hasQuestCompleted(Q00226_TestOfTheHealer.class.getSimpleName()) || player.hasQuestCompleted(Q00227_TestOfTheReformer.class.getSimpleName())
							|| player.hasQuestCompleted(Q00228_TestOfMagus.class.getSimpleName()) || player.hasQuestCompleted(Q00229_TestOfWitchcraft.class.getSimpleName()) || player.hasQuestCompleted(Q00230_TestOfTheSummoner.class.getSimpleName())
							|| player.hasQuestCompleted(Q00231_TestOfTheMaestro.class.getSimpleName()) || player.hasQuestCompleted(Q00232_TestOfTheLord.class.getSimpleName()) || player.hasQuestCompleted(Q00233_TestOfTheWarSpirit.class.getSimpleName()))
						{
							qs.setMemoStateEx(1, 2);
						}
						else
						{
							qs.setMemoStateEx(1, 1);
						}
					}
					else if (player.hasQuestCompleted(Q00217_TestimonyOfTrust.class.getSimpleName()) || player.hasQuestCompleted(Q00218_TestimonyOfLife.class.getSimpleName()) || player.hasQuestCompleted(Q00219_TestimonyOfFate.class.getSimpleName())
						|| player.hasQuestCompleted(Q00220_TestimonyOfGlory.class.getSimpleName()) || player.hasQuestCompleted(Q00221_TestimonyOfProsperity.class.getSimpleName()))
					{
						if (player.hasQuestCompleted(Q00222_TestOfTheDuelist.class.getSimpleName()) || player.hasQuestCompleted(Q00223_TestOfTheChampion.class.getSimpleName()) || player.hasQuestCompleted(Q00224_TestOfSagittarius.class.getSimpleName())
							|| player.hasQuestCompleted(Q00225_TestOfTheSearcher.class.getSimpleName()) || player.hasQuestCompleted(Q00226_TestOfTheHealer.class.getSimpleName()) || player.hasQuestCompleted(Q00227_TestOfTheReformer.class.getSimpleName())
							|| player.hasQuestCompleted(Q00228_TestOfMagus.class.getSimpleName()) || player.hasQuestCompleted(Q00229_TestOfWitchcraft.class.getSimpleName()) || player.hasQuestCompleted(Q00230_TestOfTheSummoner.class.getSimpleName())
							|| player.hasQuestCompleted(Q00231_TestOfTheMaestro.class.getSimpleName()) || player.hasQuestCompleted(Q00232_TestOfTheLord.class.getSimpleName()) || player.hasQuestCompleted(Q00233_TestOfTheWarSpirit.class.getSimpleName()))
						{
							qs.setMemoStateEx(1, 2);
						}
						else
						{
							qs.setMemoStateEx(1, 1);
						}
					}
					else if (player.hasQuestCompleted(Q00222_TestOfTheDuelist.class.getSimpleName()) || player.hasQuestCompleted(Q00223_TestOfTheChampion.class.getSimpleName()) || player.hasQuestCompleted(Q00224_TestOfSagittarius.class.getSimpleName())
						|| player.hasQuestCompleted(Q00225_TestOfTheSearcher.class.getSimpleName()) || player.hasQuestCompleted(Q00226_TestOfTheHealer.class.getSimpleName()) || player.hasQuestCompleted(Q00227_TestOfTheReformer.class.getSimpleName())
						|| player.hasQuestCompleted(Q00228_TestOfMagus.class.getSimpleName()) || player.hasQuestCompleted(Q00229_TestOfWitchcraft.class.getSimpleName()) || player.hasQuestCompleted(Q00230_TestOfTheSummoner.class.getSimpleName())
						|| player.hasQuestCompleted(Q00231_TestOfTheMaestro.class.getSimpleName()) || player.hasQuestCompleted(Q00232_TestOfTheLord.class.getSimpleName()) || player.hasQuestCompleted(Q00233_TestOfTheWarSpirit.class.getSimpleName()))
					{
						qs.setMemoStateEx(1, 1);
					}
					htmltext = "31092-02.html";
				}
				break;
			}
			case "REPLY_2":
			{
				if (qs.isMemoState(10))
				{
					if (qs.getMemoStateEx(1) >= 3)
					{
						htmltext = "31092-03b.html";
					}
					else if (qs.getMemoStateEx(1) >= 1)
					{
						htmltext = "31092-03.html";
					}
					else
					{
						htmltext = "31092-03a.html";
					}
				}
				break;
			}
			case "REPLY_3":
			{
				if (qs.isMemoState(10))
				{
					if (qs.getMemoStateEx(1) >= 3)
					{
						giveItems(player, Inventory.ADENA_ID, THREE_MILLION);
						htmltext = "31092-04a.html";
					}
					else if (qs.getMemoStateEx(1) == 2)
					{
						giveItems(player, Inventory.ADENA_ID, TWO_MILLION);
						htmltext = "31092-04b.html";
					}
					else if (qs.getMemoStateEx(1) == 1)
					{
						giveItems(player, Inventory.ADENA_ID, ONE_MILLION);
						htmltext = "31092-04b.html";
					}
					qs.exitQuest(false, true);
				}
				break;
			}
			case "REPLY_4":
			{
				if (qs.isMemoState(10))
				{
					switch (player.getClassId())
					{
						case warrior:
							return "31092-05.html";
						case knight:
							return "31092-06.html";
						case rogue:
							return "31092-07.html";
						case wizard:
							return "31092-08.html";
						case cleric:
							return "31092-09.html";
						case elvenKnight:
							return "31092-10.html";
						case elvenScout:
							return "31092-11.html";
						case elvenWizard:
							return "31092-12.html";
						case oracle:
							return "31092-13.html";
						case palusKnight:
							return "31092-14.html";
						case assassin:
							return "31092-15.html";
						case darkWizard:
							return "31092-16.html";
						case shillienOracle:
							return "31092-17.html";
						case orcRaider:
							return "31092-18.html";
						case orcMonk:
							return "31092-19.html";
						case orcShaman:
							return "31092-20.html";
						case scavenger:
							return "31092-21.html";
						case artisan:
							return "31092-22.html";
					}
					qs.exitQuest(false, true);
				}
				break;
			}
			case "REPLY_5":
			{
				if (player.isInCategory(CategoryType.SECOND_CLASS_GROUP))
				{
					switch (player.getClassId())
					{
						case warrior:
							return "31092-05a.html";
						case knight:
							return "31092-06a.html";
						case rogue:
							return "31092-07a.html";
						case wizard:
							return "31092-08a.html";
						case cleric:
							return "31092-09a.html";
						case elvenKnight:
							return "31092-10a.html";
						case elvenScout:
							return "31092-11a.html";
						case elvenWizard:
							return "31092-12a.html";
						case oracle:
							return "31092-13a.html";
						case palusKnight:
							return "31092-14a.html";
						case assassin:
							return "31092-15a.html";
						case darkWizard:
							return "31092-16a.html";
						case shillienOracle:
							return "31092-17a.html";
						case orcRaider:
							return "31092-18a.html";
						case orcMonk:
							return "31092-19a.html";
						case orcShaman:
							return "31092-20a.html";
						case scavenger:
							return "31092-21a.html";
						case artisan:
							return "31092-22a.html";
					}
				}
				break;
			}
			case "REPLY_6":
			{
				if ((player.getClassId() == ClassId.warrior))
				{
					if (!hasQuestItems(player, MARK_OF_CHALLENGER))
					{
						giveItems(player, MARK_OF_CHALLENGER, 1);
					}
					if (!hasQuestItems(player, MARK_OF_TRUST))
					{
						giveItems(player, MARK_OF_TRUST, 1);
					}
					if (!hasQuestItems(player, MARK_OF_DUELIST))
					{
						giveItems(player, MARK_OF_DUELIST, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_7":
			{
				if ((player.getClassId() == ClassId.warrior))
				{
					if (!hasQuestItems(player, MARK_OF_CHALLENGER))
					{
						giveItems(player, MARK_OF_CHALLENGER, 1);
					}
					if (!hasQuestItems(player, MARK_OF_TRUST))
					{
						giveItems(player, MARK_OF_TRUST, 1);
					}
					if (!hasQuestItems(player, MARK_OF_CHAMPION))
					{
						giveItems(player, MARK_OF_CHAMPION, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_8":
			{
				if ((player.getClassId() == ClassId.knight))
				{
					if (!hasQuestItems(player, MARK_OF_DUTY))
					{
						giveItems(player, MARK_OF_DUTY, 1);
					}
					if (!hasQuestItems(player, MARK_OF_TRUST))
					{
						giveItems(player, MARK_OF_TRUST, 1);
					}
					if (!hasQuestItems(player, MARK_OF_HEALER))
					{
						giveItems(player, MARK_OF_HEALER, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_9":
			{
				if ((player.getClassId() == ClassId.knight))
				{
					if (!hasQuestItems(player, MARK_OF_DUTY))
					{
						giveItems(player, MARK_OF_DUTY, 1);
					}
					if (!hasQuestItems(player, MARK_OF_TRUST))
					{
						giveItems(player, MARK_OF_TRUST, 1);
					}
					if (!hasQuestItems(player, MARK_OF_WITCHCRAFT))
					{
						giveItems(player, MARK_OF_WITCHCRAFT, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_10":
			{
				if ((player.getClassId() == ClassId.rogue))
				{
					if (!hasQuestItems(player, MARK_OF_SEEKER))
					{
						giveItems(player, MARK_OF_SEEKER, 1);
					}
					if (!hasQuestItems(player, MARK_OF_TRUST))
					{
						giveItems(player, MARK_OF_TRUST, 1);
					}
					if (!hasQuestItems(player, MARK_OF_SEARCHER))
					{
						giveItems(player, MARK_OF_SEARCHER, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_11":
			{
				if ((player.getClassId() == ClassId.rogue))
				{
					if (!hasQuestItems(player, MARK_OF_SEEKER))
					{
						giveItems(player, MARK_OF_SEEKER, 1);
					}
					if (!hasQuestItems(player, MARK_OF_TRUST))
					{
						giveItems(player, MARK_OF_TRUST, 1);
					}
					if (!hasQuestItems(player, MARK_OF_SAGITTARIUS))
					{
						giveItems(player, MARK_OF_SAGITTARIUS, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_12":
			{
				if ((player.getClassId() == ClassId.wizard))
				{
					if (!hasQuestItems(player, MARK_OF_SCHOLAR))
					{
						giveItems(player, MARK_OF_SCHOLAR, 1);
					}
					if (!hasQuestItems(player, MARK_OF_TRUST))
					{
						giveItems(player, MARK_OF_TRUST, 1);
					}
					if (!hasQuestItems(player, MARK_OF_MAGUS))
					{
						giveItems(player, MARK_OF_MAGUS, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_13":
			{
				if ((player.getClassId() == ClassId.wizard))
				{
					if (!hasQuestItems(player, MARK_OF_SCHOLAR))
					{
						giveItems(player, MARK_OF_SCHOLAR, 1);
					}
					if (!hasQuestItems(player, MARK_OF_TRUST))
					{
						giveItems(player, MARK_OF_TRUST, 1);
					}
					if (!hasQuestItems(player, MARK_OF_WITCHCRAFT))
					{
						giveItems(player, MARK_OF_WITCHCRAFT, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_14":
			{
				if ((player.getClassId() == ClassId.wizard))
				{
					if (!hasQuestItems(player, MARK_OF_SCHOLAR))
					{
						giveItems(player, MARK_OF_SCHOLAR, 1);
					}
					if (!hasQuestItems(player, MARK_OF_TRUST))
					{
						giveItems(player, MARK_OF_TRUST, 1);
					}
					if (!hasQuestItems(player, MARK_OF_SUMMONER))
					{
						giveItems(player, MARK_OF_SUMMONER, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_15":
			{
				if ((player.getClassId() == ClassId.cleric))
				{
					if (!hasQuestItems(player, MARK_OF_PILGRIM))
					{
						giveItems(player, MARK_OF_PILGRIM, 1);
					}
					if (!hasQuestItems(player, MARK_OF_TRUST))
					{
						giveItems(player, MARK_OF_TRUST, 1);
					}
					if (!hasQuestItems(player, MARK_OF_HEALER))
					{
						giveItems(player, MARK_OF_HEALER, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_16":
			{
				if ((player.getClassId() == ClassId.cleric))
				{
					if (!hasQuestItems(player, MARK_OF_PILGRIM))
					{
						giveItems(player, MARK_OF_PILGRIM, 1);
					}
					if (!hasQuestItems(player, MARK_OF_TRUST))
					{
						giveItems(player, MARK_OF_TRUST, 1);
					}
					if (!hasQuestItems(player, MARK_OF_REFORMER))
					{
						giveItems(player, MARK_OF_REFORMER, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_17":
			{
				if ((player.getClassId() == ClassId.elvenKnight))
				{
					if (!hasQuestItems(player, MARK_OF_DUTY))
					{
						giveItems(player, MARK_OF_DUTY, 1);
					}
					if (!hasQuestItems(player, MARK_OF_LIFE))
					{
						giveItems(player, MARK_OF_LIFE, 1);
					}
					if (!hasQuestItems(player, MARK_OF_HEALER))
					{
						giveItems(player, MARK_OF_HEALER, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_18":
			{
				if ((player.getClassId() == ClassId.elvenKnight))
				{
					if (!hasQuestItems(player, MARK_OF_CHALLENGER))
					{
						giveItems(player, MARK_OF_CHALLENGER, 1);
					}
					if (!hasQuestItems(player, MARK_OF_LIFE))
					{
						giveItems(player, MARK_OF_LIFE, 1);
					}
					if (!hasQuestItems(player, MARK_OF_DUELIST))
					{
						giveItems(player, MARK_OF_DUELIST, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_19":
			{
				if ((player.getClassId() == ClassId.elvenScout))
				{
					if (!hasQuestItems(player, MARK_OF_SEEKER))
					{
						giveItems(player, MARK_OF_SEEKER, 1);
					}
					if (!hasQuestItems(player, MARK_OF_LIFE))
					{
						giveItems(player, MARK_OF_LIFE, 1);
					}
					if (!hasQuestItems(player, MARK_OF_SEARCHER))
					{
						giveItems(player, MARK_OF_SEARCHER, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_20":
			{
				if ((player.getClassId() == ClassId.elvenScout))
				{
					if (!hasQuestItems(player, MARK_OF_SEEKER))
					{
						giveItems(player, MARK_OF_SEEKER, 1);
					}
					if (!hasQuestItems(player, MARK_OF_LIFE))
					{
						giveItems(player, MARK_OF_LIFE, 1);
					}
					if (!hasQuestItems(player, MARK_OF_SAGITTARIUS))
					{
						giveItems(player, MARK_OF_SAGITTARIUS, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_21":
			{
				if ((player.getClassId() == ClassId.elvenWizard))
				{
					if (!hasQuestItems(player, MARK_OF_SCHOLAR))
					{
						giveItems(player, MARK_OF_SCHOLAR, 1);
					}
					if (!hasQuestItems(player, MARK_OF_LIFE))
					{
						giveItems(player, MARK_OF_LIFE, 1);
					}
					if (!hasQuestItems(player, MARK_OF_MAGUS))
					{
						giveItems(player, MARK_OF_MAGUS, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_22":
			{
				if ((player.getClassId() == ClassId.elvenWizard))
				{
					if (!hasQuestItems(player, MARK_OF_SCHOLAR))
					{
						giveItems(player, MARK_OF_SCHOLAR, 1);
					}
					if (!hasQuestItems(player, MARK_OF_LIFE))
					{
						giveItems(player, MARK_OF_LIFE, 1);
					}
					if (!hasQuestItems(player, MARK_OF_SUMMONER))
					{
						giveItems(player, MARK_OF_SUMMONER, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_23":
			{
				if ((player.getClassId() == ClassId.oracle))
				{
					if (!hasQuestItems(player, MARK_OF_PILGRIM))
					{
						giveItems(player, MARK_OF_PILGRIM, 1);
					}
					if (!hasQuestItems(player, MARK_OF_LIFE))
					{
						giveItems(player, MARK_OF_LIFE, 1);
					}
					if (!hasQuestItems(player, MARK_OF_HEALER))
					{
						giveItems(player, MARK_OF_HEALER, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_24":
			{
				if ((player.getClassId() == ClassId.palusKnight))
				{
					if (!hasQuestItems(player, MARK_OF_DUTY))
					{
						giveItems(player, MARK_OF_DUTY, 1);
					}
					if (!hasQuestItems(player, MARK_OF_FATE))
					{
						giveItems(player, MARK_OF_FATE, 1);
					}
					if (!hasQuestItems(player, MARK_OF_WITCHCRAFT))
					{
						giveItems(player, MARK_OF_WITCHCRAFT, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_25":
			{
				if ((player.getClassId() == ClassId.palusKnight))
				{
					if (!hasQuestItems(player, MARK_OF_CHALLENGER))
					{
						giveItems(player, MARK_OF_CHALLENGER, 1);
					}
					if (!hasQuestItems(player, MARK_OF_FATE))
					{
						giveItems(player, MARK_OF_FATE, 1);
					}
					if (!hasQuestItems(player, MARK_OF_DUELIST))
					{
						giveItems(player, MARK_OF_DUELIST, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_26":
			{
				if ((player.getClassId() == ClassId.assassin))
				{
					if (!hasQuestItems(player, MARK_OF_SEEKER))
					{
						giveItems(player, MARK_OF_SEEKER, 1);
					}
					if (!hasQuestItems(player, MARK_OF_FATE))
					{
						giveItems(player, MARK_OF_FATE, 1);
					}
					if (!hasQuestItems(player, MARK_OF_SEARCHER))
					{
						giveItems(player, MARK_OF_SEARCHER, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_27":
			{
				if ((player.getClassId() == ClassId.assassin))
				{
					if (!hasQuestItems(player, MARK_OF_SEEKER))
					{
						giveItems(player, MARK_OF_SEEKER, 1);
					}
					if (!hasQuestItems(player, MARK_OF_FATE))
					{
						giveItems(player, MARK_OF_FATE, 1);
					}
					if (!hasQuestItems(player, MARK_OF_SAGITTARIUS))
					{
						giveItems(player, MARK_OF_SAGITTARIUS, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_28":
			{
				if ((player.getClassId() == ClassId.darkWizard))
				{
					if (!hasQuestItems(player, MARK_OF_SCHOLAR))
					{
						giveItems(player, MARK_OF_SCHOLAR, 1);
					}
					if (!hasQuestItems(player, MARK_OF_FATE))
					{
						giveItems(player, MARK_OF_FATE, 1);
					}
					if (!hasQuestItems(player, MARK_OF_MAGUS))
					{
						giveItems(player, MARK_OF_MAGUS, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_29":
			{
				if ((player.getClassId() == ClassId.darkWizard))
				{
					if (!hasQuestItems(player, MARK_OF_SCHOLAR))
					{
						giveItems(player, MARK_OF_SCHOLAR, 1);
					}
					if (!hasQuestItems(player, MARK_OF_FATE))
					{
						giveItems(player, MARK_OF_FATE, 1);
					}
					if (!hasQuestItems(player, MARK_OF_SUMMONER))
					{
						giveItems(player, MARK_OF_SUMMONER, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_30":
			{
				if ((player.getClassId() == ClassId.shillienOracle))
				{
					if (!hasQuestItems(player, MARK_OF_PILGRIM))
					{
						giveItems(player, MARK_OF_PILGRIM, 1);
					}
					if (!hasQuestItems(player, MARK_OF_FATE))
					{
						giveItems(player, MARK_OF_FATE, 1);
					}
					if (!hasQuestItems(player, MARK_OF_REFORMER))
					{
						giveItems(player, MARK_OF_REFORMER, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_31":
			{
				if ((player.getClassId() == ClassId.orcRaider))
				{
					if (!hasQuestItems(player, MARK_OF_CHALLENGER))
					{
						giveItems(player, MARK_OF_CHALLENGER, 1);
					}
					if (!hasQuestItems(player, MARK_OF_GLORY))
					{
						giveItems(player, MARK_OF_GLORY, 1);
					}
					if (!hasQuestItems(player, MARK_OF_CHAMPION))
					{
						giveItems(player, MARK_OF_CHAMPION, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_32":
			{
				if ((player.getClassId() == ClassId.orcMonk))
				{
					if (!hasQuestItems(player, MARK_OF_CHALLENGER))
					{
						giveItems(player, MARK_OF_CHALLENGER, 1);
					}
					if (!hasQuestItems(player, MARK_OF_GLORY))
					{
						giveItems(player, MARK_OF_GLORY, 1);
					}
					if (!hasQuestItems(player, MARK_OF_DUELIST))
					{
						giveItems(player, MARK_OF_DUELIST, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_33":
			{
				if ((player.getClassId() == ClassId.orcShaman))
				{
					if (!hasQuestItems(player, MARK_OF_PILGRIM))
					{
						giveItems(player, MARK_OF_PILGRIM, 1);
					}
					if (!hasQuestItems(player, MARK_OF_GLORY))
					{
						giveItems(player, MARK_OF_GLORY, 1);
					}
					if (!hasQuestItems(player, MARK_OF_LORD))
					{
						giveItems(player, MARK_OF_LORD, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_34":
			{
				if ((player.getClassId() == ClassId.orcShaman))
				{
					if (!hasQuestItems(player, MARK_OF_PILGRIM))
					{
						giveItems(player, MARK_OF_PILGRIM, 1);
					}
					if (!hasQuestItems(player, MARK_OF_GLORY))
					{
						giveItems(player, MARK_OF_GLORY, 1);
					}
					if (!hasQuestItems(player, MARK_OF_WARSPIRIT))
					{
						giveItems(player, MARK_OF_WARSPIRIT, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_35":
			{
				if ((player.getClassId() == ClassId.scavenger))
				{
					if (!hasQuestItems(player, MARK_OF_GUILDSMAN))
					{
						giveItems(player, MARK_OF_GUILDSMAN, 1);
					}
					if (!hasQuestItems(player, MARK_OF_PROSPERITY))
					{
						giveItems(player, MARK_OF_PROSPERITY, 1);
					}
					if (!hasQuestItems(player, MARK_OF_SEARCHER))
					{
						giveItems(player, MARK_OF_SEARCHER, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "REPLY_36":
			{
				if ((player.getClassId() == ClassId.artisan))
				{
					if (!hasQuestItems(player, MARK_OF_GUILDSMAN))
					{
						giveItems(player, MARK_OF_GUILDSMAN, 1);
					}
					if (!hasQuestItems(player, MARK_OF_PROSPERITY))
					{
						giveItems(player, MARK_OF_PROSPERITY, 1);
					}
					if (!hasQuestItems(player, MARK_OF_MAESTRO))
					{
						giveItems(player, MARK_OF_MAESTRO, 1);
					}
					htmltext = "31092-25.html";
				}
				break;
			}
			case "32487-04.html":
			{
				if (qs.isMemoState(1))
				{
					if (!npc.getVariables().getBoolean("SPAWNED", false))
					{
						npc.getVariables().set("SPAWNED", true);
						npc.getVariables().set("PLAYER_ID", player.getObjectId());
						final L2Npc pursuer = addSpawn(PURSUER, player.getX() + 50, player.getY() + 50, player.getZ(), 0, false, 0);
						pursuer.getVariables().set("PLAYER_ID", player.getObjectId());
						pursuer.getVariables().set("npc0", npc);
						pursuer.getVariables().set("player0", player);
						addAttackDesire(pursuer, player);
						htmltext = event;
					}
					else
					{
						htmltext = "32487-05.html";
					}
				}
				break;
			}
			case "32487-10.html":
			{
				if (qs.isMemoState(7))
				{
					takeItems(player, HELVETIAS_ANTIDOTE, 1);
					qs.setMemoState(8);
					qs.setCond(8, true);
					if (npc.getVariables().getBoolean("SPAWNED", true))
					{
						npc.getVariables().set("SPAWNED", false);
					}
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
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isStarted() && Util.checkIfInRange(1500, npc, killer, true))
		{
			if (qs.isMemoState(1))
			{
				if (killer.isPlayer())
				{
					if (killer.getObjectId() == npc.getVariables().getInt("PLAYER_ID", 0))
					{
						qs.setMemoState(2);
						qs.setCond(2, true);
						npc.broadcastPacket(new NpcSay(npc, Say2.NPC_ALL, NpcStringId.YOU_ARE_STRONG_THIS_WAS_A_MISTAKE));
					}
					else
					{
						npc.broadcastPacket(new NpcSay(npc, Say2.NPC_ALL, NpcStringId.WHO_ARE_YOU_TO_JOIN_IN_THE_BATTLE_HOW_UPSETTING));
					}
				}
			}
			final L2Npc npc0 = npc.getVariables().getObject("npc0", L2Npc.class);
			if (npc0 != null)
			{
				npc0.getVariables().set("SPAWNED", false);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		final int memoState = qs.getMemoState();
		String htmltext = getNoQuestMsg(player);
		if (qs.isCreated())
		{
			if (npc.getId() == BLUEPRINT_SELLER_DAEGER)
			{
				if (player.getRace() != Race.KAMAEL)
				{
					if (player.isInCategory(CategoryType.SECOND_CLASS_GROUP))
					{
						htmltext = (player.getLevel() >= MIN_LEVEL) ? "31435-01.htm" : "31435-03.htm";
					}
					else
					{
						htmltext = "31435-04.htm";
					}
				}
				else
				{
					htmltext = "31435-06.htm";
				}
			}
		}
		else if (qs.isStarted())
		{
			switch (npc.getId())
			{
				case BLUEPRINT_SELLER_DAEGER:
				{
					if (memoState <= 2)
					{
						htmltext = "31435-08.html";
					}
					else if (memoState == 3)
					{
						htmltext = "31435-09.html";
					}
					else if (memoState == 4)
					{
						htmltext = "31435-11.html";
					}
					else if ((memoState > 4) && (memoState < 8))
					{
						htmltext = "31435-12.html";
					}
					else if (memoState == 8)
					{
						htmltext = "31435-13.html";
					}
					else if (memoState == 9)
					{
						qs.setMemoState(10);
						qs.setCond(10, true);
						htmltext = "31435-15.html";
					}
					else if (memoState == 10)
					{
						htmltext = "31435-16.html";
					}
					break;
				}
				case GROCER_HELVERIA:
				{
					if (memoState == 4)
					{
						htmltext = "30081-01.html";
					}
					else if (memoState == 5)
					{
						htmltext = "30081-04.html";
					}
					else if (memoState == 6)
					{
						htmltext = "30081-08.html";
					}
					else if (memoState == 7)
					{
						if (!hasQuestItems(player, HELVETIAS_ANTIDOTE))
						{
							giveItems(player, HELVETIAS_ANTIDOTE, 1);
							htmltext = "30081-09.html";
						}
						else
						{
							htmltext = "30081-10.html";
						}
					}
					break;
				}
				case BLACK_MARKETEER_OF_MAMMON:
				{
					if (memoState == 10)
					{
						if (player.isInCategory(CategoryType.SECOND_CLASS_GROUP))
						{
							qs.setMemoStateEx(1, 0);
							htmltext = "31092-01.html";
						}
						else
						{
							giveItems(player, Inventory.ADENA_ID, THREE_MILLION);
							qs.exitQuest(false, true);
							htmltext = "31092-01a.html";
						}
					}
					break;
				}
				case MARK:
				{
					if (memoState == 1)
					{
						if (!npc.getVariables().getBoolean("SPAWNED", false))
						{
							htmltext = "32487-01.html";
						}
						else if (npc.getVariables().getBoolean("SPAWNED", true) && (npc.getVariables().getInt("PLAYER_ID", 0) == player.getObjectId()))
						{
							htmltext = "32487-03.html";
						}
						else if (npc.getVariables().getBoolean("SPAWNED", true))
						{
							htmltext = "32487-02.html";
						}
					}
					else if (memoState == 2)
					{
						giveItems(player, BLOODY_CLOTH_FRAGMENT, 1);
						qs.setMemoState(3);
						qs.setCond(3, true);
						htmltext = "32487-06.html";
					}
					else if ((memoState >= 3) && (memoState < 7))
					{
						htmltext = "32487-07.html";
					}
					else if (memoState == 7)
					{
						htmltext = "32487-09.html";
					}
					break;
				}
			}
		}
		else if (qs.isCompleted())
		{
			if (npc.getId() == BLUEPRINT_SELLER_DAEGER)
			{
				htmltext = getAlreadyCompletedMsg(player);
			}
			else if (npc.getId() == BLACK_MARKETEER_OF_MAMMON)
			{
				if (player.isInCategory(CategoryType.SECOND_CLASS_GROUP))
				{
					htmltext = "31092-23.html";
				}
				else
				{
					htmltext = "31092-24.html";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		startQuestTimer("DESPAWN", 60000, npc, null);
		final L2PcInstance player = npc.getVariables().getObject("player0", L2PcInstance.class);
		if (player != null)
		{
			if (player.isPlayer())
			{
				npc.broadcastPacket(new NpcSay(npc, Say2.NPC_ALL, NpcStringId.S1_I_MUST_KILL_YOU_BLAME_YOUR_OWN_CURIOSITY).addStringParameter(player.getAppearance().getVisibleName()));
			}
		}
		return super.onSpawn(npc);
	}
}