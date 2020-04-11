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
package l2r.gameserver.model.skills;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;
import l2r.Config;
import l2r.gameserver.GeoData;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.datatables.xml.SkillTreesData;
import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.handler.ITargetTypeHandler;
import l2r.gameserver.handler.TargetHandler;
import l2r.gameserver.model.ChanceCondition;
import l2r.gameserver.model.L2ExtractableProductItem;
import l2r.gameserver.model.L2ExtractableSkill;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2CubicInstance;
import l2r.gameserver.model.actor.instance.L2DoorInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2SiegeFlagInstance;
import l2r.gameserver.model.conditions.Condition;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.holders.ItemHolder;
import l2r.gameserver.model.interfaces.IChanceSkillTrigger;
import l2r.gameserver.model.interfaces.IIdentifiable;
import l2r.gameserver.model.skills.funcs.Func;
import l2r.gameserver.model.skills.funcs.FuncTemplate;
import l2r.gameserver.model.skills.targets.L2TargetType;
import l2r.gameserver.model.stats.BaseStats;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Formulas;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.FlyToLocation.FlyType;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.reunion.configsEngine.FormulasConfigs;
import gr.reunion.interf.ReunionEvents;

public abstract class L2Skill implements IChanceSkillTrigger, IIdentifiable
{
	protected static final Logger _log = LoggerFactory.getLogger(L2Skill.class);
	
	private static final L2Object[] _emptyTargetList = new L2Object[0];
	
	public static final int SKILL_CREATE_COMMON = 1320;
	public static final int SKILL_CREATE_DWARVEN = 172;
	public static final int SKILL_CRYSTALLIZE = 248;
	public static final int SKILL_DIVINE_INSPIRATION = 1405;
	public static final int SKILL_CLAN_LUCK = 390;
	public static final int SKILL_NPC_RACE = 4416;
	public static final int SKILL_ONYX_BEAST_TRANSFORMATION = 617;
	
	public static final int SKILL_SOUL_MASTERY = 467;
	
	// conditional values
	public static final int COND_RUNNING = 0x0001;
	public static final int COND_WALKING = 0x0002;
	public static final int COND_SIT = 0x0004;
	public static final int COND_BEHIND = 0x0008;
	public static final int COND_CRIT = 0x0010;
	public static final int COND_LOWHP = 0x0020;
	public static final int COND_ROBES = 0x0040;
	public static final int COND_CHARGES = 0x0080;
	public static final int COND_SHIELD = 0x0100;
	
	private static final Func[] _emptyFunctionSet = new Func[0];
	private static final L2Effect[] _emptyEffectSet = new L2Effect[0];
	
	// these two build the primary key
	private final int _id;
	private final int _level;
	
	/** Custom skill Id displayed by the client. */
	private final int _displayId;
	/** Custom skill level displayed by the client. */
	private final int _displayLevel;
	
	// not needed, just for easier debug
	private final String _name;
	private final L2SkillOpType _operateType;
	private final int _magic;
	private final L2TraitType _traitType;
	private final boolean _staticReuse;
	private final boolean _staticDamage; // Damage dealing skills do static damage based on the power value.
	private final int _mpConsume;
	private final int _mpInitialConsume;
	private final int _hpConsume;
	private final int _cpConsume;
	
	private final int _targetConsume;
	private final int _targetConsumeId;
	
	private final int _itemConsume;
	private final int _itemConsumeId;
	
	private final int _castRange;
	private final int _effectRange;
	
	// Abnormal levels for skills and their canceling, e.g. poison vs negate
	private final int _abnormalLvl; // e.g. poison or bleed lvl 2
	// Note: see also _effectAbnormalLvl
	private final int _negateLvl; // abnormalLvl is negated with negateLvl
	private final int[] _negateId; // cancels the effect of skill ID
	private final L2SkillType[] _negateStats; // lists the effect types that are canceled
	private final Map<String, Byte> _negateAbnormals; // lists the effect abnormal types with order below the presented that are canceled
	private final int _maxNegatedEffects; // maximum number of effects to negate
	
	private final boolean _stayAfterDeath; // skill should stay after death
	private final boolean _stayOnSubclassChange; // skill should stay on subclass change
	
	// kill by damage over time
	private final boolean _killByDOT;
	
	private final int _refId;
	// all times in milliseconds
	private final int _hitTime;
	private final int[] _hitTimings;
	// private final int _skillInterruptTime;
	private final int _coolTime;
	private final int _reuseHashCode;
	private final int _reuseDelay;
	
	/** Target type of the skill : SELF, PARTY, CLAN, PET... */
	private final L2TargetType _targetType;
	private final int _feed;
	// base success chance
	private final double _power;
	private final double _pvpPower;
	private final double _pvePower;
	private final int _magicLevel;
	private final int _lvlBonusRate;
	private final int _minChance;
	private final int _maxChance;
	private final int _blowChance;
	
	// Effecting area of the skill, in radius.
	// The radius center varies according to the _targetType:
	// "caster" if targetType = AURA/PARTY/CLAN or "target" if targetType = AREA
	private final int _affectRange;
	private final int[] _affectLimit = new int[2];
	
	private final L2SkillType _skillType;
	private final L2SkillType _effectType; // additional effect has a type
	private final int _effectAbnormalLvl; // abnormal level for the additional effect type, e.g. poison lvl 1
	private final int _effectId;
	private final int _effectLvl; // normal effect level
	
	private final boolean _nextActionIsAttack;
	
	private final boolean _removedOnAnyActionExceptMove;
	private final boolean _removedOnDamage;
	
	private final byte _element;
	private final int _elementPower;
	
	private final BaseStats _basicProperty;
	
	private final int _condition;
	private final int _conditionValue;
	private final boolean _overhit;
	
	private final int _minPledgeClass;
	private final boolean _isOffensive;
	private final int _chargeConsume;
	private final int _triggeredId;
	private final int _triggeredLevel;
	private final String _chanceType;
	private final int _soulMaxConsume;
	private final int _critChance;
	private final boolean _dependOnTargetBuff;
	
	private final int _transformId;
	private final int _transformDuration;
	
	private final int _afterEffectId;
	private final int _afterEffectLvl;
	private final boolean _isHeroSkill; // If true the skill is a Hero Skill
	private final boolean _isGMSkill; // True if skill is GM skill
	private final boolean _isSevenSigns;
	
	private final int _baseCritRate; // percent of success for skill critical hit (especially for PDAM & BLOW - they're not affected by rCrit values or buffs). Default loads -1 for all other skills but 0 to PDAM & BLOW
	private final int _halfKillRate;
	private final int _lethalStrikeRate;
	private final boolean _directHpDmg; // If true then dmg is being make directly
	private final boolean _isTriggeredSkill; // If true the skill will take activation buff slot instead of a normal buff slot
	private final float _sSBoost; // If true skill will have SoulShot boost (power*2)
	private final int _aggroPoints;
	
	protected List<Condition> _preCondition;
	protected List<Condition> _itemPreCondition;
	protected FuncTemplate[] _funcTemplates;
	public EffectTemplate[] _effectTemplates;
	protected EffectTemplate[] _effectTemplatesSelf;
	protected EffectTemplate[] _effectTemplatesPassive;
	
	protected ChanceCondition _chanceCondition = null;
	
	// Flying support
	private final FlyType _flyType;
	private final int _flyRadius;
	private final float _flyCourse;
	
	private final boolean _isDebuff;
	
	private final String _attribute;
	
	private final boolean _ignoreShield;
	
	private final boolean _isSuicideAttack;
	private final boolean _canBeReflected;
	private final boolean _canBeDispeled;
	
	private final boolean _isClanSkill;
	private final boolean _excludedFromCheck;
	private final boolean _simultaneousCast;
	
	private L2ExtractableSkill _extractableItems = null;
	
	private int _npcId = 0;
	
	private byte[] _effectTypes;
	
	protected L2Skill(StatsSet set)
	{
		_id = set.getInteger("skill_id");
		_level = set.getInteger("level");
		_refId = set.getInteger("referenceId", 0);
		_displayId = set.getInteger("displayId", _id);
		_displayLevel = set.getInteger("displayLevel", _level);
		_name = set.getString("name", "");
		_operateType = set.getEnum("operateType", L2SkillOpType.class);
		_magic = set.getInteger("isMagic", 0);
		_traitType = set.getEnum("trait", L2TraitType.class, L2TraitType.NONE);
		_staticReuse = set.getBool("staticReuse", false);
		_staticDamage = set.getBool("staticDamage", false);
		_mpConsume = set.getInteger("mpConsume", 0);
		_mpInitialConsume = set.getInteger("mpInitialConsume", 0);
		_hpConsume = set.getInteger("hpConsume", 0);
		_cpConsume = set.getInteger("cpConsume", 0);
		_targetConsume = set.getInteger("targetConsumeCount", 0);
		_targetConsumeId = set.getInteger("targetConsumeId", 0);
		_itemConsume = set.getInteger("itemConsumeCount", 0);
		_itemConsumeId = set.getInteger("itemConsumeId", 0);
		_afterEffectId = set.getInteger("afterEffectId", 0);
		_afterEffectLvl = set.getInteger("afterEffectLvl", 1);
		
		_castRange = set.getInteger("castRange", -1);
		_effectRange = set.getInteger("effectRange", -1);
		
		_abnormalLvl = set.getInteger("abnormalLvl", -1);
		_effectAbnormalLvl = set.getInteger("effectAbnormalLvl", -1); // support for a separate effect abnormal lvl, e.g. poison inside a different skill
		_negateLvl = set.getInteger("negateLvl", -1);
		
		_attribute = set.getString("attribute", "");
		String str = set.getString("negateStats", "");
		
		if (str.isEmpty())
		{
			_negateStats = new L2SkillType[0];
		}
		else
		{
			String[] stats = str.split(" ");
			L2SkillType[] array = new L2SkillType[stats.length];
			
			for (int i = 0; i < stats.length; i++)
			{
				L2SkillType type = null;
				try
				{
					type = Enum.valueOf(L2SkillType.class, stats[i]);
				}
				catch (Exception e)
				{
					throw new IllegalArgumentException("SkillId: " + _id + "Enum value of type " + L2SkillType.class.getName() + "required, but found: " + stats[i]);
				}
				
				array[i] = type;
			}
			_negateStats = array;
		}
		
		String negateAbnormals = set.getString("negateAbnormals", null);
		if ((negateAbnormals != null) && !negateAbnormals.isEmpty())
		{
			_negateAbnormals = new FastMap<>();
			for (String ngtStack : negateAbnormals.split(";"))
			{
				String[] ngt = ngtStack.split(",");
				if (ngt.length == 1) // Only abnormalType is present, without abnormalLvl
				{
					_negateAbnormals.put(ngt[0], Byte.MAX_VALUE);
				}
				else if (ngt.length == 2) // Both abnormalType and abnormalLvl are present
				{
					try
					{
						_negateAbnormals.put(ngt[0], Byte.parseByte(ngt[1]));
					}
					catch (Exception e)
					{
						throw new IllegalArgumentException("SkillId: " + _id + " Byte value required, but found: " + ngt[1]);
					}
				}
				else
				{
					throw new IllegalArgumentException("SkillId: " + _id + ": Incorrect negate Abnormals for " + ngtStack + ". Lvl: abnormalType1,abnormalLvl1;abnormalType2,abnormalLvl2;abnormalType3,abnormalLvl3... or abnormalType1;abnormalType2;abnormalType3...");
				}
			}
		}
		else
		{
			_negateAbnormals = null;
		}
		
		String negateId = set.getString("negateId", null);
		if (negateId != null)
		{
			String[] valuesSplit = negateId.split(",");
			_negateId = new int[valuesSplit.length];
			for (int i = 0; i < valuesSplit.length; i++)
			{
				_negateId[i] = Integer.parseInt(valuesSplit[i]);
			}
		}
		else
		{
			_negateId = new int[0];
		}
		_maxNegatedEffects = set.getInteger("maxNegated", 0);
		
		_stayAfterDeath = set.getBool("stayAfterDeath", false);
		_stayOnSubclassChange = set.getBool("stayOnSubclassChange", true);
		
		_killByDOT = set.getBool("killByDOT", false);
		_hitTime = set.getInteger("hitTime", 0);
		String hitTimings = set.getString("hitTimings", null);
		if (hitTimings != null)
		{
			try
			{
				String[] valuesSplit = hitTimings.split(",");
				_hitTimings = new int[valuesSplit.length];
				for (int i = 0; i < valuesSplit.length; i++)
				{
					_hitTimings[i] = Integer.parseInt(valuesSplit[i]);
				}
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("SkillId: " + _id + " invalid hitTimings value: " + hitTimings + ", \"percent,percent,...percent\" required");
			}
		}
		else
		{
			_hitTimings = new int[0];
		}
		
		_coolTime = set.getInteger("coolTime", 0);
		_isDebuff = set.getBool("isDebuff", false);
		_feed = set.getInteger("feed", 0);
		_reuseHashCode = SkillData.getSkillHashCode(_id, _level);
		
		if (Config.ENABLE_MODIFY_SKILL_REUSE && Config.SKILL_REUSE_LIST.containsKey(_id))
		{
			if (Config.DEBUG)
			{
				_log.info("*** Skill " + _name + " (" + _level + ") changed reuse from " + set.getInteger("reuseDelay", 0) + " to " + Config.SKILL_REUSE_LIST.get(_id) + " seconds.");
			}
			_reuseDelay = Config.SKILL_REUSE_LIST.get(_id);
		}
		else
		{
			_reuseDelay = set.getInteger("reuseDelay", 0);
		}
		
		_affectRange = set.getInteger("affectRange", 0);
		
		final String affectLimit = set.getString("affectLimit", null);
		if (affectLimit != null)
		{
			try
			{
				String[] valuesSplit = affectLimit.split("-");
				_affectLimit[0] = Integer.parseInt(valuesSplit[0]);
				_affectLimit[1] = Integer.parseInt(valuesSplit[1]);
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("SkillId: " + _id + " invalid affectLimit value: " + affectLimit + ", \"percent-percent\" required");
			}
		}
		
		_targetType = set.getEnum("targetType", L2TargetType.class);
		_power = set.getFloat("power", 0.f);
		_pvpPower = set.getFloat("pvpPower", (float) getPower());
		_pvePower = set.getFloat("pvePower", (float) getPower());
		_magicLevel = set.getInteger("magicLvl", 0);
		_lvlBonusRate = set.getInteger("lvlBonusRate", 0);
		_minChance = set.getInteger("minChance", FormulasConfigs.MIN_ABNORMAL_STATE_SUCCESS_RATE);
		_maxChance = set.getInteger("maxChance", FormulasConfigs.MAX_ABNORMAL_STATE_SUCCESS_RATE);
		_ignoreShield = set.getBool("ignoreShld", false);
		_skillType = set.getEnum("skillType", L2SkillType.class, L2SkillType.DUMMY);
		_effectType = set.getEnum("effectType", L2SkillType.class, null);
		_effectId = set.getInteger("effectId", 0);
		_effectLvl = set.getInteger("effectLevel", 0);
		
		_nextActionIsAttack = set.getBool("nextActionAttack", false);
		
		_removedOnAnyActionExceptMove = set.getBool("removedOnAnyActionExceptMove", false);
		_removedOnDamage = set.getBool("removedOnDamage", _skillType == L2SkillType.SLEEP);
		
		_element = set.getByte("element", (byte) -1);
		_elementPower = set.getInteger("elementPower", 0);
		
		_basicProperty = set.getEnum("basicProperty", BaseStats.class, BaseStats.NONE);
		
		_condition = set.getInteger("condition", 0);
		_conditionValue = set.getInteger("conditionValue", 0);
		_overhit = set.getBool("overHit", false);
		_isSuicideAttack = set.getBool("isSuicideAttack", false);
		
		_minPledgeClass = set.getInteger("minPledgeClass", 0);
		_isOffensive = set.getBool("offensive", false);
		_chargeConsume = set.getInteger("chargeConsume", 0);
		_triggeredId = set.getInteger("triggeredId", 0);
		_triggeredLevel = set.getInteger("triggeredLevel", 1);
		_chanceType = set.getString("chanceType", "");
		if (!_chanceType.isEmpty())
		{
			_chanceCondition = ChanceCondition.parse(set);
		}
		
		_soulMaxConsume = set.getInteger("soulMaxConsumeCount", 0);
		_blowChance = set.getInteger("blowChance", 0);
		_critChance = set.getInteger("critChance", 0);
		
		_transformId = set.getInteger("transformId", 0);
		_transformDuration = set.getInteger("transformDuration", 0);
		
		_isHeroSkill = SkillTreesData.getInstance().isHeroSkill(_id, _level);
		_isGMSkill = SkillTreesData.getInstance().isGMSkill(_id, _level);
		_isSevenSigns = (_id > 4360) && (_id < 4367);
		_isClanSkill = SkillTreesData.getInstance().isClanSkill(_id, _level);
		
		_baseCritRate = set.getInteger("baseCritRate", ((_skillType == L2SkillType.PDAM) || (_skillType == L2SkillType.BLOW)) ? 0 : -1);
		_halfKillRate = set.getInteger("halfKillRate", 0);
		_lethalStrikeRate = set.getInteger("lethalStrikeRate", 0);
		
		_directHpDmg = set.getBool("dmgDirectlyToHp", false);
		_isTriggeredSkill = set.getBool("isTriggeredSkill", false);
		_sSBoost = set.getFloat("SSBoost", 0.f);
		_aggroPoints = set.getInteger("aggroPoints", 0);
		
		_flyType = set.getEnum("flyType", FlyType.class, null);
		_flyRadius = set.getInteger("flyRadius", 0);
		_flyCourse = set.getFloat("flyCourse", 0);
		_canBeReflected = set.getBool("canBeReflected", true);
		
		_canBeDispeled = set.getBool("canBeDispeled", true);
		
		_excludedFromCheck = set.getBool("excludedFromCheck", false);
		_dependOnTargetBuff = set.getBool("dependOnTargetBuff", false);
		_simultaneousCast = set.getBool("simultaneousCast", false);
		
		String capsuled_items = set.getString("capsuled_items_skill", null);
		if (capsuled_items != null)
		{
			if (capsuled_items.isEmpty())
			{
				_log.warn("Empty Extractable Item Skill data in Skill Id: " + _id);
			}
			
			_extractableItems = parseExtractableSkill(_id, _level, capsuled_items);
		}
		_npcId = set.getInteger("npcId", 0);
	}
	
	public abstract void useSkill(L2Character caster, L2Object[] targets);
	
	public final int getConditionValue()
	{
		return _conditionValue;
	}
	
	public final L2SkillType getSkillType()
	{
		return _skillType;
	}
	
	public final L2TraitType getTraitType()
	{
		return _traitType;
	}
	
	public final byte getElement()
	{
		return _element;
	}
	
	public final int getElementPower()
	{
		return _elementPower;
	}
	
	/**
	 * Return the target type of the skill : SELF, PARTY, CLAN, PET...
	 * @return
	 */
	public final L2TargetType getTargetType()
	{
		return _targetType;
	}
	
	public final int getCondition()
	{
		return _condition;
	}
	
	public final boolean isOverhit()
	{
		return _overhit;
	}
	
	public final boolean killByDOT()
	{
		return _killByDOT;
	}
	
	public final boolean isSuicideAttack()
	{
		return _isSuicideAttack;
	}
	
	public final boolean allowOnTransform()
	{
		return isPassive();
	}
	
	/**
	 * Return the power of the skill.
	 * @param activeChar
	 * @param target
	 * @param isPvP
	 * @param isPvE
	 * @return
	 */
	public final double getPower(L2Character activeChar, L2Character target, boolean isPvP, boolean isPvE)
	{
		if (activeChar == null)
		{
			return getPower(isPvP, isPvE);
		}
		
		switch (_skillType)
		{
			case DEATHLINK:
			{
				return getPower(isPvP, isPvE) * (-((activeChar.getCurrentHp() * 2) / activeChar.getMaxHp()) + 2);
			}
			case FATAL:
			{
				return getPower(isPvP, isPvE) * (-((target.getCurrentHp() * 2) / target.getMaxHp()) + 2);
			}
			default:
			{
				return getPower(isPvP, isPvE);
			}
		}
	}
	
	public final double getPower()
	{
		return _power;
	}
	
	public final double getPower(boolean isPvP, boolean isPvE)
	{
		return isPvE ? _pvePower : isPvP ? _pvpPower : _power;
	}
	
	public final L2SkillType[] getNegateStats()
	{
		return _negateStats;
	}
	
	public final Map<String, Byte> getNegateAbnormals()
	{
		return _negateAbnormals;
	}
	
	public final int getAbnormalLvl()
	{
		return _abnormalLvl;
	}
	
	public final int getNegateLvl()
	{
		return _negateLvl;
	}
	
	public final int[] getNegateId()
	{
		return _negateId;
	}
	
	public final int getMagicLevel()
	{
		return _magicLevel;
	}
	
	public final int getMaxNegatedEffects()
	{
		return _maxNegatedEffects;
	}
	
	public final int getLvlBonusRate()
	{
		return _lvlBonusRate;
	}
	
	/**
	 * Return custom minimum skill/effect chance.
	 * @return
	 */
	public final int getMinChance()
	{
		return _minChance;
	}
	
	/**
	 * Return custom maximum skill/effect chance.
	 * @return
	 */
	public final int getMaxChance()
	{
		return _maxChance;
	}
	
	/**
	 * Return true if skill effects should be removed on any action except movement
	 * @return
	 */
	public final boolean isRemovedOnAnyActionExceptMove()
	{
		return _removedOnAnyActionExceptMove;
	}
	
	/**
	 * Return true if skill effects should be removed on damage
	 * @return
	 */
	public final boolean isRemovedOnDamage()
	{
		return _removedOnDamage;
	}
	
	/**
	 * Return the additional effect Id.
	 * @return
	 */
	public final int getEffectId()
	{
		return _effectId;
	}
	
	/**
	 * Return the additional effect level.
	 * @return
	 */
	public final int getEffectLvl()
	{
		return _effectLvl;
	}
	
	public final int getEffectAbnormalLvl()
	{
		return _effectAbnormalLvl;
	}
	
	/**
	 * Return the additional effect skill type (ex : STUN, PARALYZE,...).
	 * @return
	 */
	public final L2SkillType getEffectType()
	{
		return _effectType;
	}
	
	/**
	 * Return true if character should attack target after skill
	 * @return
	 */
	public final boolean nextActionIsAttack()
	{
		return _nextActionIsAttack;
	}
	
	/**
	 * TODO: Zoey76, temp fix until skill reworks is done.
	 * @return the calculated buff duration used to display buff icons.
	 */
	public final int getBuffDuration()
	{
		int duration = 0;
		final EffectTemplate firstEffect = hasEffects() ? getEffectTemplates()[0] : null;
		if (firstEffect != null)
		{
			duration = firstEffect.abnormalTime * 1000;
			if (firstEffect.counter > 1)
			{
				duration *= firstEffect.counter;
			}
		}
		return duration;
	}
	
	/**
	 * @return Returns the castRange.
	 */
	public final int getCastRange()
	{
		return _castRange;
	}
	
	/**
	 * @return Returns the cpConsume;
	 */
	public final int getCpConsume()
	{
		return _cpConsume;
	}
	
	/**
	 * @return Returns the effectRange.
	 */
	public final int getEffectRange()
	{
		return _effectRange;
	}
	
	/**
	 * @return Returns the hpConsume.
	 */
	public final int getHpConsume()
	{
		return _hpConsume + 1;
	}
	
	/**
	 * Gets the skill ID.
	 * @return the skill ID
	 */
	@Override
	public final int getId()
	{
		return _id;
	}
	
	/**
	 * @return Returns the boolean _isDebuff.
	 */
	public final boolean isDebuff()
	{
		return _isDebuff;
	}
	
	public int getDisplayId()
	{
		return _displayId;
	}
	
	public int getDisplayLevel()
	{
		return _displayLevel;
	}
	
	public int getTriggeredId()
	{
		return _triggeredId;
	}
	
	public int getTriggeredLevel()
	{
		return _triggeredLevel;
	}
	
	public boolean triggerAnotherSkill()
	{
		return _triggeredId > 1;
	}
	
	/**
	 * Return skill saveVs base stat (STR, INT ...).
	 * @return
	 */
	public final BaseStats getBasicProperty()
	{
		return _basicProperty;
	}
	
	/**
	 * @return Returns the _targetConsumeId.
	 */
	public final int getTargetConsumeId()
	{
		return _targetConsumeId;
	}
	
	/**
	 * @return Returns the targetConsume.
	 */
	public final int getTargetConsume()
	{
		return _targetConsume;
	}
	
	/**
	 * @return Returns the itemConsume.
	 */
	public final int getItemConsume()
	{
		return _itemConsume;
	}
	
	/**
	 * @return Returns the itemConsumeId.
	 */
	public final int getItemConsumeId()
	{
		return _itemConsumeId;
	}
	
	/**
	 * @return Returns the level.
	 */
	public final int getLevel()
	{
		return _level;
	}
	
	/**
	 * @return Returns true to set physical skills.
	 */
	public final boolean isPhysical()
	{
		return _magic == 0;
	}
	
	/**
	 * @return Returns true to set magic skills.
	 */
	public final boolean isMagic()
	{
		return _magic == 1;
	}
	
	/**
	 * @return Returns true to set static skills.
	 */
	public final boolean isStatic()
	{
		return _magic == 2;
	}
	
	public final boolean isDance()
	{
		return _magic == 3;
	}
	
	/**
	 * @return Returns true to set static reuse.
	 */
	public final boolean isStaticReuse()
	{
		return _staticReuse;
	}
	
	public final boolean isStaticDamage()
	{
		return _staticDamage;
	}
	
	/**
	 * @return Returns the mpConsume.
	 */
	public final int getMpConsume()
	{
		return _mpConsume;
	}
	
	/**
	 * @return Returns the mpInitialConsume.
	 */
	public final int getMpInitialConsume()
	{
		return _mpInitialConsume;
	}
	
	/**
	 * @return Returns the name.
	 */
	public final String getName()
	{
		return _name;
	}
	
	/**
	 * @return Returns the reuseDelay.
	 */
	public final int getReuseDelay()
	{
		return _reuseDelay;
	}
	
	public final int getReuseHashCode()
	{
		return _reuseHashCode;
	}
	
	public final int getHitTime()
	{
		return _hitTime;
	}
	
	public final int getHitCounts()
	{
		return _hitTimings.length;
	}
	
	public final int[] getHitTimings()
	{
		return _hitTimings;
	}
	
	/**
	 * @return Returns the coolTime.
	 */
	public final int getCoolTime()
	{
		return _coolTime;
	}
	
	public final int getAffectRange()
	{
		return _affectRange;
	}
	
	public final int getAffectLimit()
	{
		return (_affectLimit[0] + Rnd.get(_affectLimit[1]));
	}
	
	public final boolean isActive()
	{
		return (_operateType != null) && _operateType.isActive();
	}
	
	public final boolean isPassive()
	{
		return (_operateType != null) && _operateType.isPassive();
	}
	
	public final boolean isToggle()
	{
		return (_operateType != null) && _operateType.isToggle();
	}
	
	public final boolean isChance()
	{
		return (_chanceCondition != null) && isPassive();
	}
	
	public final boolean isTriggeredSkill()
	{
		return _isTriggeredSkill;
	}
	
	public final float getSSBoost()
	{
		return _sSBoost;
	}
	
	public final int getAggroPoints()
	{
		return _aggroPoints;
	}
	
	public final boolean useSoulShot()
	{
		switch (getSkillType())
		{
			case PDAM:
			case CHARGEDAM:
			case BLOW:
				return true;
			default:
				return false;
		}
	}
	
	public final boolean useSpiritShot()
	{
		return _magic == 1;
	}
	
	public final boolean useFishShot()
	{
		return ((getSkillType() == L2SkillType.PUMPING) || (getSkillType() == L2SkillType.REELING));
	}
	
	public int getMinPledgeClass()
	{
		return _minPledgeClass;
	}
	
	public final boolean isOffensive()
	{
		return _isOffensive;
	}
	
	public final boolean isHeroSkill()
	{
		return _isHeroSkill;
	}
	
	public final boolean isGMSkill()
	{
		return _isGMSkill;
	}
	
	public final boolean is7Signs()
	{
		return _isSevenSigns;
	}
	
	public final int getChargeConsume()
	{
		return _chargeConsume;
	}
	
	public final int getMaxSoulConsumeCount()
	{
		return _soulMaxConsume;
	}
	
	public final int getCritChance()
	{
		return _critChance;
	}
	
	public final int getTransformId()
	{
		return _transformId;
	}
	
	public final int getTransformDuration()
	{
		return _transformDuration;
	}
	
	public final int getBaseCritRate()
	{
		return _baseCritRate;
	}
	
	public final int getHalfKillRate()
	{
		return _halfKillRate;
	}
	
	public final int getLethalStrikeRate()
	{
		return _lethalStrikeRate;
	}
	
	public final boolean getDmgDirectlyToHP()
	{
		return _directHpDmg;
	}
	
	public final FlyType getFlyType()
	{
		return _flyType;
	}
	
	public final int getFlyRadius()
	{
		return _flyRadius;
	}
	
	public final float getFlyCourse()
	{
		return _flyCourse;
	}
	
	public final boolean isStayAfterDeath()
	{
		return _stayAfterDeath;
	}
	
	public final boolean isStayOnSubclassChange()
	{
		return _stayOnSubclassChange;
	}
	
	public boolean checkCondition(L2Character activeChar, L2Object target, boolean itemOrWeapon)
	{
		if (activeChar.canOverrideCond(PcCondOverride.SKILL_CONDITIONS) && !Config.GM_SKILL_RESTRICTION)
		{
			return true;
		}
		if ((getCondition() & L2Skill.COND_SHIELD) != 0)
		{
			// L2ItemInstance dummy = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			// L2Armor armorPiece = (L2Armor) dummy.getItem();
			// TODO add checks for shield here.
		}
		
		List<Condition> preCondition = _preCondition;
		if (itemOrWeapon)
		{
			preCondition = _itemPreCondition;
		}
		
		if ((preCondition == null) || preCondition.isEmpty())
		{
			return true;
		}
		
		for (Condition cond : preCondition)
		{
			Env env = new Env();
			env.setCharacter(activeChar);
			if (target instanceof L2Character)
			{
				env.setTarget((L2Character) target);
			}
			env.setSkill(this);
			
			if (!cond.test(env))
			{
				String msg = cond.getMessage();
				int msgId = cond.getMessageId();
				if (msgId != 0)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(msgId);
					if (cond.isAddName())
					{
						sm.addSkillName(_id);
					}
					activeChar.sendPacket(sm);
				}
				else if (msg != null)
				{
					activeChar.sendMessage(msg);
				}
				return false;
			}
		}
		return true;
	}
	
	public final L2Object[] getTargetList(L2Character activeChar, boolean onlyFirst)
	{
		// Init to null the target of the skill
		L2Character target = null;
		
		// Get the L2Objcet targeted by the user of the skill at this moment
		L2Object objTarget = activeChar.getTarget();
		// If the L2Object targeted is a L2Character, it becomes the L2Character target
		if (objTarget instanceof L2Character)
		{
			target = (L2Character) objTarget;
		}
		
		return getTargetList(activeChar, onlyFirst, target);
	}
	
	/**
	 * Return all targets of the skill in a table in function a the skill type.<br>
	 * <B><U>Values of skill type</U>:</B>
	 * <ul>
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
	 * <li>ITEM</li>
	 * <ul>
	 * @param activeChar The L2Character who use the skill
	 * @param onlyFirst
	 * @param target
	 * @return
	 */
	public final L2Object[] getTargetList(L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		final ITargetTypeHandler handler = TargetHandler.getInstance().getHandler(getTargetType());
		if (handler != null)
		{
			try
			{
				return handler.getTargetList(this, activeChar, onlyFirst, target);
			}
			catch (Exception e)
			{
				_log.warn("Exception in L2Skill.getTargetList(): " + e.getMessage(), e);
			}
		}
		activeChar.sendMessage("Target type of skill is not currently handled.");
		return _emptyTargetList;
	}
	
	public final L2Object[] getTargetList(L2Character activeChar)
	{
		return getTargetList(activeChar, false);
	}
	
	public final L2Object getFirstOfTargetList(L2Character activeChar)
	{
		L2Object[] targets = getTargetList(activeChar, true);
		if (targets.length == 0)
		{
			return null;
		}
		return targets[0];
	}
	
	/**
	 * Check if should be target added to the target list false if target is dead, target same as caster,<br>
	 * target inside peace zone, target in the same party with caster, caster can see target Additional checks if not in PvP zones (arena, siege):<br>
	 * target in not the same clan and alliance with caster, and usual skill PvP check. If TvT event is active - performing additional checks. Caution: distance is not checked.
	 * @param caster
	 * @param target
	 * @param skill
	 * @param sourceInArena
	 * @return
	 */
	public static final boolean checkForAreaOffensiveSkills(L2Character caster, L2Character target, L2Skill skill, boolean sourceInArena)
	{
		if ((target == null) || target.isDead() || (target == caster))
		{
			return false;
		}
		
		boolean geoEnabled = Config.GEODATA > 0;
		boolean geoCanSeeTarget = geoEnabled ? GeoData.getInstance().canSeeTarget(caster, target) : true;
		
		final L2PcInstance player = caster.getActingPlayer();
		final L2PcInstance targetPlayer = target.getActingPlayer();
		if (player != null)
		{
			if (targetPlayer != null)
			{
				if ((targetPlayer == caster) || (targetPlayer == player))
				{
					return false;
				}
				
				if (ReunionEvents.isInEvent(player))
				{
					if (!ReunionEvents.isInEvent(targetPlayer))
					{
						return false;
					}
					
					if (!ReunionEvents.isSkillNeutral(player, skill))
					{
						if (ReunionEvents.isSkillOffensive(player, skill) && !ReunionEvents.canAttack(player, targetPlayer))
						{
							return false;
						}
						
						if (!ReunionEvents.isSkillOffensive(player, skill) && !ReunionEvents.canSupport(player, targetPlayer))
						{
							return false;
						}
					}
					
					return geoCanSeeTarget;
				}
				
				if (targetPlayer.inObserverMode())
				{
					return false;
				}
				
				if (skill.isOffensive() && (player.getSiegeState() > 0) && player.isInsideZone(ZoneIdType.SIEGE) && (player.getSiegeState() == targetPlayer.getSiegeState()) && (player.getSiegeSide() == targetPlayer.getSiegeSide()))
				{
					return false;
				}
				
				if (skill.isOffensive() && target.isInsideZone(ZoneIdType.PEACE))
				{
					return false;
				}
				
				if (player.isInParty() && targetPlayer.isInParty())
				{
					// Same party
					if (player.getParty().getLeaderObjectId() == targetPlayer.getParty().getLeaderObjectId())
					{
						return false;
					}
					
					// Same command channel
					if (player.getParty().isInCommandChannel() && (player.getParty().getCommandChannel() == targetPlayer.getParty().getCommandChannel()))
					{
						return false;
					}
				}
				
				if (!sourceInArena && !(targetPlayer.isInsideZone(ZoneIdType.PVP) && !targetPlayer.isInsideZone(ZoneIdType.SIEGE)))
				{
					if ((player.getAllyId() != 0) && (player.getAllyId() == targetPlayer.getAllyId()))
					{
						return false;
					}
					
					if ((player.getClanId() != 0) && (player.getClanId() == targetPlayer.getClanId()))
					{
						return false;
					}
					
					if (!player.checkPvpSkill(targetPlayer, skill, (caster instanceof L2Summon)))
					{
						return false;
					}
				}
			}
		}
		else
		{
			// target is mob
			if ((targetPlayer == null) && (target.isAttackable()) && (caster.isAttackable()))
			{
				String casterEnemyClan = ((L2Attackable) caster).getEnemyClan();
				if ((casterEnemyClan == null) || casterEnemyClan.isEmpty())
				{
					return false;
				}
				
				String targetClan = ((L2Attackable) target).getClan();
				if ((targetClan == null) || targetClan.isEmpty())
				{
					return false;
				}
				
				if (!casterEnemyClan.equals(targetClan))
				{
					return false;
				}
			}
		}
		
		if (geoEnabled && !GeoData.getInstance().canSeeTarget(caster, target))
		{
			return false;
		}
		
		return true;
	}
	
	public static final boolean addSummon(L2Character caster, L2PcInstance owner, int radius, boolean isDead)
	{
		if (!owner.hasSummon())
		{
			return false;
		}
		return addCharacter(caster, owner.getSummon(), radius, isDead);
	}
	
	public static final boolean addCharacter(L2Character caster, L2Character target, int radius, boolean isDead)
	{
		if (isDead != target.isDead())
		{
			return false;
		}
		
		if ((radius > 0) && !Util.checkIfInRange(radius, caster, target, true))
		{
			return false;
		}
		
		return true;
		
	}
	
	public final Func[] getStatFuncs(L2Effect effect, L2Character player)
	{
		if (_funcTemplates == null)
		{
			return _emptyFunctionSet;
		}
		
		if (!(player instanceof L2Playable) && !(player.isAttackable()))
		{
			return _emptyFunctionSet;
		}
		
		List<Func> funcs = new ArrayList<>(_funcTemplates.length);
		
		Env env = new Env();
		env.setCharacter(player);
		env.setSkill(this);
		
		Func f;
		for (FuncTemplate t : _funcTemplates)
		{
			f = t.getFunc(env, this); // skill is owner
			if (f != null)
			{
				funcs.add(f);
			}
		}
		
		if (funcs.isEmpty())
		{
			return _emptyFunctionSet;
		}
		return funcs.toArray(new Func[funcs.size()]);
	}
	
	public boolean hasEffects()
	{
		return ((_effectTemplates != null) && (_effectTemplates.length > 0));
	}
	
	public EffectTemplate[] getEffectTemplates()
	{
		return _effectTemplates;
	}
	
	public EffectTemplate[] getEffectTemplatesPassive()
	{
		return _effectTemplatesPassive;
	}
	
	public boolean hasSelfEffects()
	{
		return ((_effectTemplatesSelf != null) && (_effectTemplatesSelf.length > 0));
	}
	
	public boolean hasPassiveEffects()
	{
		return ((_effectTemplatesPassive != null) && (_effectTemplatesPassive.length > 0));
	}
	
	/**
	 * Env is used to pass parameters for secondary effects (shield and ss/bss/bsss)
	 * @param effector
	 * @param effected
	 * @param env
	 * @return an array with the effects that have been added to effector
	 */
	public final L2Effect[] getEffects(L2Character effector, L2Character effected, Env env)
	{
		if (!hasEffects() || isPassive())
		{
			return _emptyEffectSet;
		}
		
		// doors and siege flags cannot receive any effects
		if ((effected instanceof L2DoorInstance) || (effected instanceof L2SiegeFlagInstance))
		{
			return _emptyEffectSet;
		}
		
		// Check bad skills against target.
		if ((effector != effected) && isOffensive() && (effected.isInvul() || (effector.isGM() && !effector.getAccessLevel().canGiveDamage())))
		{
			return _emptyEffectSet;
		}
		
		if (effected.isInvulAgainst(getId(), getLevel()))
		{
			effected.sendDebugMessage("Skill " + toString() + " has been ignored (invul against)");
			return _emptyEffectSet;
		}
		
		List<L2Effect> effects = new ArrayList<>(_effectTemplates.length);
		if (env == null)
		{
			env = new Env();
		}
		
		env.setSkillMastery(Formulas.calcSkillMastery(effector, this));
		env.setCharacter(effector);
		env.setTarget(effected);
		env.setSkill(this);
		
		for (EffectTemplate et : _effectTemplates)
		{
			if (Formulas.calcEffectSuccess(effector, effected, et, this, env.getShield(), env.isSoulShot(), env.isSpiritShot(), env.isBlessedSpiritShot()))
			{
				L2Effect e = et.getEffect(env);
				if (e != null)
				{
					e.scheduleEffect();
					effects.add(e);
				}
			}
			// display fail message only for effects with icons
			else if (et.icon && effector.isPlayer())
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
				sm.addCharName(effected);
				sm.addSkillName(this);
				((L2PcInstance) effector).sendPacket(sm);
			}
		}
		
		if (effects.isEmpty())
		{
			return _emptyEffectSet;
		}
		
		return effects.toArray(new L2Effect[effects.size()]);
	}
	
	/**
	 * Warning: this method doesn't consider modifier (shield, ss, sps, bss) for secondary effects
	 * @param effector
	 * @param effected
	 * @return
	 */
	public final L2Effect[] getEffects(L2Character effector, L2Character effected)
	{
		return getEffects(effector, effected, null);
	}
	
	/**
	 * This method has suffered some changes in CT2.2 ->CT2.3<br>
	 * Effect engine is now supporting secondary effects with independent success/fail calculus from effect skill.<br>
	 * Env parameter has been added to pass parameters like soulshot, spiritshots, blessed spiritshots or shield defense.<br>
	 * Some other optimizations have been done<br>
	 * This new feature works following next rules:
	 * <ul>
	 * <li>To enable feature, effectPower must be over -1 (check DocumentSkill#attachEffect for further information)</li>
	 * <li>If main skill fails, secondary effect always fail</li>
	 * </ul>
	 * @param effector
	 * @param effected
	 * @param env
	 * @return
	 */
	public final L2Effect[] getEffects(L2CubicInstance effector, L2Character effected, Env env)
	{
		if (!hasEffects() || isPassive())
		{
			return _emptyEffectSet;
		}
		
		if (effector.getOwner() != effected)
		{
			if (isDebuff() || isOffensive())
			{
				if (effected.isInvul())
				{
					return _emptyEffectSet;
				}
				
				if (effector.getOwner().isGM() && !effector.getOwner().getAccessLevel().canGiveDamage())
				{
					return _emptyEffectSet;
				}
			}
		}
		
		List<L2Effect> effects = new ArrayList<>(_effectTemplates.length);
		if (env == null)
		{
			env = new Env();
		}
		
		env.setCharacter(effector.getOwner());
		env.setCubic(effector);
		env.setTarget(effected);
		env.setSkill(this);
		
		for (EffectTemplate et : _effectTemplates)
		{
			if (Formulas.calcEffectSuccess(effector.getOwner(), effected, et, this, env.getShield(), env.isSoulShot(), env.isSpiritShot(), env.isBlessedSpiritShot()))
			{
				L2Effect e = et.getEffect(env);
				if (e != null)
				{
					e.scheduleEffect();
					effects.add(e);
				}
			}
		}
		
		if (effects.isEmpty())
		{
			return _emptyEffectSet;
		}
		
		return effects.toArray(new L2Effect[effects.size()]);
	}
	
	public final L2Effect[] getEffectsSelf(L2Character effector)
	{
		if (!hasSelfEffects() || isPassive())
		{
			return _emptyEffectSet;
		}
		
		List<L2Effect> effects = new ArrayList<>(_effectTemplatesSelf.length);
		for (EffectTemplate et : _effectTemplatesSelf)
		{
			Env env = new Env();
			env.setCharacter(effector);
			env.setTarget(effector);
			env.setSkill(this);
			L2Effect e = et.getEffect(env);
			if (e != null)
			{
				e.setSelfEffect();
				e.scheduleEffect();
				effects.add(e);
			}
		}
		
		if (effects.isEmpty())
		{
			return _emptyEffectSet;
		}
		return effects.toArray(new L2Effect[effects.size()]);
	}
	
	public final L2Effect[] getEffectsPassive(L2Character effector)
	{
		if (!hasPassiveEffects())
		{
			return _emptyEffectSet;
		}
		
		List<L2Effect> effects = new ArrayList<>(_effectTemplatesPassive.length);
		for (EffectTemplate et : _effectTemplatesPassive)
		{
			Env env = new Env();
			env.setCharacter(effector);
			env.setTarget(effector);
			env.setSkill(this);
			L2Effect e = et.getEffect(env);
			if (e != null)
			{
				e.setPassiveEffect();
				e.scheduleEffect();
				effects.add(e);
			}
		}
		
		if (effects.isEmpty())
		{
			return _emptyEffectSet;
		}
		return effects.toArray(new L2Effect[effects.size()]);
	}
	
	public final void attach(FuncTemplate f)
	{
		if (_funcTemplates == null)
		{
			_funcTemplates = new FuncTemplate[]
			{
				f
			};
		}
		else
		{
			int len = _funcTemplates.length;
			FuncTemplate[] tmp = new FuncTemplate[len + 1];
			System.arraycopy(_funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			_funcTemplates = tmp;
		}
	}
	
	public final void attach(EffectTemplate effect)
	{
		if (_effectTemplates == null)
		{
			_effectTemplates = new EffectTemplate[]
			{
				effect
			};
		}
		else
		{
			int len = _effectTemplates.length;
			EffectTemplate[] tmp = new EffectTemplate[len + 1];
			System.arraycopy(_effectTemplates, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplates = tmp;
		}
		
	}
	
	public final void attachSelf(EffectTemplate effect)
	{
		if (_effectTemplatesSelf == null)
		{
			_effectTemplatesSelf = new EffectTemplate[]
			{
				effect
			};
		}
		else
		{
			int len = _effectTemplatesSelf.length;
			EffectTemplate[] tmp = new EffectTemplate[len + 1];
			System.arraycopy(_effectTemplatesSelf, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplatesSelf = tmp;
		}
	}
	
	public final void attachPassive(EffectTemplate effect)
	{
		if (_effectTemplatesPassive == null)
		{
			_effectTemplatesPassive = new EffectTemplate[]
			{
				effect
			};
		}
		else
		{
			int len = _effectTemplatesPassive.length;
			EffectTemplate[] tmp = new EffectTemplate[len + 1];
			System.arraycopy(_effectTemplatesPassive, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplatesPassive = tmp;
		}
	}
	
	public final void attach(Condition c, boolean itemOrWeapon)
	{
		if (itemOrWeapon)
		{
			if (_itemPreCondition == null)
			{
				_itemPreCondition = new ArrayList<>();
			}
			_itemPreCondition.add(c);
		}
		else
		{
			if (_preCondition == null)
			{
				_preCondition = new ArrayList<>();
			}
			_preCondition.add(c);
		}
	}
	
	@Override
	public String toString()
	{
		return _name + "[id=" + _id + ",lvl=" + _level + "]";
	}
	
	/**
	 * @return pet food
	 */
	public int getFeed()
	{
		return _feed;
	}
	
	/**
	 * used for tracking item id in case that item consume cannot be used
	 * @return reference item id
	 */
	public int getReferenceItemId()
	{
		return _refId;
	}
	
	public int getAfterEffectId()
	{
		return _afterEffectId;
	}
	
	public int getAfterEffectLvl()
	{
		return _afterEffectLvl;
	}
	
	@Override
	public boolean triggersChanceSkill()
	{
		return (_triggeredId > 0) && isChance();
	}
	
	@Override
	public int getTriggeredChanceId()
	{
		return _triggeredId;
	}
	
	@Override
	public int getTriggeredChanceLevel()
	{
		return _triggeredLevel;
	}
	
	@Override
	public ChanceCondition getTriggeredChanceCondition()
	{
		return _chanceCondition;
	}
	
	public String getAttributeName()
	{
		return _attribute;
	}
	
	/**
	 * @return the _blowChance
	 */
	public int getBlowChance()
	{
		return _blowChance;
	}
	
	public boolean ignoreShield()
	{
		return _ignoreShield;
	}
	
	public boolean canBeReflected()
	{
		return _canBeReflected;
	}
	
	public boolean canBeDispeled()
	{
		return _canBeDispeled;
	}
	
	public boolean isClanSkill()
	{
		return _isClanSkill;
	}
	
	public boolean isExcludedFromCheck()
	{
		return _excludedFromCheck;
	}
	
	public boolean getDependOnTargetBuff()
	{
		return _dependOnTargetBuff;
	}
	
	public boolean isSimultaneousCast()
	{
		return _simultaneousCast;
	}
	
	/**
	 * @param skillId
	 * @param skillLvl
	 * @param values
	 * @return L2ExtractableSkill
	 * @author Zoey76
	 */
	private L2ExtractableSkill parseExtractableSkill(int skillId, int skillLvl, String values)
	{
		final String[] prodLists = values.split(";");
		final List<L2ExtractableProductItem> products = new ArrayList<>();
		String[] prodData;
		for (String prodList : prodLists)
		{
			prodData = prodList.split(",");
			if (prodData.length < 3)
			{
				_log.warn("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> wrong seperator!");
			}
			List<ItemHolder> items = null;
			double chance = 0;
			int prodId = 0;
			int quantity = 0;
			final int lenght = prodData.length - 1;
			try
			{
				items = new ArrayList<>(lenght / 2);
				for (int j = 0; j < lenght; j++)
				{
					prodId = Integer.parseInt(prodData[j]);
					quantity = Integer.parseInt(prodData[j += 1]);
					if ((prodId <= 0) || (quantity <= 0))
					{
						_log.warn("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " wrong production Id: " + prodId + " or wrond quantity: " + quantity + "!");
					}
					items.add(new ItemHolder(prodId, quantity));
				}
				chance = Double.parseDouble(prodData[lenght]);
			}
			catch (Exception e)
			{
				_log.warn("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> incomplete/invalid production data or wrong seperator!");
			}
			products.add(new L2ExtractableProductItem(items, chance));
		}
		
		if (products.isEmpty())
		{
			_log.warn("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> There are no production items!");
		}
		return new L2ExtractableSkill(SkillData.getSkillHashCode(skillId, skillLvl), products);
	}
	
	public L2ExtractableSkill getExtractableSkill()
	{
		return _extractableItems;
	}
	
	/**
	 * @return the _npcId
	 */
	public int getNpcId()
	{
		return _npcId;
	}
	
	/**
	 * @param types
	 * @return {@code true} if at least one of specified {@link L2EffectType} types present on the current skill's effects, {@code false} otherwise.
	 */
	public boolean hasEffectType(L2EffectType... types)
	{
		if (hasEffects() && (types != null) && (types.length > 0))
		{
			if (_effectTypes == null)
			{
				_effectTypes = new byte[_effectTemplates.length];
				
				final Env env = new Env();
				env.setSkill(this);
				
				int i = 0;
				for (EffectTemplate et : _effectTemplates)
				{
					final L2Effect e = et.getEffect(env, true);
					if (e == null)
					{
						continue;
					}
					_effectTypes[i++] = (byte) e.getEffectType().ordinal();
				}
				Arrays.sort(_effectTypes);
			}
			
			for (L2EffectType type : types)
			{
				if (Arrays.binarySearch(_effectTypes, (byte) type.ordinal()) >= 0)
				{
					return true;
				}
			}
		}
		return false;
	}
}