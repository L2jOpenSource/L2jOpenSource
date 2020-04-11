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
package com.l2jserver.gameserver.model.events.impl.character.playable;

import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.events.EventType;
import com.l2jserver.gameserver.model.events.impl.IBaseEvent;

/**
 * @author UnAfraid
 */
public class OnPlayableExpChanged implements IBaseEvent
{
	private final L2Playable _activeChar;
	private final long _oldExp;
	private final long _newExp;
	
	public OnPlayableExpChanged(L2Playable activeChar, long oldExp, long newExp)
	{
		_activeChar = activeChar;
		_oldExp = oldExp;
		_newExp = newExp;
	}
	
	public L2Playable getActiveChar()
	{
		return _activeChar;
	}
	
	public long getOldExp()
	{
		return _oldExp;
	}
	
	public long getNewExp()
	{
		return _newExp;
	}
	
	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYABLE_EXP_CHANGED;
	}
}
