package com.l2jfrozen.gameserver.skills.conditions;

import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author mkizub
 */
public abstract class ConditionInventory extends Condition
{
	protected final int slot;
	
	public ConditionInventory(final int slot)
	{
		this.slot = slot;
	}
	
	@Override
	public abstract boolean testImpl(Env env);
}
