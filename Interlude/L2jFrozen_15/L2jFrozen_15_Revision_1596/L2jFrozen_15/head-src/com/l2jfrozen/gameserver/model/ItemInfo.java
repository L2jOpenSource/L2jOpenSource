package com.l2jfrozen.gameserver.model;

import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.templates.L2Item;

/**
 * Get all information from L2ItemInstance to generate ItemInfo.<BR>
 * <BR>
 */
public class ItemInfo
{
	/** Identifier of the L2ItemInstance */
	private int objectId;
	
	/** The L2Item template of the L2ItemInstance */
	private L2Item itemI;
	
	/** The level of enchant on the L2ItemInstance */
	private int enchant;
	
	/** The augmentation of the item */
	private int augmentation;
	
	/** The quantity of L2ItemInstance */
	private int count;
	
	/** The price of the L2ItemInstance */
	private int price;
	
	/** The custom L2ItemInstance types (used loto, race tickets) */
	private int type1;
	private int type2;
	
	/** If True the L2ItemInstance is equipped */
	private int equipped;
	
	/** The action to do clientside (1=ADD, 2=MODIFY, 3=REMOVE) */
	private int change;
	
	/** The mana of this item */
	private int mana;
	
	/**
	 * Get all information from L2ItemInstance to generate ItemInfo.<BR>
	 * <BR>
	 * @param item
	 */
	public ItemInfo(final L2ItemInstance item)
	{
		if (item == null)
		{
			return;
		}
		
		// Get the Identifier of the L2ItemInstance
		objectId = item.getObjectId();
		
		// Get the L2Item of the L2ItemInstance
		itemI = item.getItem();
		
		// Get the enchant level of the L2ItemInstance
		enchant = item.getEnchantLevel();
		
		// Get the augmentation boni
		if (item.isAugmented())
		{
			augmentation = item.getAugmentation().getAugmentationId();
		}
		else
		{
			augmentation = 0;
		}
		
		// Get the quantity of the L2ItemInstance
		count = item.getCount();
		
		// Get custom item types (used loto, race tickets)
		type1 = item.getCustomType1();
		type2 = item.getCustomType2();
		
		// Verify if the L2ItemInstance is equipped
		equipped = item.isEquipped() ? 1 : 0;
		
		// Get the action to do clientside
		switch (item.getLastChange())
		{
			case L2ItemInstance.ADDED:
			{
				change = 1;
				break;
			}
			case L2ItemInstance.MODIFIED:
			{
				change = 2;
				break;
			}
			case L2ItemInstance.REMOVED:
			{
				change = 3;
				break;
			}
		}
		
		// Get shadow item mana
		mana = item.getMana();
	}
	
	public ItemInfo(final L2ItemInstance item, final int change)
	{
		if (item == null)
		{
			return;
		}
		
		// Get the Identifier of the L2ItemInstance
		objectId = item.getObjectId();
		
		// Get the L2Item of the L2ItemInstance
		itemI = item.getItem();
		
		// Get the enchant level of the L2ItemInstance
		enchant = item.getEnchantLevel();
		
		// Get the augmentation boni
		if (item.isAugmented())
		{
			augmentation = item.getAugmentation().getAugmentationId();
		}
		else
		{
			augmentation = 0;
		}
		
		// Get the quantity of the L2ItemInstance
		count = item.getCount();
		
		// Get custom item types (used loto, race tickets)
		type1 = item.getCustomType1();
		type2 = item.getCustomType2();
		
		// Verify if the L2ItemInstance is equipped
		equipped = item.isEquipped() ? 1 : 0;
		
		// Get the action to do clientside
		this.change = change;
		
		// Get shadow item mana
		mana = item.getMana();
	}
	
	public int getObjectId()
	{
		return objectId;
	}
	
	public L2Item getItem()
	{
		return itemI;
	}
	
	public int getEnchant()
	{
		return enchant;
	}
	
	public int getAugemtationBoni()
	{
		return augmentation;
	}
	
	public int getCount()
	{
		return count;
	}
	
	public int getPrice()
	{
		return price;
	}
	
	public int getCustomType1()
	{
		return type1;
	}
	
	public int getCustomType2()
	{
		return type2;
	}
	
	public int getEquipped()
	{
		return equipped;
	}
	
	public int getChange()
	{
		return change;
	}
	
	public int getMana()
	{
		return mana;
	}
}
