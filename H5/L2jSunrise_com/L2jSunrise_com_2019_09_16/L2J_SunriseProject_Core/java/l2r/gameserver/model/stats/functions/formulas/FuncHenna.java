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

import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Stats;
import l2r.gameserver.model.stats.functions.AbstractFunction;

/**
 * @author UnAfraid
 */
public class FuncHenna extends AbstractFunction
{
	private static final Map<Stats, FuncHenna> _fh_instance = new HashMap<>();
	
	public static AbstractFunction getInstance(Stats st)
	{
		if (!_fh_instance.containsKey(st))
		{
			_fh_instance.put(st, new FuncHenna(st));
		}
		return _fh_instance.get(st);
	}
	
	private FuncHenna(Stats stat)
	{
		super(stat, 1, null, 0, null);
	}
	
	@Override
	public void calc(Env env)
	{
		L2PcInstance pc = env.getPlayer();
		if (pc != null)
		{
			switch (getStat())
			{
				case STAT_STR:
					env.addValue(pc.getHennaEx().getHennaStatSTR());
					break;
				case STAT_CON:
					env.addValue(pc.getHennaEx().getHennaStatCON());
					break;
				case STAT_DEX:
					env.addValue(pc.getHennaEx().getHennaStatDEX());
					break;
				case STAT_INT:
					env.addValue(pc.getHennaEx().getHennaStatINT());
					break;
				case STAT_WIT:
					env.addValue(pc.getHennaEx().getHennaStatWIT());
					break;
				case STAT_MEN:
					env.addValue(pc.getHennaEx().getHennaStatMEN());
					break;
			}
		}
	}
}