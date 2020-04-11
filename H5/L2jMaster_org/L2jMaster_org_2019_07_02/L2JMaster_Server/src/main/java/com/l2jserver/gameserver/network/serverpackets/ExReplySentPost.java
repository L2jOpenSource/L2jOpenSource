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
package com.l2jserver.gameserver.network.serverpackets;

import com.l2jserver.gameserver.model.entity.Message;
import com.l2jserver.gameserver.model.itemcontainer.ItemContainer;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;

/**
 * ExReplySentPost packet implementation.
 * @author Migi, DS
 */
public class ExReplySentPost extends AbstractItemPacket
{
	private final Message _msg;
	private L2ItemInstance[] _items = null;
	
	public ExReplySentPost(Message msg)
	{
		_msg = msg;
		if (msg.hasAttachments())
		{
			final ItemContainer attachments = msg.getAttachments();
			if ((attachments != null) && (attachments.getSize() > 0))
			{
				_items = attachments.getItems();
			}
			else
			{
				_log.warning("Message " + msg.getId() + " has attachments but itemcontainer is empty.");
			}
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xAD);
		writeD(_msg.getId());
		writeD(_msg.isLocked() ? 1 : 0);
		writeS(_msg.getReceiverName());
		writeS(_msg.getSubject());
		writeS(_msg.getContent());
		
		if ((_items != null) && (_items.length > 0))
		{
			writeD(_items.length);
			for (L2ItemInstance item : _items)
			{
				writeItem(item);
				writeD(item.getObjectId());
			}
			writeQ(_msg.getReqAdena());
			writeD(_msg.getSendBySystem());
		}
		else
		{
			writeD(0x00);
			writeQ(_msg.getReqAdena());
		}
	}
}
