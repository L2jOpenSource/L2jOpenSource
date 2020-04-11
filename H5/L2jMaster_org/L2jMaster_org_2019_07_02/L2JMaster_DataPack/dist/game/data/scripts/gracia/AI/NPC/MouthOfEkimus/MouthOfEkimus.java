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
package gracia.AI.NPC.MouthOfEkimus;

import com.l2jserver.gameserver.instancemanager.SeedOfInfinityManager;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.quest.QuestState;

import ai.npc.AbstractNpcAI;

/**
 * Mouth Of Ekimus AI.
 * @author Sacrifice
 */
public final class MouthOfEkimus extends AbstractNpcAI
{
	private static final int MOUTH_OF_EKIMUS = 32537;
	
	public MouthOfEkimus()
	{
		super(MouthOfEkimus.class.getSimpleName(), "gracia/AI/NPC");
		
		addStartNpc(MOUTH_OF_EKIMUS);
		addFirstTalkId(MOUTH_OF_EKIMUS);
		addTalkId(MOUTH_OF_EKIMUS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		QuestState st = player.getQuestState(MouthOfEkimus.class.getSimpleName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		if (event.equalsIgnoreCase("hos_enter"))
		{
			if (SeedOfInfinityManager.getCurrentStage() == 1)
			{
				htmltext = "32537-01.htm";
			}
			else if (SeedOfInfinityManager.getCurrentStage() == 4)
			{
				htmltext = "32537-02.htm";
			}
		}
		else if (event.equalsIgnoreCase("hoe_enter"))
		{
			if (SeedOfInfinityManager.getCurrentStage() == 1)
			{
				htmltext = "32537-03.htm";
			}
			else if (SeedOfInfinityManager.getCurrentStage() == 4)
			{
				htmltext = "32537-04.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(MouthOfEkimus.class.getSimpleName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		if (npc.getId() == MOUTH_OF_EKIMUS)
		{
			return "32537.htm";
		}
		return "";
	}
	
	public static void main(String[] args)
	{
		new MouthOfEkimus();
	}
}