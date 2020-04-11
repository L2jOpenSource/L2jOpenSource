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
package l2r.gameserver.scripts.custom;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import l2r.gameserver.Announcements;
import l2r.gameserver.datatables.EventDroplist;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;
import l2r.gameserver.script.DateRange;

/**
 * Event Code for "TheValentine" http://www.lineage2.com/archive/2009/01/the_valentine_e.html
 * @author InsOmnia, StevenP
 */
public class TheValentine extends Quest
{
	
	// Event Start Date
	private static final String EVENT_DATE = "10 02 2010-17 02 2010";
	// Id of Queen of Hearts
	private static final int QUEEN_OF_HEARTS = 4301;
	private static final int CAKE_RECIPE = 20191;
	// Global drop item - Valentine Dark Chocolate
	private static final int V_DARK_CHOCOLATE = 20192;
	// Global drop item - Valentine White Chocolate
	private static final int V_WHITE_CHOCOLATE = 20193;
	// Global drop item - Valentine Fresh Cream
	private static final int V_FRESH_CREAM = 20194;
	// Global drops for this event
	private static final int[] GLOBAL_DROP =
	{
		V_DARK_CHOCOLATE,
		V_WHITE_CHOCOLATE,
		V_FRESH_CREAM
	};
	// Minimum and maximum count of dropped items
	private static final int[] GLOBAL_DROP_COUNT =
	{
		1,
		1
	};
	// Percentage drop rate for globally dropped items
	private static final int GLOBAL_DROP_CHANCE = 5;
	// Announcement displayed to player when entering game:
	private static final String[] ON_ENTER_ANNOUNCE =
	{
		"Valentine's Event is currently active.\nCollect all items and make your Valentine Cake!"
	};
	// X cords for Queen of Hearts spawn
	private static final int[] X_CORDS =
	{
		147698,
		147443,
		82218,
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
	// Y cords for Queen of Hearts spawn
	private static final int[] Y_CORDS =
	{
		-56025,
		26942,
		148608,
		53573,
		143254,
		218933,
		122914,
		-143166,
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
	// Z cords for Queen of Hearts spawn
	private static final int[] Z_CORDS =
	{
		-2775,
		-2205,
		-3473,
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
	
	private static final DateRange EVENT_DATES = DateRange.parse(EVENT_DATE, new SimpleDateFormat("dd MM yyyy", Locale.US));
	
	public TheValentine(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		
		EventDroplist.getInstance().addGlobalDrop(GLOBAL_DROP, GLOBAL_DROP_COUNT, (GLOBAL_DROP_CHANCE * 10000), EVENT_DATES);
		Announcements.getInstance().addEventAnnouncement(EVENT_DATES, ON_ENTER_ANNOUNCE);
		
		final Date currentDate = new Date();
		
		if (EVENT_DATES.isWithinRange(currentDate))
		{
			for (int i = 0; i < X_CORDS.length; i++)
			{
				addSpawn(QUEEN_OF_HEARTS, X_CORDS[i], Y_CORDS[i], Z_CORDS[i], 0, false, 0);
			}
		}
		
		addStartNpc(QUEEN_OF_HEARTS);
		addTalkId(QUEEN_OF_HEARTS);
		addFirstTalkId(QUEEN_OF_HEARTS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = QUEEN_OF_HEARTS + "-wrong.htm";
		QuestState st = player.getQuestState(getName());
		int npcId = npc.getId();
		
		if (npcId == QUEEN_OF_HEARTS && st.isCompleted())
			htmltext = QUEEN_OF_HEARTS + "-done.htm";
		else
		{
			st.giveItems(CAKE_RECIPE, 1);
			htmltext = QUEEN_OF_HEARTS + "-1.htm";
			st.setState(State.COMPLETED);
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = QUEEN_OF_HEARTS + ".htm";
		QuestState st = player.getQuestState(getName());
		if (st == null)
			st = this.newQuestState(player);
		
		int npcId = npc.getId();
		
		if (npcId == QUEEN_OF_HEARTS && st.isCompleted())
			htmltext = QUEEN_OF_HEARTS + "-done.htm";
		
		return htmltext;
	}
	
	public static void main(final String[] args)
	{
		new TheValentine(-1, "TheValentine", "retail");
	}
}
