package com.l2jfrozen.gameserver.model;

/**
 * This class ...
 * @version $Revision: 1.2.4.1 $ $Date: 2005/03/27 15:29:32 $
 */
public final class TradeItem
{
	private int objectId;
	private int itemId;
	private int price;
	private int storePrice;
	private int count;
	private int enchantLevel;
	
	public TradeItem()
	{
		// null
	}
	
	public void setObjectId(final int id)
	{
		objectId = id;
	}
	
	public int getObjectId()
	{
		return objectId;
	}
	
	public void setItemId(final int id)
	{
		itemId = id;
	}
	
	public int getItemId()
	{
		return itemId;
	}
	
	public void setOwnersPrice(final int price)
	{
		this.price = price;
	}
	
	public int getOwnersPrice()
	{
		return price;
	}
	
	public void setstorePrice(final int price)
	{
		storePrice = price;
	}
	
	public int getStorePrice()
	{
		return storePrice;
	}
	
	public void setCount(final int count)
	{
		this.count = count;
	}
	
	public int getCount()
	{
		return count;
	}
	
	public void setEnchantLevel(final int enchant)
	{
		enchantLevel = enchant;
	}
	
	public int getEnchantLevel()
	{
		return enchantLevel;
	}
	
}
