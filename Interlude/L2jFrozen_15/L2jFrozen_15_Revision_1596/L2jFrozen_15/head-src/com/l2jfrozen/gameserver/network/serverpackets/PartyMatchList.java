package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.model.PartyMatchRoom;
import com.l2jfrozen.gameserver.model.PartyMatchRoomList;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public class PartyMatchList extends L2GameServerPacket
{
	private final L2PcInstance cha;
	private final int loc;
	private final int lim;
	private final List<PartyMatchRoom> rooms;
	
	public PartyMatchList(final L2PcInstance player, final int auto, final int location, final int limit)
	{
		cha = player;
		loc = location;
		lim = limit;
		rooms = new ArrayList<>();
	}
	
	@Override
	protected final void writeImpl()
	{
		if (getClient().getActiveChar() == null)
		{
			return;
		}
		
		for (final PartyMatchRoom room : PartyMatchRoomList.getInstance().getRooms())
		{
			if (room.getMembers() < 1 || room.getOwner() == null || !room.getOwner().isOnline() || room.getOwner().getPartyRoom() != room.getId())
			{
				PartyMatchRoomList.getInstance().deleteRoom(room.getId());
				continue;
			}
			
			if (loc > 0 && loc != room.getLocation())
			{
				continue;
			}
			
			if (lim == 0 && ((cha.getLevel() < room.getMinLvl()) || (cha.getLevel() > room.getMaxLvl())))
			{
				continue;
			}
			
			rooms.add(room);
		}
		
		int count = 0;
		final int size = rooms.size();
		
		writeC(0x96);
		if (size > 0)
		{
			writeD(1);
		}
		else
		{
			writeD(0);
		}
		
		writeD(rooms.size());
		while (size > count)
		{
			writeD(rooms.get(count).getId());
			writeS(rooms.get(count).getTitle());
			writeD(rooms.get(count).getLocation());
			writeD(rooms.get(count).getMinLvl());
			writeD(rooms.get(count).getMaxLvl());
			writeD(rooms.get(count).getMembers());
			writeD(rooms.get(count).getMaxMembers());
			writeS(rooms.get(count).getOwner().getName());
			count++;
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 96 PartyMatchList";
	}
}