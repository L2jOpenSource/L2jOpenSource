package com.l2jfrozen.gameserver.model;

/**
 *
 */
public class ItemRequest
{
	int objectId;
	int itemId;
	int enchant;
	int count;
	int price;
	
	public ItemRequest(final int objectId, final int count, final int price)
	{
		this.objectId = objectId;
		this.count = count;
		this.price = price;
	}
	
	public ItemRequest(final int objectId, final int itemId, final int count, final int price)
	{
		this.objectId = objectId;
		this.itemId = itemId;
		this.count = count;
		this.price = price;
	}
	
	public ItemRequest(final int objectId, final int itemId, final int enchant, final int count, final int price)
	{
		this.objectId = objectId;
		this.itemId = itemId;
		this.count = count;
		this.enchant = enchant;
		this.price = price;
	}
	
	public int getObjectId()
	{
		return objectId;
	}
	
	public int getItemId()
	{
		return itemId;
	}
	
	public void setCount(final int count)
	{
		this.count = count;
	}
	
	public int getCount()
	{
		return count;
	}
	
	public int getPrice()
	{
		return price;
	}
	
	public int getEnchant()
	{
		return enchant;
	}
	
	/**
	 * @param enchant
	 */
	public void setEnchant(final int enchant)
	{
		this.enchant = enchant;
	}
}