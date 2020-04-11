package com.l2jfrozen.gameserver.skills.conditions;

import java.util.List;

import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author nBd
 */
public class ConditionTargetRaceId extends Condition
{
	private final List<Integer> raceIds;
	
	public ConditionTargetRaceId(final List<Integer> raceId)
	{
		raceIds = raceId;
	}
	
	@Override
	public boolean testImpl(final Env env)
	{
		if (raceIds == null || env.target == null || !(env.target instanceof L2NpcInstance))
		{
			return false;
		}
		
		final L2NpcInstance target = (L2NpcInstance) env.target;
		if (target.getTemplate() != null && target.getTemplate().race != null)
		{
			return raceIds.contains(((L2NpcInstance) env.target).getTemplate().race.ordinal() + 1);
		}
		return false;
	}
}
