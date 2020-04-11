package net.sf.l2j.gameserver.model.item;

import java.util.List;

import net.sf.l2j.commons.util.StatsSet;

import net.sf.l2j.gameserver.model.holder.IntIntHolder;

/**
 * This datatype is used to store Recipe informations used by Dwarf to craft items.<br>
 * It holds a List of {@link IntIntHolder} for materials and a simple IntIntHolder for production.
 */
public class Recipe
{
	private final List<IntIntHolder> _materials;
	private final IntIntHolder _product;
	
	private final int _id;
	private final int _level;
	private final int _recipeId;
	private final String _recipeName;
	private final int _successRate;
	private final int _mpCost;
	private final boolean _isDwarven;
	
	public Recipe(StatsSet set)
	{
		_materials = set.getIntIntHolderList("material");
		_product = set.getIntIntHolder("product");
		
		_id = set.getInteger("id");
		_level = set.getInteger("level");
		_recipeId = set.getInteger("itemId");
		_recipeName = set.getString("alias");
		_successRate = set.getInteger("successRate");
		_mpCost = set.getInteger("mpConsume");
		_isDwarven = set.getBool("isDwarven");
	}
	
	public List<IntIntHolder> getMaterials()
	{
		return _materials;
	}
	
	public IntIntHolder getProduct()
	{
		return _product;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public int getRecipeId()
	{
		return _recipeId;
	}
	
	public String getRecipeName()
	{
		return _recipeName;
	}
	
	public int getSuccessRate()
	{
		return _successRate;
	}
	
	public int getMpCost()
	{
		return _mpCost;
	}
	
	public boolean isDwarven()
	{
		return _isDwarven;
	}
}