package com.l2jfrozen.gameserver.datatables.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.controllers.RecipeController;
import com.l2jfrozen.gameserver.model.L2RecipeList;
import com.l2jfrozen.gameserver.model.actor.instance.L2RecipeInstance;

/**
 * @author programmos
 */
public class RecipeTable extends RecipeController
{
	private static final Logger LOGGER = Logger.getLogger(RecipeTable.class);
	private static RecipeTable instance;
	private final Map<Integer, L2RecipeList> lists;
	
	public static RecipeTable getInstance()
	{
		if (instance == null)
		{
			instance = new RecipeTable();
		}
		
		return instance;
	}
	
	private RecipeTable()
	{
		lists = new HashMap<>();
		String line = null;
		
		File recipesData = new File(Config.DATAPACK_ROOT, "data/csv/recipes.csv");
		
		try (FileReader reader = new FileReader(recipesData);
			BufferedReader buff = new BufferedReader(reader);
			LineNumberReader lnr = new LineNumberReader(buff))
		{
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().length() == 0 || line.startsWith("#"))
				{
					continue;
				}
				
				parseList(line);
			}
			
			LOGGER.info("RecipeController: Loaded " + lists.size() + " Recipes.");
		}
		catch (Exception e)
		{
			LOGGER.error("RecipeTable.RecipeTable : cant not find recipes.csv file in gameserver/data/csv/ folder. ", e);
		}
	}
	
	private void parseList(final String line)
	{
		try
		{
			StringTokenizer st = new StringTokenizer(line, ";");
			List<L2RecipeInstance> recipePartList = new ArrayList<>();
			
			// we use common/dwarf for easy reading of the recipes.csv file
			String recipeTypeString = st.nextToken();
			
			// now parse the string into a boolean
			boolean isDwarvenRecipe;
			
			if (recipeTypeString.equalsIgnoreCase("dwarven"))
			{
				isDwarvenRecipe = true;
			}
			else if (recipeTypeString.equalsIgnoreCase("common"))
			{
				isDwarvenRecipe = false;
			}
			else
			{ // prints a helpfull message
				LOGGER.warn("Error parsing recipes.csv, unknown recipe type " + recipeTypeString);
				return;
			}
			
			recipeTypeString = null;
			
			String recipeName = st.nextToken();
			final int id = Integer.parseInt(st.nextToken());
			final int recipeId = Integer.parseInt(st.nextToken());
			final int level = Integer.parseInt(st.nextToken());
			
			// material
			StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
			while (st2.hasMoreTokens())
			{
				StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
				final int rpItemId = Integer.parseInt(st3.nextToken());
				final int quantity = Integer.parseInt(st3.nextToken());
				L2RecipeInstance rp = new L2RecipeInstance(rpItemId, quantity);
				recipePartList.add(rp);
				rp = null;
				st3 = null;
			}
			st2 = null;
			
			final int itemId = Integer.parseInt(st.nextToken());
			final int count = Integer.parseInt(st.nextToken());
			
			// npc fee
			/* String notdoneyet = */st.nextToken();
			
			final int mpCost = Integer.parseInt(st.nextToken());
			final int successRate = Integer.parseInt(st.nextToken());
			
			L2RecipeList recipeList = new L2RecipeList(id, level, recipeId, recipeName, successRate, mpCost, itemId, count, isDwarvenRecipe);
			
			for (final L2RecipeInstance recipePart : recipePartList)
			{
				recipeList.addRecipe(recipePart);
			}
			lists.put(lists.size(), recipeList);
			
			recipeList = null;
			recipeName = null;
			st = null;
		}
		catch (final Exception e)
		{
			LOGGER.error("Exception in RecipeController.parseList()", e);
		}
	}
	
	public int getRecipesCount()
	{
		return lists.size();
	}
	
	public L2RecipeList getRecipeList(final int listId)
	{
		return lists.get(listId);
	}
	
	public L2RecipeList getRecipeByItemId(final int itemId)
	{
		for (int i = 0; i < lists.size(); i++)
		{
			final L2RecipeList find = lists.get(i);
			if (find.getRecipeId() == itemId)
			{
				return find;
			}
		}
		return null;
	}
	
	public L2RecipeList getRecipeById(final int recId)
	{
		for (int i = 0; i < lists.size(); i++)
		{
			final L2RecipeList find = lists.get(i);
			if (find.getId() == recId)
			{
				return find;
			}
		}
		return null;
	}
}
