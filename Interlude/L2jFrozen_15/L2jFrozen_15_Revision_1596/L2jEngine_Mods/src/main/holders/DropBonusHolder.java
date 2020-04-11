package main.holders;

/**
 * @author fissban
 */
public class DropBonusHolder
{
	private double amountBonus = 1.0;
	private double chanceBonus = 1.0;
	
	public DropBonusHolder()
	{
		//
	}
	
	/**
	 * 100 -> normal drop<br>
	 * 110 -> 10% bonus
	 * @param amount
	 */
	public void increaseAmountBonus(double amount)
	{
		amountBonus += amount - 1;
	}
	
	/**
	 * 1.0 -> normal drop<br>
	 * 1.1 -> 10% bonus
	 * @param amount
	 */
	public void increaseChanceBonus(double chance)
	{
		chanceBonus += chance - 1;
	}
	
	public double getAmountBonus()
	{
		return amountBonus;
	}
	
	public double getChanceBonus()
	{
		return chanceBonus;
	}
}
