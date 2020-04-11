
/*
 coded by Balancer
 ported to L2JRU by Mr
 balancer@balancer.ru
 http://balancer.ru

 version 0.1.1, 2005-06-07
 version 0.1, 2005-03-16
 */
package com.l2jfrozen.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.controllers.TradeController;
import com.l2jfrozen.gameserver.model.L2Territory;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class TerritoryTable
{
	private final static Logger LOGGER = Logger.getLogger(TradeController.class);
	private static final String SELECT_LOCATIONS = "SELECT loc_id, loc_x, loc_y, loc_zmin, loc_zmax, proc FROM `locations`";
	private static Map<Integer, L2Territory> territory = new HashMap<>();
	
	public static TerritoryTable getInstance()
	{
		return SingletonHolder.instance;
	}
	
	public TerritoryTable()
	{
		territory.clear();
		// load all data at server start
		reload_data();
	}
	
	public int[] getRandomPoint(final Integer terr)
	{
		return territory.get(terr).getRandomPoint();
	}
	
	public int getProcMax(final Integer terr)
	{
		return territory.get(terr).getProcMax();
	}
	
	public void reload_data()
	{
		territory.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_LOCATIONS);
			ResultSet rset = statement.executeQuery();)
		{
			while (rset.next())
			{
				int terr = rset.getInt("loc_id");
				
				if (territory.get(terr) == null)
				{
					L2Territory t = new L2Territory();
					territory.put(terr, t);
				}
				territory.get(terr).add(rset.getInt("loc_x"), rset.getInt("loc_y"), rset.getInt("loc_zmin"), rset.getInt("loc_zmax"), rset.getInt("proc"));
			}
		}
		catch (Exception e)
		{
			LOGGER.error("TerritoryTable.reload_data : Could not be initialized ", e);
		}
		
		LOGGER.info("TerritoryTable: Loaded {} locations " + territory.size());
	}
	
	private static class SingletonHolder
	{
		protected static final TerritoryTable instance = new TerritoryTable();
	}
}
