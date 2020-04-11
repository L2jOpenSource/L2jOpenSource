package net.sf.l2j.gameserver.model.item;

/**
 * A container used by monster drops.<br>
 * <br>
 * The chance is exprimed as 1.000.000 to handle 4 point accuracy digits (100.0000%).
 */
public class DropData
{
	public static final int MAX_CHANCE = 1000000;
	
	private final int _itemId;
	private final int _minDrop;
	private final int _maxDrop;
	private final int _chance;
	
	public DropData(int itemId, int minDrop, int maxDrop, int chance)
	{
		_itemId = itemId;
		_minDrop = minDrop;
		_maxDrop = maxDrop;
		_chance = chance;
	}
	
	@Override
	public String toString()
	{
		return "DropData =[ItemID: " + _itemId + " Min: " + _minDrop + " Max: " + _maxDrop + " Chance: " + (_chance / 10000.0) + "%]";
	}
	
	/**
	 * @return the id of the dropped item.
	 */
	public int getItemId()
	{
		return _itemId;
	}
	
	/**
	 * @return the minimum quantity of dropped items.
	 */
	public int getMinDrop()
	{
		return _minDrop;
	}
	
	/**
	 * @return the maximum quantity of dropped items.
	 */
	public int getMaxDrop()
	{
		return _maxDrop;
	}
	
	/**
	 * @return the chance to have a drop, under a 1.000.000 chance.
	 */
	public int getChance()
	{
		return _chance;
	}
}