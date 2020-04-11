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
package l2r.gameserver.scripts.handlers.skillhandlers;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import l2r.Config;
import l2r.gameserver.enums.ShotType;
import l2r.gameserver.handler.ISkillHandler;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.L2SkillType;
import l2r.gameserver.model.stats.BaseStats;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Formulas;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

public class Pdam implements ISkillHandler
{
	private static final Logger _logDamage = Logger.getLogger("damage");
	
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.PDAM,
		L2SkillType.FATAL
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}
		
		if (((skill.getFlyRadius() > 0) || (skill.getFlyType() != null)) && activeChar.isMovementDisabled())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(skill);
			activeChar.sendPacket(sm);
			return;
		}
		
		int damage = 0;
		
		boolean ss = skill.useSoulShot() && activeChar.isChargedShot(ShotType.SOULSHOTS);
		
		for (L2Character target : (L2Character[]) targets)
		{
			
			if (activeChar.isPlayer() && target.isPlayer() && target.getActingPlayer().isFakeDeath())
			{
				target.stopFakeDeath(true);
			}
			else if (target.isDead())
			{
				continue;
			}
			
			final boolean dual = activeChar.isUsingDualWeapon();
			final byte shld = Formulas.calcShldUse(activeChar, target, skill);
			// PDAM critical chance not affected by buffs, only by STR. Only some skills are meant to crit.
			boolean crit = false;
			if (!skill.isStaticDamage() && (skill.getBaseCritRate() > 0))
			{
				crit = Formulas.calcCrit(skill.getBaseCritRate() * 10 * BaseStats.STR.calcBonus(activeChar), true, target);
			}
			
			if (!crit && ((skill.getCondition() & L2Skill.COND_CRIT) != 0))
			{
				damage = 0;
			}
			else
			{
				damage = skill.isStaticDamage() ? (int) skill.getPower() : (int) Formulas.calcPhysDam(activeChar, target, skill, shld, false, dual, ss);
			}
			if (!skill.isStaticDamage() && (skill.getMaxSoulConsumeCount() > 0) && activeChar.isPlayer())
			{
				// Souls Formula (each soul increase +4%)
				int chargedSouls = (activeChar.getActingPlayer().getChargedSouls() <= skill.getMaxSoulConsumeCount()) ? activeChar.getActingPlayer().getChargedSouls() : skill.getMaxSoulConsumeCount();
				damage *= 1 + (chargedSouls * 0.04);
			}
			if (crit)
			{
				damage *= 2; // PDAM Critical damage always 2x and not affected by buffs
			}
			
			final boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(target, skill);
			
			if (!skillIsEvaded)
			{
				if (skill.hasEffects())
				{
					L2Effect[] effects;
					
					// activate attacked effects, if any
					target.stopSkillEffects(skill.getId());
					effects = skill.getEffects(activeChar, target, new Env(shld, false, false, false));
					if ((effects != null) && (effects.length > 0))
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(skill);
						target.sendPacket(sm);
					}
				}
				
				if (damage > 0)
				{
					activeChar.sendDamageMessage(target, damage, false, crit, false);
					
					if (Config.LOG_GAME_DAMAGE && activeChar.isPlayable() && (damage > Config.LOG_GAME_DAMAGE_THRESHOLD))
					{
						LogRecord record = new LogRecord(Level.INFO, "");
						record.setParameters(new Object[]
						{
							activeChar,
							" did damage ",
							damage,
							skill,
							" to ",
							target
						});
						record.setLoggerName("pdam");
						_logDamage.log(record);
					}
					
					// Possibility of a lethal strike
					Formulas.calcLethalHit(activeChar, target, skill);
					
					target.reduceCurrentHp(damage, activeChar, skill);
					
					// Check if damage should be reflected
					Formulas.calcDamageReflected(activeChar, target, skill, damage);
				}
				else
				{
					// No damage
					activeChar.sendPacket(SystemMessageId.ATTACK_FAILED);
				}
			}
			else
			{
				if (activeChar.isPlayer())
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DODGES_ATTACK);
					sm.addString(target.getName());
					activeChar.getActingPlayer().sendPacket(sm);
				}
				if (target.isPlayer())
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_C1_ATTACK);
					sm.addString(activeChar.getName());
					target.getActingPlayer().sendPacket(sm);
				}
				
				// Possibility of a lethal strike despite skill is evaded
				Formulas.calcLethalHit(activeChar, target, skill);
			}
		}
		
		// self Effect :]
		if (skill.hasSelfEffects())
		{
			final L2Effect effect = activeChar.getFirstEffect(skill.getId());
			if ((effect != null) && effect.isSelfEffect())
			{
				// Replace old effect with new one.
				effect.exit();
			}
			skill.getEffectsSelf(activeChar);
		}
		
		activeChar.setChargedShot(ShotType.SOULSHOTS, false);
		
		if (skill.isSuicideAttack())
		{
			activeChar.doDie(activeChar);
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
