package com.l2jfrozen.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2TeleportLocation;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class ...
 * @version $Revision: 1.3.2.2.2.3 $ $Date: 2005/03/27 15:29:18 $
 */
public class TeleportLocationTable
{
	private final static Logger LOGGER = Logger.getLogger(TeleportLocationTable.class);
	private static final String SELECT_TELEPORTS = "SELECT Description, id, loc_x, loc_y, loc_z, price, fornoble FROM teleport";
	private static final String SELECT_CUSTOM_TELEPORTS = "SELECT Description, id, loc_x, loc_y, loc_z, price, fornoble FROM custom_teleport";
	
	private static TeleportLocationTable instance;
	
	private Map<Integer, L2TeleportLocation> teleports;
	
	public static TeleportLocationTable getInstance()
	{
		if (instance == null)
		{
			instance = new TeleportLocationTable();
		}
		
		return instance;
	}
	
	private TeleportLocationTable()
	{
		reloadAll();
	}
	
	public void reloadAll()
	{
		teleports = new HashMap<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_TELEPORTS);
			ResultSet rset = statement.executeQuery())
		{
			L2TeleportLocation teleport;
			
			while (rset.next())
			{
				teleport = new L2TeleportLocation();
				
				teleport.setTeleId(rset.getInt("id"));
				teleport.setLocX(rset.getInt("loc_x"));
				teleport.setLocY(rset.getInt("loc_y"));
				teleport.setLocZ(rset.getInt("loc_z"));
				teleport.setPrice(rset.getInt("price"));
				teleport.setIsForNoble(rset.getInt("fornoble") == 1);
				
				teleports.put(teleport.getTeleId(), teleport);
			}
			
			LOGGER.info("TeleportLocationTable: Loaded " + teleports.size() + " Teleport Location Templates");
		}
		catch (Exception e)
		{
			LOGGER.error("TeleportLocationTable.reloadAll : Error while creating teleport table ", e);
		}
		
		if (Config.CUSTOM_TELEPORT_TABLE)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(SELECT_CUSTOM_TELEPORTS);
				ResultSet rset = statement.executeQuery())
			{
				L2TeleportLocation teleport;
				
				int cTeleCount = teleports.size();
				
				while (rset.next())
				{
					teleport = new L2TeleportLocation();
					teleport.setTeleId(rset.getInt("id"));
					teleport.setLocX(rset.getInt("loc_x"));
					teleport.setLocY(rset.getInt("loc_y"));
					teleport.setLocZ(rset.getInt("loc_z"));
					teleport.setPrice(rset.getInt("price"));
					teleport.setIsForNoble(rset.getInt("fornoble") == 1);
					teleports.put(teleport.getTeleId(), teleport);
				}
				
				cTeleCount = teleports.size() - cTeleCount;
				
				if (cTeleCount > 0)
				{
					LOGGER.info("CustomTeleportLocationTable: Loaded " + cTeleCount + " Custom Teleport Location Templates.");
				}
				
			}
			catch (Exception e)
			{
				LOGGER.error("TeleportLocationTable.reloadAll : Error while creating custom teleport table ", e);
			}
		}
	}
	
	public L2TeleportLocation getTemplate(final int id)
	{
		return teleports.get(id);
	}
}
