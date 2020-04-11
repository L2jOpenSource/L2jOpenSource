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
import java.util.function.Function;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.CreatureSay;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.util.Rnd;

/**
 * @author Battlecruiser
 */
public abstract class AbstractPlayerGroup
{
	/**
	 * @return a list of all members of this group
	 */
	public abstract List<L2PcInstance> getMembers();
	
	/**
	 * @return a list of object IDs of the members of this group
	 */
	public List<Integer> getMembersObjectId()
	{
		final List<Integer> ids = new ArrayList<>();
		forEachMember(m ->
		{
			ids.add(m.getObjectId());
			return true;
		});
		return ids;
	}
	
	/**
	 * @return the leader of this group
	 */
	public abstract L2PcInstance getLeader();
	
	/**
	 * Change the leader of this group to the specified player.
	 * @param leader the player to set as the new leader of this group
	 */
	public abstract void setLeader(L2PcInstance leader);
	
	/**
	 * @return the leader's object ID
	 */
	public int getLeaderObjectId()
	{
		return getLeader().getObjectId();
	}
	
	/**
	 * Check if a given player is the leader of this group.
	 * @param player the player to check
	 * @return {@code true} if the specified player is the leader of this group, {@code false} otherwise
	 */
	public boolean isLeader(L2PcInstance player)
	{
		return (getLeaderObjectId() == player.getObjectId());
	}
	
	/**
	 * @return the count of all players in this group
	 */
	public int getMemberCount()
	{
		return getMembers().size();
	}
	
	/**
	 * @return the level of this group
	 */
	public abstract int getLevel();
	
	/**
	 * Broadcast a packet to every member of this group.
	 * @param packet the packet to broadcast
	 */
	public void broadcastPacket(final L2GameServerPacket packet)
	{
		forEachMember(m ->
		{
			if (m != null)
			{
				m.sendPacket(packet);
			}
			return true;
		});
	}
	
	/**
	 * Broadcast a system message to this group.
	 * @param message the system message to broadcast
	 */
	public void broadcastMessage(SystemMessageId message)
	{
		broadcastPacket(SystemMessage.getSystemMessage(message));
	}
	
	/**
	 * Broadcast a text message to this group.
	 * @param text to broadcast
	 */
	public void broadcastString(String text)
	{
		broadcastPacket(SystemMessage.sendString(text));
	}
	
	public void broadcastCreatureSay(final CreatureSay msg, final L2PcInstance broadcaster)
	{
		forEachMember(m ->
		{
			if ((m != null) && !BlockList.isBlocked(m, broadcaster))
			{
				m.sendPacket(msg);
			}
			return true;
		});
	}
	
	/**
	 * Check if this group contains a given player.
	 * @param player the player to check
	 * @return {@code true} if this group contains the specified player, {@code false} otherwise
	 */
	public boolean containsPlayer(L2PcInstance player)
	{
		return getMembers().contains(player);
	}
	
	/**
	 * @return a random member of this group
	 */
	public L2PcInstance getRandomPlayer()
	{
		return getMembers().get(Rnd.get(getMemberCount()));
	}
	
	/**
	 * Iterates over the group and executes procedure on each member
	 * @param procedure the prodecure to be executed on each member.<br>
	 *            If executing the procedure on a member returns {@code true}, the loop continues to the next member, otherwise it breaks the loop
	 * @return {@code true} if the procedure executed correctly, {@code false} if the loop was broken prematurely
	 */
	public boolean forEachMember(Function<L2PcInstance, Boolean> procedure)
	{
		for (L2PcInstance player : getMembers())
		{
			if (!procedure.apply(player))
			{
				return false;
			}
		}
		return true;
	}
}
