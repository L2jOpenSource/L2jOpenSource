package net.sf.l2j.gameserver.handler;

import net.sf.l2j.gameserver.enums.SayType;
import net.sf.l2j.gameserver.model.actor.Player;

/**
 * Interface for chat handlers
 * @author durgus
 */
public interface IChatHandler
{
	/**
	 * Handles a specific type of chat messages
	 * @param type
	 * @param activeChar
	 * @param target
	 * @param text
	 */
	public void handleChat(SayType type, Player activeChar, String target, String text);
	
	/**
	 * Returns a list of all chat types registered to this handler
	 * @return
	 */
	public SayType[] getChatTypeList();
}
