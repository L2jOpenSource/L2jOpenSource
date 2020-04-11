package l2r.gameserver.model.conditions;

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.conditions.Condition;
import l2r.gameserver.model.stats.Env;

public class ConditionPlayerInInstance extends Condition
{
	public final boolean _inInstance;
	
	public ConditionPlayerInInstance(boolean inInstance)
	{
		_inInstance = inInstance;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		final L2Character effector = env.getCharacter();
		if ((effector == null) || (effector.getActingPlayer() == null))
		{
			return false;
		}
		return (effector.getInstanceId() == 0) ? !_inInstance : _inInstance;
	}
}
