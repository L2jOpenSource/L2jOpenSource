package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * ddddd
 * @version $Revision: 1.1.2.3.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class RecipeShopItemInfo extends L2GameServerPacket
{
	private final int shopId;
	private final int recipeId;
	
	public RecipeShopItemInfo(final int shopId, final int recipeId)
	{
		this.shopId = shopId;
		this.recipeId = recipeId;
	}
	
	@Override
	protected final void writeImpl()
	{
		if (!(L2World.getInstance().findObject(shopId) instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance manufacturer = (L2PcInstance) L2World.getInstance().findObject(shopId);
		writeC(0xda);
		writeD(shopId);
		writeD(recipeId);
		writeD(manufacturer != null ? (int) manufacturer.getCurrentMp() : 0);
		writeD(manufacturer != null ? manufacturer.getMaxMp() : 0);
		writeD(0xffffffff);
	}
	
	@Override
	public String getType()
	{
		return "[S] da RecipeShopItemInfo";
	}
}
