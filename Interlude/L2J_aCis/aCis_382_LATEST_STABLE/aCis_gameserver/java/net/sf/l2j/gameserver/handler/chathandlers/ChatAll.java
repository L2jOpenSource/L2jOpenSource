package net.sf.l2j.gameserver.handler.chathandlers;

import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.player.BlockList;
import net.sf.l2j.gameserver.network.FloodProtectors;
import net.sf.l2j.gameserver.network.FloodProtectors.Action;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

public class ChatAll implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
		0
	};
	
	@Override
	public void handleChat(int type, Player activeChar, String params, String text)
	{
		if (!FloodProtectors.performAction(activeChar.getClient(), Action.GLOBAL_CHAT))
			return;
		
		final CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
		for (Player player : activeChar.getKnownTypeInRadius(Player.class, 1250))
		{
			if (!BlockList.isBlocked(player, activeChar))
				player.sendPacket(cs);
		}
		activeChar.sendPacket(cs);
	}
	
	@Override
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}