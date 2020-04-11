package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.TradeList.TradeItem;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * 3 section to this packet 1)playerinfo which is always sent dd 2)list of items which can be added to sell d(hhddddhhhd) 3)list of items which have already been setup for sell in previous sell private store sell manageent d(hhddddhhhdd) *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */

/*
 * In memory of our friend Vadim 03/11/2014
 */
public class PrivateStoreManageListSell extends L2GameServerPacket
{
	private final L2PcInstance activeChar;
	private int playerAdena;
	private final boolean packageSale;
	private final TradeItem[] itemList;
	private final TradeItem[] sellList;
	
	public PrivateStoreManageListSell(final L2PcInstance player)
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
		
		activeChar.getSellList().updateItems();
		packageSale = activeChar.getSellList().isPackaged();
		itemList = activeChar.getInventory().getAvailableItems(activeChar.getSellList());
		sellList = activeChar.getSellList().getItems();
	}
	
	/**
	 * During store set no packets will be received from client just when store definition is finished.
	 */
	@Override
	protected final void writeImpl()
	{
		writeC(0x9a);
		// section 1
		writeD(activeChar.getObjectId());
		writeD(packageSale ? 1 : 0); // Package sell
		writeD(playerAdena);
		
		// section2
		writeD(itemList.length); // for potential sells
		for (TradeItem item : itemList)
		{
			writeD(item.getItem().getType2());
			writeD(item.getObjectId());
			writeD(item.getItem().getItemId());
			writeD(item.getCount());
			writeH(0x00);
			writeH(item.getEnchant());// enchant lvl
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeD(item.getPrice()); // store price
		}
		// section 3
		writeD(sellList.length); // count for any items already added for sell
		for (TradeItem item : sellList)
		{
			writeD(item.getItem().getType2());
			writeD(item.getObjectId());
			writeD(item.getItem().getItemId());
			writeD(item.getCount());
			writeH(0x00);
			writeH(item.getEnchant());// enchant lvl
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeD(item.getPrice());// your price
			writeD(item.getItem().getReferencePrice()); // store price
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 9a PrivateSellListSell";
	}
}