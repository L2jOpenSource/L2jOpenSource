package com.l2jfrozen.gameserver.network.serverpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * sample 27 00 00 01 00 // item count 04 00 // itemType1 0-weapon/ring/earring/necklace 1-armor/shield 4-item/questitem/adena c6 37 50 40 // objectId cd 09 00 00 // itemId 05 00 00 00 // count 05 00 // itemType2 0-weapon 1-shield/armor 2-ring/earring/necklace 3-questitem 4-adena 5-item 00 00 //
 * always 0 ?? 00 00 // equipped 1-yes 00 00 // slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand 00 00 // always 0 ?? 00 00 // always 0 ?? format h (h dddhhhh hh) revision 377 format h (h dddhhhd hh)
 * revision 415
 * @version $Revision: 1.4.2.1.2.4 $ $Date: 2005/03/27 15:29:57 $
 */
public class ItemList extends L2GameServerPacket
{
	private static Logger LOGGER = Logger.getLogger(ItemList.class);
	private final L2ItemInstance[] items;
	private final boolean showWindow;
	
	public ItemList(final L2PcInstance cha, final boolean showWindow)
	{
		items = cha.getInventory().getItems();
		this.showWindow = showWindow;
		if (Config.DEBUG)
		{
			showDebug();
		}
	}
	
	public ItemList(final L2ItemInstance[] items, final boolean showWindow)
	{
		this.items = items;
		this.showWindow = showWindow;
		if (Config.DEBUG)
		{
			showDebug();
		}
	}
	
	private void showDebug()
	{
		for (final L2ItemInstance temp : items)
		{
			LOGGER.debug("item:" + temp.getItem().getName() + " type1:" + temp.getItem().getType1() + " type2:" + temp.getItem().getType2());
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x1b);
		writeH(showWindow ? 0x01 : 0x00);
		final int count = items.length;
		writeH(count);
		for (final L2ItemInstance temp : items)
		{
			if ((temp == null) || (temp.getItem() == null))
			{
				continue;
			}
			writeH(temp.getItem().getType1()); // item type1
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(temp.getItem().getType2()); // item type2
			writeH(temp.getCustomType1()); // item type3
			writeH(temp.isEquipped() ? 0x01 : 0x00);
			writeD(temp.getItem().getBodyPart());
			writeH(temp.getEnchantLevel()); // enchant level
			// race tickets
			writeH(temp.getCustomType2()); // item type3
			if (temp.isAugmented())
			{
				writeD(temp.getAugmentation().getAugmentationId());
			}
			else
			{
				writeD(0x00);
			}
			writeD(temp.getMana());
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 1b ItemList";
	}
}