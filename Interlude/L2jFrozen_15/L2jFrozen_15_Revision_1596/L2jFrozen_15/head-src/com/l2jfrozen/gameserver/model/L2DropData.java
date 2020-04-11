package com.l2jfrozen.gameserver.model;

import java.util.Arrays;

/**
 * /* Special thanks to nuocnam Author: LittleVexy
 * @version $Revision: 1.1.4.4 $ $Date: 2005/03/29 23:15:15 $
 */
public class L2DropData
{
	public static final int MAX_CHANCE = 1000000;
	
	private int itemId;
	private int minDrop;
	private int maxDrop;
	private int chance;
	private String questID = null;
	private String[] stateID = null;
	private boolean customDrop = false;
	
	/**
	 * Returns the ID of the item dropped
	 * @return int
	 */
	public int getItemId()
	{
		return itemId;
	}
	
	/**
	 * Sets the ID of the item dropped
	 * @param itemId : int designating the ID of the item
	 */
	public void setItemId(final int itemId)
	{
		this.itemId = itemId;
	}
	
	/**
	 * Returns the minimum quantity of items dropped
	 * @return int
	 */
	public int getMinDrop()
	{
		return minDrop;
	}
	
	/**
	 * Returns the maximum quantity of items dropped
	 * @return int
	 */
	public int getMaxDrop()
	{
		return maxDrop;
	}
	
	/**
	 * Returns the chance of having a drop
	 * @return int
	 */
	public int getChance()
	{
		return chance;
	}
	
	/**
	 * Sets the value for minimal quantity of dropped items
	 * @param mindrop : int designating the quantity
	 */
	public void setMinDrop(final int mindrop)
	{
		minDrop = mindrop;
	}
	
	/**
	 * Sets the value for maximal quantity of dopped items
	 * @param maxdrop : int designating the quantity of dropped items
	 */
	public void setMaxDrop(final int maxdrop)
	{
		maxDrop = maxdrop;
	}
	
	/**
	 * Sets the chance of having the item for a drop
	 * @param chance : int designating the chance
	 */
	public void setChance(final int chance)
	{
		this.chance = chance;
	}
	
	/**
	 * Returns the stateID.
	 * @return String[]
	 */
	public String[] getStateIDs()
	{
		return stateID;
	}
	
	/**
	 * Adds states of the dropped item
	 * @param list : String[]
	 */
	public void addStates(final String[] list)
	{
		stateID = list;
	}
	
	/**
	 * Returns the questID.
	 * @return String designating the ID of the quest
	 */
	public String getQuestID()
	{
		return questID;
	}
	
	/**
	 * Sets the questID
	 * @param questID designating the questID to set.
	 */
	public void setQuestID(final String questID)
	{
		this.questID = questID;
	}
	
	/**
	 * Returns if the dropped item is requested for a quest
	 * @return boolean
	 */
	public boolean isQuestDrop()
	{
		return questID != null && stateID != null;
	}
	
	public boolean isCustomDrop()
	{
		return customDrop;
	}
	
	public void setIsCustomDrop(boolean custom)
	{
		customDrop = custom;
	}
	
	/**
	 * Returns a report of the object
	 * @return String
	 */
	@Override
	public String toString()
	{
		String out = "ItemID: " + getItemId() + " Min: " + getMinDrop() + " Max: " + getMaxDrop() + " Chance: " + getChance() / 10000.0 + "%";
		if (isQuestDrop())
		{
			out += " QuestID: " + getQuestID() + " StateID's: " + Arrays.toString(getStateIDs());
		}
		
		return out;
	}
	
	/**
	 * Returns if parameter "o" is a L2DropData and has the same itemID that the current object
	 * @param  o object to compare to the current one
	 * @return   boolean
	 */
	@Override
	public boolean equals(final Object o)
	{
		if (o instanceof L2DropData)
		{
			final L2DropData drop = (L2DropData) o;
			return drop.getItemId() == getItemId();
		}
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return getItemId();
	}
}
