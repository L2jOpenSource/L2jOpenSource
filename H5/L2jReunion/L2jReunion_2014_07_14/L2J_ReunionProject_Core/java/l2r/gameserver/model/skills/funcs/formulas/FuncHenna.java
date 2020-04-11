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

import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.funcs.Func;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Stats;

/**
 * @author UnAfraid
 */
public class FuncHenna extends Func
{
	private static final Map<Stats, FuncHenna> _fh_instance = new HashMap<>();
	
	public static Func getInstance(Stats st)
	{
		if (!_fh_instance.containsKey(st))
		{
			_fh_instance.put(st, new FuncHenna(st));
		}
		return _fh_instance.get(st);
	}
	
	private FuncHenna(Stats stat)
	{
		super(stat, 0x10, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		L2PcInstance pc = env.getPlayer();
		if (pc != null)
		{
			switch (stat)
			{
				case STAT_STR:
					env.addValue(pc.getHennaStatSTR());
					break;
				case STAT_CON:
					env.addValue(pc.getHennaStatCON());
					break;
				case STAT_DEX:
					env.addValue(pc.getHennaStatDEX());
					break;
				case STAT_INT:
					env.addValue(pc.getHennaStatINT());
					break;
				case STAT_WIT:
					env.addValue(pc.getHennaStatWIT());
					break;
				case STAT_MEN:
					env.addValue(pc.getHennaStatMEN());
					break;
			}
		}
	}
}