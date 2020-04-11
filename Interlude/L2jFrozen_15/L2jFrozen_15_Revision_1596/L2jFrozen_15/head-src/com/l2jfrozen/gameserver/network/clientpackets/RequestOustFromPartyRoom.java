package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.managers.TownManager;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.PartyMatchRoom;
import com.l2jfrozen.gameserver.model.PartyMatchRoomList;
import com.l2jfrozen.gameserver.model.PartyMatchWaitingList;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ExClosePartyRoom;
import com.l2jfrozen.gameserver.network.serverpackets.PartyMatchList;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * format (ch) d
 * @author -Wooden-
 */
public final class RequestOustFromPartyRoom extends L2GameClientPacket
{
	private int charid;
	
	@Override
	protected void readImpl()
	{
		charid = readD();
	}
	
	@Override
	protected void runImpl()
	{
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final L2PcInstance member = L2World.getInstance().getPlayer(charid);
		if (member == null)
		{
			return;
		}
		
		final PartyMatchRoom room = PartyMatchRoomList.getInstance().getPlayerRoom(member);
		if (room == null)
		{
			return;
		}
		
		if (room.getOwner() != activeChar)
		{
			return;
		}
		
		if (activeChar.isInParty() && member.isInParty() && activeChar.getParty().getPartyLeaderOID() == member.getParty().getPartyLeaderOID())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISMISS_PARTY_MEMBER));
		}
		else
		{
			room.deleteMember(member);
			member.setPartyRoom(0);
			
			// Close the PartyRoom window
			member.sendPacket(new ExClosePartyRoom());
			
			// Add player back on waiting list
			PartyMatchWaitingList.getInstance().addPlayer(member);
			
			// Send Room list
			final int loc = TownManager.getClosestLocation(member);
			member.sendPacket(new PartyMatchList(member, 0, loc, member.getLevel()));
			
			// Clean player's LFP title
			member.broadcastUserInfo();
			
			member.sendPacket(new SystemMessage(SystemMessageId.OUSTED_FROM_PARTY_ROOM));
		}
		
	}
	
	@Override
	public String getType()
	{
		
		return "[C] D0:01 RequestOustFromPartyRoom";
		
	}
}