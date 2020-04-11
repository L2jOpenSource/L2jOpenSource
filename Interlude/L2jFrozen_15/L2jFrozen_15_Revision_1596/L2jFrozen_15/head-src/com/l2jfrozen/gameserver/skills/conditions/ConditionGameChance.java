package com.l2jfrozen.gameserver.skills.conditions;

import com.l2jfrozen.gameserver.skills.Env;
import com.l2jfrozen.util.random.Rnd;

/**
 * @author Advi
 */
public class ConditionGameChance extends Condition
{
	private final int chance;
	
	public ConditionGameChance(final int chance)
	{
		this.chance = chance;
	}
	
	@Override
	public boolean testImpl(final Env env)
	{
		return Rnd.get(100) < chance;
	}
}
