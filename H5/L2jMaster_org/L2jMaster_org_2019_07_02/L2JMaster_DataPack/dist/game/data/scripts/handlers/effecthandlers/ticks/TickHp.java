/*
 * Copyright (C) 2004-2019 L2J DataPack
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
package handlers.effecthandlers.ticks;

import com.l2jserver.gameserver.enums.EffectCalculationType;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.conditions.Condition;
import com.l2jserver.gameserver.model.effects.AbstractEffect;
import com.l2jserver.gameserver.model.effects.L2EffectType;
import com.l2jserver.gameserver.model.skills.AbnormalType;
import com.l2jserver.gameserver.model.skills.BuffInfo;
import com.l2jserver.gameserver.network.serverpackets.ExRegenMax;

/**
 * Tick Hp effect implementation.
 * @author Adry_85
 * @since 2.6.0.0
 */
public final class TickHp extends AbstractEffect
{
	private final double _power;
	private final EffectCalculationType _mode;
	
	public TickHp(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_power = params.getDouble("power", 0);
		_mode = params.getEnum("mode", EffectCalculationType.class, EffectCalculationType.DIFF);
		setTicks(params.getInt("ticks"));
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.DMG_OVER_TIME;
	}
	
	@Override
	public boolean onActionTime(BuffInfo info)
	{
		if (info.getEffected().isDead())
		{
			return false;
		}
		
		final L2Character target = info.getEffected();
		double power = 0;
		double hp = target.getCurrentHp();
		switch (_mode)
		{
			case DIFF:
			{
				power = _power * getTicksMultiplier();
				break;
			}
			case PER:
			{
				power = hp * _power * getTicksMultiplier();
				break;
			}
		}
		
		if (power < 0)
		{
			power = Math.abs(power);
			if (power >= (target.getCurrentHp() - 1))
			{
				power = target.getCurrentHp() - 1;
			}
			
			info.getEffected().reduceCurrentHpByDOT(power, info.getEffector(), info.getSkill());
			info.getEffected().notifyDamageReceived(power, info.getEffector(), info.getSkill(), false, true, false);
		}
		else
		{
			final double maxHp = target.getMaxRecoverableHp();
			
			// Not needed to set the HP and send update packet if player is already at max HP
			if (hp > maxHp)
			{
				return true;
			}
			
			target.setCurrentHp(Math.min(hp + power, maxHp));
		}
		return false;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		if (info.getEffected().isPlayer() && (getTicks() > 0) && (info.getSkill().getAbnormalType() == AbnormalType.HP_RECOVER))
		{
			info.getEffected().sendPacket(new ExRegenMax(info.getAbnormalTime(), getTicks(), _power));
		}
	}
}
