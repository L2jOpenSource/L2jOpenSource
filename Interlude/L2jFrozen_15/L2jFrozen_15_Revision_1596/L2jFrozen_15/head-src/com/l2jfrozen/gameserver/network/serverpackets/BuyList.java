package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.List;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2TradeList;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.templates.L2Item;

/**
 * sample 1d 1e 00 00 00 // ?? 5c 4a a0 7c // buy list id 02 00 // item count 04 00 // itemType1 0-weapon/ring/earring/necklace 1-armor/shield 4-item/questitem/adena 00 00 00 00 // objectid 32 04 00 00 // itemid 00 00 00 00 // count 05 00 // itemType2 0-weapon 1-shield/armor 2-ring/earring/necklace
 * 3-questitem 4-adena 5-item 00 00 60 09 00 00 // price 00 00 00 00 00 00 b6 00 00 00 00 00 00 00 00 00 00 00 80 00 // body slot these 4 values are only used if itemtype1 = 0 or 1 00 00 // 00 00 // 00 00 // 50 c6 0c 00 format dd h (h dddhh hhhh d) revision 377 format dd h (h dddhh dhhh d)
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public final class BuyList extends L2GameServerPacket
{
	private final int listId;
	private final L2ItemInstance[] buyList;
	private final int money;
	private double taxRate = 0;
	
	public BuyList(final L2TradeList list, final int currentMoney)
	{
		listId = list.getListId();
		final List<L2ItemInstance> lst = list.getItems();
		buyList = lst.toArray(new L2ItemInstance[lst.size()]);
		money = currentMoney;
	}
	
	public BuyList(final L2TradeList list, final int currentMoney, final double taxRate)
	{
		listId = list.getListId();
		final List<L2ItemInstance> lst = list.getItems();
		buyList = lst.toArray(new L2ItemInstance[lst.size()]);
		money = currentMoney;
		this.taxRate = taxRate;
	}
	
	public BuyList(final List<L2ItemInstance> lst, final int listId, final int currentMoney)
	{
		this.listId = listId;
		buyList = lst.toArray(new L2ItemInstance[lst.size()]);
		money = currentMoney;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x11);
		writeD(money); // current money
		writeD(listId);
		
		writeH(buyList.length);
		
		for (final L2ItemInstance item : buyList)
		{
			if (item.getCount() > 0 || item.getCount() == -1)
			{
				writeH(item.getItem().getType1()); // item type1
				writeD(item.getObjectId());
				writeD(item.getItemId());
				if (item.getCount() < 0)
				{
					writeD(0x00); // max amount of items that a player can buy at a time (with this itemid)
				}
				else
				{
					writeD(item.getCount());
				}
				writeH(item.getItem().getType2()); // item type2
				writeH(0x00); // ?
				
				if (item.getItem().getType1() != L2Item.TYPE1_ITEM_QUESTITEM_ADENA)
				{
					writeD(item.getItem().getBodyPart()); // rev 415 slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
					writeH(item.getEnchantLevel()); // enchant level
					writeH(0x00); // ?
					writeH(0x00);
				}
				else
				{
					writeD(0x00); // rev 415 slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
					writeH(0x00); // enchant level
					writeH(0x00); // ?
					writeH(0x00);
				}
				
				if (item.getItemId() >= 3960 && item.getItemId() <= 4026)
				{
					writeD((int) (item.getPriceToSell() * Config.RATE_SIEGE_GUARDS_PRICE * (1 + taxRate)));
				}
				else
				{
					writeD((int) (item.getPriceToSell() * (1 + taxRate)));
				}
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 11 BuyList";
	}
}
