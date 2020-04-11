package net.sf.l2j.gameserver.enums.skills;

import java.lang.reflect.Constructor;

import net.sf.l2j.commons.util.StatsSet;

import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillAppearance;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillChargeDmg;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillCreateItem;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillDefault;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillDrain;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSeed;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSiegeFlag;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSignet;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSignetCasttime;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSpawn;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSummon;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillTeleport;

public enum L2SkillType
{
	// Damage
	PDAM,
	FATAL,
	MDAM,
	CPDAMPERCENT,
	MANADAM,
	DOT,
	MDOT,
	DRAIN_SOUL,
	DRAIN(L2SkillDrain.class),
	DEATHLINK,
	BLOW,
	SIGNET(L2SkillSignet.class),
	SIGNET_CASTTIME(L2SkillSignetCasttime.class),
	SEED(L2SkillSeed.class),
	
	// Disablers
	BLEED,
	POISON,
	STUN,
	ROOT,
	CONFUSION,
	FEAR,
	SLEEP,
	MUTE,
	PARALYZE,
	WEAKNESS,
	
	// hp, mp, cp
	HEAL,
	MANAHEAL,
	COMBATPOINTHEAL,
	HOT,
	MPHOT,
	CPHOT,
	BALANCE_LIFE,
	HEAL_STATIC,
	MANARECHARGE,
	HEAL_PERCENT,
	MANAHEAL_PERCENT,
	
	GIVE_SP,
	
	// Aggro
	AGGDAMAGE,
	AGGREDUCE,
	AGGREMOVE,
	AGGREDUCE_CHAR,
	AGGDEBUFF,
	
	// Fishing
	FISHING,
	PUMPING,
	REELING,
	
	// MISC
	UNLOCK,
	UNLOCK_SPECIAL,
	DELUXE_KEY_UNLOCK,
	ENCHANT_ARMOR,
	ENCHANT_WEAPON,
	SOULSHOT,
	SPIRITSHOT,
	SIEGEFLAG(L2SkillSiegeFlag.class),
	TAKECASTLE,
	WEAPON_SA,
	SOW,
	HARVEST,
	GET_PLAYER,
	DUMMY,
	INSTANT_JUMP,
	
	// Creation
	COMMON_CRAFT,
	DWARVEN_CRAFT,
	CREATE_ITEM(L2SkillCreateItem.class),
	EXTRACTABLE,
	EXTRACTABLE_FISH,
	
	// Summons
	SUMMON(L2SkillSummon.class),
	FEED_PET,
	DEATHLINK_PET,
	STRSIEGEASSAULT,
	ERASE,
	BETRAY,
	SPAWN(L2SkillSpawn.class),
	
	// Cancel
	CANCEL,
	MAGE_BANE,
	WARRIOR_BANE,
	
	NEGATE,
	CANCEL_DEBUFF,
	
	BUFF,
	DEBUFF,
	PASSIVE,
	CONT,
	
	RESURRECT,
	CHARGEDAM(L2SkillChargeDmg.class),
	MHOT,
	DETECT_WEAKNESS,
	LUCK,
	RECALL(L2SkillTeleport.class),
	TELEPORT(L2SkillTeleport.class),
	SUMMON_FRIEND,
	REFLECT,
	SPOIL,
	SWEEP,
	FAKE_DEATH,
	UNBLEED,
	UNPOISON,
	UNDEAD_DEFENSE,
	BEAST_FEED,
	FUSION,
	
	CHANGE_APPEARANCE(L2SkillAppearance.class),
	
	// Skill is done within the core.
	COREDONE,
	
	// unimplemented
	NOTDONE;
	
	private final Class<? extends L2Skill> _class;
	
	public L2Skill makeSkill(StatsSet set)
	{
		try
		{
			Constructor<? extends L2Skill> c = _class.getConstructor(StatsSet.class);
			
			return c.newInstance(set);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private L2SkillType()
	{
		_class = L2SkillDefault.class;
	}
	
	private L2SkillType(Class<? extends L2Skill> classType)
	{
		_class = classType;
	}
}