package com.l2jfrozen.gameserver.skills.funcs;

import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author mkizub
 */
public final class LambdaCalc extends Lambda
{
	public Func[] funcs;
	
	public LambdaCalc()
	{
		funcs = new Func[0];
	}
	
	@Override
	public double calc(final Env env)
	{
		final double saveValue = env.value;
		try
		{
			env.value = 0;
			for (final Func f : funcs)
			{
				f.calc(env);
			}
			return env.value;
		}
		finally
		{
			env.value = saveValue;
		}
	}
	
	public void addFunc(final Func f)
	{
		final int len = funcs.length;
		final Func[] tmp = new Func[len + 1];
		for (int i = 0; i < len; i++)
		{
			tmp[i] = funcs[i];
		}
		tmp[len] = f;
		funcs = tmp;
	}
	
}
