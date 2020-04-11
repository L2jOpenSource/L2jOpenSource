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

import java.util.List;
import java.util.function.Function;

import javolution.util.FastList;
import l2r.Config;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExCloseMPCC;
import l2r.gameserver.network.serverpackets.ExMPCCPartyInfoUpdate;
import l2r.gameserver.network.serverpackets.ExOpenMPCC;
import l2r.gameserver.network.serverpackets.SystemMessage;

/**
 * This class serves as a container for command channels.
 * @author chris_00
 */
public class L2CommandChannel extends AbstractPlayerGroup
{
	private final List<L2Party> _parties;
	private L2PcInstance _commandLeader = null;
	private int _channelLvl;
	
	/**
	 * Create a new command channel and add the leader's party to it.
	 * @param leader the leader of this command channel
	 */
	public L2CommandChannel(L2PcInstance leader)
	{
		_commandLeader = leader;
		L2Party party = leader.getParty();
		_parties = new FastList<L2Party>().shared();
		_parties.add(party);
		_channelLvl = party.getLevel();
		party.setCommandChannel(this);
		party.broadcastMessage(SystemMessageId.COMMAND_CHANNEL_FORMED);
		party.broadcastPacket(ExOpenMPCC.STATIC_PACKET);
	}
	
	/**
	 * Add a party to this command channel.
	 * @param party the party to add
	 */
	public void addParty(L2Party party)
	{
		if (party == null)
		{
			return;
		}
		// Update the CCinfo for existing players
		broadcastPacket(new ExMPCCPartyInfoUpdate(party, 1));
		
		_parties.add(party);
		if (party.getLevel() > _channelLvl)
		{
			_channelLvl = party.getLevel();
		}
		party.setCommandChannel(this);
		party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.JOINED_COMMAND_CHANNEL));
		party.broadcastPacket(ExOpenMPCC.STATIC_PACKET);
	}
	
	/**
	 * Remove a party from this command channel.
	 * @param party the party to remove
	 */
	public void removeParty(L2Party party)
	{
		if (party == null)
		{
			return;
		}
		
		_parties.remove(party);
		_channelLvl = 0;
		for (L2Party pty : _parties)
		{
			if (pty.getLevel() > _channelLvl)
			{
				_channelLvl = pty.getLevel();
			}
		}
		party.setCommandChannel(null);
		party.broadcastPacket(new ExCloseMPCC());
		if (_parties.size() < 2)
		{
			broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_DISBANDED));
			disbandChannel();
		}
		else
		{
			// Update the CCinfo for existing players
			broadcastPacket(new ExMPCCPartyInfoUpdate(party, 0));
		}
	}
	
	/**
	 * Disband this command channel.
	 */
	public void disbandChannel()
	{
		if (_parties != null)
		{
			for (L2Party party : _parties)
			{
				if (party != null)
				{
					removeParty(party);
				}
			}
			_parties.clear();
		}
	}
	
	/**
	 * @return the total count of all members of this command channel
	 */
	@Override
	public int getMemberCount()
	{
		int count = 0;
		for (L2Party party : _parties)
		{
			if (party != null)
			{
				count += party.getMemberCount();
			}
		}
		return count;
	}
	
	/**
	 * @return a list of all parties in this command channel
	 */
	public List<L2Party> getPartys()
	{
		return _parties;
	}
	
	/**
	 * @return a list of all members in this command channel
	 */
	@Override
	public List<L2PcInstance> getMembers()
	{
		List<L2PcInstance> members = new FastList<L2PcInstance>().shared();
		for (L2Party party : getPartys())
		{
			members.addAll(party.getMembers());
		}
		return members;
	}
	
	/**
	 * @return the level of this command channel (equals the level of the highest-leveled character in this command channel)
	 */
	@Override
	public int getLevel()
	{
		return _channelLvl;
	}
	
	@Override
	public void setLeader(L2PcInstance leader)
	{
		_commandLeader = leader;
		if (leader.getLevel() > _channelLvl)
		{
			_channelLvl = leader.getLevel();
		}
	}
	
	/**
	 * @param obj
	 * @return true if proper condition for RaidWar
	 */
	public boolean meetRaidWarCondition(L2Object obj)
	{
		if (!((obj instanceof L2Character) && ((L2Character) obj).isRaid()))
		{
			return false;
		}
		return (getMemberCount() >= Config.LOOT_RAIDS_PRIVILEGE_CC_SIZE);
	}
	
	/**
	 * @return the leader of this command channel
	 */
	@Override
	public L2PcInstance getLeader()
	{
		return _commandLeader;
	}
	
	/**
	 * Check if a given player is in this command channel.
	 * @param player the player to check
	 * @return {@code true} if he does, {@code false} otherwise
	 */
	@Override
	public boolean containsPlayer(L2PcInstance player)
	{
		if ((_parties != null) && !_parties.isEmpty())
		{
			for (L2Party party : _parties)
			{
				if (party.containsPlayer(player))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Iterates over all command channel members without the need to allocate a new list
	 * @see l2r.gameserver.model.AbstractPlayerGroup#forEachMember(Function)
	 */
	@Override
	public boolean forEachMember(Function<L2PcInstance, Boolean> function)
	{
		if ((_parties != null) && !_parties.isEmpty())
		{
			for (L2Party party : _parties)
			{
				if (!party.forEachMember(function))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Check whether the leader of this command channel is the same as the leader of the specified command channel<br>
	 * (which essentially means they're the same group).
	 * @param cc the other command channel to check against
	 * @return {@code true} if this command channel equals the specified command channel, {@code false} otherwise
	 */
	public boolean equals(L2CommandChannel cc)
	{
		return (getLeaderObjectId() == cc.getLeaderObjectId());
	}
}
