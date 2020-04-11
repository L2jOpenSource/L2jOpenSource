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

import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.stats.Env;

/**
 * Add Hate effect implementation.
 * @author Adry_85
 */
public final class AddHate extends L2Effect
{
	private final int _power;
	
	public AddHate(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_power = template.getParameters().getInt("_power", 100);
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
		
		final double val = _power;
		if (val > 0)
		{
			((L2Attackable) getEffected()).addDamageHate(getEffector(), 0, (int) val);
		}
		else if (val < 0)
		{
			((L2Attackable) getEffected()).reduceHate(getEffector(), (int) -val);
		}
		return true;
	}
}
