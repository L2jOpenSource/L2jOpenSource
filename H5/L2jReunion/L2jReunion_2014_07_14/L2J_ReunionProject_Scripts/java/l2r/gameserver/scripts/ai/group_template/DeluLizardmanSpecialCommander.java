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

public class DeluLizardmanSpecialCommander extends AbstractNpcAI
{
	private static final int DELU_LIZARDMAN_SPECIAL_COMMANDER = 21107;
	
	public DeluLizardmanSpecialCommander(int questId, String name, String descr)
	{
		super(name, descr);
		addAttackId(DELU_LIZARDMAN_SPECIAL_COMMANDER);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if (npc.getId() == DELU_LIZARDMAN_SPECIAL_COMMANDER)
		{
			if (npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			{
				if (Rnd.get(100) < 70)
					npc.broadcastNpcSay("How dare you interrupt a sacred duel! You must be taught a lesson!");
			}
			else if (Rnd.get(100) < 10)
				npc.broadcastNpcSay("Come on, I'll take you on!");
		}
		
		return super.onAttack(npc, player, damage, isPet);
	}
	
	public static void main(String[] args)
	{
		new DeluLizardmanSpecialCommander(-1, DeluLizardmanSpecialCommander.class.getSimpleName(), "ai");
	}
}
