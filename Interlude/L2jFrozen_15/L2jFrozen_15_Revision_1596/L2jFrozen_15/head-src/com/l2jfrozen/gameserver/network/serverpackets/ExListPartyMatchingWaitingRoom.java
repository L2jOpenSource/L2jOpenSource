package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.model.PartyMatchRoom;
import com.l2jfrozen.gameserver.model.PartyMatchRoomList;
import com.l2jfrozen.gameserver.model.PartyMatchWaitingList;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Gnacik
 */
public class ExListPartyMatchingWaitingRoom extends L2GameServerPacket
{
	private final L2PcInstance activeChar;
	@SuppressWarnings("unused")
	private final int page;
	private final int minlvl;
	private final int maxlvl;
	private final int mode;
	private final List<L2PcInstance> members;
	
	public ExListPartyMatchingWaitingRoom(final L2PcInstance player, final int page, final int minlvl, final int maxlvl, final int mode)
	{
		activeChar = player;
		this.page = page;
		this.minlvl = minlvl;
		this.maxlvl = maxlvl;
		this.mode = mode;
		members = new ArrayList<>();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x35);
		
		// If the mode is 0 and the activeChar isn't the PartyRoom leader, return an empty list.
		if (mode == 0)
		{
			// Retrieve the activeChar PartyMatchRoom
			final PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(activeChar.getPartyRoom());
			if (room != null && room.getOwner() != null && !room.getOwner().equals(activeChar))
			{
				writeD(0);
				writeD(0);
				return;
			}
		}
		
		for (final L2PcInstance cha : PartyMatchWaitingList.getInstance().getPlayers())
		{
			// Don't add yourself in the list
			if (cha == null || cha == activeChar)
			{
				continue;
			}
			
			if (!cha.isPartyWaiting())
			{
				PartyMatchWaitingList.getInstance().removePlayer(cha);
				continue;
			}
			
			if ((cha.getLevel() < minlvl) || (cha.getLevel() > maxlvl))
			{
				continue;
			}
			
			members.add(cha);
		}
		
		int count = 0;
		final int size = members.size();
		
		writeD(1);
		writeD(size);
		while (size > count)
		{
			writeS(members.get(count).getName());
			writeD(members.get(count).getActiveClass());
			writeD(members.get(count).getLevel());
			count++;
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:35 ExListPartyMatchingWaitingRoom";
	}
}