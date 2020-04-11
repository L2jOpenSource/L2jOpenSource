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
package ai.npc.VillageMasters.DarkElvenChange1;

import l2r.gameserver.enums.QuestSound;
import l2r.gameserver.enums.Race;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.util.Util;

import ai.npc.AbstractNpcAI;

/**
 * Dark Elven Change Part 1<br>
 * Original Jython script by DraX and DrLecter
 * @author nonom
 */
public final class DarkElvenChange1 extends AbstractNpcAI
{
	// NPCs
	private static int[] NPCS =
	{
		30290, // Xenos
		30297, // Tobias
		30462, // Tronix
		32160, // Devon
	};
	// Items
	private static int GAZE_OF_ABYSS = 1244;
	private static int IRON_HEART = 1252;
	private static int JEWEL_OF_DARKNESS = 1261;
	private static int ORB_OF_ABYSS = 1270;
	// Rewards
	private static int SHADOW_WEAPON_COUPON_DGRADE = 8869;
	// @formatter:off
	private static int[][] CLASSES = 
	{
		{ 32, 31, 15, 16, 17, 18, GAZE_OF_ABYSS }, // PK
		{ 35, 31, 19, 20, 21, 22, IRON_HEART }, // AS
		{ 39, 38, 23, 24, 25, 26, JEWEL_OF_DARKNESS }, // DW
		{ 42, 38, 27, 28, 29, 30, ORB_OF_ABYSS }, // SO
	};
	// @formatter:on
	
	public DarkElvenChange1()
	{
		super(DarkElvenChange1.class.getSimpleName(), "ai/npc/VillageMasters");
		addStartNpc(NPCS);
		addTalkId(NPCS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		if (Util.isDigit(event))
		{
			int i = Integer.valueOf(event);
			final ClassId cid = player.getClassId();
			if ((cid.getRace() == Race.DARK_ELF) && (cid.getId() == CLASSES[i][1]))
			{
				int suffix;
				final boolean item = st.hasQuestItems(CLASSES[i][6]);
				if (player.getLevel() < 20)
				{
					suffix = (!item) ? CLASSES[i][2] : CLASSES[i][3];
				}
				else
				{
					if (!item)
					{
						suffix = CLASSES[i][4];
					}
					else
					{
						suffix = CLASSES[i][5];
						st.giveItems(SHADOW_WEAPON_COUPON_DGRADE, 15);
						st.takeItems(CLASSES[i][6], -1);
						player.setClassId(CLASSES[i][0]);
						player.setBaseClass(CLASSES[i][0]);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_2);
						player.broadcastUserInfo();
						st.exitQuest(false);
					}
				}
				event = npc.getId() + "-" + suffix + ".html";
			}
		}
		return event;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		if (player.isSubClassActive())
		{
			return htmltext;
		}
		
		final ClassId cid = player.getClassId();
		if (cid.getRace() == Race.DARK_ELF)
		{
			switch (cid)
			{
				case darkFighter:
				{
					htmltext = npc.getId() + "-01.html";
					break;
				}
				case darkMage:
				{
					htmltext = npc.getId() + "-08.html";
					break;
				}
				default:
				{
					if (cid.level() == 1)
					{
						// first occupation change already made
						return npc.getId() + "-32.html";
					}
					else if (cid.level() >= 2)
					{
						// second/third occupation change already made
						return npc.getId() + "-31.html";
					}
				}
			}
		}
		else
		{
			htmltext = npc.getId() + "-33.html"; // other races
		}
		return htmltext;
	}
}
