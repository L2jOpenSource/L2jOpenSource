/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.skills.l2skills;

import l2r.gameserver.enums.ShotType;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.items.L2Weapon;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.stats.BaseStats;
import l2r.gameserver.model.stats.Formulas;
import l2r.gameserver.model.stats.Stats;
import l2r.util.Rnd;

import gr.sr.balanceEngine.BalanceHandler;

public class L2SkillChargeDmg extends L2Skill
{
	public L2SkillChargeDmg(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		final L2PcInstance attacker = caster instanceof L2PcInstance ? (L2PcInstance) caster : null;
		if (attacker == null)
		{
			return;
		}
		
		if (caster.isAlikeDead())
		{
			return;
		}
		
		boolean ss = useSoulShot() && caster.isChargedShot(ShotType.SOULSHOTS);
		double ssBoost = ss ? 2 : 1.0;
		final L2Skill skill = this;
		
		for (L2Character target : (L2Character[]) targets)
		{
			if (target.isAlikeDead())
			{
				continue;
			}
			
			// Calculate skill evasion
			final boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(caster, target, this);
			if (skillIsEvaded)
			{
				continue;
			}
			
			final boolean isPvP = attacker.isPlayable() && target.isPlayable();
			final boolean isPvE = attacker.isPlayable() && target.isAttackable();
			double attack = attacker.getPAtk(target);
			double defence = target.getPDef(attacker);
			
			if (!ignoreShield())
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
				double damageMultiplier = Formulas.calcValakasTrait(attacker, target, skill) * Formulas.calcAttributeBonus(attacker, target, skill);
				
				double weaponTypeBoost = 77;
				final L2Weapon weapon = attacker.getActiveWeaponItem();
				if ((weapon != null) && weapon.getItemType().isRanged())
				{
					weaponTypeBoost = 70;
				}
				
				// charge count should be the count before casting the skill but since its reduced before calling effects
				// we add skill consume charges to current charges
				double energyChargesBoost = (((attacker.getCharges() + skill.getChargeConsume()) - 1) * 0.2) + 1;
				
				attack += getPower(attacker, target, isPvP, isPvE);
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
				
				critical = (BaseStats.STR.calcBonus(attacker) * getBaseCritRate()) > (Rnd.nextDouble() * 100);
				if (critical)
				{
					damage *= 2;
				}
			}
			
			// Reunion balancer
			damage = BalanceHandler.getInstance().calc(attacker, target, skill, damage, false);
			// Reunion balancer - End
			
			if (damage > 0)
			{
				attacker.sendDamageMessage(target, (int) damage, false, critical, false);
				target.reduceCurrentHp(damage, attacker, skill);
				target.notifyDamageReceived(damage, attacker, skill, critical, false, false);
				
				// vGodFather: Do we need to trigger this?
				// Maybe launch chance skills on us
				if (caster.getChanceSkills() != null)
				{
					caster.getChanceSkills().onSkillHit(target, this, false, damage);
				}
				// Maybe launch chance skills on target
				if (target.getChanceSkills() != null)
				{
					target.getChanceSkills().onSkillHit(caster, this, true, damage);
				}
				
				// Check if damage should be reflected
				Formulas.calcDamageReflected(attacker, target, skill, damage, critical);
			}
		}
		
		// effect self :]
		if (hasSelfEffects())
		{
			L2Effect effect = caster.getFirstEffect(getId());
			if ((effect != null) && effect.isSelfEffect())
			{
				// Replace old effect with new one.
				effect.exit();
			}
			// cast self effect if any
			getEffectsSelf(caster);
		}
		
		// Consume shot
		caster.setChargedShot(ShotType.SOULSHOTS, false);
	}
}
