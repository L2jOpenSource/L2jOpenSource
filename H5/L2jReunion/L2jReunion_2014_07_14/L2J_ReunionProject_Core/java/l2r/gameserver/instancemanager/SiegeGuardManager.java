/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javolution.util.FastList;
import l2r.L2DatabaseFactory;
import l2r.gameserver.datatables.sql.NpcTable;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.entity.Castle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiegeGuardManager
{
	private static Logger _log = LoggerFactory.getLogger(SiegeGuardManager.class);
	
	private final Castle _castle;
	private final List<L2Spawn> _siegeGuardSpawn = new FastList<>();
	
	public SiegeGuardManager(Castle castle)
	{
		_castle = castle;
	}
	
	/**
	 * Add guard.
	 * @param activeChar
	 * @param npcId
	 */
	public void addSiegeGuard(L2PcInstance activeChar, int npcId)
	{
		if (activeChar == null)
		{
			return;
		}
		addSiegeGuard(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading(), npcId);
	}
	
	/**
	 * Add guard.
	 * @param x
	 * @param y
	 * @param z
	 * @param heading
	 * @param npcId
	 */
	public void addSiegeGuard(int x, int y, int z, int heading, int npcId)
	{
		saveSiegeGuard(x, y, z, heading, npcId, 0);
	}
	
	/**
	 * Hire merc.
	 * @param activeChar
	 * @param npcId
	 */
	public void hireMerc(L2PcInstance activeChar, int npcId)
	{
		if (activeChar == null)
		{
			return;
		}
		hireMerc(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading(), npcId);
	}
	
	/**
	 * Hire merc.
	 * @param x
	 * @param y
	 * @param z
	 * @param heading
	 * @param npcId
	 */
	public void hireMerc(int x, int y, int z, int heading, int npcId)
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
			PreparedStatement ps = con.prepareStatement("Delete From castle_siege_guards Where npcId = ? And x = ? AND y = ? AND z = ? AND isHired = 1"))
		{
			ps.setInt(1, npcId);
			ps.setInt(2, x);
			ps.setInt(3, y);
			ps.setInt(4, z);
			ps.execute();
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": Error deleting hired siege guard at " + x + ',' + y + ',' + z + ": " + e.getMessage(), e);
		}
	}
	
	/**
	 * Remove mercs.
	 */
	public void removeMercs()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("Delete From castle_siege_guards Where castleId = ? And isHired = 1"))
		{
			ps.setInt(1, getCastle().getResidenceId());
			ps.execute();
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": Error deleting hired siege guard for castle " + getCastle().getName() + ": " + e.getMessage(), e);
		}
	}
	
	/**
	 * Spawn guards.
	 */
	public void spawnSiegeGuard()
	{
		try
		{
			int hiredCount = 0, hiredMax = MercTicketManager.getInstance().getMaxAllowedMerc(_castle.getResidenceId());
			boolean isHired = (getCastle().getOwnerId() > 0) ? true : false;
			loadSiegeGuard();
			for (L2Spawn spawn : getSiegeGuardSpawn())
			{
				if (spawn != null)
				{
					spawn.init();
					if (isHired)
					{
						spawn.stopRespawn();
						if (++hiredCount > hiredMax)
						{
							return;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.error(getClass().getSimpleName() + ": Error spawning siege guards for castle " + getCastle().getName(), e);
		}
	}
	
	/**
	 * Unspawn guards.
	 */
	public void unspawnSiegeGuard()
	{
		for (L2Spawn spawn : getSiegeGuardSpawn())
		{
			if ((spawn != null) && (spawn.getLastSpawn() != null))
			{
				spawn.stopRespawn();
				spawn.getLastSpawn().doDie(spawn.getLastSpawn());
			}
		}
		
		getSiegeGuardSpawn().clear();
	}
	
	/**
	 * Load guards.
	 */
	private void loadSiegeGuard()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM castle_siege_guards Where castleId = ? And isHired = ?"))
		{
			ps.setInt(1, getCastle().getResidenceId());
			if (getCastle().getOwnerId() > 0)
			{
				ps.setInt(2, 1);
			}
			else
			{
				ps.setInt(2, 0);
			}
			try (ResultSet rs = ps.executeQuery())
			{
				L2Spawn spawn1;
				L2NpcTemplate template1;
				while (rs.next())
				{
					template1 = NpcTable.getInstance().getTemplate(rs.getInt("npcId"));
					if (template1 != null)
					{
						spawn1 = new L2Spawn(template1);
						spawn1.setAmount(1);
						spawn1.setX(rs.getInt("x"));
						spawn1.setY(rs.getInt("y"));
						spawn1.setZ(rs.getInt("z"));
						spawn1.setHeading(rs.getInt("heading"));
						spawn1.setRespawnDelay(rs.getInt("respawnDelay"));
						spawn1.setLocationId(0);
						
						_siegeGuardSpawn.add(spawn1);
					}
					else
					{
						_log.warn(getClass().getSimpleName() + ": Missing npc data in npc table for id: " + rs.getInt("npcId"));
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": Error loading siege guard for castle " + getCastle().getName() + ": " + e.getMessage(), e);
		}
	}
	
	/**
	 * Save guards.
	 * @param x
	 * @param y
	 * @param z
	 * @param heading
	 * @param npcId
	 * @param isHire
	 */
	private void saveSiegeGuard(int x, int y, int z, int heading, int npcId, int isHire)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("Insert Into castle_siege_guards (castleId, npcId, x, y, z, heading, respawnDelay, isHired) Values (?, ?, ?, ?, ?, ?, ?, ?)"))
		{
			statement.setInt(1, getCastle().getResidenceId());
			statement.setInt(2, npcId);
			statement.setInt(3, x);
			statement.setInt(4, y);
			statement.setInt(5, z);
			statement.setInt(6, heading);
			statement.setInt(7, (isHire == 1 ? 0 : 600));
			statement.setInt(8, isHire);
			statement.execute();
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": Error adding siege guard for castle " + getCastle().getName() + ": " + e.getMessage(), e);
		}
	}
	
	public final Castle getCastle()
	{
		return _castle;
	}
	
	public final List<L2Spawn> getSiegeGuardSpawn()
	{
		return _siegeGuardSpawn;
	}
}
