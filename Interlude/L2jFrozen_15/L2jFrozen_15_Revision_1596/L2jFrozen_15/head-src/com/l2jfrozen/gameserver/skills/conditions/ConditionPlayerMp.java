package com.l2jfrozen.gameserver.skills.conditions;

import com.l2jfrozen.gameserver.skills.Env;

public class ConditionPlayerMp extends Condition
{
	
	private final int mp;
	
	public ConditionPlayerMp(final int mp)
	{
		this.mp = mp;
	}
	
	@Override
	public boolean testImpl(final Env env)
	{
		return env.player.getCurrentMp() * 100 / env.player.getMaxMp() <= mp;
	}
}
