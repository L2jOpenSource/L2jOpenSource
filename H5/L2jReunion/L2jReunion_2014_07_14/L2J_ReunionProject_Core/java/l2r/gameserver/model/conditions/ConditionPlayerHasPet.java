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

import java.util.ArrayList;

import l2r.gameserver.model.actor.instance.L2PetInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.stats.Env;

/**
 * The Class ConditionPlayerHasPet.
 */
public class ConditionPlayerHasPet extends Condition
{
	private final ArrayList<Integer> _controlItemIds;
	
	/**
	 * Instantiates a new condition player has pet.
	 * @param itemIds the item ids
	 */
	public ConditionPlayerHasPet(ArrayList<Integer> itemIds)
	{
		if ((itemIds.size() == 1) && (itemIds.get(0) == 0))
		{
			_controlItemIds = null;
		}
		else
		{
			_controlItemIds = itemIds;
		}
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if ((env.getPlayer() == null) || (!(env.getPlayer().getSummon() instanceof L2PetInstance)))
		{
			return false;
		}
		
		if (_controlItemIds == null)
		{
			return true;
		}
		
		final L2ItemInstance controlItem = ((L2PetInstance) env.getPlayer().getSummon()).getControlItem();
		return (controlItem != null) && _controlItemIds.contains(controlItem.getId());
	}
}
