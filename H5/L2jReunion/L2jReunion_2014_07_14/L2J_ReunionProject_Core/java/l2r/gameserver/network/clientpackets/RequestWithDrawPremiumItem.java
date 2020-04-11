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
import l2r.gameserver.model.L2PremiumItem;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExGetPremiumItemList;
import l2r.gameserver.util.Util;

/**
 * @author Gnacik
 */
public final class RequestWithDrawPremiumItem extends L2GameClientPacket
{
	private static final String _C__D0_52_REQUESTWITHDRAWPREMIUMITEM = "[C] D0:52 RequestWithDrawPremiumItem";
	
	private int _itemNum;
	private int _charId;
	private long _itemCount;
	
	@Override
	protected void readImpl()
	{
		_itemNum = readD();
		_charId = readD();
		_itemCount = readQ();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		else if (_itemCount <= 0)
		{
			return;
		}
		else if (activeChar.getObjectId() != _charId)
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestWithDrawPremiumItem] Incorrect owner, Player: " + activeChar.getName(), Config.DEFAULT_PUNISH);
			return;
		}
		else if (activeChar.getPremiumItemList().isEmpty())
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestWithDrawPremiumItem] Player: " + activeChar.getName() + " try to get item with empty list!", Config.DEFAULT_PUNISH);
			return;
		}
		else if ((activeChar.getWeightPenalty() >= 3) || !activeChar.isInventoryUnder90(false))
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_RECEIVE_THE_VITAMIN_ITEM);
			return;
		}
		else if (activeChar.isProcessingTransaction())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_RECEIVE_A_VITAMIN_ITEM_DURING_AN_EXCHANGE);
			return;
		}
		
		L2PremiumItem _item = activeChar.getPremiumItemList().get(_itemNum);
		if (_item == null)
		{
			return;
		}
		else if (_item.getCount() < _itemCount)
		{
			return;
		}
		
		long itemsLeft = (_item.getCount() - _itemCount);
		
		activeChar.addItem("PremiumItem", _item.getId(), _itemCount, activeChar.getTarget(), true);
		
		if (itemsLeft > 0)
		{
			_item.updateCount(itemsLeft);
			activeChar.updatePremiumItem(_itemNum, itemsLeft);
		}
		else
		{
			activeChar.getPremiumItemList().remove(_itemNum);
			activeChar.deletePremiumItem(_itemNum);
		}
		
		if (activeChar.getPremiumItemList().isEmpty())
		{
			activeChar.sendPacket(SystemMessageId.THERE_ARE_NO_MORE_VITAMIN_ITEMS_TO_BE_FOUND);
		}
		else
		{
			activeChar.sendPacket(new ExGetPremiumItemList(activeChar));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_52_REQUESTWITHDRAWPREMIUMITEM;
	}
}
