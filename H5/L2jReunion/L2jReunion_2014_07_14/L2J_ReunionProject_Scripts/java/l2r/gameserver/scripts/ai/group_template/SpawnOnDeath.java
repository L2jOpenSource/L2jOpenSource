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

import java.util.Map;

import javolution.util.FastMap;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.QuestEventType;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;
import l2r.util.Rnd;

public class SpawnOnDeath extends AbstractNpcAI
{
	private static final Map<Integer, Integer> MOBSPAWNS5 = new FastMap<>();
	private static final Map<Integer, Integer> MOBSPAWNS15 = new FastMap<>();
	private static final Map<Integer, Integer> MOBSPAWNS100 = new FastMap<>();
	
	public SpawnOnDeath(int questId, String name, String descr)
	{
		super(name, descr);
		int[] temp =
		{
			22703,
			22704,
			18812,
			18813,
			18814,
			22705,
			22707
		};
		registerMobs(temp, new QuestEventType[]
		{
			QuestEventType.ON_KILL
		});
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getId();
		L2Attackable newNpc = null;
		if (MOBSPAWNS15.containsKey(Integer.valueOf(npcId)))
		{
			if (Rnd.get(100) < 15)
			{
				newNpc = (L2Attackable) addSpawn(MOBSPAWNS15.get(Integer.valueOf(npcId)).intValue(), npc);
				newNpc.setRunning();
				newNpc.addDamageHate(killer, 0, 999);
				newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
			}
		}
		else if (MOBSPAWNS100.containsKey(Integer.valueOf(npcId)))
		{
			npc.deleteMe();
			newNpc = (L2Attackable) addSpawn(MOBSPAWNS100.get(Integer.valueOf(npcId)).intValue(), npc);
		}
		else if ((MOBSPAWNS5.containsKey(Integer.valueOf(npcId))) && (Rnd.get(100) < 5))
		{
			newNpc = (L2Attackable) addSpawn(MOBSPAWNS5.get(Integer.valueOf(npcId)).intValue(), npc);
			newNpc.setRunning();
			newNpc.addDamageHate(killer, 0, 999);
			newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	static
	{
		MOBSPAWNS5.put(Integer.valueOf(22705), Integer.valueOf(22707));
		MOBSPAWNS15.put(Integer.valueOf(22703), Integer.valueOf(22703));
		MOBSPAWNS15.put(Integer.valueOf(22704), Integer.valueOf(22704));
		MOBSPAWNS100.put(Integer.valueOf(18812), Integer.valueOf(18813));
		MOBSPAWNS100.put(Integer.valueOf(18813), Integer.valueOf(18814));
		MOBSPAWNS100.put(Integer.valueOf(18814), Integer.valueOf(18812));
	}
	
	public static void main(String[] args)
	{
		new SpawnOnDeath(-1, SpawnOnDeath.class.getSimpleName(), "ai");
	}
}