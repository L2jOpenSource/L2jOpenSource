package com.l2jfrozen.gameserver.handler.usercommandhandlers;

import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.handler.IUserCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public class Time implements IUserCommandHandler
{
	// time
	private static final int[] COMMAND_IDS =
	{
		77
	};
	
	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (COMMAND_IDS[0] != id)
		{
			return false;
		}
		
		int time = GameTimeController.getInstance().getGameTime();
		
		String hour = Integer.toString(time / 60 % 24);
		String minute;
		
		minute = (time % 60 < 10 ? "0" : "") + time % 60;
		
		SystemMessage sm = new SystemMessage(GameTimeController.getInstance().isNowNight() ? SystemMessageId.TIME_S1_S2_IN_THE_NIGHT : SystemMessageId.TIME_S1_S2_IN_THE_DAY);
		sm.addString(hour);
		sm.addString(minute);
		activeChar.sendPacket(sm);
		
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
