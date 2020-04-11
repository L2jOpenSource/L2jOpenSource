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
package handlers.effecthandlers.consume;

import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.conditions.Condition;
import com.l2jserver.gameserver.model.effects.AbstractEffect;
import com.l2jserver.gameserver.model.effects.EffectFlag;
import com.l2jserver.gameserver.model.effects.L2EffectType;
import com.l2jserver.gameserver.model.skills.BuffInfo;
import com.l2jserver.gameserver.network.SystemMessageId;

/**
 * Chameleon Rest effect implementation.
 */
public final class ConsumeChameleonRest extends AbstractEffect
{
	private final double _power;
	
	public ConsumeChameleonRest(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_power = params.getDouble("power", 0);
		setTicks(params.getInt("ticks"));
	}
	
	@Override
	public int getEffectFlags()
	{
		return (EffectFlag.SILENT_MOVE.getMask() | EffectFlag.RELAXING.getMask());
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.RELAXING;
	}
	
	@Override
	public boolean onActionTime(BuffInfo info)
	{
		if (info.getEffected().isDead())
		{
			return false;
		}
		
		final L2Character target = info.getEffected();
		if (target.isPlayer())
		{
			if (!target.getActingPlayer().isSitting())
			{
				return false;
			}
		}
		
		final double manaDam = _power * getTicksMultiplier();
		if ((manaDam < 0) && ((target.getCurrentMp() + manaDam) <= 0))
		{
			target.sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
			return false;
		}
		
		target.setCurrentMp(Math.min(target.getCurrentMp() + manaDam, target.getMaxRecoverableMp()));
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		if (info.getEffected().isPlayer())
		{
			info.getEffected().getActingPlayer().sitDown(false);
		}
		else
		{
			info.getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
		}
	}
}