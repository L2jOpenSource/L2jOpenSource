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

public class TimakOrcTroopLeader extends AbstractNpcAI
{
	private static final int TIMAK_ORC_TROOP_LEADER = 20767;
	
	public TimakOrcTroopLeader()
	{
		super(TimakOrcTroopLeader.class.getSimpleName(), "ai");
		addAttackId(TIMAK_ORC_TROOP_LEADER);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if (npc.getId() == TIMAK_ORC_TROOP_LEADER)
		{
			if (npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
			{
				if (Rnd.get(100) < 50)
				{
					npc.broadcastNpcSay("Destroy the enemy, my brothers!");
				}
			}
		}
		
		return super.onAttack(npc, player, damage, isPet);
	}
}