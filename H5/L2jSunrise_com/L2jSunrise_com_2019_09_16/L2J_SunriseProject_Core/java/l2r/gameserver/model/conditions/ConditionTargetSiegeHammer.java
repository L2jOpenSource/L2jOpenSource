package l2r.gameserver.model.conditions;

import l2r.gameserver.model.actor.instance.L2DoorInstance;
import l2r.gameserver.model.stats.Env;

/**
 * The Class ConditionTargetSiegeHammer.
 * @author vGodFather
 */
public class ConditionTargetSiegeHammer extends Condition
{
	public ConditionTargetSiegeHammer()
	{
	
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		return (env.getTarget() instanceof L2DoorInstance);
	}
}
