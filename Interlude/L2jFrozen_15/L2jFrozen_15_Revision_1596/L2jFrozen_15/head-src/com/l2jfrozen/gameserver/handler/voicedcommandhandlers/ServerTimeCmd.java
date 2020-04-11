package com.l2jfrozen.gameserver.handler.voicedcommandhandlers;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.l2jfrozen.gameserver.handler.IVoicedCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public class ServerTimeCmd implements IVoicedCommandHandler
{
	private final SimpleDateFormat fmt = new SimpleDateFormat("d MMM H:mm"); // 4 Jul 21:15
	
	private static final String[] VOICED_COMMANDS =
	{
		"servertime",
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		activeChar.sendMessage("Server time: " + fmt.format(new Date(System.currentTimeMillis())));
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
