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

import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.QuestState;

import ai.npc.AbstractNpcAI;

public class Aenkinel extends AbstractNpcAI
{
	private final int GK1 = 32658;
	private final int GK2 = 32659;
	private final int GK3 = 32660;
	private final int GK4 = 32661;
	private final int GK5 = 32662;
	private final int GK6 = 32663;
	private final int AENKINEL1 = 25690;
	private final int AENKINEL2 = 25691;
	private final int AENKINEL3 = 25692;
	private final int AENKINEL4 = 25693;
	private final int AENKINEL5 = 25694;
	private final int AENKINEL6 = 25695;
	
	public Aenkinel()
	{
		super(Aenkinel.class.getSimpleName(), "ai/individual/extra");
		addStartNpc(GK1);
		addKillId(AENKINEL1);
		addStartNpc(GK2);
		addKillId(AENKINEL2);
		addStartNpc(GK3);
		addKillId(AENKINEL3);
		addStartNpc(GK4);
		addKillId(AENKINEL4);
		addStartNpc(GK5);
		addKillId(AENKINEL5);
		addStartNpc(GK6);
		addKillId(AENKINEL6);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState("aenkinel");
		int npcId = npc.getId();
		if ((npcId == AENKINEL1) || (npcId == AENKINEL2) || (npcId == AENKINEL3) || (npcId == AENKINEL4) || (npcId == AENKINEL5) || (npcId == AENKINEL6))
		{
			int instanceId = npc.getInstanceId();
			addSpawn(18820, -121524, -155073, -6752, 64792, false, 0, false, instanceId);
			addSpawn(18819, -121486, -155070, -6752, 57739, false, 0, false, instanceId);
			addSpawn(18819, -121457, -155071, -6752, 49471, false, 0, false, instanceId);
			addSpawn(18819, -121428, -155070, -6752, 41113, false, 0, false, instanceId);
			if (st == null)
			{
				return "";
			}
			st.exitQuest(true);
		}
		return "";
	}
}