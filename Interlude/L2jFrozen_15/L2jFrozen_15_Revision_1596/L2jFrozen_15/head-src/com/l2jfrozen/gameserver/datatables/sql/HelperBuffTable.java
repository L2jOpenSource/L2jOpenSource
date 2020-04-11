package com.l2jfrozen.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.datatables.csv.HennaTable;
import com.l2jfrozen.gameserver.templates.L2HelperBuff;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class represents the Newbie Helper Buff list. Author: Ayor
 */

public class HelperBuffTable
{
	private final static Logger LOGGER = Logger.getLogger(HennaTable.class);
	private static HelperBuffTable instance;
	
	private static final String SELECT_HELPER_BUFF_LIST = "SELECT id,skill_id,name,skill_level,lower_level,upper_level,is_magic_class FROM helper_buff_list";
	
	/** The table containing all Buff of the Newbie Helper */
	public List<L2HelperBuff> helperBuff;
	
	private final boolean initialized = true;
	
	/**
	 * The player level since Newbie Helper can give the fisrt buff <BR>
	 * Used to generate message : "Come back here when you have reached level ...")
	 */
	private int magicClassLowestLevel = 100;
	private int physicClassLowestLevel = 100;
	
	/**
	 * The player level above which Newbie Helper won't give any buff <BR>
	 * Used to generate message : "Only novice character of level ... or less can receive my support magic.")
	 */
	private int magicClassHighestLevel = 1;
	private int physicClassHighestLevel = 1;
	
	public static HelperBuffTable getInstance()
	{
		if (instance == null)
		{
			instance = new HelperBuffTable();
		}
		
		return instance;
	}
	
	public static void reload()
	{
		instance = null;
		getInstance();
	}
	
	/**
	 * Create and Load the Newbie Helper Buff list from SQL Table helper_buff_list
	 */
	private HelperBuffTable()
	{
		helperBuff = new ArrayList<>();
		restoreHelperBuffData();
	}
	
	/**
	 * Read and Load the Newbie Helper Buff list from SQL Table helper_buff_list
	 */
	private void restoreHelperBuffData()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_HELPER_BUFF_LIST);
			ResultSet helperbuffdata = statement.executeQuery())
		{
			while (helperbuffdata.next())
			{
				StatsSet helperBuffDat = new StatsSet();
				int id = helperbuffdata.getInt("id");
				
				helperBuffDat.set("id", id);
				helperBuffDat.set("skillID", helperbuffdata.getInt("skill_id"));
				helperBuffDat.set("skillLevel", helperbuffdata.getInt("skill_level"));
				helperBuffDat.set("lowerLevel", helperbuffdata.getInt("lower_level"));
				helperBuffDat.set("upperLevel", helperbuffdata.getInt("upper_level"));
				helperBuffDat.set("isMagicClass", helperbuffdata.getString("is_magic_class"));
				
				// Calulate the range level in wich player must be to obtain buff from Newbie Helper
				if ("false".equals(helperbuffdata.getString("is_magic_class")))
				{
					if (helperbuffdata.getInt("lower_level") < physicClassLowestLevel)
					{
						physicClassLowestLevel = helperbuffdata.getInt("lower_level");
					}
					
					if (helperbuffdata.getInt("upper_level") > physicClassHighestLevel)
					{
						physicClassHighestLevel = helperbuffdata.getInt("upper_level");
					}
				}
				else
				{
					if (helperbuffdata.getInt("lower_level") < magicClassLowestLevel)
					{
						magicClassLowestLevel = helperbuffdata.getInt("lower_level");
					}
					
					if (helperbuffdata.getInt("upper_level") > magicClassHighestLevel)
					{
						magicClassHighestLevel = helperbuffdata.getInt("upper_level");
					}
				}
				
				// Add this Helper Buff to the Helper Buff List
				L2HelperBuff template = new L2HelperBuff(helperBuffDat);
				helperBuff.add(template);
			}
			
			LOGGER.info("Helper Buff Table: Loaded " + helperBuff.size() + " templates");
		}
		catch (Exception e)
		{
			LOGGER.error("HelperBuffTable.restoreHelperBuffData : Could not select data from helper_buff_list table", e);
		}
		
	}
	
	public boolean isInitialized()
	{
		return initialized;
	}
	
	public L2HelperBuff getHelperBuffTableItem(final int id)
	{
		return helperBuff.get(id);
	}
	
	/**
	 * @return the Helper Buff List
	 */
	public List<L2HelperBuff> getHelperBuffTable()
	{
		return helperBuff;
	}
	
	/**
	 * @return Returns the magicClassHighestLevel.
	 */
	public int getMagicClassHighestLevel()
	{
		return magicClassHighestLevel;
	}
	
	/**
	 * @param magicClassHighestLevel The magicClassHighestLevel to set.
	 */
	public void setMagicClassHighestLevel(final int magicClassHighestLevel)
	{
		this.magicClassHighestLevel = magicClassHighestLevel;
	}
	
	/**
	 * @return Returns the magicClassLowestLevel.
	 */
	public int getMagicClassLowestLevel()
	{
		return magicClassLowestLevel;
	}
	
	/**
	 * @param magicClassLowestLevel The magicClassLowestLevel to set.
	 */
	public void setMagicClassLowestLevel(final int magicClassLowestLevel)
	{
		this.magicClassLowestLevel = magicClassLowestLevel;
	}
	
	/**
	 * @return Returns the physicClassHighestLevel.
	 */
	public int getPhysicClassHighestLevel()
	{
		return physicClassHighestLevel;
	}
	
	/**
	 * @param physicClassHighestLevel The physicClassHighestLevel to set.
	 */
	public void setPhysicClassHighestLevel(final int physicClassHighestLevel)
	{
		this.physicClassHighestLevel = physicClassHighestLevel;
	}
	
	/**
	 * @return Returns the physicClassLowestLevel.
	 */
	public int getPhysicClassLowestLevel()
	{
		return physicClassLowestLevel;
	}
	
	/**
	 * @param physicClassLowestLevel The physicClassLowestLevel to set.
	 */
	public void setPhysicClassLowestLevel(final int physicClassLowestLevel)
	{
		this.physicClassLowestLevel = physicClassLowestLevel;
	}
	
}
