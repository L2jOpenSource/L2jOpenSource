package com.l2jfrozen.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.model.base.ClassId;
import com.l2jfrozen.gameserver.templates.L2PcTemplate;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class CharTemplateTable
{
	private static Logger LOGGER = Logger.getLogger(CharTemplateTable.class);
	private static final String SELECT_CHARRACTER_TEMPLATE = "SELECT * FROM class_list, char_templates, lvlupgain" + " WHERE class_list.id = char_templates.classId" + " AND class_list.id = lvlupgain.classId" + " ORDER BY class_list.id";
	
	private static CharTemplateTable instance;
	
	private static final String[] CHAR_CLASSES =
	{
		"Human Fighter",
		"Warrior",
		"Gladiator",
		"Warlord",
		"Human Knight",
		"Paladin",
		"Dark Avenger",
		"Rogue",
		"Treasure Hunter",
		"Hawkeye",
		"Human Mystic",
		"Human Wizard",
		"Sorceror",
		"Necromancer",
		"Warlock",
		"Cleric",
		"Bishop",
		"Prophet",
		"Elven Fighter",
		"Elven Knight",
		"Temple Knight",
		"Swordsinger",
		"Elven Scout",
		"Plainswalker",
		"Silver Ranger",
		"Elven Mystic",
		"Elven Wizard",
		"Spellsinger",
		"Elemental Summoner",
		"Elven Oracle",
		"Elven Elder",
		"Dark Fighter",
		"Palus Knight",
		"Shillien Knight",
		"Bladedancer",
		"Assassin",
		"Abyss Walker",
		"Phantom Ranger",
		"Dark Elven Mystic",
		"Dark Elven Wizard",
		"Spellhowler",
		"Phantom Summoner",
		"Shillien Oracle",
		"Shillien Elder",
		"Orc Fighter",
		"Orc Raider",
		"Destroyer",
		"Orc Monk",
		"Tyrant",
		"Orc Mystic",
		"Orc Shaman",
		"Overlord",
		"Warcryer",
		"Dwarven Fighter",
		"Dwarven Scavenger",
		"Bounty Hunter",
		"Dwarven Artisan",
		"Warsmith",
		"dummyEntry1",
		"dummyEntry2",
		"dummyEntry3",
		"dummyEntry4",
		"dummyEntry5",
		"dummyEntry6",
		"dummyEntry7",
		"dummyEntry8",
		"dummyEntry9",
		"dummyEntry10",
		"dummyEntry11",
		"dummyEntry12",
		"dummyEntry13",
		"dummyEntry14",
		"dummyEntry15",
		"dummyEntry16",
		"dummyEntry17",
		"dummyEntry18",
		"dummyEntry19",
		"dummyEntry20",
		"dummyEntry21",
		"dummyEntry22",
		"dummyEntry23",
		"dummyEntry24",
		"dummyEntry25",
		"dummyEntry26",
		"dummyEntry27",
		"dummyEntry28",
		"dummyEntry29",
		"dummyEntry30",
		"Duelist",
		"DreadNought",
		"Phoenix Knight",
		"Hell Knight",
		"Sagittarius",
		"Adventurer",
		"Archmage",
		"Soultaker",
		"Arcana Lord",
		"Cardinal",
		"Hierophant",
		"Eva Templar",
		"Sword Muse",
		"Wind Rider",
		"Moonlight Sentinel",
		"Mystic Muse",
		"Elemental Master",
		"Eva's Saint",
		"Shillien Templar",
		"Spectral Dancer",
		"Ghost Hunter",
		"Ghost Sentinel",
		"Storm Screamer",
		"Spectral Master",
		"Shillien Saint",
		"Titan",
		"Grand Khauatari",
		"Dominator",
		"Doomcryer",
		"Fortune Seeker",
		"Maestro"
	};
	
	private final Map<Integer, L2PcTemplate> templates;
	
	public static CharTemplateTable getInstance()
	{
		if (instance == null)
		{
			instance = new CharTemplateTable();
		}
		return instance;
	}
	
	private CharTemplateTable()
	{
		templates = new HashMap<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CHARRACTER_TEMPLATE);
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				StatsSet set = new StatsSet();
				set.set("classId", rset.getInt("id"));
				set.set("className", rset.getString("className"));
				set.set("raceId", rset.getInt("raceId"));
				set.set("baseSTR", rset.getInt("STR"));
				set.set("baseCON", rset.getInt("CON"));
				set.set("baseDEX", rset.getInt("DEX"));
				set.set("baseINT", rset.getInt("_INT"));
				set.set("baseWIT", rset.getInt("WIT"));
				set.set("baseMEN", rset.getInt("MEN"));
				set.set("baseHpMax", rset.getFloat("defaultHpBase"));
				set.set("lvlHpAdd", rset.getFloat("defaultHpAdd"));
				set.set("lvlHpMod", rset.getFloat("defaultHpMod"));
				set.set("baseMpMax", rset.getFloat("defaultMpBase"));
				set.set("baseCpMax", rset.getFloat("defaultCpBase"));
				set.set("lvlCpAdd", rset.getFloat("defaultCpAdd"));
				set.set("lvlCpMod", rset.getFloat("defaultCpMod"));
				set.set("lvlMpAdd", rset.getFloat("defaultMpAdd"));
				set.set("lvlMpMod", rset.getFloat("defaultMpMod"));
				set.set("baseHpReg", 1.5);
				set.set("baseMpReg", 0.9);
				set.set("basePAtk", rset.getInt("p_atk"));
				set.set("basePDef", /* classId.isMage()? 77 : 129 */rset.getInt("p_def"));
				set.set("baseMAtk", rset.getInt("m_atk"));
				set.set("baseMDef", rset.getInt("char_templates.m_def"));
				set.set("classBaseLevel", rset.getInt("class_lvl"));
				set.set("basePAtkSpd", rset.getInt("p_spd"));
				set.set("baseMAtkSpd", /* classId.isMage()? 166 : 333 */rset.getInt("char_templates.m_spd"));
				set.set("baseCritRate", rset.getInt("char_templates.critical") / 10);
				set.set("baseRunSpd", rset.getInt("move_spd"));
				set.set("baseWalkSpd", 0);
				set.set("baseShldDef", 0);
				set.set("baseShldRate", 0);
				set.set("baseAtkRange", 40);
				
				set.set("spawnX", rset.getInt("x"));
				set.set("spawnY", rset.getInt("y"));
				set.set("spawnZ", rset.getInt("z"));
				
				L2PcTemplate ct;
				
				set.set("collision_radius", rset.getDouble("m_col_r"));
				set.set("collision_height", rset.getDouble("m_col_h"));
				ct = new L2PcTemplate(set);
				// 5items must go here
				for (int x = 1; x < 6; x++)
				{
					if (rset.getInt("items" + x) != 0)
					{
						ct.addItem(rset.getInt("items" + x));
					}
				}
				templates.put(ct.classId.getId(), ct);
				
				set = null;
				ct = null;
			}
		}
		catch (SQLException e)
		{
			LOGGER.error("CharTemplateTable.CharTemplateTable : Error while loading char templates", e);
		}
		
		LOGGER.info("CharTemplateTable: Loaded " + templates.size() + " Character Templates.");
	}
	
	public L2PcTemplate getTemplate(final ClassId classId)
	{
		return getTemplate(classId.getId());
	}
	
	public L2PcTemplate getTemplate(final int classId)
	{
		final int key = classId;
		
		return templates.get(key);
	}
	
	public static final String getClassNameById(final int classId)
	{
		return CHAR_CLASSES[classId];
	}
	
	public static final int getClassIdByName(final String className)
	{
		int currId = 1;
		
		for (final String name : CHAR_CLASSES)
		{
			if (name.equalsIgnoreCase(className))
			{
				break;
			}
			
			currId++;
		}
		
		return currId;
	}
	
}
