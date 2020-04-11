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
package handlers.effecthandlers.instant;

import com.l2jserver.gameserver.enums.EffectCalculationType;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.conditions.Condition;
import com.l2jserver.gameserver.model.effects.AbstractEffect;
import com.l2jserver.gameserver.model.skills.BuffInfo;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.model.stats.Stats;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * Mp effect implementation.
 * @author Adry_85
 * @since 2.6.0.0
 */
public final class Mp extends AbstractEffect
{
	private final double _amount;
	private final EffectCalculationType _mode;
	
	public Mp(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_amount = params.getDouble("amount", 0);
		_mode = params.getEnum("mode", EffectCalculationType.class, EffectCalculationType.DIFF);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		final L2Character target = info.getEffected();
		final L2Character activeChar = info.getEffector();
		if ((target == null) || target.isDead() || target.isDoor() || target.isInvul() || target.isMpBlocked())
		{
			return;
		}
		
		double amount = 0;
		switch (_mode)
		{
			case DIFF:
			{
				if (_amount < 0)
				{
					amount = _amount;
				}
				else
				{
					final Skill skill = info.getSkill();
					if (!skill.isStatic())
					{
						amount = target.calcStat(Stats.MANA_CHARGE, _amount, null, null);
					}
					
					amount = Math.min(skill.isStatic() ? _amount : amount, target.getMaxRecoverableMp() - target.getCurrentMp());
				}
				break;
			}
			case PER:
			{
				if (_amount < 0)
				{
					amount = (target.getCurrentMp() * _amount) / 100;
				}
				else
				{
					amount = Math.min((target.getMaxMp() * _amount) / 100.0, target.getMaxRecoverableMp() - target.getCurrentMp());
				}
				break;
			}
		}
		
		if (amount >= 0)
		{
			if (amount != 0)
			{
				target.setCurrentMp(amount + target.getCurrentMp());
			}
			
			SystemMessage sm;
			if ((activeChar != null) && (activeChar != target))
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MP_HAS_BEEN_RESTORED_BY_C1);
				sm.addCharName(info.getEffector());
				sm.addInt((int) amount);
				target.sendPacket(sm);
			}
			else
			{
				target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_MP_HAS_BEEN_RESTORED).addInt((int) amount));
			}
		}
		else
		{
			target.reduceCurrentMp(Math.abs(amount));
		}
	}
}
