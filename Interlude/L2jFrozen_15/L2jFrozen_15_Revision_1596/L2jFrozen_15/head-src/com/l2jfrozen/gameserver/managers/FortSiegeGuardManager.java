package com.l2jfrozen.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.siege.Fort;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author programmos, scoria dev
 */

public class FortSiegeGuardManager
{
	private static final Logger LOGGER = Logger.getLogger(FortSiegeGuardManager.class);
	private static final String DELETE_FORTRESS_SIEGE_GUARD = "DELETE FROM fort_siege_guards WHERE npcId = ? AND x = ? AND y = ? AND z = ? AND isHired=1";
	private static final String DELETE_FORTRESS_SIEGE_GUARD_BY_FORTRESS_ID = "DELETE FROM fort_siege_guards WHERE fortId = ? AND isHired=1";
	private static final String SELECT_FORTRESS_SIEGE_GUARD_BY_FORTRESS_ID = "SELECT fortId,id,npcId,x,y,z,heading,respawnDelay,isHired FROM fort_siege_guards WHERE fortId=?";
	private static final String INSERT_FORTRESS_SIEGE_GUARD = "INSERT INTO fort_siege_guards (fortId, npcId, x, y, z, heading, respawnDelay, isHired) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	
	private final Fort fort;
	private final List<L2Spawn> siegeGuardSpawn = new ArrayList<>();
	
	public FortSiegeGuardManager(final Fort fort)
	{
		this.fort = fort;
	}
	
	/**
	 * Add guard.<BR>
	 * <BR>
	 * @param activeChar
	 * @param npcId
	 */
	public void addSiegeGuard(final L2PcInstance activeChar, final int npcId)
	{
		if (activeChar == null)
		{
			return;
		}
		
		addSiegeGuard(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading(), npcId);
	}
	
	/**
	 * Add guard.<BR>
	 * <BR>
	 * @param x
	 * @param y
	 * @param z
	 * @param heading
	 * @param npcId
	 */
	public void addSiegeGuard(final int x, final int y, final int z, final int heading, final int npcId)
	{
		saveSiegeGuard(x, y, z, heading, npcId, 0);
	}
	
	/**
	 * Hire merc.<BR>
	 * <BR>
	 * @param activeChar
	 * @param npcId
	 */
	public void hireMerc(final L2PcInstance activeChar, final int npcId)
	{
		if (activeChar == null)
		{
			return;
		}
		
		hireMerc(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading(), npcId);
	}
	
	/**
	 * Hire merc.<BR>
	 * <BR>
	 * @param x
	 * @param y
	 * @param z
	 * @param heading
	 * @param npcId
	 */
	public void hireMerc(final int x, final int y, final int z, final int heading, final int npcId)
	{
		saveSiegeGuard(x, y, z, heading, npcId, 1);
	}
	
	/**
	 * Remove a single mercenary, identified by the npcId and location. Presumably, this is used when a fort lord picks up a previously dropped ticket
	 * @param npcId
	 * @param x
	 * @param y
	 * @param z
	 */
	public void removeMerc(int npcId, int x, int y, int z)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_FORTRESS_SIEGE_GUARD))
		{
			statement.setInt(1, npcId);
			statement.setInt(2, x);
			statement.setInt(3, y);
			statement.setInt(4, z);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("FortSiegeGuardManager.removeMerc : Could not delete from fort_siege_guards table", e);
		}
	}
	
	public void removeMercs()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_FORTRESS_SIEGE_GUARD_BY_FORTRESS_ID))
		{
			statement.setInt(1, getFort().getFortId());
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("FortSiegeGuardManager.removeMercs : Could not delete by fortress id from fort_siege_guards table", e);
		}
	}
	
	public void spawnSiegeGuard()
	{
		try
		{
			loadSiegeGuard();
			for (final L2Spawn spawn : getSiegeGuardSpawn())
			{
				if (spawn != null)
				{
					spawn.init();
				}
			}
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				t.printStackTrace();
			}
			
			LOGGER.warn("Error spawning siege guards for fort " + getFort().getName() + ":" + t.toString());
		}
	}
	
	/**
	 * Unspawn guards.<BR>
	 * <BR>
	 */
	public void unspawnSiegeGuard()
	{
		for (final L2Spawn spawn : getSiegeGuardSpawn())
		{
			if (spawn == null)
			{
				continue;
			}
			
			spawn.stopRespawn();
			spawn.getLastSpawn().doDie(spawn.getLastSpawn());
		}
		
		getSiegeGuardSpawn().clear();
	}
	
	private void loadSiegeGuard()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_FORTRESS_SIEGE_GUARD_BY_FORTRESS_ID))
		{
			statement.setInt(1, getFort().getFortId());
			
			try (ResultSet rs = statement.executeQuery())
			{
				L2Spawn spawn;
				L2NpcTemplate template;
				
				while (rs.next())
				{
					template = NpcTable.getInstance().getTemplate(rs.getInt("npcId"));
					if (template != null)
					{
						spawn = new L2Spawn(template);
						spawn.setId(rs.getInt("id"));
						spawn.setAmount(1);
						spawn.setLocx(rs.getInt("x"));
						spawn.setLocy(rs.getInt("y"));
						spawn.setLocz(rs.getInt("z"));
						spawn.setHeading(rs.getInt("heading"));
						spawn.setRespawnDelay(rs.getInt("respawnDelay"));
						spawn.setLocation(0);
						siegeGuardSpawn.add(spawn);
					}
					else
					{
						LOGGER.warn("Missing npc data in npc table for id: " + rs.getInt("npcId"));
					}
					template = null;
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("FortSiegeGuardManager.loadSiegeGuard : Error loading siege guard for fort " + getFort().getName(), e);
		}
	}
	
	private void saveSiegeGuard(int x, int y, int z, int heading, int npcId, int isHire)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_FORTRESS_SIEGE_GUARD))
		{
			statement.setInt(1, getFort().getFortId());
			statement.setInt(2, npcId);
			statement.setInt(3, x);
			statement.setInt(4, y);
			statement.setInt(5, z);
			statement.setInt(6, heading);
			statement.setInt(7, isHire == 1 ? 0 : 600);
			statement.setInt(8, isHire);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("FortSiegeGuardManager.saveSiegeGuard : Error adding siege guard for fort " + getFort().getName(), e);
		}
	}
	
	public final Fort getFort()
	{
		return fort;
	}
	
	public final List<L2Spawn> getSiegeGuardSpawn()
	{
		return siegeGuardSpawn;
	}
}
