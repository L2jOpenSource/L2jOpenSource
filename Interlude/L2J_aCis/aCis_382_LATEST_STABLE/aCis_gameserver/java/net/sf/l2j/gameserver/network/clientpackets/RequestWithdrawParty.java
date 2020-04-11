package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.enums.MessageType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoom;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoomList;
import net.sf.l2j.gameserver.network.serverpackets.ExClosePartyRoom;
import net.sf.l2j.gameserver.network.serverpackets.ExPartyRoomMember;
import net.sf.l2j.gameserver.network.serverpackets.PartyMatchDetail;

public final class RequestWithdrawParty extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		final Party party = player.getParty();
		if (party == null)
			return;
		
		party.removePartyMember(player, MessageType.LEFT);
		
		if (player.isInPartyMatchRoom())
		{
			PartyMatchRoom room = PartyMatchRoomList.getInstance().getPlayerRoom(player);
			if (room != null)
			{
				player.sendPacket(new PartyMatchDetail(room));
				player.sendPacket(new ExPartyRoomMember(room, 0));
				player.sendPacket(ExClosePartyRoom.STATIC_PACKET);
				
				room.deleteMember(player);
			}
			player.setPartyRoom(0);
			player.broadcastUserInfo();
		}
	}
}