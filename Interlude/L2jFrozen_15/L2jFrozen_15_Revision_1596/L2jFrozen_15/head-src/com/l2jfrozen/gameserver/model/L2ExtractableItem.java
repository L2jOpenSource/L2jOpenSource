package com.l2jfrozen.gameserver.model;

import java.util.List;

/**
 * @author -Nemesiss-
 */
public class L2ExtractableItem
{
	private final int itemId;
	private final List<L2ExtractableProductItem> products;
	
	public L2ExtractableItem(final int itemid, final List<L2ExtractableProductItem> products)
	{
		itemId = itemid;
		this.products = products;
	}
	
	public int getItemId()
	{
		return itemId;
	}
	
	public List<L2ExtractableProductItem> getProductItems()
	{
		return products;
	}
}
