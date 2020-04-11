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

import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.util.Util;

import ai.npc.AbstractNpcAI;

public class SelfExplosiveKamikaze extends AbstractNpcAI
{
	private static final Map<Integer, SkillHolder> MONSTERS = new HashMap<>();
	
	public SelfExplosiveKamikaze()
	{
		super(SelfExplosiveKamikaze.class.getSimpleName(), "ai/individual/extra");
		
		for (Integer integer : MONSTERS.keySet())
		{
			int npcId = integer.intValue();
			
			addAttackId(npcId);
			addSpellFinishedId(npcId);
		}
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet, L2Skill skil)
	{
		if (player != null)
		{
			if ((MONSTERS.containsKey(Integer.valueOf(npc.getId()))) && (!npc.isDead()) && (Util.checkIfInRange(MONSTERS.get(Integer.valueOf(npc.getId())).getSkill().getAffectRange(), player, npc, true)))
			{
				npc.doCast(MONSTERS.get(Integer.valueOf(npc.getId())).getSkill());
			}
		}
		return super.onAttack(npc, player, damage, isPet, skil);
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if ((MONSTERS.containsKey(Integer.valueOf(npc.getId()))) && (!npc.isDead()) && ((skill.getId() == 4614) || (skill.getId() == 5376)))
		{
			npc.doDie(null);
		}
		return super.onSpellFinished(npc, player, skill);
	}
	
	static
	{
		MONSTERS.put(Integer.valueOf(18817), new SkillHolder(5376, 4));
		MONSTERS.put(Integer.valueOf(18818), new SkillHolder(5376, 4));
		MONSTERS.put(Integer.valueOf(18821), new SkillHolder(5376, 5));
		MONSTERS.put(Integer.valueOf(21666), new SkillHolder(4614, 3));
		MONSTERS.put(Integer.valueOf(21689), new SkillHolder(4614, 4));
		MONSTERS.put(Integer.valueOf(21712), new SkillHolder(4614, 5));
		MONSTERS.put(Integer.valueOf(21735), new SkillHolder(4614, 6));
		MONSTERS.put(Integer.valueOf(21758), new SkillHolder(4614, 7));
		MONSTERS.put(Integer.valueOf(21781), new SkillHolder(4614, 9));
	}
}