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

import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.NpcSay;
import l2r.util.Rnd;

import ai.npc.AbstractNpcAI;

public class BrekaOrcOverlord extends AbstractNpcAI
{
	private static final int BREKA = 20270;
	
	private static boolean _FirstAttacked;
	
	public BrekaOrcOverlord()
	{
		super(BrekaOrcOverlord.class.getSimpleName(), "ai");
		addAttackId(BREKA);
		addKillId(BREKA);
		_FirstAttacked = false;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.getId() == BREKA)
		{
			if (_FirstAttacked)
			{
				if (Rnd.get(100) == 50)
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), "Ultimate strength!!!"));
				}
				if (Rnd.get(100) == 50)
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), "Now it is truly the beginning of the duel!!"));
				}
				if (Rnd.get(100) == 50)
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), "Did not think could even use it on the callow kid trick!"));
				}
			}
			_FirstAttacked = true;
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getId();
		if (npcId == BREKA)
		{
			_FirstAttacked = false;
		}
		return super.onKill(npc, killer, isPet);
	}
}