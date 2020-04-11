package com.l2jfrozen.gameserver.model;

import com.l2jfrozen.util.random.Rnd;

/**
 * This class defines the spawn data of a Minion type In a group mob, there are one master called RaidBoss and several slaves called Minions. <B><U> Data</U> :</B><BR>
 * <BR>
 * <li>_minionId : The Identifier of the L2Minion to spawn</li>
 * <li>_minionAmount : The number of this Minion Type to spawn</li><BR>
 * <BR>
 */
public class L2MinionData
{
	
	/** The Identifier of the L2Minion */
	private int minionId;
	
	/** The number of this Minion Type to spawn */
	private int minionAmount;
	private int minionAmountMin;
	private int minionAmountMax;
	
	/**
	 * Set the Identifier of the Minion to spawn.<BR>
	 * <BR>
	 * @param id
	 */
	public void setMinionId(final int id)
	{
		minionId = id;
	}
	
	/**
	 * @return the Identifier of the Minion to spawn.
	 */
	public int getMinionId()
	{
		return minionId;
	}
	
	/**
	 * Set the minimum of minions to amount.<BR>
	 * <BR>
	 * @param amountMin The minimum quantity of this Minion type to spawn
	 */
	public void setAmountMin(final int amountMin)
	{
		minionAmountMin = amountMin;
	}
	
	/**
	 * Set the maximum of minions to amount.<BR>
	 * <BR>
	 * @param amountMax The maximum quantity of this Minion type to spawn
	 */
	public void setAmountMax(final int amountMax)
	{
		minionAmountMax = amountMax;
	}
	
	/**
	 * Set the amount of this Minion type to spawn.<BR>
	 * <BR>
	 * @param amount The quantity of this Minion type to spawn
	 */
	public void setAmount(final int amount)
	{
		minionAmount = amount;
	}
	
	/**
	 * @return the amount of this Minion type to spawn.
	 */
	public int getAmount()
	{
		if (minionAmountMax > minionAmountMin)
		{
			minionAmount = Rnd.get(minionAmountMin, minionAmountMax);
			return minionAmount;
		}
		return minionAmountMin;
	}
	
}
