/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.network.clientpackets;

import l2r.gameserver.enums.PrivateStoreType;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.RecipeShopItemInfo;

/**
 * This class ... cdd
 * @version $Revision: 1.1.2.1.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestRecipeShopMakeInfo extends L2GameClientPacket
{
	private static final String _C__B5_RequestRecipeShopMakeInfo = "[C] B5 RequestRecipeShopMakeInfo";
	
	private int _playerObjectId;
	private int _recipeId;
	
	@Override
	protected void readImpl()
	{
		_playerObjectId = readD();
		_recipeId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final L2PcInstance shop = L2World.getInstance().getPlayer(_playerObjectId);
		if ((shop == null) || (shop.getPrivateStoreType() != PrivateStoreType.MANUFACTURE))
		{
			return;
		}
		
		player.sendPacket(new RecipeShopItemInfo(shop, _recipeId));
		
	}
	
	@Override
	public String getType()
	{
		return _C__B5_RequestRecipeShopMakeInfo;
	}
}
