package com.l2jfrozen.gameserver.skills.conditions;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.skills.Env;
import com.l2jfrozen.gameserver.skills.Stats;

/**
 * The Class ConditionPlayerWeight.
 * @author Kerberos
 */

public class ConditionTargetWeight extends Condition
{
	private final int weight;
	
	/**
	 * Instantiates a new condition player weight.
	 * @param weight the weight
	 */
	public ConditionTargetWeight(final int weight)
	{
		this.weight = weight;
	}
	
	@Override
	public boolean testImpl(final Env env)
	{
		final L2Character targetObj = env.getTarget();
		if ((targetObj != null) && targetObj.isPlayer())
		{
			final L2PcInstance target = targetObj.getActingPlayer();
			if (!target.getDietMode() && (target.getMaxLoad() > 0))
			{
				final int weightproc = (int) (((target.getCurrentLoad() - target.calcStat(Stats.WEIGHT_PENALTY, 1, target, null)) * 100) / target.getMaxLoad());
				return (weightproc < weight);
			}
		}
		return false;
	}
}