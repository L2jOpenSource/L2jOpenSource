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
package com.l2jserver.gameserver.model.events.impl.events;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.entity.TvTEventTeam;
import com.l2jserver.gameserver.model.events.EventType;
import com.l2jserver.gameserver.model.events.impl.IBaseEvent;

/**
 * @author UnAfraid
 */
public class OnTvTEventKill implements IBaseEvent
{
	private final L2PcInstance _killer;
	private final L2PcInstance _victim;
	private final TvTEventTeam _killerTeam;
	
	public OnTvTEventKill(L2PcInstance killer, L2PcInstance victim, TvTEventTeam killerTeam)
	{
		_killer = killer;
		_victim = victim;
		_killerTeam = killerTeam;
	}
	
	public L2PcInstance getKiller()
	{
		return _killer;
	}
	
	public L2PcInstance getVictim()
	{
		return _victim;
	}
	
	public TvTEventTeam getKillerTeam()
	{
		return _killerTeam;
	}
	
	@Override
	public EventType getType()
	{
		return EventType.ON_TVT_EVENT_KILL;
	}
}
