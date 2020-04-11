package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.TradeList;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.PrivateStoreManageListSell;
import com.l2jfrozen.gameserver.network.serverpackets.PrivateStoreMsgSell;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public class SetPrivateStoreListSell extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 12; // length of the one item
	private int listSellCount;
	private boolean packageSale;
	private Item[] items;
	
	@Override
	protected void readImpl()
	{
		packageSale = readD() == 1;
		listSellCount = readD();
		
		if (listSellCount <= 0 || listSellCount * BATCH_LENGTH > buf.remaining() || listSellCount > Config.MAX_ITEM_IN_PACKET)
		{
			return;
		}
		
		items = new Item[listSellCount];
		
		for (int i = 0; i < listSellCount; i++)
		{
			int itemId = readD();
			long cnt = readD();
			int price = readD();
			
			if (itemId < 1 || cnt < 1 || price < 0)
			{
				items = null;
				return;
			}
			
			items[i] = new Item(itemId, (int) cnt, price);
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
			player.sendPacket(new PrivateStoreManageListSell(player));
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disable for your Access Level");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isMoving())
		{
			player.sendPacket(SystemMessageId.CANNOT_OPEN_A_PRIVATE_STORE);
			player.sendPacket(new PrivateStoreManageListSell(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isCastingNow() || player.isCastingPotionNow())
		{
			player.sendPacket(SystemMessageId.A_PRIVATE_STORE_MAY_NOT_BE_OPENED_WHILE_USING_A_SKILL);
			player.sendPacket(new PrivateStoreManageListSell(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInCombat())
		{
			player.sendPacket(SystemMessageId.A_PRIVATE_STORE_MAY_NOT_BE_OPENED_WHILE_USING_A_SKILL);
			player.sendPacket(new PrivateStoreManageListSell(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isTradeDisabled())
		{
			player.sendMessage("Trade are disable here. Try in another place.");
			player.sendPacket(new PrivateStoreManageListSell(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isMovementDisabled() || player.inObserverMode() || player.getActiveEnchantItem() != null)
		{
			player.sendMessage("You cannot start store now..");
			player.sendPacket(new PrivateStoreManageListSell(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInsideZone(L2Character.ZONE_NOSTORE))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_OPEN_A_PRIVATE_STORE_HERE);
			player.sendPacket(new PrivateStoreManageListSell(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		TradeList tradeList = player.getSellList();
		tradeList.clear();
		tradeList.setPackaged(packageSale);
		
		long totalCost = player.getAdena();
		
		for (Item item : items)
		{
			totalCost += item.getPrice();
			
			if (totalCost > Integer.MAX_VALUE || totalCost < 0)
			{
				player.sendPacket(new PrivateStoreManageListSell(player));
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
				return;
			}
			
			if (!item.addToTradeList(tradeList))
			{
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
				player.sendPacket(new PrivateStoreManageListSell(player));
				return;
			}
		}
		
		if (listSellCount <= 0)
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			player.broadcastUserInfo();
			return;
		}
		
		if (player.isProcessingTransaction())
		{
			player.sendPacket(new PrivateStoreManageListSell(player));
			player.sendMessage("Store mode are disable while trading.");
			return;
		}
		
		// Check maximum number of allowed slots for pvt shops
		if (listSellCount > player.GetPrivateSellStoreLimit())
		{
			player.sendPacket(new PrivateStoreManageListSell(player));
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
			return;
		}
		
		player.sitDown();
		player.setPrivateStoreType(packageSale ? L2PcInstance.STORE_PRIVATE_PACKAGE_SELL : L2PcInstance.STORE_PRIVATE_SELL);
		player.broadcastUserInfo();
		player.broadcastPacket(new PrivateStoreMsgSell(player));
	}
	
	private static class Item
	{
		private final int itemId;
		private final int count;
		private final int price;
		
		public Item(int id, int num, int pri)
		{
			itemId = id;
			count = num;
			price = pri;
		}
		
		public boolean addToTradeList(TradeList list)
		{
			if ((Integer.MAX_VALUE / count) < price)
			{
				return false;
			}
			
			list.addItem(itemId, count, price);
			return true;
		}
		
		public long getPrice()
		{
			return count * price;
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 74 SetPrivateStoreListSell";
	}
}