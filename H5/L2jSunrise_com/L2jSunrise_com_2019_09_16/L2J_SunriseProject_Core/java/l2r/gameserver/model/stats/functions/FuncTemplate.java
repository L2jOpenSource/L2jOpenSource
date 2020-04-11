/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.stats.functions;

import java.lang.reflect.Constructor;

import l2r.gameserver.enums.StatFunction;
import l2r.gameserver.model.conditions.Condition;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mkizub
 */
public final class FuncTemplate
{
	protected static final Logger _log = LoggerFactory.getLogger(FuncTemplate.class);
	
	public Condition _attachCond;
	public Condition _applayCond;
	private final Constructor<?> _constructor;
	private final Stats _stat;
	private final int _order;
	private final double _value;
	public final Class<?> functionClass;
	
	public FuncTemplate(Condition attachCond, Condition applayCond, String functionName, int order, Stats stat, double value)
	{
		final StatFunction function = StatFunction.valueOf(functionName.toUpperCase());
		if (order >= 0)
		{
			_order = order;
		}
		else
		{
			_order = function.getOrder();
		}
		
		_attachCond = attachCond;
		_applayCond = applayCond;
		_stat = stat;
		_value = value;
		
		try
		{
			functionClass = Class.forName("l2r.gameserver.model.stats.functions.Func" + function.getName());
			_constructor = functionClass.getConstructor(Stats.class, // Stats to update
			Integer.TYPE, // Order of execution
			Object.class, // Owner
			Double.TYPE, // Value for function
			Condition.class // Condition
			);
		}
		catch (ClassNotFoundException | NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Gets the function stat.
	 * @return the stat.
	 */
	public Stats getStat()
	{
		return _stat;
	}
	
	/**
	 * Gets the function priority order.
	 * @return the order
	 */
	public int getOrder()
	{
		return _order;
	}
	
	/**
	 * Gets the function value.
	 * @return the value
	 */
	public double getValue()
	{
		return _value;
	}
	
	public AbstractFunction getFunc(Env env, Object owner)
	{
		if ((_attachCond != null) && !_attachCond.test(env))
		{
			return null;
		}
		try
		{
			return (AbstractFunction) _constructor.newInstance(_stat, _order, owner, _value, _applayCond);
		}
		catch (Exception e)
		{
			_log.warn(FuncTemplate.class.getSimpleName() + ": " + e.getMessage());
		}
		return null;
	}
}
