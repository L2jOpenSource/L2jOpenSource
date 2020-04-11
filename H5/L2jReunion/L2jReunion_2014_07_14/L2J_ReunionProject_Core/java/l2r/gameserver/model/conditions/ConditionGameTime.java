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

import l2r.gameserver.GameTimeController;
import l2r.gameserver.model.stats.Env;

/**
 * The Class ConditionGameTime.
 * @author mkizub
 */
public class ConditionGameTime extends Condition
{
	/**
	 * The Enum CheckGameTime.
	 */
	public enum CheckGameTime
	{
		NIGHT
	}
	
	private final CheckGameTime _check;
	private final boolean _required;
	
	/**
	 * Instantiates a new condition game time.
	 * @param check the check
	 * @param required the required
	 */
	public ConditionGameTime(CheckGameTime check, boolean required)
	{
		_check = check;
		_required = required;
	}
	
	/**
	 * Test impl.
	 * @param env the env
	 * @return true, if successful
	 */
	@Override
	public boolean testImpl(Env env)
	{
		switch (_check)
		{
			case NIGHT:
				return GameTimeController.getInstance().isNight() == _required;
		}
		return !_required;
	}
}
