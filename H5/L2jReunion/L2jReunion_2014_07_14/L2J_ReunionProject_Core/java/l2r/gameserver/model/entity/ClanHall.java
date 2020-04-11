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
package l2r.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Map;

import javolution.util.FastMap;
import l2r.L2DatabaseFactory;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.datatables.sql.ClanTable;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.actor.instance.L2DoorInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.itemcontainer.Inventory;
import l2r.gameserver.model.zone.type.L2ClanHallZone;
import l2r.gameserver.network.serverpackets.PledgeShowInfoUpdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ClanHall
{
	protected static final Logger _log = LoggerFactory.getLogger(ClanHall.class);
	
	private final int _clanHallId;
	private ArrayList<L2DoorInstance> _doors;
	private final String _name;
	private int _ownerId;
	private final String _desc;
	private final String _location;
	private L2ClanHallZone _zone;
	protected final int _chRate = 604800000;
	protected boolean _isFree = true;
	private final Map<Integer, ClanHallFunction> _functions;
	
	/** Clan Hall Functions */
	public static final int FUNC_TELEPORT = 1;
	public static final int FUNC_ITEM_CREATE = 2;
	public static final int FUNC_RESTORE_HP = 3;
	public static final int FUNC_RESTORE_MP = 4;
	public static final int FUNC_RESTORE_EXP = 5;
	public static final int FUNC_SUPPORT = 6;
	public static final int FUNC_DECO_FRONTPLATEFORM = 7; // Only Auctionable Halls
	public static final int FUNC_DECO_CURTAINS = 8; // Only Auctionable Halls
	
	public class ClanHallFunction
	{
		private final int _type;
		private int _lvl;
		protected int _fee;
		protected int _tempFee;
		private final long _rate;
		private long _endDate;
		protected boolean _inDebt;
		public boolean _cwh; // first activating clanhall function is payed from player inventory, any others from clan warehouse
		
		public ClanHallFunction(int type, int lvl, int lease, int tempLease, long rate, long time, boolean cwh)
		{
			_type = type;
			_lvl = lvl;
			_fee = lease;
			_tempFee = tempLease;
			_rate = rate;
			_endDate = time;
			initializeTask(cwh);
		}
		
		public int getType()
		{
			return _type;
		}
		
		public int getLvl()
		{
			return _lvl;
		}
		
		public int getLease()
		{
			return _fee;
		}
		
		public long getRate()
		{
			return _rate;
		}
		
		public long getEndTime()
		{
			return _endDate;
		}
		
		public void setLvl(int lvl)
		{
			_lvl = lvl;
		}
		
		public void setLease(int lease)
		{
			_fee = lease;
		}
		
		public void setEndTime(long time)
		{
			_endDate = time;
		}
		
		private void initializeTask(boolean cwh)
		{
			if (_isFree)
			{
				return;
			}
			long currentTime = System.currentTimeMillis();
			if (_endDate > currentTime)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(cwh), _endDate - currentTime);
			}
			else
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(cwh), 0);
			}
		}
		
		private class FunctionTask implements Runnable
		{
			public FunctionTask(boolean cwh)
			{
				_cwh = cwh;
			}
			
			@Override
			public void run()
			{
				try
				{
					if (_isFree)
					{
						return;
					}
					if ((ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= _fee) || !_cwh)
					{
						int fee = _fee;
						if (getEndTime() == -1)
						{
							fee = _tempFee;
						}
						
						setEndTime(System.currentTimeMillis() + getRate());
						dbSave();
						if (_cwh)
						{
							ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().destroyItemByItemId("CH_function_fee", Inventory.ADENA_ID, fee, null, null);
						}
						ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(true), getRate());
					}
					else
					{
						removeFunction(getType());
					}
				}
				catch (Exception e)
				{
					_log.error("", e);
				}
			}
		}
		
		public void dbSave()
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement("REPLACE INTO clanhall_functions (hall_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)"))
			{
				ps.setInt(1, getId());
				ps.setInt(2, getType());
				ps.setInt(3, getLvl());
				ps.setInt(4, getLease());
				ps.setLong(5, getRate());
				ps.setLong(6, getEndTime());
				ps.execute();
			}
			catch (Exception e)
			{
				_log.error("Exception: ClanHall.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " + e.getMessage(), e);
			}
		}
	}
	
	public ClanHall(StatsSet set)
	{
		_clanHallId = set.getInteger("id");
		_name = set.getString("name");
		_ownerId = set.getInteger("ownerId");
		_desc = set.getString("desc");
		_location = set.getString("location");
		_functions = new FastMap<>();
		
		if (_ownerId > 0)
		{
			L2Clan clan = ClanTable.getInstance().getClan(_ownerId);
			if (clan != null)
			{
				clan.setHideoutId(getId());
			}
			else
			{
				free();
			}
		}
	}
	
	/**
	 * @return Id Of Clan hall
	 */
	public final int getId()
	{
		return _clanHallId;
	}
	
	/**
	 * @return the Clan Hall name.
	 */
	public final String getName()
	{
		return _name;
	}
	
	/**
	 * @return OwnerId
	 */
	public final int getOwnerId()
	{
		return _ownerId;
	}
	
	/**
	 * @return Desc
	 */
	public final String getDesc()
	{
		return _desc;
	}
	
	/**
	 * @return Location
	 */
	public final String getLocation()
	{
		return _location;
	}
	
	/**
	 * @return all DoorInstance
	 */
	public final ArrayList<L2DoorInstance> getDoors()
	{
		if (_doors == null)
		{
			_doors = new ArrayList<>();
		}
		return _doors;
	}
	
	/**
	 * @param doorId
	 * @return Door
	 */
	public final L2DoorInstance getDoor(int doorId)
	{
		if (doorId <= 0)
		{
			return null;
		}
		for (L2DoorInstance door : getDoors())
		{
			if (door.getId() == doorId)
			{
				return door;
			}
		}
		return null;
	}
	
	/**
	 * @param type
	 * @return function with id
	 */
	public ClanHallFunction getFunction(int type)
	{
		if (_functions.get(type) != null)
		{
			return _functions.get(type);
		}
		return null;
	}
	
	/**
	 * Sets this clan halls zone
	 * @param zone
	 */
	public void setZone(L2ClanHallZone zone)
	{
		_zone = zone;
	}
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return true if object is inside the zone
	 */
	public boolean checkIfInZone(int x, int y, int z)
	{
		return getZone().isInsideZone(x, y, z);
	}
	
	/**
	 * @return the zone of this clan hall
	 */
	public L2ClanHallZone getZone()
	{
		return _zone;
	}
	
	/** Free this clan hall */
	public void free()
	{
		_ownerId = 0;
		_isFree = true;
		for (Integer fc : _functions.keySet())
		{
			removeFunction(fc);
		}
		_functions.clear();
		updateDb();
	}
	
	/**
	 * Set owner if clan hall is free
	 * @param clan
	 */
	public void setOwner(L2Clan clan)
	{
		// Verify that this ClanHall is Free and Clan isn't null
		if ((_ownerId > 0) || (clan == null))
		{
			return;
		}
		_ownerId = clan.getId();
		_isFree = false;
		clan.setHideoutId(getId());
		// Announce to Online member new ClanHall
		clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
		updateDb();
	}
	
	/**
	 * Open or Close Door
	 * @param activeChar
	 * @param doorId
	 * @param open
	 */
	public void openCloseDoor(L2PcInstance activeChar, int doorId, boolean open)
	{
		if ((activeChar != null) && (activeChar.getClanId() == getOwnerId()))
		{
			openCloseDoor(doorId, open);
		}
	}
	
	public void openCloseDoor(int doorId, boolean open)
	{
		openCloseDoor(getDoor(doorId), open);
	}
	
	public void openCloseDoor(L2DoorInstance door, boolean open)
	{
		if (door != null)
		{
			if (open)
			{
				door.openMe();
			}
			else
			{
				door.closeMe();
			}
		}
	}
	
	public void openCloseDoors(L2PcInstance activeChar, boolean open)
	{
		if ((activeChar != null) && (activeChar.getClanId() == getOwnerId()))
		{
			openCloseDoors(open);
		}
	}
	
	public void openCloseDoors(boolean open)
	{
		for (L2DoorInstance door : getDoors())
		{
			if (door != null)
			{
				if (open)
				{
					door.openMe();
				}
				else
				{
					door.closeMe();
				}
			}
		}
	}
	
	/** Banish Foreigner */
	public void banishForeigners()
	{
		if (_zone != null)
		{
			_zone.banishForeigners(getOwnerId());
		}
		else
		{
			_log.warn(getClass().getSimpleName() + ": Zone is null for clan hall: " + getId() + " " + getName());
		}
	}
	
	/** Load All Functions */
	protected void loadFunctions()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM clanhall_functions WHERE hall_id = ?"))
		{
			ps.setInt(1, getId());
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					_functions.put(rs.getInt("type"), new ClanHallFunction(rs.getInt("type"), rs.getInt("lvl"), rs.getInt("lease"), 0, rs.getLong("rate"), rs.getLong("endTime"), true));
				}
			}
		}
		catch (Exception e)
		{
			_log.error("Exception: ClanHall.loadFunctions(): " + e.getMessage(), e);
		}
	}
	
	/**
	 * Remove function In List and in DB
	 * @param functionType
	 */
	public void removeFunction(int functionType)
	{
		_functions.remove(functionType);
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM clanhall_functions WHERE hall_id=? AND type=?"))
		{
			ps.setInt(1, getId());
			ps.setInt(2, functionType);
			ps.execute();
		}
		catch (Exception e)
		{
			_log.error("Exception: ClanHall.removeFunctions(int functionType): " + e.getMessage(), e);
		}
	}
	
	public boolean updateFunctions(L2PcInstance player, int type, int lvl, int lease, long rate, boolean addNew)
	{
		if (player == null)
		{
			return false;
		}
		
		if (lease > 0)
		{
			if (!player.destroyItemByItemId("Consume", Inventory.ADENA_ID, lease, null, true))
			{
				return false;
			}
		}
		if (addNew)
		{
			_functions.put(type, new ClanHallFunction(type, lvl, lease, 0, rate, 0, false));
		}
		else
		{
			if ((lvl == 0) && (lease == 0))
			{
				removeFunction(type);
			}
			else
			{
				int diffLease = lease - _functions.get(type).getLease();
				if (diffLease > 0)
				{
					_functions.remove(type);
					_functions.put(type, new ClanHallFunction(type, lvl, lease, 0, rate, -1, false));
				}
				else
				{
					_functions.get(type).setLease(lease);
					_functions.get(type).setLvl(lvl);
					_functions.get(type).dbSave();
				}
			}
		}
		return true;
	}
	
	public int getGrade()
	{
		return 0;
	}
	
	public long getPaidUntil()
	{
		return 0;
	}
	
	public int getLease()
	{
		return 0;
	}
	
	public boolean isSiegableHall()
	{
		return false;
	}
	
	public boolean isFree()
	{
		return _isFree;
	}
	
	public abstract void updateDb();
}
