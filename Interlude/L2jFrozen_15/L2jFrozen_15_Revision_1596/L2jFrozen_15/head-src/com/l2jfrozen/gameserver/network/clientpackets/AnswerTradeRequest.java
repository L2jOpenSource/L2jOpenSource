package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.SendTradeDone;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class AnswerTradeRequest extends L2GameClientPacket
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
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Unsufficient privileges.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2PcInstance partner = player.getActiveRequester();
		
		if (partner == null || L2World.getInstance().findObject(partner.getObjectId()) == null)
		{
			// Trade partner not found, cancel trade
			player.sendPacket(new SendTradeDone(0));
			player.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME));
			player.setActiveRequester(null);
			return;
		}
		
		if (response == 1 && !partner.isRequestExpired())
		{
			player.startTrade(partner);
		}
		else
		{
			partner.sendPacket(new SystemMessage(SystemMessageId.S1_DENIED_TRADE_REQUEST).addString(player.getName()));
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		
		// Clears requesting status
		player.setActiveRequester(null);
		partner.onTransactionResponse();
	}
	
	@Override
	public String getType()
	{
		return "[C] 40 AnswerTradeRequest";
	}
}
