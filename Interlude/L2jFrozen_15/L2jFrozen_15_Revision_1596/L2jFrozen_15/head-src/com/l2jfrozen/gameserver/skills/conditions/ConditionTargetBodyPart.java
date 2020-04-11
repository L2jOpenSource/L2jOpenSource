package com.l2jfrozen.gameserver.skills.conditions;

import com.l2jfrozen.gameserver.model.Inventory;
import com.l2jfrozen.gameserver.skills.Env;
import com.l2jfrozen.gameserver.templates.L2Armor;
import com.l2jfrozen.gameserver.templates.L2Item;

/**
 * @author mkizub
 */
public class ConditionTargetBodyPart extends Condition
{
	private final L2Armor armor;
	
	public ConditionTargetBodyPart(final L2Armor armor)
	{
		this.armor = armor;
	}
	
	@Override
	public boolean testImpl(final Env env)
	{
		// target is attacker
		if (env.target == null)
		{
			return true;
		}
		final int bodypart = env.target.getAttackingBodyPart();
		final int armor_part = armor.getBodyPart();
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
