/*
 * Copyright (C) 2004-2015 L2J Server
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
package l2r.gameserver.model.events.impl.character.player;

import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.events.EventType;
import l2r.gameserver.model.events.impl.IBaseEvent;

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
