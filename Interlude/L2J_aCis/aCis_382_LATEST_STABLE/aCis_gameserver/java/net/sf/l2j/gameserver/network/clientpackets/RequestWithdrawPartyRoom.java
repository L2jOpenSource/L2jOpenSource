package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoom;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoomList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExClosePartyRoom;

public final class RequestWithdrawPartyRoom extends L2GameClientPacket
{
	private int _roomid;
	@SuppressWarnings("unused")
	private int _unk1;
	
	@Override
	protected void readImpl()
	{
		_roomid = readD();
		_unk1 = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getPlayer();
		if (activeChar == null)
			return;
		
		final PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(_roomid);
		if (room == null)
			return;
		
		if (activeChar.isInParty() && room.getOwner().isInParty() && activeChar.getParty().getLeaderObjectId() == room.getOwner().getParty().getLeaderObjectId())
		{
			// If user is in party with Room Owner is not removed from Room
		}
		else
		{
			room.deleteMember(activeChar);
			activeChar.setPartyRoom(0);
			activeChar.broadcastUserInfo();
			
			activeChar.sendPacket(ExClosePartyRoom.STATIC_PACKET);
			activeChar.sendPacket(SystemMessageId.PARTY_ROOM_EXITED);
		}
	}
}