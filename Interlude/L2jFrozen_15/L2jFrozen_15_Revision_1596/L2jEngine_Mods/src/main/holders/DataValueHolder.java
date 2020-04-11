package main.holders;

/**
 * @author fissban
 */
public class DataValueHolder
{
	private final String value;
	
	public DataValueHolder(String value)
	{
		this.value = value;
	}
	
	public String getString()
	{
		return value;
	}
	
	public Integer getInt()
	{
		if (value != null)
		{
			return Integer.parseInt(value);
		}
		return 0;
	}
	
	public Long getLong()
	{
		if (value != null)
		{
			return Long.parseLong(value);
		}
		return 0L;
	}
}
