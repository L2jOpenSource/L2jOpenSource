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
package l2r.gameserver.model.skills.funcs.formulas;

import l2r.gameserver.model.skills.funcs.Func;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Stats;

/**
 * @author UnAfraid
 */
public class FuncAtkEvasion extends Func
{
	private static final FuncAtkEvasion _fae_instance = new FuncAtkEvasion();
	
	public static Func getInstance()
	{
		return _fae_instance;
	}
	
	private FuncAtkEvasion()
	{
		super(Stats.EVASION_RATE, 0x10, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		final int level = env.getCharacter().getLevel();
		if (env.getCharacter().isPlayer())
		{
			// [Square(DEX)] * 6 + lvl;
			env.addValue((Math.sqrt(env.getCharacter().getDEX()) * 6) + level);
			if (level > 77)
			{
				env.addValue(level - 77);
			}
			if (level > 69)
			{
				env.addValue(level - 69);
			}
		}
		else
		{
			// [Square(DEX)] * 6 + lvl;
			env.addValue((Math.sqrt(env.getCharacter().getDEX()) * 6) + level);
			if (level > 69)
			{
				env.addValue((level - 69) + 2);
			}
		}
	}
}