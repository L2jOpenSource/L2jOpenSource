package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.ItemRequest;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.TradeList;
import com.l2jfrozen.gameserver.model.TradeList.TradeItem;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.util.Util;

public final class RequestPrivateStoreBuy extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestPrivateStoreBuy.class);
	
	private int storePlayerId;
	private int count;
	private ItemRequest[] items;
	
	@Override
	protected void readImpl()
	{
		storePlayerId = readD();
		count = readD();
		
		// count*12 is the size of a for iteration of each item
		if (count < 0 || count * 12 > buf.remaining() || count > Config.MAX_ITEM_IN_PACKET)
		{
			count = 0;
		}
		
		items = new ItemRequest[count];
		
		for (int i = 0; i < count; i++)
		{
			final int objectId = readD();
			long count = readD();
			if (count > Integer.MAX_VALUE)
			{
				count = Integer.MAX_VALUE;
			}
			final int price = readD();
			
			items[i] = new ItemRequest(objectId, (int) count, price);
		}
		
		if (Config.DEBUG)
		{
			
			LOGGER.info("Player " + getClient().getActiveChar().getName() + " requested to buy to storeId " + storePlayerId + " Items Number: " + count);
			
			for (int i = 0; i < count; i++)
			{
				LOGGER.info("Requested Item ObjectID: " + items[i].getObjectId());
				LOGGER.info("Requested Item Id: " + items[i].getItemId());
				LOGGER.info("Requested Item count: " + items[i].getCount());
				LOGGER.info("Requested Item price: " + items[i].getPrice());
				
			}
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("privatestorebuy"))
		{
			player.sendMessage("You buying items too fast.");
			return;
		}
		
		final L2Object object = L2World.getInstance().findObject(storePlayerId);
		if (object == null || !(object instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance storePlayer = (L2PcInstance) object;
		if (!(storePlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_SELL || storePlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL))
		{
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Unsufficient privileges.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final TradeList storeList = storePlayer.getSellList();
		
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
		
		// FIXME: this check should be (and most probabliy is) done in the TradeList mechanics
		long priceTotal = 0;
		for (final ItemRequest ir : items)
		{
			if (ir.getCount() > Integer.MAX_VALUE || ir.getCount() < 0)
			{
				final String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried an overflow exploit, ban this player!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				return;
			}
			
			final TradeItem sellersItem = storeList.getItem(ir.getObjectId());
			
			if (sellersItem == null)
			{
				final String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried to buy an item not sold in a private store (buy), ban this player!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				return;
			}
			
			if (ir.getPrice() != sellersItem.getPrice())
			{
				final String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried to change the seller's price in a private store (buy), ban this player!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				return;
			}
			
			final L2ItemInstance iEnchant = storePlayer.getInventory().getItemByObjectId(ir.getObjectId());
			int enchant = 0;
			if (iEnchant == null)
			{
				enchant = 0;
			}
			else
			{
				enchant = iEnchant.getEnchantLevel();
			}
			ir.setEnchant(enchant);
			
			priceTotal += ir.getPrice() * ir.getCount();
		}
		
		// FIXME: this check should be (and most probabliy is) done in the TradeList mechanics
		if (priceTotal < 0 || priceTotal > Integer.MAX_VALUE)
		{
			final String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried an overflow exploit, ban this player!";
			Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
			return;
		}
		
		if (Config.SELL_BY_ITEM)
		{
			if (player.getItemCount(Config.SELL_ITEM, -1) < priceTotal)
			{
				sendPacket(SystemMessage.sendString("You do not have needed items to buy"));
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
		}
		else
		{
			if (player.getAdena() < priceTotal)
			{
				sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		if (storePlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL)
		{
			if (storeList.getItemCount() > count)
			{
				final String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried to buy less items then sold by package-sell, ban this player for bot-usage!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				return;
			}
		}
		
		if (!storeList.PrivateStoreBuy(player, items, (int) priceTotal))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			// Punishment e LOGGER in audit
			Util.handleIllegalPlayerAction(storePlayer, "PrivateStore buy has failed due to invalid list or request. Player: " + player.getName(), Config.DEFAULT_PUNISH);
			LOGGER.warn("PrivateStore buy has failed due to invalid list or request. Player: " + player.getName() + ", Private store of: " + storePlayer.getName());
			return;
		}
		
		if (storeList.getItemCount() == 0)
		{
			storePlayer.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			storePlayer.broadcastUserInfo();
		}
		
		/*
		 * Lease holders are currently not implemented else if (_seller != null) { // lease shop sell L2MerchantInstance seller = (L2MerchantInstance)_seller; L2ItemInstance ladena = seller.getLeaseAdena(); for (TradeItem ti : buyerlist) { L2ItemInstance li = seller.getLeaseItemByObjectId(ti.getObjectId());
		 * if (li == null) { if (ti.getObjectId() == ladena.getObjectId()) { buyer.addAdena(ti.getCount()); ladena.setCount(ladena.getCount()-ti.getCount()); ladena.updateDatabase(); } continue; } int cnt = li.getCount(); if (cnt < ti.getCount()) ti.setCount(cnt); if (ti.getCount() <= 0) continue;
		 * L2ItemInstance inst = ItemTable.getInstance().createItem(li.getItemId()); inst.setCount(ti.getCount()); inst.setEnchantLevel(li.getEnchantLevel()); buyer.getInventory().addItem(inst); li.setCount(li.getCount()-ti.getCount()); li.updateDatabase();
		 * ladena.setCount(ladena.getCount()+ti.getCount()*ti.getOwnersPrice()); ladena.updateDatabase(); } }
		 */
	}
	
	@Override
	public String getType()
	{
		return "[C] 79 RequestPrivateStoreBuy";
	}
}