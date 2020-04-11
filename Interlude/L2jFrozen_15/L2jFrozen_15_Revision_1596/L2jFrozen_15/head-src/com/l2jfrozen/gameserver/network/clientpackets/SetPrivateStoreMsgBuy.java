package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.PrivateStoreMsgBuy;

public final class SetPrivateStoreMsgBuy extends L2GameClientPacket
{
	private String storeMsg;
	
	@Override
	protected void readImpl()
	{
		storeMsg = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null || player.getBuyList() == null)
		{
			return;
		}
		
		if (storeMsg.length() < 30)
		{
			player.getBuyList().setTitle(storeMsg);
			player.sendPacket(new PrivateStoreMsgBuy(player));
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 94 SetPrivateStoreMsgBuy";
	}
	
}