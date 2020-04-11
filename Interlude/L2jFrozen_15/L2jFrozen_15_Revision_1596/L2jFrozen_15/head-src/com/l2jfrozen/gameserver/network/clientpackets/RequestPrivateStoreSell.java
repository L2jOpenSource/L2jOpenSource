package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.ItemRequest;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.TradeList;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.util.Util;

public final class RequestPrivateStoreSell extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestPrivateStoreSell.class);
	
	private int storePlayerId;
	private int sellCount;
	private int price;
	private ItemRequest[] items;
	
	@Override
	protected void readImpl()
	{
		storePlayerId = readD();
		sellCount = readD();
		// count*20 is the size of a for iteration of each item
		if (sellCount < 0 || sellCount * 20 > buf.remaining() || sellCount > Config.MAX_ITEM_IN_PACKET)
		{
			sellCount = 0;
		}
		items = new ItemRequest[sellCount];
		
		long priceTotal = 0;
		for (int i = 0; i < sellCount; i++)
		{
			final int objectId = readD();
			final int itemId = readD();
			final int enchant = readH();
			readH(); // TODO analyse this
			final long count = readD();
			final int price = readD();
			
			if (count > Integer.MAX_VALUE || count < 0)
			{
				final String msgErr = "[RequestPrivateStoreSell] player " + getClient().getActiveChar().getName() + " tried an overflow exploit, ban this player!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				sellCount = 0;
				items = null;
				return;
			}
			items[i] = new ItemRequest(objectId, itemId, enchant, (int) count, price);
			priceTotal += price * count;
		}
		
		if (priceTotal < 0 || priceTotal > Integer.MAX_VALUE)
		{
			final String msgErr = "[RequestPrivateStoreSell] player " + getClient().getActiveChar().getName() + " tried an overflow exploit, ban this player!";
			Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
			sellCount = 0;
			items = null;
			return;
		}
		
		if (Config.DEBUG)
		{
			
			LOGGER.info("Player " + getClient().getActiveChar().getName() + " requested to sell to storeId " + storePlayerId + " Items Number: " + sellCount);
			
			for (int i = 0; i < sellCount; i++)
			{
				LOGGER.info("Requested Item ObjectID: " + items[i].getObjectId());
				LOGGER.info("Requested Item Id: " + items[i].getItemId());
				LOGGER.info("Requested Item count: " + items[i].getCount());
				LOGGER.info("Requested Item enchant: " + items[i].getCount());
				LOGGER.info("Requested Item price: " + items[i].getPrice());
				
			}
		}
		
		price = (int) priceTotal;
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("privatestoresell"))
		{
			player.sendMessage("You selling items too fast");
			return;
		}
		
		final L2Object object = L2World.getInstance().findObject(storePlayerId);
		if (object == null || !(object instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance storePlayer = (L2PcInstance) object;
		if (storePlayer.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_BUY)
		{
			return;
		}
		
		final TradeList storeList = storePlayer.getBuyList();
		if (storeList == null)
		{
			return;
		}
		
		// Check if player didn't choose any items
		if (items == null || items.length == 0)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (storeList.getItemCount() == 0)
		{
			storePlayer.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			storePlayer.broadcastUserInfo();
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Unsufficient privileges.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (Config.SELL_BY_ITEM)
		{
			if (storePlayer.getItemCount(Config.SELL_ITEM, -1) < price)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessage.sendString("You have not enough items to buy, canceling PrivateBuy"));
				storePlayer.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
				storePlayer.broadcastUserInfo();
				return;
			}
			
		}
		else
		{
			if (storePlayer.getAdena() < price)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				storePlayer.sendMessage("You have not enough adena, canceling PrivateBuy.");
				storePlayer.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
				storePlayer.broadcastUserInfo();
				return;
			}
		}
		
		if (!storeList.PrivateStoreSell(player, items, price))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			Util.handleIllegalPlayerAction(getClient().getActiveChar(), "Player " + getClient().getActiveChar().getName() + " provided invalid list or request! ", Config.DEFAULT_PUNISH);
			LOGGER.warn("PrivateStore sell has failed due to invalid list or request. Player: " + player.getName() + ", Private store of: " + storePlayer.getName());
			return;
		}
		
		if (storeList.getItemCount() == 0)
		{
			storePlayer.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			storePlayer.broadcastUserInfo();
		}
		
	}
	
	@Override
	public String getType()
	{
		return "[C] 96 RequestPrivateStoreSell";
	}
}