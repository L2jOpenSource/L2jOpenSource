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

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.stats.Env;

/**
 * The Class ConditionPlayerBaseStats.
 * @author mkizub
 */
public class ConditionPlayerBaseStats extends Condition
{
	
	private final BaseStat _stat;
	private final int _value;
	
	/**
	 * Instantiates a new condition player base stats.
	 * @param player the player
	 * @param stat the stat
	 * @param value the value
	 */
	public ConditionPlayerBaseStats(L2Character player, BaseStat stat, int value)
	{
		super();
		_stat = stat;
		_value = value;
	}
	
	/**
	 * Test impl.
	 * @param env the env
	 * @return true, if successful
	 */
	@Override
	public boolean testImpl(Env env)
	{
		if (env.getPlayer() == null)
		{
			return false;
		}
		final L2PcInstance player = env.getPlayer();
		switch (_stat)
		{
			case Int:
				return player.getINT() >= _value;
			case Str:
				return player.getSTR() >= _value;
			case Con:
				return player.getCON() >= _value;
			case Dex:
				return player.getDEX() >= _value;
			case Men:
				return player.getMEN() >= _value;
			case Wit:
				return player.getWIT() >= _value;
		}
		return false;
	}
}

enum BaseStat
{
	Int,
	Str,
	Con,
	Dex,
	Men,
	Wit
}
