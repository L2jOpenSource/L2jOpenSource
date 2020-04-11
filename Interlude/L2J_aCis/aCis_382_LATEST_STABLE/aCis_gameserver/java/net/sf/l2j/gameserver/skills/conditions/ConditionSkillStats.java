package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.skills.Env;

/**
 * @author mkizub
 */
public class ConditionSkillStats extends Condition
{
	private final Stats _stat;
	
	public ConditionSkillStats(Stats stat)
	{
		super();
		_stat = stat;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		return env.getSkill() != null && env.getSkill().getStat() == _stat;
	}
}