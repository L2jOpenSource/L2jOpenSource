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
package com.l2jserver.gameserver.model.events.impl.character;

import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.events.EventType;
import com.l2jserver.gameserver.model.events.impl.IBaseEvent;

/**
 * An instantly executed event when L2Character is attacked by L2Character.
 * @author UnAfraid
 */
public class OnCreatureAttack implements IBaseEvent
{
	private final L2Character _attacker;
	private final L2Character _target;
	
	public OnCreatureAttack(L2Character attacker, L2Character target)
	{
		_attacker = attacker;
		_target = target;
	}
	
	public final L2Character getAttacker()
	{
		return _attacker;
	}
	
	public final L2Character getTarget()
	{
		return _target;
	}
	
	@Override
	public EventType getType()
	{
		return EventType.ON_CREATURE_ATTACK;
	}
}