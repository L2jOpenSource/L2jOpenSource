package com.l2jfrozen.gameserver.model.multisell;

import java.util.ArrayList;
import java.util.List;

/**
 * @author programmos
 */
public class MultiSellEntry
{
	private int entryId;
	
	private final List<MultiSellIngredient> products = new ArrayList<>();
	private final List<MultiSellIngredient> ingredients = new ArrayList<>();
	
	/**
	 * @param entryId The entryId to set.
	 */
	public void setEntryId(final int entryId)
	{
		this.entryId = entryId;
	}
	
	/**
	 * @return Returns the entryId.
	 */
	public int getEntryId()
	{
		return entryId;
	}
	
	/**
	 * @param product The product to add.
	 */
	public void addProduct(final MultiSellIngredient product)
	{
		products.add(product);
	}
	
	/**
	 * @return Returns the products.
	 */
	public List<MultiSellIngredient> getProducts()
	{
		return products;
	}
	
	/**
	 * @param ingredient The ingredients to set.
	 */
	public void addIngredient(final MultiSellIngredient ingredient)
	{
		ingredients.add(ingredient);
	}
	
	/**
	 * @return Returns the ingredients.
	 */
	public List<MultiSellIngredient> getIngredients()
	{
		return ingredients;
	}
}
