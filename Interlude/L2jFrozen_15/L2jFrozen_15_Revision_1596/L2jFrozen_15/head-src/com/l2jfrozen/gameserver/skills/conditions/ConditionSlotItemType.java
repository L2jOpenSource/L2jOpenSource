package com.l2jfrozen.gameserver.skills.conditions;

import com.l2jfrozen.gameserver.model.Inventory;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author mkizub
 */
public final class ConditionSlotItemType extends ConditionInventory
{
	
	private final int mask;
	
	public ConditionSlotItemType(final int slot, final int mask)
	{
		super(slot);
		this.mask = mask;
	}
	
	@Override
	public boolean testImpl(final Env env)
	{
		if (!(env.player instanceof L2PcInstance))
		{
			return false;
		}
		final Inventory inv = ((L2PcInstance) env.player).getInventory();
		final L2ItemInstance item = inv.getPaperdollItem(slot);
		if (item == null)
		{
			return false;
		}
		return (item.getItem().getItemMask() & mask) != 0;
	}
}
