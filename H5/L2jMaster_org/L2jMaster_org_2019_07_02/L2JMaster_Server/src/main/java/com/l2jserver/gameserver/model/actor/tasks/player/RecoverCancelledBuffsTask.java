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

import java.util.List;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.skills.BuffInfo;
import com.l2jserver.gameserver.model.skills.EffectScope;
import com.l2jserver.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * @author nuLL
 */
public class RecoverCancelledBuffsTask implements Runnable
{
	private final List<BuffInfo> _buffs;
	private final L2PcInstance _player;
	
	public RecoverCancelledBuffsTask(List<BuffInfo> buffs, L2PcInstance player)
	{
		_player = player;
		_buffs = buffs;
	}
	
	@Override
	public void run()
	{
		for (BuffInfo buff : _buffs)
		{
			final BuffInfo stolenOrCanceled = new BuffInfo(_player, _player, buff.getSkill());
			stolenOrCanceled.setAbnormalTime(buff.getTime());
			buff.getSkill().applyEffectScope(EffectScope.GENERAL, stolenOrCanceled, true, true);
			
			if (stolenOrCanceled.getAbnormalTime() > 0)
			{
				_player.getEffectList().add(stolenOrCanceled);
			}
		}
		
		_player.sendPacket(new ExShowScreenMessage("Your cancelled buffs has been given back.", 2000));
	}
}