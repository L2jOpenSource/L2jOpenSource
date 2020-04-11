package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public final class RequestRecipeShopManageQuit extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
		player.broadcastUserInfo();
		player.standUp();
	}
	
	@Override
	public String getType()
	{
		return "[C] b2 RequestRecipeShopManageQuit";
	}
}
