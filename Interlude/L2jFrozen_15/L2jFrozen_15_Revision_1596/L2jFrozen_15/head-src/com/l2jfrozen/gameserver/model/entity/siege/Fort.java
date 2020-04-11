package com.l2jfrozen.gameserver.model.entity.siege;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.datatables.csv.DoorTable;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2jfrozen.gameserver.model.zone.type.L2FortZone;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author programmos
 */

public class Fort
{
	protected static final Logger LOGGER = Logger.getLogger(Fort.class);
	
	private static final String SELECT_FORTRESS_BY_ID = "SELECT id,`name`,siegeDate,siegeDayOfWeek,siegeHourOfDay,owner FROM fort WHERE id=?"; // "name" is a special WORD in MySQL / MariaDB so must be inside of ``.
	private static final String SELECT_FORTRESS_DOOR_BY_ID = "SELECT fortId,id,`name`,x,y,z,range_xmin,range_ymin,range_zmin,range_xmax,range_ymax,range_zmax,hp,pDef,mDef FROM fort_door WHERE fortId=?";
	private static final String SELECT_FORTRESS_DOOR_UPGRADE = "SELECT doorId,fortId,hp,pDef,mDef FROM fort_doorupgrade WHERE doorId IN (SELECT Id FROM fort_door WHERE fortId=?)";
	private static final String DELETE_FORTRESS_DOOR_UPGRADE = "DELETE FROM fort_doorupgrade WHERE doorId IN (SELECT id FROM fort_door WHERE fortId=?)";
	private static final String INSERT_FORTRESS_DOOR_UPGRADE = "INSERT INTO fort_doorupgrade (doorId, hp, pDef, mDef) VALUES (?,?,?,?)";
	private static final String UPDATE_FORTRESS_OWNER = "UPDATE fort SET owner=? WHERE id=?";
	
	private int fortId = 0;
	private final List<L2DoorInstance> doors = new ArrayList<>();
	private final List<String> doorDefault = new ArrayList<>();
	private String name = "";
	private int ownerId = 0;
	private L2Clan fortOwner = null;
	private FortSiege siege = null;
	private Calendar siegeDate;
	private int siegeDayOfWeek = 7; // Default to saturday
	private int siegeHourOfDay = 20; // Default to 8 pm server time
	private L2FortZone zone;
	private L2Clan formerOwner = null;
	
	public Fort(int fortId)
	{
		this.fortId = fortId;
		load();
		loadDoor();
	}
	
	public void EndOfSiege(final L2Clan clan)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new endFortressSiege(this, clan), 1000);
	}
	
	public void Engrave(final L2Clan clan, final int objId)
	{
		getSiege().announceToPlayer("Clan " + clan.getName() + " has finished to raise the flag.", true);
		setOwner(clan);
	}
	
	/**
	 * Add amount to fort instance's treasury (warehouse).
	 * @param amount
	 */
	public void addToTreasury(final int amount)
	{
		// TODO: This method add to the treasury
	}
	
	/**
	 * Add amount to fort instance's treasury (warehouse), no tax paying.
	 * @param  amount
	 * @return
	 */
	public boolean addToTreasuryNoTax(final int amount)
	{
		return true;
	}
	
	/**
	 * Move non clan members off fort area and to nearest town.<BR>
	 * <BR>
	 */
	public void banishForeigners()
	{
		zone.banishForeigners(getOwnerId());
	}
	
	/**
	 * @param  x
	 * @param  y
	 * @param  z
	 * @return   true if object is inside the zone
	 */
	public boolean checkIfInZone(final int x, final int y, final int z)
	{
		return zone.isInsideZone(x, y, z);
	}
	
	/**
	 * Sets this forts zone
	 * @param zone
	 */
	public void setZone(final L2FortZone zone)
	{
		this.zone = zone;
	}
	
	public L2FortZone getZone()
	{
		return zone;
	}
	
	/**
	 * Get the objects distance to this fort
	 * @param  obj
	 * @return
	 */
	public double getDistance(final L2Object obj)
	{
		return zone.getDistanceToZone(obj);
	}
	
	public void closeDoor(final L2PcInstance activeChar, final int doorId)
	{
		openCloseDoor(activeChar, doorId, false);
	}
	
	public void openDoor(final L2PcInstance activeChar, final int doorId)
	{
		openCloseDoor(activeChar, doorId, true);
	}
	
	public void openCloseDoor(final L2PcInstance activeChar, final int doorId, final boolean open)
	{
		if (activeChar.getClanId() != getOwnerId())
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
		
		door = null;
	}
	
	// This method is used to begin removing all fort upgrades
	public void removeUpgrade()
	{
		removeDoorUpgrade();
	}
	
	// This method updates the fort tax rate
	public void setOwner(final L2Clan clan)
	{
		// Remove old owner
		if (getOwnerId() > 0 && (clan == null || clan.getClanId() != getOwnerId()))
		{
			// Try to find clan instance
			L2Clan oldOwner = ClanTable.getInstance().getClan(getOwnerId());
			
			if (oldOwner != null)
			{
				if (formerOwner == null)
				{
					formerOwner = oldOwner;
				}
				
				// Unset has fort flag for old owner
				oldOwner.setHasFort(0);
				Announcements.getInstance().announceToAll(oldOwner.getName() + " has lost " + getName() + " fortress!");
			}
			
			oldOwner = null;
		}
		
		updateOwnerInDB(clan); // Update in database
		
		if (getSiege().getIsInProgress())
		{
			getSiege().midVictory(); // Mid victory phase of siege
		}
		
		updateClansReputation();
		
		fortOwner = clan;
	}
	
	public void removeOwner(final L2Clan clan)
	{
		if (clan != null)
		{
			formerOwner = clan;
			
			clan.setHasFort(0);
			Announcements.getInstance().announceToAll(clan.getName() + " has lost " + getName() + " fort");
			clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
		}
		
		updateOwnerInDB(null);
		
		if (getSiege().getIsInProgress())
		{
			getSiege().midVictory();
		}
		
		updateClansReputation();
		
		fortOwner = null;
	}
	
	// This method updates the fort tax rate
	public void setTaxPercent(final L2PcInstance activeChar, final int taxPercent)
	{
		int maxTax;
		
		switch (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE))
		{
			case SevenSigns.CABAL_DAWN:
				maxTax = 25;
				break;
			case SevenSigns.CABAL_DUSK:
				maxTax = 5;
				break;
			default: // no owner
				maxTax = 15;
		}
		
		if (taxPercent < 0 || taxPercent > maxTax)
		{
			activeChar.sendMessage("Tax value must be between 0 and " + maxTax + ".");
			return;
		}
		
		activeChar.sendMessage(getName() + " fort tax changed to " + taxPercent + "%.");
	}
	
	/**
	 * Respawn all doors on fort grounds<BR>
	 * <BR>
	 */
	public void spawnDoor()
	{
		spawnDoor(false);
	}
	
	/**
	 * Respawn all doors on fort grounds
	 * @param isDoorWeak
	 */
	public void spawnDoor(final boolean isDoorWeak)
	{
		for (int i = 0; i < getDoors().size(); i++)
		{
			L2DoorInstance door = getDoors().get(i);
			
			if (door.getCurrentHp() >= 0)
			{
				door.decayMe(); // Kill current if not killed already
				door = DoorTable.parseList(doorDefault.get(i));
				
				if (isDoorWeak)
				{
					door.setCurrentHp(door.getMaxHp() / 2);
				}
				else
				{
					door.setCurrentHp(door.getMaxHp());
				}
				
				door.spawnMe(door.getX(), door.getY(), door.getZ());
				getDoors().set(i, door);
			}
			else if (!door.isOpen())
			{
				door.closeMe();
			}
			
			door = null;
		}
		
		loadDoorUpgrade(); // Check for any upgrade the doors may have
	}
	
	// This method upgrade door
	public void upgradeDoor(final int doorId, final int hp, final int pDef, final int mDef)
	{
		final L2DoorInstance door = getDoor(doorId);
		
		if (door == null)
		{
			return;
		}
		
		if (door.getDoorId() == doorId)
		{
			door.setCurrentHp(door.getMaxHp() + hp);
			
			saveDoorUpgrade(doorId, hp, pDef, mDef);
			return;
		}
	}
	
	private void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_FORTRESS_BY_ID))
		{
			statement.setInt(1, getFortId());
			
			try (ResultSet rs = statement.executeQuery())
			{
				while (rs.next())
				{
					name = rs.getString("name");
					
					siegeDate = Calendar.getInstance();
					siegeDate.setTimeInMillis(rs.getLong("siegeDate"));
					
					siegeDayOfWeek = rs.getInt("siegeDayOfWeek");
					
					if (siegeDayOfWeek < 1 || siegeDayOfWeek > 7)
					{
						siegeDayOfWeek = 7;
					}
					
					siegeHourOfDay = rs.getInt("siegeHourOfDay");
					
					if (siegeHourOfDay < 0 || siegeHourOfDay > 23)
					{
						siegeHourOfDay = 20;
					}
					
					ownerId = rs.getInt("owner");
				}
				
				if (getOwnerId() > 0)
				{
					L2Clan clan = ClanTable.getInstance().getClan(getOwnerId()); // Try to find clan instance
					if (clan != null)
					{
						clan.setHasFort(getFortId());
						fortOwner = clan;
					}
				}
				else
				{
					fortOwner = null;
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Fort.load: Something went wrong", e);
		}
	}
	
	// This method loads fort door data from database
	private void loadDoor()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_FORTRESS_DOOR_BY_ID))
		{
			statement.setInt(1, getFortId());
			
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
			LOGGER.error("Fort.loadDoor : Could not select data from fort_door table", e);
		}
	}
	
	private void loadDoorUpgrade()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_FORTRESS_DOOR_UPGRADE))
		{
			statement.setInt(1, getFortId());
			
			try (ResultSet rs = statement.executeQuery())
			{
				while (rs.next())
				{
					upgradeDoor(rs.getInt("id"), rs.getInt("hp"), rs.getInt("pDef"), rs.getInt("mDef"));
				}
			}
			
		}
		catch (Exception e)
		{
			LOGGER.error("Fort.loadFortDoorUpgrade : something went wrong", e);
		}
	}
	
	private void removeDoorUpgrade()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_FORTRESS_DOOR_UPGRADE))
		{
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Fort.removeDoorUpgrade : Could not delete data from fort_doorupgrade table");
		}
	}
	
	private void saveDoorUpgrade(int doorId, int hp, int pDef, int mDef)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_FORTRESS_DOOR_UPGRADE))
		{
			statement.setInt(1, doorId);
			statement.setInt(2, hp);
			statement.setInt(3, pDef);
			statement.setInt(4, mDef);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Fort.saveDoorUpgrade : Could not insert data in fort_doorupgrade table", e);
		}
	}
	
	private void updateOwnerInDB(L2Clan clan)
	{
		if (clan != null)
		{
			ownerId = clan.getClanId(); // Update owner id property
		}
		else
		{
			ownerId = 0; // Remove owner
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_FORTRESS_OWNER))
		{
			statement.setInt(1, getOwnerId());
			statement.setInt(2, getFortId());
			statement.executeUpdate();
			
			// Announce to clan memebers
			if (clan != null)
			{
				clan.setHasFort(getFortId()); // Set has fort flag for new owner
				Announcements.getInstance().announceToAll(clan.getName() + " has taken " + getName() + " fort!");
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				clan.broadcastToOnlineMembers(new PlaySound(1, "Siege_Victory", 0, 0, 0, 0, 0));
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Fort.updateOwnerInDB : Could not update data in fort table", e);
		}
	}
	
	public final int getFortId()
	{
		return fortId;
	}
	
	public final L2Clan getOwnerClan()
	{
		return fortOwner;
	}
	
	public final int getOwnerId()
	{
		return ownerId;
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
	
	public final List<L2DoorInstance> getDoors()
	{
		return doors;
	}
	
	public final FortSiege getSiege()
	{
		if (siege == null)
		{
			siege = new FortSiege(new Fort[]
			{
				this
			});
		}
		
		return siege;
	}
	
	public final Calendar getSiegeDate()
	{
		return siegeDate;
	}
	
	public final void setSiegeDate(final Calendar siegeDate)
	{
		this.siegeDate = siegeDate;
	}
	
	public final int getSiegeDayOfWeek()
	{
		return siegeDayOfWeek;
	}
	
	public final int getSiegeHourOfDay()
	{
		return siegeHourOfDay;
	}
	
	public final String getName()
	{
		return name;
	}
	
	public void updateClansReputation()
	{
		if (formerOwner != null)
		{
			if (formerOwner != ClanTable.getInstance().getClan(getOwnerId()))
			{
				final int maxreward = Math.max(0, formerOwner.getReputationScore());
				
				L2Clan owner = ClanTable.getInstance().getClan(getOwnerId());
				
				if (owner != null)
				{
					owner.setReputationScore(owner.getReputationScore() + Math.min(500, maxreward), true);
					owner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(owner));
				}
				
				owner = null;
			}
			else
			{
				formerOwner.setReputationScore(formerOwner.getReputationScore() + 250, true);
			}
			
			formerOwner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(formerOwner));
		}
		else
		{
			L2Clan owner = ClanTable.getInstance().getClan(getOwnerId());
			if (owner != null)
			{
				owner.setReputationScore(owner.getReputationScore() + 500, true);
				owner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(owner));
			}
			
			owner = null;
		}
	}
	
	private class endFortressSiege implements Runnable
	{
		private final Fort fort;
		private final L2Clan clan;
		
		public endFortressSiege(final Fort f, final L2Clan clan)
		{
			fort = f;
			this.clan = clan;
		}
		
		@Override
		public void run()
		{
			fort.Engrave(clan, 0);
		}
		
	}
}
