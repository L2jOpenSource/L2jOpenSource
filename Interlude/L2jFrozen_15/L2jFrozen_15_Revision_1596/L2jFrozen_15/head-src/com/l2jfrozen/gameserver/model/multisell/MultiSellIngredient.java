package com.l2jfrozen.gameserver.model.multisell;

/**
 * @author programmos
 */
public class MultiSellIngredient
{
	private int itemId, itemCount, enchantmentLevel;
	private boolean isTaxIngredient, mantainIngredient;
	
	public MultiSellIngredient(final int itemId, final int itemCount, final boolean isTaxIngredient, final boolean mantainIngredient)
	{
		this(itemId, itemCount, 0, isTaxIngredient, mantainIngredient);
	}
	
	public MultiSellIngredient(final int itemId, final int itemCount, final int enchantmentLevel, final boolean isTaxIngredient, final boolean mantainIngredient)
	{
		setItemId(itemId);
		setItemCount(itemCount);
		setEnchantmentLevel(enchantmentLevel);
		setIsTaxIngredient(isTaxIngredient);
		setMantainIngredient(mantainIngredient);
	}
	
	public MultiSellIngredient(final MultiSellIngredient e)
	{
		itemId = e.getItemId();
		itemCount = e.getItemCount();
		enchantmentLevel = e.getEnchantmentLevel();
		isTaxIngredient = e.isTaxIngredient();
		mantainIngredient = e.getMantainIngredient();
	}
	
	/**
	 * @param itemId The itemId to set.
	 */
	public void setItemId(final int itemId)
	{
		this.itemId = itemId;
	}
	
	/**
	 * @return Returns the itemId.
	 */
	public int getItemId()
	{
		return itemId;
	}
	
	/**
	 * @param itemCount The itemCount to set.
	 */
	public void setItemCount(final int itemCount)
	{
		this.itemCount = itemCount;
	}
	
	/**
	 * @return Returns the itemCount.
	 */
	public int getItemCount()
	{
		return itemCount;
	}
	
	/**
	 * @param enchantmentLevel
	 */
	public void setEnchantmentLevel(final int enchantmentLevel)
	{
		this.enchantmentLevel = enchantmentLevel;
	}
	
	/**
	 * @return Returns the itemCount.
	 */
	public int getEnchantmentLevel()
	{
		return enchantmentLevel;
	}
	
	public void setIsTaxIngredient(final boolean isTaxIngredient)
	{
		this.isTaxIngredient = isTaxIngredient;
	}
	
	public boolean isTaxIngredient()
	{
		return isTaxIngredient;
	}
	
	public void setMantainIngredient(final boolean mantainIngredient)
	{
		this.mantainIngredient = mantainIngredient;
	}
	
	public boolean getMantainIngredient()
	{
		return mantainIngredient;
	}
}
