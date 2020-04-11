package com.l2jfrozen.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.model.L2LvlupData;
import com.l2jfrozen.gameserver.model.base.ClassId;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class ...
 * @author  NightMarez
 * @version $Revision: 1.3.2.4.2.3 $ $Date: 2005/03/27 15:29:18 $
 */
public class LevelUpData
{
	private static final String SELECT_ALL = "SELECT classid, defaulthpbase, defaulthpadd, defaulthpmod, defaultcpbase, defaultcpadd, defaultcpmod, defaultmpbase, defaultmpadd, defaultmpmod, class_lvl FROM lvlupgain";
	private static final String CLASS_LVL = "class_lvl";
	private static final String MP_MOD = "defaultmpmod";
	private static final String MP_ADD = "defaultmpadd";
	private static final String MP_BASE = "defaultmpbase";
	private static final String HP_MOD = "defaulthpmod";
	private static final String HP_ADD = "defaulthpadd";
	private static final String HP_BASE = "defaulthpbase";
	private static final String CP_MOD = "defaultcpmod";
	private static final String CP_ADD = "defaultcpadd";
	private static final String CP_BASE = "defaultcpbase";
	private static final String CLASS_ID = "classid";
	
	private final static Logger LOGGER = Logger.getLogger(LevelUpData.class);
	
	private static LevelUpData instance;
	
	private final Map<Integer, L2LvlupData> lvlTable;
	
	public static LevelUpData getInstance()
	{
		if (instance == null)
		{
			instance = new LevelUpData();
		}
		
		return instance;
	}
	
	private LevelUpData()
	{
		lvlTable = new HashMap<>();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_ALL);
			ResultSet rset = statement.executeQuery())
		{
			L2LvlupData lvlDat;
			
			while (rset.next())
			{
				lvlDat = new L2LvlupData();
				lvlDat.setClassid(rset.getInt(CLASS_ID));
				lvlDat.setClassLvl(rset.getInt(CLASS_LVL));
				lvlDat.setClassHpBase(rset.getFloat(HP_BASE));
				lvlDat.setClassHpAdd(rset.getFloat(HP_ADD));
				lvlDat.setClassHpModifier(rset.getFloat(HP_MOD));
				lvlDat.setClassCpBase(rset.getFloat(CP_BASE));
				lvlDat.setClassCpAdd(rset.getFloat(CP_ADD));
				lvlDat.setClassCpModifier(rset.getFloat(CP_MOD));
				lvlDat.setClassMpBase(rset.getFloat(MP_BASE));
				lvlDat.setClassMpAdd(rset.getFloat(MP_ADD));
				lvlDat.setClassMpModifier(rset.getFloat(MP_MOD));
				
				lvlTable.put(lvlDat.getClassid(), lvlDat);
			}
			
			LOGGER.info("LevelUpData: Loaded " + lvlTable.size() + " Character Level Up Templates.");
		}
		catch (Exception e)
		{
			LOGGER.error("LevelUpData.LevelUpdata : Error while creating Lvl up data table", e);
		}
	}
	
	/**
	 * @param  classId
	 * @return
	 */
	public L2LvlupData getTemplate(final int classId)
	{
		return lvlTable.get(classId);
	}
	
	public L2LvlupData getTemplate(final ClassId classId)
	{
		return lvlTable.get(classId.getId());
	}
}
