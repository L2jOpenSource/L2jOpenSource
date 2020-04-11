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
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;
import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.Announcements;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.datatables.sql.ClanTable;
import l2r.gameserver.datatables.sql.NpcTable;
import l2r.gameserver.datatables.xml.SiegeScheduleData;
import l2r.gameserver.enums.EventStage;
import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.enums.SiegeClanType;
import l2r.gameserver.enums.TeleportWhereType;
import l2r.gameserver.instancemanager.CastleManager;
import l2r.gameserver.instancemanager.MercTicketManager;
import l2r.gameserver.instancemanager.SiegeGuardManager;
import l2r.gameserver.instancemanager.SiegeManager;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.L2ClanMember;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2SiegeClan;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.SiegeScheduleDate;
import l2r.gameserver.model.TowerSpawn;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2ControlTowerInstance;
import l2r.gameserver.model.actor.instance.L2FlameTowerInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExBrExtraUserInfo;
import l2r.gameserver.network.serverpackets.RelationChanged;
import l2r.gameserver.network.serverpackets.SiegeInfo;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.UserInfo;
import l2r.gameserver.scripting.scriptengine.events.SiegeEvent;
import l2r.gameserver.scripting.scriptengine.listeners.events.SiegeListener;
import l2r.gameserver.util.Broadcast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Siege implements Siegable
{
	protected static final Logger _log = LoggerFactory.getLogger(Siege.class);
	
	private static final List<SiegeListener> siegeListeners = new FastList<SiegeListener>().shared();
	
	// typeId's
	public static final byte OWNER = -1;
	public static final byte DEFENDER = 0;
	public static final byte ATTACKER = 1;
	public static final byte DEFENDER_NOT_APPROVED = 2;
	
	public static enum TeleportWhoType
	{
		All,
		Attacker,
		DefenderNotOwner,
		Owner,
		Spectator
	}
	
	private int _controlTowerCount;
	
	public class ScheduleEndSiegeTask implements Runnable
	{
		private final Castle _castleInst;
		
		public ScheduleEndSiegeTask(Castle pCastle)
		{
			_castleInst = pCastle;
		}
		
		@Override
		public void run()
		{
			if (!isInProgress())
			{
				return;
			}
			
			try
			{
				long timeRemaining = _siegeEndDate.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
				if (timeRemaining > 3600000)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HOURS_UNTIL_SIEGE_CONCLUSION);
					sm.addInt(2);
					announceToPlayer(sm, true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining - 3600000); // Prepare task for 1 hr left.
				}
				else if ((timeRemaining <= 3600000) && (timeRemaining > 600000))
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION);
					sm.addInt((int) timeRemaining / 60000);
					announceToPlayer(sm, true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining - 600000); // Prepare task for 10 minute left.
				}
				else if ((timeRemaining <= 600000) && (timeRemaining > 300000))
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION);
					sm.addInt((int) timeRemaining / 60000);
					announceToPlayer(sm, true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining - 300000); // Prepare task for 5 minute left.
				}
				else if ((timeRemaining <= 300000) && (timeRemaining > 10000))
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION);
					sm.addInt((int) timeRemaining / 60000);
					announceToPlayer(sm, true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining - 10000); // Prepare task for 10 seconds count down
				}
				else if ((timeRemaining <= 10000) && (timeRemaining > 0))
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CASTLE_SIEGE_S1_SECONDS_LEFT);
					sm.addInt((int) timeRemaining / 1000);
					announceToPlayer(sm, true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining); // Prepare task for second count down
				}
				else
				{
					_castleInst.getSiege().endSiege();
				}
			}
			catch (Exception e)
			{
				_log.error("", e);
			}
		}
	}
	
	public class ScheduleStartSiegeTask implements Runnable
	{
		private final Castle _castleInst;
		
		public ScheduleStartSiegeTask(Castle pCastle)
		{
			_castleInst = pCastle;
		}
		
		@Override
		public void run()
		{
			_scheduledStartSiegeTask.cancel(false);
			if (isInProgress())
			{
				return;
			}
			
			try
			{
				if (!getIsTimeRegistrationOver())
				{
					long regTimeRemaining = getTimeRegistrationOverDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
					if (regTimeRemaining > 0)
					{
						_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), regTimeRemaining);
						return;
					}
					endTimeRegistration(true);
				}
				
				long timeRemaining = getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
				if (timeRemaining > 86400000)
				{
					_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 86400000); // Prepare task for 24 before siege start to end registration
				}
				else if ((timeRemaining <= 86400000) && (timeRemaining > 13600000))
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.REGISTRATION_TERM_FOR_S1_ENDED);
					sm.addCastleId(getCastle().getResidenceId());
					Announcements.getInstance().announceToAll(sm);
					_isRegistrationOver = true;
					clearSiegeWaitingClan();
					_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 13600000); // Prepare task for 1 hr left before siege start.
				}
				else if ((timeRemaining <= 13600000) && (timeRemaining > 600000))
				{
					_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 600000); // Prepare task for 10 minute left.
				}
				else if ((timeRemaining <= 600000) && (timeRemaining > 300000))
				{
					_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 300000); // Prepare task for 5 minute left.
				}
				else if ((timeRemaining <= 300000) && (timeRemaining > 10000))
				{
					_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 10000); // Prepare task for 10 seconds count down
				}
				else if ((timeRemaining <= 10000) && (timeRemaining > 0))
				{
					_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining); // Prepare task for second count down
				}
				else
				{
					_castleInst.getSiege().startSiege();
				}
			}
			catch (Exception e)
			{
				_log.error("", e);
			}
		}
	}
	
	// must support Concurrent Modifications
	private final List<L2SiegeClan> _attackerClans = new FastList<>();
	private final List<L2SiegeClan> _defenderClans = new FastList<>();
	private final List<L2SiegeClan> _defenderWaitingClans = new FastList<>();
	
	// Castle setting
	private final List<L2ControlTowerInstance> _controlTowers = new ArrayList<>();
	private final List<L2FlameTowerInstance> _flameTowers = new ArrayList<>();
	private final Castle _castle;
	private boolean _isInProgress = false;
	private boolean _isNormalSide = true; // true = Atk is Atk, false = Atk is Def
	protected boolean _isRegistrationOver = false;
	protected Calendar _siegeEndDate;
	private SiegeGuardManager _siegeGuardManager;
	protected ScheduledFuture<?> _scheduledStartSiegeTask = null;
	protected int _firstOwnerClanId = -1;
	
	public Siege(Castle castle)
	{
		_castle = castle;
		_siegeGuardManager = new SiegeGuardManager(getCastle());
		
		startAutoTask();
	}
	
	@Override
	public void endSiege()
	{
		if (isInProgress())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_ENDED);
			sm.addCastleId(getCastle().getResidenceId());
			Announcements.getInstance().announceToAll(sm);
			
			if (getCastle().getOwnerId() > 0)
			{
				L2Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
				sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_VICTORIOUS_OVER_S2_S_SIEGE);
				sm.addString(clan.getName());
				sm.addCastleId(getCastle().getResidenceId());
				Announcements.getInstance().announceToAll(sm);
				
				if (clan.getId() == _firstOwnerClanId)
				{
					// Owner is unchanged
					clan.increaseBloodAllianceCount();
				}
				else
				{
					getCastle().setTicketBuyCount(0);
					for (L2ClanMember member : clan.getMembers())
					{
						if (member != null)
						{
							L2PcInstance player = member.getPlayerInstance();
							if ((player != null) && player.isNoble())
							{
								Hero.getInstance().setCastleTaken(player.getObjectId(), getCastle().getResidenceId());
							}
						}
					}
				}
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_S1_DRAW);
				sm.addCastleId(getCastle().getResidenceId());
				Announcements.getInstance().announceToAll(sm);
			}
			
			for (L2SiegeClan attackerClan : getAttackerClans())
			{
				final L2Clan clan = ClanTable.getInstance().getClan(attackerClan.getClanId());
				if (clan == null)
				{
					continue;
				}
				
				clan.clearSiegeKills();
				clan.clearSiegeDeaths();
			}
			
			for (L2SiegeClan defenderClan : getDefenderClans())
			{
				final L2Clan clan = ClanTable.getInstance().getClan(defenderClan.getClanId());
				if (clan == null)
				{
					continue;
				}
				
				clan.clearSiegeKills();
				clan.clearSiegeDeaths();
			}
			
			getCastle().updateClansReputation();
			removeFlags(); // Removes all flags. Note: Remove flag before teleporting players
			teleportPlayer(Siege.TeleportWhoType.Attacker, TeleportWhereType.Town); // Teleport to the second closest town
			teleportPlayer(Siege.TeleportWhoType.DefenderNotOwner, TeleportWhereType.Town); // Teleport to the second closest town
			teleportPlayer(Siege.TeleportWhoType.Spectator, TeleportWhereType.Town); // Teleport to the second closest town
			_isInProgress = false; // Flag so that siege instance can be started
			updatePlayerSiegeStateFlags(true);
			saveCastleSiege(); // Save castle specific data
			clearSiegeClan(); // Clear siege clan from db
			removeTowers(); // Remove all towers from this castle
			_siegeGuardManager.unspawnSiegeGuard(); // Remove all spawned siege guard from this castle
			if (getCastle().getOwnerId() > 0)
			{
				_siegeGuardManager.removeMercs();
			}
			getCastle().spawnDoor(); // Respawn door to castle
			getCastle().getZone().setIsActive(false);
			getCastle().getZone().updateZoneStatusForCharactersInside();
			getCastle().getZone().setSiegeInstance(null);
			fireSiegeListeners(EventStage.END);
		}
	}
	
	private void removeDefender(L2SiegeClan sc)
	{
		if (sc != null)
		{
			getDefenderClans().remove(sc);
		}
	}
	
	private void removeAttacker(L2SiegeClan sc)
	{
		if (sc != null)
		{
			getAttackerClans().remove(sc);
		}
	}
	
	private void addDefender(L2SiegeClan sc, SiegeClanType type)
	{
		if (sc == null)
		{
			return;
		}
		sc.setType(type);
		getDefenderClans().add(sc);
	}
	
	private void addAttacker(L2SiegeClan sc)
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
	 */
	public void midVictory()
	{
		if (isInProgress()) // Siege still in progress
		{
			if (getCastle().getOwnerId() > 0)
			{
				_siegeGuardManager.removeMercs(); // Remove all merc entry from db
			}
			
			if (getDefenderClans().isEmpty() && // If defender doesn't exist (Pc vs Npc)
			(getAttackerClans().size() == 1 // Only 1 attacker
			))
			{
				L2SiegeClan sc_newowner = getAttackerClan(getCastle().getOwnerId());
				removeAttacker(sc_newowner);
				addDefender(sc_newowner, SiegeClanType.OWNER);
				endSiege();
				return;
			}
			if (getCastle().getOwnerId() > 0)
			{
				int allyId = ClanTable.getInstance().getClan(getCastle().getOwnerId()).getAllyId();
				if (getDefenderClans().isEmpty()) // If defender doesn't exist (Pc vs Npc)
				// and only an alliance attacks
				{
					// The player's clan is in an alliance
					if (allyId != 0)
					{
						boolean allinsamealliance = true;
						for (L2SiegeClan sc : getAttackerClans())
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
							return;
						}
					}
				}
				
				for (L2SiegeClan sc : getDefenderClans())
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
				
				// The player's clan is in an alliance
				for (L2Clan clan : ClanTable.getInstance().getClanAllies(allyId))
				{
					final L2SiegeClan sc = getAttackerClan(clan.getId());
					if (sc != null)
					{
						removeAttacker(sc);
						addDefender(sc, SiegeClanType.DEFENDER);
					}
				}
				teleportPlayer(Siege.TeleportWhoType.Attacker, TeleportWhereType.SiegeFlag); // Teleport to the second closest town
				teleportPlayer(Siege.TeleportWhoType.Spectator, TeleportWhereType.Town); // Teleport to the second closest town
				
				removeDefenderFlags(); // Removes defenders' flags
				getCastle().removeUpgrade(); // Remove all castle upgrade
				getCastle().spawnDoor(true); // Respawn door to castle but make them weaker (50% hp)
				removeTowers(); // Remove all towers from this castle
				_controlTowerCount = 0;// Each new siege midvictory CT are completely respawned.
				spawnControlTower();
				spawnFlameTower();
				updatePlayerSiegeStateFlags(false);
				fireSiegeListeners(EventStage.CONTROL_CHANGE);
			}
		}
	}
	
	/**
	 * When siege starts<BR>
	 * <BR>
	 */
	@Override
	public void startSiege()
	{
		if (!isInProgress())
		{
			if (!fireSiegeListeners(EventStage.START))
			{
				return;
			}
			_firstOwnerClanId = getCastle().getOwnerId();
			
			if (getAttackerClans().isEmpty())
			{
				SystemMessage sm;
				if (_firstOwnerClanId <= 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SIEGE_WAS_CANCELED_BECAUSE_NO_CLANS_PARTICIPATED);
					final L2Clan ownerClan = ClanTable.getInstance().getClan(_firstOwnerClanId);
					ownerClan.increaseBloodAllianceCount();
				}
				sm.addCastleId(getCastle().getResidenceId());
				Announcements.getInstance().announceToAll(sm);
				saveCastleSiege();
				return;
			}
			
			_isNormalSide = true; // Atk is now atk
			_isInProgress = true; // Flag so that same siege instance cannot be started again
			
			loadSiegeClan(); // Load siege clan from db
			updatePlayerSiegeStateFlags(false);
			teleportPlayer(Siege.TeleportWhoType.Attacker, TeleportWhereType.Town); // Teleport to the closest town
			// teleportPlayer(Siege.TeleportWhoType.Spectator, MapRegionTable.TeleportWhereType.Town); // Teleport to the second closest town
			_controlTowerCount = 0;
			spawnControlTower(); // Spawn control tower
			spawnFlameTower(); // Spawn control tower
			getCastle().spawnDoor(); // Spawn door
			spawnSiegeGuard(); // Spawn siege guard
			MercTicketManager.getInstance().deleteTickets(getCastle().getResidenceId()); // remove the tickets from the ground
			getCastle().getZone().setSiegeInstance(this);
			getCastle().getZone().setIsActive(true);
			getCastle().getZone().updateZoneStatusForCharactersInside();
			
			// Schedule a task to prepare auto siege end
			_siegeEndDate = Calendar.getInstance();
			_siegeEndDate.add(Calendar.MINUTE, SiegeManager.getInstance().getSiegeLength());
			ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(getCastle()), 1000); // Prepare auto end task
			
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_STARTED);
			sm.addCastleId(getCastle().getResidenceId());
			Announcements.getInstance().announceToAll(sm);
		}
	}
	
	/**
	 * Announce to player.<BR>
	 * <BR>
	 * @param message The SystemMessage to send to player
	 * @param bothSides True - broadcast to both attackers and defenders. False - only to defenders.
	 */
	public void announceToPlayer(SystemMessage message, boolean bothSides)
	{
		for (L2SiegeClan siegeClans : getDefenderClans())
		{
			L2Clan clan = ClanTable.getInstance().getClan(siegeClans.getClanId());
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				if (member != null)
				{
					member.sendPacket(message);
				}
			}
		}
		
		if (bothSides)
		{
			for (L2SiegeClan siegeClans : getAttackerClans())
			{
				L2Clan clan = ClanTable.getInstance().getClan(siegeClans.getClanId());
				for (L2PcInstance member : clan.getOnlineMembers(0))
				{
					if (member != null)
					{
						member.sendPacket(message);
					}
				}
			}
		}
	}
	
	public void updatePlayerSiegeStateFlags(boolean clear)
	{
		L2Clan clan;
		for (L2SiegeClan siegeclan : getAttackerClans())
		{
			if (siegeclan == null)
			{
				continue;
			}
			
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				if (member == null)
				{
					continue;
				}
				
				if (clear)
				{
					member.setSiegeState((byte) 0);
					member.setSiegeSide(0);
					member.setIsInSiege(false);
					member.stopFameTask();
				}
				else
				{
					member.setSiegeState((byte) 1);
					member.setSiegeSide(getCastle().getResidenceId());
					if (checkIfInZone(member))
					{
						member.setIsInSiege(true);
						member.startFameTask(Config.CASTLE_ZONE_FAME_TASK_FREQUENCY * 1000, Config.CASTLE_ZONE_FAME_AQUIRE_POINTS);
					}
				}
				member.sendPacket(new UserInfo(member));
				member.sendPacket(new ExBrExtraUserInfo(member));
				for (L2PcInstance player : member.getKnownList().getKnownPlayers().values())
				{
					if (player == null)
					{
						continue;
					}
					
					player.sendPacket(new RelationChanged(member, member.getRelation(player), member.isAutoAttackable(player)));
					if (member.hasSummon())
					{
						player.sendPacket(new RelationChanged(member.getSummon(), member.getRelation(player), member.isAutoAttackable(player)));
					}
				}
			}
		}
		for (L2SiegeClan siegeclan : getDefenderClans())
		{
			if (siegeclan == null)
			{
				continue;
			}
			
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				if (member == null)
				{
					continue;
				}
				
				if (clear)
				{
					member.setSiegeState((byte) 0);
					member.setSiegeSide(0);
					member.setIsInSiege(false);
					member.stopFameTask();
				}
				else
				{
					member.setSiegeState((byte) 2);
					member.setSiegeSide(getCastle().getResidenceId());
					if (checkIfInZone(member))
					{
						member.setIsInSiege(true);
						member.startFameTask(Config.CASTLE_ZONE_FAME_TASK_FREQUENCY * 1000, Config.CASTLE_ZONE_FAME_AQUIRE_POINTS);
					}
				}
				member.sendPacket(new UserInfo(member));
				member.sendPacket(new ExBrExtraUserInfo(member));
				
				for (L2PcInstance player : member.getKnownList().getKnownPlayers().values())
				{
					if (player == null)
					{
						continue;
					}
					player.sendPacket(new RelationChanged(member, member.getRelation(player), member.isAutoAttackable(player)));
					if (member.hasSummon())
					{
						player.sendPacket(new RelationChanged(member.getSummon(), member.getRelation(player), member.isAutoAttackable(player)));
					}
				}
			}
		}
	}
	
	/**
	 * Approve clan as defender for siege<BR>
	 * <BR>
	 * @param clanId The int of player's clan id
	 */
	public void approveSiegeDefenderClan(int clanId)
	{
		if (clanId <= 0)
		{
			return;
		}
		saveSiegeClan(ClanTable.getInstance().getClan(clanId), DEFENDER, true);
		loadSiegeClan();
	}
	
	/**
	 * @param object
	 * @return true if object is inside the zone
	 */
	public boolean checkIfInZone(L2Object object)
	{
		return checkIfInZone(object.getX(), object.getY(), object.getZ());
	}
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return true if object is inside the zone
	 */
	public boolean checkIfInZone(int x, int y, int z)
	{
		return (isInProgress() && (getCastle().checkIfInZone(x, y, z))); // Castle zone during siege
	}
	
	/**
	 * Return true if clan is attacker<BR>
	 * <BR>
	 * @param clan The L2Clan of the player
	 */
	@Override
	public boolean checkIsAttacker(L2Clan clan)
	{
		return (getAttackerClan(clan) != null);
	}
	
	/**
	 * Return true if clan is defender<BR>
	 * <BR>
	 * @param clan The L2Clan of the player
	 */
	@Override
	public boolean checkIsDefender(L2Clan clan)
	{
		return (getDefenderClan(clan) != null);
	}
	
	/**
	 * @param clan The L2Clan of the player
	 * @return true if clan is defender waiting approval
	 */
	public boolean checkIsDefenderWaiting(L2Clan clan)
	{
		return (getDefenderWaitingClan(clan) != null);
	}
	
	/** Clear all registered siege clans from database for castle */
	public void clearSiegeClan()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=?"))
		{
			statement.setInt(1, getCastle().getResidenceId());
			statement.execute();
			
			if (getCastle().getOwnerId() > 0)
			{
				try (PreparedStatement delete = con.prepareStatement("DELETE FROM siege_clans WHERE clan_id=?"))
				{
					delete.setInt(1, getCastle().getOwnerId());
					delete.execute();
				}
			}
			
			getAttackerClans().clear();
			getDefenderClans().clear();
			getDefenderWaitingClans().clear();
		}
		catch (Exception e)
		{
			_log.warn("Exception: clearSiegeClan(): " + e.getMessage(), e);
		}
	}
	
	/** Clear all siege clans waiting for approval from database for castle */
	public void clearSiegeWaitingClan()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=? and type = 2"))
		{
			statement.setInt(1, getCastle().getResidenceId());
			statement.execute();
			
			getDefenderWaitingClans().clear();
		}
		catch (Exception e)
		{
			_log.warn("Exception: clearSiegeWaitingClan(): " + e.getMessage(), e);
		}
	}
	
	/** Return list of L2PcInstance registered as attacker in the zone. */
	@Override
	public List<L2PcInstance> getAttackersInZone()
	{
		List<L2PcInstance> players = new ArrayList<>();
		L2Clan clan;
		for (L2SiegeClan siegeclan : getAttackerClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for (L2PcInstance player : clan.getOnlineMembers(0))
			{
				if (player == null)
				{
					continue;
				}
				
				if (player.isInSiege())
				{
					players.add(player);
				}
			}
		}
		return players;
	}
	
	/**
	 * @return list of L2PcInstance registered as defender but not owner in the zone.
	 */
	public List<L2PcInstance> getDefendersButNotOwnersInZone()
	{
		List<L2PcInstance> players = new ArrayList<>();
		L2Clan clan;
		for (L2SiegeClan siegeclan : getDefenderClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			if (clan.getId() == getCastle().getOwnerId())
			{
				continue;
			}
			for (L2PcInstance player : clan.getOnlineMembers(0))
			{
				if (player == null)
				{
					continue;
				}
				
				if (player.isInSiege())
				{
					players.add(player);
				}
			}
		}
		return players;
	}
	
	/**
	 * @return list of L2PcInstance in the zone.
	 */
	public List<L2PcInstance> getPlayersInZone()
	{
		return getCastle().getZone().getPlayersInside();
	}
	
	/**
	 * @return list of L2PcInstance owning the castle in the zone.
	 */
	public List<L2PcInstance> getOwnersInZone()
	{
		List<L2PcInstance> players = new ArrayList<>();
		L2Clan clan;
		for (L2SiegeClan siegeclan : getDefenderClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			if (clan.getId() != getCastle().getOwnerId())
			{
				continue;
			}
			for (L2PcInstance player : clan.getOnlineMembers(0))
			{
				if (player == null)
				{
					continue;
				}
				
				if (player.isInSiege())
				{
					players.add(player);
				}
			}
		}
		return players;
	}
	
	/**
	 * @return list of L2PcInstance not registered as attacker or defender in the zone.
	 */
	public List<L2PcInstance> getSpectatorsInZone()
	{
		List<L2PcInstance> players = new ArrayList<>();
		
		for (L2PcInstance player : getCastle().getZone().getPlayersInside())
		{
			if (player == null)
			{
				continue;
			}
			
			if (!player.isInSiege())
			{
				players.add(player);
			}
		}
		return players;
	}
	
	/**
	 * Control Tower was killed
	 * @param ct
	 */
	public void killedCT(L2Npc ct)
	{
		_controlTowerCount--;
		if (_controlTowerCount < 0)
		{
			_controlTowerCount = 0;
		}
	}
	
	/**
	 * Remove the flag that was killed
	 * @param flag
	 */
	public void killedFlag(L2Npc flag)
	{
		if (flag == null)
		{
			return;
		}
		for (L2SiegeClan clan : getAttackerClans())
		{
			if (clan.removeFlag(flag))
			{
				return;
			}
		}
	}
	
	/**
	 * Display list of registered clans
	 * @param player
	 */
	public void listRegisterClan(L2PcInstance player)
	{
		player.sendPacket(new SiegeInfo(getCastle()));
	}
	
	/**
	 * Register clan as attacker<BR>
	 * <BR>
	 * @param player The L2PcInstance of the player trying to register
	 */
	public void registerAttacker(L2PcInstance player)
	{
		registerAttacker(player, false);
	}
	
	public void registerAttacker(L2PcInstance player, boolean force)
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
			if ((player.getClan().getAllyId() == allyId) && !force)
			{
				player.sendPacket(SystemMessageId.CANNOT_ATTACK_ALLIANCE_CASTLE);
				return;
			}
		}
		
		if (force)
		{
			if (SiegeManager.getInstance().checkIsRegistered(player.getClan(), getCastle().getResidenceId()))
			{
				player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
			}
			else
			{
				saveSiegeClan(player.getClan(), ATTACKER, false); // Save to database
			}
			return;
		}
		
		if (checkIfCanRegister(player, ATTACKER))
		{
			saveSiegeClan(player.getClan(), ATTACKER, false); // Save to database
		}
	}
	
	/**
	 * Register a clan as defender.
	 * @param player the player to register
	 */
	public void registerDefender(L2PcInstance player)
	{
		registerDefender(player, false);
	}
	
	public void registerDefender(L2PcInstance player, boolean force)
	{
		if (getCastle().getOwnerId() <= 0)
		{
			player.sendMessage("You cannot register as a defender because " + getCastle().getName() + " is owned by NPC.");
			return;
		}
		
		if (force)
		{
			if (SiegeManager.getInstance().checkIsRegistered(player.getClan(), getCastle().getResidenceId()))
			{
				player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
			}
			else
			{
				saveSiegeClan(player.getClan(), DEFENDER_NOT_APPROVED, false); // Save to database
			}
			return;
		}
		
		if (checkIfCanRegister(player, DEFENDER_NOT_APPROVED))
		{
			saveSiegeClan(player.getClan(), DEFENDER_NOT_APPROVED, false); // Save to database
		}
	}
	
	/**
	 * Remove clan from siege<BR>
	 * <BR>
	 * @param clanId The int of player's clan id
	 */
	public void removeSiegeClan(int clanId)
	{
		if (clanId <= 0)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=? and clan_id=?"))
		{
			statement.setInt(1, getCastle().getResidenceId());
			statement.setInt(2, clanId);
			statement.execute();
			
			loadSiegeClan();
		}
		catch (Exception e)
		{
			_log.warn("Exception: removeSiegeClan(): " + e.getMessage(), e);
		}
	}
	
	/**
	 * Remove clan from siege<BR>
	 * <BR>
	 * @param clan clan being removed
	 */
	public void removeSiegeClan(L2Clan clan)
	{
		if ((clan == null) || (clan.getCastleId() == getCastle().getResidenceId()) || !SiegeManager.getInstance().checkIsRegistered(clan, getCastle().getResidenceId()))
		{
			return;
		}
		removeSiegeClan(clan.getId());
	}
	
	/**
	 * Remove clan from siege<BR>
	 * <BR>
	 * @param player The L2PcInstance of player/clan being removed
	 */
	public void removeSiegeClan(L2PcInstance player)
	{
		removeSiegeClan(player.getClan());
	}
	
	/**
	 * Start the auto tasks<BR>
	 * <BR>
	 */
	public void startAutoTask()
	{
		correctSiegeDateTime();
		
		_log.info("Siege of " + getCastle().getName() + ": " + getCastle().getSiegeDate().getTime());
		
		loadSiegeClan();
		
		// Schedule siege auto start
		if (_scheduledStartSiegeTask != null)
		{
			_scheduledStartSiegeTask.cancel(false);
		}
		_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new Siege.ScheduleStartSiegeTask(getCastle()), 1000);
	}
	
	/**
	 * Teleport players
	 * @param teleportWho
	 * @param teleportWhere
	 */
	public void teleportPlayer(TeleportWhoType teleportWho, TeleportWhereType teleportWhere)
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
		
		for (L2PcInstance player : players)
		{
			if (player.canOverrideCond(PcCondOverride.CASTLE_CONDITIONS) || player.isJailed())
			{
				continue;
			}
			player.teleToLocation(teleportWhere);
		}
	}
	
	/**
	 * Add clan as attacker<BR>
	 * <BR>
	 * @param clanId The int of clan's id
	 */
	private void addAttacker(int clanId)
	{
		getAttackerClans().add(new L2SiegeClan(clanId, SiegeClanType.ATTACKER)); // Add registered attacker to attacker list
	}
	
	/**
	 * Add clan as defender<BR>
	 * <BR>
	 * @param clanId The int of clan's id
	 */
	private void addDefender(int clanId)
	{
		getDefenderClans().add(new L2SiegeClan(clanId, SiegeClanType.DEFENDER)); // Add registered defender to defender list
	}
	
	/**
	 * <p>
	 * Add clan as defender with the specified type
	 * </p>
	 * @param clanId The int of clan's id
	 * @param type the type of the clan
	 */
	private void addDefender(int clanId, SiegeClanType type)
	{
		getDefenderClans().add(new L2SiegeClan(clanId, type));
	}
	
	/**
	 * Add clan as defender waiting approval<BR>
	 * <BR>
	 * @param clanId The int of clan's id
	 */
	private void addDefenderWaiting(int clanId)
	{
		getDefenderWaitingClans().add(new L2SiegeClan(clanId, SiegeClanType.DEFENDER_PENDING)); // Add registered defender to defender list
	}
	
	/**
	 * @param player The L2PcInstance of the player trying to register
	 * @param typeId -1 = owner 0 = defender, 1 = attacker, 2 = defender waiting
	 * @return true if the player can register.
	 */
	private boolean checkIfCanRegister(L2PcInstance player, byte typeId)
	{
		if (getIsRegistrationOver())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DEADLINE_FOR_SIEGE_S1_PASSED);
			sm.addCastleId(getCastle().getResidenceId());
			player.sendPacket(sm);
		}
		else if (isInProgress())
		{
			player.sendPacket(SystemMessageId.NOT_SIEGE_REGISTRATION_TIME2);
		}
		else if ((player.getClan() == null) || (player.getClan().getLevel() < SiegeManager.getInstance().getSiegeClanMinLevel()))
		{
			player.sendPacket(SystemMessageId.ONLY_CLAN_LEVEL_5_ABOVE_MAY_SIEGE);
		}
		else if (player.getClan().getId() == getCastle().getOwnerId())
		{
			player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING);
		}
		else if (player.getClan().getCastleId() > 0)
		{
			player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_CANNOT_PARTICIPATE_OTHER_SIEGE);
		}
		else if (SiegeManager.getInstance().checkIsRegistered(player.getClan(), getCastle().getResidenceId()))
		{
			player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
		}
		else if (checkIfAlreadyRegisteredForSameDay(player.getClan()))
		{
			player.sendPacket(SystemMessageId.APPLICATION_DENIED_BECAUSE_ALREADY_SUBMITTED_A_REQUEST_FOR_ANOTHER_SIEGE_BATTLE);
		}
		else if ((typeId == ATTACKER) && (getAttackerClans().size() >= SiegeManager.getInstance().getAttackerMaxClans()))
		{
			player.sendPacket(SystemMessageId.ATTACKER_SIDE_FULL);
		}
		else if (((typeId == DEFENDER) || (typeId == DEFENDER_NOT_APPROVED) || (typeId == OWNER)) && ((getDefenderClans().size() + getDefenderWaitingClans().size()) >= SiegeManager.getInstance().getDefenderMaxClans()))
		{
			player.sendPacket(SystemMessageId.DEFENDER_SIDE_FULL);
		}
		else
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * @param clan The L2Clan of the player trying to register
	 * @return true if the clan has already registered to a siege for the same day.
	 */
	public boolean checkIfAlreadyRegisteredForSameDay(L2Clan clan)
	{
		for (Siege siege : SiegeManager.getInstance().getSieges())
		{
			if (siege == this)
			{
				continue;
			}
			if (siege.getSiegeDate().get(Calendar.DAY_OF_WEEK) == getSiegeDate().get(Calendar.DAY_OF_WEEK))
			{
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
				}
			}
		}
		return false;
	}
	
	/**
	 * Return the correct siege date as Calendar.<BR>
	 * <BR>
	 */
	public void correctSiegeDateTime()
	{
		boolean corrected = false;
		
		if (getCastle().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
		{
			// Since siege has past reschedule it to the next one
			// This is usually caused by server being down
			corrected = true;
			setNextSiegeDate();
		}
		
		if (corrected)
		{
			saveSiegeDate();
		}
	}
	
	/** Load siege clans. */
	private void loadSiegeClan()
	{
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT clan_id,type FROM siege_clans where castle_id=?"))
		{
			getAttackerClans().clear();
			getDefenderClans().clear();
			getDefenderWaitingClans().clear();
			
			// Add castle owner as defender (add owner first so that they are on the top of the defender list)
			if (getCastle().getOwnerId() > 0)
			{
				addDefender(getCastle().getOwnerId(), SiegeClanType.OWNER);
			}
			
			statement.setInt(1, getCastle().getResidenceId());
			try (ResultSet rs = statement.executeQuery())
			{
				int typeId;
				while (rs.next())
				{
					typeId = rs.getInt("type");
					if (typeId == DEFENDER)
					{
						addDefender(rs.getInt("clan_id"));
					}
					else if (typeId == ATTACKER)
					{
						addAttacker(rs.getInt("clan_id"));
					}
					else if (typeId == DEFENDER_NOT_APPROVED)
					{
						addDefenderWaiting(rs.getInt("clan_id"));
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Exception: loadSiegeClan(): " + e.getMessage(), e);
		}
	}
	
	/** Remove all spawned towers. */
	private void removeTowers()
	{
		for (L2FlameTowerInstance ct : _flameTowers)
		{
			ct.deleteMe();
		}
		
		for (L2ControlTowerInstance ct : _controlTowers)
		{
			ct.deleteMe();
		}
		
		_flameTowers.clear();
		_controlTowers.clear();
	}
	
	/** Remove all flags. */
	private void removeFlags()
	{
		for (L2SiegeClan sc : getAttackerClans())
		{
			if (sc != null)
			{
				sc.removeFlags();
			}
		}
		for (L2SiegeClan sc : getDefenderClans())
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
		for (L2SiegeClan sc : getDefenderClans())
		{
			if (sc != null)
			{
				sc.removeFlags();
			}
		}
	}
	
	/** Save castle siege related to database. */
	private void saveCastleSiege()
	{
		setNextSiegeDate(); // Set the next set date for 2 weeks from now
		// Schedule Time registration end
		getTimeRegistrationOverDate().setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		getTimeRegistrationOverDate().add(Calendar.DAY_OF_MONTH, 1);
		getCastle().setIsTimeRegistrationOver(false);
		
		saveSiegeDate(); // Save the new date
		startAutoTask(); // Prepare auto start siege and end registration
	}
	
	/** Save siege date to database. */
	public void saveSiegeDate()
	{
		if (_scheduledStartSiegeTask != null)
		{
			_scheduledStartSiegeTask.cancel(true);
			_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new Siege.ScheduleStartSiegeTask(getCastle()), 1000);
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE castle SET siegeDate = ?, regTimeEnd = ?, regTimeOver = ?  WHERE id = ?"))
		{
			statement.setLong(1, getSiegeDate().getTimeInMillis());
			statement.setLong(2, getTimeRegistrationOverDate().getTimeInMillis());
			statement.setString(3, String.valueOf(getIsTimeRegistrationOver()));
			statement.setInt(4, getCastle().getResidenceId());
			statement.execute();
		}
		catch (Exception e)
		{
			_log.warn("Exception: saveSiegeDate(): " + e.getMessage(), e);
		}
	}
	
	/**
	 * Save registration to database.<BR>
	 * <BR>
	 * @param clan The L2Clan of player
	 * @param typeId -1 = owner 0 = defender, 1 = attacker, 2 = defender waiting
	 * @param isUpdateRegistration
	 */
	private void saveSiegeClan(L2Clan clan, byte typeId, boolean isUpdateRegistration)
	{
		if (clan.getCastleId() > 0)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			if ((typeId == DEFENDER) || (typeId == DEFENDER_NOT_APPROVED) || (typeId == OWNER))
			{
				if ((getDefenderClans().size() + getDefenderWaitingClans().size()) >= SiegeManager.getInstance().getDefenderMaxClans())
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
				try (PreparedStatement statement = con.prepareStatement("INSERT INTO siege_clans (clan_id,castle_id,type,castle_owner) values (?,?,?,0)"))
				{
					statement.setInt(1, clan.getId());
					statement.setInt(2, getCastle().getResidenceId());
					statement.setInt(3, typeId);
					statement.execute();
				}
			}
			else
			{
				try (PreparedStatement statement = con.prepareStatement("UPDATE siege_clans SET type = ? WHERE castle_id = ? AND clan_id = ?"))
				{
					statement.setInt(1, typeId);
					statement.setInt(2, getCastle().getResidenceId());
					statement.setInt(3, clan.getId());
					statement.execute();
				}
			}
			
			if ((typeId == DEFENDER) || (typeId == OWNER))
			{
				addDefender(clan.getId());
			}
			else if (typeId == ATTACKER)
			{
				addAttacker(clan.getId());
			}
			else if (typeId == DEFENDER_NOT_APPROVED)
			{
				addDefenderWaiting(clan.getId());
			}
		}
		catch (Exception e)
		{
			_log.warn("Exception: saveSiegeClan(L2Clan clan, int typeId, boolean isUpdateRegistration): " + e.getMessage(), e);
		}
	}
	
	/** Set the date for the next siege. */
	private void setNextSiegeDate()
	{
		final Calendar cal = getCastle().getSiegeDate();
		if (cal.getTimeInMillis() < System.currentTimeMillis())
		{
			cal.setTimeInMillis(System.currentTimeMillis());
		}
		
		for (SiegeScheduleDate holder : SiegeScheduleData.getInstance().getScheduleDates())
		{
			cal.set(Calendar.DAY_OF_WEEK, holder.getDay());
			cal.set(Calendar.HOUR_OF_DAY, holder.getHour());
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			if (cal.before(Calendar.getInstance()))
			{
				cal.add(Calendar.WEEK_OF_YEAR, 2);
			}
			
			if (CastleManager.getInstance().getSiegeDates(cal.getTimeInMillis()) < holder.getMaxConcurrent())
			{
				CastleManager.getInstance().registerSiegeDate(getCastle().getResidenceId(), cal.getTimeInMillis());
				break;
			}
		}
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ANNOUNCED_SIEGE_TIME);
		sm.addCastleId(getCastle().getResidenceId());
		Broadcast.toAllOnlinePlayers(sm);
		
		_isRegistrationOver = false; // Allow registration for next siege
	}
	
	/**
	 * Spawn control tower.
	 */
	private void spawnControlTower()
	{
		for (TowerSpawn ts : SiegeManager.getInstance().getControlTowers(getCastle().getResidenceId()))
		{
			try
			{
				final L2Spawn spawn = new L2Spawn(NpcTable.getInstance().getTemplate(ts.getId()));
				spawn.setLocation(ts.getLocation());
				_controlTowers.add((L2ControlTowerInstance) spawn.doSpawn());
			}
			catch (Exception e)
			{
				_log.warn(getClass().getSimpleName() + ": Cannot spawn control tower! " + e);
			}
		}
		_controlTowerCount = _controlTowers.size();
	}
	
	/**
	 * Spawn flame tower.
	 */
	private void spawnFlameTower()
	{
		for (TowerSpawn ts : SiegeManager.getInstance().getFlameTowers(getCastle().getResidenceId()))
		{
			try
			{
				final L2Spawn spawn = new L2Spawn(NpcTable.getInstance().getTemplate(ts.getId()));
				spawn.setLocation(ts.getLocation());
				final L2FlameTowerInstance tower = (L2FlameTowerInstance) spawn.doSpawn();
				tower.setUpgradeLevel(ts.getUpgradeLevel());
				tower.setZoneList(ts.getZoneList());
				_flameTowers.add(tower);
			}
			catch (Exception e)
			{
				_log.warn(getClass().getSimpleName() + ": Cannot spawn flame tower! " + e);
			}
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
		if (!getSiegeGuardManager().getSiegeGuardSpawn().isEmpty())
		{
			L2ControlTowerInstance closestCt;
			int x, y, z;
			double distance;
			double distanceClosest = 0;
			for (L2Spawn spawn : getSiegeGuardManager().getSiegeGuardSpawn())
			{
				if (spawn == null)
				{
					continue;
				}
				
				closestCt = null;
				distanceClosest = Integer.MAX_VALUE;
				
				x = spawn.getX();
				y = spawn.getY();
				z = spawn.getZ();
				
				for (L2ControlTowerInstance ct : _controlTowers)
				{
					if (ct == null)
					{
						continue;
					}
					
					distance = ct.getDistanceSq(x, y, z);
					
					if (distance < distanceClosest)
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
		}
	}
	
	@Override
	public final L2SiegeClan getAttackerClan(L2Clan clan)
	{
		if (clan == null)
		{
			return null;
		}
		return getAttackerClan(clan.getId());
	}
	
	@Override
	public final L2SiegeClan getAttackerClan(int clanId)
	{
		for (L2SiegeClan sc : getAttackerClans())
		{
			if ((sc != null) && (sc.getClanId() == clanId))
			{
				return sc;
			}
		}
		return null;
	}
	
	@Override
	public final List<L2SiegeClan> getAttackerClans()
	{
		if (_isNormalSide)
		{
			return _attackerClans;
		}
		return _defenderClans;
	}
	
	public final int getAttackerRespawnDelay()
	{
		return (SiegeManager.getInstance().getAttackerRespawnDelay());
	}
	
	public final Castle getCastle()
	{
		if (_castle == null)
		{
			return null;
		}
		return _castle;
	}
	
	@Override
	public final L2SiegeClan getDefenderClan(L2Clan clan)
	{
		if (clan == null)
		{
			return null;
		}
		return getDefenderClan(clan.getId());
	}
	
	@Override
	public final L2SiegeClan getDefenderClan(int clanId)
	{
		for (L2SiegeClan sc : getDefenderClans())
		{
			if ((sc != null) && (sc.getClanId() == clanId))
			{
				return sc;
			}
		}
		return null;
	}
	
	@Override
	public final List<L2SiegeClan> getDefenderClans()
	{
		if (_isNormalSide)
		{
			return _defenderClans;
		}
		return _attackerClans;
	}
	
	public final L2SiegeClan getDefenderWaitingClan(L2Clan clan)
	{
		if (clan == null)
		{
			return null;
		}
		return getDefenderWaitingClan(clan.getId());
	}
	
	public final L2SiegeClan getDefenderWaitingClan(int clanId)
	{
		for (L2SiegeClan sc : getDefenderWaitingClans())
		{
			if ((sc != null) && (sc.getClanId() == clanId))
			{
				return sc;
			}
		}
		return null;
	}
	
	public final List<L2SiegeClan> getDefenderWaitingClans()
	{
		return _defenderWaitingClans;
	}
	
	public final boolean isInProgress()
	{
		return _isInProgress;
	}
	
	public final boolean getIsRegistrationOver()
	{
		return _isRegistrationOver;
	}
	
	public final boolean getIsTimeRegistrationOver()
	{
		return getCastle().getIsTimeRegistrationOver();
	}
	
	@Override
	public final Calendar getSiegeDate()
	{
		return getCastle().getSiegeDate();
	}
	
	public final Calendar getTimeRegistrationOverDate()
	{
		return getCastle().getTimeRegistrationOverDate();
	}
	
	public void endTimeRegistration(boolean automatic)
	{
		getCastle().setIsTimeRegistrationOver(true);
		if (!automatic)
		{
			saveSiegeDate();
		}
	}
	
	@Override
	public List<L2Npc> getFlag(L2Clan clan)
	{
		if (clan != null)
		{
			L2SiegeClan sc = getAttackerClan(clan);
			if (sc != null)
			{
				return sc.getFlag();
			}
		}
		return null;
	}
	
	public final SiegeGuardManager getSiegeGuardManager()
	{
		if (_siegeGuardManager == null)
		{
			_siegeGuardManager = new SiegeGuardManager(getCastle());
		}
		return _siegeGuardManager;
	}
	
	public int getControlTowerCount()
	{
		return _controlTowerCount;
	}
	
	@Override
	public boolean giveFame()
	{
		return true;
	}
	
	@Override
	public int getFameFrequency()
	{
		return Config.CASTLE_ZONE_FAME_TASK_FREQUENCY;
	}
	
	@Override
	public int getFameAmount()
	{
		return Config.CASTLE_ZONE_FAME_AQUIRE_POINTS;
	}
	
	@Override
	public void updateSiege()
	{
	}
	
	// Listeners
	/**
	 * Fires the appropriate SiegeListener<br>
	 * If it returns false on EventStage.start, the siege is cancelled
	 * @param stage
	 * @return
	 */
	private boolean fireSiegeListeners(EventStage stage)
	{
		if (!siegeListeners.isEmpty())
		{
			SiegeEvent event = new SiegeEvent();
			event.setSiege(this);
			event.setStage(stage);
			switch (stage)
			{
				case START:
				{
					for (SiegeListener listener : siegeListeners)
					{
						if (!listener.onStart(event))
						{
							return false;
						}
					}
					break;
				}
				case END:
				{
					for (SiegeListener listener : siegeListeners)
					{
						listener.onEnd(event);
					}
					break;
				}
				case CONTROL_CHANGE:
				{
					for (SiegeListener listener : siegeListeners)
					{
						listener.onControlChange(event);
					}
					break;
				}
			}
		}
		return true;
	}
	
	/**
	 * Adds a siege listener
	 * @param listener
	 */
	public static void addSiegeListener(SiegeListener listener)
	{
		if (!siegeListeners.contains(listener))
		{
			siegeListeners.add(listener);
		}
	}
	
	/**
	 * Removes a siege listener
	 * @param listener
	 */
	public static void removeSiegeListener(SiegeListener listener)
	{
		siegeListeners.remove(listener);
	}
}
