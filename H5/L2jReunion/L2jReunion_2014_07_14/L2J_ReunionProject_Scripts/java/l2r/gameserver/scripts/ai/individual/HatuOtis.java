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
package l2r.gameserver.scripts.ai.individual;

import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;

public class HatuOtis extends AbstractNpcAI
{
	private static final int OTIS = 18558;
	boolean _isAlreadyUsedSkill = false;
	boolean _isAlreadyUsedSkill1 = false;
	
	public HatuOtis(int questId, String name, String descr)
	{
		super(name, descr);
		addAttackId(OTIS);
		addKillId(OTIS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("time_to_skill"))
		{
			if (_isAlreadyUsedSkill == true)
			{
				npc.setTarget(npc);
				npc.doCast(SkillData.getInstance().getInfo(4737, 3));
				_isAlreadyUsedSkill = false;
			}
			else
				return "";
		}
		return "";
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet, L2Skill skill)
	{
		int npcId = npc.getId();
		int maxHp = npc.getMaxHp();
		int nowHp = (int) npc.getStatus().getCurrentHp();
		
		if (npcId == OTIS)
		{
			if (nowHp < maxHp * 0.3)
			{
				if (_isAlreadyUsedSkill1 == false)
				{
					player.sendMessage("I will be with you, and to take care of you !");
					npc.setTarget(player);
					npc.doCast(SkillData.getInstance().getInfo(4175, 3));
					_isAlreadyUsedSkill1 = true;
				}
			}
			if (_isAlreadyUsedSkill == false)
			{
				startQuestTimer("time_to_skill", 30000, npc, player);
				_isAlreadyUsedSkill = true;
			}
		}
		return "";
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getId();
		
		if (npcId == OTIS)
			cancelQuestTimer("time_to_skill", npc, player);
		
		return "";
	}
	
	public static void main(String[] args)
	{
		new HatuOtis(-1, HatuOtis.class.getSimpleName(), "ai");
	}
}