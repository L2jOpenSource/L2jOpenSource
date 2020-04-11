package net.sf.l2j.gameserver.skills.basefuncs;

import net.sf.l2j.gameserver.skills.Env;

/**
 * @author mkizub
 */
public final class LambdaConst extends Lambda
{
	private final double _value;
	
	public LambdaConst(double value)
	{
		_value = value;
	}
	
	@Override
	public double calc(Env env)
	{
		return _value;
	}
}