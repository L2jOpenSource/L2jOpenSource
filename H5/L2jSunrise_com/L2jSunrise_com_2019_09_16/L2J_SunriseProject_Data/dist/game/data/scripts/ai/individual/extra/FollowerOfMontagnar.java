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
package ai.individual.extra;

import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import ai.npc.AbstractNpcAI;

public class FollowerOfMontagnar extends AbstractNpcAI
{
	private static final int FOFMONTAGNAR = 18569;
	
	public FollowerOfMontagnar()
	{
		super(FollowerOfMontagnar.class.getSimpleName(), "ai/individual/extra");
		addAggroRangeEnterId(FOFMONTAGNAR);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getId();
		
		if (npcId == FOFMONTAGNAR)
		{
			npc.setIsInvul(true);
		}
		
		return super.onAggroRangeEnter(npc, player, isPet);
	}
}