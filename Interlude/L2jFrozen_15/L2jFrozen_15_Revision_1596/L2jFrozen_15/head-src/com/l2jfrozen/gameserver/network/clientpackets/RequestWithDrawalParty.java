package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.PartyMatchRoom;
import com.l2jfrozen.gameserver.model.PartyMatchRoomList;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ExClosePartyRoom;
import com.l2jfrozen.gameserver.network.serverpackets.ExPartyRoomMember;
import com.l2jfrozen.gameserver.network.serverpackets.PartyMatchDetail;

public final class RequestWithDrawalParty extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final L2Party party = player.getParty();
		
		if (party != null)
		{
			if (party.isInDimensionalRift() && !party.getDimensionalRift().getRevivedAtWaitingRoom().contains(player))
			{
				player.sendMessage("You can't exit party when you are in Dimensional Rift.");
			}
			else
			{
				party.removePartyMember(player);
				
				if (player.isInPartyMatchRoom())
				{
					final PartyMatchRoom room = PartyMatchRoomList.getInstance().getPlayerRoom(player);
					if (room != null)
					{
						player.sendPacket(new PartyMatchDetail(player, room));
						player.sendPacket(new ExPartyRoomMember(player, room, 0));
						player.sendPacket(new ExClosePartyRoom());
						
						room.deleteMember(player);
					}
					player.setPartyRoom(0);
					player.broadcastUserInfo();
				}
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 2B RequestWithDrawalParty";
	}
}