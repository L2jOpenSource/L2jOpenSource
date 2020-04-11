package com.l2jfrozen.gameserver.model.holder;

import com.l2jfrozen.gameserver.model.L2Character;

/**
 * This class contains all AggroInfo of the L2Attackable against the attacker L2Character.<BR>
 * <B><U> Data</U> :</B><BR>
 * <li>attacker : The attacker L2Character concerned by this AggroInfo of this L2Attackable</li>
 * <li>hate : Hate level of this L2Attackable against the attacker L2Character (hate = damage)</li>
 * <li>damage : Number of damages that the attacker L2Character gave to this L2Attackable</li>
 */
public final class AggroInfoHolder
{
	/** The attacker L2Character concerned by this AggroInfo of this L2Attackable */
	private L2Character attacker;
	
	/** Hate level of this L2Attackable against the attacker L2Character (hate = damage) */
	private int hate;
	
	/** Number of damages that the attacker L2Character gave to this L2Attackable */
	private int damage;
	
	/**
	 * Constructor of AggroInfoHolder.
	 * @param pAttacker
	 */
	public AggroInfoHolder(final L2Character pAttacker)
	{
		attacker = pAttacker;
	}
	
	/**
	 * Init {@link #hate} & {@link #damage} to value 0
	 */
	public void init()
	{
		damage = 0;
		hate = 0;
	}
	
	/**
	 * Verify is object is equal to this AggroInfo.
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		
		if (obj instanceof AggroInfoHolder)
		{
			return ((AggroInfoHolder) obj).getAttacker() == attacker;
		}
		
		return false;
	}
	
	/**
	 * @return the attacker
	 */
	public L2Character getAttacker()
	{
		return attacker;
	}
	
	/**
	 * @return the {@link #hate}
	 */
	public int getHate()
	{
		return hate;
	}
	
	/**
	 * Increase {@link #hate} value
	 * @param ht
	 */
	public void incHate(int ht)
	{
		hate += ht;
	}
	
	/**
	 * Decrease {@link #hate} value
	 * @param ht
	 */
	public void decHate(int ht)
	{
		hate -= ht;
	}
	
	/**
	 * Init {@link #hate} to 0
	 */
	public void initHate()
	{
		hate = 0;
	}
	
	/**
	 * @return the {@link #damage}
	 */
	public int getDmg()
	{
		return damage;
	}
	
	/**
	 * Increase {@link #damage} value
	 * @param dmg
	 */
	public void incDmg(int dmg)
	{
		damage += dmg;
	}
	
	/**
	 * Return the Identifier of the attacker L2Character.
	 */
	@Override
	public int hashCode()
	{
		return attacker.getObjectId();
	}
}
