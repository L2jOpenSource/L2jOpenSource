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
package ai.npc.Teleports.NewbieGuide;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.l2jserver.gameserver.data.xml.impl.MultisellData;
import com.l2jserver.gameserver.enums.Race;
import com.l2jserver.gameserver.enums.audio.Voice;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.base.ClassId;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.network.NpcStringId;

import ai.npc.AbstractNpcAI;
import quests.Q00255_Tutorial.Q00255_Tutorial;

/**
 * Class handle all newbie guide tasks
 * @author Zealar
 * @since 2.6.0.0
 */
public final class NewbieGuide extends AbstractNpcAI
{
	// Suffix
	private static final String SUFFIX_FIGHTER_5_LEVEL = "-f05.htm";
	private static final String SUFFIX_FIGHTER_10_LEVEL = "-f10.htm";
	private static final String SUFFIX_FIGHTER_15_LEVEL = "-f15.htm";
	private static final String SUFFIX_FIGHTER_20_LEVEL = "-f20.htm";
	private static final String SUFFIX_MAGE_7_LEVEL = "-m07.htm";
	private static final String SUFFIX_MAGE_14_LEVEL = "-m14.htm";
	private static final String SUFFIX_MAGE_20_LEVEL = "-m20.htm";
	
	// Vars
	private static final int FIRST_COUPON_SIZE = 5;
	private static final int SECOND_COUPON_SIZE = 1;
	
	// Newbie helpers
	private static final int NEWBIE_GUIDE_HUMAN = 30598;
	private static final int NEWBIE_GUIDE_ELF = 30599;
	private static final int NEWBIE_GUIDE_DARK_ELF = 30600;
	private static final int NEWBIE_GUIDE_DWARF = 30601;
	private static final int NEWBIE_GUIDE_ORC = 30602;
	private static final int NEWBIE_GUIDE_KAMAEL = 32135;
	private static final int NEWBIE_GUIDE_GLUDIN = 31076;
	private static final int NEWBIE_GUIDE_GLUDIO = 31077;
	private static final int ADVENTURERS_GUIDE = 32327;
	
	private static final int GUIDE_MISSION = 41;
	
	// Item
	private static final int SOULSHOT_NO_GRADE_FOR_BEGINNERS = 5789;
	private static final int SPIRITSHOT_NO_GRADE_FOR_BEGINNERS = 5790;
	private static final int SCROLL_RECOVERY_NO_GRADE = 8594;
	
	private static final int APPRENTICE_ADVENTURERS_WEAPON_EXCHANGE_COUPON = 7832;
	private static final int ADVENTURERS_MAGIC_ACCESSORY_EXCHANGE_COUPON = 7833;
	
	// Buffs
	private static final SkillHolder WIND_WALK_FOR_BEGINNERS = new SkillHolder(4322);
	private static final SkillHolder SHIELD_FOR_BEGINNERS = new SkillHolder(4323);
	private static final SkillHolder BLESS_THE_BODY_FOR_BEGINNERS = new SkillHolder(4324);
	private static final SkillHolder VAMPIRIC_RAGE_FOR_BEGINNERS = new SkillHolder(4325);
	private static final SkillHolder REGENERATION_FOR_BEGINNERS = new SkillHolder(4326);
	private static final SkillHolder HASTE_FOR_BEGINNERS = new SkillHolder(4327);
	private static final SkillHolder BLESS_THE_SOUL_FOR_BEGINNERS = new SkillHolder(4328);
	private static final SkillHolder ACUMEN_FOR_BEGINNERS = new SkillHolder(4329);
	private static final SkillHolder CONCENTRATION_FOR_BEGINNERS = new SkillHolder(4330);
	private static final SkillHolder EMPOWER_FOR_BEGINNERS = new SkillHolder(4331);
	private static final SkillHolder LIFE_CUBIC_FOR_BEGINNERS = new SkillHolder(4338);
	private static final SkillHolder BLESSING_OF_PROTECTION = new SkillHolder(5182);
	private static final SkillHolder ADVENTURERS_HASTE = new SkillHolder(5632);
	private static final SkillHolder ADVENTURERS_MAGIC_BARRIER = new SkillHolder(5637);
	
	// Buylist
	private static final int WEAPON_MULTISELL = 305986001;
	private static final int ACCESORIES_MULTISELL = 305986002;
	
	private static final Map<Integer, List<Location>> TELEPORT_MAP = new HashMap<>();
	
	static
	{
		Location TALKING_ISLAND_VILLAGE = new Location(-84081, 243227, -3723);
		Location DARK_ELF_VILLAGE = new Location(12111, 16686, -4582);
		Location DWARVEN_VILLAGE = new Location(115632, -177996, -905);
		Location ELVEN_VILLAGE = new Location(45475, 48359, -3060);
		Location ORC_VILLAGE = new Location(-45032, -113598, -192);
		Location KAMAEL_VILLAGE = new Location(-119697, 44532, 380);
		
		TELEPORT_MAP.put(NEWBIE_GUIDE_HUMAN, Arrays.asList(DARK_ELF_VILLAGE, DWARVEN_VILLAGE, ELVEN_VILLAGE, ORC_VILLAGE, KAMAEL_VILLAGE));
		TELEPORT_MAP.put(NEWBIE_GUIDE_ELF, Arrays.asList(DARK_ELF_VILLAGE, DWARVEN_VILLAGE, TALKING_ISLAND_VILLAGE, ORC_VILLAGE, KAMAEL_VILLAGE));
		TELEPORT_MAP.put(NEWBIE_GUIDE_DARK_ELF, Arrays.asList(DWARVEN_VILLAGE, TALKING_ISLAND_VILLAGE, ELVEN_VILLAGE, ORC_VILLAGE, KAMAEL_VILLAGE));
		TELEPORT_MAP.put(NEWBIE_GUIDE_DWARF, Arrays.asList(DARK_ELF_VILLAGE, TALKING_ISLAND_VILLAGE, ELVEN_VILLAGE, ORC_VILLAGE, KAMAEL_VILLAGE));
		TELEPORT_MAP.put(NEWBIE_GUIDE_ORC, Arrays.asList(DARK_ELF_VILLAGE, DWARVEN_VILLAGE, TALKING_ISLAND_VILLAGE, ELVEN_VILLAGE, KAMAEL_VILLAGE));
		TELEPORT_MAP.put(NEWBIE_GUIDE_KAMAEL, Arrays.asList(TALKING_ISLAND_VILLAGE, DARK_ELF_VILLAGE, ELVEN_VILLAGE, DWARVEN_VILLAGE, ORC_VILLAGE));
	}
	
	private NewbieGuide()
	{
		super(NewbieGuide.class.getSimpleName(), "ai/npc/Teleports");
		
		int[] newbieList =
		{
			NEWBIE_GUIDE_HUMAN,
			NEWBIE_GUIDE_ELF,
			NEWBIE_GUIDE_DARK_ELF,
			NEWBIE_GUIDE_DWARF,
			NEWBIE_GUIDE_ORC,
			NEWBIE_GUIDE_KAMAEL,
			NEWBIE_GUIDE_GLUDIN,
			NEWBIE_GUIDE_GLUDIO,
			ADVENTURERS_GUIDE
		};
		addStartNpc(newbieList);
		addFirstTalkId(newbieList);
		addTalkId(newbieList);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState qs = player.getQuestState(Q00255_Tutorial.class.getSimpleName());
		if (qs != null)
		{
			if (npc.getId() == ADVENTURERS_GUIDE)
			{
				return "32327.htm";
			}
			if (npc.getId() == NEWBIE_GUIDE_GLUDIO || npc.getId() == NEWBIE_GUIDE_GLUDIN)
			{
				return "newbie-guide-18.htm";
			}
			return talkGuide(player, qs);
		}
		return super.onFirstTalk(npc, player);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance talker)
	{
		if (event.endsWith(".htm"))
		{
			return event;
		}
		if (event.startsWith("teleport"))
		{
			String[] tel = event.split("_");
			if (tel.length != 2)
			{
				teleportRequest(talker, npc, -1);
			}
			else
			{
				teleportRequest(talker, npc, Integer.parseInt(tel[1]));
			}
			return event;
		}
		final QuestState qs = getQuestState(talker, true);
		int ask = Integer.parseInt(event.split(";")[0]);
		int reply = Integer.parseInt(event.split(";")[1]);
		
		switch (ask)
		{
			case -7:
			{
				switch (reply)
				{
					case 1:
					{
						if (talker.getRace() == Race.KAMAEL)
						{
							if ((talker.getRace() != npc.getRace()) && (npc.getId() == NEWBIE_GUIDE_HUMAN))
							{
								showPage(talker, "30598-003.htm");
							}
							else if ((talker.getRace() != npc.getRace()) && (npc.getId() == NEWBIE_GUIDE_ELF))
							{
								showPage(talker, "30599-003.htm");
							}
							else if ((talker.getRace() != npc.getRace()) && (npc.getId() == NEWBIE_GUIDE_DARK_ELF))
							{
								showPage(talker, "30600-003.htm");
							}
							else if ((talker.getRace() != npc.getRace()) && (npc.getId() == NEWBIE_GUIDE_DWARF))
							{
								showPage(talker, "30601-003.htm");
							}
							else if ((talker.getRace() != npc.getRace()) && (npc.getId() == NEWBIE_GUIDE_ORC))
							{
								showPage(talker, "30602-003.htm");
							}
							else if ((talker.getLevel() > 20) || ((talker.getRace() != Race.KAMAEL) || (talker.getClassId().level() != 0)))
							{
								showPage(talker, "32135-002.htm");
							}
							else if (talker.getClassId() == ClassId.maleSoldier)
							{
								if (talker.getLevel() <= 5)
								{
									showPage(talker, "32135-kmf05.htm");
								}
								else if (talker.getLevel() <= 10)
								{
									showPage(talker, "32135-kmf10.htm");
								}
								else if (talker.getLevel() <= 15)
								{
									showPage(talker, "32135-kmf15.htm");
								}
								else
								{
									showPage(talker, "32135-kmf20.htm");
								}
							}
							else if (talker.getClassId() == ClassId.femaleSoldier)
							{
								if (talker.getLevel() <= 5)
								{
									showPage(talker, "32135-kff05.htm");
								}
								else if (talker.getLevel() <= 10)
								{
									showPage(talker, "32135-kff10.htm");
								}
								else if (talker.getLevel() <= 15)
								{
									showPage(talker, "32135-kff15.htm");
								}
								else
								{
									showPage(talker, "32135-kff20.htm");
								}
							}
						}
						else if ((talker.getRace() != npc.getRace()) && (npc.getId() == NEWBIE_GUIDE_HUMAN))
						{
							showPage(talker, "30598-003.htm");
						}
						else if ((talker.getRace() != npc.getRace()) && (npc.getId() == NEWBIE_GUIDE_ELF))
						{
							showPage(talker, "30599-003.htm");
						}
						else if ((talker.getRace() != npc.getRace()) && (npc.getId() == NEWBIE_GUIDE_DARK_ELF))
						{
							showPage(talker, "30600-003.htm");
						}
						else if ((talker.getRace() != npc.getRace()) && (npc.getId() == NEWBIE_GUIDE_DWARF))
						{
							showPage(talker, "30601-003.htm");
						}
						else if ((talker.getRace() != npc.getRace()) && (npc.getId() == NEWBIE_GUIDE_ORC))
						{
							showPage(talker, "30602-003.htm");
						}
						else if ((talker.getRace() != npc.getRace()) && (npc.getId() == NEWBIE_GUIDE_KAMAEL))
						{
							showPage(talker, "32135-003.htm");
						}
						else if ((talker.getLevel() > 20) || (talker.getClassId().level() != 0))
						{
							showPage(talker, "");
						}
						else if (!talker.isMageClass())
						{
							if (talker.getLevel() <= 5)
							{
								showPage(talker, npc.getId() + SUFFIX_FIGHTER_5_LEVEL);
							}
							else if (talker.getLevel() <= 10)
							{
								showPage(talker, npc.getId() + SUFFIX_FIGHTER_10_LEVEL);
							}
							else if (talker.getLevel() <= 15)
							{
								showPage(talker, npc.getId() + SUFFIX_FIGHTER_15_LEVEL);
							}
							else
							{
								showPage(talker, npc.getId() + SUFFIX_FIGHTER_20_LEVEL);
							}
						}
						else if (talker.getLevel() <= 7)
						{
							showPage(talker, npc.getId() + SUFFIX_MAGE_7_LEVEL);
						}
						else if (talker.getLevel() <= 14)
						{
							showPage(talker, npc.getId() + SUFFIX_MAGE_14_LEVEL);
						}
						else
						{
							showPage(talker, npc.getId() + SUFFIX_MAGE_20_LEVEL);
						}
						break;
					}
					case 2:
					{
						if (talker.getLevel() <= 75)
						{
							if (talker.getLevel() < 6)
							{
								showPage(talker, "buffs-low-level.htm");
							}
							else if (!talker.isMageClass() && (talker.getClassId().level() < 3))
							{
								npc.setTarget(talker);
								npc.doCast(WIND_WALK_FOR_BEGINNERS);
								npc.doCast(WIND_WALK_FOR_BEGINNERS);
								npc.doCast(SHIELD_FOR_BEGINNERS);
								npc.doCast(ADVENTURERS_MAGIC_BARRIER);
								npc.doCast(BLESS_THE_BODY_FOR_BEGINNERS);
								npc.doCast(VAMPIRIC_RAGE_FOR_BEGINNERS);
								npc.doCast(REGENERATION_FOR_BEGINNERS);
								if ((talker.getLevel() >= 6) && (talker.getLevel() <= 39))
								{
									npc.doCast(HASTE_FOR_BEGINNERS);
								}
								if ((talker.getLevel() >= 40) && (talker.getLevel() <= 75))
								{
									npc.doCast(ADVENTURERS_HASTE);
								}
								if ((talker.getLevel() >= 16) && (talker.getLevel() <= 34))
								{
									npc.doCast(LIFE_CUBIC_FOR_BEGINNERS);
								}
							}
							else if (talker.isMageClass() && (talker.getClassId().level() < 3))
							{
								npc.setTarget(talker);
								npc.doCast(WIND_WALK_FOR_BEGINNERS);
								npc.doCast(SHIELD_FOR_BEGINNERS);
								npc.doCast(ADVENTURERS_MAGIC_BARRIER);
								npc.doCast(BLESS_THE_SOUL_FOR_BEGINNERS);
								npc.doCast(ACUMEN_FOR_BEGINNERS);
								npc.doCast(CONCENTRATION_FOR_BEGINNERS);
								npc.doCast(EMPOWER_FOR_BEGINNERS);
								if ((talker.getLevel() >= 16) && (talker.getLevel() <= 34))
								{
									npc.doCast(LIFE_CUBIC_FOR_BEGINNERS);
								}
							}
						}
						else
						{
							showPage(talker, "buffs-big-level.htm");
						}
						break;
					}
					case 3:
					{
						if ((talker.getLevel() <= 39) && (talker.getClassId().level() < 3))
						{
							npc.setTarget(talker);
							npc.doCast(BLESSING_OF_PROTECTION);
						}
						else
						{
							showPage(talker, "pk-protection-002.htm");
						}
						break;
					}
					case 4:
					{
						L2Summon summon = talker.getSummon();
						if ((summon != null) && !summon.isPet())
						{
							if ((talker.getLevel() < 6) || (talker.getLevel() > 75))
							{
								showPage(talker, "buffs-big-level.htm");
							}
							else
							{
								npc.setTarget(talker);
								npc.doCast(WIND_WALK_FOR_BEGINNERS);
								npc.doCast(SHIELD_FOR_BEGINNERS);
								npc.doCast(ADVENTURERS_MAGIC_BARRIER);
								npc.doCast(BLESS_THE_BODY_FOR_BEGINNERS);
								npc.doCast(VAMPIRIC_RAGE_FOR_BEGINNERS);
								npc.doCast(REGENERATION_FOR_BEGINNERS);
								npc.doCast(BLESS_THE_SOUL_FOR_BEGINNERS);
								npc.doCast(ACUMEN_FOR_BEGINNERS);
								npc.doCast(CONCENTRATION_FOR_BEGINNERS);
								npc.doCast(EMPOWER_FOR_BEGINNERS);
								if ((talker.getLevel() >= 6) && (talker.getLevel() <= 39))
								{
									npc.doCast(HASTE_FOR_BEGINNERS);
								}
								if ((talker.getLevel() >= 40) && (talker.getLevel() <= 75))
								{
									npc.doCast(ADVENTURERS_HASTE);
								}
							}
						}
						else
						{
							showPage(talker, "buffs-no-pet.htm");
						}
						break;
					}
				}
				break;
			}
			case -1000:
			{
				switch (reply)
				{
					case 1:
					{
						if (talker.getLevel() > 5)
						{
							if ((talker.getLevel() < 20) && (talker.getClassId().level() == 0))
							{
								if (getOneTimeQuestFlag(talker, 207) == 0)
								{
									qs.giveItems(APPRENTICE_ADVENTURERS_WEAPON_EXCHANGE_COUPON, FIRST_COUPON_SIZE);
									setOneTimeQuestFlag(talker, 207, 1);
									showPage(talker, "newbie-guide-002.htm");
									qs.setNRMemoState(talker, GUIDE_MISSION, qs.getNRMemoState(talker, GUIDE_MISSION) + 100);
									showOnScreenMsg(talker, NpcStringId.ACQUISITION_OF_WEAPON_EXCHANGE_COUPON_FOR_BEGINNERS_COMPLETE_N_GO_SPEAK_WITH_THE_NEWBIE_GUIDE, 2, 5000, "");
								}
								else
								{
									showPage(talker, "newbie-guide-004.htm");
								}
							}
							else
							{
								showPage(talker, "newbie-guide-003.htm");
							}
						}
						else
						{
							showPage(talker, "newbie-guide-003.htm");
						}
						break;
					}
					case 2:
					{
						if (talker.getClassId().level() == 1)
						{
							if (talker.getLevel() < 40)
							{
								if (getOneTimeQuestFlag(talker, 208) == 0)
								{
									qs.giveItems(ADVENTURERS_MAGIC_ACCESSORY_EXCHANGE_COUPON, SECOND_COUPON_SIZE);
									setOneTimeQuestFlag(talker, 208, 1);
									showPage(talker, "newbie-guide-011.htm");
								}
								else
								{
									showPage(talker, "newbie-guide-013.htm");
								}
							}
							else
							{
								showPage(talker, "newbie-guide-012.htm");
							}
						}
						else
						{
							showPage(talker, "newbie-guide-012.htm");
						}
						break;
					}
				}
				break;
				
			}
			case -303:
			{
				switch (reply)
				{
					case 528:
						if (talker.getLevel() > 5)
						{
							if ((talker.getLevel() < 20) && (talker.getClassId().level() == 0))
							{
								MultisellData.getInstance().separateAndSend(WEAPON_MULTISELL, talker, npc, false);
							}
							else
							{
								showPage(talker, "newbie-guide-005.htm");
							}
						}
						else
						{
							showPage(talker, "newbie-guide-005.htm");
						}
						break;
					case 529:
						if (talker.getLevel() > 5)
						{
							if ((talker.getLevel() < 40) && (talker.getClassId().level() == 1))
							{
								MultisellData.getInstance().separateAndSend(ACCESORIES_MULTISELL, talker, npc, false);
							}
							else
							{
								showPage(talker, "newbie-guide-014.htm");
							}
						}
						else
						{
							showPage(talker, "newbie-guide-014.htm");
						}
						break;
				}
				break;
			}
		}
		switch (npc.getId())
		{
			case NEWBIE_GUIDE_HUMAN:
			{
				String ansGuideHumanCnacelot = eventGuideHumanCnacelot(reply, qs);
				if (!ansGuideHumanCnacelot.isEmpty())
				{
					return ansGuideHumanCnacelot;
				}
				break;
			}
			case NEWBIE_GUIDE_ELF:
			{
				String ansGuideElfRoios = eventGuideElfRoios(reply, qs);
				if (!ansGuideElfRoios.isEmpty())
				{
					return ansGuideElfRoios;
				}
				break;
			}
			case NEWBIE_GUIDE_DARK_ELF:
			{
				String ansGuideDelfFrankia = eventGuideDelfFrankia(reply, qs);
				if (!ansGuideDelfFrankia.isEmpty())
				{
					return ansGuideDelfFrankia;
				}
				break;
			}
			case NEWBIE_GUIDE_DWARF:
			{
				String ansGuideDwarfGullin = eventGuideDwarfGullin(reply, qs);
				if (!ansGuideDwarfGullin.isEmpty())
				{
					return ansGuideDwarfGullin;
				}
				break;
			}
			case NEWBIE_GUIDE_ORC:
			{
				String ansGuideOrcTanai = eventGuideOrcTanai(reply, qs);
				if (!ansGuideOrcTanai.isEmpty())
				{
					return ansGuideOrcTanai;
				}
				break;
			}
			case NEWBIE_GUIDE_KAMAEL:
			{
				String ansGuideKrenisk = eventGuideKrenisk(reply, qs);
				if (!ansGuideKrenisk.isEmpty())
				{
					return ansGuideKrenisk;
				}
				break;
			}
		}
		return "";
	}
	
	private void teleportRequest(L2PcInstance talker, L2Npc npc, int teleportId)
	{
		if (talker.getLevel() >= 20)
		{
			showPage(talker, "teleport-big-level.htm");
		}
		else if ((talker.getTransformationId() == 111) || (talker.getTransformationId() == 112) || (talker.getTransformationId() == 124))
		{
			showPage(talker, "frog-teleport.htm");
		}
		else
		{
			if ((teleportId < 0) || (teleportId > 5))
			{
				showPage(talker, npc.getId() + "-teleport.htm");
			}
			else
			{
				if (TELEPORT_MAP.containsKey(npc.getId()))
				{
					if (TELEPORT_MAP.get(npc.getId()).size() > teleportId)
					{
						talker.teleToLocation(TELEPORT_MAP.get(npc.getId()).get(teleportId), false);
					}
				}
			}
		}
	}
	
	private String talkGuide(L2PcInstance talker, QuestState tutorialQS)
	{
		final QuestState qs = getQuestState(talker, true);
		if ((tutorialQS.getMemoStateEx(1) < 5) && (getOneTimeQuestFlag(talker, GUIDE_MISSION) == 0))
		{
			if (!talker.isMageClass())
			{
				qs.playSound(Voice.TUTORIAL_VOICE_026_1000);
				qs.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 200);
				qs.giveItems(SCROLL_RECOVERY_NO_GRADE, 2);
				tutorialQS.setMemoStateEx(1, 5);
				if (talker.getLevel() <= 1)
				{
					qs.addExpAndSp(68, 50);
				}
				else
				{
					qs.addExpAndSp(0, 50);
				}
			}
			if (talker.isMageClass())
			{
				if (talker.getClassId() == ClassId.orcMage)
				{
					qs.playSound(Voice.TUTORIAL_VOICE_026_1000);
					qs.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 200);
				}
				else
				{
					qs.playSound(Voice.TUTORIAL_VOICE_027_1000);
					qs.giveItems(SPIRITSHOT_NO_GRADE_FOR_BEGINNERS, 100);
				}
				qs.giveItems(SCROLL_RECOVERY_NO_GRADE, 2);
				tutorialQS.setMemoStateEx(1, 5);
				if (talker.getLevel() <= 1)
				{
					qs.addExpAndSp(68, 50);
				}
				else
				{
					qs.addExpAndSp(0, 50);
				}
			}
			if (talker.getLevel() < 6)
			{
				if ((qs.getNRMemoState(talker, GUIDE_MISSION) % 10) == 1)
				{
					if (talker.getLevel() >= 5)
					{
						qs.giveAdena(695, true);
						qs.addExpAndSp(3154, 127);
					}
					else if (talker.getLevel() >= 4)
					{
						qs.giveAdena(1041, true);
						qs.addExpAndSp(4870, 195);
					}
					else if (talker.getLevel() >= 3)
					{
						qs.giveAdena(1240, true);
						qs.addExpAndSp(5970, 239);
					}
					if (!qs.haveNRMemo(talker, GUIDE_MISSION))
					{
						qs.setNRMemo(talker, GUIDE_MISSION);
						qs.setNRMemoState(talker, GUIDE_MISSION, 10);
					}
					else
					{
						qs.setNRMemoState(talker, GUIDE_MISSION, qs.getNRMemoState(talker, GUIDE_MISSION) + 10);
					}
					return "newbie-guide-02.htm";
				}
				switch (talker.getRace())
				{
					case HUMAN:
						qs.addRadar(-84436, 242793, -3729);
						return "newbie-guide-01a.htm";
					case ELF:
						qs.addRadar(42978, 49115, 2994);
						return "newbie-guide-01b.htm";
					case DARK_ELF:
						qs.addRadar(25790, 10844, -3727);
						return "newbie-guide-01c.htm";
					case ORC:
						qs.addRadar(-47360, -113791, -237);
						return "newbie-guide-01d.htm";
					case DWARF:
						qs.addRadar(112656, -174864, -611);
						return "newbie-guide-01e.htm";
					case KAMAEL:
						qs.addRadar(-119378, 49242, 22);
						return "newbie-guide-01f.htm";
				}
				if (!qs.haveNRMemo(talker, GUIDE_MISSION))
				{
					qs.setNRMemo(talker, GUIDE_MISSION);
					qs.setNRMemoState(talker, GUIDE_MISSION, 0);
				}
			}
			else if (talker.getLevel() < 10)
			{
				if ((((qs.getNRMemoState(talker, GUIDE_MISSION) % 1000) / 100) == 1) && (((qs.getNRMemoState(talker, GUIDE_MISSION) % 10000) / 100) == 1))
				{
					switch (talker.getRace())
					{
						case HUMAN:
							if (!talker.isMageClass())
							{
								qs.addRadar(-71384, 258304, -3109);
								return "newbie-guide-05a.htm";
							}
							qs.addRadar(-91008, 248016, -3568);
							return "newbie-guide-05b.htm";
						case ELF:
							qs.addRadar(47595, 51569, -2996);
							return "newbie-guide-05c.htm";
						case DARK_ELF:
							if (!talker.isMageClass())
							{
								qs.addRadar(10580, 17574, -4554);
								return "newbie-guide-05d.htm";
							}
							qs.addRadar(10775, 14190, -4242);
							return "newbie-guide-05e.htm";
						case ORC:
							qs.addRadar(46808, -113184, -112);
							return "newbie-guide-05f.htm";
						case DWARF:
							qs.addRadar(115717, -183488, -1483);
							return "newbie-guide-05g.htm";
						case KAMAEL:
							qs.addRadar(115717, -183488, -1483);
							return "newbie-guide-05h.htm";
					}
					if (talker.getLevel() >= 9)
					{
						qs.giveAdena(5563, true);
						qs.addExpAndSp(16851, 711);
					}
					else if (talker.getLevel() >= 8)
					{
						qs.giveAdena(9290, true);
						qs.addExpAndSp(28806, 1207);
					}
					else if (talker.getLevel() >= 7)
					{
						qs.giveAdena(11567, true);
						qs.addExpAndSp(36942, 1541);
					}
					else
					{
						qs.giveAdena(12928, true);
						qs.addExpAndSp(42191, 1753);
					}
					if (!qs.haveNRMemo(talker, GUIDE_MISSION))
					{
						qs.setNRMemo(talker, GUIDE_MISSION);
						qs.setNRMemoState(talker, GUIDE_MISSION, 10000);
					}
					else
					{
						qs.setNRMemoState(talker, GUIDE_MISSION, qs.getNRMemoState(talker, GUIDE_MISSION) + 10000);
					}
				}
				else if ((((qs.getNRMemoState(talker, GUIDE_MISSION) % 1000) / 100) == 1) && (((qs.getNRMemoState(talker, GUIDE_MISSION) % 10000) / 100) != 1))
				{
					switch (talker.getRace())
					{
						case HUMAN:
							qs.addRadar(-82236, 241573, -3728);
							return "newbie-guide-04a.htm";
						case ELF:
							qs.addRadar(42812, 51138, -2996);
							return "newbie-guide-04b.htm";
						case DARK_ELF:
							qs.addRadar(7644, 18048, -4377);
							return "newbie-guide-04c.htm";
						case ORC:
							qs.addRadar(-46802, -114011, -112);
							return "newbie-guide-04d.htm";
						case DWARF:
							qs.addRadar(116103, -178407, -948);
							return "newbie-guide-04e.htm";
						case KAMAEL:
							qs.addRadar(-119378, 49242, 22);
							return "newbie-guide-04f.htm";
					}
					if (!qs.haveNRMemo(talker, GUIDE_MISSION))
					{
						qs.setNRMemo(talker, GUIDE_MISSION);
						qs.setNRMemoState(talker, GUIDE_MISSION, 0);
					}
				}
				else
				{
					if (!qs.haveNRMemo(talker, GUIDE_MISSION))
					{
						qs.setNRMemo(talker, GUIDE_MISSION);
						qs.setNRMemoState(talker, GUIDE_MISSION, 0);
					}
					return "newbie-guide-03.htm";
				}
			}
			else
			{
				setOneTimeQuestFlag(talker, GUIDE_MISSION, 1);
				if (!qs.haveNRMemo(talker, GUIDE_MISSION))
				{
					qs.setNRMemo(talker, GUIDE_MISSION);
					qs.setNRMemoState(talker, GUIDE_MISSION, 0);
				}
				return "newbie-guide-06.htm";
			}
		}
		else if ((tutorialQS.getMemoStateEx(1) >= 5) && (getOneTimeQuestFlag(talker, GUIDE_MISSION) == 0))
		{
			if (talker.getLevel() < 6)
			{
				if ((qs.getNRMemoState(talker, GUIDE_MISSION) % 10) == 1)
				{
					if (talker.getLevel() >= 5)
					{
						qs.giveAdena(695, true);
						qs.addExpAndSp(3154, 127);
					}
					else if (talker.getLevel() >= 4)
					{
						qs.giveAdena(1041, true);
						qs.addExpAndSp(4870, 195);
					}
					else if (talker.getLevel() >= 3)
					{
						qs.giveAdena(1186, true);
						qs.addExpAndSp(5675, 227);
					}
					else
					{
						qs.giveAdena(1240, true);
						qs.addExpAndSp(5970, 239);
					}
					if (!qs.haveNRMemo(talker, GUIDE_MISSION))
					{
						qs.setNRMemo(talker, GUIDE_MISSION);
						qs.setNRMemoState(talker, GUIDE_MISSION, 10);
					}
					else
					{
						qs.setNRMemoState(talker, GUIDE_MISSION, qs.getNRMemoState(talker, GUIDE_MISSION) + 10);
					}
					return "newbie-guide-08.htm";
				}
				switch (talker.getRace())
				{
					case HUMAN:
						qs.addRadar(-84436, 242793, -3729);
						return "newbie-guide-07a.htm";
					case ELF:
						qs.addRadar(42978, 49115, 2994);
						return "newbie-guide-07b.htm";
					case DARK_ELF:
						qs.addRadar(25790, 10844, -3727);
						return "newbie-guide-07c.htm";
					case ORC:
						qs.addRadar(-47360, -113791, -237);
						return "newbie-guide-07d.htm";
					case DWARF:
						qs.addRadar(112656, -174864, -611);
						return "newbie-guide-07e.htm";
					case KAMAEL:
						qs.addRadar(-119378, 49242, 22);
						return "newbie-guide-07f.htm";
				}
				if (!qs.haveNRMemo(talker, GUIDE_MISSION))
				{
					qs.setNRMemo(talker, GUIDE_MISSION);
					qs.setNRMemoState(talker, GUIDE_MISSION, 0);
				}
			}
			else if (talker.getLevel() < 10)
			{
				if (((qs.getNRMemoState(talker, GUIDE_MISSION) % 100000) / 10000) == 1)
				{
					return "newbie-guide-09g.htm";
				}
				else if ((((qs.getNRMemoState(talker, GUIDE_MISSION) % 1000) / 100) == 1) && (((qs.getNRMemoState(talker, GUIDE_MISSION) % 10000) / 1000) == 1) && (((qs.getNRMemoState(talker, GUIDE_MISSION) % 100000) / 10000) != 1))
				{
					switch (talker.getRace())
					{
						case HUMAN:
							if (!talker.isMageClass())
							{
								qs.addRadar(-71384, 258304, -3109);
								return "newbie-guide-10a.htm";
							}
							qs.addRadar(-91008, 248016, -3568);
							return "newbie-guide-10b.htm";
						case ELF:
							qs.addRadar(47595, 51569, -2996);
							return "newbie-guide-10c.htm";
						case DARK_ELF:
							if (!talker.isMageClass())
							{
								qs.addRadar(10580, 17574, -4554);
								return "newbie-guide-10d.htm";
							}
							qs.addRadar(10775, 14190, -4242);
							return "newbie-guide-10e.htm";
						case ORC:
							qs.addRadar(-46808, -113184, -112);
							return "newbie-guide-10f.htm";
						case DWARF:
							qs.addRadar(115717, -183488, -1483);
							return "newbie-guide-10g.htm";
						case KAMAEL:
							qs.addRadar(-118080, 42835, 720);
							return "newbie-guide-10h.htm";
						
					}
					if (talker.getLevel() >= 9)
					{
						qs.giveAdena(5563, true);
						qs.addExpAndSp(16851, 711);
					}
					else if (talker.getLevel() >= 8)
					{
						qs.giveAdena(9290, true);
						qs.addExpAndSp(28806, 1207);
					}
					else if (talker.getLevel() >= 7)
					{
						qs.giveAdena(11567, true);
						qs.addExpAndSp(36942, 1541);
					}
					else
					{
						qs.giveAdena(12928, true);
						qs.addExpAndSp(42191, 1753);
					}
					if (!qs.haveNRMemo(talker, GUIDE_MISSION))
					{
						qs.setNRMemo(talker, GUIDE_MISSION);
						qs.setNRMemoState(talker, GUIDE_MISSION, 10000);
					}
					else
					{
						qs.setNRMemoState(talker, GUIDE_MISSION, qs.getNRMemoState(talker, GUIDE_MISSION) + 10000);
					}
				}
				else if ((((qs.getNRMemoState(talker, GUIDE_MISSION) % 1000) / 100) == 1) && (((qs.getNRMemoState(talker, GUIDE_MISSION) % 10000) / 1000) != 1))
				{
					switch (talker.getRace())
					{
						case HUMAN:
							qs.addRadar(-82236, 241573, -3728);
							return "newbie-guide-09a.htm";
						case ELF:
							qs.addRadar(42812, 51138, -2996);
							return "newbie-guide-09b.htm";
						case DARK_ELF:
							qs.addRadar(7644, 18048, -4377);
							return "newbie-guide-09c.htm";
						case ORC:
							qs.addRadar(-46802, -114011, -112);
							return "newbie-guide-09d.htm";
						case DWARF:
							qs.addRadar(116103, -178407, -948);
							return "newbie-guide-09e.htm";
						case KAMAEL:
							qs.addRadar(-119378, 49242, 22);
							return "newbie-guide-09f.htm";
					}
					if (!qs.haveNRMemo(talker, GUIDE_MISSION))
					{
						qs.setNRMemo(talker, GUIDE_MISSION);
						qs.setNRMemoState(talker, GUIDE_MISSION, 0);
					}
				}
				else
				{
					if (!qs.haveNRMemo(talker, GUIDE_MISSION))
					{
						qs.setNRMemo(talker, GUIDE_MISSION);
						qs.setNRMemoState(talker, GUIDE_MISSION, 0);
					}
					return "newbie-guide-08.htm";
				}
			}
			else if (talker.getLevel() < 15)
			{
				if ((((qs.getNRMemoState(talker, GUIDE_MISSION) % 1000000) / 100000) == 1) && (((qs.getNRMemoState(talker, GUIDE_MISSION) % 10000000) / 1000000) == 1))
				{
					return "newbie-guide-15.htm";
				}
				else if ((((qs.getNRMemoState(talker, GUIDE_MISSION) % 1000000) / 100000) == 1) && (((qs.getNRMemoState(talker, GUIDE_MISSION) % 10000000) / 1000000) != 1))
				{
					switch (talker.getRace())
					{
						case HUMAN:
							qs.addRadar(-84057, 242832, -3729);
							return "newbie-guide-11a.htm";
						case ELF:
							qs.addRadar(45859, 50827, -3058);
							return "newbie-guide-11b.htm";
						case DARK_ELF:
							qs.addRadar(11258, 14431, -4242);
							return "newbie-guide-11c.htm";
						case ORC:
							qs.addRadar(-45863, -112621, -200);
							return "newbie-guide-11d.htm";
						case DWARF:
							qs.addRadar(116268, -177524, -914);
							return "newbie-guide-11e.htm";
						case KAMAEL:
							qs.addRadar(-125872, 38208, 1251);
							return "newbie-guide-11f.htm";
					}
					if (talker.getLevel() >= 14)
					{
						qs.giveAdena(13002, true);
						qs.addExpAndSp(62876, 2891);
					}
					else if (talker.getLevel() >= 13)
					{
						qs.giveAdena(23468, true);
						qs.addExpAndSp(113137, 5161);
					}
					else if (talker.getLevel() >= 12)
					{
						qs.giveAdena(31752, true);
						qs.addExpAndSp(152653, 6914);
					}
					else if (talker.getLevel() >= 11)
					{
						qs.giveAdena(38180, true);
						qs.addExpAndSp(183128, 8242);
					}
					else
					{
						qs.giveAdena(43054, true);
						qs.addExpAndSp(206101, 9227);
					}
					if (!qs.haveNRMemo(talker, GUIDE_MISSION))
					{
						qs.setNRMemo(talker, GUIDE_MISSION);
						qs.setNRMemoState(talker, GUIDE_MISSION, 1000000);
					}
					else
					{
						qs.setNRMemoState(talker, GUIDE_MISSION, qs.getNRMemoState(talker, GUIDE_MISSION) + 1000000);
					}
				}
				else if (((qs.getNRMemoState(talker, GUIDE_MISSION) % 1000000) / 100000) != 1)
				{
					switch (talker.getRace())
					{
						case HUMAN:
							if (!talker.isMageClass())
							{
								qs.addRadar(-71384, 258304, -3109);
								return "newbie-guide-10a.htm";
							}
							qs.addRadar(-91008, 248016, -3568);
							return "newbie-guide-10b.htm";
						case ELF:
							qs.addRadar(47595, 51569, -2996);
							return "newbie-guide-10c.htm";
						case DARK_ELF:
							if (!talker.isMageClass())
							{
								qs.addRadar(10580, 17574, -4554);
								return "newbie-guide-10d.htm";
							}
							qs.addRadar(10775, 14190, -4242);
							return "newbie-guide-10e.htm";
						case ORC:
							qs.addRadar(-46808, -113184, -112);
							return "newbie-guide-10f.htm";
						case DWARF:
							qs.addRadar(115717, -183488, -1483);
							return "newbie-guide-10g.htm";
						case KAMAEL:
							qs.addRadar(-118080, 42835, 720);
							return "newbie-guide-10h.htm";
					}
					if (!qs.haveNRMemo(talker, GUIDE_MISSION))
					{
						qs.setNRMemo(talker, GUIDE_MISSION);
						qs.setNRMemoState(talker, GUIDE_MISSION, 0);
					}
				}
			}
			else if (talker.getLevel() < 18)
			{
				if ((((qs.getNRMemoState(talker, GUIDE_MISSION) % 100000000) / 10000000) == 1) && (((qs.getNRMemoState(talker, GUIDE_MISSION) % 1000000000) / 100000000) == 1))
				{
					setOneTimeQuestFlag(talker, GUIDE_MISSION, 1);
					return "newbie-guide-13.htm";
				}
				else if ((((qs.getNRMemoState(talker, GUIDE_MISSION) % 100000000) / 10000000) == 1) && (((qs.getNRMemoState(talker, GUIDE_MISSION) % 1000000000) / 100000000) != 1))
				{
					if (talker.getLevel() >= 17)
					{
						qs.giveAdena(22996, true);
						qs.addExpAndSp(113712, 5518);
					}
					else if (talker.getLevel() >= 16)
					{
						qs.giveAdena(10018, true);
						qs.addExpAndSp(208133, 42237);
					}
					else
					{
						qs.giveAdena(13648, true);
						qs.addExpAndSp(285670, 58155);
					}
					if (!qs.haveNRMemo(talker, GUIDE_MISSION))
					{
						qs.setNRMemo(talker, GUIDE_MISSION);
						qs.setNRMemoState(talker, GUIDE_MISSION, 100000000);
					}
					else
					{
						qs.setNRMemoState(talker, GUIDE_MISSION, qs.getNRMemoState(talker, GUIDE_MISSION) + 100000000);
					}
					setOneTimeQuestFlag(talker, GUIDE_MISSION, 1);
					return "newbie-guide-12.htm";
				}
				else if (((qs.getNRMemoState(talker, GUIDE_MISSION) % 100000000) / 10000000) != 1)
				{
					switch (talker.getRace())
					{
						case HUMAN:
							qs.addRadar(-84057, 242832, -3729);
							return "newbie-guide-11a.htm";
						case ELF:
						{
							qs.addRadar(45859, 50827, -3058);
							return "newbie-guide-11b.htm";
						}
						case DARK_ELF:
						{
							qs.addRadar(11258, 14431, -4242);
							return "newbie-guide-11c.htm";
						}
						case ORC:
						{
							qs.addRadar(-45863, -112621, -200);
							return "newbie-guide-11d.htm";
						}
						case DWARF:
						{
							qs.addRadar(116268, -177524, -914);
							return "newbie-guide-11e.htm";
						}
						case KAMAEL:
						{
							qs.addRadar(-125872, 38208, 1251);
							return "newbie-guide-11f.htm";
						}
					}
				}
				if (!qs.haveNRMemo(talker, GUIDE_MISSION))
				{
					qs.setNRMemo(talker, GUIDE_MISSION);
					qs.setNRMemoState(talker, GUIDE_MISSION, 0);
				}
			}
			else if (talker.getClassId().level() == 1)
			{
				setOneTimeQuestFlag(talker, GUIDE_MISSION, 1);
				if (!qs.haveNRMemo(talker, GUIDE_MISSION))
				{
					qs.setNRMemo(talker, GUIDE_MISSION);
					qs.setNRMemoState(talker, GUIDE_MISSION, 0);
				}
				return "newbie-guide-13.htm";
			}
			else
			{
				setOneTimeQuestFlag(talker, GUIDE_MISSION, 1);
				if (!qs.haveNRMemo(talker, GUIDE_MISSION))
				{
					qs.setNRMemo(talker, GUIDE_MISSION);
					qs.setNRMemoState(talker, GUIDE_MISSION, 0);
				}
				return "newbie-guide-14.htm";
			}
		}
		return "";
	}
	
	private String eventGuideHumanCnacelot(int event, QuestState qs)
	{
		switch (event)
		{
			case 10:
				return "30598-04.htm";
			case 11:
				return "30598-04a.htm";
			case 12:
				return "30598-04b.htm";
			case 13:
				return "30598-04c.htm";
			case 14:
				return "30598-04d.htm";
			case 15:
				return "30598-04e.htm";
			case 16:
				return "30598-04f.htm";
			case 17:
				return "30598-04g.htm";
			case 18:
				return "30598-04h.htm";
			case 19:
				return "30598-04i.htm";
			case 31:
				qs.clearRadar();
				qs.addRadar(-84108, 244604, -3729);
				return "30598-05.htm";
			case 32:
				qs.clearRadar();
				qs.addRadar(-82236, 241573, -3728);
				return "30598-05.htm";
			case 33:
				qs.clearRadar();
				qs.addRadar(-82515, 241221, -3728);
				return "30598-05.htm";
			case 34:
				qs.clearRadar();
				qs.addRadar(-82319, 244709, -3727);
				return "30598-05.htm";
			case 35:
				qs.clearRadar();
				qs.addRadar(-82659, 244992, -3717);
				return "30598-05.htm";
			case 36:
				qs.clearRadar();
				qs.addRadar(-86114, 244682, -3727);
				return "30598-05.htm";
			case 37:
				qs.clearRadar();
				qs.addRadar(-86328, 244448, -3724);
				return "30598-05.htm";
			case 38:
				qs.clearRadar();
				qs.addRadar(-86322, 241215, -3727);
				return "30598-05.htm";
			case 39:
				qs.clearRadar();
				qs.addRadar(-85964, 240947, -3727);
				return "30598-05.htm";
			case 40:
				qs.clearRadar();
				qs.addRadar(-85026, 242689, -3729);
				return "30598-05.htm";
			case 41:
				qs.clearRadar();
				qs.addRadar(-83789, 240799, -3717);
				return "30598-05.htm";
			case 42:
			{
				qs.clearRadar();
				qs.addRadar(-84204, 240403, -3717);
				return "30598-05.htm";
			}
			case 43:
			{
				qs.clearRadar();
				qs.addRadar(-86385, 243267, -3717);
				return "30598-05.htm";
			}
			case 44:
			{
				qs.clearRadar();
				qs.addRadar(-86733, 242918, -3717);
				return "30598-05.htm";
			}
			case 45:
			{
				qs.clearRadar();
				qs.addRadar(-84516, 245449, -3714);
				return "30598-05.htm";
			}
			case 46:
			{
				qs.clearRadar();
				qs.addRadar(-84729, 245001, -3726);
				return "30598-05.htm";
			}
			case 47:
			{
				qs.clearRadar();
				qs.addRadar(-84965, 245222, -3726);
				return "30598-05.htm";
			}
			case 48:
			{
				qs.clearRadar();
				qs.addRadar(-84981, 244764, -3726);
				return "30598-05.htm";
			}
			case 49:
			{
				qs.clearRadar();
				qs.addRadar(-85186, 245001, -3726);
				return "30598-05.htm";
			}
			case 50:
			{
				qs.clearRadar();
				qs.addRadar(-83326, 242964, -3718);
				return "30598-05.htm";
			}
			case 51:
			{
				qs.clearRadar();
				qs.addRadar(-83020, 242553, -3718);
				return "30598-05.htm";
			}
			case 52:
			{
				qs.clearRadar();
				qs.addRadar(-83175, 243065, -3718);
				return "30598-05.htm";
			}
			case 53:
			{
				qs.clearRadar();
				qs.addRadar(-82809, 242751, -3718);
				return "30598-05.htm";
			}
			case 54:
			{
				qs.clearRadar();
				qs.addRadar(-81895, 243917, -3721);
				return "30598-05.htm";
			}
			case 55:
			{
				qs.clearRadar();
				qs.addRadar(-81840, 243534, -3721);
				return "30598-05.htm";
			}
			case 56:
			{
				qs.clearRadar();
				qs.addRadar(-81512, 243424, -3720);
				return "30598-05.htm";
			}
			case 57:
			{
				qs.clearRadar();
				qs.addRadar(-84436, 242793, -3729);
				return "30598-05.htm";
			}
			case 58:
			{
				qs.clearRadar();
				qs.addRadar(-78939, 240305, -3443);
				return "30598-05.htm";
			}
			case 59:
			{
				qs.clearRadar();
				qs.addRadar(-85301, 244587, -3725);
				return "30598-05.htm";
			}
			case 60:
			{
				qs.clearRadar();
				qs.addRadar(-83163, 243560, -3728);
				return "30598-05.htm";
			}
			case 61:
			{
				qs.clearRadar();
				qs.addRadar(-97131, 258946, -3622);
				return "30598-05.htm";
			}
			case 62:
			{
				qs.clearRadar();
				qs.addRadar(-114685, 222291, -2925);
				return "30598-05.htm";
			}
			case 63:
			{
				qs.clearRadar();
				qs.addRadar(-84057, 242832, -3729);
				return "30598-05.htm";
			}
			case 64:
			{
				qs.clearRadar();
				qs.addRadar(-100332, 238019, -3573);
				return "30598-05.htm";
			}
			case 65:
			{
				qs.clearRadar();
				qs.addRadar(-82041, 242718, -3725);
				return "30598-05.htm";
			}
		}
		return "";
	}
	
	private String eventGuideElfRoios(int event, QuestState qs)
	{
		switch (event)
		{
			case 10:
				return "30599-04.htm";
			case 11:
				return "30599-04a.htm";
			case 12:
				return "30599-04b.htm";
			case 13:
				return "30599-04c.htm";
			case 14:
				return "30599-04d.htm";
			case 15:
				return "30599-04e.htm";
			case 16:
				return "30599-04f.htm";
			case 17:
				return "30599-04g.htm";
			case 18:
				return "30599-04h.htm";
			case 31:
				qs.clearRadar();
				qs.addRadar(46926, 51511, -2977);
				return "30599-05.htm";
			case 32:
				qs.clearRadar();
				qs.addRadar(44995, 51706, -2803);
				return "30599-05.htm";
			case 33:
				qs.clearRadar();
				qs.addRadar(45727, 51721, -2803);
				return "30599-05.htm";
			case 34:
				qs.clearRadar();
				qs.addRadar(42812, 51138, -2996);
				return "30599-05.htm";
			case 35:
				qs.clearRadar();
				qs.addRadar(45487, 46511, -2996);
				return "30599-05.htm";
			case 36:
				qs.clearRadar();
				qs.addRadar(47401, 51764, -2996);
				return "30599-05.htm";
			case 37:
				qs.clearRadar();
				qs.addRadar(42971, 51372, -2996);
				return "30599-05.htm";
			case 38:
				qs.clearRadar();
				qs.addRadar(47595, 51569, -2996);
				return "30599-05.htm";
			case 39:
				qs.clearRadar();
				qs.addRadar(45778, 46534, -2996);
				return "30599-05.htm";
			case 40:
				qs.clearRadar();
				qs.addRadar(44476, 47153, -2984);
				return "30599-05.htm";
			case 41:
				qs.clearRadar();
				qs.addRadar(42700, 50057, -2984);
				return "30599-05.htm";
			case 42:
				qs.clearRadar();
				qs.addRadar(42766, 50037, -2984);
				return "30599-05.htm";
			case 43:
				qs.clearRadar();
				qs.addRadar(44683, 46952, -2981);
				return "30599-05.htm";
			case 44:
				qs.clearRadar();
				qs.addRadar(44667, 46896, -2982);
				return "30599-05.htm";
			case 45:
				qs.clearRadar();
				qs.addRadar(45725, 52105, -2795);
				return "30599-05.htm";
			case 46:
				qs.clearRadar();
				qs.addRadar(44823, 52414, -2795);
				return "30599-05.htm";
			case 47:
				qs.clearRadar();
				qs.addRadar(45000, 52101, -2795);
				return "30599-05.htm";
			case 48:
				qs.clearRadar();
				qs.addRadar(45919, 52414, -2795);
				return "30599-05.htm";
			case 49:
				qs.clearRadar();
				qs.addRadar(44692, 52261, -2795);
				return "30599-05.htm";
			case 50:
				qs.clearRadar();
				qs.addRadar(47780, 49568, -2983);
				return "30599-05.htm";
			case 51:
				qs.clearRadar();
				qs.addRadar(47912, 50170, -2983);
				return "30599-05.htm";
			case 52:
				qs.clearRadar();
				qs.addRadar(47868, 50167, -2983);
				return "30599-05.htm";
			case 53:
				qs.clearRadar();
				qs.addRadar(28928, 74248, -3773);
				return "30599-05.htm";
			case 54:
				qs.clearRadar();
				qs.addRadar(43673, 49683, -3046);
				return "30599-05.htm";
			case 55:
				qs.clearRadar();
				qs.addRadar(45610, 49008, -3059);
				return "30599-05.htm";
			case 56:
				qs.clearRadar();
				qs.addRadar(50592, 54986, -3376);
				return "30599-05.htm";
			case 57:
				qs.clearRadar();
				qs.addRadar(42978, 49115, -2994);
				return "30599-05.htm";
			case 58:
				qs.clearRadar();
				qs.addRadar(46475, 50495, -3058);
				return "30599-05.htm";
			case 59:
				qs.clearRadar();
				qs.addRadar(45859, 50827, -3058);
				return "30599-05.htm";
			case 60:
				qs.clearRadar();
				qs.addRadar(51210, 82474, -3283);
				return "30599-05.htm";
			case 61:
				qs.clearRadar();
				qs.addRadar(49262, 53607, -3216);
				return "30599-05.htm";
		}
		return "";
	}
	
	private String eventGuideDelfFrankia(int event, QuestState qs)
	{
		switch (event)
		{
			case 10:
				return "30600-04.htm";
			case 11:
				return "30600-04a.htm";
			case 12:
				return "30600-04b.htm";
			case 13:
				return "30600-04c.htm";
			case 14:
				return "30600-04d.htm";
			case 15:
				return "30600-04e.htm";
			case 16:
				return "30600-04f.htm";
			case 17:
				return "30600-04g.htm";
			case 18:
				return "30600-04h.htm";
			case 31:
				qs.clearRadar();
				qs.addRadar(9670, 15537, -4574);
				return "30600-05.htm";
			case 32:
				qs.clearRadar();
				qs.addRadar(15120, 15656, -4376);
				return "30600-05.htm";
			case 33:
				qs.clearRadar();
				qs.addRadar(17306, 13592, -3724);
				return "30600-05.htm";
			case 34:
				qs.clearRadar();
				qs.addRadar(15272, 16310, -4377);
				return "30600-05.htm";
			case 35:
				qs.clearRadar();
				qs.addRadar(6449, 19619, -3694);
				return "30600-05.htm";
			case 36:
				qs.clearRadar();
				qs.addRadar(-15404, 71131, -3445);
				return "30600-05.htm";
			case 37:
				qs.clearRadar();
				qs.addRadar(7496, 17388, -4377);
				return "30600-05.htm";
			case 38:
				qs.clearRadar();
				qs.addRadar(17102, 13002, -3743);
				return "30600-05.htm";
			case 39:
				qs.clearRadar();
				qs.addRadar(6532, 19903, -3693);
				return "30600-05.htm";
			case 40:
				qs.clearRadar();
				qs.addRadar(-15648, 71405, -3451);
				return "30600-05.htm";
			case 41:
				qs.clearRadar();
				qs.addRadar(7644, 18048, -4377);
				return "30600-05.htm";
			case 42:
				qs.clearRadar();
				qs.addRadar(-1301, 75883, -3566);
				return "30600-05.htm";
			case 43:
				qs.clearRadar();
				qs.addRadar(-1152, 76125, -3566);
				return "30600-05.htm";
			case 44:
				qs.clearRadar();
				qs.addRadar(10580, 17574, -4554);
				return "30600-05.htm";
			case 45:
				qs.clearRadar();
				qs.addRadar(12009, 15704, -4554);
				return "30600-05.htm";
			case 46:
				qs.clearRadar();
				qs.addRadar(11951, 15661, -4554);
				return "30600-05.htm";
			case 47:
				qs.clearRadar();
				qs.addRadar(10761, 17970, -4554);
				return "30600-05.htm";
			case 48:
				qs.clearRadar();
				qs.addRadar(10823, 18013, -4554);
				return "30600-05.htm";
			case 49:
				qs.clearRadar();
				qs.addRadar(11283, 14226, -4242);
				return "30600-05.htm";
			case 50:
				qs.clearRadar();
				qs.addRadar(10447, 14620, -4242);
				return "30600-05.htm";
			case 51:
				qs.clearRadar();
				qs.addRadar(11258, 14431, -4242);
				return "30600-05.htm";
			case 52:
				qs.clearRadar();
				qs.addRadar(10344, 14445, -4242);
				return "30600-05.htm";
			case 53:
				qs.clearRadar();
				qs.addRadar(10315, 14293, -4242);
				return "30600-05.htm";
			case 54:
				qs.clearRadar();
				qs.addRadar(10775, 14190, -4242);
				return "30600-05.htm";
			case 55:
				qs.clearRadar();
				qs.addRadar(11235, 14078, -4242);
				return "30600-05.htm";
			case 56:
				qs.clearRadar();
				qs.addRadar(11012, 14128, -4242);
				return "30600-05.htm";
			case 57:
				qs.clearRadar();
				qs.addRadar(13380, 17430, -4542);
				return "30600-05.htm";
			case 58:
				qs.clearRadar();
				qs.addRadar(13464, 17751, -4541);
				return "30600-05.htm";
			case 59:
				qs.clearRadar();
				qs.addRadar(13763, 17501, -4542);
				return "30600-05.htm";
			case 60:
				qs.clearRadar();
				qs.addRadar(-44225, 79721, -3652);
				return "30600-05.htm";
			case 61:
				qs.clearRadar();
				qs.addRadar(-44015, 79683, -3652);
				return "30600-05.htm";
			case 62:
				qs.clearRadar();
				qs.addRadar(25856, 10832, -3724);
				return "30600-05.htm";
			case 63:
				qs.clearRadar();
				qs.addRadar(12328, 14947, -4574);
				return "30600-05.htm";
			case 64:
				qs.clearRadar();
				qs.addRadar(13081, 18444, -4573);
				return "30600-05.htm";
			case 65:
				qs.clearRadar();
				qs.addRadar(12311, 17470, -4574);
				return "30600-05.htm";
		}
		return "";
	}
	
	private String eventGuideDwarfGullin(int event, QuestState qs)
	{
		switch (event)
		{
			case 10:
				return "30601-04.htm";
			case 11:
				return "30601-04a.htm";
			case 12:
				return "30601-04b.htm";
			case 13:
				return "30601-04c.htm";
			case 14:
				return "30601-04d.htm";
			case 15:
				return "30601-04e.htm";
			case 16:
				return "30601-04f.htm";
			case 17:
				return "30601-04g.htm";
			case 18:
				return "30601-04h.htm";
			case 31:
				qs.clearRadar();
				qs.addRadar(115072, -178176, -906);
				return "30601-05.htm";
			case 32:
				qs.clearRadar();
				qs.addRadar(117847, -182339, -1537);
				return "30601-05.htm";
			case 33:
				qs.clearRadar();
				qs.addRadar(116617, -184308, -1569);
				return "30601-05.htm";
			case 34:
				qs.clearRadar();
				qs.addRadar(117826, -182576, -1537);
				return "30601-05.htm";
			case 35:
				qs.clearRadar();
				qs.addRadar(116378, -184308, -1571);
				return "30601-05.htm";
			case 36:
				qs.clearRadar();
				qs.addRadar(115183, -176728, -791);
				return "30601-05.htm";
			case 37:
				qs.clearRadar();
				qs.addRadar(114969, -176752, -790);
				return "30601-05.htm";
			case 38:
				qs.clearRadar();
				qs.addRadar(117366, -178725, -1118);
				return "30601-05.htm";
			case 39:
				qs.clearRadar();
				qs.addRadar(117378, -178914, -1120);
				return "30601-05.htm";
			case 40:
				qs.clearRadar();
				qs.addRadar(116226, -178529, -948);
				return "30601-05.htm";
			case 41:
				qs.clearRadar();
				qs.addRadar(116190, -178441, -948);
				return "30601-05.htm";
			case 42:
				qs.clearRadar();
				qs.addRadar(116016, -178615, -948);
				return "30601-05.htm";
			case 43:
				qs.clearRadar();
				qs.addRadar(116190, -178615, -948);
				return "30601-05.htm";
			case 44:
				qs.clearRadar();
				qs.addRadar(116103, -178407, -948);
				return "30601-05.htm";
			case 45:
				qs.clearRadar();
				qs.addRadar(116103, -178653, -948);
				return "30601-05.htm";
			case 46:
				qs.clearRadar();
				qs.addRadar(115468, -182446, -1434);
				return "30601-05.htm";
			case 47:
				qs.clearRadar();
				qs.addRadar(115315, -182155, -1444);
				return "30601-05.htm";
			case 48:
				qs.clearRadar();
				qs.addRadar(115271, -182692, -1445);
				return "30601-05.htm";
			case 49:
				qs.clearRadar();
				qs.addRadar(115900, -177316, -915);
				return "30601-05.htm";
			case 50:
				qs.clearRadar();
				qs.addRadar(116268, -177524, -914);
				return "30601-05.htm";
			case 51:
				qs.clearRadar();
				qs.addRadar(115741, -181645, -1344);
				return "30601-05.htm";
			case 52:
				qs.clearRadar();
				qs.addRadar(116192, -181072, -1344);
				return "30601-05.htm";
			case 53:
				qs.clearRadar();
				qs.addRadar(115205, -180024, -870);
				return "30601-05.htm";
			case 54:
				qs.clearRadar();
				qs.addRadar(114716, -180018, -871);
				return "30601-05.htm";
			case 55:
				qs.clearRadar();
				qs.addRadar(114832, -179520, -871);
				return "30601-05.htm";
			case 56:
				qs.clearRadar();
				qs.addRadar(115717, -183488, -1483);
				return "30601-05.htm";
			case 57:
				qs.clearRadar();
				qs.addRadar(115618, -183265, -1483);
				return "30601-05.htm";
			case 58:
				qs.clearRadar();
				qs.addRadar(114348, -178537, -813);
				return "30601-05.htm";
			case 59:
				qs.clearRadar();
				qs.addRadar(114990, -177294, -854);
				return "30601-05.htm";
			case 60:
				qs.clearRadar();
				qs.addRadar(114426, -178672, -812);
				return "30601-05.htm";
			case 61:
				qs.clearRadar();
				qs.addRadar(114409, -178415, -812);
				return "30601-05.htm";
			case 62:
				qs.clearRadar();
				qs.addRadar(117061, -181867, -1413);
				return "30601-05.htm";
			case 63:
				qs.clearRadar();
				qs.addRadar(116164, -184029, -1507);
				return "30601-05.htm";
			case 64:
				qs.clearRadar();
				qs.addRadar(115563, -182923, -1448);
				return "30601-05.htm";
			case 65:
				qs.clearRadar();
				qs.addRadar(112656, -174864, -611);
				return "30601-05.htm";
			case 66:
				qs.clearRadar();
				qs.addRadar(116852, -183595, -1566);
				return "30601-05.htm";
		}
		return "";
	}
	
	private String eventGuideOrcTanai(int event, QuestState qs)
	{
		switch (event)
		{
			case 10:
				return "30602-04.htm";
			case 11:
				return "30602-04a.htm";
			case 12:
				return "30602-04b.htm";
			case 13:
				return "30602-04c.htm";
			case 14:
				return "30602-04d.htm";
			case 15:
				return "30602-04e.htm";
			case 16:
				return "30602-04f.htm";
			case 17:
				return "30602-04g.htm";
			case 18:
				return "30602-04h.htm";
			case 19:
				return "30602-04i.htm";
			case 31:
				qs.clearRadar();
				qs.addRadar(-45264, -112512, -235);
				return "30602-05.htm";
			case 32:
				qs.clearRadar();
				qs.addRadar(-46576, -117311, -242);
				return "30602-05.htm";
			case 33:
				qs.clearRadar();
				qs.addRadar(-47360, -113791, -237);
				return "30602-05.htm";
			case 34:
				qs.clearRadar();
				qs.addRadar(-47360, -113424, -235);
				return "30602-05.htm";
			case 35:
				qs.clearRadar();
				qs.addRadar(-45744, -117165, -236);
				return "30602-05.htm";
			case 36:
				qs.clearRadar();
				qs.addRadar(-46528, -109968, -250);
				return "30602-05.htm";
			case 37:
				qs.clearRadar();
				qs.addRadar(-45808, -110055, -255);
				return "30602-05.htm";
			case 38:
				qs.clearRadar();
				qs.addRadar(-45731, -113844, -237);
				return "30602-05.htm";
			case 39:
				qs.clearRadar();
				qs.addRadar(-45728, -113360, -237);
				return "30602-05.htm";
			case 40:
				qs.clearRadar();
				qs.addRadar(-45952, -114784, -199);
				return "30602-05.htm";
			case 41:
				qs.clearRadar();
				qs.addRadar(-45952, -114496, -199);
				return "30602-05.htm";
			case 42:
				qs.clearRadar();
				qs.addRadar(-45863, -112621, -200);
				return "30602-05.htm";
			case 43:
				qs.clearRadar();
				qs.addRadar(-45864, -112540, -199);
				return "30602-05.htm";
			case 44:
				qs.clearRadar();
				qs.addRadar(-43264, -112532, -220);
				return "30602-05.htm";
			case 45:
				qs.clearRadar();
				qs.addRadar(-43910, -115518, -194);
				return "30602-05.htm";
			case 46:
				qs.clearRadar();
				qs.addRadar(-43950, -115457, -194);
				return "30602-05.htm";
			case 47:
				qs.clearRadar();
				qs.addRadar(-44416, -111486, -222);
				return "30602-05.htm";
			case 48:
				qs.clearRadar();
				qs.addRadar(-43926, -111794, -222);
				return "30602-05.htm";
			case 49:
				qs.clearRadar();
				qs.addRadar(-43109, -113770, -221);
				return "30602-05.htm";
			case 50:
				qs.clearRadar();
				qs.addRadar(-43114, -113404, -221);
				return "30602-05.htm";
			case 51:
				qs.clearRadar();
				qs.addRadar(-46768, -113610, -3);
				return "30602-05.htm";
			case 52:
				qs.clearRadar();
				qs.addRadar(-46802, -114011, -112);
				return "30602-05.htm";
			case 53:
				qs.clearRadar();
				qs.addRadar(-46247, -113866, -21);
				return "30602-05.htm";
			case 54:
				qs.clearRadar();
				qs.addRadar(-46808, -113184, -112);
				return "30602-05.htm";
			case 55:
				qs.clearRadar();
				qs.addRadar(-45328, -114736, -237);
				return "30602-05.htm";
			case 56:
				qs.clearRadar();
				qs.addRadar(-44624, -111873, -238);
				return "30602-05.htm";
		}
		return "";
	}
	
	private String eventGuideKrenisk(int event, QuestState qs)
	{
		switch (event)
		{
			case 10:
				return "32135-04.htm";
			case 11:
				return "32135-04a.htm";
			case 12:
				return "32135-04b.htm";
			case 13:
				return "32135-04c.htm";
			case 14:
				return "32135-04d.htm";
			case 15:
				return "32135-04e.htm";
			case 16:
				return "32135-04f.htm";
			case 17:
				return "32135-04g.htm";
			case 18:
				return "32135-04h.htm";
			case 19:
				return "32135-04i.htm";
			case 20:
				return "32135-04j.htm";
			case 21:
				return "32135-04k.htm";
			case 22:
				return "32135-04l.htm";
			case 31:
				qs.clearRadar();
				qs.addRadar(-116879, 46591, 380);
				return "32135-05.htm";
			case 32:
				qs.clearRadar();
				qs.addRadar(-119378, 49242, 22);
				return "32135-05.htm";
			case 33:
				qs.clearRadar();
				qs.addRadar(-119774, 49245, 22);
				return "32135-05.htm";
			case 34:
				qs.clearRadar();
				qs.addRadar(-119830, 51860, -787);
				return "32135-05.htm";
			case 35:
				qs.clearRadar();
				qs.addRadar(-119362, 51862, -780);
				return "32135-05.htm";
			case 36:
				qs.clearRadar();
				qs.addRadar(-112872, 46850, 68);
				return "32135-05.htm";
			case 37:
				qs.clearRadar();
				qs.addRadar(-112352, 47392, 68);
				return "32135-05.htm";
			case 38:
				qs.clearRadar();
				qs.addRadar(-110544, 49040, -1124);
				return "32135-05.htm";
			case 39:
				qs.clearRadar();
				qs.addRadar(-110536, 45162, -1132);
				return "32135-05.htm";
			case 40:
				qs.clearRadar();
				qs.addRadar(-115888, 43568, 524);
				return "32135-05.htm";
			case 41:
				qs.clearRadar();
				qs.addRadar(-115486, 43567, 525);
				return "32135-05.htm";
			case 42:
				qs.clearRadar();
				qs.addRadar(-116920, 47792, 464);
				return "32135-05.htm";
			case 43:
				qs.clearRadar();
				qs.addRadar(-116749, 48077, 462);
				return "32135-05.htm";
			case 44:
				qs.clearRadar();
				qs.addRadar(-117153, 48075, 463);
				return "32135-05.htm";
			case 45:
				qs.clearRadar();
				qs.addRadar(-119104, 43280, 559);
				return "32135-05.htm";
			case 46:
				qs.clearRadar();
				qs.addRadar(-119104, 43152, 559);
				return "32135-05.htm";
			case 47:
				qs.clearRadar();
				qs.addRadar(-117056, 43168, 559);
				return "32135-05.htm";
			case 48:
				qs.clearRadar();
				qs.addRadar(-117060, 43296, 559);
				return "32135-05.htm";
			case 49:
				qs.clearRadar();
				qs.addRadar(-118192, 42384, 838);
				return "32135-05.htm";
			case 50:
				qs.clearRadar();
				qs.addRadar(-117968, 42384, 838);
				return "32135-05.htm";
			case 51:
				qs.clearRadar();
				qs.addRadar(-118132, 42788, 723);
				return "32135-05.htm";
			case 52:
				qs.clearRadar();
				qs.addRadar(-118028, 42788, 720);
				return "32135-05.htm";
			case 53:
				qs.clearRadar();
				qs.addRadar(-114802, 44821, 524);
				return "32135-05.htm";
			case 54:
				qs.clearRadar();
				qs.addRadar(-114975, 44658, 524);
				return "32135-05.htm";
			case 55:
				qs.clearRadar();
				qs.addRadar(-114801, 45031, 525);
				return "32135-05.htm";
			case 56:
				qs.clearRadar();
				qs.addRadar(-120432, 45296, 416);
				return "32135-05.htm";
			case 57:
				qs.clearRadar();
				qs.addRadar(-120706, 45079, 419);
				return "32135-05.htm";
			case 58:
				qs.clearRadar();
				qs.addRadar(-120356, 45293, 416);
				return "32135-05.htm";
			case 59:
				qs.clearRadar();
				qs.addRadar(-120604, 44960, 423);
				return "32135-05.htm";
			case 60:
				qs.clearRadar();
				qs.addRadar(-120294, 46013, 384);
				return "32135-05.htm";
			case 61:
				qs.clearRadar();
				qs.addRadar(-120157, 45813, 355);
				return "32135-05.htm";
			case 62:
				qs.clearRadar();
				qs.addRadar(-120158, 46221, 354);
				return "32135-05.htm";
			case 63:
				qs.clearRadar();
				qs.addRadar(-120400, 46921, 415);
				return "32135-05.htm";
			case 64:
				qs.clearRadar();
				qs.addRadar(-120407, 46755, 423);
				return "32135-05.htm";
			case 65:
				qs.clearRadar();
				qs.addRadar(-120442, 47125, 422);
				return "32135-05.htm";
			case 66:
				qs.clearRadar();
				qs.addRadar(-118720, 48062, 473);
				return "32135-05.htm";
			case 67:
				qs.clearRadar();
				qs.addRadar(-118918, 47956, 474);
				return "32135-05.htm";
			case 68:
				qs.clearRadar();
				qs.addRadar(-118527, 47955, 473);
				return "32135-05.htm";
			case 69:
				qs.clearRadar();
				qs.addRadar(-117605, 48079, 472);
				return "32135-05.htm";
			case 70:
				qs.clearRadar();
				qs.addRadar(-117824, 48080, 476);
				return "32135-05.htm";
			case 71:
				qs.clearRadar();
				qs.addRadar(-118030, 47930, 465);
				return "32135-05.htm";
			case 72:
				qs.clearRadar();
				qs.addRadar(-119221, 46981, 380);
				return "32135-05.htm";
			case 73:
				qs.clearRadar();
				qs.addRadar(-118080, 42835, 720);
				return "32135-05.htm";
		}
		return "";
	}
	
	public static void main(String[] args)
	{
		new NewbieGuide();
	}
}
