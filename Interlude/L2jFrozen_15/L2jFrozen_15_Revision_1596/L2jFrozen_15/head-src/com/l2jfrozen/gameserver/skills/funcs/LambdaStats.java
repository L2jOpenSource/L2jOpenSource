package com.l2jfrozen.gameserver.skills.funcs;

import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author mkizub
 */
public final class LambdaStats extends Lambda
{
	
	public enum StatsType
	{
		PLAYER_LEVEL,
		TARGET_LEVEL,
		PLAYER_MAX_HP,
		PLAYER_MAX_MP
	}
	
	private final StatsType stat;
	
	public LambdaStats(final StatsType stat)
	{
		this.stat = stat;
	}
	
	@Override
	public double calc(final Env env)
	{
		switch (stat)
		{
			case PLAYER_LEVEL:
				if (env.player == null)
				{
					return 1;
				}
				return env.player.getLevel();
			case TARGET_LEVEL:
				if (env.target == null)
				{
					return 1;
				}
				return env.target.getLevel();
			case PLAYER_MAX_HP:
				if (env.player == null)
				{
					return 1;
				}
				return env.player.getMaxHp();
			case PLAYER_MAX_MP:
				if (env.player == null)
				{
					return 1;
				}
				return env.player.getMaxMp();
		}
		return 0;
	}
	
}
