package net.sf.l2j.gameserver.model.holder;

/**
 * A container extending {@link IntIntHolder} used for schemes buffer.
 */
public final class BuffSkillHolder extends IntIntHolder
{
	private final int _price;
	private final String _type;
	private final String _description;
	
	public BuffSkillHolder(int id, int level, int price, String type, String description)
	{
		super(id, level);
		
		_price = price;
		_type = type;
		_description = description;
	}
	
	@Override
	public String toString()
	{
		return "BuffSkillHolder [id=" + getId() + " value=" + getValue() + " price=" + _price + " type=" + _type + " desc=" + _description + "]";
	}
	
	public final int getPrice()
	{
		return _price;
	}
	
	public final String getType()
	{
		return _type;
	}
	
	public final String getDescription()
	{
		return _description;
	}
}