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
package handlers.effecthandlers.pump;

import com.l2jserver.gameserver.enums.EffectCalculationType;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.stat.CharStat;
import com.l2jserver.gameserver.model.conditions.Condition;
import com.l2jserver.gameserver.model.effects.AbstractEffect;
import com.l2jserver.gameserver.model.skills.BuffInfo;
import com.l2jserver.gameserver.model.stats.Stats;
import com.l2jserver.gameserver.model.stats.functions.FuncAdd;
import com.l2jserver.gameserver.model.stats.functions.FuncMul;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Zealar
 */
public final class MaxCp extends AbstractEffect
{
	private final double _power;
	private final EffectCalculationType _type;
	private final boolean _heal;
	
	public MaxCp(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_type = params.getEnum("type", EffectCalculationType.class, EffectCalculationType.DIFF);
		switch (_type)
		{
			case DIFF:
			{
				_power = params.getDouble("power", 0);
				break;
			}
			default:
			{
				_power = 1 + (params.getDouble("power", 0) / 100.0);
			}
		}
		_heal = params.getBoolean("heal", false);
		
		if (params.isEmpty())
		{
			_log.warning(getClass().getSimpleName() + ": must have parameters.");
		}
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		final L2Character effected = info.getEffected();
		final CharStat charStat = effected.getStat();
		final double currentCp = effected.getCurrentCp();
		double amount = _power;
		
		synchronized (charStat)
		{
			switch (_type)
			{
				case DIFF:
				{
					charStat.getActiveChar().addStatFunc(new FuncAdd(Stats.MAX_CP, 1, this, _power, null));
					if (_heal)
					{
						effected.setCurrentCp((currentCp + _power));
					}
					else
					{
						effected.setCurrentCp(currentCp);
					}
					break;
				}
				case PER:
				{
					final double maxCp = effected.getMaxCp();
					charStat.getActiveChar().addStatFunc(new FuncMul(Stats.MAX_CP, 1, this, _power, null));
					if (_heal)
					{
						amount = (_power - 1) * maxCp;
						effected.setCurrentCp(currentCp + amount);
					}
					break;
				}
			}
		}
		if (_heal)
		{
			effected.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CP_HAS_BEEN_RESTORED).addInt((int) amount));
		}
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		final CharStat charStat = info.getEffected().getStat();
		synchronized (charStat)
		{
			charStat.getActiveChar().removeStatsOwner(this);
		}
	}
	
}
