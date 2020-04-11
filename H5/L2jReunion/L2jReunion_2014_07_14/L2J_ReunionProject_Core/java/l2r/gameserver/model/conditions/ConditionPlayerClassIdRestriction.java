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
package l2r.gameserver.model.conditions;

import java.util.ArrayList;

import l2r.gameserver.model.stats.Env;

/**
 * The Class ConditionPlayerClassIdRestriction.
 */
public class ConditionPlayerClassIdRestriction extends Condition
{
	private final ArrayList<Integer> _classIds;
	
	/**
	 * Instantiates a new condition player class id restriction.
	 * @param classId the class id
	 */
	public ConditionPlayerClassIdRestriction(ArrayList<Integer> classId)
	{
		_classIds = classId;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		return (env.getPlayer() != null) && (_classIds.contains(env.getPlayer().getClassId().getId()));
	}
}
