package com.l2jfrozen.gameserver.skills.conditions;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.base.Race;
import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author mkizub
 */
public class ConditionPlayerRace extends Condition
{
	private final Race race;
	
	public ConditionPlayerRace(final Race race)
	{
		this.race = race;
	}
	
	@Override
	public boolean testImpl(final Env env)
	{
		if (!(env.player instanceof L2PcInstance))
		{
			return false;
		}
		return ((L2PcInstance) env.player).getRace() == race;
	}
}
