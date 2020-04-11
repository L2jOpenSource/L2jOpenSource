package com.l2jfrozen.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance.ItemLocation;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public class PcFreight extends ItemContainer
{
	// private static final Logger LOGGER = Logger.getLogger(PcFreight.class);
	
	private final L2PcInstance owner; // This is the L2PcInstance that owns this Freight;
	private int activeLocationId;
	
	public PcFreight(final L2PcInstance owner)
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
		return ItemLocation.FREIGHT;
	}
	
	public void setActiveLocation(final int locationId)
	{
		activeLocationId = locationId;
	}
	
	public int getactiveLocation()
	{
		return activeLocationId;
	}
	
	/**
	 * Returns the quantity of items in the inventory
	 * @return int
	 */
	@Override
	public int getSize()
	{
		int size = 0;
		
		for (final L2ItemInstance item : itemsList)
		{
			if (item.getEquipSlot() == 0 || activeLocationId == 0 || item.getEquipSlot() == activeLocationId)
			{
				size++;
			}
		}
		return size;
	}
	
	/**
	 * Returns the list of items in inventory
	 * @return L2ItemInstance : items in inventory
	 */
	@Override
	public L2ItemInstance[] getItems()
	{
		List<L2ItemInstance> list = new ArrayList<>();
		
		for (L2ItemInstance item : itemsList)
		{
			if (item.getEquipSlot() == 0 || item.getEquipSlot() == activeLocationId)
			{
				list.add(item);
			}
		}
		
		return list.toArray(new L2ItemInstance[list.size()]);
	}
	
	/**
	 * Returns the item from inventory by using its <B>itemId</B>
	 * @param  itemId : int designating the ID of the item
	 * @return        L2ItemInstance designating the item or null if not found in inventory
	 */
	@Override
	public L2ItemInstance getItemByItemId(final int itemId)
	{
		for (final L2ItemInstance item : itemsList)
		{
			if (item.getItemId() == itemId && (item.getEquipSlot() == 0 || activeLocationId == 0 || item.getEquipSlot() == activeLocationId))
			{
				return item;
			}
		}
		
		return null;
	}
	
	/**
	 * Adds item to PcFreight for further adjustments.
	 * @param item : L2ItemInstance to be added from inventory
	 */
	@Override
	protected void addItem(final L2ItemInstance item)
	{
		super.addItem(item);
		if (activeLocationId > 0)
		{
			item.setLocation(item.getLocation(), activeLocationId);
		}
	}
	
	/**
	 * Get back items in PcFreight from database
	 */
	@Override
	public void restore()
	{
		final int locationId = activeLocationId;
		activeLocationId = 0;
		super.restore();
		activeLocationId = locationId;
	}
	
	@Override
	public boolean validateCapacity(final int slots)
	{
		return getSize() + slots <= owner.GetFreightLimit();
	}
}
