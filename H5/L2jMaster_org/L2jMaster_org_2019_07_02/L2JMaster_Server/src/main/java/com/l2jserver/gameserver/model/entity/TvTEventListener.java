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
package com.l2jserver.gameserver.model.entity;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.interfaces.IEventListener;

/**
 * @author UnAfraid
 */
public final class TvTEventListener implements IEventListener
{
	private final L2PcInstance _player;
	
	protected TvTEventListener(L2PcInstance player)
	{
		_player = player;
	}
	
	@Override
	public boolean isOnEvent()
	{
		return TvTEvent.isStarted() && TvTEvent.isPlayerParticipant(getPlayer().getObjectId());
	}
	
	@Override
	public boolean isBlockingExit()
	{
		return true;
	}
	
	@Override
	public boolean isBlockingDeathPenalty()
	{
		return true;
	}
	
	@Override
	public boolean canRevive()
	{
		return false;
	}
	
	@Override
	public L2PcInstance getPlayer()
	{
		return _player;
	}
}
