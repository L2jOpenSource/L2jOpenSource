package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.enums.actors.OperateType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.trade.TradeList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreManageListBuy;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreMsgBuy;

public final class SetPrivateStoreListBuy extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 16; // length of one item
	
	private Item[] _items = null;
	
	@Override
	protected void readImpl()
	{
		int count = readD();
		if (count < 1 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
			return;
		
		_items = new Item[count];
		for (int i = 0; i < count; i++)
		{
			int itemId = readD();
			int enchant = readH();
			readH(); // TODO analyse this
			int cnt = readD();
			int price = readD();
			
			if (itemId < 1 || cnt < 1 || price < 0 || enchant < 0 || enchant > 65535)
			{
				_items = null;
				return;
			}
			_items[i] = new Item(itemId, cnt, price, enchant);
		}
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		final TradeList tradeList = player.getBuyList();
		tradeList.clear();
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		// Check multiple conditions. Message is sent directly from the method.
		if (!player.canOpenPrivateStore(false))
			return;
		
		// Integrity check.
		if (_items == null)
		{
			player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT);
			player.sendPacket(new PrivateStoreManageListBuy(player));
			return;
		}
		
		// Check maximum number of allowed slots for pvt shops
		if (_items.length > player.getPrivateBuyStoreLimit())
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
			player.sendPacket(new PrivateStoreManageListBuy(player));
			return;
		}
		
		int totalCost = 0;
		for (Item i : _items)
		{
			if (!i.addToTradeList(tradeList))
			{
				player.sendPacket(SystemMessageId.EXCEEDED_THE_MAXIMUM);
				player.sendPacket(new PrivateStoreManageListBuy(player));
				return;
			}
			
			totalCost += i.getCost();
			if (totalCost > Integer.MAX_VALUE)
			{
				player.sendPacket(SystemMessageId.EXCEEDED_THE_MAXIMUM);
				player.sendPacket(new PrivateStoreManageListBuy(player));
				return;
			}
		}
		
		// Check for available funds
		if (totalCost > player.getAdena())
		{
			player.sendPacket(SystemMessageId.THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY);
			player.sendPacket(new PrivateStoreManageListBuy(player));
			return;
		}
		
		player.sitDown();
		player.setOperateType(OperateType.BUY);
		player.broadcastUserInfo();
		player.broadcastPacket(new PrivateStoreMsgBuy(player));
	}
	
	private static class Item
	{
		private final int _itemId, _count, _price, _enchant;
		
		public Item(int id, int num, int pri, int enc)
		{
			_itemId = id;
			_count = num;
			_price = pri;
			_enchant = enc;
		}
		
		public boolean addToTradeList(TradeList list)
		{
			if ((Integer.MAX_VALUE / _count) < _price)
				return false;
			
			list.addItemByItemId(_itemId, _count, _price, _enchant);
			return true;
		}
		
		public long getCost()
		{
			return _count * _price;
		}
	}
}