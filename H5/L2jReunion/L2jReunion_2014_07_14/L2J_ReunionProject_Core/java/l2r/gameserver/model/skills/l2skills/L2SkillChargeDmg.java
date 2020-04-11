/*
 * Copyright (C) 2004-2014 L2J Server
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

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import l2r.Config;
import l2r.gameserver.enums.ShotType;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.stats.BaseStats;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Formulas;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

public class L2SkillChargeDmg extends L2Skill
{
	private static final Logger _logDamage = Logger.getLogger("damage");
	
	public L2SkillChargeDmg(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if (caster.isAlikeDead())
		{
			return;
		}
		
		double modifier = 0;
		if (caster.isPlayer())
		{
			// Charges Formula (each charge increase +25%)
			modifier = ((caster.getActingPlayer().getCharges() * 0.25) + 1);
		}
		boolean ss = useSoulShot() && caster.isChargedShot(ShotType.SOULSHOTS);
		
		for (L2Character target : (L2Character[]) targets)
		{
			if (target.isAlikeDead())
			{
				continue;
			}
			
			// Calculate skill evasion
			boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(target, this);
			if (skillIsEvaded)
			{
				if (caster.isPlayer())
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DODGES_ATTACK);
					sm.addString(target.getName());
					caster.getActingPlayer().sendPacket(sm);
				}
				if (target.isPlayer())
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_C1_ATTACK2);
					sm.addString(caster.getName());
					target.getActingPlayer().sendPacket(sm);
				}
				
				// no futher calculations needed.
				continue;
			}
			
			// TODO: should we use dual or not?
			// because if so, damage are lowered but we don't do anything special with dual then
			// like in doAttackHitByDual which in fact does the calcPhysDam call twice
			// boolean dual = caster.isUsingDualWeapon();
			byte shld = Formulas.calcShldUse(caster, target, this);
			boolean crit = false;
			if ((getBaseCritRate() > 0) && !isStaticDamage())
			{
				crit = Formulas.calcCrit(getBaseCritRate() * 10 * BaseStats.STR.calcBonus(caster), true, target);
			}
			// damage calculation, crit is static 2x
			double damage = isStaticDamage() ? getPower() : Formulas.calcPhysDam(caster, target, this, shld, false, false, ss);
			if (crit)
			{
				damage *= 2;
			}
			
			if (damage > 0)
			{
				byte reflect = Formulas.calcSkillReflect(target, this);
				if (hasEffects())
				{
					if ((reflect & Formulas.SKILL_REFLECT_SUCCEED) != 0)
					{
						caster.stopSkillEffects(getId());
						getEffects(target, caster);
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(this);
						caster.sendPacket(sm);
					}
					else
					{
						// activate attacked effects, if any
						target.stopSkillEffects(getId());
						if (Formulas.calcSkillSuccess(caster, target, this, shld, false, false, true))
						{
							getEffects(caster, target, new Env(shld, false, false, false));
							
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
							sm.addSkillName(this);
							target.sendPacket(sm);
						}
						else
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
							sm.addCharName(target);
							sm.addSkillName(this);
							caster.sendPacket(sm);
						}
					}
				}
				
				double finalDamage = isStaticDamage() ? damage : damage * modifier;
				
				if (Config.LOG_GAME_DAMAGE && caster.isPlayable() && (damage > Config.LOG_GAME_DAMAGE_THRESHOLD))
				{
					LogRecord record = new LogRecord(Level.INFO, "");
					record.setParameters(new Object[]
					{
						caster,
						" did damage ",
						(int) damage,
						this,
						" to ",
						target
					});
					record.setLoggerName("pdam");
					_logDamage.log(record);
				}
				
				target.reduceCurrentHp(finalDamage, caster, this);
				
				// vengeance reflected damage
				if ((reflect & Formulas.SKILL_REFLECT_VENGEANCE) != 0)
				{
					if (target.isPlayer())
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.COUNTERED_C1_ATTACK);
						sm.addCharName(caster);
						target.sendPacket(sm);
					}
					if (caster.isPlayer())
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_PERFORMING_COUNTERATTACK);
						sm.addCharName(target);
						caster.sendPacket(sm);
					}
					// Formula from Diego Vargas post: http://www.l2guru.com/forum/showthread.php?p=3122630
					// 1189 x Your PATK / PDEF of target
					double vegdamage = ((1189 * target.getPAtk(caster)) / (double) caster.getPDef(target));
					caster.reduceCurrentHp(vegdamage, target, this);
				}
				
				caster.sendDamageMessage(target, (int) finalDamage, false, crit, false);
				
			}
			else
			{
				caster.sendDamageMessage(target, 0, false, false, true);
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
