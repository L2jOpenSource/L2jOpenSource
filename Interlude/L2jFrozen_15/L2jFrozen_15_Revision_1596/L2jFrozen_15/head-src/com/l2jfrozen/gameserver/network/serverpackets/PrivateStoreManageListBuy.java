package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.TradeList;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:40 $
 */
public class PrivateStoreManageListBuy extends L2GameServerPacket
{
	private final L2PcInstance activeChar;
	private int playerAdena;
	private final L2ItemInstance[] itemList;
	private final TradeList.TradeItem[] buyList;
	
	public PrivateStoreManageListBuy(final L2PcInstance player)
	{
		activeChar = player;
		
		if (Config.SELL_BY_ITEM)
		{
			playerAdena = activeChar.getItemCount(Config.SELL_ITEM, -1);
		}
		else
		{
			playerAdena = activeChar.getAdena();
		}
		
		itemList = activeChar.getInventory().getUniqueItems(false, true, true);
		buyList = activeChar.getBuyList().getItems();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xb7);
		// section 1
		writeD(activeChar.getObjectId());
		writeD(playerAdena);
		
		// section2
		writeD(itemList.length); // inventory items for potential buy
		for (final L2ItemInstance item : itemList)
		{
			writeD(item.getItemId());
			writeH(item.getEnchantLevel()); // show enchant lvl, but you can't buy enchanted weapons because of L2 Interlude Client bug
			writeD(item.getCount());
			writeD(item.getReferencePrice());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getItem().getType2());
		}
		
		// section 3
		writeD(buyList.length); // count for all items already added for buy
		for (final TradeList.TradeItem item : buyList)
		{
			writeD(item.getItem().getItemId());
			writeH(item.getEnchant());
			writeD(item.getCount());
			writeD(item.getItem().getReferencePrice());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getItem().getType2());
			writeD(item.getPrice());// your price
			writeD(item.getItem().getReferencePrice());// fixed store price
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] b7 PrivateSellListBuy";
	}
}