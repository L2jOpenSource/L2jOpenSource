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

import l2r.util.Rnd;

/**
 * This class defines the spawn data of a Minion type In a group mob, there are one master called RaidBoss and several slaves called Minions.
 */
public class L2MinionData
{
	
	/** The Identifier of the L2Minion to spawn */
	private int _minionId;
	
	/** The number of this Minion Type to spawn */
	private int _minionAmount;
	private int _minionAmountMin;
	private int _minionAmountMax;
	
	/**
	 * Set the Identifier of the Minion to spawn.
	 * @param id The L2Character Identifier to spawn
	 */
	public void setMinionId(int id)
	{
		_minionId = id;
	}
	
	/**
	 * @return the Identifier of the Minion to spawn.
	 */
	public int getMinionId()
	{
		return _minionId;
	}
	
	/**
	 * Set the minimum of minions to amount.
	 * @param amountMin The minimum quantity of this Minion type to spawn
	 */
	public void setAmountMin(int amountMin)
	{
		_minionAmountMin = amountMin;
	}
	
	/**
	 * Set the maximum of minions to amount.
	 * @param amountMax The maximum quantity of this Minion type to spawn
	 */
	public void setAmountMax(int amountMax)
	{
		_minionAmountMax = amountMax;
	}
	
	/**
	 * Set the amount of this Minion type to spawn.
	 * @param amount The quantity of this Minion type to spawn
	 */
	public void setAmount(int amount)
	{
		_minionAmount = amount;
	}
	
	/**
	 * @return the amount of this Minion type to spawn.
	 */
	public int getAmount()
	{
		if (_minionAmountMax > _minionAmountMin)
		{
			_minionAmount = Rnd.get(_minionAmountMin, _minionAmountMax);
			return _minionAmount;
		}
		return _minionAmountMin;
	}
}
