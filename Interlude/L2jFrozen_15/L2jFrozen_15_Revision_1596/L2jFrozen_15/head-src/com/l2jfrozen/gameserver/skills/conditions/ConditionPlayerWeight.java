package com.l2jfrozen.gameserver.skills.conditions;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.skills.Env;
import com.l2jfrozen.gameserver.skills.Stats;

/**
 * The Class ConditionPlayerWeight.
 * @author Kerberos
 */
public class ConditionPlayerWeight extends Condition
{
	private final int weight;
	
	/**
	 * Instantiates a new condition player weight.
	 * @param weight the weight
	 */
	public ConditionPlayerWeight(final int weight)
	{
		this.weight = weight;
	}
	
	@Override
	public boolean testImpl(final Env env)
	{
		final L2PcInstance player = env.getPlayer();
		if ((player != null) && (player.getMaxLoad() > 0))
		{
			final int weightproc = (int) (((player.getCurrentLoad() - player.calcStat(Stats.WEIGHT_PENALTY, 1, player, null)) * 100) / player.getMaxLoad());
			return (weightproc < weight) || player.getDietMode();
		}
		return true;
	}
}