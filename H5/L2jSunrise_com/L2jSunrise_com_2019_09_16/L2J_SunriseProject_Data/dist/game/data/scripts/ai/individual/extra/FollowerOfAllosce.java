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

import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import ai.npc.AbstractNpcAI;

public class FollowerOfAllosce extends AbstractNpcAI
{
	private static final int FOFALLOSCE = 18568;
	
	public FollowerOfAllosce()
	{
		super(FollowerOfAllosce.class.getSimpleName(), "ai/individual/extra");
		addAggroRangeEnterId(FOFALLOSCE);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("time_to_skill"))
		{
			npc.setTarget(player);
			npc.doCast(SkillData.getInstance().getInfo(5624, 1));
			startQuestTimer("time_to_skill", 30000, npc, player);
		}
		return "";
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getId();
		
		if (npcId == FOFALLOSCE)
		{
			npc.setIsInvul(true);
			startQuestTimer("time_to_skill", 30000, npc, player);
			npc.setTarget(player);
			npc.doCast(SkillData.getInstance().getInfo(5624, 1));
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
}