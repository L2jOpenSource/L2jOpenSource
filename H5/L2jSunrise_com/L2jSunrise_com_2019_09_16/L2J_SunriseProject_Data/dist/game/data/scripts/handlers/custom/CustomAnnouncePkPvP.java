/*
 * Copyright (C) 2004-2015 L2J DataPack
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
package handlers.custom;

import l2r.Config;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.events.Containers;
import l2r.gameserver.model.events.EventType;
import l2r.gameserver.model.events.impl.character.player.OnPlayerPvPKill;
import l2r.gameserver.model.events.listeners.ConsumerEventListener;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Broadcast;

public class CustomAnnouncePkPvP
{
	public CustomAnnouncePkPvP()
	{
		if (Config.ANNOUNCE_PK_PVP)
		{
			Containers.Players().addListener(new ConsumerEventListener(Containers.Players(), EventType.ON_PLAYER_PVP_KILL, (OnPlayerPvPKill event) -> OnPlayerPvPKill(event), this));
		}
	}
	
	/**
	 * @param event
	 * @return
	 */
	private Object OnPlayerPvPKill(OnPlayerPvPKill event)
	{
		L2PcInstance pk = event.getActiveChar();
		if (pk.isGM())
		{
			return null;
		}
		L2PcInstance player = event.getTarget();
		
		String msg = Config.ANNOUNCE_PVP_MSG;
		if (player.getPvpFlag() == 0)
		{
			msg = Config.ANNOUNCE_PK_MSG;
		}
		msg = msg.replace("$killer", pk.getName()).replace("$target", player.getName());
		if (Config.ANNOUNCE_PK_PVP_NORMAL_MESSAGE)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1);
			sm.addString(msg);
			Broadcast.toAllOnlinePlayers(sm);
		}
		else
		{
			Broadcast.toAllOnlinePlayers(msg, false);
		}
		return null;
	}
}