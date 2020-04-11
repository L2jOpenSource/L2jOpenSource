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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ConditionPlayerGrade.
 * @author Gigiikun
 */
public final class ConditionPlayerGrade extends Condition
{
	protected static final Logger _log = LoggerFactory.getLogger(ConditionPlayerGrade.class);
	// conditional values
	public static final int COND_NO_GRADE = 0x0001;
	public static final int COND_D_GRADE = 0x0002;
	public static final int COND_C_GRADE = 0x0004;
	public static final int COND_B_GRADE = 0x0008;
	public static final int COND_A_GRADE = 0x0010;
	public static final int COND_S_GRADE = 0x0020;
	public static final int COND_S80_GRADE = 0x0040;
	public static final int COND_S84_GRADE = 0x0080;
	
	private final int _value;
	
	/**
	 * Instantiates a new condition player grade.
	 * @param value the value
	 */
	public ConditionPlayerGrade(int value)
	{
		_value = value;
	}
	
	/**
	 * 
	 */
	@Override
	public boolean testImpl(Env env)
	{
		return (env.getPlayer() != null) && (_value == (byte) env.getPlayer().getExpertiseLevel());
	}
}
