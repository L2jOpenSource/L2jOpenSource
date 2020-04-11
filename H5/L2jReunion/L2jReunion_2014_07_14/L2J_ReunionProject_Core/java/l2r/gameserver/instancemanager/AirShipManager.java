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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import l2r.L2DatabaseFactory;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.VehiclePathPoint;
import l2r.gameserver.model.actor.instance.L2AirShipInstance;
import l2r.gameserver.model.actor.instance.L2ControllableAirShipInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.templates.L2CharTemplate;
import l2r.gameserver.network.serverpackets.ExAirShipTeleportList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AirShipManager
{
	private static final Logger _log = LoggerFactory.getLogger(AirShipManager.class);
	
	private static final String LOAD_DB = "SELECT * FROM airships";
	private static final String ADD_DB = "INSERT INTO airships (owner_id,fuel) VALUES (?,?)";
	private static final String UPDATE_DB = "UPDATE airships SET fuel=? WHERE owner_id=?";
	
	private L2CharTemplate _airShipTemplate = null;
	private final Map<Integer, StatsSet> _airShipsInfo = new HashMap<>();
	private final Map<Integer, L2AirShipInstance> _airShips = new HashMap<>();
	private final Map<Integer, AirShipTeleportList> _teleports = new HashMap<>();
	
	protected AirShipManager()
	{
		StatsSet npcDat = new StatsSet();
		npcDat.set("npcId", 9);
		npcDat.set("level", 0);
		npcDat.set("jClass", "boat");
		
		npcDat.set("baseSTR", 0);
		npcDat.set("baseCON", 0);
		npcDat.set("baseDEX", 0);
		npcDat.set("baseINT", 0);
		npcDat.set("baseWIT", 0);
		npcDat.set("baseMEN", 0);
		
		npcDat.set("baseShldDef", 0);
		npcDat.set("baseShldRate", 0);
		npcDat.set("baseAccCombat", 38);
		npcDat.set("baseEvasRate", 38);
		npcDat.set("baseCritRate", 38);
		
		npcDat.set("collision_radius", 0);
		npcDat.set("collision_height", 0);
		npcDat.set("sex", "male");
		npcDat.set("type", "");
		npcDat.set("baseAtkRange", 0);
		npcDat.set("baseMpMax", 0);
		npcDat.set("baseCpMax", 0);
		npcDat.set("rewardExp", 0);
		npcDat.set("rewardSp", 0);
		npcDat.set("basePAtk", 0);
		npcDat.set("baseMAtk", 0);
		npcDat.set("basePAtkSpd", 0);
		npcDat.set("aggroRange", 0);
		npcDat.set("baseMAtkSpd", 0);
		npcDat.set("rhand", 0);
		npcDat.set("lhand", 0);
		npcDat.set("armor", 0);
		npcDat.set("baseWalkSpd", 0);
		npcDat.set("baseRunSpd", 0);
		npcDat.set("name", "AirShip");
		npcDat.set("baseHpMax", 50000);
		npcDat.set("baseHpReg", 3.e-3f);
		npcDat.set("baseMpReg", 3.e-3f);
		npcDat.set("basePDef", 100);
		npcDat.set("baseMDef", 100);
		_airShipTemplate = new L2CharTemplate(npcDat);
		
		load();
	}
	
	public L2AirShipInstance getNewAirShip(int x, int y, int z, int heading)
	{
		final L2AirShipInstance airShip = new L2AirShipInstance(IdFactory.getInstance().getNextId(), _airShipTemplate);
		
		airShip.setHeading(heading);
		airShip.setXYZInvisible(x, y, z);
		airShip.spawnMe();
		airShip.getStat().setMoveSpeed(280);
		airShip.getStat().setRotationSpeed(2000);
		return airShip;
	}
	
	public L2AirShipInstance getNewAirShip(int x, int y, int z, int heading, int ownerId)
	{
		final StatsSet info = _airShipsInfo.get(ownerId);
		if (info == null)
		{
			return null;
		}
		
		final L2AirShipInstance airShip;
		if (_airShips.containsKey(ownerId))
		{
			airShip = _airShips.get(ownerId);
			airShip.refreshID();
		}
		else
		{
			airShip = new L2ControllableAirShipInstance(IdFactory.getInstance().getNextId(), _airShipTemplate, ownerId);
			_airShips.put(ownerId, airShip);
			
			airShip.setMaxFuel(600);
			airShip.setFuel(info.getInteger("fuel"));
			airShip.getStat().setMoveSpeed(280);
			airShip.getStat().setRotationSpeed(2000);
		}
		
		airShip.setHeading(heading);
		airShip.setXYZInvisible(x, y, z);
		airShip.spawnMe();
		return airShip;
	}
	
	public void removeAirShip(L2AirShipInstance ship)
	{
		if (ship.getOwnerId() != 0)
		{
			storeInDb(ship.getOwnerId());
			final StatsSet info = _airShipsInfo.get(ship.getOwnerId());
			if (info != null)
			{
				info.set("fuel", ship.getFuel());
			}
		}
	}
	
	public boolean hasAirShipLicense(int ownerId)
	{
		return _airShipsInfo.containsKey(ownerId);
	}
	
	public void registerLicense(int ownerId)
	{
		if (!_airShipsInfo.containsKey(ownerId))
		{
			final StatsSet info = new StatsSet();
			info.set("fuel", 600);
			
			_airShipsInfo.put(ownerId, info);
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(ADD_DB))
			{
				ps.setInt(1, ownerId);
				ps.setInt(2, info.getInteger("fuel"));
				ps.executeUpdate();
			}
			catch (SQLException e)
			{
				_log.warn(getClass().getSimpleName() + ": Could not add new airship license: " + e.getMessage(), e);
			}
			catch (Exception e)
			{
				_log.warn(getClass().getSimpleName() + ": Error while initializing: " + e.getMessage(), e);
			}
		}
	}
	
	public boolean hasAirShip(int ownerId)
	{
		final L2AirShipInstance ship = _airShips.get(ownerId);
		if ((ship == null) || !(ship.isVisible() || ship.isTeleporting()))
		{
			return false;
		}
		
		return true;
	}
	
	public void registerAirShipTeleportList(int dockId, int locationId, VehiclePathPoint[][] tp, int[] fuelConsumption)
	{
		if (tp.length != fuelConsumption.length)
		{
			return;
		}
		
		_teleports.put(dockId, new AirShipTeleportList(locationId, fuelConsumption, tp));
	}
	
	public void sendAirShipTeleportList(L2PcInstance player)
	{
		if ((player == null) || !player.isInAirShip())
		{
			return;
		}
		
		final L2AirShipInstance ship = player.getAirShip();
		if (!ship.isCaptain(player) || !ship.isInDock() || ship.isMoving())
		{
			return;
		}
		
		int dockId = ship.getDockId();
		if (!_teleports.containsKey(dockId))
		{
			return;
		}
		
		final AirShipTeleportList all = _teleports.get(dockId);
		player.sendPacket(new ExAirShipTeleportList(all.location, all.routes, all.fuel));
	}
	
	public VehiclePathPoint[] getTeleportDestination(int dockId, int index)
	{
		final AirShipTeleportList all = _teleports.get(dockId);
		if (all == null)
		{
			return null;
		}
		
		if ((index < -1) || (index >= all.routes.length))
		{
			return null;
		}
		
		return all.routes[index + 1];
	}
	
	public int getFuelConsumption(int dockId, int index)
	{
		final AirShipTeleportList all = _teleports.get(dockId);
		if (all == null)
		{
			return 0;
		}
		
		if ((index < -1) || (index >= all.fuel.length))
		{
			return 0;
		}
		
		return all.fuel[index + 1];
	}
	
	private void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery(LOAD_DB))
		{
			StatsSet info;
			while (rs.next())
			{
				info = new StatsSet();
				info.set("fuel", rs.getInt("fuel"));
				_airShipsInfo.put(rs.getInt("owner_id"), info);
			}
		}
		catch (SQLException e)
		{
			_log.warn(getClass().getSimpleName() + ": Could not load airships table: " + e.getMessage(), e);
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": Error while initializing: " + e.getMessage(), e);
		}
		_log.info(getClass().getSimpleName() + ": Loaded " + _airShipsInfo.size() + " private airships");
	}
	
	private void storeInDb(int ownerId)
	{
		StatsSet info = _airShipsInfo.get(ownerId);
		if (info == null)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_DB))
		{
			ps.setInt(1, info.getInteger("fuel"));
			ps.setInt(2, ownerId);
			ps.executeUpdate();
		}
		catch (SQLException e)
		{
			_log.warn(getClass().getSimpleName() + ": Could not update airships table: " + e.getMessage(), e);
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": Error while save: " + e.getMessage(), e);
		}
	}
	
	private static class AirShipTeleportList
	{
		public int location;
		public int[] fuel;
		public VehiclePathPoint[][] routes;
		
		public AirShipTeleportList(int loc, int[] f, VehiclePathPoint[][] r)
		{
			location = loc;
			fuel = f;
			routes = r;
		}
	}
	
	public static final AirShipManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AirShipManager _instance = new AirShipManager();
	}
}