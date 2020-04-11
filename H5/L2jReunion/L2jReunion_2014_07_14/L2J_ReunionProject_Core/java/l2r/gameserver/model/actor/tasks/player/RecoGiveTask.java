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

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.UserInfo;

/**
 * Task dedicated to increase player's recommendation bonus.
 * @author UnAfraid
 */
public class RecoGiveTask implements Runnable
{
	private final L2PcInstance _player;
	
	public RecoGiveTask(L2PcInstance player)
	{
		_player = player;
	}
	
	@Override
	public void run()
	{
		if (_player != null)
		{
			// 10 recommendations to give out after 2 hours of being logged in
			// 1 more recommendation to give out every hour after that.
			int recoToGive = 1;
			if (!_player.isRecoTwoHoursGiven())
			{
				recoToGive = 10;
				_player.setRecoTwoHoursGiven(true);
			}
			
			_player.setRecomLeft(_player.getRecomLeft() + recoToGive);
			
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_OBTAINED_S1_RECOMMENDATIONS);
			sm.addInt(recoToGive);
			_player.sendPacket(sm);
			_player.sendPacket(new UserInfo(_player));
		}
	}
}
