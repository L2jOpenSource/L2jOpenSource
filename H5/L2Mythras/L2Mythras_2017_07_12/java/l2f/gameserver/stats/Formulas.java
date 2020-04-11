package l2f.gameserver.stats;

import l2f.commons.util.Rnd;
import l2f.gameserver.BalancerConfig;
import l2f.gameserver.Config;
import l2f.gameserver.model.Creature;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.Skill;
import l2f.gameserver.model.Skill.SkillType;
import l2f.gameserver.model.Summon;
import l2f.gameserver.model.base.BaseStats;
import l2f.gameserver.model.base.Element;
import l2f.gameserver.model.base.SkillTrait;
import l2f.gameserver.model.instances.ReflectionBossInstance;
import l2f.gameserver.model.items.Inventory;
import l2f.gameserver.model.items.ItemInstance;
import l2f.gameserver.network.serverpackets.SystemMessage;
import l2f.gameserver.network.serverpackets.SystemMessage2;
import l2f.gameserver.network.serverpackets.components.SystemMsg;
import l2f.gameserver.skills.EffectType;
import l2f.gameserver.skills.effects.EffectTemplate;
import l2f.gameserver.stats.funcs.FuncEnchant;
import l2f.gameserver.templates.item.WeaponTemplate;
import l2f.gameserver.utils.PositionUtils;

public class Formulas
{
	public static double calcHpRegen(Creature cha)
	{
		double init = cha.isPlayer() ? ((cha.getLevel() <= 10 ? 1.5 + (cha.getLevel() / 20.) : 1.4 + (cha.getLevel() / 10.)) * cha.getLevelMod()) : cha.getTemplate().baseHpReg;

		if (cha.isPlayable())
		{
			init *= BaseStats.CON.calcBonus(cha);
			if (cha.isSummon())
			{
				init *= 2;
			}
		}

		return cha.calcStat(Stats.REGENERATE_HP_RATE, init, null, null);
	}

	public static double calcMpRegen(Creature cha)
	{
		double init = cha.isPlayer() ? ((.87 + (cha.getLevel() * .03)) * cha.getLevelMod()) : cha.getTemplate().baseMpReg;

		if (cha.isPlayable())
		{
			init *= BaseStats.MEN.calcBonus(cha);
			if (cha.isSummon())
			{
				init *= 2;
			}
		}

		return cha.calcStat(Stats.REGENERATE_MP_RATE, init, null, null);
	}

	public static double calcCpRegen(Creature cha)
	{
		double init = (1.5 + (cha.getLevel() / 10)) * cha.getLevelMod() * BaseStats.CON.calcBonus(cha);
		return cha.calcStat(Stats.REGENERATE_CP_RATE, init, null, null);
	}

	public static class AttackInfo
	{
		public double damage = 0;
		public double defence = 0;
		public double crit_static = 0;
		public double death_rcpt = 0;
		public double lethal1 = 0;
		public double lethal2 = 0;
		public double lethal_dmg = 0;
		public boolean crit = false;
		public boolean shld = false;
		public boolean lethal = false;
		public boolean miss = false;
	}

	/**
	 * For simple strokes patk = patk a critical simple stroke: patk = patk * (1
	 * + crit_damage_rcpt) * crit_damage_mod + crit_damage_static to blow skill
	 * TODO To skillovyh crits, damage just doubled buffs have no effect (except
	 * for blow, for them above) patk = (1 + crit_damage_rcpt) * (patk +
	 * Skill_power) For normal attacks damage = patk * ss_bonus * 70 / pdef
	 * 
	 * @param attacker
	 * @param target
	 * @param skill
	 * @param dual
	 * @param blow
	 * @param ss
	 * @param onCrit
	 * @return
	 */
	@SuppressWarnings("incomplete-switch")
	public static AttackInfo calcPhysDam(Creature attacker, Creature target, Skill skill, boolean dual, boolean blow, boolean ss, boolean onCrit)
	{
		AttackInfo info = new AttackInfo();

		info.damage = attacker.getPAtk(target);

		info.crit_static = attacker.calcStat(Stats.CRITICAL_DAMAGE_STATIC, target, skill);
		info.death_rcpt = 0.01 * target.calcStat(Stats.DEATH_VULNERABILITY, attacker, skill);
		info.lethal1 = skill == null ? 0 : skill.getLethal1() * BalancerConfig.LETHAL1_CHANCE * info.death_rcpt;
		info.lethal2 = skill == null ? 0 : skill.getLethal2() * BalancerConfig.LETHAL2_CHANCE * info.death_rcpt;
		info.crit = Rnd.chance(calcCrit(attacker, target, skill, blow));
		info.shld = ((skill == null) || !skill.getShieldIgnore()) && Formulas.calcShldUse(attacker, target);
		info.lethal = false;
		info.miss = false;
		boolean isPvP = attacker.isPlayable() && target.isPlayable();

		info.defence = Math.max(info.shld ? target.getPDef(attacker) + target.getShldDef() : target.getPDef(attacker), 1);

		// TODO: New Summon Calculator
		if (attacker instanceof Summon)
		{
			if (ss)
				info.damage *= 1.8;

			if (target instanceof Player)
				info.damage *= 0.9;

			info.damage *= 1 + (((Rnd.get() * attacker.getRandomDamage() * 1.8) - attacker.getRandomDamage()) / 100);

			if (dual)
				info.damage /= 2.;

			if (info.crit)
			{
				info.damage *= 0.01 * attacker.calcStat(Stats.CRITICAL_DAMAGE, target, skill);
				info.damage = 1.8 * target.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, info.damage, attacker, skill);
				info.damage += info.crit_static;
			}

			info.damage *= 70. / info.defence;
			info.damage = attacker.calcStat(Stats.PHYSICAL_DAMAGE, info.damage, target, skill);

			return info;
		}

		if (ss)
			info.damage *= blow ? 1.0 : 2.0;

		if (skill != null)
		{
			if (!blow && !target.isLethalImmune())
			{
				if (Rnd.chance(info.lethal1))
				{
					if (target.isPlayer())
					{
						info.lethal = true;
						info.lethal_dmg = target.getCurrentCp();
						target.sendPacket(SystemMsg.YOUR_CP_WAS_DRAINED_BECAUSE_YOU_WERE_HIT_WITH_A_CP_SIPHON_SKILL);
					} else
					{
						info.lethal_dmg = target.getCurrentHp() / 2;
					}
					attacker.sendPacket(SystemMsg.CP_SIPHON);
				} else if (Rnd.chance(info.lethal2))
				{
					if (target.isPlayer())
					{
						info.lethal = true;
						info.lethal_dmg = (target.getCurrentHp() + target.getCurrentCp()) - 1.1; // Oly
																									// \
																									// Duel
																									// hack
																									// installation
																									// is
																									// not
																									// exactly
																									// 1
																									// HP,
																									// and
																									// a
																									// little
																									// more
																									// to
																									// prevent
																									// psevdosmerti
						target.sendPacket(SystemMsg.LETHAL_STRIKE);
					} else
					{
						info.lethal_dmg = target.getCurrentHp() - 1;
					}
					attacker.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
				}
			}

			// If skill does not have his strength is useless to go further, you
			// can immediately return to damage from flying
			if (skill.getPower(target) == 0)
			{
				info.damage = 0; // normal damage in this case does not apply
				return info;
			}

			info.damage += Math.max(0.0D, attacker.calcStat(Stats.SKILL_POWER, skill.getPower(target)));

			if (blow)
			{
				if (!skill.isBehind() && ss)
				{
					if (attacker.isPlayer() && target.isPlayer())
					{
						if (attacker.isInOlympiadMode() && target.isInOlympiadMode())
							info.damage *= BalancerConfig.OLY_BLOW_NOT_BEHIND_DAMAGE;
						else
							info.damage *= BalancerConfig.BLOW_NOT_BEHIND_DAMAGE;
					} else
						info.damage *= BalancerConfig.BLOW_NOT_BEHIND_DAMAGE;
				} else if (skill.isBehind() && ss)
				{
					if (attacker.isPlayer() && target.isPlayer())
					{
						if (attacker.isInOlympiadMode() && target.isInOlympiadMode())
							info.damage *= BalancerConfig.OLY_BLOW_BEHIND_DAMAGE;
						else
							info.damage *= BalancerConfig.BLOW_BEHIND_DAMAGE;
					} else
						info.damage *= BalancerConfig.BLOW_BEHIND_DAMAGE;
				}

				info.damage *= 0.01 * attacker.calcStat(Stats.CRITICAL_DAMAGE, target, skill);
				info.damage = target.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, info.damage, attacker, skill);
				info.damage += 6.1 * info.crit_static;
			}

			// Rechargeable skills have permanent damage
			if (skill.isChargeBoost())
			{
				info.damage *= 0.8 + (0.2 * attacker.getIncreasedForce());
			} else if (!skill.isChargeBoost())
			{
				info.damage *= 1 + (((Rnd.get() * attacker.getRandomDamage() * 2) - attacker.getRandomDamage()) / 100);
			}

			if (skill.getSkillType() == SkillType.CHARGE)
			{
				info.damage *= 1.3;
			} else if (skill.isSoulBoost())
			{
				info.damage *= 1.0 + (0.06 * Math.min(attacker.getConsumedSouls(), 5));
			}

			// Gracia Physical Skill Damage Bonus
			info.damage *= 1.10113;

			if (info.crit)
			{
				// Rechargeable skills ignore reducing Critical stats
				if (skill.isChargeBoost() || (skill.getSkillType() == SkillType.CHARGE) || !blow)
				{
					info.damage *= 2;
				} else
				{
					info.damage = 2 * target.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, info.damage, attacker, skill);
				}
			}
		} else
		{
			info.damage *= 1 + (((Rnd.get() * attacker.getRandomDamage() * 2) - attacker.getRandomDamage()) / 100);

			if (dual)
			{
				info.damage /= 2.;
			}

			if (info.crit)
			{
				info.damage *= 0.01 * attacker.calcStat(Stats.CRITICAL_DAMAGE, target, skill);
				info.damage = 2 * target.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, info.damage, attacker, skill);
				info.damage += info.crit_static;
			}
		}

		if (info.crit)
		{
			// Absorption chance soul (no animation) on crit if Soul Mastery
			// Level 4 or higher
			int chance = attacker.getSkillLevel(Skill.SKILL_SOUL_MASTERY);
			if (chance > 0)
			{
				if (chance >= 21)
				{
					chance = 30;
				} else if (chance >= 15)
				{
					chance = 25;
				} else if (chance >= 9)
				{
					chance = 20;
				} else if (chance >= 4)
				{
					chance = 15;
				}
				if (Rnd.chance(chance))
				{
					attacker.setConsumedSouls(attacker.getConsumedSouls() + 1, null);
				}
			}
		}

		switch (PositionUtils.getDirectionTo(target, attacker))
		{
			case BEHIND:
				info.damage *= 1.2;
				break;
			case SIDE:
				info.damage *= 1.1;
				break;
		}

		info.damage *= 70. / info.defence;
		info.damage = attacker.calcStat(Stats.PHYSICAL_DAMAGE, info.damage, target, skill);

		if (info.shld && Rnd.chance(5))
		{
			info.damage = 1;
		}

		if (isPvP)
		{
			if (skill == null)
			{
				info.damage *= attacker.calcStat(Stats.PVP_PHYS_DMG_BONUS, 1, null, null);
				info.damage /= target.calcStat(Stats.PVP_PHYS_DEFENCE_BONUS, 1, null, null);
			} else
			{
				info.damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG_BONUS, 1, null, null);
				info.damage /= target.calcStat(Stats.PVP_PHYS_SKILL_DEFENCE_BONUS, 1, null, null);
			}
			if (attacker.isPlayer())
			{
				// ClassId clazz = attacker.getPlayer().getClassId();
				// if(clazz == ClassId.sagittarius || clazz ==
				// ClassId.ghostSentinel || clazz == ClassId.moonlightSentinel)
				// info.damage *= 1.3;
			}
		}

		// Then only check if skill! = Null, since Player.onHitTimer not cheat
		// damage.
		if (skill != null)
		{
			if (info.shld)
			{
				if (info.damage == 1)
				{
					target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
				} else
				{
					target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
				}
			}

			// Flee from physical attack skills leads to 0
			// if ((info.damage > 1.0) && !skill.hasEffects() &&
			// Rnd.chance(target.calcStat(Stats.PSKILL_EVASION, 0.0, attacker,
			// skill)))
			if ((info.damage > 1.0) && Rnd.chance(target.calcStat(Stats.PSKILL_EVASION, 0.0, attacker, skill)))
			{
				attacker.sendPacket(new SystemMessage2(SystemMsg.C1S_ATTACK_WENT_ASTRAY).addName(attacker));
				target.sendPacket(new SystemMessage2(SystemMsg.C1_HAS_EVADED_C2S_ATTACK).addName(target).addName(attacker));
				info.damage = 0.0;
			}

			if ((info.damage > 1.0) && skill.isDeathlink())
			{
				info.damage *= 1.8 * (1.0 - attacker.getCurrentHpRatio());
			}

			if (onCrit && !calcBlow(attacker, target, skill))
			{
				info.miss = true;
				info.damage = 0.0;
				attacker.sendPacket(new SystemMessage2(SystemMsg.C1S_ATTACK_WENT_ASTRAY).addName(attacker));
			}

			if (blow)
			{
				if (Rnd.chance(info.lethal1))
				{
					if (target.isPlayer())
					{
						info.lethal = true;
						info.lethal_dmg = target.getCurrentCp();
						target.sendPacket(SystemMsg.YOUR_CP_WAS_DRAINED_BECAUSE_YOU_WERE_HIT_WITH_A_CP_SIPHON_SKILL);
					} else if (target.isLethalImmune())
						info.damage *= BalancerConfig.LETHAL_IMMUNE_TARGET_BOOST_DAMAGE;
					else
						info.lethal_dmg = target.getCurrentHp() / 2.0;

					if (info.lethal_dmg > 0)
						attacker.sendPacket(SystemMsg.CP_SIPHON);
				} else if (Rnd.chance(info.lethal2))
				{
					if (target.isPlayer())
					{
						info.lethal = true;
						info.lethal_dmg = (target.getCurrentHp() + target.getCurrentCp()) - 1.1;
						target.sendPacket(SystemMsg.LETHAL_STRIKE);
					} else if (target.isLethalImmune())
						info.damage *= BalancerConfig.LETHAL_IMMUNE_TARGET_BOOST_DAMAGE;
					else
						info.lethal_dmg = target.getCurrentHp() - 1.0;

					if (info.lethal_dmg > 0)
						attacker.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
				}
			}

			if (info.damage > 0.0)
			{
				attacker.displayGiveDamageMessage(target, (int) info.damage, info.crit || blow, false, false, false);
			}

			if (target.isStunned() && calcStunBreak(info.crit))
			{
				target.getEffectList().stopEffects(EffectType.Stun);
			}

			if (calcCastBreak(target, info.crit))
			{
				target.abortCast(false, true);
			}
		}

		if (attacker instanceof Player && target instanceof Player)
			info.damage = DamageBalancer.optimizer(((Player) attacker), ((Player) target), info.damage, info.crit, false);

		return info;
	}

	public static double calcMagicDam(Creature attacker, Creature target, Skill skill, int sps)
	{
		final boolean isCubic = skill.getMatak() > 0;
		boolean isPvP = attacker.isPlayable() && target.isPlayable();
		// ShieldIgnore option for magical skills is inverted
		boolean shield = skill.getShieldIgnore() && calcShldUse(attacker, target);

		int levelDiff = target.getLevel() - attacker.getLevel();

		double mAtk = attacker.getMAtk(target, skill);

		if (sps == 2)
			mAtk *= 4;
		else if (sps == 1)
			mAtk *= 2;

		double mdef = target.getMDef(null, skill);

		if (shield)
			mdef += target.getShldDef();

		if (mdef <= 0)
			mdef = 1;

		double power = skill.getPower(target);
		double lethalDamage = 0;

		if (Rnd.chance(skill.getLethal1()))
		{
			if (target.isPlayer())
			{
				lethalDamage = target.getCurrentCp();
				target.sendPacket(SystemMsg.YOUR_CP_WAS_DRAINED_BECAUSE_YOU_WERE_HIT_WITH_A_CP_SIPHON_SKILL);
			} else if (target.isLethalImmune())
				power *= BalancerConfig.LETHAL_IMMUNE_TARGET_BOOST_DAMAGE;
			else
				lethalDamage = target.getCurrentHp() / 2.0;

			if (lethalDamage > 0)
				attacker.sendPacket(SystemMsg.CP_SIPHON);
		} else if (Rnd.chance(skill.getLethal2()))
		{
			if (levelDiff <= 9)
			{
				if (target.isPlayer())
				{
					lethalDamage = (target.getCurrentHp() + target.getCurrentCp()) - 1.1;
					target.sendPacket(SystemMsg.LETHAL_STRIKE);
				} else if (target.isLethalImmune())
					power *= BalancerConfig.LETHAL_IMMUNE_TARGET_BOOST_DAMAGE;
				else
					lethalDamage = target.getCurrentHp() - 1;

				if (lethalDamage > 0)
					attacker.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
			} else
			{
				lethalDamage = 0;
				power = 0;
			}
		}

		if (power == 0)
		{
			if (lethalDamage > 0)
			{
				attacker.displayGiveDamageMessage(target, (int) lethalDamage, false, false, false, false);
			}
			return lethalDamage;
		}

		if (skill.isSoulBoost())
		{
			power *= 1.0 + (0.06 * Math.min(attacker.getConsumedSouls(), 5));
		}

		double damage = (91 * power * Math.sqrt(mAtk)) / mdef;

		damage *= 1 + (((Rnd.get() * attacker.getRandomDamage() * 2) - attacker.getRandomDamage()) / 100);

		boolean crit = calcMCrit(attacker.getMagicCriticalRate(target, skill));

		if (crit)
		{
			if (!isCubic)
				damage *= attacker.calcStat(Stats.MCRITICAL_DAMAGE, attacker.isPlayable() && target.isPlayable() ? BalancerConfig.MAGIC_CRITICAL_DAMAGE_ON_PLAYERS : BalancerConfig.MAGIC_CRITICAL_DAMAGE_GENERAL, target, skill);
			else
				damage *= (target.isPlayable() ? BalancerConfig.MAGIC_CRITICAL_DAMAGE_ON_PLAYERS : BalancerConfig.MAGIC_CRITICAL_DAMAGE_GENERAL);
		}

		if (!isCubic)
			damage = attacker.calcStat(Stats.MAGIC_DAMAGE, damage, target, skill);

		if (shield)
		{
			if (Rnd.chance(5))
			{
				damage = 0;
				target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
				attacker.sendPacket(new SystemMessage2(SystemMsg.C1_RESISTED_C2S_MAGIC).addName(target).addName(attacker));
			} else
			{
				target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
				attacker.sendPacket(new SystemMessage2(SystemMsg.YOUR_OPPONENT_HAS_RESISTANCE_TO_MAGIC_THE_DAMAGE_WAS_DECREASED));
			}
		}

		if ((damage > 1) && skill.isDeathlink())
		{
			damage *= BalancerConfig.CURSE_DEATH_LINK_MUL * (1.0 - attacker.getCurrentHpRatio());
		}

		if ((damage > 1) && skill.isBasedOnTargetDebuff())
		{
			damage *= 1 + (0.05 * target.getEffectList().getAllEffects().size());
		}

		damage += lethalDamage;

		if (skill.getSkillType() == SkillType.MANADAM)
		{
			damage = Math.max(1, damage / 4.);
		}

		if (isPvP && (damage > 1))
		{
			if (!isCubic)
				damage *= attacker.calcStat(Stats.PVP_MAGIC_SKILL_DMG_BONUS, 1, null, null);
			damage /= target.calcStat(Stats.PVP_MAGIC_SKILL_DEFENCE_BONUS, 1, null, null);
		}

		double magic_rcpt = target.calcStat(Stats.MAGIC_RESIST, attacker, skill);
		if (!isCubic)
			magic_rcpt -= attacker.calcStat(Stats.MAGIC_POWER, target, skill);

		double failChance = 4. * Math.max(1., levelDiff) * (1. + (magic_rcpt / 100.));
		if (Rnd.chance(failChance))
		{
			if (levelDiff > 9)
			{
				damage = 0;
				SystemMessage msg = new SystemMessage(SystemMessage.C1_RESISTED_C2S_MAGIC).addName(target).addName(attacker);
				attacker.sendPacket(msg);
				target.sendPacket(msg);
			} else
			{
				damage /= 2;
				SystemMessage msg = new SystemMessage(SystemMessage.DAMAGE_IS_DECREASED_BECAUSE_C1_RESISTED_AGAINST_C2S_MAGIC).addName(target).addName(attacker);
				attacker.sendPacket(msg);
				target.sendPacket(msg);
			}
		}

		if (attacker instanceof Player && target instanceof Player)
			damage = DamageBalancer.optimizer(((Player) attacker), ((Player) target), damage, crit, true);

		if (damage > 0)
			attacker.displayGiveDamageMessage(target, (int) damage, crit, false, false, true);

		if (calcCastBreak(target, crit))
			target.abortCast(false, true);

		return damage;
	}

	public static boolean calcStunBreak(boolean crit)
	{
		return Rnd.chance(crit ? 75 : 10);
	}

	/**
	 * @param activeChar
	 * @param target
	 * @param skill
	 * @return Returns true in case of fatal blow success
	 */
	@SuppressWarnings("incomplete-switch")
	public static boolean calcBlow(Creature activeChar, Creature target, Skill skill)
	{
		WeaponTemplate weapon = activeChar.getActiveWeaponItem();

		double base_weapon_crit = weapon == null ? 4. : weapon.getCritical();
		double crit_height_bonus = (0.008 * Math.min(25, Math.max(-25, target.getZ() - activeChar.getZ()))) + 1.1;
		double buffs_mult = activeChar.calcStat(Stats.FATALBLOW_RATE, target, skill);
		double skill_mod = skill.isBehind() ? 5 : 4; // CT 2.3 blowrate increase

		double chance = base_weapon_crit * buffs_mult * crit_height_bonus * skill_mod;

		if (!target.isInCombat())
		{
			chance *= 1.1;
		}

		switch (PositionUtils.getDirectionTo(target, activeChar))
		{
			case BEHIND:
				chance *= 1.3;
				break;
			case SIDE:
				chance *= 1.1;
				break;
			case FRONT:
				if (skill.isBehind())
				{
					chance = 3.0;
				}
				break;
		}
		chance = Math.min(skill.isBehind() ? 100 : 80, chance);
		return Rnd.chance(chance);
	}

	@SuppressWarnings("incomplete-switch")
	public static double calcCrit(Creature attacker, Creature target, Skill skill, boolean blow)
	{
		if (attacker.isPlayer() && (attacker.getActiveWeaponItem() == null))
		{
			return 0;
		}
		if (skill != null)
		{
			return skill.getCriticalRate() * (blow ? BaseStats.DEX.calcBonus(attacker) : BaseStats.STR.calcBonus(attacker)) * 0.01 * attacker.calcStat(Stats.SKILL_CRIT_CHANCE_MOD, target, skill);
		}

		double rate = attacker.getCriticalHit(target, null) * 0.01 * target.calcStat(Stats.CRIT_CHANCE_RECEPTIVE, attacker, skill);

		switch (PositionUtils.getDirectionTo(target, attacker))
		{
			case BEHIND:
				rate *= 1.4;
				break;
			case SIDE:
				rate *= 1.2;
				break;
		}

		return rate / 10;
	}

	public static boolean calcMCrit(double mRate)
	{
		// floating point random gives more accuracy calculation, because
		// argument also floating point
		return (Rnd.get() * 100) <= Math.min(BalancerConfig.LIM_MCRIT, mRate);
	}

	public static boolean calcCastBreak(Creature target, boolean crit)
	{
		if ((target == null) || target.isInvul() || target.isRaid() || !target.isCastingNow())
		{
			return false;
		}
		Skill skill = target.getCastingSkill();
		if ((skill != null) && ((skill.getSkillType() == SkillType.TAKECASTLE) || (skill.getSkillType() == SkillType.TAKEFORTRESS) || (skill.getSkillType() == SkillType.TAKEFLAG)))
		{
			return false;
		}
		return Rnd.chance(target.calcStat(Stats.CAST_INTERRUPT, crit ? 75 : 10, null, skill));
	}

	/**
	 * Calculate delay (in milliseconds) before next ATTACK
	 * 
	 * @param rate
	 * @return
	 */
	public static int calcPAtkSpd(double rate)
	{
		/*
		 * return (int) (500000.0 / rate); // in milliseconds, so 500 * 1000
		 */
		// attack speed 312 equals 1500 ms delay... (or 300 + 40 ms delay?)
		if (rate < 2)
			return 2700;
		return (int) (470000 / rate);
	}

	/**
	 * Calculate delay (in milliseconds) for skills cast
	 * 
	 * @param attacker
	 * @param skill
	 * @param skillTime
	 * @return
	 */
	public static int calcMAtkSpd(Creature attacker, Skill skill, double skillTime)
	{
		if (skill.isMagic())
		{
			return (int) ((skillTime * 333) / Math.max(attacker.getMAtkSpd(), 1));
		}
		return (int) ((skillTime * 333) / Math.max(attacker.getPAtkSpd(), 1));
	}

	/** Calculate reuse delay (in milliseconds) for skills */
	public static long calcSkillReuseDelay(Creature actor, Skill skill)
	{
		long reuseDelay = skill.getReuseDelay(actor);
		if (actor.isMonster())
		{
			reuseDelay = skill.getReuseForMonsters();
		}
		if (skill.isReuseDelayPermanent() || skill.isHandler() || skill.isItemSkill())
		{
			return reuseDelay;
		}
		if (actor.getSkillMastery(skill.getId()) == 1)
		{
			actor.removeSkillMastery(skill.getId());
			return 0;
		}
		if (skill.isMagic())
		{
			return (long) actor.calcStat(Stats.MAGIC_REUSE_RATE, reuseDelay, null, skill);
		}
		return (long) actor.calcStat(Stats.PHYSIC_REUSE_RATE, reuseDelay, null, skill);
	}

	/** Returns true if hit missed (target evaded) */
	@SuppressWarnings("incomplete-switch")
	public static boolean calcHitMiss(Creature attacker, Creature target)
	{
		int chanceToHit = 88 + (2 * (attacker.getAccuracy() - target.getEvasionRate(attacker)));

		chanceToHit = Math.max(chanceToHit, 28);
		chanceToHit = Math.min(chanceToHit, 98);

		PositionUtils.TargetDirection direction = PositionUtils.getDirectionTo(attacker, target);
		switch (direction)
		{
			case BEHIND:
				chanceToHit *= 1.2;
				break;
			case SIDE:
				chanceToHit *= 1.1;
				break;
		}
		return !Rnd.chance(chanceToHit);
	}

	/**
	 * Returns true if shield defence successfull
	 * 
	 * @param attacker
	 * @param target
	 * @return
	 */
	public static boolean calcShldUse(Creature attacker, Creature target)
	{
		WeaponTemplate template = target.getSecondaryWeaponItem();
		if ((template == null) || (template.getItemType() != WeaponTemplate.WeaponType.NONE))
		{
			return false;
		}
		int angle = (int) target.calcStat(Stats.SHIELD_ANGLE, attacker, null);
		if (!PositionUtils.isFacing(target, attacker, angle))
		{
			return false;
		}
		return Rnd.chance((int) target.calcStat(Stats.SHIELD_RATE, attacker, null));
	}

	private static double getAttackerWeaponMod(Creature caster)
	{
		Player player = caster.getPlayer();
		ItemInstance weapon = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (weapon == null)
			return 0.0;
		return weapon.getStatFunc(Stats.MAGIC_ATTACK) + FuncEnchant.valueToAdd(Stats.MAGIC_ATTACK, weapon);
	}

	private static final int[] DEFENDER_JEWELRY_SLOTS =
	{ Inventory.PAPERDOLL_LEAR, Inventory.PAPERDOLL_REAR, Inventory.PAPERDOLL_LFINGER, Inventory.PAPERDOLL_RFINGER, Inventory.PAPERDOLL_NECK };

	private static double getDefenderJewelryMod(Creature target)
	{
		Player player = target.getPlayer();
		double totalMDef = 0.0;

		for (int slot : DEFENDER_JEWELRY_SLOTS)
		{
			ItemInstance item = player.getInventory().getPaperdollItem(slot);
			if (item != null)
			{
				totalMDef += item.getStatFunc(Stats.MAGIC_DEFENCE) + FuncEnchant.valueToAdd(Stats.MAGIC_DEFENCE, item);
			}
		}
		return totalMDef;
	}

	public static boolean calcSkillSuccess(Creature player, Creature target, Skill skill, int activateRate)
	{
		Env env = new Env();
		env.character = player;
		env.target = target;
		env.skill = skill;
		env.value = activateRate;
		return calcSkillSuccess(env, null, player.getChargedSpiritShot());
	}

	public static boolean calcSkillSuccess(Env env, EffectTemplate et, int spiritshot)
	{
		if (env.value == -1)
			return true;

		// Skill Base Chance
		if (env.character instanceof Player)
		{
			//env.character.sendMessage("1: " + env.value);
		}
		env.value = Math.max(Math.min(env.value, 100), 1);
		if (env.character instanceof Player)
		{
			//env.character.sendMessage("2: " + env.value);
		}

		final Skill skill = env.skill;

		if (BalancerConfig.CUSTOM_CHANCE_SKILLS_ENABLED)
		{
			if (BalancerConfig.CUSTOM_CHANCE_SKILLS.containsKey(skill.getId()))
			{
				env.value = BalancerConfig.CUSTOM_CHANCE_SKILLS.get(skill.getId());
				if (env.character instanceof Player)
				{
					//env.character.sendMessage("3: " + env.value);
				}
				if (BalancerConfig.USE_METHOD_CHANCE_WITHOUT_RESISTS)
					return Rnd.chance((int) env.value);
			}
		}

		if (!skill.isOffensive())
		{
			return Rnd.chance(env.value);
		}

		final double base = env.value;
		final Creature caster = env.character;
		final Creature target = env.target;

		boolean debugCaster = false;
		boolean debugTarget = false;
		boolean debugGlobal = false;

		if (Config.ALT_DEBUG_ENABLED || caster.getAccessLevel() > 0)
		{
			debugCaster = (caster.getPlayer() != null) && (caster.getPlayer().isDebug() || caster.getAccessLevel() > 0);
			debugTarget = (target.getPlayer() != null) && target.getPlayer().isDebug();
			final boolean debugPvP = Config.ALT_DEBUG_PVP_ENABLED && (debugCaster && debugTarget) && (!Config.ALT_DEBUG_PVP_DUEL_ONLY || (caster.getPlayer().isInDuel() && target.getPlayer().isInDuel()));
			debugGlobal = debugPvP || (Config.ALT_DEBUG_PVE_ENABLED && ((debugCaster && target.isMonster()) || (debugTarget && caster.isMonster())));
		}

		if (debugCaster && et == null)
			caster.getPlayer().sendMessage("Chance Initial: " + env.value);
		else if (debugCaster && et != null)
			caster.getPlayer().sendMessage("Chance Initial: " + env.value + " effect type " + et.getEffectType().name());

		double mAtkMod = 1.0;
		if (skill.isMagic())
		{
			if (caster.isPlayable() && target.isPlayable())
			{
				int ssMod = 1;
				int mdef = Math.max(1, target.getMDef(target, skill));
				double matk = caster.getMAtk(target, skill);

				if (skill.isSSPossible())
				{
					switch (spiritshot)
					{
						case ItemInstance.CHARGED_BLESSED_SPIRITSHOT:
							ssMod = 4;
							break;
						case ItemInstance.CHARGED_SPIRITSHOT:
							ssMod = 2;
							break;
						default:
							ssMod = 1;
					}
					matk *= ssMod;
				}

				mAtkMod = (BalancerConfig.SKILLS_CHANCE_MOD * Math.pow(matk, BalancerConfig.SKILLS_CHANCE_POW)) / mdef;

				double attackerWeaponMod = Math.max(getAttackerWeaponMod(caster), 1.0);
				double defenderJewelryMod = Math.max(getDefenderJewelryMod(target), 1.0);

				mAtkMod = (attackerWeaponMod * BalancerConfig.SKILLS_ATTACKER_WEAPON_MOD) / defenderJewelryMod;
				mAtkMod = Math.min(BalancerConfig.SKILLS_M_ATK_MOD_MAX, mAtkMod);
				mAtkMod = Math.max(BalancerConfig.SKILLS_M_ATK_MOD_MIN, mAtkMod);

				env.value *= mAtkMod;

				if (debugCaster)
					caster.getPlayer().sendMessage("MatkMod: " + mAtkMod + " chance " + env.value);
			} else if (caster.isMonster())
			{
				env.value *= BalancerConfig.SKILLS_MOB_CHANCE;
			}
		}

		double statMod = 1;
		if (skill.getSaveVs() != null && BalancerConfig.SKILLS_CALC_STAT_MOD)
		{
			statMod -= skill.getSaveVs().calcChanceMod(target);
			env.value *= statMod;

			if (debugCaster)
				caster.getPlayer().sendMessage("SaveVs: " + statMod + " chance " + env.value);
		}

		double deltaMod = 1.0;
		int mLevel = skill.getMagicLevel() == 0 ? caster.getLevel() : skill.getMagicLevel();
		if (mLevel > 0)
		{
			double diff = mLevel - target.getLevel(); // 35 - 85 = -50
			deltaMod = (diff * 0.06) / 10.0;
			deltaMod = 1.0 + deltaMod;

			env.value *= deltaMod;

			if (debugCaster)
				caster.getPlayer().sendMessage("LvlModif: " + deltaMod + " chance " + env.value);
		}

		double resMod = 1.0;
		double debuffMod = 1.0;
		if (!skill.isIgnoreResists())
		{
			// debuffMod = 1.0 - (target.calcStat(Stats.DEBUFF_RESIST, caster,
			// skill) / 120.);
			debuffMod = 1. - (target.calcStat(Stats.DEBUFF_RESIST, 100., caster, skill) - 100.) / 120.;

			if (debuffMod != 1)
			{
				if (debuffMod == Double.NEGATIVE_INFINITY)
				{
					if (debugGlobal)
					{
						if (debugCaster)
						{
							caster.getPlayer().sendMessage("Full debuff immunity");
						}
						if (debugTarget)
						{
							target.getPlayer().sendMessage("Full debuff immunity");
						}
					}
					return false;
				}
				if (debuffMod == Double.POSITIVE_INFINITY)
				{
					if (debugGlobal)
					{
						if (debugCaster)
						{
							caster.getPlayer().sendMessage("Full debuff vulnerability");
						}
						if (debugTarget)
						{
							target.getPlayer().sendMessage("Full debuff vulnerability");
						}
					}
					return true;
				}

				debuffMod = Math.max(debuffMod, 0.0);
				if (caster.isMonster())
				{
					env.value *= debuffMod * BalancerConfig.SKILLS_DEBUFF_MOB_CHANCE;
				} else
				{
					env.value *= debuffMod;
				}

				if (debugCaster)
					caster.getPlayer().sendMessage("DebuffMod: " + debuffMod + " chance " + env.value);
			}

			SkillTrait trait = skill.getTraitType();
			if (trait != null)
			{
				if ((trait == SkillTrait.ETC || trait == SkillTrait.GUST || trait == SkillTrait.HOLD || trait == SkillTrait.SHOCK) && target.isPlayable() && caster.isPlayable())
				{
					resMod = 1.0 - Math.max(trait.calcVuln(env) / 100.0, 0.0);
					env.value *= resMod;
				} else
				{
					double vulnMod = trait.calcVuln(env);
					double profMod = trait.calcProf(env);

					if (vulnMod != 0.0 || profMod != 0.0)
					{
						if (debugCaster)
						{
							caster.getPlayer().sendMessage("vulnMod: " + vulnMod + " chance " + env.value);
							caster.getPlayer().sendMessage("profMod: " + profMod + " chance " + env.value);
						}

						final double maxResist = 90 + (profMod * 0.85);
						resMod = (maxResist - vulnMod) / 60.;

						if (resMod != 1.0)
						{
							if (resMod == Double.NEGATIVE_INFINITY)
							{
								if (debugGlobal)
								{
									if (debugCaster)
									{
										caster.getPlayer().sendMessage("Full immunity");
									}
									if (debugTarget)
									{
										target.getPlayer().sendMessage("Full immunity");
									}
								}
								return false;
							}
							if (resMod == Double.POSITIVE_INFINITY)
							{
								if (debugGlobal)
								{
									if (debugCaster)
									{
										caster.getPlayer().sendMessage("Full vulnerability");
									}
									if (debugTarget)
									{
										target.getPlayer().sendMessage("Full vulnerability");
									}
								}
								return true;
							}

							resMod = Math.max(resMod, 0.0);
							env.value *= resMod;
						}
					}
				}

				if (debugCaster)
					caster.getPlayer().sendMessage("Trait Res: " + resMod + " chance " + env.value);
			}
		}

		double elementMod = 1.0;
		final Element element = skill.getElement();
		if (element != Element.NONE)
		{
			elementMod = skill.getElementPower();
			elementMod += caster.calcStat(element.getAttack(), 0.0, null, null);

			elementMod -= target.calcStat(element.getDefence(), 0.0);

			elementMod = (elementMod * BalancerConfig.SKILLS_ELEMENT_MOD_MULT) / 300.0;
			elementMod += 1.0;

			elementMod = Math.min(BalancerConfig.SKILLS_ELEMENT_MOD_MAX, elementMod);
			elementMod = Math.max(BalancerConfig.SKILLS_ELEMENT_MOD_MIN, elementMod);
			env.value *= elementMod;

			if (debugCaster)
				caster.getPlayer().sendMessage("Element Mod: " + elementMod + " chance " + env.value);
		}

		if (caster.isMonster())
		{
			env.value *= BalancerConfig.SKILLS_MOB_CHANCE;
		} else if (target.isPlayable())
		{
			final double getReceivedDebuffMod = target.getReceivedDebuffMod(skill.getId(), env.value);
			env.value *= getReceivedDebuffMod;

			if (debugCaster)
				caster.getPlayer().sendMessage("Received DebuffMod: " + getReceivedDebuffMod + " chance " + env.value);
		}

		if (target.isPlayable() && caster.isPlayable() && et != null)
		{
			final double getSkillEffectStatModifier = calcSkillEffectStatModifier(et);
			env.value *= getSkillEffectStatModifier;

			if (debugCaster)
				caster.getPlayer().sendMessage("Received skillEffectStatModifier: " + getSkillEffectStatModifier + " chance " + env.value);
		}

		if (target.isPlayer() && caster.isPlayer())
		{
			if (target.isInOlympiadMode() && caster.isInOlympiadMode() && env.value > BalancerConfig.SKILLS_MAX_CHANCE_SUCCESS_IN_OLYMPIAD)
			{
				env.value = BalancerConfig.SKILLS_MAX_CHANCE_SUCCESS_IN_OLYMPIAD;
			}
		}

		if (debugCaster)
			caster.getPlayer().sendMessage("Chance before optimize: " + env.value);

		if (env.value < BalancerConfig.MINIMUM_CHANCE_SKILLS)
			env.value += (BalancerConfig.MINIMUM_CHANCE_SKILLS - env.value) * BalancerConfig.DELDA_FOR_SKILL_DOWN_OF_MINIMUM;
		if (env.character instanceof Player)
		{
			//env.character.sendMessage("5: " + env.value);
		}
		// final boolean result = Rnd.chance((int) env.value);
		final boolean result = Rnd.chance(env.value);

		if (debugCaster)
			caster.getPlayer().sendMessage("Chance after optimize: " + env.value + " and result: " + result);

		return result;
	}

	public static void calcSkillMastery(Skill skill, Creature activeChar)
	{
		if (skill.isHandler())
		{
			return;
		}

		// Skill id 330 for fighters, 331 for mages
		// Actually only GM can have 2 skill masteries, so let's make them more
		// lucky ^^
		if (calcSkillMasterySuccess(activeChar, skill))
		{
			// byte mastery level, 0 = no skill mastery, 1 = no reuseTime, 2 =
			// buff duration*2, 3 = power*3
			int masteryLevel;
			SkillType type = skill.getSkillType();
			if (skill.isMusic() || (type == SkillType.BUFF) || (type == SkillType.HOT) || (type == SkillType.HEAL_PERCENT))
			{
				masteryLevel = 2;
			} else if (type == SkillType.HEAL)
			{
				masteryLevel = 3;
			} else
			{
				masteryLevel = 1;
			}
			if (masteryLevel > 0)
			{
				activeChar.setSkillMastery(skill.getId(), masteryLevel);
			}
		}
	}

	private static boolean calcSkillMasterySuccess(Creature activeChar, Skill skill)
	{
		if (skill.isIgnoreSkillMastery())
			return false;

		if (activeChar.getSkillLevel(331) > 0 && activeChar.calcStat(Stats.SKILL_MASTERY, activeChar.getINT(), null, skill) >= Rnd.get(1000))
			return true;

		if ((activeChar.getSkillLevel(330) > 0) && (activeChar.calcStat(Stats.SKILL_MASTERY, activeChar.getSTR(), null, skill) >= Rnd.get(1000)))
			return true;

		return false;
	}

	public static double calcDamageResists(Skill skill, Creature attacker, Creature defender, double value)
	{
		if (attacker == defender)
		{
			return value; // TODO: should be considered on a good defense, but
							// because these non-magical skills you should make
							// a separate mechanism
		}

		if (attacker.isBoss())
		{
			value *= Config.RATE_EPIC_ATTACK;
		} else if (attacker.isRaid() || (attacker instanceof ReflectionBossInstance))
		{
			value *= Config.RATE_RAID_ATTACK;
		}

		if (defender.isBoss())
		{
			value /= Config.RATE_EPIC_DEFENSE;
		} else if (defender.isRaid() || (defender instanceof ReflectionBossInstance))
		{
			value /= Config.RATE_RAID_DEFENSE;
		}

		Player pAttacker = attacker.getPlayer();

		// if the player's level is lower than 2 or more mobs 78 + levels, its
		// damage is reduced by mob
		int diff = defender.getLevel() - (pAttacker != null ? pAttacker.getLevel() : attacker.getLevel());
		if (attacker.isPlayable() && defender.isMonster() && (defender.getLevel() >= 78) && (diff > 2))
		{
			value *= .7 / Math.pow(diff - 2, .25);
		}

		Element element = Element.NONE;
		double power = 0.;

		if (skill != null)
		{
			element = skill.getElement();
			power = skill.getElementPower();
		} else
		{
			element = getAttackElement(attacker, defender);
		}

		if (element == Element.NONE)
		{
			return value;
		}

		if ((pAttacker != null) && pAttacker.isGM() && Config.DEBUG)
		{
			pAttacker.sendMessage("Element: " + element.name());
			pAttacker.sendMessage("Attack: " + attacker.calcStat(element.getAttack(), power));
			pAttacker.sendMessage("Defence: " + defender.calcStat(element.getDefence(), 0.));
			pAttacker.sendMessage("Modifier: " + getElementMod(defender.calcStat(element.getDefence(), 0.), attacker.calcStat(element.getAttack(), power)));
		}

		return value * getElementMod(defender.calcStat(element.getDefence(), 0.), attacker.calcStat(element.getAttack(), power));
	}

	private static double getElementMod(double defense, double attack)
	{
		double diff = attack - defense;
		if (diff <= 0)
		{
			return 1.0;
		} else if (diff < 50)
		{
			return 1.0 + (diff * 0.003948);
		} else if (diff < 150)
		{
			return 1.2;
		} else if (diff < 300)
		{
			return 1.4;
		} else
		{
			return 1.7;
		}
	}

	public static Element getAttackElement(Creature attacker, Creature target)
	{
		double val, max = Double.MIN_VALUE;
		Element result = Element.NONE;
		for (Element e : Element.VALUES)
		{
			val = attacker.calcStat(e.getAttack(), 0., null, null);
			if (val <= 0.)
			{
				continue;
			}

			if (target != null)
			{
				val -= target.calcStat(e.getDefence(), 0., null, null);
			}

			if (val > max)
			{
				result = e;
				max = val;
			}
		}

		return result;
	}

	private static double calcSkillEffectStatModifier(EffectTemplate et)
	{
		double multiplierRate = 1;

		switch (et.getEffectType())
		{
			case Stun:
				multiplierRate = BalancerConfig.SKILLS_CHANCE_STUN;
				break;
			case RemoveTarget:
				multiplierRate = BalancerConfig.SKILLS_CHANCE_REMOVE_TARGET;
				break;
			default:
				return multiplierRate;
		}

		return Math.max(0, multiplierRate);
	}
}