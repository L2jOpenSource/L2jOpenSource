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
package l2r.gameserver.model.holders;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author UnAfraid
 */
public class InvulSkillHolder extends SkillHolder
{
	private final AtomicInteger _instances = new AtomicInteger(1);
	
	public InvulSkillHolder(int skillId, int skillLevel)
	{
		super(skillId, skillLevel);
	}
	
	public InvulSkillHolder(SkillHolder holder)
	{
		super(holder.getSkill());
	}
	
	public int getInstances()
	{
		return _instances.get();
	}
	
	public int increaseInstances()
	{
		return _instances.incrementAndGet();
	}
	
	public int decreaseInstances()
	{
		return _instances.decrementAndGet();
	}
}
