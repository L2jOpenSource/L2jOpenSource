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

import l2r.gameserver.datatables.xml.EnchantItemData;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.enchant.EnchantScroll;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExPutEnchantTargetItemResult;

/**
 * @author KenM
 */
public class RequestExTryToPutEnchantTargetItem extends L2GameClientPacket
{
	private static final String _C__D0_4C_REQUESTEXTRYTOPUTENCHANTTARGETITEM = "[C] D0:4C RequestExTryToPutEnchantTargetItem";
	
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
		if ((_objectId == 0) || (activeChar == null))
		{
			return;
		}
		
		if (activeChar.isEnchanting())
		{
			return;
		}
		
		final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		final L2ItemInstance scroll = activeChar.getInventory().getItemByObjectId(activeChar.getActiveEnchantItemId());
		if ((item == null) || (scroll == null))
		{
			return;
		}
		
		final EnchantScroll scrollTemplate = EnchantItemData.getInstance().getEnchantScroll(scroll);
		if ((scrollTemplate == null) || !scrollTemplate.isValid(item, null))
		{
			activeChar.sendPacket(SystemMessageId.DOES_NOT_FIT_SCROLL_CONDITIONS);
			activeChar.setActiveEnchantItemId(L2PcInstance.ID_NONE);
			activeChar.sendPacket(new ExPutEnchantTargetItemResult(0));
			if (scrollTemplate == null)
			{
				_log.warn(getClass().getSimpleName() + ": Undefined scroll have been used id: " + scroll.getId());
			}
			return;
		}
		activeChar.setIsEnchanting(true);
		activeChar.setActiveEnchantTimestamp(System.currentTimeMillis());
		activeChar.sendPacket(new ExPutEnchantTargetItemResult(_objectId));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_4C_REQUESTEXTRYTOPUTENCHANTTARGETITEM;
	}
}
