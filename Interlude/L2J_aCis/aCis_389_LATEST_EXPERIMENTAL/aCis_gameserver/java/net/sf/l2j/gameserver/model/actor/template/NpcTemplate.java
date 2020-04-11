package net.sf.l2j.gameserver.model.actor.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.l2j.commons.util.ArraysUtil;
import net.sf.l2j.commons.util.StatsSet;

import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.manager.ClanHallManager;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.enums.actors.NpcAiType;
import net.sf.l2j.gameserver.enums.actors.NpcRace;
import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.MinionData;
import net.sf.l2j.gameserver.model.clanhall.ClanHall;
import net.sf.l2j.gameserver.model.clanhall.SiegableHall;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.item.DropCategory;
import net.sf.l2j.gameserver.model.item.DropData;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.skills.L2Skill;

public class NpcTemplate extends CreatureTemplate
{
	private final int _npcId;
	private final int _idTemplate;
	private final String _type;
	private final String _name;
	private boolean _usingServerSideName;
	private final String _title;
	private boolean _usingServerSideTitle;
	private final boolean _cantBeChampionMonster;
	private final byte _level;
	private final int _exp;
	private final int _sp;
	private final int _rHand;
	private final int _lHand;
	private final int _enchantEffect;
	private final int _corpseTime;
	
	private int _dropHerbGroup;
	private NpcRace _race = NpcRace.UNKNOWN;
	private NpcAiType _aiType;
	
	private final int _ssCount;
	private final int _ssRate;
	private final int _spsCount;
	private final int _spsRate;
	private final int _aggroRange;
	
	private String[] _clans;
	private int _clanRange;
	private int[] _ignoredIds;
	
	private final boolean _canMove;
	private final boolean _isSeedable;
	
	private List<DropCategory> _categories;
	private List<MinionData> _minions;
	private List<ClassId> _teachInfo;
	
	private final Map<NpcSkillType, List<L2Skill>> _skills = new HashMap<>();
	private final Map<ScriptEventType, List<Quest>> _questEvents = new HashMap<>();
	
	private Castle _castle;
	private ClanHall _clanHall;
	private SiegableHall _siegableHall;
	
	public NpcTemplate(StatsSet set)
	{
		super(set);
		
		_npcId = set.getInteger("id");
		_idTemplate = set.getInteger("idTemplate", _npcId);
		_type = set.getString("type");
		_name = set.getString("name");
		_usingServerSideName = set.getBool("usingServerSideName", false);
		_title = set.getString("title", "");
		_usingServerSideTitle = set.getBool("usingServerSideTitle", false);
		_cantBeChampionMonster = _title.equalsIgnoreCase("Quest Monster") || isType("Chest");
		_level = set.getByte("level", (byte) 1);
		_exp = set.getInteger("exp", 0);
		_sp = set.getInteger("sp", 0);
		_rHand = set.getInteger("rHand", 0);
		_lHand = set.getInteger("lHand", 0);
		_enchantEffect = set.getInteger("enchant", 0);
		_corpseTime = set.getInteger("corpseTime", 7);
		_dropHerbGroup = set.getInteger("dropHerbGroup", 0);
		
		if (set.containsKey("raceId"))
			setRace(set.getInteger("raceId"));
		
		_aiType = set.getEnum("aiType", NpcAiType.class, NpcAiType.DEFAULT);
		
		_ssCount = set.getInteger("ssCount", 0);
		_ssRate = set.getInteger("ssRate", 0);
		_spsCount = set.getInteger("spsCount", 0);
		_spsRate = set.getInteger("spsRate", 0);
		_aggroRange = set.getInteger("aggro", 0);
		
		if (set.containsKey("clan"))
		{
			_clans = set.getStringArray("clan");
			_clanRange = set.getInteger("clanRange");
			
			if (set.containsKey("ignoredIds"))
				_ignoredIds = set.getIntegerArray("ignoredIds");
		}
		
		_canMove = set.getBool("canMove", true);
		_isSeedable = set.getBool("seedable", false);
		
		_categories = set.getList("drops");
		_minions = set.getList("minions");
		
		if (set.containsKey("teachTo"))
		{
			final int[] classIds = set.getIntegerArray("teachTo");
			
			_teachInfo = new ArrayList<>(classIds.length);
			for (int classId : classIds)
				_teachInfo.add(ClassId.VALUES[classId]);
		}
		
		addSkills(set.getList("skills"));
		
		// Set the Castle if existing.
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			if (castle.getRelatedNpcIds().contains(_npcId))
			{
				_castle = castle;
				break;
			}
		}
		
		// Set the ClanHall if existing.
		for (ClanHall ch : ClanHallManager.getInstance().getClanHalls().values())
		{
			if (ArraysUtil.contains(ch.getRelatedNpcIds(), _npcId))
			{
				if (ch instanceof SiegableHall)
					_siegableHall = (SiegableHall) ch;
				
				_clanHall = ch;
				break;
			}
		}
	}
	
	public int getNpcId()
	{
		return _npcId;
	}
	
	public int getIdTemplate()
	{
		return _idTemplate;
	}
	
	public String getType()
	{
		return _type;
	}
	
	/**
	 * @param type : the type to check.
	 * @return true if the instance type written as {@link String} is the same as this {@link NpcTemplate}, or false otherwise.
	 */
	public boolean isType(String type)
	{
		return _type.equalsIgnoreCase(type);
	}
	
	public String getName()
	{
		return _name;
	}
	
	public boolean isUsingServerSideName()
	{
		return _usingServerSideName;
	}
	
	public String getTitle()
	{
		return _title;
	}
	
	public boolean isUsingServerSideTitle()
	{
		return _usingServerSideTitle;
	}
	
	public boolean cantBeChampion()
	{
		return _cantBeChampionMonster;
	}
	
	public byte getLevel()
	{
		return _level;
	}
	
	public int getRewardExp()
	{
		return _exp;
	}
	
	public int getRewardSp()
	{
		return _sp;
	}
	
	public int getRightHand()
	{
		return _rHand;
	}
	
	public int getLeftHand()
	{
		return _lHand;
	}
	
	public int getEnchantEffect()
	{
		return _enchantEffect;
	}
	
	public int getCorpseTime()
	{
		return _corpseTime;
	}
	
	public int getDropHerbGroup()
	{
		return _dropHerbGroup;
	}
	
	public NpcRace getRace()
	{
		return _race;
	}
	
	public void setRace(int raceId)
	{
		// Race.UNKNOWN is already the default value. No needs to handle it.
		if (raceId < 1 || raceId > 23)
			return;
		
		_race = NpcRace.VALUES[raceId];
	}
	
	public NpcAiType getAiType()
	{
		return _aiType;
	}
	
	public int getSsCount()
	{
		return _ssCount;
	}
	
	public int getSsRate()
	{
		return _ssRate;
	}
	
	public int getSpsCount()
	{
		return _spsCount;
	}
	
	public int getSpsRate()
	{
		return _spsRate;
	}
	
	public int getAggroRange()
	{
		return _aggroRange;
	}
	
	public String[] getClans()
	{
		return _clans;
	}
	
	public int getClanRange()
	{
		return _clanRange;
	}
	
	public int[] getIgnoredIds()
	{
		return _ignoredIds;
	}
	
	public boolean canMove()
	{
		return _canMove;
	}
	
	public boolean isSeedable()
	{
		return _isSeedable;
	}
	
	public Castle getCastle()
	{
		return _castle;
	}
	
	public ClanHall getClanHall()
	{
		return _clanHall;
	}
	
	public SiegableHall getSiegableHall()
	{
		return _siegableHall;
	}
	
	/**
	 * @return the {@link List} of all possible UNCATEGORIZED {@link DropData}s of this {@link NpcTemplate}.
	 */
	public List<DropCategory> getDropData()
	{
		return _categories;
	}
	
	/**
	 * @return the {@link List} of all possible {@link DropData}s of this {@link NpcTemplate} linked to DROP behavior.
	 */
	public List<DropData> getAllDropData()
	{
		final List<DropData> list = new ArrayList<>();
		for (DropCategory category : _categories)
		{
			if (!category.isSweep())
				list.addAll(category.getAllDrops());
		}
		return list;
	}
	
	/**
	 * @return the {@link List} of all possible {@link DropData}s of this {@link NpcTemplate} linked to SPOIL behavior.
	 */
	public List<DropData> getAllSpoilData()
	{
		final List<DropData> list = new ArrayList<>();
		for (DropCategory category : _categories)
		{
			if (category.isSweep())
				list.addAll(category.getAllDrops());
		}
		return list;
	}
	
	/**
	 * Add a {@link DropData} to a given category. If the category does not exist, create it.
	 * @param drop : The DropData to add.
	 * @param categoryType : The category type we refer.
	 */
	public void addDropData(DropData drop, int categoryType)
	{
		final boolean isBossType = isType("RaidBoss") || isType("GrandBoss");
		
		synchronized (_categories)
		{
			// Category exists, stores the drop and return.
			for (DropCategory cat : _categories)
			{
				if (cat.getCategoryType() == categoryType)
				{
					cat.addDropData(drop, isBossType);
					return;
				}
			}
			
			// Category doesn't exist, create and store it.
			final DropCategory cat = new DropCategory(categoryType);
			cat.addDropData(drop, isBossType);
			
			_categories.add(cat);
		}
	}
	
	/**
	 * @return the {@link List} of all {@link MinionData}.
	 */
	public List<MinionData> getMinionData()
	{
		return _minions;
	}
	
	/**
	 * @param classId : The ClassId to check.
	 * @return true if _teachInfo exists and if it contains the {@link ClassId} set as parameter, or false otherwise.
	 */
	public boolean canTeach(ClassId classId)
	{
		return _teachInfo != null && _teachInfo.contains((classId.level() == 3) ? classId.getParent() : classId);
	}
	
	/**
	 * @return the {@link Map} holding the {@link List} of {@link L2Skill}s.
	 */
	public Map<NpcSkillType, List<L2Skill>> getSkills()
	{
		return _skills;
	}
	
	/**
	 * @param type : The SkillType to test.
	 * @return the {@link List} of {@link L2Skill}s based on a given {@link NpcSkillType}.
	 */
	public List<L2Skill> getSkills(NpcSkillType type)
	{
		return _skills.getOrDefault(type, Collections.emptyList());
	}
	
	/**
	 * Distribute {@link L2Skill}s amongst the different {@link NpcSkillType}s. Categories are used for easier management of AI.
	 * @param skills : The initial List of L2Skill to distribute.
	 */
	public void addSkills(List<L2Skill> skills)
	{
		for (L2Skill skill : skills)
		{
			if (skill.isPassive())
			{
				addSkill(NpcSkillType.PASSIVE, skill);
				continue;
			}
			
			if (skill.isSuicideAttack())
			{
				addSkill(NpcSkillType.SUICIDE, skill);
				continue;
			}
			
			switch (skill.getSkillType())
			{
				case GET_PLAYER:
				case INSTANT_JUMP:
					addSkill(NpcSkillType.TELEPORT, skill);
					continue;
				
				case BUFF:
				case CONT:
				case REFLECT:
					addSkill(NpcSkillType.BUFF, skill);
					continue;
				
				case HEAL:
				case HOT:
				case HEAL_PERCENT:
				case HEAL_STATIC:
				case BALANCE_LIFE:
				case MANARECHARGE:
				case MANAHEAL_PERCENT:
					addSkill(NpcSkillType.HEAL, skill);
					continue;
				
				case DEBUFF:
				case ROOT:
				case SLEEP:
				case STUN:
				case PARALYZE:
				case POISON:
				case DOT:
				case MDOT:
				case BLEED:
				case MUTE:
				case FEAR:
				case CANCEL:
				case NEGATE:
				case WEAKNESS:
				case AGGDEBUFF:
					addSkill(NpcSkillType.DEBUFF, skill);
					continue;
				
				case PDAM:
				case MDAM:
				case BLOW:
				case DRAIN:
				case CHARGEDAM:
				case FATAL:
				case DEATHLINK:
				case MANADAM:
				case CPDAMPERCENT:
				case AGGDAMAGE:
					addSkill((skill.getCastRange() > 150) ? NpcSkillType.LONG_RANGE : NpcSkillType.SHORT_RANGE, skill);
					continue;
			}
			// _log.warning(skill.getName() + " skill wasn't added due to specific logic."); TODO
		}
	}
	
	/**
	 * Submethod of {@link #addSkills(List)}. Add a {@link L2Skill} to the given {@link NpcSkillType} {@link List}.<br>
	 * <br>
	 * Create the category if it's not existing.
	 * @param type : The SkillType to test.
	 * @param skill : The L2Skill to add.
	 */
	private void addSkill(NpcSkillType type, L2Skill skill)
	{
		List<L2Skill> list = _skills.get(type);
		if (list == null)
		{
			list = new ArrayList<>(5);
			list.add(skill);
			
			_skills.put(type, list);
		}
		else
			list.add(skill);
	}
	
	/**
	 * @return the {@link Map} of {@link Quest}s {@link List} categorized by {@link ScriptEventType}.
	 */
	public Map<ScriptEventType, List<Quest>> getEventQuests()
	{
		return _questEvents;
	}
	
	/**
	 * @param type : The ScriptEventType to refer.
	 * @return the {@link List} of {@link Quest}s associated to a {@link ScriptEventType}.
	 */
	public List<Quest> getEventQuests(ScriptEventType type)
	{
		return _questEvents.get(type);
	}
	
	/**
	 * Add a {@link Quest} to the given {@link ScriptEventType} {@link List}.<br>
	 * <br>
	 * Create the category if it's not existing.
	 * @param type : The ScriptEventType to test.
	 * @param quest : The Quest to add.
	 */
	public void addQuestEvent(ScriptEventType type, Quest quest)
	{
		List<Quest> list = _questEvents.get(type);
		if (list == null)
		{
			list = new ArrayList<>(5);
			list.add(quest);
			
			_questEvents.put(type, list);
		}
		else
		{
			list.remove(quest);
			
			if (type.isMultipleRegistrationAllowed() || list.isEmpty())
				list.add(quest);
		}
	}
}