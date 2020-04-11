/*
 * Copyright (C) 2004-2013 L2J DataPack
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

import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Formulas;

/**
 * CP Damage Percent effect implementation.
 * @author Zoey76
 */
public class CpDamPercent extends L2Effect
{
	private final double _power;
	
	public CpDamPercent(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_power = template.getParameters().getDouble("power", 0);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		if (!getEffected().isPlayer())
		{
			return false;
		}
		
		if (getEffected().isPlayer() && getEffected().getActingPlayer().isFakeDeath())
		{
			getEffected().stopFakeDeath(true);
		}
		
		int damage = (int) ((getEffected().getCurrentCp() * _power) / 100);
		// Manage attack or cast break of the target (calculating rate, sending message)
		if (!getEffected().isRaid() && Formulas.calcAtkBreak(getEffected(), damage))
		{
			getEffected().breakAttack();
			getEffected().breakCast();
		}
		
		if (damage > 0)
		{
			getEffected().setCurrentCp(getEffected().getCurrentCp() - damage);
		}
		return true;
	}
}