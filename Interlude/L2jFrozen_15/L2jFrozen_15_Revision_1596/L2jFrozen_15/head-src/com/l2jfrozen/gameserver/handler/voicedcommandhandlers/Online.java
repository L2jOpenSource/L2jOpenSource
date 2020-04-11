package com.l2jfrozen.gameserver.handler.voicedcommandhandlers;

import com.l2jfrozen.gameserver.handler.IVoicedCommandHandler;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public class Online implements IVoicedCommandHandler
{
	private static String[] voicedCommands =
	{
		"online"
	};
	
	@Override
	public boolean useVoicedCommand(final String command, final L2PcInstance activeChar, final String target)
	{
		if (command.equalsIgnoreCase("online"))
		{
			activeChar.sendMessage("======<Players Online!>======");
			activeChar.sendMessage("There are " + L2World.getInstance().getAllPlayers().size() + " players online!.");
			activeChar.sendMessage("=======================");
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return voicedCommands;
	}
}
