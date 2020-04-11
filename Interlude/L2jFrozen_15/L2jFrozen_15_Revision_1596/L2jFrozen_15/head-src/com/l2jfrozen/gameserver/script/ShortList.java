package com.l2jfrozen.gameserver.script;

/**
 * @author -Nemesiss-
 */
public class ShortList
{
	public static short[] parse(final String range)
	{
		if (range.contains("-"))
		{
			return getShortList(range.split("-"));
		}
		else if (range.contains(","))
		{
			return getShortList(range.split(","));
		}
		
		final short[] list =
		{
			getShort(range)
		};
		return list;
	}
	
	private static short getShort(final String number)
	{
		return Short.parseShort(number);
	}
	
	private static short[] getShortList(final String[] numbers)
	{
		final short[] list = new short[numbers.length];
		for (int i = 0; i < list.length; i++)
		{
			list[i] = getShort(numbers[i]);
		}
		return list;
	}
}
