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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

import javolution.util.FastList;
import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.instancemanager.DimensionalRiftManager;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.network.serverpackets.Earthquake;
import l2r.util.Rnd;

/**
 * Thanks to L2Fortress and balancer.ru - kombat
 */
public class DimensionalRift
{
	protected byte _type;
	protected L2Party _party;
	protected FastList<Byte> _completedRooms = new FastList<>();
	private static final long seconds_5 = 5000L;
	// private static final int MILLISECONDS_IN_MINUTE = 60000;
	protected byte jumps_current = 0;
	
	private Timer teleporterTimer;
	private TimerTask teleporterTimerTask;
	private Timer spawnTimer;
	private TimerTask spawnTimerTask;
	
	private Future<?> earthQuakeTask;
	
	protected byte _choosenRoom = -1;
	private boolean _hasJumped = false;
	protected FastList<L2PcInstance> deadPlayers = new FastList<>();
	protected FastList<L2PcInstance> revivedInWaitingRoom = new FastList<>();
	private boolean isBossRoom = false;
	
	public DimensionalRift(L2Party party, byte type, byte room)
	{
		DimensionalRiftManager.getInstance().getRoom(type, room).setPartyInside(true);
		_type = type;
		_party = party;
		_choosenRoom = room;
		int[] coords = getRoomCoord(room);
		party.setDimensionalRift(this);
		for (L2PcInstance p : party.getMembers())
		{
			final Quest riftQuest = QuestManager.getInstance().getQuest(635);
			if (riftQuest != null)
			{
				QuestState qs = p.getQuestState(riftQuest.getName());
				if (qs == null)
				{
					qs = riftQuest.newQuestState(p);
				}
				if (!qs.isStarted())
				{
					qs.startQuest();
				}
			}
			p.teleToLocation(new Location(coords[0], coords[1], coords[2]));
		}
		createSpawnTimer(_choosenRoom);
		createTeleporterTimer(true);
	}
	
	public byte getType()
	{
		return _type;
	}
	
	public byte getCurrentRoom()
	{
		return _choosenRoom;
	}
	
	protected void createTeleporterTimer(final boolean reasonTP)
	{
		if (_party == null)
		{
			return;
		}
		
		if (teleporterTimerTask != null)
		{
			teleporterTimerTask.cancel();
			teleporterTimerTask = null;
		}
		
		if (teleporterTimer != null)
		{
			teleporterTimer.cancel();
			teleporterTimer = null;
		}
		
		if (earthQuakeTask != null)
		{
			earthQuakeTask.cancel(false);
			earthQuakeTask = null;
		}
		
		teleporterTimer = new Timer();
		teleporterTimerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				if (_choosenRoom > -1)
				{
					DimensionalRiftManager.getInstance().getRoom(_type, _choosenRoom).unspawn().setPartyInside(false);
				}
				
				if (reasonTP && (jumps_current < getMaxJumps()) && (_party.getMemberCount() > deadPlayers.size()))
				{
					jumps_current++;
					
					_completedRooms.add(_choosenRoom);
					_choosenRoom = -1;
					
					for (L2PcInstance p : _party.getMembers())
					{
						if (!revivedInWaitingRoom.contains(p))
						{
							teleportToNextRoom(p);
						}
					}
					createTeleporterTimer(true);
					createSpawnTimer(_choosenRoom);
				}
				else
				{
					for (L2PcInstance p : _party.getMembers())
					{
						if (!revivedInWaitingRoom.contains(p))
						{
							teleportToWaitingRoom(p);
						}
					}
					killRift();
					cancel();
				}
			}
		};
		
		if (reasonTP)
		{
			long jumpTime = calcTimeToNextJump();
			teleporterTimer.schedule(teleporterTimerTask, jumpTime); // Teleporter task, 8-10 minutes
			
			earthQuakeTask = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					for (L2PcInstance p : _party.getMembers())
					{
						if (!revivedInWaitingRoom.contains(p))
						{
							p.sendPacket(new Earthquake(p.getX(), p.getY(), p.getZ(), 65, 9));
						}
					}
				}
			}, jumpTime - 7000);
		}
		else
		{
			teleporterTimer.schedule(teleporterTimerTask, seconds_5); // incorrect party member invited.
		}
	}
	
	public void createSpawnTimer(final byte room)
	{
		if (spawnTimerTask != null)
		{
			spawnTimerTask.cancel();
			spawnTimerTask = null;
		}
		
		if (spawnTimer != null)
		{
			spawnTimer.cancel();
			spawnTimer = null;
		}
		
		spawnTimer = new Timer();
		spawnTimerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				DimensionalRiftManager.getInstance().getRoom(_type, room).spawn();
			}
		};
		
		spawnTimer.schedule(spawnTimerTask, Config.RIFT_SPAWN_DELAY);
	}
	
	public void partyMemberInvited()
	{
		createTeleporterTimer(false);
	}
	
	public void partyMemberExited(L2PcInstance player)
	{
		if (deadPlayers.contains(player))
		{
			deadPlayers.remove(player);
		}
		
		if (revivedInWaitingRoom.contains(player))
		{
			revivedInWaitingRoom.remove(player);
		}
		
		if ((_party.getMemberCount() < Config.RIFT_MIN_PARTY_SIZE) || (_party.getMemberCount() == 1))
		{
			for (L2PcInstance p : _party.getMembers())
			{
				teleportToWaitingRoom(p);
			}
			killRift();
		}
	}
	
	public void manualTeleport(L2PcInstance player, L2Npc npc)
	{
		if (!player.isInParty() || !player.getParty().isInDimensionalRift())
		{
			return;
		}
		
		if (player.getObjectId() != player.getParty().getLeaderObjectId())
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
			return;
		}
		
		if (_hasJumped)
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/AlreadyTeleported.htm", npc);
			return;
		}
		
		_hasJumped = true;
		DimensionalRiftManager.getInstance().getRoom(_type, _choosenRoom).unspawn().setPartyInside(false);
		_completedRooms.add(_choosenRoom);
		_choosenRoom = -1;
		
		for (L2PcInstance p : _party.getMembers())
		{
			teleportToNextRoom(p);
		}
		
		DimensionalRiftManager.getInstance().getRoom(_type, _choosenRoom).setPartyInside(true);
		
		createSpawnTimer(_choosenRoom);
		createTeleporterTimer(true);
	}
	
	public void manualExitRift(L2PcInstance player, L2Npc npc)
	{
		if (!player.isInParty() || !player.getParty().isInDimensionalRift())
		{
			return;
		}
		
		if (player.getObjectId() != player.getParty().getLeaderObjectId())
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
			return;
		}
		
		for (L2PcInstance p : player.getParty().getMembers())
		{
			teleportToWaitingRoom(p);
		}
		killRift();
	}
	
	protected void teleportToNextRoom(L2PcInstance player)
	{
		
		if (_choosenRoom == -1)
		{
			FastList<Byte> emptyRooms;
			do
			{
				emptyRooms = DimensionalRiftManager.getInstance().getFreeRooms(_type);
				// Do not tp in the same room a second time
				emptyRooms.removeAll(_completedRooms);
				// If no room left, find any empty
				if (emptyRooms.isEmpty())
				{
					emptyRooms = DimensionalRiftManager.getInstance().getFreeRooms(_type);
				}
				_choosenRoom = emptyRooms.get(Rnd.get(1, emptyRooms.size()) - 1);
			}
			while (DimensionalRiftManager.getInstance().getRoom(_type, _choosenRoom).isPartyInside());
		}
		
		DimensionalRiftManager.getInstance().getRoom(_type, _choosenRoom).setPartyInside(true);
		checkBossRoom(_choosenRoom);
		int[] coords = getRoomCoord(_choosenRoom);
		player.teleToLocation(new Location(coords[0], coords[1], coords[2]));
	}
	
	protected void teleportToWaitingRoom(L2PcInstance player)
	{
		DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
		final Quest riftQuest = QuestManager.getInstance().getQuest(635);
		if (riftQuest != null)
		{
			final QuestState qs = player.getQuestState(riftQuest.getName());
			if ((qs != null) && qs.isCond(1))
			{
				qs.exitQuest(true, true);
			}
		}
	}
	
	public void killRift()
	{
		_completedRooms = null;
		
		if (_party != null)
		{
			_party.setDimensionalRift(null);
		}
		
		_party = null;
		revivedInWaitingRoom = null;
		deadPlayers = null;
		
		if (earthQuakeTask != null)
		{
			earthQuakeTask.cancel(false);
			earthQuakeTask = null;
		}
		
		DimensionalRiftManager.getInstance().getRoom(_type, _choosenRoom).unspawn().setPartyInside(false);
		DimensionalRiftManager.getInstance().killRift(this);
	}
	
	public Timer getTeleportTimer()
	{
		return teleporterTimer;
	}
	
	public TimerTask getTeleportTimerTask()
	{
		return teleporterTimerTask;
	}
	
	public Timer getSpawnTimer()
	{
		return spawnTimer;
	}
	
	public TimerTask getSpawnTimerTask()
	{
		return spawnTimerTask;
	}
	
	public void setTeleportTimer(Timer t)
	{
		teleporterTimer = t;
	}
	
	public void setTeleportTimerTask(TimerTask tt)
	{
		teleporterTimerTask = tt;
	}
	
	public void setSpawnTimer(Timer t)
	{
		spawnTimer = t;
	}
	
	public void setSpawnTimerTask(TimerTask st)
	{
		spawnTimerTask = st;
	}
	
	private long calcTimeToNextJump()
	{
		int time = Rnd.get(Config.RIFT_AUTO_JUMPS_TIME_MIN, Config.RIFT_AUTO_JUMPS_TIME_MAX) * 1000;
		
		if (isBossRoom)
		{
			return (long) (time * Config.RIFT_BOSS_ROOM_TIME_MUTIPLY);
		}
		return time;
	}
	
	public void memberDead(L2PcInstance player)
	{
		if (!deadPlayers.contains(player))
		{
			deadPlayers.add(player);
		}
	}
	
	public void memberRessurected(L2PcInstance player)
	{
		if (deadPlayers.contains(player))
		{
			deadPlayers.remove(player);
		}
	}
	
	public void usedTeleport(L2PcInstance player)
	{
		if (!revivedInWaitingRoom.contains(player))
		{
			revivedInWaitingRoom.add(player);
		}
		
		if (!deadPlayers.contains(player))
		{
			deadPlayers.add(player);
		}
		
		if ((_party.getMemberCount() - revivedInWaitingRoom.size()) < Config.RIFT_MIN_PARTY_SIZE)
		{
			// int pcm = _party.getMemberCount();
			// int rev = revivedInWaitingRoom.size();
			// int min = Config.RIFT_MIN_PARTY_SIZE;
			
			for (L2PcInstance p : _party.getMembers())
			{
				if ((p != null) && !revivedInWaitingRoom.contains(p))
				{
					teleportToWaitingRoom(p);
				}
			}
			killRift();
		}
	}
	
	public FastList<L2PcInstance> getDeadMemberList()
	{
		return deadPlayers;
	}
	
	public FastList<L2PcInstance> getRevivedAtWaitingRoom()
	{
		return revivedInWaitingRoom;
	}
	
	public void checkBossRoom(byte room)
	{
		isBossRoom = DimensionalRiftManager.getInstance().getRoom(_type, room).isBossRoom();
	}
	
	public int[] getRoomCoord(byte room)
	{
		return DimensionalRiftManager.getInstance().getRoom(_type, room).getTeleportCoorinates();
	}
	
	public byte getMaxJumps()
	{
		if ((Config.RIFT_MAX_JUMPS <= 8) && (Config.RIFT_MAX_JUMPS >= 1))
		{
			return (byte) Config.RIFT_MAX_JUMPS;
		}
		return 4;
	}
}
