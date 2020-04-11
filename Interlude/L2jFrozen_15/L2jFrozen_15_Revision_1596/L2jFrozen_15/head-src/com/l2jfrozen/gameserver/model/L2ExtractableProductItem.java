package com.l2jfrozen.gameserver.model;

/**
 * @author -Nemesiss-
 */
public class L2ExtractableProductItem
{
	private final int id;
	private final int amount;
	private final int chance;
	
	public L2ExtractableProductItem(final int id, final int ammount, final int chance)
	{
		this.id = id;
		amount = ammount;
		this.chance = chance;
	}
	
	public int getId()
	{
		return id;
	}
	
	public int getAmmount()
	{
		return amount;
	}
	
	public int getChance()
	{
		return chance;
	}
}
