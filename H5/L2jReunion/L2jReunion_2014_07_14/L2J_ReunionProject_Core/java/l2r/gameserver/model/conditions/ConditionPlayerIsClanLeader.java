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

import l2r.gameserver.model.stats.Env;

/**
 * The Class ConditionPlayerIsClanLeader.
 */
public class ConditionPlayerIsClanLeader extends Condition
{
	private final boolean _val;
	
	/**
	 * Instantiates a new condition player is clan leader.
	 * @param val the val
	 */
	public ConditionPlayerIsClanLeader(boolean val)
	{
		_val = val;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (env.getPlayer() == null)
		{
			return false;
		}
		return (env.getPlayer().isClanLeader() == _val);
	}
}
