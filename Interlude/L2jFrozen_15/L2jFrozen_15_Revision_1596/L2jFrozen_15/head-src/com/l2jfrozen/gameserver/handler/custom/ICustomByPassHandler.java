package com.l2jfrozen.gameserver.handler.custom;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Azagthtot
 */
public interface ICustomByPassHandler
{
	/**
	 * @return as String -
	 */
	public String[] getByPassCommands();
	
	public void handleCommand(String command, L2PcInstance player, String parameters);
}
