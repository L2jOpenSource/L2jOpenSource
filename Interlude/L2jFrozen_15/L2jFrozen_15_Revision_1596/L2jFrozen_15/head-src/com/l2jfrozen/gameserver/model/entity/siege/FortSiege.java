package com.l2jfrozen.gameserver.model.entity.siege;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.crypt.nProtect;
import com.l2jfrozen.crypt.nProtect.RestrictionType;
import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.managers.FortSiegeGuardManager;
import com.l2jfrozen.gameserver.managers.FortSiegeManager;
import com.l2jfrozen.gameserver.managers.FortSiegeManager.SiegeSpawn;
import com.l2jfrozen.gameserver.managers.MercTicketManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2SiegeClan;
import com.l2jfrozen.gameserver.model.L2SiegeClan.SiegeClanType;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2ArtefactInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2CommanderInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.FortressSiegeInfo;
import com.l2jfrozen.gameserver.network.serverpackets.RelationChanged;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.network.serverpackets.UserInfo;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * The Class FortSiege.
 * @author programmos
 */
public class FortSiege
{
	
	/** The Constant LOGGER. */
	protected static final Logger LOGGER = Logger.getLogger(FortSiege.class);
	
	/**
	 * The Enum TeleportWhoType.
	 */
	public static enum TeleportWhoType
	{
		
		/** The All. */
		All,
		
		/** The Attacker. */
		Attacker,
		
		/** The Defender not owner. */
		DefenderNotOwner,
		
		/** The Owner. */
		Owner,
		
		/** The Spectator. */
		Spectator
	}
	
	public class ScheduleEndSiegeTask implements Runnable
	{
		
		private final Fort fortInst;
		
		/**
		 * Instantiates a new schedule end siege task.
		 * @param pFort the fort
		 */
		public ScheduleEndSiegeTask(final Fort pFort)
		{
			fortInst = pFort;
		}
		
		@Override
		public void run()
		{
			if (!getIsInProgress())
			{
				return;
			}
			
			try
			{
				final long timeRemaining = siegeEndDate.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
				
				if (timeRemaining > 3600000)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(fortInst), timeRemaining - 3600000); // Prepare task for 1 hr left.
				}
				else if (timeRemaining <= 3600000 && timeRemaining > 600000)
				{
					announceToPlayer((timeRemaining / 60000) + " minute(s) until " + getFort().getName() + " siege conclusion.", true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(fortInst), timeRemaining - 600000); // Prepare task for 10 minute left.
				}
				else if (timeRemaining <= 600000 && timeRemaining > 300000)
				{
					announceToPlayer((timeRemaining / 60000) + " minute(s) until " + getFort().getName() + " siege conclusion.", true);
					
					// Prepare task for 5 minute left.
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(fortInst), timeRemaining - 300000);
				}
				else if (timeRemaining <= 300000 && timeRemaining > 10000)
				{
					announceToPlayer((timeRemaining / 60000) + " minute(s) until " + getFort().getName() + " siege conclusion.", true);
					
					// Prepare task for 10 seconds count down
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(fortInst), timeRemaining - 10000);
				}
				else if (timeRemaining <= 10000 && timeRemaining > 0)
				{
					announceToPlayer(getFort().getName() + " siege " + (timeRemaining / 1000) + " second(s) left!", true);
					
					// Prepare task for second count down
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(fortInst), timeRemaining);
				}
				else
				{
					fortInst.getSiege().endSiege();
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
	
	/**
	 * The Class ScheduleStartSiegeTask.
	 */
	public class ScheduleStartSiegeTask implements Runnable
	{
		
		private final Fort fortInst;
		
		/**
		 * Instantiates a new schedule start siege task.
		 * @param pFort the fort
		 */
		public ScheduleStartSiegeTask(final Fort pFort)
		{
			fortInst = pFort;
		}
		
		@Override
		public void run()
		{
			if (getIsInProgress())
			{
				return;
			}
			
			try
			{
				final long timeRemaining = getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
				if (timeRemaining > 86400000)
				{
					// Prepare task for 24 before siege start to end registration
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(fortInst), timeRemaining - 86400000);
				}
				else if (timeRemaining <= 86400000 && timeRemaining > 13600000)
				{
					// Prepare task for 1 hr left before siege start.
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(fortInst), timeRemaining - 13600000);
				}
				else if (timeRemaining <= 13600000 && timeRemaining > 600000)
				{
					announceToPlayer((timeRemaining / 60000) + " minute(s) until " + getFort().getName() + " siege begin.", false);
					
					// Prepare task for 10 minute left.
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(fortInst), timeRemaining - 600000);
				}
				else if (timeRemaining <= 600000 && timeRemaining > 300000)
				{
					announceToPlayer("The registration term for " + getFort().getName() + " has ended.", false);
					
					isRegistrationOver = true;
					
					clearSiegeWaitingClan();
					
					// Prepare task for 5 minute left.
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(fortInst), timeRemaining - 300000);
				}
				else if (timeRemaining <= 300000 && timeRemaining > 10000)
				{
					announceToPlayer((timeRemaining / 60000) + " minute(s) until " + getFort().getName() + " siege begin.", false);
					
					// Prepare task for 10 seconds count down
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(fortInst), timeRemaining - 10000);
				}
				else if (timeRemaining <= 10000 && timeRemaining > 0)
				{
					announceToPlayer(getFort().getName() + " siege " + (timeRemaining / 1000) + " second(s) to start!", false);
					
					// Prepare task for second count down
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(fortInst), timeRemaining);
				}
				else
				{
					fortInst.getSiege().startSiege();
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
	
	// Attacker and Defender
	private final List<L2SiegeClan> attackerClans = new ArrayList<>();
	private final List<L2SiegeClan> defenderClans = new ArrayList<>();
	private final List<L2SiegeClan> defenderWaitingClans = new ArrayList<>();
	private int defenderRespawnDelayPenalty;
	// Fort setting
	private List<L2CommanderInstance> commanders = new ArrayList<>();
	private List<L2ArtefactInstance> combatflag = new ArrayList<>();
	private final Fort[] fort;
	private boolean isInProgress = false;
	private boolean isScheduled = false;
	private boolean isNormalSide = true; // true = Atk is Atk, false = Atk is Def
	protected boolean isRegistrationOver = false;
	protected Calendar siegeEndDate;
	private FortSiegeGuardManager siegeGuardManager;
	protected Calendar siegeRegistrationEndDate;
	
	/**
	 * Instantiates a new fort siege.
	 * @param fort the fort
	 */
	public FortSiege(final Fort[] fort)
	{
		this.fort = fort;
		
		checkAutoTask();
	}
	
	public void endSiege()
	{
		if (getIsInProgress())
		{
			announceToPlayer("The siege of " + getFort().getName() + " has finished!", false);
			
			if (getFort().getOwnerId() <= 0)
			{
				announceToPlayer("The siege of " + getFort().getName() + " has ended in a draw.", false);
			}
			
			// Removes all flags. Note: Remove flag before teleporting players
			removeFlags();
			unSpawnFlags();
			
			// Teleport to the second closest town
			teleportPlayer(FortSiege.TeleportWhoType.Attacker, MapRegionTable.TeleportWhereType.Town);
			
			// Teleport to the second closest town
			teleportPlayer(FortSiege.TeleportWhoType.DefenderNotOwner, MapRegionTable.TeleportWhereType.Town);
			
			// Teleport to the second closest town
			teleportPlayer(FortSiege.TeleportWhoType.Spectator, MapRegionTable.TeleportWhereType.Town);
			
			// Flag so that siege instance can be started
			isInProgress = false;
			
			updatePlayerSiegeStateFlags(true);
			
			// Save fort specific data
			saveFortSiege();
			
			// Clear siege clan from db
			clearSiegeClan();
			
			// Remove commander from this fort
			removeCommander();
			
			siegeGuardManager.unspawnSiegeGuard(); // Remove all spawned siege guard from this fort
			
			if (getFort().getOwnerId() > 0)
			{
				siegeGuardManager.removeMercs();
			}
			
			// Respawn door to fort
			getFort().spawnDoor();
			getFort().getZone().updateZoneStatusForCharactersInside();
		}
	}
	
	/**
	 * Removes the defender.
	 * @param sc the sc
	 */
	private void removeDefender(final L2SiegeClan sc)
	{
		if (sc != null)
		{
			getDefenderClans().remove(sc);
		}
	}
	
	/**
	 * Removes the attacker.
	 * @param sc the sc
	 */
	private void removeAttacker(final L2SiegeClan sc)
	{
		if (sc != null)
		{
			getAttackerClans().remove(sc);
		}
	}
	
	/**
	 * Adds the defender.
	 * @param sc   the sc
	 * @param type the type
	 */
	private void addDefender(final L2SiegeClan sc, final SiegeClanType type)
	{
		if (sc == null)
		{
			return;
		}
		
		sc.setType(type);
		getDefenderClans().add(sc);
	}
	
	/**
	 * Adds the attacker.
	 * @param sc the sc
	 */
	private void addAttacker(final L2SiegeClan sc)
	{
		if (sc == null)
		{
			return;
		}
		
		sc.setType(SiegeClanType.ATTACKER);
		getAttackerClans().add(sc);
	}
	
	/**
	 * When control of fort changed during siege<BR>
	 * <BR>
	 * .
	 */
	public void midVictory()
	{
		if (getIsInProgress()) // Siege still in progress
		{
			// defenders to attacker
			for (final L2SiegeClan sc : getDefenderClans())
			{
				if (sc != null)
				{
					removeDefender(sc);
					addAttacker(sc);
				}
			}
			
			// owner as defender
			L2SiegeClan sc_newowner = getAttackerClan(getFort().getOwnerId());
			removeAttacker(sc_newowner);
			addDefender(sc_newowner, SiegeClanType.OWNER);
			endSiege();
			sc_newowner = null;
			
			return;
		}
	}
	
	public void startSiege()
	{
		if (!getIsInProgress())
		{
			if (getAttackerClans().size() <= 0)
			{
				SystemMessage sm;
				
				if (getFort().getOwnerId() <= 0)
				{
					sm = new SystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_SIEGE_WAS_CANCELED_BECAUSE_NO_CLANS_PARTICIPATED);
				}
				
				sm.addString(getFort().getName());
				Announcements.getInstance().announceToAll(sm);
				sm = null;
				
				return;
			}
			
			isNormalSide = true; // Atk is now atk
			isInProgress = true; // Flag so that same siege instance cannot be started again
			isScheduled = false;
			
			// Load siege clan from db
			loadSiegeClan();
			updatePlayerSiegeStateFlags(false);
			
			// Teleport to the closest town
			teleportPlayer(FortSiege.TeleportWhoType.Attacker, MapRegionTable.TeleportWhereType.Town);
			
			// Spawn commander
			spawnCommander(getFort().getFortId());
			
			// Spawn door
			getFort().spawnDoor();
			
			// Spawn siege guard
			spawnSiegeGuard();
			
			// remove the tickets from the ground
			MercTicketManager.getInstance().deleteTickets(getFort().getFortId());
			
			// Reset respawn delay
			defenderRespawnDelayPenalty = 0;
			
			getFort().getZone().updateZoneStatusForCharactersInside();
			
			// Schedule a task to prepare auto siege end
			siegeEndDate = Calendar.getInstance();
			siegeEndDate.add(Calendar.MINUTE, FortSiegeManager.getInstance().getSiegeLength());
			nProtect.getInstance().checkRestriction(null, RestrictionType.RESTRICT_EVENT, new Object[]
			{
				FortSiege.class,
				this
			});
			ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(getFort()), 1000); // Prepare auto end task
			
			announceToPlayer("The siege of " + getFort().getName() + " has started!", false);
			saveFortSiege();
			FortSiegeManager.getInstance().addSiege(this);
			
		}
	}
	
	/**
	 * Announce to player.<BR>
	 * <BR>
	 * @param message    The String of the message to send to player
	 * @param inAreaOnly The boolean flag to show message to players in area only.
	 */
	public void announceToPlayer(final String message, final boolean inAreaOnly)
	{
		if (inAreaOnly)
		{
			getFort().getZone().announceToPlayers(message);
			return;
		}
		
		// Get all players
		for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			player.sendMessage(message);
		}
	}
	
	/**
	 * Update player siege state flags.
	 * @param clear the clear
	 */
	public void updatePlayerSiegeStateFlags(final boolean clear)
	{
		L2Clan clan;
		for (final L2SiegeClan siegeclan : getAttackerClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for (final L2PcInstance member : clan.getOnlineMembers(""))
			{
				if (clear)
				{
					member.setSiegeState((byte) 0);
				}
				else
				{
					member.setSiegeState((byte) 1);
				}
				
				member.sendPacket(new UserInfo(member));
				
				for (final L2PcInstance player : member.getKnownList().getKnownPlayers().values())
				{
					player.sendPacket(new RelationChanged(member, member.getRelation(player), member.isAutoAttackable(player)));
				}
			}
		}
		
		for (final L2SiegeClan siegeclan : getDefenderClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for (final L2PcInstance member : clan.getOnlineMembers(""))
			{
				if (clear)
				{
					member.setSiegeState((byte) 0);
				}
				else
				{
					member.setSiegeState((byte) 2);
				}
				
				member.sendPacket(new UserInfo(member));
				
				for (final L2PcInstance player : member.getKnownList().getKnownPlayers().values())
				{
					player.sendPacket(new RelationChanged(member, member.getRelation(player), member.isAutoAttackable(player)));
				}
			}
		}
		
		clan = null;
	}
	
	/**
	 * Approve clan as defender for siege<BR>
	 * <BR>
	 * .
	 * @param clanId The int of player's clan id
	 */
	public void approveSiegeDefenderClan(final int clanId)
	{
		if (clanId <= 0)
		{
			return;
		}
		
		saveSiegeClan(ClanTable.getInstance().getClan(clanId), 0, true);
		loadSiegeClan();
	}
	
	/**
	 * Check if in zone.
	 * @param  object the object
	 * @return        true if object is inside the zone
	 */
	public boolean checkIfInZone(final L2Object object)
	{
		return checkIfInZone(object.getX(), object.getY(), object.getZ());
	}
	
	/**
	 * Check if in zone.
	 * @param  x the x
	 * @param  y the y
	 * @param  z the z
	 * @return   true if object is inside the zone
	 */
	public boolean checkIfInZone(final int x, final int y, final int z)
	{
		return getIsInProgress() && getFort().checkIfInZone(x, y, z); // Fort zone during siege
	}
	
	/**
	 * Check is attacker.
	 * @param  clan The L2Clan of the player
	 * @return      true if clan is attacker
	 */
	public boolean checkIsAttacker(final L2Clan clan)
	{
		return getAttackerClan(clan) != null;
	}
	
	/**
	 * Check is defender.
	 * @param  clan The L2Clan of the player
	 * @return      true if clan is defender
	 */
	public boolean checkIsDefender(final L2Clan clan)
	{
		return getDefenderClan(clan) != null;
	}
	
	/**
	 * Check is defender waiting.
	 * @param  clan The L2Clan of the player
	 * @return      true if clan is defender waiting approval
	 */
	public boolean checkIsDefenderWaiting(final L2Clan clan)
	{
		return getDefenderWaitingClan(clan) != null;
	}
	
	/**
	 * Clear all registered siege clans from database for fort.
	 */
	public void clearSiegeClan()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=?");
			statement.setInt(1, getFort().getFortId());
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
			
			if (getFort().getOwnerId() > 0)
			{
				PreparedStatement statement2 = con.prepareStatement("DELETE FROM fortsiege_clans WHERE clan_id=?");
				statement2.setInt(1, getFort().getOwnerId());
				statement2.execute();
				statement2.close();
				statement2 = null;
			}
			
			getAttackerClans().clear();
			getDefenderClans().clear();
			getDefenderWaitingClans().clear();
		}
		catch (final Exception e)
		{
			LOGGER.warn("Exception: clearSiegeClan(): " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	/** Set the date for the next siege. */
	private void clearSiegeDate()
	{
		getFort().getSiegeDate().setTimeInMillis(0);
		isRegistrationOver = false; // Allow registration for next siege
	}
	
	/**
	 * Clear all siege clans waiting for approval from database for fort.
	 */
	public void clearSiegeWaitingClan()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=? and type = 2");
			statement.setInt(1, getFort().getFortId());
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
			
			getDefenderWaitingClans().clear();
		}
		catch (final Exception e)
		{
			LOGGER.warn("Exception: clearSiegeWaitingClan(): " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	/**
	 * Gets the attackers in zone.
	 * @return list of L2PcInstance registered as attacker in the zone.
	 */
	public List<L2PcInstance> getAttackersInZone()
	{
		List<L2PcInstance> players = new ArrayList<>();
		L2Clan clan;
		
		for (final L2SiegeClan siegeclan : getAttackerClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			
			for (final L2PcInstance player : clan.getOnlineMembers(""))
			{
				if (checkIfInZone(player.getX(), player.getY(), player.getZ()))
				{
					players.add(player);
				}
			}
		}
		
		clan = null;
		
		return players;
	}
	
	/**
	 * Return list of L2PcInstance registered as defender but not owner in the zone.
	 * @return the defenders but not owners in zone
	 */
	public List<L2PcInstance> getDefendersButNotOwnersInZone()
	{
		List<L2PcInstance> players = new ArrayList<>();
		L2Clan clan;
		
		for (final L2SiegeClan siegeclan : getDefenderClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			
			if (clan.getClanId() == getFort().getOwnerId())
			{
				continue;
			}
			
			for (final L2PcInstance player : clan.getOnlineMembers(""))
			{
				if (checkIfInZone(player.getX(), player.getY(), player.getZ()))
				{
					players.add(player);
				}
			}
		}
		
		clan = null;
		
		return players;
	}
	
	/**
	 * Return list of L2PcInstance in the zone.
	 * @return the players in zone
	 */
	public List<L2PcInstance> getPlayersInZone()
	{
		return getFort().getZone().getAllPlayers();
	}
	
	/**
	 * Return list of L2PcInstance owning the fort in the zone.
	 * @return the owners in zone
	 */
	public List<L2PcInstance> getOwnersInZone()
	{
		List<L2PcInstance> players = new ArrayList<>();
		L2Clan clan;
		
		for (final L2SiegeClan siegeclan : getDefenderClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			
			if (clan.getClanId() != getFort().getOwnerId())
			{
				continue;
			}
			
			for (final L2PcInstance player : clan.getOnlineMembers(""))
			{
				if (checkIfInZone(player.getX(), player.getY(), player.getZ()))
				{
					players.add(player);
				}
			}
		}
		
		clan = null;
		
		return players;
	}
	
	/**
	 * Return list of L2PcInstance not registered as attacker or defender in the zone.
	 * @return the spectators in zone
	 */
	public List<L2PcInstance> getSpectatorsInZone()
	{
		List<L2PcInstance> players = new ArrayList<>();
		
		for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			// quick check from player states, which don't include siege number however
			if (!player.isInsideZone(L2Character.ZONE_SIEGE) || player.getSiegeState() != 0)
			{
				continue;
			}
			
			if (checkIfInZone(player.getX(), player.getY(), player.getZ()))
			{
				players.add(player);
			}
		}
		
		return players;
	}
	
	/**
	 * Control Tower was skilled.
	 * @param ct the ct
	 */
	public void killedCT(final L2NpcInstance ct)
	{
		defenderRespawnDelayPenalty += FortSiegeManager.getInstance().getControlTowerLosePenalty(); // Add respawn penalty to defenders for each control tower lose
	}
	
	/**
	 * Commanderr was skilled.
	 * @param ct the ct
	 */
	public void killedCommander(final L2CommanderInstance ct)
	{
		if (commanders != null)
		{
			commanders.remove(ct);
			
			if (commanders.size() == 0)
			{
				spawnFlag(getFort().getFortId());
				// LOGGER.info("Commander empty !");
			}
		}
		
	}
	
	/**
	 * Remove the flag that was killed.
	 * @param flag the flag
	 */
	public void killedFlag(final L2NpcInstance flag)
	{
		if (flag == null)
		{
			return;
		}
		
		for (int i = 0; i < getAttackerClans().size(); i++)
		{
			if (getAttackerClan(i).removeFlag(flag))
			{
				return;
			}
		}
	}
	
	/**
	 * Display list of registered clans.
	 * @param player the player
	 */
	public void listRegisterClan(final L2PcInstance player)
	{
		player.sendPacket(new FortressSiegeInfo(getFort()));
	}
	
	/**
	 * Register clan as attacker<BR>
	 * <BR>
	 * .
	 * @param player The L2PcInstance of the player trying to register
	 */
	public void registerAttacker(final L2PcInstance player)
	{
		registerAttacker(player, false);
	}
	
	/**
	 * Register attacker.
	 * @param player the player
	 * @param force  the force
	 */
	public void registerAttacker(final L2PcInstance player, final boolean force)
	{
		
		if (player.getClan() == null)
		{
			return;
		}
		
		int allyId = 0;
		
		if (getFort().getOwnerId() != 0)
		{
			allyId = ClanTable.getInstance().getClan(getFort().getOwnerId()).getAllyId();
		}
		
		if (allyId != 0)
		{
			if (player.getClan().getAllyId() == allyId && !force)
			{
				player.sendMessage("You cannot register as an attacker because your alliance owns the fort");
				return;
			}
		}
		
		if (player.getInventory().getItemByItemId(57) != null && player.getInventory().getItemByItemId(57).getCount() < 250000)
		{
			player.sendMessage("You do not have enough adena.");
			return;
		}
		
		if (force || checkIfCanRegister(player))
		{
			player.getInventory().destroyItemByItemId("Siege", 57, 250000, player, player.getTarget());
			player.getInventory().updateDatabase();
			
			saveSiegeClan(player.getClan(), 1, false); // Save to database
			
			// if the first registering we start the timer
			if (getAttackerClans().size() == 1)
			{
				startAutoTask(true);
			}
		}
	}
	
	/**
	 * Register clan as defender<BR>
	 * <BR>
	 * .
	 * @param player The L2PcInstance of the player trying to register
	 */
	public void registerDefender(final L2PcInstance player)
	{
		registerDefender(player, false);
	}
	
	/**
	 * Register defender.
	 * @param player the player
	 * @param force  the force
	 */
	public void registerDefender(final L2PcInstance player, final boolean force)
	{
		if (getFort().getOwnerId() <= 0)
		{
			player.sendMessage("You cannot register as a defender because " + getFort().getName() + " is owned by NPC.");
		}
		else if (force || checkIfCanRegister(player))
		{
			saveSiegeClan(player.getClan(), 2, false); // Save to database
		}
	}
	
	/**
	 * Remove clan from siege<BR>
	 * <BR>
	 * .
	 * @param clanId The int of player's clan id
	 */
	public void removeSiegeClan(final int clanId)
	{
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			
			if (clanId != 0)
			{
				statement = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=? and clan_id=?");
			}
			else
			{
				statement = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=?");
			}
			
			statement.setInt(1, getFort().getFortId());
			
			if (clanId != 0)
			{
				statement.setInt(2, clanId);
			}
			
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
			
			loadSiegeClan();
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
	
	/**
	 * Remove clan from siege<BR>
	 * <BR>
	 * .
	 * @param clan the clan
	 */
	public void removeSiegeClan(final L2Clan clan)
	{
		if (clan == null || clan.getHasFort() == getFort().getFortId() || !FortSiegeManager.getInstance().checkIsRegistered(clan, getFort().getFortId()))
		{
			return;
		}
		
		removeSiegeClan(clan.getClanId());
	}
	
	/**
	 * Remove clan from siege<BR>
	 * <BR>
	 * .
	 * @param player The L2PcInstance of player/clan being removed
	 */
	public void removeSiegeClan(final L2PcInstance player)
	{
		removeSiegeClan(player.getClan());
	}
	
	/**
	 * Start the auto tasks<BR>
	 * <BR>
	 * .
	 */
	public void checkAutoTask()
	{
		if (getFort().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
		{
			clearSiegeDate();
			saveSiegeDate();
			removeSiegeClan(0); // remove all clans
			return;
		}
		
		startAutoTask(false);
	}
	
	/**
	 * Start the auto tasks<BR>
	 * <BR>
	 * .
	 * @param setTime the set time
	 */
	public void startAutoTask(final boolean setTime)
	{
		if (setTime)
		{
			setSiegeDateTime();
		}
		
		LOGGER.info("Siege of " + getFort().getName() + ": " + getFort().getSiegeDate().getTime());
		setIsScheduled(true);
		loadSiegeClan();
		
		// Schedule registration end
		siegeRegistrationEndDate = Calendar.getInstance();
		siegeRegistrationEndDate.setTimeInMillis(getFort().getSiegeDate().getTimeInMillis());
		siegeRegistrationEndDate.add(Calendar.MINUTE, -10);
		
		// Schedule siege auto start
		ThreadPoolManager.getInstance().scheduleGeneral(new FortSiege.ScheduleStartSiegeTask(getFort()), 1000);
	}
	
	/**
	 * Teleport players.
	 * @param teleportWho   the teleport who
	 * @param teleportWhere the teleport where
	 */
	public void teleportPlayer(final TeleportWhoType teleportWho, final MapRegionTable.TeleportWhereType teleportWhere)
	{
		List<L2PcInstance> players;
		switch (teleportWho)
		{
			case Owner:
				players = getOwnersInZone();
				break;
			case Attacker:
				players = getAttackersInZone();
				break;
			case DefenderNotOwner:
				players = getDefendersButNotOwnersInZone();
				break;
			case Spectator:
				players = getSpectatorsInZone();
				break;
			default:
				players = getPlayersInZone();
		}
		
		for (final L2PcInstance player : players)
		{
			if (player.isGM() || player.isInJail())
			{
				continue;
			}
			
			player.teleToLocation(teleportWhere);
		}
		
		players = null;
	}
	
	/**
	 * Add clan as attacker<BR>
	 * <BR>
	 * .
	 * @param clanId The int of clan's id
	 */
	private void addAttacker(final int clanId)
	{
		getAttackerClans().add(new L2SiegeClan(clanId, SiegeClanType.ATTACKER)); // Add registered attacker to attacker list
	}
	
	/**
	 * Add clan as defender<BR>
	 * <BR>
	 * .
	 * @param clanId The int of clan's id
	 */
	private void addDefender(final int clanId)
	{
		getDefenderClans().add(new L2SiegeClan(clanId, SiegeClanType.DEFENDER)); // Add registered defender to defender list
	}
	
	/**
	 * <p>
	 * Add clan as defender with the specified type
	 * </p>
	 * .
	 * @param clanId The int of clan's id
	 * @param type   the type of the clan
	 */
	private void addDefender(final int clanId, final SiegeClanType type)
	{
		getDefenderClans().add(new L2SiegeClan(clanId, type));
	}
	
	/**
	 * Add clan as defender waiting approval<BR>
	 * <BR>
	 * .
	 * @param clanId The int of clan's id
	 */
	private void addDefenderWaiting(final int clanId)
	{
		getDefenderWaitingClans().add(new L2SiegeClan(clanId, SiegeClanType.DEFENDER_PENDING)); // Add registered defender to defender list
	}
	
	/**
	 * Return true if the player can register.<BR>
	 * <BR>
	 * @param  player The L2PcInstance of the player trying to register
	 * @return        true, if successful
	 */
	private boolean checkIfCanRegister(final L2PcInstance player)
	{
		if (getIsRegistrationOver())
		{
			player.sendMessage("The deadline to register for the siege of " + getFort().getName() + " has passed.");
		}
		else if (getIsInProgress())
		{
			player.sendMessage("This is not the time for siege registration and so registration and cancellation cannot be done.");
		}
		else if (player.getClan() == null || player.getClan().getLevel() < FortSiegeManager.getInstance().getSiegeClanMinLevel())
		{
			player.sendMessage("Only clans with Level " + FortSiegeManager.getInstance().getSiegeClanMinLevel() + " and higher may register for a fort siege.");
		}
		else if (player.getClan().getHasFort() > 0)
		{
			player.sendMessage("You cannot register because your clan already own a fort.");
		}
		else if (player.getClan().getCastleId() > 0)
		{
			player.sendMessage("You cannot register because your clan already own a castle.");
		}
		else if (player.getClan().getClanId() == getFort().getOwnerId())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING));
		}
		else if (FortSiegeManager.getInstance().checkIsRegistered(player.getClan(), getFort().getFortId()))
		{
			player.sendMessage("You are already registered in a Siege.");
		}
		else
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Sets the siege date time.
	 */
	private void setSiegeDateTime()
	{
		Calendar newDate = Calendar.getInstance();
		newDate.add(Calendar.MINUTE, 60);
		getFort().setSiegeDate(newDate);
		saveSiegeDate();
		newDate = null;
	}
	
	/** Load siege clans. */
	private void loadSiegeClan()
	{
		Connection con = null;
		try
		{
			getAttackerClans().clear();
			getDefenderClans().clear();
			getDefenderWaitingClans().clear();
			
			// Add fort owner as defender (add owner first so that they are on the top of the defender list)
			if (getFort().getOwnerId() > 0)
			{
				addDefender(getFort().getOwnerId(), SiegeClanType.OWNER);
			}
			
			PreparedStatement statement = null;
			ResultSet rs = null;
			
			con = L2DatabaseFactory.getInstance().getConnection();
			
			statement = con.prepareStatement("SELECT clan_id,type FROM fortsiege_clans where fort_id=?");
			statement.setInt(1, getFort().getFortId());
			rs = statement.executeQuery();
			
			int typeId;
			
			while (rs.next())
			{
				typeId = rs.getInt("type");
				
				if (typeId == 0)
				{
					addDefender(rs.getInt("clan_id"));
				}
				else if (typeId == 1)
				{
					addAttacker(rs.getInt("clan_id"));
				}
				else if (typeId == 2)
				{
					addDefenderWaiting(rs.getInt("clan_id"));
				}
			}
			
			rs.close();
			DatabaseUtils.close(statement);
			statement = null;
			rs = null;
		}
		catch (final Exception e)
		{
			LOGGER.warn("Exception: loadSiegeClan(): " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	/** Remove artifacts spawned. */
	private void removeCommander()
	{
		if (commanders != null)
		{
			// Remove all instance of artifact for this fort
			for (final L2CommanderInstance commander : commanders)
			{
				if (commander != null)
				{
					commander.decayMe();
				}
			}
			commanders = null;
		}
	}
	
	/** Remove all flags. */
	private void removeFlags()
	{
		for (final L2SiegeClan sc : getAttackerClans())
		{
			if (sc != null)
			{
				sc.removeFlags();
			}
		}
		for (final L2SiegeClan sc : getDefenderClans())
		{
			if (sc != null)
			{
				sc.removeFlags();
			}
		}
	}
	
	/** Save fort siege related to database. */
	private void saveFortSiege()
	{
		clearSiegeDate(); // clear siege date
		saveSiegeDate(); // Save the new date
		setIsScheduled(false);
	}
	
	/** Save siege date to database. */
	private void saveSiegeDate()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("Update fort set siegeDate = ? where id = ?");
			statement.setLong(1, getSiegeDate().getTimeInMillis());
			statement.setInt(2, getFort().getFortId());
			statement.execute();
			
			DatabaseUtils.close(statement);
			statement = null;
		}
		catch (final Exception e)
		{
			LOGGER.warn("Exception: saveSiegeDate(): " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	/**
	 * Save registration to database.<BR>
	 * <BR>
	 * @param clan                 The L2Clan of player
	 * @param typeId               -1 = owner 0 = defender, 1 = attacker, 2 = defender waiting
	 * @param isUpdateRegistration the is update registration
	 */
	private void saveSiegeClan(final L2Clan clan, final int typeId, final boolean isUpdateRegistration)
	{
		if (clan.getHasFort() > 0)
		{
			return;
		}
		
		Connection con = null;
		try
		{
			if (typeId == 0 || typeId == 2 || typeId == -1)
			{
				if (getDefenderClans().size() + getDefenderWaitingClans().size() >= FortSiegeManager.getInstance().getDefenderMaxClans())
				{
					return;
				}
			}
			else
			{
				if (getAttackerClans().size() >= FortSiegeManager.getInstance().getAttackerMaxClans())
				{
					return;
				}
			}
			
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			if (!isUpdateRegistration)
			{
				statement = con.prepareStatement("INSERT INTO fortsiege_clans (clan_id,fort_id,type,fort_owner) values (?,?,?,0)");
				statement.setInt(1, clan.getClanId());
				statement.setInt(2, getFort().getFortId());
				statement.setInt(3, typeId);
				statement.execute();
				DatabaseUtils.close(statement);
				statement = null;
			}
			else
			{
				statement = con.prepareStatement("Update fortsiege_clans set type = ? where fort_id = ? and clan_id = ?");
				statement.setInt(1, typeId);
				statement.setInt(2, getFort().getFortId());
				statement.setInt(3, clan.getClanId());
				statement.execute();
				DatabaseUtils.close(statement);
				statement = null;
			}
			
			if (typeId == 0 || typeId == -1)
			{
				addDefender(clan.getClanId());
				announceToPlayer(clan.getName() + " has been registered to defend " + getFort().getName(), false);
			}
			else if (typeId == 1)
			{
				addAttacker(clan.getClanId());
				announceToPlayer(clan.getName() + " has been registered to attack " + getFort().getName(), false);
			}
			else if (typeId == 2)
			{
				addDefenderWaiting(clan.getClanId());
				announceToPlayer(clan.getName() + " has requested to defend " + getFort().getName(), false);
			}
		}
		catch (final Exception e)
		{
			LOGGER.warn("Exception: saveSiegeClan(L2Clan clan, int typeId, boolean isUpdateRegistration): " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	/**
	 * Spawn artifact.
	 * @param Id the id
	 */
	private void spawnCommander(final int Id)
	{
		// Set commanders array size if one does not exist
		if (commanders == null)
		{
			commanders = new ArrayList<>();
		}
		
		for (final SiegeSpawn sp : FortSiegeManager.getInstance().getCommanderSpawnList(Id))
		{
			L2CommanderInstance commander;
			
			commander = new L2CommanderInstance(IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(sp.getNpcId()));
			commander.setCurrentHpMp(commander.getMaxHp(), commander.getMaxMp());
			commander.setHeading(sp.getLocation().getHeading());
			commander.spawnMe(sp.getLocation().getX(), sp.getLocation().getY(), sp.getLocation().getZ() + 50);
			
			commanders.add(commander);
			commander = null;
		}
	}
	
	/**
	 * Spawn flag.
	 * @param Id the id
	 */
	private void spawnFlag(final int Id)
	{
		if (combatflag == null)
		{
			combatflag = new ArrayList<>();
		}
		
		for (final SiegeSpawn sp : FortSiegeManager.getInstance().getFlagList(Id))
		{
			L2ArtefactInstance combatflag;
			
			combatflag = new L2ArtefactInstance(IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(sp.getNpcId()));
			combatflag.setCurrentHpMp(combatflag.getMaxHp(), combatflag.getMaxMp());
			combatflag.setHeading(sp.getLocation().getHeading());
			combatflag.spawnMe(sp.getLocation().getX(), sp.getLocation().getY(), sp.getLocation().getZ() + 10);
			
			this.combatflag.add(combatflag);
			combatflag = null;
		}
		
	}
	
	/**
	 * Un spawn flags.
	 */
	private void unSpawnFlags()
	{
		
		if (combatflag != null)
		{
			// Remove all instance of artifact for this fort
			for (final L2ArtefactInstance sp : combatflag)
			{
				if (sp != null)
				{
					sp.decayMe();
				}
			}
			combatflag = null;
		}
		
	}
	
	/**
	 * Spawn siege guard.<BR>
	 * <BR>
	 */
	private void spawnSiegeGuard()
	{
		getSiegeGuardManager().spawnSiegeGuard();
	}
	
	/**
	 * Gets the attacker clan.
	 * @param  clan the clan
	 * @return      the attacker clan
	 */
	public final L2SiegeClan getAttackerClan(final L2Clan clan)
	{
		if (clan == null)
		{
			return null;
		}
		
		return getAttackerClan(clan.getClanId());
	}
	
	/**
	 * Gets the attacker clan.
	 * @param  clanId the clan id
	 * @return        the attacker clan
	 */
	public final L2SiegeClan getAttackerClan(final int clanId)
	{
		for (final L2SiegeClan sc : getAttackerClans())
		{
			if (sc != null && sc.getClanId() == clanId)
			{
				return sc;
			}
		}
		
		return null;
	}
	
	/**
	 * Gets the attacker clans.
	 * @return the attacker clans
	 */
	public final List<L2SiegeClan> getAttackerClans()
	{
		if (isNormalSide)
		{
			return attackerClans;
		}
		
		return defenderClans;
	}
	
	/**
	 * Gets the attacker respawn delay.
	 * @return the attacker respawn delay
	 */
	public final int getAttackerRespawnDelay()
	{
		return FortSiegeManager.getInstance().getAttackerRespawnDelay();
	}
	
	/**
	 * Gets the fort.
	 * @return the fort
	 */
	public final Fort getFort()
	{
		if (fort == null || fort.length <= 0)
		{
			return null;
		}
		
		return fort[0];
	}
	
	/**
	 * Gets the defender clan.
	 * @param  clan the clan
	 * @return      the defender clan
	 */
	public final L2SiegeClan getDefenderClan(final L2Clan clan)
	{
		if (clan == null)
		{
			return null;
		}
		
		return getDefenderClan(clan.getClanId());
	}
	
	/**
	 * Gets the defender clan.
	 * @param  clanId the clan id
	 * @return        the defender clan
	 */
	public final L2SiegeClan getDefenderClan(final int clanId)
	{
		for (final L2SiegeClan sc : getDefenderClans())
		{
			if (sc != null && sc.getClanId() == clanId)
			{
				return sc;
			}
		}
		
		return null;
	}
	
	/**
	 * Gets the defender clans.
	 * @return the defender clans
	 */
	public final List<L2SiegeClan> getDefenderClans()
	{
		if (isNormalSide)
		{
			return defenderClans;
		}
		
		return attackerClans;
	}
	
	/**
	 * Gets the defender waiting clan.
	 * @param  clan the clan
	 * @return      the defender waiting clan
	 */
	public final L2SiegeClan getDefenderWaitingClan(final L2Clan clan)
	{
		if (clan == null)
		{
			return null;
		}
		
		return getDefenderWaitingClan(clan.getClanId());
	}
	
	/**
	 * Gets the defender waiting clan.
	 * @param  clanId the clan id
	 * @return        the defender waiting clan
	 */
	public final L2SiegeClan getDefenderWaitingClan(final int clanId)
	{
		for (final L2SiegeClan sc : getDefenderWaitingClans())
		{
			if (sc != null && sc.getClanId() == clanId)
			{
				return sc;
			}
		}
		
		return null;
	}
	
	/**
	 * Gets the defender waiting clans.
	 * @return the defender waiting clans
	 */
	public final List<L2SiegeClan> getDefenderWaitingClans()
	{
		return defenderWaitingClans;
	}
	
	/**
	 * Gets the defender respawn delay.
	 * @return the defender respawn delay
	 */
	public final int getDefenderRespawnDelay()
	{
		return FortSiegeManager.getInstance().getDefenderRespawnDelay() + defenderRespawnDelayPenalty;
	}
	
	/**
	 * Gets the checks if is in progress.
	 * @return the checks if is in progress
	 */
	public final boolean getIsInProgress()
	{
		return isInProgress;
	}
	
	/**
	 * Gets the checks if is scheduled.
	 * @return the checks if is scheduled
	 */
	public final boolean getIsScheduled()
	{
		return isScheduled;
	}
	
	/**
	 * Sets the checks if is scheduled.
	 * @param isScheduled the new checks if is scheduled
	 */
	public final void setIsScheduled(final boolean isScheduled)
	{
		this.isScheduled = isScheduled;
	}
	
	/**
	 * Gets the checks if is registration over.
	 * @return the checks if is registration over
	 */
	public final boolean getIsRegistrationOver()
	{
		return isRegistrationOver;
	}
	
	/**
	 * Gets the siege date.
	 * @return the siege date
	 */
	public final Calendar getSiegeDate()
	{
		return getFort().getSiegeDate();
	}
	
	/**
	 * Gets the flag.
	 * @param  clan the clan
	 * @return      the flag
	 */
	public List<L2NpcInstance> getFlag(final L2Clan clan)
	{
		if (clan != null)
		{
			final L2SiegeClan sc = getAttackerClan(clan);
			if (sc != null)
			{
				return sc.getFlag();
			}
		}
		
		return null;
	}
	
	/**
	 * Gets the siege guard manager.
	 * @return the siege guard manager
	 */
	public final FortSiegeGuardManager getSiegeGuardManager()
	{
		if (siegeGuardManager == null)
		{
			siegeGuardManager = new FortSiegeGuardManager(getFort());
		}
		
		return siegeGuardManager;
	}
}
