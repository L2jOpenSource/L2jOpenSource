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

import l2r.gameserver.RecipeController;
import l2r.gameserver.enums.PrivateStoreType;
import l2r.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Administrator
 */
public final class RequestRecipeItemMakeSelf extends L2GameClientPacket
{
	private static final String _C__B8_REQUESTRECIPEITEMMAKESELF = "[C] B8 RequestRecipeItemMakeSelf";
	
	private int _id;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getManufacture().tryPerformAction("RecipeMakeSelf"))
		{
			return;
		}
		
		if (activeChar.getPrivateStoreType() != PrivateStoreType.NONE)
		{
			activeChar.sendMessage("You cannot create items while trading.");
			return;
		}
		
		if (activeChar.isInCraftMode())
		{
			activeChar.sendMessage("You are currently in Craft Mode.");
			return;
		}
		
		RecipeController.getInstance().requestMakeItem(activeChar, _id);
	}
	
	@Override
	public String getType()
	{
		return _C__B8_REQUESTRECIPEITEMMAKESELF;
	}
}
