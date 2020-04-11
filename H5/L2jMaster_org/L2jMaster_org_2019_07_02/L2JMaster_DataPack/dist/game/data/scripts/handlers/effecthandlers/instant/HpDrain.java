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
import com.l2jserver.gameserver.model.skills.BuffInfo;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.model.stats.Formulas;

/**
 * HP Drain effect implementation.
 * @author Adry_85
 */
public final class HpDrain extends AbstractEffect
{
	private final double _power;
	private final double _drain;
	
	public HpDrain(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_power = params.getDouble("power", 0);
		_drain = params.getDouble("drain", 0);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.HP_DRAIN;
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
		
		// TODO: Unhardcode Cubic Skill to avoid double damage
		if (activeChar.isAlikeDead() || (skill.getId() == 4050))
		{
			return;
		}
		
		boolean sps = skill.useSpiritShot() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
		boolean bss = skill.useSpiritShot() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
		boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, skill));
		byte shld = Formulas.calcShldUse(activeChar, target, skill);
		double damage = Formulas.calcMagicDam(activeChar, target, skill, shld, sps, bss, mcrit, _power);
		
		double drain = 0;
		double cp = target.getCurrentCp();
		double hp = target.getCurrentHp();
		
		if (cp > 0)
		{
			drain = (damage < cp) ? 0 : (damage - cp);
		}
		else if (damage > hp)
		{
			drain = hp;
		}
		else
		{
			drain = damage;
		}
		
		final double hpAdd = (_drain * drain);
		final double hpFinal = ((activeChar.getCurrentHp() + hpAdd) > activeChar.getMaxHp() ? activeChar.getMaxHp() : (activeChar.getCurrentHp() + hpAdd));
		activeChar.setCurrentHp(hpFinal);
		
		if (damage > 0)
		{
			// Manage attack or cast break of the target (calculating rate, sending message...)
			if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
			{
				target.breakAttack();
				target.breakCast();
			}
			activeChar.sendDamageMessage(target, (int) damage, mcrit, false, false);
			target.reduceCurrentHp(damage, activeChar, skill);
			target.notifyDamageReceived(damage, activeChar, skill, mcrit, false, false);
		}
	}
}