package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.TradeList;
import com.l2jfrozen.gameserver.model.TradeList.TradeItem;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Beetle
 */
public class TradeUpdate extends L2GameServerPacket
{
	private final L2ItemInstance[] items;
	private final TradeItem[] trade_items;
	
	public TradeUpdate(final TradeList trade, final L2PcInstance activeChar)
	{
		items = activeChar.getInventory().getItems();
		trade_items = trade.getItems();
	}
	
	private int getItemCount(final int objectId)
	{
		for (final L2ItemInstance item : items)
		{
			if (item.getObjectId() == objectId)
			{
				return item.getCount();
			}
		}
		return 0;
	}
	
	@Override
	public String getType()
	{
		return "[S] 74 TradeUpdate";
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x74);
		
		writeH(trade_items.length);
		for (final TradeItem item : trade_items)
		{
			int aveable_count = getItemCount(item.getObjectId()) - item.getCount();
			boolean isStackable = item.getItem().isStackable();
			if (aveable_count == 0)
			{
				aveable_count = 1;
				isStackable = false;
			}
			writeH(isStackable ? 3 : 2);
			writeH(item.getItem().getType1()); // item type1
			writeD(item.getObjectId());
			writeD(item.getItem().getItemId());
			writeD(aveable_count);
			writeH(item.getItem().getType2()); // item type2
			writeH(0x00); // ?
			writeD(item.getItem().getBodyPart()); // rev 415 slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
			writeH(item.getEnchant()); // enchant level
			writeH(0x00); // ?
			writeH(0x00);
		}
	}
}
