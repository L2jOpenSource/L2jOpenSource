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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.util.Rnd;

import ai.npc.AbstractNpcAI;

public class CryptsOfDisgrace extends AbstractNpcAI
{
	private static final Map<Integer, Integer> MOBSPAWNS5 = new ConcurrentHashMap<>();
	private static final Map<Integer, Integer> MOBSPAWNS15 = new ConcurrentHashMap<>();
	
	//@formatter:off
	public static final int[] MOBS =
	{
		22703, 22704, 22705, 22706, 22707
	};
	
	static
	{
		MOBSPAWNS5.put(Integer.valueOf(22705), Integer.valueOf(22707));
		MOBSPAWNS15.put(Integer.valueOf(22703), Integer.valueOf(22703));
		MOBSPAWNS15.put(Integer.valueOf(22704), Integer.valueOf(22704));
	}
	
	private static final int[][] MobSpawns =
	{
		{ 18464, -28681, 255110, -2160, 10 },
		{ 18464, -26114, 254708, -2139, 10 },
		{ 18463, -28457, 256584, -1926, 10 },
		{ 18463, -26482, 257663, -1925, 10 },
		{ 18464, -26453, 256745, -1930, 10 },
		{ 18463, -27362, 256282, -1935, 10 },
		{ 18464, -25441, 256441, -2147, 10 }
	};
	//@formatter:on
	
	public CryptsOfDisgrace()
	{
		super(CryptsOfDisgrace.class.getSimpleName(), "ai");
		
		addKillId(MOBS);
		
		for (int[] loc : MobSpawns)
		{
			addSpawn(loc[0], loc[1], loc[2], loc[3], loc[4]);
		}
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getId();
		L2Attackable newNpc = null;
		if (MOBSPAWNS15.containsKey(Integer.valueOf(npcId)))
		{
			if (Rnd.get(100) < 15)
			{
				newNpc = (L2Attackable) addSpawn(MOBSPAWNS15.get(Integer.valueOf(npcId)).intValue(), npc);
				newNpc.setRunning();
				newNpc.addDamageHate(player, 0, 999);
				newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
		}
		else if ((MOBSPAWNS5.containsKey(Integer.valueOf(npcId))) && (Rnd.get(100) < 5))
		{
			newNpc = (L2Attackable) addSpawn(MOBSPAWNS5.get(Integer.valueOf(npcId)).intValue(), npc);
			newNpc.setRunning();
			newNpc.addDamageHate(player, 0, 999);
			newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}
		return super.onKill(npc, player, isPet);
	}
}