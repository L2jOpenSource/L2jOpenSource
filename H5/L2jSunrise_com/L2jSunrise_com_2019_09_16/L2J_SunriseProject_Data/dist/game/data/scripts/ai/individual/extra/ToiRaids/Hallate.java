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
package ai.individual.extra.ToiRaids;

import l2r.gameserver.model.actor.instance.L2NpcInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import ai.npc.AbstractNpcAI;

public class Hallate extends AbstractNpcAI
{
	private static final int HALLATE = 25220;
	private static final int z1 = -2150;
	private static final int z2 = -1650;
	
	public Hallate()
	{
		super(Hallate.class.getSimpleName(), "ai");
		addAttackId(HALLATE);
	}
	
	public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getId();
		
		if (npcId == HALLATE)
		{
			int z = npc.getZ();
			if ((z > z2) || (z < z1))
			{
				npc.teleToLocation(113548, 17061, -2125);
				npc.getStatus().setCurrentHp(npc.getMaxHp());
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
}
