package com.l2jfrozen.gameserver.model;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.HeroSkillTable;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.sql.SkillTreeTable;
import com.l2jfrozen.gameserver.geo.GeoData;
import com.l2jfrozen.gameserver.managers.SiegeManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2ArtefactInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ChestInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ControlTowerInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfrozen.gameserver.model.base.ClassId;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.model.entity.siege.Siege;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.BaseStats;
import com.l2jfrozen.gameserver.skills.Env;
import com.l2jfrozen.gameserver.skills.Formulas;
import com.l2jfrozen.gameserver.skills.Stats;
import com.l2jfrozen.gameserver.skills.conditions.Condition;
import com.l2jfrozen.gameserver.skills.effects.EffectCharge;
import com.l2jfrozen.gameserver.skills.effects.EffectTemplate;
import com.l2jfrozen.gameserver.skills.funcs.Func;
import com.l2jfrozen.gameserver.skills.funcs.FuncTemplate;
import com.l2jfrozen.gameserver.skills.l2skills.L2SkillCharge;
import com.l2jfrozen.gameserver.skills.l2skills.L2SkillChargeDmg;
import com.l2jfrozen.gameserver.skills.l2skills.L2SkillChargeEffect;
import com.l2jfrozen.gameserver.skills.l2skills.L2SkillCreateItem;
import com.l2jfrozen.gameserver.skills.l2skills.L2SkillDefault;
import com.l2jfrozen.gameserver.skills.l2skills.L2SkillDrain;
import com.l2jfrozen.gameserver.skills.l2skills.L2SkillSeed;
import com.l2jfrozen.gameserver.skills.l2skills.L2SkillSignet;
import com.l2jfrozen.gameserver.skills.l2skills.L2SkillSignetCasttime;
import com.l2jfrozen.gameserver.skills.l2skills.L2SkillSummon;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.gameserver.util.Util;

/**
 * This class...
 * @authors ProGramMoS, eX1steam, l2jfrozen dev
 * @version $Revision: 1.3.3 $ $Date: 2009/04/29 00:08 $
 */
public abstract class L2Skill
{
	protected static final Logger LOGGER = Logger.getLogger(L2Skill.class);
	
	public static final int SKILL_CUBIC_MASTERY = 143;
	public static final int SKILL_LUCKY = 194;
	public static final int SKILL_CREATE_COMMON = 1320;
	public static final int SKILL_CREATE_DWARVEN = 172;
	public static final int SKILL_CRYSTALLIZE = 248;
	public static final int SKILL_DIVINE_INSPIRATION = 1405;
	
	public static final int SKILL_FAKE_INT = 9001;
	public static final int SKILL_FAKE_WIT = 9002;
	public static final int SKILL_FAKE_MEN = 9003;
	public static final int SKILL_FAKE_CON = 9004;
	public static final int SKILL_FAKE_DEX = 9005;
	public static final int SKILL_FAKE_STR = 9006;
	
	private final int targetConsumeId;
	private final int targetConsume;
	
	public static enum SkillOpType
	{
		OP_PASSIVE,
		OP_ACTIVE,
		OP_TOGGLE,
		OP_CHANCE
	}
	
	/** Target types of skills : SELF, PARTY, CLAN, PET... */
	public static enum SkillTargetType
	{
		TARGET_NONE,
		TARGET_SELF,
		TARGET_ONE,
		TARGET_PARTY,
		TARGET_ALLY,
		TARGET_CLAN,
		TARGET_PET,
		TARGET_AREA,
		TARGET_AURA,
		TARGET_CORPSE,
		TARGET_UNDEAD,
		TARGET_AREA_UNDEAD,
		TARGET_MULTIFACE,
		TARGET_CORPSE_ALLY,
		TARGET_CORPSE_CLAN,
		TARGET_CORPSE_PLAYER,
		TARGET_CORPSE_PET,
		TARGET_ITEM,
		TARGET_AREA_CORPSE_MOB,
		TARGET_CORPSE_MOB,
		TARGET_UNLOCKABLE,
		TARGET_HOLY,
		TARGET_PARTY_MEMBER,
		TARGET_PARTY_OTHER,
		TARGET_ENEMY_SUMMON,
		TARGET_OWNER_PET,
		TARGET_GROUND,
		TARGET_SIEGE,
		TARGET_TYRANNOSAURUS,
		TARGET_AREA_AIM_CORPSE,
		TARGET_CLAN_MEMBER
	}
	
	public static enum SkillType
	{
		// Damage
		PDAM,
		MDAM,
		CPDAM,
		MANADAM,
		DOT,
		MDOT,
		DRAIN_SOUL,
		DRAIN(L2SkillDrain.class),
		DEATHLINK,
		FATALCOUNTER,
		BLOW,
		
		// Disablers
		BLEED,
		POISON,
		STUN,
		ROOT,
		CONFUSION,
		FEAR,
		SLEEP,
		CONFUSE_MOB_ONLY,
		MUTE,
		PARALYZE,
		WEAKNESS,
		
		// hp, mp, cp
		HEAL,
		HOT,
		BALANCE_LIFE,
		HEAL_PERCENT,
		HEAL_STATIC,
		COMBATPOINTHEAL,
		COMBATPOINTPERCENTHEAL,
		CPHOT,
		MANAHEAL,
		MANA_BY_LEVEL,
		MANAHEAL_PERCENT,
		MANARECHARGE,
		MPHOT,
		
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
		ENCHANT_ARMOR,
		ENCHANT_WEAPON,
		SOULSHOT,
		SPIRITSHOT,
		SIEGEFLAG,
		TAKECASTLE,
		DELUXE_KEY_UNLOCK,
		SOW,
		HARVEST,
		GET_PLAYER,
		
		// Creation
		COMMON_CRAFT,
		DWARVEN_CRAFT,
		CREATE_ITEM(L2SkillCreateItem.class),
		SUMMON_TREASURE_KEY,
		
		// Summons
		SUMMON(L2SkillSummon.class),
		FEED_PET,
		DEATHLINK_PET,
		STRSIEGEASSAULT,
		ERASE,
		BETRAY,
		
		// Cancel
		CANCEL,
		MAGE_BANE,
		WARRIOR_BANE,
		NEGATE,
		
		BUFF,
		DEBUFF,
		PASSIVE,
		CONT,
		SIGNET(L2SkillSignet.class),
		SIGNET_CASTTIME(L2SkillSignetCasttime.class),
		
		RESURRECT,
		CHARGE(L2SkillCharge.class),
		CHARGE_EFFECT(L2SkillChargeEffect.class),
		CHARGEDAM(L2SkillChargeDmg.class),
		MHOT,
		DETECT_WEAKNESS,
		LUCK,
		RECALL,
		SUMMON_FRIEND,
		REFLECT,
		SPOIL,
		SWEEP,
		FAKE_DEATH,
		UNBLEED,
		UNPOISON,
		UNDEAD_DEFENSE,
		SEED(L2SkillSeed.class),
		BEAST_FEED,
		FORCE_BUFF,
		CLAN_GATE,
		GIVE_SP,
		COREDONE,
		ZAKENPLAYER,
		ZAKENSELF,
		
		// unimplemented
		NOTDONE;
		
		private final Class<? extends L2Skill> classSkill;
		
		public L2Skill makeSkill(final StatsSet set)
		{
			try
			{
				final Constructor<? extends L2Skill> c = classSkill.getConstructor(StatsSet.class);
				
				return c.newInstance(set);
			}
			catch (final Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		
		private SkillType()
		{
			classSkill = L2SkillDefault.class;
		}
		
		private SkillType(final Class<? extends L2Skill> classType)
		{
			classSkill = classType;
		}
	}
	
	protected ChanceCondition chanceCondition = null;
	// elements
	public final static int ELEMENT_WIND = 1;
	public final static int ELEMENT_FIRE = 2;
	public final static int ELEMENT_WATER = 3;
	public final static int ELEMENT_EARTH = 4;
	public final static int ELEMENT_HOLY = 5;
	public final static int ELEMENT_DARK = 6;
	
	// stat effected
	public final static int STAT_PATK = 301; // pAtk
	public final static int STAT_PDEF = 302; // pDef
	public final static int STAT_MATK = 303; // mAtk
	public final static int STAT_MDEF = 304; // mDef
	public final static int STAT_MAXHP = 305; // maxHp
	public final static int STAT_MAXMP = 306; // maxMp
	public final static int STAT_CURHP = 307;
	public final static int STAT_CURMP = 308;
	public final static int STAT_HPREGEN = 309; // regHp
	public final static int STAT_MPREGEN = 310; // regMp
	public final static int STAT_CASTINGSPEED = 311; // sCast
	public final static int STAT_ATKSPD = 312; // sAtk
	public final static int STAT_CRITDAM = 313; // critDmg
	public final static int STAT_CRITRATE = 314; // critRate
	public final static int STAT_FIRERES = 315; // fireRes
	public final static int STAT_WINDRES = 316; // windRes
	public final static int STAT_WATERRES = 317; // waterRes
	public final static int STAT_EARTHRES = 318; // earthRes
	public final static int STAT_HOLYRES = 336; // holyRes
	public final static int STAT_DARKRES = 337; // darkRes
	public final static int STAT_ROOTRES = 319; // rootRes
	public final static int STAT_SLEEPRES = 320; // sleepRes
	public final static int STAT_CONFUSIONRES = 321; // confusRes
	public final static int STAT_BREATH = 322; // breath
	public final static int STAT_AGGRESSION = 323; // aggr
	public final static int STAT_BLEED = 324; // bleed
	public final static int STAT_POISON = 325; // poison
	public final static int STAT_STUN = 326; // stun
	public final static int STAT_ROOT = 327; // root
	public final static int STAT_MOVEMENT = 328; // move
	public final static int STAT_EVASION = 329; // evas
	public final static int STAT_ACCURACY = 330; // accu
	public final static int STAT_COMBAT_STRENGTH = 331;
	public final static int STAT_COMBAT_WEAKNESS = 332;
	public final static int STAT_ATTACK_RANGE = 333; // rAtk
	public final static int STAT_NOAGG = 334; // noagg
	public final static int STAT_SHIELDDEF = 335; // sDef
	public final static int STAT_MP_CONSUME_RATE = 336; // Rate of mp consume per skill use
	public final static int STAT_HP_CONSUME_RATE = 337; // Rate of hp consume per skill use
	public final static int STAT_MCRITRATE = 338; // Magic Crit Rate
	
	// COMBAT DAMAGE MODIFIER SKILLS...DETECT WEAKNESS AND WEAKNESS/STRENGTH
	public final static int COMBAT_MOD_ANIMAL = 200;
	public final static int COMBAT_MOD_BEAST = 201;
	public final static int COMBAT_MOD_BUG = 202;
	public final static int COMBAT_MOD_DRAGON = 203;
	public final static int COMBAT_MOD_MONSTER = 204;
	public final static int COMBAT_MOD_PLANT = 205;
	public final static int COMBAT_MOD_HOLY = 206;
	public final static int COMBAT_MOD_UNHOLY = 207;
	public final static int COMBAT_MOD_BOW = 208;
	public final static int COMBAT_MOD_BLUNT = 209;
	public final static int COMBAT_MOD_DAGGER = 210;
	public final static int COMBAT_MOD_FIST = 211;
	public final static int COMBAT_MOD_DUAL = 212;
	public final static int COMBAT_MOD_SWORD = 213;
	public final static int COMBAT_MOD_POISON = 214;
	public final static int COMBAT_MOD_BLEED = 215;
	public final static int COMBAT_MOD_FIRE = 216;
	public final static int COMBAT_MOD_WATER = 217;
	public final static int COMBAT_MOD_EARTH = 218;
	public final static int COMBAT_MOD_WIND = 219;
	public final static int COMBAT_MOD_ROOT = 220;
	public final static int COMBAT_MOD_STUN = 221;
	public final static int COMBAT_MOD_CONFUSION = 222;
	public final static int COMBAT_MOD_DARK = 223;
	
	// conditional values
	public final static int COND_RUNNING = 0x0001;
	public final static int COND_WALKING = 0x0002;
	public final static int COND_SIT = 0x0004;
	public final static int COND_BEHIND = 0x0008;
	public final static int COND_CRIT = 0x0010;
	public final static int COND_LOWHP = 0x0020;
	public final static int COND_ROBES = 0x0040;
	public final static int COND_CHARGES = 0x0080;
	public final static int COND_SHIELD = 0x0100;
	public final static int COND_GRADEA = 0x010000;
	public final static int COND_GRADEB = 0x020000;
	public final static int COND_GRADEC = 0x040000;
	public final static int COND_GRADED = 0x080000;
	public final static int COND_GRADES = 0x100000;
	
	private static final Func[] emptyFunctionSet = new Func[0];
	private static final L2Effect[] emptyEffectSet = new L2Effect[0];
	
	// these two build the primary key
	private final int id;
	private final int level;
	
	/** Identifier for a skill that client can't display */
	private int displayId;
	
	// not needed, just for easier debug
	private final String name;
	private final SkillOpType operateType;
	private final boolean magic;
	private final boolean staticReuse;
	private final boolean staticHitTime;
	private final int mpConsume;
	private final int mpInitialConsume;
	private final int hpConsume;
	private final int itemConsume;
	private final int itemConsumeId;
	// item consume count over time
	protected int itemConsumeOT;
	// item consume id over time
	protected int itemConsumeIdOT;
	// how many times to consume an item
	protected int itemConsumeSteps;
	// for summon spells:
	// a) What is the total lifetime of summons (in millisecs)
	private final int summonTotalLifeTime;
	// b) how much lifetime is lost per second of idleness (non-fighting)
	protected int summonTimeLostIdle;
	// c) how much time is lost per second of activity (fighting)
	protected int summonTimeLostActive;
	
	// item consume time in milliseconds
	protected int itemConsumeTime;
	private final int castRange;
	private final int effectRange;
	
	// all times in milliseconds
	private final int hitTime;
	// private final int skillInterruptTime;
	private final int coolTime;
	private final int reuseDelay;
	private final int buffDuration;
	private final int reuseHashCode;
	
	/** Target type of the skill : SELF, PARTY, CLAN, PET... */
	private final SkillTargetType targetType;
	
	private final double power;
	private final int effectPoints;
	private final int magicLevel;
	private final String[] negateSkillTypes;
	private final String[] negateEffectTypes;
	private final float negatePower;
	private final int negateId;
	private final int levelDepend;
	
	// Effecting area of the skill, in radius.
	// The radius center varies according to the targetType:
	// "caster" if targetType = AURA/PARTY/CLAN or "target" if targetType = AREA
	private final int skillRadius;
	
	private final SkillType skillType;
	private final SkillType effectType;
	private final int effectPower;
	private final int effectId;
	private final int effectLvl;
	
	private final boolean isPotion;
	private final int element;
	private final BaseStats saveVs;
	
	private final boolean isSuicideAttack;
	
	private final Stats stat;
	
	private final int condition;
	private final int conditionValue;
	private final boolean overhit;
	private final int weaponsAllowed;
	private final int armorsAllowed;
	
	private final int addCrossLearn; // -1 disable, otherwice SP price for others classes, default 1000
	private final float mulCrossLearn; // multiplay for others classes, default 2
	private final float mulCrossLearnRace; // multiplay for others races, default 2
	private final float mulCrossLearnProf; // multiplay for fighter/mage missmatch, default 3
	private final List<ClassId> classCanLearn; // which classes can learn
	private final List<Integer> teacherList; // which NPC teaches
	private final int minPledgeClass;
	
	private final boolean isOffensive;
	private final int numCharges;
	private final int triggeredId;
	private final int triggeredLevel;
	
	private final boolean bestowed;
	
	private final boolean isHeroSkill; // If true the skill is a Hero Skill
	
	private final int baseCritRate; // percent of success for skill critical hit (especially for PDAM & BLOW - they're not affected by rCrit values or buffs). Default loads -1 for all other skills but 0 to PDAM & BLOW
	private final int lethalEffect1; // percent of success for lethal 1st effect (hit cp to 1 or if mob hp to 50%) (only for PDAM skills)
	private final int lethalEffect2; // percent of success for lethal 2nd effect (hit cp,hp to 1 or if mob hp to 1) (only for PDAM skills)
	private final boolean directHpDmg; // If true then dmg is being make directly
	private final boolean isDance; // If true then casting more dances will cost more MP
	private final int nextDanceCost;
	private final float sSBoost; // If true skill will have SoulShot boost (power*2)
	private final int aggroPoints;
	
	private final float pvpMulti;
	
	protected Condition preConditionSkill;
	protected Condition itemPreCondition;
	protected FuncTemplate[] funcTemplates;
	protected EffectTemplate[] effectTemplates;
	protected EffectTemplate[] effectTemplatesSelf;
	
	private final boolean nextActionIsAttack;
	
	private final int minChance;
	private final int maxChance;
	
	private final boolean singleEffect;
	
	private final boolean isDebuff;
	
	private final boolean advancedFlag;
	private final int advancedMultiplier;
	
	protected L2Skill(final StatsSet set)
	{
		id = set.getInteger("skill_id", 0);
		level = set.getInteger("level", 1);
		
		advancedFlag = set.getBool("advancedFlag", false);
		advancedMultiplier = set.getInteger("advancedMultiplier", 1);
		
		displayId = set.getInteger("displayId", id);
		name = set.getString("name");
		operateType = set.getEnum("operateType", SkillOpType.class);
		magic = set.getBool("isMagic", false);
		staticReuse = set.getBool("staticReuse", false);
		staticHitTime = set.getBool("staticHitTime", false);
		isPotion = set.getBool("isPotion", false);
		mpConsume = set.getInteger("mpConsume", 0);
		mpInitialConsume = set.getInteger("mpInitialConsume", 0);
		hpConsume = set.getInteger("hpConsume", 0);
		itemConsume = set.getInteger("itemConsumeCount", 0);
		itemConsumeId = set.getInteger("itemConsumeId", 0);
		itemConsumeOT = set.getInteger("itemConsumeCountOT", 0);
		itemConsumeIdOT = set.getInteger("itemConsumeIdOT", 0);
		itemConsumeTime = set.getInteger("itemConsumeTime", 0);
		itemConsumeSteps = set.getInteger("itemConsumeSteps", 0);
		summonTotalLifeTime = set.getInteger("summonTotalLifeTime", 1200000); // 20 minutes default
		summonTimeLostIdle = set.getInteger("summonTimeLostIdle", 0);
		summonTimeLostActive = set.getInteger("summonTimeLostActive", 0);
		
		castRange = set.getInteger("castRange", 0);
		effectRange = set.getInteger("effectRange", -1);
		
		hitTime = set.getInteger("hitTime", 0);
		coolTime = set.getInteger("coolTime", 0);
		// skillInterruptTime = set.getInteger("hitTime", hitTime / 2);
		reuseDelay = set.getInteger("reuseDelay", 0);
		buffDuration = set.getInteger("buffDuration", 0);
		
		skillRadius = set.getInteger("skillRadius", 80);
		
		targetType = set.getEnum("target", SkillTargetType.class);
		power = set.getFloat("power", 0.f);
		effectPoints = set.getInteger("effectPoints", 0);
		negateSkillTypes = set.getString("negateSkillTypes", "").split(" ");
		negateEffectTypes = set.getString("negateEffectTypes", "").split(" ");
		negatePower = set.getFloat("negatePower", 0.f);
		negateId = set.getInteger("negateId", 0);
		magicLevel = set.getInteger("magicLvl", SkillTreeTable.getInstance().getMinSkillLevel(id, level));
		levelDepend = set.getInteger("lvlDepend", 0);
		stat = set.getEnum("stat", Stats.class, null);
		
		skillType = set.getEnum("skillType", SkillType.class);
		effectType = set.getEnum("effectType", SkillType.class, null);
		effectPower = set.getInteger("effectPower", 0);
		effectId = set.getInteger("effectId", 0);
		effectLvl = set.getInteger("effectLevel", 0);
		
		element = set.getInteger("element", 0);
		saveVs = set.getEnum("saveVs", BaseStats.class, null);
		
		condition = set.getInteger("condition", 0);
		conditionValue = set.getInteger("conditionValue", 0);
		overhit = set.getBool("overHit", false);
		isSuicideAttack = set.getBool("isSuicideAttack", false);
		weaponsAllowed = set.getInteger("weaponsAllowed", 0);
		armorsAllowed = set.getInteger("armorsAllowed", 0);
		
		addCrossLearn = set.getInteger("addCrossLearn", 1000);
		mulCrossLearn = set.getFloat("mulCrossLearn", 2.f);
		mulCrossLearnRace = set.getFloat("mulCrossLearnRace", 2.f);
		mulCrossLearnProf = set.getFloat("mulCrossLearnProf", 3.f);
		minPledgeClass = set.getInteger("minPledgeClass", 0);
		isOffensive = set.getBool("offensive", isSkillTypeOffensive());
		numCharges = set.getInteger("num_charges", 0);
		triggeredId = set.getInteger("triggeredId", 0);
		triggeredLevel = set.getInteger("triggeredLevel", 0);
		
		bestowed = set.getBool("bestowed", false);
		
		targetConsume = set.getInteger("targetConsumeCount", 0);
		targetConsumeId = set.getInteger("targetConsumeId", 0);
		
		if (operateType == SkillOpType.OP_CHANCE)
		{
			chanceCondition = ChanceCondition.parse(set);
		}
		
		isHeroSkill = HeroSkillTable.isHeroSkill(id);
		
		baseCritRate = set.getInteger("baseCritRate", skillType == SkillType.PDAM || skillType == SkillType.BLOW ? 0 : -1);
		lethalEffect1 = set.getInteger("lethal1", 0);
		lethalEffect2 = set.getInteger("lethal2", 0);
		
		directHpDmg = set.getBool("dmgDirectlyToHp", false);
		isDance = set.getBool("isDance", false);
		nextDanceCost = set.getInteger("nextDanceCost", 0);
		sSBoost = set.getFloat("SSBoost", 0.f);
		aggroPoints = set.getInteger("aggroPoints", 0);
		
		pvpMulti = set.getFloat("pvpMulti", 1.f);
		
		nextActionIsAttack = set.getBool("nextActionAttack", false);
		
		minChance = set.getInteger("minChance", 1);
		maxChance = set.getInteger("maxChance", 99);
		
		String canLearn = set.getString("canLearn", null);
		if (canLearn == null)
		{
			classCanLearn = null;
		}
		else
		{
			classCanLearn = new ArrayList<>();
			StringTokenizer st = new StringTokenizer(canLearn, " \r\n\t,;");
			
			while (st.hasMoreTokens())
			{
				String cls = st.nextToken();
				try
				{
					classCanLearn.add(ClassId.valueOf(cls));
				}
				catch (final Throwable t)
				{
					LOGGER.error("Bad class " + cls + " to learn skill", t);
				}
				cls = null;
			}
			
			st = null;
		}
		
		canLearn = null;
		
		String teachers = set.getString("teachers", null);
		if (teachers == null)
		{
			teacherList = null;
		}
		else
		{
			teacherList = new ArrayList<>();
			StringTokenizer st = new StringTokenizer(teachers, " \r\n\t,;");
			while (st.hasMoreTokens())
			{
				String npcid = st.nextToken();
				try
				{
					teacherList.add(Integer.parseInt(npcid));
				}
				catch (final Throwable t)
				{
					LOGGER.error("Bad teacher id " + npcid + " to teach skill", t);
				}
				
				npcid = null;
			}
			
			st = null;
		}
		
		teachers = null;
		
		singleEffect = set.getBool("singleEffect", false);
		
		isDebuff = set.getBool("isDebuff", false);
		
		reuseHashCode = SkillTable.getSkillHashCode(id, level);
		
	}
	
	public abstract void useSkill(L2Character caster, L2Object[] targets);
	
	public boolean is_singleEffect()
	{
		return singleEffect;
	}
	
	public boolean is_Debuff()
	{
		boolean type_debuff = false;
		
		switch (skillType)
		{
			case AGGDEBUFF:
			case DEBUFF:
			case STUN:
			case BLEED:
			case CONFUSION:
			case FEAR:
			case PARALYZE:
			case SLEEP:
			case ROOT:
			case POISON:
			case MUTE:
			case WEAKNESS:
				type_debuff = true;
				
		}
		
		return isDebuff || type_debuff;
	}
	
	/**
	 * @return true if character should attack target after skill
	 */
	public final boolean nextActionIsAttack()
	{
		return nextActionIsAttack;
	}
	
	public final boolean isPotion()
	{
		return isPotion;
	}
	
	public final int getArmorsAllowed()
	{
		return armorsAllowed;
	}
	
	public final int getConditionValue()
	{
		return conditionValue;
	}
	
	public final SkillType getSkillType()
	{
		return skillType;
	}
	
	public final boolean hasEffectWhileCasting()
	{
		return getSkillType() == SkillType.SIGNET_CASTTIME;
	}
	
	public final BaseStats getSavevs()
	{
		return saveVs;
	}
	
	public final int getElement()
	{
		return element;
	}
	
	/**
	 * @return the target type of the skill : SELF, PARTY, CLAN, PET...
	 */
	public final SkillTargetType getTargetType()
	{
		return targetType;
	}
	
	public final int getCondition()
	{
		return condition;
	}
	
	public final boolean isOverhit()
	{
		return overhit;
	}
	
	public final boolean isSuicideAttack()
	{
		return isSuicideAttack;
	}
	
	/**
	 * @param  activeChar
	 * @return            the power of the skill.
	 */
	public final double getPower(final L2Character activeChar)
	{
		/*
		 * if(skillType == SkillType.DEATHLINK && activeChar != null) return power * Math.pow(1.7165 - activeChar.getCurrentHp() / activeChar.getMaxHp(), 2) * 0.577; else
		 */
		if (skillType == SkillType.FATALCOUNTER && activeChar != null)
		{
			return power * 3.5 * (1 - activeChar.getCurrentHp() / activeChar.getMaxHp());
		}
		return power;
	}
	
	public final double getPower()
	{
		return power;
	}
	
	public final int getEffectPoints()
	{
		return effectPoints;
	}
	
	public final String[] getNegateSkillTypes()
	{
		return negateSkillTypes;
	}
	
	public final String[] getNegateEffectTypes()
	{
		return negateEffectTypes;
	}
	
	public final float getNegatePower()
	{
		return negatePower;
	}
	
	public final int getNegateId()
	{
		return negateId;
	}
	
	public final int getMagicLevel()
	{
		return magicLevel;
	}
	
	/**
	 * @return Returns true to set static reuse.
	 */
	public final boolean isStaticReuse()
	{
		return staticReuse;
	}
	
	/**
	 * @return Returns true to set static hittime.
	 */
	public final boolean isStaticHitTime()
	{
		return staticHitTime;
	}
	
	public final int getLevelDepend()
	{
		return levelDepend;
	}
	
	/**
	 * @return the additional effect power or base probability.
	 */
	public final int getEffectPower()
	{
		return effectPower;
	}
	
	/**
	 * @return the additional effect Id.
	 */
	public final int getEffectId()
	{
		return effectId;
	}
	
	/**
	 * @return the additional effect level.
	 */
	public final int getEffectLvl()
	{
		return effectLvl;
	}
	
	/**
	 * @return the additional effect skill type (ex : STUN, PARALYZE,...).
	 */
	public final SkillType getEffectType()
	{
		return effectType;
	}
	
	/**
	 * @return Returns the buffDuration.
	 */
	public final int getBuffDuration()
	{
		return buffDuration;
	}
	
	/**
	 * @return Returns the castRange.
	 */
	public final int getCastRange()
	{
		return castRange;
	}
	
	/**
	 * @return Returns the effectRange.
	 */
	public final int getEffectRange()
	{
		return effectRange;
	}
	
	/**
	 * @return Returns the hpConsume.
	 */
	public final int getHpConsume()
	{
		return hpConsume;
	}
	
	/**
	 * @return Returns the id.
	 */
	public final int getId()
	{
		return id;
	}
	
	public int getDisplayId()
	{
		return displayId;
	}
	
	public void setDisplayId(final int id)
	{
		displayId = id;
	}
	
	public int getTriggeredId()
	{
		return triggeredId;
	}
	
	public int getTriggeredLevel()
	{
		return triggeredLevel;
	}
	
	/**
	 * @return the skill type (ex : BLEED, SLEEP, WATER...).
	 */
	public final Stats getStat()
	{
		return stat;
	}
	
	/**
	 * @return Returns the itemConsume.
	 */
	public final int getItemConsume()
	{
		return itemConsume;
	}
	
	/**
	 * @return Returns the itemConsumeId.
	 */
	public final int getItemConsumeId()
	{
		return itemConsumeId;
	}
	
	/**
	 * @return Returns the itemConsume count over time.
	 */
	public final int getItemConsumeOT()
	{
		return itemConsumeOT;
	}
	
	/**
	 * @return Returns the itemConsumeId over time.
	 */
	public final int getItemConsumeIdOT()
	{
		return itemConsumeIdOT;
	}
	
	/**
	 * @return Returns the itemConsume count over time.
	 */
	public final int getItemConsumeSteps()
	{
		return itemConsumeSteps;
	}
	
	/**
	 * @return Returns the itemConsume count over time.
	 */
	public final int getTotalLifeTime()
	{
		return summonTotalLifeTime;
	}
	
	/**
	 * @return Returns the itemConsume count over time.
	 */
	public final int getTimeLostIdle()
	{
		return summonTimeLostIdle;
	}
	
	/**
	 * @return Returns the itemConsumeId over time.
	 */
	public final int getTimeLostActive()
	{
		return summonTimeLostActive;
	}
	
	/**
	 * @return Returns the itemConsume time in milliseconds.
	 */
	public final int getItemConsumeTime()
	{
		return itemConsumeTime;
	}
	
	/**
	 * @return Returns the level.
	 */
	public final int getLevel()
	{
		return level;
	}
	
	/**
	 * @return Returns the magic.
	 */
	public final boolean isMagic()
	{
		return magic;
	}
	
	/**
	 * @return Returns the mpConsume.
	 */
	public final int getMpConsume()
	{
		return mpConsume;
	}
	
	/**
	 * @return Returns the mpInitialConsume.
	 */
	public final int getMpInitialConsume()
	{
		return mpInitialConsume;
	}
	
	/**
	 * @return Returns the name.
	 */
	public final String getName()
	{
		return name;
	}
	
	/**
	 * @return Returns the reuseDelay.
	 */
	public final int getReuseDelay()
	{
		return reuseDelay;
	}
	
	@Deprecated
	public final int getSkillTime()
	{
		return hitTime;
	}
	
	public final int getHitTime()
	{
		return hitTime;
	}
	
	/**
	 * @return Returns the coolTime.
	 */
	public final int getCoolTime()
	{
		return coolTime;
	}
	
	public final int getSkillRadius()
	{
		return skillRadius;
	}
	
	public final boolean isActive()
	{
		return operateType == SkillOpType.OP_ACTIVE;
	}
	
	public final boolean isPassive()
	{
		return operateType == SkillOpType.OP_PASSIVE;
	}
	
	public final boolean isToggle()
	{
		return operateType == SkillOpType.OP_TOGGLE;
	}
	
	public final boolean isChance()
	{
		return operateType == SkillOpType.OP_CHANCE;
	}
	
	public ChanceCondition getChanceCondition()
	{
		return chanceCondition;
	}
	
	public final boolean isDance()
	{
		return isDance;
	}
	
	public final int getNextDanceMpCost()
	{
		return nextDanceCost;
	}
	
	public final float getSSBoost()
	{
		return sSBoost;
	}
	
	public final int getAggroPoints()
	{
		return aggroPoints;
	}
	
	public final float getPvpMulti()
	{
		return pvpMulti;
	}
	
	public final boolean useSoulShot()
	{
		return getSkillType() == SkillType.PDAM || getSkillType() == SkillType.STUN || getSkillType() == SkillType.CHARGEDAM || getSkillType() == SkillType.BLOW;
	}
	
	public final boolean useSpiritShot()
	{
		return isMagic();
	}
	
	public final boolean useFishShot()
	{
		return getSkillType() == SkillType.PUMPING || getSkillType() == SkillType.REELING;
	}
	
	public final int getWeaponsAllowed()
	{
		return weaponsAllowed;
	}
	
	public final int getCrossLearnAdd()
	{
		return addCrossLearn;
	}
	
	public final float getCrossLearnMul()
	{
		return mulCrossLearn;
	}
	
	public final float getCrossLearnRace()
	{
		return mulCrossLearnRace;
	}
	
	public final float getCrossLearnProf()
	{
		return mulCrossLearnProf;
	}
	
	public final boolean getCanLearn(final ClassId cls)
	{
		return classCanLearn == null || classCanLearn.contains(cls);
	}
	
	public final boolean canTeachBy(final int npcId)
	{
		return teacherList == null || teacherList.contains(npcId);
	}
	
	public int getMinPledgeClass()
	{
		return minPledgeClass;
	}
	
	public final boolean isPvpSkill()
	{
		switch (skillType)
		{
			case DOT:
			case AGGREDUCE:
			case AGGDAMAGE:
			case AGGREDUCE_CHAR:
			case CONFUSE_MOB_ONLY:
			case BLEED:
			case CONFUSION:
			case POISON:
			case DEBUFF:
			case AGGDEBUFF:
			case STUN:
			case ROOT:
			case FEAR:
			case SLEEP:
			case MDOT:
			case MANADAM:
			case MUTE:
			case WEAKNESS:
			case PARALYZE:
			case CANCEL:
			case MAGE_BANE:
			case WARRIOR_BANE:
			case FATALCOUNTER:
			case BETRAY:
				return true;
			default:
				return false;
		}
	}
	
	public final boolean isOffensive()
	{
		return isOffensive;
	}
	
	public final boolean isHeroSkill()
	{
		return isHeroSkill;
	}
	
	public final int getNumCharges()
	{
		return numCharges;
	}
	
	public final int getBaseCritRate()
	{
		return baseCritRate;
	}
	
	public final int getLethalChance1()
	{
		return lethalEffect1;
	}
	
	public final int getLethalChance2()
	{
		return lethalEffect2;
	}
	
	public final boolean getDmgDirectlyToHP()
	{
		return directHpDmg;
	}
	
	public boolean bestowed()
	{
		return bestowed;
	}
	
	public boolean triggerAnotherSkill()
	{
		return triggeredId > 1;
	}
	
	public final boolean isSkillTypeOffensive()
	{
		switch (skillType)
		{
			case PDAM:
			case MDAM:
			case CPDAM:
			case DOT:
			case BLEED:
			case POISON:
			case AGGDAMAGE:
			case DEBUFF:
			case AGGDEBUFF:
			case STUN:
			case ROOT:
			case CONFUSION:
			case ERASE:
			case BLOW:
			case FEAR:
			case DRAIN:
			case SLEEP:
			case CHARGEDAM:
			case CONFUSE_MOB_ONLY:
			case DEATHLINK:
			case DETECT_WEAKNESS:
			case MANADAM:
			case MDOT:
			case MUTE:
			case SOULSHOT:
			case SPIRITSHOT:
			case SPOIL:
			case WEAKNESS:
			case MANA_BY_LEVEL:
			case SWEEP:
			case PARALYZE:
			case DRAIN_SOUL:
			case AGGREDUCE:
			case CANCEL:
			case MAGE_BANE:
			case WARRIOR_BANE:
			case AGGREMOVE:
			case AGGREDUCE_CHAR:
			case FATALCOUNTER:
			case BETRAY:
			case DELUXE_KEY_UNLOCK:
			case SOW:
			case HARVEST:
				return true;
			default:
				return false;
		}
	}
	
	// int weapons[] = {L2Weapon.WEAPON_TYPE_ETC, L2Weapon.WEAPON_TYPE_BOW,
	// L2Weapon.WEAPON_TYPE_POLE, L2Weapon.WEAPON_TYPE_DUALFIST,
	// L2Weapon.WEAPON_TYPE_DUAL, L2Weapon.WEAPON_TYPE_BLUNT,
	// L2Weapon.WEAPON_TYPE_SWORD, L2Weapon.WEAPON_TYPE_DAGGER};
	
	public final boolean getWeaponDependancy(final L2Character activeChar)
	{
		if (getWeaponDependancy(activeChar, false))
		{
			return true;
		}
		final SystemMessage message = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
		message.addSkillName(getId());
		activeChar.sendPacket(message);
		
		return false;
	}
	
	public final boolean getWeaponDependancy(final L2Character activeChar, final boolean chance)
	{
		final int weaponsAllowed = getWeaponsAllowed();
		// check to see if skill has a weapon dependency.
		if (weaponsAllowed == 0)
		{
			return true;
		}
		
		int mask = 0;
		if (activeChar.getActiveWeaponItem() != null)
		{
			mask |= activeChar.getActiveWeaponItem().getItemType().mask();
		}
		if (activeChar.getSecondaryWeaponItem() != null)
		{
			mask |= activeChar.getSecondaryWeaponItem().getItemType().mask();
		}
		
		if ((mask & weaponsAllowed) != 0)
		{
			return true;
		}
		
		return false;
	}
	
	public boolean checkCondition(final L2Character activeChar, final L2Object target, final boolean itemOrWeapon)
	{
		Condition preCondition = preConditionSkill;
		
		if (itemOrWeapon)
		{
			preCondition = itemPreCondition;
		}
		
		if (preCondition == null)
		{
			return true;
		}
		
		Env env = new Env();
		env.player = activeChar;
		if (target instanceof L2Character)
		{
			env.target = (L2Character) target;
		}
		
		env.skill = this;
		if (!preCondition.test(env))
		{
			String msg = preCondition.getMessage();
			if (msg != null)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString(msg);
				activeChar.sendPacket(sm);
				sm = null;
			}
			
			msg = null;
			
			return false;
		}
		
		env = null;
		preCondition = null;
		
		return true;
	}
	
	public final L2Object[] getTargetList(final L2Character activeChar, final boolean onlyFirst)
	{
		// Init to null the target of the skill
		L2Character target = null;
		
		// Get the L2Objcet targeted by the user of the skill at this moment
		final L2Object objTarget = activeChar.getTarget();
		// If the L2Object targeted is a L2Character, it becomes the L2Character target
		if (objTarget instanceof L2Character)
		{
			target = (L2Character) objTarget;
		}
		
		return getTargetList(activeChar, onlyFirst, target);
	}
	
	/**
	 * Return all targets of the skill in a table in function a the skill type.<BR>
	 * <BR>
	 * <B><U> Values of skill type</U> :</B><BR>
	 * <BR>
	 * <li>ONE : The skill can only be used on the L2PcInstance targeted, or on the caster if it's a L2PcInstance and no L2PcInstance targeted</li>
	 * <li>SELF</li>
	 * <li>HOLY, UNDEAD</li>
	 * <li>PET</li>
	 * <li>AURA, AURA_CLOSE</li>
	 * <li>AREA</li>
	 * <li>MULTIFACE</li>
	 * <li>PARTY, CLAN</li>
	 * <li>CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN</li>
	 * <li>UNLOCKABLE</li>
	 * <li>ITEM</li><BR>
	 * <BR>
	 * @param  activeChar The L2Character who use the skill
	 * @param  onlyFirst
	 * @param  target
	 * @return
	 */
	public final L2Object[] getTargetList(final L2Character activeChar, final boolean onlyFirst, L2Character target)
	{
		if (activeChar instanceof L2PcInstance)
		{ // to avoid attacks during oly start period
			
			if (isOffensive() && (((L2PcInstance) activeChar).isInOlympiadMode() && !((L2PcInstance) activeChar).isInOlympiadFight()))
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				return null;
			}
			
		}
		
		final List<L2Character> targetList = new ArrayList<>();
		
		if (isPotion())
		{
			
			return new L2Character[]
			{
				activeChar
			};
			
		}
		
		// Get the target type of the skill
		// (ex : ONE, SELF, HOLY, PET, AURA, AURA_CLOSE, AREA, MULTIFACE, PARTY, CLAN, CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN, UNLOCKABLE, ITEM, UNDEAD)
		final SkillTargetType targetType = getTargetType();
		
		// Get the type of the skill
		// (ex : PDAM, MDAM, DOT, BLEED, POISON, HEAL, HOT, MANAHEAL, MANARECHARGE, AGGDAMAGE, BUFF, DEBUFF, STUN, ROOT, RESURRECT, PASSIVE...)
		final SkillType skillType = getSkillType();
		
		switch (targetType)
		{
			// The skill can only be used on the L2Character targeted, or on the caster itself
			case TARGET_ONE:
			{
				boolean canTargetSelf = false;
				switch (skillType)
				{
					case BUFF:
					case HEAL:
					case HOT:
					case HEAL_PERCENT:
					case MANARECHARGE:
					case MANAHEAL:
					case NEGATE:
					case REFLECT:
					case UNBLEED:
					case UNPOISON: // case CANCEL:
					case SEED:
					case COMBATPOINTHEAL:
					case COMBATPOINTPERCENTHEAL:
					case MAGE_BANE:
					case WARRIOR_BANE:
					case BETRAY:
					case BALANCE_LIFE:
					case FORCE_BUFF:
						canTargetSelf = true;
						break;
				}
				
				switch (skillType)
				{
					case CONFUSION:
					case DEBUFF:
					case STUN:
					case ROOT:
					case FEAR:
					case SLEEP:
					case MUTE:
					case WEAKNESS:
					case PARALYZE:
					case CANCEL:
					case MAGE_BANE:
					case WARRIOR_BANE:
						if (checkPartyClan(activeChar, target))
						{
							activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
							return null;
						}
						break;
				}
				
				switch (skillType)
				{
					case AGGDEBUFF:
					case DEBUFF:
					case BLEED:
					case CONFUSION:
					case FEAR:
					case PARALYZE:
					case SLEEP:
					case ROOT:
					case WEAKNESS:
					case MUTE:
					case CANCEL:
					case DOT:
					case POISON:
					case AGGREDUCE_CHAR:
					case AGGDAMAGE:
					case AGGREMOVE:
					case MANADAM:
						// Like L2OFF if the skills is TARGET_ONE (skillType) can't be used on Npc
						if (target instanceof L2NpcInstance && !(target instanceof L2MonsterInstance))
						{
							activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
							return null;
						}
						break;
				}
				
				// Like L2OFF Shield stun can't be used on Npc
				if (getId() == 92 && target instanceof L2NpcInstance && !(target instanceof L2MonsterInstance))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return null;
				}
				
				// Check for null target or any other invalid target
				if (target == null || target.isDead() || target == activeChar && !canTargetSelf)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return null;
				}
				
				// If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
				return new L2Character[]
				{
					target
				};
			}
			case TARGET_SELF:
			case TARGET_GROUND:
			{
				return new L2Character[]
				{
					activeChar
				};
			}
			case TARGET_HOLY:
			{
				if (activeChar instanceof L2PcInstance)
				{
					if (activeChar.getTarget() instanceof L2ArtefactInstance)
					{
						return new L2Character[]
						{
							(L2ArtefactInstance) activeChar.getTarget()
						};
					}
				}
				
				return null;
			}
			
			case TARGET_PET:
			{
				target = activeChar.getPet();
				if (target != null && !target.isDead())
				{
					return new L2Character[]
					{
						target
					};
				}
				
				return null;
			}
			case TARGET_OWNER_PET:
			{
				if (activeChar instanceof L2Summon)
				{
					target = ((L2Summon) activeChar).getOwner();
					if (target != null && !target.isDead())
					{
						return new L2Character[]
						{
							target
						};
					}
				}
				
				return null;
			}
			case TARGET_CORPSE_PET:
			{
				if (activeChar instanceof L2PcInstance)
				{
					target = activeChar.getPet();
					if (target != null && target.isDead())
					{
						return new L2Character[]
						{
							target
						};
					}
				}
				
				return null;
			}
			case TARGET_AURA:
			{
				final int radius = getSkillRadius();
				final boolean srcInArena = activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE);
				
				L2PcInstance src = null;
				if (activeChar instanceof L2PcInstance)
				{
					src = (L2PcInstance) activeChar;
				}
				
				if (activeChar instanceof L2Summon)
				{
					src = ((L2Summon) activeChar).getOwner();
				}
				
				// Go through the L2Character knownList
				for (final L2Object obj : activeChar.getKnownList().getKnownCharactersInRadius(radius))
				{
					if (obj == null || !(activeChar instanceof L2PlayableInstance) && !(obj instanceof L2PlayableInstance))
					{
						continue;
					}
					
					// Like L2OFF you can cast the skill on peace zone but hasn't any effect
					if (isOffensive() && L2Character.isInsidePeaceZone(target, activeChar))
					{
						continue;
					}
					
					if (src != null && (obj instanceof L2Attackable || obj instanceof L2PlayableInstance))
					{
						// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
						if (obj == activeChar || obj == src)
						{
							continue;
						}
						
						if (!GeoData.getInstance().canSeeTarget(activeChar, obj))
						{
							continue;
						}
						
						// check if both attacker and target are L2PcInstances and if they are in same party
						if (obj instanceof L2PcInstance)
						{
							if (((L2PcInstance) obj).isDead())
							{
								continue;
							}
							
							if (((L2PcInstance) obj).getAppearance().isInvisible())
							{
								continue;
							}
							
							if (!src.checkPvpSkill(obj, this))
							{
								continue;
							}
							
							/*
							 * if(src.isInOlympiadMode() && !src.isOlympiadStart()) { continue; } if(src.getParty() != null && ((L2PcInstance) obj).getParty() != null && src.getParty().getPartyLeaderOID() == ((L2PcInstance) obj).getParty().getPartyLeaderOID()) { continue; }
							 */
							
							if (!srcInArena && !(((L2Character) obj).isInsideZone(L2Character.ZONE_PVP) && !((L2Character) obj).isInsideZone(L2Character.ZONE_SIEGE)))
							{
								if (checkPartyClan(src, obj))
								{
									continue;
								}
								
								/*
								 * if(src.getClanId() != 0 && src.getClanId() == ((L2PcInstance) obj).getClanId()) { continue; }
								 */
								
								if (src.getAllyId() != 0 && src.getAllyId() == ((L2PcInstance) obj).getAllyId())
								{
									continue;
								}
							}
						}
						if (obj instanceof L2Summon)
						{
							L2PcInstance trg = ((L2Summon) obj).getOwner();
							
							if (trg == null)
							{
								continue;
							}
							
							if (trg == src)
							{
								continue;
							}
							
							if (!src.checkPvpSkill(trg, this))
							{
								continue;
							}
							
							/*
							 * if(src.isInOlympiadMode() && !src.isOlympiadStart()) { continue; }
							 */
							
							if (src.getParty() != null && trg.getParty() != null && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
							{
								continue;
							}
							
							if (!srcInArena && !(((L2Character) obj).isInsideZone(L2Character.ZONE_PVP) && !((L2Character) obj).isInsideZone(L2Character.ZONE_SIEGE)))
							{
								/*
								 * if(src.getClanId() != 0 && src.getClanId() == trg.getClanId()) { continue; }
								 */
								if (checkPartyClan(src, obj))
								{
									continue;
								}
								
								if (src.getAllyId() != 0 && src.getAllyId() == trg.getAllyId())
								{
									continue;
								}
							}
							
							trg = null;
						}
					}
					
					if (!Util.checkIfInRange(radius, activeChar, obj, true))
					{
						continue;
					}
					
					if (!onlyFirst)
					{
						targetList.add((L2Character) obj);
					}
					else
					{
						return new L2Character[]
						{
							(L2Character) obj
						};
					}
					
				}
				
				src = null;
				
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_AREA:
			{
				// Like L2OFF players can use TARGET_AREA skills on NPC in peacezone
				if (!(target instanceof L2Attackable || target instanceof L2PlayableInstance || target instanceof L2NpcInstance) || // Target is not L2Attackable or L2PlayableInstance or L2NpcInstance
					getCastRange() >= 0 && (target == activeChar || target.isAlikeDead())) // target is null or self or dead/faking
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return null;
				}
				
				L2Character cha;
				
				if (getCastRange() >= 0)
				{
					cha = target;
					
					if (!onlyFirst)
					{
						targetList.add(cha); // Add target to target list
					}
					else
					{
						return new L2Character[]
						{
							cha
						};
					}
				}
				else
				{
					cha = activeChar;
				}
				
				final boolean effectOriginIsL2PlayableInstance = cha instanceof L2PlayableInstance;
				
				L2PcInstance src = null;
				if (activeChar instanceof L2PcInstance)
				{
					src = (L2PcInstance) activeChar;
				}
				else if (activeChar instanceof L2Summon)
				{
					src = ((L2Summon) activeChar).getOwner();
				}
				
				final int radius = getSkillRadius();
				
				final boolean srcInArena = activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE);
				
				for (final L2Object obj : activeChar.getKnownList().getKnownObjects().values())
				{
					if (obj == null || !(activeChar instanceof L2PlayableInstance) && !(obj instanceof L2PlayableInstance))
					{
						continue;
					}
					
					if (!(obj instanceof L2Attackable || obj instanceof L2PlayableInstance))
					{
						continue;
					}
					if (obj == cha)
					{
						continue;
					}
					
					if (src != null && !src.checkPvpSkill(obj, this))
					{
						continue;
					}
					
					target = (L2Character) obj;
					
					if (!GeoData.getInstance().canSeeTarget(activeChar, target))
					{
						continue;
					}
					
					if (isOffensive() && L2Character.isInsidePeaceZone(activeChar, target))
					{
						continue;
					}
					
					if (!target.isAlikeDead() && target != activeChar)
					{
						if (!Util.checkIfInRange(radius, obj, cha, true))
						{
							continue;
						}
						
						if (src != null) // caster is l2playableinstance and exists
						{
							// check for Events
							if (obj instanceof L2PcInstance)
							{
								
								final L2PcInstance trg = (L2PcInstance) obj;
								if (trg == src)
								{
									continue;
								}
								
								// if src is in event and trg not OR viceversa:
								// to be fixed for mixed events status (in TvT joining phase, someone can attack a partecipating CTF player with area attack)
								if (((src.inEvent || src.inEventCTF || src.inEventDM || src.inEventTvT) && (!trg.inEvent && !trg.inEventCTF && !trg.inEventDM && !trg.inEventTvT)) || ((trg.inEvent || trg.inEventCTF || trg.inEventDM || trg.inEventTvT) && (!src.inEvent && !src.inEventCTF && !src.inEventDM && !src.inEventTvT)))
								{
									continue;
								}
								
							}
							else if (obj instanceof L2Summon)
							{
								
								final L2PcInstance trg = ((L2Summon) obj).getOwner();
								if (trg == src)
								{
									continue;
								}
								
								// if src is in event and trg not OR viceversa:
								// to be fixed for mixed events status (in TvT joining phase, someone can attack a partecipating CTF player with area attack)
								if (((src.inEvent || src.inEventCTF || src.inEventDM || src.inEventTvT) && (!trg.inEvent && !trg.inEventCTF && !trg.inEventDM && !trg.inEventTvT)) || ((trg.inEvent || trg.inEventCTF || trg.inEventDM || trg.inEventTvT) && (!src.inEvent && !src.inEventCTF && !src.inEventDM && !src.inEventTvT)))
								{
									continue;
								}
								
							}
							
							if (obj instanceof L2PcInstance)
							{
								L2PcInstance trg = (L2PcInstance) obj;
								if (trg == src)
								{
									continue;
								}
								
								if (((L2PcInstance) obj).getAppearance().isInvisible())
								{
									continue;
								}
								
								if (src.getParty() != null && trg.getParty() != null && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
								{
									continue;
								}
								
								if (!srcInArena && !(trg.isInsideZone(L2Character.ZONE_PVP) && !trg.isInsideZone(L2Character.ZONE_SIEGE)))
								{
									if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
									{
										continue;
									}
									
									if (checkPartyClan(src, obj))
									{
										continue;
									}
									/*
									 * if(src.getClan() != null && trg.getClan() != null) { if(src.getClan().getClanId() == trg.getClan().getClanId()) { continue; } }
									 */
									
									if (!src.checkPvpSkill(obj, this))
									{
										continue;
									}
								}
								
								trg = null;
							}
							if (obj instanceof L2Summon)
							{
								L2PcInstance trg = ((L2Summon) obj).getOwner();
								
								if (trg == null)
								{
									continue;
								}
								
								if (trg == src)
								{
									continue;
								}
								
								if (src.getParty() != null && trg.getParty() != null && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
								{
									continue;
								}
								
								if (!srcInArena && !(trg.isInsideZone(L2Character.ZONE_PVP) && !trg.isInsideZone(L2Character.ZONE_SIEGE)))
								{
									if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
									{
										continue;
									}
									
									/*
									 * if(src.getClan() != null && trg.getClan() != null) { if(src.getClan().getClanId() == trg.getClan().getClanId()) { continue; } }
									 */
									if (checkPartyClan(src, obj))
									{
										continue;
									}
									
									if (!src.checkPvpSkill(trg, this))
									{
										continue;
									}
								}
								
								trg = null;
							}
						}
						else
						// Skill user is not L2PlayableInstance
						{
							if (effectOriginIsL2PlayableInstance && // If effect starts at L2PlayableInstance and
								!(obj instanceof L2PlayableInstance))
							{
								continue;
							}
						}
						
						targetList.add((L2Character) obj);
					}
				}
				
				if (targetList.size() == 0)
				{
					return null;
				}
				
				src = null;
				
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_MULTIFACE:
			{
				if (!(target instanceof L2Attackable) && !(target instanceof L2PcInstance))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return null;
				}
				
				if (!onlyFirst)
				{
					targetList.add(target);
				}
				else
				{
					return new L2Character[]
					{
						target
					};
				}
				
				final int radius = getSkillRadius();
				
				L2PcInstance src = null;
				if (activeChar instanceof L2PcInstance)
				{
					src = (L2PcInstance) activeChar;
				}
				else if (activeChar instanceof L2Summon)
				{
					src = ((L2Summon) activeChar).getOwner();
				}
				
				for (final L2Object obj : activeChar.getKnownList().getKnownObjects().values())
				{
					if (obj == null)
					{
						continue;
					}
					
					if (!Util.checkIfInRange(radius, activeChar, obj, true))
					{
						continue;
					}
					
					// check for Events
					if (src != null)
					{
						if (obj instanceof L2PcInstance)
						{
							
							final L2PcInstance trg = (L2PcInstance) obj;
							if (trg == src)
							{
								continue;
							}
							
							// if src is in event and trg not OR viceversa:
							// to be fixed for mixed events status (in TvT joining phase, someone can attack a partecipating CTF player with area attack)
							if (((src.inEvent || src.inEventCTF || src.inEventDM || src.inEventTvT) && (!trg.inEvent && !trg.inEventCTF && !trg.inEventDM && !trg.inEventTvT)) || ((trg.inEvent || trg.inEventCTF || trg.inEventDM || trg.inEventTvT) && (!src.inEvent && !src.inEventCTF && !src.inEventDM && !src.inEventTvT)))
							{
								continue;
							}
							
						}
						else if (obj instanceof L2Summon)
						{
							
							final L2PcInstance trg = ((L2Summon) obj).getOwner();
							if (trg == src)
							{
								continue;
							}
							
							// if src is in event and trg not OR viceversa:
							// to be fixed for mixed events status (in TvT joining phase, someone can attack a partecipating CTF player with area attack)
							if (((src.inEvent || src.inEventCTF || src.inEventDM || src.inEventTvT) && (!trg.inEvent && !trg.inEventCTF && !trg.inEventDM && !trg.inEventTvT)) || ((trg.inEvent || trg.inEventCTF || trg.inEventDM || trg.inEventTvT) && (!src.inEvent && !src.inEventCTF && !src.inEventDM && !src.inEventTvT)))
							{
								continue;
							}
							
						}
					}
					
					if (obj instanceof L2Attackable && obj != target)
					{
						targetList.add((L2Character) obj);
					}
					
					if (targetList.size() == 0)
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_CANT_FOUND));
						return null;
					}
				}
				return targetList.toArray(new L2Character[targetList.size()]);
				// TODO multiface targets all around right now. need it to just get targets
				// the character is facing.
			}
			case TARGET_PARTY:
			{
				if (onlyFirst)
				{
					return new L2Character[]
					{
						activeChar
					};
				}
				
				targetList.add(activeChar);
				
				L2PcInstance player = activeChar.getActingPlayer();
				if (player == null)
				{
					return new L2Character[]
					{
						activeChar
					};
				}
				
				if (activeChar instanceof L2Summon)
				{
					targetList.add(player);
				}
				else if (activeChar instanceof L2PcInstance)
				{
					if (player.getPet() != null)
					{
						targetList.add(player.getPet());
					}
				}
				
				if (activeChar.getParty() != null)
				{
					// Get all visible objects in a spheric area near the L2Character
					// Get a list of Party Members
					List<L2PcInstance> partyList = activeChar.getParty().getPartyMembers();
					
					for (final L2PcInstance partyMember : partyList)
					{
						if (partyMember == null)
						{
							continue;
						}
						if (partyMember == player)
						{
							continue;
						}
						
						// check if allow interference is allowed if player is not on event but target is on event
						// if(((TvT._started && !Config.TVT_ALLOW_INTERFERENCE) || (CTF._started && !Config.CTF_ALLOW_INTERFERENCE) || (DM._started && !Config.DM_ALLOW_INTERFERENCE)) && !player.isGM())
						if (((TvT.isStarted() && !Config.TVT_ALLOW_INTERFERENCE) || (CTF.isStarted() && !Config.CTF_ALLOW_INTERFERENCE) || (DM.isStarted() && !Config.DM_ALLOW_INTERFERENCE))/* && !player.isGM() */)
						{
							if ((partyMember.inEventTvT && !player.inEventTvT) || (!partyMember.inEventTvT && player.inEventTvT))
							{
								continue;
							}
							if ((partyMember.inEventCTF && !player.inEventCTF) || (!partyMember.inEventCTF && player.inEventCTF))
							{
								continue;
							}
							if ((partyMember.inEventDM && !player.inEventDM) || (!partyMember.inEventDM && player.inEventDM))
							{
								continue;
							}
						}
						
						if (!partyMember.isDead() && Util.checkIfInRange(getSkillRadius(), activeChar, partyMember, true))
						{
							L2PcInstance src = null;
							if (activeChar instanceof L2PcInstance)
							{
								src = (L2PcInstance) activeChar;
							}
							else if (activeChar instanceof L2Summon)
							{
								src = ((L2Summon) activeChar).getOwner();
							}
							
							final L2PcInstance trg = partyMember;
							
							// if src is in event and trg not OR viceversa:
							// to be fixed for mixed events status (in TvT joining phase, someone can attack a partecipating CTF player with area attack)
							if (src != null)
							{
								if (((src.inEvent || src.inEventCTF || src.inEventDM || src.inEventTvT) && (!trg.inEvent && !trg.inEventCTF && !trg.inEventDM && !trg.inEventTvT)) || ((trg.inEvent || trg.inEventCTF || trg.inEventDM || trg.inEventTvT) && (!src.inEvent && !src.inEventCTF && !src.inEventDM && !src.inEventTvT)))
								{
									continue;
								}
							}
							
							targetList.add(partyMember);
							
							if (partyMember.getPet() != null && !partyMember.getPet().isDead())
							{
								targetList.add(partyMember.getPet());
							}
						}
					}
					
					partyList = null;
				}
				
				player = null;
				
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_PARTY_MEMBER:
			{
				if (target != null && !target.isDead() && (target == activeChar || (activeChar.getParty() != null && target.getParty() != null && activeChar.getParty().getPartyLeaderOID() == target.getParty().getPartyLeaderOID()) || (activeChar.getPet() == target) || (activeChar == target.getPet())))
				{
					// If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
					return new L2Character[]
					{
						target
					};
					
				}
				activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				return null;
			}
			case TARGET_PARTY_OTHER:
			{
				if (target != activeChar && target != null && !target.isDead() && activeChar.getParty() != null && target.getParty() != null && activeChar.getParty().getPartyLeaderOID() == target.getParty().getPartyLeaderOID())
				{
					// If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
					return new L2Character[]
					{
						target
					};
				}
				activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				return null;
			}
			case TARGET_CORPSE_ALLY:
			case TARGET_ALLY:
			{
				if (activeChar instanceof L2PcInstance)
				{
					
					final int radius = getSkillRadius();
					L2PcInstance player = (L2PcInstance) activeChar;
					L2Clan clan = player.getClan();
					
					if (targetType != SkillTargetType.TARGET_CORPSE_ALLY) // if corpose, the caster is not included
					{
						if (player.isInOlympiadMode())
						{
							return new L2Character[]
							{
								player
							};
						}
						
						if (!onlyFirst)
						{
							targetList.add(player);
						}
						else
						{
							return new L2Character[]
							{
								player
							};
						}
					}
					
					L2PcInstance src = null;
					if (activeChar instanceof L2PcInstance)
					{
						src = (L2PcInstance) activeChar;
					}
					else if (activeChar instanceof L2Summon)
					{
						src = ((L2Summon) activeChar).getOwner();
					}
					
					if (clan != null)
					{
						// Get all visible objects in a spheric area near the L2Character
						// Get Clan Members
						for (final L2Object newTarget : activeChar.getKnownList().getKnownObjects().values())
						{
							if (newTarget == null || !(newTarget instanceof L2PcInstance))
							{
								continue;
							}
							
							final L2PcInstance playerTarget = (L2PcInstance) newTarget;
							
							if (playerTarget.isDead() && targetType != SkillTargetType.TARGET_CORPSE_ALLY)
							{
								
								continue;
								
							}
							
							// if ally is different --> clan is different too, so --> continue
							if (player.getAllyId() != 0)
							{
								
								if (playerTarget.getAllyId() != player.getAllyId())
								{
									continue;
								}
								
							}
							else
							{ // check if clan is not the same --> continue
								
								if (player.getClanId() != playerTarget.getClanId())
								{
									continue;
								}
								
							}
							
							// check for Events
							if (src != null)
							{
								
								if (playerTarget == src)
								{
									continue;
								}
								
								// if src is in event and trg not OR viceversa:
								// to be fixed for mixed events status (in TvT joining phase, someone can attack a partecipating CTF player with area attack)
								if (((src.inEvent || src.inEventCTF || src.inEventDM || src.inEventTvT) && (!playerTarget.inEvent && !playerTarget.inEventCTF && !playerTarget.inEventDM && !playerTarget.inEventTvT)) || ((playerTarget.inEvent || playerTarget.inEventCTF || playerTarget.inEventDM || playerTarget.inEventTvT) && (!src.inEvent && !src.inEventCTF && !src.inEventDM && !src.inEventTvT)))
								{
									continue;
								}
								
							}
							
							/*
							 * The target_ally or target_corpse_ally have to work indipendent on duel/party status if(player.isInDuel() && (player.getDuelId() != ((L2PcInstance) newTarget).getDuelId() || player.getParty() != null && !player.getParty().getPartyMembers().contains(newTarget))) { continue; }
							 */
							
							L2Summon pet = ((L2PcInstance) newTarget).getPet();
							if (pet != null && Util.checkIfInRange(radius, activeChar, pet, true) && !onlyFirst && (targetType == SkillTargetType.TARGET_CORPSE_ALLY && pet.isDead() || targetType == SkillTargetType.TARGET_ALLY && !pet.isDead()) && player.checkPvpSkill(newTarget, this))
							{
								targetList.add(pet);
							}
							pet = null;
							
							if (targetType == SkillTargetType.TARGET_CORPSE_ALLY)
							{
								if (!((L2PcInstance) newTarget).isDead())
								{
									continue;
								}
								
								if (getSkillType() == SkillType.RESURRECT && ((L2PcInstance) newTarget).isInsideZone(L2Character.ZONE_SIEGE))
								{
									continue;
								}
							}
							
							if (!Util.checkIfInRange(radius, activeChar, newTarget, true))
							{
								continue;
							}
							
							// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
							if (!player.checkPvpSkill(newTarget, this))
							{
								continue;
							}
							
							if (!onlyFirst)
							{
								targetList.add((L2Character) newTarget);
							}
							else
							{
								return new L2Character[]
								{
									(L2Character) newTarget
								};
							}
							
						}
					}
					
					player = null;
					clan = null;
				}
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_CORPSE_CLAN:
			case TARGET_CLAN:
			{
				if (activeChar instanceof L2PcInstance)
				{
					final int radius = getSkillRadius();
					L2PcInstance player = (L2PcInstance) activeChar;
					L2Clan clan = player.getClan();
					
					if (targetType != SkillTargetType.TARGET_CORPSE_CLAN)
					{
						if (player.isInOlympiadMode())
						{
							return new L2Character[]
							{
								player
							};
						}
						
						if (!onlyFirst)
						{
							targetList.add(player);
						}
						else
						{
							return new L2Character[]
							{
								player
							};
						}
					}
					
					if (clan != null)
					{
						// Get all visible objects in a spheric area near the L2Character
						// Get Clan Members
						for (final L2ClanMember member : clan.getMembers())
						{
							L2PcInstance newTarget = member.getPlayerInstance();
							
							if (newTarget == null || newTarget == player)
							{
								continue;
							}
							
							if (player.isInDuel() && (player.getDuelId() != newTarget.getDuelId() || player.getParty() == null && player.getParty() != newTarget.getParty()))
							{
								continue;
							}
							
							final L2PcInstance trg = newTarget;
							final L2PcInstance src = player;
							
							// if src is in event and trg not OR viceversa:
							// to be fixed for mixed events status (in TvT joining phase, someone can attack a partecipating CTF player with area attack)
							if (((src.inEvent || src.inEventCTF || src.inEventDM || src.inEventTvT) && (!trg.inEvent && !trg.inEventCTF && !trg.inEventDM && !trg.inEventTvT)) || ((trg.inEvent || trg.inEventCTF || trg.inEventDM || trg.inEventTvT) && (!src.inEvent && !src.inEventCTF && !src.inEventDM && !src.inEventTvT)))
							{
								continue;
							}
							
							/*
							 * //check if allow interference is allowed if player is not on event but target is on event //if(((TvT._started && !Config.TVT_ALLOW_INTERFERENCE) || (CTF._started && !Config.CTF_ALLOW_INTERFERENCE) || (DM._started && !Config.DM_ALLOW_INTERFERENCE)) && !player.isGM()) if(((TvT.is_inProgress() &&
							 * !Config.TVT_ALLOW_INTERFERENCE) || (CTF.is_inProgress() && !Config.CTF_ALLOW_INTERFERENCE) || (DM.is_inProgress() && !Config.DM_ALLOW_INTERFERENCE))) { if((newTarget._inEventTvT && !player._inEventTvT) || (!newTarget._inEventTvT && player._inEventTvT)) { continue; } if((newTarget._inEventCTF &&
							 * !player._inEventCTF) || (!newTarget._inEventCTF && player._inEventCTF)) { continue; } if((newTarget._inEventDM && !player._inEventDM) || (!newTarget._inEventDM && player._inEventDM)) { continue; } }
							 */
							
							L2Summon pet = newTarget.getPet();
							if (pet != null && Util.checkIfInRange(radius, activeChar, pet, true) && !onlyFirst && (targetType == SkillTargetType.TARGET_CORPSE_CLAN && pet.isDead() || targetType == SkillTargetType.TARGET_CLAN && !pet.isDead()) && player.checkPvpSkill(newTarget, this))
							{
								targetList.add(pet);
							}
							
							pet = null;
							
							if (targetType == SkillTargetType.TARGET_CORPSE_CLAN)
							{
								if (!newTarget.isDead())
								{
									continue;
								}
								
								if (getSkillType() == SkillType.RESURRECT)
								{
									// check target is not in a active siege zone
									Siege siege = SiegeManager.getInstance().getSiege(newTarget);
									if (siege != null && siege.getIsInProgress())
									{
										continue;
									}
									
									siege = null;
								}
							}
							
							if (!Util.checkIfInRange(radius, activeChar, newTarget, true))
							{
								continue;
							}
							
							// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
							if (!player.checkPvpSkill(newTarget, this))
							{
								continue;
							}
							
							if (!onlyFirst)
							{
								targetList.add(newTarget);
							}
							else
							{
								return new L2Character[]
								{
									newTarget
								};
							}
							
							newTarget = null;
						}
					}
					
					player = null;
					clan = null;
				}
				else if (activeChar instanceof L2NpcInstance)
				{
					// for buff purposes, returns friendly mobs nearby and mob itself
					final L2NpcInstance npc = (L2NpcInstance) activeChar;
					if (npc.getFactionId() == null || npc.getFactionId().isEmpty())
					{
						return new L2Character[]
						{
							activeChar
						};
					}
					targetList.add(activeChar);
					final Collection<L2Object> objs = activeChar.getKnownList().getKnownObjects().values();
					// synchronized (activeChar.getKnownList().getKnownObjects())
					{
						for (final L2Object newTarget : objs)
						{
							if (newTarget instanceof L2NpcInstance && npc.getFactionId().equals(((L2NpcInstance) newTarget).getFactionId()))
							{
								if (!Util.checkIfInRange(getCastRange(), activeChar, newTarget, true))
								{
									continue;
								}
								targetList.add((L2NpcInstance) newTarget);
							}
						}
					}
				}
				
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_CORPSE_PLAYER:
			{
				if (target != null && target.isDead())
				{
					L2PcInstance player = null;
					
					if (activeChar instanceof L2PcInstance)
					{
						player = (L2PcInstance) activeChar;
					}
					
					L2PcInstance targetPlayer = null;
					
					if (target instanceof L2PcInstance)
					{
						targetPlayer = (L2PcInstance) target;
					}
					
					L2PetInstance targetPet = null;
					
					if (target instanceof L2PetInstance)
					{
						targetPet = (L2PetInstance) target;
					}
					
					if (player != null && (targetPlayer != null || targetPet != null))
					{
						boolean condGood = true;
						
						if (getSkillType() == SkillType.RESURRECT)
						{
							// check target is not in a active siege zone
							if (target.isInsideZone(L2Character.ZONE_SIEGE))
							{
								condGood = false;
								player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE));
							}
							
							if (targetPlayer != null)
							{
								if (targetPlayer.isReviveRequested())
								{
									if (targetPlayer.isRevivingPet())
									{
										player.sendPacket(new SystemMessage(SystemMessageId.MASTER_CANNOT_RES)); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
									}
									else
									{
										player.sendPacket(new SystemMessage(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED)); // Resurrection is already been proposed.
									}
									condGood = false;
								}
							}
							else if (targetPet != null)
							{
								if (targetPet.getOwner() != player)
								{
									condGood = false;
									player.sendMessage("You are not the owner of this pet");
								}
							}
						}
						
						if (condGood)
						{
							if (!onlyFirst)
							{
								targetList.add(target);
								return targetList.toArray(new L2Object[targetList.size()]);
							}
							
							return new L2Character[]
							{
								target
							};
							
						}
					}
					
					player = null;
					targetPlayer = null;
					targetPet = null;
				}
				activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				
				return null;
			}
			case TARGET_CORPSE_MOB:
			{
				if (!(target instanceof L2Attackable) || !target.isDead())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return null;
				}
				
				if (!onlyFirst)
				{
					targetList.add(target);
					return targetList.toArray(new L2Object[targetList.size()]);
				}
				
				return new L2Character[]
				{
					target
				};
				
			}
			case TARGET_AREA_CORPSE_MOB:
			{
				if (!(target instanceof L2Attackable) || !target.isDead())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return null;
				}
				
				if (!onlyFirst)
				{
					targetList.add(target);
				}
				else
				{
					return new L2Character[]
					{
						target
					};
				}
				
				final boolean srcInArena = activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE);
				L2PcInstance src = null;
				
				if (activeChar instanceof L2PcInstance)
				{
					src = (L2PcInstance) activeChar;
				}
				
				L2PcInstance trg = null;
				
				final int radius = getSkillRadius();
				
				if (activeChar.getKnownList() != null)
				{
					for (final L2Object obj : activeChar.getKnownList().getKnownObjects().values())
					{
						if (obj == null)
						{
							continue;
						}
						
						if (!(obj instanceof L2Attackable || obj instanceof L2PlayableInstance) || ((L2Character) obj).isDead() || (L2Character) obj == activeChar)
						{
							continue;
						}
						
						if (!Util.checkIfInRange(radius, target, obj, true))
						{
							continue;
						}
						
						if (!GeoData.getInstance().canSeeTarget(activeChar, obj))
						{
							continue;
						}
						
						if (isOffensive() && L2Character.isInsidePeaceZone(activeChar, obj))
						{
							continue;
						}
						
						if (obj instanceof L2PcInstance && src != null)
						{
							trg = (L2PcInstance) obj;
							
							if (src.getParty() != null && trg.getParty() != null && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
							{
								continue;
							}
							
							if (!srcInArena && !(trg.isInsideZone(L2Character.ZONE_PVP) && !trg.isInsideZone(L2Character.ZONE_SIEGE)))
							{
								if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
								{
									continue;
								}
								
								if (src.getClan() != null && trg.getClan() != null)
								{
									if (src.getClan().getClanId() == trg.getClan().getClanId())
									{
										continue;
									}
								}
								
								if (!src.checkPvpSkill(obj, this))
								{
									continue;
								}
							}
						}
						if (obj instanceof L2Summon && src != null)
						{
							trg = ((L2Summon) obj).getOwner();
							if (trg == null)
							{
								continue;
							}
							
							if (src.getParty() != null && trg.getParty() != null && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
							{
								continue;
							}
							
							if (!srcInArena && !(trg.isInsideZone(L2Character.ZONE_PVP) && !trg.isInsideZone(L2Character.ZONE_SIEGE)))
							{
								if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
								{
									continue;
								}
								
								if (src.getClan() != null && trg.getClan() != null)
								{
									if (src.getClan().getClanId() == trg.getClan().getClanId())
									{
										continue;
									}
								}
								
								if (!src.checkPvpSkill(trg, this))
								{
									continue;
								}
							}
							
						}
						
						// check for Events
						if (trg == src)
						{
							continue;
						}
						
						// if src is in event and trg not OR viceversa:
						// to be fixed for mixed events status (in TvT joining phase, someone can attack a partecipating CTF player with area attack)
						if (src != null && trg != null)
						{
							if (((src.inEvent || src.inEventCTF || src.inEventDM || src.inEventTvT) && (!trg.inEvent && !trg.inEventCTF && !trg.inEventDM && !trg.inEventTvT)) || ((trg.inEvent || trg.inEventCTF || trg.inEventDM || trg.inEventTvT) && (!src.inEvent && !src.inEventCTF && !src.inEventDM && !src.inEventTvT)))
							{
								continue;
							}
						}
						
						targetList.add((L2Character) obj);
					}
				}
				
				if (targetList.size() == 0)
				{
					return null;
				}
				
				trg = null;
				src = null;
				
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_UNLOCKABLE:
			{
				if (!(target instanceof L2DoorInstance) && !(target instanceof L2ChestInstance))
				{
					// Like L2OFF if target isn't door or chest send message of incorrect target
					activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
					return null;
				}
				
				if (!onlyFirst)
				{
					targetList.add(target);
					return targetList.toArray(new L2Object[targetList.size()]);
				}
				
				return new L2Character[]
				{
					target
				};
				
			}
			case TARGET_ITEM:
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Target type of skill is not currently handled");
				activeChar.sendPacket(sm);
				sm = null;
				return null;
			}
			case TARGET_UNDEAD:
			{
				if (target instanceof L2NpcInstance || target instanceof L2SummonInstance)
				{
					if (!target.isUndead() || target.isDead())
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
						return null;
					}
					
					if (!onlyFirst)
					{
						targetList.add(target);
					}
					else
					{
						return new L2Character[]
						{
							target
						};
					}
					
					return targetList.toArray(new L2Object[targetList.size()]);
				}
				activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				return null;
			}
			case TARGET_AREA_UNDEAD:
			{
				L2Character cha;
				
				final int radius = getSkillRadius();
				
				if (getCastRange() >= 0 && (target instanceof L2NpcInstance || target instanceof L2SummonInstance) && target.isUndead() && !target.isAlikeDead())
				{
					cha = target;
					
					if (!onlyFirst)
					{
						targetList.add(cha); // Add target to target list
					}
					else
					{
						return new L2Character[]
						{
							cha
						};
					}
					
				}
				else
				{
					cha = activeChar;
				}
				
				if (cha != null && cha.getKnownList() != null)
				{
					for (final L2Object obj : cha.getKnownList().getKnownObjects().values())
					{
						if (obj == null)
						{
							continue;
						}
						
						if (obj instanceof L2NpcInstance)
						{
							target = (L2NpcInstance) obj;
						}
						else if (obj instanceof L2SummonInstance)
						{
							target = (L2SummonInstance) obj;
						}
						else
						{
							continue;
						}
						
						if (!GeoData.getInstance().canSeeTarget(activeChar, target))
						{
							continue;
						}
						
						if (!target.isAlikeDead()) // If target is not dead/fake death and not self
						{
							if (!target.isUndead())
							{
								continue;
							}
							
							if (!Util.checkIfInRange(radius, cha, obj, true))
							{
								continue;
							}
							
							if (!onlyFirst)
							{
								targetList.add((L2Character) obj); // Add obj to target lists
							}
							else
							{
								return new L2Character[]
								{
									(L2Character) obj
								};
							}
						}
					}
				}
				
				if (targetList.size() == 0)
				{
					return null;
				}
				
				cha = null;
				
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_ENEMY_SUMMON:
			{
				if (target != null && target instanceof L2Summon)
				{
					L2Summon targetSummon = (L2Summon) target;
					if (activeChar instanceof L2PcInstance && activeChar.getPet() != targetSummon && !targetSummon.isDead() && (targetSummon.getOwner().getPvpFlag() != 0 || targetSummon.getOwner().getKarma() > 0 || targetSummon.getOwner().isInDuel()) || targetSummon.getOwner().isInsideZone(L2Character.ZONE_PVP) && ((L2PcInstance) activeChar).isInsideZone(L2Character.ZONE_PVP))
					{
						return new L2Character[]
						{
							targetSummon
						};
					}
					
					targetSummon = null;
				}
				return null;
			}
			case TARGET_SIEGE:
			{
				if (target != null && !target.isDead() && (target instanceof L2DoorInstance || target instanceof L2ControlTowerInstance))
				{
					return new L2Character[]
					{
						target
					};
				}
				return null;
			}
			case TARGET_TYRANNOSAURUS:
			{
				if (target instanceof L2PcInstance)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return null;
				}
				
				if (target instanceof L2MonsterInstance && (((L2MonsterInstance) target).getNpcId() == 22217 || ((L2MonsterInstance) target).getNpcId() == 22216 || ((L2MonsterInstance) target).getNpcId() == 22215))
				{
					return new L2Character[]
					{
						target
					};
				}
				return null;
			}
			case TARGET_AREA_AIM_CORPSE:
			{
				// Spectral Lord - Corpse Kaboom
				// TODO: Check if "Corpse Kaboom" skill affect playrs in PvP or PK mode
				if (target != null && target.isDead() && target instanceof L2MonsterInstance)
				{
					if (activeChar instanceof L2Summon)
					{
						for (L2Character creature : activeChar.getKnownList().getKnownCharactersInRadius(skillRadius))
						{
							if (creature == null)
							{
								continue;
							}
							
							if (!GeoData.getInstance().canSeeTarget(activeChar, creature))
							{
								continue;
							}
							
							if (isOffensive() && L2Character.isInsidePeaceZone(activeChar, creature))
							{
								continue;
							}
							
							if (!(creature instanceof L2MonsterInstance))
							{
								continue;
							}
							
							targetList.add(creature);
						}
					}
					
					return targetList.toArray(new L2Object[targetList.size()]);
				}
				return null;
			}
			// npc only for now - untested
			case TARGET_CLAN_MEMBER:
			{
				if (activeChar instanceof L2NpcInstance)
				{
					// for buff purposes, returns friendly mobs nearby and mob itself
					final L2NpcInstance npc = (L2NpcInstance) activeChar;
					if (npc.getFactionId() == null || npc.getFactionId().isEmpty())
					{
						return new L2Character[]
						{
							activeChar
						};
					}
					final Collection<L2Object> objs = activeChar.getKnownList().getKnownObjects().values();
					for (final L2Object newTarget : objs)
					{
						if (newTarget instanceof L2NpcInstance && npc.getFactionId().equals(((L2NpcInstance) newTarget).getFactionId()))
						{
							if (!Util.checkIfInRange(getCastRange(), activeChar, newTarget, true))
							{
								continue;
							}
							if (((L2NpcInstance) newTarget).getFirstEffect(this) != null)
							{
								continue;
							}
							targetList.add((L2NpcInstance) newTarget);
							break; // found
						}
					}
					if (targetList.isEmpty())
					{
						targetList.add(npc);
					}
				}
				return null;
			}
			default:
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Target type of skill is not currently handled");
				activeChar.sendPacket(sm);
				sm = null;
				return null;
			}
		}// end switch
	}
	
	public final L2Object[] getTargetList(final L2Character activeChar)
	{
		return getTargetList(activeChar, false);
	}
	
	public final L2Object getFirstOfTargetList(final L2Character activeChar)
	{
		L2Object[] targets;
		
		targets = getTargetList(activeChar, true);
		if (targets == null || targets.length == 0)
		{
			return null;
		}
		return targets[0];
	}
	
	public final Func[] getStatFuncs(final L2Effect effect, final L2Character player)
	{
		if (!(player instanceof L2PcInstance) && !(player instanceof L2Attackable) && !(player instanceof L2Summon))
		{
			return emptyFunctionSet;
		}
		
		if (funcTemplates == null)
		{
			return emptyFunctionSet;
		}
		
		final List<Func> funcs = new ArrayList<>();
		
		for (final FuncTemplate t : funcTemplates)
		{
			final Env env = new Env();
			env.player = player;
			env.skill = this;
			final Func f = t.getFunc(env, this); // skill is owner
			
			if (f != null)
			{
				funcs.add(f);
			}
		}
		if (funcs.size() == 0)
		{
			return emptyFunctionSet;
		}
		
		return funcs.toArray(new Func[funcs.size()]);
	}
	
	public boolean hasEffects()
	{
		return effectTemplates != null && effectTemplates.length > 0;
	}
	
	public EffectTemplate[] getEffectTemplates()
	{
		return effectTemplates;
	}
	
	public final L2Effect[] getEffects(final L2Character effector, final L2Character effected)
	{
		return this.getEffects(effector, effected, false, false, false);
	}
	
	public final L2Effect[] getEffects(final L2Character effector, final L2Character effected, final boolean ss, final boolean sps, final boolean bss)
	{
		if (isPassive())
		{
			return emptyEffectSet;
		}
		
		if (effectTemplates == null)
		{
			return emptyEffectSet;
		}
		
		if (effector != effected && effected.isInvul())
		{
			return emptyEffectSet;
		}
		
		if (getSkillType() == SkillType.BUFF && effected.isBlockBuff())
		{
			return emptyEffectSet;
		}
		
		final List<L2Effect> effects = new ArrayList<>();
		
		boolean skillMastery = false;
		
		if (!isToggle() && Formulas.getInstance().calcSkillMastery(effector))
		{
			skillMastery = true;
		}
		
		final Env env = new Env();
		env.player = effector;
		env.target = effected;
		env.skill = this;
		env.skillMastery = skillMastery;
		
		for (final EffectTemplate et : effectTemplates)
		{
			boolean success = true;
			if (et.effectPower > -1)
			{
				success = Formulas.calcEffectSuccess(effector, effected, et, this, ss, sps, bss);
			}
			
			if (success)
			{
				L2Effect e = et.getEffect(env);
				if (e != null)
				{
					// e.scheduleEffect();
					effects.add(e);
				}
				
				e = null;
			}
			/*
			 * L2Effect e = et.getEffect(env); if(e != null) { effects.add(e); } e = null;
			 */
		}
		
		if (effects.size() == 0)
		{
			return emptyEffectSet;
		}
		
		return effects.toArray(new L2Effect[effects.size()]);
	}
	
	/**
	 * @see             L2Skill#getEffects
	 * @param  effector
	 * @param  effected
	 * @param  time     : Time duration for this skill, value in <B>seconds</B>
	 * @return          L2Effect[] List as Array
	 */
	public L2Effect[] getEffects(L2Character effector, L2Character effected, int time)
	{
		boolean ss = false; // Soulshot
		boolean sps = false; // Spiritshot
		boolean bss = false; // Blessed spiritshot
		
		if (isPassive())
		{
			return emptyEffectSet;
		}
		
		if (effectTemplates == null)
		{
			return emptyEffectSet;
		}
		
		if (effector != effected && effected.isInvul())
		{
			return emptyEffectSet;
		}
		
		if (getSkillType() == SkillType.BUFF && effected.isBlockBuff())
		{
			return emptyEffectSet;
		}
		
		boolean skillMastery = false;
		
		if (!isToggle() && Formulas.getInstance().calcSkillMastery(effector))
		{
			skillMastery = true;
		}
		
		Env env = new Env();
		env.player = effector;
		env.target = effected;
		env.skill = this;
		env.skillMastery = skillMastery;
		
		List<L2Effect> effects = new ArrayList<>();
		
		for (EffectTemplate et : effectTemplates)
		{
			boolean success = true;
			if (et.effectPower > -1)
			{
				success = Formulas.calcEffectSuccess(effector, effected, et, this, ss, sps, bss);
			}
			
			if (success)
			{
				et.setPeriod(time);
				L2Effect e = et.getEffect(env);
				if (e != null)
				{
					effects.add(e);
				}
			}
		}
		
		if (effects.isEmpty())
		{
			return emptyEffectSet;
		}
		
		return effects.toArray(new L2Effect[effects.size()]);
	}
	
	public final L2Effect[] getEffectsSelf(final L2Character effector)
	{
		if (isPassive())
		{
			return emptyEffectSet;
		}
		
		if (effectTemplatesSelf == null)
		{
			return emptyEffectSet;
		}
		
		final List<L2Effect> effects = new ArrayList<>();
		
		final Env env = new Env();
		env.player = effector;
		env.target = effector;
		env.skill = this;
		
		for (final EffectTemplate et : effectTemplatesSelf)
		{
			
			L2Effect e = et.getEffect(env);
			if (e != null)
			{
				// Implements effect charge
				if (e.getEffectType() == L2Effect.EffectType.CHARGE)
				{
					env.skill = SkillTable.getInstance().getInfo(8, effector.getSkillLevel(8));
					final EffectCharge effect = (EffectCharge) env.target.getFirstEffect(L2Effect.EffectType.CHARGE);
					if (effect != null)
					{
						int effectcharge = effect.getLevel();
						if (effectcharge < numCharges)
						{
							effectcharge++;
							effect.addNumCharges(effectcharge);
							if (env.target instanceof L2PcInstance)
							{
								env.target.sendPacket(new EtcStatusUpdate((L2PcInstance) env.target));
								SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
								sm.addNumber(effectcharge);
								env.target.sendPacket(sm);
								sm = null;
							}
						}
					}
					else
					{
						effects.add(e);
					}
				}
				else
				{
					effects.add(e);
				}
			}
			
			e = null;
		}
		if (effects.size() == 0)
		{
			return emptyEffectSet;
		}
		
		return effects.toArray(new L2Effect[effects.size()]);
	}
	
	public final void attach(final FuncTemplate f)
	{
		if (funcTemplates == null)
		{
			funcTemplates = new FuncTemplate[]
			{
				f
			};
		}
		else
		{
			final int len = funcTemplates.length;
			FuncTemplate[] tmp = new FuncTemplate[len + 1];
			System.arraycopy(funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			funcTemplates = tmp;
			tmp = null;
		}
	}
	
	public final void attach(final EffectTemplate effect)
	{
		if (effectTemplates == null)
		{
			effectTemplates = new EffectTemplate[]
			{
				effect
			};
		}
		else
		{
			final int len = effectTemplates.length;
			EffectTemplate[] tmp = new EffectTemplate[len + 1];
			System.arraycopy(effectTemplates, 0, tmp, 0, len);
			tmp[len] = effect;
			effectTemplates = tmp;
			tmp = null;
		}
	}
	
	public final void attachSelf(final EffectTemplate effect)
	{
		if (effectTemplatesSelf == null)
		{
			effectTemplatesSelf = new EffectTemplate[]
			{
				effect
			};
		}
		else
		{
			final int len = effectTemplatesSelf.length;
			EffectTemplate[] tmp = new EffectTemplate[len + 1];
			System.arraycopy(effectTemplatesSelf, 0, tmp, 0, len);
			tmp[len] = effect;
			effectTemplatesSelf = tmp;
			tmp = null;
		}
	}
	
	// Author Jose Moreira
	public boolean isAbnormalEffectByName(final int abnormalEffect)
	{
		// Function to know if the skill has "abnormalEffect"
		if (isPassive())
		{
			return false;
		}
		
		if (effectTemplates == null)
		{
			return false;
		}
		
		for (final EffectTemplate et : effectTemplates)
		{
			if (et.abnormalEffect == abnormalEffect)
			{
				return true;
			}
		}
		return false;
	}
	
	public final void attach(final Condition c, final boolean itemOrWeapon)
	{
		if (itemOrWeapon)
		{
			itemPreCondition = c;
		}
		else
		{
			preConditionSkill = c;
		}
	}
	
	public boolean checkPartyClan(final L2Character activeChar, final L2Object target)
	{
		if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance)
		{
			L2PcInstance targetChar = (L2PcInstance) target;
			L2PcInstance activeCh = (L2PcInstance) activeChar;
			
			if (activeCh.isInOlympiadMode() && activeCh.isInOlympiadFight() && targetChar.isInOlympiadMode() && targetChar.isInOlympiadFight())
			{
				return false;
			}
			
			if (activeCh.isInDuel() && targetChar.isInDuel() && activeCh.getDuelId() == targetChar.getDuelId())
			{
				return false;
			}
			
			// if src is in event and trg not OR viceversa, the target must be not attackable
			// to be fixed for mixed events status (in TvT joining phase, someone can attack a partecipating CTF player with area attack)
			if (((activeCh.inEvent || activeCh.inEventCTF || activeCh.inEventDM || activeCh.inEventTvT) && (!targetChar.inEvent && !targetChar.inEventCTF && !targetChar.inEventDM && !targetChar.inEventTvT))
				|| ((targetChar.inEvent || targetChar.inEventCTF || targetChar.inEventDM || targetChar.inEventTvT) && (!activeCh.inEvent && !activeCh.inEventCTF && !activeCh.inEventDM && !activeCh.inEventTvT)))
			{
				return true;
			}
			
			if ((activeCh.inEvent && targetChar.inEvent) || (activeCh.inEventDM && targetChar.inEventDM) || (activeCh.inEventTvT && targetChar.inEventTvT) || (activeCh.inEventCTF && targetChar.inEventCTF))
			{
				
				return false;
			}
			
			if (activeCh.getParty() != null && targetChar.getParty() != null && // Is in the same party???
				activeCh.getParty().getPartyLeaderOID() == targetChar.getParty().getPartyLeaderOID())
			{
				return true;
			}
			if (activeCh.getClan() != null && targetChar.getClan() != null && // Is in the same clan???
				activeCh.getClan().getClanId() == targetChar.getClan().getClanId())
			{
				return true;
			}
			targetChar = null;
			activeCh = null;
		}
		return false;
	}
	
	@Override
	public String toString()
	{
		return "" + name + "[id=" + id + ",lvl=" + level + "]";
	}
	
	public final int getTargetConsumeId()
	{
		return targetConsumeId;
	}
	
	public final int getTargetConsume()
	{
		return targetConsume;
	}
	
	public boolean hasSelfEffects()
	{
		return (effectTemplatesSelf != null && effectTemplatesSelf.length > 0);
	}
	
	/**
	 * @return minimum skill/effect land rate (default is 1).
	 */
	public final int getMinChance()
	{
		return minChance;
	}
	
	/**
	 * @return maximum skill/effect land rate (default is 99).
	 */
	public final int getMaxChance()
	{
		return maxChance;
	}
	
	public boolean is_advancedFlag()
	{
		return advancedFlag;
	}
	
	public int get_advancedMultiplier()
	{
		return advancedMultiplier;
	}
	
	public int getReuseHashCode()
	{
		return reuseHashCode;
	}
}