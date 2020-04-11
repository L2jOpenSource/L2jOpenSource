package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.PartyMatchRoom;
import com.l2jfrozen.gameserver.model.PartyMatchRoomList;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * Format: (ch) dd
 * @author -Wooden-
 */
public class RequestDismissPartyRoom extends L2GameClientPacket
{
	private int roomId;
	@SuppressWarnings("unused")
	private int data2;
	
	@Override
	protected void readImpl()
	{
		roomId = readD();
		data2 = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(roomId);
		if (room == null)
		{
			return;
		}
		
		PartyMatchRoomList.getInstance().deleteRoom(roomId);
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:02 RequestDismissPartyRoom";
	}
}