package net.sf.l2j.gameserver.handler.chathandlers;

import net.sf.l2j.gameserver.enums.SayType;
import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

public class ChatClan implements IChatHandler
{
	private static final SayType[] COMMAND_IDS =
	{
		SayType.CLAN
	};
	
	@Override
	public void handleChat(SayType type, Player activeChar, String target, String text)
	{
		final Clan clan = activeChar.getClan();
		if (clan == null)
			return;
		
		clan.broadcastToOnlineMembers(new CreatureSay(activeChar, type, text));
	}
	
	@Override
	public SayType[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}