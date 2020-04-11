package com.l2jfrozen.gameserver.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.ai.special.manager.AIExtend;
import com.l2jfrozen.gameserver.model.L2DropCategory;
import com.l2jfrozen.gameserver.model.L2DropData;
import com.l2jfrozen.gameserver.model.L2MinionData;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.base.ClassId;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.skills.Stats;

/**
 * This cl contains all generic data of a L2Spawn object.<BR>
 * <BR>
 * <B><U> Data</U> :</B><BR>
 * <BR>
 * <li>npcId, type, name, sex</li>
 * <li>rewardExp, rewardSp</li>
 * <li>aggroRange, factionId, factionRange</li>
 * <li>rhand, lhand, armor</li>
 * <li>isUndead</li>
 * <li>_drops</li>
 * <li>_minions</li>
 * <li>_teachInfo</li>
 * <li>_skills</li>
 * <li>_questsStart</li><BR>
 * <BR>
 * @version $Revision: 1.1.2.4 $ $Date: 2005/04/02 15:57:51 $
 */
public final class L2NpcTemplate extends L2CharTemplate
{
	protected static final Logger LOGGER = Logger.getLogger(Quest.class);
	
	public final int npcId;
	public final int idTemplate;
	public final String type;
	public final String name;
	public final boolean serverSideName;
	public final String title;
	public final boolean serverSideTitle;
	public final String sex;
	public final byte level;
	public final int rewardExp;
	public final int rewardSp;
	public final int aggroRange;
	public final int rhand;
	public final int lhand;
	public final int armor;
	public final String factionId;
	public final int factionRange;
	public final int absorbLevel;
	public final AbsorbCrystalType absorbType;
	public Race race;
	
	private final boolean custom;
	
	public static enum AbsorbCrystalType
	{
		LAST_HIT,
		FULL_PARTY,
		PARTY_ONE_RANDOM
	}
	
	public static enum Race
	{
		UNDEAD,
		MAGICCREATURE,
		BEAST,
		ANIMAL,
		PLANT,
		HUMANOID,
		SPIRIT,
		ANGEL,
		DEMON,
		DRAGON,
		GIANT,
		BUG,
		FAIRIE,
		HUMAN,
		ELVE,
		DARKELVE,
		ORC,
		DWARVE,
		OTHER,
		NONLIVING,
		SIEGEWEAPON,
		DEFENDINGARMY,
		MERCENARIE,
		UNKNOWN
	}
	
	private final StatsSet npcStatsSet;
	
	/** The table containing all Item that can be dropped by L2NpcInstance using this L2NpcTemplate */
	private final List<L2DropCategory> categories = new ArrayList<>();
	
	/** The table containing all Minions that must be spawn with the L2NpcInstance using this L2NpcTemplate */
	private final List<L2MinionData> minions = new ArrayList<>();
	
	private final List<ClassId> teachInfo = new ArrayList<>();
	private final Map<Integer, L2Skill> skills = new HashMap<>();
	private final Map<Stats, Double> vulnerabilities = new HashMap<>();
	// contains a list of quests for each event type (questStart, questAttack, questKill, etc)
	private final Map<Quest.QuestEventType, Quest[]> questEvents = new HashMap<>();
	private static Map<AIExtend.Action, AIExtend[]> aiEvents = new HashMap<>();
	
	/**
	 * Constructor of L2Character.<BR>
	 * <BR>
	 * @param set    The StatsSet object to transfer data to the method
	 * @param custom
	 */
	public L2NpcTemplate(final StatsSet set, final boolean custom)
	{
		super(set);
		npcId = set.getInteger("npcId");
		idTemplate = set.getInteger("idTemplate");
		type = set.getString("type");
		name = set.getString("name");
		serverSideName = set.getBool("serverSideName");
		title = set.getString("title");
		serverSideTitle = set.getBool("serverSideTitle");
		sex = set.getString("sex");
		level = set.getByte("level");
		rewardExp = set.getInteger("rewardExp");
		rewardSp = set.getInteger("rewardSp");
		aggroRange = set.getInteger("aggroRange");
		rhand = set.getInteger("rhand");
		lhand = set.getInteger("lhand");
		armor = set.getInteger("armor");
		final String f = set.getString("factionId", null);
		if (f == null)
		{
			factionId = null;
		}
		else
		{
			factionId = f.intern();
		}
		factionRange = set.getInteger("factionRange", 0);
		absorbLevel = set.getInteger("absorb_level", 0);
		absorbType = AbsorbCrystalType.valueOf(set.getString("absorb_type"));
		npcStatsSet = set;
		this.custom = custom;
	}
	
	public void addTeachInfo(final ClassId classId)
	{
		teachInfo.add(classId);
	}
	
	public ClassId[] getTeachInfo()
	{
		return teachInfo.toArray(new ClassId[teachInfo.size()]);
	}
	
	public boolean canTeach(final ClassId classId)
	{
		// If the player is on a third class, fetch the class teacher
		// information for its parent class.
		if (classId.getId() >= 88)
		{
			return teachInfo.contains(classId.getParent());
		}
		
		return teachInfo.contains(classId);
	}
	
	// add a drop to a given category. If the category does not exist, create it.
	public void addDropData(final L2DropData drop, final int categoryType)
	{
		if (drop.isQuestDrop())
		{
			// if (questDrops == null)
			// questDrops = new ArrayList<L2DropData>();
			// questDrops.add(drop);
		}
		else
		{
			// if the category doesn't already exist, create it first
			// synchronized (_categories)
			// {
			boolean catExists = false;
			for (final L2DropCategory cat : categories)
			{
				// if the category exists, add the drop to this category.
				if (cat.getCategoryType() == categoryType)
				{
					cat.addDropData(drop, type.equalsIgnoreCase("L2RaidBoss") || type.equalsIgnoreCase("L2GrandBoss"));
					catExists = true;
					break;
				}
			}
			// if the category doesn't exit, create it and add the drop
			if (!catExists)
			{
				final L2DropCategory cat = new L2DropCategory(categoryType);
				cat.addDropData(drop, type.equalsIgnoreCase("L2RaidBoss") || type.equalsIgnoreCase("L2GrandBoss"));
				categories.add(cat);
			}
			// }
		}
	}
	
	public void addRaidData(final L2MinionData minion)
	{
		minions.add(minion);
	}
	
	public void addSkill(final L2Skill skill)
	{
		skills.put(skill.getId(), skill);
	}
	
	public void addVulnerability(final Stats id, final double vuln)
	{
		vulnerabilities.put(id, vuln);
	}
	
	public double getVulnerability(final Stats id)
	{
		if (vulnerabilities.get(id) == null)
		{
			return 1;
		}
		
		return vulnerabilities.get(id);
	}
	
	public double removeVulnerability(final Stats id)
	{
		return vulnerabilities.remove(id);
	}
	
	/**
	 * Return the list of all possible UNCATEGORIZED drops of this L2NpcTemplate.<BR>
	 * <BR>
	 * @return
	 */
	public List<L2DropCategory> getDropData()
	{
		return categories;
	}
	
	/**
	 * Return the list of all possible item drops of this L2NpcTemplate.<BR>
	 * (ie full drops and part drops, mats, miscellaneous & UNCATEGORIZED)<BR>
	 * <BR>
	 * @return
	 */
	public List<L2DropData> getAllDropData()
	{
		final List<L2DropData> lst = new ArrayList<>();
		
		for (L2DropCategory tmp : categories)
		{
			lst.addAll(tmp.getAllDrops());
		}
		
		return lst;
	}
	
	/**
	 * Empty all possible drops of this L2NpcTemplate.<BR>
	 * <BR>
	 */
	public synchronized void clearAllDropData()
	{
		categories.forEach(dropCategory -> dropCategory.clearAllDrops());
		categories.clear();
	}
	
	/**
	 * Return the list of all Minions that must be spawn with the L2NpcInstance using this L2NpcTemplate.<BR>
	 * <BR>
	 * @return
	 */
	public List<L2MinionData> getMinionData()
	{
		return minions;
	}
	
	public Map<Integer, L2Skill> getSkills()
	{
		return skills;
	}
	
	public void addQuestEvent(final Quest.QuestEventType EventType, final Quest q)
	{
		if (questEvents.get(EventType) == null)
		{
			questEvents.put(EventType, new Quest[]
			{
				q
			});
		}
		else
		{
			final Quest[] quests = questEvents.get(EventType);
			final int len = quests.length;
			
			// if only one registration per npc is allowed for this event type
			// then only register this NPC if not already registered for the specified event.
			// if a quest allows multiple registrations, then register regardless of count
			// In all cases, check if this new registration is replacing an older copy of the SAME quest
			if (!EventType.isMultipleRegistrationAllowed())
			{
				if (quests[0].getName().equals(q.getName()))
				{
					quests[0] = q;
				}
				else
				{
					LOGGER.warn("Quest event not allowed in multiple quests.  Skipped addition of Event Type \"" + EventType + "\" for NPC \"" + name + "\" and quest \"" + q.getName() + "\".");
				}
			}
			else
			{
				// be ready to add a new quest to a new copy of the list, with larger size than previously.
				final Quest[] tmp = new Quest[len + 1];
				// loop through the existing quests and copy them to the new list. While doing so, also
				// check if this new quest happens to be just a replacement for a previously loaded quest.
				// If so, just save the updated reference and do NOT use the new list. Else, add the new
				// quest to the end of the new list
				for (int i = 0; i < len; i++)
				{
					if (quests[i].getName().equals(q.getName()))
					{
						quests[i] = q;
						return;
					}
					tmp[i] = quests[i];
				}
				tmp[len] = q;
				questEvents.put(EventType, tmp);
			}
		}
	}
	
	public Quest[] getEventQuests(final Quest.QuestEventType EventType)
	{
		if (questEvents.get(EventType) == null)
		{
			return new Quest[0];
		}
		
		return questEvents.get(EventType);
	}
	
	// TODO
	public void addAIEvent(final AIExtend.Action actionType, final AIExtend ai)
	{
		if (aiEvents.get(actionType) == null)
		{
			aiEvents.put(actionType, new AIExtend[]
			{
				ai
			});
		}
		else
		{
			final AIExtend[] arrayOfAI = aiEvents.get(actionType);
			final int len = arrayOfAI.length;
			
			// if only one registration per npc is allowed for this event type
			// then only register this NPC if not already registered for the specified event.
			// if a quest allows multiple registrations, then register regardless of count
			// In all cases, check if this new registration is replacing an older copy of the SAME quest
			if (!actionType.isRegistred())
			{
				if (arrayOfAI[0].getID() == ai.getID())
				{
					arrayOfAI[0] = ai;
				}
				else
				{
					LOGGER.warn("Skipped AI: \"" + ai.getID() + "\".");
				}
			}
			else
			{
				// be ready to add a new quest to a new copy of the list, with larger size than previously.
				final AIExtend[] tmp = new AIExtend[len + 1];
				// loop through the existing quests and copy them to the new list. While doing so, also
				// check if this new quest happens to be just a replacement for a previously loaded quest.
				// If so, just save the updated reference and do NOT use the new list. Else, add the new
				// quest to the end of the new list
				for (int i = 0; i < len; i++)
				{
					if (arrayOfAI[i].getID() == ai.getID())
					{
						arrayOfAI[i] = ai;
						return;
					}
					tmp[i] = arrayOfAI[i];
				}
				
				tmp[len] = ai;
				aiEvents.put(actionType, tmp);
			}
		}
	}
	
	public static void clearAI()
	{
		aiEvents.clear();
	}
	
	public StatsSet getStatsSet()
	{
		return npcStatsSet;
	}
	
	public void setRace(final int raceId)
	{
		switch (raceId)
		{
			case 1:
				race = L2NpcTemplate.Race.UNDEAD;
				break;
			case 2:
				race = L2NpcTemplate.Race.MAGICCREATURE;
				break;
			case 3:
				race = L2NpcTemplate.Race.BEAST;
				break;
			case 4:
				race = L2NpcTemplate.Race.ANIMAL;
				break;
			case 5:
				race = L2NpcTemplate.Race.PLANT;
				break;
			case 6:
				race = L2NpcTemplate.Race.HUMANOID;
				break;
			case 7:
				race = L2NpcTemplate.Race.SPIRIT;
				break;
			case 8:
				race = L2NpcTemplate.Race.ANGEL;
				break;
			case 9:
				race = L2NpcTemplate.Race.DEMON;
				break;
			case 10:
				race = L2NpcTemplate.Race.DRAGON;
				break;
			case 11:
				race = L2NpcTemplate.Race.GIANT;
				break;
			case 12:
				race = L2NpcTemplate.Race.BUG;
				break;
			case 13:
				race = L2NpcTemplate.Race.FAIRIE;
				break;
			case 14:
				race = L2NpcTemplate.Race.HUMAN;
				break;
			case 15:
				race = L2NpcTemplate.Race.ELVE;
				break;
			case 16:
				race = L2NpcTemplate.Race.DARKELVE;
				break;
			case 17:
				race = L2NpcTemplate.Race.ORC;
				break;
			case 18:
				race = L2NpcTemplate.Race.DWARVE;
				break;
			case 19:
				race = L2NpcTemplate.Race.OTHER;
				break;
			case 20:
				race = L2NpcTemplate.Race.NONLIVING;
				break;
			case 21:
				race = L2NpcTemplate.Race.SIEGEWEAPON;
				break;
			case 22:
				race = L2NpcTemplate.Race.DEFENDINGARMY;
				break;
			case 23:
				race = L2NpcTemplate.Race.MERCENARIE;
				break;
			default:
				race = L2NpcTemplate.Race.UNKNOWN;
				break;
		}
	}
	
	public L2NpcTemplate.Race getRace()
	{
		if (race == null)
		{
			race = L2NpcTemplate.Race.UNKNOWN;
		}
		
		return race;
	}
	
	/**
	 * @return the level
	 */
	public byte getLevel()
	{
		return level;
	}
	
	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * @return the npcId
	 */
	public int getNpcId()
	{
		return npcId;
	}
	
	public final boolean isCustom()
	{
		return custom;
	}
	
	public String getType()
	{
		return type;
	}
}
