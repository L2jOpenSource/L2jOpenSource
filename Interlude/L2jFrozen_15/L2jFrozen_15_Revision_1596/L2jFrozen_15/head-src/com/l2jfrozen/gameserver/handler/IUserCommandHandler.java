package com.l2jfrozen.gameserver.handler;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * @version $Revision: 1.1.2.1.2.2 $ $Date: 2005/03/27 15:30:10 $
 */
public interface IUserCommandHandler
{
	/**
	 * this is the worker method that is called when someone uses an admin command.
	 * @param  id
	 * @param  activeChar
	 * @return            command success
	 */
	public boolean useUserCommand(int id, L2PcInstance activeChar);
	
	/**
	 * this method is called at initialization to register all the item ids automatically
	 * @return all known itemIds
	 */
	public int[] getUserCommandList();
}
