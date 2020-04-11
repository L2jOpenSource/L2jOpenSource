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
package gracia.AI.NPC.GatekeeperOfAbyss;

import com.l2jserver.gameserver.instancemanager.SeedOfInfinityManager;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.quest.QuestState;

import ai.npc.AbstractNpcAI;

/**
 * Gatekeeper Of The Abyss AI.
 * @author Sacrifice
 */
public final class GatekeeperOfAbyss extends AbstractNpcAI
{
	private static final int GATEKEEPER_OF_THE_ABYSS = 32539;
	
	public GatekeeperOfAbyss()
	{
		super(GatekeeperOfAbyss.class.getSimpleName(), "gracia/AI/NPC");
		
		addStartNpc(GATEKEEPER_OF_THE_ABYSS);
		addFirstTalkId(GATEKEEPER_OF_THE_ABYSS);
		addTalkId(GATEKEEPER_OF_THE_ABYSS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		QuestState st = player.getQuestState(GatekeeperOfAbyss.class.getSimpleName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		if (event.equalsIgnoreCase("request_permission"))
		{
			if ((SeedOfInfinityManager.getCurrentStage() == 2) || (SeedOfInfinityManager.getCurrentStage() == 5))
			{
				htmltext = "32539-02.htm";
			}
			else if ((SeedOfInfinityManager.getCurrentStage() == 3) && SeedOfInfinityManager.isSeedOpen())
			{
				htmltext = "32539-03.htm";
			}
			else
			{
				htmltext = "32539-01.htm";
			}
		}
		else if (event.equalsIgnoreCase("enter_seed"))
		{
			if (SeedOfInfinityManager.getCurrentStage() == 3)
			{
				SeedOfInfinityManager.teleportInSeed(player);
				return null;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(GatekeeperOfAbyss.class.getSimpleName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		if (npc.getId() == GATEKEEPER_OF_THE_ABYSS)
		{
			return "32539.htm";
		}
		return "";
	}
	
	public static void main(String[] args)
	{
		new GatekeeperOfAbyss();
	}
}