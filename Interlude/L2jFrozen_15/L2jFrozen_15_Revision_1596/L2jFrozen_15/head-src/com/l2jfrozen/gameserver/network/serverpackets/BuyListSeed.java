package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.model.L2TradeList;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;

/**
 * Format: c ddh[hdddhhd] c - id (0xE8) d - money d - manor id h - size [ h - item type 1 d - object id d - item id d - count h - item type 2 h d - price ]
 * @author l3x
 */

public final class BuyListSeed extends L2GameServerPacket
{
	private final int manorId;
	private List<L2ItemInstance> list = new ArrayList<>();
	private final int money;
	
	public BuyListSeed(final L2TradeList list, final int manorId, final int currentMoney)
	{
		money = currentMoney;
		this.manorId = manorId;
		this.list = list.getItems();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xE8);
		
		writeD(money); // current money
		writeD(manorId); // manor id
		
		writeH(list.size()); // list length
		
		for (final L2ItemInstance item : list)
		{
			writeH(0x04); // item->type1
			writeD(0x00); // objectId
			writeD(item.getItemId()); // item id
			writeD(item.getCount()); // item count
			writeH(0x04); // item->type2
			writeH(0x00); // unknown :)
			writeD(item.getPriceToSell()); // price
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] E8 BuyListSeed";
	}
}
