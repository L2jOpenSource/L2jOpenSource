package com.l2jfrozen.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.entity.siege.Fort;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author programmos, scoria dev
 */

public class FortManager
{
	protected static final Logger LOGGER = Logger.getLogger(FortManager.class);
	private static final String SELECT_FORTRESS_ID = "SELECT id FROM fort ORDER BY id";
	
	public static final FortManager getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private final List<Fort> forts = new ArrayList<>();
	
	public FortManager()
	{
		forts.clear();
		load();
	}
	
	public final int findNearestFortIndex(final L2Object obj)
	{
		int index = getFortIndex(obj);
		if (index < 0)
		{
			double closestDistance = 99999999;
			double distance;
			Fort fort;
			
			for (int i = 0; i < getForts().size(); i++)
			{
				fort = getForts().get(i);
				
				if (fort == null)
				{
					continue;
				}
				
				distance = fort.getDistance(obj);
				
				if (closestDistance > distance)
				{
					closestDistance = distance;
				}
				index = i;
			}
		}
		return index;
	}
	
	private final void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_FORTRESS_ID);
			ResultSet rs = statement.executeQuery())
		{
			while (rs.next())
			{
				getForts().add(new Fort(rs.getInt("id"))); // Fortress name will be given in Fort class constructor
			}
			
			LOGGER.info("Loaded: " + getForts().size() + " fortress");
		}
		catch (Exception e)
		{
			LOGGER.error("FortManager.loadFortData : Could not select from fort table", e);
		}
		
	}
	
	public final Fort getFortById(final int fortId)
	{
		for (final Fort f : getForts())
		{
			if (f.getFortId() == fortId)
			{
				return f;
			}
		}
		return null;
	}
	
	public final Fort getFortByOwner(final L2Clan clan)
	{
		for (final Fort f : getForts())
		{
			if (f.getOwnerId() == clan.getClanId())
			{
				return f;
			}
		}
		return null;
	}
	
	public final Fort getFort(final String name)
	{
		for (final Fort f : getForts())
		{
			if (f.getName().equalsIgnoreCase(name.trim()))
			{
				return f;
			}
		}
		return null;
	}
	
	public final Fort getFort(final int x, final int y, final int z)
	{
		for (final Fort f : getForts())
		{
			if (f.checkIfInZone(x, y, z))
			{
				return f;
			}
		}
		return null;
	}
	
	public final Fort getFort(final L2Object activeObject)
	{
		return getFort(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public final int getFortIndex(final int fortId)
	{
		Fort fort;
		for (int i = 0; i < getForts().size(); i++)
		{
			fort = getForts().get(i);
			if (fort != null && fort.getFortId() == fortId)
			{
				fort = null;
				return i;
			}
		}
		return -1;
	}
	
	public final int getFortIndex(final L2Object activeObject)
	{
		return getFortIndex(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public final int getFortIndex(final int x, final int y, final int z)
	{
		Fort fort;
		for (int i = 0; i < getForts().size(); i++)
		{
			fort = getForts().get(i);
			if (fort != null && fort.checkIfInZone(x, y, z))
			{
				fort = null;
				return i;
			}
		}
		return -1;
	}
	
	public final List<Fort> getForts()
	{
		return forts;
	}
	
	private static class SingletonHolder
	{
		protected static final FortManager instance = new FortManager();
	}
}
