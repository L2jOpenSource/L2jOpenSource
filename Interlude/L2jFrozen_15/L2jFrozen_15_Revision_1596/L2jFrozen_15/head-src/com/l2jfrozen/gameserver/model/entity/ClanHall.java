package com.l2jfrozen.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.csv.DoorTable;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.managers.AuctionManager;
import com.l2jfrozen.gameserver.managers.ClanHallManager;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.zone.type.L2ClanHallZone;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class ClanHall
{
	protected static final Logger LOGGER = Logger.getLogger(ClanHall.class);
	private static final String UPDATE_CLAN_HALL_BY_ID = "UPDATE clanhall SET ownerId=?, paidUntil=?, paid=? WHERE id=?";
	private static final String DELETE_CLAN_HALL_FUNCTION_BY_CLAN_HALL_ID = "DELETE FROM clanhall_functions WHERE hall_id=? AND type=?";
	private static final String SELECT_CASTLE_DOOR_BY_CASTLE_ID = "SELECT castleId,id,name,x,y,z,range_xmin,range_ymin,range_zmin,range_xmax,range_ymax,range_zmax,hp,pDef,mDef FROM castle_door WHERE castleId=?";
	private static final String INSERT_CLAN_HALL_FUNCTION = "INSERT INTO clanhall_functions (hall_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)";
	private static final String UPDATE_CLAN_HALL_FUNCTION = "UPDATE clanhall_functions SET lvl=?, lease=?, endTime=? WHERE hall_id=? AND type=?";
	private static final String SELECT_CLAN_HALL_FUNCTIONS = "SELECT hall_id,type,lvl,lease,rate,endTime FROM clanhall_functions WHERE hall_id=?";
	
	private final int clanHallId;
	private final List<L2DoorInstance> doors = new ArrayList<>();
	private final List<String> doorDefault = new ArrayList<>();
	private final String name;
	private int clanHallownerId;
	private L2Clan ownerClan;
	private final int lease;
	private final String desc;
	private final String location;
	protected long paidUntil;
	private L2ClanHallZone zone;
	private final int grade;
	protected final int chRate = 604800000;
	protected boolean isFree = true;
	private final Map<Integer, ClanHallFunction> functions;
	protected boolean paid;
	public static final int FUNC_TELEPORT = 1;
	public static final int FUNC_ITEM_CREATE = 2;
	public static final int FUNC_RESTORE_HP = 3;
	public static final int FUNC_RESTORE_MP = 4;
	public static final int FUNC_RESTORE_EXP = 5;
	public static final int FUNC_SUPPORT = 6;
	public static final int FUNC_DECO_FRONTPLATEFORM = 7;
	public static final int FUNC_DECO_CURTAINS = 8;
	
	public class ClanHallFunction
	{
		private final int type;
		private int lvl;
		protected int chFee;
		protected int tempFee;
		private final long rate;
		private long endDate;
		protected boolean inDebt;
		
		public ClanHallFunction(final int type, final int lvl, final int lease, final int tempLease, final long rate, final long time)
		{
			this.type = type;
			this.lvl = lvl;
			chFee = lease;
			tempFee = tempLease;
			this.rate = rate;
			endDate = time;
			initializeTask();
		}
		
		public int getType()
		{
			return type;
		}
		
		public int getLvl()
		{
			return lvl;
		}
		
		public int getLease()
		{
			return chFee;
		}
		
		public long getRate()
		{
			return rate;
		}
		
		public long getEndTime()
		{
			return endDate;
		}
		
		public void setLvl(final int lvl)
		{
			this.lvl = lvl;
		}
		
		public void setLease(final int lease)
		{
			chFee = lease;
		}
		
		public void setEndTime(final long time)
		{
			endDate = time;
		}
		
		private void initializeTask()
		{
			if (isFree)
			{
				return;
			}
			
			final long currentTime = System.currentTimeMillis();
			
			if (endDate > currentTime)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(), endDate - currentTime);
			}
			else
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(), 0);
			}
		}
		
		private class FunctionTask implements Runnable
		{
			
			public FunctionTask()
			{
			}
			
			@Override
			public void run()
			{
				try
				{
					if (isFree)
					{
						return;
					}
					
					if (getOwnerClan().getWarehouse().getAdena() >= chFee)
					{
						int fee = chFee;
						boolean newfc = true;
						
						if (getEndTime() == 0 || getEndTime() == -1)
						{
							if (getEndTime() == -1)
							{
								newfc = false;
								fee = tempFee;
							}
						}
						else
						{
							newfc = false;
						}
						
						setEndTime(System.currentTimeMillis() + getRate());
						dbSave(newfc);
						getOwnerClan().getWarehouse().destroyItemByItemId("CH_function_fee", 57, fee, null, null);
						
						if (Config.DEBUG)
						{
							LOGGER.warn("deducted " + fee + " adena from " + getName() + " owner's cwh for function id : " + getType());
						}
						
						ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(), getRate());
					}
					else
					{
						removeFunction(getType());
					}
				}
				catch (final Throwable t)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						t.printStackTrace();
					}
				}
			}
		}
		
		public void dbSave(boolean newFunction)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();)
			{
				if (newFunction)
				{
					try (PreparedStatement statement = con.prepareStatement(INSERT_CLAN_HALL_FUNCTION))
					{
						statement.setInt(1, getId());
						statement.setInt(2, getType());
						statement.setInt(3, getLvl());
						statement.setInt(4, getLease());
						statement.setLong(5, getRate());
						statement.setLong(6, getEndTime());
						statement.executeUpdate();
					}
				}
				else
				{
					try (PreparedStatement statement = con.prepareStatement(UPDATE_CLAN_HALL_FUNCTION))
					{
						statement.setInt(1, getLvl());
						statement.setInt(2, getLease());
						statement.setLong(3, getEndTime());
						statement.setInt(4, getId());
						statement.setInt(5, getType());
						statement.executeUpdate();
					}
				}
			}
			catch (Exception e)
			{
				LOGGER.error(" ClanHall.updateFunctions : Could not insert or update clan hall function", e);
			}
		}
	}
	
	public ClanHall(final int clanHallId, final String name, final int ownerId, final int lease, final String desc, final String location, final long paidUntil, final int Grade, final boolean paid)
	{
		this.clanHallId = clanHallId;
		this.name = name;
		clanHallownerId = ownerId;
		
		if (Config.DEBUG)
		{
			LOGGER.warn("Init Owner : " + clanHallownerId);
		}
		
		this.lease = lease;
		this.desc = desc;
		this.location = location;
		this.paidUntil = paidUntil;
		grade = Grade;
		this.paid = paid;
		loadDoor();
		functions = new HashMap<>();
		
		if (ownerId != 0)
		{
			isFree = false;
			
			initialyzeTask(false);
			loadFunctions();
		}
	}
	
	public final boolean getPaid()
	{
		return paid;
	}
	
	public final int getId()
	{
		return clanHallId;
	}
	
	public final String getName()
	{
		return name;
	}
	
	public final int getOwnerId()
	{
		return clanHallownerId;
	}
	
	public final int getLease()
	{
		return lease;
	}
	
	public final String getDesc()
	{
		return desc;
	}
	
	public final String getLocation()
	{
		return location;
	}
	
	public final long getPaidUntil()
	{
		return paidUntil;
	}
	
	public final int getGrade()
	{
		return grade;
	}
	
	public final List<L2DoorInstance> getDoors()
	{
		return doors;
	}
	
	public final L2DoorInstance getDoor(final int doorId)
	{
		if (doorId <= 0)
		{
			return null;
		}
		
		for (int i = 0; i < getDoors().size(); i++)
		{
			L2DoorInstance door = getDoors().get(i);
			
			if (door.getDoorId() == doorId)
			{
				return door;
			}
			
			door = null;
		}
		return null;
	}
	
	public ClanHallFunction getFunction(final int type)
	{
		if (functions.get(type) != null)
		{
			return functions.get(type);
		}
		
		return null;
	}
	
	public void setZone(final L2ClanHallZone zone)
	{
		this.zone = zone;
	}
	
	public L2ClanHallZone getZone()
	{
		return zone;
	}
	
	public void free()
	{
		clanHallownerId = 0;
		isFree = true;
		
		for (final Map.Entry<Integer, ClanHallFunction> fc : functions.entrySet())
		{
			removeFunction(fc.getKey());
		}
		
		functions.clear();
		paidUntil = 0;
		paid = false;
		updateDb();
	}
	
	public void setOwner(final L2Clan clan)
	{
		// Verify that this ClanHall is Free and Clan isn't null
		if (clanHallownerId > 0 || clan == null)
		{
			return;
		}
		
		clanHallownerId = clan.getClanId();
		isFree = false;
		paidUntil = System.currentTimeMillis();
		initialyzeTask(true);
		
		// Annonce to Online member new ClanHall
		clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
		updateDb();
	}
	
	public L2Clan getOwnerClan()
	{
		if (clanHallownerId == 0)
		{
			return null;
		}
		
		if (ownerClan == null)
		{
			ownerClan = ClanTable.getInstance().getClan(getOwnerId());
		}
		
		return ownerClan;
	}
	
	public void spawnDoor()
	{
		for (int i = 0; i < getDoors().size(); i++)
		{
			L2DoorInstance door = getDoors().get(i);
			
			if (door.getCurrentHp() <= 0)
			{
				door.decayMe(); // Kill current if not killed already
				door = DoorTable.parseList(doorDefault.get(i));
				
				door.spawnMe(door.getX(), door.getY(), door.getZ());
				getDoors().set(i, door);
			}
			else if (door.isOpen())
			{
				door.closeMe();
			}
			
			door.setCurrentHp(door.getMaxHp());
			
			door = null;
		}
	}
	
	public void openCloseDoor(final L2PcInstance activeChar, final int doorId, final boolean open)
	{
		if (activeChar != null && activeChar.getClanId() == getOwnerId())
		{
			openCloseDoor(doorId, open);
		}
	}
	
	public void openCloseDoor(final int doorId, final boolean open)
	{
		openCloseDoor(getDoor(doorId), open);
	}
	
	public void openCloseDoor(final L2DoorInstance door, final boolean open)
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
	
	public void openCloseDoors(final L2PcInstance activeChar, final boolean open)
	{
		if (activeChar != null && activeChar.getClanId() == getOwnerId())
		{
			openCloseDoors(open);
		}
	}
	
	public void openCloseDoors(final boolean open)
	{
		for (final L2DoorInstance door : getDoors())
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
	
	public void banishForeigners()
	{
		zone.banishForeigners(getOwnerId());
	}
	
	private void loadFunctions()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CLAN_HALL_FUNCTIONS))
		{
			statement.setInt(1, getId());
			
			try (ResultSet rs = statement.executeQuery())
			{
				while (rs.next())
				{
					functions.put(rs.getInt("type"), new ClanHallFunction(rs.getInt("type"), rs.getInt("lvl"), rs.getInt("lease"), 0, rs.getLong("rate"), rs.getLong("endTime")));
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("ClanHall.loadFunctions : Could not select clan hall functions from db", e);
		}
	}
	
	public void removeFunction(int functionType)
	{
		functions.remove(functionType);
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_CLAN_HALL_FUNCTION_BY_CLAN_HALL_ID))
		{
			statement.setInt(1, getId());
			statement.setInt(2, functionType);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("ClanHall.removeFunctions: Could not delete clan hall function from db", e);
		}
	}
	
	public boolean updateFunctions(final int type, final int lvl, final int lease, final long rate, final boolean addNew)
	{
		if (Config.DEBUG)
		{
			LOGGER.warn("Called ClanHall.updateFunctions(int type, int lvl, int lease, long rate, boolean addNew) Owner : " + getOwnerId());
		}
		
		if (addNew)
		{
			if (ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() < lease)
			{
				return false;
			}
			
			functions.put(type, new ClanHallFunction(type, lvl, lease, 0, rate, 0));
		}
		else
		{
			if (lvl == 0 && lease == 0)
			{
				removeFunction(type);
			}
			else
			{
				final int diffLease = lease - functions.get(type).getLease();
				
				if (Config.DEBUG)
				{
					LOGGER.warn("Called ClanHall.updateFunctions diffLease : " + diffLease);
				}
				
				if (diffLease > 0)
				{
					if (ClanTable.getInstance().getClan(clanHallownerId).getWarehouse().getAdena() < diffLease)
					{
						return false;
					}
					
					functions.remove(type);
					functions.put(type, new ClanHallFunction(type, lvl, lease, diffLease, rate, -1));
				}
				else
				{
					functions.get(type).setLease(lease);
					functions.get(type).setLvl(lvl);
					functions.get(type).dbSave(false);
				}
			}
		}
		return true;
	}
	
	public void updateDb()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_CLAN_HALL_BY_ID))
		{
			statement.setInt(1, clanHallownerId);
			statement.setLong(2, paidUntil);
			statement.setInt(3, paid ? 1 : 0);
			statement.setInt(4, clanHallId);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("ClanHall.updateDb : Could not update clan hall data in db", e);
		}
	}
	
	private void initialyzeTask(final boolean forced)
	{
		final long currentTime = System.currentTimeMillis();
		
		if (paidUntil > currentTime)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), paidUntil - currentTime);
		}
		else if (!paid && !forced)
		{
			if (System.currentTimeMillis() + 1000 * 60 * 60 * 24 <= paidUntil + chRate)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), System.currentTimeMillis() + 1000 * 60 * 60 * 24);
			}
			else
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), paidUntil + chRate - System.currentTimeMillis());
			}
		}
		else
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), 0);
		}
	}
	
	protected class FeeTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if (isFree)
				{
					return;
				}
				
				L2Clan Clan = ClanTable.getInstance().getClan(getOwnerId());
				
				if (ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= getLease())
				{
					if (paidUntil != 0)
					{
						while (paidUntil < System.currentTimeMillis())
						{
							paidUntil += chRate;
						}
					}
					else
					{
						paidUntil = System.currentTimeMillis() + chRate;
					}
					
					ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().destroyItemByItemId("CH_rental_fee", 57, getLease(), null, null);
					
					if (Config.DEBUG)
					{
						LOGGER.warn("deducted " + getLease() + " adena from " + getName() + " owner's cwh for ClanHall paidUntil" + paidUntil);
					}
					
					ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), paidUntil - System.currentTimeMillis());
					paid = true;
					updateDb();
				}
				else
				{
					paid = false;
					if (System.currentTimeMillis() > paidUntil + chRate)
					{
						if (ClanHallManager.loaded())
						{
							AuctionManager.getInstance().initNPC(getId());
							ClanHallManager.getInstance().setFree(getId());
							Clan.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED));
						}
						else
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), 3000);
						}
					}
					else
					{
						updateDb();
						SystemMessage sm = new SystemMessage(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
						sm.addNumber(getLease());
						Clan.broadcastToOnlineMembers(sm);
						sm = null;
						
						if (System.currentTimeMillis() + 1000 * 60 * 60 * 24 <= paidUntil + chRate)
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), System.currentTimeMillis() + 1000 * 60 * 60 * 24);
						}
						else
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), paidUntil + chRate - System.currentTimeMillis());
						}
						
					}
				}
				
				Clan = null;
			}
			catch (final Exception t)
			{
				t.printStackTrace();
			}
		}
	}
	
	private void loadDoor()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CASTLE_DOOR_BY_CASTLE_ID))
		{
			statement.setInt(1, getId());
			
			try (ResultSet rs = statement.executeQuery())
			{
				while (rs.next())
				{
					// Create list of the door default for use when respawning dead doors
					doorDefault.add(rs.getString("name") + ";" + rs.getInt("id") + ";" + rs.getInt("x") + ";" + rs.getInt("y") + ";" + rs.getInt("z") + ";" + rs.getInt("range_xmin") + ";" + rs.getInt("range_ymin") + ";" + rs.getInt("range_zmin") + ";" + rs.getInt("range_xmax") + ";" + rs.getInt("range_ymax") + ";" + rs.getInt("range_zmax") + ";" + rs.getInt("hp") + ";" + rs.getInt("pDef") + ";"
						+ rs.getInt("mDef"));
					
					L2DoorInstance door = DoorTable.parseList(doorDefault.get(doorDefault.size() - 1));
					door.spawnMe(door.getX(), door.getY(), door.getZ());
					doors.add(door);
					DoorTable.getInstance().putDoor(door);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("ClanHall.loadDoor : Could not select clan hall door drom db", e);
		}
	}
}
