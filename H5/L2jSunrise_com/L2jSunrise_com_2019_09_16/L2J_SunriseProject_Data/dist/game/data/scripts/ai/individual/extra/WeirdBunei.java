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
import l2r.gameserver.model.skills.L2Skill;

import ai.npc.AbstractNpcAI;

public class WeirdBunei extends AbstractNpcAI
{
	private static final int WEIRD = 18564;
	
	boolean _isAlreadyStarted = false;
	
	public WeirdBunei()
	{
		super(WeirdBunei.class.getSimpleName(), "ai/individual/extra");
		addAttackId(WEIRD);
		addKillId(WEIRD);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("time_to_skill"))
		{
			if (_isAlreadyStarted == true)
			{
				_isAlreadyStarted = false;
				npc.setTarget(player);
				npc.doCast(SkillData.getInstance().getInfo(5625, 1));
			}
			else
			{
				return "";
			}
		}
		return "";
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet, L2Skill skill)
	{
		int npcId = npc.getId();
		if (npcId == WEIRD)
		{
			if (_isAlreadyStarted == false)
			{
				startQuestTimer("time_to_skill", 30000, npc, player);
				_isAlreadyStarted = true;
			}
			else if (_isAlreadyStarted == true)
			{
				return "";
			}
		}
		return "";
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getId();
		if (npcId == WEIRD)
		{
			cancelQuestTimer("time_to_skill", npc, player);
		}
		return "";
	}
}