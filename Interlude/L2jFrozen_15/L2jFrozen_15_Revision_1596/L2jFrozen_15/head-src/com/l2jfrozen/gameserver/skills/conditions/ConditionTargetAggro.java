package com.l2jfrozen.gameserver.skills.conditions;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author mkizub
 */
public class ConditionTargetAggro extends Condition
{
	
	private final boolean isAggro;
	
	public ConditionTargetAggro(final boolean isAggro)
	{
		this.isAggro = isAggro;
	}
	
	@Override
	public boolean testImpl(final Env env)
	{
		final L2Character target = env.target;
		if (target instanceof L2MonsterInstance)
		{
			return ((L2MonsterInstance) target).isAggressive() == isAggro;
		}
		if (target instanceof L2PcInstance)
		{
			return ((L2PcInstance) target).getKarma() > 0;
		}
		return false;
	}
}
