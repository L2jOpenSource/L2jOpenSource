package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.PartyMatchRoom;
import com.l2jfrozen.gameserver.model.PartyMatchRoomList;
import com.l2jfrozen.gameserver.model.PartyMatchWaitingList;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ExManagePartyRoomMember;
import com.l2jfrozen.gameserver.network.serverpackets.ExPartyRoomMember;
import com.l2jfrozen.gameserver.network.serverpackets.PartyMatchDetail;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Gnacik
 */

public final class RequestPartyMatchDetail extends L2GameClientPacket
{
	
	private int roomid;
	@SuppressWarnings("unused")
	private int unk1;
	@SuppressWarnings("unused")
	private int unk2;
	@SuppressWarnings("unused")
	private int unk3;
	
	@Override
	protected void readImpl()
	{
		roomid = readD();
		/*
		 * IF player click on Room all unk are 0 IF player click AutoJoin values are -1 1 1
		 */
		unk1 = readD();
		unk2 = readD();
		unk3 = readD();
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
		
		if ((activeChar.getLevel() >= room.getMinLvl()) && (activeChar.getLevel() <= room.getMaxLvl()))
		{
			// Remove from waiting list
			PartyMatchWaitingList.getInstance().removePlayer(activeChar);
			
			activeChar.setPartyRoom(roomid);
			
			activeChar.sendPacket(new PartyMatchDetail(activeChar, room));
			activeChar.sendPacket(new ExPartyRoomMember(activeChar, room, 0));
			
			for (final L2PcInstance member : room.getPartyMembers())
			{
				if (member == null)
				{
					continue;
				}
				
				member.sendPacket(new ExManagePartyRoomMember(activeChar, room, 0));
				member.sendPacket(new SystemMessage(SystemMessageId.S1_ENTERED_PARTY_ROOM).addString(activeChar.getName()));
			}
			room.addMember(activeChar);
			
			// Info Broadcast
			activeChar.broadcastUserInfo();
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_ENTER_PARTY_ROOM));
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 71 RequestPartyMatchDetail";
	}
}