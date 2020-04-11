/*
 * Copyright (C) 2004-2015 L2J Server
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.DuelResult;
import l2r.gameserver.enums.DuelState;
import l2r.gameserver.enums.Team;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.enums.audio.Music;
import l2r.gameserver.instancemanager.DuelManager;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.instance.L2DoorInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.ExDuelEnd;
import l2r.gameserver.network.serverpackets.ExDuelReady;
import l2r.gameserver.network.serverpackets.ExDuelStart;
import l2r.gameserver.network.serverpackets.ExDuelUpdateUserInfo;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.PlaySound;
import l2r.gameserver.network.serverpackets.SocialAction;
import l2r.gameserver.network.serverpackets.SystemMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Duel
{
	protected static final Logger LOG = LoggerFactory.getLogger(Duel.class);
	
	private static final PlaySound B04_S01 = Music.B04_S01.getPacket();
	
	private static final int PARTY_DUEL_DURATION = 300;
	private static final int PARTY_DUEL_PREPARE_TIME = 30;
	private static final int PARTY_DUEL_TELEPORT_BACK_TIME = 10 * 1000;
	private static final int PLAYER_DUEL_DURATION = 120;
	private static final int DUEL_PREPARE_TIME = 5;
	
	private final int _duelId;
	private final List<L2PcInstance> _teamA;
	private final L2PcInstance _leaderA;
	private final List<L2PcInstance> _teamB;
	private final L2PcInstance _leaderB;
	private final boolean _partyDuel;
	private final Calendar _duelEndTime;
	private int _surrenderRequest = 0;
	private int _countdown;
	private final Map<Integer, PlayerCondition> _playerConditions = new ConcurrentHashMap<>();
	private int _duelInstanceId;
	
	public Duel(L2PcInstance playerA, L2PcInstance playerB, boolean partyDuel, int duelId)
	{
		_duelId = duelId;
		if (partyDuel)
		{
			_leaderA = playerA;
			_leaderB = playerB;
			_teamA = new ArrayList<>(playerA.getParty().getMembers());
			_teamB = new ArrayList<>(playerB.getParty().getMembers());
		}
		else
		{
			_leaderA = playerA;
			_leaderB = playerB;
			
			_teamA = new ArrayList<>();
			_teamB = new ArrayList<>();
			
			_teamA.add(playerA);
			_teamB.add(playerB);
		}
		_partyDuel = partyDuel;
		
		_duelEndTime = Calendar.getInstance();
		_duelEndTime.add(Calendar.SECOND, _partyDuel ? PARTY_DUEL_DURATION : PLAYER_DUEL_DURATION);
		
		savePlayerConditions();
		if (_partyDuel)
		{
			_countdown = PARTY_DUEL_PREPARE_TIME;
			teleportPlayers();
		}
		else
		{
			_countdown = DUEL_PREPARE_TIME;
		}
		// Schedule duel start
		ThreadPoolManager.getInstance().scheduleGeneral(new DuelPreparationTask(this), _countdown - 3);
	}
	
	public static class PlayerCondition
	{
		private L2PcInstance _player;
		private double _hp;
		private double _mp;
		private double _cp;
		private boolean _paDuel;
		private int _x, _y, _z;
		private Set<L2Effect> _debuffs;
		
		public PlayerCondition(L2PcInstance player, boolean partyDuel)
		{
			if (player == null)
			{
				return;
			}
			_player = player;
			_hp = _player.getCurrentHp();
			_mp = _player.getCurrentMp();
			_cp = _player.getCurrentCp();
			_paDuel = partyDuel;
			
			if (_paDuel)
			{
				_x = _player.getX();
				_y = _player.getY();
				_z = _player.getZ();
			}
		}
		
		public void restoreCondition()
		{
			if (_player == null)
			{
				return;
			}
			_player.setCurrentHp(_hp);
			_player.setCurrentMp(_mp);
			_player.setCurrentCp(_cp);
			
			_player.setIsInDuel(0);
			_player.setTeam(Team.NONE);
			_player.broadcastUserInfo();
			
			if (_paDuel)
			{
				teleportBack();
			}
			
			if (_debuffs != null) // Debuff removal
			{
				for (L2Effect temp : _debuffs)
				{
					if (temp != null)
					{
						temp.exit();
					}
				}
			}
		}
		
		public void registerDebuff(L2Effect debuff)
		{
			if (_debuffs == null)
			{
				_debuffs = ConcurrentHashMap.newKeySet();
			}
			
			_debuffs.add(debuff);
		}
		
		public void teleportBack()
		{
			_player.teleToLocation(_x, _y, _z);
		}
		
		public L2PcInstance getPlayer()
		{
			return _player;
		}
	}
	
	public static class DuelPreparationTask implements Runnable
	{
		private final Duel _duel;
		
		public DuelPreparationTask(Duel duel)
		{
			_duel = duel;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_duel.countdown() > 0) // duel not started yet - continue countdown
				{
					ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
				}
				else
				{
					_duel.startDuel();
				}
			}
			catch (Exception e)
			{
				LOG.error("There has been a problem while runing a duel start task!", e);
			}
		}
	}
	
	public class DuelClockTask implements Runnable
	{
		private final Duel _duel;
		
		public DuelClockTask(Duel duel)
		{
			_duel = duel;
		}
		
		@Override
		public void run()
		{
			try
			{
				switch (_duel.checkEndDuelCondition())
				{
					case CONTINUE:
					{
						ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
						break;
					}
					default:
					{
						endDuel();
						break;
					}
				}
			}
			catch (Exception e)
			{
				LOG.error("There has been a problem while runing a duel task!", e);
			}
		}
	}
	
	/**
	 * Starts the duel
	 */
	public void startDuel()
	{
		// Set duel state and team
		
		// Send duel packets
		broadcastToTeam1(ExDuelReady.PARTY_DUEL);
		broadcastToTeam2(ExDuelReady.PARTY_DUEL);
		broadcastToTeam1(ExDuelStart.PARTY_DUEL);
		broadcastToTeam2(ExDuelStart.PARTY_DUEL);
		
		for (L2PcInstance temp : _teamA)
		{
			temp.cancelActiveTrade();
			temp.setIsInDuel(_duelId);
			temp.setTeam(Team.BLUE);
			temp.broadcastUserInfo();
			broadcastToTeam2(new ExDuelUpdateUserInfo(temp));
		}
		for (L2PcInstance temp : _teamB)
		{
			temp.cancelActiveTrade();
			temp.setIsInDuel(_duelId);
			temp.setTeam(Team.RED);
			temp.broadcastUserInfo();
			broadcastToTeam1(new ExDuelUpdateUserInfo(temp));
		}
		
		if (_partyDuel)
		{
			// Close doors chickens cannot run from the destiny
			for (L2DoorInstance door : InstanceManager.getInstance().getInstance(getDueldInstanceId()).getDoors())
			{
				if ((door != null) && door.isOpened())
				{
					door.closeMe();
				}
			}
		}
		
		// play sound
		broadcastToTeam1(B04_S01);
		broadcastToTeam2(B04_S01);
		
		// start dueling task
		ThreadPoolManager.getInstance().scheduleGeneral(new DuelClockTask(this), 1000);
	}
	
	/**
	 * Stops all players from attacking. Used for duel timeout / interrupt.
	 */
	private void stopFighting()
	{
		for (L2PcInstance temp : _teamA)
		{
			temp.abortCast();
			temp.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			temp.setTarget(null);
			temp.sendPacket(ActionFailed.STATIC_PACKET);
		}
		for (L2PcInstance temp : _teamB)
		{
			temp.abortCast();
			temp.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			temp.setTarget(null);
			temp.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	/**
	 * Save the current player condition: hp, mp, cp, location
	 */
	public void savePlayerConditions()
	{
		for (L2PcInstance player : _teamA)
		{
			_playerConditions.put(player.getObjectId(), new PlayerCondition(player, _partyDuel));
		}
		for (L2PcInstance player : _teamB)
		{
			_playerConditions.put(player.getObjectId(), new PlayerCondition(player, _partyDuel));
		}
	}
	
	/**
	 * Restore player conditions
	 */
	public void restorePlayerConditions()
	{
		// restore player conditions
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			_playerConditions.values().forEach(c -> c.restoreCondition());
		} , _partyDuel ? PARTY_DUEL_TELEPORT_BACK_TIME : 1000);
		
		ThreadPoolManager.getInstance().scheduleGeneral(() -> clear(), _partyDuel ? PARTY_DUEL_TELEPORT_BACK_TIME : 1000);
	}
	
	/**
	 * Get the duel id
	 * @return id
	 */
	public int getId()
	{
		return _duelId;
	}
	
	/**
	 * Get duel instance id
	 * @return id
	 */
	public int getDueldInstanceId()
	{
		return _duelInstanceId;
	}
	
	/**
	 * Returns the remaining time
	 * @return remaining time
	 */
	public int getRemainingTime()
	{
		return (int) (_duelEndTime.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
	}
	
	/**
	 * Get the team that requested the duel
	 * @return duel requester
	 */
	public List<L2PcInstance> getTeamA()
	{
		return _teamA;
	}
	
	/**
	 * Get the team that was challenged
	 * @return challenged team
	 */
	public List<L2PcInstance> getTeamB()
	{
		return _teamB;
	}
	
	/**
	 * Get the team that requested the duel
	 * @return duel requester
	 */
	public L2PcInstance getTeamLeaderA()
	{
		return _leaderA;
	}
	
	/**
	 * Get the team that was challenged
	 * @return challenged team
	 */
	public L2PcInstance getTeamLeaderB()
	{
		return _leaderB;
	}
	
	/**
	 * Get the duel looser
	 * @return looser
	 */
	public List<L2PcInstance> getLooser()
	{
		if ((_leaderA == null) || (_leaderB == null))
		{
			return null;
		}
		if (_leaderA.getDuelState() == DuelState.WINNER)
		{
			return _teamB;
		}
		else if (_leaderB.getDuelState() == DuelState.WINNER)
		{
			return _teamA;
		}
		return null;
	}
	
	/**
	 * Returns whether this is a party duel or not
	 * @return is party duel
	 */
	public boolean isPartyDuel()
	{
		return _partyDuel;
	}
	
	/**
	 * Teleports all players to a party duel instance.
	 */
	public void teleportPlayers()
	{
		if (!_partyDuel)
		{
			return;
		}
		
		_duelInstanceId = InstanceManager.getInstance().createDynamicInstance("PartyDuel.xml");
		
		Instance instance = InstanceManager.getInstance().getInstance(_duelInstanceId);
		
		int i = 0;
		for (L2PcInstance player : _teamA)
		{
			Location loc = instance.getEnterLocs().get(i++);
			
			player.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), 0, _duelInstanceId, 0);
		}
		
		i = 9;
		for (L2PcInstance player : _teamB)
		{
			Location loc = instance.getEnterLocs().get(i++);
			player.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), 0, _duelInstanceId, 0);
		}
	}
	
	/**
	 * Broadcast a packet to the challenger team
	 * @param packet
	 */
	public void broadcastToTeam1(L2GameServerPacket packet)
	{
		if ((_teamA == null) || _teamA.isEmpty())
		{
			return;
		}
		
		for (L2PcInstance temp : _teamA)
		{
			temp.sendPacket(packet);
		}
	}
	
	/**
	 * Broadcast a packet to the challenged team
	 * @param packet
	 */
	public void broadcastToTeam2(L2GameServerPacket packet)
	{
		if ((_teamB == null) || _teamB.isEmpty())
		{
			return;
		}
		
		for (L2PcInstance temp : _teamB)
		{
			temp.sendPacket(packet);
		}
	}
	
	/**
	 * Playback the bow animation for all looser
	 */
	private void playKneelAnimation()
	{
		List<L2PcInstance> looser = getLooser();
		
		if (looser == null)
		{
			return;
		}
		
		for (L2PcInstance temp : looser)
		{
			temp.broadcastPacket(new SocialAction(temp.getObjectId(), 7));
		}
	}
	
	/**
	 * Do the countdown and send message to players if necessary
	 * @return current count
	 */
	int countdown()
	{
		if (--_countdown > 3)
		{
			return _countdown;
		}
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_DUEL_WILL_BEGIN_IN_S1_SECONDS);
		if (_countdown > 0)
		{
			sm.addInt(_countdown);
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.LET_THE_DUEL_BEGIN);
		}
		
		broadcastToTeam1(sm);
		broadcastToTeam2(sm);
		
		return _countdown;
	}
	
	/**
	 * The duel has reached a state in which it can no longer continue
	 */
	void endDuel()
	{
		// Send end duel packet
		final ExDuelEnd duelEnd = _partyDuel ? ExDuelEnd.PARTY_DUEL : ExDuelEnd.PLAYER_DUEL;
		broadcastToTeam1(duelEnd);
		broadcastToTeam2(duelEnd);
		playKneelAnimation();
		sendEndMessages();
		restorePlayerConditions();
	}
	
	/**
	 * Clear current duel from DuelManager
	 */
	private void clear()
	{
		InstanceManager.getInstance().destroyInstance(getDueldInstanceId());
		DuelManager.getInstance().removeDuel(this);
	}
	
	/**
	 * Send required messages for duel end
	 */
	private void sendEndMessages()
	{
		SystemMessage sm = null;
		switch (checkEndDuelCondition())
		{
			case TEAM_1_WIN:
			case TEAM_2_SURRENDER:
				if (_partyDuel)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.C1_PARTY_HAS_WON_THE_DUEL);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_DUEL);
				}
				sm.addString(_leaderA.getName());
				break;
			case TEAM_1_SURRENDER:
			case TEAM_2_WIN:
				if (_partyDuel)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.C1_PARTY_HAS_WON_THE_DUEL);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_DUEL);
				}
				sm.addString(_leaderB.getName());
				break;
			case CANCELED:
			case TIMEOUT:
				stopFighting();
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_DUEL_HAS_ENDED_IN_A_TIE);
				break;
		}
		broadcastToTeam1(sm);
		broadcastToTeam2(sm);
	}
	
	/**
	 * Did a situation occur in which the duel has to be ended?
	 * @return DuelResult duel status
	 */
	DuelResult checkEndDuelCondition()
	{
		// one of the players might leave during duel
		if ((_teamA == null) || (_teamB == null))
		{
			return DuelResult.CANCELED;
		}
		
		// got a duel surrender request?
		if (_surrenderRequest != 0)
		{
			if (_surrenderRequest == 1)
			{
				return DuelResult.TEAM_1_SURRENDER;
			}
			return DuelResult.TEAM_2_SURRENDER;
		}
		// duel timed out
		else if (getRemainingTime() <= 0)
		{
			return DuelResult.TIMEOUT;
		}
		// Has a player been declared winner yet?
		else if (_leaderA.getDuelState() == DuelState.WINNER)
		{
			// If there is a Winner already there should be no more fighting going on
			stopFighting();
			return DuelResult.TEAM_1_WIN;
		}
		else if (_leaderB.getDuelState() == DuelState.WINNER)
		{
			// If there is a Winner already there should be no more fighting going on
			stopFighting();
			return DuelResult.TEAM_2_WIN;
		}
		
		// More end duel conditions for 1on1 duels
		else if (!_partyDuel)
		{
			// Duel was interrupted e.g.: player was attacked by mobs / other players
			if ((_leaderA.getDuelState() == DuelState.INTERRUPTED) || (_leaderB.getDuelState() == DuelState.INTERRUPTED))
			{
				return DuelResult.CANCELED;
			}
			
			// Are the players too far apart?
			if (!_leaderA.isInsideRadius(_leaderB, 2000, false, false))
			{
				return DuelResult.CANCELED;
			}
			
			// is one of the players in a Siege, Peace or PvP zone?
			if (_leaderA.isInsideZone(ZoneIdType.PEACE) || _leaderB.isInsideZone(ZoneIdType.PEACE) || _leaderA.isInsideZone(ZoneIdType.SIEGE) || _leaderB.isInsideZone(ZoneIdType.SIEGE) || _leaderA.isInsideZone(ZoneIdType.PVP) || _leaderB.isInsideZone(ZoneIdType.PVP))
			{
				return DuelResult.CANCELED;
			}
		}
		
		return DuelResult.CONTINUE;
	}
	
	/**
	 * Register a surrender request
	 * @param player the player that surrenders.
	 */
	public void doSurrender(L2PcInstance player)
	{
		// already received a surrender request
		if ((_surrenderRequest != 0) || _partyDuel)
		{
			return;
		}
		
		// stop the fight
		stopFighting();
		
		if (player == _leaderA)
		{
			_surrenderRequest = 1;
			_leaderA.setDuelState(DuelState.DEAD);
			_leaderB.setDuelState(DuelState.WINNER);
		}
		else if (player == _leaderB)
		{
			_surrenderRequest = 2;
			_leaderB.setDuelState(DuelState.DEAD);
			_leaderA.setDuelState(DuelState.WINNER);
		}
	}
	
	/**
	 * This function is called whenever a player was defeated in a duel
	 * @param player the player defeated.
	 */
	public void onPlayerDefeat(L2PcInstance player)
	{
		// Set player as defeated
		player.setDuelState(DuelState.DEAD);
		player.setTeam(Team.NONE);
		
		if (_partyDuel)
		{
			boolean teamdefeated = true;
			
			boolean isInTeamA = true;
			
			if (_teamA.contains(player))
			{
				for (L2PcInstance temp : _teamA)
				{
					if (temp.getDuelState() == DuelState.DUELLING)
					{
						teamdefeated = false;
						break;
					}
				}
			}
			else if (_teamB.contains(player))
			{
				isInTeamA = false;
				for (L2PcInstance temp : _teamB)
				{
					if (temp.getDuelState() == DuelState.DUELLING)
					{
						teamdefeated = false;
						break;
					}
				}
			}
			if (teamdefeated)
			{
				List<L2PcInstance> winners = (isInTeamA ? _teamB : _teamA);
				for (L2PcInstance temp : winners)
				{
					temp.setDuelState(DuelState.WINNER);
				}
			}
		}
		else
		{
			if ((player != _leaderA) && (player != _leaderB))
			{
				LOG.warn("Error in onPlayerDefeat(): player is not part of this 1vs1 duel!");
			}
			
			if (_leaderA == player)
			{
				_leaderB.setDuelState(DuelState.WINNER);
			}
			else
			{
				_leaderA.setDuelState(DuelState.WINNER);
			}
		}
	}
	
	public void onBuff(L2PcInstance player, L2Effect debuff)
	{
		final PlayerCondition cond = _playerConditions.get(player.getObjectId());
		if (cond != null)
		{
			cond.registerDebuff(debuff);
		}
	}
}