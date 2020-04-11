package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.PartyMatchRoom;
import com.l2jfrozen.gameserver.model.PartyMatchRoomList;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ExClosePartyRoom;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * Format (ch) dd
 * @author -Wooden-
 */
public final class RequestWithdrawPartyRoom extends L2GameClientPacket
{
	
	private int roomid;
	@SuppressWarnings("unused")
	private int unk1;
	
	@Override
	protected void readImpl()
	{
		roomid = readD();
		unk1 = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		final PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(roomid);
		if (room == null)
		{
			return;
		}
		
		if ((activeChar.isInParty() && room.getOwner().isInParty()) && (activeChar.getParty().getPartyLeaderOID() == room.getOwner().getParty().getPartyLeaderOID()))
		{
			// If user is in party with Room Owner is not removed from Room
		}
		else
		{
			room.deleteMember(activeChar);
			activeChar.setPartyRoom(0);
			activeChar.broadcastUserInfo();
			
			activeChar.sendPacket(new ExClosePartyRoom());
			activeChar.sendPacket(new SystemMessage(SystemMessageId.PARTY_ROOM_EXITED));
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:02 RequestWithdrawPartyRoom";
	}
}