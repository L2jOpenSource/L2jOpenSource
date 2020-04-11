/*
 * This file is part of the L2J Sunrise project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.playeractions;

import java.util.logging.Logger;

import l2r.gameserver.enums.PrivateStoreType;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.handler.IPlayerActionHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.ActionDataHolder;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.PrivateStoreManageListBuy;
import l2r.gameserver.network.serverpackets.PrivateStoreManageListSell;
import l2r.gameserver.network.serverpackets.RecipeShopManageList;

/**
 * Open/Close private store player action handler.
 * @author Nik
 */
public final class PrivateStore implements IPlayerActionHandler
{
	private static final Logger LOGGER = Logger.getLogger(PrivateStore.class.getName());
	
	@Override
	public void useAction(L2PcInstance activeChar, ActionDataHolder data, boolean ctrlPressed, boolean shiftPressed)
	{
		final PrivateStoreType type = PrivateStoreType.findById(data.getOptionId());
		if (type == null)
		{
			LOGGER.warning("Incorrect private store type: " + data.getOptionId());
			return;
		}
		
		// Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
		if (!activeChar.canOpenPrivateStore())
		{
			if (activeChar.isInsideZone(ZoneIdType.NO_STORE))
			{
				activeChar.sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
			}
			
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		switch (type)
		{
			case SELL:
			case SELL_MANAGE:
			case PACKAGE_SELL:
			{
				if ((activeChar.getPrivateStoreType() == PrivateStoreType.SELL) || (activeChar.getPrivateStoreType() == PrivateStoreType.SELL_MANAGE) || (activeChar.getPrivateStoreType() == PrivateStoreType.PACKAGE_SELL))
				{
					activeChar.setPrivateStoreType(PrivateStoreType.NONE);
				}
				break;
			}
			case BUY:
			case BUY_MANAGE:
			{
				if ((activeChar.getPrivateStoreType() == PrivateStoreType.BUY) || (activeChar.getPrivateStoreType() == PrivateStoreType.BUY_MANAGE))
				{
					activeChar.setPrivateStoreType(PrivateStoreType.NONE);
				}
				break;
			}
			case MANUFACTURE:
			{
				activeChar.setPrivateStoreType(PrivateStoreType.NONE);
				activeChar.broadcastUserInfo();
			}
		}
		
		if (activeChar.getPrivateStoreType() == PrivateStoreType.NONE)
		{
			if (activeChar.isSitting())
			{
				activeChar.standUp();
			}
			
			switch (type)
			{
				case SELL:
				case SELL_MANAGE:
				case PACKAGE_SELL:
				{
					activeChar.setPrivateStoreType(PrivateStoreType.SELL_MANAGE);
					activeChar.sendPacket(new PrivateStoreManageListSell(activeChar, type == PrivateStoreType.PACKAGE_SELL));
					break;
				}
				case BUY:
				case BUY_MANAGE:
				{
					activeChar.setPrivateStoreType(PrivateStoreType.BUY_MANAGE);
					activeChar.sendPacket(new PrivateStoreManageListBuy(activeChar));
					break;
				}
				case MANUFACTURE:
				{
					activeChar.sendPacket(new RecipeShopManageList(activeChar, true));
				}
			}
		}
	}
}
