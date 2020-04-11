/*
 * Copyright (C) 2004-2013 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.npc.Teleports.TeleportWithCharm;

import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import ai.npc.AbstractNpcAI;

/**
 * Charm teleport AI.<br>
 * Original Jython script by DraX.
 * @author Plim
 */
public class TeleportWithCharm extends AbstractNpcAI
{
	// NPCs
	private final static int WHIRPY = 30540;
	private final static int TAMIL = 30576;
	// Items
	private final static int ORC_GATEKEEPER_CHARM = 1658;
	private final static int DWARF_GATEKEEPER_TOKEN = 1659;
	
	public TeleportWithCharm()
	{
		super(TeleportWithCharm.class.getSimpleName(), "ai/npc/Teleports");
		addStartNpc(WHIRPY, TAMIL);
		addTalkId(WHIRPY, TAMIL);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		switch (npc.getId())
		{
			case WHIRPY:
			{
				if (hasQuestItems(player, DWARF_GATEKEEPER_TOKEN))
				{
					takeItems(player, DWARF_GATEKEEPER_TOKEN, 1);
					player.teleToLocation(-80826, 149775, -3043);
				}
				else
				{
					return "30540-01.htm";
				}
				break;
			}
			case TAMIL:
			{
				if (hasQuestItems(player, ORC_GATEKEEPER_CHARM))
				{
					takeItems(player, ORC_GATEKEEPER_CHARM, 1);
					player.teleToLocation(-80826, 149775, -3043);
				}
				else
				{
					return "30576-01.htm";
				}
				break;
			}
		}
		return super.onTalk(npc, player);
	}
}
