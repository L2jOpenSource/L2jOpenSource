package com.l2jfrozen.gameserver.model.entity.siege;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.csv.DoorTable;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.CastleManorManager;
import com.l2jfrozen.gameserver.managers.CastleManorManager.CropProcure;
import com.l2jfrozen.gameserver.managers.CastleManorManager.SeedProduction;
import com.l2jfrozen.gameserver.managers.CrownManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2Manor;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2jfrozen.gameserver.model.zone.type.L2CastleTeleportZone;
import com.l2jfrozen.gameserver.model.zone.type.L2CastleZone;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.updaters.CastleUpdater;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class Castle
{
	protected static Logger LOGGER = Logger.getLogger(Castle.class);
	
	private List<CropProcure> procure = new ArrayList<>();
	private List<SeedProduction> production = new ArrayList<>();
	private List<CropProcure> procureNext = new ArrayList<>();
	private List<SeedProduction> productionNext = new ArrayList<>();
	private boolean isNextPeriodApproved = false;
	
	private static final String CASTLE_MANOR_DELETE_PRODUCTION = "DELETE FROM castle_manor_production WHERE castle_id=?";
	
	private static final String CASTLE_MANOR_DELETE_PRODUCTION_PERIOD = "DELETE FROM castle_manor_production WHERE castle_id=? AND period=?";
	
	private static final String CASTLE_MANOR_DELETE_PROCURE = "DELETE FROM castle_manor_procure WHERE castle_id=?";
	
	private static final String CASTLE_MANOR_DELETE_PROCURE_PERIOD = "DELETE FROM castle_manor_procure WHERE castle_id=? AND period=?";
	
	private static final String CASTLE_UPDATE_CROP = "UPDATE castle_manor_procure SET can_buy=? WHERE crop_id=? AND castle_id=? AND period=?";
	
	private static final String CASTLE_UPDATE_SEED = "UPDATE castle_manor_production SET can_produce=? WHERE seed_id=? AND castle_id=? AND period=?";
	
	private static final String UPDATE_CASTLE_TREASURY_BY_CASTLE_ID = "UPDATE castle SET treasury=? WHERE id=?";
	private static final String UPDATE_CASTLE_TAX_BY_CASTLE_ID = "UPDATE castle SET taxPercent=? WHERE id=?";
	
	private static final String INSERT_CASTLE_DOOR_UPGRADE = "INSERT INTO castle_doorupgrade (doorId, hp, pDef, mDef) VALUES (?,?,?,?)";
	
	private static final String SELECT_CASTLE_DOORS_BY_CASTLE_ID = "SELECT castleId,id,name,x,y,z,range_xmin,range_ymin,range_zmin,range_xmax,range_ymax,range_zmax,hp,pDef,mDef FROM castle_door WHERE castleId=?";
	private static final String SELECT_DOOR_UPGRADE_BY_CASTLE_ID = "SELECT doorId,hp,pDef,mDef FROM castle_doorupgrade WHERE doorId IN (SELECT id FROM castle_door WHERE castleId=?)";
	private static final String DELETE_DOOR_UPGRADE_BY_CASTLE_ID = "DELETE FROM castle_doorupgrade WHERE doorId IN (SELECT id FROM castle_door WHERE castleId=?)";
	
	private static final String UPDATE_NO_CASTLE_CLAN_DATA = "UPDATE clan_data SET hasCastle=0 WHERE hasCastle=?";
	private static final String UPDATE_CASTLE_CLAN_DATA = "UPDATE clan_data SET hasCastle=? WHERE clan_id=?";
	
	private int castleId = 0;
	private final List<L2DoorInstance> doors = new ArrayList<>();
	private final List<String> doorDefault = new ArrayList<>();
	private String name = "";
	private int ownerId = 0;
	private Siege siege = null;
	private Calendar siegeDate;
	private int siegeDayOfWeek = 7; // Default to saturday
	private int siegeHourOfDay = 20; // Default to 8 pm server time
	private int castleTaxPercent = 0;
	private double taxRate = 0;
	private int treasury = 0;
	private L2CastleZone zone;
	private L2CastleTeleportZone teleZone;
	private L2Clan formerOwner = null;
	private int nbArtifact = 1;
	private final int[] gate =
	{
		Integer.MIN_VALUE,
		0,
		0
	};
	private final Map<Integer, Integer> engrave = new HashMap<>();
	
	// Castle IDs
	public static final int GLUDIO = 1;
	public static final int DION = 2;
	public static final int GIRAN = 3;
	public static final int OREN = 4;
	public static final int ADEN = 5;
	public static final int INNADRIL = 6;
	public static final int GODDARD = 7;
	public static final int RUNE = 8;
	public static final int SCHUTTGART = 9;
	
	public Castle(final int castleId)
	{
		this.castleId = castleId;
		
		if (this.castleId == GODDARD || castleId == SCHUTTGART)
		{
			nbArtifact = 2;
		}
		
		load();
		loadDoor();
	}
	
	public void Engrave(final L2Clan clan, final int objId)
	{
		engrave.put(objId, clan.getClanId());
		
		if (engrave.size() == nbArtifact)
		{
			boolean rst = true;
			
			for (final int id : engrave.values())
			{
				if (id != clan.getClanId())
				{
					rst = false;
				}
			}
			
			if (rst)
			{
				engrave.clear();
				setOwner(clan);
			}
			else
			{
				getSiege().announceToPlayer("Clan " + clan.getName() + " has finished to engrave one of the rulers.", true);
			}
		}
		else
		{
			getSiege().announceToPlayer("Clan " + clan.getName() + " has finished to engrave one of the rulers.", true);
		}
	}
	
	// This method add to the treasury
	/**
	 * Add amount to castle instance's treasury (warehouse).
	 * @param amount
	 */
	public void addToTreasury(int amount)
	{
		if (getOwnerId() <= 0)
		{
			return;
		}
		
		if (name.equalsIgnoreCase("Schuttgart") || name.equalsIgnoreCase("Goddard"))
		{
			Castle rune = CastleManager.getInstance().getCastle("rune");
			if (rune != null)
			{
				final int runeTax = (int) (amount * rune.getTaxRate());
				
				if (rune.getOwnerId() > 0)
				{
					rune.addToTreasury(runeTax);
				}
				
				amount -= runeTax;
			}
			
			rune = null;
		}
		if (!name.equalsIgnoreCase("aden") && !name.equalsIgnoreCase("Rune") && !name.equalsIgnoreCase("Schuttgart") && !name.equalsIgnoreCase("Goddard")) // If current castle instance is not Aden, Rune, Goddard or Schuttgart.
		{
			Castle aden = CastleManager.getInstance().getCastle("aden");
			
			if (aden != null)
			{
				final int adenTax = (int) (amount * aden.getTaxRate()); // Find out what Aden gets from the current castle instance's income
				
				if (aden.getOwnerId() > 0)
				{
					aden.addToTreasury(adenTax); // Only bother to really add the tax to the treasury if not npc owned
				}
				
				amount -= adenTax; // Subtract Aden's income from current castle instance's income
			}
			
			aden = null;
		}
		
		addToTreasuryNoTax(amount);
	}
	
	/**
	 * Add amount to castle instance's treasury (warehouse), no tax paying.
	 * @param  amount
	 * @return
	 */
	public boolean addToTreasuryNoTax(int amount)
	{
		if (getOwnerId() <= 0)
		{
			return false;
		}
		
		if (amount < 0)
		{
			amount *= -1;
			
			if (treasury < amount)
			{
				return false;
			}
			
			treasury -= amount;
		}
		else
		{
			if ((long) treasury + amount > Integer.MAX_VALUE)
			{
				treasury = Integer.MAX_VALUE;
			}
			else
			{
				treasury += amount;
			}
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_CASTLE_TREASURY_BY_CASTLE_ID))
		{
			statement.setInt(1, getTreasury());
			statement.setInt(2, getCastleId());
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Castle.addToTreasuryNoTax : Could not update castle treasury in castle table", e);
		}
		
		return true;
	}
	
	/**
	 * Move non clan members off castle area and to nearest town.<BR>
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
		if (zone == null)
		{
			return false;
		}
		
		return zone.isInsideZone(x, y, z);
	}
	
	/**
	 * Sets this castles zone
	 * @param zone
	 */
	public void setZone(final L2CastleZone zone)
	{
		this.zone = zone;
	}
	
	public L2CastleZone getZone()
	{
		return zone;
	}
	
	public void setTeleZone(final L2CastleTeleportZone zone)
	{
		teleZone = zone;
	}
	
	public L2CastleTeleportZone getTeleZone()
	{
		return teleZone;
	}
	
	/**
	 * Get the objects distance to this castle
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
	
	// This method is used to begin removing all castle upgrades
	public void removeUpgrade()
	{
		removeDoorUpgrade();
	}
	
	public void setOwner(final L2Clan clan)
	{
		// Remove old owner
		if (getOwnerId() > 0 && (clan == null || clan.getClanId() != getOwnerId()))
		{
			L2Clan oldOwner = ClanTable.getInstance().getClan(getOwnerId()); // Try to find clan instance
			
			if (oldOwner != null)
			{
				if (formerOwner == null)
				{
					formerOwner = oldOwner;
					if (Config.REMOVE_CASTLE_CIRCLETS)
					{
						CastleManager.getInstance().removeCirclet(formerOwner, getCastleId());
					}
				}
				oldOwner.setHasCastle(0); // Unset has castle flag for old owner
				Announcements.getInstance().announceToAll(oldOwner.getName() + " has lost " + getName() + " castle!");
				
				// remove crowns
				CrownManager.getInstance().checkCrowns(oldOwner);
			}
			
			oldOwner = null;
		}
		
		updateOwnerInDB(clan); // Update in database
		
		if (getSiege().getIsInProgress())
		{
			getSiege().midVictory(); // Mid victory phase of siege
		}
		
		updateClansReputation();
	}
	
	public void removeOwner(final L2Clan clan)
	{
		if (clan != null)
		{
			formerOwner = clan;
			
			if (Config.REMOVE_CASTLE_CIRCLETS)
			{
				CastleManager.getInstance().removeCirclet(formerOwner, getCastleId());
			}
			
			clan.setHasCastle(0);
			
			Announcements.getInstance().announceToAll(clan.getName() + " has lost " + getName() + " castle");
			clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
		}
		
		updateOwnerInDB(null);
		
		if (getSiege().getIsInProgress())
		{
			getSiege().midVictory();
		}
		
		updateClansReputation();
	}
	
	// This method updates the castle tax rate
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
		
		setTaxPercent(taxPercent);
		activeChar.sendMessage(getName() + " castle tax changed to " + taxPercent + "%.");
	}
	
	public void setTaxPercent(final int taxPercent)
	{
		castleTaxPercent = taxPercent;
		taxRate = castleTaxPercent / 100.0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_CASTLE_TAX_BY_CASTLE_ID))
		{
			statement.setInt(1, taxPercent);
			statement.setInt(2, getCastleId());
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Castle.setTaxPercent : Could not update tax percent in castle table", e);
		}
	}
	
	/**
	 * Respawn all doors on castle grounds<BR>
	 * <BR>
	 */
	public void spawnDoor()
	{
		spawnDoor(false);
	}
	
	/**
	 * Respawn all doors on castle grounds
	 * @param isDoorWeak
	 */
	public void spawnDoor(final boolean isDoorWeak)
	{
		for (int i = 0; i < getDoors().size(); i++)
		{
			L2DoorInstance door = getDoors().get(i);
			if (door.getCurrentHp() <= 0)
			{
				door.decayMe(); // Kill current if not killed already
				door = DoorTable.parseList(doorDefault.get(i));
				
				if (isDoorWeak)
				{
					door.setCurrentHpDirect(door.getMaxHp() / 2);
				}
				else
				{
					door.setCurrentHpDirect(door.getMaxHp());
				}
				
				door.spawnMe(door.getX(), door.getY(), door.getZ());
				getDoors().set(i, door);
			}
			else if (door.isOpen())
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
			door.setCurrentHpDirect(door.getMaxHp() + hp);
			
			saveDoorUpgrade(doorId, hp, pDef, mDef);
			return;
		}
	}
	
	private void load()
	{
		Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;
			
			con = L2DatabaseFactory.getInstance().getConnection();
			
			statement = con.prepareStatement("Select * from castle where id = ?");
			statement.setInt(1, getCastleId());
			rs = statement.executeQuery();
			
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
				
				castleTaxPercent = rs.getInt("taxPercent");
				treasury = rs.getInt("treasury");
			}
			
			rs.close();
			DatabaseUtils.close(statement);
			statement = null;
			rs = null;
			
			taxRate = castleTaxPercent / 100.0;
			
			statement = con.prepareStatement("Select clan_id from clan_data where hasCastle = ?");
			statement.setInt(1, getCastleId());
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				ownerId = rs.getInt("clan_id");
			}
			
			if (getOwnerId() > 0)
			{
				L2Clan clan = ClanTable.getInstance().getClan(getOwnerId()); // Try to find clan instance
				ThreadPoolManager.getInstance().scheduleGeneral(new CastleUpdater(clan, 1), 3600000); // Schedule owner tasks to start running
				clan = null;
			}
			
			rs.close();
			DatabaseUtils.close(statement);
			statement = null;
			rs = null;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	// This method loads castle door data from database
	private void loadDoor()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CASTLE_DOORS_BY_CASTLE_ID))
		{
			statement.setInt(1, getCastleId());
			
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
					
					door = null;
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Castle.loadDoor : Could not select castle doors from castle_door table", e);
		}
	}
	
	// This method loads castle door upgrade data from database
	private void loadDoorUpgrade()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_DOOR_UPGRADE_BY_CASTLE_ID))
		{
			statement.setInt(1, getCastleId());
			
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
			LOGGER.error("Castle.loadDoorUpgrade : Could not select data from castle_doorupgrade table", e);
		}
	}
	
	private void removeDoorUpgrade()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_DOOR_UPGRADE_BY_CASTLE_ID))
		{
			statement.setInt(1, getCastleId());
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Castle.removeDoorUpgrade : Could not delete data from castle_doorupgrade table", e);
		}
	}
	
	private void saveDoorUpgrade(final int doorId, final int hp, final int pDef, final int mDef)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_CASTLE_DOOR_UPGRADE))
		{
			statement.setInt(1, doorId);
			statement.setInt(2, hp);
			statement.setInt(3, pDef);
			statement.setInt(4, mDef);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Castle.saveDoorUpgrade : Could not save door upgrade in castle_doorupgrade table", e);
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
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement statement = con.prepareStatement(UPDATE_NO_CASTLE_CLAN_DATA))
			{
				statement.setInt(1, getCastleId());
				statement.executeUpdate();
			}
			
			try (PreparedStatement statement = con.prepareStatement(UPDATE_CASTLE_CLAN_DATA))
			{
				statement.setInt(1, getCastleId());
				statement.setInt(2, getOwnerId());
				statement.executeUpdate();
			}
			
			// Announce to clan memebers
			if (clan != null)
			{
				clan.setHasCastle(getCastleId()); // Set has castle flag for new owner
				Announcements.getInstance().announceToAll(clan.getName() + " has taken " + getName() + " castle!");
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				clan.broadcastToOnlineMembers(new PlaySound(1, "Siege_Victory", 0, 0, 0, 0, 0));
				// give crowns
				CrownManager.getInstance().checkCrowns(clan);
				
				ThreadPoolManager.getInstance().scheduleGeneral(new CastleUpdater(clan, 1), 3600000); // Schedule owner tasks to start running
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Castle.updateOwnerInDB : Could not update hasCastle owner in clan_data table", e);
		}
	}
	
	public int getCastleId()
	{
		return castleId;
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
	
	public final String getName()
	{
		return name;
	}
	
	public final int getOwnerId()
	{
		return ownerId;
	}
	
	public final Siege getSiege()
	{
		if (siege == null)
		{
			siege = new Siege(new Castle[]
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
	
	public final int getSiegeDayOfWeek()
	{
		return siegeDayOfWeek;
	}
	
	public final int getSiegeHourOfDay()
	{
		return siegeHourOfDay;
	}
	
	public final int getTaxPercent()
	{
		return castleTaxPercent;
	}
	
	public final double getTaxRate()
	{
		return taxRate;
	}
	
	public final int getTreasury()
	{
		return treasury;
	}
	
	public List<SeedProduction> getSeedProduction(final int period)
	{
		return period == CastleManorManager.PERIOD_CURRENT ? production : productionNext;
	}
	
	public List<CropProcure> getCropProcure(final int period)
	{
		return period == CastleManorManager.PERIOD_CURRENT ? procure : procureNext;
	}
	
	public void setSeedProduction(final List<SeedProduction> seed, final int period)
	{
		if (period == CastleManorManager.PERIOD_CURRENT)
		{
			production = seed;
		}
		else
		{
			productionNext = seed;
		}
	}
	
	public void setCropProcure(final List<CropProcure> crop, final int period)
	{
		if (period == CastleManorManager.PERIOD_CURRENT)
		{
			procure = crop;
		}
		else
		{
			procureNext = crop;
		}
	}
	
	public synchronized SeedProduction getSeed(final int seedId, final int period)
	{
		for (final SeedProduction seed : getSeedProduction(period))
		{
			if (seed.getId() == seedId)
			{
				return seed;
			}
		}
		return null;
	}
	
	public synchronized CropProcure getCrop(final int cropId, final int period)
	{
		for (final CropProcure crop : getCropProcure(period))
		{
			if (crop.getId() == cropId)
			{
				return crop;
			}
		}
		return null;
	}
	
	public int getManorCost(final int period)
	{
		List<CropProcure> procure;
		List<SeedProduction> production;
		
		if (period == CastleManorManager.PERIOD_CURRENT)
		{
			procure = this.procure;
			production = this.production;
		}
		else
		{
			procure = procureNext;
			production = productionNext;
		}
		
		int total = 0;
		
		if (production != null)
		{
			for (final SeedProduction seed : production)
			{
				total += L2Manor.getInstance().getSeedBuyPrice(seed.getId()) * seed.getStartProduce();
			}
		}
		
		if (procure != null)
		{
			for (final CropProcure crop : procure)
			{
				total += crop.getPrice() * crop.getStartAmount();
			}
		}
		
		procure = null;
		production = null;
		
		return total;
	}
	
	// save manor production data
	public void saveSeedData()
	{
		Connection con = null;
		PreparedStatement statement;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			statement = con.prepareStatement(CASTLE_MANOR_DELETE_PRODUCTION);
			statement.setInt(1, getCastleId());
			
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
			
			if (production != null)
			{
				int count = 0;
				
				String query = "INSERT INTO castle_manor_production VALUES ";
				String values[] = new String[production.size()];
				
				for (final SeedProduction s : production)
				{
					values[count] = "(" + getCastleId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + CastleManorManager.PERIOD_CURRENT + ")";
					count++;
				}
				
				if (values.length > 0)
				{
					query += values[0];
					for (int i = 1; i < values.length; i++)
					{
						query += "," + values[i];
					}
					statement = con.prepareStatement(query);
					statement.execute();
					DatabaseUtils.close(statement);
					statement = null;
				}
				
				query = null;
				values = null;
			}
			
			if (productionNext != null)
			{
				int count = 0;
				
				String query = "INSERT INTO castle_manor_production VALUES ";
				String values[] = new String[productionNext.size()];
				
				for (final SeedProduction s : productionNext)
				{
					values[count] = "(" + getCastleId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + CastleManorManager.PERIOD_NEXT + ")";
					count++;
				}
				
				if (values.length > 0)
				{
					query += values[0];
					
					for (int i = 1; i < values.length; i++)
					{
						query += "," + values[i];
					}
					
					statement = con.prepareStatement(query);
					statement.execute();
					DatabaseUtils.close(statement);
					statement = null;
				}
				
				query = null;
				values = null;
			}
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.info("Error adding seed production data for castle " + getName() + ": " + e.getMessage());
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	// save manor production data for specified period
	public void saveSeedData(final int period)
	{
		Connection con = null;
		PreparedStatement statement;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			statement = con.prepareStatement(CASTLE_MANOR_DELETE_PRODUCTION_PERIOD);
			statement.setInt(1, getCastleId());
			statement.setInt(2, period);
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
			
			List<SeedProduction> prod = null;
			prod = getSeedProduction(period);
			
			if (prod != null)
			{
				int count = 0;
				
				String query = "INSERT INTO castle_manor_production VALUES ";
				String values[] = new String[prod.size()];
				
				for (final SeedProduction s : prod)
				{
					values[count] = "(" + getCastleId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + period + ")";
					count++;
				}
				if (values.length > 0)
				{
					query += values[0];
					for (int i = 1; i < values.length; i++)
					{
						query += "," + values[i];
					}
					statement = con.prepareStatement(query);
					statement.execute();
					DatabaseUtils.close(statement);
					statement = null;
				}
				
				query = null;
				values = null;
			}
			
			prod = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.info("Error adding seed production data for castle " + getName() + ": " + e.getMessage());
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	// save crop procure data
	public void saveCropData()
	{
		Connection con = null;
		PreparedStatement statement;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			statement = con.prepareStatement(CASTLE_MANOR_DELETE_PROCURE);
			statement.setInt(1, getCastleId());
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
			
			if (procure != null)
			{
				int count = 0;
				
				String query = "INSERT INTO castle_manor_procure VALUES ";
				String values[] = new String[procure.size()];
				
				for (final CropProcure cp : procure)
				{
					values[count] = "(" + getCastleId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + CastleManorManager.PERIOD_CURRENT + ")";
					count++;
				}
				
				if (values.length > 0)
				{
					query += values[0];
					
					for (int i = 1; i < values.length; i++)
					{
						query += "," + values[i];
					}
					
					statement = con.prepareStatement(query);
					statement.execute();
					DatabaseUtils.close(statement);
					statement = null;
				}
				
				query = null;
				values = null;
			}
			
			if (procureNext != null)
			{
				int count = 0;
				
				String query = "INSERT INTO castle_manor_procure VALUES ";
				String values[] = new String[procureNext.size()];
				
				for (final CropProcure cp : procureNext)
				{
					values[count] = "(" + getCastleId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + CastleManorManager.PERIOD_NEXT + ")";
					count++;
				}
				
				if (values.length > 0)
				{
					query += values[0];
					for (int i = 1; i < values.length; i++)
					{
						query += "," + values[i];
					}
					statement = con.prepareStatement(query);
					statement.execute();
					DatabaseUtils.close(statement);
					statement = null;
				}
				
				query = null;
				values = null;
			}
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.info("Error adding crop data for castle " + getName() + ": " + e.getMessage());
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	// save crop procure data for specified period
	public void saveCropData(final int period)
	{
		Connection con = null;
		PreparedStatement statement;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			statement = con.prepareStatement(CASTLE_MANOR_DELETE_PROCURE_PERIOD);
			statement.setInt(1, getCastleId());
			statement.setInt(2, period);
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
			
			List<CropProcure> proc = null;
			proc = getCropProcure(period);
			
			if (proc != null)
			{
				int count = 0;
				
				String query = "INSERT INTO castle_manor_procure VALUES ";
				final String values[] = new String[proc.size()];
				
				for (final CropProcure cp : proc)
				{
					values[count] = "(" + getCastleId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + period + ")";
					count++;
				}
				
				if (values.length > 0)
				{
					query += values[0];
					
					for (int i = 1; i < values.length; i++)
					{
						query += "," + values[i];
					}
					
					statement = con.prepareStatement(query);
					statement.execute();
					DatabaseUtils.close(statement);
					statement = null;
				}
				
				query = null;
			}
			
			proc = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.info("Error adding crop data for castle " + getName() + ": " + e.getMessage());
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	public void updateCrop(final int cropId, final int amount, final int period)
	{
		Connection con = null;
		PreparedStatement statement;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			statement = con.prepareStatement(CASTLE_UPDATE_CROP);
			statement.setInt(1, amount);
			statement.setInt(2, cropId);
			statement.setInt(3, getCastleId());
			statement.setInt(4, period);
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.info("Error adding crop data for castle " + getName() + ": " + e.getMessage());
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	public void updateSeed(int seedId, int amount, int period)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(CASTLE_UPDATE_SEED))
		{
			statement.setInt(1, amount);
			statement.setInt(2, seedId);
			statement.setInt(3, getCastleId());
			statement.setInt(4, period);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Castle.updateSeed : Error adding seed production data for castle " + getName(), e);
		}
	}
	
	public boolean isNextPeriodApproved()
	{
		return isNextPeriodApproved;
	}
	
	public void setNextPeriodApproved(final boolean val)
	{
		isNextPeriodApproved = val;
	}
	
	public void updateClansReputation()
	{
		if (formerOwner != null)
		{
			if (formerOwner != ClanTable.getInstance().getClan(getOwnerId()))
			{
				final int maxreward = Math.max(0, formerOwner.getReputationScore());
				formerOwner.setReputationScore(formerOwner.getReputationScore() - 1000, true);
				
				L2Clan owner = ClanTable.getInstance().getClan(getOwnerId());
				
				if (owner != null)
				{
					owner.setReputationScore(owner.getReputationScore() + Math.min(1000, maxreward), true);
					owner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(owner));
				}
				
				owner = null;
			}
			else
			{
				formerOwner.setReputationScore(formerOwner.getReputationScore() + 500, true);
			}
			
			formerOwner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(formerOwner));
		}
		else
		{
			L2Clan owner = ClanTable.getInstance().getClan(getOwnerId());
			
			if (owner != null)
			{
				owner.setReputationScore(owner.getReputationScore() + 1000, true);
				owner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(owner));
			}
			
			owner = null;
		}
	}
	
	public void createClanGate(final int x, final int y, final int z)
	{
		gate[0] = x;
		gate[1] = y;
		gate[2] = z;
	}
	
	/** Optimized as much as possible. */
	public void destroyClanGate()
	{
		gate[0] = Integer.MIN_VALUE;
	}
	
	/**
	 * This method must always be called before using gate coordinate retrieval methods! Optimized as much as possible.
	 * @return is a Clan Gate available
	 */
	
	public boolean isGateOpen()
	{
		return gate[0] != Integer.MIN_VALUE;
	}
	
	public int getGateX()
	{
		return gate[0];
	}
	
	public int getGateY()
	{
		return gate[1];
	}
	
	public int getGateZ()
	{
		return gate[2];
	}
	
	public void oustAllPlayers()
	{
		if (Config.DEBUG && teleZone != null)
		{
			LOGGER.info("Castle Teleport Zone ID: " + teleZone.getZoneId());
			LOGGER.info("Players Number in Castle Teleport Zone: " + teleZone.getAllPlayers().size());
			for (final L2Character actual : teleZone.getAllPlayers())
			{
				LOGGER.info("	Player Name: " + actual.getName());
			}
		}
		getTeleZone().oustAllPlayers();
	}
	
	/**
	 * @return
	 */
	public boolean isSiegeInProgress()
	{
		if (siege != null)
		{
			return siege.getIsInProgress();
		}
		
		return false;
	}
}
