package net.sf.l2j.gameserver.handler.chathandlers;

import net.sf.l2j.gameserver.data.manager.PartyMatchRoomManager;
import net.sf.l2j.gameserver.enums.SayType;
import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.group.PartyMatchRoom;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

public class ChatPartyMatchRoom implements IChatHandler
{
	private static final SayType[] COMMAND_IDS =
	{
		SayType.PARTYMATCH_ROOM
	};
	
	@Override
	public void handleChat(SayType type, Player activeChar, String target, String text)
	{
		if (!activeChar.isInPartyMatchRoom())
			return;
		
		final PartyMatchRoom room = PartyMatchRoomManager.getInstance().getRoom(activeChar.getPartyRoom());
		if (room == null)
			return;
		
		room.broadcastPacket(new CreatureSay(activeChar, type, text));
	}
	
	@Override
	public SayType[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}