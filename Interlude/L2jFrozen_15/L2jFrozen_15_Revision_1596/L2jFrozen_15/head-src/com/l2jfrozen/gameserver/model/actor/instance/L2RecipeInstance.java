package com.l2jfrozen.gameserver.model.actor.instance;

/**
 * This class describes a RecipeList component (1 line of the recipe : Item-Quantity needed).
 */
public class L2RecipeInstance
{
	
	/** The Identifier of the item needed in the L2RecipeInstance. */
	private final int itemId;
	
	/** The item quantity needed in the L2RecipeInstance. */
	private final int quantity;
	
	/**
	 * Constructor of L2RecipeInstance (create a new line in a RecipeList).<BR>
	 * <BR>
	 * @param itemId   the item id
	 * @param quantity the quantity
	 */
	public L2RecipeInstance(final int itemId, final int quantity)
	{
		this.itemId = itemId;
		this.quantity = quantity;
	}
	
	/**
	 * Return the Identifier of the L2RecipeInstance Item needed.<BR>
	 * <BR>
	 * @return the item id
	 */
	public int getItemId()
	{
		return itemId;
	}
	
	/**
	 * Return the Item quantity needed of the L2RecipeInstance.<BR>
	 * <BR>
	 * @return the quantity
	 */
	public int getQuantity()
	{
		return quantity;
	}
	
}
