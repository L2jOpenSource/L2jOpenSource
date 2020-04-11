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
import l2r.gameserver.model.stats.BaseStats;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Stats;

/**
 * @author UnAfraid
 */
public class FuncPAtkMod extends Func
{
	private static final FuncPAtkMod _fpa_instance = new FuncPAtkMod();
	
	public static Func getInstance()
	{
		return _fpa_instance;
	}
	
	private FuncPAtkMod()
	{
		super(Stats.POWER_ATTACK, 0x30, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		if (env.getCharacter().isPlayer())
		{
			env.mulValue(BaseStats.STR.calcBonus(env.getPlayer()) * env.getPlayer().getLevelMod());
		}
		else
		{
			env.mulValue(BaseStats.STR.calcBonus(env.getCharacter()) * env.getCharacter().getLevelMod());
		}
	}
}