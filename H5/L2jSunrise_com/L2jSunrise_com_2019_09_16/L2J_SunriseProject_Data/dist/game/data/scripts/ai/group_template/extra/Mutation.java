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
package ai.group_template.extra;

import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.util.Rnd;

import ai.npc.AbstractNpcAI;

public class Mutation extends AbstractNpcAI
{
	private static final int CHANCE = 1; // 1% chance
	private static final String[] NPCS_MESSAGES =
	{
		"I cannot despise the fellow! I see his sincerity in the duel.",
		"Nows we truly begin!",
		"Fool! Right now is only practice!",
		"Have a look at my true strength.",
		"This time at the last! The end!"
	};
	
	private static final int[][] MUTATION_IDS =
	{
		// old ID, new ID
		{
			20835,
			21608
		},
		{
			21608,
			21609
		},
		{
			20832,
			21602
		},
		{
			21602,
			21603
		},
		{
			20833,
			21605
		},
		{
			21605,
			21606
		},
		{
			21625,
			21623
		},
		{
			21623,
			21624
		},
		{
			20842,
			21620
		},
		{
			21620,
			21621
		}
	};
	
	public Mutation()
	{
		super(Mutation.class.getSimpleName(), "ai");
		for (int npcId : MUTATION_IDS[0])
		{
			addAttackId(npcId);
		}
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		final int npcId = npc.getId();
		if (Rnd.get(100) <= CHANCE)
		{
			int arrId = getContainsId(MUTATION_IDS[0], npcId);
			if (!(arrId < 0))
			{
				npc.broadcastNpcSay(NPCS_MESSAGES[4]);
				npc.deleteMe();
				L2Npc newNpc = addSpawn(MUTATION_IDS[1][arrId], npc);
				newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
				
				return super.onAttack(newNpc, player, damage, isPet);
			}
		}
		return super.onAttack(npc, player, damage, isPet);
	}
	
	/**
	 * @param is
	 * @param npcId
	 * @return
	 */
	private int getContainsId(int[] is, int npcId)
	{
		return 0;
	}
}
