package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.TradeList;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.PrivateStoreManageListBuy;
import com.l2jfrozen.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public class SetPrivateStoreListBuy extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 16; // length of one item
	private int count;
	private Item[] items = null;
	
	@Override
	protected void readImpl()
	{
		count = readD();
		
		if (count < 1 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH > buf.remaining())
		{
			return;
		}
		
		items = new Item[count];
		
		for (int i = 0; i < count; i++)
		{
			int itemId = readD();
			int enchant = readH(); // it's the enchant value, but the interlude client has a bug, so it dnt send back the correct enchant value
			readH(); // damage?
			int cnt = readD();
			int price = readD();
			
			if (itemId < 1 || cnt < 1 || price < 0)
			{
				items = null;
				return;
			}
			items[i] = new Item(itemId, cnt, price, enchant);
		}
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
		{
			return;
		}
		
		if (items == null)
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			player.broadcastUserInfo();
			player.sendPacket(new PrivateStoreManageListBuy(player));
			return;
		}
		
		if (player.isMoving())
		{
			player.sendPacket(SystemMessageId.CANNOT_OPEN_A_PRIVATE_STORE);
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isCastingNow() || player.isCastingPotionNow())
		{
			player.sendPacket(SystemMessageId.A_PRIVATE_STORE_MAY_NOT_BE_OPENED_WHILE_USING_A_SKILL);
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInCombat())
		{
			player.sendPacket(SystemMessageId.A_PRIVATE_STORE_MAY_NOT_BE_OPENED_WHILE_USING_A_SKILL);
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendPacket(SystemMessageId.CANT_CRAFT_DURING_COMBAT);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isTradeDisabled())
		{
			player.sendMessage("Trade are disable here. Try in another place.");
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isMovementDisabled() || player.inObserverMode() || player.getActiveEnchantItem() != null)
		{
			player.sendMessage("You cannot start store now..");
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInsideZone(L2Character.ZONE_NOSTORE))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_OPEN_A_PRIVATE_STORE_HERE);
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		TradeList tradeList = player.getBuyList();
		tradeList.clear();
		
		int totalCost = 0;
		for (Item item : items)
		{
			totalCost += item.getCost();
			if (totalCost > player.getAdena())
			{
				player.sendPacket(SystemMessageId.THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY);
				player.sendPacket(new PrivateStoreManageListBuy(player));
				return;
			}
			
			if (!item.addToTradeList(tradeList))
			{
				player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				player.sendPacket(new PrivateStoreManageListBuy(player));
				return;
			}
			
		}
		
		if (count <= 0)
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			player.broadcastUserInfo();
			return;
		}
		
		if (player.isProcessingTransaction())
		{
			player.sendMessage("Store mode are disable while trading.");
			player.sendPacket(new PrivateStoreManageListBuy(player));
			return;
		}
		
		// Check maximum number of allowed slots for pvt shops
		if (count > player.GetPrivateBuyStoreLimit())
		{
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
			return;
		}
		
		// Check for available funds
		if (Config.SELL_BY_ITEM)
		{
			if (totalCost > player.getItemCount(Config.SELL_ITEM, -1) || totalCost <= 0)
			{
				player.sendPacket(new PrivateStoreManageListBuy(player));
				player.sendPacket(new SystemMessage(SystemMessageId.THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY));
				return;
			}
		}
		else
		{
			if (totalCost > player.getAdena() || totalCost <= 0)
			{
				player.sendPacket(new PrivateStoreManageListBuy(player));
				player.sendPacket(new SystemMessage(SystemMessageId.THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY));
				return;
			}
		}
		
		player.sitDown();
		player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_BUY);
		player.broadcastUserInfo();
		player.broadcastPacket(new PrivateStoreMsgBuy(player));
	}
	
	private static class Item
	{
		private final int itemId;
		private final int count;
		private final int price;
		private final int enchant;
		
		public Item(int id, int num, int pri, int ench)
		{
			itemId = id;
			count = num;
			price = pri;
			enchant = ench;
		}
		
		public boolean addToTradeList(TradeList list)
		{
			if ((Integer.MAX_VALUE / count) < price)
			{
				return false;
			}
			
			list.addItemByItemId(itemId, count, price, enchant);
			
			return true;
		}
		
		public long getCost()
		{
			return count * price;
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 91 SetPrivateStoreListBuy";
	}
	
}