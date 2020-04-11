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
package com.l2jserver.gameserver.instancemanager.achievements.conditions;

import com.l2jserver.gameserver.instancemanager.achievements.base.Condition;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.entity.Hero;

public class HeroCount extends Condition
{
	public HeroCount(Object value)
	{
		super(value);
		setName("Hero Count");
	}
	
	@Override
	public boolean meetConditionRequirements(L2PcInstance player)
	{
		if (getValue() == null)
		{
			return false;
		}
		
		int val = Integer.parseInt(getValue().toString());
		for (Integer integer : Hero.getInstance().getHeroes().keySet())
		{
			int hero = integer.intValue();
			if (hero == player.getObjectId())
			{
				StatsSet sts = Hero.getInstance().getHeroes().get(Integer.valueOf(hero));
				if (sts.getString("char_name").equals(player.getName()))
				{
					if (sts.getInt("count") >= val)
					{
						return true;
					}
				}
			}
		}
		
		return false;
	}
}