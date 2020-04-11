package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2ManufactureList;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.RecipeShopManageList;

public final class RequestRecipeShopManageList extends L2GameClientPacket
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
		
		// Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
		if (player.isAlikeDead())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.getPrivateStoreType() != 0)
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			player.broadcastUserInfo();
			if (player.isSitting())
			{
				player.standUp();
			}
		}
		
		if (player.getCreateList() == null)
		{
			player.setCreateList(new L2ManufactureList());
		}
		
		player.sendPacket(new RecipeShopManageList(player, true));
		
		/*
		 * int privatetype=player.getPrivateStoreType(); if (privatetype == 0) { if (player.getWaitType() !=1) { player.setWaitType(1); player.sendPacket(new ChangeWaitType (player,1)); player.broadcastPacket(new ChangeWaitType (player,1)); } if (player.getTradeList() == null) { player.setTradeList(new
		 * L2TradeList(0)); } if (player.getSellList() == null) { player.setSellList(new ArrayList()); } player.getTradeList().updateSellList(player,player.getSellList()); player.setPrivateStoreType(2); player.sendPacket(new PrivateSellListSell(client.getActiveChar())); player.sendPacket(new
		 * UserInfo(player)); player.broadcastPacket(new UserInfo(player)); } if (privatetype == 1) { player.setPrivateStoreType(2); player.sendPacket(new PrivateSellListSell(client.getActiveChar())); player.sendPacket(new ChangeWaitType (player,1)); player.broadcastPacket(new ChangeWaitType (player,1)); }
		 */
		
	}
	
	@Override
	public String getType()
	{
		return "[C] b0 RequestRecipeShopManageList";
	}
}
