package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.RecipeShopItemInfo;

public final class RequestRecipeShopMakeInfo extends L2GameClientPacket
{
	private int playerObjectId;
	private int recipeId;
	
	@Override
	protected void readImpl()
	{
		playerObjectId = readD();
		recipeId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		player.sendPacket(new RecipeShopItemInfo(playerObjectId, recipeId));
	}
	
	@Override
	public String getType()
	{
		return "[C] b5 RequestRecipeShopMakeInfo";
	}
	
}
