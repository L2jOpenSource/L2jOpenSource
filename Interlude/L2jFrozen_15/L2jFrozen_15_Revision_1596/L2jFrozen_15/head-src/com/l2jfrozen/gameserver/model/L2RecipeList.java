package com.l2jfrozen.gameserver.model;

import com.l2jfrozen.gameserver.model.actor.instance.L2RecipeInstance;

/**
 * This class describes a Recipe used by Dwarf to craft Item. All L2RecipeList are made of L2RecipeInstance (1 line of the recipe : Item-Quantity needed).<BR>
 * <BR>
 */
public class L2RecipeList
{
	/** The table containing all L2RecipeInstance (1 line of the recipe : Item-Quantity needed) of the L2RecipeList */
	private L2RecipeInstance[] recipes;
	
	/** The Identifier of the Instance */
	private final int id;
	
	/** The crafting level needed to use this L2RecipeList */
	private final int level;
	
	/** The Identifier of the L2RecipeList */
	private final int recipeId;
	
	/** The name of the L2RecipeList */
	private final String recipeName;
	
	/** The crafting succes rate when using the L2RecipeList */
	private final int successRate;
	
	/** The crafting MP cost of this L2RecipeList */
	private final int mpCost;
	
	/** The Identifier of the Item crafted with this L2RecipeList */
	private final int itemId;
	
	/** The quantity of Item crafted when using this L2RecipeList */
	private final int count;
	
	/** If this a common or a dwarven recipe */
	private final boolean isDwarvenRecipe;
	
	private boolean fromDB;
	
	/**
	 * Constructor of L2RecipeList (create a new Recipe).
	 * @param id
	 * @param level
	 * @param recipeId
	 * @param recipeName
	 * @param successRate
	 * @param mpCost
	 * @param itemId
	 * @param count
	 * @param isDwarvenRecipe
	 */
	public L2RecipeList(final int id, final int level, final int recipeId, final String recipeName, final int successRate, final int mpCost, final int itemId, final int count, final boolean isDwarvenRecipe)
	{
		this.id = id;
		recipes = new L2RecipeInstance[0];
		this.level = level;
		this.recipeId = recipeId;
		this.recipeName = recipeName;
		this.successRate = successRate;
		this.mpCost = mpCost;
		this.itemId = itemId;
		this.count = count;
		this.isDwarvenRecipe = isDwarvenRecipe;
		fromDB = false;
	}
	
	/**
	 * Add a L2RecipeInstance to the L2RecipeList (add a line Item-Quantity needed to the Recipe).
	 * @param recipe
	 */
	public void addRecipe(final L2RecipeInstance recipe)
	{
		final int len = recipes.length;
		L2RecipeInstance[] tmp = new L2RecipeInstance[len + 1];
		System.arraycopy(recipes, 0, tmp, 0, len);
		tmp[len] = recipe;
		recipes = tmp;
		tmp = null;
	}
	
	/**
	 * @return the Identifier of the Instance.
	 */
	public int getId()
	{
		return id;
	}
	
	/**
	 * @return the crafting level needed to use this L2RecipeList.
	 */
	public int getLevel()
	{
		return level;
	}
	
	/**
	 * @return the Identifier of the L2RecipeList.
	 */
	public int getRecipeId()
	{
		return recipeId;
	}
	
	/**
	 * @return the name of the L2RecipeList.
	 */
	public String getRecipeName()
	{
		return recipeName;
	}
	
	/**
	 * @return the crafting success rate when using the L2RecipeList.
	 */
	public int getSuccessRate()
	{
		return successRate;
	}
	
	/**
	 * @return the crafting MP cost of this L2RecipeList.
	 */
	public int getMpCost()
	{
		return mpCost;
	}
	
	/**
	 * @return true if the Item crafted with this L2RecipeList is consumable (shot, arrow,...).
	 */
	public boolean isConsumable()
	{
		return itemId >= 1463 && itemId <= 1467 || itemId >= 2509 && itemId <= 2514 || itemId >= 3947 && itemId <= 3952 || itemId >= 1341 && itemId <= 1345;
	}
	
	/**
	 * @return the Identifier of the Item crafted with this L2RecipeList.
	 */
	public int getItemId()
	{
		return itemId;
	}
	
	/**
	 * @return the quantity of Item crafted when using this L2RecipeList.
	 */
	public int getCount()
	{
		return count;
	}
	
	/**
	 * @return true if this a Dwarven recipe or false if its a Common recipe
	 */
	public boolean isDwarvenRecipe()
	{
		return isDwarvenRecipe;
	}
	
	/**
	 * @return the table containing all L2RecipeInstance (1 line of the recipe : Item-Quantity needed) of the L2RecipeList.
	 */
	public L2RecipeInstance[] getRecipes()
	{
		return recipes;
	}
	
	public boolean isFromDB()
	{
		return fromDB;
	}
	
	public void setIsFromDB(boolean value)
	{
		fromDB = value;
	}
}
