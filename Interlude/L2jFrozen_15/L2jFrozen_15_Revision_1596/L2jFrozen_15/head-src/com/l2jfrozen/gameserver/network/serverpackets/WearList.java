package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.List;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2TradeList;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.templates.L2Item;

public class WearList extends L2GameServerPacket
{
	private final int listId;
	private final L2ItemInstance[] list;
	private final int money;
	private int expertise;
	
	public WearList(final L2TradeList list, final int currentMoney, final int expertiseIndex)
	{
		listId = list.getListId();
		List<L2ItemInstance> lst = list.getItems();
		this.list = lst.toArray(new L2ItemInstance[lst.size()]);
		money = currentMoney;
		expertise = expertiseIndex;
	}
	
	public WearList(final List<L2ItemInstance> lst, final int listId, final int currentMoney)
	{
		this.listId = listId;
		list = lst.toArray(new L2ItemInstance[lst.size()]);
		money = currentMoney;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xef);
		writeC(0xc0); // ?
		writeC(0x13); // ?
		writeC(0x00); // ?
		writeC(0x00); // ?
		writeD(money); // current money
		writeD(listId);
		
		int newlength = 0;
		for (final L2ItemInstance item : list)
		{
			if (item.getItem().getCrystalType() <= expertise && item.isEquipable())
			{
				newlength++;
			}
		}
		writeH(newlength);
		
		for (final L2ItemInstance item : list)
		{
			if (item.getItem().getCrystalType() <= expertise && item.isEquipable())
			{
				writeD(item.getItemId());
				writeH(item.getItem().getType2()); // item type2
				
				if (item.getItem().getType1() != L2Item.TYPE1_ITEM_QUESTITEM_ADENA)
				{
					writeH(item.getItem().getBodyPart()); // rev 415 slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
				}
				else
				{
					writeH(0x00); // rev 415 slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
				}
				
				writeD(Config.WEAR_PRICE);
				
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] EF WearList";
	}
}
