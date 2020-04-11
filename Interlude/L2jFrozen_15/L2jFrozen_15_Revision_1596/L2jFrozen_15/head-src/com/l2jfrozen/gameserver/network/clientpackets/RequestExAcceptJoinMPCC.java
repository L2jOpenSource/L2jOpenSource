package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2CommandChannel;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * @author -Wooden-
 */
public final class RequestExAcceptJoinMPCC extends L2GameClientPacket
{
	private int response;
	
	@Override
	protected void readImpl()
	{
		response = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final L2PcInstance requestor = player.getActiveRequester();
		if (requestor == null)
		{
			return;
		}
		
		if (response == 1)
		{
			boolean newCc = false;
			if (!requestor.getParty().isInCommandChannel())
			{
				new L2CommandChannel(requestor); // Create new CC
				newCc = true;
			}
			
			requestor.getParty().getCommandChannel().addParty(player.getParty());
			if (!newCc)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.JOINED_COMMAND_CHANNEL));
			}
		}
		else
		{
			requestor.sendPacket(new SystemMessage(SystemMessageId.S1_DECLINED_CHANNEL_INVITATION).addString(player.getName()));
		}
		
		player.setActiveRequester(null);
		requestor.onTransactionResponse();
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:0E RequestExAcceptJoinMPCC";
	}
	
}
