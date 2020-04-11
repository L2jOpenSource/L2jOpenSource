/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.data.sql.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.data.xml.impl.PrimeShopData;
import com.l2jserver.gameserver.model.PrimeShop;

/**
 * Loads Prime Shop from database.
 * @author U3Games
 */
public final class PrimeShopTable
{
	private static final Logger LOG = LoggerFactory.getLogger(PrimeShopTable.class);
	private static final String LOAD_PRODUCT = "SELECT productId FROM prime_shop WHERE charId=? ORDER BY transactionTime DESC";
	private static final String ADD_PRODUCT = "INSERT INTO prime_shop (charId, productId, quantity, maxStock) values (?,?,?,?)";
	private static final String LOAD_ACTUAL_STOCK = "SELECT quantity FROM prime_shop WHERE productId=?";
	private static final String LOAD_MAX_STOCK = "SELECT maxStock FROM prime_shop WHERE productId=?";
	
	private final List<PrimeShop> _itemList = new ArrayList<>();
	
	public PrimeShopTable()
	{
		// empty
	}
	
	public void addPoduct(int objectId, int productId, int quantity, int maxStock)
	{
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement(ADD_PRODUCT))
		{
			st.setInt(1, objectId);
			st.setInt(2, productId);
			st.setInt(3, quantity);
			st.setInt(4, maxStock);
			st.execute();
			st.close();
		}
		catch (SQLException e)
		{
			LOG.warn("{}: There was a problem while add product of prime shop!", getClass().getSimpleName(), e);
		}
	}
	
	private void loadPoducts(int objectId)
	{
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(LOAD_PRODUCT))
		{
			ps.setInt(1, objectId);
			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					final PrimeShop product = PrimeShopData.getInstance().getProduct(rset.getInt("productId"));
					if ((product != null) && !_itemList.contains(product))
					{
						_itemList.add(product);
					}
				}
				rset.close();
			}
			catch (SQLException e)
			{
				LOG.warn("{}: There was a problem while load product of prime shop!", getClass().getSimpleName(), e);
			}
			ps.close();
		}
		catch (Exception e)
		{
			LOG.warn("{}: Error while load products of prime shop!", getClass().getSimpleName(), e);
		}
	}
	
	public int getActualStock(int productId)
	{
		int getActualStock = 0;
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(LOAD_ACTUAL_STOCK))
		{
			ps.setInt(1, productId);
			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					return getActualStock = rset.getInt("quantity");
				}
				rset.close();
			}
			catch (SQLException e)
			{
				LOG.warn("{}: There was a problem while get actual stock of product in prime shop!", getClass().getSimpleName(), e);
			}
			ps.close();
		}
		catch (Exception e)
		{
			LOG.warn("{}: Error while get actual stock of products in prime shop!", getClass().getSimpleName(), e);
		}
		
		return getActualStock;
	}
	
	public int getMaxStock(int productId)
	{
		int maxStock = 0;
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(LOAD_MAX_STOCK))
		{
			ps.setInt(1, productId);
			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					return maxStock = rset.getInt("maxStock");
				}
				rset.close();
			}
			catch (SQLException e)
			{
				LOG.warn("{}: There was a problem while get max stock of product in prime shop!", getClass().getSimpleName(), e);
			}
			ps.close();
		}
		catch (Exception e)
		{
			LOG.warn("{}: Error while get max stock of products in prime shop!", getClass().getSimpleName(), e);
		}
		
		return maxStock;
	}
	
	public List<PrimeShop> getPoducts(int objectId)
	{
		loadPoducts(objectId);
		return _itemList;
	}
	
	public static PrimeShopTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PrimeShopTable _instance = new PrimeShopTable();
	}
}