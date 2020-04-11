/*
 * Copyright (C) 2004-2014 L2J Server
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
package l2r.gameserver.model.effects;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import l2r.gameserver.handler.EffectHandler;
import l2r.gameserver.model.ChanceCondition;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.conditions.Condition;
import l2r.gameserver.model.skills.funcs.FuncTemplate;
import l2r.gameserver.model.skills.funcs.Lambda;
import l2r.gameserver.model.stats.Env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mkizub
 */
public class EffectTemplate
{
	static Logger _log = LoggerFactory.getLogger(EffectTemplate.class);
	
	private final Class<?> _func;
	
	private final Constructor<?> _constructor;
	public final Condition attachCond;
	public final Condition applayCond;
	public final Lambda lambda;
	public final int counter;
	public final int abnormalTime; // in seconds
	public final AbnormalEffect abnormalEffect;
	public final AbnormalEffect[] specialEffect;
	public final AbnormalEffect eventEffect;
	public FuncTemplate[] funcTemplates;
	public final String abnormalType;
	public final byte abnormalLvl;
	public final boolean icon;
	public final String funcName;
	public final double effectPower; // to handle chance
	
	public final int triggeredId;
	public final int triggeredLevel;
	public final ChanceCondition chanceCondition;
	private final StatsSet _parameters;
	
	public EffectTemplate(Condition pAttachCond, Condition pApplayCond, String func, Lambda pLambda, int pCounter, int pAbnormalTime, AbnormalEffect pAbnormalEffect, AbnormalEffect[] pSpecialEffect, AbnormalEffect pEventEffect, String pAbnormalType, byte pAbnormalLvl, boolean showicon, double ePower, int trigId, int trigLvl, ChanceCondition chanceCond, StatsSet params)
	{
		attachCond = pAttachCond;
		applayCond = pApplayCond;
		lambda = pLambda;
		counter = pCounter;
		abnormalTime = pAbnormalTime;
		abnormalEffect = pAbnormalEffect;
		specialEffect = pSpecialEffect;
		eventEffect = pEventEffect;
		abnormalType = pAbnormalType;
		abnormalLvl = pAbnormalLvl;
		icon = showicon;
		funcName = func;
		effectPower = ePower;
		
		triggeredId = trigId;
		triggeredLevel = trigLvl;
		chanceCondition = chanceCond;
		_parameters = params;
		
		_func = EffectHandler.getInstance().getHandler(func);
		if (_func == null)
		{
			_log.warn("EffectTemplate: Requested Unexistent effect: " + func);
			throw new RuntimeException();
		}
		
		try
		{
			_constructor = _func.getConstructor(Env.class, EffectTemplate.class);
		}
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public L2Effect getEffect(Env env)
	{
		return getEffect(env, false);
	}
	
	public L2Effect getEffect(Env env, boolean ignoreTest)
	{
		if (!ignoreTest && ((attachCond != null) && !attachCond.test(env)))
		{
			return null;
		}
		try
		{
			L2Effect effect = (L2Effect) _constructor.newInstance(env, this);
			return effect;
		}
		catch (IllegalAccessException e)
		{
			_log.warn(String.valueOf(e));
			return null;
		}
		catch (InstantiationException e)
		{
			_log.warn(String.valueOf(e));
			return null;
		}
		catch (InvocationTargetException e)
		{
			_log.warn("Error creating new instance of Class " + _func + " Exception was: " + e.getTargetException().getMessage(), e.getTargetException());
			return null;
		}
		
	}
	
	/**
	 * Creates an L2Effect instance from an existing one and an Env object.
	 * @param env
	 * @param stolen
	 * @return the stolent effect
	 */
	public L2Effect getStolenEffect(Env env, L2Effect stolen)
	{
		Class<?> func = EffectHandler.getInstance().getHandler(funcName);
		if (func == null)
		{
			throw new RuntimeException();
		}
		
		Constructor<?> stolenCons;
		try
		{
			stolenCons = func.getConstructor(Env.class, L2Effect.class);
		}
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
		try
		{
			L2Effect effect = (L2Effect) stolenCons.newInstance(env, stolen);
			// if (_applayCond != null)
			// effect.setCondition(_applayCond);
			return effect;
		}
		catch (IllegalAccessException e)
		{
			_log.warn(String.valueOf(e));
			return null;
		}
		catch (InstantiationException e)
		{
			_log.warn(String.valueOf(e));
			return null;
		}
		catch (InvocationTargetException e)
		{
			_log.warn("Error creating new instance of Class " + func + " Exception was: " + e.getTargetException().getMessage(), e.getTargetException());
			return null;
		}
	}
	
	public void attach(FuncTemplate f)
	{
		if (funcTemplates == null)
		{
			funcTemplates = new FuncTemplate[]
			{
				f
			};
		}
		else
		{
			int len = funcTemplates.length;
			FuncTemplate[] tmp = new FuncTemplate[len + 1];
			System.arraycopy(funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			funcTemplates = tmp;
		}
	}
	
	public StatsSet getParameters()
	{
		return _parameters;
	}
	
	public boolean hasParameters()
	{
		return _parameters != null;
	}
}