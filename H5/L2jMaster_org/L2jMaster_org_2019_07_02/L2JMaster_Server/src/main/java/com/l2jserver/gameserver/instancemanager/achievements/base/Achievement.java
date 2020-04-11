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
package com.l2jserver.gameserver.instancemanager.achievements.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

public class Achievement
{
	private static Logger _log = Logger.getLogger(Achievement.class.getName());
	private final int _id;
	private final String _name;
	private final String _reward;
	private String _description = "No Description!";
	private final boolean _repeatable;
	private final HashMap<Integer, Long> _rewardList;
	private final ArrayList<Condition> _conditions;
	
	@SuppressWarnings(
	{
		"rawtypes",
		"unchecked"
	})
	public Achievement(int id, String name, String description, String reward, boolean repeatable, ArrayList<Condition> conditions)
	{
		this._rewardList = new HashMap();
		this._id = id;
		this._name = name;
		this._description = description;
		this._reward = reward;
		this._conditions = conditions;
		this._repeatable = repeatable;
		
		createRewardList();
	}
	
	private void createRewardList()
	{
		for (String s : this._reward.split(";"))
		{
			if ((s != null) && (!s.isEmpty()))
			{
				String[] split = s.split(",");
				Integer item = Integer.valueOf(0);
				Long count = new Long(0L);
				try
				{
					item = Integer.valueOf(split[0]);
					count = Long.valueOf(split[1]);
				}
				catch (NumberFormatException nfe)
				{
					_log.warning("AchievementsEngine: Error wrong reward " + nfe);
				}
				this._rewardList.put(item, count);
			}
		}
	}
	
	public boolean meetAchievementRequirements(L2PcInstance player)
	{
		for (Condition c : getConditions())
		{
			if (!c.meetConditionRequirements(player))
			{
				return false;
			}
		}
		
		return true;
	}
	
	public int getID()
	{
		return this._id;
	}
	
	public String getName()
	{
		return this._name;
	}
	
	public String getDescription()
	{
		return this._description;
	}
	
	public String getReward()
	{
		return this._reward;
	}
	
	public boolean isRepeatable()
	{
		return this._repeatable;
	}
	
	public HashMap<Integer, Long> getRewardList()
	{
		return this._rewardList;
	}
	
	public ArrayList<Condition> getConditions()
	{
		return this._conditions;
	}
}