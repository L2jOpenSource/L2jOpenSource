package com.l2jfrozen.gameserver.skills.conditions;

import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author mkizub
 */
public final class ConditionUsingSkill extends Condition
{
	private final int skillId;
	
	public ConditionUsingSkill(final int skillId)
	{
		this.skillId = skillId;
	}
	
	@Override
	public boolean testImpl(final Env env)
	{
		if (env.skill == null)
		{
			return false;
		}
		return env.skill.getId() == skillId;
	}
}
