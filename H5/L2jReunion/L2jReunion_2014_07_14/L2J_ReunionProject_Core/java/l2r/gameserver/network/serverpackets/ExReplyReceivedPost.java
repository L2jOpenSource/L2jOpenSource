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
package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.entity.Message;
import l2r.gameserver.model.itemcontainer.ItemContainer;
import l2r.gameserver.model.items.instance.L2ItemInstance;

/**
 * @author Migi, DS
 */
public class ExReplyReceivedPost extends L2GameServerPacket
{
	private final Message _msg;
	private L2ItemInstance[] _items = null;
	
	public ExReplyReceivedPost(Message msg)
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
				_log.warn("Message " + msg.getId() + " has attachments but itemcontainer is empty.");
			}
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xab);
		writeD(_msg.getId());
		writeD(_msg.isLocked() ? 1 : 0);
		writeD(0x00); // Unknown
		writeS(_msg.getSenderName());
		writeS(_msg.getSubject());
		writeS(_msg.getContent());
		
		if ((_items != null) && (_items.length > 0))
		{
			writeD(_items.length);
			for (L2ItemInstance item : _items)
			{
				writeD(0x00);
				writeD(item.getDisplayId());
				writeD(item.getLocationSlot());
				writeQ(item.getCount());
				writeH(item.getItem().getType2());
				writeH(item.getCustomType1());
				writeH(item.isEquipped() ? 0x01 : 0x00);
				writeD(item.getItem().getBodyPart());
				writeH(item.getEnchantLevel());
				writeH(item.getCustomType2());
				if (item.isAugmented())
				{
					writeD(item.getAugmentation().getAugmentationId());
				}
				else
				{
					writeD(0x00);
				}
				writeD(item.getMana());
				writeD(item.isTimeLimitedItem() ? (int) (item.getRemainingTime() / 1000) : -9999);
				writeH(item.getAttackElementType());
				writeH(item.getAttackElementPower());
				for (byte i = 0; i < 6; i++)
				{
					writeH(item.getElementDefAttr(i));
				}
				// Enchant Effects
				for (int op : item.getEnchantOptions())
				{
					writeH(op);
				}
				writeD(item.getObjectId());
			}
		}
		else
		{
			writeD(0x00);
		}
		
		writeQ(_msg.getReqAdena());
		writeD(_msg.hasAttachments() ? 1 : 0);
		writeD(_msg.getSendBySystem());
	}
}
