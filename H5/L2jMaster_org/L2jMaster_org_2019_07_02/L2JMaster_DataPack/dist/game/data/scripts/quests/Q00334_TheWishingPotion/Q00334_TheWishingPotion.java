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
package quests.Q00334_TheWishingPotion;

import java.util.ArrayList;
import java.util.List;

import com.l2jserver.gameserver.enums.audio.Sound;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.network.NpcStringId;
import com.l2jserver.gameserver.util.Util;

/**
 * The Wishing Potion (334)
 * @author Zealar
 */
public final class Q00334_TheWishingPotion extends Quest
{
	// NPCs
	private static final int TORAI = 30557;
	private static final int ALCHEMIST_MATILD = 30738;
	private static final int FAIRY_RUPINA = 30742;
	private static final int WISDOM_CHEST = 30743;
	
	// Monsters
	private static final int WHISPERING_WIND = 20078;
	private static final int ANT_SOLDIER = 20087;
	private static final int ANT_WARRIOR_CAPTAIN = 20088;
	private static final int SILENOS = 20168;
	private static final int TYRANT = 20192;
	private static final int TYRANT_KINGPIN = 20193;
	private static final int AMBER_BASILISK = 20199;
	private static final int MIST_HORROR_RIPPER = 20227;
	private static final int TURAK_BUGBEAR = 20248;
	private static final int TURAK_BUGBEAR_WARRIOR = 20249;
	private static final int GRIMA = 27135;
	private static final int GLASS_JAGUAR = 20250;
	private static final int SUCCUBUS_OF_SEDUCTION = 27136;
	private static final int GREAT_DEMON_KING = 27138;
	private static final int SECRET_KEEPER_TREE = 27139;
	private static final int DLORD_ALEXANDROSANCHES = 27153;
	private static final int ABYSSKING_BONAPARTERIUS = 27154;
	private static final int EVILOVERLORD_RAMSEBALIUS = 27155;
	
	// Items
	private static final int Q_WISH_POTION = 3467;
	private static final int Q_ANCIENT_CROWN = 3468;
	private static final int Q_CERTIFICATE_OF_ROYALTY = 3469;
	private static final int Q_ALCHEMY_TEXT = 3678;
	private static final int Q_SECRET_BOOK_OF_POTION = 3679;
	private static final int Q_POTION_RECIPE_1 = 3680;
	private static final int Q_POTION_RECIPE_2 = 3681;
	private static final int Q_MATILDS_ORB = 3682;
	private static final int Q_FOBBIDEN_LOVE_SCROLL = 3683;
	
	// Items required for create wish potion
	private static final int Q_AMBER_SCALE = 3684;
	private static final int Q_WIND_SOULSTONE = 3685;
	private static final int Q_GLASS_EYE = 3686;
	private static final int Q_HORROR_ECTOPLASM = 3687;
	private static final int Q_SILENOS_HORN = 3688;
	private static final int Q_ANT_SOLDIER_APHID = 3689;
	private static final int Q_TYRANTS_CHITIN = 3690;
	private static final int Q_BUGBEAR_BLOOD = 3691;
	
	// Rewards
	private static final int NECKLACE_OF_GRACE = 931;
	private static final int DEMONS_TUNIC_FABRIC = 1979;
	private static final int DEMONS_HOSE_PATTERN = 1980;
	private static final int DEMONS_BOOTS_FABRIC = 2952;
	private static final int DEMONS_GLOVES_FABRIC = 2953;
	private static final int Q_MUSICNOTE_LOVE = 4408;
	private static final int Q_MUSICNOTE_BATTLE = 4409;
	private static final int Q_GOLD_CIRCLET = 12766;
	private static final int Q_SILVER_CIRCLET = 12767;
	
	private static final int DEMONS_TUNIC = 441;
	private static final int DEMONS_HOSE = 472;
	private static final int DEMONS_BOOTS = 2435;
	private static final int DEMONS_GLOVES = 2459;
	
	// Misc
	private static final String FLAG = "flag";
	private static final String I_QUEST0 = "i_quest0";
	private static final String EXCHANGE = "Exchange";
	
	// Reward
	public Q00334_TheWishingPotion()
	{
		super(334, Q00334_TheWishingPotion.class.getSimpleName(), "The Wishing Potion");
		addStartNpc(ALCHEMIST_MATILD);
		addTalkId(ALCHEMIST_MATILD, TORAI, FAIRY_RUPINA, WISDOM_CHEST);
		addKillId(WHISPERING_WIND, ANT_SOLDIER, ANT_WARRIOR_CAPTAIN, SILENOS, TYRANT, TYRANT_KINGPIN, AMBER_BASILISK, MIST_HORROR_RIPPER);
		addKillId(TURAK_BUGBEAR, TURAK_BUGBEAR_WARRIOR, GRIMA, GLASS_JAGUAR, SUCCUBUS_OF_SEDUCTION, GREAT_DEMON_KING, SECRET_KEEPER_TREE);
		addKillId(DLORD_ALEXANDROSANCHES, ABYSSKING_BONAPARTERIUS, EVILOVERLORD_RAMSEBALIUS);
		addSpawnId(GRIMA, SUCCUBUS_OF_SEDUCTION, GREAT_DEMON_KING, DLORD_ALEXANDROSANCHES, ABYSSKING_BONAPARTERIUS, EVILOVERLORD_RAMSEBALIUS, FAIRY_RUPINA);
		registerQuestItems(Q_SECRET_BOOK_OF_POTION, Q_AMBER_SCALE, Q_GLASS_EYE, Q_HORROR_ECTOPLASM, Q_SILENOS_HORN, Q_ANT_SOLDIER_APHID, Q_TYRANTS_CHITIN, Q_BUGBEAR_BLOOD);
		registerQuestItems(Q_WISH_POTION, Q_POTION_RECIPE_1, Q_POTION_RECIPE_2, Q_MATILDS_ORB, Q_ALCHEMY_TEXT);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (npc.getId())
		{
			case TORAI:
			{
				if (qs.hasQuestItems(Q_FOBBIDEN_LOVE_SCROLL))
				{
					qs.giveAdena(500000, true);
					qs.takeItems(Q_FOBBIDEN_LOVE_SCROLL, 1);
					qs.playSound(Sound.ITEMSOUND_QUEST_MIDDLE);
					return "30557-01.html";
				}
				break;
			}
			case ALCHEMIST_MATILD:
			{
				if (qs.isCreated())
				{
					if (player.getLevel() < 30)
					{
						return "30738-01.htm";
					}
					return "30738-02.html";
				}
				if (!qs.hasQuestItems(Q_SECRET_BOOK_OF_POTION) && qs.hasQuestItems(Q_ALCHEMY_TEXT))
				{
					return "30738-05.html";
				}
				if (qs.hasQuestItems(Q_SECRET_BOOK_OF_POTION) && qs.hasQuestItems(Q_ALCHEMY_TEXT))
				{
					return "30738-06.html";
				}
				if (qs.hasQuestItems(Q_POTION_RECIPE_1, Q_POTION_RECIPE_2) && (!qs.hasQuestItems(Q_AMBER_SCALE) || (qs.hasQuestItems(Q_WIND_SOULSTONE) && !qs.hasQuestItems(Q_GLASS_EYE))
					|| (!qs.hasQuestItems(Q_HORROR_ECTOPLASM) || !qs.hasQuestItems(Q_SILENOS_HORN) || !qs.hasQuestItems(Q_ANT_SOLDIER_APHID) || !qs.hasQuestItems(Q_TYRANTS_CHITIN) || !qs.hasQuestItems(Q_BUGBEAR_BLOOD))))
				{
					return "30738-08.html";
				}
				if (qs.hasQuestItems(Q_POTION_RECIPE_1, Q_POTION_RECIPE_2, Q_AMBER_SCALE, Q_WIND_SOULSTONE, Q_WIND_SOULSTONE, Q_GLASS_EYE, Q_GLASS_EYE, Q_HORROR_ECTOPLASM, Q_SILENOS_HORN, Q_ANT_SOLDIER_APHID, Q_TYRANTS_CHITIN, Q_BUGBEAR_BLOOD))
				{
					return "30738-09.html";
				}
				if (qs.hasQuestItems(Q_MATILDS_ORB) && !qs.hasQuestItems(Q_POTION_RECIPE_1) && !qs.hasQuestItems(Q_POTION_RECIPE_2) && (!qs.hasQuestItems(Q_AMBER_SCALE) || (qs.hasQuestItems(Q_WIND_SOULSTONE) && !qs.hasQuestItems(Q_GLASS_EYE)) || !qs.hasQuestItems(Q_HORROR_ECTOPLASM)
					|| !qs.hasQuestItems(Q_SILENOS_HORN) || !qs.hasQuestItems(Q_ANT_SOLDIER_APHID) || !qs.hasQuestItems(Q_TYRANTS_CHITIN) || !qs.hasQuestItems(Q_BUGBEAR_BLOOD)))
				{
					return "30738-12.html";
				}
				break;
			}
			case FAIRY_RUPINA:
			{
				if (qs.getInt(FLAG) == 1)
				{
					String html = null;
					if ((getRandom(4) < 4))
					{
						qs.giveItems(NECKLACE_OF_GRACE, 1);
						qs.set(FLAG, 0);
						html = "30742-01.html";
					}
					else
					{
						switch (getRandom(4))
						{
							case 0:
							{
								qs.giveItems(DEMONS_TUNIC_FABRIC, 1);
								break;
							}
							case 1:
							{
								qs.giveItems(DEMONS_HOSE_PATTERN, 1);
								break;
							}
							case 2:
							{
								qs.giveItems(DEMONS_BOOTS_FABRIC, 1);
								break;
							}
							case 3:
							{
								qs.giveItems(DEMONS_GLOVES_FABRIC, 1);
							}
						}
						html = "30742-02.html";
					}
					qs.set(FLAG, 0);
					npc.deleteMe();
					return html;
				}
				break;
			}
			case WISDOM_CHEST:
			{
				if (qs.getInt(FLAG) == 4)
				{
					int random = getRandom(100);
					String html = null;
					if (random < 10)
					{
						qs.giveItems(Q_FOBBIDEN_LOVE_SCROLL, 1);
						html = "30743-02.html";
					}
					else if ((random >= 10) && (random < 50))
					{
						switch (getRandom(4))
						{
							case 0:
							{
								qs.giveItems(DEMONS_TUNIC_FABRIC, 1);
								break;
							}
							case 1:
							{
								qs.giveItems(DEMONS_HOSE_PATTERN, 1);
								break;
							}
							case 2:
							{
								qs.giveItems(DEMONS_BOOTS_FABRIC, 1);
								break;
							}
							case 3:
							{
								qs.giveItems(DEMONS_GLOVES_FABRIC, 1);
								break;
							}
						}
						html = "30743-03.html";
					}
					else if ((random >= 50) && (random < 100))
					{
						switch (getRandom(2))
						{
							case 0:
							{
								qs.giveItems(Q_MUSICNOTE_LOVE, 1);
								break;
							}
							case 1:
							{
								qs.giveItems(Q_MUSICNOTE_BATTLE, 1);
								break;
							}
						}
						html = "30743-04.html";
					}
					else if ((random >= 85) && (random < 95))
					{
						switch (getRandom(4))
						{
							case 0:
							{
								qs.giveItems(DEMONS_TUNIC, 1);
								break;
							}
							case 1:
							{
								qs.giveItems(DEMONS_HOSE, 1);
								break;
							}
							case 2:
							{
								qs.giveItems(DEMONS_BOOTS, 1);
								break;
							}
							case 3:
							{
								qs.giveItems(DEMONS_GLOVES, 1);
								break;
							}
						}
						html = "30743-05.html";
					}
					else if (random >= 95)
					{
						switch (getRandom(2))
						{
							case 0:
							{
								qs.giveItems(Q_GOLD_CIRCLET, 1);
								break;
							}
							case 1:
							{
								qs.giveItems(Q_SILVER_CIRCLET, 1);
							}
						}
						html = "30743-06.htm";
					}
					qs.set(FLAG, 0);
					npc.deleteMe();
					return html;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		switch (npc.getId())
		{
			case GRIMA:
			{
				startQuestTimer("2336002", 1000 * 200, npc, null);
				npc.say(NpcStringId.OH_OH_OH);
				break;
			}
			case SUCCUBUS_OF_SEDUCTION:
			{
				startQuestTimer("2336003", 1000 * 200, npc, null);
				npc.say(NpcStringId.DO_YOU_WANT_US_TO_LOVE_YOU_OH);
				break;
			}
			case GREAT_DEMON_KING:
			{
				startQuestTimer("2336007", 1000 * 600, npc, null);
				npc.say(NpcStringId.WHO_KILLED_MY_UNDERLING_DEVIL);
				break;
			}
			case DLORD_ALEXANDROSANCHES:
			{
				startQuestTimer("2336004", 1000 * 200, npc, null);
				npc.say(NpcStringId.WHO_IS_CALLING_THE_LORD_OF_DARKNESS);
				break;
			}
			case ABYSSKING_BONAPARTERIUS:
			{
				startQuestTimer("2336005", 1000 * 200, npc, null);
				npc.say(NpcStringId.I_AM_A_GREAT_EMPIRE_BONAPARTERIUS);
				break;
			}
			case EVILOVERLORD_RAMSEBALIUS:
			{
				startQuestTimer("2336006", 1000 * 200, npc, null);
				npc.say(NpcStringId.LET_YOUR_HEAD_DOWN_BEFORE_THE_LORD);
				break;
			}
			case FAIRY_RUPINA:
			{
				startQuestTimer("2336001", 120 * 1000, npc, null);
				npc.say(NpcStringId.I_WILL_MAKE_YOUR_LOVE_COME_TRUE_LOVE_LOVE_LOVE);
				break;
			}
			case WISDOM_CHEST:
			{
				startQuestTimer("2336007", 120 * 1000, npc, null);
				npc.say(NpcStringId.I_HAVE_WISDOM_IN_ME_I_AM_THE_BOX_OF_WISDOM);
				break;
			}
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (npc.getId())
		{
			case FAIRY_RUPINA:
			case GRIMA:
			case SUCCUBUS_OF_SEDUCTION:
			case GREAT_DEMON_KING:
			{
				npc.deleteMe();
				break;
			}
			case DLORD_ALEXANDROSANCHES:
			{
				npc.say(NpcStringId.OH_ITS_NOT_AN_OPPONENT_OF_MINE_HA_HA_HA);
				npc.deleteMe();
				break;
			}
			case ABYSSKING_BONAPARTERIUS:
			{
				npc.say(NpcStringId.OH_ITS_NOT_AN_OPPONENT_OF_MINE_HA_HA_HA);
				npc.deleteMe();
				break;
			}
			case EVILOVERLORD_RAMSEBALIUS:
			{
				npc.say(NpcStringId.OH_ITS_NOT_AN_OPPONENT_OF_MINE_HA_HA_HA);
				npc.deleteMe();
				break;
			}
			case WISDOM_CHEST:
			{
				npc.say(NpcStringId.DONT_INTERRUPT_MY_REST_AGAIN);
				npc.say(NpcStringId.YOURE_A_GREAT_DEVIL_NOW);
				npc.deleteMe();
				break;
			}
			case ALCHEMIST_MATILD:
			{
				final QuestState qs = getQuestState(player, false);
				
				if (event.equals("QUEST_ACCEPTED"))
				{
					qs.playSound(Sound.ITEMSOUND_QUEST_ACCEPT);
					qs.startQuest();
					qs.setMemoState(1);
					qs.setCond(1);
					qs.showQuestionMark(334);
					if (!qs.hasQuestItems(Q_ALCHEMY_TEXT))
					{
						qs.giveItems(Q_ALCHEMY_TEXT, 1);
					}
					qs.playSound(Sound.ITEMSOUND_QUEST_MIDDLE);
					return "30738-04.htm";
				}
				
				switch (Integer.parseInt(event))
				{
					case 1:
					{
						return "30738-03.htm";
					}
					case 2:
					{
						qs.takeItems(Q_SECRET_BOOK_OF_POTION, -1);
						qs.takeItems(Q_ALCHEMY_TEXT, -1);
						qs.giveItems(Q_POTION_RECIPE_1, 1);
						qs.giveItems(Q_POTION_RECIPE_2, 1);
						qs.setMemoState(2);
						qs.setCond(3, true);
						qs.showQuestionMark(334);
						return "30738-07.html";
					}
					case 3:
					{
						return "30738-10.html";
					}
					case 4:
					{
						if (qs.hasQuestItems(Q_AMBER_SCALE, Q_GLASS_EYE, Q_HORROR_ECTOPLASM, Q_SILENOS_HORN, Q_ANT_SOLDIER_APHID, Q_TYRANTS_CHITIN, Q_BUGBEAR_BLOOD, Q_WIND_SOULSTONE, Q_POTION_RECIPE_1, Q_POTION_RECIPE_2))
						{
							qs.giveItems(Q_WISH_POTION, 1);
							if (!qs.hasQuestItems(Q_MATILDS_ORB))
							{
								qs.giveItems(Q_MATILDS_ORB, 1);
							}
							qs.takeItems(Q_AMBER_SCALE, 1);
							qs.takeItems(Q_GLASS_EYE, 1);
							qs.takeItems(Q_HORROR_ECTOPLASM, 1);
							qs.takeItems(Q_SILENOS_HORN, 1);
							qs.takeItems(Q_ANT_SOLDIER_APHID, 1);
							qs.takeItems(Q_TYRANTS_CHITIN, 1);
							qs.takeItems(Q_BUGBEAR_BLOOD, 1);
							qs.takeItems(Q_WIND_SOULSTONE, 1);
							qs.takeItems(Q_POTION_RECIPE_1, -1);
							qs.takeItems(Q_POTION_RECIPE_2, -1);
							qs.setMemoState(2);
							qs.playSound(Sound.ITEMSOUND_QUEST_MIDDLE);
							qs.setCond(5);
							qs.showQuestionMark(334);
							return "30738-11.html";
						}
						break;
					}
					case 5:
					{
						if (qs.hasQuestItems(Q_WISH_POTION))
						{
							if (qs.getInt(I_QUEST0) != 1)
							{
								qs.set(I_QUEST0, 0);
							}
							return "30738-13.html";
						}
						return "30738-14.html";
					}
					case 6:
					{
						if (qs.hasQuestItems(Q_WISH_POTION))
						{
							return "30738-15a.html";
						}
						qs.giveItems(Q_POTION_RECIPE_1, 1);
						qs.giveItems(Q_POTION_RECIPE_2, 1);
						return "30738-15.html";
					}
					case 7:
					{
						if (qs.hasQuestItems(Q_WISH_POTION))
						{
							if (qs.getInt(EXCHANGE) == 0)
							{
								
								qs.takeItems(Q_WISH_POTION, 1);
								qs.set(I_QUEST0, 1);
								qs.set(FLAG, 1);
								startQuestTimer("2336008", 3 * 1000, npc, player);
								return "30738-16.html";
							}
							return "30738-20.html";
						}
						return "30738-14.html";
					}
					case 8:
					{
						if (qs.hasQuestItems(Q_WISH_POTION))
						{
							if (qs.getInt(EXCHANGE) == 0)
							{
								
								qs.takeItems(Q_WISH_POTION, 1);
								qs.set(I_QUEST0, 2);
								qs.set(FLAG, 2);
								startQuestTimer("2336008", 3 * 1000, npc, player);
								return "30738-17.html";
							}
							return "30738-20.html";
						}
						return "30738-14.html";
					}
					case 9:
					{
						if (qs.hasQuestItems(Q_WISH_POTION))
						{
							if (qs.getInt(EXCHANGE) == 0)
							{
								
								qs.takeItems(Q_WISH_POTION, 1);
								qs.set(I_QUEST0, 3);
								qs.set(FLAG, 3);
								startQuestTimer("2336008", 3 * 1000, npc, player);
								return "30738-18.html";
							}
							return "30738-20.html";
						}
						return "30738-14.html";
					}
					case 10:
					{
						if (qs.hasQuestItems(Q_WISH_POTION))
						{
							if (qs.getInt(EXCHANGE) == 0)
							{
								
								qs.takeItems(Q_WISH_POTION, 1);
								qs.set(I_QUEST0, 4);
								qs.set(FLAG, 4);
								startQuestTimer("2336008", 3 * 1000, npc, player);
								return "30738-19.html";
							}
							return "30738-20.html";
						}
						return "30738-14.html";
					}
					case 2336008:
					{
						npc.say(NpcStringId.OK_EVERYBODY_PRAY_FERVENTLY);
						startQuestTimer("2336009", 4 * 1000, npc, player);
						break;
					}
					case 2336009:
					{
						npc.say(NpcStringId.BOTH_HANDS_TO_HEAVEN_EVERYBODY_YELL_TOGETHER);
						startQuestTimer("2336010", 4 * 1000, npc, player);
						break;
					}
					case 2336010:
					{
						npc.say(NpcStringId.ONE_TWO_MAY_YOUR_DREAMS_COME_TRUE);
						int i0 = 0;
						switch (qs.getInt(I_QUEST0))
						{
							case 1:
								i0 = getRandom(2);
								break;
							case 2:
							case 3:
							case 4:
								i0 = getRandom(3);
								break;
						}
						switch (i0)
						{
							case 0:
							{
								switch (qs.getInt(I_QUEST0))
								{
									case 1:
									{
										addSpawn(FAIRY_RUPINA, npc, true, 0, false);
										qs.set("Exchange", 0);
										break;
									}
									case 2:
									{
										addSpawn(GRIMA, npc, true, 0, false);
										addSpawn(GRIMA, npc, true, 0, false);
										addSpawn(GRIMA, npc, true, 0, false);
										qs.set("Exchange", 0);
										break;
									}
									case 3:
									{
										qs.giveItems(Q_CERTIFICATE_OF_ROYALTY, 1);
										qs.set("Exchange", 0);
										break;
									}
									case 4:
									{
										addSpawn(WISDOM_CHEST, npc, true, 0, false);
										qs.set("Exchange", 0);
										break;
									}
								}
								break;
							}
							case 1:
							{
								switch (qs.getInt(I_QUEST0))
								{
									case 1:
									{
										addSpawn(SUCCUBUS_OF_SEDUCTION, npc, true, 0, false);
										addSpawn(SUCCUBUS_OF_SEDUCTION, npc, true, 0, false);
										addSpawn(SUCCUBUS_OF_SEDUCTION, npc, true, 0, false);
										addSpawn(SUCCUBUS_OF_SEDUCTION, npc, true, 0, false);
										qs.set("Exchange", 0);
										break;
									}
									case 2:
									{
										qs.giveAdena(10000, true);
										qs.set("Exchange", 0);
										break;
									}
									case 3:
									{
										addSpawn(DLORD_ALEXANDROSANCHES, npc, true, 0, false);
										qs.set("Exchange", 0);
										break;
									}
									case 4:
									{
										addSpawn(WISDOM_CHEST, npc, true, 0, false);
										qs.set("Exchange", 0);
										break;
									}
								}
								break;
							}
							case 2:
							{
								switch (qs.getInt(I_QUEST0))
								{
									case 2:
									{
										qs.giveAdena(10000, true);
										qs.set("Exchange", 0);
										break;
									}
									case 3:
									{
										qs.giveItems(Q_ANCIENT_CROWN, 1);
										qs.set("Exchange", 0);
										break;
									}
									case 4:
									{
										addSpawn(WISDOM_CHEST, npc, true, 0, false);
										qs.set("Exchange", 0);
										break;
									}
								}
								break;
							}
						}
						break;
					}
				}
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getRandomPlayerFromParty(killer, npc);
		if (qs != null)
		{
			switch (npc.getId())
			{
				case WHISPERING_WIND:
				{
					if (qs.hasQuestItems(Q_POTION_RECIPE_1, Q_POTION_RECIPE_2) && !qs.hasQuestItems(Q_WIND_SOULSTONE))
					{
						if (getRandom(10) == 0)
						{
							qs.giveItems(Q_WIND_SOULSTONE, 1);
							qs.playSound(Sound.ITEMSOUND_QUEST_ITEMGET);
							if (qs.hasQuestItems(Q_AMBER_SCALE, Q_GLASS_EYE, Q_HORROR_ECTOPLASM, Q_SILENOS_HORN, Q_ANT_SOLDIER_APHID, Q_TYRANTS_CHITIN, Q_BUGBEAR_BLOOD))
							{
								qs.setCond(4, true);
								qs.showQuestionMark(334);
							}
							else
							{
								qs.playSound(Sound.ITEMSOUND_QUEST_ITEMGET);
							}
						}
					}
					break;
				}
				case ANT_SOLDIER:
				case ANT_WARRIOR_CAPTAIN:
				{
					if (qs.hasQuestItems(Q_POTION_RECIPE_1, Q_POTION_RECIPE_2) && !qs.hasQuestItems(Q_ANT_SOLDIER_APHID))
					{
						if (getRandom(10) == 0)
						{
							qs.giveItems(Q_ANT_SOLDIER_APHID, 1);
							qs.playSound(Sound.ITEMSOUND_QUEST_ITEMGET);
							if (qs.hasQuestItems(Q_AMBER_SCALE, Q_WIND_SOULSTONE, Q_GLASS_EYE, Q_HORROR_ECTOPLASM, Q_SILENOS_HORN, Q_ANT_SOLDIER_APHID, Q_TYRANTS_CHITIN, Q_BUGBEAR_BLOOD))
							{
								qs.setCond(4, true);
								qs.showQuestionMark(334);
							}
							else
							{
								qs.playSound(Sound.ITEMSOUND_QUEST_ITEMGET);
							}
						}
					}
					break;
				}
				case SILENOS:
				{
					if (qs.hasQuestItems(Q_POTION_RECIPE_1, Q_POTION_RECIPE_2) && !qs.hasQuestItems(Q_SILENOS_HORN))
					{
						if (getRandom(10) == 0)
						{
							qs.giveItems(Q_SILENOS_HORN, 1);
							qs.playSound(Sound.ITEMSOUND_QUEST_ITEMGET);
							if (qs.hasQuestItems(Q_AMBER_SCALE, Q_WIND_SOULSTONE, Q_GLASS_EYE, Q_HORROR_ECTOPLASM, Q_ANT_SOLDIER_APHID, Q_TYRANTS_CHITIN, Q_BUGBEAR_BLOOD))
							{
								qs.setCond(4, true);
								qs.showQuestionMark(334);
							}
							else
							{
								qs.playSound(Sound.ITEMSOUND_QUEST_ITEMGET);
							}
						}
					}
					break;
				}
				case TYRANT:
				case TYRANT_KINGPIN:
				{
					if (qs.hasQuestItems(Q_POTION_RECIPE_1, Q_POTION_RECIPE_2) && !qs.hasQuestItems(Q_TYRANTS_CHITIN))
					{
						if (getRandom(10) == 0)
						{
							qs.giveItems(Q_TYRANTS_CHITIN, 1);
							qs.playSound(Sound.ITEMSOUND_QUEST_ITEMGET);
							if (qs.hasQuestItems(Q_AMBER_SCALE, Q_WIND_SOULSTONE, Q_GLASS_EYE, Q_HORROR_ECTOPLASM, Q_SILENOS_HORN, Q_ANT_SOLDIER_APHID, Q_TYRANTS_CHITIN, Q_BUGBEAR_BLOOD))
							{
								qs.setCond(4, true);
								qs.showQuestionMark(334);
							}
							else
							{
								qs.playSound(Sound.ITEMSOUND_QUEST_ITEMGET);
							}
						}
					}
					break;
				}
				case AMBER_BASILISK:
				{
					if (qs.hasQuestItems(Q_POTION_RECIPE_1, Q_POTION_RECIPE_2) && !qs.hasQuestItems(Q_AMBER_SCALE))
					{
						if (getRandom(10) == 0)
						{
							qs.giveItems(Q_AMBER_SCALE, 1);
							qs.playSound(Sound.ITEMSOUND_QUEST_ITEMGET);
							if (qs.hasQuestItems(Q_WIND_SOULSTONE, Q_GLASS_EYE, Q_HORROR_ECTOPLASM, Q_SILENOS_HORN, Q_ANT_SOLDIER_APHID, Q_TYRANTS_CHITIN, Q_BUGBEAR_BLOOD))
							{
								qs.setCond(4, true);
								qs.showQuestionMark(334);
							}
							else
							{
								qs.playSound(Sound.ITEMSOUND_QUEST_ITEMGET);
							}
						}
					}
					break;
				}
				case MIST_HORROR_RIPPER:
				{
					if (qs.hasQuestItems(Q_POTION_RECIPE_1, Q_POTION_RECIPE_2) && !qs.hasQuestItems(Q_HORROR_ECTOPLASM))
					{
						if (getRandom(10) == 0)
						{
							qs.giveItems(Q_HORROR_ECTOPLASM, 1);
							qs.playSound(Sound.ITEMSOUND_QUEST_ITEMGET);
							if (qs.hasQuestItems(Q_AMBER_SCALE, Q_WIND_SOULSTONE, Q_GLASS_EYE, Q_HORROR_ECTOPLASM, Q_SILENOS_HORN, Q_ANT_SOLDIER_APHID, Q_TYRANTS_CHITIN, Q_BUGBEAR_BLOOD))
							{
								qs.setCond(4, true);
								qs.showQuestionMark(334);
							}
							else
							{
								qs.playSound(Sound.ITEMSOUND_QUEST_ITEMGET);
							}
						}
					}
					break;
				}
				case TURAK_BUGBEAR:
				case TURAK_BUGBEAR_WARRIOR:
				{
					if (qs.hasQuestItems(Q_POTION_RECIPE_1, Q_POTION_RECIPE_2) && !qs.hasQuestItems(Q_BUGBEAR_BLOOD))
					{
						if (getRandom(10) == 0)
						{
							qs.giveItems(Q_BUGBEAR_BLOOD, 1);
							qs.playSound(Sound.ITEMSOUND_QUEST_ITEMGET);
							if (qs.hasQuestItems(Q_AMBER_SCALE, Q_WIND_SOULSTONE, Q_GLASS_EYE, Q_HORROR_ECTOPLASM, Q_SILENOS_HORN, Q_ANT_SOLDIER_APHID, Q_TYRANTS_CHITIN, Q_BUGBEAR_BLOOD))
							{
								qs.setCond(4, true);
								qs.showQuestionMark(334);
							}
							else
							{
								qs.playSound(Sound.ITEMSOUND_QUEST_ITEMGET);
							}
						}
					}
					break;
				}
				case GLASS_JAGUAR:
				{
					if (qs.hasQuestItems(Q_POTION_RECIPE_1, Q_POTION_RECIPE_2) && !qs.hasQuestItems(Q_GLASS_EYE))
					{
						if (getRandom(10) == 0)
						{
							qs.giveItems(Q_GLASS_EYE, 1);
							qs.playSound(Sound.ITEMSOUND_QUEST_ITEMGET);
							if (qs.hasQuestItems(Q_AMBER_SCALE, Q_WIND_SOULSTONE, Q_GLASS_EYE, Q_HORROR_ECTOPLASM, Q_SILENOS_HORN, Q_ANT_SOLDIER_APHID, Q_TYRANTS_CHITIN, Q_BUGBEAR_BLOOD))
							{
								qs.setCond(4, true);
								qs.showQuestionMark(334);
							}
							else
							{
								qs.playSound(Sound.ITEMSOUND_QUEST_ITEMGET);
							}
						}
					}
					break;
				}
				case GRIMA:
				{
					if (qs.isMemoState(2) && (qs.getInt(FLAG) == 2))
					{
						if (getRandom(1000) < 33)
						{
							if (getRandom(1000) == 0)
							{
								qs.giveAdena(100_000_000, true);
							}
							else
							{
								qs.giveAdena(900_000, true);
							}
							qs.playSound(Sound.ITEMSOUND_QUEST_ITEMGET);
							qs.set(FLAG, 0);
						}
					}
					break;
				}
				case SUCCUBUS_OF_SEDUCTION:
				{
					if (qs.isMemoState(2) && !qs.hasQuestItems(Q_FOBBIDEN_LOVE_SCROLL) && (qs.getInt(FLAG) == 1) && (getRandom(1000) < 28))
					{
						qs.giveItems(Q_FOBBIDEN_LOVE_SCROLL, 1);
						qs.playSound(Sound.ITEMSOUND_QUEST_ITEMGET);
						qs.set(FLAG, 0);
					}
					break;
				}
				case GREAT_DEMON_KING:
				{
					if (qs.isMemoState(2) && (qs.getInt(FLAG) == 3))
					{
						qs.giveAdena(1_406_956, true);
						qs.playSound(Sound.ITEMSOUND_QUEST_ITEMGET);
						qs.set(FLAG, 0);
					}
					break;
				}
				case SECRET_KEEPER_TREE:
				{
					if (qs.isMemoState(1) && !qs.hasQuestItems(Q_SECRET_BOOK_OF_POTION))
					{
						qs.giveItems(Q_SECRET_BOOK_OF_POTION, 1);
						qs.setCond(2, true);
						qs.showQuestionMark(334);
						qs.playSound(Sound.ITEMSOUND_QUEST_ITEMGET);
					}
					break;
				}
				case DLORD_ALEXANDROSANCHES:
				{
					if (qs.isMemoState(2) && (qs.getInt(FLAG) == 3))
					{
						npc.say(NpcStringId.BONAPARTERIUS_ABYSS_KING_WILL_PUNISH_YOU);
						if (getRandom(2) == 0)
						{
							addSpawn(ABYSSKING_BONAPARTERIUS, npc, true, 0, false);
						}
						else
						{
							switch (getRandom(4))
							{
								case 0:
								{
									qs.giveItems(DEMONS_TUNIC_FABRIC, 1);
									break;
								}
								case 1:
								{
									qs.giveItems(DEMONS_HOSE_PATTERN, 1);
									break;
								}
								case 2:
								{
									qs.giveItems(DEMONS_BOOTS_FABRIC, 1);
									break;
								}
								case 3:
								{
									qs.giveItems(DEMONS_GLOVES_FABRIC, 1);
								}
							}
						}
						break;
					}
				}
				case ABYSSKING_BONAPARTERIUS:
				{
					if (qs.isMemoState(2) && (qs.getInt(FLAG) == 3))
					{
						npc.say(NpcStringId.REVENGE_IS_OVERLORD_RAMSEBALIUS_OF_THE_EVIL_WORLD);
						if (getRandom(2) == 0)
						{
							addSpawn(EVILOVERLORD_RAMSEBALIUS, npc, true, 0, false);
						}
						else
						{
							switch (getRandom(4))
							{
								case 0:
								{
									qs.giveItems(DEMONS_TUNIC_FABRIC, 1);
									break;
								}
								case 1:
								{
									qs.giveItems(DEMONS_HOSE_PATTERN, 1);
									break;
								}
								case 2:
								{
									qs.giveItems(DEMONS_BOOTS_FABRIC, 1);
									break;
								}
								case 3:
								{
									qs.giveItems(DEMONS_GLOVES_FABRIC, 1);
									break;
								}
							}
						}
						break;
					}
					break;
				}
				case EVILOVERLORD_RAMSEBALIUS:
				{
					if (qs.isMemoState(2) && (qs.getInt(FLAG) == 3))
					{
						npc.say(NpcStringId.OH_GREAT_DEMON_KING);
						if (getRandom(2) == 0)
						{
							addSpawn(GREAT_DEMON_KING, npc, true, 0, false);
						}
						else
						{
							switch (getRandom(4))
							{
								case 0:
								{
									qs.giveItems(DEMONS_TUNIC_FABRIC, 1);
									break;
								}
								case 1:
								{
									qs.giveItems(DEMONS_HOSE_PATTERN, 1);
									break;
								}
								case 2:
								{
									qs.giveItems(DEMONS_BOOTS_FABRIC, 1);
									break;
								}
								case 3:
								{
									qs.giveItems(DEMONS_GLOVES_FABRIC, 1);
									break;
								}
							}
						}
						break;
					}
					break;
				}
			}
			
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	private QuestState getRandomPlayerFromParty(L2PcInstance player, L2Npc npc)
	{
		QuestState qs = player.getQuestState(getName());
		final List<QuestState> candidates = new ArrayList<>();
		
		if ((qs != null) && qs.isStarted())
		{
			candidates.add(qs);
			candidates.add(qs);
		}
		
		if (player.isInParty())
		{
			player.getParty().getMembers().stream().forEach(pm ->
			{
				
				QuestState qss = pm.getQuestState(getName());
				if ((qss != null) && qss.isStarted() && Util.checkIfInRange(1500, npc, pm, true))
				{
					candidates.add(qss);
				}
			});
		}
		return candidates.isEmpty() ? null : candidates.get(getRandom(candidates.size()));
	}
}
