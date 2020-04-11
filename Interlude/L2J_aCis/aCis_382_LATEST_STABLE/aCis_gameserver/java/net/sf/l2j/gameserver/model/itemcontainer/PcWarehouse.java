package net.sf.l2j.gameserver.model.itemcontainer;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance.ItemLocation;

public class PcWarehouse extends ItemContainer
{
	private final Player _owner;
	
	public PcWarehouse(Player owner)
	{
		_owner = owner;
	}
	
	@Override
	public String getName()
	{
		return "Warehouse";
	}
	
	@Override
	public Player getOwner()
	{
		return _owner;
	}
	
	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.WAREHOUSE;
	}
	
	@Override
	public boolean validateCapacity(int slots)
	{
		return _items.size() + slots <= _owner.getWareHouseLimit();
	}
}