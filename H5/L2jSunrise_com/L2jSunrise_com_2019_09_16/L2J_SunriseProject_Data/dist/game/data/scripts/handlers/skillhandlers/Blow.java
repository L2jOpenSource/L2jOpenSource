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
package handlers.skillhandlers;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import l2r.Config;
import l2r.gameserver.enums.ShotType;
import l2r.gameserver.enums.audio.Sound;
import l2r.gameserver.handler.ISkillHandler;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.L2SkillType;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Formulas;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Steuf
 */
public class Blow implements ISkillHandler
{
	private static final Logger _logDamage = Logger.getLogger("damage");
	
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.BLOW
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}
		
		boolean ss = skill.useSoulShot() && activeChar.isChargedShot(ShotType.SOULSHOTS);
		boolean sps = skill.useSpiritShot() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
		boolean bss = skill.useSpiritShot() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
		
		for (L2Character target : (L2Character[]) targets)
		{
			if (target.isAlikeDead())
			{
				continue;
			}
			
			// Check firstly if target dodges skill
			final boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(activeChar, target, skill);
			if (!skillIsEvaded && Formulas.calcBlowSuccess(activeChar, target, skill))
			{
				if (skill.hasEffects())
				{
					final byte shld = Formulas.calcShldUse(activeChar, target, skill);
					target.stopSkillEffects(skill.getId());
					if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss))
					{
						skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss));
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(skill);
						target.sendPacket(sm);
					}
					else
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
						sm.addCharName(target);
						sm.addSkillName(skill);
						activeChar.sendPacket(sm);
					}
				}
				
				byte shld = Formulas.calcShldUse(activeChar, target, skill);
				
				double damage = skill.isStaticDamage() ? skill.getPower() : (int) Formulas.calcBlowDamage(activeChar, target, skill, shld, ss);
				if (!skill.isStaticDamage() && (skill.getMaxSoulConsumeCount() > 0) && activeChar.isPlayer())
				{
					// Souls Formula (each soul increase +4%)
					int chargedSouls = (activeChar.getActingPlayer().getChargedSouls() <= skill.getMaxSoulConsumeCount()) ? activeChar.getActingPlayer().getChargedSouls() : skill.getMaxSoulConsumeCount();
					damage *= 1 + (chargedSouls * 0.04);
				}
				
				boolean crit = Formulas.calcCrit(activeChar, target, skill);
				// Crit rate base crit rate for skill, modified with STR bonus
				if (!skill.isStaticDamage() && crit)
				{
					damage *= 2;
				}
				
				// vGodFather retail message order
				activeChar.sendDamageMessage(target, (int) damage, false, true, false);
				target.reduceCurrentHp(damage, activeChar, skill);
				target.notifyDamageReceived(damage, activeChar, skill, crit, false, false);
				
				if (!skill.getDmgDirectlyToHP())
				{
					activeChar.sendPacket(Sound.SKILLSOUND_CRITICAL_HIT_2.getPacket());
				}
				
				if (Config.LOG_GAME_DAMAGE && activeChar.isPlayable() && (damage > Config.LOG_GAME_DAMAGE_THRESHOLD))
				{
					LogRecord record = new LogRecord(Level.INFO, "");
					record.setParameters(new Object[]
					{
						activeChar,
						" did damage ",
						(int) damage,
						skill,
						" to ",
						target
					});
					record.setLoggerName("pdam");
					_logDamage.log(record);
				}
				
				// Maybe launch chance skills on us
				if (activeChar.getChanceSkills() != null)
				{
					activeChar.getChanceSkills().onSkillHit(target, skill, false, damage);
				}
				// Maybe launch chance skills on target
				if (target.getChanceSkills() != null)
				{
					target.getChanceSkills().onSkillHit(activeChar, skill, true, damage);
				}
				
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				
				// Check if damage should be reflected
				Formulas.calcDamageReflected(activeChar, target, skill, damage, crit);
				
				// vGodFather: ss must consume only if skill succeed
				activeChar.setChargedShot(ShotType.SOULSHOTS, false);
			}
			
			// Possibility of a lethal strike
			Formulas.calcLethalHit(activeChar, target, skill);
			
			// Self Effect
			if (skill.hasSelfEffects())
			{
				final L2Effect effect = activeChar.getFirstEffect(skill.getId());
				if ((effect != null) && effect.isSelfEffect())
				{
					effect.exit();
				}
				skill.getEffectsSelf(activeChar);
			}
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
