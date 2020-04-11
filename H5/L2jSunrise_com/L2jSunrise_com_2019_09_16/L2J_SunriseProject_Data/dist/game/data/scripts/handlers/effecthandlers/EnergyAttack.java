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

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import l2r.Config;
import l2r.gameserver.enums.ShotType;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.items.L2Weapon;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.stats.BaseStats;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Formulas;
import l2r.gameserver.model.stats.Stats;
import l2r.util.Rnd;

/**
 * Energy Attack effect implementation.
 * @author NosBit
 */
public final class EnergyAttack extends L2Effect
{
	private static final Logger _logDamage = Logger.getLogger("damage");
	
	private final double _power;
	private final int _criticalChance;
	private final boolean _ignoreShieldDefence;
	
	public EnergyAttack(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_power = template.getParameters().getDouble("power", 0);
		_criticalChance = template.getParameters().getInt("criticalChance", 0);
		_ignoreShieldDefence = template.getParameters().getBoolean("ignoreShieldDefence", false);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.PHYSICAL_ATTACK;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		final L2PcInstance attacker = getEffector() instanceof L2PcInstance ? (L2PcInstance) getEffector() : null;
		if (attacker == null)
		{
			return false;
		}
		
		// Check firstly if target dodges skill
		final boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(getEffector(), getEffected(), getSkill());
		if (skillIsEvaded)
		{
			return false;
		}
		
		final L2Character target = getEffected();
		final L2Skill skill = getSkill();
		
		double attack = attacker.getPAtk(target);
		double defence = target.getPDef(attacker);
		
		if (!_ignoreShieldDefence)
		{
			byte shield = Formulas.calcShldUse(attacker, target, skill, true);
			switch (shield)
			{
				case Formulas.SHIELD_DEFENSE_FAILED:
				{
					break;
				}
				case Formulas.SHIELD_DEFENSE_SUCCEED:
				{
					defence += target.getShldDef();
					break;
				}
				case Formulas.SHIELD_DEFENSE_PERFECT_BLOCK:
				{
					defence = -1;
					break;
				}
			}
		}
		
		double damage = 1;
		boolean critical = false;
		
		if (defence != -1)
		{
			// FIXME: Traits
			// double damageMultiplier = Formulas.calcWeaponTraitBonus(attacker, target) * Formulas.calcAttributeBonus(attacker, target, skill) * Formulas.calcGeneralTraitBonus(attacker, target, skill.getTraitType(), true);
			double damageMultiplier = Formulas.calcValakasTrait(attacker, target, getSkill()) * Formulas.calcAttributeBonus(attacker, target, skill);
			
			boolean ss = getSkill().useSoulShot() && attacker.isChargedShot(ShotType.SOULSHOTS);
			double ssBoost = ss ? 2 : 1.0;
			
			double weaponTypeBoost = 77;
			final L2Weapon weapon = attacker.getActiveWeaponItem();
			if ((weapon != null) && weapon.getItemType().isRanged())
			{
				weaponTypeBoost = 70;
			}
			
			// charge count should be the count before casting the skill but since its reduced before calling effects
			// we add skill consume charges to current charges
			double energyChargesBoost = (((attacker.getCharges() + skill.getChargeConsume()) - 1) * 0.2) + 1;
			
			attack += _power;
			attack *= ssBoost;
			attack *= energyChargesBoost;
			attack *= weaponTypeBoost;
			
			damage = attack / defence;
			damage *= damageMultiplier;
			if (target instanceof L2PcInstance)
			{
				damage *= attacker.getStat().calcStat(Stats.PVP_PHYS_SKILL_DMG, 1.0);
				damage /= target.getStat().calcStat(Stats.PVP_PHYS_SKILL_DEF, 1.0);
				damage = attacker.getStat().calcStat(Stats.PHYSICAL_SKILL_POWER, damage);
			}
			
			critical = (BaseStats.STR.calcBonus(attacker) * _criticalChance) > (Rnd.nextDouble() * 100);
			if (critical)
			{
				damage *= 2;
			}
		}
		
		if (damage > 0)
		{
			attacker.sendDamageMessage(target, (int) damage, false, critical, false);
			target.reduceCurrentHp(damage, attacker, skill);
			target.notifyDamageReceived(damage, attacker, skill, critical, false, false);
			
			if (Config.LOG_GAME_DAMAGE && attacker.isPlayable() && (damage > Config.LOG_GAME_DAMAGE_THRESHOLD))
			{
				LogRecord record = new LogRecord(Level.INFO, "");
				record.setParameters(new Object[]
				{
					attacker,
					" did damage ",
					(int) damage,
					skill,
					" to ",
					target
				});
				record.setLoggerName("pdam");
				_logDamage.log(record);
			}
			
			// Check if damage should be reflected
			Formulas.calcDamageReflected(attacker, target, skill, damage, critical);
		}
		return true;
	}
}