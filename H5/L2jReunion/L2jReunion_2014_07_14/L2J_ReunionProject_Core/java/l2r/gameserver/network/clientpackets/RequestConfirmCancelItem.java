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

import l2r.Config;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExPutItemResultForVariationCancel;
import l2r.gameserver.util.Util;
import gr.reunion.configsEngine.CustomServerConfigs;

/**
 * Format(ch) d
 * @author -Wooden-
 */
public final class RequestConfirmCancelItem extends L2GameClientPacket
{
	private static final String _C__D0_42_REQUESTCONFIRMCANCELITEM = "[C] D0:42 RequestConfirmCancelItem";
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		if (item == null)
		{
			return;
		}
		
		if (item.getOwnerId() != activeChar.getObjectId())
		{
			Util.handleIllegalPlayerAction(getClient().getActiveChar(), "Warning!! Character " + getClient().getActiveChar().getName() + " of account " + getClient().getActiveChar().getAccountName() + " tryied to destroy augment on item that doesn't own.", Config.DEFAULT_PUNISH);
			return;
		}
		
		if (!item.isAugmented() && !CustomServerConfigs.ALT_ALLOW_REFINE_HERO_ITEM)
		{
			activeChar.sendPacket(SystemMessageId.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM);
			return;
		}
		
		if (item.isPvp() && !CustomServerConfigs.ALT_ALLOW_REFINE_PVP_ITEM)
		{
			activeChar.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}
		
		int price = 0;
		switch (item.getItem().getCrystalType())
		{
			case C:
				if (item.getCrystalCount() < 1720)
				{
					price = 95000;
				}
				else if (item.getCrystalCount() < 2452)
				{
					price = 150000;
				}
				else
				{
					price = 210000;
				}
				break;
			case B:
				if (item.getCrystalCount() < 1746)
				{
					price = 240000;
				}
				else
				{
					price = 270000;
				}
				break;
			case A:
				if (item.getCrystalCount() < 2160)
				{
					price = 330000;
				}
				else if (item.getCrystalCount() < 2824)
				{
					price = 390000;
				}
				else
				{
					price = 420000;
				}
				break;
			case S:
				price = 480000;
				break;
			case S80:
			case S84:
				price = 920000;
				break;
			// TODO: S84 TOP price 3.2M
			// any other item type is not augmentable
			default:
				return;
		}
		
		activeChar.sendPacket(new ExPutItemResultForVariationCancel(item, price));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_42_REQUESTCONFIRMCANCELITEM;
	}
}
