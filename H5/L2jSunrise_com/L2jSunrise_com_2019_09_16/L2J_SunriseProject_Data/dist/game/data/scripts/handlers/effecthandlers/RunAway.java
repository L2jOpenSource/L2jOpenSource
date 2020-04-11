/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import l2r.gameserver.ai.L2AttackableAI;
import l2r.gameserver.enums.CtrlEvent;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.stats.Env;
import l2r.util.Rnd;

/**
 * Run Away effect implementation.
 * @author Zoey76
 */
public final class RunAway extends L2Effect
{
	private final int _power;
	private final int _time;
	
	public RunAway(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_power = template.getParameters().getInt("power", 0);
		
		_time = template.getParameters().getInt("time", 0);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		if (!getEffected().isAttackable())
		{
			return false;
		}
		
		if (Rnd.get(100) > _power)
		{
			return false;
		}
		
		if (getEffected().isCastingNow() && getEffected().canAbortCast())
		{
			getEffected().abortCast();
		}
		
		((L2AttackableAI) getEffected().getAI()).setFearTime(_time);
		
		getEffected().getAI().notifyEvent(CtrlEvent.EVT_AFRAID, getEffector(), true);
		return true;
	}
}