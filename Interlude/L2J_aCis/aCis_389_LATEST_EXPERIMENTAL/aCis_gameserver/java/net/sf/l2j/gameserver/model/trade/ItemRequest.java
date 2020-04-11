package net.sf.l2j.gameserver.model.trade;

public class ItemRequest
{
	private int _objectId;
	private int _itemId;
	private int _count;
	private int _price;
	private int _enchant;
	
	public ItemRequest(int objectId, int count, int price)
	{
		_objectId = objectId;
		_count = count;
		_price = price;
	}
	
	public ItemRequest(int objectId, int itemId, int count, int price, int enchant)
	{
		_objectId = objectId;
		_itemId = itemId;
		_count = count;
		_price = price;
		_enchant = enchant;
	}
	
	public int getObjectId()
	{
		return _objectId;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public void setCount(int count)
	{
		_count = count;
	}
	
	public int getCount()
	{
		return _count;
	}
	
	public int getPrice()
	{
		return _price;
	}
	
	public int getEnchantLevel()
	{
		return _enchant;
	}
}