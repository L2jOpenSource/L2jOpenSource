package net.sf.l2j.gameserver.handler.chathandlers;

import net.sf.l2j.gameserver.enums.SayType;
import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.FloodProtectors;
import net.sf.l2j.gameserver.network.FloodProtectors.Action;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

public class ChatHeroVoice implements IChatHandler
{
	private static final SayType[] COMMAND_IDS =
	{
		SayType.HERO_VOICE
	};
	
	@Override
	public void handleChat(SayType type, Player activeChar, String target, String text)
	{
		if (!activeChar.isHero())
			return;
		
		if (!FloodProtectors.performAction(activeChar.getClient(), Action.HERO_VOICE))
			return;
		
		World.toAllOnlinePlayers(new CreatureSay(activeChar, type, text));
	}
	
	@Override
	public SayType[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}