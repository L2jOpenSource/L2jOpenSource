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

import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Stats;
import l2r.gameserver.model.stats.functions.AbstractFunction;

/**
 * @author UnAfraid
 */
public class FuncAtkAccuracy extends AbstractFunction
{
	private static final FuncAtkAccuracy _faa_instance = new FuncAtkAccuracy();
	
	public static AbstractFunction getInstance()
	{
		return _faa_instance;
	}
	
	private FuncAtkAccuracy()
	{
		super(Stats.ACCURACY_COMBAT, 1, null, 0, null);
	}
	
	@Override
	public void calc(Env env)
	{
		final int level = env.getCharacter().getLevel();
		// [Square(DEX)] * 6 + lvl + weapon hitbonus;
		env.addValue((Math.sqrt(env.getCharacter().getDEX()) * 6) + level);
		if (level > 77)
		{
			env.addValue(level - 76);
		}
		if (level > 69)
		{
			env.addValue(level - 69);
		}
	}
}