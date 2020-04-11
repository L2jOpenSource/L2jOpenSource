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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.enums.Team;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.instancemanager.tasks.PenaltyRemoveTask;
import l2r.gameserver.model.ArenaParticipantsHolder;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.itemcontainer.PcInventory;
import l2r.gameserver.model.olympiad.OlympiadManager;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExCubeGameAddPlayer;
import l2r.gameserver.network.serverpackets.ExCubeGameChangeTeam;
import l2r.gameserver.network.serverpackets.ExCubeGameRemovePlayer;
import l2r.gameserver.network.serverpackets.SystemMessage;

/**
 * This class manage the player add/remove, team change and event arena status,<br>
 * as the clearance of the participants list or liberate the arena.
 * @author BiggBoss
 */
public final class HandysBlockCheckerManager
{
	// All the participants and their team classified by arena
	private static final ArenaParticipantsHolder[] _arenaPlayers = new ArenaParticipantsHolder[4];
	
	// Arena votes to start the game
	private static final Map<Integer, Integer> _arenaVotes = new HashMap<>();
	
	// Arena Status, True = is being used, otherwise, False
	private static final Map<Integer, Boolean> _arenaStatus = new HashMap<>();
	
	// Registration request penalty (10 seconds)
	protected static Set<Integer> _registrationPenalty = Collections.synchronizedSet(new HashSet<Integer>());
	
	/**
	 * Return the number of event-start votes for the specified arena id
	 * @param arenaId
	 * @return int (number of votes)
	 */
	public synchronized int getArenaVotes(int arenaId)
	{
		return _arenaVotes.get(arenaId);
	}
	
	/**
	 * Add a new vote to start the event for the specified arena id
	 * @param arena
	 */
	public synchronized void increaseArenaVotes(int arena)
	{
		int newVotes = _arenaVotes.get(arena) + 1;
		ArenaParticipantsHolder holder = _arenaPlayers[arena];
		
		if ((newVotes > (holder.getAllPlayers().size() / 2)) && !holder.getEvent().isStarted())
		{
			clearArenaVotes(arena);
			if ((holder.getBlueTeamSize() == 0) || (holder.getRedTeamSize() == 0))
			{
				return;
			}
			if (Config.HBCE_FAIR_PLAY)
			{
				holder.checkAndShuffle();
			}
			ThreadPoolManager.getInstance().executeGeneral(holder.getEvent().new StartEvent());
		}
		else
		{
			_arenaVotes.put(arena, newVotes);
		}
	}
	
	/**
	 * Will clear the votes queue (of event start) for the specified arena id
	 * @param arena
	 */
	public synchronized void clearArenaVotes(int arena)
	{
		_arenaVotes.put(arena, 0);
	}
	
	protected HandysBlockCheckerManager()
	{
		// Initialize arena status
		_arenaStatus.put(0, false);
		_arenaStatus.put(1, false);
		_arenaStatus.put(2, false);
		_arenaStatus.put(3, false);
		
		// Initialize arena votes
		_arenaVotes.put(0, 0);
		_arenaVotes.put(1, 0);
		_arenaVotes.put(2, 0);
		_arenaVotes.put(3, 0);
	}
	
	/**
	 * Returns the players holder
	 * @param arena
	 * @return ArenaParticipantsHolder
	 */
	public ArenaParticipantsHolder getHolder(int arena)
	{
		return _arenaPlayers[arena];
	}
	
	/**
	 * Initializes the participants holder
	 */
	public void startUpParticipantsQueue()
	{
		for (int i = 0; i < 4; ++i)
		{
			_arenaPlayers[i] = new ArenaParticipantsHolder(i);
		}
	}
	
	/**
	 * Add the player to the specified arena (through the specified arena manager) and send the needed server -> client packets
	 * @param player
	 * @param arenaId
	 * @return
	 */
	public boolean addPlayerToArena(L2PcInstance player, int arenaId)
	{
		ArenaParticipantsHolder holder = _arenaPlayers[arenaId];
		
		synchronized (holder)
		{
			boolean isRed;
			
			for (int i = 0; i < 4; i++)
			{
				if (_arenaPlayers[i].getAllPlayers().contains(player))
				{
					SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ALREADY_REGISTERED_ON_THE_MATCH_WAITING_LIST);
					msg.addCharName(player);
					player.sendPacket(msg);
					return false;
				}
			}
			
			if (player.isCursedWeaponEquipped())
			{
				player.sendPacket(SystemMessageId.CANNOT_REGISTER_PROCESSING_CURSED_WEAPON);
				return false;
			}
			
			if (OlympiadManager.getInstance().isRegistered(player))
			{
				OlympiadManager.getInstance().unRegisterNoble(player);
				player.sendPacket(SystemMessageId.COLISEUM_OLYMPIAD_KRATEIS_APPLICANTS_CANNOT_PARTICIPATE);
			}
			
			// if(UnderGroundColiseum.getInstance().isRegisteredPlayer(player))
			// {
			// UngerGroundColiseum.getInstance().removeParticipant(player);
			// player.sendPacket(SystemMessageId.COLISEUM_OLYMPIAD_KRATEIS_APPLICANTS_CANNOT_PARTICIPATE));
			// }
			// if(KrateiCubeManager.getInstance().isRegisteredPlayer(player))
			// {
			// KrateiCubeManager.getInstance().removeParticipant(player);
			// player.sendPacket(SystemMessageId.COLISEUM_OLYMPIAD_KRATEIS_APPLICANTS_CANNOT_PARTICIPATE));
			// }
			
			if (_registrationPenalty.contains(player.getObjectId()))
			{
				player.sendPacket(SystemMessageId.CANNOT_REQUEST_REGISTRATION_10_SECS_AFTER);
				return false;
			}
			
			if (holder.getBlueTeamSize() < holder.getRedTeamSize())
			{
				holder.addPlayer(player, 1);
				isRed = false;
			}
			else
			{
				holder.addPlayer(player, 0);
				isRed = true;
			}
			holder.broadCastPacketToTeam(new ExCubeGameAddPlayer(player, isRed));
			return true;
		}
	}
	
	/**
	 * Will remove the specified player from the specified team and arena and will send the needed packet to all his team mates / enemy team mates
	 * @param player
	 * @param arenaId
	 * @param team
	 */
	public void removePlayer(L2PcInstance player, int arenaId, int team)
	{
		ArenaParticipantsHolder holder = _arenaPlayers[arenaId];
		synchronized (holder)
		{
			boolean isRed = team == 0 ? true : false;
			
			holder.removePlayer(player, team);
			holder.broadCastPacketToTeam(new ExCubeGameRemovePlayer(player, isRed));
			
			// End event if theres an empty team
			int teamSize = isRed ? holder.getRedTeamSize() : holder.getBlueTeamSize();
			if (teamSize == 0)
			{
				holder.getEvent().endEventAbnormally();
			}
			
			_registrationPenalty.add(player.getObjectId());
			schedulePenaltyRemoval(player.getObjectId());
		}
	}
	
	/**
	 * Will change the player from one team to other (if possible) and will send the needed packets
	 * @param player
	 * @param arena
	 * @param team
	 */
	public void changePlayerToTeam(L2PcInstance player, int arena, int team)
	{
		ArenaParticipantsHolder holder = _arenaPlayers[arena];
		
		synchronized (holder)
		{
			boolean isFromRed = holder.getRedPlayers().contains(player);
			
			if (isFromRed && (holder.getBlueTeamSize() == 6))
			{
				player.sendMessage("The team is full");
				return;
			}
			else if (!isFromRed && (holder.getRedTeamSize() == 6))
			{
				player.sendMessage("The team is full");
				return;
			}
			
			int futureTeam = isFromRed ? 1 : 0;
			holder.addPlayer(player, futureTeam);
			
			if (isFromRed)
			{
				holder.removePlayer(player, 0);
			}
			else
			{
				holder.removePlayer(player, 1);
			}
			holder.broadCastPacketToTeam(new ExCubeGameChangeTeam(player, isFromRed));
		}
	}
	
	/**
	 * Will erase all participants from the specified holder
	 * @param arenaId
	 */
	public synchronized void clearPaticipantQueueByArenaId(int arenaId)
	{
		_arenaPlayers[arenaId].clearPlayers();
	}
	
	/**
	 * Returns true if arena is holding an event at this momment
	 * @param arenaId
	 * @return boolean
	 */
	public boolean arenaIsBeingUsed(int arenaId)
	{
		if ((arenaId < 0) || (arenaId > 3))
		{
			return false;
		}
		return _arenaStatus.get(arenaId);
	}
	
	/**
	 * Set the specified arena as being used
	 * @param arenaId
	 */
	public void setArenaBeingUsed(int arenaId)
	{
		_arenaStatus.put(arenaId, true);
	}
	
	/**
	 * Set as free the specified arena for future events
	 * @param arenaId
	 */
	public void setArenaFree(int arenaId)
	{
		_arenaStatus.put(arenaId, false);
	}
	
	/**
	 * Called when played logs out while participating in Block Checker Event
	 * @param player
	 */
	public void onDisconnect(L2PcInstance player)
	{
		int arena = player.getBlockCheckerArena();
		int team = getHolder(arena).getPlayerTeam(player);
		HandysBlockCheckerManager.getInstance().removePlayer(player, arena, team);
		if (player.getTeam() != Team.NONE)
		{
			player.stopAllEffects();
			// Remove team aura
			player.setTeam(Team.NONE);
			
			// Remove the event items
			PcInventory inv = player.getInventory();
			
			if (inv.getItemByItemId(13787) != null)
			{
				long count = inv.getInventoryItemCount(13787, 0);
				inv.destroyItemByItemId("Handys Block Checker", 13787, count, player, player);
			}
			if (inv.getItemByItemId(13788) != null)
			{
				long count = inv.getInventoryItemCount(13788, 0);
				inv.destroyItemByItemId("Handys Block Checker", 13788, count, player, player);
			}
			player.setInsideZone(ZoneIdType.PVP, false);
			// Teleport Back
			player.teleToLocation(-57478, -60367, -2370);
		}
	}
	
	public void removePenalty(int objectId)
	{
		_registrationPenalty.remove(objectId);
	}
	
	private void schedulePenaltyRemoval(int objId)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new PenaltyRemoveTask(objId), 10000);
	}
	
	/**
	 * Gets the single instance of {@code HandysBlockCheckerManager}.
	 * @return single instance of {@code HandysBlockCheckerManager}
	 */
	public static HandysBlockCheckerManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final HandysBlockCheckerManager _instance = new HandysBlockCheckerManager();
	}
}