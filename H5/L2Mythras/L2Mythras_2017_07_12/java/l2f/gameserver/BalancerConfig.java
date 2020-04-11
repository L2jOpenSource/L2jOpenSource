package l2f.gameserver;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2f.commons.configuration.ExProperties;

public class BalancerConfig
{
	protected static final Logger _log = Logger.getLogger(BalancerConfig.class.getName());

	public static final String BALANCER_FILE = "config/balancer/balancer.properties";
	public static final String DAMAGE_BALANCER_FILE = "config/balancer/damagebalancer.properties";

	public static HashMap<Integer, Double> DuelistDamageMap;
	public static HashMap<Integer, Double> DuelistDamageMapOly;

	public static HashMap<Integer, Double> DreadnoughtDamageMap;
	public static HashMap<Integer, Double> DreadnoughtDamageMapOly;

	public static HashMap<Integer, Double> PheonixKnightDamageMap;
	public static HashMap<Integer, Double> PheonixKnightDamageMapOly;

	public static HashMap<Integer, Double> HellKnightDamageMap;
	public static HashMap<Integer, Double> HellKnightDamageMapOly;

	public static HashMap<Integer, Double> SagittariusDamageMap;
	public static HashMap<Integer, Double> SagittariusDamageMapOly;

	public static HashMap<Integer, Double> AdventurerDamageMap;
	public static HashMap<Integer, Double> AdventurerDamageMapOly;

	public static HashMap<Integer, Double> ArchmageDamageMap;
	public static HashMap<Integer, Double> ArchmageDamageMapOly;

	public static HashMap<Integer, Double> SoultakerDamageMap;
	public static HashMap<Integer, Double> SoultakerDamageMapOly;

	public static HashMap<Integer, Double> ArcanalordDamageMap;
	public static HashMap<Integer, Double> ArcanalordDamageMapOly;

	public static HashMap<Integer, Double> CardinalDamageMap;
	public static HashMap<Integer, Double> CardinalDamageMapOly;

	public static HashMap<Integer, Double> HierophantDamageMap;
	public static HashMap<Integer, Double> HierophantDamageMapOly;

	public static HashMap<Integer, Double> EvatemplarDamageMap;
	public static HashMap<Integer, Double> EvatemplarDamageMapOly;

	public static HashMap<Integer, Double> SwordmuseDamageMap;
	public static HashMap<Integer, Double> SwordmuseDamageMapOly;

	public static HashMap<Integer, Double> WindriderDamageMap;
	public static HashMap<Integer, Double> WindriderDamageMapOly;

	public static HashMap<Integer, Double> MoonlightDamageMap;
	public static HashMap<Integer, Double> MoonlightDamageMapOly;

	public static HashMap<Integer, Double> MysticmuseDamageMap;
	public static HashMap<Integer, Double> MysticmuseDamageMapOly;

	public static HashMap<Integer, Double> ElementalMasterDamageMap;
	public static HashMap<Integer, Double> ElementalMasterDamageMapOly;

	public static HashMap<Integer, Double> EvasaintMasterDamageMap;
	public static HashMap<Integer, Double> EvasaintMasterDamageMapOly;

	public static HashMap<Integer, Double> ShillienTemplarDamageMap;
	public static HashMap<Integer, Double> ShillienTemplarDamageMapOly;

	public static HashMap<Integer, Double> SpectralDancerDamageMap;
	public static HashMap<Integer, Double> SpectralDancerDamageMapOly;

	public static HashMap<Integer, Double> GhostHunterDamageMap;
	public static HashMap<Integer, Double> GhostHunterDamageMapOly;

	public static HashMap<Integer, Double> GhostSentinelDamageMap;
	public static HashMap<Integer, Double> GhostSentinelDamageMapOly;

	public static HashMap<Integer, Double> StormscreamerDamageMap;
	public static HashMap<Integer, Double> StormscreamerDamageMapOly;

	public static HashMap<Integer, Double> SpectralMasterDamageMap;
	public static HashMap<Integer, Double> SpectralMasterDamageMapOly;

	public static HashMap<Integer, Double> ShillenSaintDamageMap;
	public static HashMap<Integer, Double> ShillenSaintDamageMapOly;

	public static HashMap<Integer, Double> TitanDamageMap;
	public static HashMap<Integer, Double> TitanDamageMapOly;

	public static HashMap<Integer, Double> GrandKhauatariDamageMap;
	public static HashMap<Integer, Double> GrandKhauatariDamageMapOly;

	public static HashMap<Integer, Double> DominatorDamageMap;
	public static HashMap<Integer, Double> DominatorDamageMapOly;

	public static HashMap<Integer, Double> DoomcryerDamageMap;
	public static HashMap<Integer, Double> DoomcryerDamageMapOly;

	public static HashMap<Integer, Double> FortuneSeekerDamageMap;
	public static HashMap<Integer, Double> FortuneSeekerDamageMapOly;

	public static HashMap<Integer, Double> MaestroDamageMap;
	public static HashMap<Integer, Double> MaestroDamageMapOly;

	public static HashMap<Integer, Double> DoombringerDamageMap;
	public static HashMap<Integer, Double> DoombringerDamageMapOly;

	public static HashMap<Integer, Double> MaleSoulhoundDamageMap;
	public static HashMap<Integer, Double> MaleSoulhoundDamageMapOly;

	public static HashMap<Integer, Double> FemaleSoulhoundDamageMap;
	public static HashMap<Integer, Double> FemaleSoulhoundDamageMapOly;

	public static HashMap<Integer, Double> TricksterDamageMap;
	public static HashMap<Integer, Double> TricksterDamageMapOly;

	public static HashMap<Integer, Double> JudicatorDamageMap;
	public static HashMap<Integer, Double> JudicatorDamageMapOly;

	public static boolean DEBUG;
	public static double LETHAL1_CHANCE;
	public static double LETHAL2_CHANCE;
	public static double LETHAL_IMMUNE_TARGET_BOOST_DAMAGE;
	public static double BOW_DAMAGE_BOOST;
	public static double MAGE_DAMAGE;
	public static double MAGIC_CRITICAL_DAMAGE_ON_PLAYERS;
	public static double MAGIC_CRITICAL_DAMAGE_GENERAL;
	public static double FIGHTER_DAMAGE_VS_MAGE_OLYMPIAD;

	public static double BLOW_BEHIND_DAMAGE;
	public static double OLY_BLOW_BEHIND_DAMAGE;
	public static double BLOW_NOT_BEHIND_DAMAGE;
	public static double OLY_BLOW_NOT_BEHIND_DAMAGE;
	public static double CURSE_DEATH_LINK_MUL;

	public static int DUELIST_DAMAGE_LIMIT;
	public static int DREADNOUGHT_DAMAGE_LIMIT;
	public static int PHEONIXKNIGHT_DAMAGE_LIMIT;
	public static int HELLKNIGHT_DAMAGE_LIMIT;
	public static int SAGITTARIUS_DAMAGE_LIMIT;
	public static int ADVENTURER_DAMAGE_LIMIT;
	public static int ARCHMAGE_DAMAGE_LIMIT;
	public static int SOULTAKER_DAMAGE_LIMIT;
	public static int ARCANALORD_DAMAGE_LIMIT;
	public static int CARDINAL_DAMAGE_LIMIT;
	public static int HIEROPHANT_DAMAGE_LIMIT;
	public static int EVATEMPLAR_DAMAGE_LIMIT;
	public static int SWORDMUSE_DAMAGE_LIMIT;
	public static int WINDRIDER_DAMAGE_LIMIT;
	public static int MOONLIGHTSENTINEL_DAMAGE_LIMIT;
	public static int MYSTICMUSE_DAMAGE_LIMIT;
	public static int ELEMENTALMASTER_DAMAGE_LIMIT;
	public static int EVASAINT_DAMAGE_LIMIT;
	public static int SHILLIENTEMPLAR_DAMAGE_LIMIT;
	public static int SPECTRALDANCER_DAMAGE_LIMIT;
	public static int GHOSTHUNTER_DAMAGE_LIMIT;
	public static int GHOSTSENTINEL_DAMAGE_LIMIT;
	public static int STORMSCREAMER_DAMAGE_LIMIT;
	public static int SPECTRALMASTER_DAMAGE_LIMIT;
	public static int SHILLENSAINT_DAMAGE_LIMIT;
	public static int TITAN_DAMAGE_LIMIT;
	public static int GRANDKHAUATARI_DAMAGE_LIMIT;
	public static int DOMINATOR_DAMAGE_LIMIT;
	public static int DOOMCRYER_DAMAGE_LIMIT;
	public static int FORTUNESEEKER_DAMAGE_LIMIT;
	public static int MAESTRO_DAMAGE_LIMIT;
	public static int DOOMBRINGER_DAMAGE_LIMIT;
	public static int MALESOULHOUND_DAMAGE_LIMIT;
	public static int FEMALESOULHOUND_DAMAGE_LIMIT;
	public static int TRICKSTER_DAMAGE_LIMIT;
	public static int JUDICATOR_DAMAGE_LIMIT;

	public static double SKILLS_CHANCE_MOD;
	public static double SKILLS_CHANCE_POW;
	public static double SKILLS_MOB_CHANCE;
	public static double SKILLS_DEBUFF_MOB_CHANCE;
	public static int SKILLS_CAST_TIME_MIN;
	public static double SKILLS_ATTACKER_WEAPON_MOD;
	public static double SKILLS_M_ATK_MOD_MAX;
	public static double SKILLS_M_ATK_MOD_MIN;
	public static double SKILLS_ELEMENT_MOD_MULT;
	public static double SKILLS_ELEMENT_MOD_MAX;
	public static double SKILLS_ELEMENT_MOD_MIN;
	public static boolean SKILLS_CALC_STAT_MOD;
	public static double ALT_ABSORB_DAMAGE_MODIFIER;

	/** limits of stats **/
	public static int LIM_PATK;
	public static int LIM_MATK;
	public static int LIM_PDEF;
	public static int LIM_MDEF;
	public static int LIM_MATK_SPD;
	public static int LIM_PATK_SPD;
	public static int LIM_CRIT_DAM;
	public static int LIM_CRIT;
	public static int LIM_MCRIT;
	public static int LIM_ACCURACY;
	public static int LIM_EVASION;
	public static int LIM_MOVE;
	public static int GM_LIM_MOVE;

	public static double ALT_NPC_PATK_MODIFIER;
	public static double ALT_NPC_MATK_MODIFIER;
	public static double ALT_NPC_MAXHP_MODIFIER;
	public static double ALT_NPC_MAXMP_MODIFIER;
	public static double ALT_NPC_PDEF_MODIFIER;
	public static double ALT_NPC_MDEF_MODIFIER;
	public static double ALT_POLE_DAMAGE_MODIFIER;
	public static double ALT_SUMMONS_DAMAGE;

	public static double SKILLS_CHANCE_STUN;
	public static double SKILLS_CHANCE_REMOVE_TARGET;
	public static double SKILLS_MAX_CHANCE_SUCCESS_IN_OLYMPIAD;
	public static boolean CUSTOM_POWER_SKILLS_ENABLED;
	public static boolean CUSTOM_POWER_SKILLS_DEBUG;
	public static Map<Integer, Double> CUSTOM_POWER_SKILLS = new HashMap<>();

	public static double MINIMUM_CHANCE_SKILLS;
	public static double DELDA_FOR_SKILL_DOWN_OF_MINIMUM;

	public static boolean CUSTOM_CHANCE_SKILLS_ENABLED;
	public static boolean USE_METHOD_CHANCE_WITHOUT_RESISTS;
	public static boolean CUSTOM_CHANCE_SKILLS_DEBUG;
	public static Map<Integer, Integer> CUSTOM_CHANCE_SKILLS = new HashMap<>();

	public static void LoadConfig()
	{
		ExProperties balancer = load(BALANCER_FILE);
		DEBUG = balancer.getProperty("Debug", false);
		LETHAL1_CHANCE = balancer.getProperty("Lethal1Chance", 1.0);
		LETHAL2_CHANCE = balancer.getProperty("Lethal2Chance", 1.0);
		LETHAL_IMMUNE_TARGET_BOOST_DAMAGE = balancer.getProperty("LethalImmuneTargetBoostDamage", 1.0);
		BOW_DAMAGE_BOOST = balancer.getProperty("BowDamageBoost", 1.0);
		MAGE_DAMAGE = balancer.getProperty("MageDamage", 1.0);
		MAGIC_CRITICAL_DAMAGE_ON_PLAYERS = balancer.getProperty("MagicCriticalDamageOnPlayers", 2.5);
		MAGIC_CRITICAL_DAMAGE_GENERAL = balancer.getProperty("MagicCriticalDamageGeneral", 3.0);
		FIGHTER_DAMAGE_VS_MAGE_OLYMPIAD = balancer.getProperty("FighterDamageVsMageOlympiad", 1.0);
		BLOW_BEHIND_DAMAGE = balancer.getProperty("BlowBehindDamage", 1.0);
		OLY_BLOW_BEHIND_DAMAGE = balancer.getProperty("OlyBlowBehindDamage", 1.0);
		BLOW_NOT_BEHIND_DAMAGE = balancer.getProperty("BlowNotBehindDamage", 1.0);
		OLY_BLOW_NOT_BEHIND_DAMAGE = balancer.getProperty("OlyBlowNotBehindDamage", 1.0);

		CURSE_DEATH_LINK_MUL = balancer.getProperty("CurseDeathLinkDamageMul", 1.0);

		SKILLS_CHANCE_MOD = balancer.getProperty("SkillsChanceMod", 11.);
		SKILLS_CHANCE_POW = balancer.getProperty("SkillsChancePow", 0.5);
		SKILLS_MOB_CHANCE = balancer.getProperty("SkillsMobChance", 0.5);
		SKILLS_DEBUFF_MOB_CHANCE = balancer.getProperty("SkillsDebuffMobChance", 0.5);
		SKILLS_CAST_TIME_MIN = balancer.getProperty("SkillsCastTimeMin", 333);

		SKILLS_ATTACKER_WEAPON_MOD = balancer.getProperty("SkillsAttackerWeaponMod", 1.95);
		SKILLS_M_ATK_MOD_MAX = balancer.getProperty("SkillsMAtkModMax", 1.3);
		SKILLS_M_ATK_MOD_MIN = balancer.getProperty("SkillsMAtkModMin", 0.7);

		SKILLS_ELEMENT_MOD_MULT = balancer.getProperty("SkillsElementModMult", 0.2);
		SKILLS_ELEMENT_MOD_MAX = balancer.getProperty("SkillsElementModMax", 1.2);
		SKILLS_ELEMENT_MOD_MIN = balancer.getProperty("SkillsElementModMin", 0.8);
		SKILLS_CALC_STAT_MOD = balancer.getProperty("SkillsCalcStatMod", true);

		ALT_ABSORB_DAMAGE_MODIFIER = balancer.getProperty("AbsorbDamageModifier", 1.0);

		LIM_PATK = balancer.getProperty("LimitPatk", 20000);
		LIM_MATK = balancer.getProperty("LimitMAtk", 25000);
		LIM_PDEF = balancer.getProperty("LimitPDef", 15000);
		LIM_MDEF = balancer.getProperty("LimitMDef", 15000);
		LIM_PATK_SPD = balancer.getProperty("LimitPatkSpd", 1500);
		LIM_MATK_SPD = balancer.getProperty("LimitMatkSpd", 1999);
		LIM_CRIT_DAM = balancer.getProperty("LimitCriticalDamage", 2000);
		LIM_CRIT = balancer.getProperty("LimitCritical", 500);
		LIM_MCRIT = balancer.getProperty("LimitMCritical", 20);
		LIM_ACCURACY = balancer.getProperty("LimitAccuracy", 200);
		LIM_EVASION = balancer.getProperty("LimitEvasion", 200);
		LIM_MOVE = balancer.getProperty("LimitMove", 250);
		GM_LIM_MOVE = balancer.getProperty("GmLimitMove", 1500);

		ALT_NPC_PATK_MODIFIER = balancer.getProperty("NpcPAtkModifier", 1.0);
		ALT_NPC_MATK_MODIFIER = balancer.getProperty("NpcMAtkModifier", 1.0);
		ALT_NPC_MAXHP_MODIFIER = balancer.getProperty("NpcMaxHpModifier", 1.00);
		ALT_NPC_MAXMP_MODIFIER = balancer.getProperty("NpcMapMpModifier", 1.00);
		ALT_NPC_PDEF_MODIFIER = balancer.getProperty("NpcPDefModifier", 1.00);
		ALT_NPC_MDEF_MODIFIER = balancer.getProperty("NpcMDefModifier", 1.00);

		ALT_POLE_DAMAGE_MODIFIER = balancer.getProperty("PoleDamageModifier", 1.0);
		ALT_SUMMONS_DAMAGE = balancer.getProperty("AltSummonDamMultiplier", 1.0);
		SKILLS_CHANCE_STUN = balancer.getProperty("StunModChance", 1.00);
		SKILLS_CHANCE_REMOVE_TARGET = balancer.getProperty("RemoveTargetModChance", 1.00);
		SKILLS_MAX_CHANCE_SUCCESS_IN_OLYMPIAD = balancer.getProperty("SkillsMaxChanceSuccessInOlympiad", 100.00);

		CUSTOM_POWER_SKILLS_ENABLED = Boolean.parseBoolean(balancer.getProperty("PowerSkillsEnabled", "false"));
		CUSTOM_POWER_SKILLS_DEBUG = Boolean.parseBoolean(balancer.getProperty("PowerSkillsDebug", "false"));

		String propertyValue = balancer.getProperty("PowerSkills");
		if ((propertyValue != null) && (propertyValue.length() > 0))
		{
			String[] propertySplit = propertyValue.split(";");
			if (propertySplit.length > 0)
			{
				for (String value : propertySplit)
				{
					String[] valueSplit = value.split(",");
					CUSTOM_POWER_SKILLS.put(Integer.parseInt(valueSplit[0]), Double.parseDouble(valueSplit[1]));
				}
			}
		}

		if (CUSTOM_POWER_SKILLS.size() == 0)
			CUSTOM_POWER_SKILLS_ENABLED = false;

		MINIMUM_CHANCE_SKILLS = balancer.getProperty("MinimumChanceSkills", 20.0);
		DELDA_FOR_SKILL_DOWN_OF_MINIMUM = balancer.getProperty("DeldaForSkillsDownOfMinimum", 0.5);

		CUSTOM_CHANCE_SKILLS_ENABLED = Boolean.parseBoolean(balancer.getProperty("ChanceSkillsEnabled", "false"));
		USE_METHOD_CHANCE_WITHOUT_RESISTS = Boolean.parseBoolean(balancer.getProperty("UseMethodChanceWithoutResists", "false"));
		CUSTOM_CHANCE_SKILLS_DEBUG = Boolean.parseBoolean(balancer.getProperty("ChanceSkillsDebug", "false"));

		propertyValue = balancer.getProperty("ChanceSkills");
		if ((propertyValue != null) && (propertyValue.length() > 0))
		{
			String[] propertySplit = propertyValue.split(";");
			if (propertySplit.length > 0)
			{
				for (String value : propertySplit)
				{
					String[] valueSplit = value.split(",");
					CUSTOM_CHANCE_SKILLS.put(Integer.parseInt(valueSplit[0]), Integer.parseInt(valueSplit[1]));
				}
			}
		}

		if (CUSTOM_CHANCE_SKILLS.size() == 0)
			CUSTOM_CHANCE_SKILLS_ENABLED = false;

		DUELIST_DAMAGE_LIMIT = balancer.getProperty("DuelistDamageLimit", 99999);
		DREADNOUGHT_DAMAGE_LIMIT = balancer.getProperty("DreadnoughtDamageLimit", 99999);
		PHEONIXKNIGHT_DAMAGE_LIMIT = balancer.getProperty("PheonixknightDamageLimit", 99999);
		HELLKNIGHT_DAMAGE_LIMIT = balancer.getProperty("HellknightDamageLimit", 99999);
		SAGITTARIUS_DAMAGE_LIMIT = balancer.getProperty("SagittariusDamageLimit", 99999);
		ADVENTURER_DAMAGE_LIMIT = balancer.getProperty("AdventurerDamageLimit", 99999);
		ARCHMAGE_DAMAGE_LIMIT = balancer.getProperty("ArchmageDamageLimit", 99999);
		SOULTAKER_DAMAGE_LIMIT = balancer.getProperty("SoultakerDamageLimit", 99999);
		ARCANALORD_DAMAGE_LIMIT = balancer.getProperty("ArcanalordDamageLimit", 99999);
		CARDINAL_DAMAGE_LIMIT = balancer.getProperty("CardinalDamageLimit", 99999);
		HIEROPHANT_DAMAGE_LIMIT = balancer.getProperty("HierophantDamageLimit", 99999);
		EVATEMPLAR_DAMAGE_LIMIT = balancer.getProperty("EvatemplarDamageLimit", 99999);
		SWORDMUSE_DAMAGE_LIMIT = balancer.getProperty("SwordmuseDamageLimit", 99999);
		WINDRIDER_DAMAGE_LIMIT = balancer.getProperty("WindriderDamageLimit", 99999);
		MOONLIGHTSENTINEL_DAMAGE_LIMIT = balancer.getProperty("MoonlightsentinelDamageLimit", 99999);
		MYSTICMUSE_DAMAGE_LIMIT = balancer.getProperty("MysticmuseDamageLimit", 99999);
		ELEMENTALMASTER_DAMAGE_LIMIT = balancer.getProperty("ElementalmasterDamageLimit", 99999);
		EVASAINT_DAMAGE_LIMIT = balancer.getProperty("EvasaintDamageLimit", 99999);
		SHILLIENTEMPLAR_DAMAGE_LIMIT = balancer.getProperty("ShillientemplarDamageLimit", 99999);
		SPECTRALDANCER_DAMAGE_LIMIT = balancer.getProperty("SpectraldancerDamageLimit", 99999);
		GHOSTHUNTER_DAMAGE_LIMIT = balancer.getProperty("GhosthunterDamageLimit", 99999);
		GHOSTSENTINEL_DAMAGE_LIMIT = balancer.getProperty("GhostsentinelDamageLimit", 99999);
		STORMSCREAMER_DAMAGE_LIMIT = balancer.getProperty("StormscreamerDamageLimit", 99999);
		SPECTRALMASTER_DAMAGE_LIMIT = balancer.getProperty("SpectralmasterDamageLimit", 99999);
		SHILLENSAINT_DAMAGE_LIMIT = balancer.getProperty("ShillensaintDamageLimit", 99999);
		TITAN_DAMAGE_LIMIT = balancer.getProperty("TitanDamageLimit", 99999);
		GRANDKHAUATARI_DAMAGE_LIMIT = balancer.getProperty("GrandkhauatariDamageLimit", 99999);
		DOMINATOR_DAMAGE_LIMIT = balancer.getProperty("DominatorDamageLimit", 99999);
		DOOMCRYER_DAMAGE_LIMIT = balancer.getProperty("DoomcryerDamageLimit", 99999);
		FORTUNESEEKER_DAMAGE_LIMIT = balancer.getProperty("FortuneseekerDamageLimit", 99999);
		MAESTRO_DAMAGE_LIMIT = balancer.getProperty("MaestroDamageLimit", 99999);
		DOOMBRINGER_DAMAGE_LIMIT = balancer.getProperty("DoombringerDamageLimit", 99999);
		MALESOULHOUND_DAMAGE_LIMIT = balancer.getProperty("MalesoulhoundDamageLimit", 99999);
		FEMALESOULHOUND_DAMAGE_LIMIT = balancer.getProperty("FemalesoulhoundDamageLimit", 99999);
		TRICKSTER_DAMAGE_LIMIT = balancer.getProperty("TricksterDamageLimit", 99999);
		JUDICATOR_DAMAGE_LIMIT = balancer.getProperty("JudicatorDamageLimit", 99999);

		ExProperties damage_balancer = load(DAMAGE_BALANCER_FILE);

		DuelistDamageMap = new HashMap<>();
		DuelistDamageMapOly = new HashMap<>();

		DreadnoughtDamageMap = new HashMap<>();
		DreadnoughtDamageMapOly = new HashMap<>();

		PheonixKnightDamageMap = new HashMap<>();
		PheonixKnightDamageMapOly = new HashMap<>();

		HellKnightDamageMap = new HashMap<>();
		HellKnightDamageMapOly = new HashMap<>();

		SagittariusDamageMap = new HashMap<>();
		SagittariusDamageMapOly = new HashMap<>();

		AdventurerDamageMap = new HashMap<>();
		AdventurerDamageMapOly = new HashMap<>();

		ArchmageDamageMap = new HashMap<>();
		ArchmageDamageMapOly = new HashMap<>();

		SoultakerDamageMap = new HashMap<>();
		SoultakerDamageMapOly = new HashMap<>();

		ArcanalordDamageMap = new HashMap<>();
		ArcanalordDamageMapOly = new HashMap<>();

		CardinalDamageMap = new HashMap<>();
		CardinalDamageMapOly = new HashMap<>();

		HierophantDamageMap = new HashMap<>();
		HierophantDamageMapOly = new HashMap<>();

		EvatemplarDamageMap = new HashMap<>();
		EvatemplarDamageMapOly = new HashMap<>();

		SwordmuseDamageMap = new HashMap<>();
		SwordmuseDamageMapOly = new HashMap<>();

		WindriderDamageMap = new HashMap<>();
		WindriderDamageMapOly = new HashMap<>();

		MoonlightDamageMap = new HashMap<>();
		MoonlightDamageMapOly = new HashMap<>();

		MysticmuseDamageMap = new HashMap<>();
		MysticmuseDamageMapOly = new HashMap<>();

		ElementalMasterDamageMap = new HashMap<>();
		ElementalMasterDamageMapOly = new HashMap<>();

		EvasaintMasterDamageMap = new HashMap<>();
		EvasaintMasterDamageMapOly = new HashMap<>();

		ShillienTemplarDamageMap = new HashMap<>();
		ShillienTemplarDamageMapOly = new HashMap<>();

		SpectralDancerDamageMap = new HashMap<>();
		SpectralDancerDamageMapOly = new HashMap<>();

		GhostHunterDamageMap = new HashMap<>();
		GhostHunterDamageMapOly = new HashMap<>();

		GhostSentinelDamageMap = new HashMap<>();
		GhostSentinelDamageMapOly = new HashMap<>();

		StormscreamerDamageMap = new HashMap<>();
		StormscreamerDamageMapOly = new HashMap<>();

		SpectralMasterDamageMap = new HashMap<>();
		SpectralMasterDamageMapOly = new HashMap<>();

		ShillenSaintDamageMap = new HashMap<>();
		ShillenSaintDamageMapOly = new HashMap<>();

		TitanDamageMap = new HashMap<>();
		TitanDamageMapOly = new HashMap<>();

		GrandKhauatariDamageMap = new HashMap<>();
		GrandKhauatariDamageMapOly = new HashMap<>();

		DominatorDamageMap = new HashMap<>();
		DominatorDamageMapOly = new HashMap<>();

		DoomcryerDamageMap = new HashMap<>();
		DoomcryerDamageMapOly = new HashMap<>();

		FortuneSeekerDamageMap = new HashMap<>();
		FortuneSeekerDamageMapOly = new HashMap<>();

		MaestroDamageMap = new HashMap<>();
		MaestroDamageMapOly = new HashMap<>();

		DoombringerDamageMap = new HashMap<>();
		DoombringerDamageMapOly = new HashMap<>();

		MaleSoulhoundDamageMap = new HashMap<>();
		MaleSoulhoundDamageMapOly = new HashMap<>();

		FemaleSoulhoundDamageMap = new HashMap<>();
		FemaleSoulhoundDamageMapOly = new HashMap<>();

		TricksterDamageMap = new HashMap<>();
		TricksterDamageMapOly = new HashMap<>();

		JudicatorDamageMap = new HashMap<>();
		JudicatorDamageMapOly = new HashMap<>();

		// Duelist
		int times = 0;
		for (String prop : damage_balancer.getProperty("DamageDuelistVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			DuelistDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageDuelistVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			DuelistDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageDreadnoughtVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			DreadnoughtDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageDreadnoughtVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			DreadnoughtDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamagePheonixKnightVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			PheonixKnightDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamagePheonixKnightVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			PheonixKnightDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageHellKnightVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			HellKnightDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageHellKnightVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			HellKnightDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageSagittariusVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			SagittariusDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageSagittariusVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			SagittariusDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageAdventurerVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			AdventurerDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageAdventurerVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			AdventurerDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageArchmageVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			ArchmageDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageArchmageVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			ArchmageDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageSoultakerVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			SoultakerDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageSoultakerVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			SoultakerDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageArcanalordVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			ArcanalordDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageArcanalordVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			ArcanalordDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageCardinalVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			CardinalDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageCardinalVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			CardinalDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageHierophantVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			HierophantDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageHierophantVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			HierophantDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageEvatemplarVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			EvatemplarDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageEvatemplarVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			EvatemplarDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageSwordmuseVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			SwordmuseDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageSwordmuseVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			SwordmuseDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageWindriderVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			WindriderDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageWindriderVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			WindriderDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageMoonlightVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			MoonlightDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageMoonlightVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			MoonlightDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageMysticmuseVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			MysticmuseDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageMysticmuseVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			MysticmuseDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageElementalMasterVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			ElementalMasterDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageElementalMasterVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			ElementalMasterDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageEvasaintMasterVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			EvasaintMasterDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageEvasaintMasterVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			EvasaintMasterDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageShillienTemplarVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			ShillienTemplarDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageShillienTemplarVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			ShillienTemplarDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageSpectralDancerVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			SpectralDancerDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageSpectralDancerVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			SpectralDancerDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageGhostHunterVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			GhostHunterDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageGhostHunterVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			GhostHunterDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageGhostSentinelVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			GhostSentinelDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageGhostSentinelVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			GhostSentinelDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageStormscreamerVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			StormscreamerDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageStormscreamerVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			StormscreamerDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageSpectralMasterVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			SpectralMasterDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageSpectralMasterVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			SpectralMasterDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageShillenSaintVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			ShillenSaintDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageShillenSaintVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			ShillenSaintDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageTitanVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			TitanDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageTitanVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			TitanDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageGrandKhauatariVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			GrandKhauatariDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageGrandKhauatariVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			GrandKhauatariDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageDominatorVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			DominatorDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageDominatorVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			DominatorDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageDoomcryerVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			DoomcryerDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageDoomcryerVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			DoomcryerDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageFortuneSeekerVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			FortuneSeekerDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageFortuneSeekerVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			FortuneSeekerDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageMaestroVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			MaestroDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageMaestroVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			MaestroDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageDoombringerVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			DoombringerDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageDoombringerVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			DoombringerDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageMaleSoulhoundVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			MaleSoulhoundDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageMaleSoulhoundVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			MaleSoulhoundDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageFemaleSoulhoundVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			FemaleSoulhoundDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageFemaleSoulhoundVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			FemaleSoulhoundDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageTricksterVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			TricksterDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageTricksterVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			TricksterDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageJudicatorVsClasses", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			JudicatorDamageMap.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

		times = 0;
		for (String prop : damage_balancer.getProperty("DamageJudicatorVsClassesInOly", "1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00").split(","))
		{
			JudicatorDamageMapOly.put(getClassNumber(times), Double.parseDouble(prop));
			times++;
		}

	}

	public static ExProperties load(String filename)
	{
		return load(new File(filename));
	}

	public static ExProperties load(File file)
	{
		ExProperties result = new ExProperties();
		try
		{
			result.load(file);
		} catch (IOException e)
		{
			_log.log(Level.WARNING, "Error loading config : " + file.getName() + "!");
		}

		return result;
	}

	private static int getClassNumber(int Times)
	{
		if (Times >= 31 && Times < 35)
			return 100 + Times;
		else if (Times == 35)
			return 136;

		return Times + 88;
	}
}