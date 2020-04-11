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

import java.util.Map;

import javolution.util.FastMap;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExClosePartyRoom;

/**
 * @author Gnacik
 */
public class PartyMatchRoomList
{
	private int _maxid = 1;
	private final Map<Integer, PartyMatchRoom> _rooms;
	
	protected PartyMatchRoomList()
	{
		_rooms = new FastMap<>();
	}
	
	public synchronized void addPartyMatchRoom(int id, PartyMatchRoom room)
	{
		_rooms.put(id, room);
		_maxid++;
	}
	
	public void deleteRoom(int id)
	{
		for (L2PcInstance _member : getRoom(id).getPartyMembers())
		{
			if (_member == null)
			{
				continue;
			}
			
			_member.sendPacket(new ExClosePartyRoom());
			_member.sendPacket(SystemMessageId.PARTY_ROOM_DISBANDED);
			
			_member.setPartyRoom(0);
			// _member.setPartyMatching(0);
			_member.broadcastUserInfo();
		}
		_rooms.remove(id);
	}
	
	public PartyMatchRoom getRoom(int id)
	{
		return _rooms.get(id);
	}
	
	public PartyMatchRoom[] getRooms()
	{
		return _rooms.values().toArray(new PartyMatchRoom[_rooms.size()]);
	}
	
	public int getPartyMatchRoomCount()
	{
		return _rooms.size();
	}
	
	public int getMaxId()
	{
		return _maxid;
	}
	
	public PartyMatchRoom getPlayerRoom(L2PcInstance player)
	{
		for (PartyMatchRoom _room : _rooms.values())
		{
			for (L2PcInstance member : _room.getPartyMembers())
			{
				if (member.equals(player))
				{
					return _room;
				}
			}
		}
		return null;
	}
	
	public int getPlayerRoomId(L2PcInstance player)
	{
		for (PartyMatchRoom _room : _rooms.values())
		{
			for (L2PcInstance member : _room.getPartyMembers())
			{
				if (member.equals(player))
				{
					return _room.getId();
				}
			}
		}
		return -1;
	}
	
	public static PartyMatchRoomList getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PartyMatchRoomList _instance = new PartyMatchRoomList();
	}
}