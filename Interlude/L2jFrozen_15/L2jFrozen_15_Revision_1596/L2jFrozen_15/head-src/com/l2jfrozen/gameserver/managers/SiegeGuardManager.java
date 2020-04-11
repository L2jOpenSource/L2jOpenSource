package com.l2jfrozen.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class SiegeGuardManager
{
	private static Logger LOGGER = Logger.getLogger(SiegeGuardManager.class);
	private static final String DELETE_CASTLE_SIEGE_GUARD = "DELETE FROM castle_siege_guards WHERE npcId = ? AND x = ? AND y = ? AND z = ? AND isHired = 1";
	private static final String INSERT_CASTLE_SIEGE_GUARD = "INSERT INTO castle_siege_guards (castleId, npcId, x, y, z, heading, respawnDelay, isHired) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String SELECT_CASTLE_SIEGE_GUARD = "SELECT castleId,id,npcId,x,y,z,heading,respawnDelay,isHired FROM castle_siege_guards WHERE castleId = ? AND isHired=?";
	private static final String DELETE_CASTLE_SIEGE_GUARD_HIRED_BY_CASTLE_ID = "DELETE FROM castle_siege_guards WHERE castleId=? AND isHired=1";
	
	private final Castle castle;
	private final List<L2Spawn> siegeGuardSpawn = new ArrayList<>();
	
	public SiegeGuardManager(final Castle castle)
	{
		this.castle = castle;
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
	 * Remove a single mercenary, identified by the npcId and location. Presumably, this is used when a castle lord picks up a previously dropped ticket
	 * @param npcId
	 * @param x
	 * @param y
	 * @param z
	 */
	public void removeMerc(int npcId, int x, int y, int z)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_CASTLE_SIEGE_GUARD))
		{
			statement.setInt(1, npcId);
			statement.setInt(2, x);
			statement.setInt(3, y);
			statement.setInt(4, z);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("SiegeGuardManager.removeMerc : Error deleting hired siege guard", e);
		}
	}
	
	public void removeMercs()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_CASTLE_SIEGE_GUARD_HIRED_BY_CASTLE_ID))
		{
			statement.setInt(1, getCastle().getCastleId());
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("SiegeGuardManager.removeMercs : Error deleting hired siege guard for castle " + getCastle().getName(), e);
		}
	}
	
	/**
	 * Spawn guards.<BR>
	 * <BR>
	 */
	public void spawnSiegeGuard()
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
			PreparedStatement statement = con.prepareStatement(SELECT_CASTLE_SIEGE_GUARD))
		{
			statement.setInt(1, getCastle().getCastleId());
			statement.setInt(2, getCastle().getOwnerId() > 0 ? 1 : 0);
			statement.setInt(2, 0);
			
			try (ResultSet rs = statement.executeQuery())
			{
				L2NpcTemplate template1;
				
				while (rs.next())
				{
					template1 = NpcTable.getInstance().getTemplate(rs.getInt("npcId"));
					if (template1 != null)
					{
						L2Spawn spawn1;
						spawn1 = new L2Spawn(template1);
						spawn1.setId(rs.getInt("id"));
						spawn1.setAmount(1);
						spawn1.setLocx(rs.getInt("x"));
						spawn1.setLocy(rs.getInt("y"));
						spawn1.setLocz(rs.getInt("z"));
						spawn1.setHeading(rs.getInt("heading"));
						spawn1.setRespawnDelay(rs.getInt("respawnDelay"));
						spawn1.setLocation(0);
						siegeGuardSpawn.add(spawn1);
						spawn1 = null;
					}
					else
					{
						LOGGER.warn("Missing npc data in npc table for id: " + rs.getInt("npcId"));
					}
					template1 = null;
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("SiegeGuardManager.loadSiegeGuard : Error loading siege guard for castle " + getCastle().getName(), e);
		}
	}
	
	private void saveSiegeGuard(int x, int y, int z, int heading, int npcId, int isHire)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_CASTLE_SIEGE_GUARD))
		{
			statement.setInt(1, getCastle().getCastleId());
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
			LOGGER.error("SiegeGuardManager.saveSiegeGuard : Could not insert siege guard in db", e);
		}
	}
	
	public final Castle getCastle()
	{
		return castle;
	}
	
	public final List<L2Spawn> getSiegeGuardSpawn()
	{
		return siegeGuardSpawn;
	}
}
