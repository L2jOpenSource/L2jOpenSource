package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.TradeList;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * @version $Revision: 1.7.2.2.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class PrivateStoreListBuy extends L2GameServerPacket
{
	private final L2PcInstance privateStorePlayer;
	private final L2PcInstance activeChar;
	private int playerAdena;
	private final TradeList.TradeItem[] items;
	
	public PrivateStoreListBuy(final L2PcInstance player, final L2PcInstance storePlayer)
	{
		privateStorePlayer = storePlayer;
		activeChar = player;
		
		if (Config.SELL_BY_ITEM)
		{
			final CreatureSay cs11 = new CreatureSay(0, 15, "", "ATTENTION: Store System is not based on Adena, be careful!"); // 8D
			activeChar.sendPacket(cs11);
			playerAdena = activeChar.getItemCount(Config.SELL_ITEM, -1);
		}
		else
		{
			playerAdena = activeChar.getAdena();
		}
		
		// privateStorePlayer.getSellList().updateItems(); // Update SellList for case inventory content has changed
		// this items must be the items available into the activeChar (seller) inventory
		items = privateStorePlayer.getBuyList().getAvailableItems(activeChar.getInventory());
		
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xb8);
		writeD(privateStorePlayer.getObjectId());
		writeD(playerAdena);
		
		writeD(items.length);
		
		for (final TradeList.TradeItem item : items)
		{
			writeD(item.getObjectId());
			writeD(item.getItem().getItemId());
			writeH(item.getEnchant());
			// writeD(item.getCount()); //give max possible sell amount
			writeD(item.getCurCount());
			
			writeD(item.getItem().getReferencePrice());
			writeH(0);
			
			writeD(item.getItem().getBodyPart());
			writeH(item.getItem().getType2());
			writeD(item.getPrice());// buyers price
			
			writeD(item.getCount()); // maximum possible tradecount
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] b8 PrivateStoreListBuy";
	}
}