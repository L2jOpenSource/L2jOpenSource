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

import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.util.Rnd;

import ai.npc.AbstractNpcAI;

public class EtisEtina extends AbstractNpcAI
{
	private static final int ETIS = 18949;
	private static final int GUARD1 = 18950;
	private static final int GUARD2 = 18951;
	private boolean summonsReleased = false;
	private L2Npc warrior1;
	private L2Npc warrior2;
	
	public EtisEtina()
	{
		super(EtisEtina.class.getSimpleName(), "ai/individual/extra");
		addAttackId(ETIS);
		addKillId(ETIS);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.getId() == ETIS)
		{
			int maxHp = npc.getMaxHp();
			double nowHp = npc.getStatus().getCurrentHp();
			
			if ((nowHp < (maxHp * 0.7D)) && !summonsReleased)
			{
				warrior1 = addSpawn(GUARD1, npc.getX() + Rnd.get(10, 50), npc.getY() + Rnd.get(10, 50), npc.getZ(), 0, false, 0L, false, npc.getInstanceId());
				warrior1.setRunning();
				warrior1.setTarget(attacker);
				((L2Attackable) warrior1).addDamageHate(attacker, 0, 999);
				warrior1.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker, null);
				
				warrior2 = addSpawn(GUARD2, npc.getX() + Rnd.get(10, 80), npc.getY() + Rnd.get(10, 80), npc.getZ(), 0, false, 0L, false, npc.getInstanceId());
				warrior2.setRunning();
				warrior2.setTarget(attacker);
				((L2Attackable) warrior2).addDamageHate(attacker, 0, 999);
				warrior2.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker, null);
				
				summonsReleased = true;
			}
		}
		
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if ((warrior1 != null) && !warrior1.isDead())
		{
			warrior1.decayMe();
		}
		
		if ((warrior2 != null) && !warrior2.isDead())
		{
			warrior2.decayMe();
		}
		
		return super.onKill(npc, killer, isPet);
	}
}