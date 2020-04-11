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
package l2r.gameserver.model.skills.funcs;

import l2r.gameserver.model.stats.Env;

/**
 * @author mkizub
 */
public final class LambdaStats extends Lambda
{
	public enum StatsType
	{
		PLAYER_LEVEL,
		CUBIC_LEVEL,
		TARGET_LEVEL,
		PLAYER_MAX_HP,
		PLAYER_MAX_MP
	}
	
	private final StatsType _stat;
	
	public LambdaStats(StatsType stat)
	{
		_stat = stat;
	}
	
	@Override
	public double calc(Env env)
	{
		switch (_stat)
		{
			case PLAYER_LEVEL:
				if (env.getCharacter() == null)
				{
					return 1;
				}
				return env.getCharacter().getLevel();
			case CUBIC_LEVEL:
				if (env.getCubic() == null)
				{
					return 1;
				}
				return env.getCubic().getOwner().getLevel();
			case TARGET_LEVEL:
				if (env.getTarget() == null)
				{
					return 1;
				}
				return env.getTarget().getLevel();
			case PLAYER_MAX_HP:
				if (env.getCharacter() == null)
				{
					return 1;
				}
				return env.getCharacter().getMaxHp();
			case PLAYER_MAX_MP:
				if (env.getCharacter() == null)
				{
					return 1;
				}
				return env.getCharacter().getMaxMp();
		}
		return 0;
	}
}
