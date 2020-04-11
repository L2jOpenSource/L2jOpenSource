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
public final class CropProcure
{
	final int _cropId;
	long _buyResidual;
	final int _rewardType;
	final long _buy;
	final long _price;
	
	public CropProcure(int id)
	{
		_cropId = id;
		_buyResidual = 0;
		_rewardType = 0;
		_buy = 0;
		_price = 0;
	}
	
	public CropProcure(int id, long amount, int type, long buy, long price)
	{
		_cropId = id;
		_buyResidual = amount;
		_rewardType = type;
		_buy = buy;
		_price = price;
	}
	
	public int getReward()
	{
		return _rewardType;
	}
	
	public int getId()
	{
		return _cropId;
	}
	
	public long getAmount()
	{
		return _buyResidual;
	}
	
	public long getStartAmount()
	{
		return _buy;
	}
	
	public long getPrice()
	{
		return _price;
	}
	
	public void setAmount(long amount)
	{
		_buyResidual = amount;
	}
	
}
