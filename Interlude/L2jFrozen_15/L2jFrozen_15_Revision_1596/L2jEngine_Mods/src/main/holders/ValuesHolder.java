package main.holders;

/**
 * @author fissban
 */
public class ValuesHolder
{
	private final String mod;
	private final String event;
	private DataValueHolder value;
	
	public ValuesHolder(String mod, String event, String value)
	{
		this.mod = mod;
		this.event = event;
		this.value = new DataValueHolder(value);
	}
	
	public String getEvent()
	{
		return event;
	}
	
	public String getMod()
	{
		return mod;
	}
	
	public DataValueHolder getValue()
	{
		return value;
	}
	
	public void setValue(String value)
	{
		this.value = new DataValueHolder(value);
	}
}
