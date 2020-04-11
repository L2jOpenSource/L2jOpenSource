package com.l2jfrozen.gameserver.handler.skillhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.BaseStats;
import com.l2jfrozen.gameserver.skills.Formulas;
import com.l2jfrozen.gameserver.skills.Stats;
import com.l2jfrozen.gameserver.templates.L2WeaponType;
import com.l2jfrozen.gameserver.util.Util;

/**
 * @author Steuf-Shyla-L2jFrozen
 */
public class Blow implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.BLOW
	};
	
	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}
		
		final boolean bss = activeChar.checkBss();
		final boolean sps = activeChar.checkSps();
		final boolean ss = activeChar.checkSs();
		
		Formulas.getInstance();
		
		for (final L2Character target : (L2Character[]) targets)
		{
			if (target.isAlikeDead())
			{
				continue;
			}
			
			// Check firstly if target dodges skill
			final boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(target, skill);
			
			byte successChance = 0;// = SIDE;
			
			if (skill.getName().equals("Backstab"))
			{
				if (activeChar.isBehindTarget())
				{
					successChance = (byte) Config.BACKSTAB_ATTACK_BEHIND;
				}
				else if (activeChar.isFrontTarget())
				{
					successChance = (byte) Config.BACKSTAB_ATTACK_FRONT;
				}
				else
				{
					successChance = (byte) Config.BACKSTAB_ATTACK_SIDE;
				}
			}
			else
			{
				if (activeChar.isBehindTarget())
				{
					successChance = (byte) Config.BLOW_ATTACK_BEHIND;
				}
				else if (activeChar.isFrontTarget())
				{
					successChance = (byte) Config.BLOW_ATTACK_FRONT;
				}
				else
				{
					successChance = (byte) Config.BLOW_ATTACK_SIDE;
				}
			}
			
			// If skill requires Crit or skill requires behind,
			// calculate chance based on DEX, Position and on self BUFF
			/*
			 * if ((skill.getCondition() & L2Skill.COND_BEHIND) != 0) { if (skill.getName().equals("Backstab")) { successChance = (byte) Config.BACKSTAB_ATTACK_BEHIND; } else { successChance = (byte) Config.BLOW_ATTACK_BEHIND; } }
			 */
			
			boolean success = true;
			
			if ((skill.getCondition() & L2Skill.COND_CRIT) != 0)
			{
				success = (success && Formulas.getInstance().calcBlow(activeChar, target, successChance));
			}
			
			if (!skillIsEvaded && success)
			{
				// no reflection implemented
				// final byte reflect = Formulas.getInstance().calcSkillReflect(target, skill);
				
				if (skill.hasEffects())
				{
					/*
					 * if (reflect == Formulas.getInstance().SKILL_REFLECT_SUCCEED) { activeChar.stopSkillEffects(skill.getId()); skill.getEffects(target, activeChar); SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT); sm.addSkillName(skill); activeChar.sendPacket(sm); } else {
					 */
					// no shield reflection
					// final byte shld = Formulas.getInstance().calcShldUse(activeChar, target, skill);
					target.stopSkillEffects(skill.getId());
					// if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, shld, false, false, true))
					
					if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						// skill.getEffects(activeChar, target, new Env(shld, false, false, false));
						skill.getEffects(activeChar, target, ss, sps, bss);
						final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(skill);
						target.sendPacket(sm);
					}
					else
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.ATTACK_FAILED);
						sm.addSkillName(skill);
						activeChar.sendPacket(sm);
						return;
					}
					// }
				}
				
				final L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
				boolean soul = false;
				if (weapon != null)
				{
					soul = (ss && (weapon.getItemType() == L2WeaponType.DAGGER));
				}
				
				// byte shld = Formulas.getInstance().calcShldUse(activeChar, target, skill);
				final boolean shld = Formulas.calcShldUse(activeChar, target);
				
				// Critical hit
				boolean crit = false;
				
				// Critical damage condition is applied for sure if there is skill critical condition
				if ((skill.getCondition() & L2Skill.COND_CRIT) != 0)
				{
					crit = true;
					// if there is not critical condition, calculate critical chance
				}
				else if (Formulas.calcCrit(skill.getBaseCritRate() * 10 * BaseStats.DEX.calcBonus(activeChar)))
				{
					crit = true;
				}
				
				double damage = (int) Formulas.calcBlowDamage(activeChar, target, skill, shld, crit, soul);
				
				// if (soul)
				// weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
				
				if (skill.getDmgDirectlyToHP() && target instanceof L2PcInstance)
				{
					// no vegeange implementation
					final L2Character[] ts =
					{
						target,
						activeChar
					};
					
					/*
					 * This loop iterates over previous array but, if skill damage is not reflected it stops on first iteration (target) and misses activeChar
					 */
					for (final L2Character targ : ts)
					{
						final L2PcInstance player = (L2PcInstance) targ;
						// L2PcInstance player = (L2PcInstance)target;
						if (!player.isInvul())
						{
							// Check and calculate transfered damage
							final L2Summon summon = player.getPet();
							if (summon instanceof L2SummonInstance && Util.checkIfInRange(900, player, summon, true))
							{
								int tDmg = (int) damage * (int) player.getStat().calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0, null, null) / 100;
								
								// Only transfer dmg up to current HP, it should
								// not be killed
								if (summon.getCurrentHp() < tDmg)
								{
									tDmg = (int) summon.getCurrentHp() - 1;
								}
								if (tDmg > 0)
								{
									summon.reduceCurrentHp(tDmg, activeChar);
									damage -= tDmg;
								}
							}
							if (damage >= player.getCurrentHp())
							{
								if (player.isInDuel())
								{
									player.setCurrentHp(1);
								}
								else
								{
									player.setCurrentHp(0);
									if (player.isInOlympiadMode())
									{
										player.abortAttack();
										player.abortCast();
										player.getStatus().stopHpMpRegeneration();
										// player.setIsDead(true);
										player.setIsPendingRevive(true);
										if (player.getPet() != null)
										{
											player.getPet().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
										}
									}
									else
									{
										player.doDie(activeChar);
									}
								}
							}
							else
							{
								player.setCurrentHp(player.getCurrentHp() - damage);
							}
						}
						final SystemMessage smsg = new SystemMessage(SystemMessageId.S1_HIT_YOU_S2_DMG);
						smsg.addString(activeChar.getName());
						smsg.addNumber((int) damage);
						player.sendPacket(smsg);
						
						// stop if no vengeance, so only target will be effected
						if (!player.vengeanceSkill(skill))
						{
							break;
						}
					} // end for
				} // end skill directlyToHp check
				else
				{
					target.reduceCurrentHp(damage, activeChar);
					
					// vengeance reflected damage
					if (target.vengeanceSkill(skill))
					{
						activeChar.reduceCurrentHp(damage, target);
					}
				}
				
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				if (activeChar instanceof L2PcInstance)
				{
					final L2PcInstance activePlayer = (L2PcInstance) activeChar;
					
					activePlayer.sendDamageMessage(target, (int) damage, false, true, false);
					if (activePlayer.isInOlympiadMode() && target instanceof L2PcInstance && ((L2PcInstance) target).isInOlympiadMode() && ((L2PcInstance) target).getOlympiadGameId() == activePlayer.getOlympiadGameId())
					{
						Olympiad.getInstance().notifyCompetitorDamage(activePlayer, (int) damage, activePlayer.getOlympiadGameId());
					}
				}
				
				// Possibility of a lethal strike
				Formulas.calcLethalHit(activeChar, target, skill);
				final PlaySound PlaySound = new PlaySound("skillsound.critical_hit_02");
				activeChar.sendPacket(PlaySound);
				
			}
			else
			{
				if (skillIsEvaded)
				{
					if (target instanceof L2PcInstance)
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.AVOIDED_S1S_ATTACK);
						sm.addString(activeChar.getName());
						((L2PcInstance) target).sendPacket(sm);
					}
				}
				
				final SystemMessage sm = new SystemMessage(SystemMessageId.ATTACK_FAILED);
				sm.addSkillName(skill);
				activeChar.sendPacket(sm);
				return;
			}
			
			// Self Effect
			if (skill.hasSelfEffects())
			{
				final L2Effect effect = activeChar.getFirstEffect(skill.getId());
				if (effect != null && effect.isSelfEffect())
				{
					effect.exit(false);
				}
				skill.getEffectsSelf(activeChar);
			}
		}
		
		if (skill.isMagic())
		{
			if (bss)
			{
				activeChar.removeBss();
			}
			else if (sps)
			{
				activeChar.removeSps();
			}
		}
		else
		{
			activeChar.removeSs();
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}