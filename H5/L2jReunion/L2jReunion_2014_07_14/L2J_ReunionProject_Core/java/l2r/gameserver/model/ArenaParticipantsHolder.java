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
package l2r.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.instancemanager.HandysBlockCheckerManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.BlockCheckerEngine;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.SystemMessage;

/**
 * @author xban1x
 */
public final class ArenaParticipantsHolder
{
	private final int _arena;
	private final List<L2PcInstance> _redPlayers;
	private final List<L2PcInstance> _bluePlayers;
	private final BlockCheckerEngine _engine;
	
	public ArenaParticipantsHolder(int arena)
	{
		_arena = arena;
		_redPlayers = new ArrayList<>(6);
		_bluePlayers = new ArrayList<>(6);
		_engine = new BlockCheckerEngine(this, _arena);
	}
	
	public List<L2PcInstance> getRedPlayers()
	{
		return _redPlayers;
	}
	
	public List<L2PcInstance> getBluePlayers()
	{
		return _bluePlayers;
	}
	
	public List<L2PcInstance> getAllPlayers()
	{
		List<L2PcInstance> all = new ArrayList<>(12);
		all.addAll(_redPlayers);
		all.addAll(_bluePlayers);
		return all;
	}
	
	public void addPlayer(L2PcInstance player, int team)
	{
		if (team == 0)
		{
			_redPlayers.add(player);
		}
		else
		{
			_bluePlayers.add(player);
		}
	}
	
	public void removePlayer(L2PcInstance player, int team)
	{
		if (team == 0)
		{
			_redPlayers.remove(player);
		}
		else
		{
			_bluePlayers.remove(player);
		}
	}
	
	public int getPlayerTeam(L2PcInstance player)
	{
		if (_redPlayers.contains(player))
		{
			return 0;
		}
		else if (_bluePlayers.contains(player))
		{
			return 1;
		}
		else
		{
			return -1;
		}
	}
	
	public int getRedTeamSize()
	{
		return _redPlayers.size();
	}
	
	public int getBlueTeamSize()
	{
		return _bluePlayers.size();
	}
	
	public void broadCastPacketToTeam(L2GameServerPacket packet)
	{
		for (L2PcInstance p : _redPlayers)
		{
			p.sendPacket(packet);
		}
		for (L2PcInstance p : _bluePlayers)
		{
			p.sendPacket(packet);
		}
	}
	
	public void clearPlayers()
	{
		_redPlayers.clear();
		_bluePlayers.clear();
	}
	
	public BlockCheckerEngine getEvent()
	{
		return _engine;
	}
	
	public void updateEvent()
	{
		_engine.updatePlayersOnStart(this);
	}
	
	public void checkAndShuffle()
	{
		final int redSize = _redPlayers.size();
		final int blueSize = _bluePlayers.size();
		if (redSize > (blueSize + 1))
		{
			broadCastPacketToTeam(SystemMessage.getSystemMessage(SystemMessageId.TEAM_ADJUSTED_BECAUSE_WRONG_POPULATION_RATIO));
			final int needed = redSize - (blueSize + 1);
			for (int i = 0; i < (needed + 1); i++)
			{
				final L2PcInstance plr = _redPlayers.get(i);
				if (plr == null)
				{
					continue;
				}
				HandysBlockCheckerManager.getInstance().changePlayerToTeam(plr, _arena, 1);
			}
		}
		else if (blueSize > (redSize + 1))
		{
			broadCastPacketToTeam(SystemMessage.getSystemMessage(SystemMessageId.TEAM_ADJUSTED_BECAUSE_WRONG_POPULATION_RATIO));
			final int needed = blueSize - (redSize + 1);
			for (int i = 0; i < (needed + 1); i++)
			{
				final L2PcInstance plr = _bluePlayers.get(i);
				if (plr == null)
				{
					continue;
				}
				HandysBlockCheckerManager.getInstance().changePlayerToTeam(plr, _arena, 0);
			}
		}
	}
}
