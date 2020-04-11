package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.TradeList;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * @version $Revision: 1.2.2.3.2.6 $ $Date: 2005/03/27 15:29:57 $
 */
public class PrivateStoreListSell extends L2GameServerPacket
{
	private final L2PcInstance privateStorePlayer;
	private final L2PcInstance activeChar;
	private int playerAdena;
	private final boolean packageSale;
	private final TradeList.TradeItem[] items;
	
	// player's private shop
	public PrivateStoreListSell(final L2PcInstance player, final L2PcInstance storePlayer)
	{
		activeChar = player;
		privateStorePlayer = storePlayer;
		
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
		
		privateStorePlayer.getSellList().updateItems();
		items = privateStorePlayer.getSellList().getItems();
		packageSale = privateStorePlayer.getSellList().isPackaged();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x9b);
		writeD(privateStorePlayer.getObjectId());
		writeD(packageSale ? 1 : 0);
		writeD(playerAdena);
		
		writeD(items.length);
		for (final TradeList.TradeItem item : items)
		{
			writeD(item.getItem().getType2());
			writeD(item.getObjectId());
			writeD(item.getItem().getItemId());
			writeD(item.getCount());
			writeH(0x00);
			writeH(item.getEnchant());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeD(item.getPrice()); // your price
			writeD(item.getItem().getReferencePrice()); // store price
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 9b PrivateStoreListSell";
	}
}