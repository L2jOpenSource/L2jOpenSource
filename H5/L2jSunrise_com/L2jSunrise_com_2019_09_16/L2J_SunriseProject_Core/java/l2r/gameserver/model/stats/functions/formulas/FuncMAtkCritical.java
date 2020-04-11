/*
 * Copyright (C) 2004-2015 L2J Server
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
package l2r.gameserver.model.stats.functions.formulas;

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.stats.BaseStats;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Stats;
import l2r.gameserver.model.stats.functions.AbstractFunction;

/**
 * @author UnAfraid
 */
public class FuncMAtkCritical extends AbstractFunction
{
	private static final FuncMAtkCritical _fac_instance = new FuncMAtkCritical();
	
	public static AbstractFunction getInstance()
	{
		return _fac_instance;
	}
	
	private FuncMAtkCritical()
	{
		super(Stats.MCRITICAL_RATE, 1, null, 0, null);
	}
	
	@Override
	public void calc(Env env)
	{
		L2Character p = env.getCharacter();
		// CT2: The magic critical rate has been increased to 10 times.
		if (p.isPlayer())
		{
			if (p.getActiveWeaponInstance() != null)
			{
				env.mulValue(BaseStats.WIT.calcBonus(p) * 10);
			}
		}
		else
		{
			env.mulValue(BaseStats.WIT.calcBonus(p) * 10);
		}
	}
}