/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.Config;
import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.data.sql.impl.ClanTable;
import com.l2jserver.gameserver.enums.FortTeleportWhoType;
import com.l2jserver.gameserver.instancemanager.FortManager;
import com.l2jserver.gameserver.instancemanager.FortSiegeGuardManager;
import com.l2jserver.gameserver.instancemanager.FortSiegeManager;
import com.l2jserver.gameserver.instancemanager.TerritoryWarManager;
import com.l2jserver.gameserver.model.CombatFlag;
import com.l2jserver.gameserver.model.FortSiegeSpawn;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.L2SiegeClan;
import com.l2jserver.gameserver.model.L2SiegeClan.SiegeClanType;
import com.l2jserver.gameserver.model.L2Spawn;
import com.l2jserver.gameserver.model.PcCondOverride;
import com.l2jserver.gameserver.model.TeleportWhereType;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jserver.gameserver.model.actor.instance.L2FortCommanderInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.events.EventDispatcher;
import com.l2jserver.gameserver.model.events.impl.sieges.fort.OnFortSiegeFinish;
import com.l2jserver.gameserver.model.events.impl.sieges.fort.OnFortSiegeStart;
import com.l2jserver.gameserver.network.NpcStringId;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.clientpackets.Say2;
import com.l2jserver.gameserver.network.serverpackets.NpcSay;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class FortSiege implements Siegable
{
	protected static final Logger _log = Logger.getLogger(FortSiege.class.getName());
	
	// SQL
	private static final String DELETE_FORT_SIEGECLANS_BY_CLAN_ID = "DELETE FROM fortsiege_clans WHERE fort_id = ? AND clan_id = ?";
	private static final String DELETE_FORT_SIEGECLANS = "DELETE FROM fortsiege_clans WHERE fort_id = ?";
	
	public class ScheduleEndSiegeTask implements Runnable
	{
		@Override
		public void run()
		{
			if (!isInProgress())
			{
				return;
			}
			
			try
			{
				_siegeEnd = null;
				endSiege();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Exception: ScheduleEndSiegeTask() for Fort: " + _fort.getName() + " " + e.getMessage(), e);
			}
		}
	}
	
	public class ScheduleStartSiegeTask implements Runnable
	{
		private final Fort _fortInst;
		private final int _time;
		
		public ScheduleStartSiegeTask(int time)
		{
			_fortInst = _fort;
			_time = time;
		}
		
		@Override
		public void run()
		{
			if (isInProgress())
			{
				return;
			}
			
			try
			{
				final SystemMessage sm;
				if (_time == 3600) // 1hr remains
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(600), 3000000); // Prepare task for 10 minutes left.
				}
				else if (_time == 600) // 10min remains
				{
					getFort().despawnSuspiciousMerchant();
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_THE_FORTRESS_BATTLE_STARTS);
					sm.addInt(10);
					announceToPlayer(sm);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(300), 300000); // Prepare task for 5 minutes left.
				}
				else if (_time == 300) // 5min remains
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_THE_FORTRESS_BATTLE_STARTS);
					sm.addInt(5);
					announceToPlayer(sm);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(60), 240000); // Prepare task for 1 minute left.
				}
				else if (_time == 60) // 1min remains
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_THE_FORTRESS_BATTLE_STARTS);
					sm.addInt(1);
					announceToPlayer(sm);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(30), 30000); // Prepare task for 30 seconds left.
				}
				else if (_time == 30) // 30seconds remains
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SECONDS_UNTIL_THE_FORTRESS_BATTLE_STARTS);
					sm.addInt(30);
					announceToPlayer(sm);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(10), 20000); // Prepare task for 10 seconds left.
				}
				else if (_time == 10) // 10seconds remains
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SECONDS_UNTIL_THE_FORTRESS_BATTLE_STARTS);
					sm.addInt(10);
					announceToPlayer(sm);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(5), 5000); // Prepare task for 5 seconds left.
				}
				else if (_time == 5) // 5seconds remains
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SECONDS_UNTIL_THE_FORTRESS_BATTLE_STARTS);
					sm.addInt(5);
					announceToPlayer(sm);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(1), 4000); // Prepare task for 1 seconds left.
				}
				else if (_time == 1) // 1seconds remains
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SECONDS_UNTIL_THE_FORTRESS_BATTLE_STARTS);
					sm.addInt(1);
					announceToPlayer(sm);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(0), 1000); // Prepare task start siege.
				}
				else if (_time == 0)// start siege
				{
					_fortInst.getSiege().startSiege();
				}
				else
				{
					_log.warning("Exception: ScheduleStartSiegeTask(): unknown siege time: " + String.valueOf(_time));
				}
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Exception: ScheduleStartSiegeTask() for Fort: " + _fortInst.getName() + " " + e.getMessage(), e);
			}
		}
	}
	
	public class ScheduleSuspiciousMerchantSpawn implements Runnable
	{
		@Override
		public void run()
		{
			if (isInProgress())
			{
				return;
			}
			
			try
			{
				_fort.spawnSuspiciousMerchant();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Exception: ScheduleSuspicoiusMerchantSpawn() for Fort: " + _fort.getName() + " " + e.getMessage(), e);
			}
		}
	}
	
	public class ScheduleSiegeRestore implements Runnable
	{
		@Override
		public void run()
		{
			if (!isInProgress())
			{
				return;
			}
			
			try
			{
				_siegeRestore = null;
				resetSiege();
				announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.BARRACKS_FUNCTION_RESTORED));
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Exception: ScheduleSiegeRestore() for Fort: " + _fort.getName() + " " + e.getMessage(), e);
			}
		}
	}
	
	private final List<L2SiegeClan> _attackerClans = new CopyOnWriteArrayList<>();
	
	// Fort setting
	protected List<L2Spawn> _commanders = new CopyOnWriteArrayList<>();
	protected final Fort _fort;
	private boolean _isInProgress = false;
	private FortSiegeGuardManager _siegeGuardManager;
	ScheduledFuture<?> _siegeEnd = null;
	ScheduledFuture<?> _siegeRestore = null;
	ScheduledFuture<?> _siegeStartTask = null;
	
	public FortSiege(Fort fort)
	{
		_fort = fort;
		
		checkAutoTask();
		FortSiegeManager.getInstance().addSiege(this);
	}
	
	/**
	 * When siege ends.
	 */
	@Override
	public void endSiege()
	{
		if (isInProgress())
		{
			_isInProgress = false; // Flag so that siege instance can be started
			removeFlags(); // Removes all flags. Note: Remove flag before teleporting players
			unSpawnFlags();
			
			updatePlayerSiegeStateFlags(true);
			
			int ownerId = -1;
			if (getFort().getOwnerClan() != null)
			{
				ownerId = getFort().getOwnerClan().getId();
			}
			getFort().getZone().banishForeigners(ownerId);
			getFort().getZone().setIsActive(false);
			getFort().getZone().updateZoneStatusForCharactersInside();
			getFort().getZone().setSiegeInstance(null);
			
			saveFortSiege(); // Save fort specific data
			clearSiegeClan(); // Clear siege clan from db
			removeCommanders(); // Remove commander from this fort
			
			getFort().spawnNpcCommanders(); // Spawn NPC commanders
			getSiegeGuardManager().unspawnSiegeGuard(); // Remove all spawned siege guard from this fort
			getFort().resetDoors(); // Respawn door to fort
			
			ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleSuspiciousMerchantSpawn(), FortSiegeManager.getInstance().getSuspiciousMerchantRespawnDelay() * 60 * 1000L); // Prepare 3hr task for suspicious merchant respawn
			setSiegeDateTime(true); // store suspicious merchant spawn in DB
			
			if (_siegeEnd != null)
			{
				_siegeEnd.cancel(true);
				_siegeEnd = null;
			}
			if (_siegeRestore != null)
			{
				_siegeRestore.cancel(true);
				_siegeRestore = null;
			}
			
			if ((getFort().getOwnerClan() != null) && (getFort().getFlagPole().getMeshIndex() == 0))
			{
				getFort().setVisibleFlag(true);
			}
			
			_log.info("Siege of " + getFort().getName() + " fort finished.");
			
			// Notify to scripts.
			EventDispatcher.getInstance().notifyEventAsync(new OnFortSiegeFinish(this), getFort());
		}
	}
	
	/**
	 * When siege starts
	 */
	@Override
	public void startSiege()
	{
		if (!isInProgress())
		{
			if (_siegeStartTask != null) // used admin command "admin_startfortsiege"
			{
				_siegeStartTask.cancel(true);
				getFort().despawnSuspiciousMerchant();
			}
			_siegeStartTask = null;
			
			if (getAttackerClans().isEmpty())
			{
				return;
			}
			
			_isInProgress = true; // Flag so that same siege instance cannot be started again
			
			loadSiegeClan(); // Load siege clan from db
			updatePlayerSiegeStateFlags(false);
			teleportPlayer(FortTeleportWhoType.Attacker, TeleportWhereType.TOWN); // Teleport to the closest town
			
			getFort().despawnNpcCommanders(); // Despawn NPC commanders
			spawnCommanders(); // Spawn commanders
			getFort().resetDoors(); // Spawn door
			spawnSiegeGuard(); // Spawn siege guard
			getFort().setVisibleFlag(false);
			getFort().getZone().setSiegeInstance(this);
			getFort().getZone().setIsActive(true);
			getFort().getZone().updateZoneStatusForCharactersInside();
			
			// Schedule a task to prepare auto siege end
			_siegeEnd = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(), FortSiegeManager.getInstance().getSiegeLength() * 60 * 1000L); // Prepare auto end task
			
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_S1_HAS_BEGUN);
			sm.addCastleId(getFort().getResidenceId());
			announceToPlayer(sm);
			saveFortSiege();
			
			_log.info("Siege of " + getFort().getName() + " fort started.");
			
			// Notify to scripts.
			EventDispatcher.getInstance().notifyEventAsync(new OnFortSiegeStart(this), getFort());
		}
	}
	
	/**
	 * Announce to player.
	 * @param sm the system message to send to player
	 */
	public void announceToPlayer(SystemMessage sm)
	{
		// announce messages only for participants
		L2Clan clan;
		for (L2SiegeClan siegeclan : getAttackerClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				member.sendPacket(sm);
			}
		}
		if (getFort().getOwnerClan() != null)
		{
			clan = ClanTable.getInstance().getClan(getFort().getOwnerClan().getId());
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				if (member != null)
				{
					member.sendPacket(sm);
				}
			}
		}
	}
	
	public void announceToPlayer(SystemMessage sm, String s)
	{
		sm.addString(s);
		announceToPlayer(sm);
	}
	
	public void updatePlayerSiegeStateFlags(boolean clear)
	{
		L2Clan clan;
		for (L2SiegeClan siegeclan : getAttackerClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
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
					member.setSiegeSide(getFort().getResidenceId());
					if (checkIfInZone(member))
					{
						member.setIsInSiege(true);
						member.startFameTask(Config.FORTRESS_ZONE_FAME_TASK_FREQUENCY * 1000, Config.FORTRESS_ZONE_FAME_AQUIRE_POINTS);
					}
				}
				member.broadcastUserInfo();
			}
		}
		if (getFort().getOwnerClan() != null)
		{
			clan = ClanTable.getInstance().getClan(getFort().getOwnerClan().getId());
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
					member.setSiegeSide(getFort().getResidenceId());
					if (checkIfInZone(member))
					{
						member.setIsInSiege(true);
						member.startFameTask(Config.FORTRESS_ZONE_FAME_TASK_FREQUENCY * 1000, Config.FORTRESS_ZONE_FAME_AQUIRE_POINTS);
					}
				}
				member.broadcastUserInfo();
			}
		}
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
		return (isInProgress() && (getFort().checkIfInZone(x, y, z))); // Fort zone during siege
	}
	
	/**
	 * @param clan The L2Clan of the player
	 * @return true if clan is attacker
	 */
	@Override
	public boolean checkIsAttacker(L2Clan clan)
	{
		return (getAttackerClan(clan) != null);
	}
	
	/**
	 * @param clan The L2Clan of the player
	 * @return true if clan is defender
	 */
	@Override
	public boolean checkIsDefender(L2Clan clan)
	{
		if ((clan != null) && (getFort().getOwnerClan() == clan))
		{
			return true;
		}
		
		return false;
	}
	
	/** Clear all registered siege clans from database for fort */
	public void clearSiegeClan()
	{
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=?"))
		{
			ps.setInt(1, getFort().getResidenceId());
			ps.execute();
			
			if (getFort().getOwnerClan() != null)
			{
				try (PreparedStatement delete = con.prepareStatement("DELETE FROM fortsiege_clans WHERE clan_id=?"))
				{
					delete.setInt(1, getFort().getOwnerClan().getId());
					delete.execute();
				}
			}
			
			getAttackerClans().clear();
			
			// if siege is in progress, end siege
			if (isInProgress())
			{
				endSiege();
			}
			
			// if siege isn't in progress (1hr waiting time till siege starts), cancel waiting time
			if (_siegeStartTask != null)
			{
				_siegeStartTask.cancel(true);
				_siegeStartTask = null;
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: clearSiegeClan(): " + e.getMessage(), e);
		}
	}
	
	/** Set the date for the next siege. */
	private void clearSiegeDate()
	{
		getFort().getSiegeDate().setTimeInMillis(0);
	}
	
	/**
	 * @return list of L2PcInstance registered as attacker in the zone.
	 */
	@Override
	public List<L2PcInstance> getAttackersInZone()
	{
		final List<L2PcInstance> players = new LinkedList<>();
		for (L2SiegeClan siegeclan : getAttackerClans())
		{
			L2Clan clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for (L2PcInstance player : clan.getOnlineMembers(0))
			{
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
		return getFort().getZone().getPlayersInside();
	}
	
	/**
	 * @return list of L2PcInstance owning the fort in the zone.
	 */
	public List<L2PcInstance> getOwnersInZone()
	{
		final List<L2PcInstance> players = new LinkedList<>();
		if (getFort().getOwnerClan() != null)
		{
			L2Clan clan = ClanTable.getInstance().getClan(getFort().getOwnerClan().getId());
			if (clan != getFort().getOwnerClan())
			{
				return null;
			}
			
			for (L2PcInstance player : clan.getOnlineMembers(0))
			{
				if (player.isInSiege())
				{
					players.add(player);
				}
			}
		}
		return players;
	}
	
	/**
	 * TODO: To DP AI<br>
	 * Commander was killed
	 * @param instance
	 */
	public void killedCommander(L2FortCommanderInstance instance)
	{
		if (!_commanders.isEmpty() && (getFort() != null))
		{
			L2Spawn spawn = instance.getSpawn();
			if (spawn != null)
			{
				List<FortSiegeSpawn> commanders = FortSiegeManager.getInstance().getCommanderSpawnList(getFort().getResidenceId());
				for (FortSiegeSpawn spawn2 : commanders)
				{
					if (spawn2.getId() == spawn.getId())
					{
						NpcStringId npcString = null;
						switch (spawn2.getMessageId())
						{
							case 1:
								npcString = NpcStringId.YOU_MAY_HAVE_BROKEN_OUR_ARROWS_BUT_YOU_WILL_NEVER_BREAK_OUR_WILL_ARCHERS_RETREAT;
								break;
							case 2:
								npcString = NpcStringId.AIIEEEE_COMMAND_CENTER_THIS_IS_GUARD_UNIT_WE_NEED_BACKUP_RIGHT_AWAY;
								break;
							case 3:
								npcString = NpcStringId.AT_LAST_THE_MAGIC_FIELD_THAT_PROTECTS_THE_FORTRESS_HAS_WEAKENED_VOLUNTEERS_STAND_BACK;
								break;
							case 4:
								npcString = NpcStringId.I_FEEL_SO_MUCH_GRIEF_THAT_I_CANT_EVEN_TAKE_CARE_OF_MYSELF_THERE_ISNT_ANY_REASON_FOR_ME_TO_STAY_HERE_ANY_LONGER;
								break;
						}
						if (npcString != null)
						{
							instance.broadcastPacket(new NpcSay(instance.getObjectId(), Say2.NPC_SHOUT, instance.getId(), npcString));
						}
					}
				}
				_commanders.remove(spawn);
				if (_commanders.isEmpty())
				{
					// spawn fort flags
					spawnFlag(getFort().getResidenceId());
					// cancel door/commanders respawn
					if (_siegeRestore != null)
					{
						_siegeRestore.cancel(true);
					}
					// open doors in main building
					for (L2DoorInstance door : getFort().getDoors())
					{
						if (door.getIsShowHp())
						{
							continue;
						}
						
						// TODO this also opens control room door at big fort
						door.openMe();
					}
					getFort().getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.ALL_BARRACKS_OCCUPIED));
				}
				// schedule restoring doors/commanders respawn
				else if (_siegeRestore == null)
				{
					getFort().getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.SEIZED_BARRACKS));
					_siegeRestore = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleSiegeRestore(), FortSiegeManager.getInstance().getCountDownLength() * 60 * 1000L);
				}
				else
				{
					getFort().getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.SEIZED_BARRACKS));
				}
			}
			else
			{
				_log.warning("FortSiege.killedCommander(): killed commander, but commander not registered for fortress. NpcId: " + instance.getId() + " FortId: " + getFort().getResidenceId());
			}
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
	 * Register clan as attacker.<BR>
	 * @param player The L2PcInstance of the player trying to register.
	 * @param checkConditions True if should be checked conditions, false otherwise
	 * @return Number that defines what happened. <BR>
	 *         0 - Player don't have clan.<BR>
	 *         1 - Player don't have enough adena to register.<BR>
	 *         2 - Is not right time to register Fortress now.<BR>
	 *         3 - Players clan is already registered to siege.<BR>
	 *         4 - Players clan is successfully registered to siege.
	 */
	public int addAttacker(L2PcInstance player, boolean checkConditions)
	{
		if (player.getClan() == null)
		{
			return 0; // Player dont have clan
		}
		
		if (checkConditions)
		{
			if (getFort().getSiege().getAttackerClans().isEmpty() && (player.getInventory().getAdena() < 250000))
			{
				return 1; // Player dont havee enough adena to register
			}
			
			else if ((System.currentTimeMillis() < TerritoryWarManager.getInstance().getTWStartTimeInMillis()) && TerritoryWarManager.getInstance().getIsRegistrationOver())
			{
				return 2; // Is not right time to register Fortress now
			}
			
			if ((System.currentTimeMillis() > TerritoryWarManager.getInstance().getTWStartTimeInMillis()) && TerritoryWarManager.getInstance().isTWChannelOpen())
			{
				return 2; // Is not right time to register Fortress now
			}
			
			for (Fort fort : FortManager.getInstance().getForts())
			{
				if (fort.getSiege().getAttackerClan(player.getClanId()) != null)
				{
					return 3; // Players clan is already registred to siege
				}
				
				if ((fort.getOwnerClan() == player.getClan()) && (fort.getSiege().isInProgress() || (fort.getSiege()._siegeStartTask != null)))
				{
					return 3; // Players clan is already registred to siege
				}
			}
		}
		
		saveSiegeClan(player.getClan());
		if (getAttackerClans().size() == 1)
		{
			if (checkConditions)
			{
				player.reduceAdena("FortressSiege", 250000, null, true);
			}
			startAutoTask(true);
		}
		return 4; // Players clan is successfully registred to siege
	}
	
	/**
	 * Remove clan from siege
	 * @param clan The clan being removed
	 */
	public void removeAttacker(L2Clan clan)
	{
		if ((clan == null) || (clan.getFortId() == getFort().getResidenceId()) || !FortSiegeManager.getInstance().checkIsRegistered(clan, getFort().getResidenceId()))
		{
			return;
		}
		removeSiegeClan(clan.getId());
	}
	
	/**
	 * This function does not do any checks and should not be called from bypass !
	 * @param clanId
	 */
	private void removeSiegeClan(int clanId)
	{
		final String query = (clanId != 0) ? DELETE_FORT_SIEGECLANS_BY_CLAN_ID : DELETE_FORT_SIEGECLANS;
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			ps.setInt(1, getFort().getResidenceId());
			if (clanId != 0)
			{
				ps.setInt(2, clanId);
			}
			ps.execute();
			
			loadSiegeClan();
			if (getAttackerClans().isEmpty())
			{
				if (isInProgress())
				{
					endSiege();
				}
				else
				{
					saveFortSiege(); // Clear siege time in DB
				}
				
				if (_siegeStartTask != null)
				{
					_siegeStartTask.cancel(true);
					_siegeStartTask = null;
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on removeSiegeClan: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Start the auto tasks
	 */
	public void checkAutoTask()
	{
		if (_siegeStartTask != null)
		{
			return;
		}
		
		final long delay = getFort().getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
		
		if (delay < 0)
		{
			// siege time in past
			saveFortSiege();
			clearSiegeClan(); // remove all clans
			// spawn suspicious merchant immediately
			ThreadPoolManager.getInstance().executeGeneral(new ScheduleSuspiciousMerchantSpawn());
		}
		else
		{
			loadSiegeClan();
			if (getAttackerClans().isEmpty())
			{
				// no attackers - waiting for suspicious merchant spawn
				ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleSuspiciousMerchantSpawn(), delay);
			}
			else
			{
				// preparing start siege task
				if (delay > 3600000) // more than hour, how this can happens ? spawn suspicious merchant
				{
					ThreadPoolManager.getInstance().executeGeneral(new ScheduleSuspiciousMerchantSpawn());
					_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new FortSiege.ScheduleStartSiegeTask(3600), delay - 3600000);
				}
				if (delay > 600000) // more than 10 min, spawn suspicious merchant
				{
					ThreadPoolManager.getInstance().executeGeneral(new ScheduleSuspiciousMerchantSpawn());
					_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new FortSiege.ScheduleStartSiegeTask(600), delay - 600000);
				}
				else if (delay > 300000)
				{
					_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new FortSiege.ScheduleStartSiegeTask(300), delay - 300000);
				}
				else if (delay > 60000)
				{
					_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new FortSiege.ScheduleStartSiegeTask(60), delay - 60000);
				}
				else
				{
					// lower than 1 min, set to 1 min
					_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new FortSiege.ScheduleStartSiegeTask(60), 0);
				}
				
				_log.info("Siege of " + getFort().getName() + " fort: " + getFort().getSiegeDate().getTime());
			}
		}
	}
	
	/**
	 * Start the auto task
	 * @param setTime
	 */
	public void startAutoTask(boolean setTime)
	{
		if (_siegeStartTask != null)
		{
			return;
		}
		
		if (setTime)
		{
			setSiegeDateTime(false);
		}
		
		if (getFort().getOwnerClan() != null)
		{
			getFort().getOwnerClan().broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.A_FORTRESS_IS_UNDER_ATTACK));
		}
		
		// Execute siege auto start
		_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new FortSiege.ScheduleStartSiegeTask(3600), 0);
	}
	
	/**
	 * Teleport players
	 * @param teleportWho
	 * @param teleportWhere
	 */
	public void teleportPlayer(FortTeleportWhoType teleportWho, TeleportWhereType teleportWhere)
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
			default:
				players = getPlayersInZone();
		}
		
		for (L2PcInstance player : players)
		{
			if (player.canOverrideCond(PcCondOverride.FORTRESS_CONDITIONS) || player.isJailed())
			{
				continue;
			}
			
			player.teleToLocation(teleportWhere);
		}
	}
	
	/**
	 * Add clan as attacker<
	 * @param clanId
	 */
	private void addAttacker(int clanId)
	{
		getAttackerClans().add(new L2SiegeClan(clanId, SiegeClanType.ATTACKER)); // Add registered attacker to attacker list
	}
	
	/**
	 * @param clan
	 * @return {@code true} if the clan has already registered to a siege for the same day, {@code false} otherwise.
	 */
	public boolean checkIfAlreadyRegisteredForSameDay(L2Clan clan)
	{
		for (FortSiege siege : FortSiegeManager.getInstance().getSieges())
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
			}
		}
		
		return false;
	}
	
	private void setSiegeDateTime(boolean merchant)
	{
		Calendar newDate = Calendar.getInstance();
		if (merchant)
		{
			newDate.add(Calendar.MINUTE, FortSiegeManager.getInstance().getSuspiciousMerchantRespawnDelay());
		}
		else
		{
			newDate.add(Calendar.MINUTE, 60);
		}
		getFort().setSiegeDate(newDate);
		saveSiegeDate();
	}
	
	/** Load siege clans. */
	private void loadSiegeClan()
	{
		getAttackerClans().clear();
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT clan_id FROM fortsiege_clans WHERE fort_id=?"))
		{
			ps.setInt(1, getFort().getResidenceId());
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					addAttacker(rs.getInt("clan_id"));
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: loadSiegeClan(): " + e.getMessage(), e);
		}
	}
	
	/** Remove commanders. */
	private void removeCommanders()
	{
		if ((_commanders != null) && !_commanders.isEmpty())
		{
			// Remove all instance of commanders for this fort
			for (L2Spawn spawn : _commanders)
			{
				if (spawn != null)
				{
					spawn.stopRespawn();
					if (spawn.getLastSpawn() != null)
					{
						spawn.getLastSpawn().deleteMe();
					}
				}
			}
			_commanders.clear();
		}
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
	}
	
	/** Save fort siege related to database. */
	private void saveFortSiege()
	{
		clearSiegeDate(); // clear siege date
		saveSiegeDate(); // Save the new date
	}
	
	/** Save siege date to database. */
	private void saveSiegeDate()
	{
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE fort SET siegeDate = ? WHERE id = ?"))
		{
			ps.setLong(1, getSiegeDate().getTimeInMillis());
			ps.setInt(2, getFort().getResidenceId());
			ps.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: saveSiegeDate(): " + e.getMessage(), e);
		}
	}
	
	/**
	 * Save registration to database.
	 * @param clan
	 */
	private void saveSiegeClan(L2Clan clan)
	{
		if (getAttackerClans().size() >= FortSiegeManager.getInstance().getAttackerMaxClans())
		{
			return;
		}
		
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO fortsiege_clans (clan_id,fort_id) values (?,?)"))
		{
			ps.setInt(1, clan.getId());
			ps.setInt(2, getFort().getResidenceId());
			ps.execute();
			
			addAttacker(clan.getId());
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: saveSiegeClan(L2Clan clan): " + e.getMessage(), e);
		}
	}
	
	/** Spawn commanders. */
	private void spawnCommanders()
	{
		// Set commanders array size if one does not exist
		try
		{
			_commanders.clear();
			for (FortSiegeSpawn _sp : FortSiegeManager.getInstance().getCommanderSpawnList(getFort().getResidenceId()))
			{
				final L2Spawn spawnDat = new L2Spawn(_sp.getId());
				spawnDat.setAmount(1);
				spawnDat.setX(_sp.getLocation().getX());
				spawnDat.setY(_sp.getLocation().getY());
				spawnDat.setZ(_sp.getLocation().getZ());
				spawnDat.setHeading(_sp.getLocation().getHeading());
				spawnDat.setRespawnDelay(60);
				spawnDat.doSpawn();
				spawnDat.stopRespawn();
				_commanders.add(spawnDat);
			}
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.log(Level.WARNING, "FortSiege.spawnCommander: Spawn could not be initialized: " + e.getMessage(), e);
		}
	}
	
	private void spawnFlag(int Id)
	{
		for (CombatFlag cf : FortSiegeManager.getInstance().getFlagList(Id))
		{
			cf.spawnMe();
		}
	}
	
	private void unSpawnFlags()
	{
		if (FortSiegeManager.getInstance().getFlagList(getFort().getResidenceId()) == null)
		{
			return;
		}
		
		for (CombatFlag cf : FortSiegeManager.getInstance().getFlagList(getFort().getResidenceId()))
		{
			cf.unSpawnMe();
		}
	}
	
	/**
	 * Spawn siege guard.
	 */
	private void spawnSiegeGuard()
	{
		getSiegeGuardManager().spawnSiegeGuard();
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
		return _attackerClans;
	}
	
	public final Fort getFort()
	{
		return _fort;
	}
	
	public final boolean isInProgress()
	{
		return _isInProgress;
	}
	
	@Override
	public final Calendar getSiegeDate()
	{
		return getFort().getSiegeDate();
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
	
	public final FortSiegeGuardManager getSiegeGuardManager()
	{
		if (_siegeGuardManager == null)
		{
			_siegeGuardManager = new FortSiegeGuardManager(getFort());
		}
		
		return _siegeGuardManager;
	}
	
	public void resetSiege()
	{
		// reload commanders and repair doors
		removeCommanders();
		spawnCommanders();
		getFort().resetDoors();
	}
	
	public List<L2Spawn> getCommanders()
	{
		return _commanders;
	}
	
	@Override
	public L2SiegeClan getDefenderClan(int clanId)
	{
		return null;
	}
	
	@Override
	public L2SiegeClan getDefenderClan(L2Clan clan)
	{
		return null;
	}
	
	@Override
	public List<L2SiegeClan> getDefenderClans()
	{
		return null;
	}
	
	@Override
	public boolean giveFame()
	{
		return true;
	}
	
	@Override
	public int getFameFrequency()
	{
		return Config.FORTRESS_ZONE_FAME_TASK_FREQUENCY;
	}
	
	@Override
	public int getFameAmount()
	{
		return Config.FORTRESS_ZONE_FAME_AQUIRE_POINTS;
	}
	
	@Override
	public void updateSiege()
	{
	}
}
