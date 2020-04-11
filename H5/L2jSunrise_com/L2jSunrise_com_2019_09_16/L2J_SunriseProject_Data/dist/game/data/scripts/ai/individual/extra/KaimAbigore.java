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

public class KaimAbigore extends AbstractNpcAI
{
	private static final int KAIM = 18566;
	private static final int GUARD = 18567;
	
	boolean _isAlreadyStarted = false;
	boolean _isAlreadySpawned = false;
	int _isLockSpawned = 0;
	
	public KaimAbigore()
	{
		super(KaimAbigore.class.getSimpleName(), "ai/individual/extra");
		addAttackId(KAIM);
		addKillId(KAIM);
		addKillId(GUARD);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		int x = player.getX();
		int y = player.getY();
		
		if (event.equalsIgnoreCase("time_to_skill"))
		{
			npc.setTarget(player);
			npc.doCast(SkillData.getInstance().getInfo(5260, 5));
			_isAlreadyStarted = false;
		}
		else if (event.equalsIgnoreCase("time_to_spawn"))
		{
			addSpawn(GUARD, x + 100, y + 50, npc.getZ(), 0, false, 0, false, npc.getInstanceId());
			addSpawn(GUARD, x - 100, y - 50, npc.getZ(), 0, false, 0, false, npc.getInstanceId());
			addSpawn(GUARD, x, y - 80, npc.getZ(), 0, false, 0, false, npc.getInstanceId());
			_isAlreadySpawned = false;
			_isLockSpawned = 3;
		}
		return "";
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet, L2Skill skill)
	{
		int npcId = npc.getId();
		if (npcId == KAIM)
		{
			if (_isAlreadyStarted == false)
			{
				startQuestTimer("time_to_skill", 45000, npc, player);
				_isAlreadyStarted = true;
			}
			else if (_isAlreadyStarted == true)
			{
				return "";
			}
			if (_isAlreadySpawned == false)
			{
				if (_isLockSpawned == 0)
				{
					startQuestTimer("time_to_spawn", 60000, npc, player);
					_isAlreadySpawned = true;
				}
				if (_isLockSpawned == 3)
				{
					return "";
				}
			}
			else if (_isAlreadySpawned == true)
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
		if (npcId == GUARD)
		{
			_isLockSpawned = 1;
		}
		else if (npcId == KAIM)
		{
			cancelQuestTimer("time_to_spawn", npc, player);
			cancelQuestTimer("time_to_skill", npc, player);
		}
		return "";
	}
}