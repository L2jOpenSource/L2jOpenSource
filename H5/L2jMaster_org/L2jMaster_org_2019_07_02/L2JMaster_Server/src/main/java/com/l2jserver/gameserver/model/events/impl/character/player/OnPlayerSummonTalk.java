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
package com.l2jserver.gameserver.model.events.impl.character.player;

import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.events.EventType;
import com.l2jserver.gameserver.model.events.impl.IBaseEvent;

/**
 * @author St3eT
 */
public class OnPlayerSummonTalk implements IBaseEvent
{
	private final L2Summon _summon;
	
	public OnPlayerSummonTalk(L2Summon summon)
	{
		_summon = summon;
	}
	
	public L2Summon getSummon()
	{
		return _summon;
	}
	
	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_SUMMON_TALK;
	}
}
