package com.l2jfrozen.gameserver.model.holder;

import com.l2jfrozen.gameserver.model.L2Character;

/**
 * @author fissban This class contains all RewardInfo of the L2Attackable against the any attacker L2Character, based on amount of damage done.<BR>
 *         <B><U> Data</U> :</B><BR>
 *         <li>attacker : The attacker L2Character concerned by this RewardInfoHolder of this L2Attackable</li>
 *         <li>dmg : Total amount of damage done by the attacker to this L2Attackable (summon + own)</li>
 */
public class RewardInfoHolder
{
	/** The attacker L2Character concerned by this RewardInfoHolder of this L2Attackable */
	private L2Character attacker;
	
	/** Total amount of damage done by the attacker to this L2Attackable (summon + owner) */
	private int dmg = 0;
	
	/**
	 * Constructor RewardInfoHolder.
	 * @param pAttacker
	 * @param pDmg
	 */
	public RewardInfoHolder(L2Character pAttacker, int pDmg)
	{
		attacker = pAttacker;
		dmg = pDmg;
	}
	
	/**
	 * Increase de {@link #dmg} value
	 * @param pDmg
	 */
	public void addDamage(final int pDmg)
	{
		dmg += pDmg;
	}
	
	/**
	 * @return {@link #dmg}
	 */
	public int getDmg()
	{
		return dmg;
	}
	
	/**
	 * @return {@link #attacker}
	 */
	public L2Character getAttacker()
	{
		return attacker;
	}
	
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		
		if (obj instanceof RewardInfoHolder)
		{
			return ((RewardInfoHolder) obj).getAttacker() == attacker;
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return attacker.getObjectId();
	}
}
