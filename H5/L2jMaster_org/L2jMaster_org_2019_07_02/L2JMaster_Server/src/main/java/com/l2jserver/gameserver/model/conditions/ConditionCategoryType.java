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

import java.util.Set;

import com.l2jserver.gameserver.enums.CategoryType;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * Condition Category Type implementation.
 * @author Adry_85
 */
public class ConditionCategoryType extends Condition
{
	private final Set<CategoryType> _categoryTypes;
	
	public ConditionCategoryType(Set<CategoryType> categoryTypes)
	{
		_categoryTypes = categoryTypes;
	}
	
	@Override
	public boolean testImpl(L2Character effector, L2Character effected, Skill skill, L2Item item)
	{
		for (CategoryType type : _categoryTypes)
		{
			if (effector.isInCategory(type))
			{
				return true;
			}
		}
		return false;
	}
}
