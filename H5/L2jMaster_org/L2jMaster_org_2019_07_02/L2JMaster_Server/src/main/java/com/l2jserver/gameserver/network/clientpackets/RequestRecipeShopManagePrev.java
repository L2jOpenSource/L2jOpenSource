/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.network.clientpackets;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.ActionFailed;
import com.l2jserver.gameserver.network.serverpackets.RecipeShopSellList;

/**
 * This class ...
 * @version $Revision: 1.1.2.1.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestRecipeShopManagePrev extends L2GameClientPacket
{
	private static final String _C__C0_RequestRecipeShopPrev = "[C] C0 RequestRecipeShopPrev";
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getActiveChar();
		if ((player == null))
		{
			return;
		}
		else if (player.isAlikeDead() || (player.getTarget() == null) || !player.getTarget().isPlayer())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		player.sendPacket(new RecipeShopSellList(player, player.getTarget().getActingPlayer()));
	}
	
	@Override
	public String getType()
	{
		return _C__C0_RequestRecipeShopPrev;
	}
}
