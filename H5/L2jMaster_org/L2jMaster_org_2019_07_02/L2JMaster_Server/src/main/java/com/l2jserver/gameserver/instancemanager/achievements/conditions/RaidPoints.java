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

import com.l2jserver.gameserver.instancemanager.RaidBossPointsManager;
import com.l2jserver.gameserver.instancemanager.achievements.base.Condition;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

public class RaidPoints extends Condition
{
	public RaidPoints(Object value)
	{
		super(value);
		setName("Raid Points");
	}
	
	@Override
	public boolean meetConditionRequirements(L2PcInstance player)
	{
		if (getValue() == null)
		{
			return false;
		}
		
		int val = Integer.parseInt(getValue().toString());
		RaidBossPointsManager.getInstance();
		if (RaidBossPointsManager.getPointsByOwnerId(player.getObjectId()) >= val)
		{
			return true;
		}
		
		return false;
	}
}