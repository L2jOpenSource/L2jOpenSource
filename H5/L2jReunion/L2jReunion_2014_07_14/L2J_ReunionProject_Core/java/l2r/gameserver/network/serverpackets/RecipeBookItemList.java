/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.L2RecipeList;

public class RecipeBookItemList extends L2GameServerPacket
{
	private L2RecipeList[] _recipes;
	private final boolean _isDwarvenCraft;
	private final int _maxMp;
	
	public RecipeBookItemList(boolean isDwarvenCraft, int maxMp)
	{
		_isDwarvenCraft = isDwarvenCraft;
		_maxMp = maxMp;
	}
	
	public void addRecipes(L2RecipeList[] recipeBook)
	{
		_recipes = recipeBook;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xdc);
		writeD(_isDwarvenCraft ? 0x00 : 0x01); // 0 = Dwarven - 1 = Common
		writeD(_maxMp);
		
		if (_recipes == null)
		{
			writeD(0);
		}
		else
		{
			writeD(_recipes.length); // number of items in recipe book
			for (int i = 0; i < _recipes.length; i++)
			{
				writeD(_recipes[i].getId());
				writeD(i + 1);
			}
		}
	}
}
