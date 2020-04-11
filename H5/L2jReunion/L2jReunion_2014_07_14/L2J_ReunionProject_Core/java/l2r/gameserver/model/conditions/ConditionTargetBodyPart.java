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
import l2r.gameserver.model.items.L2Armor;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.stats.Env;

/**
 * The Class ConditionTargetBodyPart.
 * @author mkizub
 */
public class ConditionTargetBodyPart extends Condition
{
	private final L2Armor _armor;
	
	/**
	 * Instantiates a new condition target body part.
	 * @param armor the armor
	 */
	public ConditionTargetBodyPart(L2Armor armor)
	{
		_armor = armor;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		// target is attacker
		if (env.getTarget() == null)
		{
			return true;
		}
		int bodypart = env.getTarget().getAttackingBodyPart();
		int armor_part = _armor.getBodyPart();
		switch (bodypart)
		{
			case Inventory.PAPERDOLL_CHEST:
				return (armor_part & (L2Item.SLOT_CHEST | L2Item.SLOT_FULL_ARMOR | L2Item.SLOT_UNDERWEAR)) != 0;
			case Inventory.PAPERDOLL_LEGS:
				return (armor_part & (L2Item.SLOT_LEGS | L2Item.SLOT_FULL_ARMOR)) != 0;
			case Inventory.PAPERDOLL_HEAD:
				return (armor_part & L2Item.SLOT_HEAD) != 0;
			case Inventory.PAPERDOLL_FEET:
				return (armor_part & L2Item.SLOT_FEET) != 0;
			case Inventory.PAPERDOLL_GLOVES:
				return (armor_part & L2Item.SLOT_GLOVES) != 0;
			default:
				return true;
		}
	}
}
