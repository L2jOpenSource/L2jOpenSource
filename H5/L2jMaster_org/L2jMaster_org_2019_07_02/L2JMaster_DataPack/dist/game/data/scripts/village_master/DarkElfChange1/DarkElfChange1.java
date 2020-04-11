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
package village_master.DarkElfChange1;

import com.l2jserver.gameserver.enums.CategoryType;
import com.l2jserver.gameserver.enums.Race;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.base.ClassId;

import ai.npc.AbstractNpcAI;

/**
 * Dark Elf class transfer AI.
 * @author Adry_85
 * @since 2.6.0.0
 */
public final class DarkElfChange1 extends AbstractNpcAI
{
	// NPCs
	private static int[] NPCS =
	{
		30290, // Xenos
		30297, // Tobias
		30462, // Tronix
		32096, // Helminter
	};
	
	// Items
	private static int GAZE_OF_ABYSS = 1244;
	private static int IRON_HEART = 1252;
	private static int DARK_JEWEL = 1261;
	private static int ORB_OF_ABYSS = 1270;
	// Rewards
	private static final int SHADOW_ITEM_EXCHANGE_COUPON_D_GRADE = 8869;
	// Classes
	private static final int PALUS_KNIGHT = 32;
	private static final int ASSASSIN = 35;
	private static final int DARK_WIZARD = 39;
	private static final int SHILLIEN_ORACLE = 42;
	// Misc
	private static final int MIN_LEVEL = 20;
	
	private DarkElfChange1()
	{
		super(DarkElfChange1.class.getSimpleName(), "village_master");
		addStartNpc(NPCS);
		addTalkId(NPCS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		switch (event)
		{
			case "30290-01.htm":
			case "30290-02.htm":
			case "30290-03.htm":
			case "30290-04.htm":
			case "30290-05.htm":
			case "30290-06.htm":
			case "30290-07.htm":
			case "30290-08.htm":
			case "30290-09.htm":
			case "30290-10.htm":
			case "30290-11.htm":
			case "30290-12.htm":
			case "30290-13.htm":
			case "30290-14.htm":
			case "30297-01.htm":
			case "30297-02.htm":
			case "30297-03.htm":
			case "30297-04.htm":
			case "30297-05.htm":
			case "30297-06.htm":
			case "30297-07.htm":
			case "30297-08.htm":
			case "30297-09.htm":
			case "30297-10.htm":
			case "30297-11.htm":
			case "30297-12.htm":
			case "30297-13.htm":
			case "30297-14.htm":
			case "30462-01.htm":
			case "30462-02.htm":
			case "30462-03.htm":
			case "30462-04.htm":
			case "30462-05.htm":
			case "30462-06.htm":
			case "30462-07.htm":
			case "30462-08.htm":
			case "30462-09.htm":
			case "30462-10.htm":
			case "30462-11.htm":
			case "30462-12.htm":
			case "30462-13.htm":
			case "30462-14.htm":
			case "32096-01.htm":
			case "32096-02.htm":
			case "32096-03.htm":
			case "32096-04.htm":
			case "32096-05.htm":
			case "32096-06.htm":
			case "32096-07.htm":
			case "32096-08.htm":
			case "32096-09.htm":
			case "32096-10.htm":
			case "32096-11.htm":
			case "32096-12.htm":
			case "32096-13.htm":
			case "32096-14.htm":
			{
				htmltext = event;
				break;
			}
			case "32":
			case "35":
			case "39":
			case "42":
			{
				htmltext = ClassChangeRequested(player, npc, Integer.valueOf(event));
				break;
			}
		}
		return htmltext;
	}
	
	private String ClassChangeRequested(L2PcInstance player, L2Npc npc, int classId)
	{
		String htmltext = null;
		if (player.isInCategory(CategoryType.SECOND_CLASS_GROUP))
		{
			htmltext = npc.getId() + "-15.htm";
		}
		else if (player.isInCategory(CategoryType.THIRD_CLASS_GROUP))
		{
			htmltext = npc.getId() + "-16.htm";
		}
		else if (player.isInCategory(CategoryType.FOURTH_CLASS_GROUP))
		{
			htmltext = "30290-34.htm";
		}
		else if ((classId == PALUS_KNIGHT) && (player.getClassId() == ClassId.darkFighter))
		{
			if (player.getLevel() < MIN_LEVEL)
			{
				if (hasQuestItems(player, GAZE_OF_ABYSS))
				{
					htmltext = npc.getId() + "-17.htm";
				}
				else
				{
					htmltext = npc.getId() + "-18.htm";
				}
			}
			else if (hasQuestItems(player, GAZE_OF_ABYSS))
			{
				takeItems(player, GAZE_OF_ABYSS, -1);
				player.setClassId(PALUS_KNIGHT);
				player.setBaseClass(PALUS_KNIGHT);
				// SystemMessage and cast skill is done by setClassId
				player.broadcastUserInfo();
				giveItems(player, SHADOW_ITEM_EXCHANGE_COUPON_D_GRADE, 15);
				htmltext = npc.getId() + "-19.htm";
			}
			else
			{
				htmltext = npc.getId() + "-20.htm";
			}
		}
		else if ((classId == ASSASSIN) && (player.getClassId() == ClassId.darkFighter))
		{
			if (player.getLevel() < MIN_LEVEL)
			{
				if (hasQuestItems(player, IRON_HEART))
				{
					htmltext = npc.getId() + "-21.htm";
				}
				else
				{
					htmltext = npc.getId() + "-22.htm";
				}
			}
			else if (hasQuestItems(player, IRON_HEART))
			{
				takeItems(player, IRON_HEART, -1);
				player.setClassId(ASSASSIN);
				player.setBaseClass(ASSASSIN);
				// SystemMessage and cast skill is done by setClassId
				player.broadcastUserInfo();
				giveItems(player, SHADOW_ITEM_EXCHANGE_COUPON_D_GRADE, 15);
				htmltext = npc.getId() + "-23.htm";
			}
			else
			{
				htmltext = npc.getId() + "-24.htm";
			}
		}
		else if ((classId == DARK_WIZARD) && (player.getClassId() == ClassId.darkMage))
		{
			if (player.getLevel() < MIN_LEVEL)
			{
				if (hasQuestItems(player, DARK_JEWEL))
				{
					htmltext = npc.getId() + "-25.htm";
				}
				else
				{
					htmltext = npc.getId() + "-26.htm";
				}
			}
			else if (hasQuestItems(player, DARK_JEWEL))
			{
				takeItems(player, DARK_JEWEL, -1);
				player.setClassId(DARK_WIZARD);
				player.setBaseClass(DARK_WIZARD);
				// SystemMessage and cast skill is done by setClassId
				player.broadcastUserInfo();
				giveItems(player, SHADOW_ITEM_EXCHANGE_COUPON_D_GRADE, 15);
				htmltext = npc.getId() + "-27.htm";
			}
			else
			{
				htmltext = npc.getId() + "-28.htm";
			}
		}
		else if ((classId == SHILLIEN_ORACLE) && (player.getClassId() == ClassId.darkMage))
		{
			if (player.getLevel() < MIN_LEVEL)
			{
				if (hasQuestItems(player, ORB_OF_ABYSS))
				{
					htmltext = npc.getId() + "-29.htm";
				}
				else
				{
					htmltext = npc.getId() + "-30.htm";
				}
			}
			else if (hasQuestItems(player, ORB_OF_ABYSS))
			{
				takeItems(player, ORB_OF_ABYSS, -1);
				player.setClassId(SHILLIEN_ORACLE);
				player.setBaseClass(SHILLIEN_ORACLE);
				// SystemMessage and cast skill is done by setClassId
				player.broadcastUserInfo();
				giveItems(player, SHADOW_ITEM_EXCHANGE_COUPON_D_GRADE, 15);
				htmltext = npc.getId() + "-31.htm";
			}
			else
			{
				htmltext = npc.getId() + "-32.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		if (player.getRace() == Race.DARK_ELF)
		{
			if (player.isInCategory(CategoryType.FIGHTER_GROUP))
			{
				htmltext = npc.getId() + "-01.htm";
			}
			else if ((player.isInCategory(CategoryType.MAGE_GROUP)))
			{
				htmltext = npc.getId() + "-08.htm";
			}
		}
		else
		{
			htmltext = npc.getId() + "-33.htm";
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new DarkElfChange1();
	}
}
