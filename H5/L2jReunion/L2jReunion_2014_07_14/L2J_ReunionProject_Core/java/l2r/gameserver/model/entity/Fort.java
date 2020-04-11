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
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.FortUpdater;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.datatables.SpawnTable;
import l2r.gameserver.datatables.sql.ClanTable;
import l2r.gameserver.datatables.sql.NpcTable;
import l2r.gameserver.datatables.xml.DoorData;
import l2r.gameserver.datatables.xml.StaticObjectsData;
import l2r.gameserver.enums.FortUpdaterType;
import l2r.gameserver.enums.MountType;
import l2r.gameserver.instancemanager.CastleManager;
import l2r.gameserver.instancemanager.FortManager;
import l2r.gameserver.instancemanager.ZoneManager;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2DoorInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2StaticObjectInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.itemcontainer.Inventory;
import l2r.gameserver.model.zone.type.L2FortZone;
import l2r.gameserver.model.zone.type.L2SiegeZone;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.PlaySound;
import l2r.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2r.gameserver.network.serverpackets.SystemMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Fort extends AbstractResidence
{
	protected static final Logger _log = LoggerFactory.getLogger(Fort.class);
	
	private final List<L2DoorInstance> _doors = new ArrayList<>();
	private L2StaticObjectInstance _flagPole = null;
	private FortSiege _siege = null;
	private Calendar _siegeDate;
	private Calendar _lastOwnedTime;
	private L2SiegeZone _zone;
	private L2Clan _fortOwner = null;
	private int _fortType = 0;
	private int _state = 0;
	private int _castleId = 0;
	private int _supplyLvL = 0;
	private final FastMap<Integer, FortFunction> _function;
	private final ScheduledFuture<?>[] _FortUpdater = new ScheduledFuture<?>[2];
	
	// Spawn Data
	private boolean _isSuspiciousMerchantSpawned = false;
	private final FastList<L2Spawn> _siegeNpcs = new FastList<>();
	private final FastList<L2Spawn> _npcCommanders = new FastList<>();
	private final FastList<L2Spawn> _specialEnvoys = new FastList<>();
	
	private final Map<Integer, Integer> _envoyCastles = new HashMap<>(2);
	private final Set<Integer> _availableCastles = new HashSet<>(1);
	
	/** Fortress Functions */
	public static final int FUNC_TELEPORT = 1;
	public static final int FUNC_RESTORE_HP = 2;
	public static final int FUNC_RESTORE_MP = 3;
	public static final int FUNC_RESTORE_EXP = 4;
	public static final int FUNC_SUPPORT = 5;
	
	public class FortFunction
	{
		private final int _type;
		private int _lvl;
		protected int _fee;
		protected int _tempFee;
		private final long _rate;
		private long _endDate;
		protected boolean _inDebt;
		public boolean _cwh;
		
		public FortFunction(int type, int lvl, int lease, int tempLease, long rate, long time, boolean cwh)
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
			if (getOwnerClan() == null)
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
					if (getOwnerClan() == null)
					{
						return;
					}
					if ((getOwnerClan().getWarehouse().getAdena() >= _fee) || !_cwh)
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
							getOwnerClan().getWarehouse().destroyItemByItemId("CS_function_fee", Inventory.ADENA_ID, fee, null, null);
						}
						ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(true), getRate());
					}
					else
					{
						removeFunction(getType());
					}
				}
				catch (Throwable t)
				{
				}
			}
		}
		
		public void dbSave()
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement("REPLACE INTO fort_functions (fort_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)"))
			{
				ps.setInt(1, getResidenceId());
				ps.setInt(2, getType());
				ps.setInt(3, getLvl());
				ps.setInt(4, getLease());
				ps.setLong(5, getRate());
				ps.setLong(6, getEndTime());
				ps.execute();
			}
			catch (Exception e)
			{
				_log.error("Exception: Fort.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " + e.getMessage(), e);
			}
		}
	}
	
	public Fort(int fortId)
	{
		super(fortId);
		load();
		loadFlagPoles();
		_function = new FastMap<>();
		if (getOwnerClan() != null)
		{
			setVisibleFlag(true);
			loadFunctions();
		}
		initResidenceZone();
		initNpcs(); // load and spawn npcs (Always spawned)
		initSiegeNpcs(); // load suspicious merchants (Despawned 10mins before siege)
		// spawnSuspiciousMerchant();// spawn suspicious merchants
		initNpcCommanders(); // npc Commanders (not monsters) (Spawned during siege)
		spawnNpcCommanders(); // spawn npc Commanders
		initSpecialEnvoys(); // envoys from castles (Spawned after fort taken)
		if ((getOwnerClan() != null) && (getFortState() == 0))
		{
			spawnSpecialEnvoys();
		}
	}
	
	/**
	 * Return function with id
	 * @param type
	 * @return
	 */
	public FortFunction getFunction(int type)
	{
		if (_function.get(type) != null)
		{
			return _function.get(type);
		}
		return null;
	}
	
	public void endOfSiege(L2Clan clan)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new endFortressSiege(this, clan), 1000);
	}
	
	public void engrave(L2Clan clan)
	{
		setOwner(clan, true);
	}
	
	/**
	 * Move non clan members off fort area and to nearest town.<BR>
	 * <BR>
	 */
	public void banishForeigners()
	{
		getResidenceZone().banishForeigners(getOwnerClan().getId());
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
	
	public L2SiegeZone getZone()
	{
		if (_zone == null)
		{
			for (L2SiegeZone zone : ZoneManager.getInstance().getAllZones(L2SiegeZone.class))
			{
				if (zone.getSiegeObjectId() == getResidenceId())
				{
					_zone = zone;
					break;
				}
			}
		}
		return _zone;
	}
	
	@Override
	public L2FortZone getResidenceZone()
	{
		return (L2FortZone) super.getResidenceZone();
	}
	
	/**
	 * Get the objects distance to this fort
	 * @param obj
	 * @return
	 */
	public double getDistance(L2Object obj)
	{
		return getZone().getDistanceToZone(obj);
	}
	
	public void closeDoor(L2PcInstance activeChar, int doorId)
	{
		openCloseDoor(activeChar, doorId, false);
	}
	
	public void openDoor(L2PcInstance activeChar, int doorId)
	{
		openCloseDoor(activeChar, doorId, true);
	}
	
	public void openCloseDoor(L2PcInstance activeChar, int doorId, boolean open)
	{
		if (activeChar.getClan() != getOwnerClan())
		{
			return;
		}
		
		L2DoorInstance door = getDoor(doorId);
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
	
	// This method is used to begin removing all fort upgrades
	public void removeUpgrade()
	{
		removeDoorUpgrade();
	}
	
	/**
	 * This method will set owner for Fort
	 * @param clan
	 * @param updateClansReputation
	 * @return
	 */
	public boolean setOwner(L2Clan clan, boolean updateClansReputation)
	{
		if (clan == null)
		{
			_log.warn(getClass().getSimpleName() + ": Updating Fort owner with null clan!!!");
			return false;
		}
		final L2Clan oldowner = getOwnerClan();
		if ((oldowner != null) && (clan != oldowner))
		{
			// Remove points from old owner
			updateClansReputation(oldowner, true);
			try
			{
				L2PcInstance oldleader = oldowner.getLeader().getPlayerInstance();
				if (oldleader != null)
				{
					if (oldleader.getMountType() == MountType.WYVERN)
					{
						oldleader.dismount();
					}
				}
			}
			catch (Exception e)
			{
				_log.warn("Exception in setOwner: " + e.getMessage(), e);
			}
			removeOwner(true);
		}
		setFortState(0, 0); // initialize fort state
		
		// if clan already have castle, don't store him in fortress
		if (clan.getCastleId() > 0)
		{
			getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.NPCS_RECAPTURED_FORTRESS));
			return false;
		}
		
		// Give points to new owner
		if (updateClansReputation)
		{
			updateClansReputation(clan, false);
		}
		
		spawnSpecialEnvoys();
		// if clan have already fortress, remove it
		if (clan.getFortId() > 0)
		{
			FortManager.getInstance().getFortByOwner(clan).removeOwner(true);
		}
		
		setSupplyLvL(0);
		setOwnerClan(clan);
		updateOwnerInDB(); // Update in database
		saveFortVariables();
		
		if (getSiege().isInProgress())
		{
			getSiege().endSiege();
		}
		
		for (L2PcInstance member : clan.getOnlineMembers(0))
		{
			giveResidentialSkills(member);
			member.sendSkillList();
		}
		return true;
	}
	
	public void removeOwner(boolean updateDB)
	{
		L2Clan clan = getOwnerClan();
		if (clan != null)
		{
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				removeResidentialSkills(member);
				member.sendSkillList();
			}
			clan.setFortId(0);
			clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
			setOwnerClan(null);
			setSupplyLvL(0);
			saveFortVariables();
			removeAllFunctions();
			if (updateDB)
			{
				updateOwnerInDB();
			}
		}
	}
	
	public void raiseSupplyLvL()
	{
		_supplyLvL++;
		if (_supplyLvL > Config.FS_MAX_SUPPLY_LEVEL)
		{
			_supplyLvL = Config.FS_MAX_SUPPLY_LEVEL;
		}
	}
	
	public void setSupplyLvL(int val)
	{
		if (val <= Config.FS_MAX_SUPPLY_LEVEL)
		{
			_supplyLvL = val;
		}
	}
	
	public int getSupplyLvL()
	{
		return _supplyLvL;
	}
	
	public void saveFortVariables()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE fort SET supplyLvL=? WHERE id = ?"))
		{
			ps.setInt(1, _supplyLvL);
			ps.setInt(2, getResidenceId());
			ps.execute();
		}
		catch (Exception e)
		{
			_log.warn("Exception: saveFortVariables(): " + e.getMessage(), e);
		}
	}
	
	/**
	 * Show or hide flag inside flag pole.
	 * @param val
	 */
	public void setVisibleFlag(boolean val)
	{
		L2StaticObjectInstance flagPole = getFlagPole();
		if (flagPole != null)
		{
			flagPole.setMeshIndex(val ? 1 : 0);
		}
	}
	
	/**
	 * Respawn all doors on fort grounds<BR>
	 * <BR>
	 */
	public void resetDoors()
	{
		for (L2DoorInstance door : _doors)
		{
			if (door.getOpen())
			{
				door.closeMe();
			}
			if (door.isDead())
			{
				door.doRevive();
			}
			if (door.getCurrentHp() < door.getMaxHp())
			{
				door.setCurrentHp(door.getMaxHp());
			}
		}
		loadDoorUpgrade(); // Check for any upgrade the doors may have
	}
	
	// This method upgrade door
	public void upgradeDoor(int doorId, int hp, int pDef, int mDef)
	{
		L2DoorInstance door = getDoor(doorId);
		if ((door != null) && (door.getId() == doorId))
		{
			door.setCurrentHp(door.getMaxHp() + hp);
			
			saveDoorUpgrade(doorId, hp, pDef, mDef);
			return;
		}
	}
	
	// This method loads fort
	@Override
	protected void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM fort WHERE id = ?"))
		{
			ps.setInt(1, getResidenceId());
			int ownerId = 0;
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					setName(rs.getString("name"));
					
					_siegeDate = Calendar.getInstance();
					_lastOwnedTime = Calendar.getInstance();
					_siegeDate.setTimeInMillis(rs.getLong("siegeDate"));
					_lastOwnedTime.setTimeInMillis(rs.getLong("lastOwnedTime"));
					ownerId = rs.getInt("owner");
					_fortType = rs.getInt("fortType");
					_state = rs.getInt("state");
					_castleId = rs.getInt("castleId");
					_supplyLvL = rs.getInt("supplyLvL");
				}
			}
			if (ownerId > 0)
			{
				L2Clan clan = ClanTable.getInstance().getClan(ownerId); // Try to find clan instance
				clan.setFortId(getResidenceId());
				setOwnerClan(clan);
				int runCount = getOwnedTime() / (Config.FS_UPDATE_FRQ * 60);
				long initial = System.currentTimeMillis() - _lastOwnedTime.getTimeInMillis();
				while (initial > (Config.FS_UPDATE_FRQ * 60000L))
				{
					initial -= (Config.FS_UPDATE_FRQ * 60000L);
				}
				initial = (Config.FS_UPDATE_FRQ * 60000L) - initial;
				if ((Config.FS_MAX_OWN_TIME <= 0) || (getOwnedTime() < (Config.FS_MAX_OWN_TIME * 3600)))
				{
					_FortUpdater[0] = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FortUpdater(this, clan, runCount, FortUpdaterType.PERIODIC_UPDATE), initial, Config.FS_UPDATE_FRQ * 60000L); // Schedule owner tasks to start running
					if (Config.FS_MAX_OWN_TIME > 0)
					{
						_FortUpdater[1] = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FortUpdater(this, clan, runCount, FortUpdaterType.MAX_OWN_TIME), 3600000, 3600000); // Schedule owner tasks to remove owener
					}
				}
				else
				{
					_FortUpdater[1] = ThreadPoolManager.getInstance().scheduleGeneral(new FortUpdater(this, clan, 0, FortUpdaterType.MAX_OWN_TIME), 60000); // Schedule owner tasks to remove owner
				}
			}
			else
			{
				setOwnerClan(null);
			}
			
		}
		catch (Exception e)
		{
			_log.warn("Exception: loadFortData(): " + e.getMessage(), e);
		}
	}
	
	/** Load All Functions */
	private void loadFunctions()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM fort_functions WHERE fort_id = ?"))
		{
			ps.setInt(1, getResidenceId());
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					_function.put(rs.getInt("type"), new FortFunction(rs.getInt("type"), rs.getInt("lvl"), rs.getInt("lease"), 0, rs.getLong("rate"), rs.getLong("endTime"), true));
				}
			}
		}
		catch (Exception e)
		{
			_log.error("Exception: Fort.loadFunctions(): " + e.getMessage(), e);
		}
	}
	
	/**
	 * Remove function In List and in DB
	 * @param functionType
	 */
	public void removeFunction(int functionType)
	{
		_function.remove(functionType);
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM fort_functions WHERE fort_id=? AND type=?"))
		{
			ps.setInt(1, getResidenceId());
			ps.setInt(2, functionType);
			ps.execute();
		}
		catch (Exception e)
		{
			_log.error("Exception: Fort.removeFunctions(int functionType): " + e.getMessage(), e);
		}
	}
	
	/**
	 * Remove all fort functions.
	 */
	private void removeAllFunctions()
	{
		for (int id : _function.keySet())
		{
			removeFunction(id);
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
			_function.put(type, new FortFunction(type, lvl, lease, 0, rate, 0, false));
		}
		else
		{
			if ((lvl == 0) && (lease == 0))
			{
				removeFunction(type);
			}
			else
			{
				int diffLease = lease - _function.get(type).getLease();
				if (diffLease > 0)
				{
					_function.remove(type);
					_function.put(type, new FortFunction(type, lvl, lease, 0, rate, -1, false));
				}
				else
				{
					_function.get(type).setLease(lease);
					_function.get(type).setLvl(lvl);
					_function.get(type).dbSave();
				}
			}
		}
		return true;
	}
	
	public void activateInstance()
	{
		loadDoor();
	}
	
	// This method loads fort door data from database
	private void loadDoor()
	{
		for (L2DoorInstance door : DoorData.getInstance().getDoors())
		{
			if ((door.getFort() != null) && (door.getFort().getResidenceId() == getResidenceId()))
			{
				_doors.add(door);
			}
		}
	}
	
	private void loadFlagPoles()
	{
		for (L2StaticObjectInstance obj : StaticObjectsData.getInstance().getStaticObjects())
		{
			if ((obj.getType() == 3) && obj.getName().startsWith(getName()))
			{
				_flagPole = obj;
				break;
			}
		}
		if (_flagPole == null)
		{
			throw new NullPointerException("Can't find flagpole for Fort " + this);
		}
	}
	
	// This method loads fort door upgrade data from database
	private void loadDoorUpgrade()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM fort_doorupgrade WHERE fortId = ?"))
		{
			ps.setInt(1, getResidenceId());
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					upgradeDoor(rs.getInt("id"), rs.getInt("hp"), rs.getInt("pDef"), rs.getInt("mDef"));
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Exception: loadFortDoorUpgrade(): " + e.getMessage(), e);
		}
	}
	
	private void removeDoorUpgrade()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM fort_doorupgrade WHERE fortId = ?"))
		{
			ps.setInt(1, getResidenceId());
			ps.execute();
		}
		catch (Exception e)
		{
			_log.warn("Exception: removeDoorUpgrade(): " + e.getMessage(), e);
		}
	}
	
	private void saveDoorUpgrade(int doorId, int hp, int pDef, int mDef)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO fort_doorupgrade (doorId, hp, pDef, mDef) VALUES (?,?,?,?)"))
		{
			ps.setInt(1, doorId);
			ps.setInt(2, hp);
			ps.setInt(3, pDef);
			ps.setInt(4, mDef);
			ps.execute();
		}
		catch (Exception e)
		{
			_log.warn("Exception: saveDoorUpgrade(int doorId, int hp, int pDef, int mDef): " + e.getMessage(), e);
		}
	}
	
	private void updateOwnerInDB()
	{
		L2Clan clan = getOwnerClan();
		int clanId = 0;
		if (clan != null)
		{
			clanId = clan.getId();
			_lastOwnedTime.setTimeInMillis(System.currentTimeMillis());
		}
		else
		{
			_lastOwnedTime.setTimeInMillis(0);
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE fort SET owner=?,lastOwnedTime=?,state=?,castleId=? WHERE id = ?"))
		{
			ps.setInt(1, clanId);
			ps.setLong(2, _lastOwnedTime.getTimeInMillis());
			ps.setInt(3, 0);
			ps.setInt(4, 0);
			ps.setInt(5, getResidenceId());
			ps.execute();
			
			// Announce to clan members
			if (clan != null)
			{
				clan.setFortId(getResidenceId()); // Set has fort flag for new owner
				SystemMessage sm;
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CLAN_IS_VICTORIOUS_IN_THE_FORTRESS_BATTLE_OF_S2);
				sm.addString(clan.getName());
				sm.addCastleId(getResidenceId());
				L2World.getInstance().getPlayers().forEach(p -> p.sendPacket(sm));
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				clan.broadcastToOnlineMembers(new PlaySound(1, "Siege_Victory", 0, 0, 0, 0, 0));
				if (_FortUpdater[0] != null)
				{
					_FortUpdater[0].cancel(false);
				}
				if (_FortUpdater[1] != null)
				{
					_FortUpdater[1].cancel(false);
				}
				_FortUpdater[0] = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FortUpdater(this, clan, 0, FortUpdaterType.PERIODIC_UPDATE), Config.FS_UPDATE_FRQ * 60000L, Config.FS_UPDATE_FRQ * 60000L); // Schedule owner tasks to start running
				if (Config.FS_MAX_OWN_TIME > 0)
				{
					_FortUpdater[1] = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FortUpdater(this, clan, 0, FortUpdaterType.MAX_OWN_TIME), 3600000, 3600000); // Schedule owner tasks to remove owener
				}
			}
			else
			{
				if (_FortUpdater[0] != null)
				{
					_FortUpdater[0].cancel(false);
				}
				_FortUpdater[0] = null;
				if (_FortUpdater[1] != null)
				{
					_FortUpdater[1].cancel(false);
				}
				_FortUpdater[1] = null;
			}
		}
		catch (Exception e)
		{
			_log.warn("Exception: updateOwnerInDB(L2Clan clan): " + e.getMessage(), e);
		}
	}
	
	public final L2Clan getOwnerClan()
	{
		return _fortOwner;
	}
	
	public final void setOwnerClan(L2Clan clan)
	{
		setVisibleFlag(clan != null ? true : false);
		_fortOwner = clan;
	}
	
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
	
	public final List<L2DoorInstance> getDoors()
	{
		return _doors;
	}
	
	public final L2StaticObjectInstance getFlagPole()
	{
		return _flagPole;
	}
	
	public final FortSiege getSiege()
	{
		if (_siege == null)
		{
			_siege = new FortSiege(this);
		}
		return _siege;
	}
	
	public final Calendar getSiegeDate()
	{
		return _siegeDate;
	}
	
	public final void setSiegeDate(Calendar siegeDate)
	{
		_siegeDate = siegeDate;
	}
	
	public final int getOwnedTime()
	{
		if (_lastOwnedTime.getTimeInMillis() == 0)
		{
			return 0;
		}
		
		return (int) ((System.currentTimeMillis() - _lastOwnedTime.getTimeInMillis()) / 1000);
	}
	
	public final int getTimeTillRebelArmy()
	{
		if (_lastOwnedTime.getTimeInMillis() == 0)
		{
			return 0;
		}
		
		return (int) (((_lastOwnedTime.getTimeInMillis() + (Config.FS_MAX_OWN_TIME * 3600000L)) - System.currentTimeMillis()) / 1000L);
	}
	
	public final long getTimeTillNextFortUpdate()
	{
		if (_FortUpdater[0] == null)
		{
			return 0;
		}
		return _FortUpdater[0].getDelay(TimeUnit.SECONDS);
	}
	
	public void updateClansReputation(L2Clan owner, boolean removePoints)
	{
		if (owner != null)
		{
			if (removePoints)
			{
				owner.takeReputationScore(Config.LOOSE_FORT_POINTS, true);
			}
			else
			{
				owner.addReputationScore(Config.TAKE_FORT_POINTS, true);
			}
		}
	}
	
	private static class endFortressSiege implements Runnable
	{
		private final Fort _f;
		private final L2Clan _clan;
		
		public endFortressSiege(Fort f, L2Clan clan)
		{
			_f = f;
			_clan = clan;
		}
		
		@Override
		public void run()
		{
			try
			{
				_f.engrave(_clan);
			}
			catch (Exception e)
			{
				_log.warn("Exception in endFortressSiege " + e.getMessage(), e);
			}
		}
		
	}
	
	/**
	 * @return Returns state of fortress.<BR>
	 * <BR>
	 *         0 - not decided yet<BR>
	 *         1 - independent<BR>
	 *         2 - contracted with castle<BR>
	 */
	public final int getFortState()
	{
		return _state;
	}
	
	/**
	 * @param state <ul>
	 *            <li>0 - not decided yet</li>
	 *            <li>1 - independent</li>
	 *            <li>2 - contracted with castle</li>
	 *            </ul>
	 * @param castleId set Castle Id for contracted fort
	 */
	public final void setFortState(int state, int castleId)
	{
		_state = state;
		_castleId = castleId;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE fort SET state=?,castleId=? WHERE id = ?"))
		{
			ps.setInt(1, getFortState());
			ps.setInt(2, getCastleId());
			ps.setInt(3, getResidenceId());
			ps.execute();
		}
		catch (Exception e)
		{
			_log.warn("Exception: setFortState(int state, int castleId): " + e.getMessage(), e);
		}
	}
	
	/**
	 * @return Returns Castle Id of fortress contracted with castle.
	 */
	public final int getCastleId()
	{
		return _castleId;
	}
	
	/**
	 * @return Returns fortress type.<BR>
	 * <BR>
	 *         0 - small (3 commanders) <BR>
	 *         1 - big (4 commanders + control room)
	 */
	public final int getFortType()
	{
		return _fortType;
	}
	
	/**
	 * @param npcId the Id of the ambassador NPC
	 * @return the Id of the castle this ambassador represents
	 */
	public final int getCastleIdByAmbassador(int npcId)
	{
		return _envoyCastles.get(npcId);
	}
	
	/**
	 * @param npcId the Id of the ambassador NPC
	 * @return the castle this ambassador represents
	 */
	public final Castle getCastleByAmbassador(int npcId)
	{
		return CastleManager.getInstance().getCastleById(getCastleIdByAmbassador(npcId));
	}
	
	/**
	 * @return the Id of the castle contracted with this fortress
	 */
	public final int getContractedCastleId()
	{
		return _castleId;
	}
	
	/**
	 * @return the castle contracted with this fortress ({@code null} if no contract with any castle)
	 */
	public final Castle getContractedCastle()
	{
		return CastleManager.getInstance().getCastleById(getContractedCastleId());
	}
	
	/**
	 * Check if this is a border fortress (associated with multiple castles).
	 * @return {@code true} if this is a border fortress (associated with more than one castle), {@code false} otherwise
	 */
	public final boolean isBorderFortress()
	{
		return _availableCastles.size() > 1;
	}
	
	/**
	 * @return Returns amount of barracks.
	 */
	public final int getFortSize()
	{
		return getFortType() == 0 ? 3 : 5;
	}
	
	public void spawnSuspiciousMerchant()
	{
		if (_isSuspiciousMerchantSpawned)
		{
			return;
		}
		_isSuspiciousMerchantSpawned = true;
		
		for (L2Spawn spawnDat : _siegeNpcs)
		{
			spawnDat.doSpawn();
			spawnDat.startRespawn();
		}
	}
	
	public void despawnSuspiciousMerchant()
	{
		if (!_isSuspiciousMerchantSpawned)
		{
			return;
		}
		_isSuspiciousMerchantSpawned = false;
		
		for (L2Spawn spawnDat : _siegeNpcs)
		{
			spawnDat.stopRespawn();
			spawnDat.getLastSpawn().deleteMe();
		}
	}
	
	public void spawnNpcCommanders()
	{
		for (L2Spawn spawnDat : _npcCommanders)
		{
			spawnDat.doSpawn();
			spawnDat.startRespawn();
		}
	}
	
	public void despawnNpcCommanders()
	{
		for (L2Spawn spawnDat : _npcCommanders)
		{
			spawnDat.stopRespawn();
			spawnDat.getLastSpawn().deleteMe();
		}
	}
	
	public void spawnSpecialEnvoys()
	{
		for (L2Spawn spawnDat : _specialEnvoys)
		{
			spawnDat.doSpawn();
			spawnDat.startRespawn();
		}
	}
	
	private void initNpcs()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM fort_spawnlist WHERE fortId = ? AND spawnType = ? "))
		{
			ps.setInt(1, getResidenceId());
			ps.setInt(2, 0);
			try (ResultSet rs = ps.executeQuery())
			{
				L2Spawn spawnDat;
				L2NpcTemplate template;
				while (rs.next())
				{
					template = NpcTable.getInstance().getTemplate(rs.getInt("npcId"));
					if (template != null)
					{
						spawnDat = new L2Spawn(template);
						spawnDat.setAmount(1);
						spawnDat.setX(rs.getInt("x"));
						spawnDat.setY(rs.getInt("y"));
						spawnDat.setZ(rs.getInt("z"));
						spawnDat.setHeading(rs.getInt("heading"));
						spawnDat.setRespawnDelay(60);
						SpawnTable.getInstance().addNewSpawn(spawnDat, false);
						spawnDat.doSpawn();
						spawnDat.startRespawn();
					}
					else
					{
						_log.warn("Fort " + getResidenceId() + " initNpcs: Data missing in NPC table for ID: " + rs.getInt("npcId") + ".");
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Fort " + getResidenceId() + " initNpcs: Spawn could not be initialized: " + e.getMessage(), e);
		}
	}
	
	private void initSiegeNpcs()
	{
		_siegeNpcs.clear();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT id, npcId, x, y, z, heading FROM fort_spawnlist WHERE fortId = ? AND spawnType = ? ORDER BY id"))
		{
			ps.setInt(1, getResidenceId());
			ps.setInt(2, 2);
			try (ResultSet rs = ps.executeQuery())
			{
				L2Spawn spawnDat;
				L2NpcTemplate template;
				while (rs.next())
				{
					template = NpcTable.getInstance().getTemplate(rs.getInt("npcId"));
					if (template != null)
					{
						spawnDat = new L2Spawn(template);
						spawnDat.setAmount(1);
						spawnDat.setX(rs.getInt("x"));
						spawnDat.setY(rs.getInt("y"));
						spawnDat.setZ(rs.getInt("z"));
						spawnDat.setHeading(rs.getInt("heading"));
						spawnDat.setRespawnDelay(60);
						_siegeNpcs.add(spawnDat);
					}
					else
					{
						_log.warn("Fort " + getResidenceId() + " initSiegeNpcs: Data missing in NPC table for ID: " + rs.getInt("npcId") + ".");
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Fort " + getResidenceId() + " initSiegeNpcs: Spawn could not be initialized: " + e.getMessage(), e);
		}
	}
	
	private void initNpcCommanders()
	{
		_npcCommanders.clear();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT id, npcId, x, y, z, heading FROM fort_spawnlist WHERE fortId = ? AND spawnType = ? ORDER BY id"))
		{
			ps.setInt(1, getResidenceId());
			ps.setInt(2, 1);
			try (ResultSet rs = ps.executeQuery())
			{
				L2Spawn spawnDat;
				L2NpcTemplate template;
				while (rs.next())
				{
					template = NpcTable.getInstance().getTemplate(rs.getInt("npcId"));
					if (template != null)
					{
						spawnDat = new L2Spawn(template);
						spawnDat.setAmount(1);
						spawnDat.setX(rs.getInt("x"));
						spawnDat.setY(rs.getInt("y"));
						spawnDat.setZ(rs.getInt("z"));
						spawnDat.setHeading(rs.getInt("heading"));
						spawnDat.setRespawnDelay(60);
						_npcCommanders.add(spawnDat);
					}
					else
					{
						_log.warn("Fort " + getResidenceId() + " initNpcCommanders: Data missing in NPC table for ID: " + rs.getInt("npcId") + ".");
					}
				}
			}
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.warn("Fort " + getResidenceId() + " initNpcCommanders: Spawn could not be initialized: " + e.getMessage(), e);
		}
	}
	
	private void initSpecialEnvoys()
	{
		_specialEnvoys.clear();
		_envoyCastles.clear();
		_availableCastles.clear();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT id, npcId, x, y, z, heading, castleId FROM fort_spawnlist WHERE fortId = ? AND spawnType = ? ORDER BY id"))
		{
			ps.setInt(1, getResidenceId());
			ps.setInt(2, 3);
			try (ResultSet rs = ps.executeQuery())
			{
				L2Spawn spawnDat;
				L2NpcTemplate template;
				while (rs.next())
				{
					int castleId = rs.getInt("castleId");
					int npcId = rs.getInt("npcId");
					template = NpcTable.getInstance().getTemplate(npcId);
					if (template != null)
					{
						spawnDat = new L2Spawn(template);
						spawnDat.setAmount(1);
						spawnDat.setX(rs.getInt("x"));
						spawnDat.setY(rs.getInt("y"));
						spawnDat.setZ(rs.getInt("z"));
						spawnDat.setHeading(rs.getInt("heading"));
						spawnDat.setRespawnDelay(60);
						_specialEnvoys.add(spawnDat);
						_envoyCastles.put(npcId, castleId);
						_availableCastles.add(castleId);
					}
					else
					{
						_log.warn("Fort " + getResidenceId() + " initSpecialEnvoys: Data missing in NPC table for ID: " + rs.getInt("npcId") + ".");
					}
				}
			}
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.warn("Fort " + getResidenceId() + " initSpecialEnvoys: Spawn could not be initialized: " + e.getMessage(), e);
		}
	}
	
	@Override
	protected void initResidenceZone()
	{
		for (L2FortZone zone : ZoneManager.getInstance().getAllZones(L2FortZone.class))
		{
			if (zone.getResidenceId() == getResidenceId())
			{
				setResidenceZone(zone);
				break;
			}
		}
	}
}
