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
package l2r.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.datatables.xml.ItemData;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.datatables.xml.SkillLearnData;
import l2r.gameserver.enums.QuestEventType;
import l2r.gameserver.model.Elementals;
import l2r.gameserver.model.L2DropData;
import l2r.gameserver.model.L2MinionData;
import l2r.gameserver.model.L2NpcAIData;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.stats.BaseStats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NpcTable
{
	private static final Logger _log = LoggerFactory.getLogger(NpcTable.class);
	
	private static final Map<Integer, L2NpcTemplate> _npcs = new HashMap<>();
	
	// SQL Queries
	private static final String SELECT_NPC_ALL = "SELECT * FROM npc ORDER BY id";
	private static final String SELECT_NPC_BY_ID = "SELECT * FROM npc WHERE id = ?";
	
	private static final String SELECT_SKILLS_ALL = "SELECT * FROM npcskills ORDER BY npcid";
	private static final String SELECT_SKILLS_BY_ID = "SELECT * FROM npcskills WHERE npcid = ?";
	
	private static final String SELECT_DROPLIST_ALL = "SELECT * FROM droplist ORDER BY mobId, chance DESC";
	private static final String SELECT_DROPLIST_BY_ID = "SELECT * FROM droplist WHERE mobId = ? ORDER BY mobId, chance DESC";
	
	private static final String SELECT_NPC_AI_ALL = "SELECT * FROM npcaidata ORDER BY npcId";
	private static final String SELECT_NPC_AI_BY_ID = "SELECT * FROM npcaidata WHERE npcId = ?";
	
	private static final String SELECT_NPC_ELEMENTALS_ALL = "SELECT * FROM npc_elementals ORDER BY npc_id";
	private static final String SELECT_NPC_ELEMENTALS_BY_ID = "SELECT * FROM npc_elementals WHERE npc_id = ?";
	
	private static final String SELECT_MINION_ALL = "SELECT * FROM minions ORDER BY boss_id";
	private static final String SELECT_MINION_BY_ID = "SELECT * FROM minions WHERE boss_id = ?";
	
	// Custom SQL queries
	private static final String CUSTOM_SELECT_NPC_ALL = "SELECT * FROM custom_npc ORDER BY id";
	private static final String CUSTOM_SELECT_NPC_BY_ID = "SELECT * FROM custom_npc WHERE id = ?";
	
	private static final String CUSTOM_SELECT_SKILLS_ALL = "SELECT * FROM custom_npcskills ORDER BY npcid";
	private static final String CUSTOM_SELECT_SKILLS_BY_ID = "SELECT * FROM custom_npcskills WHERE npcid = ?";
	
	private static final String CUSTOM_SELECT_DROPLIST_ALL = "SELECT * FROM custom_droplist ORDER BY mobId, chance DESC";
	private static final String CUSTOM_SELECT_DROPLIST_BY_ID = "SELECT * FROM custom_droplist WHERE mobId = ? ORDER BY mobId, chance DESC";
	
	private static final String CUSTOM_SELECT_NPC_AI_ALL = "SELECT * FROM custom_npcaidata ORDER BY npcId";
	private static final String CUSTOM_SELECT_NPC_AI_BY_ID = "SELECT * FROM custom_npcaidata WHERE npcId = ?";
	
	private static final String CUSTOM_SELECT_NPC_ELEMENTALS_ALL = "SELECT * FROM custom_npc_elementals ORDER BY npc_id";
	private static final String CUSTOM_SELECT_NPC_ELEMENTALS_BY_ID = "SELECT * FROM custom_npc_elementals WHERE npc_id = ?";
	
	/**
	 * Instantiates a new npc table.
	 */
	protected NpcTable()
	{
		_npcs.clear();
		restoreNpcData();
	}
	
	/**
	 * Restore npc data.
	 */
	private void restoreNpcData()
	{
		loadNpcs(0);
		loadNpcsSkills(0);
		loadNpcsDrop(0);
		loadNpcsSkillLearn(0);
		loadMinions(0);
		loadNpcsAI(0);
		loadNpcsElement(0);
	}
	
	/**
	 * Fill npc table.
	 * @param NpcData the npc data
	 * @throws Exception the exception
	 */
	private void fillNpcTable(ResultSet NpcData) throws Exception
	{
		StatsSet npcDat = new StatsSet();
		int id = NpcData.getInt("id");
		int idTemp = NpcData.getInt("idTemplate");
		
		assert idTemp < 1000000;
		
		npcDat.set("npcId", id);
		npcDat.set("idTemplate", idTemp);
		int level = NpcData.getInt("level");
		npcDat.set("level", level);
		npcDat.set("client_class", NpcData.getString("class"));
		
		npcDat.set("baseShldDef", 0);
		npcDat.set("baseShldRate", 0);
		npcDat.set("baseCritRate", NpcData.getInt("critical"));
		
		npcDat.set("name", NpcData.getString("name"));
		npcDat.set("serverSideName", NpcData.getBoolean("serverSideName"));
		npcDat.set("title", NpcData.getString("title"));
		npcDat.set("serverSideTitle", NpcData.getBoolean("serverSideTitle"));
		npcDat.set("collision_radius", NpcData.getDouble("collision_radius"));
		npcDat.set("collision_height", NpcData.getDouble("collision_height"));
		npcDat.set("sex", NpcData.getString("sex"));
		npcDat.set("type", NpcData.getString("type"));
		npcDat.set("baseAtkRange", NpcData.getInt("attackrange"));
		npcDat.set("rewardExp", NpcData.getInt("exp"));
		npcDat.set("rewardSp", NpcData.getInt("sp"));
		npcDat.set("basePAtkSpd", NpcData.getInt("atkspd"));
		npcDat.set("baseMAtkSpd", NpcData.getInt("matkspd"));
		npcDat.set("rhand", NpcData.getInt("rhand"));
		npcDat.set("lhand", NpcData.getInt("lhand"));
		npcDat.set("enchant", NpcData.getInt("enchant"));
		npcDat.set("baseWalkSpd", NpcData.getInt("walkspd"));
		npcDat.set("baseRunSpd", NpcData.getInt("runspd"));
		
		// constants, until we have stats in DB
		npcDat.safeSet("baseSTR", NpcData.getInt("str"), 0, BaseStats.MAX_STAT_VALUE, "Loading npc template id: " + NpcData.getInt("idTemplate"));
		npcDat.safeSet("baseCON", NpcData.getInt("con"), 0, BaseStats.MAX_STAT_VALUE, "Loading npc template id: " + NpcData.getInt("idTemplate"));
		npcDat.safeSet("baseDEX", NpcData.getInt("dex"), 0, BaseStats.MAX_STAT_VALUE, "Loading npc template id: " + NpcData.getInt("idTemplate"));
		npcDat.safeSet("baseINT", NpcData.getInt("int"), 0, BaseStats.MAX_STAT_VALUE, "Loading npc template id: " + NpcData.getInt("idTemplate"));
		npcDat.safeSet("baseWIT", NpcData.getInt("wit"), 0, BaseStats.MAX_STAT_VALUE, "Loading npc template id: " + NpcData.getInt("idTemplate"));
		npcDat.safeSet("baseMEN", NpcData.getInt("men"), 0, BaseStats.MAX_STAT_VALUE, "Loading npc template id: " + NpcData.getInt("idTemplate"));
		
		npcDat.set("baseHpMax", NpcData.getDouble("hp"));
		npcDat.set("baseCpMax", 0);
		npcDat.set("baseMpMax", NpcData.getDouble("mp"));
		npcDat.set("baseHpReg", NpcData.getFloat("hpreg") > 0 ? NpcData.getFloat("hpreg") : 1.5 + ((level - 1) / 10.0));
		npcDat.set("baseMpReg", NpcData.getFloat("mpreg") > 0 ? NpcData.getFloat("mpreg") : 0.9 + (0.3 * ((level - 1) / 10.0)));
		npcDat.set("basePAtk", NpcData.getInt("patk"));
		npcDat.set("basePDef", NpcData.getInt("pdef"));
		npcDat.set("baseMAtk", NpcData.getInt("matk"));
		npcDat.set("baseMDef", NpcData.getInt("mdef"));
		
		npcDat.set("dropHerbGroup", NpcData.getInt("dropHerbGroup"));
		
		// Default element resists
		npcDat.set("baseFireRes", 20);
		npcDat.set("baseWindRes", 20);
		npcDat.set("baseWaterRes", 20);
		npcDat.set("baseEarthRes", 20);
		npcDat.set("baseHolyRes", 20);
		npcDat.set("baseDarkRes", 20);
		
		_npcs.put(id, new L2NpcTemplate(npcDat));
	}
	
	/**
	 * Reload npc.
	 * @param id of the NPC to reload.
	 */
	public void reloadNpc(int id)
	{
		Map<QuestEventType, List<Quest>> quests = null;
		try
		{
			// save a copy of the old data
			L2NpcTemplate old = getTemplate(id);
			
			if (old != null)
			{
				quests = old.getEventQuests();
			}
			
			loadNpcs(id);
			loadNpcsSkills(id);
			loadNpcsDrop(id);
			loadNpcsSkillLearn(id);
			loadMinions(id);
			loadNpcsAI(id);
			loadNpcsElement(id);
			
			// restore additional data from saved copy
			L2NpcTemplate created = getTemplate(id);
			
			if ((old != null) && (created != null))
			{
				created.getEventQuests().putAll(quests);
			}
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": Could not reload data for NPC " + id + ": " + e.getMessage(), e);
		}
	}
	
	/**
	 * Just wrapper.
	 */
	public void reloadAllNpc()
	{
		restoreNpcData();
	}
	
	/**
	 * Save npc.
	 * @param npc the npc
	 */
	public void saveNpc(StatsSet npc)
	{
		final Map<String, Object> set = npc.getSet();
		int length = 0;
		for (Object obj : set.keySet())
		{
			// 15 is just guessed npc name length
			length += ((String) obj).length() + 7 + 15;
		}
		
		final StringBuilder npcSb = new StringBuilder(length);
		final StringBuilder npcAiSb = new StringBuilder(30);
		String attribute;
		String value;
		for (Entry<String, Object> entry : set.entrySet())
		{
			attribute = entry.getKey();
			value = String.valueOf(entry.getValue());
			switch (attribute)
			{
				case "npcId":
					break;
				case "aggro":
				case "showName":
				case "targetable":
				{
					appendEntry(npcAiSb, attribute, value);
					break;
				}
				default:
				{
					appendEntry(npcSb, attribute, value);
				}
			}
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			int updated = 0;
			final int npcId = npc.getInteger("npcId");
			if (Config.CUSTOM_NPC_TABLE)
			{
				updated = performUpdate(npcSb, "custom_npc", "id", npcId, con);
				performUpdate(npcAiSb, "custom_npcaidata", "npcId", npcId, con);
			}
			
			if (updated == 0)
			{
				performUpdate(npcSb, "npc", "id", npcId, con);
				performUpdate(npcAiSb, "npcaidata", "npcId", npcId, con);
			}
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": Could not store new NPC data in database: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Append entry.
	 * @param sb the string builder to append the attribute and value.
	 * @param attribute the attribute to append.
	 * @param value the value to append.
	 */
	private final void appendEntry(StringBuilder sb, String attribute, String value)
	{
		if (sb.length() > 0)
		{
			sb.append(", ");
		}
		
		sb.append(attribute);
		sb.append(" = '");
		sb.append(value);
		sb.append('\'');
	}
	
	/**
	 * Perform update.
	 * @param sb the string builder with the parameters
	 * @param table the table to update.
	 * @param key the key of the table.
	 * @param npcId the Npc Id.
	 * @param con the current database connection.
	 * @return the count of updated NPCs.
	 * @throws SQLException the SQL exception.
	 */
	private final int performUpdate(StringBuilder sb, String table, String key, int npcId, Connection con) throws SQLException
	{
		int updated = 0;
		if ((sb != null) && !sb.toString().isEmpty())
		{
			final StringBuilder sbQuery = new StringBuilder(sb.length() + 28);
			sbQuery.append("UPDATE ");
			sbQuery.append(table);
			sbQuery.append(" SET ");
			sbQuery.append(sb.toString());
			sbQuery.append(" WHERE ");
			sbQuery.append(key);
			sbQuery.append(" = ?");
			try (PreparedStatement ps = con.prepareStatement(sbQuery.toString()))
			{
				ps.setInt(1, npcId);
				updated = ps.executeUpdate();
			}
		}
		return updated;
	}
	
	/**
	 * Gets the template.
	 * @param id the template Id to get.
	 * @return the template for the given id.
	 */
	public L2NpcTemplate getTemplate(int id)
	{
		return _npcs.get(id);
	}
	
	/**
	 * Gets the template by name.
	 * @param name of the template to get.
	 * @return the template for the given name.
	 */
	public L2NpcTemplate getTemplateByName(String name)
	{
		for (L2NpcTemplate npcTemplate : _npcs.values())
		{
			if (npcTemplate.getName().equalsIgnoreCase(name))
			{
				return npcTemplate;
			}
		}
		return null;
	}
	
	/**
	 * Gets the all of level.
	 * @param lvls of all the templates to get.
	 * @return the template list for the given level.
	 */
	public List<L2NpcTemplate> getAllOfLevel(int... lvls)
	{
		final List<L2NpcTemplate> list = new ArrayList<>();
		for (int lvl : lvls)
		{
			for (L2NpcTemplate t : _npcs.values())
			{
				if (t.getLevel() == lvl)
				{
					list.add(t);
				}
			}
		}
		return list;
	}
	
	/**
	 * Gets the all monsters of level.
	 * @param lvls of all the monster templates to get.
	 * @return the template list for the given level.
	 */
	public List<L2NpcTemplate> getAllMonstersOfLevel(int... lvls)
	{
		final List<L2NpcTemplate> list = new ArrayList<>();
		for (int lvl : lvls)
		{
			for (L2NpcTemplate t : _npcs.values())
			{
				if ((t.getLevel() == lvl) && t.isType("L2Monster"))
				{
					list.add(t);
				}
			}
		}
		return list;
	}
	
	/**
	 * Gets the all npc starting with.
	 * @param letters of all the NPC templates which its name start with.
	 * @return the template list for the given letter.
	 */
	public List<L2NpcTemplate> getAllNpcStartingWith(String... letters)
	{
		final List<L2NpcTemplate> list = new ArrayList<>();
		for (String letter : letters)
		{
			for (L2NpcTemplate t : _npcs.values())
			{
				if (t.getName().startsWith(letter) && t.isType("L2Npc"))
				{
					list.add(t);
				}
			}
		}
		return list;
	}
	
	/**
	 * Gets the all npc of class type.
	 * @param classTypes of all the templates to get.
	 * @return the template list for the given class type.
	 */
	public List<L2NpcTemplate> getAllNpcOfClassType(String... classTypes)
	{
		final List<L2NpcTemplate> list = new ArrayList<>();
		for (String classType : classTypes)
		{
			for (L2NpcTemplate t : _npcs.values())
			{
				if (t.isType(classType))
				{
					list.add(t);
				}
			}
		}
		return list;
	}
	
	/**
	 * Load npcs.
	 * @param id the id
	 */
	public void loadNpcs(int id)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			int count = loadNpcs(con, id, false);
			int ccount = 0;
			if (Config.CUSTOM_NPC_TABLE)
			{
				ccount = loadNpcs(con, id, true);
			}
			_log.info(getClass().getSimpleName() + ": Loaded " + count + " (Custom: " + ccount + ") NPC template(s).");
		}
		catch (Exception e)
		{
			_log.error(getClass().getSimpleName() + ": Error reading NPC AI Data: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Id equals to zero or lesser means all.
	 * @param con the database connection
	 * @param id of the NPC to load.
	 * @param isCustom the is custom
	 * @return the loaded NPC count
	 */
	public int loadNpcs(Connection con, int id, boolean isCustom)
	{
		int count = 0;
		try
		{
			final String query = isCustom ? (((id > 0) ? CUSTOM_SELECT_NPC_BY_ID : CUSTOM_SELECT_NPC_ALL)) : ((id > 0) ? SELECT_NPC_BY_ID : SELECT_NPC_ALL);
			try (PreparedStatement ps = con.prepareStatement(query))
			{
				if (id > 0)
				{
					ps.setInt(1, id);
				}
				
				try (ResultSet rs = ps.executeQuery())
				{
					while (rs.next())
					{
						fillNpcTable(rs);
						count++;
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.error(getClass().getSimpleName() + ": Error creating NPC table.", e);
		}
		return count;
	}
	
	/**
	 * Id equals to zero or lesser means all.
	 * @param id of the NPC to load it's skills.
	 */
	public void loadNpcsSkills(int id)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			int count = loadNpcsSkills(con, id, false);
			int ccount = 0;
			if (Config.CUSTOM_NPC_SKILLS_TABLE)
			{
				ccount = loadNpcsSkills(con, id, true);
			}
			_log.info(getClass().getSimpleName() + ": Loaded " + count + " (Custom: " + ccount + ") NPC skills.");
		}
		catch (Exception e)
		{
			_log.error(getClass().getSimpleName() + ": Error reading NPC AI Data: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Load npcs skills.
	 * @param con the database connection
	 * @param id the NPC Id
	 * @param isCustom the is custom
	 * @return the loaded NPC count
	 */
	private int loadNpcsSkills(Connection con, int id, boolean isCustom)
	{
		int count = 0;
		final String query = isCustom ? (((id > 0) ? CUSTOM_SELECT_SKILLS_BY_ID : CUSTOM_SELECT_SKILLS_ALL)) : ((id > 0) ? SELECT_SKILLS_BY_ID : SELECT_SKILLS_ALL);
		try (PreparedStatement ps = con.prepareStatement(query))
		{
			if (id > 0)
			{
				ps.setInt(1, id);
			}
			
			try (ResultSet rs = ps.executeQuery())
			{
				L2NpcTemplate npcDat = null;
				L2Skill npcSkill = null;
				
				while (rs.next())
				{
					int mobId = rs.getInt("npcid");
					npcDat = _npcs.get(mobId);
					
					if (npcDat == null)
					{
						_log.warn(getClass().getSimpleName() + ": Skill data for undefined NPC. npcId: " + mobId);
						continue;
					}
					
					int skillId = rs.getInt("skillid");
					int level = rs.getInt("level");
					
					if (skillId == L2Skill.SKILL_NPC_RACE)
					{
						npcDat.setRace(level);
						continue;
					}
					
					npcSkill = SkillData.getInstance().getInfo(skillId, level);
					if (npcSkill == null)
					{
						continue;
					}
					count++;
					npcDat.addSkill(npcSkill);
				}
			}
		}
		catch (Exception e)
		{
			_log.error(getClass().getSimpleName() + ": Error reading NPC skills table.", e);
		}
		return count;
	}
	
	/**
	 * Id equals to zero or lesser means all.
	 * @param id of the NPC to load it's drops.
	 */
	public void loadNpcsDrop(int id)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			int count = loadNpcsDrop(con, id, false);
			int ccount = 0;
			if (Config.CUSTOM_DROPLIST_TABLE)
			{
				ccount = loadNpcsDrop(con, id, true);
			}
			_log.info(getClass().getSimpleName() + ": Loaded " + count + " (Custom: " + ccount + ") drops.");
		}
		catch (Exception e)
		{
			_log.error(getClass().getSimpleName() + ": Error reading NPC AI Data: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Id equals to zero or lesser means all.
	 * @param id of the NPC to load it's skill learn list.
	 */
	public void loadNpcsSkillLearn(int id)
	{
		if (id > 0)
		{
			final List<ClassId> teachInfo = SkillLearnData.getInstance().getSkillLearnData(id);
			final L2NpcTemplate template = _npcs.get(id);
			if ((teachInfo != null) && (template != null))
			{
				template.addTeachInfo(teachInfo);
			}
		}
		else
		{
			for (L2NpcTemplate template : _npcs.values())
			{
				final List<ClassId> teachInfo = SkillLearnData.getInstance().getSkillLearnData(template.getId());
				if (teachInfo != null)
				{
					template.addTeachInfo(teachInfo);
				}
			}
		}
	}
	
	/**
	 * Load npcs drop.
	 * @param con the con
	 * @param id the id
	 * @param isCustom the is custom
	 * @return the int
	 */
	public int loadNpcsDrop(Connection con, int id, boolean isCustom)
	{
		int count = 0;
		final String query = isCustom ? (((id > 0) ? CUSTOM_SELECT_DROPLIST_BY_ID : CUSTOM_SELECT_DROPLIST_ALL)) : ((id > 0) ? SELECT_DROPLIST_BY_ID : SELECT_DROPLIST_ALL);
		try (PreparedStatement ps = con.prepareStatement(query))
		{
			if (id > 0)
			{
				ps.setInt(1, id);
			}
			
			try (ResultSet rs = ps.executeQuery())
			{
				L2DropData dropDat = null;
				L2NpcTemplate npcDat = null;
				while (rs.next())
				{
					int mobId = rs.getInt("mobId");
					npcDat = _npcs.get(mobId);
					if (npcDat == null)
					{
						_log.warn(getClass().getSimpleName() + ": Drop data for undefined NPC. npcId: " + mobId);
						continue;
					}
					dropDat = new L2DropData();
					
					dropDat.setItemId(rs.getInt("itemId"));
					dropDat.setMinDrop(rs.getInt("min"));
					dropDat.setMaxDrop(rs.getInt("max"));
					dropDat.setChance(rs.getInt("chance"));
					
					int category = rs.getInt("category");
					if (ItemData.getInstance().getTemplate(dropDat.getId()) == null)
					{
						_log.warn(getClass().getSimpleName() + ": Drop data for undefined item template! NpcId: " + mobId + " itemId: " + dropDat.getId());
						continue;
					}
					count++;
					npcDat.addDropData(dropDat, category);
				}
			}
		}
		catch (Exception e)
		{
			_log.error(getClass().getSimpleName() + ": Error reading NPC dropdata. ", e);
		}
		return count;
	}
	
	/**
	 * Id equals to zero or lesser means all.
	 * @param id of the NPC to load it's minions.
	 */
	public void loadMinions(int id)
	{
		final String query = (id > 0) ? SELECT_MINION_BY_ID : SELECT_MINION_ALL;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(query))
		{
			if (id > 0)
			{
				statement.setInt(1, id);
			}
			
			int count = 0;
			try (ResultSet rset = statement.executeQuery())
			{
				L2MinionData minionDat = null;
				L2NpcTemplate npcDat = null;
				
				int raidId;
				while (rset.next())
				{
					raidId = rset.getInt("boss_id");
					npcDat = _npcs.get(raidId);
					if (npcDat == null)
					{
						_log.warn(getClass().getSimpleName() + ": Minion references undefined boss NPC. Boss NpcId: " + raidId);
						continue;
					}
					
					minionDat = new L2MinionData();
					minionDat.setMinionId(rset.getInt("minion_id"));
					minionDat.setAmountMin(rset.getInt("amount_min"));
					minionDat.setAmountMax(rset.getInt("amount_max"));
					npcDat.addRaidData(minionDat);
					count++;
				}
			}
			_log.info(getClass().getSimpleName() + ": Loaded " + count + " Minions.");
		}
		catch (Exception e)
		{
			_log.error(getClass().getSimpleName() + ": Error loading minion data.", e);
		}
	}
	
	/**
	 * Id equals to zero or lesser means all.
	 * @param id of the NPC to load it's AI data.
	 */
	public void loadNpcsAI(int id)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			int count = loadNpcAi(con, id, false);
			int ccount = 0;
			if (Config.CUSTOM_NPC_TABLE)
			{
				ccount = loadNpcAi(con, id, true);
			}
			_log.info(getClass().getSimpleName() + ": Loaded " + count + " (Custom: " + ccount + ") AI Data.");
		}
		catch (Exception e)
		{
			_log.error(getClass().getSimpleName() + ": Error reading NPC AI Data: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Method that give the parameters will load one or all NPC AI from normal or custom tables.
	 * @param con the database connection
	 * @param id the NPC Id
	 * @param isCustom if {@code true} the data will be loaded from the custom table
	 * @return the count of NPC loaded
	 */
	private int loadNpcAi(Connection con, int id, boolean isCustom)
	{
		int count = 0;
		final String query = isCustom ? (((id > 0) ? CUSTOM_SELECT_NPC_AI_BY_ID : CUSTOM_SELECT_NPC_AI_ALL)) : ((id > 0) ? SELECT_NPC_AI_BY_ID : SELECT_NPC_AI_ALL);
		try (PreparedStatement ps = con.prepareStatement(query))
		{
			if (id > 0)
			{
				ps.setInt(1, id);
			}
			
			try (ResultSet rs = ps.executeQuery())
			{
				L2NpcAIData npcAIDat = null;
				L2NpcTemplate npcDat = null;
				
				int npcId;
				while (rs.next())
				{
					npcId = rs.getInt("npcId");
					npcDat = _npcs.get(npcId);
					if (npcDat == null)
					{
						_log.error(getClass().getSimpleName() + ": AI Data Error with id : " + npcId);
						continue;
					}
					
					npcAIDat = new L2NpcAIData();
					npcAIDat.setPrimarySkillId(rs.getInt("primarySkillId"));
					npcAIDat.setMinSkillChance(rs.getInt("minSkillChance"));
					npcAIDat.setMaxSkillChance(rs.getInt("maxSkillChance"));
					npcAIDat.setAggro(rs.getInt("aggro"));
					npcAIDat.setCanMove(rs.getInt("canMove"));
					npcAIDat.setShowName(rs.getInt("showName") == 1);
					npcAIDat.setTargetable(rs.getInt("targetable") == 1);
					npcAIDat.setSoulShot(rs.getInt("soulshot"));
					npcAIDat.setSpiritShot(rs.getInt("spiritshot"));
					npcAIDat.setSoulShotChance(rs.getInt("ssChance"));
					npcAIDat.setSpiritShotChance(rs.getInt("spsChance"));
					npcAIDat.setIsChaos(rs.getInt("isChaos"));
					npcAIDat.setShortRangeSkill(rs.getInt("minRangeSkill"));
					npcAIDat.setShortRangeChance(rs.getInt("minRangeChance"));
					npcAIDat.setLongRangeSkill(rs.getInt("maxRangeSkill"));
					npcAIDat.setLongRangeChance(rs.getInt("maxRangeChance"));
					npcAIDat.setClan(rs.getString("clan"));
					npcAIDat.setClanRange(rs.getInt("clanRange"));
					npcAIDat.setEnemyClan(rs.getString("enemyClan"));
					npcAIDat.setEnemyRange(rs.getInt("enemyRange"));
					npcAIDat.setDodge(rs.getInt("dodge"));
					npcAIDat.setAi(rs.getString("aiType"));
					
					npcDat.setAIData(npcAIDat);
					count++;
				}
			}
		}
		catch (SQLException e)
		{
			_log.error(getClass().getSimpleName() + ": Error reading NPC AI Data: " + e.getMessage(), e);
		}
		return count;
	}
	
	/**
	 * Id equals to zero or lesser means all.
	 * @param id of the NPC to load it's element data.
	 */
	public void loadNpcsElement(int id)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			int count = loadNpcsElement(con, id, false);
			int ccount = 0;
			if (Config.CUSTOM_NPC_TABLE)
			{
				ccount = loadNpcsElement(con, id, true);
			}
			_log.info(getClass().getSimpleName() + ": Loaded " + count + " (Custom: " + ccount + ") Elementals Data.");
		}
		catch (Exception e)
		{
			_log.error(getClass().getSimpleName() + ": Error reading NPC AI Data: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Load npcs element.
	 * @param con the con
	 * @param id the id
	 * @param isCustom the is custom
	 * @return the int
	 */
	private int loadNpcsElement(Connection con, int id, boolean isCustom)
	{
		int count = 0;
		final String query = isCustom ? (((id > 0) ? CUSTOM_SELECT_NPC_ELEMENTALS_BY_ID : CUSTOM_SELECT_NPC_ELEMENTALS_ALL)) : ((id > 0) ? SELECT_NPC_ELEMENTALS_BY_ID : SELECT_NPC_ELEMENTALS_ALL);
		try (PreparedStatement ps = con.prepareStatement(query))
		{
			if (id > 0)
			{
				ps.setInt(1, id);
			}
			
			try (ResultSet rset = ps.executeQuery())
			{
				L2NpcTemplate npcDat = null;
				int npcId;
				while (rset.next())
				{
					npcId = rset.getInt("npc_id");
					npcDat = _npcs.get(npcId);
					if (npcDat == null)
					{
						_log.error("NPCElementals: Elementals Error with id : " + npcId);
						continue;
					}
					switch (rset.getByte("elemAtkType"))
					{
						case Elementals.FIRE:
							npcDat.setBaseFire(rset.getInt("elemAtkValue"));
							break;
						case Elementals.WATER:
							npcDat.setBaseWater(rset.getInt("elemAtkValue"));
							break;
						case Elementals.EARTH:
							npcDat.setBaseEarth(rset.getInt("elemAtkValue"));
							break;
						case Elementals.WIND:
							npcDat.setBaseWind(rset.getInt("elemAtkValue"));
							break;
						case Elementals.HOLY:
							npcDat.setBaseHoly(rset.getInt("elemAtkValue"));
							break;
						case Elementals.DARK:
							npcDat.setBaseDark(rset.getInt("elemAtkValue"));
							break;
						default:
							_log.error("NPCElementals: Elementals Error with id : " + npcId + "; unknown elementType: " + rset.getByte("elemAtkType"));
							continue;
					}
					npcDat.setBaseFireRes(rset.getInt("fireDefValue"));
					npcDat.setBaseWaterRes(rset.getInt("waterDefValue"));
					npcDat.setBaseEarthRes(rset.getInt("earthDefValue"));
					npcDat.setBaseWindRes(rset.getInt("windDefValue"));
					npcDat.setBaseHolyRes(rset.getInt("holyDefValue"));
					npcDat.setBaseDarkRes(rset.getInt("darkDefValue"));
					count++;
				}
			}
		}
		catch (Exception e)
		{
			_log.error(getClass().getSimpleName() + ": Error reading NPC Elementals Data: " + e.getMessage(), e);
		}
		return count;
	}
	
	/**
	 * Gets the single instance of NpcTable.
	 * @return single instance of NpcTable
	 */
	public static NpcTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final NpcTable _instance = new NpcTable();
	}
}
