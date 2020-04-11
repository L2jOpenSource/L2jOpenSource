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
package l2r.gameserver.model;

/**
 * @author xban1x
 */
public final class SeedProduction
{
	final int _seedId;
	long _residual;
	final long _price;
	final long _sales;
	
	public SeedProduction(int id)
	{
		_seedId = id;
		_residual = 0;
		_price = 0;
		_sales = 0;
	}
	
	public SeedProduction(int id, long amount, long price, long sales)
	{
		_seedId = id;
		_residual = amount;
		_price = price;
		_sales = sales;
	}
	
	public int getId()
	{
		return _seedId;
	}
	
	public long getCanProduce()
	{
		return _residual;
	}
	
	public long getPrice()
	{
		return _price;
	}
	
	public long getStartProduce()
	{
		return _sales;
	}
	
	public void setCanProduce(long amount)
	{
		_residual = amount;
	}
}
