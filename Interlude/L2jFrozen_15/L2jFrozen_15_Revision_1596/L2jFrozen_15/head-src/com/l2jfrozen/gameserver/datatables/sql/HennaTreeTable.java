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
import com.l2jfrozen.gameserver.datatables.csv.HennaTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2HennaInstance;
import com.l2jfrozen.gameserver.model.base.ClassId;
import com.l2jfrozen.gameserver.templates.L2Henna;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class ...
 * @version $Revision$ $Date$
 */

public class HennaTreeTable
{
	private static Logger LOGGER = Logger.getLogger(HennaTreeTable.class);
	private static final HennaTreeTable instance = new HennaTreeTable();
	private final Map<ClassId, List<L2HennaInstance>> hennaTrees;
	private final boolean initialized = true;
	
	public static HennaTreeTable getInstance()
	{
		return instance;
	}
	
	private HennaTreeTable()
	{
		hennaTrees = new HashMap<>();
		int classId = 0;
		int count = 0;
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("SELECT class_name, id, parent_id FROM class_list ORDER BY id");
			final ResultSet classlist = statement.executeQuery();
			List<L2HennaInstance> list;
			
			// int parentClassId;
			// L2Henna henna;
			
			classlist:
			while (classlist.next())
			{
				list = new ArrayList<>();
				classId = classlist.getInt("id");
				final PreparedStatement statement2 = con.prepareStatement("SELECT class_id, symbol_id FROM henna_trees where class_id=? ORDER BY symbol_id");
				statement2.setInt(1, classId);
				final ResultSet hennatree = statement2.executeQuery();
				
				while (hennatree.next())
				{
					final int id = hennatree.getInt("symbol_id");
					// String name = hennatree.getString("name");
					final L2Henna template = HennaTable.getInstance().getTemplate(id);
					
					if (template == null)
					{
						hennatree.close();
						statement2.close();
						classlist.close();
						DatabaseUtils.close(statement);
						continue classlist;
					}
					
					final L2HennaInstance temp = new L2HennaInstance(template);
					temp.setSymbolId(id);
					temp.setItemIdDye(template.getDyeId());
					temp.setAmountDyeRequire(template.getAmountDyeRequire());
					temp.setPrice(template.getPrice());
					temp.setStatINT(template.getStatINT());
					temp.setStatSTR(template.getStatSTR());
					temp.setStatCON(template.getStatCON());
					temp.setStatMEM(template.getStatMEM());
					temp.setStatDEX(template.getStatDEX());
					temp.setStatWIT(template.getStatWIT());
					
					list.add(temp);
				}
				hennaTrees.put(ClassId.values()[classId], list);
				
				hennatree.close();
				statement2.close();
				
				count += list.size();
				if (Config.DEBUG)
				{
					LOGGER.info("Henna Tree for Class: " + classId + " has " + list.size() + " Henna Templates.");
				}
			}
			
			classlist.close();
			DatabaseUtils.close(statement);
			
		}
		catch (final Exception e)
		{
			LOGGER.error("Error while creating henna tree for classId {}" + " " + classId, e);
		}
		finally
		{
			CloseUtil.close(con);
		}
		
		LOGGER.info("HennaTreeTable: Loaded " + count + " Henna Tree Templates.");
		
	}
	
	public L2HennaInstance[] getAvailableHenna(final ClassId classId)
	{
		final List<L2HennaInstance> henna = hennaTrees.get(classId);
		if (henna == null)
		{
			// the hennatree for this class is undefined, so we give an empty list
			LOGGER.warn("Hennatree for class {} is not defined !" + " " + classId);
			return new L2HennaInstance[0];
		}
		
		return henna.toArray(new L2HennaInstance[henna.size()]);
	}
	
	public boolean isInitialized()
	{
		return initialized;
	}
	
}
