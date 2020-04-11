/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.model.actor.tasks.player;

import com.l2jserver.Config;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.stat.PcStat;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.network.serverpackets.ExVitalityPointInfo;

/**
 * Task dedicated to reward player with vitality.
 * @author UnAfraid
 */
public class VitalityTask implements Runnable
{
	private final L2PcInstance _player;
	
	public VitalityTask(L2PcInstance player)
	{
		_player = player;
	}
	
	@Override
	public void run()
	{
		if (!_player.isInsideZone(ZoneId.PEACE))
		{
			return;
		}
		
		if (_player.getVitalityPoints() >= PcStat.MAX_VITALITY_POINTS)
		{
			return;
		}
		
		_player.updateVitalityPoints(Config.RATE_RECOVERY_VITALITY_PEACE_ZONE, false, false);
		_player.sendPacket(new ExVitalityPointInfo(_player.getVitalityPoints()));
	}
}
