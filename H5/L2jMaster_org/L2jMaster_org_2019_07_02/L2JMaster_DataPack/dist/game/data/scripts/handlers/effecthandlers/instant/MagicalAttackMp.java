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

import com.l2jserver.gameserver.enums.ShotType;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.conditions.Condition;
import com.l2jserver.gameserver.model.effects.AbstractEffect;
import com.l2jserver.gameserver.model.effects.L2EffectType;
import com.l2jserver.gameserver.model.skills.AbnormalType;
import com.l2jserver.gameserver.model.skills.BuffInfo;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.model.stats.Formulas;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * Magical Attack MP effect.
 * @author Adry_85
 */
public final class MagicalAttackMp extends AbstractEffect
{
	private final double _power;
	
	public MagicalAttackMp(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_power = params.getDouble("power", 0);
	}
	
	@Override
	public boolean calcSuccess(BuffInfo info)
	{
		if (info.getEffected().isInvul())
		{
			return false;
		}
		if (!Formulas.calcMagicAffected(info.getEffector(), info.getEffected(), info.getSkill()))
		{
			if (info.getEffector().isPlayer())
			{
				info.getEffector().sendPacket(SystemMessageId.ATTACK_FAILED);
			}
			if (info.getEffected().isPlayer())
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_C2_DRAIN2);
				sm.addCharName(info.getEffected());
				sm.addCharName(info.getEffector());
				info.getEffected().sendPacket(sm);
			}
			return false;
		}
		return true;
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.MAGICAL_ATTACK;
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
		final Skill skill = info.getSkill();
		
		if (info.getEffector().isAlikeDead() || (target.getEffectList().getBuffInfoByAbnormalType(AbnormalType.INVINCIBILITY) != null))
		{
			return;
		}
		
		boolean sps = skill.useSpiritShot() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
		boolean bss = skill.useSpiritShot() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
		final byte shld = Formulas.calcShldUse(activeChar, target, skill);
		final boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, skill));
		double damage = Formulas.calcManaDam(activeChar, target, skill, shld, sps, bss, mcrit, _power);
		double mp = (damage > target.getCurrentMp() ? target.getCurrentMp() : damage);
		
		if (damage > 0)
		{
			target.stopEffectsOnDamage(true);
			target.setCurrentMp(target.getCurrentMp() - mp);
		}
		
		if (target.isPlayer())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MP_HAS_BEEN_DRAINED_BY_C1);
			sm.addCharName(activeChar);
			sm.addInt((int) mp);
			target.sendPacket(sm);
		}
		
		if (activeChar.isPlayer())
		{
			SystemMessage sm2 = SystemMessage.getSystemMessage(SystemMessageId.YOUR_OPPONENTS_MP_WAS_REDUCED_BY_S1);
			sm2.addInt((int) mp);
			activeChar.sendPacket(sm2);
		}
	}
}