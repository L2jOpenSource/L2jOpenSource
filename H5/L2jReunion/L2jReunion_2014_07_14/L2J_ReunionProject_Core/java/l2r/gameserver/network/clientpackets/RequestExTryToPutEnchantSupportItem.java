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
import l2r.gameserver.model.items.enchant.EnchantSupportItem;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExPutEnchantSupportItemResult;

/**
 * @author KenM
 */
public class RequestExTryToPutEnchantSupportItem extends L2GameClientPacket
{
	private static final String _C__D0_4D_REQUESTEXTRYTOPUTENCHANTSUPPORTITEM = "[C] D0:4D RequestExTryToPutEnchantSupportItem";
	
	private int _supportObjectId;
	private int _enchantObjectId;
	
	@Override
	protected void readImpl()
	{
		_supportObjectId = readD();
		_enchantObjectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.isEnchanting())
		{
			final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_enchantObjectId);
			final L2ItemInstance scroll = activeChar.getInventory().getItemByObjectId(activeChar.getActiveEnchantItemId());
			final L2ItemInstance support = activeChar.getInventory().getItemByObjectId(_supportObjectId);
			
			if ((item == null) || (scroll == null) || (support == null))
			{
				// message may be custom
				activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
				activeChar.setActiveEnchantSupportItemId(L2PcInstance.ID_NONE);
				return;
			}
			
			final EnchantScroll scrollTemplate = EnchantItemData.getInstance().getEnchantScroll(scroll);
			final EnchantSupportItem supportTemplate = EnchantItemData.getInstance().getSupportItem(support);
			
			if ((scrollTemplate == null) || (supportTemplate == null) || !scrollTemplate.isValid(item, supportTemplate))
			{
				// message may be custom
				activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
				activeChar.setActiveEnchantSupportItemId(L2PcInstance.ID_NONE);
				activeChar.sendPacket(new ExPutEnchantSupportItemResult(0));
				return;
			}
			activeChar.setActiveEnchantSupportItemId(support.getObjectId());
			activeChar.sendPacket(new ExPutEnchantSupportItemResult(_supportObjectId));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_4D_REQUESTEXTRYTOPUTENCHANTSUPPORTITEM;
	}
}
