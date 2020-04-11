package com.l2jfrozen.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Gnacik
 */
public class PartyMatchWaitingList
{
	private final List<L2PcInstance> members;
	
	private PartyMatchWaitingList()
	{
		members = new ArrayList<>();
	}
	
	public void addPlayer(final L2PcInstance player)
	{
		if (!members.contains(player))
		{
			members.add(player);
		}
	}
	
	public void removePlayer(final L2PcInstance player)
	{
		if (members.contains(player))
		{
			members.remove(player);
		}
	}
	
	public List<L2PcInstance> getPlayers()
	{
		return members;
	}
	
	public static PartyMatchWaitingList getInstance()
	{
		return SingletonHolder.instance;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final PartyMatchWaitingList instance = new PartyMatchWaitingList();
	}
}