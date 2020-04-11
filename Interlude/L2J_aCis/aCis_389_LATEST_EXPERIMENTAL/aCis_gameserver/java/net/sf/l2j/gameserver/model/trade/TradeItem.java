package net.sf.l2j.gameserver.model.trade;

import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;

public class TradeItem
{
	private int _objectId;
	private final Item _item;
	private int _enchant;
	private int _count;
	private int _price;
	
	public TradeItem(ItemInstance item, int count, int price)
	{
		_objectId = item.getObjectId();
		_item = item.getItem();
		_enchant = item.getEnchantLevel();
		_count = count;
		_price = price;
	}
	
	public TradeItem(Item item, int count, int price, int enchant)
	{
		_objectId = 0;
		_item = item;
		_enchant = enchant;
		_count = count;
		_price = price;
	}
	
	public TradeItem(TradeItem item, int count, int price)
	{
		_objectId = item.getObjectId();
		_item = item.getItem();
		_enchant = item.getEnchant();
		_count = count;
		_price = price;
	}
	
	public int getObjectId()
	{
		return _objectId;
	}
	
	public void setObjectId(int objectId)
	{
		_objectId = objectId;
	}
	
	public Item getItem()
	{
		return _item;
	}
	
	public int getEnchant()
	{
		return _enchant;
	}
	
	public void setEnchant(int enchant)
	{
		_enchant = enchant;
	}
	
	public int getCount()
	{
		return _count;
	}
	
	public void setCount(int count)
	{
		_count = count;
	}
	
	public int getPrice()
	{
		return _price;
	}
	
	public void setPrice(int price)
	{
		_price = price;
	}
}