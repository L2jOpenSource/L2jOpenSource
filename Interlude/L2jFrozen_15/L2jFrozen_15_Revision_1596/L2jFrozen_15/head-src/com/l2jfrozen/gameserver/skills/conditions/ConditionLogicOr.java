package com.l2jfrozen.gameserver.skills.conditions;

import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author mkizub
 */
public class ConditionLogicOr extends Condition
{
	private static Condition[] emptyConditions = new Condition[0];
	public Condition[] conditions = emptyConditions;
	
	public void add(final Condition condition)
	{
		if (condition == null)
		{
			return;
		}
		if (getListener() != null)
		{
			condition.setListener(this);
		}
		final int len = conditions.length;
		final Condition[] tmp = new Condition[len + 1];
		System.arraycopy(conditions, 0, tmp, 0, len);
		tmp[len] = condition;
		conditions = tmp;
	}
	
	@Override
	void setListener(final ConditionListener listener)
	{
		if (listener != null)
		{
			for (final Condition c : conditions)
			{
				c.setListener(this);
			}
		}
		else
		{
			for (final Condition c : conditions)
			{
				c.setListener(null);
			}
		}
		super.setListener(listener);
	}
	
	@Override
	public boolean testImpl(final Env env)
	{
		for (final Condition c : conditions)
		{
			if (c.test(env))
			{
				return true;
			}
		}
		return false;
	}
	
}
