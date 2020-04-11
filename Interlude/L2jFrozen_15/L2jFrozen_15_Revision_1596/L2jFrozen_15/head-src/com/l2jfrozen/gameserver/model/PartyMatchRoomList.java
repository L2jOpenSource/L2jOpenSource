package com.l2jfrozen.gameserver.model;

import java.util.HashMap;
import java.util.Map;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ExClosePartyRoom;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Gnacik
 */
public class PartyMatchRoomList
{
	private int maxId = 1;
	private final Map<Integer, PartyMatchRoom> rooms;
	
	public PartyMatchRoomList()
	{
		rooms = new HashMap<>();
	}
	
	public synchronized void addPartyMatchRoom(final int id, final PartyMatchRoom room)
	{
		rooms.put(id, room);
		maxId++;
	}
	
	public void deleteRoom(final int id)
	{
		for (final L2PcInstance member : getRoom(id).getPartyMembers())
		{
			if (member == null)
			{
				continue;
			}
			
			member.sendPacket(new ExClosePartyRoom());
			member.sendPacket(new SystemMessage(SystemMessageId.PARTY_ROOM_DISBANDED));
			
			member.setPartyRoom(0);
			member.broadcastUserInfo();
		}
		rooms.remove(id);
	}
	
	public PartyMatchRoom getRoom(final int id)
	{
		return rooms.get(id);
	}
	
	public PartyMatchRoom[] getRooms()
	{
		return rooms.values().toArray(new PartyMatchRoom[rooms.size()]);
	}
	
	public int getPartyMatchRoomCount()
	{
		return rooms.size();
	}
	
	public int getMaxId()
	{
		return maxId;
	}
	
	public PartyMatchRoom getPlayerRoom(final L2PcInstance player)
	{
		for (final PartyMatchRoom room : rooms.values())
		{
			for (final L2PcInstance member : room.getPartyMembers())
			{
				if (member.equals(player))
				{
					return room;
				}
			}
		}
		
		return null;
	}
	
	public int getPlayerRoomId(final L2PcInstance player)
	{
		for (final PartyMatchRoom room : rooms.values())
		{
			for (final L2PcInstance member : room.getPartyMembers())
			{
				if (member.equals(player))
				{
					return room.getId();
				}
			}
		}
		
		return -1;
	}
	
	public static PartyMatchRoomList getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PartyMatchRoomList instance = new PartyMatchRoomList();
	}
}