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

import java.util.List;

import l2r.gameserver.enums.CtrlEvent;
import l2r.gameserver.enums.ShotType;
import l2r.gameserver.handler.ISkillHandler;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2SiegeSummonInstance;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.L2SkillType;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Formulas;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Handles Disabler skills
 * @author _drunk_
 */
public class Disablers implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.STUN,
		L2SkillType.ROOT,
		L2SkillType.SLEEP,
		L2SkillType.AGGDAMAGE,
		L2SkillType.MUTE,
		L2SkillType.CONFUSE_MOB_ONLY,
		L2SkillType.PARALYZE,
		L2SkillType.ERASE,
		L2SkillType.BETRAY,
		L2SkillType.DISARM
	};
	
	protected static final Logger _log = LoggerFactory.getLogger(L2Skill.class);
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		L2SkillType type = skill.getSkillType();
		
		byte shld = 0;
		boolean ss = skill.useSoulShot() && activeChar.isChargedShot(ShotType.SOULSHOTS);
		boolean sps = skill.useSpiritShot() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
		boolean bss = skill.useSpiritShot() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
		
		for (L2Object obj : targets)
		{
			if (!(obj instanceof L2Character))
			{
				continue;
			}
			L2Character target = (L2Character) obj;
			if (target.isDead() || (target.isInvul() && !target.isParalyzed()))
			{
				continue;
			}
			
			shld = Formulas.calcShldUse(activeChar, target, skill);
			
			switch (type)
			{
				case BETRAY:
				{
					if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss))
					{
						skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss));
					}
					else
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
						sm.addCharName(target);
						sm.addSkillName(skill);
						activeChar.sendPacket(sm);
					}
					break;
				}
				case ROOT:
				case DISARM:
				case STUN:
				case SLEEP:
				case PARALYZE:
				{
					if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
					{
						target = activeChar;
					}
					
					if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss))
					{
						skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss));
					}
					else
					{
						if (activeChar.isPlayer())
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
							sm.addCharName(target);
							sm.addSkillName(skill);
							activeChar.sendPacket(sm);
						}
					}
					break;
				}
				case MUTE:
				{
					if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
					{
						target = activeChar;
					}
					
					if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss))
					{
						// stop same type effect if available
						List<L2Effect> effects = target.getEffectList().getEffects();
						for (L2Effect e : effects)
						{
							if ((e != null) && (e.getSkill() != null) && (e.getSkill().getSkillType() == type))
							{
								e.exit();
							}
						}
						skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss));
					}
					else
					{
						if (activeChar.isPlayer())
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
							sm.addCharName(target);
							sm.addSkillName(skill);
							activeChar.sendPacket(sm);
						}
					}
					break;
				}
				case CONFUSE_MOB_ONLY:
				{
					// do nothing if not on mob
					if (target.isAttackable())
					{
						if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss))
						{
							List<L2Effect> effects = target.getEffectList().getEffects();
							for (L2Effect e : effects)
							{
								if (e.getSkill().getSkillType() == type)
								{
									e.exit();
								}
							}
							skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss));
						}
						else
						{
							if (activeChar.isPlayer())
							{
								SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
								sm.addCharName(target);
								sm.addSkillName(skill);
								activeChar.sendPacket(sm);
							}
						}
					}
					else
					{
						activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					}
					break;
				}
				case AGGDAMAGE:
				{
					if (target.isAttackable())
					{
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int) ((150 * skill.getPower()) / (target.getLevel() + 7)));
					}
					skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss));
					// TODO: Remove this when lethal effect is done.
					Formulas.calcLethalHit(activeChar, target, skill);
					break;
				}
				case ERASE:
				{
					// Doesn't affect siege golem or wild hog cannon
					if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss) && !(target instanceof L2SiegeSummonInstance))
					{
						final L2PcInstance summonOwner = ((L2Summon) target).getOwner();
						final L2Summon summon = summonOwner.getSummon();
						if (summon != null)
						{
							boolean deleteBuffs = true;
							
							if (summon.isNoblesseBlessedAffected())
							{
								summon.stopEffects(L2EffectType.NOBLESSE_BLESSING);
								deleteBuffs = false;
							}
							if (summon.isResurrectSpecialAffected())
							{
								summon.stopEffects(L2EffectType.RESURRECTION_SPECIAL);
								deleteBuffs = false;
							}
							
							if (deleteBuffs)
							{
								summon.stopAllEffectsExceptThoseThatLastThroughDeath();
							}
							
							summon.abortAttack();
							summon.abortCast();
							summon.unSummon(summonOwner);
							summonOwner.sendPacket(SystemMessageId.YOUR_SERVITOR_HAS_VANISHED);
						}
					}
					else
					{
						if (activeChar.isPlayer())
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
							sm.addCharName(target);
							sm.addSkillName(skill);
							activeChar.sendPacket(sm);
						}
					}
					break;
				}
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
		
		activeChar.setChargedShot(bss ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, false);
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
