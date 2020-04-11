package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2ManufactureItem;
import com.l2jfrozen.gameserver.model.L2ManufactureList;
import com.l2jfrozen.gameserver.model.L2RecipeList;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * dd d(dd) d(ddd)
 * @version $Revision: 1.1.2.2.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class RecipeShopManageList extends L2GameServerPacket
{
	private final L2PcInstance playerSeller;
	private final boolean isDwarven;
	private L2RecipeList[] recipes;
	
	public RecipeShopManageList(final L2PcInstance seller, final boolean isDwarven)
	{
		playerSeller = seller;
		this.isDwarven = isDwarven;
		
		if (this.isDwarven && playerSeller.hasDwarvenCraft())
		{
			recipes = playerSeller.getDwarvenRecipeBook();
		}
		else
		{
			recipes = playerSeller.getCommonRecipeBook();
		}
		
		// clean previous recipes
		if (playerSeller.getCreateList() != null)
		{
			final L2ManufactureList list = playerSeller.getCreateList();
			for (final L2ManufactureItem item : list.getList())
			{
				if (item.isDwarven() != this.isDwarven)
				{
					list.getList().remove(item);
				}
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xd8);
		writeD(playerSeller.getObjectId());
		writeD(playerSeller.getAdena());
		writeD(isDwarven ? 0x00 : 0x01);
		
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
		
		if (playerSeller.getCreateList() == null)
		{
			writeD(0);
		}
		else
		{
			final L2ManufactureList list = playerSeller.getCreateList();
			writeD(list.size());
			
			for (final L2ManufactureItem item : list.getList())
			{
				writeD(item.getRecipeId());
				writeD(0x00);
				writeD(item.getCost());
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] d8 RecipeShopManageList";
	}
}
