package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.ItemInfo;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;

/**
 * This class ...
 * @author  Yme
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/27 15:29:57 $ Rebuild 23.2.2006 by Advi
 */
public class PetInventoryUpdate extends L2GameServerPacket
{
	private static Logger LOGGER = Logger.getLogger(InventoryUpdate.class);
	private final List<ItemInfo> items;
	
	/**
	 * @param items
	 */
	public PetInventoryUpdate(final List<ItemInfo> items)
	{
		this.items = items;
		if (Config.DEBUG)
		{
			showDebug();
		}
	}
	
	public PetInventoryUpdate()
	{
		this(new ArrayList<ItemInfo>());
	}
	
	public void addItem(final L2ItemInstance item)
	{
		items.add(new ItemInfo(item));
	}
	
	public void addNewItem(final L2ItemInstance item)
	{
		items.add(new ItemInfo(item, 1));
	}
	
	public void addModifiedItem(final L2ItemInstance item)
	{
		items.add(new ItemInfo(item, 2));
	}
	
	public void addRemovedItem(final L2ItemInstance item)
	{
		items.add(new ItemInfo(item, 3));
	}
	
	public void addItems(final List<L2ItemInstance> items)
	{
		for (final L2ItemInstance item : items)
		{
			this.items.add(new ItemInfo(item));
		}
	}
	
	private void showDebug()
	{
		for (final ItemInfo item : items)
		{
			LOGGER.debug("oid:" + Integer.toHexString(item.getObjectId()) + " item:" + item.getItem().getName() + " last change:" + item.getChange());
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xb3);
		final int count = items.size();
		writeH(count);
		for (final ItemInfo item : items)
		{
			writeH(item.getChange());
			writeH(item.getItem().getType1()); // item type1
			writeD(item.getObjectId());
			writeD(item.getItem().getItemId());
			writeD(item.getCount());
			writeH(item.getItem().getType2()); // item type2
			writeH(0x00); // ?
			writeH(item.getEquipped());
			// writeH(temp.getItem().getBodyPart()); // rev 377 slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
			writeD(item.getItem().getBodyPart()); // rev 415 slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
			writeH(item.getEnchant()); // enchant level
			writeH(0x00); // ?
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] b3 InventoryUpdate";
	}
}
