package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.PartyMatchRoom;
import com.l2jfrozen.gameserver.model.PartyMatchRoomList;
import com.l2jfrozen.gameserver.model.PartyMatchWaitingList;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.ExPartyRoomMember;
import com.l2jfrozen.gameserver.network.serverpackets.PartyMatchDetail;
import com.l2jfrozen.gameserver.network.serverpackets.PartyMatchList;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class RequestPartyMatchConfig extends L2GameClientPacket
{
	
	private int auto, loc, lvl;
	
	@Override
	protected void readImpl()
	{
		auto = readD();
		loc = readD();
		lvl = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (!activeChar.isInPartyMatchRoom() && activeChar.getParty() != null && activeChar.getParty().getLeader() != activeChar)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_VIEW_PARTY_ROOMS));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.isInPartyMatchRoom())
		{
			// If Player is in Room show him room, not list
			final PartyMatchRoomList list = PartyMatchRoomList.getInstance();
			if (list == null)
			{
				return;
			}
			
			final PartyMatchRoom room = list.getPlayerRoom(activeChar);
			if (room == null)
			{
				return;
			}
			
			activeChar.sendPacket(new PartyMatchDetail(activeChar, room));
			activeChar.sendPacket(new ExPartyRoomMember(activeChar, room, 2));
			
			activeChar.setPartyRoom(room.getId());
			activeChar.broadcastUserInfo();
		}
		else
		{
			// Add to waiting list
			PartyMatchWaitingList.getInstance().addPlayer(activeChar);
			
			// Send Room list
			activeChar.sendPacket(new PartyMatchList(activeChar, auto, loc, lvl));
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 6F RequestPartyMatchConfig";
	}
}