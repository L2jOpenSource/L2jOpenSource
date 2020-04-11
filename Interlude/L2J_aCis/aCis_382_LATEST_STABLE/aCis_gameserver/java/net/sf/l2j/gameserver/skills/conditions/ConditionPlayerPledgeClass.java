package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.skills.Env;

/**
 * The Class ConditionPlayerPledgeClass.
 * @author MrPoke
 */
public final class ConditionPlayerPledgeClass extends Condition
{
	private final int _pledgeClass;
	
	/**
	 * Instantiates a new condition player pledge class.
	 * @param pledgeClass the pledge class
	 */
	public ConditionPlayerPledgeClass(int pledgeClass)
	{
		_pledgeClass = pledgeClass;
	}
	
	/**
	 * Test impl.
	 * @param env the env
	 * @return true, if successful
	 */
	@Override
	public boolean testImpl(Env env)
	{
		if (env.getPlayer() == null)
			return false;
		
		if (env.getPlayer().getClan() == null)
			return false;
		
		if (_pledgeClass == -1)
			return env.getPlayer().isClanLeader();
		
		return env.getPlayer().getPledgeClass() >= _pledgeClass;
	}
}