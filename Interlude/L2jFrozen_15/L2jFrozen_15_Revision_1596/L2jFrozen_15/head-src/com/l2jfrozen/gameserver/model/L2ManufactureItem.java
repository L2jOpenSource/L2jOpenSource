package com.l2jfrozen.gameserver.model;

import com.l2jfrozen.gameserver.datatables.csv.RecipeTable;

/**
 * This class ...
 * @version $Revision: 1.1.2.2.2.1 $ $Date: 2005/03/27 15:29:32 $
 */
public class L2ManufactureItem
{
	private final int manufactureRecipeId;
	private final int cost;
	private final boolean isDwarven;
	
	public L2ManufactureItem(final int recipeId, final int cost)
	{
		manufactureRecipeId = recipeId;
		this.cost = cost;
		isDwarven = RecipeTable.getInstance().getRecipeById(manufactureRecipeId).isDwarvenRecipe();
	}
	
	public int getRecipeId()
	{
		return manufactureRecipeId;
	}
	
	public int getCost()
	{
		return cost;
	}
	
	public boolean isDwarven()
	{
		return isDwarven;
	}
}
