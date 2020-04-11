package main.holders;

import main.enums.ExpSpType;

/**
 * @author fissban
 */
public class ExpSpBonusHolder
{
	private final ExpSpType type;
	private final double bonus;
	
	public ExpSpBonusHolder(ExpSpType type, int bonus)
	{
		this.type = type;
		this.bonus = bonus / 100;
	}
	
	public ExpSpType getType()
	{
		return type;
	}
	
	public double getBonus()
	{
		return bonus;
	}
	
}
