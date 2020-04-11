package com.l2jfrozen.gameserver.model.entity.siege;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.crypt.nProtect;
import com.l2jfrozen.crypt.nProtect.RestrictionType;
import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable.TeleportWhereType;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.managers.MercTicketManager;
import com.l2jfrozen.gameserver.managers.SiegeGuardManager;
import com.l2jfrozen.gameserver.managers.SiegeManager;
import com.l2jfrozen.gameserver.managers.SiegeManager.SiegeSpawn;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2SiegeClan;
import com.l2jfrozen.gameserver.model.L2SiegeClan.SiegeClanType;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2ArtefactInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ControlTowerInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.RelationChanged;
import com.l2jfrozen.gameserver.network.serverpackets.SiegeInfo;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.network.serverpackets.UserInfo;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class Siege
{
	// Message to add/check
	// id=17 msg=[Castle siege has begun.] c3_attr1=[SystemMsg_k.17]
	// id=18 msg=[Castle siege is over.] c3_attr1=[SystemMsg_k.18]
	// id=288 msg=[The castle gate has been broken down.]
	// id=291 msg=[Clan $s1 is victorious over $s2's castle siege!]
	// id=292 msg=[$s1 has announced the castle siege time.]
	// - id=293 msg=[The registration term for $s1 has ended.]
	// - id=358 msg=[$s1 hour(s) until castle siege conclusion.]
	// - id=359 msg=[$s1 minute(s) until castle siege conclusion.]
	// - id=360 msg=[Castle siege $s1 second(s) left!]
	// id=640 msg=[You have failed to refuse castle defense aid.]
	// id=641 msg=[You have failed to approve castle defense aid.]
	// id=644 msg=[You are not yet registered for the castle siege.]
	// - id=645 msg=[Only clans with Level 4 and higher may register for a castle siege.]
	// id=646 msg=[You do not have the authority to modify the castle defender list.]
	// - id=688 msg=[The clan that owns the castle is automatically registered on the defending side.]
	// id=689 msg=[A clan that owns a castle cannot participate in another siege.]
	// id=690 msg=[You cannot register on the attacking side because you are part of an alliance with the clan that owns the castle.]
	// id=718 msg=[The castle gates cannot be opened and closed during a siege.]
	// - id=295 msg=[$s1's siege was canceled because there were no clans that participated.]
	// id=659 msg=[This is not the time for siege registration and so registrations cannot be accepted or rejected.]
	// - id=660 msg=[This is not the time for siege registration and so registration and cancellation cannot be done.]
	// id=663 msg=[The siege time has been declared for $s. It is not possible to change the time after a siege time has been declared. Do you want to continue?]
	// id=667 msg=[You are registering on the attacking side of the $s1 siege. Do you want to continue?]
	// id=668 msg=[You are registering on the defending side of the $s1 siege. Do you want to continue?]
	// id=669 msg=[You are canceling your application to participate in the $s1 siege battle. Do you want to continue?]
	// id=707 msg=[You cannot teleport to a village that is in a siege.]
	// - id=711 msg=[The siege of $s1 has started.]
	// - id=712 msg=[The siege of $s1 has finished.]
	// id=844 msg=[The siege to conquer $s1 has begun.]
	// - id=845 msg=[The deadline to register for the siege of $s1 has passed.]
	// - id=846 msg=[The siege of $s1 has been canceled due to lack of interest.]
	// - id=856 msg=[The siege of $s1 has ended in a draw.]
	// id=285 msg=[Clan $s1 has succeeded in engraving the ruler!]
	// - id=287 msg=[The opponent clan has begun to engrave the ruler.]
	
	protected static final Logger LOGGER = Logger.getLogger(Siege.class);
	private static final String DELETE_SIEGE_CLAN_BY_CASTLE_ID = "DELETE FROM siege_clans WHERE castle_id=?";
	private static final String DELETE_SIEGE_CLAN_BY_CLAN_ID = "DELETE FROM siege_clans WHERE clan_id=?";
	private static final String DELETE_SIEGE_CLAN_BY_CLASTE_ID_AND_TYPE_2 = "DELETE FROM siege_clans WHERE castle_id=? AND type=2";
	private static final String DELETE_SIEGE_CLAN_BY_CASTLE_AND_CLAN_ID = "DELETE FROM siege_clans WHERE castle_id=? AND clan_id=?";
	private static final String SELECT_SIEGE_CLAN_BY_CASTLE_ID = "SELECT clan_id,type FROM siege_clans WHERE castle_id=?";
	private static final String UPDATE_CASTLE_SIEGE_BY_CASTLE_ID = "UPDATE castle SET siegeDate=? WHERE id=?";
	private static final String INSERT_SIEGE_CLAN = "INSERT INTO siege_clans (clan_id,castle_id,type,castle_owner) VALUES (?,?,?,0)";
	private static final String UPDATE_SIEGE_CLAN_BY_CASTLE_AND_CLAN_ID = "UPDATE siege_clans SET type = ? WHERE castle_id = ? AND clan_id = ?";
	
	private final SimpleDateFormat fmt = new SimpleDateFormat("H:mm.");
	
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
	
	private int controlTowerCount;
	private int controlTowerMaxCount;
	
	/**
	 * Gets the control tower count.
	 * @return the control tower count
	 */
	public int getControlTowerCount()
	{
		return controlTowerCount;
	}
	
	public class ScheduleEndSiegeTask implements Runnable
	{
		private final Castle castleInst;
		
		/**
		 * Instantiates a new schedule end siege task.
		 * @param pCastle the castle
		 */
		public ScheduleEndSiegeTask(final Castle pCastle)
		{
			castleInst = pCastle;
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
					// Prepare task for 1 hr left.
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(castleInst), timeRemaining - 3600000);
				}
				else if (timeRemaining <= 3600000 && timeRemaining > 600000)
				{
					announceToPlayer((timeRemaining / 60000) + " minute(s) until " + getCastle().getName() + " siege conclusion.", true);
					
					// Prepare task for 10 minute left.
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(castleInst), timeRemaining - 600000);
				}
				else if (timeRemaining <= 600000 && timeRemaining > 300000)
				{
					announceToPlayer((timeRemaining / 60000) + " minute(s) until " + getCastle().getName() + " siege conclusion.", true);
					
					// Prepare task for 5 minute left.
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(castleInst), timeRemaining - 300000);
				}
				else if (timeRemaining <= 300000 && timeRemaining > 10000)
				{
					announceToPlayer((timeRemaining / 60000) + " minute(s) until " + getCastle().getName() + " siege conclusion.", true);
					
					// Prepare task for 10 seconds count down
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(castleInst), timeRemaining - 10000);
				}
				else if (timeRemaining <= 10000 && timeRemaining > 0)
				{
					announceToPlayer(getCastle().getName() + " siege " + (timeRemaining / 1000) + " second(s) left!", true);
					
					// Prepare task for second count down
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(castleInst), timeRemaining);
				}
				else
				{
					castleInst.getSiege().endSiege();
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
		private final Castle castleInst;
		
		/**
		 * Instantiates a new schedule start siege task.
		 * @param pCastle the castle
		 */
		public ScheduleStartSiegeTask(final Castle pCastle)
		{
			castleInst = pCastle;
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
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(castleInst), timeRemaining - 86400000);
				}
				else if (timeRemaining <= 86400000 && timeRemaining > 13600000)
				{
					announceToPlayer("The registration term for " + getCastle().getName() + " has ended.", false);
					isRegistrationOver = true;
					clearSiegeWaitingClan();
					
					// Prepare task for 1 hr left before siege start.
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(castleInst), timeRemaining - 13600000);
				}
				else if (timeRemaining <= 13600000 && timeRemaining > 600000)
				{
					announceToPlayer((timeRemaining / 60000) + " minute(s) until " + getCastle().getName() + " siege begin.", false);
					
					// Prepare task for 10 minute left.
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(castleInst), timeRemaining - 600000);
				}
				else if (timeRemaining <= 600000 && timeRemaining > 300000)
				{
					announceToPlayer((timeRemaining / 60000) + " minute(s) until " + getCastle().getName() + " siege begin.", false);
					
					// Prepare task for 5 minute left.
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(castleInst), timeRemaining - 300000);
				}
				else if (timeRemaining <= 300000 && timeRemaining > 10000)
				{
					announceToPlayer((timeRemaining / 60000) + " minute(s) until " + getCastle().getName() + " siege begin.", false);
					
					// Prepare task for 10 seconds count down
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(castleInst), timeRemaining - 10000);
				}
				else if (timeRemaining <= 10000 && timeRemaining > 0)
				{
					announceToPlayer(getCastle().getName() + " siege " + (timeRemaining / 1000) + " second(s) to start!", false);
					
					// Prepare task for second count down
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(castleInst), timeRemaining);
				}
				else
				{
					castleInst.getSiege().startSiege();
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
	// Castle setting
	private List<L2ArtefactInstance> artifacts = new ArrayList<>();
	private List<L2ControlTowerInstance> controlTowers = new ArrayList<>();
	private final Castle[] castle;
	private boolean isInProgress = false;
	private boolean isNormalSide = true; // true = Atk is Atk, false = Atk is Def
	protected boolean isRegistrationOver = false;
	protected Calendar siegeEndDate;
	private SiegeGuardManager siegeGuardManager;
	protected Calendar siegeRegistrationEndDate;
	
	/**
	 * Instantiates a new siege.
	 * @param castle the castle
	 */
	public Siege(final Castle[] castle)
	{
		this.castle = castle;
		siegeGuardManager = new SiegeGuardManager(getCastle());
		
		startAutoTask();
	}
	
	// Siege phases
	public void endSiege()
	{
		if (getIsInProgress())
		{
			announceToPlayer("The siege of " + getCastle().getName() + " has finished!", false);
			
			LOGGER.info("[SIEGE] The siege of " + getCastle().getName() + " has finished! " + fmt.format(new Date(System.currentTimeMillis())));
			
			if (getCastle().getOwnerId() <= 0)
			{
				announceToPlayer("The siege of " + getCastle().getName() + " has ended in a draw.", false);
				
				LOGGER.info("[SIEGE] The siege of " + getCastle().getName() + " has ended in a draw. " + fmt.format(new Date(System.currentTimeMillis())));
			}
			
			// Removes all flags. Note: Remove flag before teleporting players
			removeFlags();
			
			// Teleport to the second closest town
			sendEveryoneToTown();
			
			// Flag so that siege instance can be started
			isInProgress = false;
			
			updatePlayerSiegeStateFlags(true);
			
			// Save castle specific data
			saveCastleSiege();
			
			// Clear siege clan from db
			clearSiegeClan();
			
			// Remove artifact from this castle
			removeArtifact();
			
			// Remove all control tower from this castle
			removeControlTower();
			
			// Remove all spawned siege guard from this castle
			siegeGuardManager.unspawnSiegeGuard();
			
			if (getCastle().getOwnerId() > 0)
			{
				siegeGuardManager.removeMercs();
			}
			
			// Respawn door to castle
			getCastle().spawnDoor();
			getCastle().getZone().updateZoneStatusForCharactersInside();
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
	 * When control of castle changed during siege<BR>
	 * <BR>
	 * .
	 */
	public void midVictory()
	{
		if (getIsInProgress()) // Siege still in progress
		{
			if (getCastle().getOwnerId() > 0)
			{
				siegeGuardManager.removeMercs(); // Remove all merc entry from db
			}
			
			if (getDefenderClans().size() == 0 && // If defender doesn't exist (Pc vs Npc)
				getAttackerClans().size() == 1) // Only 1 attacker
			{
				L2SiegeClan sc_newowner = getAttackerClan(getCastle().getOwnerId());
				removeAttacker(sc_newowner);
				addDefender(sc_newowner, SiegeClanType.OWNER);
				endSiege();
				sc_newowner = null;
				
				return;
			}
			
			if (getCastle().getOwnerId() > 0)
			{
				final int allyId = ClanTable.getInstance().getClan(getCastle().getOwnerId()).getAllyId();
				
				if (getDefenderClans().size() == 0) // If defender doesn't exist (Pc vs Npc)
				// and only an alliance attacks
				{
					// The player's clan is in an alliance
					if (allyId != 0)
					{
						boolean allinsamealliance = true;
						
						for (final L2SiegeClan sc : getAttackerClans())
						{
							if (sc != null)
							{
								if (ClanTable.getInstance().getClan(sc.getClanId()).getAllyId() != allyId)
								{
									allinsamealliance = false;
								}
							}
						}
						if (allinsamealliance)
						{
							L2SiegeClan sc_newowner = getAttackerClan(getCastle().getOwnerId());
							removeAttacker(sc_newowner);
							addDefender(sc_newowner, SiegeClanType.OWNER);
							endSiege();
							sc_newowner = null;
							
							return;
						}
					}
				}
				
				for (final L2SiegeClan sc : getDefenderClans())
				{
					if (sc != null)
					{
						removeDefender(sc);
						addAttacker(sc);
					}
				}
				
				L2SiegeClan sc_newowner = getAttackerClan(getCastle().getOwnerId());
				removeAttacker(sc_newowner);
				addDefender(sc_newowner, SiegeClanType.OWNER);
				sc_newowner = null;
				
				// The player's clan is in an alliance
				if (allyId != 0)
				{
					L2Clan[] clanList = ClanTable.getInstance().getClans();
					
					for (final L2Clan clan : clanList)
					{
						if (clan.getAllyId() == allyId)
						{
							L2SiegeClan sc = getAttackerClan(clan.getClanId());
							
							if (sc != null)
							{
								removeAttacker(sc);
								addDefender(sc, SiegeClanType.DEFENDER);
							}
							
							sc = null;
						}
					}
					clanList = null;
				}
				
				// Teleport to the second closest town
				sendEveryoneToTown();
				
				// Removes defenders' flags
				removeDefenderFlags();
				
				// Remove all castle upgrade
				getCastle().removeUpgrade();
				
				// Respawn door to castle but make them weaker (50% hp)
				getCastle().spawnDoor(true);
				
				// Remove all control tower from this castle
				removeControlTower();
				
				// Each new siege midvictory CT are completely respawned.
				controlTowerCount = 0;
				controlTowerMaxCount = 0;
				
				spawnControlTower(getCastle().getCastleId());
				updatePlayerSiegeStateFlags(false);
			}
		}
	}
	
	public void startSiege()
	{
		if (!getIsInProgress())
		{
			if (getAttackerClans().size() <= 0)
			{
				SystemMessage sm;
				
				if (getCastle().getOwnerId() <= 0)
				{
					sm = new SystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_SIEGE_WAS_CANCELED_BECAUSE_NO_CLANS_PARTICIPATED);
				}
				
				sm.addString(getCastle().getName());
				Announcements.getInstance().announceToAll(sm);
				sm = null;
				
				return;
			}
			
			// Atk is now atk
			isNormalSide = true;
			
			// Flag so that same siege instance cannot be started again
			isInProgress = true;
			
			// Load siege clan from db
			loadSiegeClan();
			updatePlayerSiegeStateFlags(false);
			
			// Teleport to the closest town
			sendEveryoneToTown();
			
			controlTowerCount = 0;
			controlTowerMaxCount = 0;
			
			// Spawn artifact
			spawnArtifact(getCastle().getCastleId());
			
			// Spawn control tower
			spawnControlTower(getCastle().getCastleId());
			
			// Spawn door
			getCastle().spawnDoor();
			
			// Spawn siege guard
			spawnSiegeGuard();
			
			// remove the tickets from the ground
			MercTicketManager.getInstance().deleteTickets(getCastle().getCastleId());
			
			// Reset respawn delay
			defenderRespawnDelayPenalty = 0;
			
			getCastle().getZone().updateZoneStatusForCharactersInside();
			
			// Schedule a task to prepare auto siege end
			siegeEndDate = Calendar.getInstance();
			siegeEndDate.add(Calendar.MINUTE, SiegeManager.getInstance().getSiegeLength());
			nProtect.getInstance().checkRestriction(null, RestrictionType.RESTRICT_EVENT, new Object[]
			{
				Siege.class,
				this
			});
			// Prepare auto end task
			ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(getCastle()), 1000);
			
			announceToPlayer("The siege of " + getCastle().getName() + " has started!", false);
			
			LOGGER.info("[SIEGE] The siege of " + getCastle().getName() + " has started! " + fmt.format(new Date(System.currentTimeMillis())));
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
			getCastle().getZone().announceToPlayers(message);
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
	 * Return true if object is inside the zone.
	 * @param  x the x
	 * @param  y the y
	 * @param  z the z
	 * @return   true, if successful
	 */
	public boolean checkIfInZone(final int x, final int y, final int z)
	{
		return getIsInProgress() && getCastle().checkIfInZone(x, y, z); // Castle zone during siege
	}
	
	/**
	 * Return true if clan is attacker<BR>
	 * <BR>
	 * .
	 * @param  clan The L2Clan of the player
	 * @return      true, if successful
	 */
	public boolean checkIsAttacker(final L2Clan clan)
	{
		return getAttackerClan(clan) != null;
	}
	
	/**
	 * Return true if clan is defender<BR>
	 * <BR>
	 * .
	 * @param  clan The L2Clan of the player
	 * @return      true, if successful
	 */
	public boolean checkIsDefender(final L2Clan clan)
	{
		return getDefenderClan(clan) != null;
	}
	
	/**
	 * Return true if clan is defender waiting approval<BR>
	 * <BR>
	 * .
	 * @param  clan The L2Clan of the player
	 * @return      true, if successful
	 */
	public boolean checkIsDefenderWaiting(final L2Clan clan)
	{
		return getDefenderWaitingClan(clan) != null;
	}
	
	/**
	 * Clear all registered siege clans from database for castle.
	 */
	public void clearSiegeClan()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_SIEGE_CLAN_BY_CASTLE_ID))
		{
			statement.setInt(1, getCastle().getCastleId());
			statement.executeUpdate();
			
			if (getCastle().getOwnerId() > 0)
			{
				try (PreparedStatement statement2 = con.prepareStatement(DELETE_SIEGE_CLAN_BY_CLAN_ID))
				{
					statement2.setInt(1, getCastle().getOwnerId());
					statement2.executeUpdate();
				}
			}
			
			getAttackerClans().clear();
			getDefenderClans().clear();
			getDefenderWaitingClans().clear();
		}
		catch (Exception e)
		{
			LOGGER.error("Siege.clearSiegeClan : Could not delete siege in db", e);
		}
	}
	
	/**
	 * Clear all siege clans waiting for approval from database for castle.
	 */
	public void clearSiegeWaitingClan()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_SIEGE_CLAN_BY_CLASTE_ID_AND_TYPE_2))
		{
			statement.setInt(1, getCastle().getCastleId());
			statement.executeUpdate();
			
			getDefenderWaitingClans().clear();
		}
		catch (Exception e)
		{
			LOGGER.error("Siege.clearSiegeWaitingClan : Could not delete siege waiting in db", e);
		}
	}
	
	/**
	 * Return list of L2PcInstance registered as attacker in the zone.
	 * @return the attackers in zone
	 */
	public List<L2PcInstance> getAttackersInZone()
	{
		List<L2PcInstance> players = new ArrayList<>();
		L2Clan clan;
		
		for (L2SiegeClan siegeclan : getAttackerClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			
			for (L2PcInstance player : clan.getOnlineMembers(""))
			{
				if (checkIfInZone(player.getX(), player.getY(), player.getZ()))
				{
					players.add(player);
				}
			}
		}
		
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
		
		for (L2SiegeClan siegeclan : getDefenderClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			
			if (clan.getClanId() == getCastle().getOwnerId())
			{
				continue;
			}
			
			for (L2PcInstance player : clan.getOnlineMembers(""))
			{
				if (checkIfInZone(player.getX(), player.getY(), player.getZ()))
				{
					players.add(player);
				}
			}
		}
		
		return players;
	}
	
	/**
	 * Return list of L2PcInstance in the zone.
	 * @return the players in zone
	 */
	public List<L2PcInstance> getPlayersInZone()
	{
		return getCastle().getZone().getAllPlayers();
	}
	
	/**
	 * Return list of L2PcInstance owning the castle in the zone.
	 * @return the owners in zone
	 */
	public List<L2PcInstance> getOwnersInZone()
	{
		List<L2PcInstance> players = new ArrayList<>();
		L2Clan clan;
		
		for (L2SiegeClan siegeclan : getDefenderClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			
			if (clan.getClanId() != getCastle().getOwnerId())
			{
				continue;
			}
			
			for (L2PcInstance player : clan.getOnlineMembers(""))
			{
				if (checkIfInZone(player.getX(), player.getY(), player.getZ()))
				{
					players.add(player);
				}
			}
		}
		
		return players;
	}
	
	/**
	 * Return list of L2PcInstance not registered as attacker or defender in the zone.
	 * @return the spectators in zone
	 */
	public List<L2PcInstance> getSpectatorsInZone()
	{
		List<L2PcInstance> players = new ArrayList<>();
		
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
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
	 * Control Tower was destroyed.
	 */
	public void killedCT()
	{
		controlTowerCount--;
		
		if (controlTowerCount < 0)
		{
			controlTowerCount = 0;
		}
		
		// Add respawn penalty to defenders for each control tower destroyed
		if (controlTowerCount > 0 && SiegeManager.getInstance().getControlTowerLosePenalty() > 0)
		{
			controlTowerCount = (controlTowerMaxCount - controlTowerCount) * SiegeManager.getInstance().getControlTowerLosePenalty();
		}
		else
		{
			defenderRespawnDelayPenalty = 0;
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
		player.sendPacket(new SiegeInfo(getCastle()));
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
		
		if (getCastle().getOwnerId() != 0)
		{
			allyId = ClanTable.getInstance().getClan(getCastle().getOwnerId()).getAllyId();
		}
		
		if (allyId != 0)
		{
			if (player.getClan().getAllyId() == allyId && !force)
			{
				player.sendMessage("You cannot register as an attacker because your alliance owns the castle");
				return;
			}
		}
		
		if (force || checkIfCanRegister(player))
		{
			saveSiegeClan(player.getClan(), 1, false);
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
		if (getCastle().getOwnerId() <= 0)
		{
			player.sendMessage("You cannot register as a defender because " + getCastle().getName() + " is owned by NPC.");
		}
		else if (force || checkIfCanRegister(player))
		{
			saveSiegeClan(player.getClan(), 2, false);
		}
	}
	
	/**
	 * Remove clan from siege<BR>
	 * <BR>
	 * .
	 * @param clanId The int of player's clan id
	 */
	public void removeSiegeClan(int clanId)
	{
		if (clanId <= 0)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_SIEGE_CLAN_BY_CASTLE_AND_CLAN_ID))
		{
			statement.setInt(1, getCastle().getCastleId());
			statement.setInt(2, clanId);
			statement.executeUpdate();
			
			loadSiegeClan();
		}
		catch (Exception e)
		{
			LOGGER.error("Siege.removeSiegeClan : Could not remove siege clan from db", e);
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
		if (clan == null || clan.getCastleId() == getCastle().getCastleId() || !SiegeManager.getInstance().checkIsRegistered(clan, getCastle().getCastleId()))
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
	public void startAutoTask()
	{
		correctSiegeDateTime();
		
		LOGGER.info("Siege of " + getCastle().getName() + ": " + getCastle().getSiegeDate().getTime());
		
		loadSiegeClan();
		
		// Schedule registration end
		siegeRegistrationEndDate = Calendar.getInstance();
		siegeRegistrationEndDate.setTimeInMillis(getCastle().getSiegeDate().getTimeInMillis());
		siegeRegistrationEndDate.add(Calendar.DAY_OF_MONTH, -1);
		
		// Schedule siege auto start
		ThreadPoolManager.getInstance().scheduleGeneral(new Siege.ScheduleStartSiegeTask(getCastle()), 1000);
	}
	
	/**
	 * Add clan as attacker<BR>
	 * <BR>
	 * .
	 * @param clanId The int of clan's id
	 */
	private void addAttacker(final int clanId)
	{
		// Add registered attacker to attacker list
		getAttackerClans().add(new L2SiegeClan(clanId, SiegeClanType.ATTACKER));
	}
	
	/**
	 * Add clan as defender<BR>
	 * <BR>
	 * .
	 * @param clanId The int of clan's id
	 */
	private void addDefender(final int clanId)
	{
		// Add registered defender to defender list
		getDefenderClans().add(new L2SiegeClan(clanId, SiegeClanType.DEFENDER));
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
		// Add registered defender to defender list
		getDefenderWaitingClans().add(new L2SiegeClan(clanId, SiegeClanType.DEFENDER_PENDING));
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
			player.sendMessage("The deadline to register for the siege of " + getCastle().getName() + " has passed.");
		}
		else if (getIsInProgress())
		{
			player.sendMessage("This is not the time for siege registration and so registration and cancellation cannot be done.");
		}
		else if (player.getClan() == null || player.getClan().getLevel() < SiegeManager.getInstance().getSiegeClanMinLevel())
		{
			player.sendMessage("Only clans with Level " + SiegeManager.getInstance().getSiegeClanMinLevel() + " and higher may register for a castle siege.");
		}
		else if (player.getClan().getCastleId() > 0)
		{
			player.sendMessage("You cannot register because your clan already own a castle.");
		}
		else if (player.getClan().getHasFort() > 0)
		{
			player.sendMessage("You cannot register because your clan already own a fort.");
		}
		else if (player.getClan().getClanId() == getCastle().getOwnerId())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING));
		}
		else if (SiegeManager.getInstance().checkIsRegistered(player.getClan(), getCastle().getCastleId()))
		{
			player.sendMessage("You are already registered in a Siege.");
		}
		else if (checkIfAlreadyRegisteredForAnotherSiege(player.getClan()))
		{
			player.sendMessage("You are already registered in another Siege.");
		}
		else
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Return true if the clan has already registered to a siege for the same day.<BR>
	 * <BR>
	 * @param  clan The L2Clan of the player trying to register
	 * @return      true, if successful
	 */
	private boolean checkIfAlreadyRegisteredForAnotherSiege(final L2Clan clan)
	{
		for (final Siege siege : SiegeManager.getInstance().getSieges())
		{
			if (siege == this)
			{
				continue;
			}
			// if(siege.getSiegeDate().get(Calendar.DAY_OF_WEEK) == this.getSiegeDate().get(Calendar.DAY_OF_WEEK))
			// {
			if (siege.checkIsAttacker(clan))
			{
				return true;
			}
			if (siege.checkIsDefender(clan))
			{
				return true;
			}
			if (siege.checkIsDefenderWaiting(clan))
			{
				return true;
				// }
			}
		}
		return false;
	}
	
	/**
	 * Return the correct siege date as Calendar.<BR>
	 * <BR>
	 */
	private void correctSiegeDateTime()
	{
		boolean corrected = false;
		
		if (getCastle().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
		{
			// Since siege has past reschedule it to the next one (14 days)
			// This is usually caused by server being down
			corrected = true;
			setNextSiegeDate();
		}
		
		if (getCastle().getSiegeDate().get(Calendar.DAY_OF_WEEK) != getCastle().getSiegeDayOfWeek())
		{
			corrected = true;
			getCastle().getSiegeDate().set(Calendar.DAY_OF_WEEK, getCastle().getSiegeDayOfWeek());
		}
		
		if (getCastle().getSiegeDate().get(Calendar.HOUR_OF_DAY) != getCastle().getSiegeHourOfDay())
		{
			corrected = true;
			getCastle().getSiegeDate().set(Calendar.HOUR_OF_DAY, getCastle().getSiegeHourOfDay());
		}
		
		getCastle().getSiegeDate().set(Calendar.MINUTE, 0);
		
		if (corrected)
		{
			saveSiegeDate();
		}
	}
	
	private void loadSiegeClan()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_SIEGE_CLAN_BY_CASTLE_ID))
		{
			getAttackerClans().clear();
			getDefenderClans().clear();
			getDefenderWaitingClans().clear();
			
			// Add castle owner as defender (add owner first so that they are on the top of the defender list)
			if (getCastle().getOwnerId() > 0)
			{
				addDefender(getCastle().getOwnerId(), SiegeClanType.OWNER);
			}
			
			statement.setInt(1, getCastle().getCastleId());
			
			try (ResultSet rs = statement.executeQuery())
			{
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
			}
			
		}
		catch (Exception e)
		{
			LOGGER.info("Siege.loadSiege : Could not select clan sieges from db", e);
		}
	}
	
	/** Remove artifacts spawned. */
	private void removeArtifact()
	{
		if (artifacts != null)
		{
			// Remove all instance of artifact for this castle
			for (final L2ArtefactInstance art : artifacts)
			{
				if (art != null)
				{
					art.decayMe();
				}
			}
			artifacts = null;
		}
	}
	
	/** Remove all control tower spawned. */
	private void removeControlTower()
	{
		if (controlTowers != null)
		{
			// Remove all instance of control tower for this castle
			for (final L2ControlTowerInstance ct : controlTowers)
			{
				if (ct != null)
				{
					ct.decayMe();
				}
			}
			
			controlTowers = null;
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
	
	/** Remove flags from defenders. */
	private void removeDefenderFlags()
	{
		for (final L2SiegeClan sc : getDefenderClans())
		{
			if (sc != null)
			{
				sc.removeFlags();
			}
		}
	}
	
	private void saveCastleSiege()
	{
		setNextSiegeDate(); // Set the next set date for 2 weeks from now
		saveSiegeDate(); // Save the new date
		startAutoTask(); // Prepare auto start siege and end registration
	}
	
	private void saveSiegeDate()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_CASTLE_SIEGE_BY_CASTLE_ID))
		{
			statement.setLong(1, getSiegeDate().getTimeInMillis());
			statement.setInt(2, getCastle().getCastleId());
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Siege.saveSiegeDate: Could not update castle siege date into db", e);
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
		if (clan.getCastleId() > 0)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();)
		{
			if (typeId == 0 || typeId == 2 || typeId == -1)
			{
				if (getDefenderClans().size() + getDefenderWaitingClans().size() >= SiegeManager.getInstance().getDefenderMaxClans())
				{
					return;
				}
			}
			else
			{
				if (getAttackerClans().size() >= SiegeManager.getInstance().getAttackerMaxClans())
				{
					return;
				}
			}
			
			if (!isUpdateRegistration)
			{
				try (PreparedStatement statement = con.prepareStatement(INSERT_SIEGE_CLAN))
				{
					statement.setInt(1, clan.getClanId());
					statement.setInt(2, getCastle().getCastleId());
					statement.setInt(3, typeId);
					statement.executeUpdate();
				}
			}
			else
			{
				try (PreparedStatement statement = con.prepareStatement(UPDATE_SIEGE_CLAN_BY_CASTLE_AND_CLAN_ID))
				{
					statement.setInt(1, typeId);
					statement.setInt(2, getCastle().getCastleId());
					statement.setInt(3, clan.getClanId());
					statement.executeUpdate();
				}
			}
			
			if (typeId == 0 || typeId == -1)
			{
				addDefender(clan.getClanId());
				announceToPlayer(clan.getName() + " has been registered to defend " + getCastle().getName(), false);
			}
			else if (typeId == 1)
			{
				addAttacker(clan.getClanId());
				announceToPlayer(clan.getName() + " has been registered to attack " + getCastle().getName(), false);
			}
			else if (typeId == 2)
			{
				addDefenderWaiting(clan.getClanId());
				announceToPlayer(clan.getName() + " has requested to defend " + getCastle().getName(), false);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Siege.saveSiegeClan : Could not insert or update clan siene in db", e);
		}
	}
	
	/** Set the date for the next siege. */
	private void setNextSiegeDate()
	{
		while (getCastle().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
		{
			// Set next siege date if siege has passed
			// Schedule to happen in 14 days
			getCastle().getSiegeDate().add(Calendar.DAY_OF_MONTH, 14);
		}
		
		// Allow registration for next siege
		isRegistrationOver = false;
	}
	
	/**
	 * Spawn artifact.
	 * @param Id the id
	 */
	private void spawnArtifact(int Id)
	{
		// Set artefact array size if one does not exist
		if (artifacts == null)
		{
			artifacts = new ArrayList<>();
		}
		
		for (SiegeSpawn sp : SiegeManager.getInstance().getArtefactSpawnList(Id))
		{
			L2ArtefactInstance art;
			
			art = new L2ArtefactInstance(IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(sp.getNpcId()));
			art.setCurrentHpMp(art.getMaxHp(), art.getMaxMp());
			art.setHeading(sp.getLocation().getHeading());
			art.spawnMe(sp.getLocation().getX(), sp.getLocation().getY(), sp.getLocation().getZ() + 50);
			
			artifacts.add(art);
			art = null;
		}
	}
	
	/**
	 * Spawn control tower.
	 * @param Id the id
	 */
	private void spawnControlTower(final int Id)
	{
		// Set control tower array size if one does not exist
		if (controlTowers == null)
		{
			controlTowers = new ArrayList<>();
		}
		
		for (SiegeSpawn sp : SiegeManager.getInstance().getControlTowerSpawnList(Id))
		{
			L2ControlTowerInstance ct;
			
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(sp.getNpcId());
			
			template.getStatsSet().set("baseHpMax", sp.getHp());
			// TODO: Check/confirm if control towers have any special weapon resistances/vulnerabilities
			// template.addVulnerability(Stats.BOW_WPN_VULN,0);
			// template.addVulnerability(Stats.BLUNT_WPN_VULN,0);
			// template.addVulnerability(Stats.DAGGER_WPN_VULN,0);
			
			ct = new L2ControlTowerInstance(IdFactory.getInstance().getNextId(), template);
			
			ct.setCurrentHpMp(ct.getMaxHp(), ct.getMaxMp());
			ct.spawnMe(sp.getLocation().getX(), sp.getLocation().getY(), sp.getLocation().getZ() + 20);
			controlTowerCount++;
			controlTowerMaxCount++;
			controlTowers.add(ct);
			
			ct = null;
			template = null;
		}
	}
	
	/**
	 * Spawn siege guard.<BR>
	 * <BR>
	 */
	private void spawnSiegeGuard()
	{
		getSiegeGuardManager().spawnSiegeGuard();
		
		// Register guard to the closest Control Tower
		// When CT dies, so do all the guards that it controls
		if (getSiegeGuardManager().getSiegeGuardSpawn().size() > 0 && controlTowers.size() > 0)
		{
			L2ControlTowerInstance closestCt;
			
			double distance, x, y, z;
			double distanceClosest = 0;
			
			for (final L2Spawn spawn : getSiegeGuardManager().getSiegeGuardSpawn())
			{
				if (spawn == null)
				{
					continue;
				}
				
				closestCt = null;
				distanceClosest = 0;
				
				for (final L2ControlTowerInstance ct : controlTowers)
				{
					if (ct == null)
					{
						continue;
					}
					
					x = spawn.getLocx() - ct.getX();
					y = spawn.getLocy() - ct.getY();
					z = spawn.getLocz() - ct.getZ();
					
					distance = x * x + y * y + z * z;
					
					if (closestCt == null || distance < distanceClosest)
					{
						closestCt = ct;
						distanceClosest = distance;
					}
				}
				
				if (closestCt != null)
				{
					closestCt.registerGuard(spawn);
				}
			}
			
			closestCt = null;
		}
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
		return SiegeManager.getInstance().getAttackerRespawnDelay();
	}
	
	/**
	 * Gets the castle.
	 * @return the castle
	 */
	public final Castle getCastle()
	{
		if (castle == null || castle.length <= 0)
		{
			return null;
		}
		
		return castle[0];
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
		return SiegeManager.getInstance().getDefenderRespawnDelay() + defenderRespawnDelayPenalty;
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
		return getCastle().getSiegeDate();
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
	public final SiegeGuardManager getSiegeGuardManager()
	{
		if (siegeGuardManager == null)
		{
			siegeGuardManager = new SiegeGuardManager(getCastle());
		}
		
		return siegeGuardManager;
	}
	
	public void sendEveryoneToTown()
	{
		for (L2PcInstance player : getPlayersInZone())
		{
			if (player == null)
			{
				continue;
			}
			
			if (player.inObserverMode())
			{
				continue;
			}
			
			if (player.isGM())
			{
				continue;
			}
			
			player.teleToLocation(TeleportWhereType.Town);
		}
	}
}
