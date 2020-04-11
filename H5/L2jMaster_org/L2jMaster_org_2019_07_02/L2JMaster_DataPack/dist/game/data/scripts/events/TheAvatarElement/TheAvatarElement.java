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
 package events.TheAvatarElement;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.event.LongTimeEvent;
import com.l2jserver.gameserver.model.quest.QuestState;

/**
 * @author MaGa
 */
public class TheAvatarElement extends LongTimeEvent
{
	private static final int ELEMENT_NPC = 506;
	private static final int WATER_CRYSTAL = 23008;
	private static final int FIRE_CRYSTAL = 23009;
	private static final int WIND_CRYSTAL = 23010;
	private static final int EARTH_CRYSTAL = 23011;
	private static final int DARK_CRYSTAL = 23012;
	private static final int LIGHT_CRYSTAL = 23013;
	private static final int REWARD = 23007;
	
	private static final int[] REQUIRED_PIECES =
	{
		23008,
		23009,
		23010,
		23011,
		23012,
		23013
	};
	private static final int[] REQUIRED_QTY =
	{
		5,
		5,
		5,
		5,
		5,
		5
	};
	
	private static final int[] ForgeOfTheGods =
	{
		22640,
		21382,
		21376,
		22634,
		21383,
		22641,
		21380,
		22638,
		21379,
		22637,
		21377,
		22635,
		18802,
		21378,
		21652,
		22636,
		21387,
		21655,
		22644,
		21385,
		22642,
		21386,
		22643,
		21393,
		21657,
		22649
	};
	private static final int[] IsleOfPrayer =
	{
		22264,
		22263,
		22266,
		22265
	};
	private static final int[] SeedOfAnnihilation =
	{
		22750,
		22751,
		22752,
		22758,
		22759,
		22757,
		22764,
		22765,
		22763
	};
	private static final int[] StakatoNest =
	{
		22627,
		22619,
		22630,
		22631,
		22625,
		22626,
		22622,
		22628,
		22633
	};
	private static final int[] AntharasLair =
	{
		22842,
		22853,
		22846,
		22847,
		22848,
		22851,
		22849,
		22850,
		22852,
		22857,
		22855,
		22854,
		22856,
		22838,
		22840,
		22839,
		22841,
		22843,
		22846
	};
	private static final int[] MonasteryOfSilence =
	{
		22799,
		22800,
		22795,
		22797,
		22796,
		22794,
		22798
	};
	
	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		{
			htmltext = "start.htm";
		}
		return htmltext;
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		String htmltext = "start.htm";
		if (event.equalsIgnoreCase("reward"))
		{
			for (int i = 0; i < REQUIRED_PIECES.length; i++)
			{
				if (st.getQuestItemsCount(REQUIRED_PIECES[i]) < REQUIRED_QTY[i])
				{
					return "error.htm";
				}
			}
			for (int i = 0; i < REQUIRED_PIECES.length; i++)
			{
				st.takeItems(REQUIRED_PIECES[i], REQUIRED_QTY[i]);
			}
			st.giveItems(REWARD, 1);
			htmltext = "reward.htm";
		}
		else
		{
			htmltext = "info.htm";
		}
		return htmltext;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		int npcId = npc.getId();
		for (int Id : ForgeOfTheGods)
		{
			if (npcId == Id)
			{
				giveItems(player, FIRE_CRYSTAL, 1);
			}
		}
		for (int Id : IsleOfPrayer)
		{
			if (npcId == Id)
			{
				giveItems(player, WATER_CRYSTAL, 1);
			}
		}
		for (int Id : SeedOfAnnihilation)
		{
			if (npcId == Id)
			{
				giveItems(player, WIND_CRYSTAL, 1);
			}
		}
		for (int Id : StakatoNest)
		{
			if (npcId == Id)
			{
				giveItems(player, EARTH_CRYSTAL, 1);
			}
		}
		for (int Id : AntharasLair)
		{
			if (npcId == Id)
			{
				giveItems(player, DARK_CRYSTAL, 1);
			}
		}
		for (int Id : MonasteryOfSilence)
		{
			if (npcId == Id)
			{
				giveItems(player, LIGHT_CRYSTAL, 1);
			}
		}
		return super.onKill(npc, player, isPet);
	}
	
	public TheAvatarElement()
	{
		super(TheAvatarElement.class.getSimpleName(), "events");
		addStartNpc(ELEMENT_NPC);
		addFirstTalkId(ELEMENT_NPC);
		addTalkId(ELEMENT_NPC);
		addKillId(ForgeOfTheGods);
		addKillId(IsleOfPrayer);
		addKillId(SeedOfAnnihilation);
		addKillId(AntharasLair);
		addKillId(MonasteryOfSilence);
		addKillId(StakatoNest);
	}
	
	public static void main(String[] args)
	{
		new TheAvatarElement();
	}
}