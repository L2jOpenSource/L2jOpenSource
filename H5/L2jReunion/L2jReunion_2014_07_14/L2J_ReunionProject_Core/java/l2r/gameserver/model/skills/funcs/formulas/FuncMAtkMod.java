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
public class FuncMAtkMod extends Func
{
	private static final FuncMAtkMod _fma_instance = new FuncMAtkMod();
	
	public static Func getInstance()
	{
		return _fma_instance;
	}
	
	private FuncMAtkMod()
	{
		super(Stats.MAGIC_ATTACK, 0x20, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		// Level Modifier^2 * INT Modifier^2
		double lvlMod = env.getCharacter().isPlayer() ? BaseStats.INT.calcBonus(env.getPlayer()) : BaseStats.INT.calcBonus(env.getCharacter());
		double intMod = env.getCharacter().isPlayer() ? env.getPlayer().getLevelMod() : env.getCharacter().getLevelMod();
		env.mulValue(Math.pow(lvlMod, 2) * Math.pow(intMod, 2));
	}
}