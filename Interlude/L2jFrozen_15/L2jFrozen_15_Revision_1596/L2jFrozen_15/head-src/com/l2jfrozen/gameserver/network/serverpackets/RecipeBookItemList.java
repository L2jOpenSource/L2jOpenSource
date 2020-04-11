package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2RecipeList;

/**
 * format d d(dd)
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class RecipeBookItemList extends L2GameServerPacket
{
	private L2RecipeList[] recipes;
	private final boolean isDwarvenCraft;
	private final int maxMp;
	
	public RecipeBookItemList(final boolean isDwarvenCraft, final int maxMp)
	{
		this.isDwarvenCraft = isDwarvenCraft;
		this.maxMp = maxMp;
	}
	
	public void addRecipes(final L2RecipeList[] recipeBook)
	{
		recipes = recipeBook;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xD6);
		
		writeD(isDwarvenCraft ? 0x00 : 0x01); // 0 = Dwarven - 1 = Common
		writeD(maxMp);
		
		if (recipes == null)
		{
			writeD(0);
		}
		else
		{
			writeD(recipes.length);// number of items in recipe book
			
			for (int i = 0; i < recipes.length; i++)
			{
				final L2RecipeList temp = recipes[i];
				writeD(temp.getId());
				writeD(i + 1);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] D6 RecipeBookItemList";
	}
}
