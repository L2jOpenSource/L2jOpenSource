package main.util.builders.html;

/**
 * @author fissban
 */
public class HtmlBuilder
{
	public enum HtmlType
	{
		COMUNITY(12270),
		HTML(8191);
		
		int lenght;
		
		HtmlType(int lenght)
		{
			this.lenght = lenght;
		}
		
		public int getMaxValue()
		{
			return lenght;
		}
	}
	
	private final StringBuilder html;
	private final HtmlType type;
	
	/**
	 * Constructor
	 * @param size
	 */
	public HtmlBuilder(HtmlType type)
	{
		html = new StringBuilder(type.getMaxValue());
		this.type = type;
	}
	
	/**
	 * Constructor
	 */
	public HtmlBuilder()
	{
		html = new StringBuilder();
		type = HtmlType.HTML;
	}
	
	/**
	 * @param string
	 */
	public void append(String string)
	{
		html.append(string);
		
		if (html.length() >= type.getMaxValue())
		{
			System.out.println("Warning html is too long! ----> " + string);
		}
	}
	
	/**
	 * @param obj
	 */
	public void append(Object... obj)
	{
		for (Object o : obj)
		{
			html.append(o);
		}
	}
	
	@Override
	public String toString()
	{
		if (html.length() >= type.getMaxValue())
		{
			System.out.println("Warning html is too long! -> " + html.length());
			return "<html><body><br>Html was too long.</body></html>";
		}
		return html.toString();
	}
}
