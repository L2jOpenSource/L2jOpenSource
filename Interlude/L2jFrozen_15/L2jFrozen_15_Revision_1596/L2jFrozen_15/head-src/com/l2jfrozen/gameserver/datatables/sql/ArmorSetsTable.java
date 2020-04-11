package com.l2jfrozen.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2ArmorSet;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class ArmorSetsTable
{
	private final static Logger LOGGER = Logger.getLogger(ArmorSetsTable.class);
	private static final String SELECT_ARMOR_SETS = "SELECT chest, legs, head, gloves, feet, skill_id, shield, shield_skill_id, enchant6skill FROM armorsets";
	private static final String SELECT_CUSTOM_ARMOR_SETS = "SELECT chest, legs, head, gloves, feet, skill_id, shield, shield_skill_id, enchant6skill FROM custom_armorsets";
	private static ArmorSetsTable instance;
	
	public Map<Integer, L2ArmorSet> armorSets;
	
	public static ArmorSetsTable getInstance()
	{
		if (instance == null)
		{
			instance = new ArmorSetsTable();
		}
		
		return instance;
	}
	
	private ArmorSetsTable()
	{
		armorSets = new HashMap<>();
		loadData();
	}
	
	private void loadData()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement statement = con.prepareStatement(SELECT_ARMOR_SETS);
				ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int chest = rset.getInt("chest");
					int legs = rset.getInt("legs");
					int head = rset.getInt("head");
					int gloves = rset.getInt("gloves");
					int feet = rset.getInt("feet");
					int skill_id = rset.getInt("skill_id");
					int shield = rset.getInt("shield");
					int shield_skill_id = rset.getInt("shield_skill_id");
					int enchant6skill = rset.getInt("enchant6skill");
					
					L2ArmorSet armorSet = new L2ArmorSet(chest, legs, head, gloves, feet, skill_id, shield, shield_skill_id, enchant6skill);
					armorSets.put(chest, armorSet);
				}
			}
			catch (Exception e)
			{
				LOGGER.error("ArmorSetsTable.loadData : Could not select data from armorsets table. ", e);
			}
			
			LOGGER.info("Loaded: " + armorSets.size() + " armor sets.");
			
			if (Config.CUSTOM_ARMORSETS_TABLE)
			{
				int count = 0;
				try (PreparedStatement statement = con.prepareStatement(SELECT_CUSTOM_ARMOR_SETS);
					ResultSet rset = statement.executeQuery())
				{
					while (rset.next())
					{
						int chest = rset.getInt("chest");
						int legs = rset.getInt("legs");
						int head = rset.getInt("head");
						int gloves = rset.getInt("gloves");
						int feet = rset.getInt("feet");
						int skill_id = rset.getInt("skill_id");
						int shield = rset.getInt("shield");
						int shield_skill_id = rset.getInt("shield_skill_id");
						int enchant6skill = rset.getInt("enchant6skill");
						
						L2ArmorSet armorSet = new L2ArmorSet(chest, legs, head, gloves, feet, skill_id, shield, shield_skill_id, enchant6skill);
						armorSet.setIsCustom(true);
						armorSets.put(chest, armorSet);
						count++;
					}
				}
				catch (Exception e)
				{
					LOGGER.error("ArmorSetsTable.loadData : Could not select data from custom_armorsets table.", e);
				}
				
				if (count > 0)
				{
					LOGGER.info("ArmorSetsTable: Loaded " + count + " custom armor sets.");
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("ArmorSetsTable.loadData : Error while loading armor sets data", e);
		}
	}
	
	public boolean setExists(int chestId)
	{
		return armorSets.containsKey(chestId);
	}
	
	public L2ArmorSet getSet(int chestId)
	{
		return armorSets.get(chestId);
	}
}
