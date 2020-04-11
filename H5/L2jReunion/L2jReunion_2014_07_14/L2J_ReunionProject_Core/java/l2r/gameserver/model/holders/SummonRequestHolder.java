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

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.L2Skill;

/**
 * @author UnAfraid
 */
public class SummonRequestHolder
{
	private final L2PcInstance _target;
	private final L2Skill _skill;
	
	public SummonRequestHolder(L2PcInstance destination, L2Skill skill)
	{
		_target = destination;
		_skill = skill;
	}
	
	public L2PcInstance getTarget()
	{
		return _target;
	}
	
	public L2Skill getSkill()
	{
		return _skill;
	}
}
