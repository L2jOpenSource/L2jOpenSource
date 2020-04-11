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
package com.l2jserver.gameserver.model.events.impl.sieges.fort;

import com.l2jserver.gameserver.model.entity.FortSiege;
import com.l2jserver.gameserver.model.events.EventType;
import com.l2jserver.gameserver.model.events.impl.IBaseEvent;

/**
 * @author UnAfraid
 */
public class OnFortSiegeFinish implements IBaseEvent
{
	private final FortSiege _siege;
	
	public OnFortSiegeFinish(FortSiege siege)
	{
		_siege = siege;
	}
	
	public FortSiege getSiege()
	{
		return _siege;
	}
	
	@Override
	public EventType getType()
	{
		return EventType.ON_FORT_SIEGE_FINISH;
	}
}
