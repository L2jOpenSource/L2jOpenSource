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
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.serverpackets.NpcSay;
import l2r.util.Rnd;

import ai.npc.AbstractNpcAI;

public class OlMahumGeneral extends AbstractNpcAI
{
	private static final int Ol_Mahum_General = 20438;
	private static boolean _FirstAttacked;
	
	public OlMahumGeneral()
	{
		super(OlMahumGeneral.class.getSimpleName(), "ai");
		
		addAttackId(Ol_Mahum_General);
		addKillId(Ol_Mahum_General);
		_FirstAttacked = false;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.getId() == Ol_Mahum_General)
		{
			if (_FirstAttacked)
			{
				if (Rnd.get(100) == 50)
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.I_WILL_DEFINITELY_REPAY_THIS_HUMILIATION));
				}
				if (Rnd.get(100) == 50)
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.WE_SHALL_SEE_ABOUT_THAT));
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
		if (npcId == Ol_Mahum_General)
		{
			_FirstAttacked = false;
		}
		return super.onKill(npc, killer, isPet);
	}
}