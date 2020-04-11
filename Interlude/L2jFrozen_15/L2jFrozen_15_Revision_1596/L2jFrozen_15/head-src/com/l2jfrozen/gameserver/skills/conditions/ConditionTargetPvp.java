package com.l2jfrozen.gameserver.skills.conditions;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author eX1steam L2JFrozen
 */
public class ConditionTargetPvp extends Condition
{
	private final int pvp;
	
	public ConditionTargetPvp(final int pvp)
	{
		this.pvp = pvp;
	}
	
	@Override
	public boolean testImpl(final Env env)
	{
		final L2Character target = env.target;
		if (target instanceof L2PcInstance && ((L2PcInstance) target).getPvpFlag() != 0)
		{
			return ((L2PcInstance) target).getPvpFlag() == pvp;
		}
		return false;
	}
}
