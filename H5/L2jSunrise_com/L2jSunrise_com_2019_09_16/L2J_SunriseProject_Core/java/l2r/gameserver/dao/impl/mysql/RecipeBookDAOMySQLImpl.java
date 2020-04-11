/*
 * Copyright (C) 2004-2017 L2J Server
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
package l2r.gameserver.dao.impl.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import l2r.L2DatabaseFactory;
import l2r.gameserver.dao.RecipeBookDAO;
import l2r.gameserver.data.xml.impl.RecipeData;
import l2r.gameserver.model.L2RecipeList;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Recipe Book DAO MySQL implementation.
 * @author vGodFather
 */
public class RecipeBookDAOMySQLImpl implements RecipeBookDAO
{
	private static Logger Log = LoggerFactory.getLogger("DAO");
	
	private static final String INSERT = "INSERT INTO character_recipebook (charId, id, classIndex, type) values(?,?,?,?)";
	
	private static final String DELETE = "DELETE FROM character_recipebook WHERE charId=? AND id=? AND classIndex=?";
	
	private static final String SELECT_COMMON = "SELECT id, type, classIndex FROM character_recipebook WHERE charId=?";
	
	private static final String SELECT = "SELECT id FROM character_recipebook WHERE charId=? AND classIndex=? AND type = 1";
	
	@Override
	public void insert(L2PcInstance player, int recipeId, boolean isDwarf)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(INSERT))
		{
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, recipeId);
			ps.setInt(3, isDwarf ? player.getClassIndex() : 0);
			ps.setInt(4, isDwarf ? 1 : 0);
			ps.execute();
		}
		catch (SQLException e)
		{
			Log.warn("SQL exception while inserting recipe: " + recipeId + " from player " + player, e);
		}
	}
	
	@Override
	public void delete(L2PcInstance player, int recipeId, boolean isDwarf)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE))
		{
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, recipeId);
			ps.setInt(3, isDwarf ? player.getClassIndex() : 0);
			ps.execute();
		}
		catch (SQLException e)
		{
			Log.warn("SQL exception while deleting recipe: " + recipeId + " from player " + player, e);
		}
	}
	
	@Override
	public void load(L2PcInstance player, boolean loadCommon)
	{
		// TODO(Zoey76): Split into two methods.
		final String sql = loadCommon ? SELECT_COMMON : SELECT;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(sql))
		{
			ps.setInt(1, player.getObjectId());
			if (!loadCommon)
			{
				ps.setInt(2, player.getClassIndex());
			}
			
			try (ResultSet rset = ps.executeQuery())
			{
				// TODO(Zoey76): Why only dwarven is cleared?
				player.getDwarvenRecipeBookClear();
				
				final RecipeData rd = RecipeData.getInstance();
				while (rset.next())
				{
					final L2RecipeList recipe = rd.getRecipeList(rset.getInt("id"));
					if (loadCommon)
					{
						if (rset.getInt(2) == 1)
						{
							if (rset.getInt(3) == player.getClassIndex())
							{
								player.registerDwarvenRecipeList(recipe, false);
							}
						}
						else
						{
							player.registerCommonRecipeList(recipe, false);
						}
					}
					else
					{
						player.registerDwarvenRecipeList(recipe, false);
					}
				}
			}
		}
		catch (Exception e)
		{
			Log.error("Could not restore recipe book data: {}", e);
		}
	}
}
