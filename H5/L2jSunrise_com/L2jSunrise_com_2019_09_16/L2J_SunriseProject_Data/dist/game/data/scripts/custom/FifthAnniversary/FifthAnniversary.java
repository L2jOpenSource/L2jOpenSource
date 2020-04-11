/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package custom.FifthAnniversary;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import l2r.gameserver.data.EventDroplist;
import l2r.gameserver.data.sql.AnnouncementsTable;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.announce.EventAnnouncement;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.script.DateRange;
import l2r.gameserver.util.Broadcast;
import l2r.util.Rnd;

public class FifthAnniversary extends Quest
{
	private final static String qn = "FifthAnniversary";
	
	/**
	 * Event beginning and end date.
	 */
	private static final String EVENT_DATE = "28 03 2009-05 05 2009"; // change date as you want
	private static final DateRange EVENT_DATES = DateRange.parse(EVENT_DATE, new SimpleDateFormat("dd MM yyyy", Locale.US));
	private static final String EVENT_ANNOUNCE = "5th Anniversary Event is currently active.";
	private static final Date EndDate = EVENT_DATES.getEndDate();
	private static final Date currentDate = new Date();
	
	// Items
	private final static int letterL = 3882;
	private final static int letterI = 3881;
	private final static int letterN = 3883;
	private final static int letterE = 3877;
	private final static int letterA = 3875;
	private final static int letterG = 3879;
	private final static int letterII = 3888;
	private final static int letter5 = 13418;
	private final static int letterY = 13417;
	private final static int letterR = 3885;
	private final static int letterS = 3886;
	private final static int letterC = 3876;
	private final static int[] dropList =
	{
		letterL,
		letterI,
		letterN,
		letterE,
		letterA,
		letterG,
		letterII,
		letter5,
		letterY,
		letterR,
		letterS,
		letterC
	};
	private final int[] dropCount =
	{
		1,
		1
	};
	private final static int dropChance = 25000; // actually 2.5%
													// Goddard,Aden,Giran, Oren,Dion,Heine,Gludio,Schuttgart,Gludin,Hunters,Rune,SoDA,Dark Elf,TI,Dwarf,Orc,Kamael
	private final static int[] EventSpawnX =
	{
		147698,
		147443,
		82227,
		82754,
		15064,
		111067,
		-12965,
		87362,
		-81037,
		117412,
		43983,
		-45907,
		12153,
		-84458,
		114750,
		-45656,
		-117195
	};
	private final static int[] EventSpawnY =
	{
		-56025,
		26942,
		148609,
		53573,
		143254,
		218933,
		122914,
		143166,
		150092,
		76642,
		-47758,
		49387,
		16753,
		244761,
		-178692,
		-113119,
		46837
	};
	private final static int[] EventSpawnZ =
	{
		-2775,
		-2205,
		-3472,
		-1496,
		-2668,
		-3543,
		-3117,
		-1293,
		-3044,
		-2695,
		-797,
		-3060,
		-4584,
		-3730,
		-820,
		-240,
		367
	};
	
	private final static int EventNPC = 31854;
	
	private static List<L2Npc> eventManagers = new ArrayList<>();
	
	private static boolean FifthAnniversaryEvent = false;
	
	public FifthAnniversary()
	{
		super(-1, qn, "retail");
		
		EventDroplist.getInstance().addGlobalDrop(dropList, dropCount, dropChance, EVENT_DATES);
		
		AnnouncementsTable.getInstance().addAnnouncement(new EventAnnouncement(EVENT_DATES, EVENT_ANNOUNCE));
		
		addStartNpc(EventNPC);
		addFirstTalkId(EventNPC);
		addTalkId(EventNPC);
		this.startQuestTimer("EventCheck", 1800000, null, null);
		
		if (EVENT_DATES.isWithinRange(currentDate))
		{
			FifthAnniversaryEvent = true;
		}
		
		if (FifthAnniversaryEvent)
		{
			_log.info("5th Anniversary Event - ON");
			
			for (int i = 0; i < EventSpawnX.length; i++)
			{
				L2Npc eventManager = addSpawn(EventNPC, EventSpawnX[i], EventSpawnY[i], EventSpawnZ[i], 0, false, 0);
				eventManagers.add(eventManager);
			}
		}
		else
		{
			_log.info("5th Anniversary Event - OFF");
			
			Calendar endWeek = Calendar.getInstance();
			endWeek.setTime(EndDate);
			endWeek.add(Calendar.DATE, 7);
			
			if (EndDate.before(currentDate) && endWeek.getTime().after(currentDate))
			{
				for (int i = 0; i < EventSpawnX.length; i++)
				{
					L2Npc eventManager = addSpawn(EventNPC, EventSpawnX[i], EventSpawnY[i], EventSpawnZ[i], 0, false, 0);
					eventManagers.add(eventManager);
				}
			}
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st;
		int prize;
		
		if (npc == null)
		{
			if (event.equalsIgnoreCase("EventCheck"))
			{
				this.startQuestTimer("EventCheck", 1800000, null, null);
				boolean Event1 = false;
				
				if (EVENT_DATES.isWithinRange(currentDate))
				{
					Event1 = true;
				}
				
				if (!FifthAnniversaryEvent && Event1)
				{
					FifthAnniversaryEvent = true;
					_log.info("5th Anniversary Event - ON");
					Broadcast.toAllOnlinePlayers("5th Anniversary Event is currently active. See the Event NPCs to participate!");
					
					for (int i = 0; i < EventSpawnX.length; i++)
					{
						L2Npc eventManager = addSpawn(EventNPC, EventSpawnX[i], EventSpawnY[i], EventSpawnZ[i], 0, false, 0);
						eventManagers.add(eventManager);
					}
				}
				else if (FifthAnniversaryEvent && !Event1)
				{
					FifthAnniversaryEvent = false;
					_log.info("5th Anniversary Event - OFF");
					for (L2Npc eventManager : eventManagers)
					{
						eventManager.deleteMe();
					}
				}
			}
		}
		else if ((player != null) && event.equalsIgnoreCase("LINEAGEII"))
		{
			st = player.getQuestState(qn);
			
			if ((st.getQuestItemsCount(letterL) >= 1) && (st.getQuestItemsCount(letterI) >= 1) && (st.getQuestItemsCount(letterN) >= 1) && (st.getQuestItemsCount(letterE) >= 2) && (st.getQuestItemsCount(letterA) >= 1) && (st.getQuestItemsCount(letterG) >= 1) && (st.getQuestItemsCount(letterII) >= 1))
			{
				st.takeItems(letterL, 1);
				st.takeItems(letterI, 1);
				st.takeItems(letterN, 1);
				st.takeItems(letterE, 2);
				st.takeItems(letterA, 1);
				st.takeItems(letterG, 1);
				st.takeItems(letterII, 1);
				
				prize = Rnd.get(1000);
				
				if (prize <= 5)
				{
					st.giveItems(6662, 1); // 1 - Ring of Core
				}
				else if (prize <= 10)
				{
					st.giveItems(8752, 1); // 1 - High grade Life Stone 76
				}
				else if (prize <= 25)
				{
					st.giveItems(8742, 1); // 1 - Mid grade Life Stone 76
				}
				else if (prize <= 50)
				{
					st.giveItems(9157, 1); // 1 - L2day Blessed SoR
				}
				else if (prize <= 75)
				{
					st.giveItems(9156, 1); // 1 - L2day Blessed SoE
				}
				else if (prize <= 100)
				{
					st.giveItems(13429, 1); // 1 - Teddy Bear Hat
				}
				else if (prize <= 200)
				{
					st.giveItems(13430, 1); // 1 - Piggy Hat
				}
				else if (prize <= 300)
				{
					st.giveItems(13431, 1); // 1 - Jester Hat
				}
				else if (prize <= 400)
				{
					st.giveItems(13425, 1); // 1 - Small Parchement Box (1x Village Soe)
				}
				else if (prize <= 500)
				{
					st.giveItems(13426, 1); // 1 - Small Mineral Box (1x Elemental Stone)
				}
				else
				{
					st.giveItems(13428, 1); // 1 - Small Libation Box (1x L2Day Juice)
				}
			}
			else
			{
				htmltext = "31854-03.htm";
			}
		}
		else if ((player != null) && event.equalsIgnoreCase("5YEARS"))
		{
			st = player.getQuestState(qn);
			
			if ((st.getQuestItemsCount(letter5) >= 1) && (st.getQuestItemsCount(letterY) >= 1) && (st.getQuestItemsCount(letterE) >= 1) && (st.getQuestItemsCount(letterA) >= 1) && (st.getQuestItemsCount(letterR) >= 1) && (st.getQuestItemsCount(letterS) >= 1))
			{
				st.takeItems(letter5, 1);
				st.takeItems(letterY, 1);
				st.takeItems(letterE, 1);
				st.takeItems(letterA, 1);
				st.takeItems(letterR, 1);
				st.takeItems(letterS, 1);
				
				prize = Rnd.get(1000);
				
				if (prize <= 5)
				{
					st.giveItems(6660, 1); // 1 - Ring of Queen Ant
				}
				else if (prize <= 10)
				{
					st.giveItems(8762, 1); // 1 - Top grade Life Stone 76
				}
				else if (prize <= 25)
				{
					st.giveItems(8752, 2); // 2 - High grade Life Stones 76
				}
				else if (prize <= 50)
				{
					st.giveItems(9157, 2); // 2 - L2day Blessed SoRs
				}
				else if (prize <= 75)
				{
					st.giveItems(9156, 2); // 2 - L2day Blessed SoEs
				}
				else if (prize <= 100)
				{
					st.giveItems(13429, 1); // 1 - Teddy Bear Hat
				}
				else if (prize <= 150)
				{
					st.giveItems(13430, 1); // 1 - Piggy Hat
				}
				else if (prize <= 200)
				{
					st.giveItems(13431, 1); // 1 - Jester Hat
				}
				else if (prize <= 300)
				{
					st.giveItems(13422, 2); // 1 - Medium Parchement Box (2x Village Soes)
				}
				else if (prize <= 400)
				{
					st.giveItems(13423, 2); // 1 - Medium Mineral Box (2x Elemental Stones)
				}
				else
				{
					st.giveItems(13424, 3); // 1 - Large Libation Box (3x L2Day Juices)
				}
			}
			else
			{
				htmltext = "31854-03.htm";
			}
		}
		else if ((player != null) && event.equalsIgnoreCase("GRACIA"))
		{
			st = player.getQuestState(qn);
			
			if ((st.getQuestItemsCount(letterG) >= 1) && (st.getQuestItemsCount(letterR) >= 1) && (st.getQuestItemsCount(letterA) >= 2) && (st.getQuestItemsCount(letterC) >= 1) && (st.getQuestItemsCount(letterI) >= 1))
			{
				st.takeItems(letterG, 1);
				st.takeItems(letterR, 1);
				st.takeItems(letterA, 2);
				st.takeItems(letterC, 1);
				st.takeItems(letterI, 1);
				
				prize = Rnd.get(1000);
				
				if (prize <= 5)
				{
					st.giveItems(6661, 1); // 1 - Earring of Orfen
				}
				else if (prize <= 10)
				{
					st.giveItems(8752, 1); // 1 - High grade Life Stone 76
				}
				else if (prize <= 25)
				{
					st.giveItems(8742, 2); // 2 - Mid grade Life Stones 76
				}
				else if (prize <= 50)
				{
					st.giveItems(9157, 1); // 1 - L2day Blessed SoR
				}
				else if (prize <= 75)
				{
					st.giveItems(9156, 1); // 1 - L2day Blessed SoE
				}
				else if (prize <= 100)
				{
					st.giveItems(13429, 1); // 1 - Teddy Bear Hat
				}
				else if (prize <= 150)
				{
					st.giveItems(13430, 1); // 1 - Piggy Hat
				}
				else if (prize <= 200)
				{
					st.giveItems(13431, 1); // 1 - Jester Hat
				}
				else if (prize <= 300)
				{
					st.giveItems(13425, 1); // 1 - Small Parchement Box (1x Village Soe)
				}
				else if (prize <= 400)
				{
					st.giveItems(13426, 1); // 1 - Small Mineral Box (1x Elemental Stone)
				}
				else
				{
					st.giveItems(13424, 2); // 1 - Medium Libation Box (2x L2Day Juices)
				}
			}
			else
			{
				htmltext = "31854-03.htm";
			}
		}
		else if (event.equalsIgnoreCase("chat0"))
		{
			htmltext = "31854.htm";
		}
		else if (event.equalsIgnoreCase("chat1"))
		{
			htmltext = "31854-02.htm";
		}
		
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		return "31854.htm";
	}
}