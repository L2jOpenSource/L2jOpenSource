package net.sf.l2j.gameserver.handler.chathandlers;

import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

public class ChatClan implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
		4
	};
	
	@Override
	public void handleChat(int type, Player activeChar, String target, String text)
	{
		if (activeChar.getClan() == null)
			return;
		
		activeChar.getClan().broadcastToOnlineMembers(new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text));
	}
	
	@Override
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}