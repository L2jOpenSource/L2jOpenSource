/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.conditions;

import l2r.gameserver.model.itemcontainer.Inventory;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.stats.Env;

/**
 * The Class ConditionSlotItemType.
 * @author mkizub
 */
public final class ConditionSlotItemType extends ConditionInventory
{
	private final int _mask;
	
	/**
	 * Instantiates a new condition slot item type.
	 * @param slot the slot
	 * @param mask the mask
	 */
	public ConditionSlotItemType(int slot, int mask)
	{
		super(slot);
		_mask = mask;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (env.getPlayer() == null)
		{
			return false;
		}
		final Inventory inv = env.getPlayer().getInventory();
		final L2ItemInstance item = inv.getPaperdollItem(_slot);
		if (item == null)
		{
			return false;
		}
		return (item.getItem().getItemMask() & _mask) != 0;
	}
}
