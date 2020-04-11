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
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.network.serverpackets.ExRegMax;

public class HealOverTime extends L2Effect
{
	public HealOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	// Special constructor to steal this effect
	public HealOverTime(Env env, L2Effect effect)
	{
		super(env, effect);
	}
	
	@Override
	public boolean canBeStolen()
	{
		return true;
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.HEAL_OVER_TIME;
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected().isPlayer())
		{
			getEffected().sendPacket(new ExRegMax(getValue(), getTotalCount() * getAbnormalTime(), getAbnormalTime()));
		}
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		if ((getEffected().getFirstEffect(L2EffectType.INVINCIBLE) != null) || getEffected().isDead() || getEffected().isDoor())
		{
			return false;
		}
		
		double hp = getEffected().getCurrentHp();
		double maxhp = getEffected().getMaxRecoverableHp();
		
		// Not needed to set the HP and send update packet if player is already at max HP
		if (hp >= maxhp)
		{
			return true;
		}
		
		// vGodFather: herb effect must override invul check
		if ((!getEffected().isInvul() && !getEffected().isHpBlocked()) || getSkill().isHerb())
		{
			hp += getValue();
			hp = Math.min(hp, maxhp);
			
			getEffected().setCurrentHp(hp);
		}
		return true;
	}
}
