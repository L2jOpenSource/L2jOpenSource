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
package l2r.gameserver.scripting.scriptengine.events;

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.scripting.scriptengine.events.impl.L2Event;

/**
 * @author TheOne
 */
public class AttackEvent implements L2Event
{
	private L2Character attacker;
	private L2Character target;
	
	/**
	 * @return the attacker
	 */
	public L2Character getAttacker()
	{
		return attacker;
	}
	
	/**
	 * @param attacker the attacker to set
	 */
	public void setAttacker(L2Character attacker)
	{
		this.attacker = attacker;
	}
	
	/**
	 * @return the target
	 */
	public L2Character getTarget()
	{
		return target;
	}
	
	/**
	 * @param target the target to set
	 */
	public void setTarget(L2Character target)
	{
		this.target = target;
	}
}
