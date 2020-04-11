/*
 * Copyright (C) L2J Sunrise
 * This file is part of L2J Sunrise
 */
package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.Elementals;
import l2r.gameserver.model.ItemInfo;
import l2r.gameserver.model.TradeItem;
import l2r.gameserver.model.buylist.Product;
import l2r.gameserver.model.itemcontainer.PcInventory;
import l2r.gameserver.model.items.instance.L2ItemInstance;

/**
 * @author vGodFather
 */
public abstract class AbstractItemPacket extends L2GameServerPacket
{
	protected void writeItem(TradeItem item, boolean isTrade)
	{
		writeItem(new ItemInfo(item), isTrade);
	}
	
	protected void writeItem(TradeItem item)
	{
		writeItem(new ItemInfo(item), false);
	}
	
	protected void writeItem(L2ItemInstance item)
	{
		writeItem(new ItemInfo(item));
	}
	
	protected void writeItem(Product item)
	{
		writeItem(new ItemInfo(item));
	}
	
	protected void writeItem(ItemInfo item)
	{
		writeItem(item, false);
	}
	
	protected void writeItem(ItemInfo item, boolean isTrade)
	{
		if (isTrade)
		{
			writeD(item.getObjectId());
			writeD(item.getItem().getDisplayId());
			// writeD(item.getLocation());
			writeQ(item.getCount());
			writeH(item.getItem().getType2()); // item type2
			writeH(item.getCustomType1()); // item type3
			// writeH(item.getEquipped());
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchant()); // enchant level
			writeH(item.getCustomType2()); // item type3
			writeH(0x00);
			// writeD(item.getAugmentationBonus());
			// writeD(item.getMana());
			// writeD(item.getTime());
			
			writeItemElemental(item);
			
			writeItemEnchantEffect(item);
			return;
		}
		writeD(item.getObjectId());
		writeD(item.getItem().getDisplayId());
		writeD(item.getLocation());
		writeQ(item.getCount());
		writeH(item.getItem().getType2()); // item type2
		writeH(item.getCustomType1()); // item type3
		writeH(item.getEquipped());
		writeD(item.getItem().getBodyPart());
		writeH(item.getEnchant()); // enchant level
		writeH(item.getCustomType2()); // item type3
		writeD(item.getAugmentationBonus());
		writeD(item.getMana());
		writeD(item.getTime());
		
		writeItemElemental(item);
		
		writeItemEnchantEffect(item);
	}
	
	protected void writeInventoryBlock(PcInventory inventory)
	{
		if (inventory.hasInventoryBlock())
		{
			writeH(inventory.getBlockItems().length);
			writeC(inventory.getBlockMode());
			for (int i : inventory.getBlockItems())
			{
				writeD(i);
			}
		}
		else
		{
			writeH(0x00);
		}
	}
	
	protected void writeItemAugment(ItemInfo item)
	{
		writeD(item == null ? 0x00 : item.getAugmentationBonus());
		writeD(item == null ? 0x00 : item.getAugmentationBonus());
	}
	
	protected void writeItemElemental(ItemInfo item)
	{
		writeH(item == null ? 0x00 : item.getAttackElementType());
		writeH(item == null ? 0x00 : item.getAttackElementPower());
		writeH(item == null ? 0x00 : item.getElementDefAttr(Elementals.FIRE));
		writeH(item == null ? 0x00 : item.getElementDefAttr(Elementals.WATER));
		writeH(item == null ? 0x00 : item.getElementDefAttr(Elementals.WIND));
		writeH(item == null ? 0x00 : item.getElementDefAttr(Elementals.EARTH));
		writeH(item == null ? 0x00 : item.getElementDefAttr(Elementals.HOLY));
		writeH(item == null ? 0x00 : item.getElementDefAttr(Elementals.DARK));
	}
	
	protected void writeItemEnchantEffect(ItemInfo item)
	{
		for (int op : item.getEnchantOptions())
		{
			writeH(op);
		}
	}
}
