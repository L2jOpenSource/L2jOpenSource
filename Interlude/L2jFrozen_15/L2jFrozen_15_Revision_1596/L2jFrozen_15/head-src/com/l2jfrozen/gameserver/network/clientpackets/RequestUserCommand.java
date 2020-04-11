package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.handler.IUserCommandHandler;
import com.l2jfrozen.gameserver.handler.UserCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public class RequestUserCommand extends L2GameClientPacket
{
	static Logger LOGGER = Logger.getLogger(RequestUserCommand.class);
	
	private int command;
	
	@Override
	protected void readImpl()
	{
		command = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final IUserCommandHandler handler = UserCommandHandler.getInstance().getUserCommandHandler(command);
		
		if (handler == null)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("user commandID " + command + " not implemented yet");
			player.sendPacket(sm);
			sm = null;
		}
		else
		{
			handler.useUserCommand(command, getClient().getActiveChar());
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] aa RequestUserCommand";
	}
}
