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
package l2r.gameserver.scripts.ai.group_template;

import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;
import l2r.util.Rnd;

public class DeluLizardmanSpecialAgent extends AbstractNpcAI
{
	private static final int DELU_LIZARDMAN_SPECIAL_AGENT = 21105;
	
	public DeluLizardmanSpecialAgent(int questId, String name, String descr)
	{
		super(name, descr);
		addAttackId(DELU_LIZARDMAN_SPECIAL_AGENT);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if (npc.getId() == DELU_LIZARDMAN_SPECIAL_AGENT)
		{
			if (npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			{
				if (Rnd.get(100) < 70)
					npc.broadcastNpcSay("How dare you interrupt our fight! Hey guys, help!");
			}
			else if (Rnd.get(100) < 10)
				npc.broadcastNpcSay("Hey! Were having a duel here!");
		}
		
		return super.onAttack(npc, player, damage, isPet);
	}
	
	public static void main(String[] args)
	{
		new DeluLizardmanSpecialAgent(-1, DeluLizardmanSpecialAgent.class.getSimpleName(), "ai");
	}
}