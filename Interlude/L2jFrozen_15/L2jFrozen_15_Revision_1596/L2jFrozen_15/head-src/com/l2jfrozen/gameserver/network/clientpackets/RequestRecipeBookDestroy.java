package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.datatables.csv.RecipeTable;
import com.l2jfrozen.gameserver.model.L2RecipeList;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.RecipeBookItemList;

public final class RequestRecipeBookDestroy extends L2GameClientPacket
{
	private int recipeID;
	
	/**
	 * Unknown Packet:ad 0000: ad 02 00 00 00
	 */
	@Override
	protected void readImpl()
	{
		recipeID = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar != null)
		{
			if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("RecipeDestroy"))
			{
				return;
			}
			
			final L2RecipeList rp = RecipeTable.getInstance().getRecipeList(recipeID - 1);
			if (rp == null)
			{
				return;
			}
			
			activeChar.unregisterRecipeList(recipeID);
			
			final RecipeBookItemList response = new RecipeBookItemList(rp.isDwarvenRecipe(), activeChar.getMaxMp());
			
			if (rp.isDwarvenRecipe())
			{
				response.addRecipes(activeChar.getDwarvenRecipeBook());
			}
			else
			{
				response.addRecipes(activeChar.getCommonRecipeBook());
			}
			
			activeChar.sendPacket(response);
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] AD RequestRecipeBookDestroy";
	}
}
