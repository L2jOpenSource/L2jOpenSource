package com.l2jfrozen.gameserver.skills.conditions;

import com.l2jfrozen.gameserver.model.Inventory;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.skills.Env;
import com.l2jfrozen.gameserver.templates.L2ArmorType;
import com.l2jfrozen.gameserver.templates.L2Item;

/**
 * The Class ConditionUsingItemType.
 * @author mkizub
 */
public final class ConditionUsingItemType extends Condition
{
	private final boolean armor;
	private final int mask;
	
	/**
	 * Instantiates a new condition using item type.
	 * @param mask the mask
	 */
	public ConditionUsingItemType(final int mask)
	{
		this.mask = mask;
		armor = (this.mask & (L2ArmorType.MAGIC.mask() | L2ArmorType.LIGHT.mask() | L2ArmorType.HEAVY.mask())) != 0;
	}
	
	@Override
	public boolean testImpl(final Env env)
	{
		if (!(env.player instanceof L2PcInstance))
		{
			return false;
		}
		final Inventory inv = ((L2PcInstance) env.player).getInventory();
		
		// If ConditionUsingItemType is one between Light, Heavy or Magic
		if (armor)
		{
			// Get the itemMask of the weared chest (if exists)
			final L2ItemInstance chest = inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
			if (chest == null)
			{
				return false;
			}
			final int chestMask = chest.getItem().getItemMask();
			
			// If chest armor is different from the condition one return false
			if ((mask & chestMask) == 0)
			{
				return false;
			}
			
			// So from here, chest armor matches conditions
			
			final int chestBodyPart = chest.getItem().getBodyPart();
			// return True if chest armor is a Full Armor
			if (chestBodyPart == L2Item.SLOT_FULL_ARMOR)
			{
				return true;
			}
			
			final L2ItemInstance legs = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
			if (legs == null)
			{
				return false;
			}
			final int legMask = legs.getItem().getItemMask();
			// return true if legs armor matches too
			return (mask & legMask) != 0;
		}
		return (mask & inv.getWearedMask()) != 0;
	}
}
