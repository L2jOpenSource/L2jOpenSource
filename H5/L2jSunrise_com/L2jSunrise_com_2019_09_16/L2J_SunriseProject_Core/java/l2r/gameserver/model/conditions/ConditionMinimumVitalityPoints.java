package l2r.gameserver.model.conditions;

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.conditions.Condition;
import l2r.gameserver.model.stats.Env;

public class ConditionMinimumVitalityPoints extends Condition
{
	private final int _count;
	
	public ConditionMinimumVitalityPoints(int count)
	{
		_count = count;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		final L2Character effector = env.getCharacter();
		final L2PcInstance player = effector != null ? effector.getActingPlayer() : null;
		if (player != null)
		{
			return player.getVitalityPoints() >= _count;
		}
		return false;
	}
}
