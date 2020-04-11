package com.l2jfrozen.gameserver.skills.conditions;

import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author mkizub
 */
public class ConditionLogicNot extends Condition
{
	private final Condition condition;
	
	public ConditionLogicNot(final Condition condition)
	{
		this.condition = condition;
		if (getListener() != null)
		{
			this.condition.setListener(this);
		}
	}
	
	@Override
	void setListener(final ConditionListener listener)
	{
		if (listener != null)
		{
			condition.setListener(this);
		}
		else
		{
			condition.setListener(null);
		}
		super.setListener(listener);
	}
	
	@Override
	public boolean testImpl(final Env env)
	{
		return !condition.test(env);
	}
}
