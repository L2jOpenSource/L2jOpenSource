package com.l2jfrozen.gameserver.skills.conditions;

import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author mkizub
 */
public class ConditionPlayerLevel extends Condition
{
	private final int level;
	
	public ConditionPlayerLevel(final int level)
	{
		this.level = level;
	}
	
	@Override
	public boolean testImpl(final Env env)
	{
		return env.player.getLevel() >= level;
	}
}
