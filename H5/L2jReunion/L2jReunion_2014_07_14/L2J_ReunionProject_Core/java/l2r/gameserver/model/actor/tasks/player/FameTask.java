/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.actor.tasks.player;

import l2r.Config;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.UserInfo;

/**
 * Task dedicated to reward player with fame while standing on siege zone.
 * @author UnAfraid
 */
public class FameTask implements Runnable
{
	private final L2PcInstance _player;
	private final int _value;
	
	public FameTask(L2PcInstance player, int value)
	{
		_player = player;
		_value = value;
	}
	
	@Override
	public void run()
	{
		if ((_player == null) || (_player.isDead() && !Config.FAME_FOR_DEAD_PLAYERS))
		{
			return;
		}
		if (((_player.getClient() == null) || _player.getClient().isDetached()) && !Config.OFFLINE_FAME)
		{
			return;
		}
		_player.setFame(_player.getFame() + _value);
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_REPUTATION_SCORE);
		sm.addInt(_value);
		_player.sendPacket(sm);
		_player.sendPacket(new UserInfo(_player));
	}
}
