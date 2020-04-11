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
 * this program. If not, see <http://l2r.ru/>.
 */
package ai.group_template.extra;

import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.serverpackets.CreatureSay;

import ai.npc.AbstractNpcAI;

public class FieldOfWhispersSilence extends AbstractNpcAI
{
	private static final int BRAZIER_OF_PURITY = 18806;
	private static final int GUARDIAN_SPIRITS_OF_MAGIC_FORCE = 22659;
	
	public FieldOfWhispersSilence()
	{
		super(FieldOfWhispersSilence.class.getSimpleName(), "ai");
		
		addAggroRangeEnterId(BRAZIER_OF_PURITY);
		addAggroRangeEnterId(GUARDIAN_SPIRITS_OF_MAGIC_FORCE);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		switch (npc.getId())
		{
			case BRAZIER_OF_PURITY:
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), NpcStringId.THE_PURIFICATION_FIELD_IS_BEING_ATTACKED_GUARDIAN_SPIRITS_PROTECT_THE_MAGIC_FORCE));
				break;
			case GUARDIAN_SPIRITS_OF_MAGIC_FORCE:
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), NpcStringId.EVEN_THE_MAGIC_FORCE_BINDS_YOU_YOU_WILL_NEVER_BE_FORGIVEN));
				break;
		}
		
		return null;
	}
}