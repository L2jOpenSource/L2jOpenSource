package l2r.gameserver.model.conditions;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.conditions.Condition;
import l2r.gameserver.model.stats.Env;

public class ConditionTargetPlayer extends Condition
{
	@Override
	public boolean testImpl(Env env)
	{
		return (env.getTarget() instanceof L2PcInstance);
	}
}
