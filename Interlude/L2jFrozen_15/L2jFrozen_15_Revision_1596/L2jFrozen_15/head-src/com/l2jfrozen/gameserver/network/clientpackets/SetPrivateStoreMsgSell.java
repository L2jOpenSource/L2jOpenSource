package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.PrivateStoreMsgSell;

public class SetPrivateStoreMsgSell extends L2GameClientPacket
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
		if (player == null || player.getSellList() == null)
		{
			return;
		}
		
		if (storeMsg.length() < 30)
		{
			player.getSellList().setTitle(storeMsg);
			sendPacket(new PrivateStoreMsgSell(player));
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 77 SetPrivateStoreMsgSell";
	}
	
}