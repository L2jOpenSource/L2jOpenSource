
package com.l2jfrozen.gameserver.model;

import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Luno
 */
public final class L2ArmorSet
{
	private final int chestArmor;
	private final int legsArmor;
	private final int headArmor;
	private final int glovesArmor;
	private final int feetArmor;
	private final int skillIdArmor;
	
	private final int shieldArmor;
	private final int shieldSkillIdArmor;
	
	private final int enchant6SkillArmor;
	
	private boolean custom = false;
	
	public L2ArmorSet(final int chest, final int legs, final int head, final int gloves, final int feet, final int skill_id, final int shield, final int shield_skill_id, final int enchant6skill)
	{
		chestArmor = chest;
		legsArmor = legs;
		headArmor = head;
		glovesArmor = gloves;
		feetArmor = feet;
		skillIdArmor = skill_id;
		
		shieldArmor = shield;
		shieldSkillIdArmor = shield_skill_id;
		
		enchant6SkillArmor = enchant6skill;
	}
	
	/**
	 * Checks if player have equiped all items from set (not checking shield)
	 * @param  player whose inventory is being checked
	 * @return        True if player equips whole set
	 */
	public boolean containAll(final L2PcInstance player)
	{
		final Inventory inv = player.getInventory();
		
		L2ItemInstance legsItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
		L2ItemInstance headItem = inv.getPaperdollItem(Inventory.PAPERDOLL_HEAD);
		L2ItemInstance glovesItem = inv.getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
		L2ItemInstance feetItem = inv.getPaperdollItem(Inventory.PAPERDOLL_FEET);
		
		int legs = 0;
		int head = 0;
		int gloves = 0;
		int feet = 0;
		
		if (legsItem != null)
		{
			legs = legsItem.getItemId();
		}
		if (headItem != null)
		{
			head = headItem.getItemId();
		}
		if (glovesItem != null)
		{
			gloves = glovesItem.getItemId();
		}
		if (feetItem != null)
		{
			feet = feetItem.getItemId();
		}
		
		legsItem = null;
		headItem = null;
		glovesItem = null;
		feetItem = null;
		
		return containAll(chestArmor, legs, head, gloves, feet);
		
	}
	
	public boolean containAll(final int chest, final int legs, final int head, final int gloves, final int feet)
	{
		if (chestArmor != 0 && chestArmor != chest)
		{
			return false;
		}
		if (legsArmor != 0 && legsArmor != legs)
		{
			return false;
		}
		if (headArmor != 0 && headArmor != head)
		{
			return false;
		}
		if (glovesArmor != 0 && glovesArmor != gloves)
		{
			return false;
		}
		if (feetArmor != 0 && feetArmor != feet)
		{
			return false;
		}
		
		return true;
	}
	
	public boolean containItem(final int slot, final int itemId)
	{
		switch (slot)
		{
			case Inventory.PAPERDOLL_CHEST:
				return chestArmor == itemId;
			case Inventory.PAPERDOLL_LEGS:
				return legsArmor == itemId;
			case Inventory.PAPERDOLL_HEAD:
				return headArmor == itemId;
			case Inventory.PAPERDOLL_GLOVES:
				return glovesArmor == itemId;
			case Inventory.PAPERDOLL_FEET:
				return feetArmor == itemId;
			default:
				return false;
		}
	}
	
	public int getSkillId()
	{
		return skillIdArmor;
	}
	
	public boolean containShield(final L2PcInstance player)
	{
		Inventory inv = player.getInventory();
		
		L2ItemInstance shieldItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		
		if (shieldItem != null && shieldItem.getItemId() == shieldArmor)
		{
			return true;
		}
		
		inv = null;
		shieldItem = null;
		
		return false;
	}
	
	public boolean containShield(final int shield_id)
	{
		if (shieldArmor == 0)
		{
			return false;
		}
		
		return shieldArmor == shield_id;
	}
	
	public int getShieldSkillId()
	{
		return shieldSkillIdArmor;
	}
	
	public int getEnchant6skillId()
	{
		return enchant6SkillArmor;
	}
	
	/**
	 * Checks if all parts of set are enchanted to +6 or more
	 * @param  player
	 * @return
	 */
	public boolean isEnchanted6(final L2PcInstance player)
	{
		// Player don't have full set
		if (!containAll(player))
		{
			return false;
		}
		
		final Inventory inv = player.getInventory();
		
		L2ItemInstance chestItem = inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		L2ItemInstance legsItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
		L2ItemInstance headItem = inv.getPaperdollItem(Inventory.PAPERDOLL_HEAD);
		L2ItemInstance glovesItem = inv.getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
		L2ItemInstance feetItem = inv.getPaperdollItem(Inventory.PAPERDOLL_FEET);
		
		if (chestItem.getEnchantLevel() < 6)
		{
			return false;
		}
		if (legsArmor != 0 && legsItem.getEnchantLevel() < 6)
		{
			return false;
		}
		if (glovesArmor != 0 && glovesItem.getEnchantLevel() < 6)
		{
			return false;
		}
		if (headArmor != 0 && headItem.getEnchantLevel() < 6)
		{
			return false;
		}
		if (feetArmor != 0 && feetItem.getEnchantLevel() < 6)
		{
			return false;
		}
		
		chestItem = null;
		legsItem = null;
		headItem = null;
		glovesItem = null;
		feetItem = null;
		
		return true;
	}
	
	public boolean isCustom()
	{
		return custom;
	}
	
	public void setIsCustom(boolean isCustom)
	{
		custom = isCustom;
	}
}
