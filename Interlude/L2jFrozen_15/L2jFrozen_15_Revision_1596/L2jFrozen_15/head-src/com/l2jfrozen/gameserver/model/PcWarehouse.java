package com.l2jfrozen.gameserver.model;

import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance.ItemLocation;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public class PcWarehouse extends Warehouse
{
	// private static final Logger LOGGER = Logger.getLogger(PcWarehouse.class);
	
	private final L2PcInstance owner;
	
	public PcWarehouse(final L2PcInstance owner)
	{
		this.owner = owner;
	}
	
	@Override
	public L2PcInstance getOwner()
	{
		return owner;
	}
	
	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.WAREHOUSE;
	}
	
	public String getLocationId()
	{
		return "0";
	}
	
	public int getLocationId(final boolean dummy)
	{
		return 0;
	}
	
	public void setLocationId(final L2PcInstance dummy)
	{
		return;
	}
	
	@Override
	public boolean validateCapacity(final int slots)
	{
		return itemsList.size() + slots <= owner.GetWareHouseLimit();
	}
}
