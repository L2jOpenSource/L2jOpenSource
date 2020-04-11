package com.l2jfrozen.gameserver.skills.funcs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.skills.Env;
import com.l2jfrozen.gameserver.skills.Stats;
import com.l2jfrozen.gameserver.skills.conditions.Condition;

/**
 * @author mkizub
 */
public final class FuncTemplate
{
	public Condition attachCond;
	public Condition applayCond;
	public final Class<?> func;
	public final Constructor<?> constructor;
	public final Stats stat;
	public final int order;
	public final Lambda lambda;
	
	public FuncTemplate(final Condition pAttachCond, final Condition pApplayCond, final String pFunc, final Stats pStat, final int pOrder, final Lambda pLambda)
	{
		attachCond = pAttachCond;
		applayCond = pApplayCond;
		stat = pStat;
		order = pOrder;
		lambda = pLambda;
		try
		{
			func = Class.forName("com.l2jfrozen.gameserver.skills.funcs.Func" + pFunc);
		}
		catch (final ClassNotFoundException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			throw new RuntimeException(e);
		}
		try
		{
			constructor = func.getConstructor(new Class[]
			{
				Stats.class, // stats to update
				Integer.TYPE, // order of execution
				Object.class, // owner
				Lambda.class
				// value for function
			});
		}
		catch (final NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public Func getFunc(final Env env, final Object owner)
	{
		if (attachCond != null && !attachCond.test(env))
		{
			return null;
		}
		try
		{
			final Func f = (Func) constructor.newInstance(stat, order, owner, lambda);
			if (applayCond != null)
			{
				f.setCondition(applayCond);
			}
			return f;
		}
		catch (final IllegalAccessException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (final InstantiationException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (final InvocationTargetException e)
		{
			e.printStackTrace();
			return null;
		}
		
	}
}
