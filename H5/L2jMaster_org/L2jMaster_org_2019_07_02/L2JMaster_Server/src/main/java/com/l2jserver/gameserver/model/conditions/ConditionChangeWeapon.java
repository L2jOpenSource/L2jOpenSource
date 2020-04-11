/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.model.conditions;

import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.items.L2Weapon;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * The Class ConditionChangeWeapon.
 * @author nBd
 */
public class ConditionChangeWeapon extends Condition
{
	private final boolean _required;
	
	/**
	 * Instantiates a new condition change weapon.
	 * @param required the required
	 */
	public ConditionChangeWeapon(boolean required)
	{
		_required = required;
	}
	
	/**
	 * Test impl.
	 * @return true, if successful
	 */
	@Override
	public boolean testImpl(L2Character effector, L2Character effected, Skill skill, L2Item item)
	{
		if (effector.getActingPlayer() == null)
		{
			return false;
		}
		
		if (_required)
		{
			final L2Weapon weaponItem = effector.getActiveWeaponItem();
			if (weaponItem == null)
			{
				return false;
			}
			
			if (weaponItem.getChangeWeaponId() == 0)
			{
				return false;
			}
			
			if (effector.getActingPlayer().isEnchanting())
			{
				return false;
			}
		}
		return true;
	}
}
