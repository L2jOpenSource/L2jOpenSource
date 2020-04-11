package com.l2jfrozen.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2DropCategory;
import com.l2jfrozen.gameserver.model.L2DropData;
import com.l2jfrozen.gameserver.model.L2MinionData;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.base.ClassId;
import com.l2jfrozen.gameserver.skills.BaseStats;
import com.l2jfrozen.gameserver.skills.Stats;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class ...
 * @version $Revision: 1.8.2.6.2.9 $ $Date: 2005/04/06 16:13:25 $
 */
public class NpcTable
{
	private final static Logger LOGGER = Logger.getLogger(NpcTable.class);
	private static final String SELECT_DROPLIST_ORDER = "SELECT mobId, itemId, min, max, category, chance FROM droplist ORDER BY mobId, category, chance";
	private static final String SELECT_CUSTOM_DROPLIST_ORDER = "SELECT mobId, itemId, min, max, category, chance FROM custom_droplist ORDER BY mobId, category, chance";
	private static final String SELECT_MINIONS = "SELECT boss_id, minion_id, amount_min, amount_max FROM minions";
	private static NpcTable instance;
	
	private final Map<Integer, L2NpcTemplate> npcs;
	private boolean initialized = false;
	
	public static NpcTable getInstance()
	{
		if (instance == null)
		{
			instance = new NpcTable();
		}
		
		return instance;
	}
	
	private NpcTable()
	{
		npcs = new HashMap<>();
		
		restoreNpcData();
	}
	
	private void restoreNpcData()
	{
		Connection con = null;
		
		try
		{
			PreparedStatement statement;
			
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
				{
					"id",
					"idTemplate",
					"name",
					"serverSideName",
					"title",
					"serverSideTitle",
					"class",
					"collision_radius",
					"collision_height",
					"level",
					"sex",
					"type",
					"attackrange",
					"hp",
					"mp",
					"hpreg",
					"mpreg",
					"str",
					"con",
					"dex",
					"int",
					"wit",
					"men",
					"exp",
					"sp",
					"patk",
					"pdef",
					"matk",
					"mdef",
					"atkspd",
					"aggro",
					"matkspd",
					"rhand",
					"lhand",
					"armor",
					"walkspd",
					"runspd",
					"faction_id",
					"faction_range",
					"isUndead",
					"absorb_level",
					"absorb_type"
				}) + " FROM npc");
				final ResultSet npcdata = statement.executeQuery();
				fillNpcTable(npcdata, false);
				npcdata.close();
				DatabaseUtils.close(statement);
			}
			catch (final Exception e)
			{
				LOGGER.error("NPCTable: Error creating NPC table", e);
			}
			
			if (Config.CUSTOM_NPC_TABLE)
			{
				try
				{
					if (con == null)
					{
						con = L2DatabaseFactory.getInstance().getConnection();
					}
					statement = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
					{
						"id",
						"idTemplate",
						"name",
						"serverSideName",
						"title",
						"serverSideTitle",
						"class",
						"collision_radius",
						"collision_height",
						"level",
						"sex",
						"type",
						"attackrange",
						"hp",
						"mp",
						"hpreg",
						"mpreg",
						"str",
						"con",
						"dex",
						"int",
						"wit",
						"men",
						"exp",
						"sp",
						"patk",
						"pdef",
						"matk",
						"mdef",
						"atkspd",
						"aggro",
						"matkspd",
						"rhand",
						"lhand",
						"armor",
						"walkspd",
						"runspd",
						"faction_id",
						"faction_range",
						"isUndead",
						"absorb_level",
						"absorb_type"
					}) + " FROM custom_npc");
					final ResultSet npcdata = statement.executeQuery();
					fillNpcTable(npcdata, true);
					npcdata.close();
					DatabaseUtils.close(statement);
				}
				catch (final Exception e)
				{
					LOGGER.error("NPCTable: Error creating custom NPC table", e);
				}
			}
			try
			{
				if (con == null)
				{
					con = L2DatabaseFactory.getInstance().getConnection();
				}
				statement = con.prepareStatement("SELECT npcid, skillid, level FROM npcskills");
				final ResultSet npcskills = statement.executeQuery();
				L2NpcTemplate npcDat = null;
				L2Skill npcSkill = null;
				
				while (npcskills.next())
				{
					final int mobId = npcskills.getInt("npcid");
					npcDat = npcs.get(mobId);
					
					if (npcDat == null)
					{
						continue;
					}
					
					final int skillId = npcskills.getInt("skillid");
					final int level = npcskills.getInt("level");
					
					if (npcDat.race == null && skillId == 4416)
					{
						npcDat.setRace(level);
						continue;
					}
					
					npcSkill = SkillTable.getInstance().getInfo(skillId, level);
					
					if (npcSkill == null)
					{
						continue;
					}
					
					npcDat.addSkill(npcSkill);
				}
				
				npcskills.close();
				DatabaseUtils.close(statement);
			}
			catch (final Exception e)
			{
				LOGGER.error("NPCTable: Error reading NPC skills table", e);
			}
			
			if (Config.CUSTOM_DROPLIST_TABLE)
			{
				try
				{
					if (con == null)
					{
						con = L2DatabaseFactory.getInstance().getConnection();
					}
					statement = con.prepareStatement(SELECT_CUSTOM_DROPLIST_ORDER);
					final ResultSet dropData = statement.executeQuery();
					
					int cCount = 0;
					
					while (dropData.next())
					{
						final int mobId = dropData.getInt("mobId");
						
						final L2NpcTemplate npcDat = npcs.get(mobId);
						
						if (npcDat == null)
						{
							LOGGER.info("NPCTable: While loading from custom_droplist table, NPC ID " + mobId + " it does not exist in 'npc' or 'custom_npc' table ");
							continue;
						}
						
						final L2DropData dropDat = new L2DropData();
						dropDat.setItemId(dropData.getInt("itemId"));
						dropDat.setMinDrop(dropData.getInt("min"));
						dropDat.setMaxDrop(dropData.getInt("max"));
						dropDat.setChance(dropData.getInt("chance"));
						dropDat.setIsCustomDrop(true);
						
						final int category = dropData.getInt("category");
						
						npcDat.addDropData(dropDat, category);
						cCount++;
					}
					dropData.close();
					DatabaseUtils.close(statement);
					
					if (cCount > 0)
					{
						LOGGER.info("CustomDropList : Added " + cCount + " custom droplist");
					}
				}
				catch (final Exception e)
				{
					LOGGER.error("NPCTable: Error reading NPC CUSTOM drop data", e);
				}
			}
			
			try
			{
				if (con == null)
				{
					con = L2DatabaseFactory.getInstance().getConnection();
				}
				statement = con.prepareStatement(SELECT_DROPLIST_ORDER);
				final ResultSet dropData = statement.executeQuery();
				L2DropData dropDat = null;
				L2NpcTemplate npcDat = null;
				
				while (dropData.next())
				{
					final int mobId = dropData.getInt("mobId");
					
					npcDat = npcs.get(mobId);
					
					if (npcDat == null)
					{
						LOGGER.info("NPCTable: While loading from droplist table, NPC ID " + mobId + " it does not exist in 'npc' or 'custom_npc' table ");
						continue;
					}
					
					dropDat = new L2DropData();
					
					dropDat.setItemId(dropData.getInt("itemId"));
					dropDat.setMinDrop(dropData.getInt("min"));
					dropDat.setMaxDrop(dropData.getInt("max"));
					dropDat.setChance(dropData.getInt("chance"));
					
					final int category = dropData.getInt("category");
					
					npcDat.addDropData(dropDat, category);
				}
				
				dropData.close();
				DatabaseUtils.close(statement);
			}
			catch (final Exception e)
			{
				LOGGER.error("NPCTable: Error reading NPC drop data", e);
			}
			
			try
			{
				if (con == null)
				{
					con = L2DatabaseFactory.getInstance().getConnection();
				}
				statement = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
				{
					"npc_id",
					"class_id"
				}) + " FROM skill_learn");
				final ResultSet learndata = statement.executeQuery();
				
				while (learndata.next())
				{
					final int npcId = learndata.getInt("npc_id");
					final int classId = learndata.getInt("class_id");
					
					final L2NpcTemplate npc = getTemplate(npcId);
					if (npc == null)
					{
						LOGGER.warn("NPCTable: Error getting NPC template ID " + npcId + " while trying to load skill trainer data.");
						continue;
					}
					
					if (classId >= ClassId.values().length)
					{
						LOGGER.warn("NPCTable: Error defining learning data for NPC " + npcId + ": specified classId " + classId + " is higher then max one " + (ClassId.values().length - 1) + " specified into ClassID Enum --> check your Database to be complient with it");
						continue;
					}
					
					npc.addTeachInfo(ClassId.values()[classId]);
				}
				
				learndata.close();
				DatabaseUtils.close(statement);
			}
			catch (final Exception e)
			{
				LOGGER.error("NPCTable: Error reading NPC trainer data", e);
			}
			
			try
			{
				if (con == null)
				{
					con = L2DatabaseFactory.getInstance().getConnection();
				}
				statement = con.prepareStatement(SELECT_MINIONS);
				
				ResultSet minionData = statement.executeQuery();
				L2MinionData minionDat = null;
				L2NpcTemplate npcDat = null;
				int cnt = 0;
				
				while (minionData.next())
				{
					int raidId = minionData.getInt("boss_id");
					
					npcDat = npcs.get(raidId);
					
					if (npcDat == null)
					{
						LOGGER.warn("NpcTable.restoreNpcData : No raidboss NPC found for ID: " + raidId);
					}
					else
					{
						minionDat = new L2MinionData();
						minionDat.setMinionId(minionData.getInt("minion_id"));
						minionDat.setAmountMin(minionData.getInt("amount_min"));
						minionDat.setAmountMax(minionData.getInt("amount_max"));
						npcDat.addRaidData(minionDat);
						cnt++;
					}
				}
				
				minionData.close();
				DatabaseUtils.close(statement);
				LOGGER.info("NpcTable: Loaded " + cnt + " Minions.");
			}
			catch (Exception e)
			{
				LOGGER.error("NpcTable.restoreNpcData : Error loading minion data. ", e);
			}
		}
		finally
		{
			CloseUtil.close(con);
		}
		
		initialized = true;
	}
	
	private void fillNpcTable(final ResultSet NpcData, final boolean custom) throws Exception
	{
		int count = 0;
		while (NpcData.next())
		{
			final StatsSet npcDat = new StatsSet();
			
			final int id = NpcData.getInt("id");
			
			npcDat.set("npcId", id);
			npcDat.set("idTemplate", NpcData.getInt("idTemplate"));
			
			// Level: for special bosses could be different
			int level = 0;
			float diff = 0; // difference between setted value and retail one
			boolean minion = false;
			
			switch (id)
			{
				case 29002: // and minions
				case 29003:
				case 29004:
				case 29005:
					minion = true;
				case 29001:// queenAnt
				{
					if (Config.QA_LEVEL > 0)
					{
						diff = Config.QA_LEVEL - NpcData.getInt("level");
						level = Config.QA_LEVEL;
					}
					else
					{
						level = NpcData.getInt("level");
					}
					
				}
					break;
				case 29022:
				{ // zaken
					
					if (Config.ZAKEN_LEVEL > 0)
					{
						diff = Config.ZAKEN_LEVEL - NpcData.getInt("level");
						level = Config.ZAKEN_LEVEL;
					}
					else
					{
						level = NpcData.getInt("level");
					}
					
				}
					break;
				case 29015: // and minions
				case 29016:
				case 29017:
				case 29018:
					minion = true;
				case 29014:// orfen
				{
					
					if (Config.ORFEN_LEVEL > 0)
					{
						diff = Config.ORFEN_LEVEL - NpcData.getInt("level");
						level = Config.ORFEN_LEVEL;
					}
					else
					{
						level = NpcData.getInt("level");
					}
					
				}
					break;
				case 29007: // and minions
				case 29008:
				case 290011:
					minion = true;
				case 29006: // core
				{
					
					if (Config.CORE_LEVEL > 0)
					{
						diff = Config.CORE_LEVEL - NpcData.getInt("level");
						level = Config.CORE_LEVEL;
					}
					else
					{
						level = NpcData.getInt("level");
					}
					
				}
					break;
				default:
				{
					level = NpcData.getInt("level");
				}
			}
			
			npcDat.set("level", level);
			npcDat.set("jClass", NpcData.getString("class"));
			
			npcDat.set("baseShldDef", 0);
			npcDat.set("baseShldRate", 0);
			npcDat.set("baseCritRate", 4);
			
			npcDat.set("name", NpcData.getString("name"));
			npcDat.set("serverSideName", NpcData.getBoolean("serverSideName"));
			// npcDat.set("name", "");
			npcDat.set("title", NpcData.getString("title"));
			npcDat.set("serverSideTitle", NpcData.getBoolean("serverSideTitle"));
			npcDat.set("collision_radius", NpcData.getDouble("collision_radius"));
			npcDat.set("collision_height", NpcData.getDouble("collision_height"));
			npcDat.set("sex", NpcData.getString("sex"));
			npcDat.set("type", NpcData.getString("type"));
			npcDat.set("baseAtkRange", NpcData.getInt("attackrange"));
			
			// BOSS POWER CHANGES
			double multi_value = 1;
			
			if (diff >= 15)
			{ // means that there is level customization
				multi_value = multi_value * (diff / 10);
			}
			else if (diff > 0 && diff < 15)
			{
				multi_value = multi_value + (diff / 10);
			}
			
			if (minion)
			{
				multi_value = multi_value * Config.LEVEL_DIFF_MULTIPLIER_MINION; // allow to increase the power of a value
				// that for example, at 40 diff levels is
				// equal to
				// value = ((40/10)*0.8) = 3,2 --> 220 % more
			}
			else
			{
				switch (id)
				{
					case 29001: // Queen Ant
					{
						if (Config.QA_POWER_MULTIPLIER > 0)
						{
							multi_value = multi_value * Config.QA_POWER_MULTIPLIER;
						}
						
					}
						break;
					case 29022: // Zaken
					{
						if (Config.ZAKEN_POWER_MULTIPLIER > 0)
						{
							multi_value = multi_value * Config.ZAKEN_POWER_MULTIPLIER;
						}
						
					}
						break;
					case 29014: // Orfen
					{
						if (Config.ORFEN_POWER_MULTIPLIER > 0)
						{
							multi_value = multi_value * Config.ORFEN_POWER_MULTIPLIER;
						}
						
					}
						break;
					case 29006: // Core
					{
						if (Config.CORE_POWER_MULTIPLIER > 0)
						{
							multi_value = multi_value * Config.CORE_POWER_MULTIPLIER;
						}
						
					}
						break;
					case 29019: // Antharas
					{
						if (Config.ANTHARAS_POWER_MULTIPLIER > 0)
						{
							multi_value = multi_value * Config.ANTHARAS_POWER_MULTIPLIER;
						}
						
					}
						break;
					case 29028: // Valakas
					{
						if (Config.VALAKAS_POWER_MULTIPLIER > 0)
						{
							multi_value = multi_value * Config.VALAKAS_POWER_MULTIPLIER;
						}
						
					}
						break;
					case 29020: // Baium
					{
						if (Config.BAIUM_POWER_MULTIPLIER > 0)
						{
							multi_value = multi_value * Config.BAIUM_POWER_MULTIPLIER;
						}
					}
						break;
					case 29045: // Frintezza
					{
						if (Config.FRINTEZZA_POWER_MULTIPLIER > 0)
						{
							multi_value = multi_value * Config.FRINTEZZA_POWER_MULTIPLIER;
						}
					}
						break;
				}
			}
			
			npcDat.set("rewardExp", NpcData.getInt("exp") * multi_value);
			npcDat.set("rewardSp", NpcData.getInt("sp") * multi_value);
			npcDat.set("basePAtkSpd", NpcData.getInt("atkspd") * multi_value);
			npcDat.set("baseMAtkSpd", NpcData.getInt("matkspd") * multi_value);
			npcDat.set("baseHpMax", NpcData.getInt("hp") * multi_value);
			npcDat.set("baseMpMax", NpcData.getInt("mp") * multi_value);
			npcDat.set("baseHpReg", (int) NpcData.getFloat("hpreg") * multi_value > 0 ? NpcData.getFloat("hpreg") : 1.5 + (level - 1) / 10.0);
			npcDat.set("baseMpReg", (int) NpcData.getFloat("mpreg") * multi_value > 0 ? NpcData.getFloat("mpreg") : 0.9 + 0.3 * (level - 1) / 10.0);
			npcDat.set("basePAtk", NpcData.getInt("patk") * multi_value);
			npcDat.set("basePDef", NpcData.getInt("pdef") * multi_value);
			npcDat.set("baseMAtk", NpcData.getInt("matk") * multi_value);
			npcDat.set("baseMDef", NpcData.getInt("mdef") * multi_value);
			
			npcDat.set("aggroRange", NpcData.getInt("aggro"));
			npcDat.set("rhand", NpcData.getInt("rhand"));
			npcDat.set("lhand", NpcData.getInt("lhand"));
			npcDat.set("armor", NpcData.getInt("armor"));
			npcDat.set("baseWalkSpd", NpcData.getInt("walkspd"));
			npcDat.set("baseRunSpd", NpcData.getInt("runspd"));
			
			// constants, until we have stats in DB
			// constants, until we have stats in DB
			npcDat.safeSet("baseSTR", NpcData.getInt("str"), 0, BaseStats.MAX_STAT_VALUE, "Loading npc template id: " + NpcData.getInt("idTemplate"));
			npcDat.safeSet("baseCON", NpcData.getInt("con"), 0, BaseStats.MAX_STAT_VALUE, "Loading npc template id: " + NpcData.getInt("idTemplate"));
			npcDat.safeSet("baseDEX", NpcData.getInt("dex"), 0, BaseStats.MAX_STAT_VALUE, "Loading npc template id: " + NpcData.getInt("idTemplate"));
			npcDat.safeSet("baseINT", NpcData.getInt("int"), 0, BaseStats.MAX_STAT_VALUE, "Loading npc template id: " + NpcData.getInt("idTemplate"));
			npcDat.safeSet("baseWIT", NpcData.getInt("wit"), 0, BaseStats.MAX_STAT_VALUE, "Loading npc template id: " + NpcData.getInt("idTemplate"));
			npcDat.safeSet("baseMEN", NpcData.getInt("men"), 0, BaseStats.MAX_STAT_VALUE, "Loading npc template id: " + NpcData.getInt("idTemplate"));
			npcDat.set("baseCpMax", 0);
			
			npcDat.set("factionId", NpcData.getString("faction_id"));
			npcDat.set("factionRange", NpcData.getInt("faction_range"));
			
			npcDat.set("isUndead", NpcData.getString("isUndead"));
			
			npcDat.set("absorb_level", NpcData.getString("absorb_level"));
			npcDat.set("absorb_type", NpcData.getString("absorb_type"));
			
			final L2NpcTemplate template = new L2NpcTemplate(npcDat, custom);
			template.addVulnerability(Stats.BOW_WPN_VULN, 1);
			template.addVulnerability(Stats.BLUNT_WPN_VULN, 1);
			template.addVulnerability(Stats.DAGGER_WPN_VULN, 1);
			
			npcs.put(id, template);
			count++;
		}
		
		if (custom)
		{
			LOGGER.info("NpcTable: Loaded " + count + " Custom NPCs templates.");
		}
		else
		{
			LOGGER.info("NpcTable: Loaded " + count + " NPCs templates.");
		}
	}
	
	public void reloadNpc(final int id)
	{
		Connection con = null;
		
		try
		{
			// save a copy of the old data
			final L2NpcTemplate old = getTemplate(id);
			final Map<Integer, L2Skill> skills = new HashMap<>();
			
			skills.putAll(old.getSkills());
			
			final List<L2DropCategory> categories = new ArrayList<>();
			
			if (old.getDropData() != null)
			{
				categories.addAll(old.getDropData());
			}
			final ClassId[] classIds = old.getTeachInfo().clone();
			
			final List<L2MinionData> minions = new ArrayList<>();
			
			if (old.getMinionData() != null)
			{
				minions.addAll(old.getMinionData());
			}
			
			// reload the NPC base data
			con = L2DatabaseFactory.getInstance().getConnection();
			
			if (old.isCustom())
			{
				
				final PreparedStatement st = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
				{
					"id",
					"idTemplate",
					"name",
					"serverSideName",
					"title",
					"serverSideTitle",
					"class",
					"collision_radius",
					"collision_height",
					"level",
					"sex",
					"type",
					"attackrange",
					"hp",
					"mp",
					"hpreg",
					"mpreg",
					"str",
					"con",
					"dex",
					"int",
					"wit",
					"men",
					"exp",
					"sp",
					"patk",
					"pdef",
					"matk",
					"mdef",
					"atkspd",
					"aggro",
					"matkspd",
					"rhand",
					"lhand",
					"armor",
					"walkspd",
					"runspd",
					"faction_id",
					"faction_range",
					"isUndead",
					"absorb_level",
					"absorb_type"
				}) + " FROM custom_npc WHERE id=?");
				st.setInt(1, id);
				final ResultSet rs = st.executeQuery();
				fillNpcTable(rs, true);
				rs.close();
				st.close();
				
			}
			else
			{
				
				final PreparedStatement st = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
				{
					"id",
					"idTemplate",
					"name",
					"serverSideName",
					"title",
					"serverSideTitle",
					"class",
					"collision_radius",
					"collision_height",
					"level",
					"sex",
					"type",
					"attackrange",
					"hp",
					"mp",
					"hpreg",
					"mpreg",
					"str",
					"con",
					"dex",
					"int",
					"wit",
					"men",
					"exp",
					"sp",
					"patk",
					"pdef",
					"matk",
					"mdef",
					"atkspd",
					"aggro",
					"matkspd",
					"rhand",
					"lhand",
					"armor",
					"walkspd",
					"runspd",
					"faction_id",
					"faction_range",
					"isUndead",
					"absorb_level",
					"absorb_type"
				}) + " FROM npc WHERE id=?");
				st.setInt(1, id);
				final ResultSet rs = st.executeQuery();
				fillNpcTable(rs, false);
				rs.close();
				st.close();
				
			}
			
			// restore additional data from saved copy
			final L2NpcTemplate created = getTemplate(id);
			
			for (final L2Skill skill : skills.values())
			{
				created.addSkill(skill);
			}
			
			for (final ClassId classId : classIds)
			{
				created.addTeachInfo(classId);
			}
			
			for (final L2MinionData minion : minions)
			{
				created.addRaidData(minion);
			}
		}
		catch (final Exception e)
		{
			LOGGER.error("NPCTable: Could not reload data for NPC " + " " + id, e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	// just wrapper
	public void reloadAllNpc()
	{
		restoreNpcData();
	}
	
	public void saveNpc(final StatsSet npc)
	{
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final Map<String, Object> set = npc.getSet();
			
			String name = "";
			String values = "";
			
			final L2NpcTemplate old = getTemplate(npc.getInteger("npcId"));
			
			for (final Object obj : set.keySet())
			{
				name = (String) obj;
				
				if (!name.equalsIgnoreCase("npcId"))
				{
					if (values != "")
					{
						values += ", ";
					}
					
					values += name + " = '" + set.get(name) + "'";
				}
			}
			
			PreparedStatement statement = null;
			if (old.isCustom())
			{
				statement = con.prepareStatement("UPDATE custom_npc SET " + values + " WHERE id = ?");
				
			}
			else
			{
				statement = con.prepareStatement("UPDATE npc SET " + values + " WHERE id = ?");
				
			}
			statement.setInt(1, npc.getInteger("npcId"));
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
		}
		catch (final Exception e)
		{
			LOGGER.error("NPCTable: Could not store new NPC data in database", e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	public boolean isInitialized()
	{
		return initialized;
	}
	
	public void replaceTemplate(L2NpcTemplate npc)
	{
		npcs.put(npc.npcId, npc);
	}
	
	public L2NpcTemplate getTemplate(int id)
	{
		return npcs.get(id);
	}
	
	public L2NpcTemplate getTemplateByName(String name)
	{
		for (L2NpcTemplate npcTemplate : npcs.values())
		{
			if (npcTemplate.name.equalsIgnoreCase(name))
			{
				return npcTemplate;
			}
		}
		
		return null;
	}
	
	public L2NpcTemplate[] getAllOfLevel(int lvl)
	{
		List<L2NpcTemplate> list = new ArrayList<>();
		
		for (L2NpcTemplate t : npcs.values())
		{
			if (t.level == lvl)
			{
				list.add(t);
			}
		}
		
		return list.toArray(new L2NpcTemplate[list.size()]);
	}
	
	public L2NpcTemplate[] getAllMonstersOfLevel(int lvl)
	{
		List<L2NpcTemplate> list = new ArrayList<>();
		
		for (L2NpcTemplate t : npcs.values())
		{
			if (t.level == lvl && "L2Monster".equals(t.type))
			{
				list.add(t);
			}
		}
		
		return list.toArray(new L2NpcTemplate[list.size()]);
	}
	
	public L2NpcTemplate[] getAllNpcStartingWith(String text)
	{
		List<L2NpcTemplate> list = new ArrayList<>();
		
		for (L2NpcTemplate t : npcs.values())
		{
			if (t.name.startsWith(text) && "L2Npc".equals(t.type))
			{
				list.add(t);
			}
		}
		
		return list.toArray(new L2NpcTemplate[list.size()]);
	}
	
	public Map<Integer, L2NpcTemplate> getAllTemplates()
	{
		return npcs;
	}
}
