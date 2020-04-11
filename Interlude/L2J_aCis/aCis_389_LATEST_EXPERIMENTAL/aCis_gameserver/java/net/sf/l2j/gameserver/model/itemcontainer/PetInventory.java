package net.sf.l2j.gameserver.model.itemcontainer;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.items.EtcItemType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance.ItemLocation;

public class PetInventory extends Inventory
{
	private final Pet _owner;
	
	public PetInventory(Pet owner)
	{
		_owner = owner;
	}
	
	@Override
	public Pet getOwner()
	{
		return _owner;
	}
	
	@Override
	public int getOwnerId()
	{
		int id;
		try
		{
			id = _owner.getOwner().getObjectId();
		}
		catch (NullPointerException e)
		{
			return 0;
		}
		return id;
	}
	
	@Override
	protected void refreshWeight()
	{
		super.refreshWeight();
		
		getOwner().updateAndBroadcastStatus(1);
		getOwner().sendPetInfosToOwner();
	}
	
	public boolean validateCapacity(ItemInstance item)
	{
		int slots = 0;
		
		if (!(item.isStackable() && getItemByItemId(item.getItemId()) != null) && item.getItemType() != EtcItemType.HERB)
			slots++;
		
		return validateCapacity(slots);
	}
	
	@Override
	public boolean validateCapacity(int slots)
	{
		return _items.size() + slots <= _owner.getInventoryLimit();
	}
	
	public boolean validateWeight(ItemInstance item, int count)
	{
		return validateWeight(count * item.getItem().getWeight());
	}
	
	@Override
	public boolean validateWeight(int weight)
	{
		return _totalWeight + weight <= _owner.getMaxLoad();
	}
	
	@Override
	protected ItemLocation getBaseLocation()
	{
		return ItemLocation.PET;
	}
	
	@Override
	protected ItemLocation getEquipLocation()
	{
		return ItemLocation.PET_EQUIP;
	}
	
	@Override
	public void deleteMe()
	{
		final Player petOwner = getOwner().getOwner();
		if (petOwner != null)
		{
			for (ItemInstance item : _items)
			{
				if (petOwner.getInventory().validateCapacity(1))
					getOwner().transferItem("return", item.getObjectId(), item.getCount(), petOwner.getInventory(), petOwner, getOwner());
				else
				{
					final ItemInstance droppedItem = dropItem("drop", item.getObjectId(), item.getCount(), petOwner, getOwner());
					droppedItem.dropMe(getOwner(), getOwner().getX() + Rnd.get(-70, 70), getOwner().getY() + Rnd.get(-70, 70), getOwner().getZ() + 30);
				}
				
			}
		}
		_items.clear();
	}
}