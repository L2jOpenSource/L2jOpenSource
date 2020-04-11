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
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;

/**
 * Passive effect implementation.
 * @author Adry_85
 */
public final class Passive extends L2Effect
{
	private final boolean _onlyUndead;
	
	public Passive(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_onlyUndead = template.getParameters().getBoolean("onlyUndead", false);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.PASSIVE;
	}
	
	@Override
	public void onExit()
	{
		if (getEffected().isAttackable())
		{
			((L2Attackable) getEffected()).removeAggro(false);
		}
	}
	
	@Override
	public boolean onStart()
	{
		if (!getEffected().isAttackable())
		{
			return false;
		}
		
		if (_onlyUndead && !getEffected().isUndead())
		{
			return false;
		}
		
		((L2Attackable) getEffected()).removeAggro(true);
		return true;
	}
}
