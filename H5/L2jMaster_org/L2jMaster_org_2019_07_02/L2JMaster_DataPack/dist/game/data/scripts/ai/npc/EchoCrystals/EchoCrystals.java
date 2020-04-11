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
package ai.npc.EchoCrystals;

import java.util.HashMap;
import java.util.Map;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.itemcontainer.Inventory;

import ai.npc.AbstractNpcAI;

/**
 * Echo Crystals AI.
 * @author Plim, Adry_85
 * @since 2.6.0.0
 */
public final class EchoCrystals extends AbstractNpcAI
{
	private static final class RewardInfo
	{
		public final int _crystalId;
		public final String _okMsg;
		public final String _noAdenaMsg;
		public final String _noScoreMsg;
		
		public RewardInfo(int crystalId, String okMsg, String noAdenaMsg, String noScoreMsg)
		{
			_crystalId = crystalId;
			_okMsg = okMsg;
			_noAdenaMsg = noAdenaMsg;
			_noScoreMsg = noScoreMsg;
		}
		
		public int getCrystalId()
		{
			return _crystalId;
		}
		
		public String getOkMsg()
		{
			return _okMsg;
		}
		
		public String getNoAdenaMsg()
		{
			return _noAdenaMsg;
		}
		
		public String getNoScoreMsg()
		{
			return _noScoreMsg;
		}
	}
	
	// NPCs
	private final static int[] NPCs =
	{
		31042, // Kantabilon
		31043, // Octavia
	};
	
	private static final Map<Integer, RewardInfo> SCORES = new HashMap<>();
	static
	{
		SCORES.put(4410, new RewardInfo(4411, "01", "02", "03"));
		SCORES.put(4409, new RewardInfo(4412, "04", "05", "06"));
		SCORES.put(4408, new RewardInfo(4413, "07", "08", "09"));
		SCORES.put(4420, new RewardInfo(4414, "10", "11", "12"));
		SCORES.put(4421, new RewardInfo(4415, "13", "14", "15"));
		SCORES.put(4419, new RewardInfo(4417, "16", "02", "03"));
		SCORES.put(4418, new RewardInfo(4416, "17", "02", "03"));
	}
	
	private EchoCrystals()
	{
		super(EchoCrystals.class.getSimpleName(), "ai/npc");
		addStartNpc(NPCs);
		addTalkId(NPCs);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		final int score = Integer.valueOf(event);
		if (SCORES.containsKey(score))
		{
			if (!hasQuestItems(player, score))
			{
				htmltext = npc.getId() + "-" + SCORES.get(score).getNoScoreMsg() + ".htm";
			}
			else if (player.getAdena() < 200)
			{
				htmltext = npc.getId() + "-" + SCORES.get(score).getNoAdenaMsg() + ".htm";
			}
			else
			{
				takeItems(player, Inventory.ADENA_ID, 200);
				giveItems(player, SCORES.get(score).getCrystalId(), 1);
				htmltext = npc.getId() + "-" + SCORES.get(score).getOkMsg() + ".htm";
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new EchoCrystals();
	}
}
