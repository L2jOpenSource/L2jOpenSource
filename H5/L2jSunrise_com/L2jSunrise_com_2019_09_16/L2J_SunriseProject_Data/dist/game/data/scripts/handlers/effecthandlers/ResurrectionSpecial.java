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

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2PetInstance;
import l2r.gameserver.model.effects.EffectFlag;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;

/**
 * Resurrection Special effect implementation.
 * @author Zealar
 */
public final class ResurrectionSpecial extends L2Effect
{
	private final int _power;
	private final boolean _heal;
	
	public ResurrectionSpecial(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_power = template.getParameters().getInt("power", 0);
		_heal = template.getParameters().getBoolean("heal", false);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.RESURRECTION_SPECIAL;
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.RESURRECTION_SPECIAL.getMask();
	}
	
	@Override
	public void onExit()
	{
		if (!getEffected().isPlayer() && !getEffected().isPet())
		{
			return;
		}
		
		L2PcInstance caster = getEffector().getActingPlayer();
		if (getEffected().isPlayer())
		{
			getEffected().getActingPlayer().reviveRequest(caster, getSkill(), false, _power, _heal);
			return;
		}
		
		if (getEffected().isPet())
		{
			L2PetInstance pet = (L2PetInstance) getEffected();
			getEffected().getActingPlayer().reviveRequest(pet.getActingPlayer(), getSkill(), true, _power, _heal);
		}
	}
}