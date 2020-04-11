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

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

/**
 * @author UnAfraid, vGodFather
 */
public class ManaHealPercent extends L2Effect
{
	public ManaHealPercent(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.MANAHEAL_PERCENT;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		L2Character target = getEffected();
		if ((target == null) || target.isDead() || target.isDoor())
		{
			return false;
		}
		
		// vGodFather: herb effect must override invul check
		if ((target.isInvul() || target.isMpBlocked()) && !getSkill().isHerb())
		{
			return false;
		}
		
		double amount = 0;
		double power = getValue();
		boolean full = (power == 100.0);
		
		amount = full ? target.getMaxMp() : (target.getMaxMp() * power) / 100.0;
		// Prevents overheal and negative amount
		amount = Math.max(Math.min(amount, target.getMaxRecoverableMp() - target.getCurrentMp()), 0);
		if (amount != 0)
		{
			target.setCurrentMp(amount + target.getCurrentMp());
		}
		
		// vGodFather: Summons must feel heal effect from herbs
		if (getSkill().getName().toLowerCase().contains("herb") && getSkill().getName().toLowerCase().contains("mana") && target.hasSummon())
		{
			L2Summon summon = target.getSummon();
			if ((summon != null) && !summon.isDead() && !summon.isInvul())
			{
				double newAmount = 0;
				newAmount = full ? summon.getMaxMp() : (summon.getMaxMp() * power) / 100.0;
				newAmount = Math.max(Math.min(newAmount, summon.getMaxRecoverableMp() - summon.getCurrentMp()), 0);
				if (newAmount != 0)
				{
					summon.setCurrentMp(newAmount + summon.getCurrentMp());
				}
			}
		}
		
		SystemMessage sm;
		if (getEffector().getObjectId() != target.getObjectId())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MP_RESTORED_BY_C1);
			sm.addCharName(getEffector());
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED);
		}
		sm.addInt((int) amount);
		target.sendPacket(sm);
		return true;
	}
}
