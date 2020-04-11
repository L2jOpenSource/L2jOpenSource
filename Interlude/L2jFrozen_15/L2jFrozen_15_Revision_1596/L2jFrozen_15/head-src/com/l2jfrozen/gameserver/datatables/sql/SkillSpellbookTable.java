package com.l2jfrozen.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author l2jserver
 */
public class SkillSpellbookTable
{
	private final static Logger LOGGER = Logger.getLogger(SkillTreeTable.class);
	private static final String SELECT_SKILL_SPELLBOOKS = "SELECT skill_id, item_id FROM skill_spellbooks";
	
	private static SkillSpellbookTable instance;
	
	private static Map<Integer, Integer> skillSpellbooks;
	
	public static SkillSpellbookTable getInstance()
	{
		if (instance == null)
		{
			instance = new SkillSpellbookTable();
		}
		
		return instance;
	}
	
	private SkillSpellbookTable()
	{
		skillSpellbooks = new HashMap<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_SKILL_SPELLBOOKS);
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				skillSpellbooks.put(rset.getInt("skill_id"), rset.getInt("item_id"));
			}
			
			LOGGER.info("SkillSpellbookTable: Loaded " + skillSpellbooks.size() + " spellbooks");
		}
		catch (Exception e)
		{
			LOGGER.error("SkillSpellbookTable.SkillSpellbookTable : Error while loading spellbook data", e);
		}
	}
	
	public int getBookForSkill(final int skillId, final int level)
	{
		if (skillId == L2Skill.SKILL_DIVINE_INSPIRATION && level != -1)
		{
			switch (level)
			{
				case 1:
					return 8618; // Ancient Book - Divine Inspiration (Modern Language Version)
				case 2:
					return 8619; // Ancient Book - Divine Inspiration (Original Language Version)
				case 3:
					return 8620; // Ancient Book - Divine Inspiration (Manuscript)
				case 4:
					return 8621; // Ancient Book - Divine Inspiration (Original Version)
				default:
					return -1;
			}
		}
		
		if (!skillSpellbooks.containsKey(skillId))
		{
			return -1;
		}
		
		return skillSpellbooks.get(skillId);
	}
	
	public int getBookForSkill(final L2Skill skill)
	{
		return getBookForSkill(skill.getId(), -1);
	}
	
	public int getBookForSkill(final L2Skill skill, final int level)
	{
		return getBookForSkill(skill.getId(), level);
	}
}
