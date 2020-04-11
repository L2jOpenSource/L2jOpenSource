package com.l2jfrozen.gameserver.skills.conditions;

import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author mkizub
 */
public class ConditionTargetNone extends Condition
{
	public ConditionTargetNone()
	{
	}
	
	@Override
	public boolean testImpl(final Env env)
	{
		return env.target == null;
	}
}
