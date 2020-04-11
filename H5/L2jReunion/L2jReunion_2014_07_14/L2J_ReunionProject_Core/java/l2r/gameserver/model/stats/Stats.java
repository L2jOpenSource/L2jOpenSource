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
package l2r.gameserver.model.stats;

import java.util.NoSuchElementException;

/**
 * Enum of basic stats.
 * @author mkizub
 */
public enum Stats
{
	// Base stats, for each in Calculator a slot is allocated
	
	// HP, MP & CP
	MAX_HP("maxHp"),
	MAX_MP("maxMp"),
	MAX_CP("maxCp"),
	MAX_RECOVERABLE_HP("maxRecoverableHp"), // The maximum HP that is able to be recovered trough heals
	MAX_RECOVERABLE_MP("maxRecoverableMp"),
	MAX_RECOVERABLE_CP("maxRecoverableCp"),
	REGENERATE_HP_RATE("regHp"),
	REGENERATE_CP_RATE("regCp"),
	REGENERATE_MP_RATE("regMp"),
	MANA_CHARGE("manaCharge"),
	HEAL_EFFECT("healEffect"),
	
	// ATTACK & DEFENCE
	POWER_DEFENCE("pDef"),
	MAGIC_DEFENCE("mDef"),
	POWER_ATTACK("pAtk"),
	MAGIC_ATTACK("mAtk"),
	PHYSICAL_SKILL_POWER("physicalSkillPower"),
	POWER_ATTACK_SPEED("pAtkSpd"),
	MAGIC_ATTACK_SPEED("mAtkSpd"), // Magic Skill Casting Time Rate
	ATK_REUSE("atkReuse"), // Bows Hits Reuse Rate
	P_REUSE("pReuse"), // Physical Skill Reuse Rate
	MAGIC_REUSE_RATE("mReuse"), // Magic Skill Reuse Rate
	SHIELD_DEFENCE("sDef"),
	CRITICAL_DAMAGE("cAtk"),
	CRITICAL_DAMAGE_ADD("cAtkAdd"), // this is another type for special critical damage mods - vicious stance, critical power and critical damage SA
	MAGIC_CRIT_DMG("mCritPower"),
	
	// PVP BONUS
	PVP_PHYSICAL_DMG("pvpPhysDmg"),
	PVP_MAGICAL_DMG("pvpMagicalDmg"),
	PVP_PHYS_SKILL_DMG("pvpPhysSkillsDmg"),
	PVP_PHYSICAL_DEF("pvpPhysDef"),
	PVP_MAGICAL_DEF("pvpMagicalDef"),
	PVP_PHYS_SKILL_DEF("pvpPhysSkillsDef"),
	
	// PVE BONUS
	PVE_PHYSICAL_DMG("pvePhysDmg"),
	PVE_PHYS_SKILL_DMG("pvePhysSkillsDmg"),
	PVE_BOW_DMG("pveBowDmg"),
	PVE_BOW_SKILL_DMG("pveBowSkillsDmg"),
	PVE_MAGICAL_DMG("pveMagicalDmg"),
	
	// ATTACK & DEFENCE RATES
	EVASION_RATE("rEvas"),
	P_SKILL_EVASION("pSkillEvas"),
	DEFENCE_CRITICAL_RATE("defCritRate"),
	DEFENCE_CRITICAL_RATE_ADD("defCritRateAdd"),
	DEFENCE_CRITICAL_DAMAGE("defCritDamage"),
	DEFENCE_CRITICAL_DAMAGE_ADD("defCritDamageAdd"), // Resistance to critical damage in value (Example: +100 will be 100 more critical damage, NOT 100% more).
	SHIELD_RATE("rShld"),
	CRITICAL_RATE("rCrit"),
	BLOW_RATE("blowRate"),
	LETHAL_RATE("lethalRate"),
	MCRITICAL_RATE("mCritRate"),
	EXPSP_RATE("rExp"),
	BONUS_EXP("bonusExp"),
	BONUS_SP("bonusSp"),
	ATTACK_CANCEL("cancel"),
	
	// ACCURACY & RANGE
	ACCURACY_COMBAT("accCombat"),
	POWER_ATTACK_RANGE("pAtkRange"),
	MAGIC_ATTACK_RANGE("mAtkRange"),
	ATTACK_COUNT_MAX("atkCountMax"),
	// Run speed, walk & escape speed are calculated proportionally, magic speed is a buff
	MOVE_SPEED("runSpd"),
	
	// BASIC STATS
	STAT_STR("STR"),
	STAT_CON("CON"),
	STAT_DEX("DEX"),
	STAT_INT("INT"),
	STAT_WIT("WIT"),
	STAT_MEN("MEN"),
	
	// Special stats, share one slot in Calculator
	
	// VARIOUS
	BREATH("breath"),
	FALL("fall"),
	AGGRESSION("aggression"), // locks a mob on tank caster
	
	// VULNERABILITIES
	AGGRESSION_VULN("aggressionVuln"),
	BLEED_VULN("bleedVuln"),
	POISON_VULN("poisonVuln"),
	STUN_VULN("stunVuln"),
	PARALYZE_VULN("paralyzeVuln"),
	ROOT_VULN("rootVuln"),
	SLEEP_VULN("sleepVuln"),
	PHYSICALBLOCKADE_VULN("physicalBlockadeVuln"),
	BOSS_VULN("bossVuln"),
	GUST_VULN("gustVuln"),
	DAMAGE_ZONE_VULN("damageZoneVuln"),
	MOVEMENT_VULN("movementVuln"),
	CANCEL_VULN("cancelVuln"), // Resistance for cancel type skills
	DERANGEMENT_VULN("derangementVuln"),
	DEBUFF_VULN("debuffVuln"),
	BUFF_VULN("buffVuln"),
	VALAKAS_VULN("valakasVuln"),
	
	// RESISTANCES
	FIRE_RES("fireRes"),
	WIND_RES("windRes"),
	WATER_RES("waterRes"),
	EARTH_RES("earthRes"),
	HOLY_RES("holyRes"),
	DARK_RES("darkRes"),
	MAGIC_SUCCESS_RES("magicSuccRes"),
	// BUFF_IMMUNITY("buffImmunity"), //TODO: Implement me
	DEBUFF_IMMUNITY("debuffImmunity"),
	
	// ELEMENT POWER
	FIRE_POWER("firePower"),
	WATER_POWER("waterPower"),
	WIND_POWER("windPower"),
	EARTH_POWER("earthPower"),
	HOLY_POWER("holyPower"),
	DARK_POWER("darkPower"),
	
	// PROFICIENCY
	AGGRESSION_PROF("aggressionProf"),
	BLEED_PROF("bleedProf"),
	POISON_PROF("poisonProf"),
	STUN_PROF("stunProf"),
	PARALYZE_PROF("paralyzeProf"),
	ROOT_PROF("rootProf"),
	SLEEP_PROF("sleepProf"),
	CANCEL_PROF("cancelProf"),
	DERANGEMENT_PROF("derangementProf"),
	DEBUFF_PROF("debuffProf"),
	CRIT_PROF("critProf"),
	VALAKAS_PROF("valakasProf"),
	
	// WEAPONS VULNERABILITIES
	NONE_WPN_VULN("noneWpnVuln"), // Shields!!!
	SWORD_WPN_VULN("swordWpnVuln"),
	BLUNT_WPN_VULN("bluntWpnVuln"),
	DAGGER_WPN_VULN("daggerWpnVuln"),
	BOW_WPN_VULN("bowWpnVuln"),
	CROSSBOW_WPN_VULN("crossbowWpnVuln"),
	POLE_WPN_VULN("poleWpnVuln"),
	ETC_WPN_VULN("etcWpnVuln"),
	FIST_WPN_VULN("fistWpnVuln"),
	DUAL_WPN_VULN("dualWpnVuln"),
	DUALFIST_WPN_VULN("dualFistWpnVuln"),
	DUALDAGGER_WPN_VULN("dualDaggerWpnVuln"),
	RAPIER_WPN_VULN("rapierWpnVuln"),
	ANCIENT_WPN_VULN("ancientWpnVuln"),
	PET_WPN_VULN("petWpnVuln"),
	
	REFLECT_DAMAGE_PERCENT("reflectDam"),
	REFLECT_SKILL_MAGIC("reflectSkillMagic"),
	REFLECT_SKILL_PHYSIC("reflectSkillPhysic"),
	VENGEANCE_SKILL_MAGIC_DAMAGE("vengeanceMdam"),
	VENGEANCE_SKILL_PHYSICAL_DAMAGE("vengeancePdam"),
	ABSORB_DAMAGE_PERCENT("absorbDam"),
	TRANSFER_DAMAGE_PERCENT("transDam"),
	MANA_SHIELD_PERCENT("manaShield"),
	TRANSFER_DAMAGE_TO_PLAYER("transDamToPlayer"),
	ABSORB_MANA_DAMAGE_PERCENT("absorbDamMana"),
	
	WEIGHT_LIMIT("weightLimit"),
	WEIGHT_PENALTY("weightPenalty"),
	
	PATK_PLANTS("pAtk-plants"),
	PATK_INSECTS("pAtk-insects"),
	PATK_ANIMALS("pAtk-animals"),
	PATK_MONSTERS("pAtk-monsters"),
	PATK_DRAGONS("pAtk-dragons"),
	PATK_GIANTS("pAtk-giants"),
	PATK_MCREATURES("pAtk-magicCreature"),
	
	PDEF_PLANTS("pDef-plants"),
	PDEF_INSECTS("pDef-insects"),
	PDEF_ANIMALS("pDef-animals"),
	PDEF_MONSTERS("pDef-monsters"),
	PDEF_DRAGONS("pDef-dragons"),
	PDEF_GIANTS("pDef-giants"),
	PDEF_MCREATURES("pDef-magicCreature"),
	
	// ExSkill
	INV_LIM("inventoryLimit"),
	WH_LIM("whLimit"),
	FREIGHT_LIM("FreightLimit"),
	P_SELL_LIM("PrivateSellLimit"),
	P_BUY_LIM("PrivateBuyLimit"),
	REC_D_LIM("DwarfRecipeLimit"),
	REC_C_LIM("CommonRecipeLimit"),
	
	// C4 Stats
	PHYSICAL_MP_CONSUME_RATE("PhysicalMpConsumeRate"),
	MAGICAL_MP_CONSUME_RATE("MagicalMpConsumeRate"),
	DANCE_MP_CONSUME_RATE("DanceMpConsumeRate"),
	BOW_MP_CONSUME_RATE("BowMpConsumeRate"),
	MP_CONSUME("MpConsume"),
	
	// T1 stats
	transformId("transformId"),
	TALISMAN_SLOTS("talisman"),
	CLOAK_SLOT("cloak"),
	
	// Shield Stats
	SHIELD_DEFENCE_ANGLE("shieldDefAngle"),
	
	// Skill mastery
	SKILL_MASTERY("skillMastery"),
	
	// Vitality
	VITALITY_CONSUME_RATE("vitalityConsumeRate"),
	
	// Souls
	MAX_SOULS("maxSouls");
	
	public static final int NUM_STATS = values().length;
	
	private String _value;
	
	public String getValue()
	{
		return _value;
	}
	
	private Stats(String s)
	{
		_value = s;
	}
	
	public static Stats valueOfXml(String name)
	{
		name = name.intern();
		for (Stats s : values())
		{
			if (s.getValue().equals(name))
			{
				return s;
			}
		}
		
		throw new NoSuchElementException("Unknown name '" + name + "' for enum BaseStats");
	}
}
