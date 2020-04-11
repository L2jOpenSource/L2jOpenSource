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
package com.l2jserver.gameserver.model.conditions;

import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.skills.AbnormalType;
import com.l2jserver.gameserver.model.skills.BuffInfo;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * Condition implementation to verify player's abnormal type and level.
 * @author Zoey76
 */
public class ConditionPlayerCheckAbnormal extends Condition
{
	private final AbnormalType _type;
	private final int _level;
	
	/**
	 * Instantiates a new condition player check abnormal.
	 * @param type the abnormal type
	 */
	public ConditionPlayerCheckAbnormal(AbnormalType type)
	{
		_type = type;
		_level = -1;
	}
	
	/**
	 * Instantiates a new condition player check abnormal.
	 * @param type the abnormal type
	 * @param level the abnormal level
	 */
	public ConditionPlayerCheckAbnormal(AbnormalType type, int level)
	{
		_type = type;
		_level = level;
	}
	
	@Override
	public boolean testImpl(L2Character effector, L2Character effected, Skill skill, L2Item item)
	{
		final BuffInfo info = effector.getEffectList().getBuffInfoByAbnormalType(_type);
		return ((info != null) && ((_level == -1) || (_level >= info.getSkill().getAbnormalLvl())));
	}
}
