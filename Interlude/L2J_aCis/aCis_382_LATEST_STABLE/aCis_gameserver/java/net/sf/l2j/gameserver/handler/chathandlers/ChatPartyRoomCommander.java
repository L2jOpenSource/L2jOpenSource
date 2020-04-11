package net.sf.l2j.gameserver.handler.chathandlers;

import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.group.CommandChannel;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

public class ChatPartyRoomCommander implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
		15
	};
	
	@Override
	public void handleChat(int type, Player player, String target, String text)
	{
		final Party party = player.getParty();
		if (party == null)
			return;
		
		final CommandChannel channel = party.getCommandChannel();
		if (channel == null || !channel.isLeader(player))
			return;
		
		channel.broadcastCreatureSay(new CreatureSay(player.getObjectId(), type, player.getName(), text), player);
	}
	
	@Override
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}