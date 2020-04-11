/*
 * Copyright (C) 2004-2015 L2J Server
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
package l2r.gameserver.model.actor.templates;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import l2r.gameserver.data.xml.impl.HerbDropData;
import l2r.gameserver.enums.AISkillScope;
import l2r.gameserver.enums.Race;
import l2r.gameserver.model.L2DropCategory;
import l2r.gameserver.model.L2DropData;
import l2r.gameserver.model.L2MinionData;
import l2r.gameserver.model.L2NpcAIData;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.interfaces.IIdentifiable;
import l2r.gameserver.model.skills.L2Skill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Zoey76
 */
public final class L2NpcTemplate extends L2CharTemplate implements IIdentifiable
{
	private static final Logger _log = LoggerFactory.getLogger(L2NpcTemplate.class);
	
	public final int _id;
	private final int _displayId;
	private final String _type;
	private final String _name;
	private final boolean _serverSideName;
	private final String _title;
	private final boolean _serverSideTitle;
	private final String _sex;
	private byte _level;
	private final int _rewardExp;
	private final int _rewardSp;
	private final int _chestId;
	private final int _rhandId;
	private final int _lhandId;
	private final int _weaponEnchant;
	
	private Race _race = Race.NONE;
	private final String _clientClass;
	
	private final int _dropHerbGroup;
	private final boolean _isCustom;
	/**
	 * Doesn't include all mobs that are involved in quests, just plain quest monsters for preventing champion spawn.
	 */
	private final boolean _isQuestMonster;
	private final float _baseVitalityDivider;
	
	// Skill AI
	private final List<L2Skill> _buffSkills = new ArrayList<>();
	private final List<L2Skill> _negativeSkills = new ArrayList<>();
	private final List<L2Skill> _debuffSkills = new ArrayList<>();
	private final List<L2Skill> _atkSkills = new ArrayList<>();
	private final List<L2Skill> _rootSkills = new ArrayList<>();
	private final List<L2Skill> _stunskills = new ArrayList<>();
	private final List<L2Skill> _sleepSkills = new ArrayList<>();
	private final List<L2Skill> _paralyzeSkills = new ArrayList<>();
	private final List<L2Skill> _fossilSkills = new ArrayList<>();
	private final List<L2Skill> _floatSkills = new ArrayList<>();
	private final List<L2Skill> _immobilizeSkills = new ArrayList<>();
	private final List<L2Skill> _healSkills = new ArrayList<>();
	private final List<L2Skill> _resSkills = new ArrayList<>();
	private final List<L2Skill> _dotSkills = new ArrayList<>();
	private final List<L2Skill> _cotSkills = new ArrayList<>();
	private final List<L2Skill> _universalSkills = new ArrayList<>();
	private final List<L2Skill> _manaSkills = new ArrayList<>();
	private final List<L2Skill> _longRangeSkills = new ArrayList<>();
	private final List<L2Skill> _shortRangeSkills = new ArrayList<>();
	private final List<L2Skill> _generalSkills = new ArrayList<>();
	private final List<L2Skill> _suicideSkills = new ArrayList<>();
	
	private L2NpcAIData _AIdataStatic = new L2NpcAIData();
	
	/**
	 * The table containing all Item that can be dropped by L2NpcInstance using this L2NpcTemplate
	 */
	private final List<L2DropCategory> _categories = new ArrayList<>();
	
	/**
	 * The table containing all Minions that must be spawn with the L2NpcInstance using this L2NpcTemplate
	 */
	private final List<L2MinionData> _minions = new ArrayList<>();
	
	private final List<ClassId> _teachInfo = new ArrayList<>();
	
	private final Map<Integer, L2Skill> _skills = new ConcurrentHashMap<>();
	
	private StatsSet _parameters = StatsSet.EMPTY_STATSET;
	
	public static boolean isAssignableTo(Class<?> sub, Class<?> clazz)
	{
		// If clazz represents an interface
		if (clazz.isInterface())
		{
			// check if obj implements the clazz interface
			Class<?>[] interfaces = sub.getInterfaces();
			for (Class<?> interface1 : interfaces)
			{
				if (clazz.getName().equals(interface1.getName()))
				{
					return true;
				}
			}
		}
		else
		{
			do
			{
				if (sub.getName().equals(clazz.getName()))
				{
					return true;
				}
				
				sub = sub.getSuperclass();
			}
			while (sub != null);
		}
		return false;
	}
	
	/**
	 * Checks if obj can be assigned to the Class represented by clazz.<br>
	 * This is true if, and only if, obj is the same class represented by clazz, or a subclass of it or obj implements the interface represented by clazz.
	 * @param obj
	 * @param clazz
	 * @return {@code true} if the object can be assigned to the class, {@code false} otherwise
	 */
	public static boolean isAssignableTo(Object obj, Class<?> clazz)
	{
		return L2NpcTemplate.isAssignableTo(obj.getClass(), clazz);
	}
	
	/**
	 * Constructor of L2Character.
	 * @param set The StatsSet object to transfer data to the method
	 */
	public L2NpcTemplate(StatsSet set)
	{
		super(set);
		_id = set.getInt("npcId");
		_displayId = set.getInt("idTemplate");
		_type = set.getString("type");
		_name = set.getString("name");
		_serverSideName = set.getBoolean("serverSideName");
		_title = set.getString("title");
		_isQuestMonster = getTitle().equalsIgnoreCase("Quest Monster");
		_serverSideTitle = set.getBoolean("serverSideTitle");
		_sex = set.getString("sex");
		_level = set.getByte("level");
		_rewardExp = set.getInt("rewardExp");
		_rewardSp = set.getInt("rewardSp");
		
		_chestId = set.getInt("chestId", 0);
		_rhandId = set.getInt("rhandId", 0);
		_lhandId = set.getInt("lhandId", 0);
		_weaponEnchant = set.getInt("weaponEnchant", 0);
		
		_race = null;
		final int herbGroup = set.getInt("dropHerbGroup");
		if ((herbGroup > 0) && (HerbDropData.getInstance().getHerbDroplist(herbGroup) == null))
		{
			_log.warn("Missing Herb Drop Group for npcId: " + getId());
			_dropHerbGroup = 0;
		}
		else
		{
			_dropHerbGroup = herbGroup;
		}
		
		_clientClass = set.getString("client_class");
		
		// TODO: Could be loaded from db.
		_baseVitalityDivider = (getLevel() > 0) && (getRewardExp() > 0) ? (getBaseHpMax() * 9 * getLevel() * getLevel()) / (100 * getRewardExp()) : 0;
		
		_isCustom = _id != _displayId;
	}
	
	public void addAtkSkill(L2Skill skill)
	{
		_atkSkills.add(skill);
	}
	
	public void addBuffSkill(L2Skill skill)
	{
		_buffSkills.add(skill);
	}
	
	public void addCOTSkill(L2Skill skill)
	{
		_cotSkills.add(skill);
	}
	
	public void addDebuffSkill(L2Skill skill)
	{
		_debuffSkills.add(skill);
	}
	
	public void addDOTSkill(L2Skill skill)
	{
		_dotSkills.add(skill);
	}
	
	/**
	 * Add a drop to a given category.<br>
	 * If the category does not exist, create it.
	 * @param drop
	 * @param categoryType
	 */
	public void addDropData(L2DropData drop, int categoryType)
	{
		if (!drop.isQuestDrop())
		{
			// If the category doesn't already exist, create it first
			synchronized (_categories)
			{
				boolean catExists = false;
				for (L2DropCategory cat : _categories)
				{
					// If the category exists, add the drop to this category.
					if (cat.getCategoryType() == categoryType)
					{
						cat.addDropData(drop, isType("L2RaidBoss") || isType("L2GrandBoss"));
						catExists = true;
						break;
					}
				}
				// If the category doesn't exit, create it and add the drop
				if (!catExists)
				{
					final L2DropCategory cat = new L2DropCategory(categoryType);
					cat.addDropData(drop, isType("L2RaidBoss") || isType("L2GrandBoss"));
					_categories.add(cat);
				}
			}
		}
	}
	
	public void addFloatSkill(L2Skill skill)
	{
		_floatSkills.add(skill);
	}
	
	public void addFossilSkill(L2Skill skill)
	{
		_fossilSkills.add(skill);
	}
	
	public void addGeneralSkill(L2Skill skill)
	{
		_generalSkills.add(skill);
	}
	
	public void addHealSkill(L2Skill skill)
	{
		_healSkills.add(skill);
	}
	
	public void addImmobiliseSkill(L2Skill skill)
	{
		_immobilizeSkills.add(skill);
	}
	
	public void addManaHealSkill(L2Skill skill)
	{
		_manaSkills.add(skill);
	}
	
	public void addNegativeSkill(L2Skill skill)
	{
		_negativeSkills.add(skill);
	}
	
	public void addParalyzeSkill(L2Skill skill)
	{
		_paralyzeSkills.add(skill);
	}
	
	public void addRaidData(L2MinionData minion)
	{
		_minions.add(minion);
	}
	
	public void addRangeSkill(L2Skill skill)
	{
		if ((skill.getCastRange() <= 150) && (skill.getCastRange() > 0))
		{
			_shortRangeSkills.add(skill);
		}
		else if (skill.getCastRange() > 150)
		{
			_longRangeSkills.add(skill);
		}
	}
	
	public void addResSkill(L2Skill skill)
	{
		_resSkills.add(skill);
	}
	
	public void addRootSkill(L2Skill skill)
	{
		_rootSkills.add(skill);
	}
	
	public void addSkill(L2Skill skill)
	{
		if (!skill.isPassive())
		{
			if (skill.isSuicideAttack())
			{
				addSuicideSkill(skill);
			}
			else
			{
				addGeneralSkill(skill);
				switch (skill.getSkillType())
				{
					case BUFF:
						addBuffSkill(skill);
						break;
					case DEBUFF:
						addDebuffSkill(skill);
						addCOTSkill(skill);
						addRangeSkill(skill);
						break;
					case ROOT:
						addRootSkill(skill);
						addImmobiliseSkill(skill);
						addRangeSkill(skill);
						break;
					case SLEEP:
						addSleepSkill(skill);
						addImmobiliseSkill(skill);
						break;
					case STUN:
						addRootSkill(skill);
						addImmobiliseSkill(skill);
						addRangeSkill(skill);
						break;
					case PARALYZE:
						addParalyzeSkill(skill);
						addImmobiliseSkill(skill);
						addRangeSkill(skill);
						break;
					case PDAM:
					case MDAM:
					case BLOW:
					case DRAIN:
					case CHARGEDAM:
						addAtkSkill(skill);
						addUniversalSkill(skill);
						addRangeSkill(skill);
						break;
					case POISON:
					case DOT:
					case MDOT:
					case BLEED:
						addDOTSkill(skill);
						addRangeSkill(skill);
						break;
					case MUTE:
					case FEAR:
						addCOTSkill(skill);
						addRangeSkill(skill);
						break;
					default:
						if (skill.hasEffectType(L2EffectType.CANCEL, L2EffectType.DISPEL, L2EffectType.DISPEL_BY_SLOT))
						{
							addNegativeSkill(skill);
							addRangeSkill(skill);
						}
						else if (skill.hasEffectType(L2EffectType.HEAL, L2EffectType.HEAL_PERCENT))
						{
							addHealSkill(skill);
						}
						else if (skill.hasEffectType(L2EffectType.RESURRECTION))
						{
							addResSkill(skill);
						}
						else if (skill.hasEffectType(L2EffectType.MAGICAL_ATTACK_MP, L2EffectType.PHYSICAL_ATTACK, L2EffectType.PHYSICAL_ATTACK_HP_LINK, L2EffectType.DEATH_LINK))
						{
							addAtkSkill(skill);
							addUniversalSkill(skill);
							addRangeSkill(skill);
						}
						else
						{
							addUniversalSkill(skill);
						}
						break;
				}
			}
		}
		
		_skills.put(skill.getId(), skill);
	}
	
	public void addSleepSkill(L2Skill skill)
	{
		_sleepSkills.add(skill);
	}
	
	public void addStunSkill(L2Skill skill)
	{
		_stunskills.add(skill);
	}
	
	public void addSuicideSkill(L2Skill skill)
	{
		_suicideSkills.add(skill);
	}
	
	public void addTeachInfo(List<ClassId> teachInfo)
	{
		_teachInfo.addAll(teachInfo);
	}
	
	public void addUniversalSkill(L2Skill skill)
	{
		_universalSkills.add(skill);
	}
	
	public boolean canTeach(ClassId classId)
	{
		// If the player is on a third class, fetch the class teacher
		// information for its parent class.
		if (classId.level() == 3)
		{
			return _teachInfo.contains(classId.getParent());
		}
		return _teachInfo.contains(classId);
	}
	
	/**
	 * Empty all possible drops of this L2NpcTemplate.
	 */
	public synchronized void clearAllDropData()
	{
		for (L2DropCategory cat : _categories)
		{
			cat.clearAllDrops();
		}
		_categories.clear();
	}
	
	public L2NpcAIData getAIDataStatic()
	{
		return _AIdataStatic;
	}
	
	/**
	 * @return the list of all possible item drops of this L2NpcTemplate.<br>
	 *         (ie full drops and part drops, mats, miscellaneous & UNCATEGORIZED)
	 */
	public List<L2DropData> getAllDropData()
	{
		final List<L2DropData> list = new ArrayList<>();
		for (L2DropCategory tmp : _categories)
		{
			list.addAll(tmp.getAllDrops());
		}
		return list;
	}
	
	/**
	 * @return the attack skills.
	 */
	public List<L2Skill> getAtkSkills()
	{
		return _atkSkills;
	}
	
	/**
	 * @return the base vitality divider value.
	 */
	public float getBaseVitalityDivider()
	{
		return _baseVitalityDivider;
	}
	
	/**
	 * @return the buff skills.
	 */
	public List<L2Skill> getBuffSkills()
	{
		return _buffSkills;
	}
	
	/**
	 * @return the client class (same as texture path).
	 */
	public String getClientClass()
	{
		return _clientClass;
	}
	
	/**
	 * @return the cost over time skills.
	 */
	public List<L2Skill> getCostOverTimeSkills()
	{
		return _cotSkills;
	}
	
	/**
	 * @return the debuff skills.
	 */
	public List<L2Skill> getDebuffSkills()
	{
		return _debuffSkills;
	}
	
	/**
	 * @return the list of all possible UNCATEGORIZED drops of this L2NpcTemplate.
	 */
	public List<L2DropCategory> getDropData()
	{
		return _categories;
	}
	
	/**
	 * @return the drop herb group.
	 */
	public int getDropHerbGroup()
	{
		return _dropHerbGroup;
	}
	
	public int getWeaponEnchant()
	{
		return _weaponEnchant;
	}
	
	/**
	 * @return the general skills.
	 */
	public List<L2Skill> getGeneralskills()
	{
		return _generalSkills;
	}
	
	/**
	 * @return the heal skills.
	 */
	public List<L2Skill> getHealSkills()
	{
		return _healSkills;
	}
	
	/**
	 * @return the immobilize skills.
	 */
	public List<L2Skill> getImmobiliseSkills()
	{
		return _immobilizeSkills;
	}
	
	/**
	 * @return the NPC level.
	 */
	public byte getLevel()
	{
		return _level;
	}
	
	public void setLevel(byte level)
	{
		_level = level;
	}
	
	/**
	 * @return the long range skills.
	 */
	public List<L2Skill> getLongRangeSkills()
	{
		return _longRangeSkills;
	}
	
	/**
	 * @return the list of all Minions that must be spawn with the L2NpcInstance using this L2NpcTemplate.
	 */
	public List<L2MinionData> getMinionData()
	{
		return _minions;
	}
	
	/**
	 * @return the NPC name.
	 */
	public String getName()
	{
		return _name;
	}
	
	/**
	 * @return the negative skills.
	 */
	public List<L2Skill> getNegativeSkills()
	{
		return _negativeSkills;
	}
	
	/**
	 * Gets the NPC ID.
	 * @return the NPC ID
	 */
	@Override
	public int getId()
	{
		return _id;
	}
	
	public int getDisplayId()
	{
		return _displayId;
	}
	
	/**
	 * @return the NPC race.
	 */
	public Race getRace()
	{
		if (_race == null)
		{
			_race = Race.NONE;
		}
		return _race;
	}
	
	/**
	 * @return the resurrection skills.
	 */
	public List<L2Skill> getResSkills()
	{
		return _resSkills;
	}
	
	/**
	 * @return the reward Exp.
	 */
	public int getRewardExp()
	{
		return _rewardExp;
	}
	
	/**
	 * @return the reward SP.
	 */
	public int getRewardSp()
	{
		return _rewardSp;
	}
	
	/**
	 * @return the NPC sex.
	 */
	public String getSex()
	{
		return _sex;
	}
	
	public int getChestId()
	{
		return _chestId;
	}
	
	public int getRHandId()
	{
		return _rhandId;
	}
	
	public int getLHandId()
	{
		return _lhandId;
	}
	
	/**
	 * @return the short range skills.
	 */
	public List<L2Skill> getShortRangeSkills()
	{
		return _shortRangeSkills;
	}
	
	@Override
	public Map<Integer, L2Skill> getSkills()
	{
		return _skills;
	}
	
	public List<L2Skill> getSuicideSkills()
	{
		return _suicideSkills;
	}
	
	public List<ClassId> getTeachInfo()
	{
		return _teachInfo;
	}
	
	/**
	 * @return the NPC title.
	 */
	public String getTitle()
	{
		return _title;
	}
	
	/**
	 * @return the NPC type.
	 */
	public String getType()
	{
		return _type;
	}
	
	/**
	 * @return the universal skills.
	 */
	public List<L2Skill> getUniversalSkills()
	{
		return _universalSkills;
	}
	
	/**
	 * @return {@code true} if the NPC is custom, {@code false} otherwise.
	 */
	public boolean isCustom()
	{
		return _isCustom;
	}
	
	/**
	 * @return {@code true} if the NPC is a quest monster, {@code false} otherwise.
	 */
	public boolean isQuestMonster()
	{
		return _isQuestMonster;
	}
	
	/**
	 * @return {@code true} if the NPC uses server side name, {@code false} otherwise.
	 */
	public boolean isUsingServerSideName()
	{
		return _serverSideName;
	}
	
	/**
	 * @return {@code true} if the NPC uses server side title, {@code false} otherwise.
	 */
	public boolean isUsingServerSideTitle()
	{
		return _serverSideTitle;
	}
	
	/**
	 * Checks types, ignore case.
	 * @param t the type to check.
	 * @return {@code true} if the type are the same, {@code false} otherwise.
	 */
	public boolean isType(String t)
	{
		return _type.equalsIgnoreCase(t);
	}
	
	/**
	 * @return {@code true} if the NPC is an undead, {@code false} otherwise.
	 */
	public boolean isUndead()
	{
		return _race == Race.UNDEAD;
	}
	
	public void setAIData(L2NpcAIData AIData)
	{
		_AIdataStatic = AIData;
	}
	
	public void setRace(int raceId)
	{
		switch (raceId)
		{
			case 1:
				_race = Race.UNDEAD;
				break;
			case 2:
				_race = Race.MAGICCREATURE;
				break;
			case 3:
				_race = Race.BEAST;
				break;
			case 4:
				_race = Race.ANIMAL;
				break;
			case 5:
				_race = Race.PLANT;
				break;
			case 6:
				_race = Race.HUMANOID;
				break;
			case 7:
				_race = Race.SPIRIT;
				break;
			case 8:
				_race = Race.ANGEL;
				break;
			case 9:
				_race = Race.DEMON;
				break;
			case 10:
				_race = Race.DRAGON;
				break;
			case 11:
				_race = Race.GIANT;
				break;
			case 12:
				_race = Race.BUG;
				break;
			case 13:
				_race = Race.FAIRIE;
				break;
			case 14:
				_race = Race.HUMAN;
				break;
			case 15:
				_race = Race.ELF;
				break;
			case 16:
				_race = Race.DARK_ELF;
				break;
			case 17:
				_race = Race.ORC;
				break;
			case 18:
				_race = Race.DWARF;
				break;
			case 19:
				_race = Race.OTHER;
				break;
			case 20:
				_race = Race.NONLIVING;
				break;
			case 21:
				_race = Race.SIEGEWEAPON;
				break;
			case 22:
				_race = Race.DEFENDINGARMY;
				break;
			case 23:
				_race = Race.MERCENARIE;
				break;
			case 24:
				_race = Race.UNKNOWN;
				break;
			case 25:
				_race = Race.KAMAEL;
				break;
			default:
				_race = Race.NONE;
				break;
		}
	}
	
	public boolean hasParameters()
	{
		return _parameters != null;
	}
	
	public StatsSet getParameters()
	{
		return _parameters;
	}
	
	public void setParameters(StatsSet set)
	{
		_parameters = set;
	}
	
	/**
	 * @param aiSkillScope
	 * @return
	 */
	public List<L2Skill> getAISkills(AISkillScope aiSkillScope)
	{
		switch (aiSkillScope)
		{
			case ATTACK:
				return _atkSkills;
			case BUFF:
				return _buffSkills;
			case COT:
				return _cotSkills;
			case DEBUFF:
				return _debuffSkills;
			case GENERAL:
				return _generalSkills;
			case HEAL:
				return _healSkills;
			case IMMOBILIZE:
				return _immobilizeSkills;
			case LONG_RANGE:
				return _longRangeSkills;
			case NEGATIVE:
				return _negativeSkills;
			case RES:
				return _resSkills;
			case SHORT_RANGE:
				return _shortRangeSkills;
			case SUICIDE:
				return _suicideSkills;
			case UNIVERSAL:
				return _universalSkills;
			default:
				return null;
		}
	}
}
